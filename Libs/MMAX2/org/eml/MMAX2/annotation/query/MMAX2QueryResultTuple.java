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

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.api.QueryResultTupleAPI;

public class MMAX2QueryResultTuple implements QueryResultTupleAPI
{
    private ArrayList elements;
    
    private int lmdp = -1;
    private int rmdp = -1;
    
    private int orgIndex=-1;
    
    private boolean discont = false;
    
    public MMAX2QueryResultTuple()
    {
        // This constructor is used to construct an empty tuple, to be filled with either single
        // elements
        elements = new ArrayList();
    }
    
    /** Creates a new instance of MMAX2QueryResultTuple of width 1. The discourse positions are
        copied from input, and also any discontinuity information. */
    public MMAX2QueryResultTuple(MMAX2QueryResultTupleElement e1) 
    {
        elements = new ArrayList();
        elements.add(e1);
        // This is a one-element tuple, so just copy its extent from only element
        lmdp = e1.getLeftmostDiscoursePosition();
        rmdp = e1.getRightmostDiscoursePosition();
        
        // This makes sense here because if the only element is discont, so is the entire tuple
        if (e1.isDiscontinuous())
        {
            setDiscontinuous();
        }
    }
            

    public MMAX2QueryResultTuple(MMAX2QueryResultTuple toClone)
    {
        elements = new ArrayList();
        int width = toClone.getWidth();
        for (int z=0;z<width;z++)
        {
            elements.add(toClone.getValueAt(z));
        }
        // Copy extent from original 
        lmdp = toClone.getLeftmostDiscoursePosition();
        rmdp = toClone.getRightmostDiscoursePosition();
        
        if (toClone.isDiscontinuous())
        {            
            setDiscontinuous();   
        }
        // New: copy orgIndex
        setOriginalIndex(toClone.getOriginalIndex());
    }

    
    public MMAX2QueryResultTuple(ArrayList _elements)
    {
        // This method is used to create result tuples from lists retrieved 
        // during relation-queries.
        // _elements is supposed to contain MMAX2QueryResultTupleElements already!!
        // Todo: Make this accept lists of *markables*, do conversion to tuple element here.
        
        elements = new ArrayList();
        // The width is one element per input markable
        int width = _elements.size();
        for (int z=0;z<width;z++)
        {
            elements.add((MMAX2QueryResultTupleElement)_elements.get(z));
            // New April 11th: Maintain discont marker on tuples
            // If any input element is discont, so is the entire tuple.
            // Does this make sense if this is only used to create lists of associatively
            // related markables??
            if (((MMAX2QueryResultTupleElement)_elements.get(z)).isDiscontinuous())
            {
                setDiscontinuous();
            }
        }
        // Set extent from first and last element
        // Does this make sense if this is only used to create lists of associatively
        // related markables??

        lmdp = getValueAt(0).getLeftmostDiscoursePosition();
        rmdp = getValueAt(width-1).getRightmostDiscoursePosition();
    }
    
    public final void setOriginalIndex(int _org)
    {
        orgIndex = _org;
    }
    
    public final int getOriginalIndex()
    {
        return orgIndex;
    }
    
    public final void add(MMAX2QueryResultTupleElement element)
    {
        elements.add(element);
        // Note: Since we do not know the semantics underlying this add, do not modify extent here
        // This is done in the merge method, which does know the said semantics and is the only method
        // using this add method.        
    }
    
    public final int getWidth()
    {
        return elements.size();
    }
    
    public boolean equals(Object otherObject)
    {
        // This compares two MMAX2QueryResultTuples
        // Default: true, to allow for and-connecting
        boolean result = true;
        MMAX2QueryResultTuple otherTuple = (MMAX2QueryResultTuple) otherObject;
        if (getWidth() == otherTuple.getWidth())
        {
            int width = getWidth();
            for (int v=0;v<width;v++)
            {
                result = result && (getValueAt(v).equals(otherTuple.getValueAt(v)));
                if (result == false)
                {
                    break;
                }
            }
        }
        else
        {
            result = false;
        }
        return result;
    }
    
    public final void setOuterDiscoursePositions(int _lmdp, int _rmdp)
    {
        lmdp = _lmdp;
        rmdp = _rmdp;
    }
    
    public final int getLeftmostDiscoursePosition()
    {
        return lmdp;
    }
    
    public final int getRightmostDiscoursePosition()
    {
        return rmdp;
    }
    
    public final boolean isDiscontinuous()
    {
        return discont;
    }
    
    public final void setDiscontinuous()
    {
        discont = true;
    }
    
    public final String[][] getFragments()
    {
        ArrayList temp = new ArrayList();        
        MMAX2QueryResultTupleElement currentElement = null;
        String[][] currentFrags = null;
        // Iterate over all tuple elements in this result tuple
        for (int z=0;z<getWidth();z++)
        {
            // Get current element
            currentElement = getValueAt(z);
            // Get Array of Array of de ids
            currentFrags = currentElement.getFragments();
            // Iterate over array of arrays
            for (int g=0;g<currentFrags.length;g++)
            {
                // Add each array (=fragment) to list
                temp.add(currentFrags[g]);
            }
        }
        return (String[][]) temp.toArray(new String[0][0]);
    }

    
    public final MMAX2QueryResultTupleElement getElementAt(int index)
    {
    	return getValueAt(index);
    }
    
    public final MMAX2QueryResultTupleElement getValueAt(int index)
    {
        if (index < elements.size())
        {
            return (MMAX2QueryResultTupleElement)elements.get(index);
        }
        else
        {
            return null;
        }
    }
    
    
    public final Markable getLastMarkableWithAttribute(String attributeName, int type)
    {
        Markable result = null;
        MMAX2QueryResultTupleElement currentElement = null;
        Markable temp = null;
        // Iterate over all MMAX2QueryResultTupleElemets backwards, to find last one
        for (int b=elements.size()-1;b>=0;b--)
        {
            // Get current element
            currentElement = (MMAX2QueryResultTupleElement) elements.get(b);
            if (currentElement.isMarkable())
            {
                // The current element wraps a markable
                // Get it
               temp = currentElement.getMarkable();
               if (temp.getMarkableLevel().getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$",type)!=null)
               {
                   result = temp;
                   break;
               }
            }
        }
        return result;
    }

    public final Markable getFirstMarkableWithAttribute(String attributeName, int type)
    {
        Markable result = null;
        MMAX2QueryResultTupleElement currentElement = null;
        Markable temp = null;
        // Iterate over all MMAX2QueryResultTupleElemets forward, to find first one
        for (int b=0;b<elements.size();b++)
        {
            // Get current element
            currentElement = (MMAX2QueryResultTupleElement) elements.get(b);
            if (currentElement.isMarkable())
            {
                // The current element wraps a markable
                // Get it
               temp = currentElement.getMarkable();
               if (temp.getMarkableLevel().getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$",type)!=null)
               {
                   result = temp;
                   break;
               }
            }
        }
        return result;
    }
    
    
    public final String getMarkableLevelNameAtColumnIndex(int index)
    {
        return (getValueAt(index)).getLevelName();
    }    
   
    public final String toString(ArrayList attributes)
    {
        String result = "";
        MMAX2QueryResultTupleElement currentElement = null;
        for (int u=0;u<elements.size();u++)
        {
            currentElement = (MMAX2QueryResultTupleElement) elements.get(u);
            result = result + ":::"+(currentElement).toString();
            if (attributes != null)
            {
                result=result+"{";
                String currentAttribute="";
                for (int m=0;m<attributes.size();m++)
                {
                    currentAttribute = (String)attributes.get(m);
                    result=result+currentAttribute+"=\""+currentElement.getAttributeValue(currentAttribute,"UNDEFINED")+"\"";
                    if (m<attributes.size()-1)
                    {
                        result=result+",";
                    }
                }
                result=result+"}";
            }
        }
        
        result = result + getLeftmostDiscoursePosition()+"-"+getRightmostDiscoursePosition()+" "+isDiscontinuous();
        return result;
    }    
}
