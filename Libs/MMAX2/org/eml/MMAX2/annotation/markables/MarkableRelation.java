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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eml.MMAX2.api.MarkableRelationAPI;
import org.eml.MMAX2.utils.MMAX2Utils;

public class MarkableRelation implements MarkableRelationAPI
{
    // The name of the attribute the Markables of which this MarkableRelation handles (e.g. coref_class, antecedent, etc.)
    private String attributeName;
    // Whether individual MarkableSets or MarkablePointers (with maxsize>1) are ordered or unordered
    private boolean ordered;
    // Collection of all MarkableSets/MarkablePointers/SetPointers available for this attribute, mapped to their String values 
    private HashMap individualSets;
    /** Width of the line drawn when rendering sets. */
    private int lineWidth;
    
    /** Color of the line drawn used when rendering sets. */
    private Color lineColor;
    private Color defaultLineColor;
    /** Style of lines used when rendering sets: MMAX2.STRAIGHT or MMAX2.RCURVE or MMAX2.LCURVE. */
    private int lineStyle;        
    private int defaultLineStyle;
    
    private String attributeNameToShowInFlag="";
    
    private int maxSize;
    private boolean dashed;
    public MarkableRelation(String _attributeName, int type, boolean _ordered, int _lineWidth, Color _color, int _lineStyle, int _maxSize, boolean _dashed, String _attributeNameToShowInFlag)
    {
        attributeNameToShowInFlag = _attributeNameToShowInFlag;
        attributeName = _attributeName;
        ordered = _ordered;
        individualSets = new HashMap();
        lineWidth = _lineWidth;
        defaultLineColor = _color;        
        lineColor = _color;
        lineStyle = _lineStyle;
        defaultLineStyle = _lineStyle;
        maxSize = _maxSize;
        dashed = _dashed;
    }
    
    public final Color getLineColor()
    {
        return lineColor;
    }
    
    public final void setLineColor(Color _color)
    {
        if (_color == null)
        {
            lineColor = defaultLineColor;
        }
        else
        {
            lineColor = _color;
        }
    }

    public final String getAttributeNameToShowInFlag()
    {
        return attributeNameToShowInFlag;
    }
    
    public final int getLineStyle()
    {
        return lineStyle;
    }
    
    public final void setLineStyle(Integer _style)
    {
        if (_style == null)
        {
            lineStyle = defaultLineStyle;
        }
        else
        {
            lineStyle = _style.intValue();
        }
    }
    
    
    public final MarkableSet[] getMarkableSets(boolean order)
    {
        ArrayList allSets = new ArrayList(individualSets.values());
        MarkableSet[] sets = (MarkableSet[])allSets.toArray(new MarkableSet[0]);
        if (order)
        {
            java.util.Arrays.sort(sets,new MarkableSetComparator());
        }
        
        return sets;
    }

    public final MarkablePointer[] getMarkablePointers(boolean order)
    {
        ArrayList allPointers = new ArrayList(individualSets.values());
        MarkablePointer[] pointers = (MarkablePointer[])allPointers.toArray(new MarkablePointer[0]);
        if (order)
        {
            java.util.Arrays.sort(pointers,new MarkablePointerComparator());
        }
        
        return pointers;
    }
    
    
    public final String getAttributeName()
    {
        return attributeName;
    }        
    
    /** This method adds Markable _markable to the MarkableSet attributeValue value (e.g. 'set_5'). If no set exists, a new one will be
        created. This method is called to add a Markable that has a valid value in the MARKABLE_SET-type attribute that this 
        MarkableRelation object is associated with. */
    public final MarkableSet addMarkableWithAttributeValueToMarkableSet(Markable _markable, String value)
    {
        // Try to get the MarkableSet already exisiting for value value
        MarkableSet oldSet = (MarkableSet)individualSets.get(value);
        if (oldSet == null)
        {
            oldSet = new MarkableSet(value, ordered, lineWidth, lineColor, lineStyle,this);
            individualSets.put(new String(value), oldSet);
        }
        // Now, oldSet is guaranteed to be a reference to a valid MarkableSet
        // Add Markable to be added
        oldSet.addMarkable(_markable);
        return oldSet;
    }
    
    public final MarkableSet getMarkableSetWithAttributeValue(String _attributeValue)
    {
        return (MarkableSet) individualSets.get(_attributeValue);
    }
    
    public final MarkableSet getMarkableSetContainingMarkable(Markable contained)
    {
        MarkableSet result = null;
        Iterator allMarkableSets = individualSets.values().iterator();
        while(allMarkableSets.hasNext())
        {
            result = (MarkableSet)allMarkableSets.next();
            if (result.containsMarkable(contained))
            {
                break;
            }
            else
            {
                result = null;
            }
        }
        return result;
    }
    
    public final void removeMarkableSet(MarkableSet removee)
    {
        individualSets.remove(removee.getAttributeValue());
    }            
    
    public final void removeMarkablePointer(MarkablePointer removee)
    {       
        individualSets.remove(removee.getSourceMarkable().getID());
    }            
    
    
    /** This method is used to create a new MarkablePointer with sourceMarkable as source Markable. The value that the source Markable
        has in its this.attributeName attribute is parsed into a list of satellite Markable IDs, which are then added to the set. 
        This method is called to add a Markable with a valid value in its MARKABLE_POINTER-type attribute. */
    public final void createMarkablePointer(Markable sourceMarkable, MarkableLevel sourceMarkableLevel)
    {
        MarkablePointer newMarkablePointer = new MarkablePointer(sourceMarkable,lineWidth,lineColor,lineStyle,maxSize,this,dashed);
        ArrayList allTargetIDs = MMAX2Utils.parseTargetSpan(sourceMarkable.getAttributeValue(attributeName),";");
        for(int z=0;z<allTargetIDs.size();z++)
        {
            String currentIDEntry = (String)allTargetIDs.get(z);
            // Use full as default (assume no namespace)
            String currentID=currentIDEntry;
            // By default, assume target to be source
            String currentTargetLevelName=sourceMarkableLevel.getMarkableLevelName();
            MarkableLevel currentTargetMarkableLevel = sourceMarkableLevel;
            if (currentIDEntry.indexOf(":")!=-1)
            {
                // If the target id has a name space
                // Get that name space as new target levle name
                currentTargetLevelName = currentIDEntry.substring(0,currentIDEntry.indexOf(":"));
                currentTargetMarkableLevel = sourceMarkableLevel.getCurrentDiscourse().getMarkableLevelByName(currentTargetLevelName, false);
                currentID=currentIDEntry.substring(currentIDEntry.indexOf(":")+1);
            }
            
            
            Markable currentTarget = sourceMarkableLevel.getCurrentDiscourse().getCurrentMarkableChart().getMarkableByID(currentID,currentTargetMarkableLevel);
            //Markable currentSatellite = (Markable) sourceMarkableLevel.getMarkableById((String)allSatelliteIDs.get(z));
            if (currentTarget != null)
            {
                newMarkablePointer.addTargetMarkable(currentTarget);
            }
            else
            {
                System.err.println("Markable "+currentID+" was not found");
            }
        }
        individualSets.put(new String(sourceMarkable.getID()),newMarkablePointer);        
    }

        
    /** This method is used to create a new MarkablePointer with sourceMarkable as source Markable. The value that the source Markable
        has in its this.attributeName attribute is parsed into a list of satellite Markable IDs, which are then added to the set. 
        This method is called to add a Markable with a valid value in its MARKABLE_POINTER-type attribute. */
    public final void createMarkablePointer(Markable sourceMarkable, Markable firstTargetMarkable)
    {
        MarkablePointer newMarkablePointer = new MarkablePointer(sourceMarkable,lineWidth,lineColor,lineStyle,maxSize,this,dashed);
        newMarkablePointer.addTargetMarkable(firstTargetMarkable);
        individualSets.put(new String(sourceMarkable.getID()),newMarkablePointer);        
    }
    
    
    public final MarkablePointer getMarkablePointerForSourceMarkable(Markable sourceMarkable)
    {
        return (MarkablePointer) individualSets.get((String)sourceMarkable.getID());
    }
    
    public final MarkablePointer[] getMarkablePointersWithTargetMarkable(Markable target)
    {
        ArrayList result = new ArrayList();
        Iterator allPointerSets = individualSets.keySet().iterator();
        while (allPointerSets.hasNext())
        {
            MarkablePointer currentMarkablePointer = (MarkablePointer) individualSets.get((String)allPointerSets.next());
            if (currentMarkablePointer.isTargetMarkable(target))
            {
                result.add(currentMarkablePointer);
            }
        }
        return (MarkablePointer[]) result.toArray(new MarkablePointer[0]);
    }
    
    class MarkableSetComparator implements java.util.Comparator
    {        
        public int compare(Object o1, Object o2) 
        {
            MarkableSet s1 = (MarkableSet) o1;
            MarkableSet s2 = (MarkableSet) o2;
            int m1 = s1.getInitialMarkable().getLeftmostDiscoursePosition();
            int m2 = s2.getInitialMarkable().getLeftmostDiscoursePosition();
            if (m1 < m2) 
            {
                return -1;
            }
            else if (m2 < m1)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }        
    }
    
    class MarkablePointerComparator implements java.util.Comparator
    {        
        public int compare(Object o1, Object o2) 
        {
            MarkablePointer p1 = (MarkablePointer) o1;
            MarkablePointer p2 = (MarkablePointer) o2;
            int m1 = p1.getSourceMarkable().getLeftmostDiscoursePosition();
            int m2 = p2.getSourceMarkable().getLeftmostDiscoursePosition();
            if (m1 < m2) 
            {
                return -1;
            }
            else if (m2 < m1)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }        
    }    
}