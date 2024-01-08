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
package edu.berkeley.cs.db.yfilter.filter;

import edu.berkeley.cs.db.yfilterplus.xmltree.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;
import edu.berkeley.cs.db.yfilterplus.utility.*;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import javax.xml.parsers.*;
import java.util.*;
import java.io.*;

abstract public class EXfilter extends DefaultHandler{

    /// for in memory document tree ///
    
	// constructed for the second phase of parsing or
	// view construction or output the substree that
	// matches the query. m_eventSequence keeps the document
	// order, while ParsingContext element keeps a
	// parent id for quick reverse retrieval. m_eventIndex
	// provides quick retrieval of a ParsingContext
	// element corresponding to an event Id.
	protected XMLTree m_tree				= null;
    protected ParsingContext[] m_events 	= null;
    protected Hashtable m_eventIndex 		= null;

    /// for bookkeeping in document scan ///
    
    protected Stack m_contextPath;	//history of context, now just a path

    /// for debugging ///
    
    protected boolean m_trace;
    protected PrintStream m_out;
	public static boolean DEBUG_QUERY_PARSING = false;
	public static boolean DEBUG_XML_PARSING = false;

    ///////////////////
    /// constructor ///
    ///////////////////

    public EXfilter() {
        m_contextPath = new Stack();
        m_trace = false;
        m_out = System.out;
    }

    ///////////////////////////
    /// build document tree ///
    ///////////////////////////

    public void setXMLTree(XMLTree tree) {
        ArrayList events = tree.getEvents();
        m_events = new ParsingContext[events.size()];
        m_events = (ParsingContext[])events.toArray(m_events);
        m_eventIndex = tree.getEventIndex();
        //tree = null;
    }

    public void setEventSequence(ArrayList events) {
        m_events = new ParsingContext[events.size()];
        m_events = (ParsingContext[])events.toArray(m_events);
    }

	////////////////////////
	/// query operations ///
	////////////////////////

	abstract public int addQuery(Query query); 
	abstract public void deleteQuery(Query query, int queryId); 

    //////////////////////////////////////////////
    /// drivers and handlers for document scan ///
    //////////////////////////////////////////////

    /**
     * this function calls event based parsing that in turns calls the
     * event handles provided by the subclasses
     * @param filename
     */
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

    /**
     * this function reads an element stream from a parsed representation
     * and calls a similar set of handlers for each element encountered
     */

    public void startParsing() {
        startDocument();
        int size = m_events.length;
        for (int i=0; i<size; i++) {
            ParsingContext c = m_events[i];
            if (!c.isEndElement()) {	// start element
                m_contextPath.push(c);
                startElement();
            }
            else {
                m_contextPath.pop();
                endElement();
            }
        }
        endDocument();
        m_contextPath.clear();
    }

    /**
     * event-based processing according to the SAX programming interface
     */ 

    public void startDocument() {}
    public void startElement(String uri, String local,
                             String elementName , Attributes attrs) {}
    public void characters(char ch[], int start, int count) {}
    public void endElement(String uri, String local, String eleName) {}
    public void endDocument() {}

    public void startElement(){}
    public void endElement(){}


    /**
     * clear the content for the next message
     **/

    public void clear() {
        m_events = null;
        m_eventIndex = null;
    }
	

	/**
	 * Parses the command line and sets filter parameters.
	 * 
	 * @param args command-line arguments
	 */
	static void checkCommandLine (String [] args) {
		if (args.length < 2) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("NAME\n");
			sb.append("\t edu.berkeley.cs.db.yfilter.filter.EXfilter\n");
			sb.append("\t - Processes queries on a set of documents and outputs \n\t   the results of query matching and filtering performance. \n\n");
			sb.append("SYNOPSIS\n");
			sb.append("\t java edu.berkeley.cs.db.yfilter.filter.EXfilter\n");
			sb.append("\t\tDOC_FILE/DIR QUERY_FILE [options]\n\n");
			sb.append("PARAMETERS\n\n");
			sb.append("DOC_FILE/DIR\n");
			sb.append("\t XML document or Directory containing a set of XML documents.\n\n");
			sb.append("QUERY_FILE\n");
			sb.append("\t File containing the queries. \n\n");
			sb.append("OPTIONS\n\n");
			sb.append("--num_docs=NUM\n");
			sb.append("\t Number of documents to be processed.\n\n");
			sb.append("--num_queries=NUM\n");
			sb.append("\t Number of queries to be processed.\n\n");
			sb.append("--profile\n");
			sb.append("\t Enables profiling.\n\n");
			sb.append("--result=LEVEL\n");
			sb.append("\t Output level. LEVEL can be one of: \n");
			sb.append("\t\tNONE - no output.\n");
			sb.append("\t\tBOOLEAN - if query matched (true/false).   \n");
			sb.append("\t\tONE  - one matching element.\n");
			sb.append("\t\tALL  - all matching elements.\n\n");
			sb.append("--output=indiv\n");
			sb.append("\t Output matches to individual files.\n");
			//sb.append("\n--schema=SCHEMA\n");
			//sb.append("\t Schema name for the XML documents.\n");
			sb.append("\n--nfa_opt=LEVEL\n");
			sb.append("\t The NFA execution level. LEVEL can be one of:\n");
			sb.append("\t\t2 -- for NFA execution and result collection\n");
			sb.append("\t\t1 -- for NFA execution and simplified result collection\n\t\t     if queries don't contain predicates\n");
			sb.append("\t\t0 -- for NFA execution only\n");
			sb.append("\n--msg_preparse=FLAG\n");
			sb.append("\t Preparse messages. FLAG can be TRUE or FALSE.\n");
			sb.append("\n--warmup=VALUE\n");
			sb.append("\t Warm-up the filter. VALUE can be one of:\n");
			sb.append("\t\tan XML document file used for warm-up or, \n");
			sb.append("\t\ta positive integer, indicating the number of docs to be used\n\t\tfor warmup if the input is an XML directory.\n");
			//sb.append("\n--repeat=N\n");
			//sb.append("\t Repeat query processing N times.\n");
			sb.append("\nDIAGNOSTICS\n");
			sb.append("\nBUGS\n");
						
			System.err.println(sb.toString());
			
			System.exit(0);
		}
	}

    ////////////////////////////////////////////////////////////
    public static void main(String[] args) {

        //////////////////////////////
        /// read system parameters ///
        //////////////////////////////
		
		checkCommandLine(args);
		
		// read parameters for this test driver		
		String docSource = args[0];		// the file or the directory of xml files
		String queryFile = args[1];		// the query file
		 
		int DOCS = Integer.MAX_VALUE, noQueries = Integer.MAX_VALUE;
		for (int i=2; i<args.length; i++) {
			if (args[i].startsWith("--num_docs="))
				DOCS = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
			else if (args[i].startsWith("--num_queries="))			 
				noQueries = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
		}

		// parse the optional parameters and set system-wide parameters		
		SystemGlobals.outputLevel = SystemGlobals.OUTPUT_BOOLEAN; // for benchmarking
		SystemGlobals.parseCommandLineOptions(args, 2); 

		/*
		/// required parameters ///
        if (argv.length < 4) {
            System.out.println("Not enough arguments: ");
            System.out.println("doc source (dir or file), # docs, query-file, # queries");
            return;
        }
        String docSource = argv[0];             // the file or the directory of xml files
        int DOCS = Integer.parseInt(argv[1]);   // how many files are to be processed
        String queryFile = argv[2];
        int noQueries = Integer.parseInt(argv[3]);

		/// optional parameters ///
		SystemGlobals.outputLevel = SystemGlobals.OUTPUT_BOOLEAN; // for benchmarking 
        SystemGlobals.parseCommandLine(argv, 4);
        */
		 
        ///////////////////////////////////////
        /// variables for performance study ///
        ///////////////////////////////////////

        /// output files ///
        PrintWriter outQueries=null;
        PrintWriter outPerformance=null;
        try {
            if (SystemGlobals.algNo == 0) {
                //if (result)
                outQueries = new PrintWriter(new FileOutputStream("result.txt"));
                outPerformance = new PrintWriter(new FileOutputStream("performance.txt"));
            }
            else {
                //if (result)
                outQueries = new PrintWriter(new FileOutputStream("result.txt", true));
                outPerformance = new PrintWriter(new FileOutputStream("performance.txt", true));
            }
        }
        catch ( IOException ioe ) {
            ioe.printStackTrace();
        }

        /////////////////////////
        /// experiments start ///
        /////////////////////////

        EXfilterBasic xfilterBasic, xfilterTest;
        xfilterBasic = xfilterTest = null;

        /// 1. add queries and build index	///
        int qNum = 0;
        if (SystemGlobals.algNo == SystemGlobals.PREDICATE_SP ||
            SystemGlobals.algNo == SystemGlobals.PREDICATE_SP_SORTBY) {
            xfilterBasic = new EXfilterBasic();
            System.out.println();
            System.out.println("Building index for basic algorithms...");
            
            //QueryParser qp = new XFQueryParser(queryFile);
            QueryParser qp = new XPQueryParser(queryFile);
            Query query;
            while (qNum<noQueries && ((query = qp.readNextQuery())!=null)) {
            	if (EXfilter.DEBUG_QUERY_PARSING)
            		System.out.println(query);
                xfilterBasic.addQuery(query);
                qNum++;
            }            
            //System.out.println();
            //xfilterBasic.printQueryIndex();
            
            xfilterTest =  xfilterBasic;
            System.out.println();
            outQueries.println();
            if (SystemGlobals.algNo == SystemGlobals.PREDICATE_SP) {		// basic
                System.out.println("basic");
                outQueries.println("basic");
            }
        }

        /// 2. start processing an xml document	///

        // read those file names in //
        ArrayList filenames = null;
        
        if (DOCS == Integer.MAX_VALUE) {
        	filenames = DocumentReader.getFileNames(docSource);
        	DOCS = filenames.size();
        }
        else
        	filenames = DocumentReader.getFileNames(docSource, DOCS);

        // filtering time //
        long[] cpuTime = new long[DOCS+1];	// the last one is for the sum
        long[] cpuTime2 = new long[DOCS+1];
        int[] noResults = new int[DOCS+1];
        
        // pre-processing //
        if (SystemGlobals.profiling)
            Profiler.clear();
        xfilterTest.preAllocate();

        // JVM warmup using a separate (and usually large) document //
        // in this case, no message materialization is advised      //
        if (SystemGlobals.warmupDoc != null) {
            System.out.println("\nJVM warmup using "+SystemGlobals.warmupDoc+"...");
            if (SystemGlobals.preparsing) {
                XMLTree tree = new XMLTree(SystemGlobals.warmupDoc);
                xfilterTest.setEventSequence(tree.getEvents());
                xfilterTest.startParsing();
            }
            else {
                xfilterTest.startParsing(SystemGlobals.warmupDoc);
            }
            xfilterTest.clear();
        }

        // start the processing //
        String xmlFile;
        System.out.println("\nProcessing xml documents ...");
        for (int j=1; j<=DOCS; j++) {
            System.out.print(j+" ");

            xmlFile = (String)filenames.get(j-1);
            if (SystemGlobals.preparsing) {
                // document parsing cost //
                long start_time = System.currentTimeMillis();
                XMLTree tree = new XMLTree(xmlFile);
                if (EXfilter.DEBUG_XML_PARSING)
                	tree.print();
                xfilterTest.setEventSequence(tree.getEvents());
                cpuTime2[j-1] = System.currentTimeMillis()-start_time;

                // document processing cost //
                start_time = System.currentTimeMillis();
                for (int k=0; k<SystemGlobals.REPEAT; k++)
                	xfilterTest.startParsing();
                cpuTime[j-1] = System.currentTimeMillis()-start_time;
            }
            else {
                long start_time = System.currentTimeMillis();
                xfilterTest.startParsing(xmlFile);
                cpuTime[j-1] = System.currentTimeMillis()-start_time;
            }

            // print the matched queries //
            if (SystemGlobals.hasQueries) {            
            	if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE) {
                    if (SystemGlobals.outputTo == SystemGlobals.OUTPUT_TO_ONE_STREAM) {
                	    outQueries.print(xmlFile+": ");
					    xfilterTest.printQueryResults(outQueries);
                    }
                    else {
                        String outputFile = "output"+j+".txt";
                        try {
                            PrintWriter outResults = new PrintWriter(new FileOutputStream(outputFile));
                            xfilterTest.printQueryResults(outResults);
                            outResults.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            	}
				noResults[j-1] = xfilterTest.getNoMatchedQueries();
            }
            
            // clean up after each message processing
            xfilterTest.clear();
        }
        System.out.println();

		/////////////////////////////////////////////////
		///  output profiling results ///
		/////////////////////////////////////////////////

        if (SystemGlobals.profiling) {

            Profiler.takeAverage(DOCS);
            //NFAExecution.takeAverage(DOCS);
            try {
                PrintWriter outProfile = new PrintWriter(new FileOutputStream("profile.txt", true));
                outProfile.println("----- queries -----");
                outProfile.println("no. of queries = "+qNum);
                outProfile.println("no. of distinct paths = "+xfilterTest.getNoDistinctPaths());
                outProfile.println("nfa size = "+xfilterTest.getNoStates());

                outProfile.println("----- execution -----");
                Profiler.report(outProfile);

                outProfile.close();
            }
            catch ( IOException ioe ) {
                ioe.printStackTrace();
            }
        }

        /////////////////////////////////////////////////
        ///  output filtering performance and results ///
        /////////////////////////////////////////////////

        int BURNIN;
        if (DOCS <= SystemGlobals.BURNIN)
            BURNIN = 0;
        else
            BURNIN = SystemGlobals.BURNIN;

        // for performance //
        if (SystemGlobals.algNo == 0) {
            outPerformance.println("CPU TIME");
            outPerformance.println("algorithms\tdocs...\tavg");
        }
        outPerformance.println("alg "+SystemGlobals.algNo+":");

        if (SystemGlobals.preparsing) {
            outPerformance.println("-- parsing cost --");
            for (int j=0; j<DOCS; j++) {
                outPerformance.print("\t"+cpuTime2[j]);
                if (j<BURNIN)
                    continue;
                if (j==BURNIN)  //k=1...ALGORITHMS
                    cpuTime2[DOCS]=cpuTime2[j];
                else
                    cpuTime2[DOCS]+=cpuTime2[j];
            }
            outPerformance.println();
            //cpuTime2[DOCS]/=(DOCS-BURNIN);
            outPerformance.println("Avg:\t"+(cpuTime2[DOCS]/(DOCS-BURNIN)));
        }

        if (SystemGlobals.preparsing)
            outPerformance.println("-- filtering cost --");
        else
            outPerformance.println("-- parsing + filtering cost --");
        for (int j=0; j<DOCS; j++) {
            outPerformance.print("\t"+cpuTime[j]);
            if (j<BURNIN)
                continue;
            if (j==BURNIN)  //k=1...ALGORITHMS
                cpuTime[DOCS]=cpuTime[j];
            else
                cpuTime[DOCS]+=cpuTime[j];
        }
        outPerformance.println();
        //cpuTime[DOCS]/=(DOCS-BURNIN);
        outPerformance.println("Avg:\t"+(cpuTime[DOCS]/(DOCS-BURNIN)));

        // for results //
        if (SystemGlobals.outputLevel != SystemGlobals.OUTPUT_NONE
            && SystemGlobals.algNo == 0) {
            outQueries.println("\nNo. of results:");
            int numMatchedDocs = 0;
            for (int j=0; j<DOCS; j++) {
                outQueries.print("\t"+noResults[j]);
                if (j == 0)
                    noResults[DOCS] = noResults[j];
                else
                    noResults[DOCS] += noResults[j];
                if (noResults[j] > 0)
                    numMatchedDocs++;
            }
            outQueries.println();
            outQueries.println("\nAvg number of results:\t"+(noResults[DOCS]/(double)DOCS));
            outQueries.println("Avg query selectivity:\t"+(noResults[DOCS]/(double)DOCS/qNum));
            outQueries.println("Number of matched docs:\t"+numMatchedDocs);
            outQueries.println("Doc match ratio:\t"+(numMatchedDocs/(double)DOCS));
        }

        outQueries.close();
        outPerformance.close();
    }
}
