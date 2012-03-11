package stepdetector;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;

public class VectorStepdetector {
	
	private final int threshold = 90;
	private int gravityX, gravityY, gravityZ;
	
	// Length of Buffer that is used to get gravity
	private final int BUFFER_LENGTH = 200;
	
	// Prevent that a step is detected twice - in data samples
	private final int MIN_STEPLENGTH = 20;
	
	// Loaded data
	boolean dataLoaded = false;
	long[] timestamps;
	int[] valuesX, valuesY, valuesZ;
	
	public static void main(String[] args){
			VectorStepdetector s = new VectorStepdetector(args);
	}

	VectorStepdetector(String[] args){
		
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
		
		// Normalized values
		int normValueX = 0;
		int normValueY = 0;
		int normValueZ = 0;
		
		double normLength = 0;
		double vectorLength = 0;
		double gravityLength = 0;
		double scalar = 0;
		double cosAngle = 0;
		
		for(int i = BUFFER_LENGTH; i < valuesX.length; i++){
			
			freezeFor = Math.max(--freezeFor, 0);
			
			if(freezeFor == 0){
				
				// Calculate gravity
				gravityX = averageArray(valuesX, i - BUFFER_LENGTH, i);
				gravityY = averageArray(valuesY, i - BUFFER_LENGTH, i);
				gravityZ = averageArray(valuesZ, i - BUFFER_LENGTH, i);
				gravityLength =  Math.sqrt(
						  Math.pow(gravityX, 2) + 
						  Math.pow(gravityY, 2) + 
						  Math.pow(gravityZ, 2));
				
				 System.out.println("Gravity: (" + gravityX  + ", " + gravityY + ", " + gravityZ + ") Total = " + Math.sqrt(
																																		  Math.pow(gravityX/64.0, 2) + 
																																		  Math.pow(gravityY/64.0, 2) + 
																																		  Math.pow(gravityZ/64.0, 2)) + "g");
				// Apply "highpass"
				normValueX = valuesX[i] - gravityX;
				normValueY = valuesY[i] - gravityY;
				normValueZ = valuesZ[i] - gravityZ;
				
				normLength =  Math.sqrt(
						  Math.pow(normValueX, 2) + 
						  Math.pow(normValueY, 2) + 
						  Math.pow(normValueZ, 2));
				
				vectorLength =  Math.sqrt(
						  Math.pow(valuesX[i], 2) + 
						  Math.pow(valuesY[i], 2) + 
						  Math.pow(valuesZ[i], 2));
				
				System.out.println("Values: (" + normValueX + ", " + normValueY + ", " + normValueZ + ") Total = " + normLength);
				
				scalar = (gravityX * valuesX[i] + gravityY * valuesY[i] + gravityZ * valuesZ[i]);
				cosAngle =  scalar / (vectorLength * gravityLength);
				System.out.println("normLength = " + normLength);
				
				if(normLength > threshold){
					
					addTimestamp(list, timestamps[i]);
					freezeFor = MIN_STEPLENGTH;
				}
			}
			
			/*
			 * TEST, stop after ten minutes
			 */
			if(timestamps[i] - timestamps[0] > 600000)
				return list;
		}
		
		return list;
	}
	
	/**
	 * Calculates the average value of an int[] from position a to position b
	 * @param a
	 * @param from
	 * @param to
	 * @return
	 */
	private int averageArray(int[] array, int a, int b){
		int ret = 0;
		
		for(int i = a; i <= b; i++){
			ret += array[i];
		}
		
		return ret / (b - a);
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
