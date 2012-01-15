package evaluationtool.intervaldata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.Data;
import evaluationtool.gui.TrackVisualization;
import evaluationtool.util.TimestampConverter;

public class IntervalTrackVisualization extends TrackVisualization{
	
	// Statics to determine which part of an event is selected
	public static int STARTPOINT 	= 1;
	public static int ENDPOINT 		= 2;
	public static int WHOLE_EVENT 	= 3;

	// Painting variables
	private int n_events 			= 0;
	private int n_activities 		= 0;
	private Activity[] events 		= null;

	// Data source
	IntervalData dataSource;
	
	// For every Interval, there is a Rectangle2D.Float for start and end
	RoundRectangle2D.Float[] intervals;
	RoundRectangle2D.Float[] startpoints;
	RoundRectangle2D.Float[] endpoints;
	
	// Listener for everything
	VisualizationMouseListener listener;
	
	// Minimized view
	boolean minimize = false;
	
	public IntervalTrackVisualization(Data sd, IntervalDataVisualization sdv) {
		super(sd);
		dataSource = (IntervalData)sd;
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
		events = dataSource.getEvents();
		n_events = events.length;
		n_activities = dataSource.getPossibleActivities().length;
	}
	
	/**
	 * Paints the tracks 
	 * @param g2d
	 */
	public void paintTracks(Graphics2D g2d){
		// Create start, end and interval Rectangles
		createRectangles();
							
		g2d.setStroke(new BasicStroke(1));
	
		// Draw data
		for(int i = 0; i < n_events; i++){
				
					Point p = this.getMousePosition();
					if(p != null && (intervals[i].contains(p)
													    && !startpoints[i].contains(p)
													    && !(endpoints[i] != null && endpoints[i].contains(p)))){
						g2d.setColor(new Color((events[i].activitytype * 30) % 255, (events[i].activitytype * 75) % 255, (events[i].activitytype * 120) % 255, 210));
						g2d.fill(intervals[i]);
						
						displayInformationAbout(i, g2d);
					}
					else{
						g2d.setColor(new Color((events[i].activitytype * 30) % 255, (events[i].activitytype * 75) % 255, (events[i].activitytype * 120) % 255, 150));
						g2d.fill(intervals[i]);
					}
					
					if(!dataSource.isLocked()){
						// Draw start and end
						g2d.setColor(Color.GREEN);
						if(p != null && startpoints[i].contains(p))
							g2d.fill(startpoints[i]);
						else
							g2d.draw(startpoints[i]);
						
						g2d.setColor(Color.RED);
						if(endpoints[i] != null){
							if(p != null && endpoints[i].contains(p))
								g2d.fill(endpoints[i]);
							else
								g2d.draw(endpoints[i]);
						}
					}
		}
		
		// Draw table
		for(int i = 0; i < n_activities; i++){
			g2d.setColor(Color.BLACK);
			g2d.drawLine(0, (int)((float)(this.getHeight() - TIMELINE_HEIGHT) * i / n_activities), this.getWidth(), (int)((float)(this.getHeight() - TIMELINE_HEIGHT) * i / n_activities));
		}
		
		// Draw x-axis
		g2d.setColor(Color.BLACK);
		
		// Draw table
		for(int i = 0; i < dataSource.getPossibleActivities().length; i++){
			g2d.drawLine(0, i / n_activities, this.getWidth(), i / n_activities);
			g2d.drawString(dataSource.getPossibleActivities()[i], 
						      this.getWidth() / 2 - g2d.getFontMetrics(g2d.getFont()).stringWidth(dataSource.getPossibleActivities()[i]) / 2, 
						      i * (this.getHeight() - TIMELINE_HEIGHT) / n_activities + 14);
		}	
	}
	
	/**
	 * Shows information about activity i
	 * @param i
	 * @param g2d
	 */
	private void displayInformationAbout(int i, Graphics2D g2d) {
		coordinatesPopup = null;
		
		// Show additional information about one event
		String infoStart;
		
		float boxHeight = g2d.getFontMetrics().getHeight() + 2f;
		int boxY = (int)(intervals[i].getY() + intervals[i].getHeight() / 2 - boxHeight / 2);
		
		// Start info
		infoStart = "From: " + TimestampConverter.getVideoTimestamp(events[i].timestampStart);
		RoundRectangle2D.Float infoStartBox = new RoundRectangle2D.Float(0, boxY, 
				g2d.getFontMetrics().stringWidth(infoStart) + 15f, boxHeight,
				10f, 10f);
		
		// If there is a defined end, show info about end and total length
		if(events[i].timestampEnd != 0){
			String infoEnd, infoLength;
			infoLength  = "Length: " + TimestampConverter.getVideoTimestamp(events[i].timestampEnd - events[i].timestampStart);
			infoEnd = "To: " + TimestampConverter.getVideoTimestamp(events[i].timestampEnd);
			
			RoundRectangle2D.Float infoEndBox = new RoundRectangle2D.Float(this.getWidth() - (g2d.getFontMetrics().stringWidth(infoEnd) + 15f), boxY, 
					g2d.getFontMetrics().stringWidth(infoEnd) + 15f, boxHeight,
					10f, 10f);
			
			int center = Math.max(0, (int)(intervals[i].getX() + intervals[i].getWidth() / 2 + 15 - g2d.getFontMetrics().stringWidth(infoLength) / 2 - 8));
			RoundRectangle2D.Float infoLengthBox = new RoundRectangle2D.Float(Math.min(center, this.getWidth() - (g2d.getFontMetrics().stringWidth(infoLength) + 15f)), boxY, 
					g2d.getFontMetrics().stringWidth(infoLength) + 15f, boxHeight,
					10f, 10f);
			
			if(infoLengthBox.intersects(new Rectangle2D.Float(infoStartBox.x, 	infoStartBox.y, infoStartBox.width, infoStartBox.height))){
				infoStartBox.y -= infoStartBox.height / 2;
				infoLengthBox.y += infoStartBox.height / 2;
			}
			if(infoLengthBox.intersects(new Rectangle2D.Float(infoEndBox.x, 	infoEndBox.y, 	infoEndBox.width, 	infoEndBox.height))){
				infoEndBox.y -= infoEndBox.height / 2;
				infoLengthBox.y += infoEndBox.height / 2;
			}
			
			// Draw and fill rectangles, then draw Strings
			g2d.setColor(timelineColorTrack);
			g2d.fill(infoEndBox);
			g2d.fill(infoLengthBox);
			g2d.setColor(Color.WHITE);
			g2d.draw(infoEndBox);
			g2d.draw(infoLengthBox);
			g2d.setColor(fontColorTrack);
			g2d.drawString(infoEnd, (int)infoEndBox.getX() + 7, (int)(infoEndBox.getY() + 13));
			g2d.drawString(infoLength, (int)infoLengthBox.getX() + 7, (int)(infoLengthBox.getY() + 13));
		}
		
		g2d.setColor(timelineColorTrack);
		g2d.fill(infoStartBox);
		g2d.setColor(Color.WHITE);
		g2d.draw(infoStartBox);
		g2d.setColor(fontColorTrack);
		g2d.drawString(infoStart, (int)infoStartBox.getX() + 7, (int)(infoStartBox.getY() + 13));
	}

	/**
	 * Creates all event-related rectangles that need to be drawn
	 */
	private void createRectangles(){
		
		intervals = new RoundRectangle2D.Float[n_events];
		startpoints = new RoundRectangle2D.Float[n_events];
		endpoints = new RoundRectangle2D.Float[n_events];
		
		for(int i = 0; i < n_events; i++){
			
			// Create startpoint
			startpoints[i] = 	new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart) - 5, (float)events[i].activitytype / n_activities * (this.getHeight() - TIMELINE_HEIGHT), 
						   							  10,  (float)(this.getHeight() - TIMELINE_HEIGHT) / n_activities - 1, 7, 7);
			
			if(events[i].timestampEnd != 0){
				// Create interval
				intervals[i] = 		new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart), (float)events[i].activitytype / n_activities * (this.getHeight() - TIMELINE_HEIGHT), 
													   	  mapTimeToPixel((float)events[i].timestampEnd) - mapTimeToPixel((float)events[i].timestampStart),  (float)(this.getHeight() - TIMELINE_HEIGHT) / n_activities,
													   	  1, 1);
				// Create endpoint
				endpoints[i] = 		new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampEnd) - 5, (float)events[i].activitytype / n_activities * (this.getHeight() - TIMELINE_HEIGHT), 
					   	  								   10,  (float)(this.getHeight() - TIMELINE_HEIGHT) / n_activities - 1, 
					   	  								   7, 7);

			}
			else{
				intervals[i] = new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart), (float)events[i].activitytype / n_activities * (this.getHeight() - TIMELINE_HEIGHT), 
						   					    this.getWidth() - mapTimeToPixel((float)events[i].timestampStart), (float)(this.getHeight() - TIMELINE_HEIGHT) / n_activities, 1, 1);
				endpoints[i] = null;			
			}		
		}
	}

	public int mapPixelToActivity(int y) {
		return (int)((float)y / this.getHeight() * n_activities);
	}

	/**
	 * Returns underlying event
	 * @param p
	 * @return
	 */
	public Activity getEventAt(Point p) {
		for(int i = 0; i < n_events; i++){
			// Endpoints have to be checked for null pointer
			if(startpoints[i].contains(p) || (endpoints[i] != null && endpoints[i].contains(p)) || intervals[i].contains(p))
				return events[i];
		}
		
		return null;
	}
	
	/**
	 * Returns which part of the event is selected
	 * @param p
	 * @return 1 for 
	 */
	public int getPartAt(Point p){
		for(int i = 0; i < n_events; i++){
			if(startpoints[i].contains(p))
				return STARTPOINT;
			if(endpoints[i] != null && endpoints[i].contains(p))
				return ENDPOINT;
			if(intervals[i].contains(p))
				return WHOLE_EVENT;
		}
		
		// This should not happen, as getEventAt should be called before
		return 0;
	}
}
