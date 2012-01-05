package evaluationtool.pointdata;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.pointdata.SensorTrackVisualization;

/**
 * Allows the manipulation of tracks with the mouse
 * @author anfi
 *
 */
public class VisualizationMouseListener implements MouseWheelListener, MouseListener, MouseMotionListener{
	
	SensorDataVisualization source;
	SensorTrackVisualization track;
	
	boolean shiftingTime = false;
	int tempMouseX = 0;
	int tempMouseY = 0;
	
	public VisualizationMouseListener(SensorDataVisualization s, SensorTrackVisualization tv) {
		source = s;
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
	 * Toggles between compact and expanded view on MMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON2){
				source.toggleCompactView();
		}
		// Set playback position to this point
				else if(e.getButton() == MouseEvent.BUTTON1){
					source.getDataSource().getModel().setPosition((long)track.mapPixelToTime(e.getX()));
				}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		source.showCoordinates(null);
		shiftingTime = false;
	}
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3){
			shiftingTime = true;
			tempMouseX = e.getX();
		}
	}
	public void mouseReleased(MouseEvent e) {
		shiftingTime = false;
	}
	
	public void mouseDragged(MouseEvent e) {

			if(shiftingTime){
				System.out.println("Shifted " + (e.getX() - tempMouseX) + " pixels: " + (e.getX() - tempMouseX) / source.getPixelsPerMillisecond() + " ms");
				source.setOffset((long)(source.getOffset() + (e.getX() - tempMouseX) / source.getPixelsPerMillisecond()));
				source.repaint();
				tempMouseX = e.getX();
			}

		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
	
	public void mouseMoved(MouseEvent e) {
		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
}
