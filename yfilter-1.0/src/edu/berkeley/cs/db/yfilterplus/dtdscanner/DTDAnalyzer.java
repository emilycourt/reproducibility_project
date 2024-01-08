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

public class DTDAnalyzer {
    TreeMap m_elements;	// a sorted map from element name to its children
    //HashMap m_elements;
    String m_rootElement;
    
    ArrayList m_loops = null;
    TreeSet m_elementPairOnLoop;
    TreeSet m_elementOnLoop;
    TreeSet m_multiplePathElements;
    TreeSet m_singlePathPairs;
    //DTDGraph m_dtdGraph;
    
    ///////////////////
    //* constructor *//
    ///////////////////
    
    public DTDAnalyzer(Hashtable elements, String root) {
    	//m_elements = new HashMap((int)Math.ceil(elements.size()/0.75));
    	m_elements = new TreeMap();
    	Iterator iter = elements.entrySet().iterator();
    	Map.Entry entry;
    	while (iter.hasNext()) {
    	   entry = (Map.Entry)iter.next();
    	   m_elements.put(entry.getKey(), ((dtdElement)entry.getValue()).getChildren());
    	}
    	//m_elements = elements;
    	m_rootElement = root;
    	if (m_rootElement == null) {
    	    System.out.println("DTDAnalyzer::DTDAnalyzer -- root element is null!");
    	}
    	
    	//m_loops = new ArrayList();
    	m_elementPairOnLoop = new TreeSet();
    	m_elementOnLoop = new TreeSet();
    	m_multiplePathElements = new TreeSet();
    	m_singlePathPairs = new TreeSet();
    }

    //////////////////////
    //* reads and gets *//
    //////////////////////
    public String getRoot() {
    	return m_rootElement;
    }
    
    public boolean isMultiplePathElement(String elementName) {
    	return m_multiplePathElements.contains(elementName);
    }
    
    public boolean isElementOnLoop(String elementName) {
    	return m_elementOnLoop.contains(elementName);
    }
    
    public boolean isElementPairOnLoop(String name, String name2) {
    	String hashKey = null;
    	if (name.compareTo(name2) <= 0)
    	    hashKey = name + "-" + name2;
    	else
    	    hashKey = name2 + "-" + name;
    	return m_elementPairOnLoop.contains(hashKey);
    }
    
    public boolean isSinglePathPair(String name, String name2) {
    	if (m_multiplePathElements.contains(name2) == false)
    	    return true;
    	if (m_elementOnLoop.contains(name) || m_elementOnLoop.contains(name2))
    	    return false;
    	String hashKey = null;
    	if (name.compareTo(name2) <= 0)
    	    hashKey = name + "-" + name2;
    	else
    	    hashKey = name2 + "-" + name;    
    	if (m_singlePathPairs.contains(hashKey))
    	    return true;
    	return false; 
    }
    
    public void readStatFromFile(String fileName) {
    	System.out.println("Reading auxiliary DTD statistics from file "+fileName+"...");
    	try {
	    BufferedReader in = new BufferedReader(new FileReader(fileName));				
	    String line;
	    
	    // read in the multiple path elements;
	    line = in.readLine();
	    if (line == null) {
	    	System.out.println("DTDAnalyzer::readStatFromFile -- stat file format wrong!");
	    	return;
	    }
	    StringTokenizer st = new StringTokenizer(line.trim());
	    while (st.hasMoreTokens()) {
	    	m_multiplePathElements.add(st.nextToken());
	    }
	    
	    // read in elements on loops;
	    line = in.readLine();
	    if (line == null) {
	    	System.out.println("DTDAnalyzer::readStatFromFile -- stat file format wrong!");
	    	return;
	    }
	    st = new StringTokenizer(line.trim());
	    while (st.hasMoreTokens()) {
	    	m_elementOnLoop.add(st.nextToken());
	    }
	    
	    // read in elements pair on loops;
	    line = in.readLine();
	    if (line == null) {
	    	System.out.println("DTDAnalyzer::readStatFromFile -- stat file format wrong!");
	    	return;
	    }
	    st = new StringTokenizer(line.trim());
	    while (st.hasMoreTokens()) {
	    	m_elementPairOnLoop.add(st.nextToken());
	    }
	    
	    // read in single path pairs;
	    line = in.readLine();
	    if (line == null) {
	    	System.out.println("DTDAnalyzer::readStatFromFile -- stat file format wrong!");
	    	return;
	    }
	    st = new StringTokenizer(line.trim());
	    while (st.hasMoreTokens()) {
	    	m_singlePathPairs.add(st.nextToken());
	    }
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /////////////////////////////////////////////////////////
    //* find elements with multiple paths leading to them *//
    /////////////////////////////////////////////////////////
    
    public void findMultiplePathElements() {
    	System.out.println("\n Analyzing elements in the DTD...\n");
  
  	/// 1. find basic multiple path elements using BFS from the root ///
  	  	
    	HashSet visited = new HashSet();
    	visited.add(m_rootElement);
    	LinkedList queue = new LinkedList();
    	queue.add(m_rootElement);
    	
    	String elementName;
    	String[] children;
    	while (queue.size() > 0) {
    	    elementName = (String)queue.removeFirst();	
    	    children = (String[])m_elements.get(elementName);
    	    if (children == null)
    	    	continue;
    	    int len = children.length;
    	    for (int i=0; i<len; i++) {
    	        //System.out.print(children[i]+" ");
    	        if (visited.add(children[i])) {
    	            queue.add(children[i]);
    	        }
    	        else {
    	            // there are multiple paths to children	
    	            m_multiplePathElements.add(children[i]);
    	        }
    	    }
    	    //System.out.println();
    	}
    	//printMultiplePathElements();
    	
    	/// 2. find descendents of the basic ones using BFS from each basic one ///
    	
    	// it is not hard to see that this set of multiple path elements
    	// must contain elements on a loop because loop elements must have
    	// an edge going back to an element on the path leading to it
    	// by adding the descendents of the basic multiple path elements,
    	// it is assued to contain all elements with multiple paths leading there
    	
    	addDescendentsOfMultiplePathElements();
    	//printMultiplePathElements();
    }

    private void addDescendentsOfMultiplePathElements() {
    	if (m_multiplePathElements.size() == 0)
    	    return;
    	    
    	String elementName, elementName2;
    	LinkedList queue;
    	
    	TreeSet extendedSet = new TreeSet(m_multiplePathElements);
    	Iterator iter = m_multiplePathElements.iterator();
    	while (iter.hasNext()) {
    	    elementName = (String)iter.next();
    	    //System.out.println("checking the descendents of "+elementName+"...");
    	
    	    ///  add all descendents of elementName to the extendedSet ///
    	    
    	    String[] children;
    	    int len;
    	    
    	    queue = new LinkedList();
    	    queue.add(elementName);
    	    while (queue.size() > 0) {
    	    	elementName2 = (String)queue.removeFirst();	
    	    	children = (String[])m_elements.get(elementName2);
    	    	if (children == null)
    	    	    continue;
    	    	len = children.length;
    	    	for (int i=0; i<len; i++) {
    	            //System.out.print(children[i]+" ");
    	            if (extendedSet.add(children[i])) {
    	            	queue.add(children[i]);
    	            }
    	        }
    	    }
    	    //System.out.println();
    	}
    	m_multiplePathElements = extendedSet;
    }

    //////////////////////////////////////////////////////////////////////
    //* for each pair of elements, report if they are on the same loop *//
    //////////////////////////////////////////////////////////////////////
        
    /**
     * 1) this function check for each pair of elements (can be identical),
     * if there is a loop before them. 2) then it is easy to determine if an element 
     * is on a loop -- just check if this element is ever on any loop. 3) finally
     * all elements on a loop are determined to have multiple paths reaching them.
     **/
      
    public void analyzeElementsOnLoop() {
    	Iterator iter = m_elements.keySet().iterator();
    	Iterator iter2;
    	String name, name2;
    	//String hashKey;
    	while (iter.hasNext()) {
    	    name = (String)iter.next();
    	    iter2 = m_elements.keySet().iterator();
    	    while (iter2.hasNext()) {
    	    	name2 = (String)iter2.next();
    	    	if (name.compareTo(name2) > 0)
    	    	    continue;
    	    	if (existsPathByBFS(name, name2) == false) 
    	    	    continue;
    	    	if (existsPathByBFS(name2, name) == false)
    	    	    continue;
    	    	m_elementOnLoop.add(name);
    	    	m_elementOnLoop.add(name2);
    	    	//if (name.compareTo(name2) <= 0)
    	    	//    hashKey = name + "-" + name2;
    	    	//else
    	    	//    hashKey = name2 + "-" + name;
    	    	m_elementPairOnLoop.add(name + "-" + name2);
    	    }
    	}
    }
    
    private boolean existsPathByBFS(String start, String end) {
    	String[] children;
    	int len;
    	String elementName;
    	
    	HashSet visited = new HashSet();
    	visited.add(start);
    	LinkedList queue = new LinkedList();
    	queue.add(start);
    	while (queue.size() > 0) {
    	    elementName = (String)queue.removeFirst();	
    	    children = (String[])m_elements.get(elementName);
    	    if (children == null)
    	        continue;
    	    len = children.length;
    	    for (int i=0; i<len; i++) {
    	        //System.out.print(children[i]+" ");
    	        
    	        // found a path
    	        if (children[i].equals(end))
    	            return true;
    	        // not found a path yet but children[i] has child elements
    	        if (visited.add(children[i])) 
    	            queue.add(children[i]);
    	       
    	    }
    	}
    	return false;
    }
 
    private boolean existsPathByDFS(String start, String end) {
    	//System.out.println("DTDAnalyzer::exisitsPathByDFS is called for "+start+"-->"+end+".");
    	
    	String[] children = (String[])m_elements.get(start);
    	if (children == null)
    	    return false;
    	    
    	/// depth first search to find all paths from start to end ///
    	Stack pathStack = new Stack();
    	pathStack.push(m_elements.get(start));
    	int top = 0;
    	int[] stepElementIndex = new int[100];	// assume the max depth of the dtd path is 100
    	stepElementIndex[top] = 0;
    	String[] children2;
    	HashSet visited = new HashSet();
    	while (top >=0) {
    	    children = (String[])pathStack.peek();
    	    int len = children.length;
    	    //System.out.println("stack top="+top+" contains "+len+" elements");
    	    
    	    if (stepElementIndex[top] >= len) {
    	    	// backtrack
    	    	pathStack.pop();
    	    	top--;
    	    	continue;
    	    }
    	    
    	    String elementName;
    	    boolean push = false;
    	    while (push == false && stepElementIndex[top] < len) {
    	    	elementName = children[stepElementIndex[top]];
    	    	//System.out.print("check element "+elementName+": ");
    	    	
    	    	if (visited.add(elementName)==false) {
    	    	    //System.out.println("it has been visited.");
    	    	    // it has been visited before
    	    	    stepElementIndex[top]++;
    	    	    continue;
    	    	}
    	    	
    	    	if (elementName.equals(end)) {
    	    	    //System.out.println("it matches the end element.");
    	    	    // stop the branch here
    	    	    stepElementIndex[top]++;
    	    	    
    	    	    // output
    	    	    /*
    	    	    int size = pathStack.size();
    	    	    path = new ArrayList(size+1);
    	    	    path.add(start);
    	    	    for (int i=0; i<size; i++) {
    	    	    	children2 = (String[])pathStack.get(i);
    	    	    	path.add(children2[stepElementIndex[i]-1]);
    	    	    }
    	    	    m_loops.add(path);
    	    	    printPath(path);    	    	    
    	    	    */
      	    	    return true;
    	    	}
    	    	
    	    	// otherwise, get children of the element and search into them
    	    	children2 = (String[])m_elements.get(elementName);
    	    	if (children2 != null) {
    	    	    //System.out.println("it doesnot match the end element but has children.");
    	    	    stepElementIndex[top]++;
    	    	    pathStack.push(children2);
    	    	    top++;
    	    	    stepElementIndex[top] = 0;
    	    	    push = true;
    	    	}
    	    	else {
    	    	    //System.out.println("it doesnot match the end element and has no children.");
    	    	    stepElementIndex[top]++;
    	    	}
    	    }// end of inner while
    	}// end of outer while
    	return false;
    }
    
    /////////////////////////////////////////////////////////////
    //* find a pair of elements with a single path in between *//
    /////////////////////////////////////////////////////////////
    
    /**
     * this function finds element pair between which there is a single path
     * this information is complementory to elementOnLoop and multiplePathElement
     * 1) if the start or end element in on a loop, the answer is no
     * 2) if the end element is not a multiplePathElement, the answer is yes
     * ignoring these trivial cases, this functions for the case that the start 
     * element is not on a loop and the end element is a multiplePathElement but 
     * not on a loop.
     **/
     
    public void findSinglePathPairs() {
    	Iterator iter = m_elements.keySet().iterator();
    	Iterator iter2;
    	String name, name2;
    	String hashKey;
    	while (iter.hasNext()) {
    	    name = (String)iter.next();
    	    if (m_elementOnLoop.contains(name))
    	    	continue;
    	    iter2 = m_elements.keySet().iterator();
    	    while (iter2.hasNext()) {
    	    	name2 = (String)iter2.next();
    	    	if (name2.equals(name))
    	    	    continue;
    	    	if (m_elementOnLoop.contains(name2) || !m_multiplePathElements.contains(name2))
    	    	    continue;
    	    	if (singlePathPair(name, name2)) {
    	    	    hashKey = null;
    		    if (name.compareTo(name2) <= 0)
    	    		hashKey = name + "-" + name2;
    		    else
    	      		hashKey = name2 + "-" + name;
    	    	    m_singlePathPairs.add(hashKey);
    	    	}
    	    }
    	}
    }
    
    private boolean singlePathPair(String start, String end) {
    	//System.out.println("DTDAnalyzer::singlePathPair is called for "+start+"-->"+end+".");
    	
    	String[] children = (String[])m_elements.get(start);
    	if (children == null)
    	    return false;
    	    
    	/// depth first search to find all paths from start to end ///
    	Stack pathStack = new Stack();
    	pathStack.push(m_elements.get(start));
    	int top = 0;
    	int[] stepElementIndex = new int[100];	// assume the max depth of the dtd path is 100
    	stepElementIndex[top] = 0;
    	String[] children2;
    	HashSet visited = new HashSet();
    	boolean found = false;
    	while (top >=0) {
    	    children = (String[])pathStack.peek();
    	    int len = children.length;
    	    //System.out.println("stack top="+top+" contains "+len+" elements");
    	    
    	    if (stepElementIndex[top] >= len) {
    	    	// backtrack
    	    	pathStack.pop();
    	    	top--;
    	    	continue;
    	    }
    	    
    	    String elementName;
    	    boolean push = false;
    	    while (push == false && stepElementIndex[top] < len) {
    	    	elementName = children[stepElementIndex[top]];
    	    	//System.out.print("check element "+elementName+": ");
    	    	
    	    	if (elementName.equals(end)) {
    	    	    //System.out.println("it matches the end element.");
    	    	    // stop the branch here
    	    	    stepElementIndex[top]++;
    	    	    
    	    	    // output  
    	    	      	    
    	    	    int size = pathStack.size();
    	    	    //for (int i=0; i<size; i++) {
    	    	    //	children2 = (String[])pathStack.get(i);
    	    	    //	System.out.print(children2[stepElementIndex[i]-1]+"-");
    	    	    //}
    	    	    //System.out.println();
    	    	    if (found) 	// this is the second time being found
    	    	        return false;
    	    	        	// this is the first time being found
    	    	    found = true;
    	    	    	
    	    	    //path = new ArrayList(size+1);
    	    	    //path.add(start);
    	    	    boolean singlePath = true;
    	    	    for (int i=0; i<size-1; i++) {
    	    	    	children2 = (String[])pathStack.get(i);
    	    	    	//path.add(children2[stepElementIndex[i]-1]);
    	    	    	if (m_elementOnLoop.contains(children2[stepElementIndex[i]-1])) {
    	    	    	    singlePath = false;
    	    	    	    break;
    	    	    	}
    	    	    }
    	    	    if (singlePath == false) { 
    	    	    		// first time being found but has a loop element on the path
    	    	    	return false;
    	    	    }
    	    	    continue;
    	    	}
    	    	
    	    	if (visited.add(elementName)==false) {
    	    	    //System.out.println("it has been visited.");
    	    	    // it has been visited before
    	    	    stepElementIndex[top]++;
    	    	    continue;
    	    	}
    	    	
    	    	// otherwise, get children of the element and search into them
    	    	children2 = (String[])m_elements.get(elementName);
    	    	if (children2 != null) {
    	    	    //System.out.println("it doesnot match the end element but has children.");
    	    	    stepElementIndex[top]++;
    	    	    pathStack.push(children2);
    	    	    top++;
    	    	    stepElementIndex[top] = 0;
    	    	    push = true;
    	    	}
    	    	else {
    	    	    //System.out.println("it doesnot match the end element and has no children.");
    	    	    stepElementIndex[top]++;
    	    	}
    	    }// end of inner while
    	}// end of outer while
    	if (found)
    	    return true;
    	return false;
    }
    /*
    private boolean singlePathPair(String start, String end) {
    	String[] children;
    	int len;
    	String elementName;
    	
    	boolean found = false;
    	HashSet visited = new HashSet();
    	visited.add(start);
    	LinkedList queue = new LinkedList();
    	queue.add(start);
    	while (queue.size() > 0) {
    	    elementName = (String)queue.removeFirst();	
    	    children = (String[])m_elements.get(elementName);
    	    if (children == null)
    	        continue;
    	    len = children.length;
    	    for (int i=0; i<len; i++) {
    	        //System.out.print(children[i]+" ");
    	        
    	        // found a path
    	        if (children[i].equals(end))  {
    	            found = true;
    	            // found another path
    	            if (visited.add(children[i]) == false)
    	            	return false;
    	        }
    	        // not found a path yet but children[i] has child elements
    	        else if (visited.add(children[i])) 
    	            queue.add(children[i]);
    	       
    	    }
    	}
    	if (found)
    	    return true;
    	return false;
    }
    */
    /**
     * this function finds a query path from start to end using DFS in a graph
     * we assume start and end correspond to different location steps
     **/
  
   public ArrayList generateQueryPath(String start, String end) {
    	//System.out.println("DTDAnalyzer::findPath is called for "+start+"-->"+end+".");
    	ArrayList path;
    	
    	//if (start.equals(end)) {
    	//    path = new ArrayList();
    	//    path.add(end);
    	//    return path;
    	//}
    	
    	String[] children = (String[])m_elements.get(start);
    	if (children == null)
    	    return null;
    	    
    	/// depth first search to find all paths from start to end ///
    	Stack pathStack = new Stack();
    	pathStack.push(m_elements.get(start));
    	int top = 0;
    	int[] stepElementIndex = new int[100];	// assume the max depth of the dtd path is 100
    	stepElementIndex[top] = 0;
    	String[] children2;
    	HashSet visited = new HashSet();
    	
    	while (top >=0) {
    	    children = (String[])pathStack.peek();
    	    int len = children.length;
    	    //System.out.println("stack top="+top+" contains "+len+" elements");
    	    
    	    if (stepElementIndex[top] >= len) {
  	    	// backtrack
    	    	pathStack.pop();
    	    	top--;
    	    	continue;
    	    }
    	    
    	    String elementName;
    	    boolean push = false;
    	    while (push == false && stepElementIndex[top] < len) {
    	    	elementName = children[stepElementIndex[top]];
    	    	//System.out.print("check element "+elementName+": ");
    	    	
    	    	if (visited.add(elementName)==false) {
    	    	    //System.out.println("it has been visited.");
    	    	    // it has been visited before
    	    	    stepElementIndex[top]++;
    	    	    continue;
    	    	}
    	    	
    	    	if (elementName.equals(end)) {
    	    	    //System.out.println("it matches the end element.");
    	    	    // stop the branch here
    	    	    stepElementIndex[top]++;
    	    	    
    	    	    // output
    	    	    
    	    	    int size = pathStack.size();
    	    	    path = new ArrayList(size+1);
    	    	    path.add(start);
    	    	    for (int i=0; i<size; i++) {
    	    	    	children2 = (String[])pathStack.get(i);
    	    	    	path.add(children2[stepElementIndex[i]-1]);
    	    	    }
    	    	    
    	    	    //printPath(path);    	    	    
    	    	    
      	    	    return path;
    	    	}
    	    	
    	    	// otherwise, get children of the element and search into them
    	    	children2 = (String[])m_elements.get(elementName);
    	    	if (children2 != null) {
    	    	    //System.out.println("it doesnot match the end element but has children.");
    	    	    stepElementIndex[top]++;
    	    	    pathStack.push(children2);
    	    	    top++;
    	    	    stepElementIndex[top] = 0;
    	    	    push = true;
    	    	}
    	    	else {
    	    	    //System.out.println("it doesnot match the end element and has no children.");
    	    	    stepElementIndex[top]++;
    	    	}
    	    }// end of inner while
    	}// end of outer while
    	return null;
    }
       
    ///////////////////////////////////////////////////////
    //* report loops -- hard to catch independent loops *//
    /////////////////////////////////////////////////////// 
    
    /**
     * this function reports two types of information on dtd elements:
     * 1) the loops in dtd and elements on a specific loop; and
     * 2) the number of paths going from the root element to a specific element
     **/
    public void analyzeLoops() {
    	System.out.println("\n Analyzing elements in the DTD...\n");
    	
    	HashSet elementSet = new HashSet();
    	elementSet.add(m_rootElement);
    	LinkedList queue = new LinkedList();
    	queue.add(m_rootElement);
    	
    	dtdElement element;
    	String elementName;
    	String[] children;
    	ArrayList path;
    	while (queue.size() > 0) {
    	    elementName = (String)queue.removeFirst();	
    	    children = (String[])m_elements.get(elementName);
    	    if (children == null)
    	    	continue;
    	    int len = children.length;
    	    for (int i=0; i<len; i++) {
    	        //System.out.print(children[i]+" ");
    	        if (elementSet.add(children[i])) {
    	            queue.add(children[i]);
    	        }
    	        else {
    	            checkPath(children[i], elementName);
    	            	// loops are added in this function
    	            m_multiplePathElements.add(children[i]);
    	            	// there are multiple paths to children	
    	        }
    	    }
    	    //System.out.println();
    	}
    	System.out.println("\nFound "+m_loops.size()+" loops.\n");
    	addDescendentsOfMultiplePathElements();
    	printMultiplePathElements();
    }
    
    /**
     * this function finds all path from start to end using DFS in a graph
     **/
    
    private void checkPath(String start, String end) {
    	System.out.println("DTDAnalyzer::checkPath is called for "+start+"-->"+end+".");
    	ArrayList path = new ArrayList();
    	if (start.equals(end)) {
    	    path.add(start);
    	    m_loops.add(path);
    	    printPath(path);
    	    return;
    	}
    	
    	String[] children = (String[])m_elements.get(start);
    	if (children == null)
    	    return;
    	    
    	/// depth first search to find all paths from start to end ///
    	Stack pathStack = new Stack();
    	pathStack.push(m_elements.get(start));
    	int top = 0;
    	int[] stepElementIndex = new int[100];	// assume the max depth of the dtd path is 100
    	stepElementIndex[top] = 0;
    	String[] children2;
    	HashSet visited = new HashSet();
    	while (top >=0) {
    	    children = (String[])pathStack.peek();
    	    int len = children.length;
    	    //System.out.println("stack top="+top+" contains "+len+" elements");
    	    
    	    if (stepElementIndex[top] >= len) {
    	    	// backtrack
    	    	pathStack.pop();
    	    	top--;
    	    	continue;
    	    }
    	    
    	    String elementName;
    	    boolean push = false;
    	    while (push == false && stepElementIndex[top] < len) {
    	    	elementName = children[stepElementIndex[top]];
    	    	//System.out.print("check element "+elementName+": ");
    	    	
    	    	if (visited.add(elementName)==false) {
    	    	    //System.out.println("it has been visited.");
    	    	    // it has been visited before
    	    	    stepElementIndex[top]++;
    	    	    continue;
    	    	}
    	    	
    	    	if (elementName.equals(end)) {
    	    	    //System.out.println("it matches the end element.");
    	    	    // stop the branch here
    	    	    stepElementIndex[top]++;
    	    	    
    	    	    // output
    	    	    int size = pathStack.size();
    	    	    path = new ArrayList(size+1);
    	    	    path.add(start);
    	    	    for (int i=0; i<size; i++) {
    	    	    	children2 = (String[])pathStack.get(i);
    	    	    	path.add(children2[stepElementIndex[i]-1]);
    	    	    }
    	    	    m_loops.add(path);
    	    	    printPath(path);
    	    	    
      	    	    continue;
    	    	}
    	    	
    	    	// otherwise, get children of the element and search into them
    	    	children2 = (String[])m_elements.get(elementName);
    	    	if (children2 != null) {
    	    	    //System.out.println("it doesnot match the end element but has children.");
    	    	    stepElementIndex[top]++;
    	    	    pathStack.push(children2);
    	    	    top++;
    	    	    stepElementIndex[top] = 0;
    	    	    push = true;
    	    	}
    	    	else {
    	    	    //System.out.println("it doesnot match the end element and has no children.");
    	    	    stepElementIndex[top]++;
    	    	}
    	    }
    	}
    }
    
    private void printPath(ArrayList path) {
    	int size = path.size();    
    	System.out.print("Path : ");	
    	for (int i=0; i<size; i++) 
	    System.out.print((String)path.get(i)+" - ");    	
 	System.out.println();
    }
    
    ///////////////////////
    //* print functions *//
    ///////////////////////
    
    public void printMultiplePathElements() {
    	System.out.println("\n"+m_multiplePathElements.size()+" elements with multiple paths: ");
    	Iterator iter = m_multiplePathElements.iterator();
    	while (iter.hasNext()) {
    	    System.out.print((String)iter.next()+" ");
    	}
    	System.out.println();
    }
    
    public  void printElementOnLoop() {
    	System.out.println("\n"+m_elementOnLoop.size()+" elements on some loops: ");
    	Iterator iter = m_elementOnLoop.iterator();
    	while (iter.hasNext()) {
    	    System.out.print((String)iter.next()+" ");
    	}
    	System.out.println();
    }
    
    public  void printElementPairOnLoop() {
    	System.out.println("\n"+m_elementPairOnLoop.size()+" element Pair on some loops: ");
    	Iterator iter = m_elementPairOnLoop.iterator();
    	while (iter.hasNext()) {
    	    System.out.print((String)iter.next()+" ");
    	}
    	System.out.println();
    }

    public  void printSinglePathPairs() {
    	System.out.println("\n"+m_singlePathPairs.size()+" single path pairs: ");
    	Iterator iter = m_singlePathPairs.iterator();
    	while (iter.hasNext()) {
    	    System.out.print((String)iter.next()+" ");
    	}
    	System.out.println();
    }
        
    public void printMultiplePathElements(PrintWriter pw) {
    	//try {
    		
    	//pw.println("\n"+m_multiplePathElements.size()+" elements with multiple paths: ");
    	Iterator iter = m_multiplePathElements.iterator();
    	while (iter.hasNext()) {
    	    pw.print((String)iter.next()+" ");
    	}
    	pw.println();
    	
    	//}
    	//catch (IOException e) {
    	//    e.printStackTrace();
    	//}
    }
    
    public  void printElementOnLoop(PrintWriter pw) {
    	//pw.println("\n"+m_elementOnLoop.size()+" elements on some loops: ");
    	//try {
    		
    	Iterator iter = m_elementOnLoop.iterator();
    	while (iter.hasNext()) {
    	    pw.print((String)iter.next()+" ");
    	}
    	pw.println();
    	
    	//}
    	//catch (IOException e) {
    	//   e.printStackTrace();
    	//}
    }
    
    public  void printElementPairOnLoop(PrintWriter pw) {
    	//try {
    		
    	//pw.println("\n"+m_elementPairOnLoop.size()+" element Pair on some loops: ");
    	Iterator iter = m_elementPairOnLoop.iterator();
    	while (iter.hasNext()) {
    	    pw.print((String)iter.next()+" ");
    	}
    	pw.println();
    	
    	//}
    	//catch (IOException e) {
    	//    e.printStackTrace();
    	//}
    }
    
    public void printSinglePathPairs(PrintWriter pw) {
    	//try {
    		
    	//pw.println("\n"+m_singlePathPairs.size()+" element Pair on some loops: ");
    	Iterator iter = m_singlePathPairs.iterator();
    	while (iter.hasNext()) {
    	    pw.print((String)iter.next()+" ");
    	}
    	pw.println();
    	
    	//}
    	//catch (IOException e) {
    	//    e.printStackTrace();
    	//}
    }

    public void printParentChildren() {
    	if (m_rootElement == null) {
    	    System.out.println("DTDAnalyzer::printParentChildren -- root element is null!");
    	    return;
    	}
    	
    	System.out.println("\nPrint parent child relationships ...");
    	HashSet elementSet = new HashSet();
    	elementSet.add(m_rootElement);
    	LinkedList queue = new LinkedList();
    	queue.add(m_rootElement);
    	//dtdElement element;
    	String elementName;
    	String[] children;
    	while (queue.size() > 0) {
    	    elementName = (String)queue.removeFirst();	

    	    	// first time encountered
    	    	System.out.print(elementName+":\t");
    	    	//element = (dtdElement)m_elements.get(elementName);
    	    	//children = element.getChildren();
    	    	children = (String[])m_elements.get(elementName);
    	    	if (children == null) {
    	    	    System.out.println();
    	    	    continue;
    	    	}
    	    	int len = children.length;
    	    	for (int i=0; i<len; i++) {
    	    	    System.out.print(children[i]+" ");
    	    	    if (elementSet.add(children[i]))
    	    	    	queue.add(children[i]);
    	    	}
    	    	System.out.println();
    	    	
    	}
    }
    	
    public static void main(String[] args) {
    	DTDStatReader stat = new DTDStatReader();
	stat.readStatFromFile(args[0]);
	
	char mode = args[1].charAt(0);
	
	if (mode == 'g') {		
	DTDAnalyzer analyzer = new DTDAnalyzer(stat.getElements(), stat.getRoot());
	//analyzer.printParentChildren();
	//analyzer.analyze();
	String fileName = args[0]+".aux"; 
	
	try {
	PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
	
	analyzer.findMultiplePathElements();	
	
	analyzer.analyzeElementsOnLoop();
		
	analyzer.findSinglePathPairs();
	
	analyzer.printMultiplePathElements();
	analyzer.printElementOnLoop();
	analyzer.printElementPairOnLoop();
	analyzer.printSinglePathPairs();
	
	analyzer.printMultiplePathElements(pw);
	analyzer.printElementOnLoop(pw);
	analyzer.printElementPairOnLoop(pw);
	analyzer.printSinglePathPairs(pw);
	
	pw.close();
	
	}
    	catch (IOException e) {
    	    e.printStackTrace();
    	}
    	}
    	
    	else if (mode == 'r') {
    	// read process
    	DTDAnalyzer analyzer = new DTDAnalyzer(stat.getElements(), stat.getRoot());
    	analyzer.readStatFromFile(args[0]+".aux");
    	analyzer.printMultiplePathElements();
    	analyzer.printElementOnLoop();
    	analyzer.printElementPairOnLoop();
    	analyzer.printSinglePathPairs();
    	}
    }
}