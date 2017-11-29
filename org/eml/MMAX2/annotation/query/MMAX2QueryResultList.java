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

import org.eml.MMAX2.api.QueryResultListAPI;

public class MMAX2QueryResultList extends ArrayList implements QueryResultListAPI
{
//    private int width = -1;
    private int indexToUse = 0;
    private boolean indexSetByUser = false;
    private ArrayList attributeNamesToDisplay = null;
    private String command = "";
    
    /** Creates a new instance of MMAX2QueryResultList of width 1 */
    public MMAX2QueryResultList(ArrayList elements) 
    {
        // elements can be either MMAX2QueryResultTupleElements or (Markables or Basedata)
        super();
        int len = elements.size();
        for (int z=0;z<len;z++)
        {
            if (elements.get(z) instanceof MMAX2QueryResultTupleElement)
            {
                add(new MMAX2QueryResultTuple((MMAX2QueryResultTupleElement)elements.get(z)));
            }
            else
            {
                add(new MMAX2QueryResultTuple(new MMAX2QueryResultTupleElement(elements.get(z))));   
            }                
        }
    }    

    /** Creates a new empty instance of MMAX2QueryResultList */
    public MMAX2QueryResultList() 
    {
        super();
    }    
    
    
    /** This constructor returns a clone of MMAX2QueryResultList toClone. */
    public MMAX2QueryResultList(MMAX2QueryResultList toClone)
    {
        super();
//        width = toClone.getWidth();
        int len = toClone.size();
        for (int z=0;z<len;z++)
        {                      
            add(new MMAX2QueryResultTuple((MMAX2QueryResultTuple)toClone.get(z)));
        }        
        // Note: No command or col spec is copied here (just orgIndex)!!
    }

    /** This constructor returns a MMAX2QueryResultList with 1-element tuples copied from position index from oldList*/
    public MMAX2QueryResultList(MMAX2QueryResultList oldList, int index)
    {
        super();
        int len = oldList.size();
        MMAX2QueryResultTupleElement elem = null;
        // Iterate over all tuples in oldList
        for (int z=0;z<len;z++)
        {                      
            elem = ((MMAX2QueryResultTuple)oldList.get(z)).getValueAt(index);
            add(new MMAX2QueryResultTuple(elem));
        }
    }
        
    public final int getResultSize()
    {
    	return size();
    }
    
    public final int getElementIndexBeforeDiscoursePosition(int discPos)
    {                        
        int low = 0;
        int hi = size()-1;
        int midpt = 0;
        int temp = 0;
        while (low <= hi)
        {    
            midpt = (low + hi) / 2;
            if (isIndexSetByUser())
            {
                // The index to use has been set explicitly, so use that for determining the LMDP
                temp = getElementAtIndexFromColumnToUse(midpt).getLeftmostDiscoursePosition();
            }
            else
            {
                // Use entire tuple for determining LMDP
                temp = getTupleAtIndex(midpt).getLeftmostDiscoursePosition();
            }
            if (discPos == temp)
            {
                if (isIndexSetByUser())
                {
                    int tempMidPt = midpt;
                    // The index to use has been set explicitly, so use that for determining the LMDP
                    while(tempMidPt>0 && getElementAtIndexFromColumnToUse(tempMidPt).getLeftmostDiscoursePosition()==temp)
                    {
                        tempMidPt--;
                    }
                    midpt=tempMidPt;
                }
                else
                {
                    int tempMidPt = midpt;
                    // Use entire tuple for determining LMDP
                    while(tempMidPt>0 && getTupleAtIndex(tempMidPt).getLeftmostDiscoursePosition()==temp)
                    {
                        tempMidPt--;
                    }
                    midpt=tempMidPt;
                }                
                return midpt;
            }
            else if (discPos < temp)
            {
                hi = midpt-1;
            }
            else
            {
                low = midpt+1;
            }
        }
        if (isIndexSetByUser())
        {
            int tempMidPt = midpt;
            // The index to use has been set explicitly, so use that for determining the LMDP
            while(tempMidPt>0 && getElementAtIndexFromColumnToUse(tempMidPt).getLeftmostDiscoursePosition()==temp)
            {
                tempMidPt--;
            }
            midpt=tempMidPt;
        }
        else
        {
            int tempMidPt = midpt;
            // Use entire tuple for determining LMDP
            while(tempMidPt>0 && getTupleAtIndex(tempMidPt).getLeftmostDiscoursePosition()==temp)
            {
                tempMidPt--;
            }
            midpt=tempMidPt;
        }                
        
        return midpt;
    }
    
    public final void setCommand(String _command)
    {
        System.err.println("Setting command to "+_command);
        command = _command;
    }
    
    public final String getCommand()
    {
        return command;
    }
    
    public final void setAttributeNamesToDisplay(ArrayList list)
    {
        attributeNamesToDisplay = list;
    }

    public final ArrayList getAttributeNamesToDisplay()
    {
        return attributeNamesToDisplay;
    }
    
    /** This method returns true if this ResultList  was accessed via a variable which had a column specifier, false otherwise. */
    public final boolean isIndexSetByUser()
    {
        return indexSetByUser;
    }
    
    public final void setIndexSetByUser()
    {
        indexSetByUser = true;
    }
    
    public final int getWidth()
    {
        return getMaximumWidth();
    }
    
    public final int getMaximumWidth()
    {
        int result = -1;
        for (int z=0;z<size();z++)
        {
            if (((MMAX2QueryResultTuple)this.get(z)).getWidth() > result)
            {
                result = ((MMAX2QueryResultTuple)this.get(z)).getWidth();
            }
        }
        return result;
    }
    
    public final int getIndexToUse()
    {
        return indexToUse;
    }
    
    public final void setIndexToUse(int index)
    {
        if (indexToUse < getWidth())
        {
            indexToUse = index;
            indexSetByUser = true;
        }
        else
        {
            System.err.println("Error: Cannot set indexToUse to "+index+"! Defaulting to 0");
            toDefaultIndexToUse();
        }        
    }
    
    public final void toDefaultIndexToUse()
    {
        indexToUse = 0;
        indexSetByUser = false;
    }
    

    /** This method adds a new copy of the the supplied tuple to this. It is used for creating the results
         of 'filter' queries, i.e. in which only the first tuple in a match is retained in the result. 
         Therefore, discontinuity is inherited to the copy. Also, the outer discourse positions are
         simply copied from input tuple. */
    public final void addSingleTuple(MMAX2QueryResultTuple tuple1)
    {
        // This does in effect create a *clone* of the input tuple
        
        MMAX2QueryResultTuple tuple = new MMAX2QueryResultTuple();       
        
        // Add all elements from input tuple
        for (int a=0;a<tuple1.getWidth();a++)
        {
            tuple.add(tuple1.getValueAt(a));
        }
        if (tuple1.isDiscontinuous())
        {
            tuple.setDiscontinuous();
        }

        // Set extent of result to be the same as that of input
        tuple.setOuterDiscoursePositions(tuple1.getLeftmostDiscoursePosition(),tuple1.getRightmostDiscoursePosition());   
        // Add to this.
        add(tuple);
    }
    
    /** This method merges two result tuples into a new one, and adds the new tuple to this. */
    public final void mergeAndAdd(MMAX2QueryResultTuple tuple1, MMAX2QueryResultTuple tuple2, int mergeMode)
    {
        // Create new empty MMAX2QueryResultTuple
        MMAX2QueryResultTuple tuple = new MMAX2QueryResultTuple();
        
        // Add all elements from first list
        for (int a=0;a<tuple1.getWidth();a++)
        {
            tuple.add(tuple1.getValueAt(a));
        }
        
        // Add all elements from second list
        for (int a=0;a<tuple2.getWidth();a++)
        {
            tuple.add(tuple2.getValueAt(a));
        }
        
        // New April 7th: Set new tuple's extent based on semantic mode of merge operation
        int a_start = tuple1.getLeftmostDiscoursePosition();
        int a_end = tuple1.getRightmostDiscoursePosition();
        
        int b_start = tuple2.getLeftmostDiscoursePosition();
        int b_end = tuple2.getRightmostDiscoursePosition();
        
        if (mergeMode==Constants.AStart_AEnd)
        {           
            // The new tuple inherits the extent of the first argument tuple.
            // This is true for hierarchical relations:
            // during, contains, ends, ends_with, starts, starts_with (and negated variants)
            // In this case, the new tuple receives the extent of the *first* argument tuple
            tuple.setOuterDiscoursePositions(a_start, a_end);
            // Also, in this case discontinuity will only be inherited from the *first*
            // argument tuple.
            if (tuple1.isDiscontinuous())
            {
                tuple.setDiscontinuous();
            }
        }
        else if (mergeMode==Constants.AStart_BEnd)
        {
            // The new tuple has as extent the entire span of both input tuples
            // This is true for sequential relations:
            // meets, overlaps_right, overlaps_left
            tuple.setOuterDiscoursePositions(a_start, b_end);
            // In this case, discontinuity will be inherited from both tuple1 and tuple2
            if (tuple1.isDiscontinuous() || tuple2.isDiscontinuous())
            {
                tuple.setDiscontinuous();
            }
        }
        else if (mergeMode==Constants.BStart_AEnd)
        {
            tuple.setOuterDiscoursePositions(b_start, a_end);
        }
        else
        {
            System.err.println("Error: Invalid merge mode!");
        }
        
        // Add newly created tuple to this MMAX2QueryResultList
        // This will do nothing but add tuple mto this (standard method)
        add(tuple);
    }

    
    /** This method returns the MMAX2QueryResultTuple at list position index. */
    public final MMAX2QueryResultTuple getTupleAtIndex(int index)
    {
        return ((MMAX2QueryResultTuple)get(index));
    }

    
    /** This method returns from the MMAX2QueryResultTuple at list position index the Element in 
        the column this.indexToUse. */
    public final MMAX2QueryResultTupleElement getElementAtIndexFromColumnToUse(int index)
    {
        return ((MMAX2QueryResultTuple)get(index)).getValueAt(indexToUse);
    }
    
    /** This method returns from the MMAX2QueryResultTuple at list position index the Markable in 
        the column column. */
    public final MMAX2QueryResultTupleElement getElementAtIndexFromColumn(int index, int column)
    {
        return ((MMAX2QueryResultTuple)get(index)).getValueAt(column);
    }
    
    /** This method returns the numerical index of the column in which markables from MarkableLevel name are stored.
        It returns -1 if either the name does not exist or if it is ambiguous, i.e. not unique within this list. */
    public final int getColumnIndexByColumnName(String name)
    {
        int index = -1;
        boolean found = false;
        MMAX2QueryResultTuple firstTuple = null;
        if (size()>0)
        {
            firstTuple = (MMAX2QueryResultTuple)get(0);
            int width = getWidth();
            for (int z=0;z<width;z++)
            {
                if (firstTuple.getMarkableLevelNameAtColumnIndex(z).equalsIgnoreCase(name))
                {
                    if (found == true)
                    {
                        System.out.println("Column identifier '"+name+"' is ambiguous!");
                        // Reset previously found index
                        index = -1;
                        break;
                    }
                    else
                    {
                        found = true;
                        index = z;
                    }
                }
            }
        }
        return index;
    }
    
    public final void dump()
    {
        for (int o=0;o<size();o++)
        {
            System.out.println(((MMAX2QueryResultTuple)get(o)).toString());
        }
    }
    
}
