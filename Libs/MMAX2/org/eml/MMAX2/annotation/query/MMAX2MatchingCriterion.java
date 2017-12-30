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

import java.util.ArrayList;

// To be passed to matchesAll or matchesAny. A MMAX2MatchingCriterion contains one MMAX2AtomicQueryTerm.
public class MMAX2MatchingCriterion 
{
    // Name of the attribute this query accesses. There is only one.
    String attributeName = "";
    // Type of the attribute this query accesses. There is only one.
    int attributeType;
    // List of (mutually exclusive) values to match, 0 to n. For regExp queries, only one value is possible
    ArrayList values = null;

    boolean negated = false;
    boolean regExpMatch = false;
    boolean matchAll = false;
    
    /** Creates new MMAX2MatchingCriterion */
    public MMAX2MatchingCriterion(String _attributeName, int _attributeType, boolean _negated, boolean _regExpMatch)
    {
        attributeName = _attributeName;
        attributeType = _attributeType;
        values = new ArrayList();
        negated = _negated;
        regExpMatch = _regExpMatch;
    }

    public void setMatchAll()
    {
        matchAll = true;
    }
    
    public boolean isMatchAll()
    {
        return matchAll;
    }
    
    public final void addValue(String _value)
    {
        values.add(_value);
    }
    
    public final int getSize()
    {
        return values.size();
    }
    
    public final String getAttributeName()
    {
        return attributeName;
    }
    
    public final String getValue(int z)
    {
        return (String) values.get(z);
    }    
    
    public final int getAttributeType()
    {
        return attributeType;
    }
    
    public final boolean getNegated()
    {
        return negated;
    }
    
    public final boolean getRegExpMatch()
    {
        return regExpMatch;
    }
}
