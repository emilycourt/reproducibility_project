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
import java.io.*;

public class DTDStat {

    /**
     * we want the following information for each element:
     * element name, its child elements	(the number as well), its attributes
     * prob. of	element	occuring is taken care of by the zipf's	distribution
     * prob. of	attribute occuring is taken care of by parameters P and	D
     * no. of values the element's data	can take is chosen randomly
     * no of values an attribute can take is also chosen randomly
     * In real use, we can assume these	values are determined by the applications.
     **/

    // private DTD m_dtd = null;

    /**
     * a hashtable, for	each element, stores:
     * no of its data values (integer)
     * its children (an	array of strings with a	fixed length);
     * attributes (an array of strings with a fixed length); and
     * the no of values	each attribute can take	(an array of int's with	a fixed	length)
     **/
    Hashtable m_elements;
    String m_elementRoot;
    DTD m_dtd;
    Random m_rand = new	Random();

    int m_noAllPredicates = 0;
    int m_noElements = 0;

    public DTDStat(String dtdFilename, int maxValue) {
        try {
            DTDParser parser = new DTDParser(new File(dtdFilename), false);
            m_dtd = parser.parse(true);

            if (m_dtd == null) {
                System.out.println("dtd are null.");
                return;
            }

            if (m_dtd.rootElement == null) {
                System.out.println("dtd.rootElement is null.");
                BufferedReader br = new BufferedReader(new FileReader(dtdFilename));
                String line;
                m_elementRoot =	null;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens() != true) {
                        continue;
                    }

                    String token = st.nextToken();
                    if (token.startsWith("<!ELEMENT")) {
                        m_elementRoot = st.nextToken();
                        break;
                    }
                }
            }
            else {
                m_elementRoot =	m_dtd.rootElement.name;
            }
            //System.out.println("Root is " + m_elementRoot);
            m_elements = new Hashtable();

            String elementKey;
            DTDElement element;
            HashSet subElements;

            if (m_dtd.elements == null) {
                System.out.println("dtd.elements are null.");
                return;
            }
            if (m_dtd.elements.keys() == null) {
                System.out.println("dtd.elements.keys are null.");
                return;
            }

            //System.out.println("So far so good.");
            Enumeration	e =	m_dtd.elements.keys();
            while (e.hasMoreElements()) {
                elementKey = (String)(e.nextElement());
                //System.out.println(elementKey);
                element	= (DTDElement)(m_dtd.elements.get(elementKey));
                subElements = new HashSet();	// for each	element, store all its children	here
                boolean	pcData = dumpElementSuccessor(element.content, subElements);
                dtdElement dtdEle = new dtdElement(subElements, element.attributes, pcData, maxValue);
                m_elements.put(elementKey, dtdEle);
                m_noElements ++;
                //m_noAllPredicates += (element.attributes.size() + 2);
                m_noAllPredicates += element.attributes.size();
                //if (elementKey.equals("nitf"))
                //	dtdEle.print();
            }


            // print the stat out to a file
            //this.printToFile("DTDStats.txt");
            this.reportStat();
        }  catch (Exception	e) {
            e.printStackTrace(System.out);
        }
    }

    public DTDStat(String dtdFilename) {
        try {
            DTDParser parser = new DTDParser(new File(dtdFilename), false);
            m_dtd = parser.parse(true);

            if (m_dtd == null) {
                System.out.println("dtd are null.");
                return;
            }

            if (m_dtd.rootElement == null) {
                System.out.println("dtd.rootElement is null.");
                BufferedReader br = new BufferedReader(new FileReader(dtdFilename));
                String line;
                m_elementRoot =	null;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens() != true) {
                        continue;
                    }

                    String token = st.nextToken();
                    if (token.startsWith("<!ELEMENT")) {
                        m_elementRoot = st.nextToken();
                        break;
                    }
                }
            }
            else {
                m_elementRoot =	m_dtd.rootElement.name;
            }
            //System.out.println("Root is " + m_elementRoot);
            m_elements = new Hashtable();

            String elementKey;
            DTDElement element;
            HashSet subElements;

            if (m_dtd.elements == null) {
                System.out.println("dtd.elements are null.");
                return;
            }
            if (m_dtd.elements.keys() == null) {
                System.out.println("dtd.elements.keys are null.");
                return;
            }

            //System.out.println("So far so good.");
            Enumeration	e =	m_dtd.elements.keys();
            while (e.hasMoreElements()){
                elementKey = (String)(e.nextElement());
                //System.out.println(elementKey);
                element	= (DTDElement)(m_dtd.elements.get(elementKey));
                subElements = new HashSet();	// for each	element, store all its children	here
                boolean	pcData = dumpElementSuccessor(element.content, subElements);
                dtdElement dtdEle = new dtdElement(subElements, element.attributes, pcData);
                m_elements.put(elementKey, dtdEle);
                m_noElements ++;
                //m_noAllPredicates += (element.attributes.size() + 2);
                m_noAllPredicates += element.attributes.size();
                //if (elementKey.equals("nitf"))
                //	dtdEle.print();
            }


            // print the stat out to a file
            //this.printToFile("DTDStats.txt");
            this.reportStat();
        }  catch (Exception	e) {
            e.printStackTrace(System.out);
        }
    }


    /**
     * Fills in the content of m_elements using dtd. it is a data structure
     * returned	by DTDParser and contains a hashtable of all elements
     * and their contents, rootElement,	etc. values of attributes or elements
     * have value from 1 to a random number between [1,20].
     **/
    public DTDStat(DTD dtd, int maxValue)
    {
        //System.out.println("DTDStat::DTDStat is called.");
        if (dtd == null) {
            System.out.println("dtd are null.");
            return;
        }
        if (dtd.rootElement == null) {
            System.out.println("dtd.rootElement is null.");
            m_elementRoot = null;
        }
        else
            m_elementRoot = dtd.rootElement.name;

        m_elements = new Hashtable();

        String elementKey;
        DTDElement element;
        HashSet	subElements;

        if (dtd.elements == null) {
            System.out.println("dtd.elements are null.");
            return;
        }
        if (dtd.elements.keys() == null) {
            System.out.println("dtd.elements.keys are null.");
            return;
        }

        //System.out.println("So far so good.");
        Enumeration e =	dtd.elements.keys();
        while (e.hasMoreElements()){
            elementKey = (String)(e.nextElement());
            //System.out.println(elementKey);
            element = (DTDElement)(dtd.elements.get(elementKey));
            subElements	= new HashSet();	// for each	element, store all its children	here
            boolean pcData = dumpElementSuccessor(element.content, subElements);
            dtdElement dtdEle = new dtdElement(subElements, element.attributes, pcData);
            m_elements.put(elementKey, dtdEle);
            m_noElements ++;
            m_noAllPredicates += (element.attributes.size() + 2);
            //if (elementKey.equals("nitf"))
            //	dtdEle.print();
        }

    }

    public String getRoot() {
        return m_elementRoot;
    }

    public Hashtable getElements() {
        return m_elements;
    }

    public DTD getDTD() {
        return m_dtd;
    }

    public String getReportStat() {
        return "No. of elements: " + m_noElements + "\nNo. of all attributes: " +
                m_noAllPredicates + "\nAvg no. of elements: " + (m_noAllPredicates)/(double)m_noElements + "\n";
    }

    public void reportStat() {
        System.out.println("No of elements: "+m_noElements);
        System.out.println("No of all attributes: "+m_noAllPredicates);
        System.out.println("Avg no. of elements: "+(m_noAllPredicates)/(double)m_noElements);
    }

    /**
     * This	function adds all children names to	a hashset hs
     * It is a recursive function because the content of an	element
     * can be nested, e.g. (a, (b|c)?, c)+
     **/
    private boolean dumpElementSuccessor(DTDItem item, HashSet hs)
    {
        //System.out.println("DTDStat::dumpElementSuccessor is called.");
        if (item == null)
            return false;
        boolean	pcData = false;
        if (item instanceof DTDPCData) {
            pcData = true;
        }
        else if	(item instanceof DTDName) {		// a ? or a+
            hs.add(((DTDName)item).value);
        }
        else if	(item instanceof DTDChoice)	// (a|b)? or (a|b)+
        {
            DTDItem[] items	= ((DTDChoice) item).getItems();
            for (int i=0; i	< items.length;	i++) {
                boolean result = dumpElementSuccessor(items[i], hs);
                if (result)
                    pcData = true;
            }
        }
        else if	(item instanceof DTDSequence)	// (a,b)? or (a,b)+
        {
            DTDItem[] items	= ((DTDSequence) item).getItems();
            for (int i=0; i < items.length; i++) {
                boolean result = dumpElementSuccessor(items[i], hs);
                if (result)
                    pcData = true;
            }
        }
        else if	(item instanceof DTDMixed)	// (a|b|#PCDATA)? or (a|b|#PCDATA)+
        {
            DTDItem[] items	= ((DTDMixed) item).getItems();
            for (int i=0; i < items.length;	i++) {
                boolean result = dumpElementSuccessor(items[i], hs);
                if (result)
                    pcData = true;
            }
        }
        return pcData;
    }

    public void	printToFile(String filename) {

        System.out.println("Saving DTD statistics to file " + filename + "...");

        try	{
            PrintWriter	fileOut	= new PrintWriter(new FileOutputStream(filename));
            fileOut.println("ROOT "+m_elementRoot);
            fileOut.println();
            Enumeration	e =	m_elements.keys();
            String akey;
            dtdElement element;
            int	count =	0;
            while (e.hasMoreElements()){
                akey = (String)(e.nextElement());
                element	= (dtdElement)(m_elements.get(akey));

                fileOut.println("ELEMENT "+count);
                fileOut.println(akey+"\t"+element.m_noDataValues);
                for (int i=0; i<element.m_childElements.length;	i++){
                    fileOut.print(element.m_childElements[i]+"\t");
                }
                fileOut.println();
                for (int i=0; i<element.m_attributes.length; i++){
                    fileOut.print(element.m_attributes[i]+"\t");
                }
                fileOut.println();
                for (int i=0; i<element.m_attributes.length; i++){
                    fileOut.print(element.m_attrRequired[i]+"\t");
                }
                fileOut.println();
                for (int i=0; i<element.m_attrProbs.length;	i++){
                    fileOut.print(element.m_attrProbs[i]+"\t");
                }
                fileOut.println();
                for (int i=0; i<element.m_noAttrValues.length; i++){
                    fileOut.print(element.m_noAttrValues[i]+"\t");
                }
                fileOut.println();
                fileOut.println(element.m_pcData);
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
        //  new DTDStat(args[0]);

        /*
        try {
        DTDParser parser = new DTDParser(new File(args[0]),	false);
        DTD	dtd = parser.parse(true);
        DTDStat stat = null;
        if (dtd != null)
        stat = new DTDStat(dtd);
        else {
        System.out.println("Error! DTDParser cannot parse "+args[0]+".");
        return;
        }
        // print the stat out to a file
        stat.printToFile(args[1]);
        stat.reportStat();
        }  catch (Exception e)
        {
        e.printStackTrace(System.out);
        }
        */
        try {
        	int MAX_VALUE_DEFAULT  = 20;
            int maxValue = MAX_VALUE_DEFAULT;
            // which means default setting, randomly generated between 1 and 20
            if (args.length == 3)
                maxValue = Integer.parseInt(args[2]);
            if (maxValue < 0)
            	maxValue = MAX_VALUE_DEFAULT;
            	
            DTDStat stat = new DTDStat(args[0], maxValue);
            // print the stat out to a file
            stat.printToFile(args[1]);
            //stat.reportStat();
        }  catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
}
