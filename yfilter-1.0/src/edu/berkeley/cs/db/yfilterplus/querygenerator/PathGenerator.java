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

/**
 * PathGenerator generates user queries according to parameter values
 *
 * references:
 * [Knu73] The Art of Computer Programming (Vol 3), pp 397-399
 * [AF00]  zipf.cpp util.cpp
 * 
 * This query generator implements three ways of query rewriting
 * in order to speed up NFA execution.
 * 1) For pattern //*{/*}(0, n)//, the first // is removed
 * 2) Pattern  //*{/*}(0, n) is rewritten as /*{/*}(0, n)//
 * 3) A query ending with //*{/*}(0, n) will end with /*{/*}(0, n)//
 *    after step 2) and the last // is always dropped.
 * 4) A sequence of consecutive * can be compressed, which requires
 *    modifying NFA and is not supported yet.
 **/

package edu.berkeley.cs.db.yfilterplus.querygenerator;

import edu.berkeley.cs.db.yfilterplus.dtdscanner.*;

import java.util.*;
import java.io.*;

public class PathGenerator
{	
    /// element information for query generation  ///
    /// it is read from DTDStatReader             ///  
    /// and zipf's dist for each element is set.  ///

    String m_rootElement;
    Hashtable m_elements;


    /// for zipf's distribution; a pool of zipf     ///
    /// dist given theta and the number of elements ///

    double m_theta;
    ZipfSet m_zipfSet;

    /// variables used in recursive path generation ///

    int m_currentLevel;
    dtdElement m_currentElement = null;
    String m_currentElementName = null;

    /// for optimizing paths ///
    boolean m_optimizePath = false;
    DTDAnalyzer m_dtdAnalyzer;

    /// output ///
    PrintWriter m_fileOut = null;

    //int m_noQueries = 0;
    //String m_outFileName = null;

	/// random number generator ///
    Random rand = new Random();
    Random rand2 = new Random();

	/// other parameters ///
	boolean m_distinctQueries = false;

    final static int MAX_POSITION = 5;

    public static boolean PUBLIC_USE = true;


    ////////////////////
    //* constructors *//
    ////////////////////

    public PathGenerator(DTDStat stat, double theta)
    {
        m_rootElement = stat.getRoot();
        m_elements = stat.getElements();
        m_theta = theta;

        m_zipfSet = new ZipfSet();
        String akey;
        dtdElement element;
        Enumeration enu = m_elements.keys();
        while (enu.hasMoreElements()) {
            akey = (String)enu.nextElement();
            element = (dtdElement)m_elements.get(akey);
            int size = element.getSizeOfChildren();
            element.setZipf(m_zipfSet.getZipf(m_theta, size));
        }
    }

    public PathGenerator(String root, Hashtable elements, double theta)
    {
        m_rootElement = root;
        m_elements = elements;
        m_theta = theta;

        m_zipfSet = new ZipfSet();
        String akey;
        dtdElement element;
        Enumeration enu = m_elements.keys();
        while (enu.hasMoreElements()) {
            akey = (String)enu.nextElement();
            element = (dtdElement)m_elements.get(akey);
            int size = element.getSizeOfChildren();
            element.setZipf(m_zipfSet.getZipf(m_theta, size));
        }
    }

    public PathGenerator(String root, Hashtable elements, double theta, DTDAnalyzer analyzer)
    {
        m_rootElement = root;
        m_elements = elements;
        m_theta = theta;

        m_zipfSet = new ZipfSet();
        String akey;
        dtdElement element;
        Enumeration enu = m_elements.keys();
        while (enu.hasMoreElements()) {
            akey = (String)enu.nextElement();
            element = (dtdElement)m_elements.get(akey);
            int size = element.getSizeOfChildren();
            element.setZipf(m_zipfSet.getZipf(m_theta, size));
        }

        m_optimizePath = true;
        m_dtdAnalyzer = analyzer;

    }


    public PathGenerator(String dtdFileName, double theta)
    {
        DTDStat stat = new DTDStat(dtdFileName);

        m_rootElement = stat.getRoot();
        m_elements = stat.getElements();
        m_theta = theta;

        m_zipfSet = new ZipfSet();
        String akey;
        dtdElement element;
        Enumeration enu = m_elements.keys();
        while (enu.hasMoreElements()) {
            akey = (String)enu.nextElement();
            element = (dtdElement)m_elements.get(akey);
            int size = element.getSizeOfChildren();
            element.setZipf(m_zipfSet.getZipf(m_theta, size));
        }
    }

    //////////////////////////////
    // generate a set of paths ///
    //////////////////////////////

    public void  generatePaths(int numQ, int maxDepth, double wildcard, double dSlash,
                               double predProb, char levelDist, double nestedPathProb, int levelPathNesting,
                               double joinProb, String outFilename)
    {
        System.out.println("Printing "+numQ+" queries printed to "+outFilename+" ...");
        StringBuffer query;
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(outFilename));
            int noPreds = (int)Math.floor(predProb);
            int noNestedPaths = (int)Math.floor(nestedPathProb);
            for (int i=0; i<numQ; i++){			// generate queries one by one
                //System.out.println("query "+i);
                if (predProb < 1 && nestedPathProb < 1) {
                    query = generateOnePath(maxDepth, 0, wildcard, dSlash, predProb, levelDist,
                            nestedPathProb, levelPathNesting, joinProb, m_rootElement);
                    pw.println(query.toString());
                }
                else {
                    query = generateOnePath(maxDepth, 0, wildcard, dSlash, noPreds, levelDist,
                            noNestedPaths, levelPathNesting, joinProb, m_rootElement);
                    if (query == null)
                        i--;	// it may fail
                    else
                        pw.println(query.toString());
                }
                //pw.println(query.toString());
            }
            pw.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void  generateDistinctPaths(int numQ, int maxDepth, double wildcard, double dSlash,
                                       double predProb, char levelDist, double nestedPathProb, int levelPathNesting,
                                       double joinProb, String outFilename)
    {
        m_distinctQueries = true;

        System.out.println("Printing "+numQ+" distinct queries to "+outFilename+" ...");
        StringBuffer query;
        String s;
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(outFilename));
            HashSet set = new HashSet();
            int noPreds = (int)Math.floor(predProb);
            int noNestedPaths = (int)Math.floor(nestedPathProb);

            int maxTry = numQ * 100;
            int tries = 0, i=0;
            for (; i<numQ && tries<maxTry; i++, tries++){ // generate queries one by one
                //System.out.println("query "+i);
                if (predProb < 1 && nestedPathProb < 1) {
                    query = generateOnePath(maxDepth, 0, wildcard, dSlash, predProb, levelDist,
                            nestedPathProb, levelPathNesting, joinProb, m_rootElement);
                    //pw.println(query.toString());
                }
                else {
                    query = generateOnePath(maxDepth, 0, wildcard, dSlash, noPreds, levelDist,
                            noNestedPaths, levelPathNesting, joinProb, m_rootElement);
                    if (query == null) {
                        i--;	// the query fail to have the required number of nested paths
                        tries--;
                        continue;
                    }
                    //else
                    //   pw.println(query.toString());
                }
                s = query.toString();
                if (set.add(s))
                    pw.println(s);
                else i--;
            }
            pw.close();
            System.out.println("Generated "+i+" distinct queries.");
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        m_distinctQueries = false;
    }

    //////////////////////////////////
    /// generate an absoluate path ///
    //////////////////////////////////

    /** 
     * Generates a query based on parameters. 
     * 
     * This is a basic path generation function that starts from the specified root element 
     * and generates a path according to parameters. Note that it tries to generate 
     * a fixed number of predicates and nested path expressions.
     * 
     *  Default values for query generation. Refer README
	 * 	@param maxDepth 		Maximum depth of a query: 6
	 * 	@param currentLevel 	The current context level: 0 (the root node) 
	 *  @param wildcard 		Probability of a wildcard (*) at each location step: 0.2
	 * 	@param dSlash 			Probability of a double-slash (//) at each location step: 0.2
	 *  @param noPreds			Number or probability of value-based predicates
	 *  @param levelDist		Distribution of predicates across the location steps in a query. Currently uniform only.
	 *  @param noNestedPaths 	Number of nested path filters in a query: 0. (Uniformly distributed across all location steps.)
	 *  @param levelPathNesting Level of nesting of path expressions. Currently nesting to one level only.
	 *  @param joinProb			Probability of having two nested paths compared by "=". Currently joinProb = 0.
	 *  @param startElement 	The starting element
	 */
    
    public StringBuffer  generateOnePath(int 	maxDepth, 
    									int 	currentLevel, 
										double 	wildcard,
                                        double 	dSlash, 
										int 	noPreds, 
										char 	levelDist,
                                        int 	noNestedPaths, 
										int 	levelPathNesting,
                                        double 	joinProb, 
										String 	startElement)
    {
        double coin;

        StringBuffer query = new StringBuffer();

        String elementName = startElement;	// a query either starts with /startElement or //
        int level = currentLevel;		// can not be larger than depth
        dtdElement element;
        Zipf z;

        boolean inDoubleSlashMode = false;
        int skipLevels = 0;

        ArrayList elementList = new ArrayList();
        ArrayList stepList = new ArrayList();

        //boolean afterStar = false, afterDslash = false;
        while (level < maxDepth) {
            // the exit of while is controlled by maxDepth
            // or when an element has no child, so
            // actual depth can be shorter than maxDepth
            element = (dtdElement)m_elements.get(elementName);
            // the current element in consideration
            //System.out.println("level "+level+": "+elementName);

            if (element.getSizeOfChildren() > 0 ) {
                //current element has children
                /// find the element in the next level ///
                z = element.getZipf();
                int childIndex1 = z.probe();
                // childIndex1 is for the main branch

                ///  generate the operator, name test and predicates for the current level ///
                if (inDoubleSlashMode && skipLevels > 0) {
                    // skipping phase
                    skipLevels--;
                }
                else {					// inDoubleSlashmode == false or
                    // it is true && skiplevel = 0

                    // choose an operator for this location step
                    // dslash controls the occurrence of "//"
                    // since two consecutive "//"s donot make sence, so
                    // if it is inDoubleSlashMode, this occurrence is suppressed

                    if (inDoubleSlashMode==false && rand.nextDouble()< dSlash ) {
                        //afterDslash = true;
                        inDoubleSlashMode = true;
                        query.append("/");
                        /// determine the next element occurs at which level ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        //skipLevels = (int)Math.floor(coin * (maxDepth - 1 - level));
                        skipLevels = (int)Math.floor(coin * (maxDepth - level));
                        //if (skipLevels == 0)
                        //	inDoubleSlashMode = false;
                    }

                    if (skipLevels > 0)
                        skipLevels --;
                    // if (skipLevels == 0)
                    else {
                        /// choose wildcard or not at this step, controlled by wildcard ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        if (1-coin < wildcard) {
                            // * is printed instead of elementName, but the path is
                            // already chosen along elementName and its children
                            //afterStar = true;
                            //query.append("/*");
                            // query writing: rule 2
                            if (inDoubleSlashMode) {
                                query.append("*");
                            }
                            else {
                                query.append("/*");
                            }
                        }
                        else {
                            //afterStar = false;
                            //afterDslash = false;
                            inDoubleSlashMode = false;
                            query.append("/");
                            query.append(elementName);
                        }
                        elementList.add(elementName);

                        /// generate predicates ///
                        //generatePredicates(query, element, predProb);
                        stepList.add(query);
                        query = new StringBuffer();


                        if (inDoubleSlashMode)
                            query.append("/");
                    }// end if skipLeves == 0
                } // end of else (inDoubleSlashMode && skipLevels > 0)


                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                elementName = element.getChild(childIndex1);
                level++;
            } // end if element.getSizeOfChildren() > 0

            else{
                // this element doesn't have children
                /// choose "//" controlled by dslash and inDoubleSlashMode ///
                if (inDoubleSlashMode == false) {
                    //coin = rand.nextDouble();
                    //System.out.println(coin);
                    if (rand.nextDouble() < dSlash ) {
                        query.append("/");
                        inDoubleSlashMode = true;
                    }
                }
                /// choose wildcard or not at this step, controlled by wildcard ///
                if ((1-rand.nextDouble()) < wildcard) {
                    if (inDoubleSlashMode)
                        query.append("*");
                    else
                        query.append("/*");
                }
                else {
                    query.append("/");
                    query.append(elementName);
                }
                elementList.add(elementName);

                /// generate predicates ///
                //generatePredicates(query, element, predProb);
                stepList.add(query);
                //query = new StringBuffer();

                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                break;
            }
        }


        int size = elementList.size();
        //for (int i=0; i<size; i++) {
        //    System.out.print("level "+i+" -- "+elementList.get(i)+": ");
        //    System.out.println((StringBuffer)stepList.get(i));
        //}
        int npLevel;
        int chooseSize = Math.min(size, maxDepth-1);
        int maxTry = 50, tries;

        /// predicates ///

        ArrayList[] predicates = null;
        if (m_distinctQueries && noPreds > 1)
            predicates = new ArrayList[size];
        StringBuffer predicate = null;
        tries = 0;
        for (int i=0; i<noPreds && tries<maxTry; i++, tries++) {
            npLevel = (int)Math.floor(rand.nextDouble() * chooseSize);
            elementName = (String)elementList.get(npLevel);
            //System.out.println("choose level = "+npLevel+"; element = "+elementName);
            element = (dtdElement)m_elements.get(elementName);

            predicate = new StringBuffer();
            generateOnePredicate(predicate, element);

            if (m_distinctQueries && noPreds > 1) {
                String s = predicate.toString();
                if (predicates[npLevel] == null) {
                    predicates[npLevel] = new ArrayList();
                    predicates[npLevel].add(s);
                }
                else {
                    // check if the nested path is distinct at this level
                    // if it is, insert it in the sorted order
                    int size2 = predicates[npLevel].size();
                    int j;
                    for (j=0; j<size2; j++) {
                        int result = s.compareTo((String)predicates[npLevel].get(j));
                        if (result==0) { // identical nested path
                            predicate = null;
                            break;
                        }
                        if (result < 0) {
                            predicates[npLevel].add(j,s);
                            break;
                        }
                    }
                    if (j == size2)
                        predicates[npLevel].add(s);
                }
            }

            if (predicate != null) {
                if (!(m_distinctQueries && noPreds > 1)) {
                    query = (StringBuffer)stepList.get(npLevel);
                    //query.append("[");
                    query.append(predicate);
                    //query.append("]");
                }
            }
            else
                i--;
        }
        if (tries == maxTry)
            return null;

        /// create nested paths ///

        String nextElementName, nextElementName2;
        int childIndex;
        ArrayList[] nestedPaths = null;
        if (m_distinctQueries && noNestedPaths > 1)
            nestedPaths = new ArrayList[size];
        StringBuffer branch = null;
        tries = 0;
        for (int i=0; i<noNestedPaths && tries<maxTry; i++, tries++) {
            npLevel = (int)Math.floor(rand.nextDouble() * chooseSize);
            elementName = (String)elementList.get(npLevel);
            //System.out.println("choose level = "+npLevel+"; element = "+elementName);
            element = (dtdElement)m_elements.get(elementName);
            if (npLevel < size-1)
                nextElementName = (String)elementList.get(npLevel+1);
            else
                nextElementName = "";

            // generate one nested filtering path
            if (element.getSizeOfChildren() > 1) {
                z = element.getZipf();
                do {
                    childIndex = z.probe();
                    nextElementName2 = element.getChild(childIndex);
                } while (nextElementName.equals(nextElementName2));

                branch = generateRelativePath(maxDepth, npLevel+1,
                        wildcard, dSlash, 0, levelDist,
                        0, levelPathNesting-1, joinProb,
                        nextElementName2);
                if (m_distinctQueries && noNestedPaths > 1) {
                    String s = branch.toString();
                    if (nestedPaths[npLevel] == null) {
                        nestedPaths[npLevel] = new ArrayList();
                        nestedPaths[npLevel].add(s);
                    }
                    else {
                        // check if the nested path is distinct at this level
                        // if it is, insert it in the sorted order
                        int size2 = nestedPaths[npLevel].size();
                        int j;
                        for (j=0; j<size2; j++) {
                            int result = s.compareTo((String)nestedPaths[npLevel].get(j));
                            if (result==0) { // identical nested path
                                branch = null;
                                break;
                            }
                            if (result < 0) {
                                nestedPaths[npLevel].add(j,s);
                                break;
                            }
                        }
                        if (j == size2)
                            nestedPaths[npLevel].add(s);
                    }
                }

                if (branch != null) {
                    if (!(m_distinctQueries && noNestedPaths > 1)) {
                        query = (StringBuffer)stepList.get(npLevel);
                        query.append("[");
                        query.append(branch);
                        query.append("]");
                    }
                }
                else
                    i--;

            }
            else {
                i--;
            }
        }
        if (tries == maxTry)
            return null;

        /// assemble element axis, name tests and predicates ///

        query = new StringBuffer();
        for (int i=0; i<size; i++) {
            query.append(((StringBuffer)stepList.get(i)).toString());

            if (m_distinctQueries && noPreds > 1)
                if (predicates[i] != null) {
                    int size2 = predicates[i].size();
                    for (int j=0; j<size2; j++) {
                        //query.append("[");
                        query.append((String)predicates[i].get(j));
                        //query.append("]");
                    }
                }

            if (m_distinctQueries && noNestedPaths > 1)
                if (nestedPaths[i] != null) {
                    int size2 = nestedPaths[i].size();
                    for (int j=0; j<size2; j++) {
                        query.append("[");
                        query.append((String)nestedPaths[i].get(j));
                        query.append("]");
                    }
                }
        }
        //System.out.println(query);
        return query;
    }


    /**
     * this function differs from above by using probability for
     * generation of predicates and nested path expression.
     */

    public StringBuffer  generateOnePath(int maxDepth, int currentLevel, double wildcard,
                                         double dSlash, double predProb, char levelDist,
                                         double nestedPathProb, int levelPathNesting,
                                         double joinProb, String startElement)
    {
        double coin;

        StringBuffer query = new StringBuffer();

        String elementName = startElement;	// a query either starts with /startElement or //
        int level = currentLevel;		// can not be larger than depth

        boolean inDoubleSlashMode = false;
        int skipLevels = 0;

        //boolean afterStar = false, afterDslash = false;
        while (level < maxDepth) {
            // the exit of while is controlled by maxDepth
            // or when an element has no child, so
            // actual depth can be shorter than maxDepth
            dtdElement element = (dtdElement)m_elements.get(elementName);
            // the current element in consideration
            //System.out.println("level "+level+": "+elementName);

            if (element.getSizeOfChildren() > 0 ) {
                //current element has children
                /// find the element in the next level ///
                Zipf z = element.getZipf();
                int childIndex1 = z.probe();
                // childIndex1 is for the main branch
                int childIndex2;		// childIndex2 is for a nested path in a predicate

                ///  generate the operator, name test and predicates for the current level ///
                if (inDoubleSlashMode && skipLevels > 0) {
                    // skipping phase
                    skipLevels--;
                }
                else {					// inDoubleSlashmode == false or
                    // it is true && skiplevel = 0

                    // choose an operator for this location step
                    // dslash controls the occurrence of "//"
                    // since two consecutive "//"s donot make sence, so
                    // if it is inDoubleSlashMode, this occurrence is suppressed

                    if (inDoubleSlashMode==false && rand.nextDouble()< dSlash ) {
                        //afterDslash = true;
                        inDoubleSlashMode = true;
                        query.append("/");
                        /// determine the next element occurs at which level ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        //skipLevels = (int)Math.floor(coin * (maxDepth - 1 - level));
                        skipLevels = (int)Math.floor(coin * (maxDepth - level));
                        //if (skipLevels == 0)
                        //	inDoubleSlashMode = false;
                    }

                    if (skipLevels > 0)
                        skipLevels --;
                    // if (skipLevels == 0)
                    else {
                        /// choose wildcard or not at this step, controlled by wildcard ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        if (1-coin < wildcard) {
                            // * is printed instead of elementName, but the path is
                            // already chosen along elementName and its children
                            //afterStar = true;
                            //query.append("/*");
                            // query writing: rule 2
                            if (inDoubleSlashMode) {
                                query.append("*");
                            }
                            else {
                                query.append("/*");
                            }
                        }
                        else {
                            //afterStar = false;
                            //afterDslash = false;
                            inDoubleSlashMode = false;
                            query.append("/" + elementName);
                        }

                        /// generate predicates ///
                        generatePredicates(query, element, predProb);

                        /// create a nested path ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        // generate one nested filtering path
                        if (coin < nestedPathProb && level < maxDepth-1
                                && element.getSizeOfChildren() > 1) {
                            // at least a nested path will be generated
                            StringBuffer branch = null;
                            StringBuffer branch1 = null;
                            StringBuffer branch2 = null;
                            do {
                                childIndex2 = z.probe();
                            } while (childIndex2 == childIndex1);
                            branch = generateRelativePath(maxDepth, level+1,
                                    wildcard, dSlash, predProb, levelDist,
                                    nestedPathProb, levelPathNesting-1, joinProb,
                                    element.getChild(childIndex2));

                            // or generate a join of two nested filtering paths
                            boolean join = false;
                            if (element.getSizeOfChildren()>2 && rand.nextDouble()< joinProb) {
                                // it is possible to generate a join
                                do {
                                    childIndex2 = z.probe();
                                } while (childIndex2 == childIndex1);
                                branch1 = generateRelativeJoinPath(maxDepth, level+1,
                                        wildcard, dSlash, predProb, levelDist,
                                        nestedPathProb, levelPathNesting-1, joinProb,
                                        element.getChild(childIndex2));

                                int childIndex3 = -1;
                                do {
                                    childIndex3 = z.probe();
                                } while (childIndex3==childIndex1 || childIndex3==childIndex2);
                                branch2 = generateRelativeJoinPath(maxDepth, level+1,
                                        wildcard, dSlash, predProb, levelDist,
                                        nestedPathProb, levelPathNesting-1, joinProb,
                                        element.getChild(childIndex3));
                                if (branch1!=null && branch2!=null)
                                    join = true;
                            }
                            if (join == false) {
                                if (branch != null)
                                    query.append("["+branch+"]");
                            }
                            else
                                query.append("["+branch1+"="+branch2+"]");
                        }
                        if (inDoubleSlashMode)
                            query.append("/");
                    }// end if skipLeves == 0
                } // end of else (inDoubleSlashMode && skipLevels > 0)


                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                elementName = element.getChild(childIndex1);
                level++;
            } // end if element.getSizeOfChildren() > 0

            else{
                // this element doesn't have children
                /// choose "//" controlled by dslash and inDoubleSlashMode ///
                if (inDoubleSlashMode == false) {
                    //coin = rand.nextDouble();
                    //System.out.println(coin);
                    if (rand.nextDouble() < dSlash ) {
                        query.append("/");
                        inDoubleSlashMode = true;
                    }
                }
                /// choose wildcard or not at this step, controlled by wildcard ///
                if ((1-rand.nextDouble()) < wildcard) {
                    if (inDoubleSlashMode)
                        query.append("*");
                    else
                        query.append("/*");
                }
                else
                    query.append("/" + elementName);

                /// generate predicates ///
                generatePredicates(query, element, predProb);

                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                break;
            }
        }
        int size = query.length();
        //System.out.println("PathGenerator::generateOnePath -- "+query.toString());

        if (query.charAt(size-1) == '/')
            query.deleteCharAt(size-1);

        //System.out.println("PathGenerator::generateOnePath -- "+query.toString());
        //if (query.length() == 0) {
        //    System.out.println("level = "+currentLevel);
        //    System.out.println("maxDepth = "+maxDepth);
        //    System.out.println("startElement = "+startElement);
        //}
        return query;
    }

	/**
	 * this is a simpler version of the above function without an
	 * explicit root element. so the default root element will be used.
	 */

	public StringBuffer  generateOnePath(int maxDepth, int currentLevel, double wildcard,
										 double dSlash, double predProb, char levelDist,
										 double nestedPathProb, int levelPathNesting,
										 double joinProb) {
		return generateOnePath(maxDepth, currentLevel, wildcard, dSlash,
				predProb, levelDist, nestedPathProb, levelPathNesting, joinProb,
				m_rootElement);
	}

	/**
	 * this is an even simpler variation of the above function, without
	 * consideration of deep nesting of paths, joins. the default root
	 * element will be used.
	 */

	public StringBuffer  generateOnePath(int maxDepth, int currentLevel, double wildcard,
										 double dSlash, double predProb, char levelDist,
										 double nestedPathProb) {
		return generateOnePath(maxDepth, currentLevel, wildcard, dSlash,
				predProb, levelDist, nestedPathProb, 1, 0, m_rootElement);
	}

	/**
	 * this is an another simpler variation without consideration of 
	 * deep nesting of paths, joins. but the root element is provided
	 */

	public StringBuffer  generateOnePath(int maxDepth, int currentLevel, double wildcard,
										 double dSlash, double predProb, char levelDist,
										 double nestedPathProb, String root) {
		return generateOnePath(maxDepth, currentLevel, wildcard, dSlash,
				predProb, levelDist, nestedPathProb, 1, 0, root);
	}

    ////////////////////////////////
    /// generate a relative path ///
    ////////////////////////////////

    /**
     * generate a relative path 
     **/
    public StringBuffer  generateRelativePath(int maxDepth, 
    											int currentLevel, 
												double wildcard,
												double dSlash, 
												double predProb, 
												char levelDist,
												double nestedPathProb, 
												int levelPathNesting, 
												double joinProb)
    {
        return generateRelativePath(maxDepth, currentLevel, wildcard, dSlash, predProb, levelDist,
                nestedPathProb, levelPathNesting, joinProb, m_rootElement);
    }

    public StringBuffer  generateRelativePath(int maxDepth, 
    											int currentLevel, 
												double wildcard,
												double dSlash, 
												double predProb, 
												char levelDist,
												double nestedPathProb, 
												int levelPathNesting, 
												double joinProb,
												String startElement)
    {

        double coin;

        StringBuffer query = new StringBuffer();

        String elementName = startElement;	// a query either starts with /startElement or //
        int level = currentLevel;		// can not be larger than depth

        boolean inDoubleSlashMode = false;
        int skipLevels = 0;

        while (level < maxDepth) {
            // the exit of while is controlled by maxDepth
            // or when an element has no child, so
            // actual depth can be shorter than maxDepth
            dtdElement element = (dtdElement)m_elements.get(elementName);
            //System.out.println("[level "+level);
            //System.out.println(elementName+"]");

            if (element.getSizeOfChildren() > 0 ) {
                //current element has children
                /// find the element in the next level ///
                Zipf z = element.getZipf();
                int childIndex1 = z.probe();
                // childIndex1 is for the main branch
                int childIndex2;		// childIndex2 is for a nested path in a predicate

                /// generate the operator, name test and predicates for the current level ///
                if (inDoubleSlashMode && skipLevels > 0) {
                    // skipping phase
                    skipLevels--;
                }
                else {					// inDoubleSlashmode == false or
                    // it is true && skiplevel = 0

                    // choose an operator for this location step
                    // dslash controls the occurrence of "//"
                    // since two consecutive "//"s donot make sence, so
                    // if it is inDoubleSlashMode, this occurrence is suppressed

                    if (inDoubleSlashMode==false && level>currentLevel && rand.nextDouble()< dSlash ) {
                        //afterDslash = true;
                        inDoubleSlashMode = true;
                        query.append("/");
                        /// determine the next element occurs at which level ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        //skipLevels = (int)Math.floor(coin * (maxDepth - 1 - level));
                        skipLevels = (int)Math.floor(coin * (maxDepth - level));
                        //if (skipLevels == 0)
                        //	inDoubleSlashMode = false;
                    }

                    if (skipLevels > 0)
                        skipLevels --;

                    else {
                        //if (skipLevels == 0) {
                        /// choose wildcard or not at this step, controlled by wildcard ///
                        if (level>currentLevel && 1-rand.nextDouble() < wildcard) {
                            // * is printed instead of elementName, but the path is
                            // already chosen along elementName and its children
                            //afterStar = true;
                            //query.append("/*");
                            // query writing: rule 2
                            if (inDoubleSlashMode) {
                                query.append("*");
                            }
                            else {
                                query.append("/*");
                            }
                        }
                        else {
                            //afterStar = false;
                            //afterDslash = false;
                            inDoubleSlashMode = false;
                            if (level > currentLevel)
                                query.append("/");
                            query.append(elementName);
                        }

                        /// generate predicates ///
                        generatePredicates(query, element, predProb);

                        /// create a nested path ///
                        coin = rand.nextDouble();
                        if (levelPathNesting > 0 && coin < nestedPathProb && level < maxDepth-1
                                && element.getSizeOfChildren() > 1) {
                            // at least a nested path will be generated
                            StringBuffer branch = null, branch1 = null, branch2 = null;
                            do {
                                childIndex2 = z.probe();
                            } while (childIndex2 == childIndex1);
                            branch = generateRelativePath(maxDepth, level+1,
                                    wildcard, dSlash, predProb, levelDist,
                                    nestedPathProb, levelPathNesting-1, joinProb,
                                    element.getChild(childIndex2));
                            if (branch != null)
                                query.append("["+branch+"]");
                        }


                        if (inDoubleSlashMode)
                            query.append("/");
                    }
                }

                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                elementName = element.getChild(childIndex1);
                level++;
            }
            else{
                // this element doesn't have children
                /// choose "//" controlled by dslash and inDoubleSlashMode ///
                if (inDoubleSlashMode == false && level>currentLevel) {
                    //coin = rand.nextDouble();
                    //System.out.println(coin);
                    if (rand.nextDouble() < dSlash ) {
                        query.append("/");
                        inDoubleSlashMode = true;
                    }
                }
                /// choose wildcard or not at this step, controlled by wildcard ///
                if (level>currentLevel && (1-rand.nextDouble()) < wildcard) {
                    if (inDoubleSlashMode)
                        query.append("*");
                    else
                        query.append("/*");
                }
                else {
                    if (level > currentLevel)
                        query.append("/");
                    query.append(elementName);
                }
                /// generate predicates ///
                generatePredicates(query, element, predProb);

                m_currentLevel = level;
                m_currentElement = element;
                m_currentElementName = elementName;

                break;
            }
        }
        int len = query.length();
        if (query.charAt(len-1) == '/')
        	query = query.deleteCharAt(len-1);
        return query;
    }


    /**
     *  greedily look for a path that has PCData for join 
     **/

    public StringBuffer  generateRelativeJoinPath(int maxDepth, int currentLevel, double wildcard,
                                                  double dSlash, double predProb, char levelDist,
                                                  double nestedPathProb, int levelPathNesting, double joinProb,
                                                  String startElement)
    {

        double coin;

        StringBuffer query = new StringBuffer();

        String elementName = startElement;	// a query either starts with /startElement or //
        int level = currentLevel;			// can not be larger than depth

        boolean inDoubleSlashMode = false;
        int skipLevels = 0;

        while (level < maxDepth) {
            // the exit of while is controlled by maxDepth
            // or when an element has no child, so
            // actual depth can be shorter than maxDepth
            dtdElement element = (dtdElement)m_elements.get(elementName);
            // if the current element has PCDATA, Good!
            // generate the element's attributes and return it
            if (element.getPCData()){
                if (level > currentLevel || inDoubleSlashMode)
                    query.append("/");
                query.append(elementName);
                /// generate predicates ///
                // predicate on data element
                // coin = rand2.nextDouble();
                // if (coin < predProb) {
                //	int maxValue = element.getNoDataValues();
                //	int value = (int)Math.ceil(1 + rand2.nextDouble() * (maxValue - 1));
                //	query.append("[text()="+value+"]");
                // }
                // predicates on attriutes
                String[] attrs = element.getAttributes();
                int[] attrValues = element.getAttrValues();
                for (int i=0; i<attrs.length; i++) {
                    coin = rand2.nextDouble();
                    if (coin < predProb) {
                        int maxValue = attrValues[i];
                        //int value = (int)Math.ceil(1 + rand2.nextDouble() * (maxValue - 1));
                        int value = (int)Math.ceil(rand2.nextDouble() * maxValue);
                        query.append("[@"+attrs[i]+"="+value+"]");
                    }
                }
                // predicate on the position
                /*
                if (rand2.nextDouble() < predProb) {
                    // for simplicity, the position is within [1,MAX_POSITION]
                    // and is only quality predicate
                    int pos = (int)Math.ceil(rand2.nextDouble()*MAX_POSITION);
                    query.append("["+pos+"]");
                }
                */
                return query;
            }
            // otherwise look for one child that allows PCData
            int noChildren = element.getSizeOfChildren();
            if (noChildren > 0 ) {
                // current element has children
                /// find a child that has pc data ///
                Zipf z = element.getZipf();
                int count = 0;
                int childIndex1 = -1;
                boolean found = false;
                do {
                    childIndex1 = z.probe();
                    String childName = element.getChild(childIndex1);
                    dtdElement childElement = (dtdElement)m_elements.get(childName);
                    if (childElement.getPCData()) {
                        found = true;
                        break;
                    }
                    count ++;
                } while (count < noChildren);
                // childIndex1 is for the main branch

                /// determine the operator, name test and predicates for the current level ///
                if (inDoubleSlashMode && skipLevels > 0) {
                    // skipping phase
                    if (found)
                        skipLevels = 0;
                    else
                        skipLevels--;
                }
                else {					// inDoubleSlashmode == false or
                    // it is true && skiplevel = 0

                    // choose an operator for this location step
                    // dslash controls the occurrence of "//"
                    // since two consecutive "//"s donot make sence, so
                    // if it is inDoubleSlashMode, this occurrence is suppressed
                    if (inDoubleSlashMode==false && found == false
                            && level>currentLevel && rand.nextDouble()< dSlash ) {
                        //afterDslash = true;
                        inDoubleSlashMode = true;
                        query.append("/");

                        ///* determine the next element occurs at which level *///
                        coin = rand.nextDouble();
                        //skipLevels = (int)Math.floor(coin * (maxDepth - 1 - level));
                        skipLevels = (int)Math.floor(coin * (maxDepth - level));
                        //if (skipLevels == 0)
                        //	inDoubleSlashMode = false;
                    }

                    if (skipLevels > 0)
                        skipLevels --;
                    else {
                        //if (skipLevels == 0) {
                        /// choose wildcard or not at this step, controlled by wildcard ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        if (1-coin < wildcard && level>currentLevel) {
                            // * is printed instead of elementName, but the path is
                            // already chosen along elementName and its children
                            //afterStar = true;
                            //query.append("/*");
                            // query writing: rule 2
                            if (inDoubleSlashMode) {
                                query.append("*");
                            }
                            else {
                                query.append("/*");
                            }
                        }
                        else {
                            //afterStar = false;
                            //afterDslash = false;
                            inDoubleSlashMode = false;
                            if (level > currentLevel)
                                query.append("/");
                            query.append(elementName);
                        }

                        /// generate predicates ///
                        generatePredicates(query, element, predProb);
                        if (inDoubleSlashMode)
                            query.append("/");
                    }
                }

                elementName = element.getChild(childIndex1);
                level++;
            }
            else{
                // this element doens't have PCData and it doesn't have child elements
                // the generation of a join path fails
                return null;
            }
        }
        return null;
    }

    ///////////////////////////////////////
    /// generate value-based predicates ///
    ///////////////////////////////////////

    private void generatePredicates(StringBuffer query, dtdElement element, double predProb) {
        double coin;
        // predicate on the element data
        boolean dataExists = element.getPCData();
        if (dataExists) {
            coin = rand2.nextDouble();
            //System.out.println(coin);
            if (coin < predProb) {
                int maxValue = element.getNoDataValues();
                coin = rand2.nextDouble();
                //System.out.println(coin);
                //int value = (int)Math.ceil(1 + coin * (maxValue - 1));
                int value = (int)Math.ceil(coin * maxValue);
                query.append("[text()="+value+"]");
            }
        }
        // predicate on the attributes
        String[] attrs = element.getAttributes();
        int[] attrValues = element.getAttrValues();
        for (int i=0; i<attrs.length; i++) {
            coin = rand2.nextDouble();
            //System.out.println(coin);
            if (coin < predProb) {
                int maxValue = attrValues[i];
                coin = rand2.nextDouble();
                //System.out.println(coin);
                //int value = (int)Math.ceil(1 + coin * (maxValue - 1));
                int value = (int)Math.ceil(coin * maxValue);
                query.append("[@"+attrs[i]+"="+value+"]");
            }
        }
        // predicate on the position
        /*
        if (rand2.nextDouble() < predProb) {
            // for simplicity, the position is within [1,MAX_POSITION]
            // and is only quality predicate
            int pos = (int)Math.ceil(rand2.nextDouble()*MAX_POSITION);
            query.append("["+pos+"]");
        }
        */
    }


    public void generateOnePredicate(StringBuffer query, dtdElement element) {
        double coin;
        boolean found = false;

        // though we only need to find one predicate, we still  //
        // use a probabilistic model to find this predicate for //
        // good randomness and better chance for uniqueness     //

        /// start with predicate on the attributes ///

        String[] attrs = element.getAttributes();
        boolean dataExists = element.getPCData();

        int len = attrs.length;
        int totalChoice = len+1;
        if (dataExists)
            totalChoice++;

		/// predicate on the element data ///
		if (dataExists && rand2.nextDouble() <= 1/(double)totalChoice) {
			int maxValue = element.getNoDataValues();
			coin = rand2.nextDouble();
			//System.out.println(coin);
			int value = (int)Math.ceil(coin * maxValue);
			query.append("[text()="+value+"]");
			found = true;
		}

		/// if still not found, predicate on attributes
        if (found == false && len > 0 /*&& rand2.nextDouble() <= len/(double)totalChoice*/ ) {
            /// determine which attribute to choose ///
            int index = (int)Math.floor(rand2.nextDouble() * attrs.length);

            /// determine the value to choose ///
            int[] attrValues = element.getAttrValues();
            int maxValue = attrValues[index];
            coin = rand2.nextDouble();
            //System.out.println(coin);
            int value = (int)Math.ceil(coin * maxValue);
            query.append("[@"+attrs[index]+"="+value+"]");
            found = true;
        }

        /// if still not found, predicate on the position ///
        /*
        if (found == false) {
            // for simplicity, the position is within [1,MAX_POSITION]
            // and is only quality predicate

            int pos = (int)Math.ceil(rand2.nextDouble()*MAX_POSITION);
            query.append("["+pos+"]");
        }
        */
    }

    ////////////////////////////////////////////////
    /// retrieve information on path constrution ///
    ////////////////////////////////////////////////

    public dtdElement getCurrentElement() {
        return m_currentElement;
    }

    public String getCurrentElementName() {
        return m_currentElementName;
    }

    public int getCurrentLevel() {
        return m_currentLevel;
    }

    ////////////////////////////////////////////////////////
    /// extension to generate paths for FLWR expressions ///
    ////////////////////////////////////////////////////////

    public ArrayList generatePredicatePaths(int numPredicatePaths, int maxDepth,
                                            double wildcard, double dSlash, dtdElement element) {
        ArrayList paths = new ArrayList(numPredicatePaths);

        int maxTry = numPredicatePaths * 3;
        StringBuffer path;
        HashSet pathSet = new HashSet(numPredicatePaths);
        String startElementName;
        Zipf z;
        for (int i=0, tries=0; i<numPredicatePaths && tries < maxTry; i++, tries++) {
            z = element.getZipf();
            int childIndex1 = z.probe();
            startElementName = element.getChild(childIndex1);
            path = generateSimplePath(maxDepth, wildcard, dSlash, "predicate", startElementName);
            if (path!=null && pathSet.add(path.toString()))
                paths.add(path);
            else
                i--;
        }
        return paths;
    }

    public ArrayList generateSubSelects(int numSubSelects, int maxDepth,
                                        double wildcard, double dSlash, dtdElement element) {
        ArrayList paths = new ArrayList(numSubSelects);

        int maxTry = numSubSelects * 3;
        StringBuffer path;
        HashSet pathSet = new HashSet(numSubSelects);
        String startElementName;
        Zipf z;

        int i = 0;
        if (element.getSizeOfChildren() > 0) {
            for (int tries=0; i<numSubSelects && tries < maxTry; i++, tries++) {
                z = element.getZipf();
                int childIndex1 = z.probe();
                startElementName = element.getChild(childIndex1);
                path = generateSimplePath(maxDepth, wildcard, dSlash, "select-node", startElementName);
                if (path!=null && pathSet.add(path.toString()))
                    paths.add(path);
                else
                    i--;
            }
        }
        if (i < numSubSelects) {
            /// trivial selects:  the node element itself or its data or attributes ///
            if (i == 0 && numSubSelects == 1)
            /// select the node itself ///
                paths.add(new StringBuffer("/."));
            else {
                if (element.getPCData()) {
                    /// select its data ///
                    paths.add(new StringBuffer("/.[text()]"));
                    i++;
                }

                /// select its attributes ///
                String[] attributes = element.getAttributes();
                int noAttrs = attributes.length;
                for (int j=0; i<numSubSelects && j<noAttrs; i++, j++) {
                    path = new StringBuffer("/.[@");
                    path.append(attributes[j]);
                    path.append("]");
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    public StringBuffer  generateSimplePath(int maxDepth, double wildcard,
                                            double dSlash, String type, String startElement)
    {

        double coin;

        StringBuffer query = new StringBuffer();

        String elementName = startElement;	// a query either starts with /startElement or //
        int level = 0;				// can not be larger than depth
        dtdElement element = null;

        boolean inDoubleSlashMode = false;
        int skipLevels = 0;

        while (level < maxDepth) {
            // the exit of while is controlled by maxDepth
            // or when an element has no child, so
            // actual depth can be shorter than maxDepth
            element = (dtdElement)m_elements.get(elementName);

            if (element.getSizeOfChildren() > 0 ) {
                /// find the element in the next level ///
                Zipf z = element.getZipf();
                int childIndex1 = z.probe();

                ///  generate the operator, name test and predicates for the current level ///
                if (inDoubleSlashMode && skipLevels > 0) {
                    // skipping phase
                    skipLevels--;
                }
                else {
                    // inDoubleSlashmode == false or
                    // it is true && skiplevel = 0

                    // choose an operator for this location step
                    // dslash controls the occurrence of "//"
                    // since two consecutive "//"s donot make sence, so
                    // if it is inDoubleSlashMode, this occurrence is suppressed

                    if (inDoubleSlashMode==false && rand.nextDouble()< dSlash ) {
                        //afterDslash = true;
                        inDoubleSlashMode = true;
                        query.append("/");
                        /// determine the next element occurs at which level ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        //skipLevels = (int)Math.floor(coin * (maxDepth - 1 - level));
                        skipLevels = (int)Math.floor(coin * (maxDepth - level));
                        //if (skipLevels == 0)
                        //	inDoubleSlashMode = false;
                    }

                    if (skipLevels > 0)
                        skipLevels --;
                    else {
                        //if (skipLevels == 0) {
                        /// choose wildcard or not at this step, controlled by wildcard ///
                        coin = rand.nextDouble();
                        //System.out.println(coin);
                        if (1-coin < wildcard) {
                            // * is printed instead of elementName, but the path is
                            // already chosen along elementName and its children

                            //query.append("/*");
                            // query writing: rule 2
                            if (inDoubleSlashMode) {
                                query.append("*");
                            }
                            else {
                                query.append("/*");
                            }
                        }
                        else {
                            inDoubleSlashMode = false;
                            query.append("/" + elementName);
                        }

                        if (inDoubleSlashMode)
                            query.append("/");
                    }
                }

                elementName = element.getChild(childIndex1);
                level++;
            }
            else{ // this element doesn't have children

                /// choose "//" controlled by dslash and inDoubleSlashMode ///
                if (inDoubleSlashMode == false) {
                    //coin = rand.nextDouble();
                    //System.out.println(coin);
                    if (rand.nextDouble() < dSlash ) {
                        query.append("/");
                        inDoubleSlashMode = true;
                    }
                }
                /// choose wildcard or not at this step, controlled by wildcard ///
                if ((1-rand.nextDouble()) < wildcard) {
                    if (inDoubleSlashMode)
                        query.append("*");
                    else
                        query.append("/*");
                }
                else
                    query.append("/" + elementName);

                break;
            }
        }
        int size = query.length();
        if (query.charAt(size-1) == '/')
            query.deleteCharAt(size-1);

        /// generate a predicate if required ///
        if (type.equals("predicate") && element != null) {
            generateOnePredicate(query, element);

        }
        //else if (type.equals("select-node")) {
        //    /// select the nodes ///
        //    return query;
        //}
        else if (type.equals("select-data")) {
            /// selects data ///
            if (element.getPCData())
                query.append("[text()]");
            else
                query = null;
        }
        else if (type.equals("select-attribute")) {
            // selects attribute ///
            if (!selectOneAttribute(query, element))
                query = null;
        }

        return query;
    }

    private boolean selectOneAttribute(StringBuffer query, dtdElement element) {
        // it doesnot always succeed
        boolean found = false;

        String[] attrs = element.getAttributes();
        if (attrs.length > 0) {
            /// determine which attribute to choose ///
            int index = (int)Math.floor(rand2.nextDouble() * attrs.length);
            query.append("[@"+attrs[index]+"]");
            found = true;
        }
        return found;
    }

    ////////////////////////
    /// output functions ///
    ////////////////////////

    public void printStats() {

        System.out.println("Printing DTD statistics in Query Generator to file elementStats.tmp.txt...");

        try {
            PrintWriter fileOut = new PrintWriter(new FileOutputStream("elementStats.tmp.txt"));
            fileOut.println("ROOT "+m_rootElement);
            fileOut.println();
            Enumeration e = m_elements.keys();
            String akey;
            dtdElement element;
            Zipf z;
            int count = 0;
            while (e.hasMoreElements()){
                akey = (String)(e.nextElement());
                element = (dtdElement)(m_elements.get(akey));

                fileOut.println("ELEMENT "+count);
                fileOut.println(akey+'\t'+element.getNoDataValues());
                for (int i=0; i<element.getSizeOfChildren(); i++){
                    fileOut.print(element.getChild(i)+'\t');
                }
                fileOut.println();
                z = element.getZipf();
                double[] childProbs = z.getProbs();
                for (int i=0; i<childProbs.length; i++){
                    fileOut.print(childProbs[i]);
                    fileOut.print('\t');
                }
                fileOut.println();
                for (int i=0; i<(element.getAttributes()).length; i++){
                    fileOut.print((element.getAttributes())[i]+'\t');
                }
                fileOut.println();
                for (int i=0; i<element.getAttrValues().length; i++){
                    fileOut.print(element.getNoAttrValues(i));
                    fileOut.print('\t');
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


    ///////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
    	/// 1. command line processing ///
		if (args.length < 6) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("NAME\n");
			sb.append("\t edu.berkeley.cs.db.yfilterplus.querygenerator.PathGenerator \n" +				"\t - Generates a set of XPath paths according to workload parameters. \n\n");
			sb.append("SYNOPSIS\n");
			sb.append("\t java edu.berkeley.cs.db.yfilterplus.querygenerator.PathGenerator \n");
			sb.append("\t\tDTD/STAT_FILE\tOUTPUT_FILE\tNUM_QUERIES \n\t\tMAX_QUERY_DEPTH\tPROB_WILDCARD\tPROB_DOUBLESLASH\n\t\t[options]\n\n");
			sb.append("PARAMETERS\n\n");
			sb.append("DTD/STAT_FILE\n");
			sb.append("\t File containing the DTD or statistics.\n\n");
			sb.append("OUTPUT_FILE\n");
			sb.append("\t Output file.\n\n");
			sb.append("NUM_QUERIES\n");
			sb.append("\t Number of queries to be generated.\n\n");
			sb.append("MAX_QUERY_DEPTH\n");
			sb.append("\t Maximum query depth.\n\n");
			sb.append("PROB_WILDCARD\n");
			sb.append("\t Probability of the wildcard occurrence in each location step.\n\n");
			sb.append("PROB_DOUBLESLASH\n");
			sb.append("\t Probability of the double-slash occurrence in each location step.\n\n");
			sb.append("OPTIONS\n\n");
			sb.append("--theta=REAL\n");
			sb.append("\t Distribution of the child elements appearing inside a chosen element.\n\t REAL is between 0 and 1:");
			sb.append(" 0 - uniform distribution;");
			sb.append(" 1 - skewed zipf distribution.\n\n");
			sb.append("--num_predicates=NUM\n");
			sb.append("\t Number of predicates per query.\n\n");
			sb.append("--num_nestedpaths=NUM\n");
			sb.append("\t Number of nested paths per query.\n\n");
			sb.append("--prob_predicate=REAL\n");
			sb.append("\t Probability of each possible predicate appearing in a query.\n\n");
			sb.append("--prob_nestedpath=REAL\n");
			sb.append("\t Probability of having a nested path in each location step.\n\n");
			sb.append("--distinct=VALUE\n");
			sb.append("\t Whether generated paths are distinct. VALUE can be one of TRUE or FALSE.\n\n");
			sb.append("\nDIAGNOSTICS\n");
			sb.append("\nBUGS\n");
			
			
			System.err.println(sb.toString());
			
			System.exit(0);
		} 

		String fileName = null, outFile = null;
		int numQueries = 0, maxDepth = 6;
		double wildcard = 0.2, dSlash = 0.2, theta = 0;
		double predProb = 0, nestedPathProb = 0;
		boolean distinct = false;
		
		if (PathGenerator.PUBLIC_USE) {
		
			// required parameters
		
			fileName = args[0];
        
			outFile = args[1];
		
			numQueries = Integer.parseInt(args[2]);
        	
			maxDepth = Integer.parseInt(args[3]);
			// default 6;
        
			wildcard = Double.parseDouble(args[4]);
			// default 0.2;
        	
			dSlash = Double.parseDouble(args[5]);
			// default 0.2;
		
			// options
		
			for (int i=6; i<args.length; i++) {
			
				if (args[i].startsWith("--theta=")) {				
					theta = Double.parseDouble(args[i].substring(args[i].indexOf('=') + 1));
					// theta=1 gives very skewed zipf, theta=0 yields uniform distribution
				}
				else if (args[i].startsWith("--num_predicates=")) {
					predProb = Double.parseDouble(args[i].substring(args[i].indexOf('=') + 1));
					// default = 0;
				}
				else if (args[i].startsWith("--prob_predicate=")) {
					predProb = Double.parseDouble(args[i].substring(args[i].indexOf('=') + 1));
					if (predProb == 1)
						predProb = 0.9999;
					// default = 0;
				}
				else if (args[i].startsWith("--num_nestedpaths=")) {
					nestedPathProb = Double.parseDouble(args[i].substring(args[i].indexOf('=') + 1));
					// default = 0;
				}
				else if (args[i].startsWith("--prob_nestedpath=")) {
					nestedPathProb = Double.parseDouble(args[i].substring(args[i].indexOf('=') + 1));
					if (nestedPathProb == 1)
						nestedPathProb = 0.9999;
					// default = 0;
				}
				else if (args[i].startsWith("--distinct=")) {
					if (args[i].startsWith("--distinct=TRUE"))
						distinct = true;
				}
				else {
					System.err.println("\nPathGenerator::main -- \n\t\tUnknown option: "+args[i]);
					System.exit(0);
				}
			}
			
			// limitations: cannot mix prob_predicate with num_nestedpaths, or num_predicates with num_predicates
			if (predProb > 0 && predProb < 1 && nestedPathProb >= 1 ||
				nestedPathProb > 0 && nestedPathProb < 1 && predProb >= 1) {
					System.err.println("\nPathGenerator::main -- ");
					System.err.println("\t\tCannot mix the uses of NUMBER and PROBABILITY for predicates and nested paths!");
					System.err.println("\t\tUse either NUMBER or PROBABILITY for both of them.");
					System.exit(0);
				}
		}	
		else { 
		
        	// old program argument processing

        	fileName = args[0];
        
        	outFile = args[1];
		
        	numQueries = Integer.parseInt(args[2]);
        	
        	maxDepth = Integer.parseInt(args[3]);
        	// default 6;
        
        	wildcard = Double.parseDouble(args[4]);
        	// default 0.2;
        	
        	dSlash = Double.parseDouble(args[5]);
        	// default 0.2;
        
        	theta = Double.parseDouble(args[6]);
        	// theta=1 gives very skewed zipf, theta=0 yields uniform distribution
        	
        	predProb = Double.parseDouble(args[7]);
        	// default 0;
        
        	nestedPathProb = Double.parseDouble(args[8]);
        	// default 0;
        
        	if (args.length >= 10 && (args[9].equals("true") || args[9].equals("TRUE")))
            	distinct = true;
		}
		
        // additinal -- deprecated //
        
        char levelDist = 'u';
        // default 'u' -- uniform;
        // 's' -- heavy start, 'e' -- heavy end, 'm' -- heavy middle
        
        int levelPathNesting = 1;
        
        double joinProb = 0;
			
        /// 2. Read in the statistics of DTD elements ///
        PathGenerator pg = null;
        if (fileName.endsWith(".dtd")) {
            pg = new PathGenerator(fileName, theta);
        }
        else if (fileName.endsWith(".stat")) {
            DTDStatReader dtdReader = new DTDStatReader();
            dtdReader.readStatFromFile(fileName);
            Hashtable dtdElementStats = dtdReader.getElements();
            String root = dtdReader.getRoot();
            pg = new PathGenerator(root, dtdElementStats, theta);
        }
        //qg.printStats();

		/// 3. generate path expressions ///
		
        if (distinct)
            pg.generateDistinctPaths(numQueries, maxDepth, wildcard, dSlash, predProb, levelDist,
                    nestedPathProb, levelPathNesting, joinProb, outFile);
        else
            pg.generatePaths(numQueries, maxDepth, wildcard, dSlash, predProb, levelDist,
                    nestedPathProb, levelPathNesting, joinProb, outFile);

    }

}
