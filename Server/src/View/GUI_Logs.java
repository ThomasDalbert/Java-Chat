package View;

public class GUI_Logs {

	public GUI_Logs() {}

	public void renderServerStarted( final int p_port ) {
		System.out.println( "Listening for connection on port " + p_port + "..." );
	}
	
	public void renderConnection( final String p_time, final String p_clientIp ) {
		System.out.println( "[" + p_time + "] : user with IP \"" + p_clientIp + "\" connected." );
	}
	
	public void renderDisconnection( final String p_message ) {
		System.out.println( p_message.replace( "[newDisconnection]", "" ) );
	}	
	
	public void renderNewMessage( final String p_text ) {
		System.out.println( p_text.replace( "[newMessage]", "" ) );
	}
}