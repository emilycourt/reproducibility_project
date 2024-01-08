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

public class SimplePredicate {
	
	public static char PREDICATE_TEXT = 'd';
	public static char PREDICATE_ATTRIBUTE = 'a';
	public static char PREDICATE_POSITION = 'p';
	public static char PREDICATE_UNKNOWN = 'u';

	public static char OPERATOR_EQ = '1';
	public static char OPERATOR_NE = '2';
	public static char OPERATOR_GT = '3';
	public static char OPERATOR_GE = '4';
	public static char OPERATOR_LT = '5';
	public static char OPERATOR_LE = '6';
	
	char m_type;
	String m_attrName;
	char m_operator;
	Object m_value; // String or Integer
	
	/**
	 * simple_predicate ::= AT qname EQUALS literal
	 * @param type: predicate type
	 * @param targetName: attribute name
	 * @param operator: eq
	 * @param value: literal
	 */
	public SimplePredicate(char type, String attrName, char operator, String value) {
		m_type = type;	
		m_attrName = attrName;
		m_operator = operator;
		m_value = value;
	}
	
	/**
	 * simple_predicate ::= number
	 * @param type: predicate type
	 * @param value: position
	 */
	public SimplePredicate(char type, Integer value) {
		m_type = type;
		m_operator = OPERATOR_EQ;
		m_value = value;
	}
	
	/**
	 * simple_predicate ::= fn_position comparator number
	 * @param type: predicate type
	 * @param operator: comparison operator
	 * @param value: position
	 */
	public SimplePredicate(char type, char operator, Integer value) {
		m_type = type;
		m_operator = operator;
		m_value = value;
	}
	
	/**
	 * simple_predicate ::= fn_text EQUALS literal
	 * @param type
	 * @param operator
	 * @param value
	 */
	public SimplePredicate(char type, char operator, String value) {
		m_type = type;
		m_operator = operator;
		m_value = value;
	}
	
	///////////////
	/// getters ///
	///////////////
	
	public char getPredicateType() {
		return m_type;
	}
	
	public String getAttributeName() {
		return m_attrName;
	}
	
	public char getOperator() {
		return m_operator;
	}
	
	public Object getValue() {
		return m_value;
	}
	
	//////////////
	/// output ///
	//////////////
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if (m_type == PREDICATE_ATTRIBUTE) {
			sb.append("@");
			sb.append(m_attrName);			
			sb.append(getOperatorString(m_operator));
			sb.append(m_value);
		}
		else if (m_type == PREDICATE_POSITION) {
			sb.append("position()");
			sb.append(getOperatorString(m_operator));
			sb.append(m_value);
		}
		else if (m_type == PREDICATE_TEXT) {
			sb.append("text()");
			sb.append(getOperatorString(m_operator));
			sb.append(m_value);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String getOperatorString(char operator) {
		String op = null;
		switch (operator) {
			case '1' :
				op = "=";
				break;
			case '2' :
				op = "!=";
				break;
			case '3' :
				op = ">";
				break;
			case '4' :
				op = ">=";
				break;
			case '5' :
				op = "<";
				break;
			case '6' :
				op = "<=";
				break;
			default :
				;//System.out.println("SimplePredicate::toString -- unknown operator!");
		}
		return op;
	}
}