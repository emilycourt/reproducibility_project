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
import edu.berkeley.cs.db.yfilterplus.queryparser.*;
import edu.berkeley.cs.db.yfilterplus.xmltree.*;

import java.util.*;

public class NFAPostProcessor {

	/**
	 * in the current implementation, when this function is called,
	 * no post processing has been actually done yet. this function 
	 * performs delayed query evaluation at the end of document parsing 
	 * (due to the existence of nested path expressions).
	 * this function operates on the materialized path-tupe streams.
	 **/

	public static void evaluateQueries(ArrayList queries, 
									  BottomStreams bottomStreams)
	{
		long begin = -1;
		
		int maxNoPaths = 5;
		ArrayList[] pathCache1 = null;
		if (SystemGlobals.hasPredicates) {
			pathCache1 = new ArrayList[maxNoPaths];
			for (int i=0; i<maxNoPaths; i++)
				pathCache1[i] = new ArrayList(10);
		}
		ArrayList[] pathCache2 = new ArrayList[maxNoPaths];

		//ArrayList queries = queryIndex.getQueries();
		int noQueries = queries.size();
		QueryInMemory q;
		int[] stateIds = null;
		Object ob;
		boolean queryResult;
		for (int i=0; i<noQueries; i++) {
			ob = queries.get(i);
			q = null;
			if (ob instanceof int[])
				stateIds = (int[])ob;
			else
				q = (QueryInMemory)ob;

			queryResult = false;
			if (q == null) {
                if (stateIds == null)
                    // this query has been deleted
                    continue;
				// case 1: there is no predicate or nested path
				// check if any element matches this path
				if (bottomStreams.m_activeStreams.get(stateIds[0]-1)) {
					queryResult = true;
					if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE)//
						ResultCollection.collect(i+1, bottomStreams.getStream(stateIds[0]));//
				}
			} // end of case 1

			else if (q.m_noPaths == 1) {
				// case 2: it is a single path with predicates

				ArrayList pathMatches = bottomStreams.getStream(q.m_stateIds[0]);
				if (q.m_allPredicates != null) {
					ArrayList resultCache = null;//
					if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
						SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)//
						resultCache = new ArrayList(10);//

					if (SystemGlobals.profiling) 
						begin = System.currentTimeMillis();					
					
					Predicate[] predicatesOnPath = q.getPredicates(1);
					int size = pathMatches.size();
					ArrayList pathMatch = null;
                    boolean pathResult;
					for (int j=0; j<size; j++) {
						pathMatch = (ArrayList)pathMatches.get(j);
						pathResult = PredicateEvaluation.evaluatePath(predicatesOnPath, pathMatch);
						if (SystemGlobals.profiling)
							Profiler.m_noPredEval ++;

						if (pathResult) {
                            queryResult = true;
							if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
								SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)//
								resultCache.add(pathMatch);//
							if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_ALL)//
								break;
						}
					}
					
					if (SystemGlobals.profiling)
						Profiler.m_costOfSimplePredicates2 += System.currentTimeMillis()-begin;					
					
					if (queryResult && SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE)//
						ResultCollection.collect(i+1, resultCache);//					
				}
				else {					
					queryResult = true;
					if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE)//
						ResultCollection.collect(i+1, pathMatches);//					
				}
			} // end of case 2

			else {
				// case 3: there are definately nested paths

				boolean fail = false;	// count-based check
				for (int j=0; j<q.m_noPaths; j++)
					if (bottomStreams.m_activeStreams.get(q.m_stateIds[j]-1)==false) {
						fail = true;
						break;
					}
				if (fail)
					continue;

				// prepare path cache including predicate checks
				if (q.m_allPredicates != null) {
					if (SystemGlobals.profiling) 
						begin = System.currentTimeMillis();					

					// check predicates on paths
					Predicate[] predicatesOnPath;
					boolean pathResult;
					for (int j=0; j<q.m_noPaths; j++) {
						// get the predicates
						predicatesOnPath = (Predicate[])q.m_allPredicates.get(j);
						// get there records
						ArrayList pathMatches =
								bottomStreams.getStream(q.m_stateIds[j]);

						// copy the records matching a path to pathCache
						int size = pathMatches.size();
						ArrayList pathMatch;
						for (int k=0; k<size; k++) {
							pathMatch = (ArrayList)pathMatches.get(k);
							pathResult = true;
							if (predicatesOnPath != null) {
								pathResult = PredicateEvaluation.evaluatePath(predicatesOnPath, pathMatch);
								if (SystemGlobals.profiling)
									Profiler.m_noPredEval ++;
							}
							if (pathResult)								
								pathCache1[j].add(pathMatch);							
						}// end of for each path match
						if (pathCache1[j].size() == 0) {
							fail = true;
							break;
						}
					}// end of for each path
					
					if (SystemGlobals.profiling) 
						Profiler.m_costOfSimplePredicates2 += System.currentTimeMillis()-begin;					
				}
				else {
					// set the pointers in pathCache to the bottom streams
					for (int j=0; j<q.m_noPaths; j++)
						pathCache2[j] = bottomStreams.getStream(q.m_stateIds[j]);

				}
				if (fail == false) {
					ArrayList resultCache = null;//
					if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
						SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)//
						resultCache = new ArrayList(10);//
					
					// at this moment we should have all info
					// for nested path evaluation
					if (q.m_allPredicates != null)
						queryResult = evaluateNestedPaths(q.m_noPaths, pathCache1,
								q.m_branchingLevels, resultCache);
					else
						queryResult = evaluateNestedPaths(q.m_noPaths, pathCache2,
								q.m_branchingLevels, resultCache);

					if (queryResult && SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE) //
						ResultCollection.collect(i+1, resultCache);//														
				}

				// clear path cache
				if (q.m_allPredicates != null) {
					for (int j=0; j<q.m_noPaths; j++)
						pathCache1[j].clear();
				}
			}// end of case 3

			if (queryResult && 
				( SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
				  SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL ) 
				&& q != null && q.hasExtraSelect()) 
				ResultCollection.extraSelect(i+1, q.m_extraSelectType, q.m_extraSelectAttr);
																		
		}// end of for each query
	}
	
	/**
	 * seemingly all paths of this query have been 
	 * satified let us check if the instantiation match
	 * joins between nested paths are not supported yet.
	 **/

	static private boolean evaluateNestedPaths(int noPaths, ArrayList[] pathCache,
								  int[] branchingLevels, ArrayList resultCache) {

		long begin = -1;
		if (SystemGlobals.profiling)
			begin = System.currentTimeMillis();		

		ArrayList mainPath, pathContext;
		ParsingContext c;
		boolean result = false;
		int mainPathSize = pathCache[0].size();
		for (int i=0; i<mainPathSize; i++) {
			// for each main path match
			mainPath = (ArrayList)pathCache[0].get(i);

			int j;
			for (j=1; j<noPaths; j++) {
				// check the j's nested path filter using
				// pathCache[j] 's elements at branchingLevels[j]
				c = (ParsingContext)mainPath.get(branchingLevels[j]);

				int npFilterSize = pathCache[j].size();
				int k;
				for (k=0; k<npFilterSize; k++)  {
					pathContext = (ArrayList)pathCache[j].get(k);
					if (c.equals((ParsingContext)pathContext.get(branchingLevels[j])))
						break;
				}// end for each nested path match

				if (k == npFilterSize)
					break;
			}// end for each nested path
			
			if (j >= noPaths) {
				// this main path match satisfies all branches 
				result = true;
				if (SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
					SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)//
					resultCache.add(mainPath);//
				if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_ALL)//
					break;					 
			}
		}// end of for each main path match
		
		if (SystemGlobals.profiling)
			Profiler.m_costOfNPEval += System.currentTimeMillis()-begin;		

		return result;
	}

	/**
	 * this function does post processing for queries associated 
	 * with a single accepting state in a pipelined fashion. 
	 **/
	public static void evaluateSimplePredicates(ArrayList queries, ArrayList queryIds,
								ArrayList queryPathContextList) {
		long begin = -1;
		if (SystemGlobals.profiling) {
			begin = System.currentTimeMillis();
		}

		/// no nested paths, check predicates ///

		int len = queryIds.size();
		if (SystemGlobals.profiling) {
			Profiler.m_noIDScanned += len;
		}
		int size = queryPathContextList.size();

		IDPair id;
		int queryId, pathId;
		QueryInMemory q;
		for (int i=0; i<len; i++) {
			/// get the id of a candidate
			id = (IDPair)queryIds.get(i);
			queryId = id.getQueryId();
			
			/// the following is a short-cut evaluation strategy; it
			/// requires the setting of query result no matter output or not
			if (ResultCollection.m_queryEval.get(queryId-1) &&
				SystemGlobals.outputLevel != SystemGlobals.OUTPUT_ALL)
				continue;

			/// get the query information 
			boolean queryResult = false;
			Object ob = queries.get(queryId-1);
			q = null;
			if (ob instanceof QueryInMemory)
				q = (QueryInMemory)ob;
			if (q == null)
				/// no predicates 
				queryResult = true;
			else {
				if (q.m_allPredicates != null) {
					/// evaluate the predicates 
					Predicate[] predicatesOnPath = q.getPredicates(1);
					ArrayList queryPathContext;
					for (int j = 0; j < size; j++) {
						queryPathContext = (ArrayList) queryPathContextList.get(j);
						queryResult = PredicateEvaluation.evaluatePath(predicatesOnPath, queryPathContext);
						
						if (SystemGlobals.profiling) {
							Profiler.m_noPredEval++;
						}
						if (queryResult)
							break;						
					}
				}
				else
					queryResult = true;
			}
			if (queryResult) {			
				/// collect the result
				ResultCollection.collectCurrentElement(queryId);
				/// handle extra selects
				if ((SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ONE ||
					 SystemGlobals.outputLevel == SystemGlobals.OUTPUT_ALL)  
					&& q != null && q.hasExtraSelect()) 
					ResultCollection.extraSelectCurrentElement(
                            queryId, q.m_extraSelectType, q.m_extraSelectAttr);
			}			
		}

		if (SystemGlobals.profiling) 
			Profiler.m_costOfSimplePredicates += System.currentTimeMillis()-begin;		
	}
	
	/**
	 * setQueryTrue is only called for a query system without any predicates
	 * or nested path expressions. Other cases go through different paths
	 * @param entry: the HashEntryBasic element corresponding to an accepting state
	 * @param candidates: the list of query identifiers at the accepting state
	 */

	public static void setQueryTrue(ArrayList candidates) {
		int len = candidates.size();
		IDPair id;
		int queryId;
		for (int i=0; i<len; i++) {
			id = (IDPair)candidates.get(i);
			queryId = id.getQueryId();
			
			/// the following is a short-cut evaluation strategy; it
			/// requires the setting of query result no matter output or not
			if (ResultCollection.m_queryEval.get(queryId-1) &&
				SystemGlobals.outputLevel != SystemGlobals.OUTPUT_ALL)
				continue;		
			
			/// collect results								
			ResultCollection.collectCurrentElement(queryId);			
		}
	}
}