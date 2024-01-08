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

package edu.berkeley.cs.db.yfilter.operators;

import edu.berkeley.cs.db.yfilterplus.xmltree.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.*;
import edu.berkeley.cs.db.yfilterplus.queryparser.xpathparser.SimplePredicate;

import java.util.*;

public class PredicateEvaluation {

    public PredicateEvaluation() {}

    ///////////// predicate evaluation /////////////////////////

    /**
     * evaluate a single predicate with an element
     **/
    public static boolean evaluatePredicate(Predicate p, ParsingContext context) 
    {
        if (p.getType() == 'a') {
            /// attributes ///
            String attrName = p.getAttrName();
            
            String realValue = context.getAttributeValue(attrName);
            if (realValue == null)
            	// the queried attributes does not occur in the element
                return false;

			String predicateValue = p.getStringValue();            
            if (predicateValue == null)
            	// the predicate only wants to check the
            	// existence of the attribute
                return true;
                        
            if (!realValue.equals(predicateValue))
                return false;

        }
        else if (p.getType() == 'p') {
            /// positions ///
            char op = p.getOperator();
            int targetValue = p.getValue();
            int position = context.getPosition();         
            if (op == SimplePredicate.OPERATOR_EQ) {
                if (position != targetValue)
                    return false;                
            }
            else if (op == SimplePredicate.OPERATOR_NE) {
                if (position == targetValue)
                    return false;
            }
            else if (op == SimplePredicate.OPERATOR_LT) {
                if (position >= targetValue)
                    return false;
            }
            else if (op == SimplePredicate.OPERATOR_LE) {
                if (position > targetValue)
                    return false;
            }
            else if (op == SimplePredicate.OPERATOR_GT) {
                if (position <= targetValue)
                    return false;
            }
            else if (op == SimplePredicate.OPERATOR_GE) {
                if (position < targetValue)
                    return false;
            }
        }
        else{
            /// element data ///   
			if (!context.hasElementData())
				// there is no data for this element
				return false;

			String predicateValue = p.getStringValue();
			if (predicateValue == null)
				// this predicate only wants to check the existence of data
				return true;			

			if (!context.textDataEquals(predicateValue))
				return false;

            /*  //-- does not work for mixed content --      
            String realValue = context.getElementData();
            if (realValue == null)  
            	// there is no data for this element
                return false;
            
			String predicateValue = p.getStringValue();            
            if (predicateValue == null) {
                // this predicate only wants to check the existence of data
                return true;
            }
						
            if (!realValue.equals(predicateValue))
                return false;
            */
        }
        return true;
    }

    /**
     * evaluate all predicates on a path of this query 
     **/
    public static boolean evaluatePath(Predicate[] predicates,
                                       ArrayList queryPathContext) {
        // predicates on this path are sorted
        // by literal level, not doc level

        Predicate p;
        int level;
        ParsingContext context;
        int size = predicates.length;
        int i;
        // sequential scan of the whole list. improvement can be
        // done by sorting predicates by predicate selectivity
        for (i=0; i<size; i++) {
            p = predicates[i];
            level = p.getLevel();
            context = (ParsingContext)queryPathContext.get(level);
			boolean result = evaluatePredicate(p, context);
			if (result == false)
				break;
        }
        
        if (i >= size) {            
            return true;
        }
        return false;
    } 
}
