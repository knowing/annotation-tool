package stepdetector;

import java.io.File;
import java.util.LinkedList;

import javax.swing.JFileChooser;

/**
 * Detects steps by building a three-dimensional vector for each sample. It compares its length with a threshold and also calculates the angle between peak and gravity,
 * a step is only set, if the angle is wide enough
 * @author anfi
 *
 */
public class VectorStepdetector extends Stepdetector {
	
	// Minimu length of peak
	private final int threshold = 120;
	
	// Minmum angle between peak vector and gravity
	private final int threshold_angle = 60;
	
	// Saves the current calculation for gravity
	private int gravityX, gravityY, gravityZ;
	
	// Length of Buffer that is used to calculate gravity
	private final int BUFFER_LENGTH = 200;
	
	// Prevent that a step is detected twice - in data samples
	private final int MIN_STEPLENGTH = 10; // 400 ms
	
	// Loaded data
	boolean dataLoaded = false;
	
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
	

	public LinkedList<Timestamp> detectSteps(File output) {
		LinkedList<Timestamp> list = new LinkedList<Timestamp>();
		
		int freezeFor = 0;
		
		// Normalized values
		int normValueX = 0;
		int normValueY = 0;
		int normValueZ = 0;
		
		double normLength = 0;
		double vectorLength = 0;
		double gravityLength = 0;
		double scalar = 0;
		double cosAngle = 0;
		double angle = 0;
		
		for(int i = BUFFER_LENGTH; i < valuesX.length; i++){
			
			freezeFor = Math.max(--freezeFor, 0);
			
				// Calculate gravity
				gravityX = averageArray(valuesX, i - BUFFER_LENGTH, i);
				gravityY = averageArray(valuesY, i - BUFFER_LENGTH, i);
				gravityZ = averageArray(valuesZ, i - BUFFER_LENGTH, i);
				gravityLength =  Math.sqrt(
						  Math.pow(gravityX, 2) + 
						  Math.pow(gravityY, 2) + 
						  Math.pow(gravityZ, 2));
				
				// Apply "highpass"
				normValueX = valuesX[i] - gravityX;
				normValueY = valuesY[i] - gravityY;
				normValueZ = valuesZ[i] - gravityZ;
				
				
				// Length of normalized vector
				normLength =  Math.sqrt(
						  Math.pow(normValueX, 2) + 
						  Math.pow(normValueY, 2) + 
						  Math.pow(normValueZ, 2));
				
				vectorLength =  Math.sqrt(
						  Math.pow(valuesX[i], 2) + 
						  Math.pow(valuesY[i], 2) + 
						  Math.pow(valuesZ[i], 2));
				
				// Scalar between gravity and peak
				scalar = (gravityX * valuesX[i] + gravityY * valuesY[i] + gravityZ * valuesZ[i]);
				
				// Angle between gravity and the actual peak. This is not the normalized vector. The normalized vector could be used as well, but the threshold angle would change (increase).
				cosAngle =  scalar / (vectorLength * gravityLength);
				angle = Math.acos(cosAngle) * 180 / Math.PI;
				
				// If both thresholds are met, set a step
				if(normLength > threshold && angle > threshold_angle){
					
					// Add step if freezetime is over, reset freezetime otherwise
					if(freezeFor == 0){
						System.out.println("Adding step\n-------------------------------------------------------->> " + list.size());
						addTimestamp(list, timestamps[i]);
						freezeFor = MIN_STEPLENGTH;
					}
				}
		}
		
		return list;
	}
	
	/**
	 * Calculates the average value of an int[] from position a to position b. This is the way to get gravity.
	 * @param a
	 * @param from
	 * @param to
	 * @return The average of all numbers in the specified area of the given array
	 */
	private int averageArray(int[] array, int a, int b){
		int ret = 0;
		
		for(int i = a; i <= b; i++){
			ret += array[i];
		}
		
		return ret / (b - a);
	}
}
