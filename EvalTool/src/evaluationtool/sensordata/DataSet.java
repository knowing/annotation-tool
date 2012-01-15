package evaluationtool.sensordata;

/**
 * Represents one three dimensional sensor value - timestamp pair
 * @author anfi
 *
 */
public class DataSet{
	public long time;
	public int[] values;
	
	public DataSet(long t, int[] v){
		time = t;
		values = v;
	}
}