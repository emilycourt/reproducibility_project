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

import edu.berkeley.cs.db.yfilter.querymodule.*;
import edu.berkeley.cs.db.yfilter.filter.*;

import java.util.*;

public class NFAExecution {

    static protected int EMPTY_HENTRY = -1;

    /// for retrieving records ///
    protected int[] m_tmpStack;

    ///////////////////
    /// constructor ///
    ///////////////////

    public NFAExecution() {
        m_tmpStack = new int[20];	// max doc depth
    }

    /////////////////////////////////////
    /// match an xml doc with queries ///
    /////////////////////////////////////

    /**
     * this function returns the start state of the NFA
     **/
    public RunStackElementBasic startDocument(QueryIndexBasic queryIndex)
    {
        long begin = -1;
        if (SystemGlobals.profiling) {
            begin = System.currentTimeMillis();
        }

        // prepare for NFA execution optimization
		if (SystemGlobals.NFAExecLevel == 1) {
			boolean done = SystemGlobals.incInternalDocID();
			if (!done)
				queryIndex.clearAcceptingStates_DocID();
		}
		
        // add the first element to the bottom of the run stack
        RunStackElementBasic ss = new RunStackElementBasic();
        ss.addHashtable(0, -1);
        HashMap ht = queryIndex.getNode(0);
        HashEntryBasic entry = (HashEntryBasic)ht.get("$");
        if (entry != null) {
            ss.addHashtable(entry.getNextHtId(), -1);
        }

        if (SystemGlobals.profiling) {
            Profiler.m_costOfNFAInit += System.currentTimeMillis()-begin;
        }

        return ss;
    }

    /**
     * this function returns the next set of states given the current set of states.
     * it handles the state transition lookup by hashing attrname into each of the
     * hashtables corresponding to the current state set and returning the resulting
     * pointed at results.
     **/
    public void startElement(QueryIndexBasic queryIndex,
                             Stack runStack, Stack contextPath, String attrname,
                             BottomStreams bottomStreams)
    {
        long begin = -1;
        if (SystemGlobals.profiling) {
            begin = System.currentTimeMillis();
        }

        RunStackElementBasic top_runstack = (RunStackElementBasic)runStack.peek();
        RunStackElementBasic topplus1_runstack = new RunStackElementBasic();

        ArrayList nodes = queryIndex.getNodes();
        ArrayList queries = queryIndex.getQueries();
        BitSet dslashChild = queryIndex.getDSlashChild();

        Set cur_hashtables = top_runstack.getHashtables();
        // there should not be duplicates because the parents
        // of //-child states are in the hashset
        // but we preserve the order to remember their parents
        Iterator iter = cur_hashtables.iterator();
        int htId, next_htId, next_next_htId;
        HashMap ht, next_ht;
        HashEntryBasic entry, next_entry, entry1 = null, entry2 = null;
        ArrayList queryPathContext = null;
        ArrayList candidates1=null, candidates2=null;
        int acceptingStateId1=-1, acceptingStateId2=-1;
        while (iter.hasNext())
        {
            htId=((Integer)iter.next()).intValue();
            ht = (HashMap)nodes.get(htId);

            entry = (HashEntryBasic)ht.get(attrname);
            if (entry != null)
            {
                //// new active state ////
                next_htId=entry.getNextHtId();
                if (next_htId!=EMPTY_HENTRY) {
                    topplus1_runstack.addHashtable(next_htId, htId);
                    if (SystemGlobals.profiling) {
                        Profiler.m_noTransitions ++;
                    }

                    next_ht = (HashMap)nodes.get(next_htId);
                    next_entry = (HashEntryBasic)next_ht.get("$");
                    if (next_entry != null) {
                        // no accepts stored in this entry/bucket
                        // the getNextHtId() should return a not -1 value for '$'
                        topplus1_runstack.addHashtable(next_entry.getNextHtId(), htId);
                        if (SystemGlobals.profiling) {
							Profiler.m_noTransitions ++;
                        }
                    }
                }
                //// get all accepts in this bucket ////
                candidates1 = entry.m_accepts;
                acceptingStateId1 = entry.m_acceptingStateId;
                entry1 = entry;
            }

            // since * matches anything, we have to look it up independent of attrname
            entry = (HashEntryBasic)ht.get("*");
            if (entry != null)
            {
                //// new active state ////
                next_htId=entry.getNextHtId();
                if (next_htId!=EMPTY_HENTRY) {
                    topplus1_runstack.addHashtable(next_htId, htId);
                    if (SystemGlobals.profiling) {
						Profiler.m_noTransitions ++;
                    }

                    next_ht = (HashMap)nodes.get(next_htId);
                    next_entry = (HashEntryBasic)next_ht.get("$");
                    if (next_entry != null) {
                        // the getNextHtId() should return a not -1 value for '$'
                        topplus1_runstack.addHashtable(next_entry.getNextHtId(), htId);
                        if (SystemGlobals.profiling) {
							Profiler.m_noTransitions ++;
                        }
                    }
                }
                //// get all accepts in this bucket ////
                candidates2 = entry.m_accepts;
                acceptingStateId2 = entry.m_acceptingStateId;
                entry2 = entry;
            }

            //// post-processing ////
            if (SystemGlobals.NFAExecLevel > 0 &&
                    (candidates1 != null || candidates2 != null) ) {
                if (SystemGlobals.hasPredicates || SystemGlobals.hasNestedPaths) {
                    // get XML path context for queries literally
                    // the level of queries literals differs from real xml doc level
                    // because of //. so some elements on xml path should be ignored
                    queryPathContext = getQueryPathContext(htId, runStack, contextPath);

                    // evaluate the accepts already satified by structure now by predicates
                    if (candidates1 != null) {
                        if (SystemGlobals.hasNestedPaths || 
                        	(SystemGlobals.hasPredicates && ! SystemGlobals.preparsing))
                            bottomStreams.appendStream(queryPathContext, acceptingStateId1);
                        else
							NFAPostProcessor.evaluateSimplePredicates(
								queries, candidates1, queryPathContext);
                        if (SystemGlobals.profiling) 
							Profiler.m_noAccStatesHit += queryPathContext.size();                        
                    }
                    if (candidates2 != null) {
                        if (SystemGlobals.hasNestedPaths  || 
                        	(SystemGlobals.hasPredicates &&! SystemGlobals.preparsing))
                            bottomStreams.appendStream(queryPathContext, acceptingStateId2);
                        else
							NFAPostProcessor.evaluateSimplePredicates(
								queries, candidates2, queryPathContext);
                        if (SystemGlobals.profiling) 
							Profiler.m_noAccStatesHit += queryPathContext.size();                        
                    }
                }
                else {
					if (SystemGlobals.NFAExecLevel == 1) {
						if (candidates1 != null && ! entry1.documentSeen(SystemGlobals.internalDocID))
							NFAPostProcessor.setQueryTrue(candidates1);
						if (candidates2 != null && ! entry2.documentSeen(SystemGlobals.internalDocID))
							NFAPostProcessor.setQueryTrue(candidates2);							
					}
					else {
                    	if (candidates1 != null)
							NFAPostProcessor.setQueryTrue(candidates1);
                    	if (candidates2 != null)
							NFAPostProcessor.setQueryTrue(candidates2);
					}
                }
                candidates1 = candidates2 = null;
            } // end post-processing

            if (dslashChild.get(htId)) {
                topplus1_runstack.addHashtable(htId, htId);
                if (SystemGlobals.profiling) {
					Profiler.m_noTransitions ++;
                }
            }
        }

        //m_grow = true;
        runStack.push(topplus1_runstack);

        if (SystemGlobals.profiling) {
            Profiler.m_costOfNFAExec += System.currentTimeMillis()-begin;
        }
    }

    protected ArrayList getQueryPathContext(int last_htId, Stack runStack, Stack contextPath) {
        // current status: the event is being processed
        // new active states haven't been added to the stack
        long begin = -1;
        if (SystemGlobals.profiling) {
            begin = System.currentTimeMillis();
        }

        ArrayList results = new ArrayList();

        Arrays.fill(m_tmpStack,-1);
        // this stack stores the other predecessor
        // if one state has two
        int pathLen = runStack.size();
        int[] transitions = new int[pathLen];
        // it stores one possible path

        //// recover the path to check predicates ////
        int cur_htId = last_htId;
        int top = pathLen - 1;
        boolean grow = true;
        RunStackElementBasic sElement = null;
        ArrayList path = null;
        while (grow || top<pathLen) {
            if (grow) {
                transitions[top] = cur_htId;
                if (top == 0) {	// one path has been extracted
                    //// output ////
                    //for (int i=0; i<pathLen; i++)
                    //	System.out.print(transitions[i]+" ");
                    //System.out.println("last");
                    path = new ArrayList(10);
                    for (int i=1; i<pathLen; i++)
                        if (transitions[i-1] != transitions[i]) {
                            path.add(contextPath.get(i-1));
                        }
                    path.add(contextPath.peek());
                    //for (int i=0; i<path.size(); i++) {
                    //	((ParsingContext)path.get(i)).print();
                    //	System.out.println();
                    //}
                    results.add(path);
                    //// start backtracking ////
                    top++;
                    grow = false;
                }
                else {			// keep growing
                    sElement = (RunStackElementBasic)runStack.get(top);
                    int[] from = sElement.getFrom(cur_htId);

                    top--;
                    if (from[0]!=-1 && from[1]!=-1) {
                        cur_htId = from[0];
                        m_tmpStack[top] = from[1];
                    }
                    else
                        if (from[0] != -1) {
                            cur_htId = from[0];
                            m_tmpStack[top] = -1;
                        }
                        else {
                            cur_htId = from[1];
                            m_tmpStack[top] = -1;
                        }
                }
            }
            else {			// just backtracked here
                if (m_tmpStack[top] != -1) {
                    cur_htId = m_tmpStack[top];
                    m_tmpStack[top] = -1;
                    // pop it off
                    grow = true;
                }
                else
                    top++;
            }
        }

        if (SystemGlobals.profiling) {
            Profiler.m_costOfRecordGeneration += System.currentTimeMillis()-begin;
        }

        return results;
    }

    /**
     * in the current implementation, when this function is called,
     * no post processing has been actually done yet. it doesnot work
     * for mixed workload (single path, one predicate, or nested path) 
     * very well. for now, this function is called if there is 
     * at least one nested path per query.
     **/
    public void endDocument(QueryIndexBasic queryIndex, BottomStreams bottomStreams)
    {
		long begin = -1;
		if (SystemGlobals.profiling) {
			begin = System.currentTimeMillis();
		}

    	NFAPostProcessor.evaluateQueries(queryIndex.getQueries(), bottomStreams);
    	
		if (SystemGlobals.profiling) {
			Profiler.m_costOfNFAEndDoc += System.currentTimeMillis()-begin;
		}

    }
}
