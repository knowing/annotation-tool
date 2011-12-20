package evaluationtool;

import java.util.*;

import evaluationtool.gui.EvalGUI;
import evaluationtool.pointdata.SensorData;
import evaluationtool.pointdata.DataSet;




/**
 * This class holds the data that is currently worked on
 * @author anfi
 *
 */
public class DataModel {
	String video = "U:\\Video\\Top Gear\\Series 12\\Top Gear - S12E01.avi";
	LinkedList<Data> loadedDataTracks = new LinkedList<Data>();
	
	EvalGUI gui;
	
	public void setGUI(EvalGUI g){
		gui = g;
	}
	
	public String getVideoPath(){
		return video;
	}
	
	public void removeTrack(Data d){
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
		
		if(SensorData.canPlayFile(fileExtension))
			addDataTrack(src);
		// If it is not a readable data track, try to open as video
		else
			gui.loadFile();
			
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
	
	public void addDataTrack(String src){
		// Do the magic
		// While magic is unavailable, just create random tracks
		int length = 80000;
		SensorData s = new SensorData(this, length);
		
		// Generate random data
		for(int i = 0; i < length; i++){
			// Generate a value every 10 milliseconds
			s.setDataAt(i, new DataSet(i*10, new int[]{(int)(Math.random() * 255 - 128),(int)(Math.random() * 255 - 128),
												  (int)(Math.random() * 255 - 128)}));
		}
		
		if(loadedDataTracks.size()%2 == 0)
			s.getVisualization().setAlternativeColorScheme(true);
		else
			s.getVisualization().setAlternativeColorScheme(false);
		// Add to list
		loadedDataTracks.add(s);
	}
}
