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

package edu.berkeley.cs.db.yfilter.querymodule;

import edu.berkeley.cs.db.yfilter.filter.SystemGlobals;
import edu.berkeley.cs.db.yfilterplus.dtdscanner.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;

import java.util.*;
import java.io.*;

/**
 *  this class encodes a set of xpath queries as a query
 *  tree consisting of a set of hashtables with links to
 *  each other
 *
 **/

public class QueryIndexBasic
{

	static int EMPTY_HENTRY = -1;
	final int MAX_STATES = 50000;
	
	/// NFA index ///
	//	the nodes of the hashtree that indexes m_queries
	//	for inline, predicates are stored with nodes
	//	o.w they are stored in m_queries with each queries 	
	ArrayList m_nodes;
	// a bit array indicating those states that are accepting states
	BitSet m_dslashChild;

	/// schema and staticstics ///	
	//	currently only for hashtable capacity	
	Hashtable m_elements;
    //TODO: the dtdFile should be also used to optimize
    //paths before they are inserted into the NFA

	/// predicate processing ///
	//	element type is QueryInMemory, index: queryId-1   
	//	it stores all predicates along each path, and nested paths 
	//	this is used when QueryPlans are not created, for simple queries	
	ArrayList m_queries = null;  
	
	/// for query deletion ///	
	//	this is used when QueryPlans are not created, for simple queries
	BitSet m_deletedQueries;
	
	/// statistics ///
	int m_noQueries;
	int m_noPredicates;
	int m_noPaths;
	int m_totalPathLength;
	int m_noAcceptingStates;
	
	///////////////////
	/// constructor ///
	///////////////////
	
	/**
	 * initialize the query tree with the start node
	 * which is an empty hashtable
	 **/
	public QueryIndexBasic()
	{
		m_dslashChild = new BitSet(MAX_STATES);
		m_nodes = new ArrayList();

		m_queries = new ArrayList();

		m_noQueries = 0;
		m_noPredicates = 0;
		m_noPaths = 0;
		m_totalPathLength = 0;
		m_noAcceptingStates = 0;

		if (SystemGlobals.hasDTDSourceFile()) {
			DTDStatReader stat = new DTDStatReader();
			stat.readStat(SystemGlobals.domain);
			m_elements = stat.getElements();
		}
		
		int initCapacity = 4;
		// 3/0.75: 1 for root, 1 for // and 1 for *
		m_nodes.add(new HashMap(initCapacity));
	}

	public QueryIndexBasic(String dtdFile) {
		m_dslashChild = new BitSet(MAX_STATES);
		m_nodes = new ArrayList();

		m_queries = new ArrayList();

		m_noQueries = 0;
		m_noPredicates = 0;
		m_noPaths = 0;
		m_totalPathLength = 0;
		m_noAcceptingStates = 0;

		DTDStatReader stat = new DTDStatReader();
		stat.readStat(dtdFile);		
		m_elements = stat.getElements();

		int initCapacity = 4;
		// 3/0.75: 1 for root, 1 for // and 1 for *
		m_nodes.add(new HashMap(initCapacity));
	}


	public QueryIndexBasic(DTDStat stat) {
		m_dslashChild = new BitSet(MAX_STATES);
		m_nodes = new ArrayList();

		m_queries = new ArrayList();

		m_noQueries = 0;
		m_noPredicates = 0;
		m_noPaths = 0;
		m_totalPathLength = 0;
		m_noAcceptingStates = 0;
		
		m_elements = stat.getElements();
		
		int initCapacity = 4;
		// 3/0.75: 1 for root, 1 for // and 1 for *
		m_nodes.add(new HashMap(initCapacity));
	}

	public QueryIndexBasic(Hashtable elements) {
		m_dslashChild = new BitSet(MAX_STATES);
		m_nodes = new ArrayList();

		m_queries = new ArrayList();

		m_noQueries = 0;
		m_noPredicates = 0;
		m_noPaths = 0;
		m_totalPathLength = 0;
		m_noAcceptingStates = 0;
		
		m_elements = elements;
		
		int initCapacity = 4;
		// 3/0.75: 1 for root, 1 for // and 1 for *
		m_nodes.add(new HashMap(initCapacity));
	}

	////////////////////////////////////
	/// retrieve data and statistics ///
	////////////////////////////////////
	
	/// on queries ///
	public ArrayList getQueries() {
		return m_queries;
	}

	public int getNoQueries() {
		return m_noQueries;
	}

    public int getNoActiveQueries() {
        if (m_deletedQueries == null)
            return m_noQueries;
        else
            return m_noQueries - m_deletedQueries.cardinality();
    }

	public int getNoPaths() {
		return m_noPaths;
	}
	
	public int getNoPaths(int queryId) {
		return ((QueryInMemory)m_queries.get(queryId-1)).getNoPaths();
	}
	
	public int getNoPredicates() {
		return m_noPredicates;
	}

	public double getPathLength() {
		if (m_noPaths != 0)
			return ((double)m_totalPathLength)/m_noPaths;
		else
			return 0;
	}

	/// on NFA ///
	public ArrayList getNodes() {
		return m_nodes;
	}
	
	public HashMap getNode(int htId) {
		return (HashMap)m_nodes.get(htId);
	}
		
	public BitSet getDSlashChild() {
		return m_dslashChild;
	}

	public int getNoStates() {
		int count = 0;
		int size = m_nodes.size();
		for (int i = 0; i < size; i++) {
			HashMap ht = (HashMap) m_nodes.get(i);
			count += (ht.keySet()).size();
		} 
		return count;
	}

	public int getNoDistinctPaths() {
		if (m_noAcceptingStates == 0 && m_nodes.size() > 1) {
			int count = 0;
			int size = m_nodes.size();
			HashEntryBasic he = null;
			String s = null;
			for (int i = 0; i < size; i++) {
				HashMap ht = (HashMap) m_nodes.get(i);
				Iterator iter = (ht.keySet()).iterator();
				while (iter.hasNext()) {
					s = (String) iter.next();
					he = (HashEntryBasic) ht.get(s);
					if (he.containsAccept()) // this is an accepting state
						count++;
				}
			}
			return count;		
		}
		else
			return m_noAcceptingStates;
	}
    
    /////////////////////////////////////////    	 
	/// operations via traversing the NFA ///
	/////////////////////////////////////////
	
	public void clearAcceptingStates_DocID() {
		int size = m_nodes.size();
		HashEntryBasic he = null;
		String s = null;
		for (int i = 0; i < size; i++) {
			HashMap ht = (HashMap) m_nodes.get(i);
			Iterator iter = (ht.keySet()).iterator();
			while (iter.hasNext()) {
				s = (String) iter.next();
				he = (HashEntryBasic) ht.get(s);
				if (he.containsAccept()) // this is an accepting state
					he.clearState_DocID();
			}
		}		
	}
		        	
	//////////////////////////
	/// add/delete queries ///
	//////////////////////////
	
	/**
	 * add a given xpath to the query tree
	 * this just adds ** and * into the hash
	 * table the same as any other symbol.
	 *
	 **/
	public int addQuery(Query query)
	{
		int queryId = ++m_noQueries;
		query.setQueryId(queryId);
		
		Path[] paths = query.getPaths();	// get all the paths
		int len = paths.length;

		/// add one element to m_queries ///
		QueryInMemory qInMemory = null;
		if (query.hasNestedPaths() || query.hasPredicates()) {
			qInMemory = new QueryInMemory(paths);
			m_queries.add(qInMemory);
			int noPreds = qInMemory.getNoPredicates();
			m_noPredicates += noPreds;
			m_noPaths += len;
			
			if (noPreds > 0)
				SystemGlobals.hasPredicates = true;
			if (len > 1)
				SystemGlobals.hasNestedPaths = true;			
		}
		
		/// add extra select to QueryInMemory object ///
		if (query.hasExtraSelect()) {
			// since extra selects always cause a new predicate 
			// to be created, surely qInMemory is not null here.
			qInMemory.setExtraSelect(query.getExtraSelectType(), query.getExtraSelectAttribute());
		}
		
		/// index all the paths in the NFA ///				
		int[] accStateIds = new int[len];
		for (int i=0; i<len; i++) {
		    accStateIds[i] = indexPath(queryId, paths[i]);
		}
		if (qInMemory != null)
			qInMemory.setStateIds(accStateIds);
		else
			m_queries.add(accStateIds);
			
		return queryId;
	}

    /**
     * this function only removes (1) the identifiers of paths
     * from the accepting states of the NFA without changing
     * the structure of the NFA; and (2) the data structure
     * containing query predicates from the list of queries.
     * the effects are the following: if queries are evaluated
     * on the fly driven by the matching of paths, removal of
     * the path ids from the NFA will never trigger the query
     * evaluation; otherwise, i.e., queries are evaluated at
     * the end of document in batch, the null pointer in the
     * query list will also cause this queries to be ignored.
     * periodic reconstruction is needed to clean up the NFA
     * and the query list, and reassign query identifers.
     */
	public void deleteQuery(Query query, int queryId)
	{
		Path[] paths = query.getPaths();	// get all the paths
		int len = paths.length;

		for (int i=0; i<len; i++) {
			removePath(queryId, paths[i]);
		}
		
		m_queries.set(queryId-1, null);
		if (m_deletedQueries == null)
			m_deletedQueries = new BitSet(m_noQueries);
		m_deletedQueries.set(queryId-1);
	}

	////////////////////////////////////////
	/// insert/delete paths from the NFA ///
	////////////////////////////////////////
	
	/** 
	 * now match the new query against the existing data structure
	 * until we need to start adding new nodes. just follow the hash
	 * tables until we can't find the attribute and create a new subtree.
	 * set miss to true if xpath[level] doesn't hash to a new table
	 **/

	public int indexPath(int queryId, Path path) {

		//Phase 0: get path information
		int pathId = path.getPathId();
		String[] steps = path.getSteps();
		return indexPath(queryId, pathId, steps);
	}
	
	public int indexPath(int queryId, int pathId, String[] steps) {
		int acceptingStateId = -1;
		
		//Phase 1: search the index tree;
		boolean miss = false;
		boolean nohashentry = true;
		int cur_htId = 0;
		int level = 0;
		int path_len = steps.length;

		for (int i=0; i<path_len; i++)
			if (!steps[i].equals("$") && m_totalPathLength < Integer.MAX_VALUE)
				m_totalPathLength++;
		
		
		while((!miss) && (level < path_len))
		{
			HashMap ht = (HashMap)m_nodes.get(cur_htId);
			HashEntryBasic entry = (HashEntryBasic)ht.get(steps[level]);
			if (entry != null)
			{
				/* move on along the path */

				// a hash entry exists in the current hashtable
				// because of one of the following things:
				// 1. it is an accept state of a path
				// 2. it is an intermediate step of a path
				if (level < path_len-1) {
					int next_htId = entry.getNextHtId();
					//have to check if the hashentry points to
					//another table or just to accept states
					if (next_htId != EMPTY_HENTRY)
					{
						cur_htId = next_htId;
						level++;
					}
					else
					{
						miss = true;
						nohashentry = false;
					}
				}
				else
					level++;
			}
			else	// the hash entry does not exist in the current hashtable
			{
				miss = true;
			}
		}

		//Phase 2: expand the tree;
		if (miss==false)
		{
			//we already have an entry in the right table for our attribute
			//update that hash entry to indicate that we want to accept
			//we have to back up one level and one state though since the
			//the previous loop takes us one step too far
			level--;
			HashMap ht = (HashMap)m_nodes.get(cur_htId);
			HashEntryBasic hentry = (HashEntryBasic)ht.get(steps[level]);
			hentry.addAccept(queryId, pathId);
			acceptingStateId = hentry.getAcceptingStateId();
			if (acceptingStateId == -1) {
		    	    m_noAcceptingStates++;
		    	    acceptingStateId = m_noAcceptingStates;
		    	    hentry.setAcceptingStateId(acceptingStateId);
			}
		}
		else
		{
			if (m_nodes.size() + path_len - level > MAX_STATES) {
				BitSet newDslashChild = new BitSet((int)Math.floor(MAX_STATES*1.5));
				newDslashChild.or(m_dslashChild);
				m_dslashChild = newDslashChild;
			}		
			//now we need to add create one or more new hashtables
			//and link them below the current hashtable.  we don't
			//allocate the last hashtable since we actually want
			//an accept there instead
			HashMap cur_ht = (HashMap)m_nodes.get(cur_htId);
			int level_bak = level;
			while (level < (path_len-1))
			{
				// create a new hashtable that correspond to the state
				// reached by reading element steps[level]
				// and add it to m_nodes
				HashMap new_ht;
				if (steps[level].equals("$")) {
					new_ht = new HashMap();	//default 101
					m_dslashChild.set(m_nodes.size());
				}
				else if(steps[level].equals("*")) 
					new_ht = new HashMap();	//default 101
				else {
					if (m_elements != null) {					
						dtdElement element = (dtdElement)m_elements.get(steps[level]);					
						int noChildren = element.getSizeOfChildren();
						int initCapacity = (int)Math.ceil((noChildren+2)/0.75);
						new_ht = new HashMap(initCapacity);
					}
					else
						new_ht = new HashMap();
				}
				int new_htId = m_nodes.size();
				m_nodes.add(new_ht);

				//create a hash entry in the current state hash
				// table which points at the new hash table
				if (level==level_bak && nohashentry==false) {
					// the hash entry exists but m_nextHtId==-1
					// predicates have been added in the search phase
					HashEntryBasic old_hentry=(HashEntryBasic)cur_ht.get(steps[level]);
					old_hentry.setNextHtId(new_htId);
					cur_ht.put(steps[level],old_hentry);
				}
				else {	// a new entry
					//System.out.println("add a new entry with predicates");
					HashEntryBasic new_hentry = new HashEntryBasic(new_htId);

					cur_ht.put(steps[level],new_hentry);
				}

				//now increment the level and update the state
				level++;
				cur_ht = new_ht;
				cur_htId=new_htId;
			}

			//finally, add the hash entry indicating accept state
			m_noAcceptingStates++;
			HashEntryBasic new_hentry = new HashEntryBasic(EMPTY_HENTRY, m_noAcceptingStates);
			new_hentry.addAccept(queryId, pathId);
			acceptingStateId = m_noAcceptingStates;
			
			cur_ht.put(steps[level],new_hentry);
		}
		
		return acceptingStateId;
	}

	public void removePath(int queryId, Path path) {
		// now match the new query against the existing data structure
		// until we need to start adding new nodes. just follow the hash
		// tables until we can't find the attribute and create a new subtree.
		// set miss to true if xpath[level] doesn't hash to a new table

        //Phase 0: get path information
		int pathId = path.getPathId();
		String[] steps = path.getSteps();

        // phase 1: search for the path
		boolean miss = false;
		boolean nohashentry = true;
		int cur_htId = 0;
		int level = 0;
		int path_len = steps.length;
		while((!miss) && (level < path_len))
		{
			HashMap ht = (HashMap)m_nodes.get(cur_htId);
			HashEntryBasic entry = (HashEntryBasic)ht.get(steps[level]);
			if (entry != null)
			{
				/* move on along the path */

				// a hash entry exists in the current hashtable
				// because of one of the following things:
				// 1. it is an accept state of a path
				// 2. it is an intermediate step of a path
				if (level < path_len-1) {
					int next_htId = entry.getNextHtId();
					//have to check if the hashentry points to
					//another table or just to accept states
					if (next_htId != EMPTY_HENTRY)
					{
						cur_htId = next_htId;
						level++;
					}
					else
					{
						miss = true;
						nohashentry = false;
					}
				}
				else
					level++;
			}
			else	// the hash entry does not exist in the current hashtable
			{
				miss = true;
			}
		}

		//Phase 2: update the tree;
		if (miss==false)
		{
			//we have to back up one level since the
			//the previous loop takes us one step too far
			level--;
			HashMap ht = (HashMap)m_nodes.get(cur_htId);
			HashEntryBasic hentry = (HashEntryBasic)ht.get(steps[level]);
			//hentry.addAccept(queryId, pathId);
			hentry.removeAccept(queryId, pathId);
		}
		else
		{
			System.err.println("QueryIndexBasic::removePath -- Error! A query to be deleted doesnot exist in the index.");
		}
	}

	//////////////////////
	/// Enhanced Stuff ///
	//////////////////////
	/*
	public void readQueryElements() {
		BufferedReader in = null;
		System.out.println("Reading step elements from file stepElements.txt...");		
		try {
			in = new BufferedReader(new FileReader("stepElements.txt"));				
		}
		catch  (Exception e){
			e.printStackTrace();
		}	
		
		QueryInMemory query = null;
		Vector steps = new Vector();	// for steps on each path
		try {
			String line;
			int count = 0;
			String queryId;
			//boolean inQuery = false;
			while ((line = in.readLine()) != null) {
				line.trim();
				if (line.startsWith("Query")) {
					//inQuery = true;
					count++;
					queryId = line.substring(6, line.length());
					if (!queryId.equals(String.valueOf(count))) {
						System.out.println("Error in reading stepElements.txt, queryId in disorder");
						return;
					}
					query = (QueryInMemory)m_queries.get(count-1);
					query.allocateStepElements();
					continue;
				}
				if ( line.startsWith("Path")) {
					steps.clear();
					StringTokenizer st = new StringTokenizer(line);
					if (st.hasMoreTokens()) 
						st.nextToken();	// read off the "path..."
     				while (st.hasMoreTokens()) {
         				steps.add(st.nextToken());
         			}
					query.addStepElements(steps);
				}
				//if (line.length()==0)
				//	query.print();
	 		}
	 		in.close();
		}
		catch  (Exception e){
			e.printStackTrace();
		}
	}
	*/
	/*
	public void sortPredicates() {
		BufferedReader in = null;
		System.out.println("Reading step elements from file stepElements.txt...");		
		try {
			in = new BufferedReader(new FileReader("stepElements.txt"));				
		}
		catch  (Exception e){
			e.printStackTrace();
		}	
		
		QueryInMemory query = null;
		Vector steps = new Vector();	// for steps on each path
		try {
			String line;
			int count = 0;
			String queryId = null;
			//boolean inQuery = false;
			while ((line = in.readLine()) != null) {
				line.trim();
				if (line.startsWith("Query")) {
					//inQuery = true;
					count++;
					queryId = line.substring(6, line.length());
					if (!queryId.equals(String.valueOf(count))) {
						System.out.println("Error in reading stepElements.txt, queryId in disorder");
						return;
					}
					query = (QueryInMemory)m_queries.get(count-1);
					if (query!=null) query.allocateStepElements();
					continue;
				}
				if ( line.startsWith("Path")) {
					steps.clear();
					StringTokenizer st = new StringTokenizer(line);
					if (st.hasMoreTokens()) 
						st.nextToken();	// read off the "path..."
     				while (st.hasMoreTokens()) {
         				steps.add(st.nextToken());
         			}
					if (query!=null) query.addStepElements(steps);
				}
				if (line.length()==0) {
					//System.out.println(queryId+":");
					if (query!=null) query.sortAllPredicates(m_elements);
				}
	 		}
	 		in.close();
		}
		catch  (Exception e){
			e.printStackTrace();
		}
		
		//int noQueries = m_queries.size();
		//for (int i=0; i<noQueries; i++) {
		//	query = (QueryInMemory)m_queries.get(i);
		//	//System.out.println("Query "+(i+1));
		//	query.sortAllPredicates(m_elements);
		//}
	}
	*/
	
	/*
	public void elementFilter(HashSet docElements, BitSet losers) {
		QueryInMemory query;
		int noQueries = m_queries.size();
		for (int i=0; i<noQueries; i++) {
			query = (QueryInMemory)m_queries.get(i);
			//System.out.println("Query "+(i+1));
			boolean result = query.allElementsIn(docElements);
			if (result == false)
				losers.set(i);
		}
	}
	*/
	
	///////////////////////////////////
	/// print queries and the index ///
	///////////////////////////////////
	
	/**
	 * render the query tree to stdout
	 **/
	
	public void printIndex()
	{
		int len = m_nodes.size();
		System.out.println("---------- Index ----------");
		for (int i = 0; i < len; i++)
		{
			System.out.println("ht "+i + " //-child="+m_dslashChild.get(i)+" : ");
			HashMap ht = (HashMap)m_nodes.get(i);
			Iterator iter = ht.keySet().iterator();
			while(iter.hasNext())
			{
				String attr = (String)iter.next();
				HashEntryBasic he = (HashEntryBasic)ht.get(attr);
				System.out.println(attr + ": ");
				he.print();
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void printIndexToFile()
	{
		System.out.println("Print index to index.txt...");
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream("index.txt"));
		}
		catch ( IOException ioe ) {
				ioe.printStackTrace();
		}
		
		out.println("---------- Index ----------");
		int len = m_nodes.size();
		for (int i = 0; i < len; i++)
		{
			out.println("ht "+i + " //-child="+m_dslashChild.get(i)+" : ");
			HashMap ht = (HashMap)m_nodes.get(i);
			Iterator iter = ht.keySet().iterator();
			while(iter.hasNext())
			{
				String attr = (String)iter.next();
				HashEntryBasic he = (HashEntryBasic)ht.get(attr);
				out.println(attr + ": ");
				he.printToFile(out);
			}
			out.println();
		}
		out.println();
		
		out.close();
	}


	public void printQueries()
	{
		System.out.println("---------- Queries ----------");
		int len = m_queries.size();
		for (int i = 0; i < len; i++)
		{
			QueryInMemory q = (QueryInMemory)m_queries.get(i);
			if (q != null) {
				System.out.println("Query "+(i+1)+": ");
				q.print();
				//System.out.println();
			}
		}
		System.out.println();
	}
	
	public void printQueriesToFile()
	{
		System.out.println("Print in-memory queries to in-queries.txt...");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream("in-queries.txt"));
		}
		catch ( IOException ioe ) {
				ioe.printStackTrace();
		}
		
		out.println("---------- Queries ----------");
		int len = m_queries.size();
		for (int i = 0; i < len; i++)
		{
			out.println("Query "+(i+1)+": ");
			((QueryInMemory)m_queries.get(i)).printToFile(out);
			out.println();
		}
		out.println();
		
		out.close();
	}
}

