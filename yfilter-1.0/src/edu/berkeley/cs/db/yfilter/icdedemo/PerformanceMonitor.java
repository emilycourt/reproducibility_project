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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.image.*;
import java.io.File;
import java.util.Vector;


public class PerformanceMonitor extends JFrame {
  ThroughputChart throughputChart;
  FilterChart filtertimeChart;
  JButton changeScaleFilter;
  JButton changeScaleThroughput;

  public PerformanceMonitor(Vector f, Vector t) {
    super("Performance Monitor");
    filtertimeChart = new FilterChart(f);
    throughputChart = new ThroughputChart(t);
    changeScaleFilter = new JButton("Change Scale");
    changeScaleFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	changeFilterChartScale();
      }
    });

    changeScaleThroughput = new JButton("Change Scale");
    changeScaleThroughput.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	changeThroughputChartScale();
      }
    });
    
    //text fields on right.
    JPanel contentPane = new JPanel();
    
    JPanel filterPanel = new JPanel();
    TitledBorder filterTitle = BorderFactory.createTitledBorder("Filtering Time per document");
    filterTitle.setTitleJustification(TitledBorder.CENTER);
    filterPanel.setLayout(new BorderLayout());
    filterPanel.setBorder(filterTitle);
    filtertimeChart.setSize(600,300);
    filterPanel.add(filtertimeChart, BorderLayout.CENTER);
    filterPanel.add(changeScaleFilter, BorderLayout.SOUTH);

    JPanel throughputPanel = new JPanel();
    TitledBorder throughputTitle = BorderFactory.createTitledBorder("Throughput");
    throughputTitle.setTitleJustification(TitledBorder.CENTER);
    throughputPanel.setLayout(new BorderLayout());
    throughputPanel.setBorder(throughputTitle);
    throughputChart.setSize(400,300);
    throughputPanel.add(throughputChart, BorderLayout.CENTER);
    throughputPanel.add(changeScaleThroughput, BorderLayout.SOUTH);
    
    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPane.setLayout(new BorderLayout());
    contentPane.add(filterPanel, BorderLayout.WEST);
    contentPane.add(throughputPanel, BorderLayout.EAST);
    
    setContentPane(contentPane);
    pack();

    //Create a timer.
    //    timer = new Timer(1000, new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    //	filtertimeChart.repaint(); 
    // }
    //});
  }

  public void changeFilterChartScale() {
    String strMax = JOptionPane.showInputDialog(this, "Please input the new display max value");
    if (strMax == null) {
      return;
    }    
    try {
      int max = Integer.parseInt(strMax);
      if (max < 100) throw new NumberFormatException();
      filtertimeChart.setMaxValue(max);
      filtertimeChart.repaint();
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
    }	
  }

  public void changeThroughputChartScale() {
    String strMax = JOptionPane.showInputDialog(this, "Please input the new display max value");
    if (strMax == null) {
      return;
    }    
    try {
      int max = Integer.parseInt(strMax);
      if (max < 100) throw new NumberFormatException();
      throughputChart.setMaxValue(max);
      throughputChart.repaint();
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }	

  public void update() {
    filtertimeChart.repaint();
    throughputChart.repaint();
  }
}

