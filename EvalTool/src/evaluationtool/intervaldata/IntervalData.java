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
	
	// Changes
	boolean changed = false;
	
	// Possible activities
	private String[] ACTIVITIES;
	
	// LinkedList containing all start- and endpoints
	LinkedList<Activity> events = new LinkedList<Activity>();
		
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
		addEvent(timestampStart, timestampEnd, activitytype);
	}
	
	/**
	 * Adds an event to the list of events
	 * @param timestampStart
	 * @param timestampEnd
	 * @param activitytype
	 */
	public void addEvent(long start, long end, int activity){
		Activity set;
		
		if(end != 0)
			set = new Activity(removeSettingsFromTimestamp(start), removeSettingsFromTimestamp(end), activity); 
		else
			set = new Activity(removeSettingsFromTimestamp(start), 0, activity); 
		
		// Sort by starttime
		for(int i = 0; i < events.size(); i++){
			if(set.timestampStart < events.get(i).timestampStart){
				events.add(i, set);
				mergeOverlappingActivities(i);
				vis.getTrackVisualization().releaseEvent();
				return;
			}
		}
		
		// Add as last element
		events.add(set);
		mergeOverlappingActivities(events.size() - 1);
		vis.getTrackVisualization().releaseEvent();
	}
	
	/**
	 * Combines two activities if they overlap
	 * @param draggedEvent
	 */
	public void mergeOverlappingActivities(int draggedEventIndex) {
		Activity draggedEvent = events.get(draggedEventIndex);
		// First check for overlaps
				for(int i = 0; i < events.size(); i++){
					if(Activity.doOverlap(events.get(i), draggedEvent)){
						// Merge events
						draggedEvent.timestampStart = Math.min(draggedEvent.timestampStart, events.get(i).timestampStart);
						draggedEvent.timestampEnd = Math.max(draggedEvent.timestampEnd, events.get(i).timestampEnd);
						
						// Delete existing event
						events.remove(i);
						vis.getTrackVisualization().releaseEvent();
					}
				}
				
		vis.getTrackVisualization().repaint();
	}

	/**
	 *  Ends the activity at timestamp
	 */
	public void endActivityAt(long timestamp){
		int currentActivity = getActivityAt(((IntervalDataVisualization)vis).getCurrentMenuActivity(), timestamp);
		
		if(currentActivity != Activity.NO_ACTIVITY)
			events.get(currentActivity).timestampEnd = removeSettingsFromTimestamp(timestamp);
		
		vis.getTrackVisualization().releaseEvent();
	}
	
	public void deleteActivityAtCurrentPosition() {
		deleteActivityAt(vis.getTrackVisualization().getPosition());
	}
	
	/**
	 * Removes all activities that happen at the speciefied time.
	 * @param timestamp
	 */
	public void deleteActivityAt(long timestamp){
		int currentActivity = Activity.NO_ACTIVITY;
		
		for(int i = 0; i < ACTIVITIES.length; i++){
			currentActivity = getActivityAt(i, timestamp);
			
			if(currentActivity != -1){
				events.remove(currentActivity);
			}
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
	public long getEventStart(int i){
		return addSettingsToTimestamp(events.get(i).timestampStart);
	}
	
	public long getEventEnd(int i){
		if(events.get(i).timestampEnd != 0)
			return addSettingsToTimestamp(events.get(i).timestampEnd);
		else
			return 0;
	}
	
	public int getEventActivity(int i){
		return events.get(i).activitytype;
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
			if((events.get(i).activitytype == requestedActivity || requestedActivity == Activity.NO_ACTIVITY)
				&& getEventStart(i) <= timestamp 
				&& (getEventEnd(i) >= timestamp || getEventEnd(i) == 0))
				
				return i;
		}

		return Activity.NO_ACTIVITY;
	}

	public long getLength() {
		if(events.size() < 2){
			return 0;
		}
		else{
			return getEventEnd(events.size() - 1) - getEventStart(0);
		}
	}

	public void setSource(String filename) {
		this.source = filename;
	}
	
	/**
	 * This will bring the events in proper order
	 */
	public void orderEvents() {

		LinkedList<Activity> eventsOld = events;
		events = new LinkedList<Activity>();
		
		while(!eventsOld.isEmpty()){
			readdEvent(eventsOld.poll());
		}
	}

	private void readdEvent(Activity set){
		for(int i = 0; i < events.size(); i++){
			if(set.timestampStart < events.get(i).timestampStart){
				events.add(i, set);
				return;
			}
		}
		
		// Add as last element
		events.add(set);
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean b) {
		locked = b;
	}

	public void toggleEventAtCurrentPosition(int activity) {
		int currentActivity = getActivityAt(activity, vis.getTrackVisualization().getPosition());
		
		if(currentActivity == -1){
			addEventAtCurrentPosition(activity);
		}
		else{
			events.get(currentActivity).timestampEnd = removeSettingsFromTimestamp(vis.getTrackVisualization().getPosition());
		}
		vis.getTrackVisualization().repaint();
	}
	
	public long addSettingsToTimestamp(long time) {
		return (long)(time / getPlaybackSpeed() + getOffset());
	}
	public long removeSettingsFromTimestamp(long time){
		return (long)((time - getOffset()) * getPlaybackSpeed());
	}

	public int getNEvents() {
		return events.size();
	}
	
	public LinkedList<Activity> getEvents(){
		return events;
	}
	public void moveStartpoint(int draggedEvent, long time) {
		events.get(draggedEvent).timestampStart = removeSettingsFromTimestamp(time);
		if(events.get(draggedEvent).timestampStart > events.get(draggedEvent).timestampEnd){
			events.get(draggedEvent).timestampEnd = events.get(draggedEvent).timestampStart;
		}
	}
	
	public void moveEndpoint(int draggedEvent, long time) {
		events.get(draggedEvent).timestampEnd = removeSettingsFromTimestamp(time);
		if(events.get(draggedEvent).timestampEnd < events.get(draggedEvent).timestampStart){
			events.get(draggedEvent).timestampStart = events.get(draggedEvent).timestampEnd;
		}
	}

	public void moveEvent(int draggedEvent, float f) {
		events.get(draggedEvent).timestampStart += f * playbackSpeed;
		events.get(draggedEvent).timestampEnd += f * playbackSpeed;
	}

	public boolean getChanged() {
		return !vis.isLocked();
	}
}
