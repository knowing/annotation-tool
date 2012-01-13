package evaluationtool.intervaldata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;

import evaluationtool.Data;
import evaluationtool.gui.TrackVisualization;

public class IntervalTrackVisualization extends TrackVisualization{
	
	// Statics to determine which part of an event is selected
	public static int STARTPOINT 	= 1;
	public static int ENDPOINT 		= 2;
	public static int WHOLE_EVENT 	= 3;

	// Painting variables
	private int n_events = 0;
	private int n_activities = 0;
	private DataSet[] events = null;

	// Data arrays
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
				
					g2d.setColor(new Color((events[i].activitytype * 30) % 255, (events[i].activitytype * 75) % 255, (events[i].activitytype * 120) % 255, 150));
					g2d.fill(intervals[i]);
					
					if(!dataSource.isLocked()){
						// Draw start and end
						g2d.setColor(Color.GREEN);
						g2d.draw(startpoints[i]);
						
						g2d.setColor(Color.RED);
						if(endpoints[i] != null)
							g2d.draw(endpoints[i]);
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
						      (i + 0.5f) * (this.getHeight() - TIMELINE_HEIGHT) / n_activities);
		}	
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
	public DataSet getEventAt(Point p) {
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
