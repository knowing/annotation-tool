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
	TrackVisualization track;
	
	boolean dragging = false;
	int tempMouseX = 0;
	int tempMouseY = 0;
	
	public VisualizationMouseListener(IntervalDataVisualization s, TrackVisualization tv) {
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
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		source.showCoordinates(null);
		dragging = false;
	}
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3)
			dragging = true;
			tempMouseX = e.getX();
	}
	public void mouseReleased(MouseEvent e) {
		if(dragging && e.getButton() == MouseEvent.BUTTON3){
			source.setOffset((long)(source.getOffset() + (e.getX() - tempMouseX) / source.getPixelsPerMillisecond()));
			dragging = false;
			source.repaint();
		}
	}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		source.showCoordinates(new RoundRectangle2D.Float((float)e.getX(), (float)e.getY() - 15f, 80f, 15f, 10f, 10f));
	}
}
