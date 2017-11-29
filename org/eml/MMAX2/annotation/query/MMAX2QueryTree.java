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
import java.util.Comparator;
import java.util.HashSet;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseLoader;

/** A MMAX2QueryTree represents a query concerning _one_ MarkableLevel. */
public class MMAX2QueryTree 
{
    // Complete string of the original query
    String originalQuery="";
    // Single ComplexQueryTerm object constituting the root of this query tree
    MMAX2ComplexQueryTerm root = null;
    
    static int ENUMERATED = 2; // e.g. npform={defnp, indefnp}
    static int REGEXP = 3; // e.g. *text={[a-z][A-Z]*}
    
    /** Creates new MMAX2QueryTree */
    public MMAX2QueryTree(String _query, MarkableLevel _level) throws MMAX2QueryException 
    {
        int from = 0;
        boolean topLevelIsNegated = false;
        originalQuery = _query.trim();
        try
        {
            if (originalQuery.startsWith("!"))
            {
                topLevelIsNegated = true;
                originalQuery = originalQuery.substring(1);
                originalQuery = originalQuery.trim();
            }
            else if (originalQuery.substring(0,3).equalsIgnoreCase("not"))
            {
                topLevelIsNegated = true;
                originalQuery = originalQuery.substring(3);
                originalQuery = originalQuery.trim();
            }
        }
        catch (java.lang.Exception ex)
        {
            throw new MMAX2QueryException("Query Syntax Error: Something's wrong with the start of the query!");
        }
        
        // This expects the string to be enclosed in brackets !!        
        root = new MMAX2ComplexQueryTerm(originalQuery, _level, from, originalQuery.length()-1,topLevelIsNegated);
/*        if (root.getEndsAt()==originalQuery.length()-1)
        {
        	
        }
        */
    }

    
    /** This method executes the query described by this MMAX2QueryTree. It returns an ArrayList of Markables matching the query. */
    public final ArrayList execute()
    {
        return root.execute();
    }

    /** This method executes the query described by this MMAX2QueryTree. It returns an ArrayList of Markables matching the query. */
    public final ArrayList execute(Comparator comp)
    {        
        ArrayList temp = root.execute();
        if (comp != null)
        {
            Markable[] tempArray =  (Markable[])temp.toArray(new Markable[0]);;
            java.util.Arrays.sort(tempArray,comp);
            temp =  new ArrayList(java.util.Arrays.asList(tempArray));
        }
        return temp;
    }
    
    
    public final static void main (String args[])
    {
        String infile = args[0];
        String level = args[1];
        String query = args[2];                        
                        
        MMAX2DiscourseLoader loader = new MMAX2DiscourseLoader(infile, false,"");        
        MMAX2Discourse currentDiscourse = loader.getCurrentDiscourse();
        currentDiscourse.applyStyleSheet("");
        currentDiscourse.performNonGUIInitializations();
        MMAX2QueryTree tree = null;
        try
        {
            tree = new MMAX2QueryTree(query,currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(level,true));                
        }
        catch (MMAX2QueryException ex)
        {
            ex.printStackTrace();
        }
        tree.execute();
        System.exit(0);
    }
    
    
    public final static ArrayList invert(ArrayList invertee, ArrayList background)
    {
        ArrayList result = new ArrayList();
        Markable currentMarkable = null;
        for (int z=0;z<background.size();z++)
        {
            currentMarkable = (Markable) background.get(z);
            if (invertee.contains(currentMarkable)==false)
            {
                result.add(currentMarkable);
            }
        }
        return result;
    }
    
    public final static ArrayList[] invertAll (ArrayList[] invertees, ArrayList background)
    {
        ArrayList[] result = new ArrayList[invertees.length];
        for (int z=0;z<invertees.length;z++)
        {
            result[z] = invert(invertees[z],background);
        }        
        return result;
    }
    
    /** This method accepts an abitrary number of ArrayLists of Markables and returns the list resulting from the merge of all. */
    public final static ArrayList merge(ArrayList[] lists)
    {
        HashSet mergeResult = new HashSet();
        for (int z=0;z<lists.length;z++)
        {
            mergeResult.addAll(new HashSet(((ArrayList)lists[z])));
        }
        return new ArrayList(mergeResult);
    }

    /** This method accepts an abitrary number of ArrayLists of Markables and returns a list resulting from the intersection of all. */
    public final static ArrayList intersect(ArrayList[] lists)
    {
        ArrayList returnedResultList = new ArrayList();

        ArrayList resultList = null;
        ArrayList currentList = null;
        int currentMinSize = Integer.MAX_VALUE;
        // Iterate over all lists
        for (int z=0;z<lists.length;z++)
        {
            // Get current list
            currentList = lists[z];
            // If current list is shorter than previously shortest one, select it
            if (currentList.size() < currentMinSize)
            {
                currentMinSize = currentList.size();
                resultList = currentList;
            }            
        }
        
        Markable currentMarkable = null;
        boolean mismatchFound = false;
        
        // Now, we know resultList to be the shortest of all supplied lists
        // Iterate over shortest list
        for (int o=0;o<resultList.size();o++)
        {
            mismatchFound = false;
            // Get current Markable from shortest list
            currentMarkable = (Markable) resultList.get(o);
            // Now, currentMarkable has to be contained in all other lists as well        
            // Iterate over all lists again
            for (int z=0;z<lists.length;z++)
            {
                // Get current list
                currentList = lists[z];
                if (currentList.contains(currentMarkable)==false)
                {
                    // If there is one list NOT containing currentMarkable, currentMarkable has to be removed from the intersection
                    // set
                    mismatchFound = true;
                    break;
                }
            } // next list in lists
            // Here, we iterated over all lists, or found a mismatch and broke
            // If mismatchFound = false, all lists did contain the currentMarkable
            if (mismatchFound == false)
            {
                // So keep it in/add it to intersection list
                returnedResultList.add(currentMarkable);
            }
        } // next element in minimal resultlist
        return returnedResultList;
    }    
}
