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
	int draggedEvent;
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
		
		draggedEvent = -1;
	}
	
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		
		// Drag startpoint, endpoint or a whole interval
		if(e.getButton() == MouseEvent.BUTTON1 && !intervalVis.isLocked()){
			draggedEvent = intervalTrack.getEventAt(e.getPoint());
			if(draggedEvent != -1){
				draggedPart = intervalTrack.getPartAt(e.getPoint());
				
				// Set point to the cursor position
				if(draggedPart == IntervalTrackVisualization.STARTPOINT){
					intervalVis.getDataSource().moveStartpoint(draggedEvent, track.mapPixelToTime(e.getX()));
				}
				else if(draggedPart == IntervalTrackVisualization.ENDPOINT){
					intervalVis.getDataSource().moveEndpoint(draggedEvent, track.mapPixelToTime(e.getX()));		
				}
			}
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		
		draggedEvent = -1;
	}
	
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		
			if(draggedEvent != -1 && intervalVis.getDataSource().getEvents().size() > draggedEvent){
				if(draggedPart == IntervalTrackVisualization.STARTPOINT){
					intervalVis.getDataSource().moveStartpoint(draggedEvent, track.mapPixelToTime(e.getX()));		
				}
				else if(draggedPart == IntervalTrackVisualization.ENDPOINT){
					intervalVis.getDataSource().moveEndpoint(draggedEvent, track.mapPixelToTime(e.getX()));		
				}
				else if(draggedPart == IntervalTrackVisualization.WHOLE_EVENT){
					intervalVis.getDataSource().moveEvent(draggedEvent, (e.getX() - tempMouseX) / intervalVis.getPixelsPerMillisecond());		
				}
				
				intervalVis.getDataSource().mergeOverlappingActivities(draggedEvent);
				source.repaint();
				tempMouseX = e.getX();
			}
	}
	
	/**
	 * Should be called when an activity is removed or merged, so draggedEvent does not point to a wrong or null element
	 */
	public void releaseEvent(){
		draggedEvent = -1;
	}
}
