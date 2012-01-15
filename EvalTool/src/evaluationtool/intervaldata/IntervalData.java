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
	private String[] ACTIVITIES;
	
	// LinkedList containing all start- and endpoints
	LinkedList<DataSet> events = new LinkedList<DataSet>();
		
	// Visualization track
	IntervalDataVisualization vis;
	
	// Determines whether the user can add, change, or remove intervals 
	boolean locked = false;
	
	public IntervalData(DataModel model, String src, String[] activities){
		source = src;
		this.model = model;
		
		ACTIVITIES = activities;
		
		// Create visualization
		vis = new IntervalDataVisualization(this);
	}
	
	public void addEventAtCurrentPosition(int activity) {
		if(activity >= 0 && activity < ACTIVITIES.length){
			createAndAddEvent(vis.getTrackVisualization().getPosition(), 0, activity);
		}
	}
	
	/**
	 * Creates an event from two timestamps and an activity number and adds in to the list
	 * @param timestampStart
	 * @param timestampEnd
	 * @param activitytype
	 */
	public void createAndAddEvent(long timestampStart, long timestampEnd, int activitytype){	
		DataSet set = new DataSet(timestampStart, timestampEnd, activitytype);
		addEvent(set);
	}
	
	/**
	 * Adds an event to the list of events
	 * @param timestampStart
	 * @param timestampEnd
	 * @param activitytype
	 */
	public void addEvent(DataSet set){

		mergeOverlappingActivities(set);
		
		// Sort by starttime
		for(int i = 0; i < events.size(); i++){
			if(set.timestampStart < events.get(i).timestampStart){
				events.add(i, set);
				return;
			}
		}
		
		// Add as last element
		events.add(set);
	}
	
	public void mergeOverlappingActivities(DataSet set) {
		// First check for overlaps
				for(int i = 0; i < events.size(); i++){
					if(DataSet.doOverlap(events.get(i), set)){
						// Merge events
						set.timestampStart = Math.min(set.timestampStart, events.get(i).timestampStart);
						set.timestampEnd = Math.max(set.timestampEnd, events.get(i).timestampEnd);
						
						// Delete existing event
						events.remove(i);
					}
				}
	}

	/**
	 *  Ends the activity at timestamp
	 */
	public void endActivityAt(long timestamp){
		int currentActivity = getActivityAt(((IntervalDataVisualization)vis).getCurrentMenuActivity(), timestamp);
		
		if(currentActivity != DataSet.NO_ACTIVITY)
			events.get(currentActivity).timestampEnd = timestamp;
	}
	
	public void deleteActivityAtCurrentPosition() {
		deleteActivityAt(vis.getTrackVisualization().getPosition());
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

	public void setSource(String filename) {
		this.source = filename;
	}
	
	/**
	 * This will bring the events in proper order
	 */
	public void orderEvents() {

		LinkedList<DataSet> eventsOld = events;
		events = new LinkedList<DataSet>();
		
		while(!eventsOld.isEmpty()){
			addEvent(eventsOld.pop());
		}
	}

	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean b) {
		locked = b;
	}
}
