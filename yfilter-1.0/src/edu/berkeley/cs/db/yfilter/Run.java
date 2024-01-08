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
package edu.berkeley.cs.db.yfilter;

import edu.berkeley.cs.db.yfilter.filter.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;
import edu.berkeley.cs.db.yfilterplus.utility.*;
import edu.berkeley.cs.db.yfilterplus.xmltree.*;

import java.util.*;

public class Run {
	public static void main(String[] argv) {

		/// 1. read system parameters ///

		// required parameters //
		if (argv.length < 2) {
			System.out.println("Not enough arguments: doc source (dir or file), query-file");
			return;
		}
		String docSource = argv[0]; // the file or the directory of xml files
		String queryFile = argv[1];

		/// 2. add queries and build index	///
		EXfilterBasic filterTest = new EXfilterBasic();

		QueryParser qp = new XPQueryParser(queryFile);
		Query query;
		int qNum = 0;
		while ((query = qp.readNextQuery()) != null) {
			filterTest.addQuery(query);
			qNum++;
		}
		if (qNum < 2)
			System.out.println("Read "+qNum+" query!");
		else
			System.out.println("Read "+qNum+" queries!");
				
		/// 3. start processing an xml document	///

		// read those file names in //
		ArrayList filenames = DocumentReader.getFileNames(docSource);
		int DOCS = filenames.size();

		// start the processing //
		String xmlFile;		
		for (int j = 1; j <= DOCS; j++) {

			xmlFile = (String) filenames.get(j - 1);
			System.out.println("\nProcesssing " + xmlFile + "... ");

			XMLTree tree = new XMLTree(xmlFile);
			filterTest.setEventSequence(tree.getEvents());
			filterTest.startParsing();
			//xfilterTest.startParsing(xmlFile);

			// print the matched queries //
			if (SystemGlobals.hasQueries) {				
				filterTest.printQueryResults(System.out);
			}

			// clean up after each message processing
			filterTest.clear();
		}
		System.out.println();
	}
}