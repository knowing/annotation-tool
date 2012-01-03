package evaluationtool.intervaldata;

public class DataSet {
	public long timestampStart;
	public long timestampEnd;
	public int activitytype;
	
	public static final int NO_ACTIVITY = -1;
	public static final int DELETE_ACTIVITY = -2;

	
	DataSet(long tstart, long tend, int activitytype){
		timestampStart = tstart;
		timestampEnd = tend;
		this.activitytype = activitytype;
	}
}
