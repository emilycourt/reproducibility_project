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

package edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser;

import java.io.*;

public class XPathParser {

    protected parser p;
    
    public void reportException (Exception e) {
	StringBuffer err = ((Lexer) p.getScanner()).getInputBuffer();
	if (err != null)
	    System.err.println("Read input: " + err);
	e.printStackTrace();
	
    }

    public XPathParser (StringReader in) {
	try {
	    p = new parser(in);
	} catch(Exception e) {
	    reportException(e);
	}
    }

    public XPathParser (String query) {
	StringReader in;
	try {
	    in = new StringReader(query);
	    p = new parser(in);
	} catch(Exception e) {
	    reportException(e);
	}
    }

    public java_cup.runtime.Symbol parse () {
	java_cup.runtime.Symbol s = null;

	try {
	    s = p.parse();
	} catch(Exception e) {
	    reportException(e);
	} finally {
	    return s;
	}
    }

    public parser getParser () {
	return p;
    }

    public void setDebug (boolean flag) {
	p.setDebug(flag);
	((Lexer)p.getScanner()).setDebug(flag);
    }
}
