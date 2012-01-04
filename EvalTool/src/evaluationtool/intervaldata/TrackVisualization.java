package evaluationtool.intervaldata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.util.LinkedList;

import javax.swing.JPanel;

import evaluationtool.TimestampConverter;

public class TrackVisualization extends JPanel{
	
	// Statics to determine which part of an event is selected
	public static int STARTPOINT 	= 1;
	public static int ENDPOINT 		= 2;
	public static int WHOLE_EVENT 	= 3;

	// Painting variables
	private float pixelsPerMillisecond = 0;		// in pixels/millisecond
	private int n_events = 0;
	private int n_activities = 0;
	private DataSet[] events = null;
	private int DISTANCE_BETWEEN_BARS = 100;
	private int n_bars = 10;
	
	// Displaying information
	private float position = 0f;				// in milliseconds
	private float zoomlevel = 1f;
	private float offset = 0f;					// in milliseconds
	private float dataLength = 0;				// in milliseconds
		
		
	// Data arrays
	IntervalData dataSource;
	
	// For every Interval, there is a Rectangle2D.Float for start and end
	RoundRectangle2D.Float[] intervals;
	RoundRectangle2D.Float[] startpoints;
	RoundRectangle2D.Float[] endpoints;
	
	// Colors		
	private Color backgroundColorTrack;
	private Color fontColorTrack;
	private Color timelineColorTrack;
	
	
	// Listener for everything
	VisualizationMouseListener listener;
	
	// Determines whether the user can add, change, or remove intervals 
	boolean locked = false;
	
	// Follow position automatically?
	RoundRectangle2D.Float coordinatesPopup = null;
	
	// Minimized view
	boolean minimize = false;
	
	// Design constant
	final int TIMELINE_HEIGHT = 14;
		
		public TrackVisualization(IntervalData sd, IntervalDataVisualization sdv) {
			dataSource = sd;
			
			// Add listener with reference to visualization main component
			listener = new VisualizationMouseListener(sdv, this);
			
			addMouseWheelListener(listener);
			addMouseListener(listener);
			addMouseMotionListener(listener);
			
			// no mouse pointer needed
			this.setCursor(null);
		}
	
	public void paint(Graphics g){
		// Graphics2D object
		Graphics2D g2d = (Graphics2D) g;
		
		// Draw background
		g2d.setColor(backgroundColorTrack);
		g2d.fill(new Rectangle2D.Float(0, 0, this.getWidth(), this.getHeight()));
		g2d.setColor(Color.BLACK);
		
		// Activate anti-aliasing
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		n_bars = this.getWidth() / DISTANCE_BETWEEN_BARS;
		
		// Update data reference
		events = dataSource.getEvents();
		n_events = events.length;
		n_activities = dataSource.getPossibleActivities().length;
		
		// Create start, end and interval Rectangles
		createRectangles();
		
		paintTracks(g2d);
	}
	
	/**
	 * Paints the tracks 
	 * @param g2d
	 */
	private void paintTracks(Graphics2D g2d){
		
		g2d.setStroke(new BasicStroke(1));
		
		// If there is no data, set dataLength to length of video
		if(dataLength == 0)
			dataLength = dataSource.getModel().getProjectLength();
		
		/*
		 * For zoom level 1f, the whole length points must fit in  	 	 		 this.getWidth()				pixels.
		 * The distance between two milliseconds is therefore  					(this.getWidth() / dataLength) 	pixels.
		 * Adding zoom, the distance will be 						zoomlevel * (this.getWidth() / dataLength) pixels.
		 */
		pixelsPerMillisecond = zoomlevel * (this.getWidth() / dataLength);
		
		
		// Check if cursor gets out of view and change offset (only when video is playing)
		if(dataSource.getModel().isVideoPlaying())
			if(mapTimeToPixel(position) > this.getWidth() * 9 / 10 || 
			   mapTimeToPixel(position) < 0){
				offset = -(position - 0.1f/pixelsPerMillisecond * this.getWidth());
			}	
		
		// Draw data
		for(int i = 0; i < n_events; i++){
				
					g2d.setColor(new Color((events[i].activitytype * 30) % 255, (events[i].activitytype * 75) % 255, (events[i].activitytype * 120) % 255, 150));
					g2d.fill(intervals[i]);
					
					if(!isLocked()){
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
			g2d.drawLine(0, (int)((float)this.getHeight() * i / n_activities), this.getWidth(), (int)((float)this.getHeight() * i / n_activities));
		}
		
		// Draw x-axis
		g2d.setColor(Color.BLACK);
		
		// Draw table
		for(int i = 0; i < dataSource.getPossibleActivities().length; i++){
			g2d.drawLine(0, i / n_activities, this.getWidth(), i / n_activities);
			g2d.drawString(dataSource.getPossibleActivities()[i], 
						      this.getWidth() / 2 - g2d.getFontMetrics(g2d.getFont()).stringWidth(dataSource.getPossibleActivities()[i]) / 2, 
						      (i + 0.5f) * this.getHeight() / n_activities);
		}	
		
		// Draw bar for timeline
		g2d.setColor(timelineColorTrack);
		g2d.fillRect(0, this.getHeight() - TIMELINE_HEIGHT, this.getWidth(), TIMELINE_HEIGHT);
		
		// Draw grid
		g2d.setStroke(new BasicStroke(1));
		
		// Determine grid resolution
		float i = -offset;
		float timeBetweenBars = (this.getWidth() / pixelsPerMillisecond) / n_bars;
		
		float xCoord = mapTimeToPixel(i);
		while(xCoord < this.getWidth()){
			g2d.setColor(timelineColorTrack);
			g2d.draw(new Line2D.Float(xCoord, 2, xCoord, this.getHeight() - 2));
			g2d.setColor(fontColorTrack);
			g2d.drawString(TimestampConverter.getVideoTimestamp((long)i), xCoord, this.getHeight() - 2);
			
			// Update xCoord
			i += timeBetweenBars;
			xCoord = mapTimeToPixel(i);
		}

		
		// Draw cursor to indicate current position
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.draw(new Line2D.Float(mapTimeToPixel(position), 2, mapTimeToPixel(position), (this.getHeight() - TIMELINE_HEIGHT) - 2));
		
		// Draw information on current mouse position
		if(coordinatesPopup != null){
			String timestamp = TimestampConverter.getVideoTimestamp((long)(coordinatesPopup.x / pixelsPerMillisecond - offset));
			
			// Adjust length of popup to text
			coordinatesPopup.width = g2d.getFontMetrics(g2d.getFont()).stringWidth(timestamp) + 10;
			
			g2d.setColor(timelineColorTrack);
			g2d.fill(coordinatesPopup);
			g2d.setColor(fontColorTrack);
			g2d.drawString(timestamp, coordinatesPopup.x + 5, coordinatesPopup.y + coordinatesPopup.height - 3);
			g2d.setColor(Color.BLACK);
			g2d.draw(coordinatesPopup);
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
			startpoints[i] = 	new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart) - 5, (float)events[i].activitytype / n_activities * this.getHeight(), 
						   							  10,  (float)this.getHeight() / n_activities - 1, 7, 7);
			
			if(events[i].timestampEnd != 0){
				// Create interval
				intervals[i] = 		new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart), (float)events[i].activitytype / n_activities * this.getHeight(), 
													   	  mapTimeToPixel((float)events[i].timestampEnd) - mapTimeToPixel((float)events[i].timestampStart),  (float)this.getHeight() / n_activities,
													   	  1, 1);
				// Create endpoint
				endpoints[i] = 		new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampEnd) - 5, (float)events[i].activitytype / n_activities * this.getHeight(), 
					   	  								   10,  (float)this.getHeight() / n_activities - 1, 
					   	  								   7, 7);

			}
			else{
				intervals[i] = new RoundRectangle2D.Float(mapTimeToPixel((float)events[i].timestampStart), (float)events[i].activitytype / n_activities * this.getHeight(), 
						   					    this.getWidth() - mapTimeToPixel((float)events[i].timestampStart), (float)this.getHeight() / n_activities, 1, 1);
				endpoints[i] = null;			
			}		
		}
	}
	
	/**
	 * 
	 * @param time Timestamp to be mapped
	 * @param pixelsPerMillisecond Scaling factor 
	 * @return Position on x-axis for time
	 */
	public float mapTimeToPixel(float time){
		/*
		 * Position from 0:		 (time 			 * pixelsPerMillisecond
		 * Add offset:		   	 (time + offset) * pixelsPerMillisecond
		 */
		return (time + offset) * pixelsPerMillisecond;
	}
	
	public long mapPixelToTime(float pixel){
		return (long) ((pixel / pixelsPerMillisecond) - offset);
	}
	
	/**
	 * Allows the GUI to switch color scheme
	 */
	public void setAlternativeColorScheme(boolean b){
		if(b){			
			backgroundColorTrack 	= Color.WHITE;
			fontColorTrack 			= Color.WHITE;
			timelineColorTrack 		= Color.DARK_GRAY;
		}
		else{
			backgroundColorTrack 	= new Color(255, 255, 225);
			fontColorTrack 			= Color.WHITE;
			timelineColorTrack 		= Color.BLACK;
		}
	}
	
	/**
	 * Sets the position for the cursor
	 */
	public void setPosition(float p){
		position = p;
		repaint();
	}
	
	/**
	 * Changes the viewable area of the track
	 * @param o
	 */
	protected void setOffset(float o){
		offset = o;
	}
	
	protected float getOffset(){
		return offset;
	}

	protected float getZoomlevel(){
		return zoomlevel;
	}
	
	protected float getPixelsPerMillisecond(){
		return pixelsPerMillisecond;
	}
	
	/**
	 * Sets the zoom level
	 * @param z
	 */
	protected void setZoomlevel(float z){
		// Limit bounds
		zoomlevel = Math.min(Math.max(0.0001f, z), 3e8f);
		repaint();
	}
	
	/**
	 * Toggles editability
	 */
	public void toggleLocked(){
		locked = !locked;
		repaint();
	}
	
	public boolean isLocked(){
		return locked;
	}
	
	public void showCoordinates(RoundRectangle2D.Float f){
		coordinatesPopup = f;
		repaint();
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
			if(startpoints[i].contains(p) || endpoints[i].contains(p) || intervals[i].contains(p))
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
			if(endpoints[i].contains(p))
				return ENDPOINT;
			if(intervals[i].contains(p))
				return WHOLE_EVENT;
		}
		
		// This should not happen, as getEventAt should be called before
		return 0;
	}
}
