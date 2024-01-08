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

package edu.berkeley.cs.db.yfilter.filterinstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A driver class demonstrating the YFilter API
 *   
 */
public class SimpleApp extends Thread {

	/**
	 *  The parser object
	 */
	FilterInstance 	yfilter 				= null;
	
	/**
	 *  Controls debug prints
  	 */
	boolean 		DEBUG				 	= true;
	
	Hashtable 		m_queryMap 				= null;
	
	Hashtable 		m_docMap 				= null;
	
	String 			m_xmlFile 				= null;

	String 			m_queryFile 			= null;
	
	/**
	 * Enables dynamic query generation
	 */
	boolean 		m_enableDynamicQueries 	= false;
	
	int 			m_queryRate				= -1;
	
	String 			m_dtdFileName 			= null;
	
	PrintStream 	m_err					= null;

	
	public SimpleApp () {
		yfilter = new FilterInstance();	
	}

	/**
	 * Close the error file, if open.
	 */
	protected void finalize () {
		m_err.close();

	}

	/**
	 * An example of the FilterAdapter usage. 
	 * 
	 * Loads up a set of queries, runs it against a set of documents 
	 * and generates results.  
	 * 
	 */
	void bulkLoad (String [] args) {
		try {
			if (args.length < 2) {
				System.err.println("ParserDriver <xmlfile> <queryfile> [errorfile] [queryRate] [DTDfile]");
				System.exit(-1);
			}
	
			if (args.length == 3) {
				m_err = new PrintStream(new FileOutputStream(args[2]));
				System.setErr(m_err);
			}

			if (args.length == 5) {
				m_enableDynamicQueries = true;
				m_queryRate = Integer.parseInt(args[3]);
				m_dtdFileName = args[4];
			}
			
			String xmlFile 		= args[0];
			String queryFile 	= args[1];
			m_queryMap	 		= new Hashtable();
			m_docMap			= new Hashtable();
			m_xmlFile 			= xmlFile;
			m_queryFile 		= queryFile;

			
		} catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		}
		
		
		// Load queries
		m_queryMap = yfilter.addQuery(m_queryFile);
		
		// Add document(s)
		m_docMap = yfilter.bindDoc(m_xmlFile);
		
		// Get results
		Enumeration queries = m_queryMap.keys();
		Enumeration docs = this.m_docMap.keys();
		while (docs.hasMoreElements()) {
			String docFile = (String)docs.nextElement();
			FileOutputStream out = null;
			try {
				if (docFile.lastIndexOf(File.pathSeparator) == -1)
					out = new FileOutputStream(docFile + ".out");
				else
					out = new FileOutputStream(docFile.substring(docFile.lastIndexOf(File.pathSeparator) + 1));
			} catch(FileNotFoundException fnfe) {
				System.err.println("Error creating output file: " + docFile);
			}
			int docid = ((Integer)m_docMap.get(docFile)).intValue();
			System.err.println("doc id: " + docid + " doc file: " + docFile);
			while (queries.hasMoreElements()) {
				String query = (String)queries.nextElement();
				int qid = ((Integer)m_queryMap.get(query)).intValue();
				System.err.println("query id: " + qid + " query: " + query);
				try {
					String results = yfilter.getResult(qid, docid);
					if (results != null) {
						try {
							out.write(("Q"+ qid + ":\n" + results).getBytes());
							//System.out.write(("Q"+ yfilter.getQueryID(hshq) + ":\n" + results).getBytes());
						} catch(IOException ioe) {
							System.err.println("Error writing output file: " + docFile);
						}
					}
				} catch(NullPointerException npe) {
					System.err.println("No results yet!");
				}
			}
		}
	}
	
	/**
	 * An example of FilterAdapter usage. 
	 * 
	 * Add queries, run against documents, delete a few queries, check results.
	 */
	void addAndDeleteQueries (String [] args) {
		String results = null;

		if (args.length != 4) {
			System.err.println("ParserDriver <documents> <queries> <doc id> <query id>");
			System.exit(-1);
		}

		String docs = args[0];
		if (DEBUG)
			System.err.println("docs: " + docs);
		String queries = args[1];
		if (DEBUG)
			System.err.println("queries: " + queries);
		int docid = Integer.parseInt(args[2]);
		if (DEBUG)
			System.err.println("doc id: " + docid);
		int qid = Integer.parseInt(args[3]);
		if (DEBUG)
			System.err.println("query id: " + qid);
		
		// load docs
		Hashtable docMap = yfilter.bindDoc(docs);
		
		// load queries
		Hashtable queryMap = yfilter.addQuery(queries);
		
		try {
			sleep(4000);
		} catch (InterruptedException ie) {
			System.err.println("SimpleApp: sleep interrupted!");
		}
		
		// delete queries
		
		yfilter.deleteQuery(1);
		yfilter.deleteQuery(8);
		yfilter.deleteQuery(9);
		yfilter.deleteQuery(13);
		yfilter.deleteQuery(17);
		yfilter.deleteQuery(21);
		yfilter.deleteQuery(67);
		
		yfilter.bindDoc(docs);
	}

	/**
	 * An example of FilterAdapter usage. 
	 * 
	 * Generate queries dynamically, add documents, check results.
	 */
	void dynamicQueries (String [] args) {
		// Generate queries dynamically, based on a query arrival rate (queries/sec).
		if (m_enableDynamicQueries)
			m_queryMap = yfilter.dynamicQueries(m_queryRate, m_dtdFileName);		
		
		m_docMap = yfilter.bindDoc("filter_instance" + File.pathSeparator + "xml");
		
	}
	
	/**
	 * Run the application using different aspects of the API
	 * @param args
	 */
	public void run (String [] args) {
		
		//bulkLoad(args);
		
		addAndDeleteQueries(args);
		
		//dynamicQueries(args);
	}

	public static void main(String [] args) {

			SimpleApp pd = new SimpleApp();
			
			pd.run(args);
	}
}





