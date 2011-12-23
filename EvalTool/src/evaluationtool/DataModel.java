package evaluationtool;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

import weka.core.Instances;

import de.sendsor.SDRConverter;

import evaluationtool.gui.EvalGUI;
import evaluationtool.pointdata.SensorData;
import evaluationtool.pointdata.DataSet;




/**
 * This class holds the data that is currently worked on
 * @author anfi
 *
 */
public class DataModel {
	String video = "";
	LinkedList<Data> loadedDataTracks = new LinkedList<Data>();
	
	EvalGUI gui;
	
	public void setGUI(EvalGUI g){
		gui = g;
	}
	
	public String getVideoPath(){
		return video;
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
		
		gui.updatePanelSouth();
	}
	
	/**
	 * Handles new files by deciding the type
	 * @param src
	 */
	public void loadFile(String src){
		StringTokenizer st = new StringTokenizer(src, ".");
		String fileExtension = "";
		
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
		// If it is not a readable data track, try to open as video
		else{
			video = src;
			gui.loadVideo(src);
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
		gui.loadVideo(src);
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
			
			newData = new SensorData(this, ins.size());	

			for(int i = 0; i < ins.size(); i++){
					((SensorData)newData).setDataAt(i, new DataSet((long)ins.get(i).value(0) - firstTimestamp, 
										 new int[]{(int)ins.get(i).value(1), 
												   (int)ins.get(i).value(2), 
												   (int)ins.get(i).value(3)}));
			}
		}
		
		System.out.println("Added track");
		
		if(newData != null){
			if(loadedDataTracks.size()%2 == 0)
				newData.getVisualization().setAlternativeColorScheme(true);
			else
				newData.getVisualization().setAlternativeColorScheme(false);
			
			// Add to list
			loadedDataTracks.add(newData);
			gui.updatePanelSouth();
		}
		else{
			System.err.println("Error loading file");
		}
	}
}
