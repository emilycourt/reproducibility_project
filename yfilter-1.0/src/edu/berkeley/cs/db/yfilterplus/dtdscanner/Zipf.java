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

package edu.berkeley.cs.db.yfilterplus.dtdscanner;

import java.util.Random;

public class Zipf {
	
	int m_size;
	double m_theta;
	double[] m_prob;  
	
	static Random m_rand = new Random();
	
	public Zipf(int size, double theta) 
	{
		m_size = size;
		m_theta = theta;
		m_prob = new double[size];
		
		int i;
		double sum = 0;
		for (i=0; i<size; i++) {
			m_prob[i]= Math.pow(1.0/(i+1), m_theta);
			sum += m_prob[i];
		}
		for (i = 0; i < size; i++) {
			m_prob[i] /= sum;
			if (i > 0)
				m_prob[i] += m_prob[i-1];	// just a trick for selecting an element
											// using a random number
		}		
	}
	
	public int getSize() {
		return m_size;
	}
	
	public double[] getProbs() {
		return m_prob;
	}
	public int probe() {
  		double r = m_rand.nextDouble() * m_prob[m_size-1];

  		for (int i=0; i<m_size; i++)
    		if (r <= m_prob[i])
      			return i;

  		System.out.println("ZIPF: There is something wrong here.");
  		return m_size;
  	}
}
