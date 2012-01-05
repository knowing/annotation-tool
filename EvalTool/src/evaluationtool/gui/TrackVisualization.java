package evaluationtool.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import evaluationtool.DataModel;
import evaluationtool.TimestampConverter;

/**
 * Abstract class that provides basic features for displaying values on a timeline, including zoom scrolling
 * @author anfi
 *
 */
abstract public class TrackVisualization extends JPanel{
	
	protected float pixelsPerMillisecond = 0;		// in pixels/millisecond
	protected int DISTANCE_BETWEEN_BARS = 100;
	protected int n_bars = 10;
	
	// Displaying information
	protected float position = 0f;				// in milliseconds
	protected float zoomlevel = 1f;	
	protected float offset = 0f;				// in milliseconds
	protected float dataLength = 0;				// in milliseconds
	
	// Colors		
	protected Color backgroundColorTrack;
	protected Color fontColorTrack;
	protected Color timelineColorTrack;
	
	// Design constant
	protected final int TIMELINE_HEIGHT = 14;
	
	// Follow position automatically?
	protected RoundRectangle2D.Float coordinatesPopup = null;
	
	// Reference to the model
	protected DataModel model;
	
	public void paint(Graphics g){
		// Graphics2D object
		Graphics2D g2d = (Graphics2D) g;
	
		// Activate anti-aliasing
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		updateDataReferences();
		
		preparePainting(g2d);
		paintTracks(g2d);
		paintBasics(g2d);	
	}
	
	abstract public void updateDataReferences();
	abstract public void paintTracks(Graphics2D g2d);
	
	protected void preparePainting(Graphics2D g2d){
		// Draw background
		g2d.setColor(backgroundColorTrack);
		g2d.fill(new Rectangle2D.Float(0, 0, this.getWidth(), this.getHeight()));
		g2d.setColor(Color.BLACK);
		
		// Calculate vertical bars
		n_bars = this.getWidth() / DISTANCE_BETWEEN_BARS;
				
		// If there is no data, set dataLength to length of video
		if(dataLength == 0)
			dataLength = model.getProjectLength();
		
		/*
		 * For zoom level 1f, the whole length points must fit in  	 	 		 this.getWidth()				pixels.
		 * The distance between two milliseconds is therefore  					(this.getWidth() / dataLength) 	pixels.
		 * Adding zoom, the distance will be 						zoomlevel * (this.getWidth() / dataLength) pixels.
		 */
		pixelsPerMillisecond = zoomlevel * (this.getWidth() / dataLength);
		
		
		// Check if cursor gets out of view and change offset (only when video is playing)
		if(model.isVideoPlaying())
			if(mapTimeToPixel(position) > this.getWidth() * 9 / 10 || 
			   mapTimeToPixel(position) < 0){
				offset = -(position - 0.1f/pixelsPerMillisecond * this.getWidth());
			}
	}
	
	protected void paintBasics(Graphics2D g2d){
		g2d.setStroke(new BasicStroke(1));
		
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
	public void setOffset(float o){
		offset = o;
	}
	
	public float getOffset(){
		return offset;
	}

	public float getZoomlevel(){
		return zoomlevel;
	}
	
	public float getPixelsPerMillisecond(){
		return pixelsPerMillisecond;
	}
	
	/**
	 * Sets the zoom level
	 * @param z
	 */
	public void setZoomlevel(float z){
		// Limit bounds
		zoomlevel = Math.min(Math.max(0.0001f, z), 3e8f);
		repaint();
	}
	
	public void showCoordinates(RoundRectangle2D.Float f){
		coordinatesPopup = f;
		repaint();
	}
}
