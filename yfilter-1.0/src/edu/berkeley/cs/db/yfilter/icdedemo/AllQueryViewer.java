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
import java.util.ArrayList;

/* 
 * This class provides a viewer for matched queries and, their elements.
 * 
 * 
 */
public final class AllQueryViewer extends JFrame {
  private JTextArea textarea;
  private JButton search;
  private final static int MAX_QUERIES_PER_DISPLAY = 20;
  private int index = 0;
  private JButton next;
  private JButton prev;
  private JTextField queryField, elementField;
  private JLabel queryLabel;//, elementLabel;
  private JTextArea elementLabel;
  private ICDEDemo demo;
  private ArrayList matchedQueries;
  private JLabel noQueriesLabel;

  public AllQueryViewer(ICDEDemo d) {
    super("Query Viewer");
      
    demo = d;
    textarea = new JTextArea("Not used in step element mode", 25, 15);
    textarea.setLineWrap(true);
    textarea.setMargin(new Insets(5,5,5,5));
    textarea.setEditable(false);
    textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
    JScrollPane scrollPane = new JScrollPane(textarea);

    noQueriesLabel = new JLabel(" All queries in filter");

    JPanel controlPane = new JPanel();
    controlPane.setLayout(new BorderLayout());
    controlPane.add(prev, BorderLayout.WEST);
    controlPane.add(noQueriesLabel, BorderLayout.CENTER);
    controlPane.add(next, BorderLayout.EAST);
    JPanel textPanel = new JPanel();
    TitledBorder controlTitle = BorderFactory.createTitledBorder("All Queries");
    controlTitle.setTitleJustification(TitledBorder.CENTER);
    textPanel.setLayout(new BorderLayout());
    textPanel.setBorder(controlTitle);
    textPanel.add(controlPane, BorderLayout.NORTH);
    textPanel.add(scrollPane, BorderLayout.CENTER);

    
    queryField = new JTextField("0", 7);
    queryField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	updateQuery();
      }
    });
    queryLabel = new JLabel("No such query");
    JPanel queryPanel = new JPanel();
    TitledBorder queryTitle = BorderFactory.createTitledBorder("View Query");
    queryPanel.setBorder(queryTitle);
    queryPanel.setLayout(new BorderLayout());
    queryPanel.add(queryField, BorderLayout.WEST);
    queryPanel.add(queryLabel, BorderLayout.CENTER);

    elementField = new JTextField("0", 7);
    elementField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	updateElements();
      }
    });
    //elementLabel = new JLabel("No such query");
    elementLabel = new JTextArea("No such query");
    elementLabel.setEditable(false);
    elementLabel.setLineWrap(true);
    JPanel elementPanel = new JPanel();
    TitledBorder elementTitle = BorderFactory.createTitledBorder("View matched elements");
    elementPanel.setBorder(elementTitle);
    elementPanel.setLayout(new BorderLayout());
    elementPanel.add(elementField, BorderLayout.WEST);
    elementPanel.add(elementLabel, BorderLayout.CENTER);


    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    //pane.setPreferredSize(new Dimension(50,100));
    pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    pane.add(textPanel, BorderLayout.CENTER);
    // XXX why do we need this?
    //pane.add(queryPanel, BorderLayout.SOUTH);
    //pane.add(elementPanel, BorderLayout.SOUTH);
    setContentPane(pane);
    textPanel.setEnabled(false);
    pack();

  }	

  public void updateQuery() {
    int queryID;

    try {
      queryID = Integer.parseInt(queryField.getText());
      if (queryID <= 0) {
	throw new NumberFormatException();
      }
      String queryString = demo.getQuery(queryID);
      if (queryString == null) {
	JOptionPane.showMessageDialog(this, "No such query", "Error", JOptionPane.ERROR_MESSAGE);
	queryLabel.setText("No such query");
      } else {
	queryLabel.setText(queryString);
      }
    } catch(NumberFormatException nfe) {
       JOptionPane.showMessageDialog(this, "Invalid Query ID", "Error", JOptionPane.ERROR_MESSAGE);
       queryLabel.setText("Invalid Query ID");
    }
  }

  public void updateElements() {
  	int queryID;

    try {
      queryID = Integer.parseInt(elementField.getText());
      if (queryID <= 0) {
	throw new NumberFormatException();
      }
      String queryString = null;
      //XXX: Should this be part of ResultCollection?
      /*
      Object obj = demo.getMatchingElements(queryID);
     
      if (obj instanceof ParsingContext)
			queryString = ((ParsingContext)obj).toXMLString();
      else if (obj instanceof String)
			queryString = (String)obj;
      else if (obj instanceof ArrayList) {
      	queryString = "";
		ArrayList l = (ArrayList)obj;
		int size2 = l.size();
		for (int j=0; j<size2; j++) {
			Object ob = l.get(j);
			if (ob instanceof ParsingContext)
				queryString += ((ParsingContext)ob).toXMLString();
			else if (ob instanceof String)
				queryString += (String)ob;
		}
      	
      }
 
   
     */
      queryString = demo.getStringOfMatchingElements(queryID);
      
      if (queryString == null) {
      	JOptionPane.showMessageDialog(this, "No such query", "Error", JOptionPane.ERROR_MESSAGE);
      	elementLabel.setText("No such query");
      } else {
      	int len = queryString.length();

      	/*
      	if (len > 20) {
          	// if the string is too long, fold it using HTML tags
          	StringBuffer sb = new StringBuffer();
          	sb.append("<html>");
          	int startIndex = 0, endIndex = 19;
          	
          	while (startIndex < len) {
          		//endIndex = queryString.indexOf("/>", startIndex);
          		endIndex = startIndex + 19;
          		if (endIndex > len)
          			endIndex = len - 1;
              	sb.append(queryString.substring(startIndex, endIndex));
              	sb.append("<br>");
              	startIndex = endIndex + 1;
         	}
          	sb.append("</html>");
          	queryString = sb.toString();
      	}
      	*/
      	
      	elementLabel.setText(queryString);
      }
    } catch(NumberFormatException nfe) {
       JOptionPane.showMessageDialog(this, "Invalid Query ID", "Error", JOptionPane.ERROR_MESSAGE);
       elementLabel.setText("Invalid Query ID");
    } catch(Exception e) {
    JOptionPane.showMessageDialog(this, "No matching elements", "Error", JOptionPane.ERROR_MESSAGE);
    elementLabel.setText("No matching elements!");
 }
  }
  
  
  public void show(String xml) {
    super.show();
  }

  public void writeln(String str) {
    textarea.append(str + "\n");
  }

  public void update(ArrayList querylist) {
    matchedQueries = querylist;
    index = 0;
    textarea.setText("Click Next to view Matched Queries");
    noQueriesLabel.setText(matchedQueries.size() + " match(es).");
    next.setEnabled(true);
  }

  // base index is "index"
  public void nextQueries() {
    int size = matchedQueries.size();
    int limit = Math.min(size, index + MAX_QUERIES_PER_DISPLAY);
    textarea.setText("");
    int queryID;
    for (int i = index; i < limit; i++) {
      queryID = ((Integer)matchedQueries.get(i)).intValue();
      writeln("Query #" + queryID + ": " + demo.getQuery(queryID));
    }
    index = limit;
    if (limit >= size) {
      next.setEnabled(false);
    } else {
      next.setEnabled(true);
    }
    if (index - MAX_QUERIES_PER_DISPLAY <= 0) {
      prev.setEnabled(false);
    } else {
      prev.setEnabled(true);
    }
  }

  public void prevQueries() {
    index = Math.max(0, index - MAX_QUERIES_PER_DISPLAY - MAX_QUERIES_PER_DISPLAY);
    nextQueries();
  }
    
  public void waitForDisplay() {
    textarea.setText("Matched Queries are not ready to display");
    next.setEnabled(false);
    prev.setEnabled(false);
  }

}      
     
     
      
