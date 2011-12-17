package evaluationtool;

/**
 * The class offers methods to create String representations of tiemstamps
 * @author anfi
 *
 */
public class TimestampConverter {
	
	public static String getVideoTimestamp(long time){
		// Time is in ms
		long ms 	= time % 1000;
		
		// Time in s
		time 		= (time - ms) / 1000;
		
		// Calculate hours
		long h 		= time / 3600;
		time	 	= time - h * 3600;
		
		// Calculate minutes and seconds
		long min 	= time / 60;
		long s		= time - min * 60;
		
		// Build String
		String sMin;
		if(min < 10)
			sMin = "0" + min;
		else
			sMin = ""  + min;
		
		String sSec;
		if(s < 10)
			sSec = "0" + s;
		else
			sSec = ""  + s;
		
		String sMinsec;
		if(ms < 10)
			sMinsec = "00" + ms;
		else if(ms < 100)
			sMinsec = "0"  + ms;
		else
			sMinsec = ""  + ms;
		
		return (h + ":" + sMin + ":" + sSec + "." + sMinsec);
	}
}
