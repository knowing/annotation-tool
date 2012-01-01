package evaluationtool.intervaldata;

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
		
	// Visualization track
	IntervalDataVisualization vis;
	
	public IntervalData(DataModel model, String src){
		source = src;
		this.model = model;
		
		// Create visualization
		vis = new IntervalDataVisualization(this);
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
	
	public long getOffset(){
		return offset;
	}
	
	public float getPlaybackSpeed(){
		return playbackSpeed;
	}

	public void remove() {
		model.removeTrack(this);
	}
}
