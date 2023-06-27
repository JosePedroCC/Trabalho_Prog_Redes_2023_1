package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JTextPane;
import java.awt.FlowLayout;
import javax.swing.JDesktopPane;
import java.awt.Component;
import java.awt.Container;

import javax.swing.Box;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JSplitPane;
import javax.swing.JLayeredPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.CardLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import utilidades.Comandos;

import javax.swing.event.ChangeEvent;

@SuppressWarnings({ "serial", "unused" })
public class GUICliente extends JFrame {
	
	private Container frame;
	private TCPClient client;
	private CardLayout cardLayout;
// Telas
	private JPanel login;
	private JPanel shell;
// Campos da Tela de Login
	private JTextField txtLogin;
	private JTextField campoLogin;
	private JTextField txtSenha;
	protected JPasswordField campoSenha;
	private JTextField txtAvisoLogin;
	private JTextField txtIP;
	private JTextField campoIP;
	private JButton btnCadastrar;
	private JButton btnEntrar;
// Campos da Tela da shell
	private JTextField campoPrompt;
	protected JTextComponent campoResultado;
	private List<String> historico = new LinkedList<>();
	private int i = 0;

	/**
	 * Launch the application.
	 */	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUICliente tela = new GUICliente();
					tela.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUICliente() {
		cardLayout = new CardLayout(0, 0);
		frame = getContentPane();
		setBackground(new Color(0, 0, 128));
		frame.setLayout(cardLayout);
		setBounds(100, 100, 960, 540);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
/************************************************************************************************************************************************************/
// Janela de Login

		login = new JPanel();
		login.setBackground(new Color(0, 0, 128));
		frame.add(login, "login");

		txtLogin = new JTextField();
		txtLogin.setEditable(false);
		txtLogin.setBackground(new Color(0, 0, 128));
		txtLogin.setForeground(new Color(255, 255, 255));
		txtLogin.setFont(new Font("Tahoma", Font.BOLD, 25));
		txtLogin.setText("LOGIN:");
		txtLogin.setColumns(10);

		campoLogin = new JTextField();
		campoLogin.setColumns(15);
		campoLogin.setBackground(new Color(0, 0, 128));
		campoLogin.setForeground(new Color(255, 255, 255));
		campoLogin.setFont(new Font("Tahoma", Font.BOLD, 25));
		
		txtAvisoLogin = new JTextField();
		txtAvisoLogin.setHorizontalAlignment(SwingConstants.CENTER);
		txtAvisoLogin.setEnabled(false);
		txtAvisoLogin.setForeground(Color.WHITE);
		txtAvisoLogin.setFont(new Font("Tahoma", Font.BOLD, 22));
		txtAvisoLogin.setEditable(false);
		txtAvisoLogin.setColumns(10);
		txtAvisoLogin.setBackground(new Color(0, 0, 128));
		
		txtSenha = new JTextField();
		txtSenha.setText("SENHA:");
		txtSenha.setForeground(Color.WHITE);
		txtSenha.setFont(new Font("Tahoma", Font.BOLD, 25));
		txtSenha.setEditable(false);
		txtSenha.setColumns(10);
		txtSenha.setBackground(new Color(0, 0, 128));
		
		campoSenha = new JPasswordField();
		campoSenha.setEchoChar('●');
		campoSenha.setFont(new Font("Tahoma", Font.BOLD, 25));
		campoSenha.setForeground(Color.WHITE);
		campoSenha.setBackground(new Color(0, 0, 128));
		
		JCheckBox chckbxMostrar = new JCheckBox("MOSTRAR?");
		chckbxMostrar.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxMostrar.setBackground(new Color(0, 0, 128));
		chckbxMostrar.setForeground(Color.WHITE);
		chckbxMostrar.setFont(new Font("Tahoma", Font.BOLD, 15));
		chckbxMostrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxMostrar.isSelected()) {
					campoSenha.setEchoChar((char) 0);
				}
				else {
					campoSenha.setEchoChar('●');
				}
			}
		});
				
		txtIP = new JTextField();
		txtIP.setText("IP:");
		txtIP.setForeground(Color.WHITE);
		txtIP.setFont(new Font("Tahoma", Font.BOLD, 25));
		txtIP.setEditable(false);
		txtIP.setColumns(10);
		txtIP.setBackground(new Color(0, 0, 128));
		
		campoIP = new JTextField();
		campoIP.setText("localhost");
		campoIP.setForeground(Color.WHITE);
		campoIP.setFont(new Font("Tahoma", Font.BOLD, 25));
		campoIP.setColumns(15);
		campoIP.setBackground(new Color(0, 0, 128));
		
		btnEntrar = new JButton("ENTRAR");
		btnEntrar.setBackground(Color.DARK_GRAY);
		btnEntrar.setForeground(new Color(255, 255, 255));
		btnEntrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				char[] passwordChars = campoSenha.getPassword();
                String password = new String(passwordChars);
        
                if (campoLogin.getText().isBlank() || password.isBlank() || campoIP.getText().isBlank()) {
					txtAvisoLogin.setText("Todos campos devem estar preenchidos!");
				}
				else {
	                try {
	                	client = new TCPClient(campoIP.getText());
		                if(client.login(campoLogin.getText(), password).equals("S")) {
		                	txtAvisoLogin.setText("");
		                	cardLayout.show(getContentPane(), "shell");	
						}
		                else {
		                	txtAvisoLogin.setText("Impossivel logar!");
		                }	
	                }catch (Exception e1) {
	                	txtAvisoLogin.setText("Server offline!");
					}
				}
			}
		});
		
		btnCadastrar = new JButton("CADASTRAR");
		btnCadastrar.setForeground(Color.WHITE);
		btnCadastrar.setBackground(Color.DARK_GRAY);
		btnCadastrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				char[] passwordChars = campoSenha.getPassword();
                String password = new String(passwordChars);
				client = new TCPClient(campoIP.getText());
				if (campoLogin.getText().isBlank() || password.isBlank() || campoIP.getText().isBlank()) {
					txtAvisoLogin.setText("Todos campos devem estar preenchidos!");
				}
				else {
					try {
						if(client.cadastrar(campoLogin.getText(), password).equals("S")) {
							txtAvisoLogin.setText("Cadastrado!");
						}
						else {
							txtAvisoLogin.setText("Impossivel Cadastrar!");
		                }                
					}catch (Exception e1) {
	                	txtAvisoLogin.setText("Server offline!");
					}
				}
			}
		});
		
		GroupLayout gl_login = new GroupLayout(login);
		gl_login.setHorizontalGroup(
			gl_login.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_login.createSequentialGroup()
					.addGap(85)
					.addGroup(gl_login.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(txtIP, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(campoIP, GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE))
						.addComponent(campoLogin, GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(txtLogin, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(txtAvisoLogin, GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE))
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(txtSenha, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 582, Short.MAX_VALUE)
							.addComponent(chckbxMostrar, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(btnEntrar, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
							.addGap(40)
							.addComponent(btnCadastrar, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(campoSenha, GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGap(62))
		);
		gl_login.setVerticalGroup(
			gl_login.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_login.createSequentialGroup()
					.addGap(41)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtLogin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtAvisoLogin, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(campoLogin, GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
					.addGap(16)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxMostrar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(txtSenha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(campoSenha, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
					.addGap(26)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(campoIP, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
					.addGap(30)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnEntrar, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
						.addComponent(btnCadastrar, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))
					.addGap(39))
		);
		login.setLayout(gl_login);

/******************************************************************************************************************************************************************************/
// Janela da shell
		
		shell = new JPanel();
		shell.setBackground(new Color(0, 0, 128));
		frame.add(shell, "shell");
		
		JScrollPane scrollPane = new JScrollPane();
		
		campoResultado = new JTextPane();
		campoResultado.setBackground(new Color(0, 0, 128));
		campoResultado.setForeground(Color.WHITE);
		campoResultado.setFont(new Font("Tahoma", Font.BOLD, 15));
		campoResultado.setEditable(false);
		scrollPane.setViewportView(campoResultado);
		
		campoPrompt = new JTextField();
		campoPrompt.setBackground(new Color(0, 0, 128));
		campoPrompt.setForeground(Color.WHITE);
		campoPrompt.setFont(new Font("Tahoma", Font.BOLD, 25));
		campoPrompt.setColumns(10);
		campoPrompt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					String msg = campoPrompt.getText();					
					
					if(!msg.isBlank()) {
						historico.add(0,msg);
						if(msg.equals(Comandos.CLEAR.getLinhaDeComando())) {
							campoResultado.setText("");
							
						}
						else if(msg.equals(Comandos.EXIT.getLinhaDeComando())) {
							try {
								client.enviaMensagem(msg);
								cardLayout.show(getContentPane(), "login");
								campoPrompt.setText("");
								campoResultado.setText("");
								
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						else if(msg.split(" ")[0].equals(Comandos.CMD.getLinhaDeComando())) {
							try {
								client.bash(msg, campoResultado);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						else {
							try {
								String temp = campoResultado.getText();
								campoResultado.setText(temp + "\n" + client.enviaMensagem(msg));
							} catch (UnknownHostException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						campoPrompt.setText("");
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_UP) {
					try {
						campoPrompt.setText(historico.get(i));
						i++;
					} catch (Exception e2) {
						i = historico.size();
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					try {
						campoPrompt.setText(historico.get(i));
						i--;
					} catch (Exception e2) {
						i = 0;
					}
				}
			}
			
		});
		
		
		GroupLayout gl_shell = new GroupLayout(shell);
		gl_shell.setHorizontalGroup(
			gl_shell.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_shell.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_shell.createParallelGroup(Alignment.TRAILING)
						.addComponent(campoPrompt, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 926, Short.MAX_VALUE)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 926, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_shell.setVerticalGroup(
			gl_shell.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_shell.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(campoPrompt, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		shell.setLayout(gl_shell);

		cardLayout.show(getContentPane(), "login");
	}
}
