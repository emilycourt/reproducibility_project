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


package edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser;

import java.util.*;

public class StepExpression {
	public static char UNKNOWN = 'u';
	public static char CHILD = 'c';
	public static char DESCENDANT = 'd';
	
	char m_axis				= UNKNOWN;
	String m_nameTest		= null;
	ArrayList m_predicates 	= null;
	
	public StepExpression(String nameTest) {
		m_nameTest = nameTest;
	}
	
	public StepExpression(String nameTest, ArrayList predicates) {
		m_nameTest = nameTest;
		m_predicates = predicates;	
	}
	
	public void setAxis(char axis) {
		m_axis = axis;
	}
	
	///////////////
	/// getters ///
	///////////////
	
	public char getAxis() {
		return m_axis;
	}
	
	public String getNameTest() {
		return m_nameTest;
	}
	
	public ArrayList getPredicates() {
		return m_predicates;
	}
	
	////////////////
	/// toString ///
	////////////////
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (m_axis == CHILD)
			sb.append("/");
		else if (m_axis == DESCENDANT) 
			sb.append("//");
		else if (m_axis == UNKNOWN)
			sb.append("?");
		sb.append(m_nameTest);
		// 07/08/04 anil check if predicates exist
		int size = 0;
		if (m_predicates != null)
		    size = m_predicates.size();
		for (int i=0; i<size; i++)
			sb.append(m_predicates.get(i));
		return sb.toString();
	}
}
