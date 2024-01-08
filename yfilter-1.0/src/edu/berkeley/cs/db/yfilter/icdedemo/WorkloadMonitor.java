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

import edu.berkeley.cs.db.yfilter.filter.*;
import java.awt.GridLayout;
import javax.swing.*;
import java.awt.*;

public final class WorkloadMonitor extends JFrame {

  //Strings for the labels
  private final static String queriesTotalString = "# of queries in Yfilter";
  private final static String queriesDistinctString = "# of distinct paths";
  private final static String queriesLengthString = "Average path length";
  private final static String predicatesString = "# of predicates";
  private final static String nfaSizeString = "Size of NFA (# of states)";
  //private static String acceptingStateString = "# of accepting states";
  private final static String selectivityString = "Query selectivity";

  // Labels to store results
  private JLabel queriesTotalField;
  private JLabel queriesDistinctField;
  private JLabel queriesLengthField;
  private JLabel predicatesField;
  private JLabel nfaSizeField;
  //private JLabel acceptingStateField;
  private JLabel selectivityField;

  // Filter
  EXfilterBasic filter;

  public WorkloadMonitor(EXfilter f) {
    super("Workload Monitoring");

     //Labels to identify the text fields
    JLabel queriesTotalLabel;
    JLabel queriesDistinctLabel;
    JLabel queriesLengthLabel;
    JLabel predicatesLabel;
    JLabel nfaSizeLabel;
    //private JLabel acceptingStateLabel;
    JLabel selectivityLabel;

    if (! (f instanceof edu.berkeley.cs.db.yfilter.filter.EXfilterBasic)) {
        System.out.println("WorkloadMonitor(EXfilter) can only take \\" +
                "type of edu.berkeley.cs.db.yfilter.filter.EXfilterBasic!");
        System.exit(-1);
    }

    filter = (EXfilterBasic)f;

    // Create the labels
    queriesTotalLabel = new JLabel(queriesTotalString);
    queriesDistinctLabel = new JLabel(queriesDistinctString);
    queriesLengthLabel = new JLabel(queriesLengthString);
    predicatesLabel = new JLabel(predicatesString);
    nfaSizeLabel = new JLabel(nfaSizeString);
    //acceptingStateLabel = new JLabel(acceptingStateString);
    selectivityLabel = new JLabel(selectivityString);

     // Create the fields
    queriesTotalField = new JLabel("      ---      ");
    queriesDistinctField = new JLabel("      ---     ");
    queriesLengthField = new JLabel("      ---     ");
    predicatesField = new JLabel("      ---     ");
    nfaSizeField = new JLabel("      ---     ");
    //acceptingStateField = new JLabel("      ---     ");
    selectivityField = new JLabel("      ---     ");

    //Layout the labels in a panel.
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(0, 1));
    labelPane.add(queriesTotalLabel);
    labelPane.add(queriesDistinctLabel);
    labelPane.add(queriesLengthLabel);
    labelPane.add(predicatesLabel);
    labelPane.add(nfaSizeLabel);	
    // labelPane.add(acceptingStateLabel);
    labelPane.add(selectivityLabel);

    //Layout the fields in a panel.
    JPanel fieldPane = new JPanel();
    fieldPane.setLayout(new GridLayout(0, 1));
    fieldPane.add(queriesTotalField);
    fieldPane.add(queriesDistinctField);
    fieldPane.add(queriesLengthField);
    fieldPane.add(predicatesField);
    fieldPane.add(nfaSizeField);	
    //fieldPane.add(acceptingStateField);
    fieldPane.add(selectivityField);

    
    //Put the panels in another panel, labels on left,
    //text fields on right.
    JPanel contentPane = new JPanel();
    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPane.setLayout(new BorderLayout());
    contentPane.add(labelPane, BorderLayout.CENTER);
    contentPane.add(fieldPane, BorderLayout.EAST);
    
    setContentPane(contentPane);
    pack();
  }

  public void update() {
    synchronized(filter) {
      queriesTotalField.setText(String.valueOf(filter.getNoQueries()));
      queriesDistinctField.setText(String.valueOf(filter.getNoDistinctPaths()));
      queriesLengthField.setText(String.valueOf(Math.round(filter.getPathLength() * 100) / 100.0));
      predicatesField.setText(String.valueOf(filter.getNoPredicates()));
      nfaSizeField.setText(String.valueOf(filter.getNoStates()));
      selectivityField.setText(String.valueOf(Math.round(filter.getSelectivity() * 100)) + " %");
    }
  }

}

