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

import java.io.PrintWriter;

public class IDPair {
	protected int m_queryId;
	protected int m_pathId;
	
	public IDPair(int queryId, int pathId) {
		m_queryId = queryId;
		m_pathId = pathId;
	}
	
	public int getQueryId() {
		return m_queryId;
	}
	
	public int getPathId() {
		return m_pathId;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(m_queryId);
		sb.append("-");
		sb.append(m_pathId);
		sb.append(" ");
		return sb.toString();
	}
	
	public void print() {
		System.out.print(m_queryId+"-"+m_pathId+" ");
	}
	
	public void printToFile(PrintWriter out) {
		out.print(m_queryId+"-"+m_pathId+" ");
	}
	
	public int hashCode() {
		return m_queryId*31 + m_pathId;
	}

	public boolean equals(Object id) {
		//System.out.println("IDPair::equals is called.");
		if (id instanceof IDPair) {
			//IDPair id = (IDPair)o;
			if (m_queryId == ((IDPair)id).getQueryId() && m_pathId == ((IDPair)id).getPathId())
				return true;
			else
				return false;
		}
		return false;
	}
}
