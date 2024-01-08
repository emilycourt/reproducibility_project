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

import java.util.*;
import java.io.PrintWriter;

/**
 * this class is the object we store in the hash table
 * each entry can point to multiple accept states as 
 * well as the next state.
 **/
public class HashEntryBasic
{
	/// pointer to the next hash table 
	int m_nextHtId;
		
	/// accepting state: id, path id list
	public ArrayList m_accepts;  		// for accepted queries
	public int m_acceptingStateId = -1;	// only for accepting states

	/// bookkeeping: id of the last matched doc
	/// used for optimizing the NFA execution
    public int m_matchedDocID = 0;

	/// storing simple predicates, used only for Inline
	//protected Vector m_attrPredicates;
	//protected Vector m_dataPredicates;

	///////////////////
	//* constructor *//
	///////////////////

	public HashEntryBasic(int nextHtId)
	{
		m_nextHtId = nextHtId;
		//m_accepts = new ArrayList();
	}

	public HashEntryBasic(int nextHtId, int acceptingStateId)
	{
		m_nextHtId = nextHtId;
		m_acceptingStateId = acceptingStateId;
		//m_accepts = new ArrayList();
	}
	
	///////////////////////////////////////////
	//* operations on the next hashtable id *//
	///////////////////////////////////////////

	/**
	 * this method gets the nextState which is
	 * an integer > 0.
	 **/
	public int getNextHtId()
	{
	  return m_nextHtId;
	}
	
	public void setNextHtId(int next_htId) {
		m_nextHtId=next_htId;
	}

	///////////////////////////////////////////
	//* operations on the accepting state id *//
	///////////////////////////////////////////
	
	public int getAcceptingStateId() {
	    return m_acceptingStateId;
	}

	public void setAcceptingStateId(int id) {
	    m_acceptingStateId = id;
	}
	
	//////////////////////////////////////
	//* operations on the accepts list *//
	//////////////////////////////////////

	public void addAccept(int queryId, int pathId) {
	    if (m_accepts == null)
	    	m_accepts = new ArrayList(50);

	    m_accepts.add(new IDPair(queryId, pathId));
	}
	
	public void removeAccept(int queryId, int pathId) {
		if (m_accepts == null) {
			System.out.println("Error! A query to be deleted doesnot exist in the indexed list.");
			return;
		}

		int index = m_accepts.indexOf(new IDPair(queryId, pathId));
		if (index != -1)
			m_accepts.remove(index);
		else
			System.out.println("Error! A query to be deleted doesnot exist in the indexed list.");
	}
	
	/**
	 * this method gets the set of acceptStates
	 **/
	public ArrayList getAccepts()
	{
	  return m_accepts;
	}
	
	public int getAcceptsSize() {
		if (m_accepts == null)
			return 0;
		return m_accepts.size();
	}
	
	public boolean containsAccept()
	{
		if (m_accepts == null)
			return false;
		return (!m_accepts.isEmpty());
	}

    //////////////////////////////
    /// document id processing ///
    //////////////////////////////

    public boolean documentSeen(int docID) {
        if (m_matchedDocID < docID) {
            m_matchedDocID = docID;
            return false;
        }
        else
            return true;
    }

	public void clearState_DocID() {
		m_matchedDocID = 0;
	}
	
	////////////////////////////////////////
	/// misc stuff for inline and hybird ///
	////////////////////////////////////////

	/*
	public boolean containsAttrPredicates() {
		return (!m_attrPredicates.isEmpty());
	}
	
	public boolean containsDataPredicates() {
		return (!m_dataPredicates.isEmpty());
	}
	
	public void addPredicate(Predicate p) {
		char type = p.getType();
		if (type == 'a')
			m_attrPredicates.addElement(p);
		else if (type == 'd')
			m_dataPredicates.addElement(p);
	}
	
	// check attributes 
	public void addLosers(HashMap attributes, BitSet losers) {
		Predicate p;
		int size = m_attrPredicates.size();
		int queryId;
		// sequential scan of the whole list
		// improvement can be to sort predicates by attribute names and values
		for (int i=0; i<size; i++) {
			p = (Predicate)m_attrPredicates.elementAt(i);
			queryId = p.getQueryId();
			if (losers.get(queryId-1))	
				// the query is filtered
				continue;
			String attrName = p.getAttrName();
			if (attrName == null) {
				System.out.println("Error in storing predicates on attributes on a hash entry.");
				return;
			}
			if (!attributes.containsKey(attrName))	{
				// the queried attributes does not occur in the element
				losers.set(queryId-1);
				continue;
			}
			int attrValue = p.getValue();
			if (attrValue == -1)	
				// the predicate only wants to check the existence of the attribute
				continue;
			int value = Integer.parseInt(((String)attributes.get(attrName)).trim());
			if (attrValue != value) 
				losers.set(queryId-1);
		}
	}
		
	// check element data value
	public void addLosers(String dataValue, BitSet losers) {
		System.out.println("add losers by data predicates ("+dataValue+") is called");
		int realValue = Integer.parseInt(dataValue.trim());
		Predicate p;
		int size = m_dataPredicates.size();
		int queryId;
		// sequential scan of the whole list
		// improvement can be to sort predicates by data values
		for (int i=0; i<size; i++) {
			p = (Predicate)m_dataPredicates.elementAt(i);
			queryId = p.getQueryId();
			if (losers.get(queryId-1))	
				// the query is filtered
				continue;
			int value = p.getValue();
			if (value == -1) {
				System.out.println("Error in ignoring data values in predicates.");
				return;
			}
			if (value != realValue) 
				losers.set(queryId-1);
		}
		//System.out.println("add losers by data predicates is finished");
	}
	*/
		
	/**
	 * add all the nodes we point to to the
	 * state set ss which is passed to us
	 */
	/*
	protected void updateRunStackElement(RunStackElementNFA ss)
	{
	  if (m_nextHtId != QueryIndexNFA.EMPTY_HENTRY)
	  {
		ss.addHashtable(m_nextHtId);
	  } 
	  if (m_accepts.size()>0)
	  {
		ss.addAccepts(m_accepts);
	  }
	}*/

	/**
	 * this method converts the hash entry
	 * into a human readable string.
	 */
	
	public String toString()
	{
	  StringBuffer sb = new StringBuffer();
		
	  /// list of query identifiers
	  sb.append("accepts: ");
	  if (m_accepts != null) {
		  int size = m_accepts.size();
		  for (int i=0; i<size; i++)
			  sb.append(((IDPair)m_accepts.get(i)).toString());
	  }
	  sb.append("\n");
			
	  /// the next hashtable pointer
	  sb.append("next ht: ");
	  sb.append(m_nextHtId);
	  sb.append("\n");

	  return sb.toString();
	}
	
	/// output for debugging ///
	
	public void print() {
		/* print the predicates in this entry
		System.out.println("-- attribute predicates--");
		Enumeration e = m_attrPredicates.elements();
		while (e.hasMoreElements())
			((Predicate)e.nextElement()).print();
		System.out.println("-- data predicates--");
		e = m_dataPredicates.elements();
		while (e.hasMoreElements())
			((Predicate)e.nextElement()).print();	
		*/
		
		System.out.println("--accepts --");
		
		/// arraylist ///
		if (m_accepts != null) {
		    int size = m_accepts.size();
		    for (int i=0; i<size; i++)
				((IDPair)m_accepts.get(i)).print();
		    if (m_accepts.size()>0)
				System.out.println();
		}
			
		// print the next hashtable pointer
		System.out.println("--next ht: "+m_nextHtId);
	}
	
	public void printToFile(PrintWriter out) {
		/* print the predicates in this entry
		out.println("-- attribute predicates--");
		Enumeration e = m_attrPredicates.elements();
		while (e.hasMoreElements())
			((Predicate)e.nextElement()).printToFile(out);
		out.println("-- data predicates--");
		e = m_dataPredicates.elements();
		while (e.hasMoreElements())
			((Predicate)e.nextElement()).printToFile(out);	
		*/
		
		// print accepted paths here
		out.println("--accepts --");
				
		/// arraylist ///
		
		if (m_accepts != null) {
		    int size = m_accepts.size();
		    for (int i=0; i<size; i++)
				((IDPair)m_accepts.get(i)).printToFile(out);				
		    if (m_accepts.size()>0)
				out.println();
		}
		
		// print the next hashtable pointer
		out.println("--next ht: "+m_nextHtId);
	}
}
