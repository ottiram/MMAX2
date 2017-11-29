/*
 * Copyright 2007 Mark-Christoph Müller
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package org.eml.MMAX2.annotation.markables;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTuple;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTupleElement;

public class QueryResultTupleComparator implements java.util.Comparator
{
    int relevantPosition = -1;
    
    /** Creates new QueryResultTupleComparator. This orders MMAX2QueryResultTuples according to the disc pos of the
        Markables it contains. It compares the disc pos at position relevantPosition, and orders tuples with
        Markables with smaller initial disc pos before ones with Markables with greater ones. If two tuples have at 
        the relevant position Markables with the same initial disc pos, the tuple with the shorter Markable is ordered
        before the one with the longer one. If both Markables have the same length, it tries to sort recursively on
        relevantPosition+1, if possible. 
     */    
    public QueryResultTupleComparator(int _relevantPosition) 
    {
        super();
        relevantPosition = _relevantPosition;
    }
    
    public int compare(Object _tupel1, Object _tupel2)
    {        
        // Cast input objects to individual query result tuple elements (i.e. markables or discourse elements)
        // Use elements at index relevantPosition, which is always 0 at the beginning. 
        MMAX2QueryResultTupleElement e1 = ((MMAX2QueryResultTuple) _tupel1).getValueAt(relevantPosition);
        MMAX2QueryResultTupleElement e2 = ((MMAX2QueryResultTuple) _tupel2).getValueAt(relevantPosition);

        // Get initial disc pos's from both elements
        int start1 = e1.getLeftmostDiscoursePosition();
        int start2 = e2.getLeftmostDiscoursePosition();
        
        if (start1 < start2)
        {
            return -1;
        }
        else if (start1 > start2)
        {
            return 1;
        }
        else
        {
            // Both have the same initial disc pos
            if (e1.getSize() < e2.getSize())
            {
                // e1 is shorter, so we want it _before_ e2, so say it's smaller
                return -1;
            }
            else if (e2.getSize() < e1.getSize())
            {
                // e1 is longer, so we want it _after_ e2, so say it's bigger
                return 1;
            }
            else
            {
                // Both elements are the same size and the same disc pos (i.e. identical)
                // NEW 21st February 2005: Use next column, if available
                if (((MMAX2QueryResultTuple) _tupel1).getWidth() > relevantPosition+1 && 
                    ((MMAX2QueryResultTuple) _tupel2).getWidth() > relevantPosition+1)
                {
                    // Create new comparator for next column
                    QueryResultTupleComparator compi = new QueryResultTupleComparator(relevantPosition+1);                    
                    return compi.compare (_tupel1,_tupel2);
                }
                else
                {
                    // Complete identity
                    return 0;
                }
            }
        }
    }    
}

