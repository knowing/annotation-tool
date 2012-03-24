package evaluationtool.sensordata;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.gui.TrackMouseListener;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.gui.Visualization;
import evaluationtool.intervaldata.IntervalDataVisualization;
import evaluationtool.intervaldata.IntervalTrackVisualization;
import evaluationtool.sensordata.SensorTrackVisualization;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener extends TrackMouseListener implements MouseWheelListener, MouseListener, MouseMotionListener{
	
	// Casts
	SensorDataVisualization sensorVis;
	SensorTrackVisualization sensorTrack;

	public VisualizationMouseListener(Visualization s, SensorTrackVisualization tv) {
		super(s, tv);
		sensorVis = (SensorDataVisualization) s;
		sensorTrack = (SensorTrackVisualization) tv;
	}

	boolean shiftingTime = false;
	int tempMouseX = 0;
	int tempMouseY = 0;

	/**
	 * Toggles between compact and expanded view on MMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		
		if(e.getButton() == MouseEvent.BUTTON2){
				((SensorDataVisualization)sensorVis).toggleCompactView();
		}
		else if(e.getButton() == MouseEvent.BUTTON3){
			sensorVis.getPopupMenu(track.mapPixelToTime(e.getX())).show(track, e.getX(), e.getY());
		}
	}
}
