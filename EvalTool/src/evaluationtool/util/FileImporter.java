package evaluationtool.util;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import de.sendsor.SDRConverter;
import evaluationtool.Data;
import evaluationtool.DataModel;
import evaluationtool.intervaldata.IntervalData;
import evaluationtool.intervaldata.IntervalDataVisualization;
import evaluationtool.pointdata.PointData;
import evaluationtool.pointdata.PointDataVisualization;
import evaluationtool.pointdata.Timestamp;
import evaluationtool.sensordata.DataSet;
import evaluationtool.sensordata.SensorData;

/**
 * Imports sensor data or interval data into a given DataModel
 * @author anfi
 *
 */
public class FileImporter {
	
	/**
	 * Handles new files and eliminating duplicates
	 * @param src
	 */
	public static void loadFile(DataModel model, String src){
		
		StringTokenizer st = new StringTokenizer(src, ".");
		String fileExtension = "";
		
		// Check for duplicate and do not add track if it already exists
		if(ProjectFileHandler.getFilenameFromPath(src).equals(ProjectFileHandler.getFilenameFromPath(model.getVideoPath()))){
			return;
		}
		for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
			if(ProjectFileHandler.getFilenameFromPath(model.getLoadedDataTracks().get(i).getSource()).equals(ProjectFileHandler.getFilenameFromPath(src))){
				return;
			}

		}
		
		while(st.hasMoreElements()){
			fileExtension = st.nextToken();
		}
		
		boolean fileLoaded = false;
		
		try{
			if(!fileLoaded && (fileExtension.equals("sdr") || fileExtension.equals("arff") || fileExtension.equals("csv"))){
				fileLoaded = FileImporter.addDataTrack(model, src, fileExtension);
			}
				
			if(!fileLoaded && (fileExtension.equals("arff"))){
				fileLoaded = FileImporter.addIntervalTrack(model, src, fileExtension);
			}
			
			if(!fileLoaded && (fileExtension.equals("arff"))){
				fileLoaded = FileImporter.addPointTrack(model, src, fileExtension);
			}
				
		}
		catch(IOException ioe){
			JOptionPane.showMessageDialog(model.getGUI().getActiveFrame(), "Could not read from file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if(!fileLoaded && (fileExtension.equals("zip"))){
			String ret = ProjectFileHandler.loadProjectFile(src, model);
			if(ret == null){
				fileLoaded = true;
			}
			// Save config and wait for error message
			String s  = model.saveConfiguration();
						
			// If there is an error, create a message dialog
				if(s != null){
					JOptionPane.showMessageDialog(model.getGUI().getActiveFrame(), "Error saving configuration: " + s, "File error", JOptionPane.ERROR_MESSAGE);
				}
		}
		// If it is not a readable data track, try to open as video
		if(!fileLoaded){
			model.setVideoPath(src);
		}
			
	}
	
	/**
	 * Determines file types and adds data track
	 * @param src
	 * @param fileExtension
	 * @return 
	 * @throws IOException
	 */
	public static boolean addDataTrack(DataModel model, String src, String fileExtension) throws IOException{
	
		Data newData = null;

		// Load sdr file with SDRConverter
		if(fileExtension.equals("sdr")){
			SDRConverter sdrc = new SDRConverter();
			sdrc.setRelativeTimestamp(true);
			sdrc.setAggregate(SDRConverter.AGGREGATE_NONE);
			
			sdrc.setFile(new File(src));
			Instances ins = sdrc.getDataSet();
			
			long firstTimestamp = (long)ins.get(0).value(0);
			
			newData = new SensorData(model, ins.size(), src);	

			for(int i = 0; i < ins.size(); i++){
					((SensorData)newData).setDataAt(i, new DataSet((long)ins.get(i).value(0) - firstTimestamp, 
										 new int[]{(int)ins.get(i).value(1), 
												   (int)ins.get(i).value(2), 
												   (int)ins.get(i).value(3)}));
			}
		}
		/* 
		 * Load arff file if it has attributes timestamp and second is numeric.
		 * All numeric attriubtes will be shown.
		 */
		else if(fileExtension.equals("arff")){
			weka.core.converters.ArffLoader arffin = new ArffLoader();
			arffin.setFile(new File(src));
			
			if(		arffin.getStructure().numAttributes() > 1 &&
					arffin.getStructure().attribute(0).name().equals("timestamp") 
				&& 	arffin.getStructure().attribute(1).isNumeric()){
				
				Instances ins = arffin.getDataSet();
				
				long firstTimestamp = (long)ins.get(0).value(0);
				
				newData = new SensorData(model, ins.size(), src);	
				
				// Count numeric attributes
				int nNumeric = 0;
				
				for(int a = 1; a < ins.numAttributes(); a++){
					if(ins.attribute(a).isNumeric()){
						nNumeric++;
					}
				}
				int counter;
	
				// Add all samples
				for(int i = 0; i < ins.size(); i++){
					
					// Create array for values
					int[] temp = new int[nNumeric];
					counter = 0;
					
					for(int a = 1; a < ins.numAttributes(); a++){
						// Add values of they are numeric
						if(ins.attribute(a).isNumeric())
							temp[counter++] = (int)ins.get(i).value(a);
					}
							
						((SensorData)newData).setDataAt(i, new DataSet((long)ins.get(i).value(0) - firstTimestamp, temp));
				}
			}
		}
		
		// Add track to list and to layout
		
		if(newData != null){
			if(model.getLoadedDataTracks().size()%2 == 0)
				newData.getVisualization().setAlternativeColorScheme(true);
			else
				newData.getVisualization().setAlternativeColorScheme(false);
			
			// Add to list
			model.getLoadedDataTracks().add(newData);
			model.getGUI().updateFrames();
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Adds an interval track for algorithm results
	 * @return 
	 */
	public static boolean addIntervalTrack(DataModel model, String src, String fileExtension) throws IOException{
		
		IntervalData newData = null;

		if(fileExtension.equals("arff")){
			weka.core.converters.ArffLoader arffin = new ArffLoader();
			arffin.setFile(new File(src));

			if(		arffin.getStructure().numAttributes() > 2 &&
					arffin.getStructure().attribute(0).name().equals("from") &&
					arffin.getStructure().attribute(1).name().equals("to") && 
					arffin.getStructure().attribute(2).name().equals("class")){
				
				String[] activities = new String[arffin.getStructure().attribute(2).numValues()];
				
				for(int i = 0; i < activities.length; i++){
					activities[i] = arffin.getStructure().attribute(2).value(i);
				}
				
				newData = new IntervalData(model, src, activities);
				
				Instances ins = arffin.getDataSet();

				//long firstTimestamp = (long)ins.get(0).value(0);
				long firstTimestamp = 0;

				for(int i = 0; i < ins.size(); i++){
					((IntervalData)newData).createAndAddEvent((long)ins.get(i).value(0) - firstTimestamp, (long)ins.get(i).value(1) - firstTimestamp, (int)ins.get(i).value(2));
				}
			}
		}
		
		// Add track to list and to layout
		
		if(newData != null){
			if(model.getLoadedDataTracks().size()%2 == 0)
				newData.getVisualization().setAlternativeColorScheme(true);
			else
				newData.getVisualization().setAlternativeColorScheme(false);
			
			// Add to list
			model.getLoadedDataTracks().add(newData);
			((IntervalDataVisualization)newData.getVisualization()).toggleLocked();
			model.getGUI().updateFrames();
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Adds a point track for algorithm results
	 * @return 
	 */
	public static boolean addPointTrack(DataModel model, String src, String fileExtension) throws IOException{
		
		PointData newData = null;

		if(fileExtension.equals("arff")){
			weka.core.converters.ArffLoader arffin = new ArffLoader();
			arffin.setFile(new File(src));

			if(		arffin.getStructure().numAttributes() == 1 &&
					arffin.getStructure().attribute(0).name().equals("timestamp")){
	
				newData = new PointData(model, src);
				
				Instances ins = arffin.getDataSet();

				//long firstTimestamp = (long)ins.get(0).value(0);
				long firstTimestamp = 0;

				for(int i = 0; i < ins.size(); i++){
					System.out.println("Importing arff point data. Point " + i);
					newData.addPoint(new Timestamp((long)ins.get(i).value(0) - firstTimestamp));
				}
				
				newData.createQuickPoints();
			}
		}
		
		// Add track to list and to layout
		
		if(newData != null){
			if(model.getLoadedDataTracks().size()%2 == 0)
				newData.getVisualization().setAlternativeColorScheme(true);
			else
				newData.getVisualization().setAlternativeColorScheme(false);
			
			// Add to list
			model.getLoadedDataTracks().add(newData);
			((PointDataVisualization)newData.getVisualization()).toggleLocked();
			model.getGUI().updateFrames();
			return true;
		}
		else{
			return false;
		}
	}
}
