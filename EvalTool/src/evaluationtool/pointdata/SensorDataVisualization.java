package evaluationtool.pointdata;

import java.awt.*;
import java.awt.geom.*;

import evaluationtool.TimestampConverter;
import evaluationtool.gui.Visualization;

public class SensorDataVisualization extends Visualization {
	
	// Colors
	private Color backgroundColorMenu;
	private Color buttonColorMenu;
	private Color fontColorMenu;
	
	private Color backgroundColorTrack;
	private Color fontColorTrack;
	private Color timelineColorTrack;
	
	
	// Displaying information
	private float position = 0f;				// in milliseconds
	private float zoomlevel = 1f;
	private float offset = 0f;					// in milliseconds
	private float dataLength = 0;				// in milliseconds
	private int dataDimension = 1;
	
	private float pixelsPerMillisecond = 0;		// in pixels/millisecond
	
	// Compact view shows all three dimensions on one track in different colors
	boolean compactView = false;
	
	// Follow position automatically?
	boolean autoScroll = true;
	
	// Minimized view
	boolean minimize = false;
	
	// Data arrays
	SensorData dataSource;
	
	//Design constants
	final int MENU_WIDTH = 90;
	final int TIMELINE_HEIGHT = 14;
	// Listener for everything
	VisualizationMouseListener listener = new VisualizationMouseListener(this);
	
	SensorDataVisualization(SensorData sd){
		dataSource = sd;
		
		// Will be overwritten anyway, just set this in case repaint is called earlier
		setAlternativeColorScheme(false);
		
		this.addMouseWheelListener(listener);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
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
		g2d.setStroke(new BasicStroke(3));
		g2d.draw(new Rectangle2D.Float(0, 0, this.getWidth(), this.getHeight()));
		
		paintTracks(g2d);
		paintMenu(g2d);
	}
	
	/**
	 * Paints the menu
	 * @param g2d
	 */
	private void paintMenu(Graphics2D g2d){
		// Draw menu
		g2d.setColor(backgroundColorMenu);
		g2d.setStroke(new BasicStroke(1));
		
		g2d.fill(new Rectangle2D.Float(0, 00, MENU_WIDTH, this.getHeight()));
		
		g2d.setColor(buttonColorMenu);
		g2d.fill(new Rectangle2D.Float(4, 02, MENU_WIDTH - 4, 17));
		g2d.fill(new Rectangle2D.Float(4, 22, MENU_WIDTH - 4, 17));
		g2d.fill(new Rectangle2D.Float(4, 42, MENU_WIDTH - 4, 17));
		
		// Draw menu entries
		g2d.setColor(fontColorMenu);
		g2d.drawString("Open file", 5, 15);
		g2d.drawString("Remove track", 5, 35);
		if(compactView)
			g2d.drawString("Expanded view", 5, 55);
		else
			g2d.drawString("Compact view", 5, 55);
		
		g2d.drawString("Offset: " + (int)(dataSource.getOffset() * 100) / 100f, 5, 75);
		g2d.drawString("Zoom: " + 	(int)(dataSource.getPlaybackSpeed() * 100) / 100f, 5, 95);
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
		 * For zoom level 1f, the whole length points must fit in  	 	 		 (this.getWidth() - MENU_WIDTH) 				pixels.
		 * The distance between two milliseconds is therefore  					((this.getWidth() - MENU_WIDTH) / dataLength) 	pixels.
		 * Adding zoom, the distance will be 						zoomlevel * ((this.getWidth() - MENU_WIDTH) / dataLength) 	pixels.
		 */
		pixelsPerMillisecond = zoomlevel * ((this.getWidth() - MENU_WIDTH) / dataLength);
		
		/*
		 * To improve performance, the first value that will be displayed is calculated here.
		 * As the input data is linear, the loop can be exited when the value does not fit in the canvas anymore
		 */
		int first = calculateFirstDisplayedValue(pixelsPerMillisecond);
		
		// Check if cursor gets out of view
		if(autoScroll && (mapTimeToPixel(position, pixelsPerMillisecond) > this.getWidth() * 9 / 10) || 
						  mapTimeToPixel(position, pixelsPerMillisecond) < 0){
			offset = -(position - 0.1f/pixelsPerMillisecond * (this.getWidth() - MENU_WIDTH));
		}	
		
		// Temporary values
		float horizontalCoordinate = MENU_WIDTH;
		float verticalCoordinate[] = new float[dataDimension];
		float tempHorizontalCoordinate = MENU_WIDTH;
		float tempVerticalCoordinate[] = new float[dataDimension];
		
		if(first != -1){
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
		g2d.fillRect(MENU_WIDTH, this.getHeight() - TIMELINE_HEIGHT , this.getWidth() - MENU_WIDTH, TIMELINE_HEIGHT);
		
		// Draw grid
		g2d.setStroke(new BasicStroke(1));
		
		// Determine grid resolution
		float i = -offset;
		float distanceBetweenLines = ((this.getWidth() - MENU_WIDTH)/pixelsPerMillisecond) / 10;
		
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
		g2d.setColor(timelineColorTrack);
		g2d.setStroke(new BasicStroke(2));
		g2d.draw(new Line2D.Float(mapTimeToPixel(position, pixelsPerMillisecond), 2, mapTimeToPixel(position, pixelsPerMillisecond), (this.getHeight() - TIMELINE_HEIGHT) - 2));
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
		 * Add menu and offset:	 (time + offset) * pixelsPerMilliseconds + MENU_WIDTH	
		 */
		return (time + offset) * pixelsPerMilliseconds + MENU_WIDTH;
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
		switch(d){
		case 0: return new Color(255, 0, 0, 180);
		case 1: return new Color(0, 255, 0, 180);
		case 2: return new Color(0, 0, 255, 180);
		}
		return new Color((d * 100)%255, (d * 50)%255, (d * 200)%255, 180);
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
	}
	
	/**
	 * Toggles the view mode
	 */
	protected void toggleCompactView(){
		compactView = !compactView;
	}
	
	/**
	 * Removes the track from gui
	 */
	protected void remove(){
		dataSource.remove();
	}
	
	/**
	 * Allows the gui to switch color scheme
	 */
	public void setAlternativeColorScheme(boolean b){
		if(b){
			backgroundColorMenu 	= Color.DARK_GRAY;
			buttonColorMenu 		= Color.WHITE;
			fontColorMenu 			= Color.WHITE;
			
			backgroundColorTrack 	= Color.WHITE;
			fontColorTrack 			= Color.WHITE;
			timelineColorTrack 		= backgroundColorMenu;
		}
		else{
			backgroundColorMenu 	= Color.LIGHT_GRAY;
			buttonColorMenu 		= Color.BLACK;
			fontColorMenu 			= Color.WHITE;
			
			backgroundColorTrack 	= Color.BLACK;
			fontColorTrack 			= Color.BLACK;
			timelineColorTrack 		= backgroundColorMenu;
		}
	}
}