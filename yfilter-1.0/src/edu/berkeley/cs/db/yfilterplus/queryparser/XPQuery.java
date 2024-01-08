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

import edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser.*;
import java.io.*;

public class XPQuery extends Query {
	protected Path[] m_paths;
	protected char m_extraSelectType;
	protected String m_extraSelectAttr;

	///////////////////
	/// constructor ///
	///////////////////

	public XPQuery(Path[] paths) {
		m_paths = paths;
		m_extraSelectType = 'u';
		
		m_hasPredicates = false;
		m_hasNestedPaths = false;
		int len = paths.length;
		for (int i = 0; i < len; i++) { 
			// set the path id
			((XPPath)m_paths[i]).setPathId(i);		
			// set the annotation for having predicates
			if (m_paths[i].hasPredicates()) {			
				m_hasPredicates = true;				
			}
		}
		if (len > 1)
			m_hasNestedPaths = true;
	}
	
	public XPQuery(Path[] paths, char extraSelectType, String extraSelectAttr) {
		m_paths = paths;
		m_extraSelectType = extraSelectType;
		m_extraSelectAttr = extraSelectAttr;
		
		m_hasPredicates = false;
		m_hasNestedPaths = false;
		int len = paths.length;
		for (int i = 0; i < len; i++) { 
			// set the path id
			((XPPath)m_paths[i]).setPathId(i);		
			// set the annotation for having predicates
			if (m_paths[i].hasPredicates()) {			
				m_hasPredicates = true;				
			}
		}
		if (len > 1)
			m_hasNestedPaths = true;
	}

	///////////////
	/// setters ///
	///////////////
	
	public void setQueryId(int id) {
		m_queryId = id;
		int len = m_paths.length;
		for (int i = 0; i < len; i++) { 
			// set the path id
			((XPPath)m_paths[i]).setQueryId(id);					
		}
	}
	  
	///////////////
	/// getters ///
	///////////////

	public Path[] getPaths() {
		return m_paths;
	}

	public int getNoPaths() {
		return m_paths.length;
	}

	public int getNoPredicates() {
		int len = m_paths.length;
		int sum = 0;
		for (int i = 0; i < len; i++)
			sum += m_paths[i].getNoPredicates();
		return sum;
	}

	public boolean hasExtraSelect() {
		return m_extraSelectType != 'u';
	}
	
	public char getExtraSelectType() {
		return m_extraSelectType;
	}

	public String getExtraSelectAttribute() {
		return m_extraSelectAttr;
	}
	
	//////////////////
	/// for otuput ///
	//////////////////

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Query ");
		s.append(m_queryId);
		s.append(": \n");

		for (int i = 0; i < m_paths.length; i++) {		
			s.append(m_paths[i].toString());
		}
		
		if (m_extraSelectType != 'u') {
			s.append("Extra select:\t");
			if (m_extraSelectType == SimplePredicate.PREDICATE_ATTRIBUTE) {
				s.append("@");
				s.append(m_extraSelectAttr);
			} else if (m_extraSelectType == SimplePredicate.PREDICATE_TEXT) {
				s.append("text()");
			}
			s.append("\n");
		}
		return s.toString();
	}

	public void print() {
		System.out.println(this.toString());
	}

	public void printToFile() {
		try {
			PrintWriter fileOut;
			if (m_queryId == 1)
				fileOut =
					new PrintWriter(new FileOutputStream("parsedQueries.txt"));
			else
				fileOut = 
					new PrintWriter(
						new FileOutputStream("parsedQueries.txt", true));
			
			fileOut.println(this.toString());
			
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printStepElementsToFile() {
		try {
			PrintWriter fileOut;
			if (m_queryId == 1)
				fileOut =
					new PrintWriter(new FileOutputStream("stepElements.txt"));
			else
				fileOut =
					new PrintWriter(
						new FileOutputStream("stepElements.txt", true));
			fileOut.println("Query " + m_queryId);

			int len = m_paths.length;
			for (int i = 0; i < len; i++) {
				m_paths[i].printStepElementsToFile(fileOut);
			}
			fileOut.println();
			fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////
	/// create a new Query from string ///
	//////////////////////////////////////
	
	public static Query parseQuery(String line, int queryId) {
		StringReader queryInput = new StringReader(line);
		XPathParser p = new XPathParser(queryInput);
		java_cup.runtime.Symbol s = null;
		try {
			s = p.parse();
		} catch (Exception e) {
			System.out.println("Exception while parsing: " + e.toString());
			e.printStackTrace();
		}

		XPQuery compiled_query = null;
		if (s != null && s.value != null) {
			PathQuery parsed_query = (PathQuery)s.value;		
			try {			
				compiled_query = XPQueryParser.compile(parsed_query);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			compiled_query.setQueryId(queryId);
		}
		return compiled_query;
	}
}
