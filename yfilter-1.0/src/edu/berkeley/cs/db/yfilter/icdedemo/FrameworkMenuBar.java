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

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class FrameworkMenuBar extends JMenuBar {
  Framework parent;  // the frame controls everything
  SystemMenu systemMenu;
  ModeMenu modeMenu;
  ParamMenu paramMenu;
  MonitorMenu monitorMenu;
  HelpMenu helpMenu;

  public FrameworkMenuBar(Framework frame) {
    parent = frame;
    systemMenu = new SystemMenu(parent);
    modeMenu = new ModeMenu(parent);
    paramMenu = new ParamMenu(parent);
    monitorMenu = new MonitorMenu(parent);
    helpMenu = new HelpMenu(parent);
    

    add(systemMenu);
    add(modeMenu);
    add(paramMenu);
    add(monitorMenu);
    add(helpMenu);

    setModeEnabled(false);
    setParamEnabled(false);
    setMonitorEnabled(false);
    //  setResultEnabled(false);
    //super.setHelpMenu(helpMenu);
  }

  protected class HelpMenu extends JMenu {
    Framework parent;
    JMenuItem clear;
    JMenuItem about;
    
    public HelpMenu(Framework frame) {
      super("Help");
      parent = frame;

      clear = new JMenuItem("Clear Display");
      clear.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.clearText();
	}
      });

      about = new JMenuItem("About");
      about.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  JOptionPane.showMessageDialog(parent, "YFilter Demo version 1.0", "About", JOptionPane.INFORMATION_MESSAGE);
	}
      });

      add(clear);
      addSeparator();
      add(about);
    }
  }

  public void setMonitorEnabled(boolean b) {
    monitorMenu.setEnabled(b);
  }

  public void setModeEnabled(boolean b) {
    modeMenu.setEnabled(b);
  }

  public void setDynamicUpdate(boolean b) {
    modeMenu.setDynamicUpdate(b);
  }

  public void setBatchMode(boolean b) {
    modeMenu.setBatchMode(b);
  }

  public void setParamEnabled(boolean b) {
    paramMenu.setEnabled(b);
  }

  protected class MonitorMenu extends JMenu {
    JMenuItem performance;
    JMenuItem system;
    JMenuItem workload;

    public MonitorMenu(Framework frame) {
      super("Monitor");
      parent = frame;

      performance = new JMenuItem("Performance");
      system = new JMenuItem("Workloads");
      workload = new JMenuItem("Statistics");

      performance.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.showPerformanceMonitor();
	}
      });
      
      system.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.showSystemMonitor();
	}
      });

      workload.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.showWorkloadMonitor();
	}
      });

      add(performance);
      add(system);
      add(workload);
    }
  }

  protected class ParamMenu extends JMenu {
    JMenuItem queryRate;
    JMenuItem queryBufferSize;
    JMenuItem xmlBufferSize;
    Framework parent;
    JMenuItem queryDepth;
    JMenuItem wildcard;
    JMenuItem dSlash;
    JMenuItem predProb;
    JMenuItem levelDist;
    JMenuItem nestedPath;
    JMenuItem xmlLevel;

    public ParamMenu(Framework frame) {
      super("Parameter");
      
      parent = frame;

      queryRate = new JMenuItem("Query Rate");
      queryBufferSize = new JMenuItem("Query Buffer Size");
      xmlBufferSize = new JMenuItem("XML Buffer Size");

      queryDepth = new JMenuItem("Query Depth");
      wildcard = new JMenuItem("Wildcard %");
      dSlash = new JMenuItem("DSlash %");
      predProb = new JMenuItem("Predicate %");
      //levelDist = new JMenuItem("LevelDist");
      nestedPath = new JMenuItem("Nested Path %");
      xmlLevel = new JMenuItem("XML Level");

      queryRate.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          parent.changeQueryRate();
        }
      });

      queryBufferSize.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeQueryBufferSize();
	}
      });
      
      xmlBufferSize.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeXMLBufferSize();
	}
      });
      
      queryDepth.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeQueryDepth();
	}
      });
      
      wildcard.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeWildcard();
	}
      });

      dSlash.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeDSlash();
	}
      });

      predProb.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changePredProb();
	}
      });

	/*
      levelDist.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeLevelDist();
	}
      });
	*/
      nestedPath.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeNestedPath();
	}
      });

      xmlLevel.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeXMLLevel();
	}
      });

      add(queryRate);
      add(queryBufferSize);
      add(xmlBufferSize);
      addSeparator();
      add(queryDepth);
      add(wildcard);
      add(dSlash);
      add(predProb);
      //add(levelDist);
      add(nestedPath);
      addSeparator();
      add(xmlLevel);
    }
  }
  
  protected class ModeMenu extends JMenu {
    JMenuItem dynamicUpdate;
    JMenuItem batchMode;
    Framework parent;

    public void setDynamicUpdate(boolean b) {
      if (b) {
	dynamicUpdate.setText("Disable Dynamic Update");
      } else {
	dynamicUpdate.setText("Enable Dynamic Update");
      }
    }

    public void setBatchMode(boolean b) {
      if (b) {
	batchMode.setText("One-XML Mode");
      } else {
	batchMode.setText("Batch Mode");
      }
    }

    public ModeMenu(Framework frame) {
      super("Mode");
      
      parent = frame;

      dynamicUpdate = new JMenuItem("Disable Dynamic Update");
      batchMode = new JMenuItem("Batch Mode");

      dynamicUpdate.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeDynamicUpdate();
	}
      });
      
      batchMode.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  parent.changeBatchMode();
	}
      });

      add(dynamicUpdate);
      add(batchMode);
    }
  }
  
  public void disableSelect() {
    systemMenu.setSelectEnabled(false);
  }

  public void enableRun() {
    systemMenu.setRunEnabled(true);
  }

  public void disableRun() {
    systemMenu.setRunEnabled(false);
  }

  public void enablePause() {
    systemMenu.setPauseEnabled(true);
  }
  
  public void disablePause() {
    systemMenu.setPauseEnabled(false);
  }

  protected class SystemMenu extends JMenu {
    Framework parent;
    JMenuItem select;
    JMenuItem run;
    JMenuItem pause;
    JMenuItem exit;

    public void setSelectEnabled(boolean b) {
      select.setEnabled(b);
    }

    public void setRunEnabled(boolean b) {
      run.setEnabled(b);
    }

    public void setPauseEnabled(boolean b) {
      pause.setEnabled(b);
    }

    public SystemMenu(Framework frame) {
      super("System");

      select = new JMenuItem("Select DTD");
      run = new JMenuItem("Run/Resume");
      pause = new JMenuItem("Pause");
      exit = new JMenuItem("Exit");

      parent = frame;
      select.setEnabled(true);
      run.setEnabled(false);
      pause.setEnabled(false);
      exit.setEnabled(true);

      select.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	  parent.selectDTDFile();
        }
      });
      
      run.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // run the system
	  parent.runDemo();
        }
      });
      
      pause.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // pause system
          parent.pauseDemo();
	}
      });

      exit.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  System.exit(0);
	}
      });

      add(select);
      add(run);
      add(pause);
      add(exit);
    }    
  }

}
