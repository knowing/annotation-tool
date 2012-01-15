package evaluationtool.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Lets the user zoom and move within the track
 * @author anfi
 *
 */
public class TrackMouseListener implements MouseWheelListener, MouseListener, MouseMotionListener{
	
	protected Visualization source;
	protected TrackVisualization track;
	
	protected boolean shiftingTime = false;
	
	protected long draggingStartTime;
	
	protected int tempMouseX = 0;
	protected int tempMouseY = 0;
	
	public TrackMouseListener(Visualization s, TrackVisualization tv) {
		source = s;
		track = tv;
	}
	
	/**
	 * Changes the zoomlevel on mouse wheel rotation
	 * @param e
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		long mouseTime = track.mapPixelToTime(e.getX());
		
		// Do not allow zoomlevel to get < 1 or > 3e8f
		if(e.getWheelRotation() < 0)
			track.setZoomlevel(track.getZoomlevel() * 1.3f);
		else if(e.getWheelRotation() > 0)
			track.setZoomlevel(track.getZoomlevel() / 1.3f);
		
		/*
		 *  Set offset so that the track zooms onto the current mouse position
		 */
		track.adjustOffset(mouseTime, e.getX());
		
		// Sync with global view
		if(source.getDataSource().getModel().getGUI().getGlobalZoom()){
			source.getDataSource().getModel().getGUI().setGlobalPixelsPerMillisecond(track.calculatePixelsPerMillisecond());
			source.getDataSource().getModel().getGUI().setGlobalOffset(track.getOffset());
		}
	}

	public void mouseClicked(MouseEvent e) {

		// Set playback position to this point
		if(e.getButton() == MouseEvent.BUTTON1){
			source.getDataSource().getModel().setPlaybackPosition((long)track.mapPixelToTime(e.getX()));
		}
	}
	public void mouseEntered(MouseEvent e) {
		track.requestFocusInWindow();
	}
	public void mouseExited(MouseEvent e) {
		track.showCoordinates(null);
		shiftingTime = false;
	}
	public void mousePressed(MouseEvent e) {
		tempMouseX = e.getX();
		
		// Move offset
		if(e.getButton() == MouseEvent.BUTTON3){
			shiftingTime = true;
		}
	}
	public void mouseReleased(MouseEvent e) {
		shiftingTime = false;
	}
	
	public void mouseDragged(MouseEvent e) {

			if(shiftingTime){
				
				long newOffset = (long)(track.getOffset() + (e.getX() - tempMouseX) / track.getPixelsPerMillisecond());
				
				if(source.getDataSource().getModel().getGUI().getGlobalZoom()){
					source.getDataSource().getModel().getGUI().setGlobalOffset(newOffset);
				}
				else{
					track.setOffset(newOffset);
				}
				tempMouseX = e.getX();
			}
		
		track.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
	
	public void mouseMoved(MouseEvent e) {
		track.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
}
