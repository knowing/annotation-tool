package evaluationtool;

import javax.swing.JPanel;

import evaluationtool.gui.Visualization;

/**
 * Interface for all types of data that can be loaded
 * @author anfi
 *
 */
public interface Data {
	public Visualization getVisualization();
	public String getSource();
}
