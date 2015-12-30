package Controller;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public class Music {
	Clip clip;
	AudioInputStream inputStream;
	
	public Music() {
		try {
	        this.clip = AudioSystem.getClip();
	        File url = new File( "notificationSound.wav" );
	        this.inputStream = AudioSystem.getAudioInputStream( url );
		} 
		catch ( Exception e ) {
			System.err.println( e.getMessage() );
		}
   }
  
	public void startMusic() {
		try {
			this.clip.open( this.inputStream );
			this.clip.start();
		}
		catch ( LineUnavailableException | IOException e ) {
			e.printStackTrace();
		}
	}
}

