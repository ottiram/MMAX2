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

import org.eml.MMAX2.annotation.markables.MarkableLevel;

public class MMAX2AtomicQueryTerm
{
    // Name of the attribute that this QueryTerm accesses
    String attributeName="";
    // Type of the attribute this QueryTerm accesses, as defined in MMAX2
    int attributeType=-1;
    // Array of possible values to be matched by this query (should be only one for regExpMatches)
    String[] values=null;
    // MarkableLevel this query is concerned with
    MarkableLevel level=null;
    boolean negated=false;
    boolean regExpMatch=false;
    int endsAt = -1;
    // Compact string representation of this query
    String compactString ="";
    
    /** Creates new MMAX2AtomicQueryTerm */
    public MMAX2AtomicQueryTerm(String _attributeName, int _attributeType, String[] _values, MarkableLevel _level, boolean _negated, boolean _regExpMatch, int _endsAt, String _compactString) 
    {
        attributeName = _attributeName;
        attributeType = _attributeType;
        values =_values;
        level = _level;
        negated = _negated;
        regExpMatch = _regExpMatch;
        endsAt = _endsAt;
        compactString = _compactString;       

    }

    /** used for decoration and screen feedback only .*/
    public final String toCompactString()
    {
        String result = compactString+" [";
        ArrayList tempList = toConstraints();
        for (int z=0;z<tempList.size();z++)
        {
            result = result + (String)tempList.get(z)+" ";
            if (negated && z < tempList.size()-1) 
            {
                result = result + "AND ";
            }
            else if (! negated && z < tempList.size()-1)
            {
                result = result + "OR ";
            }
        }
        result = result.trim();
        result = result + "]";
        return result;
    }
    
    public final String toString()
    {
        String result = "AttributeName: "+attributeName;
        result = result + " Values: ";
        for (int z=0;z<values.length;z++)
        {
            result = result + (z+1)+". "+values[z]+" ";
        }        
        result =result + " Negated: "+negated;
        result = result + " Type: "+attributeType;
        return result;
    }
    
    public final int getEndsAt()
    {
        return endsAt;
    }
    
    /** This method returns the match condition represented in this query term as an ArrayList of Strings of the form 
        attribute::value, to be used with the match method on the Markable object. 
        Used for decoration only! */
    public final ArrayList toConstraints()
    {
        ArrayList result = new ArrayList();
        String tempResult = "";
        if (!regExpMatch)
        {
            for (int u=0;u<values.length;u++)
            {
                tempResult=attributeName+"::"+values[u];
                if (negated) tempResult="!"+tempResult;
                result.add(tempResult);
                tempResult = "";
            }
        }
        else
        {
            // The term is a reg exp term
            tempResult="*"+attributeName+"::"+values[0];
            if (negated) tempResult="!"+tempResult;            
            result.add(tempResult);
            tempResult = "";
        }
        return result;
    }
    
    /** Used to create the actual criterion to be matched against a MarkableLevel bmo getMarkablesMatchingAll()/Any() */
    protected final MMAX2MatchingCriterion getMatchingCriterion()
    {
        // Create a matching criterion for this query, taking this queries name, type, and boolean flags
        MMAX2MatchingCriterion crit = new MMAX2MatchingCriterion(attributeName,attributeType,negated,regExpMatch);
        // Add all values
        for (int z=0;z<values.length;z++)
        {
            crit.addValue(values[z]);
        }
        // Make sure that empty brackets are mapped as empty string
        if(crit.getSize()==0)
        {
            crit.addValue("");
        }
        return crit;
    }
        
    public final ArrayList execute()
    {
        ArrayList resultList = null;
        MMAX2MatchingCriterion crit = getMatchingCriterion();
        if ((!regExpMatch) && negated)
        {   
            // If query is of form not x and y and z, use matchingAll
            resultList = level.getMarkablesMatchingAll(crit);
        }
        else
        {
            // Use matching any
            resultList = level.getMarkablesMatchingAny(crit);
        }        
        return resultList;
    }
}
