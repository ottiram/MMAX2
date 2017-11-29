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

package org.eml.MMAX2.annotation.query;
import java.util.HashMap;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.api.QueryResultTupleElementAPI;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
public class MMAX2QueryResultTupleElement implements QueryResultTupleElementAPI
{        
    private Markable markable = null;
    private MMAX2DiscourseElement discourseElement = null;
    
    /** Creates a new instance of MMAX2QueryResultTupleElement */
    public MMAX2QueryResultTupleElement(Object object)
    {
        if (object instanceof Markable)
        {
            markable = (Markable)object;
        }
        else if (object instanceof MMAX2DiscourseElement)
        {
            discourseElement = (MMAX2DiscourseElement) object;
        }
        else
        {
            System.err.println("Error: Unsupported MMAX2QueryResultTupleElement: "+object);            
        }
    }
    
    /** This method returns an array of all MMAX2DiscourseElement objects in the span of this .*/
    public final MMAX2DiscourseElement[] getDiscourseElements(MMAX2Discourse discourse)
    {
        MMAX2DiscourseElement[] result = null;
        if (markable != null)
        {
            result = discourse.getDiscourseElements(markable);
        }
        else
        {
            result = new MMAX2DiscourseElement[1];
            result[0] = discourseElement;
        }
        return result;
    }
    
    public final String getLevelName()
    {
        if (markable != null)
        {
            return markable.getMarkableLevelName();
        }
        else
        {
            return "basedata";
        }
    }
    
    public final boolean isMarkable()
    {
        return markable!=null;
    }
    
    public final Markable getMarkable()
    {
        return markable;
    }

    public final boolean isDiscourseElement()
    {
        return discourseElement!=null;
    }
    
    
    public final MMAX2DiscourseElement getDiscourseElement()
    {
        return discourseElement;
    }
    
    public final String getAttributeValue(String attributeName, String valIfUndefined)
    {
        if (markable != null)
        {
            return markable.getAttributeValue(attributeName,valIfUndefined);
        }
        else
        {
            return discourseElement.getAttributeValue(attributeName,valIfUndefined);
        }
    }
    
    public boolean equals (Object other)
    {
        MMAX2QueryResultTupleElement otherElement = (MMAX2QueryResultTupleElement) other;
        if (isMarkable() && otherElement.isMarkable())
        {
            return getMarkable()==otherElement.getMarkable();
        }
        else if (isMarkable()==false && otherElement.isMarkable()==false)
        {
            return getID().equalsIgnoreCase(otherElement.getID());
        }
        else
        {
            return false;
        }        
    }
    
    public final int getLeftmostDiscoursePosition()
    {
        if (isMarkable())
        {
            return markable.getLeftmostDiscoursePosition();
        }
        else
        {
            return discourseElement.getDiscoursePosition();
        }
    }
    
    public final int getRightmostDiscoursePosition()
    {
        if (isMarkable())
        {
            return markable.getRightmostDiscoursePosition();
        }
        else
        {
            return discourseElement.getDiscoursePosition();
        }
    }
 
    public final HashMap getAttributes()
    {
        if (isMarkable())
        {
            return markable.getAttributes();
        }
        else
        {
            return discourseElement.getAttributes();
        }
    }

    public final int getSize()
    {
        if (isMarkable())
        {
            return markable.getSize();
        }
        else
        {
            // Basedata is always size 1
            return 1;
        }
    }
        
    public final boolean isDiscontinuous()
    {
        if (isMarkable())
        {
            return markable.isDiscontinuous();
        }
        else
        {
            // Basedata is never discontinuous
            return false;
        }
    }
    
    public final String[][] getFragments()
    {
        String[][] result = new String[1][1];        
        if (isMarkable())
        {
            result = markable.getFragments();
        }
        else
        {
            result[0][0] = discourseElement.getID();
        }
        return result;
    }

    public final String getID()
    {
        if (isMarkable())
        {
            return markable.getID();
        }
        else
        {
            return discourseElement.getID();
        }
    }
    

    public final String toTrimmedString(int maxWidth)
    {
        if (isMarkable())
        {
            return markable.toTrimmedString(maxWidth);
        }
        else
        {
            return discourseElement.toString();
        }
    }

    
    public final String toString()
    {
        if (isMarkable())
        {
            return markable.toString();
        }
        else
        {
            return discourseElement.toString();
        }
    }
}
