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

public class BottomStreams{
	final public boolean PREALLOCATE_STREAMS	= true;
	final protected int MAX_NUM_STREAMS			= 50000;
	final static private ArrayList m_emptyStream = new ArrayList(1);
	
	protected int m_maxNumStreams	= 0;	// for allocation only
	protected int m_numStreams		= 0;	// the number of known distinct path expressions 
	
	// a bit vector indicating if a stream is non-empty
	public BitSet m_activeStreams 	= null;
	
	//	an array of arraylists, each component array is
	//	for a stream, equivalently a distinct path expression
	// 	this data structure adapts well to query changes,
	//	esp. when new queries contain new distinct paths	
	protected ArrayList m_streams = null;	

	///////////////////
	/// constructor ///
	///////////////////
	
	public BottomStreams(int noPaths) {
		m_numStreams = noPaths;
		m_maxNumStreams = (int)Math.ceil(noPaths * 1.2);	
		//if (m_maxNumStreams < 100)
		//	m_maxNumStreams = 100;	
		
		m_activeStreams = new BitSet(m_maxNumStreams);
		
		m_streams = new ArrayList(m_maxNumStreams);
		for (int i=0; i<m_maxNumStreams; i++)
			if (PREALLOCATE_STREAMS)
				m_streams.add(new ArrayList(10));
			else
				m_streams.add(null);
	}

    //////////////////////////
    /// extend the streams ///
    //////////////////////////

    public void extendStreams(int noPaths) {
        if (noPaths > m_numStreams)  {
            m_numStreams = noPaths;
            //if the pre-allocated space has been exhausted, allocate more
            if (noPaths > m_maxNumStreams) {
                for (int i=m_maxNumStreams; i<noPaths; i++)
                    m_streams.add(null);
                m_maxNumStreams = noPaths;
            }
        }
    }

	//////////////////////////////
	/// enqueue to the streams ///
	//////////////////////////////

	public void appendStream(ArrayList records, int pathId) {
        if (pathId < 1 || pathId > m_numStreams) {
            System.out.println("BottomStreams::getStream -- Error! the path does not exist.");
            return;
        }

		/// 2. allocate the stream, if not yet
		ArrayList stream = (ArrayList)m_streams.get(pathId-1);
		if (stream == null) {
			stream = new ArrayList(10);
			m_streams.set(pathId-1, stream);
		}
		///	3. add the new path matches/records to the stream    
		stream.addAll(records);
		m_activeStreams.set(pathId - 1);
	}

	public void clear() {		
		int len = m_activeStreams.length();	// the last set bit
		for (int i = 0; i < len; i++)
			if (m_activeStreams.get(i))
				((ArrayList)m_streams.get(i)).clear();							
		m_activeStreams.clear();
	}

	public ArrayList getStream(int pathId) {
		if (pathId < 1 || pathId > m_numStreams) {
			System.out.println("BottomStreams::getStream -- Error! the path does not exist.");
			return null;
		}
        ArrayList stream = (ArrayList)m_streams.get(pathId - 1);
        if (stream == null)
            return m_emptyStream;
        else
            return stream;
	}

	public void print() {
		//int len = m_activeStreams.length();
		int len = m_numStreams;
		System.out.println(len + " streams");
		for (int i = 0; i < len; i++) {
			System.out.println("stream " + (i + 1) + ": ");
			ArrayList stream = (ArrayList)m_streams.get(i);
			if (stream != null) {
				int noRecords = stream.size();
				for (int j = 0; j < noRecords; j++)
					System.out.println(stream.get(j));
			}
		}
	}
}