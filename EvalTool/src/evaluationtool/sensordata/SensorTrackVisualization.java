package evaluationtool.sensordata;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import evaluationtool.Data;
import evaluationtool.gui.TrackVisualization;

public class SensorTrackVisualization extends TrackVisualization{
	
	private int dataDimension = 1;

	private int dataResolution = 256;
	
	// Data arrays
	SensorData dataSource;
	
	// Listener for everything
	VisualizationMouseListener listener;
	
	// Compact view shows all three dimensions on one track in different colors
	boolean compactView = false;

		
	public SensorTrackVisualization(Data sd, SensorDataVisualization sdv) {
		super(sd);
		dataSource = (SensorData)sd;
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
		// synchronize data
		dataDimension 	= dataSource.getDataDimension();
		// Get data length
		dataLength 		= dataSource.getLength();
	}
	
	/**
	 * Paints the tracks 
	 * @param g2d
	 */
	public void paintTracks(Graphics2D g2d){
		
		g2d.setStroke(new BasicStroke(1));
		
		/*
		 * To improve performance, the first value that will be displayed is calculated here.
		 * As the input data is linear, the loop can be exited when the value does not fit in the canvas anymore
		 */
		int first = calculateFirstDisplayedValue(pixelsPerMillisecond);
		
		// Draw x-axis
		g2d.setColor(Color.BLACK);
		if(compactView){
				g2d.drawLine(0, (this.getHeight() - TIMELINE_HEIGHT) / 2, this.getWidth(), (this.getHeight() - TIMELINE_HEIGHT) / 2);
		}
		else{
			for(int d = 0; d < dataDimension; d++){
				g2d.drawLine(0, (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / (2 * dataDimension), this.getWidth(), (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / (2 * dataDimension));
			}
		}
		
		// Draw data
		if(first != -1){
			
			// Temporary values
			float horizontalCoordinate = 0;
			float verticalCoordinate[] = new float[dataDimension];
			float tempHorizontalCoordinate = 0;
			float tempVerticalCoordinate[] = new float[dataDimension];
			
			if(compactView){
				// Go through dimensions
				for(int d = 0; d < dataDimension; d++){
					tempVerticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) 	/ 2f + ((this.getHeight() - TIMELINE_HEIGHT) / (1.1f * dataResolution)) * dataSource.getValueAt(d, first);
				}
			}
			else{
				for(int d = 0; d < dataDimension; d++){
					tempVerticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / (2f * dataDimension) + ((this.getHeight() - TIMELINE_HEIGHT) / (3.1f * dataResolution)) * dataSource.getValueAt(d, first);
				}
			}
			
			// If data does not start at 0
			tempHorizontalCoordinate = mapTimeToPixel(dataSource.getTimeAt(first));
			
			for(int i  = first; 
					i < dataSource.getNValues(); 
					// When there are several values per pixel, skip some for improved performance
				    i += Math.max(1, (int)(0.01f / pixelsPerMillisecond))){
				
				
				// Calculate coordinates
				horizontalCoordinate = mapTimeToPixel(dataSource.getTimeAt(i));
				
				if(compactView){
					// Go through dimensions
					for(int d = 0; d < dataDimension; d++){
						verticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) 	/ 2f + ((this.getHeight() - TIMELINE_HEIGHT) / (1.1f * dataResolution)) * -dataSource.getValueAt(d, i);
					}
				}
				else{
					for(int d = 0; d < dataDimension; d++){
						verticalCoordinate[d]  = (this.getHeight() - TIMELINE_HEIGHT) * (2 * d + 1) / (2f * dataDimension) + ((this.getHeight() - TIMELINE_HEIGHT) / ((dataDimension + 0.1f) * dataResolution)) * -dataSource.getValueAt(d, i);
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
	}
	
	/**
	 * Calculates the first time-values pair that does not have negative coordinates
	 * @param pixelsPerMilliseconds
	 * @return The first time-values pair that does not have negative coordinates
	 */
	private int calculateFirstDisplayedValue(float pixelsPerMilliseconds){

		// Binary search for first value to display
		int start = 0;
		int end = dataSource.getNValues() - 1;
		int partLength = end - start;
		
		// Cancel if nothing is visible
		if		   (mapTimeToPixel(dataSource.getTimeAt(end)) < 0 
				|| 	mapTimeToPixel(dataSource.getTimeAt(start)) > this.getWidth()){
			
			return -1;
		}

		// While start and end are different, move closer to the value
		while(start != end && partLength > 1){
			
			if	(mapTimeToPixel(dataSource.getTimeAt(start + partLength / 2)) > 0)
						end 	= start + partLength / 2;
			else
						start	= start + partLength / 2;
			
			partLength = end - start;
		}

		return start;
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
	 * Toggles the view mode
	 */
	protected void toggleCompactView(){
		compactView = !compactView;
		repaint();
	}
	
	public boolean isCompactView(){
		return compactView;
	}
}
