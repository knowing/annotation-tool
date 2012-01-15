package evaluationtool.pointdata;

import java.util.LinkedList;

import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.gui.Visualization;

public class PointData implements Data {

	// Reference to model
	DataModel model;
		
	// Timing corrections
	private float playbackSpeed = 1f;	// Factor of original
	private long offset = 0;		
		
	// Source file
	String source = "";
	
	// LinkedList containing all points
	LinkedList<Long> points = new LinkedList<Long>();
		
	// Visualization track
	PointDataVisualization vis;
	
	// Determines whether the user can add, change, or remove points
	boolean locked = false;
	
	public PointData(DataModel model, String src){
		source = src;
		this.model = model;
		
		// Create visualization
		vis = new PointDataVisualization(this);
	}
	
	public void addPointAtCurrentPosition() {
			addPoint(vis.getTrackVisualization().getPosition());
	}
	
	/**
	 * Adds a point to the list
	 * @param time
	 */
	public void addPoint(long time){

		// Sort by starttime
		for(int i = 0; i < points.size(); i++){
			if(time < points.get(i)){
				points.add(i, time);
				return;
			}
		}
		
		// Add as last element
		points.add(time);
	}
	
	public void deletePoint(int index){
		points.remove(index);
	}
	
	public Visualization getVisualization() {
		return vis;
	}

	public String getSource() {
		return source;
	}
	
	public DataModel getModel(){
		return model;
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
	public long[] getPoints(){
		long[] temp = new long[points.size()];
		
		for(int i = 0; i < points.size(); i++){
			temp[i] = points.get(i);
		}
		
		return temp;
	}
	
	public long getOffset(){
		return offset;
	}
	
	public float getPlaybackSpeed(){
		return playbackSpeed;
	}

	public void remove() {
		model.removeTrack(this);
	}
	
	public long getLength() {
		if(points.size() < 2){
			return 0;
		}
		else{
			return points.getLast() - points.getFirst();
		}
	}

	public void setSource(String filename) {
		this.source = filename;
	}
	
	/**
	 * This will bring the points in proper order
	 */
	public void orderPoints() {

		LinkedList<Long> pointsOld = points;
		points = new LinkedList<Long>();
		
		while(!pointsOld.isEmpty()){
			addPoint(pointsOld.pop());
		}
	}

	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean b) {
		locked = b;
	}

	public void movePoint(int draggedPoint, long newTime) {
		points.remove(draggedPoint);
		points.add(draggedPoint, newTime);
	}
}
