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
import java.awt.*;
import javax.swing.border.*;
import java.awt.image.*;
import java.util.Vector;


public final class FilterChart extends Canvas {
  private Vector data;
  private final String title = "Filtering time";
  private final String xlabel = "";
  private final String ylabel = "time (ms)";
  private int max_yValue = 1000;
  private int division  = 10;
  private final static int SPACING = 5;
  private final static int MAX_DATA = 20;

  public FilterChart(Vector v) {
    data = v;
  }

  public void setMaxValue(int max) {
    max_yValue = max;
  }

  public void computeGreatest() {
    int max = max_yValue;
    if (data.size() == 0) {
      return;
    }
    for (int i = 0; i < data.size(); i++) {
      int val = ((Long)data.get(i)).intValue();
      if (val > max) {
	max = val;
      }
    }

    if (max > max_yValue) {
      max_yValue = max + max / division;
    }
  }
  
  public void paint(Graphics graphics) {


    Image screen = createImage(getSize().width, getSize().height);
    Graphics g = screen.getGraphics();

    int height = getSize().height;
    int width = getSize().width;
    int baseY = getSize().height - 30;
    int baseX = 30;
    int farY = 30;
    int farX = getSize().width - 20;
   
    int nodata = data.size(); 
    int barWidth = (farX - baseX - SPACING * MAX_DATA) / MAX_DATA;
    double height_per_unit = (baseY - farY) / (double)max_yValue;
    
    int locX = baseX + 1;
   
       
    // title
    g.drawChars(title.toCharArray(), 0, title.length(), width/2 - title.length()*3/2, 10);
   
    // x-axis
    g.drawLine(baseX, baseY, farX, baseY);
    g.drawChars(xlabel.toCharArray(), 0, xlabel.length(), width/2, baseY + 20); 
    // y-axis
    g.drawLine(baseX, baseY, baseX, farY);  
    g.drawChars(ylabel.toCharArray(), 0, ylabel.length(), 0, 10);
    
    // gridlines
    int value_per_segment = max_yValue / division;
    g.drawChars(String.valueOf(0).toCharArray(), 0, String.valueOf(0).length(), baseX-20, baseY);
    for (int val = value_per_segment; val <= max_yValue; val+=value_per_segment) {
      int val_height = (int)(height_per_unit * val);
      //g.setColor(Color.yellow);
      g.drawLine(baseX+1, baseY - val_height, farX, baseY - val_height);
      //g.setColor(Color.black);
      g.drawChars(String.valueOf(val).toCharArray(), 0, String.valueOf(val).length(), 0, baseY - val_height);
    }

    if (nodata != 0) {
      int pos;
      int value;
      for (int i = 0; i < nodata; i++) {
	
	value = ((Long)data.get(i)).intValue();
	if (value > max_yValue) {
	  // prevent bar drawing out of bound
	  pos = baseY - farY;
	} else {
	  pos = (int)(height_per_unit * value);
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


