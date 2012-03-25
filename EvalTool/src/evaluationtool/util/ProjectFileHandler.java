package evaluationtool.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.media.sound.Toolkit;

import evaluationtool.DataModel;
import evaluationtool.gui.EvalGUI;

public class ProjectFileHandler {
	
	static final String USER_GENERATED_TRACK_PREFIX = "track_";
	
	public static String loadProjectFile(String path, DataModel model){
		
		// Extract archive into a temporary folder
		File temp = new File("\\temp");
		
		
		 deleteTemporaryFiles(temp);
		 
		// Create temp directory if it does not exist
		temp.mkdir();
		
		BufferedOutputStream dest = null;
		
		      try(FileInputStream fis = new FileInputStream(path); ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));){         
			         
		         ZipEntry entry;
		         
		         while((entry = zis.getNextEntry()) != null) {

		            int count;
		            byte data[] = new byte[1024];
		            // Write file in temp directory
		            FileOutputStream fos = new FileOutputStream("\\temp\\" + entry.getName());
		            
		            dest = new BufferedOutputStream(fos, 1024);
		            
		            while ((count = zis.read(data, 0, 1024)) != -1) {
		               dest.write(data, 0, count);
		            }
		            
		            dest.flush();
		            dest.close();
		         }
		      }
		        catch(IOException ioe){
		        	 return ioe.getMessage();
		         }
		      
		 File f = new File("\\temp\\project.cfg");
		 
		 if(!f.exists()){
			 return "Not a project file. No project.cfg";
		 }
		 
		 // Reset data model
		 model.reset();
		 model.setProjectPath(path);
		 
		 String videoPath = "";
		 
		 // Read project.cfg
		 try(BufferedReader br = new BufferedReader(new FileReader(f))){
				String line = br.readLine();

				while(line != null){
					if	(line.startsWith(model.VIDEOPATH_LINE)){
						// Absolute path
						if(line.contains("\\"))
							videoPath = line.substring(model.VIDEOPATH_LINE.length());
						// Relative path
						else
							videoPath = "\\temp\\" + line.substring(model.VIDEOPATH_LINE.length());
					}
					else if (line.startsWith(model.DATAPATH_LINE)){
						FileImporter.loadFile(model, "\\temp\\" + line.substring(model.DATAPATH_LINE.length()));
					}
					else if (line.startsWith(model.OFFSET_LINE)){
						model.getLoadedDataTracks().getLast().setOffset(Long.parseLong(line.substring(model.OFFSET_LINE.length())));
					}
					else if (line.startsWith(model.SPEED_LINE)){
						model.getLoadedDataTracks().getLast().setPlaybackSpeed(Float.parseFloat(line.substring(model.SPEED_LINE.length())));
					}
					
					line = br.readLine();
				}	
				
				// Convert path to system standard
				File videoFile = new File(videoPath);
				model.setVideoPath(videoFile.getAbsolutePath());
			}
			catch(IOException ioe){
				System.err.println("Project file error: " + ioe.getMessage());
			}

		return null;
	}

	private static void deleteTemporaryFiles(File temp) {
		if(temp.exists()){
			// Empty directory
			String[] filesInTemp = temp.list();
			for(int i = 0; i < filesInTemp.length; i++){
				File t = new File(filesInTemp[i]).getAbsoluteFile();
				t.delete();
			}
		 temp.delete();
		}
	}

	/**
	 * Saves a project file and returns an IOError message or "SHOW_AGAIN" in case the user does not wish to overwrite the selected file
	 * @param path
	 * @return
	 */
	public static String saveProjectFile(DataModel model, String path, boolean includeVideo){
		
		// Create project file first
		File f = new File(path);
		
		// Delete file and recreate
					if(f.exists()){
						int overwriteAnswer = JOptionPane.showOptionDialog(model.getGUI().getActiveFrame(), "Overwrite " + path + "?", "File already exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
						if(overwriteAnswer == JOptionPane.OK_OPTION){
							f.delete();
						}
						else{
							return "SHOW_AGAIN";
						}
					}
		
		f = new File("project.cfg");
		
		try(FileWriter fw = new FileWriter(f)){
			if(!f.canWrite()){
				throw new IOException("File is read-only");
			}
			
			// Delete file and recreate, this is a temporary file anyway
			if(f.exists())
				f.delete();
			f.createNewFile();		
			
			
			// Save video path
			fw.write("# Video path");
			if(includeVideo){
				// Save filename only
				fw.write("\n" + model.VIDEOPATH_LINE + getFilenameFromPath(model.getVideoPath()) + "\n\n");
			}
			else{
				// Save file path
				fw.write("\n" + model.VIDEOPATH_LINE + model.getVideoPath() + "\n\n");
			}
			
			File savepath = new File("\\temp\\save\\");
			savepath.mkdirs();
			
			// Save data path
			fw.write("# Data tracks");
			for(int i = 0; i < model.getLoadedDataTracks().size(); i++){

				if(model.getLoadedDataTracks().get(i).getSource().equals("")){
					int tempNumber = 1;
					// Find a name
					while(nameExists(model, USER_GENERATED_TRACK_PREFIX + tempNumber + ".arff")){
						tempNumber++;
					}
					model.getLoadedDataTracks().get(i).setSource(savepath.getAbsolutePath() + "\\" + USER_GENERATED_TRACK_PREFIX + tempNumber + ".arff");
				}	
				
				File test = new File(model.getLoadedDataTracks().get(i).getSource());
				
				// Check if file exists, otherwise save it
				if(!test.exists()){
					// Save track. If the track cannot be saved, continue with next track
					if(!model.saveTrack(i, model.getLoadedDataTracks().get(i).getSource())){
						continue;
					}
				}
				
				if(getFilenameFromPath(model.getLoadedDataTracks().get(i).getSource()) != null){
					fw.write("\n" + model.DATAPATH_LINE + getFilenameFromPath(model.getLoadedDataTracks().get(i).getSource()));
					fw.write("\n" + model.OFFSET_LINE + (long)model.getLoadedDataTracks().get(i).getOffset());
					fw.write("\n" + model.SPEED_LINE + model.getLoadedDataTracks().get(i).getPlaybackSpeed() + "\n");
				}
				else
					fw.write("\n" + "Could not save the following file: " + model.getLoadedDataTracks().get(i).getSource());
				
				}
			
		}
		catch(IOException ioe){
			return "Error writing config.cfg: " + ioe.getMessage();
		}
		
		f = new File(path);
		
		// Now create zip file containing project file
		try(FileOutputStream fileout = new FileOutputStream(path); ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(fileout));){
			if(!f.canWrite()){
				throw new IOException("File is read-only");
			}

		FileInputStream fi;
		ZipEntry entry;
			
		// Add video file
			if(includeVideo){
				fi = new FileInputStream(model.getVideoPath());
				entry = new ZipEntry(getFilenameFromPath(model.getVideoPath()));
				zipout.putNextEntry(entry);
				
				 int count;
		         byte data[] = new byte[1024];
				
		         while((count = fi.read(data, 0, 1024)) != -1) {
				   zipout.write(data, 0, count);
		         }
			}
			
		File temp = new File("\\temp");

		// Add data tracks
		for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
			
			fi = new FileInputStream(model.getLoadedDataTracks().get(i).getSource());
			entry = new ZipEntry(getFilenameFromPath(model.getLoadedDataTracks().get(i).getSource()));
			
			zipout.putNextEntry(entry);
			
			 int count;
	         byte data[] = new byte[1024];
			
	         while((count = fi.read(data, 0, 1024)) != -1) {
			   zipout.write(data, 0, count);
	         }
		}
		
		// Add project.cfg
		fi = new FileInputStream("project.cfg");
		entry = new ZipEntry("project.cfg");
		zipout.putNextEntry(entry);
		
		 int count;
         byte data[] = new byte[1024];
		
         while((count = fi.read(data, 0, 1024)) != -1) {
		   zipout.write(data, 0, count);
         }
			
         deleteTemporaryFiles(temp);
         
		} catch (FileNotFoundException e) {
			return "ProjectFileHandler - File not found" + e.getMessage();
		} catch (IOException e) {
			return "ProjectFileHandler - IOException - " + e.getMessage();
		}
		
		return null;
	}
	
	/**
	 * Checks whether a filename already exists within a project
	 * @param model
	 * @param name
	 * @return
	 */
	private static boolean nameExists(DataModel model, String name){
		for(int i = 0; i < model.getLoadedDataTracks().size(); i++){
			if(getFilenameFromPath(model.getLoadedDataTracks().get(i).getSource()).equals(getFilenameFromPath(name)))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the filename, which is always assumed to be the substring after the last "/" in a path
	 * @param path
	 * @return
	 */
	public static String getFilenameFromPath(String path){
		
		if(path == null || path.equals("")){
			return "";
		}
		else if(!path.contains("\\")){
			return path;
		}
		
		String filename = null;
		
		// filename is the last part of a path
		StringTokenizer st = new StringTokenizer(path, "\\");
		while(st.hasMoreTokens()){
			filename = st.nextToken();
		}
		
		return filename;
	}
	
	public static void saveCurrentProject(EvalGUI gui){
		 int videoAnswer = JOptionPane.showOptionDialog(gui.getActiveFrame(), "Do you want to include the video file in the archive?", "Video file", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		 
		 if(videoAnswer == JOptionPane.YES_OPTION){
			  String result = saveProjectFile(gui.getModel(), gui.getModel().getProjectPath(), true);
			  if(result != null){
				  JOptionPane.showMessageDialog(gui.getActiveFrame(), "Error saving file: " + result, "Error" , JOptionPane.ERROR_MESSAGE);
			  }
			  else{
				  JOptionPane.showMessageDialog(gui.getActiveFrame(), "File has been saved succesfully", "File saved" , JOptionPane.INFORMATION_MESSAGE);
			  }
		  }
		  else if (videoAnswer == JOptionPane.NO_OPTION){
			  String result = saveProjectFile(gui.getModel(), gui.getModel().getProjectPath(), false);
			  if(result != null){
				  JOptionPane.showMessageDialog(gui.getActiveFrame(), "Error saving file: " + result, "Error" , JOptionPane.ERROR_MESSAGE);
			  }
			  else{
				  JOptionPane.showMessageDialog(gui.getActiveFrame(), "File has been saved succesfully", "File saved" , JOptionPane.INFORMATION_MESSAGE);
			  }
		  }
	}
	
	public static String showSaveDialog(EvalGUI gui){
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setFileFilter(new FileNameExtensionFilter("ZIP file", "zip"));

		 String path = "";
		
		 // If ok has been clicked, save project
		  if(chooser.showOpenDialog(gui.getActiveFrame()) == JFileChooser.APPROVE_OPTION){	 
			  path = chooser.getSelectedFile().getAbsolutePath();

			  if(!path.substring(path.length() - 4, path.length()).equals(".zip")){
				  path = path + ".zip";
			  }
			 
			  int videoAnswer = JOptionPane.showOptionDialog(gui.getActiveFrame(), "Do you want to include the video file in the archive?", "Video file", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				
			  if(videoAnswer == JOptionPane.YES_OPTION){
				  String result = saveProjectFile(gui.getModel(), path, true);
				  if(result != null){
					  JOptionPane.showMessageDialog(gui.getActiveFrame(), "Error saving file: " + result, "Error" , JOptionPane.ERROR_MESSAGE);
				  }
				  else{
					  JOptionPane.showMessageDialog(gui.getActiveFrame(), "File has been saved succesfully", "File saved" , JOptionPane.INFORMATION_MESSAGE);
				  }
			  }
			  else if (videoAnswer == JOptionPane.NO_OPTION){
				  String result = saveProjectFile(gui.getModel(), path, false);
				  if(result != null){
					  JOptionPane.showMessageDialog(gui.getActiveFrame(), "Error saving file: " + result, "Error" , JOptionPane.ERROR_MESSAGE);
				  }
				  else{
					  JOptionPane.showMessageDialog(gui.getActiveFrame(), "File has been saved succesfully", "File saved" , JOptionPane.INFORMATION_MESSAGE);
				  }
			  }
		  }
		  
		  return path;
	}
	
	public static void showOpenDialog(DataModel model){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileFilter(new FileNameExtensionFilter("ZIP file", "zip"));
		
		if(chooser.showOpenDialog(model.getGUI().getActiveFrame()) == JFileChooser.APPROVE_OPTION){	 
			  String path = chooser.getSelectedFile().getAbsolutePath();
			  String result = loadProjectFile(path, model);
		}
	}
	
	public static boolean canOpenFile(String extension){
		if(extension.equals("zip"))
				return true;
			else 
				return false;
		}
}
