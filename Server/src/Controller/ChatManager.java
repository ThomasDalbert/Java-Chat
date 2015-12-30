package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import View.GUI_Logs;


public class ChatManager extends Thread {

	private GUI_Logs GUI_Logs;
	
	private Server server;
	private String clientName;
	private long ID;
	private Socket clientSocket;
	private PrintStream output;
	private Timer checkPresenceTimer;
	private short delayBeforeDisconnection = 30; // 30 in seconds, time without activity after which we consider the client is offline
	
	public ChatManager( final GUI_Logs p_GUI_Logs, final Server p_server, final Socket p_clientSocket, final long p_ID ) throws IOException {
		this.GUI_Logs 	  = p_GUI_Logs;
		this.server       = p_server;
		
		this.ID			  = p_ID;
		this.clientSocket = p_clientSocket;
		this.output 	  = new PrintStream( this.clientSocket.getOutputStream() );
	}
	
	public void run() {
		String str;
		boolean initilisationDatasSent = false;
		this.checkPresence();
		
		while ( !this.isInterrupted() ) {
			str = this.receiveMessage();
			if ( str != null ) {
				try {
					// TODO : remplacer ce if/elseif par un switch (1. récupérer le string entre crochets 2. appliquer les "case" dessus). -->>
					if ( str.startsWith( "[newMessage]" ) ) {
						this.resetDelayBeforeDisconnectionIfNeeded( str );		
						this.server.broadcastOrMulticast( str, null );
						this.GUI_Logs.renderNewMessage( str );
					}
					else if ( str.startsWith( "[clientName]" ) ) {
						this.clientName = str.replace( "[clientName]", "" );
						this.server.getClientList().put( this.clientSocket, this.clientName );
						
						if ( !initilisationDatasSent ) {
							initilisationDatasSent = true;
							this.sendInitialisationDatas( this.ID );
						}
						
						this.server.broadcastOrMulticast( "[newConnection]" + this.clientName, this.clientSocket );
					}
					else if ( str.startsWith( "[newDisconnection]" ) ) {
						this.cutConnection();
						return;
					}
					else if ( str.startsWith( "[stillConnected]" ) ) {
						this.resetDelayBeforeDisconnection();
					}
					// <<-- Fin TODO
				}
				catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void checkPresence() {
		this.checkPresenceTimer = new Timer();
		
		this.checkPresenceTimer.schedule( new TimerTask() {
		    public void run() {
				--delayBeforeDisconnection;
				if ( delayBeforeDisconnection == 0 ) {
					cutConnection();
				}	
		    }
		 }, 0, 1000 );
	}
	
	private String receiveMessage() {
	    try {
	    	BufferedReader input = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
	    	return input.readLine();
	    }
	    catch ( SocketException e ) {
	    	this.cutConnection();
	    }
	    catch ( IOException e ) {
	    	e.printStackTrace();
	    }
	    return null; // "return" is voluntary done outside of any finally block here as it doesn't work if in for some obscure reason.
	}
	
	private void resetDelayBeforeDisconnectionIfNeeded( final String p_str ) {
		// Getting ID from p_str
		String id = p_str.replace( "[clientName]", "" );
		StringTokenizer stringTokenizer = new StringTokenizer( id, "|" );
		id = stringTokenizer.nextToken();
		
		// Reseting delayBeforeDisconnection if the ID matches the one of the current client 
		if ( id.equals( this.ID ) ) {
			this.resetDelayBeforeDisconnection();
		}
	}
	
	private void resetDelayBeforeDisconnection() {
		this.delayBeforeDisconnection = 30;
	}
	
	private void cutConnection(){
		this.checkPresenceTimer.cancel();
    	this.disconnect();
    	this.server.broadcastOrMulticast( "[newDisconnection]" + this.clientName, null );
		this.GUI_Logs.renderDisconnection( "Client " + this.clientName + " disconnected." );
		this.interrupt();
	}
	
	private void sendInitialisationDatas( final long p_ID ) {
	    try {
	    	Map<Socket, String> clientList = this.server.getClientList();
	    	
	    	String clientNameList = "";
	    	for( Entry<Socket, String> client : clientList.entrySet() ) {
	    		clientNameList += client.getValue() + "_$*° ";
	    	}

	    	this.output.println( "[clientData]" + p_ID + "|" + clientNameList );
	    }
	    catch ( Exception e ) {
	    	e.printStackTrace();
	    }
	}
	
	// This method should NOT be called ! Use cutConnection() instead.
	private void disconnect() {
		try {
			this.server.getClientList().remove( clientSocket );
			this.clientSocket.close();
		} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}