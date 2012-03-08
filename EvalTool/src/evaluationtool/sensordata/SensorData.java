package evaluationtool.sensordata;

import javax.swing.JPanel;

import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.gui.Visualization;


/**
 * Sensor data representation with synchronization attributes
 * @author anfi
 *
 */
public class SensorData implements Data{
	// Reference to model
	DataModel model;
	
	// Timing corrections
	private float playbackSpeed = 1f;	// Factor of original
	private long offset = 0;
	
	// Data array
	DataSet[] data;
	
	// Source file
	String source = "";
	
	// Visualization track
	SensorDataVisualization vis;
	
	/**
	 * Initializes the data array
	 * @param length The number of three dimensional values
	 */
	public SensorData(DataModel model, int length, String src){
		data = new DataSet[length];
		source = src;
		this.model = model;
		
		// Create visualization
		vis = new SensorDataVisualization(this);
	}
	
	/*
	 * Setter methods for data, offset and playback speed
	 */
	
	public void setDataAt(int pos, DataSet s){
		data[pos] = s;
	}
	
	public void setOffset(long o){
		offset = o;
		vis.updateInfo();
		getVisualization().repaint();
	}
	
	public void setPlaybackSpeed(float p){
		playbackSpeed = Math.max(p, 0.0001f);
		vis.updateInfo();
		getVisualization().repaint();
	}
	
	/*
	 * Getter methods for data, offset and playback speed
	 */
	
	public long getOffset(){
		return offset;
	}
	
	public float getPlaybackSpeed(){
		return playbackSpeed;
	}
	
	/**
	 * Calculates the timestamp of a triplet of values, including offset and playback speed
	 * @param pos
	 * @return
	 */
	public float getTimeAt(int pos){
		return (data[pos].time * playbackSpeed) + offset;
	}
	
	/*
	 * Getter methods for X, Y and Z values at a specified position
	 */
	public int getValueAt(int dimension, int position){
		return data[position].values[dimension];
	}
	
	/**
	 * @return The number of three dimensional values
	 */
	public int getNValues(){
		return data.length;
	}
	
	/**
	 * @return The length of the track in ms
	 */
	public long getLength(){
		return (long)(getTimeAt(data.length - 1) - getTimeAt(0));
	}
	
	/**
	 * @return A visualization of this data track
	 */
	public Visualization getVisualization() {
		return vis;
	}

	/**
	 * Returns the path to the source file of this data track
	 */
	public String getSource() {
		return source;
	}

	public void remove() {
		model.removeTrack(this);
	}

	public int getDataDimension() {
		if(data != null)
			return data[0].values.length;
		else return 0;
	}
	
	/**
	 * This class will be able to import csv and sdr data
	 */
	public static boolean canOpenFile(String extension){
		
		if(	extension.equals("arff") ||
			extension.equals("sdr"))
				return true;
		
		return false;
	}

	@Override
	public DataModel getModel() {
		return model;
	}

	@Override
	public void setSource(String filename) {
		this.source = filename;
	}
}
