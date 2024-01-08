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

/*

Dictionary encoding implemented by Shariq Rizvi
The idea is to create a mapping between (element + attribute) names
and a more space-efficient set, like {1, 2, ...}, while the document is parsed.

*/


package edu.berkeley.cs.db.yfilterplus.xmltree;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
//import org.xml.sax.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;

import java.io.*;
import java.util.*;

public class XMLTree extends DefaultHandler {
    //// data structures will be used in later XML processing ////
    protected Hashtable m_eventIndex 	= null;    
    protected ArrayList m_eventSequence = null;
    protected boolean m_indexing 		= true;	// default: construct the two indexes above

    //// temporary data structures for constructing the tree ////
    protected Stack m_contextPath 		= null;
    //protected boolean m_data = false;
    protected int m_eventId;
    protected boolean m_trace 			= false;
    protected PrintStream m_out 		= System.out;

    /// schema-based dictionary encoding ///
    /* Hashmap for learning a dictionary encoding */
    /* This will only be a reference, the actual object will be passed by the caller */
    private HashMap dictionary;

    /* Not everyone may need dictionary encoding */
    private boolean dictionaryEncodingNeeded = false;

    /// internal use ///
    private boolean TEST_TEXT_STRING = false;

    ///////////////////
    /// constructor ///
    ///////////////////

    public XMLTree(StringReader in) {
        m_eventSequence = new ArrayList();
        m_eventIndex = new Hashtable();
        m_contextPath = new Stack();
        startParsing(in);
    }


    public XMLTree(StringReader in, boolean dictionaryEncodingNeeded, HashMap dictionary) {
        this.dictionaryEncodingNeeded =  dictionaryEncodingNeeded;
		if(dictionaryEncodingNeeded) {
	    	this.dictionary = dictionary;
		}
        m_eventSequence = new ArrayList();
        m_eventIndex = new Hashtable();
        m_contextPath = new Stack();
        startParsing(in);
    }

    public XMLTree(String filename) {
        m_eventSequence = new ArrayList();
        m_eventIndex = new Hashtable();
        m_contextPath = new Stack();
        startParsing(filename);
    }

    public XMLTree(String filename, boolean dictionaryEncodingNeeded, HashMap dictionary) {
        this.dictionaryEncodingNeeded =  dictionaryEncodingNeeded;
		if(dictionaryEncodingNeeded) {
	    	this.dictionary = dictionary;
		}
        m_eventSequence = new ArrayList();
        m_eventIndex = new Hashtable();
        m_contextPath = new Stack();
        startParsing(filename);
    }

    public XMLTree(Stack contextPath) {
		m_contextPath = contextPath;
		m_indexing = false;		
    }
	
    //////////////////////////
    /// driver for parsing ///
    //////////////////////////
	
    protected void startParsing(StringReader in)
    {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature( "http://xml.org/sax/features/validation",false);
            factory.setFeature( "http://xml.org/sax/features/namespace-prefixes",true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(in), this);

            //XMLReader parser = new org.apache.xerces.parsers.SAXParser();
            //parser.setFeature( "http://xml.org/sax/features/validation",true);
            //parser.setFeature( "http://xml.org/sax/features/namespace-prefixes",true);
            //parser.setContentHandler(this);
            //parser.parse(new InputSource(in));
        }
        catch (Exception e)
	    {
			e.printStackTrace(System.err);
	    }
    }

    protected void startParsing(String filename)
    {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature( "http://xml.org/sax/features/validation",false);
            factory.setFeature( "http://xml.org/sax/features/namespace-prefixes",true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(filename, this);

            //XMLReader parser = new org.apache.xerces.parsers.SAXParser();
            //parser.setFeature( "http://xml.org/sax/features/validation",false);
            //parser.setFeature( "http://xml.org/sax/features/namespace-prefixes",true);
            //parser.setContentHandler(this);
            //parser.parse(filename);
        }
        catch (Exception e)
	    {
			e.printStackTrace(System.err);
	    }
    }

    ////////////////////////////////////////
    /// handlers for event-based parsing ///
    ////////////////////////////////////////
	
    public void startDocument() {
        if (m_trace)
	    {
			m_out.println("<?xml start document?>");
			//m_out.flush();
	    }
        m_eventId = 0;
    }

    /**
     * DocumentHandler :: start element:
     **/
    public void startElement(String uri, String local,
                             String elementName , Attributes attrs)
    {
		//System.out.println("XMLTree::startElement is called for "+elementName+".");
		if (m_trace) {
			m_out.print('<');
			m_out.print(elementName);
			//System.out.println("\n************Seen element name " + elementName);

			if (attrs != null) {
				int len = attrs.getLength();
				for (int i = 0; i < len; i++) {
					m_out.print(' ');
					m_out.print(attrs.getQName(i));
					m_out.print("=\"");
					m_out.print(attrs.getValue(i));
					m_out.print('"');
				}
			}
			m_out.print(">\n");
			//m_out.flush();
		}

		/// handle the element name ///
		if (dictionaryEncodingNeeded) {
			if (dictionary.containsKey(elementName) == false) {
				dictionary.put(elementName, "" + dictionary.size());
				//System.out.println("Had not seen " + elementName + ", mapped now to " + dictionary.get(elementName));				
			}
			//else 
			//	System.out.println("Had seen " + elementName + ", mapped now to " + dictionary.get(elementName));			
		}

        //// get attributes and their values ////
        int noAttrs = attrs.getLength();
        HashMap attributes = null;
        String[] orderedAttrNames = null;
        if (noAttrs > 0) {
	    	attributes = new HashMap((int)Math.ceil(noAttrs/0.75));
	    	orderedAttrNames = new String[noAttrs];
	    	for (int i=0; i<noAttrs; i++) {
            	String attrName = attrs.getQName(i);
            	orderedAttrNames[i] = attrName;	
            	//System.out.println("\n************Seen attribute name " + attrName);

            	if(dictionaryEncodingNeeded) {
		    		if(dictionary.containsKey(attrName) == false) {
						dictionary.put(attrName, "" + dictionary.size());
						//System.out.println("Had not  seen " + attrName + ", mapped now to " + dictionary.get(attrName));
		    		}
		    		//else 
		    		//	System.out.println("Have seen " + attrName + " already, mapped to " + dictionary.get(attrName));                
            	}
            	if(dictionaryEncodingNeeded) 
		    		attributes.put(dictionary.get(attrName), attrs.getValue(i));            	
            	else 
		    		attributes.put(attrs.getQName(i), attrs.getValue(i));            
	    	}
        }
        
        //// get the current position and parentId ////
        int position, parentId;
        ParsingContext parent = null;
        if (m_contextPath.size()>0) {
            // the parent exists
            parent = (ParsingContext)m_contextPath.peek();
            position = parent.getChildPosition(elementName);
            parentId = parent.getEventId();
        }
        else {				// this is the root element of the document
            position = 1;
            parentId = -1;
        }

        //// record the current parsing context ////
        ParsingContext c;

        if(dictionaryEncodingNeeded) {
            c = new ParsingContext((String)dictionary.get(elementName),
				   m_eventId, parentId,
				   attributes, orderedAttrNames, position);
        }
        else {
            c = new ParsingContext(elementName,
				   m_eventId, parentId,
				   attributes, orderedAttrNames, position);
        }

        if (parent != null)
            parent.addChild(c);
        m_contextPath.push(c);
        if (m_indexing) {        
	    	m_eventSequence.add(c);
	    	m_eventIndex.put(new Integer(m_eventId),
			     new Integer(m_eventSequence.size()-1));
        }
        m_eventId++;
        //m_data = true;

        //System.out.println("XMLTree::startElement is ended");
    }

    /**
     * DocumentHandler :: characters:
     **/
	public void characters(char ch[], int start, int count) {
		//if (m_data == false)	// not data for elements
		//	return;
        if (TEST_TEXT_STRING) {
            System.out.print("XMLTree::characters -- ");
            System.out.print(count+" char(s) "+"\"");
            for (int i=0; i<count; i++)
                System.out.print(ch[i]);
            System.out.println("\"");
        }

		String data = new String(ch, start, count);
        if (TEST_TEXT_STRING)
            System.out.println(" data \""+data+"\"");

		boolean non_whitespace = false;
		int len = data.length();
		for (int i = 0; i < len; i++) {
			if (data.charAt(i) != ' '
				&& data.charAt(i) != '\t'
				&& data.charAt(i) != '\n')
				non_whitespace = true;
		}

		if (m_trace) {
			m_out.print("\"" + data + "\"");
			if (non_whitespace == false)
				m_out.print("\t --> turned to null");
			m_out.println();
			//m_out.flush();
		}

		// the top of the stack correspond to the state
		// after reading element but the predicates for data
		// are stored with the element in the previous state
		if (non_whitespace) {
			ParsingContext context = (ParsingContext) m_contextPath.peek();
			context.appendElementData(data);
		}

		//System.out.println("XMLTree::characters is ended");
	}

    /**
     * DocumentHandler :: end element:
     **/
	public void endElement(String uri, String local, String eleName) {
		//System.out.println("XMLTree::endElement is called.");
		if (m_trace) {
			m_out.print("</" + eleName);
			m_out.print(">\n");
			//m_out.flush();
		}

		m_contextPath.pop();
		if (m_indexing) {
			if (dictionaryEncodingNeeded) {
				if (dictionary.containsKey("<end>")) {
					//System.out.println("Have seen " + attrName + " already, mapped to " + dictionary.get(attrName));
				} else {
					dictionary.put("<end>", "" + dictionary.size());
					//System.out.println("Had not  seen " + attrName + ", mapped now to " + dictionary.get(attrName));
				}
				m_eventSequence.add(
					new ParsingContext((String) dictionary.get("<end>")));
			} else {
				m_eventSequence.add(new ParsingContext("<end>"));
			}
		}
		//m_data = false;

		//System.out.println("XMLTree:endElement is ended");
	}

    public void endDocument() {
        //System.out.println("XMLTree::endDocument is called");
        m_contextPath.clear();
        //System.out.println("XMLTree::endDocument is ended");
    }

    ///////////////
    /// getters ///
    ///////////////
	
    public ArrayList getEvents() {
        return m_eventSequence;
    }

    public Hashtable getEventIndex() {
        return m_eventIndex;
    }

	/////////////////////////////////////
	/// utility for document analysis ///
	/////////////////////////////////////
    
    /**
     * this function measures(a) the doc length, (b) the maximal depth, 
     * and (c) the average path depth for the current xml tree. it writes
     * (a) to the lengthArray, (b) to the depthArray, at the position 
     * specified by the input parameter index, and returns (c).
     */
	public double analyzeXMLTree(int[] lengthArray, int[] depthArray, int index) {
		int docLength = 0;
		int docDepth = 0;
		double pathDepths = 0;
		int noPaths = 0;

		int size = m_eventSequence.size();
		for (int i=0; i<size; i++) {
			ParsingContext c = (ParsingContext)m_eventSequence.get(i);
			if (!c.isEndElement()) {	// start element
				m_contextPath.push(c);
				docLength++;
				if (m_contextPath.size() > docDepth)
					docDepth = m_contextPath.size();
				if (i < size-1 && ((ParsingContext)m_eventSequence.get(i+1)).isEndElement()) {
					// this is the last start element before an end element
					pathDepths += m_contextPath.size();
					noPaths++;
				}
			}
			else {
				m_contextPath.pop();
			}
		}

		lengthArray[index] = docLength;
		depthArray[index] = docDepth;

		/// quick clearing ///
		m_contextPath.clear();

		return pathDepths/noPaths;
	}

    //////////////
    /// output ///
    //////////////
	
	public void print() {
		//// print m_events out ////
		System.out.println("**************** all events ****************");
		int size = m_eventSequence.size();
		for (int i = 0; i < size; i++) {
			System.out.print("index " + i + ": ");
			System.out.println(m_eventSequence.get(i));
			System.out.println();
		}

		//// print the index m_eventIndex out ////
		System.out.println(
			"**************** index on the events ****************");
		System.out.println("element id \t:\tindex in the element stream");
		Iterator iter = m_eventIndex.keySet().iterator();
		while (iter.hasNext()) {
			Integer eventId = (Integer) iter.next();
			Integer elementId = (Integer) m_eventIndex.get(eventId);
			System.out.println(eventId + "\t\t:\t" + elementId);
		}
	}

    ////////////
    /// main ///
    ////////////
	
	public static void main(String[] args) {
		String xmlFile = args[0];
		XMLTree tree = new XMLTree(xmlFile);
		tree.print();
	}
    
}
