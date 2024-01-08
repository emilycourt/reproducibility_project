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

import java.io.*;

public class Profiler {
	
	/// timers ///
	
	public static int m_costOfNFAInit;
	public static int m_costOfNFAExec;
					// m_costOfNFAExec contains m_costOfPostProcessing
					// and m_costOfRecordGeneration
	public static int m_costOfNFAEndDoc;
	
	public static int m_costOfRecordGeneration;
	public static int m_costOfSimplePredicates;		// pipelined predicate evaluation
	public static int m_costOfSimplePredicates2;	// predicate evaluation on materialized streams
	public static int m_costOfNPBookkeeping;
	public static int m_costOfNPEval;
	
	/// counters ///
	public static int m_noTransitions;
	public static int m_noAccStatesHit;
	public static int m_noIDScanned;
	public static int m_noPredEval;
	//public static int noBookkeeping = 0;
	
	public Profiler() {}
	
	public static void clear() {
		m_costOfNFAInit = 0;
		m_costOfNFAExec = 0;
		m_costOfNFAEndDoc = 0;
		
		m_costOfRecordGeneration = 0;		
		m_costOfSimplePredicates = 0;
		m_costOfSimplePredicates2 = 0;
		m_costOfNPBookkeeping = 0;
		m_costOfNPEval = 0;
		
		m_noTransitions = 0;
		m_noAccStatesHit = 0;
		m_noIDScanned = 0;
		m_noPredEval = 0;
	}
	
	public static void takeAverage(int noDocs) {
		m_costOfNFAInit = (int)Math.ceil(m_costOfNFAInit/(double)noDocs);
		m_costOfNFAExec = (int)Math.ceil(m_costOfNFAExec/(double)noDocs);
		m_costOfNFAEndDoc = (int)Math.ceil(m_costOfNFAEndDoc/(double)noDocs);
		
		m_costOfRecordGeneration = (int)Math.ceil(m_costOfRecordGeneration/(double)noDocs);
		m_costOfSimplePredicates = (int)Math.ceil(m_costOfSimplePredicates/(double)noDocs);
		m_costOfSimplePredicates2 = (int)Math.ceil(m_costOfSimplePredicates2/(double)noDocs);
		m_costOfNPBookkeeping = (int)Math.ceil(m_costOfNPBookkeeping/(double)noDocs);
		m_costOfNPEval = (int)Math.ceil(m_costOfNPEval/(double)noDocs);
		
		m_noTransitions = (int)Math.ceil(m_noTransitions/(double)noDocs);
		m_noAccStatesHit = (int)Math.ceil(m_noAccStatesHit/(double)noDocs);
		m_noIDScanned = (int)Math.ceil(m_noIDScanned/(double)noDocs);
		m_noPredEval = (int)Math.ceil(m_noPredEval/(double)noDocs);
	}
	
	public static void report() {
		System.out.println("-- timers -->");
		System.out.println("NFAInit\t\t:"+m_costOfNFAInit);
		System.out.println("NFAExec total\t:"+m_costOfNFAExec);
		int sum1 = m_costOfRecordGeneration + m_costOfSimplePredicates;
		System.out.println("\t\t( NFA only --"+(m_costOfNFAExec-sum1));
		System.out.println("\t\t  Record gen -- "+m_costOfRecordGeneration);
		System.out.println("\t\t  Simple predicates -- "+m_costOfSimplePredicates+" )");
		System.out.println("NFAEndDoc\t:"+m_costOfNFAEndDoc);		
		//int sum2 = m_costOfNPBookkeeping;
		//if (SystemGlobals.evalNestedPathWhen == 1)
		//    sum2 += m_costOfNPEval;
		//System.out.println("Simple Predicates\t:"+m_costOfSimplePredicates);
		//System.out.println("NP Bookkeeping\t:"+m_costOfNPBookkeeping);
		System.out.println("\t\t( Simple Predicates -- "+m_costOfSimplePredicates2);
		System.out.println("\t\t  NP Evaluation -- "+m_costOfNPEval+")\n");
		
		System.out.println("-- stats -->");
		System.out.println("no transitions in NFA = "+m_noTransitions);
		System.out.println("no accepting states hit = "+m_noAccStatesHit);
		System.out.println("no IDs scanned = "+m_noIDScanned);
		System.out.println("no pred eval = "+m_noPredEval);
		//System.out.println("no bookkeeping = "+m_noBookkeeping);
	}
	
	public static void report(PrintWriter out) {
		out.println("-- timers -->");
		out.println("NFAInit\t\t:"+m_costOfNFAInit);
		out.println("NFAExec total\t:"+m_costOfNFAExec);
		int sum1 = m_costOfRecordGeneration + m_costOfSimplePredicates;
		out.println("\t\t( NFA only --"+(m_costOfNFAExec-sum1));
		out.println("\t\t  Record gen -- "+m_costOfRecordGeneration);
		out.println("\t\t  Simple predicates -- "+m_costOfSimplePredicates+" )");
		out.println("NFAEndDoc\t:"+m_costOfNFAEndDoc);		
		//int sum2 = m_costOfNPBookkeeping;
		//if (SystemGlobals.evalNestedPathWhen == 1)
		//    sum2 += m_costOfNPEval;
		//out.println("Simple predicates\t:"+m_costOfSimplePredicates);
		//out.println("NP Bookkeeping\t:"+m_costOfNPBookkeeping);
		out.println("\t\t( Simple Predicates -- "+m_costOfSimplePredicates2);
		out.println("\t\t  NP Evaluation -- "+m_costOfNPEval+")\n");
		
		out.println("-- stats -->");
		out.println("no transitions in NFA = "+m_noTransitions);
		out.println("no accepting states hit = "+m_noAccStatesHit);
		out.println("no IDs scanned = "+m_noIDScanned);
		out.println("no pred eval = "+m_noPredEval);
		//out.println("no bookkeeping = "+m_noBookkeeping);
	}
}