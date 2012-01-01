package evaluationtool.intervaldata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JPanel;

import evaluationtool.gui.TrackOptionsDialog;
import evaluationtool.gui.Visualization;


public class IntervalDataVisualization extends Visualization implements ComponentListener {
	
	// Menu
	JPanel menu = new JPanel();
	JButton remove = new JButton("Remove");
	JButton toggleView = new JButton("Compact view");
	JButton trackParameters = new JButton("");
	
	// Track 
	TrackVisualization trackvis;
	
	// Data arrays
	IntervalData dataSource;
	
	// Listener for menu
	MenuButtonListener menulistener = new MenuButtonListener(this);
	
	// options dialog
	TrackOptionsDialog to;
	
	IntervalDataVisualization(IntervalData sd){
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
		trackParameters.addActionListener(menulistener);
		trackParameters.setActionCommand("options");
		
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
			// Create menu
			menu.setLayout(new GridLayout(Math.max(3, this.getHeight() / 30), 1));
			menu.add(remove);
			menu.add(toggleView);
			menu.add(trackParameters);
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
	protected void toggleEditable(){
		trackvis.toggleEditable();
		
		// Set correct button text
		if(trackvis.isEditable())
			toggleView.setText("Editable");
		else
			toggleView.setText("Not editable");
	}
	
	
	protected float getZoomlevel(){
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
	protected void setZoomlevel(float z){
		trackvis.setZoomlevel(z);
	}
	
	protected void setOffset(float o){
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
}
