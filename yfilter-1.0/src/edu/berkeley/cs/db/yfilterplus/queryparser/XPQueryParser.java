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

package edu.berkeley.cs.db.yfilterplus.queryparser;

import edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser.*;
import java.io.*;
import java.util.*;

public class XPQueryParser implements QueryParser{

    protected BufferedReader m_in = null;
    protected int m_noQueries = 0;

    private boolean DEBUG = false;
    private boolean ECHO = false;

    ///////////////////
    /// constructor ///
    ///////////////////

    public XPQueryParser(String fileName) {

        System.out.println("Reading queries from file "+fileName+"...");
        try {
            m_in = new BufferedReader(new FileReader(fileName));
        }
        catch  (Exception e){
            e.printStackTrace();
        }
    }

    ////////////////////////////////
    /// methods in the interface ///
    ////////////////////////////////

    public String readNextQueryString() {
        String line = null;

        try {
            // read a non-emtpy line
            while ((line = m_in.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0)
                    break;
            }
            if (line == null)
                m_in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public Query readNextQuery() {
        XPQuery compiled_query = null;
        PathQuery parsed_query = null;

        try {
            // read a non-emtpy line
            String line;
            while ((line = m_in.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0)
                    break;
            }

            // parse the query contained in this line
            if (line != null) {                            
                if (ECHO)
                    System.out.println("Query "+(m_noQueries+1)+": "+line);

                StringReader queryInput = new StringReader(line);
                XPathParser p = new XPathParser(queryInput);
                // use the DEBUG flag for CUP
                p.setDebug(DEBUG);
                if (DEBUG)
                    System.out.println("Start parsing...");
                java_cup.runtime.Symbol s = null;
                try {
                    s = p.parse();
                } catch (Exception e) {
                    System.out.println("Exception while parsing: " + e.toString());
                    e.printStackTrace();
                }
                if (DEBUG) {
                    System.out.println("Parsing complete!");
                }

                if (s != null && s.value != null) {
                    parsed_query = (PathQuery)s.value;
                    if (DEBUG) {
                        System.out.println("\nParsed query:\n"+parsed_query);
                        System.out.println("\nCompiling the query...");
                    }

                    compiled_query = compile(parsed_query);
                    compiled_query.setQueryId(++m_noQueries);
                    if (DEBUG) {
                        System.out.println("\nCompiled query:\n"+compiled_query);
                        System.out.println("Compilation complete!");
                        //compiled_query.printStepElementsToFile();
                    }
                }
            }

            else {
                compiled_query = null;
                m_in.close();
            }
        }
        catch  (Exception e){
            e.printStackTrace();
        }

        return compiled_query;
    }

    public Query [] readNextQueriesBulk(int num) {
        System.out.println("XPQueryParser::readNextQueriesBulk -- Not implemented!");
        return null;
    }

    ///////////////
    /// getters ///
    ///////////////

    public int getNoQueries() {
        return m_noQueries;
    }

    //////////////////////////////
    /// compile a parsed query ///
    //////////////////////////////

    public static XPQuery compile(PathQuery query) throws Exception {
        ArrayList stepExprs = query.getStepExpressions();
        char extraSelectType = query.getExtraSelectType();
        String extraSelectAttr = null;
        if (extraSelectType == SimplePredicate.PREDICATE_ATTRIBUTE)
            extraSelectAttr = query.getExtraSelectAttribute();

        ArrayList compiled_paths = new ArrayList(); // a list of XPPath objects

        LinkedList nested_path_list = new LinkedList();
        nested_path_list.add(stepExprs);
        while (nested_path_list.size() > 0) {
            Object ob = nested_path_list.removeFirst();

            // handle the prefix for a nested path
            ArrayList prefix = null, steps = null;
            int branchingLevel = -1;
            if (ob instanceof ExtendedNestedPath) {
                // prefix copied over from the main path
                prefix = ((ExtendedNestedPath)ob).m_prefix;
                steps =  ((ExtendedNestedPath)ob).m_stepExprs;
                branchingLevel = ((ExtendedNestedPath)ob).m_level;
            }
            else
                steps = (ArrayList)ob;
            int level = branchingLevel;

            // prepare to create the new XPPath object
            ArrayList stringNames = new ArrayList();
            ArrayList simplePredicates = new ArrayList();
            if (prefix != null)
                stringNames.addAll(prefix);

            int size = steps.size();
            for (int i=0; i<size; i++) {
                // axes and element name tests
                StepExpression s = (StepExpression)steps.get(i);
                if (s.getAxis() == StepExpression.DESCENDANT)
                    stringNames.add("$");
                stringNames.add(s.getNameTest());
                level++;

                ArrayList allPredicates = s.getPredicates();
                if (allPredicates != null) {
                    int size2 = allPredicates.size();
                    for (int j = 0; j < size2; j++) {
                        ob = allPredicates.get(j);
                        if (ob instanceof SimplePredicate) {
                            // simple predicates
                            simplePredicates.add(new XPPredicate(level, (SimplePredicate) ob));
                        } else {
                            // path predicates
                            prefix = new ArrayList(stringNames);
                            ExtendedNestedPath np = new ExtendedNestedPath(level, prefix,
                                    ((PathPredicate) ob).getStepExpressions());
                            nested_path_list.add(np);
                        }
                    }
                }
            }

            // handle extra selects at the end of the main path
            if (compiled_paths.size() == 0) {// main path
                if (extraSelectType != SimplePredicate.PREDICATE_UNKNOWN)
                    simplePredicates.add(new XPPredicate(level, extraSelectType, extraSelectAttr));
            }

            String[] ss = new String[stringNames.size()];
            ss = (String[])stringNames.toArray(ss);
            Predicate[] ps = new Predicate[simplePredicates.size()];
            ps = (Predicate[])simplePredicates.toArray(ps);
            XPPath compiled_path = new XPPath(ss, ps, branchingLevel, -1);
            //XPPath compiled_path = new XPPath(
            //					(String[])stringNames.toArray(),
            //					(Predicate[])simplePredicates.toArray(),
            //					 branchingLevel, -1);
            compiled_paths.add(compiled_path);
        }

        Path[] cps = new Path[compiled_paths.size()];
        cps = (Path[])compiled_paths.toArray(cps);
        return new XPQuery(cps, extraSelectType, extraSelectAttr);
    }

    protected static class ExtendedNestedPath {
        protected ArrayList m_prefix;
        protected ArrayList m_stepExprs;
        protected int m_level;

        protected ExtendedNestedPath(int level, ArrayList prefix, ArrayList stepExprs) {
            m_level = level;
            m_prefix = prefix;
            m_stepExprs = stepExprs;
        }
    }
    ///////////////////
    /// test driver ///
    ///////////////////

    public static void main(String[] args) {
        String queryFile = args[0];
        int numQueries = Integer.MAX_VALUE;
        boolean print = false, debug = false, stat=false;
        if (args.length > 1) {
            for (int i=1; i<args.length; i++)
                if (args[i].startsWith("print")) {
                    print = true;
                } else if (args[i].equalsIgnoreCase("debug")) {
                    // use this to set the DEBUG flag
                    debug = true;
                }
                else if (args[i].startsWith("stat")){ 
                	stat = true;
                }
                else {                
                    try {
                        numQueries = Integer.parseInt(args[i]);
                        if (numQueries <=0 )
                            numQueries = Integer.MAX_VALUE;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }

        XPQueryParser qp = new XPQueryParser(queryFile);
        // use the DEBUG flag for CUP
        qp.DEBUG = debug;

        Query tmpQuery;
        XPQuery pathQuery;
        Path[] paths;
        int totalPaths, totalPredicates, totalPathLength;
        totalPaths = totalPredicates = totalPathLength = 0;
        int noQueries = 0;

        while (noQueries < numQueries &&
                (tmpQuery = qp.readNextQuery())!=null) {

            if (!(tmpQuery instanceof XPQuery)) {
                System.out.println("Type error in query parsing:: XPQuery is expected.");
                return;
            }

            pathQuery = (XPQuery) tmpQuery;
            noQueries++;

            if (print)
                System.out.println(pathQuery);

            // collect statistics
            if (stat) {
                totalPaths += pathQuery.getNoPaths();
                totalPredicates += pathQuery.getNoPredicates();
                paths = pathQuery.getPaths();
                totalPathLength += ((XPPath) paths[0]).getNoLocationSteps();
            }
        }

        //int noQueries = qp.getNoQueries();
        System.out.println("\nRead "+noQueries+" queries.\n");
        if (stat) {
            double avgPathLength = ((double) totalPathLength) / noQueries;
            System.out.println(
                    "Avg Length of "+noQueries+" main paths\t\t= " + avgPathLength);
            System.out.println("Total Num of predicates\t\t\t= "+totalPredicates);
            System.out.println("Avg Num of predicate(s) per query\t= "
                    + (double) totalPredicates / noQueries);
            System.out.println("Total Num of paths\t\t\t= "+totalPaths);
            System.out.println("Avg Num of nested path(s) per query\t= "
                    + (totalPaths - noQueries) / (double) noQueries);
        }
    }	
}
