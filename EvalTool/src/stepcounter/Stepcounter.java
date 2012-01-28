package stepcounter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;
import evaluationtool.util.TimestampConverter;

public class Stepcounter {
	
	private final int THRESHOLD = 80;
	boolean dataLoaded = false;
	private final int BUFFER = 100;
	private final int ESTIMATED_STEPLENGTH = 20;
	
	long[] timestamps;
	int[] valuesX, valuesY, valuesZ;
	
	public static void main(String[] args){
		Stepcounter s = new Stepcounter();
	}

	Stepcounter(){
		JFileChooser jfc = new JFileChooser();
		jfc.showOpenDialog(null);
		File input = jfc.getSelectedFile();
		dataLoaded = loadData(input);
		if(dataLoaded && valuesY.length > BUFFER){
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
		
		int averageX, averageY, averageZ;
		int currentX = 0;
		int currentY = 0; 
		int currentZ = 0;
		int nextX = 0;
		int nextY = 0;
		int nextZ = 0;
		int previousX, previousY, previousZ;
		
		for(int i = BUFFER; i < valuesY.length - 1; i++){
			averageX = Math.abs(averageArray(valuesX, i - BUFFER, i));
			averageY = Math.abs(averageArray(valuesY, i - BUFFER, i));
			averageZ = Math.abs(averageArray(valuesZ, i - BUFFER, i));
			
			previousX = currentX;
			previousY = currentY;
			previousZ = currentZ;
			
			currentX = nextX;
			currentY = nextY;
			currentZ = nextZ;
			
			nextX = Math.abs(valuesX[i + 1] - averageX);
			nextY = Math.abs(valuesY[i + 1] - averageY);
			nextZ = Math.abs(valuesZ[i + 1] - averageZ);
			
			
			System.out.println(TimestampConverter.getVideoTimestamp(timestamps[i] - timestamps[0]) + ": " + currentY + ", " +averageY);
			
			freezeFor = Math.max(--freezeFor, 0);
			
			if(freezeFor == 0 && 
					currentY >= THRESHOLD && currentY >= previousY && currentY > nextY &&
											 currentZ >= previousZ && currentZ > nextZ){
				addTimestamp(list, timestamps[i]);
				freezeFor = 10;
			}
			
			/*
			 * TEST
			 */
			if(timestamps[i] - timestamps[0] > 2400000)
				return list;
		}
		
		return list;
	}

	private int averageArray(int[] a, int from, int to){
		int ret = 0;
		
		for(int i = from; i <= to; i++){
			ret += a[i];
		}
		
		return ret / (to - from);
	}
	
	private void addTimestamp(LinkedList<Timestamp> list, long time){
		Timestamp t = new Timestamp();
		t.timestamp = time;
		list.add(t);
	}
	
	class Timestamp{
		public long timestamp;
	}
}
