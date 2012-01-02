package evaluationtool.intervaldata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

public class PopupMenuListener implements ActionListener{

	JPopupMenu menu;
	IntervalDataVisualization vis;
	
	PopupMenuListener(JPopupMenu menu, IntervalDataVisualization vis){
		this.menu = menu;
		this.vis = vis;
	}
	
	public void actionPerformed(ActionEvent ae) {
		int activitytype;
		activitytype = Integer.parseInt(ae.getActionCommand().substring(1));
		
		if(activitytype == DataSet.NO_ACTIVITY){
			vis.getDataSource().endActivityAt(vis.getCurrentMenuTime());
		}
		else if(activitytype == DataSet.DELETE_ACTIVITY){
			vis.getDataSource().deleteActivityAt(vis.getCurrentMenuTime());
		}
		else{	
			// Add event at menu position and parse action command to activity
			vis.getDataSource().addEvent(vis.getCurrentMenuTime(), 0, activitytype);
		}
	}

}
