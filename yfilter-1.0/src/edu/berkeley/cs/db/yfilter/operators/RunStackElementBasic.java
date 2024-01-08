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

import java.util.*;

/**
 * this class represents a set of states.
 * currently it just wraps a pair of vectors
 * and keeps seperate the accept and nextstate
 * information
 **/
public class RunStackElementBasic
{
	// the set of normal hashtables
	//protected HashSet m_hashtables;
	// the hashtables pointed by //, which should be kept active all way down the path
	//protected HashSet m_dslash_hashtables;
	
	protected HashMap m_transitions;
		// this is a map between the index of the ht in m_hashtables (+) or m_dslash_tables (-)
		// and a list of indexes of the ht in lower level, again  m_hashtables (+) or m_dslash_tables (-)

	/**
	 * create a new state set
	 **/
	public RunStackElementBasic()
	{
		//m_hashtables = new HashSet();
		//m_dslash_hashtables = new HashSet();
		//m_accepts = new Vector();
		m_transitions = new HashMap(1000);
	}

	/**
	 * add an state number to the
	 * state set
	 **/
	public void addHashtable(int n, int from)
	{
		Integer id = new Integer(n);
		int[] fromHtIds = (int[])m_transitions.get(id);
		if (fromHtIds == null) {
			fromHtIds = new int[2];
			fromHtIds[0] = fromHtIds[1] = -1;
										// there are only two ways to go to a state
										// either from its parent or 
										// this state has been reached before										
		}
		if (n != from)
			fromHtIds[0] = from;
		else
			fromHtIds[1] = from;			
										// from looping at the state if it is a //-child
		m_transitions.put(id, fromHtIds);
	}
	
	public int[] getFrom(int htId) {
		int[] from = (int[])m_transitions.get(new Integer(htId));
		if (from != null) {
										// this state has been reached before
			return from; 
		}
		else {
			System.out.println("RunStackElementBasic::getFromHtIds -- nonexistent htId is called for.");
			return null;
		}
	}


	/*
	public void addHashtable(int n, int from, boolean b)
	{
		if (b == false)
			addHashtable(n, from);
										// moving from a //-child to a non //-child	
		Integer htId = new Integer(n);
		if (m_transitions.containsKey(htId)) {
										// this state has been reached before
			int[] fromHtIds = (int[])m_transitions.get(htId);
			fromHtIds[1] = -from;
		}
		else {
										// this is a new state
			m_hashtables.add(htId);
			int[] fromHtIds = new int[2];
			fromHtIds[0] = -from;
			fromHtIds[1] = Integer.MIN_VALUE;			// there are only two ways to go to a state
										// either from its parent or from looping
										// at the state if it is a //-child
			m_transitions.put(htId, fromHtIds);
		}
	}

	public void addDslashHashtable(int n, int from)
	{									// this //-child state is reached 
										// from a non //-child state
		//Integer htId = new Integer(n);
		Integer neg_htId = new Integer(-n);
		if (m_transitions.containsKey(neg_htId)) {
										// this state has been reached before
			int[] fromHtIds = (int[])m_transitions.get(neg_htId);
			fromHtIds[1] = from;		
		}
		else {
										// this is a new state
			m_dslash_hashtables.add(new Integer(n));
			int[] fromHtIds = new int[2];
			fromHtIds[0] = from;
			fromHtIds[1] = Integer.MIN_VALUE;			// there are only two ways to go to a state
										// either from its parent or from looping
										// at the state if it is a //-child
			m_transitions.put(neg_htId, fromHtIds);
		}
	}

	public void addDslashHashtables(HashSet dslash_ht)
	{
		Iterator iter = dslash_ht.iterator();
		while (iter.hasNext()) {
			Integer htId = (Integer)iter.next();
			int n = htId.intValue();
			Integer neg_htId = new Integer(-n);
			if (m_transitions.containsKey(neg_htId)) {
										// this state has been reached before
				int[] fromHtIds = (int[])m_transitions.get(neg_htId);
				fromHtIds[1] = -n;
			}
			else {
										// this is a new state
				m_dslash_hashtables.add(htId);
				int[] fromHtIds = new int[2];
				fromHtIds[0] = -n;
				fromHtIds[1] = Integer.MIN_VALUE;		// there are only two ways to go to a state
										// either from its parent or from looping
										// at the state if it is a //-child
				m_transitions.put(neg_htId, fromHtIds);
			}
		}
	}
	
	public void addTransition(int n, int from) {
										// it only happens when an element triggers a null-transition
										// to a //-child and then a normal transition to a non 
										// //-child state
		if (m_transitions.containsKey(new Integer(-n)))
			return;
		int[] fromHtIds = new int[2];
		fromHtIds[0] = from;
		fromHtIds[1] = Integer.MIN_VALUE;	
		m_transitions.put(new Integer(-n), fromHtIds);
	}
	*/
	public Set getHashtables()
	{
		//return m_hashtables;
		return m_transitions.keySet();
	}
/*
	public HashSet getDslashHashtables()
	{
		return m_dslash_hashtables;
	}
*/
	
	/**
	 * dumps the state set to stdout
	 **/
	public void printRunStackElement()
	{
		Iterator iter;
	/*	System.out.print("Hashtables: ");
		iter = m_hashtables.iterator();
		while (iter.hasNext())
			System.out.print(((Integer)iter.next()).intValue()+" ");
		//for (int i=0; i<m_hashtables.size(); i++)
		//{
		//	System.out.print(m_hashtables.elementAt(i)+" ");
		//}
		System.out.println();
		
		System.out.print("Dlash Hashtables: ");
		iter = m_dslash_hashtables.iterator();
		while (iter.hasNext())
			System.out.print(((Integer)iter.next()).intValue()+" ");
		//for (int i=0; i<m_dslash_hashtables.size(); i++)
		//{
		//	System.out.print(m_dslash_hashtables.elementAt(i)+" ");
		//}
		System.out.println();
	*/	
		System.out.println("Transition maps: ");
		Set keys = m_transitions.keySet();
		iter = keys.iterator();
		Integer here;
		int[] froms;
		while (iter.hasNext()) {
			here = (Integer)iter.next();
			froms = (int[])m_transitions.get(here);
			System.out.print(here.intValue()+"<"+froms[0]+","+froms[1]+" ");
		}
		System.out.println();
		
		//System.out.print("accept: ");
		//for(int i=0; i<m_accepts.size(); i++)
		//{
		//	System.out.print(m_accepts.elementAt(i)+" ");
		//}
		//System.out.println();

	}
}	
