package Controller;

import java.io.IOException;

import Model.LogWriter;
import View.GUI_Logs;

public class Main {
	
	public static void main( String[] args ) throws IOException {
		
		new Server( new LogWriter(), new GUI_Logs() );
	}
}