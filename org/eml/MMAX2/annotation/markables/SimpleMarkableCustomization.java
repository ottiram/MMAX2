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
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.text.SimpleAttributeSet;

import org.eml.MMAX2.annotation.query.MMAX2MatchingCriterion;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.utils.MMAX2Constants;


public class SimpleMarkableCustomization 
{
    private String[] criteria = null;
    private MMAX2MatchingCriterion[] matchingCriteria = null;
    private SimpleAttributeSet attributes = null;
    private int connector = MMAX2Constants.AND;
    private MarkableLevel level = null;
    
    // Create two lists to store relations and assigned colors for relation-attributes
    private ArrayList attributeNamesForColors = new ArrayList();
    private ArrayList relationColors = new ArrayList();

    // Create two lists to store relations and assigned line styles for relation-attributes
    private ArrayList attributeNamesForStyles = new ArrayList();
    private ArrayList relationStyles = new ArrayList();
    
    /** Creates new SimpleMarkableCustomization */
    public SimpleMarkableCustomization(MarkableLevel _level, String raw_criteria, int _connector, SimpleAttributeSet _attributes) 
    {
        // Set numerical representation for connector
        // The connector determines how several patterns in one rule be connected
        connector = _connector;
        // Set attributes to be returned upon match
        attributes = _attributes;
        // Set reference to markable level, needed to determine attribute type
        level = _level;
        // Create list to temporarily accept matching criterion objects
        ArrayList temp = new ArrayList();
                       
        boolean negated = false;
        boolean regExpMatch = false;
        boolean matchAll = false;
                
        // Get names of all style-attributes to be applied in style-part of this customization
        Enumeration enum2 = attributes.getAttributeNames();
        // Iterate over them
        while (enum2.hasMoreElements())
        {
            // Get next style-attribute name
            String tempElem = enum2.nextElement().toString();
            if (tempElem.startsWith("markable_set_line_color") || tempElem.startsWith("markable_pointer_line_color"))
            {
                // Get attribute name the appearance of which is to be modified by this
                String attribute = tempElem.substring(tempElem.indexOf(":")+1);
                // Store name in list of attributes
                attributeNamesForColors.add(attribute);
                // Store Color to be applied in relationColors list
                relationColors.add((Color)attributes.getAttribute(tempElem));
            }
            
            else if (tempElem.startsWith("markable_set_line_style") || tempElem.startsWith("markable_pointer_line_style"))
            {
                // Get attribute name the appearance of which is to be modified by this
                String attribute = tempElem.substring(tempElem.indexOf(":")+1);
                // Store name in list of attributes
                attributeNamesForStyles.add(attribute);
                // Store style to be applied in relationColors list
                relationStyles.add((Integer)attributes.getAttribute(tempElem));
            }            
        }
        
        // Split entire pattern line in individual patterns, separated by ;
        StringTokenizer toki = new StringTokenizer(raw_criteria,";");
        while (toki.hasMoreTokens())
        {
            // Get next token, i.e. next part of pattern element
            String current = toki.nextToken().trim();
            if (current.startsWith("!"))
            {
                // The criterion starts with a !, so negate and cut off
                negated = true;
                current = current.substring(1).trim();
            }
            if (current.startsWith("*"))
            {
                // The criterion starts with a *, so store as regExp and cut off
                if (negated)
                {
                    System.out.println("Error: negated regExpMatch!");
                    return;                    
                }
                regExpMatch = true;
                current = current.substring(1).trim();
            }
            String attributeName="";
            String valString ="";
            if (current.equalsIgnoreCase("{all}"))
            {
                matchAll= true;
            }
            else
            {            
                // Extract attribute name
//                String attributeName = "";
                try
                {   
                    attributeName = current.substring(0,current.indexOf("=")).trim();
                }
                catch (java.lang.StringIndexOutOfBoundsException ex)
                {
                    System.out.println("Error: Expected 'attribute name=value'");                
                }
                // extract String of target values
                try
                {
                    valString = current.substring(current.indexOf("=")+1).trim();
                }
                catch (java.lang.StringIndexOutOfBoundsException ex)
                {
                    System.out.println("Error: Expected 'attribute name=value'");                
                }
                        
                if (valString.startsWith("{")== false || valString.endsWith("}")== false)
                {
                    System.out.println("Error with customization value: "+valString);
                    return;
                }
                valString = valString.substring(1,valString.length()-1).trim();
            }
            MMAX2MatchingCriterion crit = new MMAX2MatchingCriterion(attributeName, level.getCurrentAnnotationScheme().getAttributeTypeByAttributeName(attributeName), negated, regExpMatch);
            
            if (matchAll)
            {
                crit.setMatchAll();
            }
            else
            {            
                StringTokenizer valdi = new StringTokenizer(valString,",");
                while(valdi.hasMoreTokens())
                {            
                    String val = (String)valdi.nextToken();
                    crit.addValue(val);
                }            
            
                if (crit.getSize()==0)
                {
                    crit.addValue("");
                }
            }
            temp.add(crit);
            crit = null;
        }        
        matchingCriteria = (MMAX2MatchingCriterion[]) temp.toArray(new MMAX2MatchingCriterion[temp.size()]);
    }

    
    public final SimpleAttributeSet matches(Markable markable)
    {            
        boolean match = false;
        SimpleAttributeSet result = null;
        MMAX2MatchingCriterion currentCrit = null;
        if (connector==MMAX2Constants.AND)
        {
            for (int z=0;z<matchingCriteria.length;z++)
            {
                currentCrit = matchingCriteria[z];
                if (currentCrit.getNegated() && currentCrit.getRegExpMatch()==false)
                {
                    if (MarkableHelper.matchesAll(markable, currentCrit)) 
                    {
                        match = true;               
                    }
                    else
                    {
                        match = false;
                        break;
                    }
                }
                else
                {
                    if (MarkableHelper.matchesAny(markable, currentCrit)) 
                    {
                        match = true;               
                    }
                    else
                    {
                        match = false;
                        break;
                    }                    
                }
            }
        }
        else if (connector==MMAX2Constants.OR)
        {
            for (int z=0;z<matchingCriteria.length;z++)
            {
                currentCrit = matchingCriteria[z];
                if (currentCrit.getNegated() && currentCrit.getRegExpMatch()==false)
                {
                    if (MarkableHelper.matchesAll(markable, matchingCriteria[z])) 
                    {
                        match = true;
                        break;
                    }
                }
                else
                {
                    if (MarkableHelper.matchesAny(markable, matchingCriteria[z])) 
                    {
                        match = true;
                        break;
                    }                    
                }
            }
        }
        else
        {
            System.out.println("Illegal connector "+connector);
        }

        if (match) 
        {
            result = new SimpleAttributeSet(attributes);
            for (int z=0;z<attributeNamesForColors.size();z++)
            {
            	MMAX2Attribute tempAttrib = null;
                MMAX2Attribute[] tempAttribs = level.getCurrentAnnotationScheme().getAttributesByNameAndType("^"+(String)attributeNamesForColors.get(z)+"$", AttributeAPI.MARKABLE_SET, AttributeAPI.MARKABLE_POINTER);
                if (tempAttribs.length > 0)
                {
                	tempAttrib = tempAttribs[0];
                }
                if (tempAttrib != null)
                {
                    MarkableRelation relation = tempAttrib.getMarkableRelation();
                    if (relation != null)
                    {
                        relation.setLineColor((Color)relationColors.get(z));
                    }
                }
            }
            for (int z=0;z<attributeNamesForStyles.size();z++)
            {
            	MMAX2Attribute tempAttrib = null;
                MMAX2Attribute[] tempAttribs = level.getCurrentAnnotationScheme().getAttributesByNameAndType("^"+(String)attributeNamesForStyles.get(z)+"$", AttributeAPI.MARKABLE_SET, AttributeAPI.MARKABLE_POINTER);
                if (tempAttribs.length > 0)
                {
                	tempAttrib = tempAttribs[0];
                }
                if (tempAttrib != null)
                {
                    MarkableRelation relation = tempAttrib.getMarkableRelation();
                    if (relation != null)
                    {
                        relation.setLineStyle((Integer)relationStyles.get(z));
                    }
                }
            }
        }
        return result;
    }   
}
