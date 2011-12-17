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
			if(loadedDataTracks.size()%2 == 0)
				loadedDataTracks.get(i).getVisualization().setAlternativeColorScheme(true);
			else
				loadedDataTracks.get(i).getVisualization().setAlternativeColorScheme(false);
		}
		
		gui.updatePanelSouth();
	}
	
	public LinkedList<Data> getLoadedDataTracks(){
		return loadedDataTracks;
	}
	
	public void addDataTrack(String src){
		// Do the magic
		// While magic is unavailable, just create random tracks
		int length = 180000;
		SensorData s = new SensorData(this, length);
		
		// Generate random data
		for(int i = 0; i < length; i++){
			// Generate a value every 10 milliseconds
			s.setDataAt(i, new DataSet(i*10, new int[]{(int)(Math.random() * 255 - 128),
												  (int)(Math.random() * 255 - 128),
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
