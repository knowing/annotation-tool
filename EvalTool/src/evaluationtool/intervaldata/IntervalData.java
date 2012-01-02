package evaluationtool.intervaldata;

import java.util.LinkedList;

import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.gui.Visualization;

public class IntervalData implements Data {

	// Reference to model
		DataModel model;
		
	// Timing corrections
	private float playbackSpeed = 1f;	// Factor of original
	private long offset = 0;		
		
	// Source file
	String source = "";
	
	// LinkedList containing all start- and endpoints
	LinkedList<DataSet> events = new LinkedList<DataSet>();
		
	// Visualization track
	IntervalDataVisualization vis;
	
	public IntervalData(DataModel model, String src){
		source = src;
		this.model = model;
		
		// Create visualization
		vis = new IntervalDataVisualization(this);
	}
	
	public void addEvent(long timestamp, int activitytype){
		int i = 0;
		
		for(; i < events.size(); i++){
			if(timestamp < events.get(i).timestamp){
				events.add(i, new DataSet(timestamp, activitytype));
				return;
			}
		}
		
		events.add(new DataSet(timestamp, activitytype));
	}
	
	public Visualization getVisualization() {
		return vis;
	}

	public String getSource() {
		return source;
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
	public DataSet[] getEvents(){
		return events.toArray(new DataSet[events.size()]);
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
	
	public String[] getPossibleActivities(){
		return DataSet.getPossibleActivities();
	}

	public int getActivityAt(long timestamp) {
		int result = DataSet.NO_ACTIVITY;
		
		for(int i = 0; i < events.size(); i++){
			if(events.get(i).timestamp > timestamp)
				return result;
			else
				result = events.get(i).activitytype;
		}
		
		return result;
	}
}
