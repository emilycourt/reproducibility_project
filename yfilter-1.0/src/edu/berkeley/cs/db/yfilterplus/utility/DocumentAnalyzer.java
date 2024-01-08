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
package edu.berkeley.cs.db.yfilterplus.utility;

import edu.berkeley.cs.db.yfilterplus.xmltree.XMLTree;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;

public class DocumentAnalyzer {
    final String m_xmlDocNamePrefix = "outFile";
    String m_xmlDir;
    String m_currentXMLdoc = null;

    /// For experiments ///
    long[] m_performance;   	// for parsing performance
    int[] m_docLength;		// for doc length in terms of elements
    int[] m_docDepth;		// for max. doc depth
    double m_totalDepth;
    // for average doc depth

    public DocumentAnalyzer(String xmlDir) {
        m_xmlDir = xmlDir;
    }

    public void analyzeXMLDocs(int noDocs) {
        //int noDocs = endDocIndex - startDocIndex + 1;
        m_docLength = new int[noDocs+1];
        m_docLength[noDocs] = 0;
        m_docDepth = new int[noDocs+1];
        m_docDepth[noDocs] = 0;
        m_totalDepth = 0;
        double pathDepth;

        File dir = new File(m_xmlDir);
        String[] fileList = dir.list();
        int len = fileList.length;
        if (len == 0) {
            System.out.println("Empty directory: " + m_xmlDir);
            System.exit(0);
        }
        Arrays.sort( fileList );
        ArrayList filenames = new ArrayList();
        for (int i = 0, docCount = 0; i < len && docCount < noDocs; i++)
            if (fileList[i].endsWith(".xml")) {
                String fileName = m_xmlDir + '/' + fileList[i];
                filenames.add(fileName);
                docCount++;
            }
        if (filenames.size() < noDocs) {
            System.out.println("Cannot find enough XML files in " + m_xmlDir + "!");
            System.exit(0);
        }

        String filename = null;
        XMLTree xmltree = null;
        for (int i=1; i<=noDocs; i++) {
            //filename = m_xmlDir + "/"+ m_xmlDocNamePrefix + i + ".xml";
            filename = (String)filenames.get(i-1);
            m_currentXMLdoc = DocumentReader.readDoctoString(filename);
            xmltree =  new XMLTree(new StringReader(m_currentXMLdoc));
            System.out.print("\nAnalyzing document " + i + "...\t");
			pathDepth = xmltree.analyzeXMLTree(m_docLength, m_docDepth, i-1);
            m_totalDepth += pathDepth;
            System.out.println(m_docLength[i-1]+" "+m_docDepth[i-1]+" "+pathDepth);
            //m_docLength[i] = xmltree.getLength();
            //m_docDepth[i] = xmltree.getDepth();

            m_docLength[noDocs] += m_docLength[i-1];
            m_docDepth[noDocs] += m_docDepth[i-1];
        }

        m_docLength[noDocs] = (int)Math.floor(m_docLength[noDocs]/((double)noDocs));
        m_docDepth[noDocs] = (int)Math.floor(m_docDepth[noDocs]/((double)noDocs));
        System.out.println("Avg length: " + m_docLength[noDocs]);
        System.out.println("Avg max. depth: " + m_docDepth[noDocs]);
        System.out.println("Avg path depth: " + (m_totalDepth/noDocs));
    }

    public static void main(String[] argv) {
        /// read in arguments ///
        if (argv.length < 2) {
            System.out.println("Not enough arguments: ");
            System.out.println("docs dir, # docs");
            return;
        }        
        String xmlDir = argv[0];
        int noDocs = Integer.parseInt(argv[1]);

        DocumentAnalyzer da = new DocumentAnalyzer(xmlDir);
        da.analyzeXMLDocs(noDocs);
    }
}