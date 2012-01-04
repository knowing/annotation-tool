package evaluationtool.gui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

import javax.swing.*;

import com.sun.jna.NativeLibrary;

import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.TimestampConverter;

import uk.co.caprica.vlcj.component.*;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

public class EvalGUI extends JFrame implements ComponentListener, WindowListener{

  String windowTitle = "EvalTool";
  
  // Model
  DataModel model;
	
  String mrl;
  private EmbeddedMediaPlayerComponent mediaPlayerComponent;
  JPanel panelVideo;
  JPanel panelDataTracks;
  JFrame dataDialog;
  
  // Menu bar
  JMenuBar menubar;
  
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
	  * Sets VLC path. 
	  * Needed files are (Windows):
	  * - libvlc.dll
	  * - libvlccore.dll
	  * - /plugins
   */
  void initLibVlc(){
	  
	  JFileChooser chooser = new JFileChooser();
	  chooser.setDialogTitle("Please select a directory with VLC 1.2 or higher");
	  // We need a directory
	  chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	  
	  // If ok has been clicked, load the file
	  if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){	 
		  String path = chooser.getSelectedFile().getAbsolutePath();
		  NativeLibrary.addSearchPath("libvlc",  path);
		  model.setVLCPath(path);
		  
		// Save config and wait for error message
			String s  = model.saveConfiguration();
			
			// If there is an error, create a message dialog
			if(s != null){
				JOptionPane.showMessageDialog(this, "Error saving configuration: " + s, "File error", JOptionPane.ERROR_MESSAGE);			 
				}
			else
				System.out.println("Wrote config");
	  }  
	  else{
		   JOptionPane.showMessageDialog(this, "This program does not work without VLC. Quitting.", "Error", JOptionPane.ERROR_MESSAGE);	
		   System.exit(0);
	  }
 }

  public DataModel getModel(){
	  return model;
  }
  
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
	
		if(model.getVLCPath().trim().equals("")){
			initLibVlc();
		}
		else{
			// Add path if it is not ""
			NativeLibrary.addSearchPath("libvlc",  model.getVLCPath());
		}
	 
	// Create this frame
	buildVideoFrame();
    
	// Create menu
	buildMenu(); 
    
	// Show window
    this.setVisible(true);
    
    // Create second Frame for data tracks
    buildDataFrame(); 
    
    // Create synchonizer
    vi = new VideoDataSynchronizer(this, mediaPlayerComponent);

    // Show panelSouth only of there are tracks to show
    updateDataFrame();
  }
  
  /**
   * Builds this frame and its components
   */
  private void buildVideoFrame(){
	// Set window properties
		this.setTitle(windowTitle);
	    this.setLocation(100, 100);
	    this.setSize(800, 600);
	    
	    // Exit immediately when user clicks the X
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.addWindowListener(this);
	    
	    // Add ComponentListener
	    this.addComponentListener(this);
	    
	    // North panel
	    while(mediaPlayerComponent == null){
		    try{
		    // While VLC path is not valid, ask user again
		    mediaPlayerComponent = new VideoComponent();
		    }
		    catch(Exception e){
		    	initLibVlc();
		    }
	    }
	    
	    mediaPlayerComponent.addKeyListener(new VideoFrameListener(this));
	    panelVideo = new JPanel();
	    panelVideo.setLayout(new BorderLayout());
	    panelVideo.add(mediaPlayerComponent, BorderLayout.CENTER);
	    
	    // Create layout
	    panelDataTracks = new JPanel();
	    
		// Minimum sizes
		Dimension minimumSize = new Dimension(100, 100);
		mediaPlayerComponent.setMinimumSize(minimumSize);
		panelDataTracks.setMinimumSize(minimumSize);
		
		this.setContentPane(panelVideo);
  }
  
  /**
   * Creates the second frame for data tracks
   */
  private void buildDataFrame(){
	  dataDialog = new JFrame();
	  dataDialog.setSize(800, 600);
	  dataDialog.setTitle("EvalTool - Data tracks");
	    
	  dataDialog.setJMenuBar(menubar);
	    
	  dataDialog.setContentPane(panelDataTracks);
	  dataDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	    
	    // Add MenuListener to all buttons
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
  
  private ImageIcon getImageIconFromFile(String path, int size){
	 return new ImageIcon((new ImageIcon(path)).getImage().getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH));
  }
  
  /**
   * Recreates the layout of panelSouth
   */
  public void updateDataFrame(){
	  panelDataTracks.removeAll();
	  
	  if(model.getLoadedDataTracks().size() == 0 && this.isVisible()){
		  // No data tracks but video, data frame invisible
		  dataDialog.setJMenuBar(null);
		  dataDialog.setVisible(false);
		  this.setJMenuBar(menubar);
		  this.validate();
	  }
	  else{
		  // Data tracks exist, so rebuild and show frame
		  this.setJMenuBar(null);
		  this.revalidate();
		  dataDialog.setJMenuBar(menubar);
		  // Create GridLayout with a row for every track
		  panelDataTracks.setLayout(new GridLayout(model.getLoadedDataTracks().size(), 1)); 
		  
		  // Add all tracks
		  for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
			  if(model.getLoadedDataTracks().get(i).getVisualization() == null){
				  System.err.println("No visualization");
			  }
			  else{		
				  panelDataTracks.add(model.getLoadedDataTracks().get(i).getVisualization());
			  }
		  }
		  
		  dataDialog.setVisible(true);
		  panelDataTracks.validate();
		  
		  // For some reason, the layouts are only updated on a resize. Workaround.
		  dataDialog.setSize(dataDialog.getWidth() + 1, dataDialog.getHeight());
		  dataDialog.setSize(dataDialog.getWidth() - 1, dataDialog.getHeight());
	  }
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
	 // playPauseButton.setText("Play");
	  playPauseButton.setIcon(playIcon);
	  mediaPlayerComponent.getMediaPlayer().nextFrame();
  }
  
  /**
   * Stops video playback
   */
  public void stop(){
	 // playPauseButton.setText("Play");
	  playPauseButton.setIcon(playIcon);
	  
	  // Stop playback
	  if(mediaPlayerComponent.getMediaPlayer().isPlaying())
	  		mediaPlayerComponent.getMediaPlayer().pause();
	  
	  getModel().setPosition(0);
  }
  
  /**
   * Mutes and unmutes the video
   */
  public void mute(){
	  // Mute
	  mediaPlayerComponent.getMediaPlayer().mute();
	  
	  if(mediaPlayerComponent.getMediaPlayer().isMute()){
		 // muteButton.setText("Unmute");
		  muteButton.setIcon(unmuteIcon);
	  }
	  else{
		 // muteButton.setText("Mute");
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
		  //playPauseButton.setText("Play");
		  playPauseButton.setIcon(playIcon);
	  }
	  // Otherwise continue playing
	  else{
		  mediaPlayerComponent.getMediaPlayer().play();
		  //playPauseButton.setText("Pause");
		  playPauseButton.setIcon(pauseIcon);
	  }
  }
  
  public void componentHidden(ComponentEvent e) {}
  public void componentMoved(ComponentEvent e) {}
  public void componentResized(ComponentEvent e) {}
  public void componentShown(ComponentEvent e) {}


	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {
	
		vi.getVideoInfo().setRunning(false);
	
		// Stop media player properly
			mediaPlayerComponent.getMediaPlayer().stop();
			mediaPlayerComponent.getMediaPlayer().release();
			mediaPlayerComponent.release();
			
			mediaPlayerComponent = null;
		
}
public void windowDeactivated(WindowEvent arg0) {}
public void windowDeiconified(WindowEvent arg0) {}
public void windowIconified(WindowEvent arg0) {}
public void windowOpened(WindowEvent arg0) {}
}