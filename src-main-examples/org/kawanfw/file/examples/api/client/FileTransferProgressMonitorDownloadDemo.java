/*
 * This file is part of Awake FILE. 
 * Awake file: Easy file upload & download over HTTP with Java.                                    
 * Copyright (C) 2015,  KawanSoft SAS
 * (http://www.kawansoft.com). All rights reserved.                                
 *                                                                               
 * Awake FILE is free software; you can redistribute it and/or                 
 * modify it under the terms of the GNU Lesser General Public                    
 * License as published by the Free Software Foundation; either                  
 * version 2.1 of the License, or (at your option) any later version.            
 *                                                                               
 * Awake FILE is distributed in the hope that it will be useful,               
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU             
 * Lesser General Public License for more details.                               
 *                                                                               
 * You should have received a copy of the GNU Lesser General Public              
 * License along with this library; if not, write to the Free Software           
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
 * 02110-1301  USA
 *
 * Any modifications to this file must keep this entire header
 * intact.
 */
/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.kawanfw.file.examples.api.client;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.kawanfw.file.api.client.RemoteInputStream;
import org.kawanfw.file.api.client.RemoteSession;

/**
 * 
 * This class is a demo for dowloading a file with Awake FILE, using a
 * <code>ProgressMonitor</code>.&nbsp;
 * <p>
 * It works exactly like the <a href=
 * "http://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#ProgressMonitorDemo"
 * >ProgressMonitorDemo</a> class of the Java Tutorial in the chapter <a href=
 * "http://docs.oracle.com/javase/tutorial/uiswing/components/progress.html"
 * >How to Use Progress Bars</a>.
 * <p>
 * To run it, just modify the settings between the two lines in
 * doFileDownload(): <br>
 * // BEGIN MODIFY WITH YOUR VALUES <br>
 * // END MODIFY WITH YOUR VALUES
 * 
 * @author Nicolas de Pomereu
 * @since 1.0
 * 
 */

public class FileTransferProgressMonitorDownloadDemo extends JPanel implements
	ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = -3482760023137893766L;

    private ProgressMonitor progressMonitor;
    private JButton startButton;
    private JTextArea taskOutput;
    private Task task;

    /**
     * Progress between 0 and 100. Updated by doFileDownload() at each 1% input
     * stream read
     */
    private int progress = 0;

    /** Says to doFileDownload() code if transfer is cancelled by user */
    private boolean cancelled = false;

    class Task extends SwingWorker<Void, Void> {

	@Override
	public Void doInBackground() {
	    // Reset values at each download
	    cancelled = false;
	    progress = 0;
	    setProgress(0);

	    // progress is ++ at each
	    // 1% file transfer in doFileDownload()
	    while (progress < 100) {
		try {
		    Thread.sleep(50);
		} catch (InterruptedException ignore) {
		}

		if (isCancelled()) {
		    // Say to doFileDownload() that
		    // user has cancelled the download
		    cancelled = true;
		    break;
		}

		setProgress(Math.min(progress, 100));
	    }

	    return null;
	}

	@Override
	public void done() {
	    Toolkit.getDefaultToolkit().beep();
	    startButton.setEnabled(true);
	    progressMonitor.setProgress(0);
	    progressMonitor.close();
	}
    }

    public FileTransferProgressMonitorDownloadDemo() {
	super(new BorderLayout());

	// Create the demo's UI.
	startButton = new JButton("Start");
	startButton.setActionCommand("start");
	startButton.addActionListener(this);

	taskOutput = new JTextArea(5, 20);
	taskOutput.setMargin(new Insets(5, 5, 5, 5));
	taskOutput.setEditable(false);

	add(startButton, BorderLayout.PAGE_START);
	add(new JScrollPane(taskOutput), BorderLayout.CENTER);
	setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
	progressMonitor = new ProgressMonitor(
		FileTransferProgressMonitorDownloadDemo.this,
		"Downloads a file...", "", 0, 100);
	progressMonitor.setProgress(0);

	Thread t = new Thread() {
	    public void run() {
		doFileDownload();
	    }
	};

	t.start();

	task = new Task();
	task.addPropertyChangeListener(this);
	task.execute();
	startButton.setEnabled(false);

    }

    /**
     * Do the file download
     */
    private void doFileDownload() {
	try {

	    // BEGIN MODIFY WITH YOUR VALUES
	    String userHome = System.getProperty("user.home");

	    String url = "http://localhost:8080/awake-file/ServerFileManager";
	    String username = "username";
	    char[] password = "password".toCharArray();

	    File file = new File(userHome + File.separator + "image_1_1.jpg");
	    String pathname = "/image_1_1.jpg";
	    // END MODIFY WITH YOUR VALUES

	    RemoteSession remoteSession = new RemoteSession(url, username,
		    password);

	    InputStream in = null;
	    OutputStream out = null;

	    try {
		in = new RemoteInputStream(remoteSession, pathname);
		out = new BufferedOutputStream(new FileOutputStream(file));

		// Get the remote input stream length
		long fileLength = ((RemoteInputStream) in).length();

		int tempLen = 0;
		byte[] buffer = new byte[1024 * 4];
		int n = 0;

		while ((n = in.read(buffer)) != -1) {
		    tempLen += n;

		    // Test if user has cancelled the download
		    if (cancelled)
			throw new InterruptedException(
				"Download cancelled by User!");

		    // Add 1 to progress for each 1% download
		    if (tempLen > fileLength / 100) {
			tempLen = 0;
			progress++;
		    }

		    out.write(buffer, 0, n);
		}

	    } finally {
		// When finished, set to the maximum value to stop the
		// ProgressMonitor
		progress = 100;
		if (in != null)
		    in.close();
		if (out != null)
		    out.close();
	    }

	    remoteSession.logoff();

	    System.out.println("File download done.");
	} catch (Exception e) {

	    if (e instanceof InterruptedException) {
		System.out.println(e.getMessage());
		return;
	    }

	    System.err.println("Exception thrown during Download:");
	    e.printStackTrace();
	}
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
	if ("progress" == evt.getPropertyName()) {
	    int progress = (Integer) evt.getNewValue();
	    progressMonitor.setProgress(progress);
	    String message = String.format("Completed %d%%.\n", progress);
	    progressMonitor.setNote(message);
	    taskOutput.append(message);
	    if (progressMonitor.isCanceled() || task.isDone()) {
		Toolkit.getDefaultToolkit().beep();
		if (progressMonitor.isCanceled()) {
		    task.cancel(true);
		    taskOutput.append("Task canceled.\n");
		} else {
		    taskOutput.append("Task completed.\n");
		}
		startButton.setEnabled(true);
	    }
	}
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
	// Create and set up the window.
	JFrame frame = new JFrame("FileTransferProgressMonitorDemo");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	// Create and set up the content pane.
	JComponent newContentPane = new FileTransferProgressMonitorDownloadDemo();
	newContentPane.setOpaque(true); // content panes must be opaque
	frame.setContentPane(newContentPane);

	// Display the window.
	frame.pack();
	frame.setVisible(true);
    }

    public static void main(String[] args) {
	// Schedule a job for the event-dispatching thread:
	// creating and showing this application's GUI.
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	});
    }
}
