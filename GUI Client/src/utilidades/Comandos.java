package utilidades;

public enum Comandos {
	CRIAR("mkdir"),
	DELETAR("del"),
	LISTAR("ls"),
	UPLOAD("upload"),
	DOWNLOAD("download"),
	NAVEGAR("cd"),
	CMD("CMD"),
	CLEAR("clear"),
	EXIT("exit"),
	HELP("help"),
	LOGIN("LOGIN"),
	CADASTRAR("CADASTRAR");
	
	private String comando;

	private Comandos(String comando){
		this.comando = comando;
	}
	
	public String getLinhaDeComando() {
		return comando;
	}
}
