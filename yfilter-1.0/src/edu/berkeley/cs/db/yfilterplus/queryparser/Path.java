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

public abstract class Path {
	protected int m_queryId; 	// a path should also have its query ID
	protected int m_pathId;		// the path id starts from 0 (0 is for the main path)

	protected String[] m_steps;	// a sequence of element names including '$' symbols for "//"
	
	// an array of predicates in this path
	protected Predicate[] m_predicates;	
	
	// for nested paths
	protected int m_branchingLevel;
	// the query generator only generates a branch after an element or * on a path
	// in XML matching process, the level that it is matched to is dynamically determined
	// a unique id of the instantiating element must be available for this branching level
	protected int m_joinWith;

	////////////////////////////
	/// retrieve information ///
	////////////////////////////

	public int getQueryId() {
		return m_queryId;
	}

	public int getPathId() {
		return m_pathId;
	}

	public Predicate[] getPredicates() {
		return m_predicates;
	}

	public boolean hasPredicates() {
		return m_predicates.length > 0 ? true : false;
	}

	public int getNoPredicates() {
		return getPredicates().length;
	}

	public int getBranchingLevel() {
		return m_branchingLevel;
	}

	public int getJoinWith() {
		return m_joinWith;
	}

	////////////////////////
	/// abstract classes ///
	////////////////////////

	public abstract String[] getSteps();

	public abstract String toString();
	
	//public abstract void print();
	//public abstract void printToFile(PrintWriter outFile);
	public abstract void printStepElementsToFile(PrintWriter outFile);
}
