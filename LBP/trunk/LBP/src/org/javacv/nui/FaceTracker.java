package org.javacv.nui;

//FaceTracker.java
//Andrew Davison, March 2011, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from a webcam in a picture panel (FacePanel). 
 A face is highlighted with a yellow rectangle, which is updated as the face
 moves. The highlighted part of the image can be saved by the user pressing
 the "Save Face" button.

 Usage:
 > java FaceTracker
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class FaceTracker extends JFrame {
	// GUI components
	private FacePanel facePanel;

	public FaceTracker() {
		super("Face Tracker");

		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		facePanel = new FacePanel(this); // the sequence of pictures appear here
		c.add(facePanel, BorderLayout.CENTER);

		// button for saving a highlighted face
		JButton saveBut = new JButton("Save Face");
		saveBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				facePanel.saveFace();
			}
		});

		JPanel p = new JPanel();
		p.add(saveBut);
		c.add(p, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				facePanel.closeDown(); // stop snapping pics
				System.exit(0);
			}
		});

		pack();
		setResizable(false);
		setVisible(true);
	} // end of FaceTracker()

	// -------------------------------------------------------

	public static void main(String args[]) {
		new FaceTracker();
	}

} // end of FaceTracker class
