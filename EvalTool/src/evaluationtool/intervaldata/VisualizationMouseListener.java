package evaluationtool.intervaldata;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener implements MouseWheelListener, MouseListener, MouseMotionListener{
	
	IntervalDataVisualization source;
	IntervalTrackVisualization track;
	
	boolean shiftingTime = false;
	
	// Editing activities
	DataSet draggedEvent;
	int draggedPart;
	long draggingStartTime;
	
	int tempMouseX = 0;
	int tempMouseY = 0;
	
	public VisualizationMouseListener(IntervalDataVisualization s, IntervalTrackVisualization tv) {
		source = s;
		track = tv;
	}
	
	/**
	 * Changes the zoomlevel on mouse wheel rotation
	 * @param e
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Do not allow zoomlevel to get < 1 or > 3e8f
		if(e.getWheelRotation() < 0)
			source.setZoomlevel(source.getZoomlevel() * 1.3f);
		else if(e.getWheelRotation() > 0)
			source.setZoomlevel(source.getZoomlevel() / 1.3f);
	}

	/**
	 * Opens the popup menu on RMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		
		if(e.getButton() == MouseEvent.BUTTON3){
			source.updatePopupMenuForTimestamp(track.mapPixelToTime(e.getX()), track.mapPixelToActivity(e.getY()));
        	source.getPopupMenu().show(track, e.getX(), e.getY());
		}
		// Set playback position to this point
		else if(e.getButton() == MouseEvent.BUTTON1){
			source.getDataSource().getModel().setPlaybackPosition((long)track.mapPixelToTime(e.getX()));
		}
	}
	public void mouseEntered(MouseEvent e) {
		track.requestFocusInWindow();
	}
	public void mouseExited(MouseEvent e) {
		source.showCoordinates(null);
		shiftingTime = false;
		draggedEvent = null;
	}
	public void mousePressed(MouseEvent e) {
		tempMouseX = e.getX();
		
		// Move offset
		if(e.getButton() == MouseEvent.BUTTON3){
			shiftingTime = true;
		}
		// Drag startpoint, endpoint or a whole interval
		else if(e.getButton() == MouseEvent.BUTTON1 && !track.isLocked()){
			draggedEvent = track.getEventAt(e.getPoint());
			if(draggedEvent != null){
				draggedPart = track.getPartAt(e.getPoint());
				
				// Set point to the cursor position
				if(draggedPart == IntervalTrackVisualization.STARTPOINT){
					draggedEvent.timestampStart = track.mapPixelToTime(e.getX());
				}
				else if(draggedPart == IntervalTrackVisualization.ENDPOINT){
					draggedEvent.timestampEnd = track.mapPixelToTime(e.getX());				
				}
			}
		}
	}
	public void mouseReleased(MouseEvent e) {
		shiftingTime = false;
		draggedEvent = null;
	}
	
	public void mouseDragged(MouseEvent e) {

			if(shiftingTime){
				source.setOffset((long)(source.getOffset() + (e.getX() - tempMouseX) / source.getPixelsPerMillisecond()));
				source.repaint();
				tempMouseX = e.getX();
			}
			if(draggedEvent != null){
				if(draggedPart == IntervalTrackVisualization.STARTPOINT){
					draggedEvent.timestampStart += (e.getX() - tempMouseX) / source.getPixelsPerMillisecond();
				}
				else if(draggedPart == IntervalTrackVisualization.ENDPOINT){
					draggedEvent.timestampEnd += (e.getX() - tempMouseX) / source.getPixelsPerMillisecond();				
				}
				else if(draggedPart == IntervalTrackVisualization.WHOLE_EVENT){
					draggedEvent.timestampStart += (e.getX() - tempMouseX) / source.getPixelsPerMillisecond();
					draggedEvent.timestampEnd += (e.getX() - tempMouseX) / source.getPixelsPerMillisecond();
				}
				source.repaint();
				tempMouseX = e.getX();
			}

		
		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
	
	public void mouseMoved(MouseEvent e) {
		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
}
