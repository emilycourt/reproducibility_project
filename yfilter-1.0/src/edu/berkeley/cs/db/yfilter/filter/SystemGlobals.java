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

import java.io.*;

public class SystemGlobals {
    //////////////////////////////
    /// system-wide parameters ///
    //////////////////////////////

	/// domain, i.e., DTD name or schema name ///
	public static String domain;

    /// I. message processing, materialize it or purely streaming ///
    public static boolean preparsing = true;

	/// II. execution options ///

    // 1. NFA execution //
    // -- optimization 1 --
    // reduce the overhead for result collection during path navigation
    public static int NFAExecLevel = 1;
                            // 0 for path navigation only (primarily for performance study),
                            // 1 for processing each accepting state once (if queries allow),
                            // 2 for repeatedly processing the accepting states
                            // low output levels can avoid the overhead of result collection
	//doc id is used to perform the optimization when NFAExecLevel == 1
    public static int internalDocID = 0;    

   	// -- optimization 2 --
    // TODO: in NFA processing, skip a branch if all downstream 
    // accepting states have been visited. (NFAExecLevel<=1)
    public static boolean pruneNFAExec 	= false;

	// 2. predicate evaluation //
	final public static int PREDICATE_SP			= 0;
	final public static int PREDICATE_SP_SORTBY		= 1;
	final public static int PREDICATE_INLINE		= 2;
	final public static int PREDICATE_INLINE_GROUPBY= 3;
	final public static int PREDICATE_HYBRID	 	= 4;
    public static int algNo = PREDICATE_SP;

    // 3. Nested path evaluation //
    public static int evalNestedPathWhen = 0;
                            // 0 for end of document;
                            // 1 for count = 0

    /// III. output levels ///
    final public static int OUTPUT_NONE 	= 0;
	final public static int OUTPUT_BOOLEAN 	= 1;
	final public static int OUTPUT_ONE 		= 2;
	final public static int OUTPUT_ALL 		= 3; 
    public static int outputLevel = OUTPUT_ALL;

    final public static int OUTPUT_TO_ONE_STREAM    = 1;
    final public static int OUTPUT_TO_INDIV_FILE	= 2;
    public static int outputTo = OUTPUT_TO_ONE_STREAM;

    /// IX. query operations ///
    public static boolean onlineDeletes = false;

    /// X. Performance study ///
    public static String warmupDoc  = null;		// JVM warmup
    public static int BURNIN        = 100;		// number of initial docs for JVM warmup
    public static int REPEAT		= 1;

    /////////////////////////////////
    /// setting per execution run ///
    /////////////////////////////////

	/// system profiling or minitoring ///
	public static boolean profiling = false;

    ////////////////////////////////////
    /// statistics per execution run ///
    ////////////////////////////////////

    /// statistics on queries ///
	public static boolean hasQueries = false;
	public static boolean hasNestedPaths = false;
	public static boolean hasPredicates = false;


	////////////////////////////////////////////////////////////////////////
	
	public static boolean hasDTDSourceFile() {
        if (domain == null)
            return false;
		String filename = domain + ".stat";
		File f = new File(filename);
		if (f.exists() && f.isFile())
			return true;
		filename = domain + ".dtd";
		f = new File(filename);
		if (f.exists() && f.isFile())
			return true;
		return false;
	}
	
	/**
	 * this function increments the Doc ID. it returns 
	 * true for a successful operation; or false when 
	 * the Doc ID is re-set. then the doc ids store in 
	 * the accepting states of the NFA need to be cleared.
	 */
	public static boolean incInternalDocID(){
		if (internalDocID == Integer.MAX_VALUE) {
			internalDocID  = 1;
			return false;
		}
		else {		
			internalDocID ++;
			return true;
		}
	}

	/**
     * compatibility between NFAExecLevel and outputLevel
	 * nfa\output		NONE	BOOLEAN	 	ONE		ALL
	 * NFALevel=0		y		n			n		n
	 * NFALevel=1		y		y			y		n
	 * NFALevel=2		y		y			y		y
	 * NOTE: outputLevel overwrites NFAExecLevel when conflicts occur
	 * so set the output level first and then the compatible NFA execution level
	 */
	public static void setOutputLevel(int level) throws Exception {
		if (level < OUTPUT_NONE || level > OUTPUT_ALL) 
			throw new Exception("SystemGlobals::setOutputLevel -- illegal input for outputLevel!");			
		
		outputLevel = level;
		
		if (outputLevel == OUTPUT_ALL)	
			NFAExecLevel = 2;
		else if (outputLevel == OUTPUT_ONE || outputLevel == OUTPUT_BOOLEAN) {
			if (NFAExecLevel < 1)
				NFAExecLevel = 1;
		}
	}
	
	public static void setNFAExecutionLevel(int level) throws Exception {
		if (level < 0 || level > 2) 
			throw new Exception("SystemGlobals::setNFAExecutionLevel -- illegal input for NFAExecLevel!");			
		
		if (outputLevel == OUTPUT_ALL)	
			NFAExecLevel = 2;
		else if (outputLevel == OUTPUT_ONE || outputLevel == OUTPUT_BOOLEAN) {
			if (level > 0)
				NFAExecLevel = level;		
		}
		else
			NFAExecLevel = level;
		if (NFAExecLevel != level)
			System.out.println("NFA Execution level was adjusted to "+NFAExecLevel);
	}

    /**
     * parse the additional options in the command line
     */
    public static void parseCommandLine(String[] argv, int start) {
        if (argv.length > start) {
            for (int i=start; i<argv.length; i++) {
				if (argv[i].startsWith("profil"))
					profiling = true;
                else if (argv[i].startsWith("result")) {
					int level;
					if (argv[i].startsWith("result:none"))
						level = OUTPUT_NONE;
					else if (argv[i].startsWith("result:bool"))
						level = OUTPUT_BOOLEAN;
					else if (argv[i].startsWith("result:one"))
						level = OUTPUT_ONE;
					else if (argv[i].startsWith("result:all"))
						level = OUTPUT_ALL;
                    else
                        level = OUTPUT_BOOLEAN;
                    try {
                        SystemGlobals.setOutputLevel(level);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        System.exit(-1);
                    }
                }
                else if (argv[i].startsWith("output:")) {
                    if (argv[i].startsWith("output:indiv"))
                        outputTo = OUTPUT_TO_INDIV_FILE;
                }
                else if (argv[i].startsWith("schema:")) {
                    SystemGlobals.domain = argv[i].substring(7, argv[i].length());
                }
				else if (argv[i].startsWith("nfa_opt:")) {
					int level = Integer.parseInt(argv[i].substring(8, argv[i].length()));
					try {
						setNFAExecutionLevel(level);
					}
					catch (Exception e){
						e.printStackTrace(System.err);
						System.exit(-1);
					}
				}
                //else if (argv[i].startsWith("alg:"))
                //    algNo = Integer.parseInt(argv[i].substring(4, argv[i].length()));
                else if (argv[i].startsWith("msg_preparse:")) {
                    if (argv[i].charAt(13) == 'n' ||
						argv[i].charAt(13) == 'N' ||
						argv[i].charAt(13) == 'f' ||
                    	argv[i].charAt(13) == 'F')
                        preparsing = false;
					else if (argv[i].charAt(13) == 'y' ||
						argv[i].charAt(13) == 'Y' ||
						argv[i].charAt(13) == 't' ||
						argv[i].charAt(13) == 'T')
						preparsing = true;
                    else {
						SystemGlobals.preparsing = true;
						System.err.println("Message preparsing was adjusted to True!");
                    }
                }
                else if (argv[i].startsWith("warmup:")) {
                    String tmp = argv[i].substring(7, argv[i].length());
                    if (tmp.endsWith(".xml")) {
                        warmupDoc = tmp;
                        BURNIN = 0;
                    }
                    else {
                        try {
                            BURNIN = Integer.parseInt(tmp);
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace(System.err);
                            System.exit(-1);
                        }
                    }
                }
                else if (argv[i].startsWith("repeat:")) {                	
					try {
						REPEAT = Integer.parseInt(argv[i].substring(7, argv[i].length()));
						if (REPEAT < 1)
							REPEAT = 1;
					}
					catch (NumberFormatException e) {
						e.printStackTrace(System.err);
						System.exit(-1);
					}                	
                }
                //else
                //    System.err.println("Unknown option -- ignored!");
            }
        }
    }
    
	/**
	 * Parses the additional options in the command line
	 */
	public static void parseCommandLineOptions(String[] argv, int start) {
		if (argv.length > start) {
			for (int i=start; i<argv.length; i++) {
				if (argv[i].startsWith("--profile"))
					profiling = true;
				else if (argv[i].startsWith("--result=")) {
					int level;
					if (argv[i].substring(argv[i].indexOf('=') + 1).equals("NONE"))
						level = OUTPUT_NONE;
					else if (argv[i].substring(argv[i].indexOf('=') + 1).equals("BOOLEAN"))
						level = OUTPUT_BOOLEAN;
					else if (argv[i].substring(argv[i].indexOf('=') + 1).equals("ONE"))
						level = OUTPUT_ONE;
					else if (argv[i].substring(argv[i].indexOf('=') + 1).equals("ALL"))
						level = OUTPUT_ALL;
					else
						level = OUTPUT_BOOLEAN;
					try {
						SystemGlobals.setOutputLevel(level);
					} catch (Exception e) {
						e.printStackTrace(System.err);
						System.exit(-1);
					}
				}
				else if (argv[i].startsWith("--output=")) {
					if (argv[i].substring(argv[i].indexOf('=') + 1).equals("indiv"))
						outputTo = OUTPUT_TO_INDIV_FILE;
				}
				else if (argv[i].startsWith("--schema=")) {
					SystemGlobals.domain = argv[i].substring(argv[i].indexOf('=') + 1);
				}
				else if (argv[i].startsWith("--nfa_opt=")) {
					int level = Integer.parseInt(argv[i].substring(argv[i].indexOf('=') + 1));
					try {
						setNFAExecutionLevel(level);
					}
					catch (Exception e){
						e.printStackTrace(System.err);
						System.exit(-1);
					}
				}
				//else if (argv[i].startsWith("alg:"))
				//    algNo = Integer.parseInt(argv[i].substring(4, argv[i].length()));
				else if (argv[i].startsWith("--msg_preparse=")) {
					if (argv[i].substring(argv[i].indexOf('=') + 1).equals("FALSE"))
						preparsing = false;
					else if (argv[i].substring(argv[i].indexOf('=') + 1).equals("TRUE"))
						preparsing = true;
					else {
						SystemGlobals.preparsing = true;
						System.err.println("Message preparsing was adjusted to True!");
					}
				}
				else if (argv[i].startsWith("--warmup=")) {
					String tmp = argv[i].substring(argv[i].indexOf('=') + 1);
					if (tmp.endsWith(".xml")) {
						warmupDoc = tmp;
						BURNIN = 0;
					}
					else {
						try {
							BURNIN = Integer.parseInt(tmp);
						}
						catch (NumberFormatException e) {
							e.printStackTrace(System.err);
							System.exit(-1);
						}
					}
				}
				else if (argv[i].startsWith("--repeat=")) {                	
					try {
						REPEAT = Integer.parseInt(argv[i].substring(argv[i].indexOf('=') + 1));
						if (REPEAT < 1)
							REPEAT = 1;
					}
					catch (NumberFormatException e) {
						e.printStackTrace(System.err);
						System.exit(-1);
					}                	
				}
				//else
				//	System.err.println("Unknown option -- Ignored!");
			}
		}
	}
    
}