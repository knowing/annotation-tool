package evaluationtool.intervaldata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import evaluationtool.TimestampConverter;

public class TrackVisualization extends JPanel{
	
		private float pixelsPerMillisecond = 0;		// in pixels/millisecond
		private int N_BARS = 10;
		
		// Data arrays
		IntervalData dataSource;
		
		// Colors		
		private Color backgroundColorTrack;
		private Color fontColorTrack;
		private Color timelineColorTrack;
		
		// Displaying information
		private float position = 0f;				// in milliseconds
		private float zoomlevel = 1f;
		private float offset = 0f;					// in milliseconds
		private float dataLength = 0;				// in milliseconds
		
		// Listener for everything
		VisualizationMouseListener listener;
		
		// Determines whether the user can add, change, or remove intervals 
		boolean editable = false;
		
		// Follow position automatically?
		RoundRectangle2D.Float coordinatesPopup = null;
		
		// Minimized view
		boolean minimize = false;
		
		// Design constant
		final int TIMELINE_HEIGHT = 14;
		
		public TrackVisualization(IntervalData sd, IntervalDataVisualization sdv) {
			dataSource = sd;
			
			// Add listener with reference to visualization main component
			listener = new VisualizationMouseListener(sdv);
			
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
		
		N_BARS = this.getWidth() / 100;
		
		paintTracks(g2d);
	}
	
	/**
	 * Paints the tracks 
	 * @param g2d
	 */
	private void paintTracks(Graphics2D g2d){
		
		g2d.setStroke(new BasicStroke(1));
		
		/*
		 * For zoom level 1f, the whole length points must fit in  	 	 		 this.getWidth()				pixels.
		 * The distance between two milliseconds is therefore  					(this.getWidth() / dataLength) 	pixels.
		 * Adding zoom, the distance will be 						zoomlevel * (this.getWidth() / dataLength) pixels.
		 */
		pixelsPerMillisecond = zoomlevel * (this.getWidth() / dataLength);
		
		
		// Check if cursor gets out of view
		if(mapTimeToPixel(position, pixelsPerMillisecond) > this.getWidth() * 9 / 10 || 
		   mapTimeToPixel(position, pixelsPerMillisecond) < 0){
			offset = -(position - 0.1f/pixelsPerMillisecond * this.getWidth());
		}	
		
		// Draw x-axis
		g2d.setColor(Color.BLACK);
		
		//TODO Draw data
		
		// Draw bar for timeline
		g2d.setColor(timelineColorTrack);
		g2d.fillRect(0, this.getHeight() - TIMELINE_HEIGHT , this.getWidth(), TIMELINE_HEIGHT);
		
		// Draw grid
		g2d.setStroke(new BasicStroke(1));
		
		// Determine grid resolution
		float i = -offset;
		float timeBetweenBars = (this.getWidth() / pixelsPerMillisecond) / N_BARS;
		
		float xCoord = mapTimeToPixel(i, pixelsPerMillisecond);
		while(xCoord < this.getWidth()){
			g2d.setColor(timelineColorTrack);
			g2d.draw(new Line2D.Float(xCoord, 2, xCoord, this.getHeight() - 2));
			g2d.setColor(fontColorTrack);
			g2d.drawString(TimestampConverter.getVideoTimestamp((long)i), xCoord, this.getHeight() - 2);
			
			// Update xCoord
			i += timeBetweenBars;
			xCoord = mapTimeToPixel(i, pixelsPerMillisecond);
		}

		
		// Draw cursor to indicate current position
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.draw(new Line2D.Float(mapTimeToPixel(position, pixelsPerMillisecond), 2, mapTimeToPixel(position, pixelsPerMillisecond), (this.getHeight() - TIMELINE_HEIGHT) - 2));
		
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
	 * 
	 * @param time Timestamp to be mapped
	 * @param pixelsPerMillisecond Scaling factor 
	 * @return Position on x-axis for time
	 */
	private float mapTimeToPixel(float time, float pixelsPerMillisecond){
		/*
		 * Position from 0:		 (time 			 * pixelsPerMillisecond
		 * Add offset:		   	 (time + offset) * pixelsPerMillisecond
		 */
		return (time + offset) * pixelsPerMillisecond;
	}

	
	/**
	 * Generates a Color for different track dimensions
	 */
	private Color getColorForDimension(int d){
		
		// predefined values for the common three dimensions
		switch(d){
		case 0:	return new Color(50, 	50, 	150, 	180);
		case 1: return new Color(150, 	50, 	50, 	180);
		case 2: return new Color(50, 	150, 	50, 	180);
		}
		
		return new Color(((d + 1) * 173)%255, ((d + 2) * 37)%255, ((d + 3) * 120)%255, 180);
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
		offset = Math.max(0, o);
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
	protected void toggleEditable(){
		editable = !editable;
		repaint();
	}
	
	public boolean isEditable(){
		return editable;
	}
	
	public void showCoordinates(RoundRectangle2D.Float f){
		coordinatesPopup = f;
		repaint();
	}
}
