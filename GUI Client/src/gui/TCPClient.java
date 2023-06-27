package gui;

import utilidades.Comandos;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.text.JTextComponent;

import java.math.BigInteger;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//@SuppressWarnings({ "unused" })
public class TCPClient {
// Constantes
	private static final int portaServidor = 7896;
// Variaveis
	private String enderecoServidor;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String userName;
	private String senha;
	
	// Construtor
	public TCPClient(String enderecoServidor){
		setEnderecoServidor(enderecoServidor);
	}
	
	// Método de login, recebe usuário e senha e envia criptografado ao servidor.
	public String login(String userName, String senha) throws ConnectException, UnknownHostException, IOException {	
		this.userName = userName;
		this.senha = senha;
		setSocket();
		out.writeUTF(Comandos.LOGIN.getLinhaDeComando()+"<>"+ usuarioHash(userName, senha) +"<>TRUE");
		return in.readUTF();
	}
	
	// Método de cadastro, recebe usuário e senha e envia criptografado ao servidor.
	public String cadastrar(String userName, String senha) throws ConnectException, UnknownHostException, IOException {
		setSocket();
		out.writeUTF(Comandos.CADASTRAR.getLinhaDeComando()+"<>"+usuarioHash(userName, senha));
		return in.readUTF();
	}
	
	private void setEnderecoServidor(String enderecoServidor) {
		this.enderecoServidor = enderecoServidor;
	}

	private void setSocket() throws UnknownHostException, IOException, ConnectException {
		this.socket = new Socket(enderecoServidor, portaServidor);
		setIn();
		setOut();
	}

	private void setIn() throws IOException {
		this.in = new DataInputStream( socket.getInputStream());
	}

	private void setOut() throws IOException {
		this.out = new DataOutputStream( socket.getOutputStream());
	}
	
	// Função que recebe um nome de arquivo .bash e realiza todas as linhas do arquivo.
	public void bash(String fileName, JTextComponent campoResultado) throws IOException {
		// Deve conter 2 comandos
		String[] comandos = fileName.split(" ");
		if (comandos.length < 2) {
			campoResultado.setText(campoResultado.getText() + "\nNome do arquivo .bash faltando!\n");
			return;
		}
		// Arquivo deve terminar com .bash
		if (!comandos[1].endsWith(".bash")) {
			campoResultado.setText(campoResultado.getText() + "\nArquivo deve terminar com .bash!\n");
			return;
		}
		
		Scanner scanner = new Scanner(Paths.get(comandos[1].strip()));
		String line;
		// Enquanto tiver linha para ler
		while(scanner.hasNextLine()) {
			// Line recebe a linha do arquivo.
			line = scanner.nextLine();
			
			// Printa comentario
			if(line.startsWith("#"))
				campoResultado.setText(campoResultado.getText() + "\nCMD: " + line);
			// Se line for vazia, pula linha
			else if (line.isBlank()) 
				campoResultado.setText(campoResultado.getText() + "\n");	
			// Envia ao servidor e adiciona os resultados.
			else
				campoResultado.setText(campoResultado.getText() + "\nCMD: " + line + "\n" + enviaMensagem(line));
		}
		
	}
	
	// Envia mensagem ao servidor verificando alguns comandos.
	public String enviaMensagem(String mensagem) throws UnknownHostException, IOException, ConnectException {
	    setSocket();
	    out.writeUTF(Comandos.LOGIN.getLinhaDeComando() + "<>" + usuarioHash(userName, senha) + "<>FALSE");
	    String[] comandos = mensagem.split(" ", 2);
	    comandos[0] = comandos[0].strip();
	    
	    // Se comando for para fazer UPLOAD
	    if (comandos[0].equals(Comandos.UPLOAD.getLinhaDeComando())) {
	    	// Deleta ' " ' do caminho
	        comandos[1] = comandos[1].replace("\"", "").strip();
	        Path path = Paths.get(comandos[1]);
	        
	        // Se não existir, retorna.
	        if (Files.notExists(path))
	            return "Arquivo inexistente!";
	        
	        // Descobre nome do arquivo, tamanho e envia ao servidor
	        String aux[] = comandos[1].replace((char) 92, '/').split("/");
	        String nomeDoArquivo = aux[aux.length - 1];
	        out.writeUTF(comandos[0]);
	        int size = (int) Files.size(path);
	        out.writeUTF(nomeDoArquivo + "<>" + Files.size(path));
	        
	        // Começa a ler o arquivo.
	        try (InputStream fileStream = Files.newInputStream(path)) {
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            // Se arquivo for menor que 1024, diminui tamanho do buffer
	            if (size < 1024 && size > 0) {
		        	buffer = new byte[size];
				}
	            
	            while ((bytesRead = fileStream.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	            // fecha arquivo enviado
	            fileStream.close();
	        }
	    // Se comando for para fazer Download
	    }else if(comandos[0].equals(Comandos.DOWNLOAD.getLinhaDeComando())) {
	    	// Envia que servidor realizará um download, e envia nome do arquivo
	    	out.writeUTF(comandos[0]);
	    	out.writeUTF(comandos[1].strip());
	    	
	    	// Se servidor enviar que caminho para arquivo está correto, e arquivo existe
	    	if (in.readUTF().equals("OK!")) {
	    		// Pega path absoluto do arquivo TCPClient, e cria pasta de download no mesmo local.
				String diretorio = Paths.get("").toAbsolutePath().toString();
				diretorio = diretorio.replace((char) 92, '/').replace("/src/gui", "");
				diretorio = diretorio+"/src/gui/Downloads";
				
				// Se pasta Downloads ainda não existe, cria a pasta
			    Path path = Paths.get(diretorio);
			    if (Files.notExists(path)) {
					Files.createDirectories(path);
				}
			    
			    // Recebe informações de nome de arquivo e tamanho do servidor
			    String informacoes = in.readUTF();
			    String files[] = informacoes.split("<>");
			    
			    // Obtem objeto path para onde o arquivo será baixado.
			    path = Paths.get(diretorio,files[0]);
			    // Se o arquivo já existe, deleta e cria um novo.
			    if (Files.exists(path)) {
			        Files.delete(path);
			        Files.createFile(path);
			    }
			    
			    // Começo do recebimento de arquivo.
			    try (OutputStream fileStream = Files.newOutputStream(path)) {
			        byte[] buffer = new byte[1024];
			        int bytesRead;
			        int totalBytesRead = 0;
			        int size = Integer.parseUnsignedInt(files[1]);
			        // Se arquivo for menor que 1024, diminui tamanho do buffer    
			        if (size < 1024 && size > 0) {
			        	buffer = new byte[size];
					}
			        // Enquanto tiver bytes para ler, executa o while
			        while (totalBytesRead < size && (bytesRead = in.read(buffer)) != -1) {
			            fileStream.write(buffer, 0, bytesRead);
			            totalBytesRead += bytesRead;
			        }
			        // fecha arquivo recebido.
			        fileStream.close();
			        int availableBytes = in.available();
		            byte[] bufferLixo = new byte[availableBytes];
		            in.read(bufferLixo); // Lê os bytes restantes no buffer
			    }catch (Exception e) {
					// TODO: handle exception
				}
			    // retorna resposta ao cliente sobre download do arquivo
			    return "Download concluido!";
	    	}
	    }
	    else if (comandos[0].equals(Comandos.EXIT.getLinhaDeComando())) {
	    	// se comando for para sair, envia ao servidor
	        out.writeUTF(comandos[0]);
        }
	    else {
	    	// Nos demais casos apenas envia ao servidor
	        out.writeUTF(mensagem.strip());
	    }
	    // recebe resposta do servidor para retorna a interface grafica
	    return in.readUTF();
	}
	
	// Retorna Code para separacao de usuario e senha ao armazenar
	private String getCode() {
		return "#!#";
	}
	// Retorna usuario e senha "criptografado"
	private String usuarioHash(String username, String senha) {
		return hashCodeMD5(username+getCode()+senha);
	}

	// função de criptografagem que retorna hash MD5 de uma string qualquer
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
}