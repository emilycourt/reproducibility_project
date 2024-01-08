/*
 * Copyright (c) 2002, 2004, Regents of the University of California 
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:

 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 * 		and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 * 		and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the University of California at Berkeley nor the names of its contributors 
 * 		may be used to endorse or promote products derived from this software without specific prior written 
 * 		permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.berkeley.cs.db.yfilter.icdedemo;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

public class Framework extends JFrame {
  JTextArea textarea;
  ICDEDemo demo;
  FrameworkMenuBar menubar;
  JButton run;
  JButton pause;
  JButton cycle;
  JButton step;
  //JButton match;
  JButton query;
  JButton doc;
  //  Canvas canvas;
  JScrollPane scrollPanel;
  //JButton s;
  //JPanel pane;

  XMLViewer xmlviewer;
  QueryViewer queryviewer;
  SystemMonitor sm;
  PerformanceMonitor pm;
  WorkloadMonitor wm;

  /**
   *  View all queries in the system.
   */
  JButton allqueries;
  XMLViewer allqueryviewer;
  
  
  public Framework() {
    super("YFilter Demo");

    // set up text area
    textarea = new JTextArea("YFilter Demo\n", 30, 25);
    textarea.setLineWrap(true);
    textarea.setMargin(new Insets(5,5,5,5));
    textarea.setEditable(false);
    textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollPanel = new JScrollPane(textarea);
   
    // set up menu bar
    menubar = new FrameworkMenuBar(this);
    setJMenuBar(menubar);    

    // set up buttons
    run = new JButton("Run");
    run.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	runDemo();
      }
    });
    run.setEnabled(false);

    pause = new JButton("Pause");
    pause.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	pauseDemo();
      }
    });
    pause.setEnabled(false);

    cycle = new JButton("One cycle");
    cycle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	cycleDemo();
      }
    });
    cycle.setEnabled(false);

    step = new JButton("Step Element");
    step.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	stepDemo();
      }
    });
    step.setEnabled(false);

    //    match = new JButton("Matched Queries");
    //match.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    //	displayMatch();
    // }
    //});
    //match.setEnabled(false);

    query = new JButton("View matched queries");
    query.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
    	queryviewer.show();
     }
    });
    query.setEnabled(false);

    // For viewing all queries in the filter
    allqueryviewer = new XMLViewer("View all queries");
    WindowListener aqvl = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        allqueryviewer.hide();
      }
    };	
    allqueryviewer.addWindowListener(aqvl);

    allqueries = new JButton("View all queries");
    allqueries.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	allqueryviewer.show(demo.getCurrentQueries()); 
      }
    });
    allqueries.setEnabled(false);
  
    // For viewing XML
    xmlviewer = new XMLViewer("View XML");
    WindowListener xvl = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        xmlviewer.hide();
      }
    };	
    xmlviewer.addWindowListener(xvl);

    doc = new JButton("View XML");
    doc.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	//xmlviewer.setXML(demo.getCurrentXML());
	xmlviewer.show(demo.getCurrentXML()); //displayXML();
      }
    });
    doc.setEnabled(false);

    // set up control button panel
    JPanel controlPanel = new JPanel();
    TitledBorder controlTitle = BorderFactory.createTitledBorder("Controls");
    controlTitle.setTitleJustification(TitledBorder.CENTER);
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setBorder(controlTitle);
    //    controlPanel.setPreferredSize(new Dimension(350, 50));
    controlPanel.add(run);
    controlPanel.add(pause);
    controlPanel.add(cycle);
    controlPanel.add(step);
    controlPanel.setEnabled(false);
    
    // set up result button panel
    JPanel resultPanel = new JPanel();
    TitledBorder resultTitle = BorderFactory.createTitledBorder("Results");
    resultTitle.setTitleJustification(TitledBorder.CENTER);
    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.X_AXIS));
    resultPanel.setBorder(resultTitle);
    // resultPanel.setPreferredSize(new Dimension(350, 50));
    //resultPanel.add(match);
    resultPanel.add(query);
    resultPanel.add(allqueries);
    resultPanel.add(doc);
    resultPanel.setEnabled(false);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(controlPanel, BorderLayout.EAST);
    buttonPanel.add(resultPanel, BorderLayout.WEST);
    buttonPanel.setPreferredSize(new Dimension(700, 100));
    //    buttonPanel.add(s);

    // set up root panel
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setPreferredSize(new Dimension(800,600));

    // add components
    pane.add(buttonPanel, BorderLayout.NORTH);
    pane.add(scrollPanel, BorderLayout.SOUTH);
    
    //    pane.remove(scrollPanel);
    //pane.add(new BarChart(), BorderLayout.SOUTH);
    // attach panel
    setContentPane(pane);
  }

  // show System Monitor 
  public void showSystemMonitor() {
    sm.show();
  }

  // show Performance Monitor
  public void showPerformanceMonitor() {
    pm.show();
  }

  // show Workload Monitor
  public void showWorkloadMonitor() {
    wm.show();
  }

  public void setXML(String xml) {
    xmlviewer.setXML(xml);
  }

  public void searchXML(String elementName) {
    xmlviewer.search(elementName);
  }

  public void write(String text) {
    synchronized(textarea) {
      textarea.append(text);
      textarea.setCaretPosition(textarea.getDocument().getLength());
    }
  }

  public void writeln(String text) {
    synchronized(textarea) {
      textarea.append(text + "\n");
      textarea.setCaretPosition(textarea.getDocument().getLength());
    }
  }
  
  public void clearText() {
    synchronized(textarea) {
      textarea.setText("");
      textarea.setCaretPosition(0);
    }
  }

  // select bulk load file
  private void bulkLoad() {
     JFileChooser chooser = new JFileChooser();
     chooser.setDialogTitle("Bulk Load Queries from file (optional). ");
    TextFileFilter filter = new TextFileFilter();
    chooser.setFileFilter(filter);
    int returnVal = chooser.showDialog(this, "Load Queries (optional). ");
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      //System.out.println("You chose to bulk load this file: " +
      //			 chooser.getSelectedFile().getName());
      writeln(chooser.getSelectedFile().getName() + " is selected to bulk load");
      demo.enqueueQueries(chooser.getSelectedFile().getAbsolutePath());
      
    }
  }

  public void setDemo(String dtdfilename) {
    demo = new ICDEDemo(dtdfilename, this);
    textarea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    // Performance Monitor
    pm = new PerformanceMonitor(demo.getFilteringtime(), demo.getThroughput());
    WindowListener pml = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	pm.hide();
      }
    };
    pm.addWindowListener(pml);

     // System Monitor
    sm = new SystemMonitor();
    WindowListener sml = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        sm.hide();
      }
    };		
    sm.addWindowListener(sml);

    wm = new WorkloadMonitor(demo.getEXfilter());
    WindowListener wml = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	wm.hide();
      }
    };
    wm.addWindowListener(wml);

    queryviewer = new QueryViewer(demo);
    WindowListener qvl = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	queryviewer.hide();
      }
    };
    queryviewer.addWindowListener(qvl);
    queryviewer.waitForDisplay();

    Thread initThread = new Thread() {
      public void run() {
	bulkLoad();
	demo.start();
	writeln("Demo is ready to be run.");
	
	// enable/disable corresponding buttons
	
	menubar.enableRun();
	menubar.setModeEnabled(true);
	menubar.setParamEnabled(true);
	menubar.setMonitorEnabled(true);
	run.setEnabled(true);
	cycle.setEnabled(true);
	step.setEnabled(true);
	//match.setEnabled(true);
	query.setEnabled(true);
	allqueries.setEnabled(true);
	doc.setEnabled(true);
	textarea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    };
    initThread.start();
  
  }

  // Use file dialog to select a dtd file...
  public void selectDTDFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select a DTD file");
    DTDFileFilter filter = new DTDFileFilter();
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      //System.out.println("You chose to open this file: " +
      //	 chooser.getSelectedFile().getName());
      writeln(chooser.getSelectedFile().getName() + " is selected");
      menubar.disableSelect();
      setDemo(chooser.getSelectedFile().getAbsolutePath());
      writeln(chooser.getSelectedFile().getAbsolutePath());
      	    
    } else {
      JOptionPane.showMessageDialog(null, "Please select a DTD file", "Alert", JOptionPane.WARNING_MESSAGE);
    }
    
  }

  // Run Demo
  public void runDemo() {
    demo.runAgain();
    writeln("Running Demo ...");
    queryviewer.waitForDisplay();
    menubar.enablePause();
    menubar.disableRun();
    run.setEnabled(false);
    pause.setEnabled(true);
    cycle.setEnabled(false);
    step.setEnabled(false);
    //match.setEnabled(false);
  }

  // Pause Demo
  public void pauseDemo() {
    demo.pause();
    writeln("Demo has been paused ...");
    menubar.enableRun();
    menubar.disablePause();
    run.setEnabled(true);
    pause.setEnabled(false);
    //match.setEnabled(true);
    if (! demo.isBatchMode()) {
      cycle.setEnabled(true);
      step.setEnabled(true);
    }
  }

  // Cycle Demo
  public void cycleDemo() {
    writeln("Stepping Demo one cycle ...");
    queryviewer.waitForDisplay();
    menubar.disableRun();
    run.setEnabled(false);
    demo.cycle();
    menubar.enableRun();
    run.setEnabled(true);
    //match.setEnabled(true);
  }

  // Step Demo
  public void stepDemo() {
    queryviewer.waitForDisplay();
    menubar.disableRun();
    run.setEnabled(false);
    //match.setEnabled(false);
    //    xmlviewer.show();
    demo.step();
    menubar.enableRun();
    run.setEnabled(true);

  }

  // Change Query Rate
  public void changeQueryRate() {
    String strQueryRate = JOptionPane.showInputDialog(this, "Please input new query rate");
    if (strQueryRate == null) {
      return;
    }
    try {
      double queryRate = Double.parseDouble(strQueryRate);
      demo.setQueryRate(queryRate);
      sm.changeQueryRate(queryRate);
      writeln("Query rate is changed to " + queryRate + " queries per ms");
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid query rate", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Change Query Buffer Size
  public void changeQueryBufferSize() {
    String strBufferSize = JOptionPane.showInputDialog(this, "Please input new query buffer size");
    if (strBufferSize == null) {
      return;
    }
    try {
      int bufferSize = Integer.parseInt(strBufferSize);
      if (bufferSize <= 0) {
	throw new NumberFormatException();
      }
      demo.changeQueryBufferSize(bufferSize);
      sm.changeQueryBufferSize(bufferSize);
      writeln("Query Buffer size is changed to " + bufferSize);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid query buffer size", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Change XML Buffer Size
  public void changeXMLBufferSize() {
    String strBufferSize = JOptionPane.showInputDialog(this, "Please input new xml buffer size");
    if (strBufferSize == null) {
      return;
    }
    try {
      int bufferSize = Integer.parseInt(strBufferSize);
      if (bufferSize <= 0) {
	throw new NumberFormatException();
      }
      demo.changeXMLBufferSize(bufferSize);
      sm.changeXMLBufferSize(bufferSize);
      writeln("XML Buffer size is changed to " + bufferSize);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid xml buffer size", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Display Match Queries
  /*  public void displayMatch() {
    queryviewer.show();
    ArrayList matchedQueries = demo.getMatchedQueries();
    if (matchedQueries == null) {
      JOptionPane.showMessageDialog(this, "Please run the system", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String strResults = "Matched Queries are:\n";
    int size = matchedQueries.size();
    for (int i=0; i<size; i++) {
      strResults += ((Integer)matchedQueries.get(i))+" ";
    }
    writeln(strResults);
    //    JOptionPane.showMessageDialog(this, strResults, "Matched Queries", JOptionPane.INFORMATION_MESSAGE);
    }*/
    
  // Display Query
  // public void displayQuery() {
  // queryviewer.show();
    /*
    String strQueryID  = JOptionPane.showInputDialog(this, "Please enter the query id");
    if (strQueryID == null) {
      return;
    }
    int queryID;

    try {
      queryID = Integer.parseInt(strQueryID);
      if (queryID <= 0) {
	throw new NumberFormatException();
      }
      String queryString = demo.getQuery(queryID);
      if (queryString == null) {
	JOptionPane.showMessageDialog(this, "No such query", "Error", JOptionPane.ERROR_MESSAGE);
      } else {
	writeln("Query #" + queryID + ": " + queryString);
	//JOptionPane.showMessageDialog(this, queryString, "Query "+queryID, JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid Query ID", "Error", JOptionPane.ERROR_MESSAGE);
      
      }	*/
  //}

  /*
  // View currnet XML doc
  public void displayXML() {
    String doc = demo.getCurrentXML();

    if (doc == null) {
      JOptionPane.showMessageDialog(null, "Please run in ONE-XML mode to use this option", "Warning", JOptionPane.WARNING_MESSAGE);
    } else {
      writeln("Current XML doc:\n" + doc);
      //      JOptionPane.showMessageDialog(this, doc, "Current XML doc", JOptionPane.PLAIN_MESSAGE);
    }
    }*/

  // Change Dynamic Update
  public void changeDynamicUpdate() {
    if (demo.isDynamicUpdateEnabled()) {
      demo.setDynamicUpdateEnabled(false);
      menubar.setDynamicUpdate(false);
      writeln("Dynamic Update is disallowed");
    } else {
      demo.setDynamicUpdateEnabled(true);
      menubar.setDynamicUpdate(true);
      writeln("Dynamic Update is allowed");
    }
  }
   
  public void updateXMLInBuffer(int no) {
    sm.changeXMLInBuffer(no);
  }

  public void updateFrame() {
    pm.update();
    //    if (! demo.isBatchMode()) {
      wm.update();
      queryviewer.update(demo.getMatchedQueries());
      allqueryviewer.setXML(demo.getCurrentQueries());
      //}
  }

  // Change Batch mode
  public void changeBatchMode() {
    if (demo.isBatchMode()) {
      demo.setBatchMode(false);
      menubar.setBatchMode(false);
      cycle.setEnabled(true);
      step.setEnabled(true);
      writeln("Switched to One-XML Mode");
    } else {
      // pick directory
      JFileChooser fc = new JFileChooser(".");
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = fc.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
      	demo.changeDirectory(fc.getSelectedFile().getAbsolutePath());
      } else {
      	JOptionPane.showMessageDialog(this, "Did not select directory", "Cancelled", JOptionPane.ERROR_MESSAGE);
      	// do nothing
      	return;
      }

      demo.setBatchMode(true);
      menubar.setBatchMode(true);
      cycle.setEnabled(false);
      step.setEnabled(false);
      writeln("Switched to Batch Mode");
    }
  }

  public void changeQueryDepth() {
    String str = JOptionPane.showInputDialog(this, "Please input new Query Depth for Query Generator");
    if (str == null) {
      return;
    }
    try {
      int value = Integer.parseInt(str);
      if (value <= 0) {
	throw new NumberFormatException();
      }
      demo.m_maxDepth = value;
      sm.changeQueryMaxDepth(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeWildcard() {
    String str = JOptionPane.showInputDialog(this, "Please input new wildcard probability for Query Generator");
    if (str == null) {
      return;
    }
    try {
      double value = Double.parseDouble(str);
      if (value < 0 || value > 1) {
	throw new NumberFormatException();
      }
      demo.m_wildcard = value;
      sm.changeQueryWildcard(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeDSlash() {
    String str = JOptionPane.showInputDialog(this, "Please input new dSlash for Query Generator");
    if (str == null) {
      return;
    }
    try {
      double value = Double.parseDouble(str);
      if (value < 0 || value > 1) {
	throw new NumberFormatException();
      }
      demo.m_dSlash = value;
      sm.changeQueryDSlash(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public void changePredProb() {
    String str = JOptionPane.showInputDialog(this, "Please input new predicate Probability for Query Generator");
    if (str == null) {
      return;
    }
    try {
      double value = Double.parseDouble(str);
      if (value < 0 || value > 1) {
	throw new NumberFormatException();
      }
      demo.m_predProb = value;
      sm.changeQueryPredicate(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeLevelDist() {
    String str = JOptionPane.showInputDialog(this, "Please input new levelDIst for Query Generator");
    if (str == null) {
      return;
    }
    char value = str.charAt(0);
    demo.m_levelDist = value;	
    sm.changeQueryLevelDist(value);
  }  
  
  public void changeNestedPath() {
    String str = JOptionPane.showInputDialog(this, "Please input new nested path for Query Generator");
    if (str == null) {
      return;
    }
    try {
      double value = Double.parseDouble(str);
      if (value < 0 || value > 1) {
	throw new NumberFormatException();
      }
      demo.m_nestedPath = value;
      sm.changeQueryNestedPath(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeXMLLevel() {
    String str = JOptionPane.showInputDialog(this, "Please input new Query Depth for Query Generator");
    if (str == null) {
      return;
    }
    try {
      int value = Integer.parseInt(str);
      if (value <= 0) {
	throw new NumberFormatException();
      }
      demo.m_maxLevel = value;
      sm.changeXMLMaxLevel(value);
    } catch(NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
      

  public static void main(String[] args) {
  	if (args.length > 0) {
  		ICDEDemo.m_yfilter_home = args[0];
  	}
  	
    JFrame frame = new Framework();
    
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    
    frame.addWindowListener(l);
    frame.pack();
    frame.setVisible(true);
  }
}












