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

import java.util.*;
import java.io.*;

public class DTDStatReader {
	
	/**
	 * a hashtable, for each element, stores:
	 * no of its data values (integer)
	 * its children (an array of strings with a fixed length);
	 * attributes (an array of strings with a fixed length); and 
	 * the no of values each attribute can take (an array of int's with a fixed length)
	 **/
	Hashtable m_elements;	
	
	String m_elementRoot;
	
	/**
	 * fill in the content of m_elements using dtd. it is a data structure 
	 * returned by DTDParser and contains a hashtable of all elements 
	 * and their contents, rootElement, etc.
	**/
	public DTDStatReader() 
	{
		m_elements = new Hashtable();
		m_elementRoot = null;
		
	}
	
	public void readStat(String domain) {
		
		String fileName = domain+".stat";		
		readStatFromFile(fileName);
	}

	public void readStatFromFile(String fileName) {	
		System.out.println("Reading DTD statistics from file "+fileName+"...");	
		String s;
		dtdElement element;
		String eleName = null;
		int dValues = 0;
		Vector subElements = new Vector();
		Vector attrs = new Vector();
		Vector attrRequired = new Vector();
		Vector probs = new Vector();
		Vector attrValues = new Vector();
		boolean pcData;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));				
			String line;
			int infoLine = 0;
			while((line = in.readLine()) != null) {
				line.trim();
				if (line.startsWith(new String("ROOT"))) {
					m_elementRoot = line.substring(5);
					continue;
				}
				if (line.startsWith(new String("ELEMENT"))) {
					infoLine = 1;
					continue;
				}
				if (infoLine == 1) {
					//read in the element name and its noDataValue
					StringTokenizer st = new StringTokenizer(line);
					int i = 0;
     				while (st.hasMoreTokens()) {
         				if (i == 0)
         					eleName = st.nextToken();
         				else if (i == 1)
         						dValues = Integer.parseInt(st.nextToken());
         					else
         						System.out.println("DTDStat error!");
         				i ++;
         			}
         			infoLine ++;
         			continue;
     			}
     			if (infoLine == 2) {
					//read in the child element names
					StringTokenizer st = new StringTokenizer(line);
     				while (st.hasMoreTokens()) {
         				subElements.add(st.nextToken());
         			}
         			infoLine ++;
         			continue;
     			}
     			if (infoLine == 3) {
					//read in the attributes 
					StringTokenizer st = new StringTokenizer(line);
     				while (st.hasMoreTokens()) {
         				attrs.add(st.nextToken());
         			}
         			infoLine ++;
         			continue;
     			}
     			if (infoLine == 4) {
					//read in REQUIRED				
					StringTokenizer st = new StringTokenizer(line);
     				while (st.hasMoreTokens()) {
     					String bool = st.nextToken();
     					if (bool.equals("true"))
     						attrRequired.add(new Boolean(true));
     					else
     						attrRequired.add(new Boolean(false));
        			}
        			infoLine ++;
          			continue;
     			}
     			if (infoLine == 5) {
					//read in the attributes probs				
					StringTokenizer st = new StringTokenizer(line);
     				while (st.hasMoreTokens()) {
        				probs.add(new Double(st.nextToken()));
         			}
         			infoLine ++;
          			continue;
     			}
     			if (infoLine == 6) {
					//read in attributs' no of values
					StringTokenizer st = new StringTokenizer(line);
     				while (st.hasMoreTokens()) {
         				attrValues.add(new Integer(st.nextToken()));
         			}
         			infoLine ++;
         			continue;
         		}	
         		if (infoLine == 7) {
         			// read in the pcdata for this element
         			if ((line.trim()).equals("true"))
         				pcData = true;
         			else
         				pcData = false;
         			infoLine = 0;
         			
         			element = new dtdElement(dValues, subElements, attrs, 
         				attrRequired, probs, attrValues, pcData);
         			m_elements.put(eleName, element);
         			subElements.removeAllElements();
         			attrs.removeAllElements();
         			attrRequired.removeAllElements();
         			probs.removeAllElements();
         			attrValues.removeAllElements();
     			}
     		}
			in.close();
		}
		catch  (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public Hashtable getElements() {
		if (m_elements.size()==0)
			return null;
		else
			return m_elements;
	}	

	public String getRoot() {
		return m_elementRoot;
	}
	
	public void print() {		
		System.out.println("Printing DTD statistics ...");
		System.out.println("ROOT "+m_elementRoot);		
		System.out.println();
		
		Enumeration e = m_elements.keys();
		String akey;
		dtdElement element;
		int count = 0;
		while (e.hasMoreElements()){
			akey = (String)(e.nextElement());
			element = (dtdElement)(m_elements.get(akey));
			System.out.println("ELEMENT"+count);
			System.out.println(akey+'\t'+element.m_noDataValues);
			for (int i=0; i<element.m_childElements.length; i++){
				System.out.print(element.m_childElements[i]+"\t");
			}
			System.out.println();
			for (int i=0; i<element.m_attributes.length; i++){
				System.out.print(element.m_attributes[i]+"\t");
			}
			System.out.println();
			for (int i=0; i<element.m_attrProbs.length; i++){
				System.out.print(element.m_attrProbs[i]+"\t");
			}
			System.out.println();
			for (int i=0; i<element.m_noAttrValues.length; i++){
				System.out.print(element.m_noAttrValues[i]);
				System.out.print("\t");
			}
			System.out.println();
			System.out.println();
			count++;
		}
	}
	
	public void printToFile() {
		
		System.out.println("Printing DTD statistics to file DTDStat.tmp.txt...");
		
		try {
			PrintWriter fileOut = new PrintWriter(new FileOutputStream("DTDStat.tmp.txt"));
			fileOut.println("ROOT "+m_elementRoot);
			fileOut.println();
			Enumeration e = m_elements.keys();
			String akey;
			dtdElement element;
			int count = 0;
			while (e.hasMoreElements()){
				akey = (String)(e.nextElement());
				element = (dtdElement)(m_elements.get(akey));
				fileOut.println("ELEMENT "+count);
				fileOut.println(akey+'\t'+element.m_noDataValues);
				for (int i=0; i<element.m_childElements.length; i++){
					fileOut.print(element.m_childElements[i]+"\t");
				}
				fileOut.println();
				for (int i=0; i<element.m_attributes.length; i++){
					fileOut.print(element.m_attributes[i]+"\t");
				}
				fileOut.println();
				for (int i=0; i<element.m_attrProbs.length; i++){
					fileOut.print(element.m_attrProbs[i]+"\t");
				}
				fileOut.println();
				for (int i=0; i<element.m_noAttrValues.length; i++){
					fileOut.print(element.m_noAttrValues[i]);
					fileOut.print("\t");
				}
				fileOut.println();
				fileOut.println();
				count++;
			}
			fileOut.close();
		}
		catch  (Exception e){
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		
		DTDStatReader stat = new DTDStatReader();
		stat.readStatFromFile(args[0]);
		stat.getElements();
		stat.getRoot();
		stat.printToFile();
		
	}
	

}
