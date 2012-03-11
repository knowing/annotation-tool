package stepdetector;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;
import evaluationtool.util.TimestampConverter;

public class Stepdetector {
	
	private int[] THRESHOLD = new int[3];
	private boolean[] THRESHOLD_DIRECTION = new boolean[3];
	boolean dataLoaded = false;
	
	long[] timestamps;
	int[] valuesX, valuesY, valuesZ;
	
	public static void main(String[] args){
		if(args.length == 6){
			Stepdetector s = new Stepdetector(args);}
		else{
			args = new String[]{"pos", "100", "pos", "20", "pos", "0"};
			Stepdetector s = new Stepdetector(args);
			}
	}

	Stepdetector(String[] args){
		THRESHOLD_DIRECTION[0] 	= args[0].equals("pos");
		THRESHOLD[0]			= Integer.parseInt(args[1]);
		THRESHOLD_DIRECTION[1] 	= args[2].equals("pos");
		THRESHOLD[1]			= Integer.parseInt(args[3]);
		THRESHOLD_DIRECTION[2] 	= args[4].equals("pos");
		THRESHOLD[2]			= Integer.parseInt(args[5]);
		
		
		JFileChooser jfc = new JFileChooser();
		jfc.showOpenDialog(null);
		File input = jfc.getSelectedFile();
		dataLoaded = loadData(input);
		if(dataLoaded && valuesY.length > 0){
			jfc = new JFileChooser();
			jfc.showOpenDialog(null);
			File output = jfc.getSelectedFile();
			
			LinkedList<Timestamp> list = detectSteps(output);
			saveFile(output, list);
		}
		
		System.out.println("Offset: " + THRESHOLD_DIRECTION[0] + ", " + THRESHOLD[0] + " - " + 
				THRESHOLD_DIRECTION[1] + ", " + THRESHOLD[1] + " - " + 
				THRESHOLD_DIRECTION[2] + ", " + THRESHOLD[2]);
	}

	private void saveFile(File output, LinkedList<Timestamp> list) {
		weka.core.converters.ArffSaver arffout = new weka.core.converters.ArffSaver();
		try {
			output.createNewFile();
			arffout.setFile(output);
			LinkedList<String> atts = new LinkedList<String>();
			
			// Create structure for arff file
			arffout.setStructure(ResultsUtil.timeSeriesResult(atts));
			
			Instances ins = arffout.getInstances();
			
			int nPoints = list.size();
			
			for(int i = 0; i < nPoints; i++){
				DenseInstance instance = new DenseInstance(1);
				instance.setValue(ins.attribute(0), list.get(i).timestamp);
				
				ins.add(instance);
			}
			
			arffout.writeBatch();
			
		} catch (IOException e) {
			System.err.println("Error saving user generated track: " + e + "\nSkipping track.");
		}
	}

	private boolean loadData(File f){
		// Load sdr file with SDRConverter
		SDRConverter sdrc = new SDRConverter();
		sdrc.setRelativeTimestamp(true);
		sdrc.setAggregate(SDRConverter.AGGREGATE_NONE);
				
				try{	
					sdrc.setFile(f);
					Instances ins = sdrc.getDataSet();
						
					timestamps 		= new long[ins.size()];
					valuesX 		= new int[ins.size()];
					valuesY 		= new int[ins.size()];
					valuesZ 		= new int[ins.size()];
					
					long firstTimestamp = (long) ins.get(0).value(0);
			
					for(int i = 0; i < ins.size(); i++){
						timestamps[i] 	= (long)	ins.get(i).value(0) - firstTimestamp;
						valuesX[i] 		= (int)		ins.get(i).value(1);
						valuesY[i] 		= (int)		ins.get(i).value(2);
						valuesZ[i]		= (int)		ins.get(i).value(3);
					}
					
					return true;
				}
				catch(IOException ioe){
					System.err.println(ioe);
					return false;
				}
	}
	
	private LinkedList<Timestamp> detectSteps(File output) {
		LinkedList<Timestamp> list = new LinkedList<Timestamp>();
		
		int freezeFor = 0;
		
		// Add first point to synchronize
		addTimestamp(list, timestamps[0]);
		

		int xOkay = 0;
		int yOkay = 0;
		int zOkay = 0;
		
		for(int i = 0; i < valuesY.length; i++){

			System.out.println(TimestampConverter.getVideoTimestamp(timestamps[i] - timestamps[0]));
			
			freezeFor = Math.max(--freezeFor, 0);
			
			if(freezeFor == 0){
				
				if((THRESHOLD_DIRECTION[0] && aboveThreshold(valuesX, THRESHOLD[0], i)) || (!THRESHOLD_DIRECTION[0] && belowThreshold(valuesX, THRESHOLD[0], i)))
						xOkay = 10;
				if((THRESHOLD_DIRECTION[1] && aboveThreshold(valuesY, THRESHOLD[1], i)) || (!THRESHOLD_DIRECTION[1] && belowThreshold(valuesY, THRESHOLD[1], i)))
						yOkay = 10;
				if((THRESHOLD_DIRECTION[2] && aboveThreshold(valuesZ, THRESHOLD[2], i)) || (!THRESHOLD_DIRECTION[2] && belowThreshold(valuesZ, THRESHOLD[2], i)))
						zOkay = 10;
				
				
				if(xOkay + yOkay + zOkay > 20){
					addTimestamp(list, timestamps[i]);
					freezeFor = 10;
					
					xOkay = 0;
					yOkay = 0;
					zOkay = 0;
				}
				
				xOkay = Math.max(xOkay - 1, 0);
				yOkay = Math.max(yOkay - 1, 0);
				zOkay = Math.max(zOkay - 1, 0);
			}
			
			/*
			 * TEST, stop after ten minutes
			 */
			if(timestamps[i] - timestamps[0] > 600000)
				return list;
		}
		
		return list;
	}
	
	private boolean aboveThreshold(int[] data, int threshold, int pos){
		if(threshold < data[pos])
				return true;
		else 
			return false;

	}
	
	private boolean belowThreshold(int[] data, int threshold, int pos){
		if(threshold > data[pos])
			{	
				return true;
			}
		else 
			return false;

	}

	private int averageArray(int[] a, int from, int to){
		int ret = 0;
		
		for(int i = from; i <= to; i++){
			ret += a[i];
		}
		
		return ret / (to - from);
	}
	
	private void addTimestamp(LinkedList<Timestamp> list, long time){
		System.out.println("Adding at " + time);
		Timestamp t = new Timestamp();
		t.timestamp = time;
		list.add(t);
	}
	
	class Timestamp{
		public long timestamp;
	}
}
