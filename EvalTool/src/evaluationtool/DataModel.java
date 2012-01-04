package evaluationtool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

import de.sendsor.SDRConverter;

import evaluationtool.gui.EvalGUI;
import evaluationtool.intervaldata.IntervalData;
import evaluationtool.intervaldata.IntervalDataVisualization;
import evaluationtool.pointdata.SensorData;
import evaluationtool.pointdata.DataSet;
import evaluationtool.projecthandling.ProjectFileHandler;




/**
 * This class holds the data that is currently worked on
 * @author anfi
 *
 */
public class DataModel {
	String projectfile = "";
	String vlcdir = "";
	String videofile = "";
	
	public final String CONFIG_PATH = "config.cfg";
	public final String VLCPATH_LINE = "$VLCDIR=";
	public final String PROJECTPATH_LINE = "$PROJECTFILE=";
	
	public final String VIDEOPATH_LINE = "$VIDEOFILE=";
	public final String DATAPATH_LINE = "$DATAFILE=";
	public final String OFFSET_LINE = "$DATAOFFSET=";
	public final String SPEED_LINE = "$DATAZOOM=";
	
	LinkedList<Data> loadedDataTracks = new LinkedList<Data>();
	
	EvalGUI gui;
	
	public void setGUI(EvalGUI g){
		gui = g;
	}
	
	public EvalGUI getGUI(){
		return gui;
	}
	
	public String getVideoPath(){
		return videofile;
	}
	
	public boolean isVideoPlaying(){
		return gui.isVideoPlaying();
	}
	
	public String getVLCPath(){
		return vlcdir;
	}
	
	public String getProjectPath(){
		return projectfile;
	}
	
	public void setVideoTrack(String src){
		videofile = src;
		gui.loadVideo(src);
	}
	
	public void setVLCPath(String path){
		vlcdir = path;
	}
	
	public void setProjectPath(String path){
		projectfile = path;
		gui.setTitle("EvalTool - " + path );
	}
	
	
	/**
	 * Returns the length of the longest track in the current project
	 * @return
	 */
	public long getProjectLength(){	
		long start = 0;
		long end = gui.getVideoLength();
		
		for(int i = 0; i < loadedDataTracks.size(); i++){
			start 	= Math.min(start, loadedDataTracks.get(i).getOffset());
			end 	= Math.max(end, loadedDataTracks.get(i).getLength() + loadedDataTracks.get(i).getOffset());
		}
		
		return end - start;
	}
	
	/**
	 * Removes a data track from the project
	 * @param d
	 */
	public void removeTrack(Data d){
		System.out.println("Removing data track");
		loadedDataTracks.remove(d);
		
		for(int i = 0; i < loadedDataTracks.size(); i++){
			if(i%2 == 0)
				loadedDataTracks.get(i).getVisualization().setAlternativeColorScheme(true);
			else
				loadedDataTracks.get(i).getVisualization().setAlternativeColorScheme(false);
		}
		
		gui.updateDataFrame();
	}
	
	/**
	 * Handles new files and eliminating duplicates
	 * @param src
	 */
	public void loadFile(String src){
		StringTokenizer st = new StringTokenizer(src, ".");
		String fileExtension = "";
		
		// Check for duplicate and do not add track if it already exists
		if(ProjectFileHandler.getFilenameFromPath(src).equals(ProjectFileHandler.getFilenameFromPath(getVideoPath()))){
			return;
		}
		for(int i = 0; i < loadedDataTracks.size(); i++){
			if(ProjectFileHandler.getFilenameFromPath(loadedDataTracks.get(i).getSource()).equals(ProjectFileHandler.getFilenameFromPath(src))){
				return;
			}

		}
		
		while(st.hasMoreElements()){
			fileExtension = st.nextToken();
		}
		
		
		try{
			if(fileExtension.equals("sdr") || fileExtension.equals("arff") || fileExtension.equals("csv"))
				addDataTrack(src, fileExtension);
			if(fileExtension.equals("arff"))
				addIntervalTrack(src, fileExtension);
		}
		catch(IOException ioe){
			JOptionPane.showMessageDialog(gui, "Could not read from file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if(ProjectFileHandler.canOpenFile(fileExtension)){
			ProjectFileHandler.loadProjectFile(src, this);
			// Save config and wait for error message
			String s  = saveConfiguration();
						
			// If there is an error, create a message dialog
				if(s != null){
					JOptionPane.showMessageDialog(this.getGUI(), "Error saving configuration: " + s, "File error", JOptionPane.ERROR_MESSAGE);
				}
				else
						System.out.println("Wrote config");
		}
		// If it is not a readable data track, try to open as video
		else{
			setVideoTrack(src);
		}
			
	}
	
	/**
	 * Returns a linked list of currently loaded data tracks
	 * @return
	 */
	public LinkedList<Data> getLoadedDataTracks(){
		return loadedDataTracks;
	}
	
	public void reset(){
		projectfile = "";
		setVideoTrack(null);
		
		loadedDataTracks.clear();
	}
	
	/**
	 * Determins file types and adds data track
	 * @param src
	 * @param fileExtension
	 * @throws IOException
	 */
	public void addDataTrack(String src, String fileExtension) throws IOException{
	
		Data newData = null;

		// Load sdr file with SDRConverter
		if(fileExtension.equals("sdr")){
			SDRConverter sdrc = new SDRConverter();
			sdrc.setRelativeTimestamp(true);
			sdrc.setAggregate(SDRConverter.AGGREGATE_NONE);
			
			sdrc.setFile(new File(src));
			Instances ins = sdrc.getDataSet();
			
			long firstTimestamp = (long)ins.get(0).value(0);
			
			newData = new SensorData(this, ins.size(), src);	

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
			
			if(			arffin.getStructure().attribute(0).name().equals("timestamp") 
					&& 	arffin.getStructure().attribute(1).isNumeric()){
				
				Instances ins = arffin.getDataSet();
				
				long firstTimestamp = (long)ins.get(0).value(0);
				
				newData = new SensorData(this, ins.size(), src);	
				
				// Count numeric attributes
				int nNumeric = 0;
				
				for(int a = 1; a < ins.numAttributes(); a++){
					if(ins.attribute(a).isNumeric()){
						nNumeric++;
					}
				}
				System.out.println("nNumeric: " + nNumeric);
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
			else{
				return;
			}

		}
		
		// Add track to list and to layout
		
		if(newData != null){
			if(loadedDataTracks.size()%2 == 0)
				newData.getVisualization().setAlternativeColorScheme(true);
			else
				newData.getVisualization().setAlternativeColorScheme(false);
			
			// Add to list
			loadedDataTracks.add(newData);
			gui.updateDataFrame();
		}
		else{
			System.err.println("Error loading file");
		}
	}
	
	/**
	 * Adds an interval track for algorithm results
	 */
	public void addIntervalTrack(String src, String fileExtension) throws IOException{
		
		IntervalData newData = null;

		if(fileExtension.equals("arff")){
			weka.core.converters.ArffLoader arffin = new ArffLoader();
			arffin.setFile(new File(src));

			if(arffin.getStructure().attribute(0).name().equals("from") && arffin.getStructure().attribute(1).name().equals("to") && arffin.getStructure().attribute(2).name().equals("class")){
				String[] activities = new String[arffin.getStructure().attribute(2).numValues()];
				
				for(int i = 0; i < activities.length; i++){
					activities[i] = arffin.getStructure().attribute(2).value(i);
				}
				
				newData = new IntervalData(this, src, activities);
				
				Instances ins = arffin.getDataSet();

				long firstTimestamp = (long)ins.get(0).value(0);

				for(int i = 0; i < ins.size(); i++){
					((IntervalData)newData).addEvent((long)ins.get(i).value(0) - firstTimestamp, (long)ins.get(i).value(1) - firstTimestamp, (int)ins.get(i).value(2));
				}
			}
		}
		
		// Add track to list and to layout
		
				if(newData != null){
					if(loadedDataTracks.size()%2 == 0)
						newData.getVisualization().setAlternativeColorScheme(true);
					else
						newData.getVisualization().setAlternativeColorScheme(false);
					
					// Add to list
					loadedDataTracks.add(newData);
					((IntervalDataVisualization)newData.getVisualization()).toggleLocked();
					gui.updateDataFrame();
				}
				else{
					System.err.println("Error loading file");
				}
	}
	
	/**
	 * Adds an empty interval track for annotations
	 */
	public void addIntervalTrack(){
		Data newData = new IntervalData(this, "", new String[]{"Walking", "Running", "Biking", "Skating", "Crosstrainer"});
		loadedDataTracks.add(newData);
		
		if(loadedDataTracks.size()%2 == 0)
			newData.getVisualization().setAlternativeColorScheme(true);
		else
			newData.getVisualization().setAlternativeColorScheme(false);
		
		gui.updateDataFrame();
	}

	/**
	 * Sets the playback position relatively to the video, which means that 0f is the start of the video, 1f is its end.
	 * setVideoPosition can be false to avoid a loopback from VideoDataSynchronizer
	 * @param pos
	 */
	public void setPosition(float pos, boolean setVideoPosition) {
		for(int i = 0; i < loadedDataTracks.size(); i++){
			  loadedDataTracks.get(i).getVisualization().setPosition(pos * gui.getVideoLength());
		  }
		if(setVideoPosition)
			gui.setVideoPosition(pos);
	}
	
	/**
	 * Sets the playback position in ms, beginning with the start of the video
	 */
	public void setPosition(long time){
		for(int i = 0; i < loadedDataTracks.size(); i++){
			  loadedDataTracks.get(i).getVisualization().setPosition(time);
		  }
		
		gui.setVideoPosition(Math.max(0f, Math.min((float)time / (float)gui.getVideoLength(), 1f)));
	}
	
/*
 * File operations from config.cfg
 */
	/**
	 * Saves the programs configuration and returns possible errors as a string
	 * @return
	 */
	public String saveConfiguration(){
		File f = new File(CONFIG_PATH);
		
		try(FileWriter fw = new FileWriter(f)){
			if(!f.canWrite()){
				throw new IOException("File is read-only");
			}
			
			// Delete file and recreate
			if(f.exists())
				f.delete();
			f.createNewFile();		
			
			// Save vlc directory
			fw.write("# Path to VLC home directory\n");
			fw.write("\n" + VLCPATH_LINE + vlcdir + "\n\n");
			
			fw.write("# Path to current project file\n");
			fw.write("\n" + PROJECTPATH_LINE + projectfile + "\n\n");
			
			System.out.println(vlcdir + ", " + projectfile);
		}
		catch(IOException ioe){
			return ioe.getMessage();
		}
		
		return null;
	}
	
	/**
	 * Restores the last project
	 */
	public void loadConfiguration(){
		File f = new File(CONFIG_PATH);
		
		try(BufferedReader br = new BufferedReader(new FileReader(f))){
			String line = br.readLine();

			while(line != null){
				// Ignore VLC path, load project
				if (line.startsWith(PROJECTPATH_LINE)){
						projectfile = line.substring(PROJECTPATH_LINE.length());
						if(projectfile != ""){
							String result = ProjectFileHandler.loadProjectFile(projectfile, this);
							if(result != null){
								this.reset();
								JOptionPane.showMessageDialog(getGUI(), "Coud not restore last project.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
				}
				
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			System.err.println(ioe.getMessage());
		}
	}
	
	/**
	 * Loads VLCPath
	 */
	public void loadVLCPath(){
		File f = new File(CONFIG_PATH);
		
		try(BufferedReader br = new BufferedReader(new FileReader(f))){
			String line = br.readLine();

			// Check for VLC path only
			while(line != null){
				if	(line.startsWith(VLCPATH_LINE)){
						vlcdir = line.substring(VLCPATH_LINE.length());
				}
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			System.err.println(ioe.getMessage());
		}
	}
}
