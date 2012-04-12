package evaluationtool.gui;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

import com.sun.jna.NativeLibrary;

import evaluationtool.DataModel;
import evaluationtool.util.ProjectFileHandler;
import evaluationtool.util.VLCPlayerHandler;

import uk.co.caprica.vlcj.component.*;

public class EvalGUI extends WindowAdapter{

  private String windowTitle = "EvalTool ";
  
  // Model
  private DataModel model;
  
  // Media player component
  private EmbeddedMediaPlayerComponent mediaPlayerComponent = null;
  
  private JFrame dataFrame;
  private JPanel dataContent;
  private JFrame videoFrame;
  
  // Menu bar
  private JMenuBar menubar;
  
  // Menubar
  private JMenu file;
  	  private JCheckBox globalzoomCheckbox;
  	  private JMenuItem createIntervalTrack;
  	  private JMenuItem createPointsTrack;
	  private JMenuItem openfile;
	  private JMenuItem save;
	  private JMenuItem saveNewProject;
	  private JMenuItem closeProject;
	  private JMenuItem exit;
  
  private JLabel position;
  private JSlider positionslider;
  private JButton playPauseButton;
  private JButton stopButton;
  private JButton skipFrameButton;
  private JButton muteButton;
  
  // Icons for buttons
  private ImageIcon playIcon;
  private ImageIcon pauseIcon;
  private ImageIcon stopIcon;
  private ImageIcon skipFrameIcon;
  private ImageIcon muteIcon;
  private ImageIcon unmuteIcon;
  
  private VideoDataSynchronizer vi;
  
  // KeyListener for shortcuts
  private ShortcutKeyListener keylis;
  
  // Global zoom settings
  private long globalOffset = 0;
  private float globalPixelsPerMillisecond = 1;
  
  /**
   * Initializes the GUI with a DataModel
   * @param m
   */
  public EvalGUI(DataModel m) {
	
	// Reference the model
	model = m;
	model.setGUI(this);
	keylis = new ShortcutKeyListener(this);
	
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
	//videoFrame.setAlwaysOnTop(true);
	videoFrame.addWindowListener(this);
	videoFrame.addKeyListener(this.getShortcutKeyListener());
    
    // Create media player and ask for VLC path if necessary
    while(mediaPlayerComponent == null){
	    try{
	    NativeLibrary.addSearchPath("libvlc",  model.getVLCPath());
	    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
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
	  dataContent.addKeyListener(this.getShortcutKeyListener());
	  dataFrame.addKeyListener(this.getShortcutKeyListener());
  }
  
  /**
   * Creates the menu
   */
  private void buildMenu(){
	  // Create menu structure
	    menubar = new JMenuBar();
	    
    	// File menu
	    file 			= new JMenu("File");
	    openfile 		= new JMenuItem("Import file", 'I');
	    createIntervalTrack		= new JMenuItem("Create interval annotation track", 'C');
	    createPointsTrack		= new JMenuItem("Create point annotation track", 'P');
	    save 			= new JMenuItem("Save project", 'S');
	    saveNewProject 	= new JMenuItem("Save project as");
	    closeProject 	= new JMenuItem("Close project", 'Q');
	    exit 			= new JMenuItem("Exit"); 
	    
	    file.add(openfile);
	    file.add(createIntervalTrack);
	    file.add(createPointsTrack);
	    file.add(new JSeparator());
	    file.add(save);
	    file.add(saveNewProject);
	    file.add(closeProject);
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
	    globalzoomCheckbox 		= new JCheckBox("Global zoom");
	    globalzoomCheckbox.setSelected(true);
	    globalzoomCheckbox.setToolTipText("Sets all tracks to the same zoom level");
	    position 				= new JLabel("-");
	    position.setToolTipText("You can change playback speed using UP and DOWN on your keyboard");
	    positionslider			= new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
	    playPauseButton 		= new JButton(pauseIcon);
	    playPauseButton.setToolTipText("Play/Pause");
	    stopButton 				= new JButton(stopIcon);
	    stopButton.setToolTipText("Stop");
	    skipFrameButton 		= new JButton(skipFrameIcon);
	    skipFrameButton.setToolTipText("Forward one frame");
	    muteButton 				= new JButton(unmuteIcon);
	    muteButton.setToolTipText("Mute/Unmute");
    
	    menubar.add(file);
	    menubar.add(globalzoomCheckbox);
	    menubar.add(positionslider);
	    menubar.add(position);
	    menubar.add(playPauseButton);
	    menubar.add(stopButton);
	    menubar.add(skipFrameButton);
	    menubar.add(muteButton);
	    
	    // Set action commands for buttons
		save.setActionCommand("save");
		saveNewProject.setActionCommand("saveas");
		closeProject.setActionCommand("closeproject");
	    openfile.setActionCommand("openfile");
	    createIntervalTrack.setActionCommand("createintervaltrack");
	    createPointsTrack.setActionCommand("createpointstrack");
	    exit.setActionCommand("exit");
	    playPauseButton.setActionCommand("playpause");
	    stopButton.setActionCommand("stop");
	    skipFrameButton.setActionCommand("skipframe");
	    muteButton.setActionCommand("mute");
	    
	    // Add a MenuListener to all buttons
	    MenuListener menlis = new MenuListener(this);
	    save.addActionListener(menlis);
	    saveNewProject.addActionListener(menlis);
	    closeProject.addActionListener(menlis);
	    openfile.addActionListener(menlis);
	    createIntervalTrack.addActionListener(menlis);
	    createPointsTrack.addActionListener(menlis);
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
	  // Happens only if there is a problem loading vlc, in which case the gui will not show anyway
	  if(dataContent == null)
		  return;
	  
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
				  model.getLoadedDataTracks().get(i).getVisualization().getTrackVisualization().requestFocusInWindow();
		  }
		  
		  dataContent.setMinimumSize(new Dimension(100, model.getLoadedDataTracks().size() * 100));
		  dataContent.setPreferredSize(new Dimension(dataContent.getWidth(), model.getLoadedDataTracks().size() * 200));
		  
		  JScrollPane scroll = new JScrollPane(dataContent);
		  scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		  dataFrame.setContentPane(scroll);
		  
		  dataFrame.setTitle(windowTitle + model.getProjectPath());
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
  
  public KeyListener getShortcutKeyListener(){
	  return keylis;
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
	  videoFrame.setTitle(windowTitle + " - " + model.getVideoPath());
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
		stopProperly();
	}

	public void stopProperly() {
		// Stop thread
		vi.setRunning(false);
		
		// Stop media player properly
		mediaPlayerComponent.getMediaPlayer().stop();
		mediaPlayerComponent.getMediaPlayer().release();
		mediaPlayerComponent.getMediaPlayerFactory().release();
		mediaPlayerComponent.release();

		// Delete temp files
		ProjectFileHandler.deleteTemporaryFiles(new File("\\temp"));
		
		// Exit
		System.exit(0);	
	}

	public boolean getGlobalZoom() {
		return globalzoomCheckbox.isSelected();
	}

	public void setGlobalZoom(boolean globalZoom) {
		globalzoomCheckbox.setSelected(globalZoom);
	}

	public long getGlobalOffset() {
		return globalOffset;
	}

	public void setGlobalOffset(long globalOffset) {
		this.globalOffset = globalOffset;
		
		// Update visualizations
		  for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
				  model.getLoadedDataTracks().get(i).getVisualization().getTrackVisualization().repaint();
		  }
	}

	public float getGlobalPixelsPerMillisecond() {
		return globalPixelsPerMillisecond;
	}

	public void setGlobalPixelsPerMillisecond(float globalPixelsPerMillisecond) {
		this.globalPixelsPerMillisecond = globalPixelsPerMillisecond;
		
		// Update visualizations
		  for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
				  model.getLoadedDataTracks().get(i).getVisualization().getTrackVisualization().repaint();
		  }
	}

	public void increaseRate() {
		mediaPlayerComponent.getMediaPlayer().setRate(Math.min(10f, mediaPlayerComponent.getMediaPlayer().getRate() + 0.1f));	
	}
	public void decreaseRate() {
		mediaPlayerComponent.getMediaPlayer().setRate(Math.max(0.0f, mediaPlayerComponent.getMediaPlayer().getRate() - 0.1f));	
	}
}