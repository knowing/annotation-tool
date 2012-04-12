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

/**
 * Detects steps by comparing each sample to three dimensional thresholds, if every value is larger than its threshold, a point annotation is set to mark a step in the data
 * @author anfi
 *
 */
public class ThresholdStepdetector extends Stepdetector{
	
	// Number that has to be reached
	private int thresholdX,  thresholdY,  thresholdZ;
	
	// True if threshold is minimum, false for max.
	private boolean thresholdDirectionX, thresholdDirectionY, thresholdDirectionZ;
	
	// Is source data loaded?
	boolean dataLoaded = false;
	
	/**
	 * Takes ("min"|"max") [value] for thre dimensions, e.g. 'min 10 min 10 min 10' if you want to detect every point where each value is larger than 10
	 * If no parameter is given, the default is 'min -129 min -129 min 0'
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length >= 6){
			ThresholdStepdetector s = new ThresholdStepdetector(args);}
		else{
			args = new String[]{"min", "-129", "min", "-129", "min", "0"};
			ThresholdStepdetector s = new ThresholdStepdetector(args);
			}
	}

	/**
	 * Opens JFileChoosers to let the user determin input and output file. Input has to be a .sdr-File, output is always an arff file
	 * @param args
	 */
	ThresholdStepdetector(String[] args){
		thresholdDirectionX 	= args[0].equals("min");
		thresholdX			= Integer.parseInt(args[1]);
		thresholdDirectionY 	= args[2].equals("min");
		thresholdY			= Integer.parseInt(args[3]);
		thresholdDirectionZ 	= args[4].equals("min");
		thresholdZ			= Integer.parseInt(args[5]);
		
		
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


	
	public LinkedList<Timestamp> detectSteps(File output) {
		LinkedList<Timestamp> list = new LinkedList<Timestamp>();
		
		int counter = 0;
		boolean checkX, checkY, checkZ;
		
		for(int i = 0; i < valuesX.length; i++){
			
			counter = Math.max(--counter, 0);
			
			// Check for step if counter is at 0
			if(counter == 0){
				
				// Check for all dimensions, whether the threshold is met
				checkX = ((thresholdDirectionX && valuesX[i] > thresholdX) 
						|| (!thresholdDirectionX && valuesX[i] < thresholdX));
				checkY = ((thresholdDirectionY && valuesY[i] > thresholdY) 
						|| (!thresholdDirectionY && valuesY[i] < thresholdY));
				checkZ = ((thresholdDirectionZ && valuesZ[i] > thresholdZ) 
						|| (!thresholdDirectionZ && valuesZ[i] < thresholdZ));
				
				// If all values are larger than than their threhsold, add a step
				if(checkX && checkY && checkZ){
							// Add step to output
							addTimestamp(list, timestamps[i]);
							// Set counter
							counter = 10;
						}
			}
		}
		
		return list;
	}
}
