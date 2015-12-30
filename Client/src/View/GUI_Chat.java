package View;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import Controller.Client;
import Controller.Cypher;

import javax.swing.JLabel;

import java.awt.Panel;

import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.SwingConstants;
import javax.swing.JScrollPane;

public class GUI_Chat {

	private JFrame chatFrame;
	private Client socketClient;
	@SuppressWarnings("unused")
	private Cypher cypher;
	private JTextField txtWriteHere;
	private JLabel lblName;
	private JLabel chatPanelText;
	private JLabel clientListPanelText;
	private JScrollPane chatPanelScroll;
	private JScrollPane clientListPanelScroll;
//	private UndeterminedType cypherType;
	
	public GUI_Chat( Client p_clientSocket, Cypher p_cypher ) {

		this.initialize( p_clientSocket, p_cypher );
		this.createWindow();
	}

	private void initialize( final Client p_clientSocket, final Cypher p_cypher ) {
		this.socketClient = p_clientSocket;
		this.cypher       = p_cypher;
	}
	
	private void createWindow() {
		this.chatFrame = new JFrame();
		this.chatFrame.setResizable( false );
		this.chatFrame.setTitle( "Miaou" );
		this.chatFrame.setBounds( 100, 100, 451, 298 );
		this.chatFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		this.chatFrame.getContentPane().setLayout( null );
		
		this.chatFrame.addWindowListener( new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing( java.awt.event.WindowEvent windowEvent ) {
		    	socketClient.disconnect();
		    }
		});

		this.lblName = new JLabel();
		this.lblName.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
		this.lblName.setBounds( 12, 13, 77, 44 );
		this.chatFrame.getContentPane().add( this.lblName );
		
		Panel clientListPanel = new Panel();
		clientListPanel.setBackground( SystemColor.controlHighlight );
		clientListPanel.setBounds( 12, 63, 77, 147 );
		this.chatFrame.getContentPane().add( clientListPanel );
		clientListPanel.setLayout( null );
		
		this.clientListPanelText = new JLabel( "<html></html>" );
		clientListPanelText.setVerticalAlignment( SwingConstants.TOP );
		clientListPanelText.setBounds( 12, 13, 53, 121 );
		clientListPanel.add( clientListPanelText );
		
		this.clientListPanelScroll = new JScrollPane( this.clientListPanelText );
		this.clientListPanelScroll.setBounds( 0, 0, 77, 147 );
		clientListPanel.add( this.clientListPanelScroll );
		
		this.txtWriteHere = new JTextField();
		this.txtWriteHere.setToolTipText( "Write here." );
		this.txtWriteHere.setBounds( 12, 218, 299, 24 );
		this.chatFrame.getContentPane().add( txtWriteHere );
		this.txtWriteHere.setColumns( 10 );
		this.txtWriteHere.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				askToSendMessage();
			}
		});
		
		JButton sendButton = new JButton( "Send" );
		sendButton.setBounds( 323, 217, 97, 25 );
		sendButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent arg0 ) {
				askToSendMessage();
			}
		});
		this.chatFrame.getRootPane().setDefaultButton( sendButton ); // To send on pressing "Enter" where the button is selected
		this.chatFrame.getContentPane().add( sendButton );
		
		Panel chatPanel = new Panel();
		chatPanel.setBackground( SystemColor.controlHighlight );
		chatPanel.setBounds( 102, 13, 318, 197 );
		this.chatFrame.getContentPane().add( chatPanel );
		chatPanel.setLayout( null );
		
		this.chatPanelText = new JLabel( "<html></html>" );
		this.chatPanelText.setHorizontalAlignment( SwingConstants.LEFT );
		this.chatPanelText.setVerticalAlignment( SwingConstants.TOP );
		this.chatPanelText.setBounds( 12, 17, 294, 167 );
		chatPanel.add( chatPanelText );
		this.chatPanelText.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
		
		this.chatPanelScroll = new JScrollPane( this.chatPanelText );
		this.chatPanelScroll.setBounds( 0, 0, 318, 197 );
		chatPanel.add(chatPanelScroll);		
		this.chatFrame.setVisible( true );
	}
	
	private void askToSendMessage() {
		String inputContainer = this.txtWriteHere.getText();
		
		if ( inputContainer != null && !inputContainer.isEmpty() ) {
			this.socketClient.sendMessage( "[newMessage]", inputContainer );
			this.txtWriteHere.setText( "" );
		}
	}
	
	private void scrollToMaximum( final JScrollBar p_verticalScrollBar ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				p_verticalScrollBar.setValue( p_verticalScrollBar.getMaximum() );
			}
		});
	}
	
	public void renderMessage( final String p_text ) {
		try {
			String time     = p_text.substring( 0, 5 ),
				   message  = p_text.split( "\"" )[1],
				   nickname = p_text.split( " " )[2];

			this.chatPanelText.setText( this.chatPanelText.getText().replace( "</html>", "" )
										+ nickname + " [" 
										+ time     + "] : "
										+ message  + "<br/></html>"
			);
			this.scrollToMaximum( chatPanelScroll.getVerticalScrollBar() );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void renderConnectionOrDisconnection( final String p_connectionOrDisconnection, final String p_clientName ) {
		String clientName = "<strong style=\"color:#6A6A6A\">" + p_clientName + " ";
		
		if ( p_connectionOrDisconnection == "disconnection" ) {
			clientName += "dis";
		}
		else if ( p_connectionOrDisconnection != "connection" ) {
			System.err.println( "Method \"renderConnectionOrDisconnection\" called with incorrect value for parameter \"p_connectionOrDisconnection\"." );
			return;
		}
		
		clientName += "connected.</strong>";
		this.chatPanelText.setText( this.chatPanelText.getText().replace( "</html>", "" ) + clientName + "<br/></html>" );
		this.scrollToMaximum( this.chatPanelScroll.getVerticalScrollBar() );
	}
	
	public void renderUpdatedClientList( final String p_clientNameList ) {
		if ( p_clientNameList.contains( "_$*° " ) ) {
			StringTokenizer stringTokenizer = new StringTokenizer( p_clientNameList, "_$*° " );
			while ( stringTokenizer.hasMoreTokens() ) {
				this.renderClient( "add" , stringTokenizer.nextToken() );
			}
		}
		else { 
			System.err.println( "Warning : method \"renderUpdatedClientList\" called with incorrect value for parameter \"p_clientNameList\"." );
		}
		
		this.scrollToMaximum( this.clientListPanelScroll.getVerticalScrollBar() );
	}
	
	public void renderClient( final String p_addOrRemove, final String p_clientName ) {
		if ( p_addOrRemove == "add" ) {
			this.clientListPanelText.setText( this.clientListPanelText.getText().replaceFirst( "</html>", p_clientName + "<br/></html>" ) );
		}
		else if ( p_addOrRemove == "remove" ) {
			this.clientListPanelText.setText( this.clientListPanelText.getText().replaceFirst( p_clientName + "<br/>", "" ) );
		}
		else {
			System.err.println( "Warning : method \"renderClient\" called with incorrect value for parameter \"p_addOrRemove\"." );
			return;
		}
		
		this.scrollToMaximum( clientListPanelScroll.getVerticalScrollBar() );
	}
	
	public void setNameLabel( final String p_text ) {
		this.lblName.setText( p_text );
	}
}