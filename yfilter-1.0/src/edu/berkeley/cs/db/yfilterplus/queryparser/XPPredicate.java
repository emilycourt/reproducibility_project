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

public class XPPredicate extends Predicate {
	
	public XPPredicate(int level, SimplePredicate p) {
		m_level = level;	
		m_predicateType = p.getPredicateType(); 
		if (m_predicateType == 'a') {
			m_attrName = p.getAttributeName();
			m_operator = p.getOperator();
			m_stringValue = (String)p.getValue();
		}
		else if (m_predicateType == 'd') {
			m_attrName = "TEXT";
			m_operator = p.getOperator();
			m_stringValue = (String)p.getValue();
		}
		else if (m_predicateType == 'p') {
			m_attrName = "POS";
			m_operator = p.getOperator();
			m_value = ((Integer)p.getValue()).intValue();
		}			
	}	
	
	/**
	 * construct a predicate that only checks the existence 
	 * of an attribute or child text nodes
	 */

	public XPPredicate(int level, char type, String attrName) {
		m_level = level;	
		m_predicateType = type; 
		if (m_predicateType == 'a')
			m_attrName = attrName;					
		else if (m_predicateType == 'd')
			m_attrName = "TEXT";		
		else 
			System.out.println("XPPredicate::XPPredicate -- illegal type for this constructor!");
	}	
	
	///////////////
	/// setters ///
	///////////////
	
	public void setQueryId(int id) {
		m_queryId = id;
	}
	
	public void setPathId(int id) {
		m_pathId = id;
	}
}
