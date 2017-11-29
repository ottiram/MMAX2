/*
 * Copyright 2007 Mark-Christoph M�ller
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

package org.eml.MMAX2.annotation.scheme;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.xerces.parsers.DOMParser;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableHelper;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.api.AnnotationSchemeAPI;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.windows.MMAX2AttributePanel;
import org.eml.MMAX2.gui.windows.MMAX2AttributePanelContainer;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MMAX2AnnotationScheme implements AnnotationSchemeAPI
{        
    /** Maps IDs of the form level_n to MMAX2Attribute objects */
    private Hashtable attributesByID;
    /** Maps attribute name strings to MMAX2Attribute objects */
    private Hashtable attributesByLowerCasedAttributeName;
    /** Contains MMAX2Attribute objects in the sequence they appear in the scheme xml file */
    private ArrayList attributes;
    /** Maps IDs of the form value_n to IDs of the form level_m */
    private Hashtable valueIDsToAttributeIDs;
    /** Reference to AttributeWindow used to display this AnnotationScheme */
    private MMAX2AttributePanel attributepanel;
    /** List of those SchemeLevel IDs that are read only (read from .anno file ) */
    ArrayList ReadOnlySchemeLevels;
    private int size=0;
    
    MMAX2 mmax2 = null;    
    boolean ignoreClick = false;
    boolean hintLocked = false;
    String currentAttributeHintedAt = "";
    
    //private String levelUIMATypeMapping="";
    UIMATypeMapping uimaTypeMapping;
    
    private String schemeFileName ="";
    
    public MMAX2AnnotationScheme (String schemefilename)
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

        schemeFileName = schemefilename;
        /* Create generic DOMparser */
        DOMParser parser = new DOMParser();
        try
        {
            parser.setFeature("http://xml.org/sax/features/validation",false);
        }
        catch (org.xml.sax.SAXNotRecognizedException ex)
        {
            ex.printStackTrace();
        }
        catch (org.xml.sax.SAXNotSupportedException ex)
        {
            ex.printStackTrace();
        }        
        
        try
        {
            //parser.parse(new InputSource ("FILE:" +schemefilename));
        	parser.parse(new InputSource (new File(schemefilename).toURI().toString()));
        }        
        catch (org.xml.sax.SAXException exception)
        {
            exception.printStackTrace();
        }
        catch (java.io.IOException exception)
        {
            exception.printStackTrace();
        }
        
        attributesByID = new Hashtable();
        attributes = new ArrayList();
        attributesByLowerCasedAttributeName = new Hashtable();
        valueIDsToAttributeIDs = new Hashtable();
        
        int labelLength = 0;
        Document schemeDOM = parser.getDocument();        
        
        float fontSize = (float)11.0;
        
        NodeList root = schemeDOM.getElementsByTagName("annotationscheme");
        try
        {
            fontSize = Float.parseFloat(root.item(0).getAttributes().getNamedItem("fontsize").getNodeValue());
        }
        catch (java.lang.NumberFormatException ex)
        {
            
        }       
        catch (java.lang.NullPointerException ex)
        {
        	
        }
                 
        if (root.getLength() > 0 && root.item(0).getAttributes() != null && root.item(0).getAttributes().getNamedItem("uima_type_mapping") != null)
        {
        	uimaTypeMapping = new UIMATypeMapping(root.item(0).getAttributes().getNamedItem("uima_type_mapping").getNodeValue());
        	System.err.println(uimaTypeMapping.toString());
        }
        else
        {
        	uimaTypeMapping = new UIMATypeMapping("","");
        }
        
        /* Get all level elements from dom */
        NodeList allAttributes = schemeDOM.getElementsByTagName("attribute");        
        
        MMAX2Attribute currentAttribute = null;
        Node currentNode = null;
        
        // Determine max. attribute name length in entire AnnotationScheme
        for (int z=0;z<allAttributes.getLength();z++)
        {            
            currentNode = allAttributes.item(z);
            String currentNodeName = currentNode.getAttributes().getNamedItem("name").getNodeValue();
            if (currentNodeName.length() >= labelLength) labelLength = currentNodeName.length();
        }       
        
        String toolTipText = "";
        for (int z=0;z<allAttributes.getLength();z++)
        {
            currentNode = allAttributes.item(z);
            try
            {
                toolTipText = currentNode.getAttributes().getNamedItem("text").getNodeValue();
            }
            catch (java.lang.NullPointerException ex)
            {
                toolTipText = "";
            }
            
            String descriptionFileName = "";
            try
            {
                descriptionFileName = currentNode.getAttributes().getNamedItem("description").getNodeValue();
            }
            catch (java.lang.NullPointerException ex)            
            {
            	
            }
            
            String hintText="";
            if (descriptionFileName.equals("")==false)
            {
                hintText = readHTMLFromFile(schemeFileName.substring(0,schemeFileName.lastIndexOf(File.separator)+1)+descriptionFileName);
            }            
            
            String type = "";
            int typeInt = -1;
            try
            {
                /* Determine type of level */                
                    type = currentNode.getAttributes().getNamedItem("type").getNodeValue().trim();
            }
            catch (java.lang.NullPointerException ex)
            {
                /* Use type 'nominal_list' as default */
                type = "nominal_list";
            }
            
            type=type.toLowerCase();
            
            String attributeToShowInPointerFlag="";
            
            if (type.equalsIgnoreCase("nominal_button")) typeInt = AttributeAPI.NOMINAL_BUTTON;
            else if (type.equalsIgnoreCase("nominal_list")) typeInt = AttributeAPI.NOMINAL_LIST;
            else if (type.equalsIgnoreCase("freetext")) typeInt = AttributeAPI.FREETEXT;
            else if (type.equalsIgnoreCase("markable_set")) typeInt = AttributeAPI.MARKABLE_SET;
            else if (type.equalsIgnoreCase("markable_pointer")) typeInt = AttributeAPI.MARKABLE_POINTER;
            
            
            else if (type.startsWith("markable_pointer:"))
            {
                typeInt = AttributeAPI.MARKABLE_POINTER;
                attributeToShowInPointerFlag=type.substring(type.indexOf(":")+1);
            }
            
            int lineWidth = 2;
            try
            {
                lineWidth = Integer.parseInt(currentNode.getAttributes().getNamedItem("width").getNodeValue());
            }
            catch (java.lang.Exception ex)
            {
                //
            }
            
            Color color = Color.black;
            try
            {
                color = MMAX2Utils.getColorByName((String)currentNode.getAttributes().getNamedItem("color").getNodeValue());
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            String lineStyle="straight";
            int lineStyleInt=MMAX2Constants.STRAIGHT;
            try
            {
                lineStyle = (String) currentNode.getAttributes().getNamedItem("style").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }
            
            if (lineStyle.equalsIgnoreCase("straight"))
            {
                lineStyleInt = MMAX2Constants.STRAIGHT;
            }
            else if (lineStyle.equalsIgnoreCase("lcurve"))
            {
                lineStyleInt = MMAX2Constants.LCURVE;
            }
            else if (lineStyle.equalsIgnoreCase("rcurve"))
            {
                lineStyleInt = MMAX2Constants.RCURVE;
            }
            else if (lineStyle.equalsIgnoreCase("xcurve"))
            {
                lineStyleInt = MMAX2Constants.XCURVE;
            }
            else if (lineStyle.equalsIgnoreCase("smartcurve"))
            {
                lineStyleInt = MMAX2Constants.SMARTCURVE;
            }
            
            int maxSize = -1;
            try
            {
                maxSize = Integer.parseInt(currentNode.getAttributes().getNamedItem("max_size").getNodeValue());
            }
            catch (java.lang.Exception ex)
            {
                //
            }
            
            String targetDomain = "";
            try
            {
                targetDomain = (String) currentNode.getAttributes().getNamedItem("target_domain").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            boolean dashed=false;
            String dashAttrib="false";
            try
            {
                dashAttrib = (String) currentNode.getAttributes().getNamedItem("dashed").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                dashAttrib="false";
            }
            if (dashAttrib.equals("false")==false)
            {
                dashed=true;
            }
            
            String add_to_markableset_instruction = "ADD TO MARKABLE SET";
            try
            {
                add_to_markableset_instruction = (String) currentNode.getAttributes().getNamedItem("add_to_markableset_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            String remove_from_markableset_instruction = "REMOVE FROM MARKABLE SET";
            try
            {
                remove_from_markableset_instruction = (String) currentNode.getAttributes().getNamedItem("remove_from_markableset_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            String adopt_into_markableset_instruction = "ADOPT INTO MARKABLE SET";
            try
            {
                adopt_into_markableset_instruction = (String) currentNode.getAttributes().getNamedItem("adopt_into_markableset_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }
            
            String merge_into_markableset_instruction = "MERGE INTO MARKABLE SET";
            try
            {
                merge_into_markableset_instruction = (String) currentNode.getAttributes().getNamedItem("merge_into_markableset_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }
            
            String point_to_markable_instruction = "POINT TO MARKABLE";
            try
            {
                point_to_markable_instruction = (String) currentNode.getAttributes().getNamedItem("point_to_markable_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            String remove_pointer_to_markable_instruction = "REMOVE POINTER TO MARKABLE";
            try
            {
                remove_pointer_to_markable_instruction = (String) currentNode.getAttributes().getNamedItem("remove_pointer_to_markable_text").getNodeValue();
            }
            catch (java.lang.Exception ex)
            {
                //
            }

            
            UIMATypeMapping currentAttributeMapping = null;
            
            if (currentNode.getAttributes().getNamedItem("uima_type_mapping") != null)
            {
            	currentAttributeMapping = new UIMATypeMapping(currentNode.getAttributes().getNamedItem("uima_type_mapping").getNodeValue());
            	System.err.println(currentAttributeMapping.toString());
            }
            else
            {
            	currentAttributeMapping = new UIMATypeMapping("","");
            }                       
            
            /* Generate one MMAX2Attribute object for each */
            currentAttribute = new MMAX2Attribute(currentNode.getAttributes().getNamedItem("id").getNodeValue(),currentNode.getAttributes().getNamedItem("name").getNodeValue(),typeInt,currentNode.getChildNodes(), this, labelLength, toolTipText, hintText, lineWidth, color, lineStyleInt, maxSize, targetDomain, add_to_markableset_instruction, remove_from_markableset_instruction, adopt_into_markableset_instruction, merge_into_markableset_instruction,point_to_markable_instruction, remove_pointer_to_markable_instruction,fontSize,dashed,attributeToShowInPointerFlag,currentAttributeMapping);
            
            /* Map currentLevel to its ID */
            attributesByID.put(currentAttribute.getID(), currentAttribute);
            
            /* Map currentLevel to its (lower-cased) attribute name */
            attributesByLowerCasedAttributeName.put(currentNode.getAttributes().getNamedItem("name").getNodeValue().toLowerCase(),currentAttribute);
            attributes.add(currentAttribute);
            currentAttribute = null;
            size++;
        }
        
        for (int z=0;z<attributes.size();z++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(z);           
            MMAX2Attribute[] allDependentAttributes = currentAttribute.getDirectlyDependentAttributes();
            for (int b=0;b<allDependentAttributes.length;b++)
            {
                allDependentAttributes[b].addDependsOn(currentAttribute);
            }
        }        
        
        attributepanel = new MMAX2AttributePanel(this);
        attributepanel.create();            
    }
    
    public UIMATypeMapping[] getAllUIMAAttributeMappings()
    {
    	ArrayList tempResult = new ArrayList();
    	// Iterate over all attributes defined for this scheme (i.e. level)
    	for (int z=0;z<attributes.size();z++)
    	{
    		MMAX2Attribute currentAttribute = (MMAX2Attribute) attributes.get(z);
    		if (currentAttribute.getUIMATypeMapping()!=null)
    		{
    			tempResult.add(currentAttribute.getUIMATypeMapping());
    		}
    	}    	
    	return (UIMATypeMapping[])tempResult.toArray(new UIMATypeMapping[0]);
    }
    
    public static String readHTMLFromFile(String file)
    {
        String result="";
        BufferedReader abbrevReader = null;
        FileInputStream inStream = null;
        try
        {                
            inStream = new FileInputStream(file);
            abbrevReader = new BufferedReader(new InputStreamReader(inStream));//,"windows-1252"));
        }
        catch (java.io.FileNotFoundException ex)
        {
            System.err.println("Error: Couldn't find file "+file);
            return "File "+file+" could not be found!";
        }           
        String currentLine="";
        try
        {
            while((currentLine=abbrevReader.readLine())!=null)
            {
                result=result+"\n"+currentLine;
            }                        
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        try
        {
            abbrevReader.close();
        }
        catch (java.lang.Exception ex)
        {
            
        }
        return result;
    }
    
    public UIMATypeMapping getUIMATypeMapping()
    {
    	return uimaTypeMapping;
    }
    
    public final void setAttributePanelContainer(MMAX2AttributePanelContainer _container)
    {
        attributepanel.setAttributePanelContainer(_container);
    }

    public String getSchemeFileName()
    {
        return schemeFileName;
    }    
    
    public final void showAnnotationHint(String hint, boolean _lock, String _att)
    {      
        if (mmax2!=null && mmax2.getCurrentDiscourse().getCurrentMarkableChart().attributePanelContainer.getUseAnnotationHint())
        {
            if (hintLocked)
            {
                // The currently displayed hint is locked
                // So a normal hint should not be displayed unless it locks as well
                if (_lock)
                {
                    // The new hint is to be locked
                    // If the new one is the same as the currentlocked one, just unlock
                    if (_att.equals(currentAttributeHintedAt))
                    {
                        hintLocked = false;
                    }
                    else
                    {
                        // We want to lock another one
                        getCurrentAttributePanel().getContainer().hintToFront.setSelected(true);
                        mmax2.showAnnotationHint(hint,_att+" (locked)");
                        getCurrentAttributePanel().getContainer().hintToFront.setSelected(false);
                        hintLocked = true;
                        currentAttributeHintedAt = _att;
                    }
                }
            }
            else
            {
                // The current hint is not locked
                if (_lock)
                {
                    getCurrentAttributePanel().getContainer().hintToFront.setSelected(true);
                    mmax2.showAnnotationHint(hint,_att+" (locked)");
                    getCurrentAttributePanel().getContainer().hintToFront.setSelected(false);
                    hintLocked = true;
                    currentAttributeHintedAt = _att;
                }
                else
                {
                    mmax2.showAnnotationHint(hint,_att);
                    currentAttributeHintedAt = _att;                                        
                }
            }
        }
    }

    public final void hideAnnotationHint()
    {
        if (mmax2 != null && !hintLocked && mmax2.getCurrentDiscourse().getCurrentMarkableChart().attributePanelContainer.getUseAnnotationHint())
        {
            mmax2.hideAnnotationHint();
        }
    }

    public final void annotationHintToFront()
    {
        if (mmax2 != null)
        {
            mmax2.annotationHintToFront();
        }
    }

    public final void annotationHintToBack()
    {
        if (mmax2 != null)
        {
            mmax2.annotationHintToBack();
        }
    }
    
///////////      
    
    public MMAX2Attribute[] getAttributesByType(int type)
    {
        ArrayList tempresult = new ArrayList();
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getType()==type)
            {
                tempresult.add(currentAttribute);
            }
        }
        MMAX2Attribute realresult[] = new MMAX2Attribute[tempresult.size()];
        for (int z=0;z<tempresult.size();z++)
        {
            realresult[z] = (MMAX2Attribute) tempresult.get(z);
        }
        return realresult;        
    }

    public MMAX2Attribute getUniqueAttributeByType(int type)
    {
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getType()==type)
            {
                return currentAttribute;
            }
        }
        return null;
    }
    
    
    
    public MMAX2Attribute[] getAttributesByType(int type1, int type2)
    {
        ArrayList tempresult = new ArrayList();
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getType()==type1 || currentAttribute.getType()==type2)
            {
                tempresult.add(currentAttribute);
            }
        }
        MMAX2Attribute realresult[] = new MMAX2Attribute[tempresult.size()];
        for (int z=0;z<tempresult.size();z++)
        {
            realresult[z] = (MMAX2Attribute) tempresult.get(z);
        }
        return realresult;        
    }

    public MMAX2Attribute getUniqueAttributeByType(int type1, int type2)
    {
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getType()==type1 || currentAttribute.getType()==type2)
            {
                return currentAttribute;
            }
        }
        return null;
    }
    
    
    public MMAX2Attribute[] getAttributesByName(String nameRegExp)
    {
        ArrayList tempResult = new ArrayList();
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getDisplayAttributeName().toLowerCase().matches(nameRegExp))
            {
            	tempResult.add(currentAttribute);
            }
        }
        MMAX2Attribute realresult[] = new MMAX2Attribute[tempResult.size()];
        for (int z=0;z<tempResult.size();z++)
        {
            realresult[z] = (MMAX2Attribute) tempResult.get(z);
        }
        return realresult;        
    }

    public MMAX2Attribute getUniqueAttributeByName(String nameRegExp)
    {
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            if (currentAttribute.getDisplayAttributeName().toLowerCase().matches(nameRegExp))
            {
            	return currentAttribute;
            }
        }
     return null;        
    }
    

            
    public MMAX2Attribute[] getAttributesByNameAndType(String name, int type)
    {        
    	ArrayList tempResult = new ArrayList();
        MMAX2Attribute[] temp = getAttributesByType(type);
        for (int b=0;b<temp.length;b++)
        {
            if (temp[b].getDisplayAttributeName().toLowerCase().matches(name))
            {
            	tempResult.add(temp[b]);
            }
        }
        MMAX2Attribute realresult[] = new MMAX2Attribute[tempResult.size()];
        for (int z=0;z<tempResult.size();z++)
        {
            realresult[z] = (MMAX2Attribute) tempResult.get(z);
        }
        return realresult;        
    }

    public MMAX2Attribute getUniqueAttributeByNameAndType(String name, int type)
    {        
        MMAX2Attribute[] temp = getAttributesByType(type);
        for (int b=0;b<temp.length;b++)
        {
            if (temp[b].getDisplayAttributeName().toLowerCase().matches(name))
            {
            	return temp[b];
            }
        }
        return null;
    }
    
    
    public MMAX2Attribute[] getAttributesByNameAndType(String name, int type1, int type2)
    {        
    	ArrayList tempResult = new ArrayList();
        MMAX2Attribute[] temp = getAttributesByType(type1, type2);
        for (int b=0;b<temp.length;b++)
        {
            if (temp[b].getDisplayAttributeName().toLowerCase().matches(name))
            {
            	tempResult.add(temp[b]);
            }
        }
        MMAX2Attribute realresult[] = new MMAX2Attribute[tempResult.size()];
        for (int z=0;z<tempResult.size();z++)
        {
            realresult[z] = (MMAX2Attribute) tempResult.get(z);
        }
        return realresult;        
    }

    public MMAX2Attribute getUniqueAttributeByNameAndType(String name, int type1, int type2)
    {        
        MMAX2Attribute[] temp = getAttributesByType(type1, type2);
        for (int b=0;b<temp.length;b++)
        {
            if (temp[b].getDisplayAttributeName().toLowerCase().matches(name))
            {
            	return temp[b];
            }
        }
        return null;
    }
    
    
    ///// 
    
    
    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
    }
    
    /** This method produces an ArrayList of those MMAX2Attributes in annotation scheme order that do not depend
        on any other attribute. Independence is determined by checking if the attributes's dependsOn list is empty. */
    public final ArrayList getIndependentAttributes(boolean enable)
    {
        // Create list to accept result
        ArrayList tempresult = new ArrayList();       
        MMAX2Attribute currentAttribute=null; 
        
        // Iterate over all Attributes defined in this scheme, in annotation scheme order
        for (int p=0;p<attributes.size();p++)
        {
            // Get current attribute
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            
            /* Reset to Default (this will also set 'empty' for relations now!)*/
            currentAttribute.toDefault();
            
            if (currentAttribute.isIndependent())
            {
                if (enable)
                {
                    // Enable if required
                    currentAttribute.setEnabled(!(currentAttribute.getIsReadOnly()));
                }
                // Add at any rate to list of independent attributes
                tempresult.add(currentAttribute);
            }
        }
        return tempresult;
    }
    
    public MMAX2AttributePanel getAttributePanel()
    {
        return attributepanel;
    }
    
    /** This method is called from the mmaxattribute callingAttribute when the user changed 
        some value on it by clicking a button or changing the selection in a listbox. 
        This method also calls itself recursively. */
    public void valueChanged(MMAX2Attribute callingAttribute, MMAX2Attribute topCallingAttribute, MMAX2Attribute[] oldRemovedAttributes, int position, ArrayList requestedAttributesAsList)
    {                
        // If the call to this method occurs, some actual change has happened
        // So set attribute window to dirty
        attributepanel.setHasUncommittedChanges(true);
        // Any callingAttribute has been set to frozen == false at this point
        MMAX2Attribute[] removedAttributes = null;
        
        // Check if oldRemovedAttributes have been handed in
        if (oldRemovedAttributes != null)
        {            
            // Yes, so we are in a recursion
            removedAttributes = oldRemovedAttributes;
        }
        else
        {
            // There is no recursion yet
            // Remove and store those Attributes (if any) from display that depended on the one whose 
            // value was changed.
            // Some or all of these attributes may be valid again, because they can be either 
            // _identical_ to ones that are requested, or they may be different, but cover the same 
            // attributes. 
            // Something can be removed only if current attribute is branching
            if (callingAttribute.getIsBranching())
            {        
                // This removes all directly and indirectly dependent attributes
                removedAttributes = attributepanel.removeTrailingDependentAttributes(callingAttribute);
            }
        }
        
        // Get SchemeLevels that have to be displayed as a result of click, but do not reset them to 
        // default, because this could alter the values of removedLevels.
        // New: This now also covers MARKABLE_POINTER relations
        MMAX2Attribute[] requestedAttributes = callingAttribute.getNextAttributes(false);        
                                            
        if (removedAttributes != null && requestedAttributes.length != 0)
        {
            // Some levels were added and some were removed */
            // So transfer those selections in removedLevels that are valid for requestedLevels to the latter. */
            // There may be references to identical objects in the two arrays, if a level is removed and added at the same time. */
            // Frozen levels will be catered for in mapSelections !!
            mapSelections(removedAttributes, requestedAttributes);   
            
            // Now, attributes in requestedAttributes have either been set to the value they had in 
            // removedAttributes, or to the value some attribute of the same name in removedAttributes 
            // had or to default, in case either the setting failed or the requestedAttribute was not 
            // in removedAttributes at all, in which case it was defaulted from the beginning.            
            
            // The markable may still have some (temporarily undefined or 'extra' attributes which could now be applicable                                    
            // The current markable may have values for the requested levels which are NOT in the set retrieved above
            // Iterate over all requestedAttributes. These might also have been in removedAttributes
            for (int o=0;o<requestedAttributes.length;o++)
            {
                // Get current requested attribute
                MMAX2Attribute currentSchemeLevel = (MMAX2Attribute) requestedAttributes[o];
                // Get current markable value from Markable's attribute collection (may be different 
                // from Attribute window!!)
                String currentMarkableValue = attributepanel.currentMarkable.getAttributeValue(currentSchemeLevel.getLowerCasedAttributeName());
                
                if (currentMarkableValue != null && currentMarkableValue.equals("")==false)
                {
                    // The currently selected Markable has some non-null value for the current attribute
                    // This can only be the case if the user earlier opted to keep some invalid value,
                    // and if the attribute was not also removed:
                    // I.E.: An attribute becomes newly available for which the markable still has some old value
                    // This can be an attribute that is still available after value selection
                    if (currentSchemeLevel.isDefined(currentMarkableValue)==false)
                    {
                        
                        // Remove mechanism for keeping invalid values
                        // New strategy: Just overwrite
                        currentSchemeLevel.setIsFrozen(false,"");                                                
                        
                    }// isDefined==false
                    else
                    {
                        // The current markable has a value which is defined for the current 
                        // Attribute, but which was not set from removedLevels
                        if (currentSchemeLevel.oldValue.equals("")==false)
                        {
                            currentSchemeLevel.setSelectedValue(currentSchemeLevel.oldValue, true);
                            currentSchemeLevel.oldValue="";
                        }
                        else
                        {
                            currentSchemeLevel.setSelectedValue(currentMarkableValue, true);                            
                        }
                    }
                }
            }// for Requestedlevels                                    
        }
        else if (removedAttributes != null)
        {
            // Levels are removed, but none are requested (so nothing to be added later) 
            // Make sure requested is valid
            requestedAttributes = new MMAX2Attribute[0];

            // The following is obsolete in the new strategy, since
            // frozen attributes are not possible any more (in new strategy)
            
            // Make sure frozen ones among the removed ones are retained            
            for (int p=0;p<removedAttributes.length;p++)
            {                
                if (((MMAX2Attribute) removedAttributes[p]).getIsFrozen())
                {
                    System.err.println("Frozen: "+((MMAX2Attribute) removedAttributes[p]).getDisplayAttributeName());
                    if (attributepanel.keepables.contains(((MMAX2Attribute) removedAttributes[p]).getLowerCasedAttributeName())==false)
                    {
                        attributepanel.keepables.add(((MMAX2Attribute) removedAttributes[p]).getLowerCasedAttributeName());
                    }
                    System.err.println("Keeping in keepables. Size:"+attributepanel.keepables.size());
                }
            }                
        }
        else if (requestedAttributes != null)
        {
            // Levels are requested, but none are removed                                        
            // The current markable may have values for the requested levels which are NOT in the set retrieved above
            for (int o=0;o<requestedAttributes.length;o++)
            {
                MMAX2Attribute currentSchemeLevel = (MMAX2Attribute) requestedAttributes[o];
                String currentMarkableValue = attributepanel.currentMarkable.getAttributeValue(currentSchemeLevel.getLowerCasedAttributeName());
                if (currentMarkableValue != null && currentMarkableValue.equals("")==false) // && currentMarkableValue.equalsIgnoreCase(this._attributeWindow.currentMarkable.getDefaultValue())==false)
                {
                    //hasValue
                    // The currently selected Markable has some non-null value for the current attribute
                    if (currentSchemeLevel.isDefined(currentMarkableValue)==false)
                    {
                        
                        // Remove mechanism for keeping invalid values
                        // New strategy: Just overwrite
                        currentSchemeLevel.setIsFrozen(false,"");
                        
                    }// isDefined==false
                    else
                    {
                        // The current markable has a value which is defined for the current SchemeLevel, but which was not set from removedLevels
                        currentSchemeLevel.setSelectedValue(currentMarkableValue, true);
                    }
                }//HasValue
            }// for Requestedlevels            
        }// requestedLevels != null
        else
        {
            // Nothing was requested or removed
            // Do this to also set callingAttribute.currentIndex
            callingAttribute.setSelectedIndex(position);
        }
                 
        if (requestedAttributesAsList.size()==0)
        {
            // There was no recursion yet, so simply add current requested attributes at top
            // of list of attributes to display
            requestedAttributesAsList.addAll(java.util.Arrays.asList(requestedAttributes));
        }
        else
        {
            // There already is a recursion, so add current requested attributes directly after
            // attribute that triggered the recursion, i.e. callingAttribute
            int movingIndex = 0;
            for (int o=0;o<requestedAttributes.length;o++)
            {
                if (requestedAttributesAsList.contains(requestedAttributes[o])==false)
                {
                    requestedAttributesAsList.add(requestedAttributesAsList.indexOf(callingAttribute)+movingIndex+1, requestedAttributes[o]);       
                    movingIndex++;
                }
            }
        }
        
        //for (int n=0;n<requestedAttributesAsList.size();n++)
        for (int n=0;n<requestedAttributes.length;n++)
        {
            //MMAX2Attribute current = (MMAX2Attribute) requestedAttributesAsList.get(n);
            MMAX2Attribute current = requestedAttributes[n];
            if (current.getIsBranching() && current.getNextAttributes(false).length !=0)
            {
                valueChanged(current,topCallingAttribute,removedAttributes,current.getSelectedIndex(),requestedAttributesAsList);
            }
        }
                
        // Now, add attributes below the one whose value was changed 
        attributepanel.addAttributesAfter(((MMAX2Attribute[])requestedAttributesAsList.toArray(new MMAX2Attribute[0])),topCallingAttribute);                
        try
        {
            /* A change has occurred, which must be applicable and undoable */ 
            if (attributepanel.getContainer().isAutoApply() == false)
            {
                attributepanel.getContainer().setApplyEnabled(true);
                attributepanel.getContainer().setUndoEnabled(true);
            }
            else
            {
                System.out.println("---> Auto-Apply!");
                // NEW 25th February 2005: Always clear keepables on apply
                attributepanel.keepables.clear();
                attributepanel.setMarkableAttributes(attributepanel.currentMarkable,true);
                // NEW: FIX 'dirty after auto-apply'
                attributepanel.currentMarkable.getMarkableLevel().setIsDirty(true,true);
            }
            attributepanel.rebuild();
        }
        catch (java.lang.NullPointerException ex)
        {    
            
        }
        
        attributepanel.getScrollPane().scrollRectToVisible(attributepanel.getLastAttribute().getVisibleRect());
        attributepanel.invalidate();
        attributepanel.repaint();
        attributepanel.getContainer().invalidate();
        attributepanel.getContainer().repaint();
    }// end Method    
    
    /** This method transfers those selections in removedLevels that are valid for requestedLevels to the latter. 
        If a requestedLevel cannot be set according to a removedLevel, it is set to default. 
        // wrong: This method will not alter attributes of type MARAKABLE_SET!
        New: removed levels of type MARKABLE_POINTER and MARKABLE_SET are now correctly mapped / removed
        The two parameter arrays may contain references to the same objects ! */
    public void mapSelections(MMAX2Attribute[] removedLevels, MMAX2Attribute[] requestedLevels)  
    {  
        int remLen = removedLevels.length;
        int reqLen = requestedLevels.length;
        
        MMAX2Attribute currentRequestedLevel = null;
        String currentRequestedAttribute = "";
        MMAX2Attribute currentRemovedLevel = null;        
        String currentRemovedAttribute = "";   
        String currentRemovedValue = "";   
        boolean found = false;
        
        /* Iterate over all requested SchemeLevels, which have NOT been set to default yet. */
        for (int z=0;z<reqLen;z++)
        {
            /* Get current requestedLevel */
            currentRequestedLevel = requestedLevels[z];
            
            /* Iterate over all removed SchemeLevels */
            for(int u=0;u<remLen;u++)
            {
                /* Get current removedLevel */
                currentRemovedLevel = removedLevels[u];                
                
                if (currentRemovedLevel.getID().equals(currentRequestedLevel.getID()))
                {
                    /* The same (identical) SchemeLevel has been removed and requested at the same time */
                    /* Do nothing, most recent value will be kept (somehow) */
                    // Set preferred selected value
                    currentRemovedLevel.oldValue=currentRemovedLevel.getSelectedValue();
                    found = true;
                }                
                if (currentRemovedLevel.getIsFrozen()) 
                {
                    System.err.println("Frozen: "+currentRemovedLevel.getDisplayAttributeName());
                    if (attributepanel.keepables.contains(currentRemovedLevel.getLowerCasedAttributeName())==false) 
                    {
                        attributepanel.keepables.add(currentRemovedLevel.getLowerCasedAttributeName());
                        System.err.println("Keeping in keepables. Size:"+attributepanel.keepables.size());
                    }
                }                
            }// for all removed levels
            
            /* Here, all removed levels have been checked against the current requestedLevel */
            if (found == false)
            {
                /* The current requested level is NOT removed at the same time. */
                /* Check if there is a level with the same _attribute name_ removed at the same time. */
                
                /* Again iterate over all removed SchemeLevels */
                for(int u=0;u<remLen;u++)
                {
                    /* Get current removedLevel */
                    currentRemovedLevel = removedLevels[u];
                
                    /* Get Attribute on current requestedLevel */
                    currentRequestedAttribute = currentRequestedLevel.getLowerCasedAttributeName();
                    /* Get Attribute on current removedLevel */
                    currentRemovedAttribute = currentRemovedLevel.getLowerCasedAttributeName();
                                                                    
                    if(currentRequestedAttribute.equals(currentRemovedAttribute))
                    {
                        /* We are about to remove a level which has the attribute name of the current requested one, but is NOT identical. */
                        /* Check whether its value can be copied to requestedLevel */
                        /* This is true if the value in removedLevel exists in requestedLevel */
                        currentRemovedValue = currentRemovedLevel.getSelectedValue();
                        /* Try to copy the value from removed to requested level */                        
                        // oldValue has preference over currentRemovedValue, because current could be 'none' or some other meta-value that we do not want to map

                        if (currentRequestedLevel.oldValue.equals("")==false)
                        {
                            // currentRequestedlevel has a preferred reset value, so try to set that. If it fails, currentRequested will be set to default
                            currentRequestedLevel.setSelectedValue(currentRequestedLevel.oldValue,true);
                        }
                        /* If this does fail, currentRequestedLevel keeps its former value */
                        else if (currentRequestedLevel.setSelectedValue(currentRemovedValue,true)==false)
                        {
                            /* Copying fails if the value is not valid for requested  */
                            // Leave current requested value as it was (will be set to default by above call)
                            // Store currentRemovedValue as preferred default for current removed level
                            currentRemovedLevel.oldValue=currentRemovedValue;
                        }
                        else
                        {
                            // setSelectedValue succeeded 
                            currentRemovedLevel.oldValue = "";
                        }                        
                    }                
                }// for u (all removed levels, 2nd iteration)
            }// found == false
        }// for z (all requested levels )  
    }// end method
   
    public void addValueIDToAttributeIDMapping(String valueID, String attributeID)
    {
        this.valueIDsToAttributeIDs.put(valueID, attributeID);
    }
    
    
    public void resetAllAttributes()
    {
        for (int z=0;z<this.size;z++)
        {
            ((MMAX2Attribute) this.attributes.get(z)).toDefault();
            ((MMAX2Attribute) this.attributes.get(z)).setIsFrozen(false,"");
        }               
    }
    /** This method resets the AttributeWindow to the initial state as defined in this AnnotationScheme */
    public void reset()
    {
        attributepanel.removeAllAttributes();
        resetAllAttributes();        
        MMAX2Attribute[] attributes = (MMAX2Attribute[])getInitialAttributes().toArray(new MMAX2Attribute[0]);        
        attributepanel.addAttributes(attributes);             
    }
    
    public void setEnabled(boolean status)
    {
        MMAX2Attribute currentAttribute; 
        for (int p=0;p<attributes.size();p++)
        {
            currentAttribute = (MMAX2Attribute) attributes.get(p);
            currentAttribute.setEnabled(status);
        }
    }

    /** This method is used to create the list of attributes to be applied to new markables. It also
        supplies the attributes to be displayed in the attribute panel if no markable is selected. It
        returns the list of independent attributes with default values, expanded to also contain
        all dependent attributes. */
    public ArrayList getInitialAttributes()
    {
        // Get list of independent attributes in scheme order
        ArrayList independentAttributes = getIndependentAttributes(false);
        // The list now contains all independent attributes, and nothing else
        
        for (int z=0;z<independentAttributes.size();z++)
        {
            // Get current attribute
            MMAX2Attribute currentAttribute = (MMAX2Attribute) independentAttributes.get(z);
            // Get array of attributes dependent on current one (may be empty)
            MMAX2Attribute[] dependentAttributes = currentAttribute.getNextAttributes(true);
            // If there are any dependent attributes
            if (dependentAttributes.length > 0)
            {
                // Iterate over dependet attributes
                for (int b=0;b<dependentAttributes.length;b++)
                {
                    // Only if not already contained (possible?)
                    if (independentAttributes.contains(dependentAttributes[b])==false)
                    {
                        // Add directly after dependee, in 'next' order
                        independentAttributes.add(z+1+b, dependentAttributes[b]);
                    }
                }
            }
        }
        return independentAttributes;
    }
    
    /** This method returns an array of MMAX2Attribute objects reflecting the attributes of Markable markable.
        If Markable does not have any attributes, or an error occured, only independent Attributes with default 
        values are returned. This method handles branching independent attributes as well, and sets all defaults! 
        This method is the central instance for enforcing annotation scheme constraints! */
    public MMAX2Attribute[] getAttributes(Markable markable)
    {
        /* Copy attributes of current Markable. Processed attributes are incrementally removed from this list. */
        /* This contains ALL attributes found in the XML representation, incl. relations. */
        HashMap tempattribs = new HashMap(markable.getAttributes());        
        
        MMAX2Attribute currentAttribute = null;
        String currentAttributeString = "";
        String currentAttributeValue = "";
        
        // Get list of independent attributes in scheme order
        ArrayList independentAttributesAsList = getIndependentAttributes(true);        
        // The list now contains all independent attributes, and nothing else
        // All other attributes will be added to this list!
        
        /* Iterate over all independent Attributes */
        /* Markable _must_ have a value for each of it (at least default) */
        for (int z=0;z<independentAttributesAsList.size();z++)            
        {
            // Get next independent Attribute
            currentAttribute = (MMAX2Attribute)independentAttributesAsList.get(z);
                        
            // Get matchable name of its attribute
            currentAttributeString = currentAttribute.getLowerCasedAttributeName();     
            
            // Get value of current attribute from Markable to be displayed. This will work because markable attributes and values are all lower case
            currentAttributeValue = markable.getAttributeValue(currentAttributeString);
            
            // Null will be returned if the attribute does not exist on the XML markable yet
            if (currentAttributeValue == null) 
            {
                // The required independent attribute is not yet available on markable
                // Retrieve default value
                if (currentAttribute.getType() == AttributeAPI.MARKABLE_SET || currentAttribute.getType() == AttributeAPI.MARKABLE_POINTER)
                {                    
                    // For relation attributes, this means the relation is empty
                    // So use 'empty' default value
                    currentAttributeValue = MMAX2.defaultRelationValue;
                }
                else
                {
                    // Else empty simply means empty
                    currentAttributeValue = "";
                }
            }
            
            /* Try to set Attribute according to Markable's value for this attribute. */
            /* If this fails, the value is illegal/invalid for this attribute, and it will be set to default. */
            // If the markable has the _mmax default value_ here, this will be treated as default as well, and 
            // will always succeed
            // This then simply means that the attribute was not yet present on the Markable
            
            // Use ignore = true to prevent execution of valueChanged
            if (currentAttribute.setSelectedValue(currentAttributeValue,true)==false)
            {                    
                int result = -1;
                /* Attribute value on markable is invalid for this Attribute */
                // This means that Markable had a non-null value to which the attribute could _not_ be set !!
                // Note: This can never fail for relations, because relations cannot have value constraints!   
                // It can neither fail for freetext attributes
                // This can only happen if the markable was never selected before and received an invalid
                // value externally, or if it was selected earlier, but the user prompted to keep the invalid
                // value. At any rate, invalid values can ONLY be introduced externally!
                String message ="";
                if (currentAttribute.getIsReadOnly() == false)
                {            
                    message = "Error on level '"+markable.getMarkableLevelName()+"': Value '"+currentAttributeValue.toLowerCase() +"' is undefined for attribute '"+currentAttribute.getDisplayAttributeName()+"'!";
                    message = message + "\nDetails:\nString: "+markable.toString()+"\nSpan: "+MarkableHelper.getSpan(markable)+"\nFile: "+markable.getMarkableLevel().getMarkableFileName();
                    message = message + "\nIt is recommended that you check your annotation!";
                    message = message + "\n\nSelect 'Overwrite' to discard the invalid value!";
                    message = message + "\nSelect 'Keep' to keep it!";
                    
                    Object[] options = { "Overwrite with default", "Keep invalid value" };
                    result = JOptionPane.showOptionDialog(null, message, "MMAX2: Annotation inconsistency!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[1]);
                    
                    if (result == 1 || result == JOptionPane.CLOSED_OPTION)
                    {
                        // The user opted to keep the invalid value, so freeze level to prevent overwriting by default value
                        currentAttribute.setIsFrozen(true,currentAttributeValue);
                    }
                    else
                    {
                        // Unfreeze (even if not frozen before)
                        currentAttribute.setIsFrozen(false,"");
                        // currentAttribute is defaulted still, since setValue did not change this status
                    }
                }
                else
                {
                    // Read-only
                    message = "Error on read-only level '"+markable.getMarkableLevelName()+"': Value '"+currentAttributeValue.toLowerCase() +"' is undefined for attribute '"+currentAttribute.getDisplayAttributeName()+"'!";
                    message = message + "\nDetails:\nString: "+markable.toString()+"\nSpan: "+MarkableHelper.getSpan(markable)+"\nFile: "+markable.getMarkableLevel().getMarkableFileName();                    
                    message = message + "\nIt is recommended that you check your annotation!";
                    Object[] options = { "Keep invalid value" };
                    result = JOptionPane.showOptionDialog(null, message, "MMAX2: Annotation inconsistency (initial attributes)!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);                    
                    // In read-only state, annotation cannot be changed, so freeze to enforce that in the attribute window
                    currentAttribute.setIsFrozen(true,currentAttributeValue);
                }
            } // setSelectedValue failed
            
            // Now, current attribute in independentAttributesAsList is either set correctly, 
            // or to default if above failed, or frozen
            // From the markables's attributes, remove the one just processed
            tempattribs.remove(currentAttributeString); // is lower case already
            
            // Check if the current independent attribute points to some other attributes which have to be added as well
            if (currentAttribute.getIsBranching() && ((MMAX2Attribute[])currentAttribute.getNextAttributes(false)).length !=0)
            {
                // The current initial attribute does point to some other attributes
                // Get all of them as a list
                ArrayList addees = new ArrayList(java.util.Arrays.asList(currentAttribute.getNextAttributes(false)));
                // Iterate over list of dependent attributes
                for (int t=0;t<addees.size();t++)                
                {
                    // Get next dependent attribute
                    MMAX2Attribute currentDependentAttribute = (MMAX2Attribute)addees.get(t);
                    if (independentAttributesAsList.contains(currentDependentAttribute)==false)
                    {
                        // Add to result in next-span order, and directly *under* branching attribute
                        // This will increase the length of initialAttributesAsList and thus consider dependencies
                        // from attributes added here as well!
                        independentAttributesAsList.add(z+1+t, currentDependentAttribute);
                    }
                }
            }// the current attribute is not branching or does not point to anything in its current value            
        }// for z; end iteration over all initial attribute
        
        // At this point, all initial Attributes are processed, and contained in 
        // initialAttributesAsList
        
        // Convert into array
        MMAX2Attribute[] result = (MMAX2Attribute[]) independentAttributesAsList.toArray(new MMAX2Attribute[independentAttributesAsList.size()]);
        
        // Now tempattribs, the hash with all attributes carried by the markable, should be empty
        Iterator allAttributes = ((Set)tempattribs.keySet()).iterator();        
        String currentValue = "";
        String extraAttributesMessage = "";
        
        /* Iterate over all remaining attributes */
        while (allAttributes.hasNext())
        {                
            // Get name of current remaining attribute
            currentAttributeString = (String) allAttributes.next();
            // Get attribute itself
            MMAX2Attribute currentMMAX2Attribute = (MMAX2Attribute) attributesByLowerCasedAttributeName.get(currentAttributeString);
            // Get type of current attribute
            int currentAttributeType = -1;
            // But only if one was found
            if (currentMMAX2Attribute != null)
            {
                currentAttributeType = currentMMAX2Attribute.getType();
            }
            
            if ((tempattribs.get(currentAttributeString))==null ||
                 tempattribs.get(currentAttributeString).equals(""))
            {
                // If the value the markable has is empty, simply remove without asking
                markable.getAttributes().remove(currentAttributeString);
                continue;
            }
            else if (tempattribs.get(currentAttributeString).equals("empty"))
            {
                // The value is 'empty', which is special for relation attributes
                if (currentAttributeType == AttributeAPI.MARKABLE_POINTER || currentAttributeType == AttributeAPI.MARKABLE_SET)
                {
                    // If the value the markable has is 'empty', simply remove without asking
                    markable.getAttributes().remove(currentAttributeString);
                    continue;                    
                }
            }            
            /* There is a non-relation attribute remaining (span and id were not there anyway) */
            if (currentMMAX2Attribute != null)
            {
                /* It is defined somewhere in the scheme, but invalid here (possibly 'from the future') */
                /* So give a message and opt for deletion if suppress is false */
                if (attributepanel.suppressCheck.isSelected()==false)
                {              
                    String tempval = "";
                    if (markable.isDefined(currentAttributeString)) 
                    {
                        tempval = " ("+markable.getAttributeValue(currentAttributeString)+")";
                    }
                    else
                    {
                        tempval = " ()";
                    }                    
                    
                    String message = "Attribute "+currentAttributeString+ tempval+" on level "+markable.getMarkableLevelName()+" is not defined for \nthe current annotation status of this markable!";
                    message = message + "\nDetails:\nString: "+markable.toString()+"\nSpan: "+MarkableHelper.getSpan(markable)+"\nFile: "+markable.getMarkableLevel().getMarkableFileName();
                    message = message + "\n\nPress 'Remove' to remove the attribute permanently!";
                    message = message + "\nPress 'Keep' to leave markable unaltered!";                        
                    message = message + "\n\nUse 'suppress check' to suppress messages of this type in the future!";
                                        
                    Object[] options = { "Remove", "Keep" };
                    
                    int resultval = JOptionPane.showOptionDialog(null, message, "MMAX2: Annotation inconsistency!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[1]);
                    
                    if (resultval != 1 && resultval != JOptionPane.CLOSED_OPTION)
                    {
                        if (currentAttributeType == AttributeAPI.MARKABLE_SET)
                        {
                            // The user opted to remove from the current markable an attribute which is of type 
                            // markable set                            
                            MarkableChart chart = attributepanel.getContainer().getMMAX2().getCurrentDiscourse().getCurrentMarkableChart();
                            // Get relation object pertaining tom it
                            MarkableRelation relation = currentMMAX2Attribute.getMarkableRelation();
                            // Get set from which to remove the markable
                            MarkableSet setToLeave = relation.getMarkableSetWithAttributeValue(markable.getAttributeValue(currentAttributeString));
                            // Remove markable
                            chart.removeMarkableFromMarkableSet(markable, setToLeave, false);
                            attributepanel.getContainer().getMMAX2().requestRefreshDisplay();
                        }                        
                        else if (currentAttributeType == AttributeAPI.MARKABLE_POINTER)
                        {
                            // The user opted to remove from the current markable an attribute which is of type 
                            // markable pointer
                            MarkableChart chart = attributepanel.getContainer().getMMAX2().getCurrentDiscourse().getCurrentMarkableChart();
                            MarkableRelation relation = currentMMAX2Attribute.getMarkableRelation();
                            chart.removeMarkablePointerWithSourceMarkable(markable,relation,false);
                            attributepanel.getContainer().getMMAX2().requestRefreshDisplay();
                        }                            
                        else // freetext or nominal
                        {
                            // Simply remove 
                            markable.getAttributes().remove(currentAttributeString);
                        }
                    }// deletion requested by user                         
                    else
                    {
                        //  The attribute 'from the future' is to be kept
                        attributepanel.keepables.add(currentAttributeString);
                        System.err.println("Preserving "+currentAttributeString+" in keepables. Size:"+attributepanel.keepables.size());
                    }
                }
                else
                {
                    // SuppressCheck is true. What to do with any found illegal attributes? Keep
                    attributepanel.keepables.add(currentAttributeString);                 
                    System.err.println("Preserving "+currentAttributeString+" in keepables. Size:"+attributepanel.keepables.size());
                }
            }
            else
            {
                // The current remaining attribute is not defined anywhere in the AnnotationScheme
                // So keep for later display
                
                // New: Ignore new system attribute mmax_level here
                if (currentAttributeString.equalsIgnoreCase("mmax_level")==false)
                {
                    extraAttributesMessage = extraAttributesMessage + "\n"+currentAttributeString+" ("+tempattribs.get(currentAttributeString)+")";
                }
            }
        }//while hasNext    
        
        if (extraAttributesMessage.equals("") == false && attributepanel.warnOnExtraAttributes.isSelected())
        {
            extraAttributesMessage = "The following undefined attributes were\nfound on the current Markable:\n"+extraAttributesMessage;
            JOptionPane.showMessageDialog(null,extraAttributesMessage,"MMAX2: Potential annotation inconsistency!",JOptionPane.WARNING_MESSAGE);
        }
        return result; 
    }   
    
    public boolean isDefined(String attributename)
    { 
        return this.attributesByLowerCasedAttributeName.containsKey(attributename);
    }
        
    public int getAttributeTypeByAttributeName(String attribute)
    {
        int type = 0;
        if (attribute.equals("Markable"))
        {
            type = AttributeAPI.FREETEXT;     
        }
        else if (attribute.equalsIgnoreCase("markable_text"))
        {
            type = MMAX2Constants.MARKABLE_TEXT;
        }
        else if (attribute.equalsIgnoreCase("base_level"))
        {
            type = MMAX2Constants.BASE_LEVEL;
        }
        else if (attribute.equalsIgnoreCase("level_name"))
        {
            type = MMAX2Constants.LEVEL_NAME;
        }        
        else
        {
            MMAX2Attribute currentAttribute = (MMAX2Attribute) this.attributesByLowerCasedAttributeName.get(attribute.toLowerCase());
            if (currentAttribute != null) type = currentAttribute.getType();
        }
        return type;
        
    }
    
    public ArrayList getAllAttributeNames()
    {
        ArrayList resultlist = new ArrayList();
        Enumeration allAttribs = attributesByLowerCasedAttributeName.keys();
        while (allAttribs.hasMoreElements())
        {
            resultlist.add((String) allAttribs.nextElement());
        }
        return resultlist;
    }
    

    /** This method returns an ArrayList of all distinct DisplayAttributeNames (cased!), irrespective of their SchemeLevels. */
    public ArrayList getAllDistinctDisplayAttributeNames_()
    {
        ArrayList result = new ArrayList();
        int attCount = this.attributes.size();
        for ( int u=0;u<attCount;u++)
        {
            String currentName = ((MMAX2Attribute)attributes.get(u)).getDisplayAttributeName();
            if (result.contains(currentName)==false)
            {
                result.add(currentName);
            }
        }
        return result;            
    }
    
    public final MMAX2AttributePanel getCurrentAttributePanel()
    {
        return this.attributepanel;
    }
    
    public final int getSize()
    {
        return size;
    }
    public final ArrayList getAttributes()
    {
        return attributes;
    }
    public final MMAX2Attribute getAttributeByID(String id)
    {
        return (MMAX2Attribute)this.attributesByID.get(id);
    }
    
    public final String[] getAttributeNamesByType(int type)
    {        
    	ArrayList temp = new ArrayList();
    	MMAX2Attribute[] attributes = getAttributesByType(type);    	
        for (int z=0;z<attributes.length;z++)
        {
        	temp.add(attributes[z].getDisplayAttributeName());
        }
        return (String[]) temp.toArray(new String[0]);
    }
    
    /** This method returns a list of attribute names for which *all* the values in valueList are defined,
        or empty list. valueList is a comma-separated list of values, and the entire list is enclosed in 
        curly braces. optionalAttributeName is either empty or the name of the attribute for which the values
        are required to be defined. This method does not return freetext attributes except for the one given in
        optionalAttributeName. If the name of an attribute was passed in, and if this attribute is of type freetext
        it will be contained in the result list, with a * prepended to its name. */
    public final ArrayList getAttributeNamesForValues(String valueList, String optionalAttributeName)
    {
        ArrayList result = new ArrayList();
        ArrayList allValues = new ArrayList();
        // Trim off leading and trailing { and }
        valueList = valueList.substring(1,valueList.length()-1);
        StringTokenizer toki = new StringTokenizer(valueList,",");
        // Iterate over all values in list
        while(toki.hasMoreTokens())
        {
            // Get current value, and copy to list
            allValues.add(toki.nextToken());            
        }
        
        String currentValue="";
        // Now iterate over all attributes
        for (int z=0;z<attributes.size();z++)
        {
            // Get current attribute
            MMAX2Attribute currentAttribute = (MMAX2Attribute) attributes.get(z);
            
            if (currentAttribute.getType() == AttributeAPI.FREETEXT && 
                currentAttribute.getLowerCasedAttributeName().equalsIgnoreCase(optionalAttributeName)==false)
            {
                // Skip freetext attributes unless we know that the current one is the required one
                continue;
            }
            // Assume that current value is defined for current attribute
            boolean currentDefined=true;
            // Iterate over all values
            for (int n=0;n<allValues.size();n++)
            {
                // Get current value
                currentValue = (String) allValues.get(n);
                
                if (currentAttribute.isDefined(currentValue)==false)
                {
                    // If one is undefied, skip checking all the others
                    currentDefined=false;
                    break;
                }
            }
            if (currentDefined)
            {
                // The current attribute has all supplied values defined for it
                if (currentAttribute.getType() == AttributeAPI.FREETEXT==false)
                {
                    // If the current attribte is not freetext, store its normal name once
                    if (result.contains(currentAttribute.getLowerCasedAttributeName())==false)
                    {
                        result.add(currentAttribute.getLowerCasedAttributeName());
                    }
                }
                else
                {
                    // If the current attribute is freetext, store  * + its name 
                    if (result.contains("*"+currentAttribute.getLowerCasedAttributeName())==false)
                    {
                        result.add("*"+currentAttribute.getLowerCasedAttributeName());
                    }                    
                }
            }
        }        
        return result;
    }
    
    
    public final void destroyDependentComponents()
    {
        attributesByID.clear();
        attributesByID = null;
        attributesByLowerCasedAttributeName.clear();
        attributesByLowerCasedAttributeName = null;
        for (int z=0;z<attributes.size();z++)
        {
            ((MMAX2Attribute) attributes.get(z)).destroy();
        }
        attributes.clear();
        attributes = null;
    }
    
}
