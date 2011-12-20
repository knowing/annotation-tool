package evaluationtool.gui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;

import javax.swing.*;

import com.sun.jna.NativeLibrary;

import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.TimestampConverter;

import uk.co.caprica.vlcj.component.*;

public class EvalGUI extends JFrame implements ComponentListener{

  String windowTitle = "EvalTool";
  
  // Model
  DataModel model;
	
  String mrl;
  private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
  JPanel panelNorth;
  JPanel panelSouth;
  JFrame dataDialog;
  
  // Menu bar
  JMenuBar menubar;
  
  JMenu file;
  JMenuItem openfile;
  JMenuItem exit;
  
  JLabel position;
  JSlider positionslider;
  JButton playpause;
  JButton stop;
  JButton skipframe;
  JButton mute;
  
  /**
   * Sets VLC path
   */
  private void initLibVlc(){
	 // JFileChooser chooser = new JFileChooser();
	 // chooser.showOpenDialog(null);	 
	  
	  NativeLibrary.addSearchPath("libvlc",  "C:\\Users\\anfi\\Desktop\\vlc-1.2.0-pre2-20111201-0302"); 
 }
  
  /**
   * Shows a JFileChooser and loads the selected file
   */
  public void loadFile(){
	  
	  JFileChooser chooser = new JFileChooser();

	  // If ok has been clicked, load the file
	  if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){	 
		  String path = chooser.getSelectedFile().getAbsolutePath();
		  loadVideo(path);
		  this.setTitle(windowTitle + " - " + path);
	  }
  }

  /**
   * Initializes the GUI with a DataModel
   * @param m
   */
  public EvalGUI(DataModel m) {
	 /*
	  * Set VLC path. 
	  * Needed files are (Windows):
	  * - libvlc.dll
	  * - libvlccore.dll
	  * - /plugins
	  */
	 initLibVlc();
	
	// Reference the model
	model = m;
	m.setGUI(this);
	 
	// Set window properties
	this.setTitle(windowTitle);
    this.setLocation(100, 100);
    this.setSize(800, 600);
    
    // Exit immediately when user clicks the X
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // Add ComponentListener
    this.addComponentListener(this);
    
    // North panel
    mediaPlayerComponent = new VideoComponent();
    mediaPlayerComponent.addKeyListener(new VideoFrameListener(this));
    panelNorth = new JPanel();
    panelNorth.setLayout(new BorderLayout());
    panelNorth.add(mediaPlayerComponent, BorderLayout.CENTER);
    
    // Create layout
    panelSouth = new JPanel();
    
	// Minimum sizes
	Dimension minimumSize = new Dimension(100, 100);
	mediaPlayerComponent.setMinimumSize(minimumSize);
	panelSouth.setMinimumSize(minimumSize);
      
    // South panel
    model.addDataTrack("Test");
    model.addDataTrack("Test2");
    model.addDataTrack("Test3");
    model.addDataTrack("Test4");
    model.addDataTrack("Test5");
    model.addDataTrack("Test6"); 
    updatePanelSouth();
    
    // Create menu structure
    menubar = new JMenuBar();
    
    	// File menu
	    file 			= new JMenu("File");
	    openfile 		= new JMenuItem("Import file");
	    exit 			= new JMenuItem("Exit");
	    
	    file.add(openfile);
	    file.add(exit);
    
	    // Playback controls
	    position 		= new JLabel();
	    positionslider	= new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
	    playpause 		= new JButton("Pause");
	    stop 			= new JButton("Stop");
	    skipframe 		= new JButton("Skip");
	    mute 			= new JButton("Mute");
	    position 		= new JLabel("-");
    
    
	    menubar.add(file);
	    menubar.add(positionslider);
	    menubar.add(position);
	    menubar.add(playpause);
	    menubar.add(stop);
	    menubar.add(skipframe);
	    menubar.add(mute);
    
    // Set action commands for buttons
    openfile.setActionCommand("openfile");
    exit.setActionCommand("exit");
    playpause.setActionCommand("playpause");
    stop.setActionCommand("stop");
    skipframe.setActionCommand("skipframe");
    mute.setActionCommand("mute");
    
    // Add MenuListener to all buttons
    MenuListener menlis = new MenuListener(this);
    openfile.addActionListener(menlis);
    exit.addActionListener(menlis);
    playpause.addActionListener(menlis);
    stop.addActionListener(menlis);
    skipframe.addActionListener(menlis);
    mute.addActionListener(menlis);
    
    /*
     * Not needed as tracks are displayed in a seperate window
     * 
    // Add north and south panel to a new SplitPane
	    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelNorth, panelSouth);
	    splitPane.setOneTouchExpandable(false);
	    splitPane.setDividerLocation(200);
	
	    // Set splitPane as content pane
	    this.setContentPane(splitPane);
    */
    
    /*
     * JDialog holds tracks
     */
    this.setContentPane(panelNorth);
    dataDialog = new JFrame();
    dataDialog.setSize(800, 600);
    dataDialog.setTitle("EvalTool - Data tracks");
    
    dataDialog.setJMenuBar(menubar);
    
    dataDialog.setContentPane(panelSouth);
    dataDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    dataDialog.setVisible(true);
    
    
    // Start thread to synchronize playback positions and information
    VideoInfo vi = new VideoInfo(position, model.getLoadedDataTracks());
    vi.start();
    
    // Show window
    this.setVisible(true);
    
    // Load video
    if(model.getVideoPath() != null){
    	loadVideo(model.getVideoPath());
    	this.setTitle(windowTitle + " - " + model.getVideoPath());
    	}
  }
  
  /**
   * Recreates the layout of panelSouth
   */
  public void updatePanelSouth(){
	  panelSouth.removeAll();
	  
	  if(model.getLoadedDataTracks().size() == 0){
		  panelSouth.setVisible(false);
	  }
	  else{
		  // Create GridLayout with a row for every track
		  panelSouth.setLayout(new GridLayout(model.getLoadedDataTracks().size(), 1));
		  
		  
		  // Add all tracks
		  for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
			  panelSouth.add(model.getLoadedDataTracks().get(i).getVisualization());
		  }
		  
		  panelSouth.validate();
	  }
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
	 stop();
	  
    // Prepare video and start playing
	mediaPlayerComponent.getMediaPlayer().prepareMedia(mrl);
	
	// Mute by default
	mediaPlayerComponent.getMediaPlayer().mute();
	
	mediaPlayerComponent.getMediaPlayer().play();
	
	mediaPlayerComponent.getMediaPlayer().setPosition(0);
	
	playpause();
	
	// Loop the video
	mediaPlayerComponent.getMediaPlayer().setRepeat(true);
  }
  
  /**
   * Shows the next frame in the video
   */
  public void skipFrame(){
	  // This will pause the video, so change button text
	  playpause.setText("Play");
	  mediaPlayerComponent.getMediaPlayer().nextFrame();
  }
  
  /**
   * Stops video playback
   */
  public void stop(){
	  playpause.setText("Play");
	  
	  // Stop playback
	  if(mediaPlayerComponent.getMediaPlayer().isPlaying())
	  		mediaPlayerComponent.getMediaPlayer().pause();
	  mediaPlayerComponent.getMediaPlayer().setPosition(0);
  }
  
  /**
   * Mutes and unmutes the video
   */
  public void mute(){
	  // Mute
	  mediaPlayerComponent.getMediaPlayer().mute();
	  
	  if(mediaPlayerComponent.getMediaPlayer().isMute()){
		  mute.setText("Unmute");
	  }
	  else{
		  mute.setText("Mute");
	  }
  }
  
  /**
   * Switches the video between playing and paused
   */
  public void playpause (){
	  
	  // If video is playing, pause and change button
	  if(mediaPlayerComponent.getMediaPlayer().isPlaying()){
		  mediaPlayerComponent.getMediaPlayer().pause();
		  playpause.setText("Play");
	  }
	  // Otherwise continue playing
	  else{
		  mediaPlayerComponent.getMediaPlayer().play();
		  playpause.setText("Pause");
	  }
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
	  
	  VideoInfo(JLabel infolabel, LinkedList<Data> t){
		  lab = infolabel;
		  visualizations = t;
	  }
	  
	  /**
	   * Synchronizes slider positions and playback information every 100 ms
	   */
	  public void run(){
		  
		  // The last position slider value
		  int tempSliderValue = 0;
		  float tempPos= 0;
		  
		  while(running){
			  // Update every 100 ms
			  try{sleep(10);}
			  catch(Exception e){System.err.println("Could not sleep");}
			  
			  // Save old position
			  tempPos = pos;
			  
			  // Read values
			  pos 		= mediaPlayerComponent.getMediaPlayer().getPosition();
			  length 	= mediaPlayerComponent.getMediaPlayer().getLength();
			  
			  // Check if user moved slider
			  if(tempSliderValue != positionslider.getValue()){
				  // If slider has been moved, jump to new position
				  mediaPlayerComponent.getMediaPlayer().setPosition(positionslider.getValue() / 1000f);
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
				  for(int i = 0; i < visualizations.size(); i++){
					  visualizations.get(i).getVisualization().setPosition(pos * length);
				  }
			  } 
		  }
	  }
  }
  
  public void componentHidden(ComponentEvent e) {}
  public void componentMoved(ComponentEvent e) {}
  public void componentResized(ComponentEvent e) {}
  public void componentShown(ComponentEvent e) {}
}