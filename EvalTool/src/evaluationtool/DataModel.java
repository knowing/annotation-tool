package evaluationtool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

import weka.core.Instances;

import de.sendsor.SDRConverter;

import evaluationtool.gui.EvalGUI;
import evaluationtool.intervaldata.IntervalData;
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
	
	public String getVLCPath(){
		return vlcdir;
	}
	
	public long getProjectLength(){
		long tempLength = gui.getVideoLength();
		
		for(int i = 0; i < loadedDataTracks.size(); i++){
			tempLength = Math.max(tempLength, loadedDataTracks.get(i).getLength());
		}
		
		return tempLength;
	}
	
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
	 * Handles new files by deciding the type
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
		
		if(SensorData.canOpenFile(fileExtension))
			try{
			addDataTrack(src, fileExtension);
			}
		catch(IOException ioe){
			JOptionPane.showMessageDialog(gui, "Could not read from file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else if(ProjectFileHandler.canOpenFile(fileExtension)){
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
	
	public String getProjectPath(){
		return projectfile;
	}
	
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
	
	public void reset(){
		projectfile = "";
		setVideoTrack(null);
		
		loadedDataTracks.clear();
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
	 * Adds an empty interval track for annotations
	 */
	public void addIntervalTrack(){
		// Add empty interval track for annotations
		loadedDataTracks.add(new IntervalData(this, ""));
		gui.updateDataFrame();
	}
}
