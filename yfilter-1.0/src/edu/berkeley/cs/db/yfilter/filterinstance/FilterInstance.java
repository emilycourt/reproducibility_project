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

import edu.berkeley.cs.db.yfilterplus.dtdscanner.DTDStat;
import edu.berkeley.cs.db.yfilterplus.querygenerator.PathGenerator;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;
import edu.berkeley.cs.db.yfilterplus.xmltree.XMLTree;
import edu.berkeley.cs.db.yfilter.filter.*;

import java.util.*;
import java.io.*;

/**
 * A wrapper thread around the filter engine 
 * that provides an API to access the message queue, 
 * the document queue and the results. 
 * 
 *  
 */
public class FilterInstance extends Thread {

	/**
	 * Sets debug prints.
	 */
	static boolean DEBUG 				= true;

	/**
	 * Sets output prints.
	 */
	static boolean OUTPUT				= true;
	
	/**
	 * The filter object.
	 */
	static EXfilterBasic m_xfilter 		= new EXfilterBasic();
	
	
	/**
	 * Enables dynamic query generation.
	 */
	boolean m_dynamicQueries 			= false;
	
	/**
	 * Time taken to process the last document. Used to calculate dynamic query arrival.
	 */
	float m_docProcessTime				= 0;

	/**
	 * Dynamic query arrival rate.
	 */
	int m_dynamicQueryRate				= 10;
	
	/**
	 * Query generator.
	 */
	PathGenerator m_qg 					= null;

	/**
	 * Generates statistics from the DTD. 
	 */
	DTDStat m_stat						= null;
	
	/**
	 * Query buffer size
	 * 
	 * Queries are typically small.
	 */
	static int QUERY_BUFFER_SIZE		= 2000;
	
	/**
	 * Document buffer size
	 * 
	 * XML documents are fairly large and we don't want to keep too many of them in memory.
	 */
	static int DOC_BUFFER_SIZE			= 500;
	
	/**
	 * Check thrice for new documents.
	 */
	int m_docCheck 						= 0;
	
	/**
	 * No more docs in the queue. Shut thread down.
	 */
	boolean m_noMoreDocs				= false;
	
	/**
	 * Contains query ids, of queries marked for deletion.
	 */
	Vector m_deleteQuery				= new Vector();
	
	
	/**
	 * The next available doc id 
	 */
	int m_currDocID 					= -1;
	
	int m_docBeingProcessed             = -1;
	
	int m_currQueryID					= -1;
	
	/**
	 * A circular queue to hold the documents. 
	 */
	Queue m_docQueue	= null;
	
	/**
	 * A circular queue to hold the queries.
	 */
	Queue m_queryQueue = null;
	
	/**
	 * Constructor
	 * 
	 * Creates an EXfilter instance.
	 *
	 */
	public FilterInstance () {
		m_docQueue = new Queue(DOC_BUFFER_SIZE);
		m_queryQueue = new Queue(QUERY_BUFFER_SIZE, m_xfilter);
		
		// to return matching elements.
		SystemGlobals.outputLevel = SystemGlobals.OUTPUT_ALL;
		
		this.setPriority(Thread.MAX_PRIORITY);
		this.start();
	}
	
	/**
	 * Returns an EXfilter instance
	 * @return EXfilter a filter instance
	 */
	public EXfilter getInstance () {
		return m_xfilter;
	}

	/**
	 * Sets the query arrival rate and returns the previous value.
	 * 
	 * @param rate the new query arrival rate (queries/sec)
	 * @return int the previous query arrival rate
	 */
	public int setDynamicQueryRate (int rate) {
		int prevRate = m_dynamicQueryRate;
		m_dynamicQueryRate = rate;
		return prevRate;
	}
	
	public QueryParser parseQueryFile (String queryFile) {
    	QueryParser qp = new XPQueryParser(queryFile);
    	return qp;
	}

	/**
	 * Generates queries based on ms "milliseconds" and the rate
	 * 
	 * @param rate 				Query arrival rate in documents/sec
	 * @param dtdAbsolutePath 	Absolute path to the DTD path
	 * 
	 * @return Hashtable		A query to ID map
	 */
	public Hashtable dynamicQueries(int rate, String dtdAbsolutePath) {
		if (!m_dynamicQueries) {
			m_dynamicQueries = true;
			
			m_stat = new DTDStat(dtdAbsolutePath);

		    if (m_qg == null)
		    	m_qg = new PathGenerator(m_stat, 0.2);

		    if (rate != -1)
				this.m_dynamicQueryRate = rate;
		}
		
		// XXX: testing
		if (m_docProcessTime < 1000)
				m_docProcessTime = 1000;
		return generateQueries(Math.round(m_dynamicQueryRate * m_docProcessTime / 1000));
	}

	/**
	 * Generate queries and put them into buffer
	 * 
	 * @param amount Number of queries
	 * @return Hashtable A map of query strings to IDs
	 */
	public Hashtable generateQueries(long amount) {
		if (amount == -1) {
			if (m_docProcessTime < 1000)
				m_docProcessTime = 1000;
			amount = Math.round(m_dynamicQueryRate * m_docProcessTime / 1000);
		}
		StringBuffer queryString;
		Hashtable hshQueryStrToID = null;
		Query query;
		long i = 0;
		if (m_dynamicQueries) {
			//synchronized(m_queryBuffer) {
				while (i < amount) {
					// Refer definition for explanation of values.
					queryString = m_qg.generateOnePath(6, 0, 0.2, 0.2, 0.2, 'u', 0.2, m_stat.getRoot());
					hshQueryStrToID = addQueryToBuffer(queryString.toString(), hshQueryStrToID);
					i++;
			
			//System.out.println("Generated query #" + m_queryCount);
				}
			//}
		}
		return hshQueryStrToID;
	}
		  
	/**
	 * Adds a query to the query queue
	 * 
	 * @param queryFile a file containing XPath queries 
	 * @return Hashtable a query string to id map
	 */
	public Hashtable addQuery (String queryFile) {
	    String queryString = null;
	    Hashtable hshQueryStrToID = new Hashtable(); 
	    Query query = null;

	    // XXX: interface supports only files
	    QueryParser qp = new XPQueryParser(queryFile);
	    	
	    while((queryString = qp.readNextQueryString()) != null) {
	    	hshQueryStrToID = addQueryToBuffer(queryString, hshQueryStrToID);
	    }

	    return hshQueryStrToID;
	}

	Hashtable addQueryToBuffer (String queryString, Hashtable hshQueryStrToID) {
		if (hshQueryStrToID == null)
			hshQueryStrToID = new Hashtable();
		
		// parse query
    	Query query = XPQuery.parseQuery(queryString.toString(), -1);
    	
    	int qid = m_queryQueue.set(query);
	  	hshQueryStrToID.put(queryString, new Integer(qid));
	  	
    	if (DEBUG)
  			System.err.println("added query str: " + queryString + " for id: " + qid);

	  	return hshQueryStrToID;
	}
	  /**
	   * Enqueues an XML document or documents to the document queue
	   * 
	   * @param strFilename a file containing the XML document
	   * @return Hashtable a filename to doc id (a hash of the XMLTree data structure) map
	   */
	  public Hashtable bindDoc (String strFileName) {
	  	Hashtable hshDocToID = new Hashtable();
	  	int id = -1;
	  	XMLTree xmltree = null;
	  	
	  	File file = new File(strFileName);
	  	
	  	if (file.isFile()) {
	  	  	xmltree = new XMLTree(strFileName);	
	  		// id = enqueueXML(xmltree);
	  		id = m_docQueue.set(xmltree);

	  		hshDocToID.put(strFileName, new Integer(id));
	  		
		  	if (DEBUG)
		  			System.err.println("added doc id: " + id + " for hash: " + xmltree.hashCode());
	  	} else if (file.isDirectory()) {
	  		File [] files = file.listFiles();
	  		
	  		for (int i = 0; i < files.length; i ++) {
	  			String strFile = files[i].getAbsolutePath();
	  			if (strFile.endsWith("xml")) {
	  				xmltree = new XMLTree(strFile);
	  				//id = enqueueXML(xmltree);
					id = m_docQueue.set(xmltree);
					
	  				//this.m_docHash.put(new Integer(xmltree.hashCode()), new Integer(id));
	  				hshDocToID.put(strFile, new Integer(id));
	  				
	  				if (DEBUG)
	  		  			System.err.println("added doc id: " + id + " for hash: " + xmltree.hashCode());
	  			}
	  		}
	 	}
	  	
	  	
	  	return hshDocToID;
	  }
	

	/**
	 * For a given query, returns the matched paths
	 * 
	 * @param queryHash a query hash value or an actual query id
	 * @param docID a doc hash or a doc id
	 * 
	 * @return String
	 */
	public String getResult (int qid, int docID) {
		String results = null;
	
		if (DEBUG)
			System.err.println("Getting results for (doc id, qid) : " + docID + ", " + qid);
		Object o = m_docQueue.getData(docID);
		
		if (o instanceof String) {
			results = (String)o;
			return results;	
		} 
		
		Hashtable queries = (Hashtable)o;
		
		if (queries != null){
			Object oo = queries.get(new Integer(qid));
			if (oo != null)
				results = (String)oo;
		  	if (DEBUG)
	  			System.err.println("got matching elements!");
		}
		return results;
	}
	
	/**
	 * Process one XML document.
	 * 
	 * @param xmltree
	 */
	  void processXMLTree(XMLTree xmltree, int docid) {
	  	try {
		  	// XXX: do we return these?
		    long start_time;
		    long process_time;
	
		    if (DEBUG)
		    	System.err.println("Processing doc " + docid);
		    
		    m_xfilter.clear();
	
		    m_xfilter.setXMLTree(xmltree);
		    m_xfilter.allocateStreams();
		    
		    start_time = System.currentTimeMillis();
		    m_xfilter.startParsing();
		    process_time = System.currentTimeMillis() - start_time;
	
		    // update metrics
		    m_docProcessTime = process_time;

		    ArrayList queries 	= m_xfilter.getMatchedQueries();
		    if (queries.size() > 0) {
		    	if (DEBUG)
		    		System.err.println("finding matched queries for doc id: " + docid);

		    	// create a Hashtable of matching elements per query
		    	Hashtable elements 	= new Hashtable();
		    	Iterator qids = queries.iterator();
		    	while (qids.hasNext()) {
		    		Integer qid = (Integer)qids.next();
		    		if (DEBUG)
		    			System.err.println("found matched query " + qid + " for doc id: " + docid);
		    	
		    		String matchedElements = m_xfilter.stringOfMatchingElements(qid.intValue());
		    		if (OUTPUT)
		    			System.err.println("(" + docid + ", " + qid + ") : Match! " + matchedElements);
		    		elements.put(qid, matchedElements);
		    	}
		    	
		    	if (elements.size() > 0)
		    		m_docQueue.setData(elements, docid);
		    }
		    // generate queries if dynamic queries enabled.
		    generateQueries(-1);
		    
	  	} catch(Exception e) {
	  		e.printStackTrace();
	  	}
	  }

	
	  /**
	   *  Adds all queries in the query buffer to Xfilter
	   */
	  void addQueriesToFilter() throws InterruptedException {
	  	
	  	// XXX: Already happening in the buffer
	  	if (true)
	  		return;
	  	
	    Query query = null;
		int qid = -1;
		
		Hashtable hashq = m_queryQueue.processNext();
				
		if (hashq != null) {
			Enumeration keys = hashq.keys();
			while (keys.hasMoreElements())  {
				Object o = keys.nextElement();
				qid = ((Integer)o).intValue();
				query = (Query)hashq.get(o);						
			}

			int fqid = m_xfilter.addQuery(query);
			if (DEBUG)
				System.err.println("Adding query " + fqid + " to filter...");

			query.setQueryId(fqid);
		} else {
			if (DEBUG)
				System.err.println("No more queries...");
				sleep(Queue.WAIT);
		}
	  }
	  
	  /**
	   * Marks query for deletion. Query will be deleted from filter 
	   * after the current document is processed.
	   * 
	   * @param qid The query hash or query id
	   * 
	   */
	  public void deleteQuery (int qid) {
	  	m_deleteQuery.add(new Integer(qid));
	  }

	  void deleteQueriesFromFilter () {
	  	boolean result = false;
	  	while (this.m_deleteQuery.size() > 0) {
	  		Integer qid = (Integer)m_deleteQuery.get(0);
	  		m_deleteQuery.remove(0);

	  		if (DEBUG)
	  			System.err.println("Deleting query " + qid);
	  		Query query = (Query)(m_queryQueue.get(qid.intValue()));

	  		if (query == null) {
	  			System.err.println("No such query: id = " + qid);
	  		} else {
	  			m_xfilter.deleteQuery(query, query.getQueryId());
		  		if (DEBUG) {
		  			System.err.println("Deleted query id: " + query.getQueryId());
		  			System.err.println("No. of active queries: " + m_xfilter.getNoActiveQueries()); 
		  		}
	  		}
	  	}
	  }
	  
	  /**
	   * Process a document in the document buffer
	   * 
	   * @return Vector contains an ArrayList of matched queries, one per document 
	   */
	   void processDocument() throws InterruptedException {
	   		// the queries that match the current document
	   		Vector queries = new Vector();

			if (DEBUG)
				System.err.println("Processing document...");
			Hashtable hashdoc = (Hashtable)m_docQueue.processNext();
				
			if (hashdoc != null) {
				Enumeration ids = hashdoc.keys();
				while (ids.hasMoreElements()) {
					int docid = ((Integer)ids.nextElement()).intValue();
					XMLTree xmltree = (XMLTree)hashdoc.get(new Integer(docid)); 

					if (xmltree != null) {
						processXMLTree(xmltree, docid);	   				
					}
				}
   			} else {
   				// No more documents in the queue. Wait a while. Do this thrice.
   				if (DEBUG)
   					System.err.println("No more documents..");
   				if (m_docCheck >= 3)
   					// XXX: testing thread synchronization
   					//m_noMoreDocs = true;
   				m_docCheck++;
   				sleep(Queue.WAIT);
   			}
	   }
	   
	   public void run () {
		while (!m_noMoreDocs) {
			// if more queries in buffer, get queries into filter
				try {
					addQueriesToFilter();
				} catch(InterruptedException ie) {
					System.err.println("Filter instance sleep interrupted!");
				}

				// process document(s)
				try {
					processDocument();
				} catch(InterruptedException ie) {
					System.err.println("Filter instance sleep interrupted!");
				}
				/**
				 * Removes queries marked for deletion, if any.
				 */
			deleteQueriesFromFilter();	
		}
		System.err.println("No more docs to process. Filter exiting...");
	}
}





