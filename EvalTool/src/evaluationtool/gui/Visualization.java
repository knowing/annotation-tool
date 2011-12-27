package evaluationtool.gui;

import javax.swing.JPanel;

public abstract class Visualization extends JPanel{
	abstract public void setPosition(float f);
	abstract public void setAlternativeColorScheme(boolean b);
	abstract public void updateLayout();
}
