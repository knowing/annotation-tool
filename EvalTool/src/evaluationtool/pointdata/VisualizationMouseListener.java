package evaluationtool.pointdata;

import java.awt.event.MouseEvent;

import evaluationtool.gui.TrackMouseListener;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.gui.Visualization;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener extends TrackMouseListener{

	// Editing points
	int draggedPoint;
	
	// Casts
	PointDataVisualization pointVis;
	PointTrackVisualization pointTrack;

	public VisualizationMouseListener(Visualization s, TrackVisualization tv) {
		super(s, tv);
		pointVis = (PointDataVisualization) s;
		pointTrack = (PointTrackVisualization) tv;
	}
	
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);		
		draggedPoint = -1;
	}
	
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		
		// Drag point
		if(e.getButton() == MouseEvent.BUTTON1 && !pointVis.isLocked()){
			draggedPoint = pointTrack.getPointAt(e.getPoint());
			if(draggedPoint != -1){
				// Set point to the cursor position
				pointVis.getDataSource().movePoint(draggedPoint, track.mapPixelToTime(e.getX() - pointVis.getDataSource().getPoint(draggedPoint)));
				source.repaint();
			}
		}
	}
	
	public void mouseClicked(MouseEvent e){
		super.mouseClicked(e);
		
		if(e.getButton() == MouseEvent.BUTTON3 && !pointVis.isLocked()){
			draggedPoint = pointTrack.getPointAt(e.getPoint());
			
			if(draggedPoint == -1)
				pointVis.getDataSource().addPoint(new Timestamp(pointVis.getDataSource().removeSettingsFromTimestamp(track.mapPixelToTime(e.getX()))));
			else
				pointVis.getDataSource().deletePoint(draggedPoint);
			
			source.repaint();
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		draggedPoint = -1;
	}
	
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);

		if(draggedPoint != -1 && !pointVis.isLocked()){
			pointVis.getDataSource().movePoint(draggedPoint, pointVis.getDataSource().removeSettingsFromTimestamp(track.mapPixelToTime(e.getX())));
			source.repaint();
		}
	}
}
