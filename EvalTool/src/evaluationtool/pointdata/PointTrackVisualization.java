package evaluationtool.pointdata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.Data;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.util.TimestampConverter;

public class PointTrackVisualization extends TrackVisualization{

	// Painting variables
	private int n_events 			= 0;
	long[] points;
	
	// Data source
	PointData dataSource;
	
	// For every Interval, there is a Rectangle2D.Float for start and end
	RoundRectangle2D.Float[] pointRectangles;
	
	// Listener for everything
	VisualizationMouseListener listener;
	
	public PointTrackVisualization(Data sd, PointDataVisualization sdv) {
		super(sd);
		dataSource = (PointData)sd;
		model = dataSource.getModel();
		
		// Add listener with reference to visualization main component
		listener = new VisualizationMouseListener(sdv, this);
		
		addMouseWheelListener(listener);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		
		// no mouse pointer needed
		this.setCursor(null);
	}
	
	public void updateDataReferences(){
		// Update data reference
		points = dataSource.getPoints();
		n_events = points.length;
	}
	
	/**
	 * Paints the tracks 
	 * @param g2d
	 */
	public void paintTracks(Graphics2D g2d){
		// Create start, end and interval Rectangles
		createRectangles();
							
		g2d.setStroke(new BasicStroke(1));
		int additionalInfo = -1;
	
		// Draw data
		for(int i = 0; i < n_events; i++){
								
					if(this.getMousePosition() != null && (pointRectangles[i].contains(this.getMousePosition()))){
						if(dataSource.isLocked())
							g2d.setColor(Color.BLACK);
						else	
							g2d.setColor(Color.RED);
						
						g2d.fill(pointRectangles[i]);
						g2d.setColor(Color.BLACK);
						g2d.draw(pointRectangles[i]);
						
						additionalInfo = i;
					}
					else{
						if(dataSource.isLocked())
							g2d.setColor(Color.BLACK);
						else
							g2d.setColor(new Color(0, 0, 0, 180));
						
						g2d.fill(pointRectangles[i]);
					}
		}
		
		if(additionalInfo != -1)
			displayInformationAbout(additionalInfo, g2d);
	}
	
	/**
	 * Shows information about activity i
	 * @param i
	 * @param g2d
	 */
	private void displayInformationAbout(int i, Graphics2D g2d) {
		coordinatesPopup = null;
		
		// Show additional information about one event
		String info;
		
		float boxHeight = g2d.getFontMetrics().getHeight() + 2f;
		int boxY = (int)(pointRectangles[i].getY() + pointRectangles[i].getHeight() / 2 - boxHeight / 2);
		
		// Start info
		info = TimestampConverter.getVideoTimestamp(points[i]);
		Rectangle2D.Float infoBox = new Rectangle2D.Float(0, boxY, this.getWidth(), boxHeight);
		
		g2d.setColor(timelineColorTrack);
		g2d.fill(infoBox);
		g2d.setColor(Color.WHITE);
		g2d.draw(infoBox);
		g2d.setColor(fontColorTrack);
		g2d.drawString(info, (int)(pointRectangles[i].getX() - g2d.getFontMetrics().stringWidth(info) / 2), (int)(infoBox.getY() + 13));
	}

	/**
	 * Creates all event-related rectangles that need to be drawn
	 */
	private void createRectangles(){
		
		pointRectangles = new RoundRectangle2D.Float[n_events];
		
		for(int i = 0; i < n_events; i++){
			pointRectangles[i] = new RoundRectangle2D.Float(mapTimeToPixel(points[i]) - 5, 1, 10,  this.getHeight() - TIMELINE_HEIGHT - 2f, 3, 3);	
		}
	}

	public int getPointAt(Point point) {
		for(int i = 0; i < n_events; i++){
			if(pointRectangles[i].contains(point))
				return i;
		}
		
		return -1;
	}
}