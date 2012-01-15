package evaluationtool.gui;

import javax.swing.JPanel;

import evaluationtool.Data;

public abstract class Visualization extends JPanel{
	abstract public void setPosition(long f);
	abstract public void setAlternativeColorScheme(boolean b);
	abstract public void updateLayout();
	abstract public Data getDataSource();
	
	abstract public JPanel getTrackVisualization();
}
