package evaluationtool.intervaldata;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.gui.TrackMouseListener;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.gui.Visualization;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener extends TrackMouseListener{

	// Editing activities
	Activity draggedEvent;
	int draggedPart;
	
	// Casts
	IntervalDataVisualization intervalVis;
	IntervalTrackVisualization intervalTrack;

	public VisualizationMouseListener(Visualization s, TrackVisualization tv) {
		super(s, tv);
		intervalVis = (IntervalDataVisualization) s;
		intervalTrack = (IntervalTrackVisualization) tv;
	}
	
	/**
	 * Opens the popup menu on RMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		
		if(e.getButton() == MouseEvent.BUTTON3){
			intervalVis.updatePopupMenuForTimestamp(track.mapPixelToTime(e.getX()), intervalTrack.mapPixelToActivity(e.getY()));
			intervalVis.getPopupMenu().show(track, e.getX(), e.getY());
		}
	}

	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		
		draggedEvent = null;
	}
	
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		
		// Drag startpoint, endpoint or a whole interval
		if(e.getButton() == MouseEvent.BUTTON1 && !intervalVis.isLocked()){
			draggedEvent = intervalTrack.getEventAt(e.getPoint());
			if(draggedEvent != null){
				draggedPart = intervalTrack.getPartAt(e.getPoint());
				
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
		super.mouseReleased(e);
		
		draggedEvent = null;
	}
	
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		
			if(draggedEvent != null){
				if(draggedPart == IntervalTrackVisualization.STARTPOINT){
					draggedEvent.timestampStart += (e.getX() - tempMouseX) / intervalVis.getPixelsPerMillisecond();
					
					if(draggedEvent.timestampEnd != 0)
						draggedEvent.timestampEnd = Math.max(draggedEvent.timestampStart, draggedEvent.timestampEnd);
				}
				else if(draggedPart == IntervalTrackVisualization.ENDPOINT){
					draggedEvent.timestampEnd += (e.getX() - tempMouseX) / intervalVis.getPixelsPerMillisecond();
					draggedEvent.timestampStart = Math.min(draggedEvent.timestampStart, draggedEvent.timestampEnd);
				}
				else if(draggedPart == IntervalTrackVisualization.WHOLE_EVENT){
					draggedEvent.timestampStart += (e.getX() - tempMouseX) / intervalVis.getPixelsPerMillisecond();
					draggedEvent.timestampEnd += (e.getX() - tempMouseX) / intervalVis.getPixelsPerMillisecond();
				}
				
				intervalVis.getDataSource().mergeOverlappingActivities(draggedEvent);
				source.repaint();
				tempMouseX = e.getX();
			}
	}
}
