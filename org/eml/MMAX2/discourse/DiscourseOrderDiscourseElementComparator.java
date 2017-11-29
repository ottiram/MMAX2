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

public class DiscourseOrderDiscourseElementComparator implements java.util.Comparator
{
    public DiscourseOrderDiscourseElementComparator() 
    {
        super();
    }
    
    public int compare(Object _e1, Object _e2)
    {
        MMAX2DiscourseElement e1 = (MMAX2DiscourseElement) _e1;
        MMAX2DiscourseElement e2 = (MMAX2DiscourseElement) _e2;

        int start1 = e1.getDiscoursePosition();
        int start2 = e2.getDiscoursePosition();
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
            return 0;
        }
    }    
}

