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
		
		if(ae.getSource() == vis.syncPositionItem){
			vis.getDataSource().setOffset(vis.getDataSource().getOffset() - vis.getCurrentMenuTime() + vis.getTrackVisualization().getPosition());
		}
		else if(!vis.isLocked()){
			int activitytype;
			activitytype = Integer.parseInt(ae.getActionCommand().substring(1));
			
			if(activitytype == Activity.NO_ACTIVITY){
				vis.getDataSource().endActivityAt(vis.getCurrentMenuTime());
			}
			else if(activitytype == Activity.DELETE_ACTIVITY){
				vis.getDataSource().deleteActivityAt(vis.getCurrentMenuTime());
			}
			else if(activitytype == Activity.CURRENT_ACTIVITY){	
				// Add event at menu position and parse action command to activity
				vis.getDataSource().createAndAddEvent(vis.getCurrentMenuTime(), 0, vis.getCurrentMenuActivity());
			}
		}
	}

}
