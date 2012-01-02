package evaluationtool.intervaldata;

public class DataSet {
	public long timestampStart;
	public long timestampEnd;
	public int activitytype;
	
	public static int NO_ACTIVITY = -1;
	public static int DELETE_ACTIVITY = -2;
	public static String[] ACTIVITIES = new String[]{"Running", "Walking", "Biking", "Skating", "Crosstrainer"};
	
	DataSet(long tstart, long tend, int activitytype){
		timestampStart = tstart;
		timestampEnd = tend;
		this.activitytype = activitytype;
	}
	
	public static String[] getPossibleActivities(){
		return ACTIVITIES;
	}
}
