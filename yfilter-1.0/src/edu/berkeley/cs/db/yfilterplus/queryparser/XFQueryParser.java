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

import java.io.*;

public class XFQueryParser implements QueryParser{

    protected BufferedReader m_in = null;
    protected int m_noQueries = 0;
	
	private boolean DEBUG = false;
	private boolean ECHO = false;

	///////////////////
	/// constructor ///
	///////////////////
	
    public XFQueryParser(String fileName) {

        System.out.println("Reading queries from file "+fileName+"...");
        try {
            m_in = new BufferedReader(new FileReader(fileName));
        }
        catch  (Exception e){
            e.printStackTrace();
        }
    }

	////////////////////////////////
	/// methods in the interface ///
	////////////////////////////////
	
    public String readNextQueryString() {
        String line = null;

        try {
			// read a non-emtpy line
			while ((line = m_in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					break;
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public Query readNextQuery() {
        Query query = null;

        try {
			// read a non-emtpy line
			String line;
			while ((line = m_in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					break;
			}

			// parse the query contained in this line
			if (line != null) {  
				if (ECHO)                          
                	System.out.println("Query "+(m_noQueries+1)+": "+line);
                query = new XFQuery(line, ++m_noQueries);
                if (DEBUG) {
					System.out.println("\nParsed and Compiled query:\n"+query);
                	//query.printStepElementsToFile();
                }

            }
            else {
                query = null;
                m_in.close();
            }
        }
        catch  (Exception e){
            e.printStackTrace();
        }

        return query;
    }

    public Query [] readNextQueriesBulk(int num) {
        return null;
    }

	///////////////
	/// getters ///
	///////////////
	
    public int getNoQueries() {
        return m_noQueries;
    }

	///////////////////
	/// test driver ///
	///////////////////
	
    public static void main(String[] args) {
        String queryFile = args[0];
        int numQueries = Integer.MAX_VALUE;
        boolean print = false, stat=false;
        if (args.length > 1) {
            for (int i=1; i<args.length; i++)
                if (args[i].startsWith("print"))
                    print = true;
				else if (args[i].startsWith("stat")){ 
					stat = true;
				}
				else {
                    try {
                        numQueries = Integer.parseInt(args[i]);
                        if (numQueries <=0 )
                            numQueries = Integer.MAX_VALUE;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
        
        XFQueryParser qp = new XFQueryParser(queryFile);
        Query tmpQuery;
        XFQuery pathQuery;
        Path[] paths;
        int totalPaths, totalPredicates, totalPathLength;
        totalPaths = totalPredicates = totalPathLength = 0;
        int noQueries = 0;
        
        while (noQueries < numQueries &&
                (tmpQuery = qp.readNextQuery())!=null) {
            
            if (!(tmpQuery instanceof XFQuery)) {
                System.out.println("Type error in query parsing:: XFQuery is expected.");
                return;
            }
            
            pathQuery = (XFQuery)tmpQuery;
			noQueries++;
			
            if (print)
                System.out.println(pathQuery);
            
			if (stat) {
				totalPaths += pathQuery.getNoPaths();
				totalPredicates += pathQuery.getNoPredicates();
				paths = pathQuery.getPaths();
				totalPathLength += ((XFPath) paths[0]).getNoLocationSteps();
			}           
        }

        //int noQueries = qp.getNoQueries();
        System.out.println("\nRead "+noQueries+" queries.\n");
		if (stat) {
			double avgPathLength = ((double) totalPathLength) / noQueries;
			System.out.println(
					"Avg Length of "+noQueries+" main paths\t\t= " + avgPathLength);
			System.out.println("Total Num of predicates\t\t\t= "+totalPredicates);
			System.out.println("Avg Num of predicate(s) per query\t= "
					+ (double) totalPredicates / noQueries);
			System.out.println("Total Num of paths\t\t\t= "+totalPaths);
			System.out.println("Avg Num of nested path(s) per query\t= "
					+ (totalPaths - noQueries) / (double) noQueries);
		}
    }	
}
