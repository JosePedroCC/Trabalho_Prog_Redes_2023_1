package server;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import utilidades.Comandos;

@SuppressWarnings({ "resource", "unused" })
public class TCPServer {
	
	static List<Connection> connections = new ArrayList<>();

	public static void main(String args[]) {
	// Diretório raiz do servidor será onde estiver o TCPServer.java.
	// Adiciona a uma String que contem o nome do diretorio, mais a pasta do servidor.
		Path path = Paths.get("").toAbsolutePath();
		String diretorioRaiz = path.toString();
		diretorioRaiz = diretorioRaiz.replace((char) 92, '/').replace("/src/server", "");
		diretorioRaiz = diretorioRaiz+"/src/server/DiretorioServidor";
		
	// Cria objeto path da localizacao do diretorio do servidor
		path = Paths.get(diretorioRaiz);
		try{
		// Se não existe a pasta do servidor, ela é criada.
			if(Files.notExists(path))
				Files.createDirectory(path);
		// Porta do servidor
			int serverPort = 7896;
		// Aloca socket do servidor no sistema.
			ServerSocket listenSocket = new ServerSocket(serverPort);
		// Infinitamente fica esperando por requisições do cliente.
        	while (true) {
        	// Quando cliente envia uma requisição
            	Socket clientSocket = listenSocket.accept();
      		// Cria uma conexão entre o Servidor e cliente.
            	Connection connection = new Connection(clientSocket, diretorioRaiz);
            // Adiciona a conexão à lista de conexões caso ela nao tenha sido "encerrada";
            	if(!connection.isEncerrada())
            		connections.add(connection);
        }
		// Caso o socket esteja oculpado. 
		} catch(IOException e) {System.out.println("Listen socket:"+e.getMessage());}
/**/		
	}
	
// Função para obter a lista de conexões com os clientes
	public static Connection getConnectionByLogin(Connection c) {
    // Percorre a lista de conexões e retorna a conexão correspondente ao cliente
        for (Connection connection : connections) {
            if (connection.getClientUsuarioHash() == (c.getClientUsuarioHash())) {
                return connection;
            }
        }
	// Se a conexão não for encontrada
        return null;
    }
}
class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	private Diretorio raiz;
	private boolean encerrada;
	
	public Connection (Socket ClientSocket, String localizacaoRaiz) {
		try {

			this.clientSocket = ClientSocket;
			this.in = new DataInputStream( clientSocket.getInputStream());
			this.out = new DataOutputStream( clientSocket.getOutputStream());
			this.raiz = new Diretorio(localizacaoRaiz);
			this.encerrada = false;
			this.start();
			
		} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
	}

	//retorna hash do usuario armazenado em Diretorio.
	public int getClientUsuarioHash() {
        return raiz.getUsuarioHash();
    }
    
	// retorna se conexao foi encerrada pelo client
	public boolean isEncerrada() {
		return encerrada;
	}

	public void run(){
		try {
		
		// recebe informacoes para "login"
			String data = in.readUTF();
		// divide em diferentes strings para diferenciar cadastro de login e os dados de login
			String login[] = data.split("<>");
			
		// Se for para logar
			if (login[0].equals(Comandos.LOGIN.getLinhaDeComando())){
			// chama metodo de login da classe diretorio e armazena em retorno
				String retorno = raiz.login(login[1]);
			// Se a ultima parte da mensagem foi "TRUE" entao cliente espera um retorno;	
				if(login[2].equals("TRUE")) {
					out.writeUTF(retorno);
					return;
				}
			}
		// Caso for para cadastramento de usuario
			else if(login[0].equals(Comandos.CADASTRAR.getLinhaDeComando())){
			// Chama metodo da classe Diretorio responsavel pelo cadastramento e informa se foi possivel
				out.writeUTF(raiz.cadastro(login[1]));
				return;
			}
		// Somente para DEBUG
			else {
				System.out.println("Fiz algo errado!");
				return;
			}
		// Com as informacoes de login recebidas, verifica se ja ouve outra conexao com o servidor que nao tenha sido encerrada.
			Connection connection = TCPServer.getConnectionByLogin(this);
		// Se existiu
	        if (connection != null) {
	        // objeto Diretorio da conexao anterior eh armazenado na conexao atual
	        	this.raiz = connection.raiz;
	        // E objeto Connection eh removido da lista
	        	TCPServer.connections.remove(connection);
	        }
			
    	// recebe mensagem do Cliente com os comandos.
			data = in.readUTF();
		// Divide em strings, para separar comando (send, del, ...)
		// do resto das informações vindas na mensagem.
			String comandos[] = data.split(" ",2);
			
		// Se comando ser igual a CRIAR UM DIRETÓRIO.
			if (comandos[0].equals(Comandos.CRIAR.getLinhaDeComando())){
			// Chama função e retorna ao usuario se foi possivel criar o Diretorio.
				out.writeUTF(raiz.criarDir(comandos[1]));
			}
		// Se comando for igual a DELETAR DIRETÓRIO OU ARQUIVO.
			else if (comandos[0].equals(Comandos.DELETAR.getLinhaDeComando())){
			// Chama função e verifica se foi possivel deletar o Diretorio ou arquivo.
				out.writeUTF(raiz.deletar(comandos[1]));	
			}
		// Se comando for igual a LISTAR CONTEÚDO.
			else if (comandos[0].equals(Comandos.LISTAR.getLinhaDeComando())){
				out.writeUTF(raiz.listar());
			}
		// Se comando for igual a ENVIAR ARQUIVO.
			else if (comandos[0].equals(Comandos.UPLOAD.getLinhaDeComando())){
			// Recebe NOME do arquivo.
				String arquivo[] = in.readUTF().split("<>");
			// Chama função que recebe o nome do arquivo, o tamanho e armazena no diretorio.
			// Devolve mensagem informando se transferiu corretamente.
				out.writeUTF(raiz.recebeArquivo(arquivo[0], in, Long.parseUnsignedLong(arquivo[1])));
			}
			else if(comandos[0].equals(Comandos.DOWNLOAD.getLinhaDeComando())){
			// Recebe caminho para o arquivo.
				String arquivo = in.readUTF();
			// Chama função que recebe o caminho para o arquivo e envia do diretorio.
			// Devolve mensagem informando se transferiu corretamente.
				out.writeUTF(raiz.enviaArquivo(arquivo, out));
			}
		// Navega entre diretorios.
			else if(comandos[0].equals(Comandos.NAVEGAR.getLinhaDeComando())){
				out.writeUTF(raiz.trocarDiretorio(comandos[1]));
			}
		// Encerra o servidor de arquivos para o cliente.
			else if (comandos[0].equals(Comandos.EXIT.getLinhaDeComando())) {
				raiz.exit();
				encerrada = true;
				out.writeUTF("Saiu");
				//System.exit(0);
			}
		// Caso comando seja HELP, informa os comandos possiveis.
			else if (comandos[0].equals(Comandos.HELP.getLinhaDeComando())) {
				out.writeUTF(
					"\n->" + Comandos.CRIAR.getLinhaDeComando() + " - Criação de diretórios."+
					"\n->" + Comandos.DELETAR.getLinhaDeComando() + " - deletar diretório ou arquivo."+
					"\n->" + Comandos.LISTAR.getLinhaDeComando() + " - listar conteúdo do diretório atual do servidor."+
					"\n->" + Comandos.UPLOAD.getLinhaDeComando() + " - enviar aquivo para o servidor no diretório atual do servidor."+
					"\n->" + Comandos.DOWNLOAD.getLinhaDeComando() + " - recebe aquivo do servidor na pasta Downloads."+
					"\n->" + Comandos.NAVEGAR.getLinhaDeComando() + " - navegar entre pastas do servidor de arquivos."+
					"\n->" + Comandos.CMD.getLinhaDeComando() + " - executa sequência de comandos de arquivo .bash"+
					"\n->" + Comandos.CLEAR.getLinhaDeComando() + " - limpa terminal."+					
					"\n->" + Comandos.EXIT.getLinhaDeComando() + " - encerrar conexão com o servidor."+
					"\n->" + Comandos.HELP.getLinhaDeComando() + " - mostra opções de comandos do servidor."+
					"\n\n");
			}
			else{
				// Se não existe tal comando, informa
				out.writeUTF("Comando errado! Nao existe '"+data+"'!");
			}
		} catch (EOFException e){
			try {
				// Se faltou comandos, por exemplo, upload sem arquivo
				out.writeUTF("Faltou comandos!");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch(IOException e) {System.out.println("readline:"+e.getMessage());
		} finally{ try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
}

//Classe Diretorio que armazena informações de Path e execulta as funções.
class Diretorio{
//Armazena o diretório RAIZ onde o sistema de arquivos é armazenado.
//( Como se fosse o C: )
	private String nomeDiretorioRaiz;
// Usuario para implementacao de multiplos terminais.
	private String usuario;
//Armazena diretorio acessado no momento atual.
//Para implementação de navegação entre diretórios.
	private String diretorioAcessado;

	public Diretorio(String nomeDiretorio){
		nomeDiretorioRaiz = nomeDiretorio;
		usuario = "";
		diretorioAcessado = "";
	}
	
// Retorna o usuario criptografado
	public int getUsuarioHash() {
		return usuario.hashCode();
	}

// Metodo de login que recebe o usuario já criptografado.
	public String login(String usuarioHash) {
		usuario = usuarioHash;
		if(Files.exists(Paths.get(nomeDiretorioRaiz,usuario))) {
			nomeDiretorioRaiz = nomeDiretorioRaiz+"/"+usuario;
			return "S";
		}
		usuario = "";
		return "N";
	}

// Metodo de cadastro para cadastrar usuario e senha ou retorna erro caso nao seja possivel
	public String cadastro(String usuarioHash) {
		Path path = Paths.get(nomeDiretorioRaiz,usuarioHash);
		if(Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "S";
		}
		return "N";
	}
// "Encerra" conexao de usuario.
	public void exit() {
		nomeDiretorioRaiz = nomeDiretorioRaiz.replace(("/"+usuario),"");
		usuario = "";
	}

//Funcão de criar diretório
//Comparavel ao comando 'mkdir' do ubuntu
	public String criarDir(String dirNome) throws IOException {
		Path path = Paths.get(diretorio(), dirNome);
	// Verifica se não existe o diretório, se já existir não faz nada
     	if (Files.notExists(path)) {
     // Cria o diretório, caso dirNome seja composto por varios diretórios separados por /
     // cria todos os diretórios até chegar ao ultimo.
        	Files.createDirectories(path);
        	return "Diretório "+dirNome+" foi criado com sucesso!";
 		}
 		else {
			return "Diretório "+dirNome+" já existia!";            
    	}
	}

//Funcão de trocar diretório
//Comparavel ao comando 'cd' do ubuntu
	public String trocarDiretorio(String linhaDeComando) {
		
		String temp = diretorioAcessado;
		String[] comandos = linhaDeComando.split("/");
		for (String comando : comandos) {	
			if (comando.equals("..")){
				if(diretorioAcessado.isBlank()){
					diretorioAcessado = temp;
					return "Diretório não pode ser acessado!";
				}
				else{
					String aux[] = diretorioAcessado.split("/");
					diretorioAcessado = diretorioAcessado.substring(0, 
							(diretorioAcessado.length() - aux[aux.length-1].length() - 1)) ;
					// System.out.println(diretorioAcessado);
					continue;
				}
			}
			else if (comando.equals(".")){
				diretorioAcessado = temp;
				return "Comando inexistente!";
			}
			else{
				
				if (Files.exists(Paths.get(diretorio(), comando))){
					diretorioAcessado += ("/"+comando);
					continue;
				}
				else {
					diretorioAcessado = temp;
					return "Diretório inexistente!";
				}
			}
		}
		return "Diretório mudou!\nNovo diretório: "+diretorioAtual();
	}

//Funcão de deletar diretório e arquivos.
//O que vai diferenciar arquivo e diretório é o Path.
//Comparavel ao comando 'del' do ubuntu
	public String deletar(String pathName) throws IOException, DirectoryNotEmptyException {
	Path path = Paths.get(diretorio(),pathName);
	try {
		if (possivelAcessar(pathName)) {
			Files.deleteIfExists(path);
			return "Deletado com sucesso!";
		}
		else {
			return "Não foi possivel deletar, não existe!";
		}
	} catch (DirectoryNotEmptyException e) {
		return "Diretório não vazio!";
	} catch (Exception e) {
		return e.getMessage();
	}
	}

//Função Listar que lista o conteudo do diretorio. 
//Comparavel ao comando 'ls' do ubuntu
	public String listar() throws IOException {
		String retorno = diretorioAtual() + "\n\t";
	// Acha o diretorio que verificará o conteudo.
		Path path = Paths.get(diretorio());
	// método list retorna lista de objetos Stream no Pàth 
	// que é transformado em um Object Array 
		Object objectArray[] = Files.list(path).toArray();
	// Com um for de lista, percorremos todo o array transformando em strings
	// e concatenando com um \n para separar.
     	for (Object o : objectArray) {
			String aux[] = o.toString().replace((char) 92, '/').split("/");
     		retorno += aux[aux.length -1];
     		retorno += "\n\t";
     	}
     	retorno += "\n";
     	return retorno;
	}

//Função de recebiemento de um arquivo, e armazenamento no sistema.
	public String recebeArquivo(String fileName, DataInputStream input, long size) throws IOException {
		String existia;
	    Path path = Paths.get(diretorio(), fileName);
	    
	    // Se o arquivo já existe, deleta e cria um novo.
	    if (Files.exists(path)) {
	        Files.delete(path);
	        Files.createFile(path);
	        existia = "Arquivo ja existe! Conteudo sobreEscrito!";
	    }
	    else{
	    	existia = "Arquivo criado e armazenado!";
	    }

	    try (OutputStream fileStream = Files.newOutputStream(path)) {
	        byte[] buffer = new byte[1024];;
	        int bytesRead;
	        int totalBytesRead = 0;
	        
	        // Se arquivo for menor que 1024, diminui tamanho do buffer
	        if (size < 1024 && size > 0) {
	        	buffer = new byte[(int) size];
			}
	        
	        while (totalBytesRead < size && (bytesRead = input.read(buffer)) != -1) {
	            fileStream.write(buffer, 0, bytesRead);
	            totalBytesRead += bytesRead;
	        }
	    }

	    // Retorna se o arquivo já existia ou não.
	    return existia;
	}
	
//Função de envio de um arquivo armazenado no sistema.
	public String enviaArquivo(String caminho, DataOutputStream output) throws IOException {
		caminho = caminho.replace((char) 92, '/');
		try {
			if(possivelAcessar(caminho)) {
			    output.writeUTF("OK!");
			}
			else {
				output.writeUTF("ERRO!");
				return "Caminho para o arquivo inválido!";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return e.getMessage();
		}
		
    	String aux[] = caminho.split("/");
        String nomeDoArquivo = aux[aux.length - 1];
        Path path = Paths.get(diretorio(), caminho);
        long size = Files.size(path);        
        output.writeUTF(nomeDoArquivo + "<>" + Files.size(path));

        try (InputStream fileStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            // Se arquivo for menor que 1024, diminui tamanho do buffer
            if (size < 1024 && size > 0) {
	        	buffer = new byte[(int) size];
			}

            while ((bytesRead = fileStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            
        }
        return ""; 
	}
	
	@SuppressWarnings("unused")
	private String hashCodeMD5(String input) {
		try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(16);

            // Preenche com zeros à esquerda, se necessário
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            return hashText;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
	}
	
	public boolean possivelAcessar(String caminho) throws Exception {
		
		String temp = diretorioAcessado;
		String[] comandos = caminho.split("/");
		for (String comando : comandos) {	
			if (comando.equals("..")){
				if(diretorioAcessado.isBlank()){
					diretorioAcessado = temp;
					throw new Exception("Não é possivel acessar esse Diretório!");
				}
				else{
					String aux[] = diretorioAcessado.split("/");
					diretorioAcessado = diretorioAcessado.substring(0, 
							(diretorioAcessado.length() - aux[aux.length-1].length() - 1)) ;
					// System.out.println(diretorioAcessado);
					continue;
				}
			}
			else if (comando.equals(".")){
				diretorioAcessado = temp;
				throw new Exception("Comando não realiza nada!");
			}
			else {	
				if (Files.exists(Paths.get(diretorio(), comando))){
					diretorioAcessado += ("/"+comando);
					continue;
				}
				else {
					diretorioAcessado = temp;
					return false;
				}
			}
		}
		diretorioAcessado = temp;
		return true;
	}

// retorna diretorio atual do sistema de arquivos
	private String diretorio(){
		return (nomeDiretorioRaiz + "/" + diretorioAcessado) ;
	}

// retorna diretorio atual do sistema de arquivos escondendo o path do diretorio raiz.
	private String diretorioAtual(){
		return ("Server:" + diretorioAcessado) ;
	}
}
