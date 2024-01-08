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
import javax.swing.*;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;

public final class XMLViewer extends JFrame {
  JTextArea textarea;
  JButton search;
  JButton save;
  int caretPos = 0;
  int stepPos = 0;
  
  public XMLViewer(String title) {
    super(title);
      
    textarea = new JTextArea("XML document is not set!", 30, 25);
    textarea.setLineWrap(true);
    textarea.setMargin(new Insets(5,5,5,5));
    textarea.setEditable(false);
    textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
    JScrollPane scrollPane = new JScrollPane(textarea);
    
    search = new JButton("Search");
    search.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	searchText();
      }
    });

    save = new JButton("Save");
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	saveText();
      }
    });

    JPanel controlPanel = new JPanel();
    TitledBorder controlTitle = BorderFactory.createTitledBorder("");
    controlTitle.setTitleJustification(TitledBorder.CENTER);
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setBorder(controlTitle);
    controlPanel.add(save);
    controlPanel.add(search);

    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setPreferredSize(new Dimension(800,600));
    pane.add(controlPanel);
    pane.add(scrollPane);
    setContentPane(pane);
    pack();
  }	

  public void show(String xml) {
    setXML(xml);
    super.show();
  }
   
  public void setXML(String xml) {
    if (xml == null) {
      textarea.setText("Document is not set!");
    } else {
      textarea.setText(xml);
    }
  }

  public void saveText() {
    final JFileChooser fc = new JFileChooser();
    
    int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      try {
	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	bw.write(textarea.getText(), 0, textarea.getText().length());
	bw.close();
      } catch (IOException ioe) {
	ioe.printStackTrace();
      }
    }
  }

  public void search(String elementName) {
    int res;
    String xmldoc = textarea.getText();
    res = xmldoc.indexOf("<"+elementName, stepPos);
    if (res == -1) {
      stepPos = 0;
      res = xmldoc.indexOf("<" + elementName, stepPos);
    }
    textarea.requestFocus();
    textarea.setSelectionColor(Color.blue);
    textarea.setSelectedTextColor(Color.yellow);
    textarea.setCaretPosition(res+1);
    textarea.moveCaretPosition(res+elementName.length()+1);
    stepPos = res+elementName.length()+1;
  }

  public void searchText() {
    int res;
    String xmldoc = textarea.getText();
    String str = JOptionPane.showInputDialog(this, "Please enter a string to search");
    if (str == null) {
      return;
    }
    res = xmldoc.indexOf(str, caretPos);
    if (res == -1) {
      JOptionPane.showMessageDialog(this, "String not found", "Not found", JOptionPane.INFORMATION_MESSAGE);
      caretPos = 0;
      return;
    }
    textarea.requestFocus();
    textarea.setSelectionColor(Color.blue);
    textarea.setSelectedTextColor(Color.yellow);
    textarea.setCaretPosition(res);
    textarea.moveCaretPosition(res+str.length());
    //    textarea.select(res, res+str.length());
    caretPos = res+str.length();
  }

}      
     
     
      
