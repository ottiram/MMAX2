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

import java.util.ArrayList;
public class MMAX2DiscourseElementSequence
{
    MMAX2DiscourseElement[] content=null;
    
    /** Creates a new instance of MMAX2DiscourseElementSequence */
    public MMAX2DiscourseElementSequence(MMAX2DiscourseElement[] _content) 
    {
        content = _content;
    } 
    
    public final MMAX2DiscourseElement[] getContent()
    {
        return content;
    }
    
    public final int getLength()
    {
        return content.length;
    }
    
    public final String toString(String attrib)
    {
        String result = "";
        for (int z=0;z<content.length;z++)
        {
            if (content[z].getAttributeValue("ignore","+++").equals("true"))
            {
                result = result+"*"+content[z].toString()+"* ";
            }
            else
            {
                result = result+content[z].toString()+" ";
            }
        }
        if (attrib.equals("")==false)
        {
            result = result + "[";
            for (int z=0;z<content.length;z++)
            {
                result = result+content[z].getAttributeValue(attrib,"+++")+" ";
            }            
            result = result.trim() + "]";
        }
        return result;
    }
    
    public final boolean equals(Object otherObject)
    {
        boolean result = false;
        MMAX2DiscourseElementSequence otherSequence = (MMAX2DiscourseElementSequence)otherObject;
        if (otherSequence.getLength() == getLength())
        {
            // Both can be equal only if they have the same length
            if (this.getContent()[0].getID().equals(otherSequence.getContent()[0].getID()))
            {
                result = true;
            }
        }
        return result;
    }
    
    public final boolean containsDiscourseElement(MMAX2DiscourseElement element)
    {
        boolean result = false;
        for (int z=0;z<content.length;z++)
        {
            if (content[z].getID().equals(element.getID()))
            {
                result = true;
                break;
            }
        }        
        return result;
    }
    
    public final double getSumOfNumericalAttribute(String attribute)
    {
        double result = 0.0;
        for (int z=0;z<content.length;z++)
        {
            result = result + new Double(content[z].getAttributeValue(attribute,"0.0")).doubleValue();
        }
        return (double)result;
    }
    
    public final void trim(String attribute, String value)
    {
        trimLeft(attribute,value);
        trimRight(attribute,value);
    }
    
    public final void trimLeft(String attribute, String value)
    {
        ArrayList temp = new ArrayList();
        boolean contentHasStarted = false;
        // Iterate over entire content
        for (int z=0;z<content.length;z++)
        {
            // Get current DE
            MMAX2DiscourseElement currentDE = content[z];
            if (currentDE.getAttributeValue(attribute, "+++").equalsIgnoreCase(value))
            {
                // The current DE should be trimmed
                if (contentHasStarted==false)
                {
                    // We are still in the left area to trim
                }
                else
                {
                    // Content has started already, so keep everything from here on
                    temp.add(currentDE);
                }
            }
            else
            {
                // The current DE is not to be trimmed
                // So keep, and signal that content has started
                temp.add(currentDE);
                contentHasStarted = true;
            }
        }
        content = null;
        content = (MMAX2DiscourseElement[]) temp.toArray(new MMAX2DiscourseElement[0]);
    }

    public final void trimRight(String attribute, String value)
    {
        ArrayList temp = new ArrayList();
        boolean contentHasStarted = false;
        // Iterate over entire content
        for (int z=content.length-1;z>=0;z--)
        {
            // Get current DE
            MMAX2DiscourseElement currentDE = content[z];
            if (currentDE.getAttributeValue(attribute, "+++").equalsIgnoreCase(value))
            {
                // The current DE should be trimmed
                if (contentHasStarted==false)
                {
                    // We are still in the left area to trim
                }
                else
                {
                    // Content has started already, so keep everything from here on
                    temp.add(0,currentDE);
                }
            }
            else
            {
                // The current DE is not to be trimmed
                // So keep, and signal that content has started
                temp.add(0,currentDE);
                contentHasStarted = true;
            }
        }
        content = null;
        content = (MMAX2DiscourseElement[]) temp.toArray(new MMAX2DiscourseElement[0]);
    }
    
    
    public final MMAX2DiscourseElement getInitialDiscourseElement()
    {
        return content[0];
    }

    public final MMAX2DiscourseElement getFinalDiscourseElement()
    {
        return content[content.length-1];
    }
    
    
    public final ArrayList tagMissingDiscourseElements(MMAX2DiscourseElementSequence shorterSequence, String attribute, String value)
    {
        ArrayList removed = new ArrayList();
        // Iterate over all DiscourseElements in this sequence
        for (int t=0;t<content.length;t++)
        {
            // If the shorter sequence does not contain current one, tag current one
            if (shorterSequence.containsDiscourseElement(content[t])==false)
            {
                content[t].setAttributeValue(attribute,value,null);
                removed.add(new String(content[t].getID()));
            }
        }
        return removed;
    }
    
    public final MMAX2DiscourseElementSequence getSubSequence(int start, int len)
    {
        ArrayList temp = new ArrayList();
        for (int z=start;z<start+len;z++)
        {
            temp.add(content[z]);
        }
        return new MMAX2DiscourseElementSequence((MMAX2DiscourseElement[])temp.toArray(new MMAX2DiscourseElement[0]));
    }
}
