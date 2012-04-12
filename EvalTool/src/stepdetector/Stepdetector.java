package stepdetector;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import weka.core.DenseInstance;
import weka.core.Instances;
import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;

/**
 * Abstract stepdetector, only the actual step recognition and file selection have to be implemented
 * @author anfi
 */
public abstract class Stepdetector {

	long[] timestamps;
	int[] valuesX, valuesY, valuesZ;
	
	/**
	 * Saves point annotations to specified file
	 * @param output
	 * @param list
	 */
	public void saveFile(File output, LinkedList<Timestamp> list) {
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

		/**
		 * Loads an sdr file
		 * @param f
		 * @return
		 */
		public boolean loadData(File f){
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
		
		/**
		 * THis must be overwritten with a method that detects steps. It can use addTimestamp to add them to the results list.
		 * @param output
		 * @return
		 */
		public abstract LinkedList<Timestamp> detectSteps(File output);
		
		/**
		 * Adds a point to the results list
		 * @param list
		 * @param time
		 */
		public void addTimestamp(LinkedList<Timestamp> list, long time){
			System.out.println("Adding at " + time);
			Timestamp t = new Timestamp();
			t.timestamp = time;
			list.add(t);
		}
		
		class Timestamp{
			public long timestamp;
		}
	}
