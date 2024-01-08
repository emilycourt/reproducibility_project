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

package edu.berkeley.cs.db.yfilterplus.xmltree;

import java.util.*;
import java.io.*;

public class ParsingContext implements Serializable {
    String m_eleName						= null;
    transient short m_Id;
    transient short m_parentId;
    HashMap m_attributes					= null;	// a map for efficient attr name lookup
	transient String[] m_orderedAttrNames	= null;	// keep the original order for output
    short m_position;								// position of the element in its parent
    String m_eleData 						= null;	// the first text node;  
    transient boolean m_mixedContent 		= false;// if mixed content,use m_allChildren
	transient ArrayList m_allChildren 		= null; // contain text and element nodes (XPath data model)
    transient Hashtable m_childPosition 	= null;	// allocated when a child is read
    transient LinkedList m_children 		= null;	// contain only child element nodes

    /// internal use ///
    private transient boolean m_trimData            = false;

    ///////////////////
    //* constructor *//
    ///////////////////
    
    public ParsingContext() {
        // for the end of an element or a dummy object
        m_eleName = "";
        m_Id = -1;
        m_parentId = -1;
        m_attributes = null;
        m_position = -1;
    }

    public ParsingContext(String eleName) {
        // for the end of an element or a dummy object
        m_eleName = eleName;
        m_Id = -1;
        m_parentId = -1;
        m_attributes = null;
        m_position = -1;
    }

    public ParsingContext(String eleName, int id) {
        // for the end of an element or a dummy object
        m_eleName = eleName;
        m_Id = (short) id;
        m_parentId = -1;
        m_attributes = null;
        m_position = -1;
    }

    public ParsingContext(String eleName, int id,
                          HashMap attrs, int position) {
        m_eleName = eleName;
        m_Id = (short) id;
        m_parentId = -1;
        m_attributes = attrs;
        m_position = (short) position;
    }

	public ParsingContext(	String eleName, int id, int parentId,
							HashMap attrs, int position) {
		m_eleName = eleName;
		m_Id = (short) id;
		m_parentId = (short) parentId;
		m_attributes = attrs;
		m_position = (short) position;		
	}
	
	public ParsingContext(	String eleName, int id, int parentId,
							HashMap attrs, String[] orderedAttrNames, int position) {
		m_eleName = eleName;
		m_Id = (short) id;
		m_parentId = (short) parentId;
		m_attributes = attrs;
		m_orderedAttrNames = orderedAttrNames;
		m_position = (short) position;		
	}

	public ParsingContext(	String eleName, int id, 
							HashMap attrs, int position, String data) {
		m_eleName = eleName;
		m_Id = (short) id;
		m_parentId = -1;
		m_attributes = attrs;
		m_position = (short) position;		
		m_eleData = data;
	}

    ////////////////////////
    //* element checking *//
    ////////////////////////

    public boolean isDummy() {
        if (m_eleName.equals("dummy") || m_Id == -1)
            return true;
        return false;
    }

    public boolean isEndElement() {
        if (m_eleName.equals("<end>"))
            return true;
        else
            return false;
    }

	//////////////////////////
	//* element comparison *//
	//////////////////////////
	
	public int compareTo(ParsingContext c) {
		int id = c.getEventId();
		if (m_Id < id)
			return -1;
		else if (m_Id == id)
			return 0;
		else
			return 1;
	}

    ////////////////////////////////
    //* setting member variables *//
    ////////////////////////////////

    public void appendElementData(String textNode) {
        if (textNode.startsWith("\n") || textNode.endsWith("\n"))
            m_trimData = true;

		// first text node
        if (m_eleData == null)        	
            m_eleData = textNode;

		// handle the mixed content
		if (m_mixedContent == false) {		
			if (m_children != null && m_children.size() >0) {
				// mixed content: a number of child elements followed by the first text node
				m_mixedContent = true;
				m_allChildren = new ArrayList();
				m_allChildren.addAll(m_children);
				m_allChildren.add(textNode);
			}
		}
		else
			m_allChildren.add(textNode);            
    }

    public void addChild(ParsingContext c) {
        if (m_children == null)
            m_children = new LinkedList();
        m_children.add(c);
        
        // handle the mixed content
		if (m_mixedContent == false) {		
			if (m_eleData != null) {
				// mixed content: text() followed by the first child element
				m_mixedContent = true;
				m_allChildren = new ArrayList();
				m_allChildren.add((String)m_eleData);
				m_allChildren.add(c);
			}
		}
		else
			m_allChildren.add(c);
    }

    ///////////////////////////
    //* retrive information *//
    ///////////////////////////

    public int getChildPosition(String childName) {
        // allocate the hashtable, if null
        if (m_childPosition == null)
            m_childPosition = new Hashtable(15);
        int nextPosition;
        if (m_childPosition.containsKey(childName)) {
            Integer curPosition = (Integer)m_childPosition.get(childName);
            nextPosition = curPosition.intValue()+1;
            m_childPosition.put(childName, new Integer(nextPosition));
        }
        else {
            nextPosition = 1;
            m_childPosition.put(childName, new Integer(1));
        }
        return nextPosition;
    }

    public LinkedList getChildren() {
        return m_children;
    }

    public int getPosition() {
        return  (int)m_position;
    }

    public short getPositionShort() {
        return m_position;
    }

    public String getElementName() {
        return m_eleName;
    }

    public Map getAttributes() {
        return m_attributes;
    }

    public String getAttributeValue(String attrName) {
        if (m_attributes == null)
        	return null;
        return (String)m_attributes.get(attrName);
    }

	/**
	 * this function returns the string-value of an element (see XPath 1.0 Sec 5.2) 
	 * TODO: this function is not the right one for serialization	  
	 */
    public String getElementData() {
        if (m_mixedContent == false) {
        	return m_eleData;
        }
        
        // handle mixed content
		StringBuffer stringValue = new StringBuffer();
		int size = m_allChildren.size();
		for (int i=0; i<size; i++)  {
			Object ob = m_allChildren.get(i);
			if (ob instanceof String)
				stringValue.append((String)ob); // text nodes are not null in a mixed content
			else if (ob instanceof ParsingContext) {
				String s = ((ParsingContext)ob).getElementData();
				if (s != null)
					stringValue.append(s);
			}
		}
        return stringValue.toString();	
    }

	public String getChildTextData() {
		if (m_mixedContent == false) {
			return m_eleData;
		}
        
		// handle mixed content
		StringBuffer stringValue = new StringBuffer();
		int size = m_allChildren.size();
		for (int i=0; i<size; i++)  {
			Object ob = m_allChildren.get(i);
			if (ob instanceof String)
				stringValue.append((String)ob); // text nodes are not null in a mixed content
		}
		return stringValue.toString();	
	}

    public int getEventId() {
        return m_Id;
    }
    
    public int getParentId() {
    	return m_parentId;
    }

	///////////////////////////////////////////
	//* support predicates on the text data *//
	///////////////////////////////////////////
	
	public boolean hasElementData() {
		return (m_eleData != null); // it has at least one text node
	}
	
	public boolean textDataEquals(String target) {
        String data = null;
		if (m_mixedContent == false) {
            if (m_trimData)
                data = trimString(m_eleData);
            else
                data = m_eleData;
			//return target.equals(m_eleData);
            return target.equals(data);
        }

		// handle the mixed content
		boolean equal = false;
		int size = m_allChildren.size();
		for (int i = 0; i < size; i++) {
			Object ob = m_allChildren.get(i);
			if (ob instanceof String) {
                data = (String)ob;
                if (m_trimData)
                    data = trimString(data);
				//equal = target.equals((String)ob);
                equal = target.equals(data);
				if (equal)	break;
			}			
		}
		return equal;
	}

    private String trimString(String data) {
        while (data.startsWith("\n")) {
            data = data.substring(1, data.length());
        }
        while (data.endsWith("\n")) {
            data = data.substring(0, data.length()-1);
        }
        return data;
    }
    //////////////////
    //* for output *//
    //////////////////

    public void printToFile(PrintWriter pw) {
        pw.print(toString());
    }

    public void print() {
    	System.out.print(toString());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(m_eleName);
        sb.append("\t$");
        sb.append(m_Id);
		sb.append("\t$");
		sb.append(m_parentId);
		sb.append("\t$[");
        if (m_attributes != null) {
        	if (m_orderedAttrNames == null) {        	
            	Set s = m_attributes.keySet();
            	Iterator iter = s.iterator();
            	while (iter.hasNext()) {
                	String attrName = (String)iter.next();
                	String attrValue = (String)m_attributes.get(attrName);
                	sb.append("(");
                	sb.append(attrName);
					sb.append(",");
					sb.append(attrValue);
					sb.append(")");
            	}
        	}
        	else {
        		for (int i=0; i<m_orderedAttrNames.length; i++) {
					sb.append("(");
					sb.append(m_orderedAttrNames[i]);
					sb.append(",");
					String attrValue = (String)m_attributes.get(m_orderedAttrNames[i]);
					sb.append(attrValue);
					sb.append(")");        			
        		}
        	}
        }
		sb.append("]");
        sb.append("\t$");
        sb.append(m_position);
        sb.append("\t$");
        if (m_mixedContent == false) {
			if (m_eleData == null)
				sb.append(m_eleData);
			else {
				sb.append("\"");
				sb.append(m_eleData);
				sb.append("\"");
			}
        }
        else {			
			int size = m_allChildren.size();
			for (int i = 0; i < size; i++) {
				Object ob = m_allChildren.get(i);
				if (ob instanceof String) {
					// text nodes are not null in a mixed content					
					sb.append("\"");
					sb.append((String) ob);
					sb.append("\"");
				}
				
				else if (ob instanceof ParsingContext) {
					sb.append("<");
					sb.append(((ParsingContext) ob).getElementName());					
					sb.append(">");
				}
			}
			sb.append("\t$\"");
			sb.append(getElementData());
			sb.append("\"");
        }
        
        return sb.toString();
    }

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		sb.append("<");
		sb.append(m_eleName);				
		if (m_attributes != null) {
			if (m_orderedAttrNames == null) {			
				Set s = m_attributes.keySet();
				Iterator iter = s.iterator();
				while (iter.hasNext()) {
					sb.append(" ");
					String attrName = (String)iter.next();
					String attrValue = (String)m_attributes.get(attrName);				
					sb.append(attrName);
					sb.append("=\"");
					sb.append(attrValue);
					sb.append("\"");
				}
			}	
			else {
				for (int i=0; i<m_orderedAttrNames.length; i++) {
					sb.append(" ");
					sb.append(m_orderedAttrNames[i]);
					sb.append("=\"");
					String attrValue = (String)m_attributes.get(m_orderedAttrNames[i]);
					sb.append(attrValue);
					sb.append("\"");							
				}
			}
		}		
		if (m_eleData != null) {
			sb.append(">");
			// text data and possibly mixed content
			if (m_mixedContent == false)
				sb.append(m_eleData);
			else {
				int size = m_allChildren.size();
				for (int i = 0; i < size; i++) {
					Object ob = m_allChildren.get(i);
					if (ob instanceof String)
						// text nodes are not null in a mixed content					
						sb.append((String) ob);
					else if (ob instanceof ParsingContext)
						sb.append(((ParsingContext) ob).toXMLString());
				}
			}
			// closing tag
			sb.append("</");
			sb.append(m_eleName);
			sb.append(">");
		}
		else if (m_children != null && m_children.size() > 0) {
			sb.append(">");
			// child elements
			int size = m_children.size();
			for (int i = 0; i < size; i++) 
				sb.append(((ParsingContext)m_children.get(i)).toXMLString());
			// closing tag
			sb.append("</");
			sb.append(m_eleName);
			sb.append(">");
		}
		else 
			sb.append("/>");		
		        
		return sb.toString();
	}

}
