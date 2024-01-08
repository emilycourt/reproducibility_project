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

import java.util.*;
import java.io.*;

public class XFQuery extends Query {
	protected Path[] m_paths;

	////////////////////////////////////
	//* constructor -- parse a query *//
	////////////////////////////////////

	public XFQuery(String raw, int id) {
		//System.out.println("Query "+id+" is called.");
		m_queryId = id;

		//RawPath[] rawPaths = getRawPaths(raw);
		//int len = rawPaths.length;
		//m_paths = new XFPath[len];
		//m_hasPredicates = false;
		//for (int i=0; i<len; i++) {
		//	    m_paths[i] = new XFPath(rawPaths[i].getPath(), rawPaths[i].getBranchPoint(), 
		//				     rawPaths[i].getJoinWith(), m_queryId, i+1);
		//	    if (m_paths[i].hasPredicates()) 
		//	      m_hasPredicates = true;
		//}

		ArrayList rawPaths = getRawPaths(raw);
		int len = rawPaths.size();
		m_paths = new XFPath[len];
		m_hasPredicates = false;
		RawPath rp;
		for (int i = 0; i < len; i++) {
			rp = (RawPath) rawPaths.get(i);
			m_paths[i] =
				new XFPath(
					rp.getPath(),
					rp.getBranchPoint(),
					rp.getJoinWith(),
					m_queryId,
					i + 1);
			if (m_paths[i].hasPredicates())
				m_hasPredicates = true;
		}

		if (len > 1)
			m_hasNestedPaths = true;

		//printToFile();
		//System.out.println("Query "+id+" processing is ended.");
	}

	/**
	 * this function decomposes a query into a list of independent paths.
	 * it currently only supports one level of path nesting. arbitrary tree
	 * condition needs to extend the data structure in this class and the
	 * parsing code at the specified place in the function.
	 **/
	private ArrayList getRawPaths(String rawQuery) {
		//private RawPath[] getRawPaths(String rawQuery) {
		//System.out.println("XFQuery::getRawPaths is called.");
		//Vector paths = new Vector();
		ArrayList paths = new ArrayList();

		// the difference between the following two strings are:
		// mainPath is the path excluding nested paths but including predicates
		// pathPrefix excludes predicates and is used for the nested paths later.
		StringBuffer mainPath = new StringBuffer();
		StringBuffer pathPrefix = new StringBuffer();
		int len = rawQuery.length();
		int i = 0;
		int operator, element, predicate;
		int element_len;
		operator = 0;
		element = predicate = -1;
		while (i < len) {
			/* read off "/" and "//" */
			while (i < len && rawQuery.charAt(i) == '/')
				i++;
			if (i >= len) {
				System.out.println(
					"XFQuery::getRawPaths -- Error in query "
						+ m_queryId
						+ ": elename name missing.");
				break;
			}

			/* read off the element name and it should be mandatory */
			element = i;
			while (i < len
				&& rawQuery.charAt(i) != '/'
				&& rawQuery.charAt(i) != '[')
				i++;
			element_len = i - element;
			pathPrefix.append(rawQuery.substring(operator, i));
			mainPath.append(rawQuery.substring(operator, i));
			//System.out.println(mainPath);
			if (i >= len)
				break;

			/* predicate processing */
			if (rawQuery.charAt(i) == '[') {
				// there are predicates! extract the nested path, 
				// if it exists, but at most one per element.		
				do {
					predicate = i;
					i = readBracket(rawQuery, i);
					// substring(predicate, i) is a predicate
					//System.out.println(rawQuery.substring(predicate, i));
					//if (rawQuery.charAt(predicate+1) == 'p')
					//	System.out.println(rawQuery.substring(predicate+1, predicate+11));
					if (!(rawQuery.charAt(predicate + 1) == '@'
						|| (rawQuery.charAt(predicate + 1) == '.'
							&& rawQuery.charAt(predicate + 2) != '/')
						|| rawQuery.startsWith("text()", predicate + 1)
						|| (rawQuery.charAt(predicate + 1) >= '0'
							&& rawQuery.charAt(predicate + 1) <= '9')
						|| rawQuery.startsWith("position()", predicate + 1))) {
						// it is a nested path or a join of paths
						String nested =
							rawQuery.substring(predicate + 1, i - 1);
						String[] nestedPaths = getNestedPaths(nested);
						int noNestedPaths = nestedPaths.length;
						for (int j = 0; j < noNestedPaths && j < 2; j++) {
							// only process 2-way join
							StringBuffer newPath = new StringBuffer();
							boolean relativePath = true;
							//newPath.append(pathPrefix);
							if (nestedPaths[j].charAt(0) == '/') {
								// an absoluate path, e.g. [/nitf] or [//title] 
								newPath.append(nestedPaths[j]);
								relativePath = false;
							} else {
								// a relative path
								newPath.append(pathPrefix);
								if (nestedPaths[j].charAt(0) != '.') {
									// a relative path without an operator before, e.g. head[title]
									newPath.append("/");
									newPath.append(nestedPaths[j]);
								} else {
									// a path starting with '.', e.g. [./title] or [.//tiltle] 
									newPath.append(
										nestedPaths[j].substring(
											1,
											nestedPaths[j].length()));
								}
								// we assume there is no nested path in a nested path
							}

							int branchPoint;
							if (relativePath)
								branchPoint = pathPrefix.length() - element_len;
							else
								branchPoint = -1;

							int joinWith;
							if (j == 0) {
								if (noNestedPaths == 1)
									joinWith = -1;
								else
									joinWith = paths.size() + 2;
								//paths.add(new RawPath(newPath.toString(), branchPoint, joinWith));
							} else
								joinWith = paths.size();
							//paths.add(new RawPath(newPath.toString(), branchPoint, paths.size()));

							// if there can be nested path in a nested path
							// then call getRawPaths for newPath at this point 

							paths.add(
								new RawPath(
									newPath.toString(),
									branchPoint,
									joinWith));
						}
					} else
						// it is a predicate
						mainPath.append(rawQuery.substring(predicate, i));
				} while (i < len && rawQuery.charAt(i) != '/');
				// read predicates on after another
				//System.out.println(mainPath);
				if (i >= len)
					break;
			}
			//rawQuery.charAt(i) == '/'
			operator = i;
		} // read location steps one after another

		//System.out.println("add the main path");
		paths.add(0, new RawPath(mainPath.toString(), -1, -1));
		// note that the branching point of a main path is set to -1
		// the joinWith field is always -1 for the main path due to XPath grammar

		//int size = paths.size();
		//RawPath[] rawPaths = new RawPath[size];
		//paths.copyInto(rawPaths);
		//System.out.println("Getting raw paths is ended.");
		//return rawPaths;

		return paths;
	}

	private int readBracket(String rawQuery, int start) {
		if (rawQuery.charAt(start) != '[') {
			System.out.println(
				"XFQuery::readBracket -- Error in parsing a query: [ is expected.");
			return -1;
		}
		int len = rawQuery.length();
		int i = start + 1;
		int count = 1;
		while (count > 0) {
			while (rawQuery.charAt(i) != '['
				&& rawQuery.charAt(i) != ']'
				&& i < len)
				i++;
			if (i >= len) {
				System.out.println(
					"XFquery::readBracket -- Error in parsing a query: ] is expected.");
				return -1;
			}
			//System.out.println(rawQuery.substring(start+1, i));
			if (rawQuery.charAt(i) == '[')
				count++;
			if (rawQuery.charAt(i) == ']')
				count--;
			i++;
		}
		//System.out.println(rawQuery.substring(start+1, i));
		return i;
	}

	public String[] getNestedPaths(String raw) {
		//System.out.println("Query::getNestedPaths is called for "+raw);
		StringTokenizer st = new StringTokenizer(raw, "=");
		Vector v = new Vector(5); // at most two nested paths
		// two paths are connected by '='. currently other
		// comparison operators are not supported;
		// the join operators can be confused with 
		// comparison operators in predicates
		// in the nested paths
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.charAt(0) >= '0'
				&& s.charAt(0) <= '9') { // [@attr=v] or [SELF=v]
				String last = (String) v.remove(v.size() - 1);
				StringBuffer sb = new StringBuffer(last);
				sb.append("=");
				sb.append(s);
				v.add(sb.toString());
				//System.out.println(sb.toString());
			} else {
				v.add(s);
				//System.out.println(s);
			}
		}
		String[] paths = new String[v.size()];
		v.copyInto(paths);
		return paths;
	}

	/*public void printRawPaths() {
	    System.out.println();
	    //System.out.println(m_queryId);
	    int len = m_rawPaths.length;
	    for (int i=0; i<len; i++) {
		    System.out.print("path "+(i+1)+":");
		    System.out.println(m_rawPaths[i]);
	    }
	}*/

	////////////////////////////
	//* retrieve information *//
	////////////////////////////

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

	//////////////////
	//* for output *//
	//////////////////

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Query ");
		s.append(m_queryId);
		s.append(": \n");

		for (int i = 0; i < m_paths.length; i++) {		
			s.append(m_paths[i].toString());
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

	////////////// inner class ////////////
	class RawPath {
		String m_rawPath;
		int m_branchPoint;
		int m_joinWith;

		protected RawPath(String path, int branchPoint, int joinWith) {
			m_rawPath = path;
			m_branchPoint = branchPoint;
			m_joinWith = joinWith;
		}

		protected String getPath() {
			return m_rawPath;
		}

		protected int getBranchPoint() {
			return m_branchPoint;
		}

		protected int getJoinWith() {
			return m_joinWith;
		}
	}
	///////////////////////////////////////

}
