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
import java.util.HashMap;

import org.eml.MMAX2.api.DiscourseElementAPI;
/**
 *
 * @author  mueller
 */
public class MMAX2DiscourseElement implements DiscourseElementAPI
{
    String string;
    String id;
    int discPos;
    HashMap attributes;
    
    /** Creates a new instance of MMAX2DiscourseElement */
    public MMAX2DiscourseElement(String _string, String _id, int _discPos, HashMap _attributes) 
    {
        string = _string;
        id = _id;
        discPos = _discPos;
        attributes = _attributes;
    }
    
    public final String toString()
    {
        return string;
    }
    
    public final String getID()
    {
        return id;
    }
    
    public final int getDiscoursePosition()
    {
        return discPos;
    }
    
    public final String getAttributeValue(String name, String valIfNone)
    {
        String result = (String)attributes.get(name);
        if (result==null)
        {
            result = valIfNone;
        }
        return result;
    }
    
    public final void setAttributeValue(String name, String value, MMAX2Discourse disc)
    {
        attributes.put(name,value);
        if (disc!=null)
        {
            ((org.w3c.dom.Element)disc.getDiscourseElementNode(this.id)).setAttribute(name,value);
        }        
    }
    
    public final HashMap getAttributes()
    {
        return attributes;
    }
}
