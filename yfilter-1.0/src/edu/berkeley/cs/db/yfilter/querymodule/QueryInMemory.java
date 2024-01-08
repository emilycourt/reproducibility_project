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

import edu.berkeley.cs.db.yfilterplus.queryparser.*;

import java.util.*;
import java.io.*;

/**
 * in memory representation of a query:
 * all the paths are already indexed in QueryIndex:m_nodes
 * this data structure is the element type of QueryIndex:m_queries
 * for each path, it contains all predicates and their selectivities,
 * path nodes (literally), braching levels for nested paths, joins, etc.
 */

public class QueryInMemory {
	public int m_noPaths;
	
	/// simple predicates ///
	
	// predicate storage: for all paths, index is PathId-1     
	// each element is predicates on a path of type Predicate[]
	public ArrayList m_allPredicates	= null; 	
	// for predicate sorting
	//ArrayList m_allStepElements = null;
	//ArrayList m_allSelectivities = null;
	
	/// nested paths ///
	// for nested paths
	public int[] m_branchingLevels 		= null;
	// for joins between nested paths
	//public int[] m_joinWith = null;		
	
	/// index on the tuple streams ///
	
	public int[] m_stateIds 			= null;
	
	/// extra selects of text or attribute nodes ///
	
	public char m_extraSelectType 		= 'u';
	public String m_extraSelectAttr 	= null;
	
	///////////////////		
	/// constructor ///
	///////////////////
	
	public QueryInMemory() {}
	
	public QueryInMemory(Path[] paths) {
		m_noPaths = paths.length;
		
		m_allPredicates = new ArrayList(m_noPaths);
		if (m_noPaths > 1) {
			m_branchingLevels = new int[m_noPaths];
			//m_joinWith = new int[m_noPaths];
		}

		boolean hasPredicates = false;
		boolean hasJoin = false;
		Predicate[] predicates;
		for (int i=0; i<m_noPaths; i++) {
			predicates = paths[i].getPredicates();
			if (predicates.length > 0) {
				m_allPredicates.add(predicates);
				hasPredicates = true;
			}
			else
				m_allPredicates.add(null);
			
			if (m_noPaths > 1) {
				m_branchingLevels[i] = paths[i].getBranchingLevel();
				//m_joinWith[i] = paths[i].getJoinWith();
				//if (m_joinWith[i] != -1)
				//	hasJoin = true;
			}
		}
		if (hasPredicates == false)
			m_allPredicates = null;		
		//if (hasJoin == false)
		//	m_joinWith = null;
	}
	
	///////////////
	/// setters ///
	///////////////
	
	public void setStateIds(int[] stateIds) {
		m_stateIds = stateIds;
	}

	public void setExtraSelect(char type, String target) {
		m_extraSelectType = type;
		m_extraSelectAttr = target;		
	}
	
	////////////////////////////////////
	/// retrieve data and statistics ///
	////////////////////////////////////
	
	// for paths //	
	
	public int getNoPaths() {
		return m_noPaths;
	}

    public int[] getPathStateIDs() {
        return m_stateIds;
    }

	// for predicates //
	
	public Predicate[] getPredicates(int pathId) {
		if (m_allPredicates == null)
			return null;
		return (Predicate[])m_allPredicates.get(pathId-1);
	}
	
	public ArrayList getAllPredicates() {
		return m_allPredicates;
	}

	public int getNoPredicates() {
		if (m_allPredicates != null) {
			int sum = 0;
			Predicate[] predicates;
			for (int i = 0; i < m_noPaths; i++) {
				predicates = (Predicate[]) m_allPredicates.get(i);
				if (predicates != null)
					sum += predicates.length;
			}
			return sum;
		} else
			return 0;
	}

	// for nested paths //
		
	public int[] getBranchingLevels() {
		return m_branchingLevels;
	}
	
	/*
	public int[] getJoinWith() {
		return m_joinWith;
	}*/

	/*
	public boolean hasJoin() {
		return (m_joinWith != null)?true:false;
	}*/

	// for extra selects //
		
	public boolean hasExtraSelect() {
		return m_extraSelectType != 'u';
	}

	/////////////////////////
	/// predicate sorting ///
	/////////////////////////
	
	/**
	 * the next function reads the element names in location steps
	 * from disk again, since it is not stored in memory per 
	 * query, but rather merged into the shared NFA.
	 **/
	/*
	public void allocateStepElements() {
		m_allStepElements = new ArrayList();
	}
	*/
	
	/*
	public void addStepElements(Vector steps) {
		int noSteps = steps.size();
		String[] stepElements = new String[noSteps];
		steps.copyInto(stepElements);
		m_allStepElements.add(stepElements);
	}
	*/

	/**
	 * sort predicates for better performance
	 **/
	/*		
	public void sortAllPredicates(Hashtable elements) {
		
		if (m_allStepElements==null) {
			System.out.println("Error! step elements are not allocated when they are used");
			return;
		}
		m_allSelectivities = new ArrayList();
		
		String[] stepElements;	// step elements for this path
		String step;
		dtdElement element;
		Predicate[] predicates;	// predicates on this path. the level tells where it is on the path
		double[] selectivities;	// selectivity for each predicate on this path
		int noPredicates;
		Predicate p;
		
		//System.out.println("Sorting predicates...");
		//System.out.println("m_noPaths "+m_noPaths);
		for (int i=0; i<m_noPaths; i++) {
			//System.out.println("path "+(i+1));
			/// for each path, compute the selectivity of predicates ///
			
			stepElements = (String[])m_allStepElements.get(i);
			predicates = (Predicate[])m_allPredicates.get(i);
			noPredicates = predicates.length;
			selectivities = new double[noPredicates];
			for (int j=0; j<noPredicates; j++) {
				p = predicates[j];
				step = stepElements[p.getLevel()];
				if (step.equals("$")){
					System.out.println("Error! Predicates on $");
					return;
				}
				if (step.equals("*")) {
					// we don't know which element will match this
					// estimate the selectivity
					if (p.getType() == 'd') 
						selectivities[j] = 1.0/5;
					else if (p.getType() == 'p')
						selectivities[j] = 1.0/3;
					else { // for an attribute
						int attrValue = p.getValue();
						if (attrValue == -1)	// only check attribute existence
							selectivities[j] = 0.5;
						else	// check both existence and value
							selectivities[j] = 0.5 / 5;
					}
					continue;
				}
				element = (dtdElement)elements.get(step);	// this is a predicate for an element
				if (p.getType() == 'd')
					selectivities[j] = 1.0/element.getNoDataValues();
				else if (p.getType() == 'p')
					selectivities[j] = 1.0/3;
				else {// an attribute
					//p.print();
					String attrName = p.getAttrName();
					int attrValue = p.getValue();
					int index = element.getAttrIndex(attrName);
					int noAttrValues = element.getNoAttrValues(index);
					double attrProb = element.getAttrProb(index);
					if (attrValue == -1) // the predicate only checks if the attr exists
						selectivities[j] = attrProb;
					else
						selectivities[j] = attrProb / noAttrValues;
				}
			}// inner for-loop
			
			/// sort predicates by their selectivity ///
			double s; 	// plus predicate p are temps for sorting
			int minIndex;
			for (int k=0; k<noPredicates; k++) {
				minIndex = k;
			 	for  (int l=k+1; l<noPredicates; l++) 
			 		if (selectivities[l] < selectivities[minIndex]) 
			 			minIndex = l;
			 	//swap minIndex and k
			 	s = selectivities[k];
			 	p = predicates[k];
			 	selectivities[k] = selectivities[minIndex];
			 	predicates[k] = predicates[minIndex];
			 	selectivities[minIndex] = s;
			 	predicates[minIndex] = p;
			}
			m_allPredicates.set(i, predicates);
			//for (int index=0; index<selectivities.length; index++)
			//	System.out.print(selectivities[index]+" ");
			//System.out.println();
			m_allSelectivities.add(selectivities);
			//System.out.println("m_allSelectivities grows to size "+m_allSelectivities.size());
		}
		//print();
		m_allStepElements.clear();
		m_allSelectivities.clear();
		m_allStepElements = null;
		m_allSelectivities = null;
	}
	*/
	
	/**
	 * this function implements element filter proposed in XFilter
	 **/
	/*
	public boolean allElementsIn(HashSet docElements) {
		if (m_allStepElements==null) {
			System.out.println("Error! step elements are not allocated when they are used");
			return false;
		}
		int i;
		for (i=0; i<m_noPaths; i++) {
			String[] stepElements = (String[])m_allStepElements.get(i);
			int count = stepElements.length;	// # steps on this path
			int j;
			for (j=0; j<count; j++) { 
				if (stepElements[j].equals("$") || stepElements[j].equals("*"))
					continue;
				if (!docElements.contains(stepElements[j]))
					break;
			}
			if (j<count)
				break;
		}
		//m_allStepElements.clear();
		//m_allStepElements = null;
		if (i >= m_noPaths)
			return true;
		else 
			return false;
	}
	*/
		
	////////////////////////////
	/// output for debugging ///
	////////////////////////////
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int len = m_noPaths;	// the number of paths
		for (int i=0; i<len; i++) {
			sb.append("path ");
			sb.append(i+1);
			sb.append("--");
			sb.append("\n");
			
			Predicate[] predicates = (Predicate[])m_allPredicates.get(i);
			if (predicates != null) {
			    int count = predicates.length;
			    for (int j=0; j<count; j++) {
				sb.append(predicates[j]);
			    	sb.append("\n");
			    }
			}
			
			sb.append("branching level = ");
			sb.append((m_branchingLevels==null)?-1:m_branchingLevels[i]);
			sb.append("; ");
			//sb.append("join with = ");
			//if (m_joinWith != null)
			//	sb.append(m_joinWith[i]);
			//else
			//	sb.append("null");
			sb.append("\n");
		}
		return sb.toString();
	}
		
	public void print() {
		int len = m_noPaths;	// the number of paths
		for (int i=0; i<len; i++) {
			System.out.println("path "+(i+1)+"--");
			//if(m_allStepElements!=null) {
			//	String[] stepElements = (String[])m_allStepElements.get(i);
			//	int count = stepElements.length;	// # steps on this path
			//	for (int j=0; j<count; j++) 
			//		System.out.print(stepElements[j]+" ");
			//	System.out.println();
			//}
			
			Predicate[] predicates = (Predicate[])m_allPredicates.get(i);
			int count = predicates.length;
			for (int j=0; j<count; j++) 
				predicates[j].print();
					
			//if(m_allSelectivities!=null) {
			//	//System.out.println("size of m_allSelectivities "+m_allSelectivities.size());
			//	double[] selectivities = (double[])m_allSelectivities.get(i);
			//	count = selectivities.length;
			//	for (int j=0; j<count; j++) 
			//		System.out.print(selectivities[j]+" ");
			//}
			//System.out.println();
			
			System.out.print("branching level = "+m_branchingLevels[i]+"; ");
			//System.out.println("join with = ");
			//if (m_joinWith != null)
			//	System.out.println(m_joinWith[i]);
			//else
			//	System.out.println("null");
		}
		//System.out.println();
	}
	
	public void printToFile(PrintWriter out) {
		int len = m_allPredicates.size();	// the number of paths
		for (int i=0; i<len; i++) {
			out.println("path "+(i+1)+"--");
			Predicate[] predicates = (Predicate[])m_allPredicates.get(i);
			int count = predicates.length;
			for (int j=0; j<count; j++) 
				predicates[j].printToFile(out);	
		}
	}
}
