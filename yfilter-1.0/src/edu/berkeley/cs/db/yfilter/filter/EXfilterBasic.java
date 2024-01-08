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


package edu.berkeley.cs.db.yfilter.filter;

import edu.berkeley.cs.db.yfilter.querymodule.*;
import edu.berkeley.cs.db.yfilter.operators.*;
import edu.berkeley.cs.db.yfilter.icdedemo.ICDEDemo;
import edu.berkeley.cs.db.yfilterplus.dtdscanner.DTDStat;
import edu.berkeley.cs.db.yfilterplus.xmltree.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;

//import org.xml.sax.helpers.DefaultHandler;
//import org.xml.sax.Attributes; 
//import org.xml.sax.SAXException; 
//import org.xml.sax.SAXParseException; 
//import org.xml.sax.XMLReader;

import java.util.*;
import java.io.*;

import org.xml.sax.Attributes;

public class EXfilterBasic extends EXfilter
{

    /// index of all queries for path navigation, and  ///
    /// additional data structures for predicate       ///
    /// evaluation and nested path processing          ///
	protected QueryIndexBasic m_queryIndex;

    /// for NFA-based execution ///
	protected Stack m_runStack;   	// the run stack that coordinates NFA execution
	protected NFAExecution m_nfa;	    // the execution engine

    /// for nested paths ///
	protected BottomStreams m_bottomStreams = null;

    /// statistics ///
    // on documents //
    protected int m_noElements 	= 0;
	protected int m_docSN 		= 0;
    // on matching results //
	protected ArrayList m_matchedQueries = null;
	protected double[] m_matchRatio;	// buffer the number of results for 1000 docs
                            // get the average for selectivity
	protected BitSet m_prevBitSet;    // used to store previous BitSet
    // on queries //
    //private boolean m_hasQueries = false;

	/// for ICDE demo ///
	protected ICDEDemo m_demo = null;
	protected boolean DEMO_CODE = false;

    ///////////////////
    /// constructor ///
    ///////////////////

    public EXfilterBasic() {
        super();
        m_runStack = new Stack();
        m_nfa = new NFAExecution();
        m_queryIndex = new QueryIndexBasic();

        if (SystemGlobals.preparsing == false)
			m_tree = new XMLTree(m_contextPath);

        if (DEMO_CODE) {
            m_docSN = 0;
            m_matchRatio = new double[1000];
            Arrays.fill(m_matchRatio, 0);
            m_prevBitSet = new BitSet();
        }
    }

    public EXfilterBasic(DTDStat stat) {
        super();
        m_runStack = new Stack();
        m_nfa = new NFAExecution();
        m_queryIndex = new QueryIndexBasic(stat);
        
		if (SystemGlobals.preparsing == false)
			m_tree = new XMLTree(m_contextPath);

        if (DEMO_CODE) {
            m_docSN = 0;
            m_matchRatio = new double[1000];
            Arrays.fill(m_matchRatio, 0);
            m_prevBitSet = new BitSet();
        }
    }

	public EXfilterBasic(DTDStat stat, ICDEDemo demo) {
		super();
		m_demo = demo;

		m_runStack = new Stack();
		m_nfa = new NFAExecution();
		m_queryIndex = new QueryIndexBasic(stat);
        
		if (SystemGlobals.preparsing == false)
			m_tree = new XMLTree(m_contextPath);

		m_docSN = 0;
		m_matchRatio = new double[1000];
		Arrays.fill(m_matchRatio, 0);
		m_prevBitSet = new BitSet();
		DEMO_CODE = true;
		SystemGlobals.outputLevel = SystemGlobals.OUTPUT_ALL;
	}

	/////////////////////////
	/// auxiliary methods ///
	/////////////////////////
	
    /**
     * clear after each doc is processed
     */     
    public void clear() {
        super.clear();
        
        if (DEMO_CODE) { // clear bits
            int size = m_prevBitSet.size();
            for (int i = 0; i< size; i++)
                m_prevBitSet.clear(i);
        }
    }

	/**
	 * pre-allocate pathtuple streams after query bulkloading and 
	 * before processing any documents
	 */
    public void preAllocate() {
        if (SystemGlobals.hasNestedPaths  || 
        	(SystemGlobals.hasPredicates && ! SystemGlobals.preparsing)) {
            int noPaths = getNoDistinctPaths();
            m_bottomStreams = new BottomStreams(noPaths);
        }
    }

	/**
	 * if new queries have been added to the system, allocate more  
	 * pathtuple streams before executing a new document
	 */
	public void allocateStreams() {
		if (SystemGlobals.hasNestedPaths  || 
			(SystemGlobals.hasPredicates && ! SystemGlobals.preparsing)) {
            int noPaths = getNoDistinctPaths();
			if (m_bottomStreams == null)
				m_bottomStreams = new BottomStreams(noPaths);
			else
                m_bottomStreams.extendStreams(noPaths);
		}
	}

    ////////////////////////////////////////
    /// build or modify the query module ///
    ////////////////////////////////////////

    public int addQuery(Query query)
    {
        return m_queryIndex.addQuery(query);
    }

    public void deleteQuery(Query query, int queryId)
    {
        m_queryIndex.deleteQuery(query, queryId);
    }

    public QueryIndexBasic getQueryIndex() {
        return m_queryIndex;
    }

    public void printQueryIndex() {
        m_queryIndex.printIndex();
        m_queryIndex.printQueries();
    }

    public void printQueryIndexToFile() {
        m_queryIndex.printIndexToFile();
        m_queryIndex.printQueriesToFile();
    }

    /*
    public void sortPredicates() {
    	m_queryIndex.sortPredicates();
    }
    */

    //////////////////////
    /// get statistics ///
    //////////////////////

    /// for document ///
    public int getNoElements(){
        return m_noElements;
    }

    /// for queries ///
    public int getNoQueries() {
        return m_queryIndex.getNoQueries();
    }

    public int getNoDistinctQueries() {
        return -1;
    }
    
    public int getNoActiveQueries() {
    	return m_queryIndex.getNoActiveQueries();
    }

    public int getNoDistinctPaths() {
        return m_queryIndex.getNoDistinctPaths();
    }

    public double getPathLength() {
        return m_queryIndex.getPathLength();
    }

    public int getNoPredicates() {
        return m_queryIndex.getNoPredicates();
    }

    /// for NFA ///
    public int getNoStates() {
        return m_queryIndex.getNoStates();
    }

    /// for document matches ///
    private void setMatchRatio() {
        m_matchRatio[(m_docSN-1)%1000] = ((double)getNoMatchedQueries())/getNoQueries();
        //m_docSN ++;
    }

    public double getSelectivity() {
        int size = 0;
        if (m_docSN < 1000)
            size = m_docSN;
        else
            size = 1000;
        double sum = 0;
        for (int i=0; i<size; i++)
            sum += m_matchRatio[i];
        return sum/size;
    }

	////////////////////////
	/// matching results ///
	////////////////////////

	/**
	 * get newly matched queries
	 */ 
	public ArrayList getNewlyMatchedQueries() {
		if (ResultCollection.m_queryEval == null)
			return new ArrayList();
		BitSet currentBitSet = ResultCollection.m_queryEval;
		m_prevBitSet.xor(currentBitSet);
		int size = m_prevBitSet.size();
		ArrayList v = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			if (m_prevBitSet.get(i))
				v.add(new Integer(i+1));
		}
		m_prevBitSet = (BitSet)currentBitSet.clone();
		return v;
	}

    
	public int getNoMatchedQueries() {
		if (ResultCollection.m_queryEval == null)
			return 0;
		return ResultCollection.m_queryEval.cardinality();
	}

	/**
	 * get the matched queries; fill in m_matchedQuries 
	 * @return an array list containing the query identifiers
	 */
	public ArrayList getMatchedQueries() {
		if (ResultCollection.m_queryEval == null)
			return new ArrayList();
		if (m_matchedQueries == null)
			m_matchedQueries = new ArrayList((int)Math.floor(m_queryIndex.getNoQueries()*0.1));
		else
			m_matchedQueries.clear();

		BitSet results = ResultCollection.m_queryEval;
		if (results != null) {
		int length = results.length();
		for (int i=0; i<length; i++)
			if (results.get(i))
				m_matchedQueries.add(new Integer(i+1));
		}
		
		return m_matchedQueries;
	}

	/**
	 * get the elements that have matched a particular query.
	 */
	public ArrayList getMatchingElements(int queryId) throws Exception {
		int noQueries = m_queryIndex.getNoQueries();
		if (queryId < 1 || queryId > noQueries) {
			throw new Exception("EXfilterBasic::getMatchingElements -- invalid query identifier!");			
		}
		if ((SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
			 SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL   )  && 
			ResultCollection.m_queryEval.get(queryId-1)) {
			Object ob = ResultCollection.m_matchingElements.get(queryId-1);
			ArrayList list = null;
			if (ob instanceof ArrayList)
				list = (ArrayList)ob;
			else {
				 list = new ArrayList(1);
				 list.add(ob);
			}
			return list; 
		}
		else
			return new ArrayList(1);
	}
	
	/**
	 * Returns a string representation of the elements that have matched a particular query
	 * 
	 * @param queryId
	 * @return String 
	 * @throws Exception
	 */
	public String stringOfMatchingElements(int queryId) throws Exception {
		ArrayList list = getMatchingElements(queryId);
		StringBuffer sb = new StringBuffer();
		int size = list.size();
		for (int j=0; j<size; j++) {
			Object ob = list.get(j);
			if (ob instanceof ParsingContext)
				sb.append(((ParsingContext)ob).toXMLString());
			else if (ob instanceof String)
				sb.append((String)ob);
		}
		return sb.toString();
	}
	
	/**
	 * Prints all the query results to an ASCII stream
	 * 
	 * @param out the stream
	 */
	public void printQueryResults(PrintWriter out) {
		ResultCollection.printResults(out);
	}
	
	/**
	 * Prints all the query results to a binary stream
	 * 
	 * @param out the stream
	 */
	public void printQueryResults(PrintStream out) {
		ResultCollection.printResults(out);
	}
	
    /////////////////////////////////////////////
    /// event handlers for document processing ///
    /////////////////////////////////////////////

    /**
     * DocumnetHandler :: start document :
     *  initialize the execution stack so that we are in the start state.
     **/
    public void startDocument() {
        if (m_trace)
            m_out.println("EXfilterBasic::startDocument is called.");

        m_noElements = 0;
		if (SystemGlobals.preparsing == false)
			m_tree.startDocument();

        /// processing ///
		//int noQueries = m_queryIndex.getNoQueries();
        //m_hasQueries = (noQueries>0? true:false);
		SystemGlobals.hasQueries = (m_queryIndex.getNoActiveQueries()>0? true:false);
        if (SystemGlobals.hasQueries) {
			/// (1) prepare for result collection 
			ResultCollection.prepare(m_queryIndex.getNoQueries());
			        	
        	/// (2) prepare the runtime stack
            m_runStack.push(m_nfa.startDocument(m_queryIndex));

            /// (3) prepare the bottomstreams
            allocateStreams();
        }
        else
        	ResultCollection.clear();
        
        if (DEMO_CODE)
        	m_docSN++;
    }

	/**
	 * DocumnetHandler :: end document :
	 * complete post processing and clear the execution and 
	 * parsing stacks so that we go back to the start state.
	 **/

	public void endDocument() {
		if (m_trace)
			m_out.println("EXfilterBasic::endDocument is called");

		if (SystemGlobals.hasQueries) {
			if (SystemGlobals.hasNestedPaths || 
				(SystemGlobals.hasPredicates && ! SystemGlobals.preparsing)) {
				//if (SystemGlobals.hasNestedPaths || SystemGlobals.hasPredicates) {
				m_nfa.endDocument(m_queryIndex, m_bottomStreams);
				m_bottomStreams.clear();
			}

			m_runStack.clear();
			//printRunStack();

            if (DEMO_CODE)
                setMatchRatio();
		}
		//m_hasQueries = false;
        
		if (SystemGlobals.preparsing == false)
			m_tree.endDocument();

		if (m_trace)
			m_out.println("EXfilterBasic::endDocument is ended");
	}

    /**
     * DocumentHandler :: start element:
     *   here we need to check all of our current states
     *   to see which ones match this element and push
     *   the resulting set of states onto the execution
     *   stack.
     **/
    public void startElement()
    {

        if (m_trace)
            m_out.println("EXfilterBasic::startElement is called.");

        m_noElements++;

        /// processing ///
        if (SystemGlobals.hasQueries) {
            ParsingContext c = (ParsingContext)m_contextPath.peek();
            if (m_trace) {
                c.print();
                System.out.println();
            }
            // (1) store the current element for result collection
            if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
				SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)
				ResultCollection.m_currentElement = c;

			// (2) run the NFA and update the runtime stack
            String elementName = c.getElementName();
            m_nfa.startElement(m_queryIndex, m_runStack, m_contextPath, elementName, m_bottomStreams);
            //printRunStack();

			// (3) some support for the demo
            if (DEMO_CODE) {
                // added by Raymond to handle Stepping //
                if (m_demo != null)
                    m_demo.stepEnd(elementName);                
            }
        }

        if (m_trace)
            m_out.println("EXfilterBasic::startElement is ended");
    }


    /**
     * DocumentHandler :: end element:
     * now evaluating paths and finding accepts is a job here after
     * attribute checking when the element is read
     * and data value checking when the data is read;
     * need to pop the current element context and drop back to the previous one
     **/
    public void endElement()
    {

        if (m_trace)
            m_out.println("EXfilterBasic::endElement is called.");

        if (SystemGlobals.hasQueries) {
            //m_nfa.endElement();
            m_runStack.pop();
            //if (DEMO_CODE)
            //    setMatchRatio();
        }

        if (m_trace)
            m_out.println("EXfilterBasic::endElement is ended");
    }
    
	/**
	 * DocumentHandler :: start element:
	 *  without pre-parsing
	 */

    public void startElement(String uri, String local,
                             String elementName , Attributes attrs)
    {

        if (m_trace)
            m_out.println("EXfilterBasic::startElement is called.");

        m_noElements++;

        /// 1. create a new parsing context ///
        m_tree.startElement(uri, local, elementName, attrs);
        ParsingContext c = (ParsingContext)m_contextPath.peek();
        /*
        int noAttrs = attrs.getLength();
		HashMap attributes = null;
		String[] orderedAttrNames = null;
        if (noAttrs > 0) {
        	attributes = new HashMap((int)Math.ceil(noAttrs/0.75));
        	orderedAttrNames = new String[noAttrs];
        	for (int i=0; i<noAttrs; i++) {
        		String attrName = attrs.getQName(i);
        		orderedAttrNames[i] = attrName;
            	attributes.put(attrName, attrs.getValue(i));
        	}
        }
        ParsingContext c = new ParsingContext(elementName,
                    m_noElements, attributes, -1);
        m_contextPath.push(c);
        */
        if (m_trace) {
            c.print();
            System.out.println();
        }

        /// 2. processing ///
        if (SystemGlobals.hasQueries) {
			// (2-1) store the current element for result collection
			if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
				SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)
				ResultCollection.m_currentElement = c;

			// (2-2) run the NFA and update the runtime stack
            m_nfa.startElement(m_queryIndex, m_runStack, m_contextPath, elementName, m_bottomStreams);
            //printRunStack();

			// (2-3) some support for the demo
            if (DEMO_CODE) {
                // added by Raymond to handle Stepping //
                if (m_demo != null)
                    m_demo.stepEnd(elementName);
            }
        }

        if (m_trace)
            m_out.println("EXfilterBasic::startElement is ended");
    }

	/**
	 * DocumentHandler :: characters:
	 **/
	public void characters(char ch[], int start, int count)
	{
		m_tree.characters(ch, start, count);
	}

    /**
     * DocumentHandler :: end element:
     *  with preparsing
     **/
    public void endElement(String uri, String local, String eleName)
    {

        if (m_trace)
            m_out.println("EXfilterBasic::endElement is called.");
		
        //m_contextPath.pop();
		m_tree.endElement(uri, local, eleName);

        if (SystemGlobals.hasQueries) {
            //m_nfa.endElement();
            m_runStack.pop();

            if (DEMO_CODE)
                setMatchRatio();
        }

        if (m_trace)
            m_out.println("EXfilterBasic::endElement is ended");
    }

    /////////////////////
    /// for debugging ///
    /////////////////////

    public void printRunStack() {
        System.out.println("----- Run Stack -----");
        int len = m_runStack.size();
        RunStackElementBasic rse;
        for (int i=len-1; i>=0; i--) {
            System.out.println("element "+i);
            rse = (RunStackElementBasic)m_runStack.elementAt(i);
            rse.printRunStackElement();
            System.out.println();
        }
    }
}

