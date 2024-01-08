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


public class BarChart extends Canvas {
  Vector data = new Vector();
  String title = "Bar Chart";
  String xlabel = "docs";
  String ylabel = "Time (ms)";
  int divide = 10;
  int greatest = 200;
  boolean kilo = false;
  final static int SPACING = 5;
  final static int MAX_DATA = 20;

  public BarChart() {
    for (int i = 1; i < 21; i++) {
      data.add(new Long(i));
    }
    //setPreferredSize(new Dimension(300,300));
    //setSize(300,300);
  }

  public BarChart(String t, String x, String y, Vector input, int max, int dy, boolean k) {
    data = input;
    title = t;
    xlabel = x;
    ylabel = y;
    greatest = max;
    divide = dy;
    kilo = k;
  }

  public void computeGreatest() {
    int max = greatest;
    if (data.size() == 0) {
      return;
    }
    for (int i = 0; i < data.size(); i++) {
      int val = ((Long)data.get(i)).intValue();
      if (val > max) {
	max = val;
      }
    }

    if (max > greatest) {
      greatest = max + max / divide;
    }
  }
  
  public void paint(Graphics graphics) {

    //    computeGreatest();

    Image screen = createImage(getSize().width, getSize().height);
    Graphics g = screen.getGraphics();

    int height = getSize().height;
    int width = getSize().width;
    int baseY = getSize().height - 30;
    int baseX = 30;
    int farY = 30;
    int farX = getSize().width - 20;
    int incr = greatest / divide;
    int nodata = data.size();
    //final int barWidth = 540 / 20;
    int barWidth;// = (farX - baseX - SPACING * nodata) / (nodata);
    double heightUnit = (baseY - farY) / (double)greatest;
    //System.out.println("HeightUnit: " + heightUnit);
    int locX = baseX + 1;
    int yincr; 
    int val;
   
    //System.out.println("Height: " + getSize().height);
    //System.out.println("Width: " + getSize().width);
    
    // title
    g.drawChars(title.toCharArray(), 0, title.length(), width/2 - title.length()*3/2, 10);
    // x-axis
    g.drawLine(baseX, baseY, farX, baseY);
    g.drawChars(xlabel.toCharArray(), 0, xlabel.length(), width/2, baseY + 20); 
    // y-axis
    g.drawLine(baseX, baseY, baseX, farY);  
    g.drawChars(ylabel.toCharArray(), 0, ylabel.length(), baseX - 20, 20);
    
    // gridlines
    yincr = (int)(heightUnit * incr);
    //System.out.println("Yincr: " + yincr);
    val = 0;
    g.drawChars(String.valueOf(0).toCharArray(), 0, String.valueOf(0).length(), baseX-20, baseY);
    for (int i = baseY-yincr; i > farY; i-=yincr) {
      //g.setColor(Color.yellow);
      g.drawLine(baseX+1, i, farX, i);
      //g.setColor(Color.black);
      val += incr;
      if (kilo) {
	g.drawChars(String.valueOf(val/(double)1000).toCharArray(), 0, String.valueOf(val/(double)1000).length(), 0, i);
      } else {
	g.drawChars(String.valueOf(val).toCharArray(), 0, String.valueOf(val).length(), 0, i);
      }
      
    }

    if (nodata != 0) {
      barWidth = (farX - baseX - SPACING * nodata) / (nodata);
      int pos;
      int value;
      for (int i = 0; i < nodata; i++) {
	//System.out.println("Height: " + (int)(heightUnit*((Long)data.get(i)).intValue()));
	value = ((Long)data.get(i)).intValue();
	if (value > greatest) {
	  // prevent bar drawing out of bound
	  pos = (int)(heightUnit * greatest);
	} else {
	  pos = (int)(heightUnit * value);
	}
	// draw the value
	g.setColor(Color.red);
	g.drawChars(String.valueOf(value).toCharArray(), 0, String.valueOf(value).length(), locX, baseY - pos - 10);
	// draw the bar
	g.setColor(Color.blue);
	g.fill3DRect(locX, baseY-pos, barWidth, pos, true);
	
    	locX+=barWidth+SPACING;
      }
    }
    graphics.drawImage(screen, 0, 0, this);
  }
} 












