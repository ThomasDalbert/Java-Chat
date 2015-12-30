package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import View.GUI_Chat;;

public class Client {

	private Socket client;
	private GUI_Chat GUI_Chat;
	@SuppressWarnings( "unused" )
	private Cypher cypher;
	private String ID,
				   clientName;
	private Timer checkPresenceTimer;
	private short delayBeforeNotifyingPresence = 20;
	private PrintStream output;
	private BufferedReader input;
	
	public void start() {
		if ( this.connectToServer() ) {
			this.initialize();
			this.sendMessage( "[clientName]", this.clientName );
			this.notifyPresenceWhenNeeded();
			String str;

			while ( !this.client.isClosed() ) {
				if ( (str = this.listenToNewMessage()) != null ) {
					MessageManager messageManager = new MessageManager( str, this.GUI_Chat, this );
					messageManager.start();
				}
			}
		}
	}
	
	public void setInstances( final GUI_Chat p_GUI_Chat, final Cypher p_cypher ) {
		this.GUI_Chat = p_GUI_Chat;
		this.cypher   = p_cypher;
	}
	
	private boolean connectToServer() {
		try {
			this.client = new Socket( "srvchat.ddns.net", 8080 );
			return true;
		}
		catch ( Exception e ) {
			System.err.println( "Sorry, can't connect to the server. Either it is inaccessible or your internet connection doesn't work : " + e );
			return false;
		}
	}
	
	private void initialize() {
		try {
			this.output 	= new PrintStream( this.client.getOutputStream() );
			this.input  	= new BufferedReader( new InputStreamReader( this.client.getInputStream() ) );
			this.clientName = InetAddress.getLocalHost().getHostName();
			this.GUI_Chat.setNameLabel( this.clientName );
		}
		catch ( IOException e ) {
			System.err.println( "Problem occured during initialization : " + e );
			e.printStackTrace();
		}
	}
	
	private void notifyPresenceWhenNeeded() {
		this.checkPresenceTimer = new Timer();

		this.checkPresenceTimer.schedule( new TimerTask() {
		    public void run() {
				--delayBeforeNotifyingPresence;
				if ( delayBeforeNotifyingPresence == 0 ) {
					sendMessage( "[stillConnected]", null );
					resetDelayBeforeNotifyingPresence();
				}	
		    }
		 }, 0, 1000 );	
	}
	
	private void resetDelayBeforeNotifyingPresence() {
		this.delayBeforeNotifyingPresence = 20;
	}
	
	private String listenToNewMessage() {
	    try {
	    	return this.input.readLine();
		}
		catch ( IOException e ) {
			System.err.println( "Method \"listenToNewMessage\" failed : " + e );
			return null;
		}
	    
	}

	public String getClientName() {
		return this.clientName;
	}
	
	public String getID() {
		return this.ID;
	}
	
	public void setID( final String p_ID ) {
		this.ID = p_ID;
	}
	
	public void sendMessage( final String p_header, final String p_message ) {
		String message = p_header;
		
		switch ( p_header ) {
		case "[newMessage]" :
			// TODO : message should look like : [hh:mm] and not hh:mm ---->
			message += this.ID + "|" + new SimpleDateFormat( "HH:mm" ).format( new Date() )
		    		+ " : " + this.clientName + " wrote : \"" + p_message + "\".";
			// <----
			break;
		case "[newDisconnection]" :
	    	message += "[" + new SimpleDateFormat( "HH:mm" ).format( new Date() )
	    			+ "] : " + this.clientName + " disconnected.";
			break;
		case "[clientName]" :
			message += p_message;
			break;
		case "[stillConnected]" :
			break;
		default :
			System.err.println( "Incorrect value given to parameter \"p_header\" in method \"sendMessage\"." );
			break;
		}
		
		try {
			this.output.println( message );
		}
		catch ( NullPointerException e ) {
			System.err.println( "Can't send the message. The connection to the server must not be ensured : " + e );
		}
	}
	
	public void disconnect() {
		try {
			this.sendMessage( "[newDisconnection]", null );
			this.client.close();
			System.exit( 0 );
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		catch ( NullPointerException e ) {
			System.err.println( "Can't cut socket connection. The connection to the server must not be ensured : " + e );
		}
	}
}