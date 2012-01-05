package evaluationtool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;


import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import de.lmu.ifi.dbs.knowing.core.util.ResultsUtil;
import de.sendsor.SDRConverter;

import evaluationtool.gui.EvalGUI;
import evaluationtool.intervaldata.IntervalData;
import evaluationtool.intervaldata.IntervalDataVisualization;
import evaluationtool.pointdata.SensorData;
import evaluationtool.pointdata.DataSet;
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
	
	public final String CONFIG_PATH = "config.cfg";
	public final String VLCPATH_LINE = "$VLCDIR=";
	public final String PROJECTPATH_LINE = "$PROJECTFILE=";
	
	public final String VIDEOPATH_LINE = "$VIDEOFILE=";
	public final String DATAPATH_LINE = "$DATAFILE=";
	public final String OFFSET_LINE = "$DATAOFFSET=";
	public final String SPEED_LINE = "$DATAZOOM=";
	
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
			  loadedDataTracks.get(i).getVisualization().setPosition(pos * gui.getVideoLength());
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
		setVideoPath(null);
		
		loadedDataTracks.clear();
	}
	
	/**
	 * Adds an empty interval track for annotations
	 */
	public void addIntervalTrack(){
		Data newData = new IntervalData(this, "", IntervalData.PREDEFINED_ACTIVITIES);
		loadedDataTracks.add(newData);
		
		if(loadedDataTracks.size()%2 == 0)
			newData.getVisualization().setAlternativeColorScheme(true);
		else
			newData.getVisualization().setAlternativeColorScheme(false);
		
		gui.updateFrames();
	}
	
	/**
	 * Saves an interval track to the specified path
	 * @param i
	 * @param string
	 */
	public boolean saveTrack(int track, String filename) {
		// Only interval data can be saved
		if(this.getLoadedDataTracks().get(track) instanceof IntervalData){
			this.getLoadedDataTracks().get(track).setSource(filename);
			
			weka.core.converters.ArffSaver arffout = new weka.core.converters.ArffSaver();
			try {
				arffout.setFile(new File(filename));
				LinkedList<String> atts = new LinkedList<String>();
				// Create attributes list
				for(int i = 0; i < IntervalData.PREDEFINED_ACTIVITIES.length; i++){
					atts.add(IntervalData.PREDEFINED_ACTIVITIES[i]);
				}
				
				// Create structure for arff file
				arffout.setStructure(ResultsUtil.timeIntervalResult(atts));
				
				Instances ins = arffout.getInstances();
				evaluationtool.intervaldata.DataSet[] events = ((IntervalData)this.getLoadedDataTracks().get(track)).getEvents();
				
				for(int i = 0; i < events.length; i++){
					
					DenseInstance instance = new DenseInstance(3);
					instance.setValue(ins.attribute(0), events[i].timestampStart);
					instance.setValue(ins.attribute(1), events[i].timestampEnd);
					instance.setValue(ins.attribute(2), events[i].activitytype);
					
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
								JOptionPane.showMessageDialog(gui.getActiveFrame(), "Coud not restore last project.", "Error", JOptionPane.ERROR_MESSAGE);
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
	
	public void setVLCPath(String path){
		vlcdir = path;
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
