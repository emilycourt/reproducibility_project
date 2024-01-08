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

package edu.berkeley.cs.db.yfilter.operators;

import edu.berkeley.cs.db.yfilter.filter.*;
import edu.berkeley.cs.db.yfilterplus.xmltree.ParsingContext;

import java.util.*;
import java.io.*;

public class ResultCollection {

	/// query results ///
	public static BitSet m_queryEval = null;
	public static ArrayList m_matchedQueries = null;
	public static ArrayList m_matchingElements = null;
	
	/// processing context ///
	public static ParsingContext m_currentElement = null;
	
	///////////////
	/// prepare ///
	///////////////
	
	public static void prepare(int noQueries) {
		if (m_queryEval == null)
			m_queryEval = new BitSet(noQueries);
	 	else {
			//int size = m_queryEval.size();
			//if (size < noQueries)
			//	m_queryEval = new BitSet(noQueries);
			//else
				m_queryEval.clear();
	 	}

		/// case 1, 2: boolean results
		if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_NONE
			|| SystemGlobals.outputLevel == SystemGlobals.OUTPUT_BOOLEAN)
			m_matchingElements = null;
		else {
			/// case 2, 3: one or all elements
			if (m_matchingElements == null) {
				m_matchingElements = new ArrayList(noQueries);
				for (int i = 0; i < noQueries; i++)
					m_matchingElements.add(null);
			} else {
				int size = ResultCollection.m_matchingElements.size();
				for (int i = 0; i < size; i++) {
					Object ob = m_matchingElements.get(i);
					if (ob != null) {
						if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL
							&& ob instanceof ArrayList)
							 ((ArrayList) ob).clear();
						else
							m_matchingElements.set(i, null);
					}
				}
				if (size < noQueries) {
					// allocate more cells to match up with all the queries
					for (int i = size; i < noQueries; i++)
						m_matchingElements.add(null);
				}
			}
		}	       			
	}
	
	/////////////////////////
	/// clear the results ///
	/////////////////////////
	
	public static void clear() {
		if (m_queryEval != null)			
			m_queryEval.clear();
		
		if (m_matchingElements != null) {
			int size = m_matchingElements.size();
			for (int i=0; i<size; i++) {
				Object ob = m_matchingElements.get(i);
				if (ob != null) {
					if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL
						&& ob instanceof ArrayList)
						 ((ArrayList) ob).clear();
					else
						m_matchingElements.set(i, null);
				}	
			}
		}		
	}
	
	//////////////////////////
	/// collection results ///
	//////////////////////////
	
	public static void collect(int queryId, ArrayList pathMatches) {
		m_queryEval.set(queryId-1);		
		if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE) {
			ArrayList pathMatch = (ArrayList)pathMatches.get(0);
			ParsingContext c = (ParsingContext)pathMatch.get(pathMatch.size()-1);
			m_matchingElements.set(queryId-1, c);			
		}
		else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {
			ArrayList results = (ArrayList)m_matchingElements.get(queryId-1);
			if (results == null) {			
				results = new ArrayList(10);
				m_matchingElements.set(queryId-1, results);
			}
			int size = pathMatches.size();
			for (int i=0; i<size; i++) {
				ArrayList pathMatch = (ArrayList)pathMatches.get(i);
				ParsingContext c = (ParsingContext)pathMatch.get(pathMatch.size()-1);
				appendWithDupElim(results, c);
			}
		}	
	}

    private static void appendWithDupElim(ArrayList results, ParsingContext next) {
        int size = results.size();
        if (size == 0)
            results.add(next);
        else {
            ParsingContext current = (ParsingContext)results.get(size-1);
            if (current.compareTo(next) < 0)
                results.add(next);
        }
    }

	public static void collectCurrentElement(int queryId) {
		m_queryEval.set(queryId-1);	
		if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE)
			m_matchingElements.set(queryId-1, m_currentElement);
		else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {		
			ArrayList results = (ArrayList)m_matchingElements.get(queryId-1);
			if (results == null) {			
				results = new ArrayList(10);
				m_matchingElements.set(queryId-1, results);
			}
            results.add(m_currentElement);  // guaranteed duplicates free
		}
	}
		
	public static void extraSelect(int queryId, char type, String target) {
		if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE) {  
			ParsingContext c = (ParsingContext)m_matchingElements.get(queryId-1);
			String result = null;
			if (type == 'a')
				result = c.getAttributeValue(target);
			else if (type == 'd')
				result = c.getChildTextData();
			m_matchingElements.set(queryId-1, result);
		}
		else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {		
			ArrayList results = (ArrayList)m_matchingElements.get(queryId-1);
			int size = results.size();
			for (int i=0; i<size; i++) {
 				ParsingContext c = (ParsingContext)results.get(i);
				String result = null;
				if (type == 'a')
					result = target + "=\"" + c.getAttributeValue(target) + '"';
				else if (type == 'd')
					result = c.getChildTextData();
                results.set(i, result);
			}
		}		
	}

    public static void extraSelectCurrentElement(
            int queryId, char type, String target) {
        if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE) {
            ParsingContext c = (ParsingContext)m_matchingElements.get(queryId-1);
            String result = null;
            if (type == 'a')
                result = target + "=\"" + c.getAttributeValue(target) + '"';
            else if (type == 'd')
                result = c.getChildTextData();
            m_matchingElements.set(queryId-1, result);
        }
        else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {
            ArrayList results = (ArrayList)m_matchingElements.get(queryId-1);
            int size = results.size();
            ParsingContext c = (ParsingContext)results.get(size-1);
            String result = null;
            if (type == 'a')
                result = target + "=\"" + c.getAttributeValue(target) + '"';
            else if (type == 'd')
                result = c.getChildTextData();
            results.set(size-1, result);
        }
    }

	//////////////////////////
	/// output the results ///
	//////////////////////////
	
	public static int getNoMatchedQueries() {
		return m_queryEval.cardinality();
	}
	
	public static void printMatchedQueries(PrintWriter out)
	{		
		int size = m_queryEval.size();
		for (int i=0; i<size; i++)
			if (m_queryEval.get(i))
				out.print((i+1)+" ");
		out.println();
	}
	
	public static void printResults(PrintWriter out) {
		int size = m_queryEval.length();
		for (int i = 0; i < size; i++)
			if (m_queryEval.get(i)) {
				if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_BOOLEAN) {
					out.print((i + 1) + " ");
				}
				else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE) {
					out.println("\nQ"+(i + 1) + ":");
					Object ob = m_matchingElements.get(i);
					if (ob instanceof ParsingContext)
						out.print(((ParsingContext)ob).toXMLString());
					else if (ob instanceof String)
						out.print((String)ob);
				}
				else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {
					out.println("\nQ"+(i + 1) + ":");
					ArrayList l = (ArrayList)m_matchingElements.get(i);
					int size2 = l.size();
					for (int j=0; j<size2; j++) {
						Object ob = l.get(j);
						if (ob instanceof ParsingContext)
							out.print(((ParsingContext)ob).toXMLString());
						else if (ob instanceof String)
							out.print((String)ob);
					}
				}
			}		
		out.println();
	}
	
	public static void printResults(PrintStream out) {
		int size = m_queryEval.length();
		for (int i = 0; i < size; i++)
			if (m_queryEval.get(i)) {
				if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_BOOLEAN) {
					out.print((i + 1) + " ");
				}
				else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE) {
					out.println("\nQ"+(i + 1) + ":");
					Object ob = m_matchingElements.get(i);
					if (ob instanceof ParsingContext)
						out.print(((ParsingContext)ob).toXMLString());
					else if (ob instanceof String)
						out.print((String)ob);
				}
				else if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL) {
					out.println("\nQ"+(i + 1) + ":");
					ArrayList l = (ArrayList)m_matchingElements.get(i);
					int size2 = l.size();
					for (int j=0; j<size2; j++) {
						Object ob = l.get(j);
						if (ob instanceof ParsingContext)
							out.print(((ParsingContext)ob).toXMLString());
						else if (ob instanceof String)
							out.print((String)ob);
					}
				}
			}		
		out.println();
	}
}