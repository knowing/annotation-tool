package evaluationtool.pointdata;

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
	
		private int dataDimension = 1;
		private float pixelsPerMillisecond = 0;		// in pixels/millisecond
		private int N_BARS = 10;
		
		// Data arrays
		SensorData dataSource;
		
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
		
		// Compact view shows all three dimensions on one track in different colors
		boolean compactView = false;
		
		// Follow position automatically?
		RoundRectangle2D.Float coordinatesPopup = null;
		
		// Minimized view
		boolean minimize = false;
		
		// Design constant
		final int TIMELINE_HEIGHT = 14;
		
		public TrackVisualization(SensorData sd, SensorDataVisualization sdv) {
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
		
		// synchronize data
		dataDimension = dataSource.getDataDimension();
		
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
		
		// Get data length
		dataLength = dataSource.getDataLength();
		
		/*
		 * For zoom level 1f, the whole length points must fit in  	 	 		 this.getWidth()				pixels.
		 * The distance between two milliseconds is therefore  					(this.getWidth() / dataLength) 	pixels.
		 * Adding zoom, the distance will be 						zoomlevel * (this.getWidth() / dataLength) pixels.
		 */
		pixelsPerMillisecond = zoomlevel * (this.getWidth() / dataLength);
		
		/*
		 * To improve performance, the first value that will be displayed is calculated here.
		 * As the input data is linear, the loop can be exited when the value does not fit in the canvas anymore
		 */
		int first = calculateFirstDisplayedValue(pixelsPerMillisecond);
		
		// Check if cursor gets out of view
		if(mapTimeToPixel(position, pixelsPerMillisecond) > this.getWidth() * 9 / 10 || 
		   mapTimeToPixel(position, pixelsPerMillisecond) < 0){
			offset = -(position - 0.1f/pixelsPerMillisecond * this.getWidth());
		}	
		
		// Draw x-axis
		g2d.setColor(Color.BLACK);
		if(compactView){
				g2d.drawLine(0, (this.getHeight() - TIMELINE_HEIGHT) / 2, this.getWidth(), (this.getHeight() - TIMELINE_HEIGHT) / 2);
		}
		else{
			for(int d = 0; d < dataDimension; d++){
				g2d.drawLine(0, (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / 6, this.getWidth(), (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / 6);
			}
		}
		
		// Temporary values
		float horizontalCoordinate = 0;
		float verticalCoordinate[] = new float[dataDimension];
		float tempHorizontalCoordinate = 0;
		float tempVerticalCoordinate[] = new float[dataDimension];
		
		if(first != -1){
			
			// If data does not start at 0
			tempHorizontalCoordinate = mapTimeToPixel(dataSource.getTimeAt(first), pixelsPerMillisecond);
			
			for(int i  = first; 
					i < dataSource.getNValues(); 
					// When there are several values per pixel, skip some for improved performance
				    i += Math.max(1, (int)(0.01f / pixelsPerMillisecond))){
				
				
				// Calculate coordinates
				horizontalCoordinate = mapTimeToPixel(dataSource.getTimeAt(i), pixelsPerMillisecond);
				
				if(compactView){
					// Go through dimensions
					for(int d = 0; d < dataDimension; d++){
						verticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) 	/ 2f + ((this.getHeight() - TIMELINE_HEIGHT) / (1.1f * 256f)) * dataSource.getValueAt(d, i);
					}
				}
				else{
					for(int d = 0; d < dataDimension; d++){
						verticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / 6f + ((this.getHeight() - TIMELINE_HEIGHT) / (3.1f * 256f)) * dataSource.getValueAt(d, i);
					}
				}
				
				// Draw values
				for(int d = 0; d < dataDimension; d++){
					g2d.setColor(getColorForDimension(d));
					g2d.draw(new Line2D.Float(tempHorizontalCoordinate, tempVerticalCoordinate[d], horizontalCoordinate, verticalCoordinate[d]));
					// Save values for next iteration
					tempVerticalCoordinate[d]  = verticalCoordinate[d];
				}
				
				tempHorizontalCoordinate = horizontalCoordinate;
				
				// Exit as soon as the values do not fit in the canvas anymore
				if(horizontalCoordinate > this.getWidth())
					break;
			}
		}
		
		// Draw bar for timeline
		g2d.setColor(timelineColorTrack);
		g2d.fillRect(0, this.getHeight() - TIMELINE_HEIGHT , this.getWidth(), TIMELINE_HEIGHT);
		
		// Draw grid
		g2d.setStroke(new BasicStroke(1));
		
		// Determine grid resolution
		float i = -offset;
		float distanceBetweenLines = (this.getWidth() /pixelsPerMillisecond) / N_BARS;
		
		float xCoord = mapTimeToPixel(i, pixelsPerMillisecond);
		while(xCoord < this.getWidth()){
			g2d.setColor(timelineColorTrack);
			g2d.draw(new Line2D.Float(xCoord, 2, xCoord, this.getHeight() - 2));
			g2d.setColor(fontColorTrack);
			g2d.drawString(TimestampConverter.getVideoTimestamp((long)i), xCoord, this.getHeight() - 2);
			
			// Update xCoord
			i += distanceBetweenLines;
			xCoord = mapTimeToPixel(i, pixelsPerMillisecond);
		}
		
		// Draw cursor to indicate current position
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		g2d.draw(new Line2D.Float(mapTimeToPixel(position, pixelsPerMillisecond), 2, mapTimeToPixel(position, pixelsPerMillisecond), (this.getHeight() - TIMELINE_HEIGHT) - 2));
		
		// Draw information on current mouse position
		if(coordinatesPopup != null){
			g2d.setColor(timelineColorTrack);
			g2d.fill(coordinatesPopup);
			g2d.setColor(fontColorTrack);
			g2d.drawString(TimestampConverter.getVideoTimestamp((long)(coordinatesPopup.x / pixelsPerMillisecond - offset)), coordinatesPopup.x + 5, coordinatesPopup.y + coordinatesPopup.height - 3);
			g2d.setColor(Color.BLACK);
			g2d.draw(coordinatesPopup);
			}
	}
	
	/**
	 * 
	 * @param time Timestamp to be mapped
	 * @param pixelsPerMilliseconds Scaling factor 
	 * @return Position on x-axis for time
	 */
	private float mapTimeToPixel(float time, float pixelsPerMilliseconds){
		/*
		 * Position from 0:		 (time 			 * pixelsPerMilliseconds
		 * Add offset:		   	 (time + offset) * pixelsPerMilliseconds
		 */
		return (time + offset) * pixelsPerMilliseconds;
	}
	
	/**
	 * Calculates the first time-values pair that does not have negative coordinates
	 * @param pixelsPerMilliseconds
	 * @return The first time-values pair that does not have negative coordinates
	 */
	private int calculateFirstDisplayedValue(float pixelsPerMilliseconds){
		int i = 0;
		while(i < dataSource.getNValues()){
			if(mapTimeToPixel(dataSource.getTimeAt(i), pixelsPerMilliseconds) > 0)
				return i;
			
			i++;
		}
		return -1;
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
		zoomlevel = Math.max(1, z);
		repaint();
	}
	
	/**
	 * Toggles the view mode
	 */
	protected void toggleCompactView(){
		compactView = !compactView;
		repaint();
	}
	
	public boolean isCompactView(){
		return compactView;
	}
	
	public void showCoordinates(RoundRectangle2D.Float f){
		coordinatesPopup = f;
		repaint();
	}
}
