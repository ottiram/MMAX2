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

package org.eml.MMAX2.discourse;

public class MMAX2DiscourseElementSequenceComparator implements java.util.Comparator
{
    /** Creates new MMAX2DiscourseElementSequenceComparator. This orders MMAX2DiscourseElementSequences with smaller DiscPos before ones with greater DiscPos. If DiscPos
        is identical, shorter sequences are ordered _before_ longer ones. */
    public MMAX2DiscourseElementSequenceComparator() 
    {
        super();
    }
    
    public int compare(Object _sequence1, Object _sequence2)
    {
        MMAX2DiscourseElementSequence sequence1 = (MMAX2DiscourseElementSequence) _sequence1;
        MMAX2DiscourseElementSequence sequence2 = (MMAX2DiscourseElementSequence) _sequence2;

        int start1 = sequence1.getContent()[0].getDiscoursePosition();
        int start2 = sequence2.getContent()[0].getDiscoursePosition();
        
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
            if (sequence1.getLength() < sequence2.getLength())
            {
                // sequence1 is shorter, so we want it _before_ sequence2, so say it's smaller
                return -1;
            }
            else if (sequence2.getLength() < sequence1.getLength())
            {
                // sequence1 is longer, so we want it _after_ sequence2, so say it's bigger
                return 1;
            }
            else
            {
                // Both sequenceces are the same size
                return 0;
            }
        }
    }    
}

