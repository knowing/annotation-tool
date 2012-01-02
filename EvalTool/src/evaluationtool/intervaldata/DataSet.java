package evaluationtool.intervaldata;

public class DataSet {
	public long timestamp;
	public int activitytype;
	
	public static int NO_ACTIVITY = -1;
	public static String[] ACTIVITIES = new String[]{"Running", "Walking", "Biking", "Skating", "Crosstrainer"};
	
	DataSet(long t, int activitytype){
		timestamp = t;
		this.activitytype = activitytype;
	}
	
	public static String[] getPossibleActivities(){
		return ACTIVITIES;
	}
}
