package evaluationtool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

import com.sun.jna.NativeLibrary;


import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;

import evaluationtool.gui.EvalGUI;
import evaluationtool.intervaldata.Activity;
import evaluationtool.intervaldata.IntervalData;
import evaluationtool.intervaldata.IntervalDataVisualization;
import evaluationtool.pointdata.PointData;
import evaluationtool.pointdata.Timestamp;
import evaluationtool.sensordata.DataSet;
import evaluationtool.sensordata.SensorData;
import evaluationtool.util.ProjectFileHandler;

/**
 * This class holds the data that is currently worked on
 * @author anfi
 *
 */
public class DataModel {
	String projectfile = "";
	String vlcdir = "";
	String videofile = "";
	
	String PREDEFINED_ACTIVITIES[];
	
	public final String CONFIG_PATH = "config.cfg";
	public final String VLCPATH_LINE = "$VLCDIR=";
	public final String PROJECTPATH_LINE = "$PROJECTFILE=";
	
	public final String VIDEOPATH_LINE = "$VIDEOFILE=";
	public final String DATAPATH_LINE = "$DATAFILE=";
	public final String OFFSET_LINE = "$DATAOFFSET=";
	public final String SPEED_LINE = "$DATAZOOM=";
	
	public final String ACTIVITY_TYPE_LINE = "$ACTIVITYTYPE=";
	
	LinkedList<Data> loadedDataTracks = new LinkedList<Data>();
	
	EvalGUI gui;

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
	 * Sets the playback position relatively to the video, which means that 0f is the start of the video, 1f is its end.
	 * setVideoPosition can be false to avoid a loopback from VideoDataSynchronizer
	 * @param pos
	 */
	public void setPlaybackPosition(float pos, boolean setVideoPosition) {
		for(int i = 0; i < loadedDataTracks.size(); i++){
			  loadedDataTracks.get(i).getVisualization().setPosition((long)(pos * gui.getVideoLength()));
		  }
		if(setVideoPosition)
			gui.setVideoPosition(pos);
	}
	
	/**
	 * Sets the playback position in ms, beginning with the start of the video
	 */
	public void setPlaybackPosition(long time){
		for(int i = 0; i < loadedDataTracks.size(); i++){
			  loadedDataTracks.get(i).getVisualization().setPosition(time);
		  }
		
		gui.setVideoPosition(Math.max(0f, Math.min((float)time / (float)gui.getVideoLength(), 1f)));
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
		
		gui.updateFrames();
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
		setVideoPath("");
		
		loadedDataTracks.clear();
		
		this.getGUI().loadVideo("");
		this.getGUI().updateFrames();
	}
	
	/**
	 * Adds an empty interval track for annotations
	 */
	public void addIntervalTrack(){
		String name = JOptionPane.showInputDialog(this.getGUI(), "Enter name for new track");
		if(name == null || name.trim().equals(""))
			return;
		
		Data newData = new IntervalData(this, name  + ".arff", PREDEFINED_ACTIVITIES);
		loadedDataTracks.add(newData);
		
		if(loadedDataTracks.size()%2 == 0)
			newData.getVisualization().setAlternativeColorScheme(true);
		else
			newData.getVisualization().setAlternativeColorScheme(false);
		
		gui.updateFrames();
	}
	
	public void addPointsTrack() {
		String name = JOptionPane.showInputDialog(this.getGUI(), "Enter name for new track");
		if(name == null || name.trim().equals(""))
			return;
		
		Data newData = new PointData(this, name + ".arff");
		loadedDataTracks.add(newData);
		
		if(loadedDataTracks.size()%2 == 0)
			newData.getVisualization().setAlternativeColorScheme(true);
		else
			newData.getVisualization().setAlternativeColorScheme(false);
		
		gui.updateFrames();
	}
	
	/**
	 * Saves a track to the specified path
	 * @param i
	 * @param string
	 */
	public boolean saveTrack(int n_track, String filename) {
		// Only interval data can be saved
		if(this.getLoadedDataTracks().get(n_track) instanceof IntervalData){
			IntervalData track = (IntervalData) this.getLoadedDataTracks().get(n_track);
			track.setSource(filename);
			
			weka.core.converters.ArffSaver arffout = new weka.core.converters.ArffSaver();
			try {
				arffout.setFile(new File(filename));
				LinkedList<String> atts = new LinkedList<String>();
				// Create attributes list
				for(int i = 0; i < track.getPossibleActivities().length; i++){
					atts.add(track.getPossibleActivities()[i]);
				}
				
				// Create structure for arff file
				arffout.setStructure(ResultsUtil.timeIntervalResult(atts));
				
				Instances ins = arffout.getInstances();
				
				track.orderEvents();
				LinkedList<Activity> events = track.getEvents();
				
				for(int i = 0; i < track.getNEvents(); i++){
					DenseInstance instance = new DenseInstance(3);
					instance.setValue(ins.attribute(0), events.get(i).timestampStart);
					instance.setValue(ins.attribute(1), events.get(i).timestampEnd);
					instance.setValue(ins.attribute(2), events.get(i).activitytype);
					
					ins.add(instance);
				}
				
				arffout.writeBatch();
				
			} catch (IOException e) {
				System.err.println("Error saving user generated track: " + e + "\nSkipping track.");
				return false;
			}

			return true;
		}
		else if(this.getLoadedDataTracks().get(n_track) instanceof PointData){
			PointData track = (PointData) this.getLoadedDataTracks().get(n_track);
			track.setSource(filename);
			
			weka.core.converters.ArffSaver arffout = new weka.core.converters.ArffSaver();
			try {
				arffout.setFile(new File(filename));
				LinkedList<String> atts = new LinkedList<String>();
				
				// Create structure for arff file
				arffout.setStructure(ResultsUtil.timeSeriesResult(atts));
				
				Instances ins = arffout.getInstances();
				
				int nPoints = track.getNPoints();
				
				for(int i = 0; i < nPoints; i++){
					DenseInstance instance = new DenseInstance(1);
					instance.setValue(ins.attribute(0), track.removeSettingsFromTimestamp(track.getPoint(i)));
					
					ins.add(instance);
				}
				
				arffout.writeBatch();
				
			} catch (IOException e) {
				System.err.println("Error saving user generated track: " + e + "\nSkipping track.");
				return false;
			}

			return true;
		}
		else{
			System.err.println("Error saving user generated track: Wrong type. \nSkipping track.");
			return false;
		}
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
		
		if(PREDEFINED_ACTIVITIES == null){
			System.err.println("No activities in config.cfg. Please make sure there is a valid config.cfg in the programs folder.");
		}
		
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
			
			fw.write("# Standard activities\n");
			for(int i = 0; i < PREDEFINED_ACTIVITIES.length; i++){
				fw.write("\n" + ACTIVITY_TYPE_LINE + PREDEFINED_ACTIVITIES[i] + "\n");
			}
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
		
		LinkedList<String> temp = new LinkedList<String>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(f))){
			String line = br.readLine();

			while(line != null){
				if (line.startsWith(PROJECTPATH_LINE)){
						projectfile = line.substring(PROJECTPATH_LINE.length());
						if(projectfile != ""){
							String result = ProjectFileHandler.loadProjectFile(projectfile, this);
							if(result != null){
								this.reset();
								JOptionPane.showMessageDialog(gui.getActiveFrame(), "Coud not restore last project.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
				}
				// Add all activity lines to linked list
				if (line.startsWith(ACTIVITY_TYPE_LINE)){
						temp.add(line.substring(ACTIVITY_TYPE_LINE.length()));			
				}
				
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			System.err.println(ioe.getMessage());
		}
			
		PREDEFINED_ACTIVITIES = temp.toArray(new String[temp.size()]);
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
						setVLCPath(line.substring(VLCPATH_LINE.length()));
				}
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			System.err.println(ioe.getMessage());
		}
	}
	
	/*
	 * Setter and getters
	 */
	
	public void setGUI(EvalGUI g){
		gui = g;
	}
	
	public EvalGUI getGUI(){
		return gui;
	}
	
	public String getVideoPath(){
		return videofile;
	}
	
	public void setVideoPath(String src){
		videofile = src;
		gui.loadVideo(src);
	}
	
	
	public String getVLCPath(){
		return vlcdir;
	}
	
	public boolean setVLCPath(String path){
		if(path == null || path.equals("")){
			return false;
		}
		
		vlcdir = path;
		System.out.println(getVLCPath());
		
		NativeLibrary.addSearchPath("libvlc",  path);

		boolean pathOkay = gui.buildVideoFrame();
		
		return pathOkay;
	}
	
	public String getProjectPath(){
		return projectfile;
	}
	
	public void setProjectPath(String path){
		projectfile = path;
		gui.getActiveFrame().setTitle("EvalTool - " + path );
	}
	
	public boolean isVideoPlaying(){
		return gui.isVideoPlaying();
	}
}
