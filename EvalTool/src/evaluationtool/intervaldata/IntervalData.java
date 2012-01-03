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
	
	// Possible activities
	final String[] ACTIVITIES;
	
	// LinkedList containing all start- and endpoints
	LinkedList<DataSet> events = new LinkedList<DataSet>();
		
	// Visualization track
	IntervalDataVisualization vis;
	
	public IntervalData(DataModel model, String src, String[] activities){
		source = src;
		this.model = model;
		
		ACTIVITIES = activities;
		
		// Create visualization
		vis = new IntervalDataVisualization(this);
	}
	
	/**
	 * Adds an event to the list of events
	 * @param timestampStart
	 * @param timestampEnd
	 * @param activitytype
	 */
	public void addEvent(long timestampStart, long timestampEnd, int activitytype){
		int i = 0;
		
		if(events.size() == 0){
			events.add(i, new DataSet(timestampStart, timestampEnd, activitytype));
			return;
		}
		
		// Sort by starttime
		for(; i < events.size(); i++){
			if(timestampStart < events.get(i).timestampStart){
				events.add(i, new DataSet(timestampStart, timestampEnd, activitytype));
				return;
			}
		}
		
		// Add as last element
		events.add(i, new DataSet(timestampStart, timestampEnd, activitytype));
	}
	
	/**
	 *  Ends the activity at timestamp
	 */
	public void endActivityAt(long timestamp){
		int currentActivity = getActivityAt(((IntervalDataVisualization)vis).getCurrentMenuActivity(), timestamp);
		
		if(currentActivity != DataSet.NO_ACTIVITY)
			events.get(currentActivity).timestampEnd = timestamp;
	}
	
	public void deleteActivityAt(long timestamp){
		int currentActivity = getActivityAt(((IntervalDataVisualization)vis).getCurrentMenuActivity(), timestamp);
		
		if(currentActivity != DataSet.NO_ACTIVITY)
			events.remove(currentActivity);
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
		return ACTIVITIES;
	}

	/**
	 * Returns the activity number of the active activity at timestamp or NO_ACTIVITY if there is no current activity
	 * @param timestamp
	 * @return
	 */
	public int getActivityAt(int requestedActivity, long timestamp) {
		
		for(int i = 0; i < events.size(); i++){
			// Return activity if it is either of the right kind or if no type is requested
			if((events.get(i).activitytype == requestedActivity || requestedActivity == DataSet.NO_ACTIVITY)
				&& events.get(i).timestampStart < timestamp 
				&& (events.get(i).timestampEnd > timestamp || events.get(i).timestampEnd == 0))
				
				return i;
		}

		return DataSet.NO_ACTIVITY;
	}

	public long getLength() {
		if(events.size() < 2){
			return 0;
		}
		else{
			return events.getLast().timestampEnd - events.getFirst().timestampStart;
		}
	}
}
