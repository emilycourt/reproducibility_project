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

import com.wutka.dtd.DTDParser;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDSequence;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDContainer;
import com.wutka.dtd.DTDPCData;

import java.util.*;

public class dtdElement {
	
	protected int m_noDataValues;
	protected String[] m_childElements;
	protected Zipf m_zipf;
	protected String[] m_attributes;
	protected boolean[] m_attrRequired;
	protected double[] m_attrProbs;
	protected int[] m_noAttrValues;
	protected boolean m_pcData;
        static Random m_rand = new Random();
	//protected double m_attrProbAvg;
	
	public dtdElement(HashSet children, Hashtable attributes, boolean pcData, int maxValue) {
		//System.out.println("dtdElement::dtdElement is called.");
		//m_selfProb = m_rand.nextDouble();
		
		if (maxValue == -1)
		    m_noDataValues = (int)Math.ceil(m_rand.nextDouble() * 20);
		else 
		    m_noDataValues = maxValue;
		m_childElements	= new String[children.size()];
		Iterator iter =	children.iterator();
		int i =	0;
		while (iter.hasNext()){
			m_childElements[i++] = (String)(iter.next());
		}
		// sort by the attribute name
		TreeMap attrs = new TreeMap();
		Enumeration e =	attributes.keys();
		while (e.hasMoreElements()){
			String attrName	= (String)(e.nextElement());
			attrs.put(attrName, attributes.get(attrName));
		}
			
		int size = attrs.size();
		m_attributes = new String[size];
		m_attrRequired = new boolean[size];
		m_attrProbs = new double[size];
		m_noAttrValues = new int[size];
		Set s =	attrs.keySet();
		iter = s.iterator();
		i = 0;
		while (iter.hasNext()){
			String attrName	= (String)(iter.next());
			m_attributes[i]	= attrName;
			
			DTDAttribute dtdAttr =	(DTDAttribute)attrs.get(attrName);
			DTDDecl	dtdDecl	= dtdAttr.getDecl();
			boolean	fixed =	false;
			if (dtdDecl.equals(dtdDecl.FIXED)) {
				fixed = true;
				m_attrRequired[i] = true;
			}
			else if (dtdDecl.equals(dtdDecl.REQUIRED)) {
				m_attrRequired[i] = true;
			}
			else if	(dtdDecl.equals(dtdDecl.IMPLIED))
				m_attrRequired[i] = false;
			else
				m_attrRequired[i] = false;
			
			if (fixed) {
				m_attrProbs[i] = 1;
				m_noAttrValues[i] = 1;
			}
			else {
				m_attrProbs[i] = m_rand.nextDouble();
				if (maxValue == -1)
				    m_noDataValues = (int)Math.ceil(m_rand.nextDouble() * 20);
				else 
				    m_noAttrValues[i] = maxValue;
			}
					// randomly choose a number in the range [1,20]
			i++;
		}
		m_pcData = pcData;
	}

	public dtdElement(HashSet children, Hashtable attributes, boolean pcData) {
		//System.out.println("dtdElement::dtdElement is called.");
		//m_selfProb = m_rand.nextDouble();
		
		m_noDataValues = (int)Math.ceil(m_rand.nextDouble() * 20);
		m_childElements	= new String[children.size()];
		Iterator iter =	children.iterator();
		int i =	0;
		while (iter.hasNext()){
			m_childElements[i++] = (String)(iter.next());
		}
		// sort by the attribute name
		TreeMap attrs = new TreeMap();
		Enumeration e =	attributes.keys();
		while (e.hasMoreElements()){
			String attrName	= (String)(e.nextElement());
			attrs.put(attrName, attributes.get(attrName));
		}
			
		int size = attrs.size();
		m_attributes = new String[size];
		m_attrRequired = new boolean[size];
		m_attrProbs = new double[size];
		m_noAttrValues = new int[size];
		Set s =	attrs.keySet();
		iter = s.iterator();
		i = 0;
		while (iter.hasNext()){
			String attrName	= (String)(iter.next());
			m_attributes[i]	= attrName;
			
			DTDAttribute dtdAttr =	(DTDAttribute)attrs.get(attrName);
			DTDDecl	dtdDecl	= dtdAttr.getDecl();
			boolean	fixed =	false;
			if (dtdDecl.equals(dtdDecl.FIXED)) {
				fixed = true;
				m_attrRequired[i] = true;
			}
			else if (dtdDecl.equals(dtdDecl.REQUIRED)) {
				m_attrRequired[i] = true;
			}
			else if	(dtdDecl.equals(dtdDecl.IMPLIED))
				m_attrRequired[i] = false;
			else
				m_attrRequired[i] = false;
			
			if (fixed) {
				m_attrProbs[i] = 1;
				m_noAttrValues[i] = 1;
			}
			else {
				m_attrProbs[i] = m_rand.nextDouble();
				m_noAttrValues[i] = (int)Math.ceil(m_rand.nextDouble() * 20);
			}
					// randomly choose a number in the range [1,20]
			i++;
		}
		m_pcData = pcData;
	}
			
	public dtdElement(int dValues, Vector subElements, 
		Vector attrs, Vector attrRequired, 
		Vector attrProbs, Vector attrValues,
		boolean pcData) {
		
		m_noDataValues = dValues;
		m_childElements = new String[subElements.size()];
		subElements.copyInto(m_childElements);
		m_attributes = new String[attrs.size()];
		attrs.copyInto(m_attributes);
		int size = attrs.size();
		m_attrRequired = new boolean[size];
		m_attrProbs = new double[size];
		m_noAttrValues = new int[size];
		for (int i=0; i<size; i++) {
			// when it is called by DTDStatReader, the probs have not been
			// scaled by the workload parameter P, the avg attr prob of occurrence
			m_attrRequired[i] = ((Boolean)attrRequired.get(i)).booleanValue();
			m_attrProbs[i] = ((Double)(attrProbs.get(i))).doubleValue();
			m_noAttrValues[i] = ((Integer)(attrValues.get(i))).intValue();
		}
		m_pcData = pcData;
		//attrValues.copyInto(m_noAttrValues);
			  		
		m_zipf = null;
		//m_attrProbAvg = -1;
	}
	
	public int getSizeOfChildren() {
		return m_childElements.length;
	}
	
	public String[] getChildren() {
		return m_childElements;
	}
	
	public String getChild(int childIndex) {
		return m_childElements[childIndex];
	}
	
	public int getNoDataValues() {
		return m_noDataValues;
	}
	
	public String[] getAttributes() {
		return m_attributes;
	}
	
	public boolean[] getAttrRequired() {
		return m_attrRequired;
	}
	
	public double[] getAttrProbs() {
		return m_attrProbs;
	}

	public int[] getAttrValues() {
		return m_noAttrValues;
	}
	
	public int getAttrIndex(String attr) {
		int len = m_attributes.length;
		int i;
		for (i=0; i<len; i++)
			if (attr.equals(m_attributes[i]))
				break;
		if (i<len)
			return i;
		else {
			//System.out.println("Error! XMLGenerator generates an attribute not belonging to the element.");
			return -1;
		}
	}
	
  public int getSizeOfNoAttrValues() {
    return m_noAttrValues.length;
  }

	public int getNoAttrValues(int index) {
		return m_noAttrValues[index];
	}
	
	public double getAttrProb(int index) {
		return m_attrProbs[index];
	}
	
	public boolean getPCData() {
		return m_pcData;
	}
	
	public void setZipf(Zipf z) {
		m_zipf = z;
	}
	
	public Zipf getZipf() {
		return m_zipf;
	}
	
	public void print(String eleName) {
		if (eleName.equals("nitf")) {
			System.out.println(m_attributes[0]);
			System.out.println(m_attrProbs[0]);
			System.out.println(m_noAttrValues[0]);
		}
	}
			
}
	
