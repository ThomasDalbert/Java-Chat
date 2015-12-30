package Controller;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Model.LogWriter;
import View.GUI_Logs;

public class Server {

	private LogWriter logWriter;
	private GUI_Logs GUI_Logs;
	
	private PrintStream output;
	private ServerSocket server;
	private Map<Socket, String> clientList = new HashMap<Socket, String>();
	private long freeIdentifiant 	  	   = 0;
	
	public Server( final LogWriter p_logWriter, final GUI_Logs p_chat_GUI ) {
		this.logWriter = p_logWriter;
		this.GUI_Logs  = p_chat_GUI;
		
		this.startServer( 8080 );
		this.manageConnections();
	}
	
	private void startServer( final int p_port ) {
		try {
			this.server = new ServerSocket( p_port );
			this.GUI_Logs.renderServerStarted( p_port );
		} 
		catch ( IOException e ) {
			System.err.println( "Error : couldn't start the server : " + e );
		}
	}
	
	private void manageConnections() {
		try {
			while ( true ) {
				Socket clientSocket   = server.accept();
				String connectionTime = new SimpleDateFormat( "HH:mm" ).format( new Date() ),
					   clientIP       = clientSocket.getInetAddress().getHostAddress();
				
				// Treat the new connection
				this.clientList.put( clientSocket, null );
				this.freeIdentifiant++;
				
				// Start a new thread to listen to the client new messages
				ChatManager user = new ChatManager( this.GUI_Logs, this, clientSocket, this.freeIdentifiant );
				user.start();
				
				// Display and write logs on server
				this.GUI_Logs.renderConnection( connectionTime, clientIP );
				this.logWriter.logConnection( connectionTime, clientIP );
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}	
	
	public void broadcastOrMulticast( final String p_str, final Socket p_client ) {
	    try {
	    	synchronized ( this.clientList ) {
		    	for( Entry<Socket, String> client : this.clientList.entrySet() ) {
		    		if ( client.getKey() != p_client ) {
			    		this.output = new PrintStream( client.getKey().getOutputStream() );
			    		this.output.println( p_str );
		    		}
		    	}
	    	}
	    }
	    catch ( Exception e ) {
	    	System.err.println( "Error : couldn't send the message : " + e );
	    }
	}
	
	public Map<Socket, String> getClientList() {
		synchronized ( this.clientList ) {
			return this.clientList;
		}
	}
}