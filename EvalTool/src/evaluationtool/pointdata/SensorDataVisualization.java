package evaluationtool.pointdata;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;

import evaluationtool.gui.Visualization;

public class SensorDataVisualization extends Visualization implements ComponentListener {
	
	// Menu
	JPanel menu = new JPanel();
	JButton remove = new JButton("Remove");
	JButton toggleView = new JButton("Compact view");
	JLabel dataInfo = new JLabel("info");
	
	// Track 
	TrackVisualization trackvis;
	
	// Data arrays
	SensorData dataSource;
	
	// Listener for menu
	MenuButtonListener menulistener = new MenuButtonListener(this);
	
	SensorDataVisualization(SensorData sd){
		dataSource = sd;
		trackvis = new TrackVisualization(sd, this);
		
		// Will be overwritten anyway, just set this in case repaint is called earlier
		trackvis.setAlternativeColorScheme(false);
		
		// Set Layout
		this.setLayout(new BorderLayout());
		
		updateLayout();
		
		this.addComponentListener(this);
		remove.addActionListener(menulistener);
		toggleView.addActionListener(menulistener);
		
		// Add tracks
		this.add(menu, BorderLayout.WEST);
		this.add(trackvis, BorderLayout.CENTER);
	}
	
	private void updateLayout(){
		if(menu != null){
			menu.removeAll();
			// Create menu
			menu.setLayout(new GridLayout(Math.max(3, this.getHeight() / 30), 1));
			menu.add(remove);
			menu.add(toggleView);
			menu.add(dataInfo);
		}
	}
	
	/**
	 * Sets the position for the cursor
	 */
	public void setPosition(float p){
		trackvis.setPosition(p);
	}
	
	/**
	 * Toggles the view mode
	 */
	protected void toggleCompactView(){
		trackvis.toggleCompactView();
		
		// Set correct button text
		if(trackvis.isCompactView())
			toggleView.setText("Expanded view");
		else
			toggleView.setText("Compact view");
	}
	
	
	protected float getZoomlevel(){
		return trackvis.getZoomlevel();
	}

	/**
	 * Sets the zoom level
	 * @param z
	 */
	protected void setZoomlevel(float z){
		trackvis.setZoomlevel(z);
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
}