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
import java.util.StringTokenizer;

import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2ComplexQueryTerm 
{
    // Type of logical connection between all terms (atomic and complex) in this term
    int connector = -1;
    
    // Counter for occurrences of connectors, all of which have to be of the same type
    int connectorInstances = 0;
    
    // Counter for all (atomic and complex) terms in this complex term
    int size = 0;
    
    boolean negated = false;
    String queryString = null;
    MarkableLevel level = null;
    int endsAt = -1;
    int startsAt = -1;
    // List containing all MMAX2AtomicQueryTerm objects contained in this
    ArrayList atomicQueryTerms = new ArrayList();
    
    // List containing all MMAX2ComplexQueryTerm objects embedded in this
    ArrayList complexQueryTerms = new ArrayList();
    
    /** Creates new MMAX2ComplexQueryTerm */
    public MMAX2ComplexQueryTerm(String _queryString, MarkableLevel _level, int _from, int _to, boolean _negated) throws org.eml.MMAX2.annotation.query.MMAX2QueryException
    {
        queryString = _queryString.trim();
        level = _level;
        negated = _negated;
        if (queryString.charAt(_from) != '(' || queryString.charAt(_to) != ')')
        {
            throw new MMAX2QueryException("Query Syntax Error: Complex query must be surrounded by ( and ) !");
        }
        
        startsAt = _from;
        
        // Store position of closing bracket to determine when the entire string has been processed
        endsAt = _to;

        
        // Now, from and to are known to be the outer ( and ) of this ComplexQueryTerm
        // Look into the query string itself
        // Skip leading (
        _from++;
        // Skip any leading white space
        _from = skip(_from);        
                
        // Cut off trailing )
        _to--;
        // Skip any trailing white space 
        _to = skap(_to);
        
        int lastTermsEnd=-1;
        while(true)
        {
            try
            {
                lastTermsEnd = parseQueryString(_from, _to);
            }
            catch (java.lang.StringIndexOutOfBoundsException ex)
            {
                throw new MMAX2QueryException("Query Syntax Error: Something's wrong with the query!");
            }
            if (lastTermsEnd == -1 || lastTermsEnd > _to)
            {
                throw new MMAX2QueryException("Query Syntax Error: Something's wrong with the query!");
            }
            _from = lastTermsEnd+1;
            _from=skip(_from);            
            if (_from >= endsAt) break;
        }
        if (getConnector() == -1 && size > 1 )
        {
            throw new MMAX2QueryException("Query Syntax Error: No logical connector in complex query term:\n"+queryString.substring(startsAt,endsAt+1));
        }
        if (connectorInstances != size-1)
        {
            String temp = "";
            if (connector == MMAX2TermConnector.AND)
            {
                temp = "Probably you meant AND.";
            }
            else
            {
                temp = "Probably you meant OR.";
            }
            throw new MMAX2QueryException("Query Syntax Error: Missing logical connector in complex query term:\n"+queryString.substring(startsAt,endsAt+1)+"\n"+temp);
        }
    }
    
    
    /** This method recursively executes the query specified in this complex query term. It returns an ArrayList of all matching 
        Markables. */
    public final ArrayList execute()
    {
        ArrayList finalResult = new ArrayList();
        
        // Create an empty array of ArrayLists, one for each sub term in this complex query
        ArrayList[] allResults = new ArrayList[this.size];
        
        ArrayList[] allInvertedResults = null;
        
        // Iterate over all atomic query terms in this complex term
        for (int z=0;z<atomicQueryTerms.size();z++)
        {            
            // Get and store the results of all atomic queries in this term
            allResults[z] = ((MMAX2AtomicQueryTerm) atomicQueryTerms.get(z)).execute();
        }
        
        // Iterate over all complex query terms in this complex term
        for (int z=0;z<complexQueryTerms.size();z++)
        {
            // Get and store the results of all complex queries in this term
            allResults[z+atomicQueryTerms.size()] = ((MMAX2ComplexQueryTerm) complexQueryTerms.get(z)).execute();
        }
        
        if (connector == MMAX2TermConnector.AND)
        {
            // The results should be AND-connected, so no special treatment of negation necessary
            // Simply create intersection of all result sets
            if (negated)
            {
                // If this is negated, invert result
                finalResult = MMAX2QueryTree.invert(MMAX2QueryTree.intersect(allResults),level.getMarkables());
            }
            else
            {
                // If this is not negated, do not invert
                finalResult = MMAX2QueryTree.intersect(allResults);
            }
        }
        else
        {
            // The results should be OR-connected
            // So handle differently if negation is present
            if (negated)
            {
                // This complex term is negated and OR-connected
                // So apply deMorgansLaw here, turning 'not (a or b)' into 'not a and not b'
                // Invert every result
                allInvertedResults = MMAX2QueryTree.invertAll(allResults,level.getMarkables());
                finalResult = MMAX2QueryTree.intersect(allInvertedResults);
            }
            else
            {
                // This complex term is not negated, so OR-connect by simply creating the merge of all result sets
                finalResult = MMAX2QueryTree.merge(allResults);
            }
        }
        
        return finalResult;        
    }
    
    /** This method receives zero-based indices of the first and last character to consider. It returns the end index of the last
        term parsed. From and to have been moved to non-ws-positions when this method is called. */
    public final int parseQueryString(int from, int to) throws java.lang.StringIndexOutOfBoundsException, MMAX2QueryException 
    {                   
                
        MMAX2TermConnector currentConnector = null;
        MMAX2AtomicQueryTerm currentAtomicQueryTerm = null;
        MMAX2ComplexQueryTerm currentComplexQueryTerm = null;
                
        int end = -1;
        // Check if next term is complex as well 
        if (queryString.substring(from).startsWith("(") || (queryString.substring(from).startsWith("!(")) || (queryString.substring(from).startsWith("not(")))
        {
            // The next term is complex as well
            if (queryString.substring(from).startsWith("("))
            {
                // Do not move from, because we want the brackets to reach the constructor
                end = getMatchingBracketPosition(from,"(",")");
                // Build embedded complex query recursively
                currentComplexQueryTerm = new MMAX2ComplexQueryTerm(queryString,level,from,end-1,false);
            }
            else if (queryString.substring(from).startsWith("!("))
            {
                // Move from one position to get rid of negator
                from++;
                end = getMatchingBracketPosition(from,"(",")");
                // Build embedded complex query recursively
                currentComplexQueryTerm = new MMAX2ComplexQueryTerm(queryString,level,from,end-1,true);            
            }
            else if (queryString.substring(from).startsWith("not("))
            {
                // Move from three positions to get rid of negator
                from=from+3;
                end = getMatchingBracketPosition(from,"(",")");
                // Build embedded complex query recursively
                currentComplexQueryTerm = new MMAX2ComplexQueryTerm(queryString,level,from,end-1,true);            
            }
            
            if (currentComplexQueryTerm != null)
            {
                size++;
                complexQueryTerms.add(currentComplexQueryTerm);
                if (currentComplexQueryTerm.getEndsAt() < to)
                {
                    // There still is sth. after the term just found, so we must expect a connector
                    currentConnector = getNextConnector(currentComplexQueryTerm.getEndsAt());
                    if (currentConnector != null)
                    {
                        connectorInstances++;
                        if (this.connector==-1)
                       {
                            // The connector for this term has not been set yet
                            // So set it to current type (AND or OR)
                            this.connector = currentConnector.getType();
                        }
                        else
                        {
                            if (this.connector != currentConnector.getType())
                            {                                
                                String  temp = "";
                                if (this.connector == MMAX2TermConnector.AND)
                                {
                                    temp = "Found OR, expected AND.";
                                }
                                else
                                {
                                    temp = "Found AND, expected OR.";
                                }                                                                
                                throw new MMAX2QueryException("Query Syntax Error: Inconsistent connectors in complex query!\n"+temp);
                            }
                        }
                        return currentConnector.getEndsAt();
                    }
                    else
                    {
                        return currentComplexQueryTerm.getEndsAt();
                    }
                }
                else
                {                                        
                    return currentComplexQueryTerm.getEndsAt();
                }
            }
            else
            {
                // The complex term could no be built
                throw new MMAX2QueryException("Query Syntax Error: Error with complex term!");
            }                
        }
        else if ( (queryString.substring(from,from+3).equalsIgnoreCase("and")) || 
                  (queryString.substring(from,from+2).equalsIgnoreCase("&&")) || 
                  (queryString.substring(from,from+2).equalsIgnoreCase("or")) || 
                  (queryString.substring(from,from+2).equalsIgnoreCase("||"))
                )
                {
                    currentConnector = getNextConnector(from);
                    if (currentConnector != null)
                    {
                        connectorInstances++;
                        if (connector==-1)
                        {
                            // The connector for this query has not been set yet
                            // So set it to current type (AND or OR)
                            connector = currentConnector.getType();
                        }
                        else
                        {
                            // The connector for this query has been set already, so check if the current one is consistent
                            if (connector != currentConnector.getType())
                            {
                                String  temp = "";
                                if (this.connector == MMAX2TermConnector.AND)
                                {
                                    temp = "Found OR, expected AND.";
                                }
                                else
                                {
                                    temp = "Found AND, expected OR.";
                                }                                                                
                                throw new MMAX2QueryException("Query Syntax Error: Inconsistent connectors in complex query!\n"+temp);                                
                            }
                        }
                        return currentConnector.getEndsAt();
                    }
                    else
                    {
                        throw new MMAX2QueryException("Query Syntax Error: Couldn't parse connector!"); 
                    }
                }
        else
        {
            // The directly embedded term is not complex
            // Get next atomic term from queryString
            currentAtomicQueryTerm = getNextAtomicTerm(from,to);
            if (currentAtomicQueryTerm != null)
            {
                size++;
                atomicQueryTerms.add(currentAtomicQueryTerm);
                if (currentAtomicQueryTerm.getEndsAt() < to)
                {                                       
                    // The String is not finished yet, so there MUST now follow a connector, if anything
                    // Look for a connector after the end position of recently found atomic term
                    currentConnector = getNextConnector(currentAtomicQueryTerm.getEndsAt());
                    if (currentConnector != null)
                    {
                        connectorInstances++;
                        if (connector==-1)
                        {
                            // The topLevelConnector for this query has not been set yet
                            // So set it to current type (AND or OR)
                            connector = currentConnector.getType();
                        }
                        else
                        {
                            // The connector for this query has been set already, so check if the current one is consistent
                            if (connector != currentConnector.getType())
                            {
                                String  temp = "";
                                if (this.connector == MMAX2TermConnector.AND)
                                {
                                    temp = "Found OR, expected AND.";
                                }
                                else
                                {
                                    temp = "Found AND, expected OR.";
                                }                                                                
                                throw new MMAX2QueryException("Query Syntax Error: Inconsistent connectors in complex query!\n"+temp);                                
                            }
                        }
                        // A connector was found after the current term, so there should be another term
                        return currentConnector.getEndsAt();
                    }
                    else
                    {
                        return currentAtomicQueryTerm.getEndsAt();
                    }
                }
                else
                {
                    return currentAtomicQueryTerm.getEndsAt();
                }
            }
            else
            {
                throw new MMAX2QueryException("Query Syntax Error: <term> expected!");
            }
        }
    }
    
    public final MMAX2AtomicQueryTerm getNextAtomicTerm(int from, int to) throws MMAX2QueryException
    {
        // MMAX2Attribute that this query queries
        MMAX2Attribute currentAttribute = null;
        // List of all values allowed for matching this query (more than one only for enumerated matches)
        ArrayList valueList = new ArrayList();

        // String name of the attribute that this query queries
        String attributeName = "";
        
        int attributeType = -1;
        String value = "";
        boolean negated = false;
        boolean regExpMatch = false;
        boolean basedataQuery = false;
        if (level.getMarkableLevelName().equalsIgnoreCase("internal_basedata_representation"))
        {           
            basedataQuery = true;
        }
        
        String compactString = "";
        int offset = 0;
        
        // Skip all leading whitespace
        from = skip(from);
        // quit if string has been processed
        if (from == queryString.length())
        {
            return null;
        }
        // Encode negation if present
        if (queryString.charAt(from) == '!')
        {
            negated = true;
            // Skip at least ! character
            from++;
            compactString="NOT ";
        }
        else if (queryString.substring(from,from+3).equalsIgnoreCase("not"))
        {
            negated = true;
            from = from+3;
            compactString="NOT ";
        }
        
        // Skip all whitespace in between
        from = skip(from);

        // Encode regExpMatch if present
        if (queryString.charAt(from) == '*')
        {
            // There is an * before the attribute name, so this is a REGEXP term
            regExpMatch = true;
        }
        // Skip all whitespace in between
        from = skip(from);

        // Extract attribute name up to = sign (exclusive). This still contains an *, if one was present !
        try
        {
            attributeName = queryString.substring(from,queryString.indexOf("=",from));
        }
        catch (java.lang.StringIndexOutOfBoundsException ex)
        {            
            throw new MMAX2QueryException("Query Syntax Error: '<attributeName>=' expected!");
        }

        // Store how far this string has been processed
        from = from + attributeName.length();

        if (regExpMatch)
        {
            // Remove * from attributes already identified as regexp queries AFTER from-value increment
            attributeName = attributeName.substring(1);
            offset = 1;
        }

        // Remove any whitespace
        attributeName = attributeName.trim();
        
        if (regExpMatch) compactString = compactString+"*";
        
        compactString=compactString+attributeName+" = ";
        
        if (attributeName.equalsIgnoreCase("markable_text"))
        {
            attributeType = MMAX2Constants.MARKABLE_TEXT;
        }
        else if (attributeName.equalsIgnoreCase("basedata_text"))
        {
            attributeType = MMAX2Constants.BASEDATA_TEXT;
        }
        else if (attributeName.equalsIgnoreCase("base_level"))
        {
            // Not supported now
            attributeType = MMAX2Constants.BASE_LEVEL;
        }
                
        // Check consistencies
        if (attributeType == MMAX2Constants.BASEDATA_TEXT && basedataQuery==false)
        {            
            throw new MMAX2QueryException("Query Syntax Error: Attribute basedata_text only defined for level basedata!");
        }
        
        if (basedataQuery == true && attributeType == -1)
        {
            // If bdq and not bd_text, this must be bd_attributes
            attributeType = MMAX2Constants.BASEDATA_ATTRIBUTES;
        }
        
        if (attributeType != MMAX2Constants.BASEDATA_TEXT && 
            attributeType != MMAX2Constants.BASEDATA_ATTRIBUTES && 
            attributeType != MMAX2Constants.BASE_LEVEL && 
            attributeType != MMAX2Constants.MARKABLE_TEXT)            
        {
            // todo: include other meta-attributes as well            
            // Do validity checking for non-meta attributes
            // member, pointer, and nominal ON MARKABLES only !

            // Find out if attribute name is valid for current level
            // Get all attributes valid for the current level
            // level *cannot* be invalid here
            ArrayList attributes = level.getCurrentAnnotationScheme().getAttributes();        
            // Iterate over all valid attributes
            for (int z=0;z<attributes.size();z++)
            {
                // Get name of current attribute and compare to the name found in query term
                if (((MMAX2Attribute)attributes.get(z)).getLowerCasedAttributeName().equalsIgnoreCase(attributeName))
                {
                    // Get the correct attribute and exit
                    currentAttribute = (MMAX2Attribute)attributes.get(z);                    
                    // Get type of attribute, to be passed to MMAX2AtomicQueryTerm
                    attributeType = currentAttribute.getType();
                    break;
                }
            }

            // currentAttribute MUST be associated with some attribute here
            if (currentAttribute == null)
            {
                // If no attribute could be found to be valid, we have an error
                throw new MMAX2QueryException("Query Semantics Error:  Attribute '"+attributeName+"' is undefined on level '"+level.getMarkableLevelName()+"'!");
            }
        }// if meta-attributes
        
        // Here, we know that the attribute is defined for markable query, or that bd_qery is performed
        if (queryString.charAt(from) != '=')
        {
            throw new MMAX2QueryException("Query Syntax Error: '=' expected!");
        }
        
        // Skip obligatory '=' sign
        from++;        
        from = skip(from);
        
        if (!regExpMatch)
        {   
            // If we do not have a regexp
            if (queryString.charAt(from)=='{')
            {
                from++;
                from=skip(from);
                // Look for matching closing bracket, which MUST be there
                try
                {
                    value = queryString.substring(from,queryString.indexOf("}",from));
                }
                catch (java.lang.StringIndexOutOfBoundsException ex)
                {
                    throw new MMAX2QueryException("Query Syntax Error: '}' expected!");
                }
                // Get entire String from between braces
                value = value.trim();
                compactString=compactString+value;
                String tempValue = "";

                // Now, value is supposed to be a comma-separated list
                StringTokenizer toki = new StringTokenizer(value);
                while (toki.hasMoreTokens())
                {
                    // Get current value
                    tempValue = toki.nextToken(",");
                    // Remove any whitespace
                    tempValue = tempValue.trim();
                    // Do validity checking for non-regexp match, but only if this concerns 
                    // an attribute with a restricted domain
                    if (attributeType == MMAX2Constants.MARKABLE_TEXT ||
                        attributeType == MMAX2Constants.BASEDATA_TEXT ||
                        attributeType == MMAX2Constants.BASEDATA_ATTRIBUTES)
                    {
                        // This is of a type that does not have a closed domain, so just add 
                        // (this may have more than one value)
                        valueList.add(tempValue);
                    }                    
                    else if (attributeType == AttributeAPI.MARKABLE_POINTER)
                    {
                        boolean valIsNumeric = false;
                        int numVal = -1;
                        try
                        {
                            numVal = Integer.parseInt(tempValue);
                        }
                        catch (java.lang.NumberFormatException ex)
                        {
                
                        }
                        if (tempValue.equalsIgnoreCase("empty")==false && tempValue.equalsIgnoreCase("target")==false && numVal==-1)
                        {
                            throw new MMAX2QueryException("Query Semantics Error: Pointer-type attribute '"+attributeName+"' on level '"+level.getMarkableLevelName()+"' cannot be queried for value '"+tempValue+"'!");                
                        }
                        valueList.add(tempValue);
                    }                                
                    else if (attributeType == MMAX2Constants.BASE_LEVEL)
                    {
                        boolean valIsNumeric = false;
                        int numVal = -1;
                        try
                        {
                            numVal = Integer.parseInt(tempValue);
                        }
                        catch (java.lang.NumberFormatException ex)
                        {
                
                        }
                        if ((tempValue.equalsIgnoreCase("true")==false && tempValue.equalsIgnoreCase("false")==false) || numVal!=-1)
                        {
                            throw new MMAX2QueryException("Query Semantics Error: BASE_LEVEL attribute cannot be queried for value '"+tempValue+"'!\n Legal values are 'true' and 'false'!");
                        }
                        valueList.add(tempValue);
                    }                                
                    
                    else if (attributeType == AttributeAPI.MARKABLE_SET)
                    {                                                
                        boolean valIsNumeric = false;
                        int numVal = -1;
                        try
                        {
                            numVal = Integer.parseInt(tempValue);
                        }
                        catch (java.lang.NumberFormatException ex)
                        {
                
                        }
                        if (tempValue.equalsIgnoreCase("empty")==false && 
                            tempValue.equalsIgnoreCase("initial")==false &&
                            tempValue.equalsIgnoreCase("final")==false &&
                            numVal==-1               
                            )
                        {
                            throw new MMAX2QueryException("Query Semantics Error: Set-type attribute '"+attributeName+"' on level '"+level.getMarkableLevelName()+"' cannot be queried for value '"+tempValue+"'!");
                        }   
                        else
                        {
                            // OK, so add (there may be more than one value)
                            valueList.add(tempValue);
                        }
                    }
                    else
                    {
                        // MARKABLE_ATTRIBUTE
                        // Check if the current value is legal for the current attribute
                        if (currentAttribute.isDefined(tempValue)==false)
                        {
                            throw new MMAX2QueryException("Query Semantics Error: Value '"+tempValue+"' is undefined for attribute '"+currentAttribute.getLowerCasedAttributeName()+"' on level '"+level.getMarkableLevelName()+"'!");
                        }
                        else
                        {
                            // OK, so add (there may be more than one value)
                            valueList.add(tempValue);
                        }
                        
                    }
                }
            }
            else
            {
                // An opening { could not be found
                throw new MMAX2QueryException("Query Semantics Error: Value enumeration must be enclosed in { and }!");
            }
        }
        else
        {
            // The value for the current term should be a regexp
            // For those, there _never_ is a validity check
            from = skip(from);
            // This must start with a {
            if (queryString.charAt(from)!= '{')
            {
                throw new MMAX2QueryException("Query Syntax Error: regexp string must be enclosed in { and }! (Will be removed before matching!)");
            }
            int end = queryString.indexOf("}",from);

            // and end with a }
            if (end == -1)
            {
                throw new MMAX2QueryException("Query Syntax Error: regexp string must be enclosed in { and }! (Will be removed before matching!)");
            }

            // extract value string
            value = queryString.substring(from+1,end);
            value = value.trim();
            compactString=compactString+value;
            // add to value list (should be the only one)
            valueList.add(value);

        } // end processing regExps
                
        return new MMAX2AtomicQueryTerm(attributeName,attributeType,(String[]) valueList.toArray(new String[0]),level,negated, regExpMatch, from+value.length()+offset, compactString);
    }

    
    private final int skip(int from)
    {
        try
        {
            while(queryString.charAt(from) == ' ')from++;
        }
        catch (java.lang.StringIndexOutOfBoundsException ex)
        {
            //
        }
        return from;
    }

    private final int skap(int from)
    {
        try
        {
            while(queryString.charAt(from) == ' ')from--;
        }
        catch (java.lang.StringIndexOutOfBoundsException ex)
        {
            //
        }
        return from;
    }

    public int getMatchingBracketPosition(int startposition, String openingBracket, String closingBracket)
    {
        int openedBrackets = 0;
        int closedBrackets = 0;
        String currentChar="";
        /* Iterate over rest of tree from startposition on */
        for (int z=startposition+1;z<queryString.length();z++)
        {
            currentChar = queryString.substring(z,z+1);
            if (currentChar.equals(closingBracket))
            {
                /* The character at the current position is ) */
                if (openedBrackets == closedBrackets)
                {
                    /* We are on par */
                    return z+1;
                }
                else
                {
                    closedBrackets++;
                }
            }
            else if (currentChar.equals(openingBracket))
            {
                openedBrackets++;
            }
        }// for z        
        return -1;
    }    
    
    public final int getEndsAt()
    {
        return endsAt;
    }
    
    public final int getConnector()
    {
        return connector;
    }
    
    public final MMAX2TermConnector getNextConnector(int from)
    {
        MMAX2TermConnector connector = null;
        from = skip(from);
        String conni = "";
        // Expect connector before next whitespace
        int end =queryString.indexOf(" ",from);
        // or before end of string
        if (end == -1) end = queryString.length();
        conni = queryString.substring(from,end);        
        
        int endsAt = from+conni.length();
        conni = conni.trim();
        if (conni.equalsIgnoreCase("and") || conni.equalsIgnoreCase("&&"))
        {
            connector = new MMAX2TermConnector(MMAX2TermConnector.AND, endsAt);
        }
        else if (conni.equalsIgnoreCase("or") || conni.equalsIgnoreCase("||"))
        {
            connector = new MMAX2TermConnector(MMAX2TermConnector.OR, endsAt);
        }
        return connector;        
    }  
    
    public final void dumpTree(int depth)
    {
        String filler = "";
        for (int z=0;z<depth;z++)
        {
            filler= filler+" ";
        }
        
        String root = "";
        if (this.connector==MMAX2TermConnector.AND)
        {
            root = "AND";
        }
        else if (this.connector==MMAX2TermConnector.OR)
        {
            root = "OR";
        }
        
        if (negated) root = "NOT "+root;
        System.out.println(filler+root);
        depth=depth+2;
        filler = filler+"  ";
        
        for (int z=0;z<atomicQueryTerms.size();z++)
        {
            System.out.println(filler+((MMAX2AtomicQueryTerm)atomicQueryTerms.get(z)).toCompactString());
        }
        
        for (int z=0;z<complexQueryTerms.size();z++)
        {
            ((MMAX2ComplexQueryTerm)complexQueryTerms.get(z)).dumpTree(depth);
        }
        
    }
}
