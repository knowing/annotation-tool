package evaluationtool.intervaldata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import evaluationtool.gui.TrackOptionsDialog;
import evaluationtool.gui.Visualization;


public class IntervalDataVisualization extends Visualization implements ComponentListener {
	
	// Menu
	JPanel menu = new JPanel();
	JLabel file = new JLabel();
	JButton remove = new JButton("Remove");
	JButton toggleLock = new JButton("Lock");
	JButton trackParameters = new JButton("");
	
	// PopupMenu
	JPopupMenu popupMenu = new JPopupMenu();
	JMenuItem startActivityItem;
	JMenuItem endActivityItem;
	JMenuItem deleteActivityItem;
	JMenuItem syncPositionItem;
	// The current position of the popupmenu in milliseconds relative to this track
	long currentMenuTime = 0;
	int currentMenuActivity = 0;
	
	// Track 
	IntervalTrackVisualization trackvis;
	
	// Data arrays
	IntervalData dataSource;
	
	// Listener for menu
	MenuButtonListener menulistener = new MenuButtonListener(this);
	
	// options dialog
	TrackOptionsDialog to;
	
	IntervalDataVisualization(IntervalData sd){
		dataSource = sd;
		trackvis = new IntervalTrackVisualization(sd, this);
		
		// Will be overwritten anyway, just set this in case repaint is called earlier
		trackvis.setAlternativeColorScheme(false);
		
		// Set Layout
		this.setLayout(new BorderLayout());
		
		updateLayout();
		
		this.addComponentListener(this);
		remove.addActionListener(menulistener);
		toggleLock.addActionListener(menulistener);
		toggleLock.setActionCommand("togglelocked");
		trackParameters.addActionListener(menulistener);
		trackParameters.setActionCommand("options");
		
		PopupMenuListener popuplistener = new PopupMenuListener(popupMenu, this);
		
		// Add popupmenu items
		
		startActivityItem = new JMenuItem("Start activity");
		endActivityItem = new JMenuItem("End activity");
		deleteActivityItem = new JMenuItem("Delete activity");
		syncPositionItem = new JMenuItem("Synchronize this point to current playback position");
		
		startActivityItem.setActionCommand("#" + Activity.CURRENT_ACTIVITY);
		endActivityItem.setActionCommand("#" + Activity.NO_ACTIVITY);
		deleteActivityItem.setActionCommand("#" + Activity.DELETE_ACTIVITY);
		
		popupMenu.add(startActivityItem);
		startActivityItem.addActionListener(popuplistener);
		popupMenu.add(endActivityItem);
		endActivityItem.addActionListener(popuplistener);
		popupMenu.add(deleteActivityItem);
		deleteActivityItem.addActionListener(popuplistener);
		popupMenu.add(syncPositionItem);
		syncPositionItem.addActionListener(popuplistener);
		
		// Add tracks
		menu.setPreferredSize(new Dimension(200, this.getHeight()));
		this.add(menu, BorderLayout.WEST);
		this.add(trackvis, BorderLayout.CENTER);
		
		// Info dialog
		to = new TrackOptionsDialog(null, dataSource);
		updateInfo();
		
		// Update Layout
		updateLayout();
	}
	
	public void updateLayout(){
		if(menu != null){
			menu.setPreferredSize(new Dimension(200, this.getHeight()));
			
			menu.removeAll();
			
			file.setText(getDataSource().getSource());
			
			// Create menu
			menu.setLayout(new GridLayout(Math.max(4, this.getHeight() / 30), 1));
			menu.add(file);
			menu.add(remove);
			menu.add(toggleLock);
			menu.add(trackParameters);
		}
	}
	
	/**
	 * Sets the position for the cursor
	 */
	public void setPosition(long p){
		trackvis.setPosition(p);
	}
	
	/**
	 * Toggles the view mode
	 */
	public void toggleLocked(){
		dataSource.setLocked(!dataSource.isLocked());
		
		// Set correct button text
		if(dataSource.isLocked()){
			toggleLock.setText("Unlock");
		}
		else{
			toggleLock.setText("Lock");
			// Set source to empty string so the track will be saved
			dataSource.setSource("");
		}
	}
	
	public boolean isLocked(){
		return dataSource.isLocked();
	}
	
	public void updatePopupMenuForTimestamp(long timestamp, int activity){
		currentMenuTime = timestamp;
		currentMenuActivity = activity;
		
		startActivityItem.setEnabled(!isLocked());
		endActivityItem.setEnabled(!isLocked());
		deleteActivityItem.setEnabled(!isLocked());
	}
	
	public JPopupMenu getPopupMenu(){
		return popupMenu;
	}
	
	public long getCurrentMenuTime(){
		return currentMenuTime;
	}
	
	public int getCurrentMenuActivity(){
		return currentMenuActivity;
	}
	
	public IntervalData getDataSource(){
		return dataSource;
	}
	
	public float getZoomlevel(){
		return trackvis.getZoomlevel();
	}
	
	protected float getOffset(){
		return trackvis.getOffset();
	}
	
	protected float getPixelsPerMillisecond(){
		return trackvis.getPixelsPerMillisecond();
	}
	
	public void updateInfo(){
		trackParameters.setText("Offset: " + dataSource.getOffset() + ", speed: " + dataSource.getPlaybackSpeed() + "X");
	}
	
	public void showOptionsDialog(){
		to.refresh();
		to.setVisible(true);
	}

	/**
	 * Sets the zoom level
	 * @param z
	 */
	public void setZoomlevel(float z){
		trackvis.setZoomlevel(z);
	}
	
	protected void setOffset(long o){
		trackvis.setOffset(o);
	}
	
	/**
	 * Removes the track from gui
	 */
	protected void remove(){
		dataSource.remove();
	}

	public void setAlternativeColorScheme(boolean b) {
		trackvis.setAlternativeColorScheme(b);
	}
	
	public void showCoordinates(RoundRectangle2D.Float f){
		trackvis.showCoordinates(f);
	}
	
	  public void componentHidden(ComponentEvent e) {}
	  public void componentMoved(ComponentEvent e) {}
	  public void componentResized(ComponentEvent e) {
		  updateLayout();
	  }
	  public void componentShown(ComponentEvent e) {}

	public IntervalTrackVisualization getTrackVisualization() {
		return trackvis;
	}
}
