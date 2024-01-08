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

import java.io.PrintWriter;

public class XPPath extends Path {

	protected int m_noLocationSteps;

	///////////////////
	/// constructor ///
	///////////////////

	public XPPath(
		String[] steps,
		Predicate[] predicates,
		int branchingLevel,
		int joinWith) throws Exception {

		m_steps = steps;
		m_predicates = predicates;
		m_branchingLevel = branchingLevel;
		m_joinWith = joinWith;

		int dslashCount = 0;
		for (int i=0; i<steps.length; i++)
			if (steps[i].equals("$"))
				dslashCount++;
		m_noLocationSteps = m_steps.length - dslashCount;

		// Limitation: currently we only support positional predicates
		// if they are the first predicate in their location steps
		boolean first = false;
		int level, prevLevel = -1;
		for (int i=0; i<predicates.length; i++) {
			level = predicates[i].getLevel();			
			if (level > prevLevel)
				first = true;
			else
				first = false;
			prevLevel = level;
			if (predicates[i].getType() == 'p' && !first)
				throw new Exception("XPath::XPath -- positional predicates unsupported!");
		}
		
	}

	//////////////
	// setters ///
	//////////////

	public void setQueryId(int id) {
		m_queryId = id;
		for (int i=0; i<m_predicates.length; i++)
			((XPPredicate)m_predicates[i]).setQueryId(id);
	}

	public void setPathId(int id) {
		m_pathId = id;
		for (int i=0; i<m_predicates.length; i++)
			((XPPredicate)m_predicates[i]).setPathId(id);
	}
	
	//////////////
	// getters ///
	//////////////

	public String[] getSteps() {
		return m_steps;
	}

	//public int[] getStepFirstPredicate() {
	//	return m_stepFirstPredicate;
	//}

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
	/// for output ///
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
		s.append(len);
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
