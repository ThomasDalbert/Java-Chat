package Controller;

import java.util.StringTokenizer;

import View.GUI_Chat;

public class MessageManager extends Thread {

	String   str;
	GUI_Chat GUI_Chat;
	Client   socketClient;
	
	public MessageManager( final String p_str, final GUI_Chat p_GUI_Chat, final Client p_socketClient ) {
		this.str 	  	  = p_str;
		this.GUI_Chat 	  = p_GUI_Chat;
		this.socketClient = p_socketClient;
	}

	public void run() {
		if ( this.str.startsWith( "[newMessage]" ) ) {
			this.str = this.str.replace( "[newMessage]", "" );
			this.str = this.playSoundIfNeeded();
			this.GUI_Chat.renderMessage( this.str );
		}
		else if ( this.str.startsWith( "[newConnection]" ) ) {
			this.str = this.str.replace( "[newConnection]", "" );
			this.GUI_Chat.renderConnectionOrDisconnection( "connection", this.str );
			this.GUI_Chat.renderClient( "add", this.str );
		}
		else if ( this.str.startsWith( "[newDisconnection]" ) ) {
			this.str = this.str.replace( "[newDisconnection]", "" );
			this.GUI_Chat.renderConnectionOrDisconnection( "disconnection", this.str );
			this.GUI_Chat.renderClient( "remove", this.str );
		}
		else if ( this.str.startsWith( "[clientData]" ) ) {
			this.str = this.str.replace( "[clientData]", "" );
			StringTokenizer stringTokenizer = new StringTokenizer( this.str, "|" );
			this.socketClient.setID( stringTokenizer.nextToken() );
			this.GUI_Chat.renderUpdatedClientList( stringTokenizer.nextToken() );
		}
		this.interrupt();
	}
	
	private String playSoundIfNeeded() {
		StringTokenizer stringTokenizer = new StringTokenizer( this.str, "|" );
		String 			ID 				= stringTokenizer.nextToken();
		
		if ( !ID.equals( this.socketClient.getID() ) ) {
			Music notificationSound = new Music();
			notificationSound.startMusic();
		}
		
		return stringTokenizer.nextToken();
	}
}