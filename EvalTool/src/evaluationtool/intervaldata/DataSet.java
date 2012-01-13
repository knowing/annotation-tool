package evaluationtool.intervaldata;

public class DataSet {
	public long timestampStart;
	public long timestampEnd;
	public int activitytype;
	
	public static final int NO_ACTIVITY = -1;
	public static final int DELETE_ACTIVITY = -2;
	public static final int CURRENT_ACTIVITY = -3;

	
	DataSet(long tstart, long tend, int activitytype){
		timestampStart = tstart;
		timestampEnd = tend;
		this.activitytype = activitytype;
	}
	
	public static boolean doOverlap(DataSet a, DataSet b){
		// Do not compare open activities, activities of different types and check if a == b
		if(a == b || a.activitytype != b.activitytype ||  a.timestampEnd == 0 || b.timestampEnd == 0)
			return false;
		
		if(a.timestampEnd < b.timestampStart || a.timestampStart > b.timestampEnd)
			return false;
		else
			return true;
	}
}
