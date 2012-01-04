package evaluationtool.gui;

import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JSlider;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import evaluationtool.Data;
import evaluationtool.TimestampConverter;

public class VideoDataSynchronizer {

	 private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	 EvalGUI gui;
	 
	 VideoInfo vi;
	 
	 public VideoDataSynchronizer(EvalGUI gui, EmbeddedMediaPlayerComponent empc){
		 this.gui = gui;
		 mediaPlayerComponent = empc;
	 
		// Start thread to synchronize playback positions and information
		    vi = new VideoInfo(gui.getPositionLabel(), gui.getPositionSlider(), gui.getModel().getLoadedDataTracks());
		    vi.start();
	 }
	 
	 public VideoInfo getVideoInfo(){
		 return vi;
	 }
	 
	
	/*
	   * Video playback methods
	   */

	  /**
	   * Loads a new video and initializes it
	   * @param mrl
	   */
	  public void loadVideo(String mrl) {
		  
			 // End old playback
			 gui.stop();
			 
		    // Prepare video and start playing
			mediaPlayerComponent.getMediaPlayer().prepareMedia(mrl);
			
			// Mute by default
			mediaPlayerComponent.getMediaPlayer().mute();

			// Loop the video
			mediaPlayerComponent.getMediaPlayer().setRepeat(true);
		 }
	
	/**
	   * This class collects information from the running video
	   * @author anfi
	   */
	  class VideoInfo extends Thread{
		  
		  // Has to be set false to stop the thread
		  boolean running = true;
		  
		  // Reference to the label in which to out the position
		  JLabel lab;
		  
		  JSlider positionslider;
		  
		  // The track visualizations that need the playback position
		  LinkedList<Data> visualizations;
		  
		  // Current position
		  float pos = 0f;
		  
		  // Length of video
		  long length = 0;
		  
		  /**
		   * The thread can be stopped by setting running to false
		   * @param b
		   */
		  public void setRunning(boolean b){
			  running = b;
		  }
		  
		  VideoInfo(JLabel infolabel, JSlider slider, LinkedList<Data> t){
			  lab = infolabel;
			  visualizations = t;
			  positionslider = slider;
		  }
		  
		  /**
		   * Synchronizes slider positions and playback information every 100 ms
		   */
		  public void run(){
			  
			  // The last position slider value
			  int tempSliderValue = 0;
			  float tempPos= 0;
			  
			  while(running){
				  try{sleep(10);}
				  catch(Exception e){System.err.println("Could not sleep");}
				  
				  /*
				   * DEBUG OUTPUT
				   */
				  
				  //System.out.println("GUI Videofile length: " + mediaPlayerComponent.getMediaPlayer().getLength());
				  //System.out.println("Model videofile path: " + model.getVideoPath());
					 
				  
				  /*
				   * END DEBUG OUTPUT
				   */
				  
				  // Save old position
				  tempPos = pos;
				  
				  // Read values
				  try{
				  pos 		= mediaPlayerComponent.getMediaPlayer().getPosition();
				  length 	= mediaPlayerComponent.getMediaPlayer().getLength();
				  }
				  catch(NullPointerException ne){
					  // In case our mediaPlayer vanished
					  setRunning(false);
					  return;
				  }
				  // Check if user moved slider
				  if(tempSliderValue != positionslider.getValue()){
					  // If slider has been moved, jump to new position
					  gui.getModel().setPosition(positionslider.getValue() / 1000f, true);
				  }
				  else{
					  // If slider has not been moved, move it to current playing position
					  positionslider.setValue((int)(1000 * pos));		  
				  }
				  
				  // Backup slider value
				  tempSliderValue = positionslider.getValue();
				  
				  // For improved performance, update only if position changed
				  if(pos != tempPos){
					  // Set values to label and all visualizations
					  lab.setText(TimestampConverter.getVideoTimestamp((long)(pos * length)) + "/" + TimestampConverter.getVideoTimestamp(length));		  
					  gui.getModel().setPosition(pos, false);  
				  } 
			  }
		  }
	  }
}
