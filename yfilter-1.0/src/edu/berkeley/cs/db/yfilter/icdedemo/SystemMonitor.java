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

import java.awt.GridLayout;
import javax.swing.*;
import java.awt.*;

public final class SystemMonitor extends JFrame {

  //Strings for the labels
  private final static String xmlBufferSizeString = "XML buffer size: ";
  private final static String xmlInBufferString = "XML docs in buffer: ";
  private final static String queryBufferSizeString = "Query buffer size: ";
  private final static String queryRateString = "Query Rate: ";

  private final static String queryMaxDepthString = "Query Max Depth: ";
  private final static String queryWildcardString = "Query Wildcard Percentage: ";
  private final static String queryDSlashString = "Query Double Slash Percentage: ";
  private final static String queryPredicateString = "Query Predicate Precentage: ";
  private final static String queryLevelDistString = "Query Level Distance Percentage: ";
  private final static String queryNestedPathString = "Query Nested Path Percentage: ";
  private final static String xmlMaxLevelString = "XML Max Level: ";

  // Labels to store results
  private JLabel xmlBufferSizeField;
  private JLabel xmlInBufferField;
  private JLabel queryBufferSizeField;
  private JLabel queryRateField;
  
  private JLabel queryMaxDepthField;
  private JLabel queryWildcardField;
  private JLabel queryDSlashField;
  private JLabel queryPredicateField;
  private JLabel queryLevelDistField;
  private JLabel queryNestedPathField;
  private JLabel xmlMaxLevelField;

  // Progress Bar
  private JProgressBar xmlBar;

  public SystemMonitor() {
    super("System Monitoring");

    //Labels to identify the text fields
    JLabel xmlBufferSizeLabel;
    JLabel xmlInBufferLabel;
    JLabel queryBufferSizeLabel;
    JLabel queryRateLabel;
 
    JLabel queryMaxDepthLabel;
    JLabel queryWildcardLabel;
    JLabel queryDSlashLabel;
    JLabel queryPredicateLabel;
    JLabel queryLevelDistLabel;
    JLabel queryNestedPathLabel;
    JLabel xmlMaxLevelLabel;

    JLabel xmlBarLabel;
      

    // Create Progress Bar
    xmlBarLabel = new JLabel("XML docs");
    xmlBar = new JProgressBar(0, 20);
    xmlBar.setValue(0);
    xmlBar.setStringPainted(true);
    xmlBar.setString("0 doc(s)");

    // Create the labels
    xmlBufferSizeLabel = new JLabel(xmlBufferSizeString);
    xmlInBufferLabel = new JLabel(xmlInBufferString);
    queryBufferSizeLabel = new JLabel(queryBufferSizeString);
    queryRateLabel = new JLabel(queryRateString);

    xmlBufferSizeField = new JLabel("20");
    xmlInBufferField = new JLabel("0");
    queryBufferSizeField = new JLabel("10000");
    queryRateField = new JLabel("0.003");

    //Layout the labels in a panel.
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(0, 1));
    labelPane.add(xmlBufferSizeLabel);
    labelPane.add(xmlInBufferLabel);
    labelPane.add(queryBufferSizeLabel);
    labelPane.add(queryRateLabel);

    //Layout the fields in a panel.
    JPanel fieldPane = new JPanel();
    fieldPane.setLayout(new GridLayout(0, 1));
    fieldPane.add(xmlBufferSizeField);
    fieldPane.add(xmlInBufferField);
    fieldPane.add(queryBufferSizeField);
    fieldPane.add(queryRateField);

    //Put the panels in another panel, labels on left,
    //text fields on right.
    JPanel dataPane = new JPanel();
    dataPane.add(labelPane, BorderLayout.CENTER);
    dataPane.add(fieldPane, BorderLayout.EAST);

    // for Generator
    queryMaxDepthLabel = new JLabel(queryMaxDepthString);
    queryWildcardLabel = new JLabel(queryWildcardString);
    queryDSlashLabel = new JLabel(queryDSlashString);
    queryPredicateLabel = new JLabel(queryPredicateString);
    queryLevelDistLabel = new JLabel(queryLevelDistString);
    queryNestedPathLabel = new JLabel(queryNestedPathString);
    xmlMaxLevelLabel = new JLabel(xmlMaxLevelString);

    queryMaxDepthField = new JLabel("6");
    queryWildcardField = new JLabel("20.00 %");
    queryDSlashField = new JLabel("20.00 %");
    queryPredicateField = new JLabel("3.00 %");
    queryLevelDistField = new JLabel("u");
    queryNestedPathField = new JLabel("20.00 %");
    xmlMaxLevelField = new JLabel("6");

     //Layout the labels in a panel.
    JPanel genLabelPane = new JPanel();
    genLabelPane.setLayout(new GridLayout(0, 1));
    genLabelPane.add(queryMaxDepthLabel);
    genLabelPane.add(queryWildcardLabel);
    genLabelPane.add(queryDSlashLabel);
    genLabelPane.add(queryPredicateLabel);
    //genLabelPane.add(queryLevelDistLabel);
    genLabelPane.add(queryNestedPathLabel);
    genLabelPane.add(xmlMaxLevelLabel);

    //Layout the fields in a panel.
    JPanel genFieldPane = new JPanel();
    genFieldPane.setLayout(new GridLayout(0, 1));
    genFieldPane.add(queryMaxDepthField);
    genFieldPane.add(queryWildcardField);
    genFieldPane.add(queryDSlashField);
    genFieldPane.add(queryPredicateField);
    //genFieldPane.add(queryLevelDistField);
    genFieldPane.add(queryNestedPathField);
    genFieldPane.add(xmlMaxLevelField);

    JPanel genDataPane = new JPanel();
    genDataPane.add(genLabelPane, BorderLayout.CENTER);
    genDataPane.add(genFieldPane, BorderLayout.EAST);

    JPanel contentPane = new JPanel();
    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(dataPane);
    contentPane.add(xmlBarLabel);
    contentPane.add(xmlBar);
    contentPane.add(genDataPane);
    setContentPane(contentPane);
    pack();
  }

  public void changeXMLBufferSize(int size) {
    xmlBufferSizeField.setText(String.valueOf(size));
    xmlBar.setMaximum(size);
  }

  public void changeQueryBufferSize(int size) {
    queryBufferSizeField.setText(String.valueOf(size));
  }

  public void changeQueryRate(double rate) {
    queryRateField.setText(String.valueOf(rate));
  }

  public void changeXMLInBuffer(int no) {
    xmlInBufferField.setText(String.valueOf(no));
    xmlBar.setValue(no);
    xmlBar.setString(String.valueOf(no) + "docs");
  }
  
  public void changeQueryMaxDepth(int value) {
    queryMaxDepthField.setText(String.valueOf(value));
  }

  public void changeQueryWildcard(double value) {
    queryWildcardField.setText(String.valueOf(value * 100) + " %");
  }

  public void changeQueryDSlash(double value) {
    queryDSlashField.setText(String.valueOf(value*100) + " %");
  }

  public void changeQueryPredicate(double value) {
    queryPredicateField.setText(String.valueOf(value*100) + " %");
  }

  public void changeQueryLevelDist(int value) {
    queryLevelDistField.setText(String.valueOf(value));
  }

  public void changeQueryNestedPath(double value) {
    queryNestedPathField.setText(String.valueOf(value*100) + " %");
  }

  public void changeXMLMaxLevel(int value) {
    xmlMaxLevelField.setText(String.valueOf(value));
  }

}




