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

package edu.berkeley.cs.db.yfilterplus.queryparser;

import java.util.*;
import java.io.PrintWriter;

public class XFPath extends Path {

	/// a path consists of location steps with their predicates ///
	//protected String[] m_steps;
	
	// the index of the position of the first predicate
	// in the table m_predicates
	protected int[] m_stepFirstPredicate;
	 
	protected int m_noLocationSteps;

	/////////////////////////////////////
	//* constructor -- parse the path *//
	/////////////////////////////////////

	public XFPath(
		String rawPath,
		int branchPoint,
		int joinWith,
		int queryId,
		int pathId) {

		m_pathId = pathId;
		if (branchPoint == -1)
			m_branchingLevel = -1;
		m_joinWith = joinWith;

		/// replace "//" with "/$/" ///

		int bp = branchPoint;
		StringBuffer transformed = new StringBuffer();
		int len = rawPath.length();
		for (int i = 0; i < len; i++)
			if (rawPath.charAt(i) == '/'
				&& i < len - 1
				&& rawPath.charAt(i + 1) == '/') {
				transformed.append("/$/");
				i++;
				if (i < bp)
					bp++; // the branching point is moved
			} else {
				transformed.append(rawPath.charAt(i));
			}
		//System.out.println("transformed: "+transformed);
		//System.out.println("branching point is "+bp);

		/// find the new branching level for a nested path ///
		/// e.g. turning it from an index to a level after ///
		/// the conversion from //a//b to /$/a/$/b         ///

		if (m_branchingLevel > -1) {
			int blevel = -1;
			for (int i = 0; i < bp; i++)
				if (transformed.charAt(i) == '/')
					blevel++;
				else if (transformed.charAt(i) == '$')
					blevel--;
			m_branchingLevel = blevel;
		}

		/// tokenize the string by '/' and    ///
		/// for each step, extract predicates ///

		StringTokenizer st = new StringTokenizer(transformed.toString(), "/");
		int steps = st.countTokens();
		//System.out.println(steps+" steps");
		m_steps = new String[steps];
		m_stepFirstPredicate = new int[steps];
		Vector predicates = new Vector();
		int i = 0; // index for steps;
		int j = 0; // index for the predicate table;
		int j_pre;
		String rawStep;
		int dslashCount = 0; // added to modify the branching level
		while (st.hasMoreTokens()) {
			rawStep = st.nextToken();
			//System.out.println("step "+i+": "+rawStep);
			if (rawStep.equals("$")) { // just "//"
				m_steps[i] = rawStep.intern();
				m_stepFirstPredicate[i] = -1;
				// no predicate
				dslashCount++;
				// added to reduce branching level by skipping "/$/"
			} else { // a normal step
				int k = 0;
				int steplen = rawStep.length();
				while (k < steplen && rawStep.charAt(k) != '[')
					k++;
				m_steps[i] = (rawStep.substring(0, k)).intern();
				//System.out.println("element: "+m_steps[i]);
				if (k >= steplen) { // no predicate
					m_stepFirstPredicate[i] = -1;
				} else { // there are predicates
					int k_pre;
					String rawPredicate;
					j_pre = j; // the predicate table will grow
					do {
						k_pre = k;
						while (k < steplen && rawStep.charAt(k) != ']')
							k++;
						if (k >= steplen) {
							System.out.println(
								"XFPath::XFpath -- Error in raw step parsing: ] exptected.");
							return;
						}
						rawPredicate = rawStep.substring(k_pre, ++k);
						//System.out.println("predicate: "+rawPredicate);
						predicates.add(
							new XFPredicate(
								queryId,
								pathId,
								i - dslashCount,
								rawPredicate));
						// now branching level equals the location step number
						j++;
					}
					while (k < steplen && rawStep.charAt(k) == '[');
					// get all predicates at this location step
					m_stepFirstPredicate[i] = j_pre;
				}
			}
			i++;
		}

		m_noLocationSteps = m_steps.length - dslashCount;

		m_predicates = new XFPredicate[predicates.size()];
		predicates.copyInto(m_predicates);
		//print();

		// Limitation: currently we only support positional predicates
		// if they are the first predicate in their location steps
		boolean first = false;
		int level, prevLevel = -1;
		for (i=0; i<m_predicates.length; i++) {
			level = m_predicates[i].getLevel();			
			if (level > prevLevel)
				first = true;
			else
				first = false;
			prevLevel = level;
			if (m_predicates[i].getType() == 'p' && !first)
				try {
					throw new Exception("XPath::XPath -- positional predicates unsupported!");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
		}
		//System.out.println("The end of path parsing!");
	}

	public static String[] parseSteps(String rawPath) {

		/// replace "//" with "/$/" ///

		StringBuffer transformed = new StringBuffer();
		int len = rawPath.length();
		for (int i = 0; i < len; i++)
			if (rawPath.charAt(i) == '/'
				&& i < len - 1
				&& rawPath.charAt(i + 1) == '/') {
				transformed.append("/$/");
				i++;
			} else {
				transformed.append(rawPath.charAt(i));
			}

		/// tokenize the string by '/' and    ///
		/// for each step, extract predicates ///

		StringTokenizer st = new StringTokenizer(transformed.toString(), "/");
		int noSteps = st.countTokens();
		String[] steps = new String[noSteps];
		ArrayList predicates = new ArrayList();
		int i = 0; // index for steps;
		int j = 0; // index for the predicate table;
		int j_pre;
		String rawStep;
		int dslashCount = 0; // added to modify the branching level
		while (st.hasMoreTokens()) {
			rawStep = st.nextToken();
			if (rawStep.equals("$")) { // just "//"
				steps[i] = rawStep.intern();
			} else { // a normal step
				int k = 0;
				int steplen = rawStep.length();
				while (k < steplen && rawStep.charAt(k) != '[')
					k++;
				steps[i] = (rawStep.substring(0, k)).intern();
				if (k >= steplen) { // no predicate
					;
				} else { // there are predicates
					int k_pre;
					String rawPredicate;
					j_pre = j; // the predicate table will grow
					do {
						k_pre = k;
						while (k < steplen && rawStep.charAt(k) != ']')
							k++;
						if (k >= steplen) {
							System.out.println(
								"Error in raw step parsing: ] exptected.");
							return null;
						}
						//rawPredicate = rawStep.substring(k_pre, ++k);
						//predicates.add(rawPredicate);  
						// now branching level equals the location step number
						j++;
					}
					while (k < steplen && rawStep.charAt(k) == '[');
					// get all predicates at this location step
				}
			}
			i++;
		}
		return steps;
	}

	/////////////////////
	//* retrieve data *//
	/////////////////////

	public String[] getSteps() {
		return m_steps;
	}

	// this is a dangerous call. it may change the steps but not 
	// modify the predicates so that predicates and steps become mismatch
	// it is only used when there is a single predicate attached to the last
	// location step and it doesnot care about the level information
	public void setSteps(String[] steps, int levelGrow) {
		//System.out.println("XFPath::setSteps is called.");
		//System.out.println("old steps: "+Debug.toString(m_steps));
		//System.out.println("new steps: "+Debug.toString(steps));

		m_steps = steps;

		//System.out.println("old step length is "+m_noLocationSteps);
		m_noLocationSteps += levelGrow;
		//System.out.println("new step length is "+m_noLocationSteps);
	}

	public int[] getStepFirstPredicate() {
		return m_stepFirstPredicate;
	}

	public Predicate getLastPredicate() {
		int len = m_predicates.length;
		if (len > 0)
			return m_predicates[len - 1];
		return null;
	}

	public int getNoLocationSteps() {
		return m_noLocationSteps;
	}

	//////////////////
	//* for output *//
	//////////////////

	public String toString() {
		StringBuffer s = new StringBuffer();

		s.append("Path ");
		s.append(m_pathId);
		s.append("\n");

		int len = m_steps.length;
		for (int i = 0; i < len; i++) {
			s.append("(");
			s.append(m_steps[i]);
			//s.append(",");
			//s.append(m_stepFirstPredicate[i]);
			s.append(")");
			if (i < len - 1)
				s.append("\t");
			else
				s.append("\n");
		}

		len = m_predicates.length;
		//s.append(len);
		s.append("Predicate(s)\n");
		for (int i = 0; i < len; i++) {
			s.append(m_predicates[i].toString());
			s.append("\n");
		}

		s.append("Branching level :");
		s.append(m_branchingLevel);
		s.append("\n");
		
		s.append("Join with path ");
		s.append((m_joinWith == -1 ? -1 : (m_joinWith + 1)));
		s.append("\n");

		return s.toString();
	}

	public void printStepElementsToFile(PrintWriter outFile) {
		outFile.print("Path" + m_pathId + " ");

		int len = m_steps.length;
		for (int i = 0; i < len; i++) {
			outFile.print(m_steps[i] + " ");
		}
		outFile.println();
	}
}
