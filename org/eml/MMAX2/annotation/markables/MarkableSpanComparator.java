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

public class MarkableSpanComparator implements java.util.Comparator
{
    
    /** Creates a new instance of MarkableSpanComparator */
    public MarkableSpanComparator() 
    {
        
    }
    
    public int compare(Object o1, Object o2)
    {
        String span1 = (String) o1;
        String span2 = (String) o2;
        
        
        if (span1.indexOf(",")!=-1)
        {
            span1=span1.substring(0,span1.indexOf(","));
        }

        if (span2.indexOf(",")!=-1)
        {
            span2=span2.substring(0,span2.indexOf(","));
        }
        
        if (span1.indexOf(",")==-1 && span2.indexOf(",")==-1)
        {
            // None of the spans is discontinuous
            // Make sure both have the same explicit format
            if (span1.indexOf("..")==-1)
            {
                span1=span1+".."+span1;
            }
            if (span2.indexOf("..")==-1)
            {
                span2=span2+".."+span2;
            }
            
            
//            int span1Start = Integer.parseInt(span1.substring(5,span1.indexOf("..")));
//            int span1End = Integer.parseInt(span1.substring(span1.indexOf("..")+7));
//            int span2Start = Integer.parseInt(span2.substring(5,span2.indexOf("..")));
//            int span2End = Integer.parseInt(span2.substring(span2.indexOf("..")+7));

            float span1Start = Float.parseFloat(span1.substring(5,span1.indexOf("..")));
            float span1End = Float.parseFloat(span1.substring(span1.indexOf("..")+7));
            float span2Start = Float.parseFloat(span2.substring(5,span2.indexOf("..")));
            float span2End = Float.parseFloat(span2.substring(span2.indexOf("..")+7));
            
            
            if (span1Start < span2Start)
            {
                return -1;
            }
            else if (span2Start < span1Start)
            {
                return 1;
            }
            else
            {
                // Both have the same start
                if (span1End < span2End)
                {
                    return -1;
                }
                else if (span2End < span1End)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }            
        }
        else
        {
            // At least one of the spans is discontinuous
            
            return 0;
        }
    }
    
}
