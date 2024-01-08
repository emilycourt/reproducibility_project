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

import edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser.SimplePredicate;
import java.io.PrintWriter;

public abstract class Predicate {
	protected int m_queryId;
	protected int m_pathId;
	// the level is used just to identify the level in a path
	protected int m_level;
	// 'a' for attribute, 'd' for data,
	// 'p' for position, and 'u' for unknown
	// adding a new predicate type needs changing
	// (a) XFQuery.java to recognize this predicate
	// (b) this predicate constructor, and
	// (c) queryEvaluation of this predicate
	protected char m_predicateType;
	//	for an attribute, it stores the attr name
	//	for text data, it stores "TEXT"
	//	for position, it stores "POS"
	protected String m_attrName;
	//	type of operator
	protected char m_operator;
	// value to be compared to
	protected int m_value = -1;		
	protected String m_stringValue;

	//////////////////////////////
	//* setting member variables *//
	//////////////////////////////

	public void setLevel(int level) {
		m_level = level;
	}

	public void updateLevel(int offSet) {
		m_level += offSet;
	}

	//////////////////
	//* for output *//
	//////////////////

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(m_queryId);
		s.append("\t");
		s.append(m_pathId);
		s.append("\t");				
		s.append(m_level);
		s.append("\t");		
		s.append(m_predicateType);
		s.append("\t");				
		if (m_predicateType == 'a')
			s.append(m_attrName);
		else if (m_predicateType == 'd')
			s.append("text()");
		else if (m_predicateType == 'p')
			s.append("position()");
		s.append("\t");
		String op = SimplePredicate.getOperatorString(m_operator);
		if (op != null) { 
			s.append(op);
			s.append("\t");
		}		
		if (m_value != -1)
			s.append(m_value);
		else if (m_stringValue != null) {
			s.append("\"");
			s.append(m_stringValue);
			s.append("\"");
		}				
		return s.toString();
	}

	public void print() {
		System.out.println(toString());
	}
	
	public void printToFile(PrintWriter out) {
		out.println(toString());
	}

	/////////////////////
	//* retrieve data *//
	/////////////////////

	public int getLevel() {
		return m_level;
	}

	public int getQueryId() {
		return m_queryId;
	}

	public int getPathId() {
		return m_pathId;
	}

	public char getType() {
		return m_predicateType;
	}

	public String getAttrName() {
		if (m_predicateType == 'a')
			return m_attrName;
		return null;
	}

	public String getTargetName() {
		return m_attrName;
	}
	
	public char getOperator() {
		return m_operator;
	}
	
	public String getOperatorString() {
		return SimplePredicate.getOperatorString(m_operator);
	}

	public int getValue() {		
		return m_value;
	}
	
	public String getStringValue() {
		if (m_stringValue != null)
			return m_stringValue;
		else if (m_value != -1)
			return String.valueOf(m_value);
		return null;	
	}
}
