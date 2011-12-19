package evaluationtool.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import evaluationtool.Data;

/**
 * This allows a user to enter offset and speed multiplicator for a given track
 * @author anfi
 *
 */
public class TrackOptionsDialog extends JDialog implements ActionListener, KeyListener {
	
	JLabel offsetLabel, speedLabel;
	JTextField offsetField, speedField;
	
	JButton okay;
	
	Data source;

	public TrackOptionsDialog(JFrame parent, Data source){
		super(parent);
		
		this.source = source;
		
		// Init JDialog
		this.setTitle("Options");
		this.setSize(300, 100);
		this.setLocation(100, 100);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		
		offsetLabel = new JLabel("Offset in ms: ");
		speedLabel = new JLabel("Speed as scale factor: ");
		
		okay = new JButton("Ok");
		okay.addActionListener(this);
		
		// Init values
		offsetField = new JTextField();
		offsetField.setEditable(true);
		speedField = new JTextField();
		speedField.setEditable(true);
		refresh();
		
		// Set Layout
		this.setLayout(new GridLayout(3, 2));
		this.add(offsetLabel);
		this.add(offsetField);
		this.add(speedLabel);
		this.add(speedField);
		this.add(new JLabel());
		this.add(okay);
		
		offsetField.addKeyListener(this);
		speedField.addKeyListener(this);
	}
	
	public void refresh(){
		offsetField.setText(source.getOffset() + "");
		speedField.setText(source.getPlaybackSpeed() + "");
	}

	public void actionPerformed(ActionEvent ae) {
		
		float newSpeed = source.getPlaybackSpeed();
		long newOffset = source.getOffset();
		
		boolean success = true;
		
		try{
		newSpeed = Float.parseFloat(speedField.getText());
		source.setPlaybackSpeed(newSpeed);
		speedField.setForeground(Color.BLACK);
		}
		catch(NumberFormatException nfe){
			speedField.setForeground(Color.RED);
			success = false;
		}
		
		try{
			newOffset = Long.parseLong(offsetField.getText());
			source.setOffset(newOffset);
			offsetField.setForeground(Color.BLACK);
			}
			catch(NumberFormatException nfe){
				offsetField.setForeground(Color.RED);
				success = false;
			}
		
		if(success){
			this.setVisible(false);
		}
	}


	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == KeyEvent.VK_ENTER){
			System.out.println("Test");
			okay.doClick();
		}
	}
	
	
}
