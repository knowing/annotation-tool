package evaluationtool.pointdata;

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
	
	SensorDataVisualization source;
	
	boolean dragging = false;
	int tempMouseX = 0;
	int tempMouseY = 0;
	
	public VisualizationMouseListener(SensorDataVisualization s) {
		source = s;
	}
	
	/**
	 * Changes the zoomlevel on mouse wheel rotation
	 * @param e
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Do not allow zoomlevel to get < 1 or > 3e8f
		if(e.getWheelRotation() < 0)
			source.setZoomlevel(Math.min(3e8f, source.getZoomlevel() * 1.3f));
		else if(e.getWheelRotation() > 0)
			source.setZoomlevel(Math.max(1, source.getZoomlevel() / 1.3f));
	}

	/**
	 * Toggles between compact and expanded view on MMB
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON2){
				source.toggleCompactView();
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		source.showCoordinates(null);
		dragging = false;
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
}
