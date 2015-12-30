package Controller;

import java.io.IOException;

import View.GUI_Chat;

public class Main {

	public static void main( String[] args ) throws IOException  {		
		
		Client   socketClient = new Client();
		Cypher   cypher 	  = new Cypher();
		GUI_Chat IHM_Chat 	  = new GUI_Chat( socketClient, cypher );
		
		socketClient.setInstances( IHM_Chat, cypher );
		socketClient.start();
	}
}