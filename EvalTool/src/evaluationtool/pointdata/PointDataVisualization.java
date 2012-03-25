package evaluationtool.pointdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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


public class PointDataVisualization extends Visualization implements ComponentListener, ActionListener {
	
	// Menu
	JPanel menu = new JPanel();
	JButton name = new JButton();
	JButton remove = new JButton("Remove");
	JButton toggleLock = new JButton("Lock");
	JButton trackParameters = new JButton("");
	
	// Track 
	PointTrackVisualization trackvis;
	
	// PopupMenu
	JPopupMenu popupMenu = new JPopupMenu();
	JMenuItem syncPositionItem;
	long menuPosition = 0;
	
	// Data arrays
	PointData dataSource;
	
	// Listener for menu
	MenuButtonListener menulistener = new MenuButtonListener(this);
	
	// options dialog
	TrackOptionsDialog to;
	
	PointDataVisualization(PointData sd){
		dataSource = sd;
		trackvis = new PointTrackVisualization(sd, this);
		
		// Will be overwritten anyway, just set this in case repaint is called earlier
		trackvis.setAlternativeColorScheme(false);
		
		// PopupMenu
		popupMenu = new JPopupMenu();
		syncPositionItem = new JMenuItem("Synchronize this point to current playback position");
		popupMenu.add(syncPositionItem);
		syncPositionItem.addActionListener(this);
		
		// Set Layout
		this.setLayout(new BorderLayout());
		
		updateLayout();
		
		this.addComponentListener(this);
		remove.addActionListener(menulistener);
		toggleLock.addActionListener(menulistener);
		toggleLock.setActionCommand("togglelocked");
		name.addActionListener(menulistener);
		name.setActionCommand("changename");
		trackParameters.addActionListener(menulistener);
		trackParameters.setActionCommand("options");
		
		// Add tracks
		menu.setPreferredSize(new Dimension(200, 100));
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
			
			name.setText(getDataSource().getSource());
			
			// Create menu
			menu.setLayout(new GridLayout(Math.max(4, this.getHeight() / 30), 1));
			menu.add(name);
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
	
	public PointData getDataSource(){
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

	public PointTrackVisualization getTrackVisualization() {
		return trackvis;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == syncPositionItem){
			dataSource.setOffset(dataSource.getOffset() - menuPosition + this.getTrackVisualization().getPosition());
		}
	}

	public JPopupMenu getPopupMenu(long mapPixelToTime) {
		menuPosition = mapPixelToTime;
		return popupMenu;
	}

	public void rename(String newname) {
		dataSource.setSource(newname);
		name.setText(newname);
	}
}
