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

public class NormalMarkableComparator implements java.util.Comparator
{
    /** Creates new NormalMarkableComparator. This orders Markables with smaller DiscPos before ones with greater DiscPos. If DiscPos
        is identical, shorter Markables are ordered _after_ longer ones. */
    public NormalMarkableComparator(int deprecated) 
    {
        super();
    }
    
    public int compare(Object _markable1, Object _markable2)
    {
        Markable markable1 = (Markable) _markable1;
        Markable markable2 = (Markable) _markable2;

        int start1 = markable1.getLeftmostDiscoursePosition();
        int start2 = markable2.getLeftmostDiscoursePosition();
        
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
            if (markable1.size < markable2.size)
            {
                // Markable1 is shorter, so we want it _after_ Markable2, so say it's bigger
                return 1;
            }
            else if (markable2.size < markable1.size)
            {
                // Markable1 is longer, so we want it _before_ Markable2, so say it's smaller
                return -1;
            }
            else
            {
                // Both Markables are the same size
                return 0;
            }
        }
    }    
}

