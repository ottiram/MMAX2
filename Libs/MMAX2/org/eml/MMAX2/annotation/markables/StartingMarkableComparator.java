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

public class StartingMarkableComparator implements java.util.Comparator
{
    /** Creates new StartingMarkableComparator */
    public StartingMarkableComparator() 
    {
        super();
    }
    
    /** This method is used for sorting Markables for display during Markable retrieval in the MarkableLayer.getStartedMarkables
        method. Sorting can be done by simply comparing the length of the markables, because when this method is called we know
        all Markables to have the same initial DiscourseElement. */
    public int compare(Object _markable1, Object _markable2)
    {
        Markable markable1 = (Markable) _markable1;
        Markable markable2 = (Markable) _markable2;
        
        if (markable1.size < markable2.size)
        {
            // Markable1 is shorter, so we want to open it _after_ Markable2, so say it's greater
            return 1;
        }
        else if (markable2.size < markable1.size)
        {
            // Markable1 is longer, so we want to open it _before_ Markable2, so say it's less
            return -1;
        }
        else
        {
            // Both Markables are the same size
            return 0;
        }        
    }    
}

