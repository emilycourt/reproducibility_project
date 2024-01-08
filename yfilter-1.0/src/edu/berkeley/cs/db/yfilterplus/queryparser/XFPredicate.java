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

public class XFPredicate extends Predicate {
	public XFPredicate(int queryId, int pathId, int level, String raw)  {
		m_queryId = queryId;
		m_pathId = pathId;
		m_level = level;
		
		// ignore the bracket [...]
		//System.out.println(raw);
		if (raw.charAt(1) == '@') { //[@type=12]
			m_predicateType = 'a';
			m_operator = SimplePredicate.OPERATOR_EQ;
			
			int len = raw.length()-1;
			int k = 2;
			while (k<len && raw.charAt(k)!='=') 
				k++;
			m_attrName = (raw.substring(2, k)).intern();
			
			//System.out.println(raw.substring(k, len));
			if (k < len) {	// there is a value
				if (raw.charAt(k+1) == '"')
					m_stringValue = raw.substring(k+2, len-1);
				else
					//m_value = Integer.parseInt(raw.substring(k+1, len));
					m_stringValue = raw.substring(k+1, len);
			}
			else
				//m_value = -1;
				m_stringValue = null;
		}
		else if (raw.startsWith("[text()") || 
			 raw.charAt(1)=='.' && raw.charAt(2)!='/' ) { 
			m_predicateType = 'd';
			m_attrName = "TEXT";
			m_operator = SimplePredicate.OPERATOR_EQ;
			
			if (raw.startsWith("[.")) {// it has to contain a value
				if (raw.charAt(3) == '"')
					m_stringValue = raw.substring(4, raw.length()-2);
				else
			    	//m_value = Integer.parseInt(raw.substring(3, raw.length()-1));
					m_stringValue = raw.substring(3, raw.length()-1);
			}
			else {	// text() can just check the existence of a text node
			    if (raw.length() <= 9)
			    	//m_value = -1;
			    	m_stringValue = null;
			    else {
			    	if (raw.charAt(8) == '"')
						m_stringValue = raw.substring(9, raw.length()-2);
					else
			    		//m_value = Integer.parseInt(raw.substring(8, raw.length()-1));
						m_stringValue = raw.substring(8, raw.length()-1);
			    }
			}
		} else if (raw.charAt(1)>='0'&&raw.charAt(1)<='9' || 
				   raw.startsWith("position()", 1)) {
			// for position, the operators can be =, !=, <, >, <=, or >=
			m_predicateType = 'p';
			m_attrName = "POS";
			
			if (raw.charAt(1)>='0'&&raw.charAt(1)<='9') {
				m_operator = SimplePredicate.OPERATOR_EQ;
				m_value = Integer.parseInt(raw.substring(1, raw.length()-1));
			}
			else { // it can be any operator, specified after "[position()"
				if (raw.charAt(12)>='0' && raw.charAt(12)<='9') {
					// =, <, >
					String op = (raw.substring(11, 12)).intern();
					if (op.equals("="))
						m_operator = SimplePredicate.OPERATOR_EQ;
					else if (op.equals("<"))
						m_operator = SimplePredicate.OPERATOR_LT;
					else if (op.equals(">"))
						m_operator = SimplePredicate.OPERATOR_GT;
					m_value = Integer.parseInt(raw.substring(12, raw.length()-1));
				}
				else {
					// !=, <=, >=
					String op = (raw.substring(11, 13)).intern();
					if (op.equals("!="))
						m_operator = SimplePredicate.OPERATOR_NE;
					else if (op.equals("<="))
						m_operator = SimplePredicate.OPERATOR_LE;
					else if (op.equals(">="))
						m_operator = SimplePredicate.OPERATOR_GE;
					m_value = Integer.parseInt(raw.substring(13, raw.length()-1));
				}
			}
		}
		else{
			m_predicateType = 'u';
			m_value = -1;
		}  
		//System.out.println("Predicate type: "+m_predicateType);
		
	}
	
	public XFPredicate(Predicate p) {
		m_queryId = p.getQueryId();
		m_pathId = p.getPathId();
		m_level = p.getLevel();	
		m_predicateType = p.getType(); 
		m_attrName = p.getTargetName();
		m_operator = p.getOperator();
		if (m_predicateType == 'p')
			m_value = p.getValue();
		else 
			m_stringValue = p.getStringValue();
	}	
}
