package evaluationtool.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

/**
 * An EmbeddedMediaPlayerComponent that restarts the video after being resized
 * @author anfi
 *
 */
public class VideoComponent extends EmbeddedMediaPlayerComponent implements ComponentListener{

	VideoComponent(){
		// Add this as component listener
		this.addComponentListener(this);
	}
	
	public void componentHidden(ComponentEvent arg0) {}


	public void componentMoved(ComponentEvent arg0) {}

	/**
	 * Restart the video after the component has been resized
	 */
	public void componentResized(ComponentEvent arg0) {
		/*
		// Check if video player is initialized
		if(this.getMediaPlayer() != null && this.getMediaPlayer().isSeekable()){
		
			// Save current playing state
			float pos = this.getMediaPlayer().getPosition();
			
			// Stop playing
			this.getMediaPlayer().stop();
			
			try{Thread.sleep(100);}
		  	  catch(Exception e){}
			
		  	// Restart video
		  	this.getMediaPlayer().play();

			System.out.println("Reset position");
			
			// Now reset position and pause the video
			this.getMediaPlayer().setPosition(pos);
			this.getMediaPlayer().pause();
		}*/
	}


	public void componentShown(ComponentEvent arg0) {}
		
}
