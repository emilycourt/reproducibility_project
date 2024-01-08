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

import java.util.*;
import java.io.*;

public class DocumentModifier {

	private static void removeDTDDecl(BufferedReader in, PrintWriter out) 
		throws Exception {
		String line = null;
		while ((line = in.readLine()) != null) {
			if (! line.startsWith("<!DOCTYPE"))
				out.println(line);
		}
	}
	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("DocumentModifier::main -- Error !" +
					"\nNeed inputs: from dir, to dir, operation!");
			System.exit(-1);
		}		
			
		String from_dir = args[0];
		String to_dir = args[1];
		String operation = args[2];
		int num_docs = -1;
		if (args.length > 3)
			num_docs = Integer.parseInt(args[3]);
		if (num_docs == -1)
			num_docs = Integer.MAX_VALUE;
		
		ArrayList filenames = DocumentReader.getFileNames(from_dir);
		int size = filenames.size();
		for (int i=0, count=0; i<size && count<num_docs; i++, count++) {
			String filename = (String)filenames.get(i);
			System.out.println("Processing "+filename+" ...");
			
			String filename2 = to_dir + "/" + 
				DocumentReader.getLocalFileName(filename);
			BufferedReader in;
			PrintWriter out;
			try {
				in = new BufferedReader(new FileReader(filename));
				out = new PrintWriter(new FileOutputStream(filename2));
				
				if (operation.equals("rmDTDDecl"))
					removeDTDDecl(in, out);
				
				in.close();
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
