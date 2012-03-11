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

public class ThresholdStepdetector {
	
	private int[] threshold = new int[3];
	private boolean[] threshold_min = new boolean[3];
	boolean dataLoaded = false;
	
	long[] timestamps;
	int[] valuesX, valuesY, valuesZ;
	
	public static void main(String[] args){
		if(args.length == 6){
			ThresholdStepdetector s = new ThresholdStepdetector(args);}
		else{
			args = new String[]{"min", "90", "min", "128", "min", "0"};
			ThresholdStepdetector s = new ThresholdStepdetector(args);
			}
	}

	ThresholdStepdetector(String[] args){
		threshold_min[0] 	= args[0].equals("min");
		threshold[0]			= Integer.parseInt(args[1]);
		threshold_min[1] 	= args[2].equals("min");
		threshold[1]			= Integer.parseInt(args[3]);
		threshold_min[2] 	= args[4].equals("min");
		threshold[2]			= Integer.parseInt(args[5]);
		
		
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
		
		System.out.println("Offset: " + threshold_min[0] + ", " + threshold[0] + " - " + 
				threshold_min[1] + ", " + threshold[1] + " - " + 
				threshold_min[2] + ", " + threshold[2]);
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
		
		for(int i = 0; i < valuesX.length; i++){
			
			freezeFor = Math.max(--freezeFor, 0);
			
			if(freezeFor == 0){
				
				if(((threshold_min[0] && valuesX[i] > threshold[0]) || (!threshold_min[0] && valuesX[i] < threshold[0])) ||
				   ((threshold_min[1] && valuesY[i] > threshold[1]) || (!threshold_min[1] && valuesY[i] < threshold[1])) ||
				   ((threshold_min[2] && valuesZ[i] > threshold[2]) || (!threshold_min[2] && valuesZ[i] < threshold[2]))){
					addTimestamp(list, timestamps[i]);
					freezeFor = 10;
				}
			}
			
			/*
			 * TEST, don't analyze all data
			 */
			if(timestamps[i] - timestamps[0] > 600000)
				return list;
		}
		
		return list;
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
