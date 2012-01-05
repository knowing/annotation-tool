package evaluationtool.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import com.sun.jna.NativeLibrary;

import evaluationtool.DataModel;
import evaluationtool.util.VLCPlayerHandler;

import uk.co.caprica.vlcj.component.*;

public class EvalGUI extends WindowAdapter{

  String windowTitle = "EvalTool";
  
  // Model
  DataModel model;
  
  // Media player component
  EmbeddedMediaPlayerComponent mediaPlayerComponent = null;
  
  JFrame dataFrame;
  JPanel dataContent;
  JFrame videoFrame;
  
  // Menu bar
  JMenuBar menubar;
  
  // Menubar
  JMenu file;
	  JMenuItem createtrack;
	  JMenuItem openfile;
	  JMenuItem save;
	  JMenuItem saveNewProject;
	  JMenuItem exit;
  
  JLabel position;
  JSlider positionslider;
  JButton playPauseButton;
  JButton stopButton;
  JButton skipFrameButton;
  JButton muteButton;
  
  // Icons for buttons
  ImageIcon playIcon;
  ImageIcon pauseIcon;
  ImageIcon stopIcon;
  ImageIcon skipFrameIcon;
  ImageIcon muteIcon;
  ImageIcon unmuteIcon;
  
  VideoDataSynchronizer vi;
  
  /**
   * Initializes the GUI with a DataModel
   * @param m
   */
  public EvalGUI(DataModel m) {
	
	// Reference the model
	model = m;
	model.setGUI(this);
	
	// Load VLC path
	model.loadVLCPath();
	
	if(model.getVLCPath().trim().equals(""))
		VLCPlayerHandler.initLibVlc(model, null);
	
	buildMenu();
	buildVideoFrame(); 
    buildDataFrame(); 
    
    // Create synchronizer
    vi = new VideoDataSynchronizer(this, mediaPlayerComponent);

    updateFrames();
  }
  
  /**
   * Builds the frame for video
   */
  private void buildVideoFrame(){
	  
	videoFrame = new JFrame();
	  
	// Set window properties and add listeners
	videoFrame.setTitle(windowTitle);
	videoFrame.setLocation(100, 100);
	videoFrame.setSize(800, 600);
	videoFrame.addWindowListener(this);
    
    // Create media player and ask for VLC path if necessary
    while(mediaPlayerComponent == null){
	    try{
	    NativeLibrary.addSearchPath("libvlc",  model.getVLCPath());
	    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	    mediaPlayerComponent.addKeyListener(new VideoFrameListener(this));
	    }
	    catch(Exception e){
	    	VLCPlayerHandler.initLibVlc(model, videoFrame);
	    }
    }
	
	// Add media player
	videoFrame.setContentPane(mediaPlayerComponent);
	videoFrame.setVisible(true);
  }
  
  /**
   * Creates the frame for data tracks
   */
  private void buildDataFrame(){
	  dataFrame = new JFrame();
	  dataContent = new JPanel();
	  
	  dataFrame.setSize(800, 600);
	  dataFrame.setMinimumSize(new Dimension(100, 100));  
	  dataFrame.setTitle("EvalTool - Data tracks");
	  dataFrame.addWindowListener(this);
	  dataFrame.setContentPane(dataContent);
  }
  
  /**
   * Creates the menu
   */
  private void buildMenu(){
	  // Create menu structure
	    menubar = new JMenuBar();
	    
	    	// File menu
		    file 			= new JMenu("File");
		    openfile 		= new JMenuItem("Import file");
		    createtrack		= new JMenuItem("Create annotation track");
		    save 			= new JMenuItem("Save project");
		    saveNewProject 	= new JMenuItem("Save project as");
		    exit 			= new JMenuItem("Exit");
		    
		    file.add(openfile);
		    file.add(createtrack);
		    file.add(new JSeparator());
		    file.add(save);
		    file.add(saveNewProject);
		    file.add(new JSeparator());
		    file.add(exit);
	    
		    // Load icons
		    String pathToResources = "Resources\\";
		    final int ICONSIZE = 16;

		    playIcon 				= getImageIconFromFile(pathToResources + "play.png", 	ICONSIZE);
		    pauseIcon				= getImageIconFromFile(pathToResources + "pause.png", 	ICONSIZE);
		    stopIcon				= getImageIconFromFile(pathToResources + "stop.png", 	ICONSIZE);
		    skipFrameIcon			= getImageIconFromFile(pathToResources + "skip.png", 	ICONSIZE);
		    muteIcon				= getImageIconFromFile(pathToResources + "mute.png", 	ICONSIZE);
		    unmuteIcon				= getImageIconFromFile(pathToResources + "unmute.png", 	ICONSIZE);
		    
		    // Playback controls
		    position 				= new JLabel();
		    positionslider			= new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
		    playPauseButton 		= new JButton(pauseIcon);
		    stopButton 				= new JButton(stopIcon);
		    skipFrameButton 		= new JButton(skipFrameIcon);
		    muteButton 				= new JButton(muteIcon);
		    position 				= new JLabel("-");
	    
	    
		    menubar.add(file);
		    menubar.add(positionslider);
		    menubar.add(position);
		    menubar.add(playPauseButton);
		    menubar.add(stopButton);
		    menubar.add(skipFrameButton);
		    menubar.add(muteButton);
	    
	    // Set action commands for buttons
		save.setActionCommand("save");
		saveNewProject.setActionCommand("saveas");
	    openfile.setActionCommand("openfile");
	    createtrack.setActionCommand("createtrack");
	    exit.setActionCommand("exit");
	    playPauseButton.setActionCommand("playpause");
	    stopButton.setActionCommand("stop");
	    skipFrameButton.setActionCommand("skipframe");
	    muteButton.setActionCommand("mute");
	    
	    // Add a MenuListener to all buttons
	    MenuListener menlis = new MenuListener(this);
	    save.addActionListener(menlis);
	    saveNewProject.addActionListener(menlis);
	    openfile.addActionListener(menlis);
	    createtrack.addActionListener(menlis);
	    exit.addActionListener(menlis);
	    playPauseButton.addActionListener(menlis);
	    stopButton.addActionListener(menlis);
	    skipFrameButton.addActionListener(menlis);
	    muteButton.addActionListener(menlis);
  }
  
  /**
   * Recreates the layout of panelSouth
   */
  public void updateFrames(){
	  dataContent.removeAll();
	  
	  if(model.getLoadedDataTracks().size() == 0){
		  // No data tracks, so let the data frame disappear
		  dataFrame.setJMenuBar(null);
		  dataFrame.setVisible(false);
		  
		  videoFrame.setJMenuBar(menubar);
		  videoFrame.validate();
	  }
	  else{
		  // Data tracks exist, so build and show data frame
		  videoFrame.setJMenuBar(null);
		  videoFrame.revalidate();
		  
		  dataFrame.setJMenuBar(menubar);
		  // Create GridLayout with a row for every track
		  dataContent.setLayout(new GridLayout(model.getLoadedDataTracks().size(), 1)); 
		  
		  // Add all tracks
		  for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
				  dataContent.add(model.getLoadedDataTracks().get(i).getVisualization());
		  }
		  
		  dataFrame.setVisible(true);
		  dataFrame.validate();
		  
		  // For some reason, the layouts are only updated on a resize. Workaround.
		  dataFrame.setSize(dataFrame.getWidth() + 1, dataFrame.getHeight());
		  dataFrame.setSize(dataFrame.getWidth() - 1, dataFrame.getHeight());
	  }
  }
  
  public JFrame getActiveFrame() {
		if(dataFrame != null && dataFrame.isVisible())
			return dataFrame;
		else if (videoFrame != null)
			return videoFrame;
		else return null;
	}
  
  public DataModel getModel(){
	  return model;
  }
  
  public JLabel getPositionLabel(){
	  return position;
  }
  
  public JSlider getPositionSlider(){
	  return positionslider;
  }
  
  public void loadVideo(String src){
	  vi.loadVideo(src);
  }
  
  public long getVideoLength(){
	  return mediaPlayerComponent.getMediaPlayer().getLength();
  }
  
  public boolean isVideoPlaying(){
	  return mediaPlayerComponent.getMediaPlayer().isPlaying();
  }
  
  /**
   * Sets the video playback position
   * @param pos
   */
  public void setVideoPosition(float pos) {
		if(mediaPlayerComponent.getMediaPlayer() != null){
			mediaPlayerComponent.getMediaPlayer().setPosition(pos);
		}
	}
  
  /**
   * Shows the next frame in the video
   */
  public void skipFrame(){
	  // This will pause the video, so change button text
	  playPauseButton.setIcon(playIcon);
	  mediaPlayerComponent.getMediaPlayer().nextFrame();
  }
  
  /**
   * Stops video playback
   */
  public void stop(){
	  playPauseButton.setIcon(playIcon);
	  
	  // Stop playback
	  if(mediaPlayerComponent.getMediaPlayer().isPlaying())
	  		mediaPlayerComponent.getMediaPlayer().pause();
	  
	  getModel().setPlaybackPosition(0);
  }
  
  /**
   * Mutes and unmutes the video
   */
  public void mute(){
	  // Mute
	  mediaPlayerComponent.getMediaPlayer().mute();
	  
	  if(mediaPlayerComponent.getMediaPlayer().isMute()){
		  muteButton.setIcon(unmuteIcon);
	  }
	  else{
		  muteButton.setIcon(muteIcon);
	  }
  }
  
  /**
   * Switches the video between playing and paused
   */
  public void playpause(){
	  
	  // If video is playing, pause and change button
	  if(mediaPlayerComponent.getMediaPlayer().isPlaying()){
		  mediaPlayerComponent.getMediaPlayer().pause();
		  playPauseButton.setIcon(playIcon);
	  }
	  // Otherwise continue playing
	  else{
		  mediaPlayerComponent.getMediaPlayer().play();
		  playPauseButton.setIcon(pauseIcon);
	  }
  }
  
  /**
   * Loads and resizes ImageIcons
   * @param path
   * @param size
   * @return
   */
  private ImageIcon getImageIconFromFile(String path, int size){
		 return new ImageIcon((new ImageIcon(path)).getImage().getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH));
	  }

	public void windowClosing(WindowEvent arg0) {

		// Stop thread
		vi.setRunning(false);
		
		// Stop media player properly
		mediaPlayerComponent.getMediaPlayer().stop();
		mediaPlayerComponent.getMediaPlayer().release();
		mediaPlayerComponent.getMediaPlayerFactory().release();
		mediaPlayerComponent.release();

		// Exit
		System.exit(0);
}

}