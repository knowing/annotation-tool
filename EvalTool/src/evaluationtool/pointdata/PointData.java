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
	LinkedList<Timestamp> points = new LinkedList<Timestamp>();
	long[] quickPoints;
		
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
			addPoint(new Timestamp(removeSettingsFromTimestamp(vis.getTrackVisualization().getPosition())));
	}
	
	/**
	 * Adds a point to the list
	 * @param time
	 */
	public void addPoint(Timestamp t){
		// Sort by starttime
		for(int i = 0; i < points.size(); i++){
			if(t.timestamp < addSettingsToTimestamp(points.get(i).timestamp)){
				points.add(i, t);
				createQuickPoints();
				return;
			}
		}
		
		// Add as last element
		points.add(t);
		createQuickPoints();
	}
	
	public void createQuickPoints() {
		quickPoints = new long[points.size()];
		for(int i = 0; i < quickPoints.length; i++){
			quickPoints[i] = points.get(i).timestamp;
			System.out.println("#" + i);
		}
	}

	public void addLastPoint(Timestamp t){
		points.add(t);
	}
	
	public void deletePoint(int i){
		if(points.size() > i){
			points.remove(i);
			createQuickPoints();
		}
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

	/**
	 * Manipulates original data
	 */
	public void setOffset(long o){
		offset = o;

		vis.updateInfo();
		getVisualization().repaint();
	}
	
	public void movePoint(int i, long time){
		points.remove(i);
		addPoint(new Timestamp(time));
	}
	
	public void setPlaybackSpeed(float p){
		playbackSpeed = Math.max(p, 0.0001f);
		
		vis.updateInfo();
		getVisualization().repaint();
	}
	
	/*
	 * Getter methods for data, offset and playback speed
	 */
	public long getPoint(int i){
		if(quickPoints != null && quickPoints.length > i)
			return addSettingsToTimestamp(quickPoints[i]);
		else
			return 0;
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
			return addSettingsToTimestamp(points.getLast().timestamp - points.getFirst().timestamp);
		}
	}

	public void setSource(String filename) {
		this.source = filename;
	}

	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean b) {
		locked = b;
	}

	public long addSettingsToTimestamp(long time) {
		return (long)(time / getPlaybackSpeed() + getOffset());
	}
	public long removeSettingsFromTimestamp(long time){
		return (long)((time - getOffset()) * getPlaybackSpeed());
	}

	public int getNPoints() {
		if(quickPoints != null)
			return quickPoints.length;
		else return 0;
	}
}
