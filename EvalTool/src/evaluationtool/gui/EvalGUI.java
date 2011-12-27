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
  JMenuItem openfile;
  JMenuItem save;
  JMenuItem saveNewProject;
  JMenuItem exit;
  
  JLabel position;
  JSlider positionslider;
  JButton playpause;
  JButton stop;
  JButton skipframe;
  JButton mute;
  
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
		    save 			= new JMenuItem("Save project");
		    saveNewProject 	= new JMenuItem("Save project as");
		    exit 			= new JMenuItem("Exit");
		    
		    file.add(openfile);
		    file.add(new JSeparator());
		    file.add(save);
		    file.add(saveNewProject);
		    file.add(new JSeparator());
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
		save.setActionCommand("save");
		saveNewProject.setActionCommand("saveas");
	    openfile.setActionCommand("openfile");
	    exit.setActionCommand("exit");
	    playpause.setActionCommand("playpause");
	    stop.setActionCommand("stop");
	    skipframe.setActionCommand("skipframe");
	    mute.setActionCommand("mute");
	    
	    // Add MenuListener to all buttons
	    MenuListener menlis = new MenuListener(this);
	    save.addActionListener(menlis);
	    saveNewProject.addActionListener(menlis);
	    openfile.addActionListener(menlis);
	    exit.addActionListener(menlis);
	    playpause.addActionListener(menlis);
	    stop.addActionListener(menlis);
	    skipframe.addActionListener(menlis);
	    mute.addActionListener(menlis);
  }
  
  /**
   * Recreates the layout of panelSouth
   */
  public void updateDataFrame(){
	  panelDataTracks.removeAll();
	  
	  if(model.getLoadedDataTracks().size() == 0 && this.isVisible()){
		  // No data tracks but video, data frame invisible
		  this.setJMenuBar(menubar);
		  dataDialog.setJMenuBar(null);
		  dataDialog.setVisible(false);
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