package evaluationtool;

import javax.swing.JPanel;

import evaluationtool.gui.Visualization;

/**
 * Interface for all types of data that can be loaded
 * @author anfi
 *
 */
public interface Data {
	// Returns a JComponent that represents the loaded data
	public Visualization getVisualization();
	
	// Filename
	public String getSource();
	
	public float getPlaybackSpeed();
	public void setPlaybackSpeed(float f);
	public void setOffset(long ms);
	public long getOffset();
	public void remove();

	public long getLength();
	public DataModel getModel();
}
