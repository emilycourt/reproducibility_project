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

public class PathQuery {

	ArrayList m_stepExprs       = null;
	char m_extraSelectType      = SimplePredicate.PREDICATE_UNKNOWN;
	String m_extraSelectAttr    = null;
	 		
	public PathQuery(StepExpression expr) {
		m_stepExprs = new ArrayList();
		m_stepExprs.add(expr);
	}
	
	public void addStepExpr(StepExpression expr) {
		m_stepExprs.add(expr);
	}
	
	public void addExtraAttrSelect(String attrName) {
		m_extraSelectType = SimplePredicate.PREDICATE_ATTRIBUTE;
		m_extraSelectAttr = attrName;
	}
	
	public void addExtraTextSelect() {
		m_extraSelectType = SimplePredicate.PREDICATE_TEXT;
	}
	
	///////////////
	/// getters ///
	///////////////
	
	public ArrayList getStepExpressions() {
		return m_stepExprs;
	}
	
	public char getExtraSelectType() {
		return m_extraSelectType;
	}
	
	public String getExtraSelectAttribute() {
		return m_extraSelectAttr;
	}
	
	////////////////
	/// toString ///
	////////////////
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int size = m_stepExprs.size();
		for (int i=0; i<size; i++)
			sb.append(m_stepExprs.get(i));
		if (m_extraSelectType == SimplePredicate.PREDICATE_ATTRIBUTE) {
			sb.append("/@");
			sb.append(m_extraSelectAttr);
		}
		else if (m_extraSelectType == SimplePredicate.PREDICATE_TEXT) {
			sb.append("/text()");
		}
		return sb.toString();
	}
}