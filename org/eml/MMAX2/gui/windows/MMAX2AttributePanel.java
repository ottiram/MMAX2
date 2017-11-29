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

package org.eml.MMAX2.gui.windows;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.annotation.scheme.MMAX2AnnotationScheme;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2AttributePanel extends JPanel
{
    private JScrollPane pane = null;    
    
    static String VersionString = "MMAX2 port V0.5";
    public MMAX2AnnotationScheme scheme;    
    
    private MMAX2AttributePanelContainer container = null;
        
    public JCheckBox suppressCheck;        
    public JCheckBox warnOnExtraAttributes;
        
    public Markable currentMarkable;
    public ArrayList lastValidAttributes=null;
    
    public ArrayList keepables;
        
    /** Indicates if the currently loaded annotation contains unsaved changes.
        This field must be set by the attribute window's setDirty(value) method, which is called by the main application whenever changes to the annotation occur.
        It is read by the main application when the user tries to close the application in order to determine if the user has to be prompted for saving.
        Therefore, make sure to set this field to TRUE whenever permanent changes to the annotation (i.e. Markable's attributes) occurr! */
    public boolean hasUncommittedChanges=false;
    
    /** Indicates the absolute path to the directory in which the .anno file resides. This path is needed to access the scheme file in the .create() method.
        DO NOT MODIFY! */
    public static String MMAXDataPath = "";
    
    /** This field holds a clone of the currently displayed Markable with its values unchanged by any attribute window manipulations that occured since it was 
        selected by the user. Use this field to implement attribute manipulation undo. Attributes of type ID (i.e. Member and Pointer) are removed from this clone 
        directly after creation. They are not needed for undo functionality, because attributes of type ID are NEVER altered through AttributeWindow interaction. */
        
    public JPanel modifiablePanel;    
           
    private String oneClickAttributeName = "";
    
    public MMAX2AttributePanel(MMAX2AnnotationScheme _scheme)
    {        
        super();
        setAlignmentX(JPanel.LEFT_ALIGNMENT);
        setLayout(new FlowLayout(FlowLayout.LEADING));
        scheme = _scheme;     
    }
    
    public final void setScrollPane(JScrollPane _pane)
    {
        pane = _pane;
    }
    
    public final JScrollPane getScrollPane()
    {
        return pane;
    }
    
    public final void setAttributePanelContainer(MMAX2AttributePanelContainer _container)
    {
        container = _container; 
    }
    
    public final void setOneClickAnnotationAttributeName(String _name)
    {
        oneClickAttributeName = _name;
    }
    
    public final String getOneClickAnnotationAttributeName()
    {
        return oneClickAttributeName;
    }
    
    /** This method is called from the main application if the dirty status of the annotation changes.
        The implementing method must set the value of the Dirty field (as defined in PluggableAttributeWindow) appropriately. 
        In addition, measures must be implemented to inform the user on the current status. 
        Note: The AttributeWindow must also call MMAX.setSaveButtonEnabled(newStatus), in order for the save button in the MMAX file
        menu to reflect the current dirty status !! */
    public final void setHasUncommittedChanges(boolean newStatus)
    {
        if (newStatus)
        {
            if (currentMarkable!=null) container.setTitle(currentMarkable.toString()+" [dirty]");
        }
        else
        {
            if (currentMarkable!=null) 
            {
                container.setTitle(currentMarkable.toString());
            }
            else
            {
                container.setTitle("");
            }
        }
        hasUncommittedChanges = newStatus;       
    }
    
    /** This method returns an ArrayList of all attributes defined in the schemefile 
        This method is called in the context of Kappa calculation. */ 
    public final ArrayList getAllAttributes()
    {
        return scheme.getAllAttributeNames();
    }
    
    /** This method is called by the main application to inform the AttributeWindow that the display has been built. 
        This method can implement any changes it wants to make to the display, e.g. in terms of markable colouring. */
    public void displayInitComplete()
    {
        
    }
        
    /** This method creates an attribute window from the given schemefile. */
    public final void create()
    {    
        final MMAX2AnnotationScheme schemeCopy = scheme;
        keepables = new ArrayList();
        
        JPanel staticOuterPanel = new JPanel();
        
        staticOuterPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        Box staticOuterBox = Box.createVerticalBox();
        
        suppressCheck = new JCheckBox("Suppress check");
        if (MMAX2.getStandardFont() != null)
        {
            suppressCheck.setFont(MMAX2.getStandardFont().deriveFont((float)9));        
        }
        suppressCheck.setToolTipText("Suppresses consistency check for CURRENTLY invalid attributes when selecting a Markable!");
        suppressCheck.setSelected(true);
        suppressCheck.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    schemeCopy.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    schemeCopy.annotationHintToBack();
                }
            });        
        
        warnOnExtraAttributes = new JCheckBox("Warn on extra attributes");
        if (MMAX2.getStandardFont() != null)        
        {
            warnOnExtraAttributes.setFont(MMAX2.getStandardFont().deriveFont((float)9));
        }
        warnOnExtraAttributes.setToolTipText("Displays a message if undefined extra attributes are found on a Markable!");
        warnOnExtraAttributes.setSelected(true);
        warnOnExtraAttributes.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    schemeCopy.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    schemeCopy.annotationHintToBack();
                }
            });        
        
        Box outerButtonBox = Box.createVerticalBox();                
        Box smallBox = Box.createHorizontalBox();        
        
        smallBox.add(suppressCheck);
        smallBox.add(warnOnExtraAttributes);
        outerButtonBox.add(smallBox);
                
        MMAX2Attribute initialAttributes[] = (MMAX2Attribute[]) scheme.getInitialAttributes().toArray(new MMAX2Attribute[0]);        
        lastValidAttributes = new ArrayList();       
        
        modifiablePanel = new JPanel();
        Box tempbox = Box.createHorizontalBox();        
        Box AttributeBox = Box.createVerticalBox();
                
        for (int u=0;u<initialAttributes.length;u++)
        {
            AttributeBox.add(initialAttributes[u]);
        }
        
        tempbox.add(AttributeBox);
        
        modifiablePanel.add(tempbox);
        staticOuterBox.add(modifiablePanel);
        staticOuterBox.add(outerButtonBox);
        
        staticOuterPanel.add(staticOuterBox);
        staticOuterPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        add(staticOuterPanel);      
    }// method create

    public final void setEnabled(boolean status)
    {
        this.scheme.setEnabled(status);
    }
    
    public final String[] getAllNominalMMAX2AttributeNames()
    {
        String[] allButtons = scheme.getAttributeNamesByType(AttributeAPI.NOMINAL_BUTTON);
        String[] allLists = scheme.getAttributeNamesByType(AttributeAPI.NOMINAL_LIST);
        String[] allNominals = new String[allButtons.length + allLists.length];
        System.arraycopy(allButtons,0, allNominals, 0, allButtons.length);
        System.arraycopy(allLists,0, allNominals, allButtons.length, allLists.length);
        java.util.Arrays.sort(allNominals);
        return allNominals;
        
    }
    
    /** This method displays the attributes of the supplied markable, by retrieving from the underlying AnnotationScheme 
        the correct MMAX2Attribute objects. This method is called whenever a Markable gets selected in the MMAX display.*/
    public final void displayMarkableAttributes (Markable markable)
    {                   
        // Init list of illegal attributes to be kept during validation  
        keepables = new ArrayList();
        
        if (markable == null)
        {
            /* A null Markable has been selected */
            /* This is a way to set the AttributeWindow to default, e.g. before attributes for a newly created Markable are set automatically. */
            /* Reset display to initial status, i.e. display independent attributes with default values. */
            scheme.reset();
            /* Reset window title */
            container.setTitle("<nothing selected>");
            /* Reset buttons */
            setApplyEnabled(false);
            setUndoEnabled(false);
            scheme.setEnabled(false);
            
            /* Refresh attribute window component */
            rebuild();            
            return;
        }        

        // The method was called with a non-null markable
        // Set text to title
        container.setTitle(markable.toString());
        // Prior to any display, reset all attributes to default, to prevent non-default values on children 
        // when parent attributes are changed
        scheme.resetAllAttributes();
                
        /* Sth., i.e. a non-null Markable, has been selected */
        // Store a reference to it for use in UNDO
        currentMarkable = markable;                
        
        /* Get MMAX2Attributes describing the attributes of selected Markable */
        /* If Markable doesn't have any yet, defaulted independentAttributes are returned */
        MMAX2Attribute[] currentAttributes = scheme.getAttributes(markable);        
        /* Clear display completely */
        removeAllAttributes();
        
        /* Add either Attributes for markable, or default ones */
        addAttributes(currentAttributes);
        
        lastValidAttributes = getAllCurrentAttributes();
        
        /* Enable JRadioButtons to allow modifications */
        scheme.setEnabled(true);
        rebuild();
        if (container.isToFront())  
        {
            container.toFront();
        }
        setApplyEnabled(false);
        setUndoEnabled(false);
        
        // Do this at any rate. setMarkableAttributes will prevent frozen SchemeLevels from being applied and those in keepables to be removed 
        setMarkableAttributes(markable,false);            
        
    }// method displayMarkableAttributes

    /** This method returns a hashmap representing a 'snapshot' of the currently displayed attributes and their values. 
        Attribute-Value pairs for attributes of type ID are NOT contained in this snapshot, because they cannot 
        be modified via the AttributeWindow GUI.
        New: Relations are returned as well
        Both attribute and value strings are set to lowercase */
    public final HashMap getAttributeValuePairs()
    {
        HashMap result = new HashMap();
        MMAX2Attribute currentAttribute = null;
        
        Box tempbox = (Box) this.modifiablePanel.getComponent(0);
        Box attributebox = (Box) tempbox.getComponent(0);
        
        /* Get number of SchemeLevel objects in Box */
        int compCount = attributebox.getComponentCount();
        
        String currentAttributeString = "";
        String currentValue = "";
        
        /* Iterate over all available SchemeLevels */
        for (int e=0;e<compCount;e++)
        {
            /* Get SchemeLevel object at current position */
            currentAttribute = (MMAX2Attribute) attributebox.getComponent(e);
            if (currentAttribute.getType()== AttributeAPI.MARKABLE_SET) continue;
            /* Get current Attribute */
            currentAttributeString = currentAttribute.getLowerCasedAttributeName();
            /* Get current value */
            currentValue = currentAttribute.getSelectedValue();
            /* Add attribute-value pair to new Attributes */
            result.put(new String(currentAttributeString), new String(currentValue.toLowerCase()));            
        }        
        return result;        
    }
    
    /** Returns an ArrayList of all MMAX2Attribute objects currently displayed on this panel, 
        incl. member and pointer. */
    public final ArrayList getAllCurrentAttributes()
    {
        ArrayList result = new ArrayList();
        MMAX2Attribute currentAttribute = null;
        
        Box tempbox = (Box) modifiablePanel.getComponent(0);
        Box attributebox = (Box) tempbox.getComponent(0);
        
        /* Get number of MMAX2Attribute objects in Box */
        int compCount = attributebox.getComponentCount();
                
        /* Iterate over all available MMAX2Attribute objects */
        for (int e=0;e<compCount;e++)
        {
            /* Get Attribute object at current position */
            currentAttribute = (MMAX2Attribute) attributebox.getComponent(e);
            result.add(currentAttribute);
        }        
        return result;        
    }
    
    
    /** This method applies the attributes and values which are currently displayed in the AttributeWindow to Markable markable.
        It does so by creating a new Attributes HashMap, filling it with the displayed attributes and values, 
        and assigning it to markable. Values of ID-type attributes member and pointer are not changed. 
        When this method is called, BackupMarkable can be ignored. */
    public final void setMarkableAttributes (Markable markable, boolean rerender)    
    {                           
        boolean change = false;
        
        // Create empty hashmap to accept attributes to be assigned to markable.Attributes 
        HashMap newAttributes = new HashMap();
        
        // Create clone of those attributes the markable to be changed currently has, to be able to retain 
        // undefined extra attributes. This will store member and pointer attribs as well!
        HashMap oldAttributes = new HashMap(markable.getAttributes());
                
        // Get current state of AttributeWindow, incl. *all* types of attributes
        ArrayList currentAttributes = getAllCurrentAttributes();
        MMAX2Attribute currentAttribute = null;
        ArrayList frozenNames = new ArrayList();
        
        /* Iterate over all MMAX2Attributes from AttributeWindow, to be applied to markable */
        for (int z=0;z<currentAttributes.size();z++)
        {
            // Get current attribute object
            currentAttribute= (MMAX2Attribute)currentAttributes.get(z);
            
            // NEW: Do not skip relation-attributes any more                        
            // Do not copy attributes from frozen levels to new attribute set
            // NEW February 25th, 2005: No frozen attributes any more
            if (currentAttribute.getIsFrozen()) 
            {
                System.out.println("Skipping frozen "+currentAttribute.getDisplayAttributeName());
                // Store name of frozen attribute so it can be kept 
                frozenNames.add(currentAttribute.getLowerCasedAttributeName());
                continue;
            }
                        
            // At this point, all unfrozen levels are available 
            /* Get current attribute name */
            String currentAttributeString = currentAttribute.getLowerCasedAttributeName();
            /* Get value of current attribute */
            String currentValue = currentAttribute.getSelectedValue();
            /* Add attribute-value pair to new Attributes, in lower case! */            
            newAttributes.put(new String(currentAttributeString.toLowerCase()), new String(currentValue.toLowerCase()));
            /* Try to remove processed attribute from old attributes */
            /* If this fails, a new attribute was assigned, and thus a change occurred */
            // I.e. an attribute was applied to the current markable for the first time
            if (oldAttributes.remove(currentAttributeString)==null) change = true;
        }                        

        /* At this point, all currently displayed attributes (except for frozen ones) have been processed. */
        /* There may be a rest of older attributes, which in case they clash have to be removed, or which are otherwise retained. */
        /* Get all those attributes that belonged to the old markable, but are not valid for current Attribute window setting  */
        Iterator allAttributeStrings = ((Set)oldAttributes.keySet()).iterator();
        String retainedExtraAttributes = "";
        
        /* Iterate over all remaining attributes. Those are attributes which have been found on the current 
           Markable, but which could not be found on the currently valid levels. */
        while (allAttributeStrings.hasNext())
        {
            // Get name of next remaining attribute
            String currentAttributeString = (String) allAttributeStrings.next();
            // Get next attribute itself
            if (frozenNames.contains(currentAttributeString))
            {
                // Copy old att-val pair of frozen attribute to new attribute set
                newAttributes.put(new String(currentAttributeString.toLowerCase()), new String(((String) oldAttributes.get(currentAttributeString.toLowerCase()))).toLowerCase());
                continue;
            }
                        
            /* If some attribute made it here, it may be legal */
            /* So keep it, unless it clashes with some defined attribute of the same name. */
            if (scheme.isDefined(currentAttributeString)==false) 
            {
                // currentAttribute is an extra attribute that does not clash with any defined one, so keep by adding it to newAttributes
                newAttributes.put(new String(currentAttributeString.toLowerCase()), new String(((String) oldAttributes.get(currentAttributeString.toLowerCase()))).toLowerCase());
                retainedExtraAttributes = retainedExtraAttributes+"\n"+currentAttributeString+"("+(String) oldAttributes.get(currentAttributeString)+")";
            }
            else
            {
                // The current attribute clashes with some defined one (it is from the future)
                // So keep only if it is in keepables, i.e. set of invalid attributes the user opted to keep
                // New February 25th: Do never keep until after setMarkableAttributes !
                if (keepables.contains(currentAttributeString))
                {
                    newAttributes.put(new String(currentAttributeString.toLowerCase()), new String(((String) oldAttributes.get(currentAttributeString.toLowerCase()))).toLowerCase());
                }
                else
                {
                    // There is a clash, and the current remaining attribute has to be removed without message
                    // Iterate over MMAX2Attribute objects
                    for (int u=0;u<lastValidAttributes.size();u++)
                    {
                        // Get current MMAX2Attribute
                        MMAX2Attribute currentCurrentAttribute = (MMAX2Attribute) lastValidAttributes.get(u);                        
                        // Check if its removal has consequences for any existing relations
                        if (currentCurrentAttribute.getLowerCasedAttributeName().equalsIgnoreCase(currentAttributeString))
                        {                            
                            // The current Attribute is the one to be removed                            
                            if (currentCurrentAttribute.getType()==AttributeAPI.MARKABLE_POINTER)
                            {
                                // The current attribute about to be removed is a markable_pointer relation
                                // That means that the current markable may be the source of a markable pointer,
                                // in which case the pointer has to be destroyed
                                MarkableRelation relation = currentCurrentAttribute.getMarkableRelation();
                                MarkablePointer pointer = relation.getMarkablePointerForSourceMarkable(currentMarkable);
                                if (pointer != null)
                                {
                                    // Destroy set only if current markable really was the source in one
                                    currentMarkable.getMarkableLevel().getCurrentDiscourse().getCurrentMarkableChart().removeMarkablePointerWithSourceMarkable(currentMarkable,relation,false);
                                    getContainer().getMMAX2().requestRefreshDisplay();                                    
                                }
                                else
                                {
                                    System.err.println("No value for markable_pointer "+currentAttributeString);
                                }
                            }                            
                            else if (currentCurrentAttribute.getType()==AttributeAPI.MARKABLE_SET)
                            {
                                MarkableRelation relation = currentCurrentAttribute.getMarkableRelation();
                                MarkableSet set = relation.getMarkableSetWithAttributeValue(currentMarkable.getAttributeValue(currentAttributeString));
                                if (set != null)                                
                                {
                                    currentMarkable.getMarkableLevel().getCurrentDiscourse().getCurrentMarkableChart().removeMarkableFromMarkableSet(currentMarkable,set,false);
                                    getContainer().getMMAX2().requestRefreshDisplay();                                                                        
                                }
                                else
                                {
                                    System.err.println("No value for markable_set "+currentAttributeString);
                                }                                
                            }                            
                        }
                    }
                }
            }
        }//while hasNext        
        
        /* Assign new, valid attributes to markable */
        markable.setAttributes(newAttributes);
        // Set Level to dirty, but only if a modification did indeed occur
        if (change) markable.getMarkableLevel().setIsDirty(true,false);
        
        setApplyEnabled(false);
        setUndoEnabled(false);        
        
        ArrayList visibleAttributes = getAllCurrentAttributes();
        for (int z=0;z<scheme.getSize();z++)
        {
            if (visibleAttributes.contains(((MMAX2Attribute) this.scheme.getAttributes().get(z)))==false)
            {
                ((MMAX2Attribute) scheme.getAttributes().get(z)).toDefault();
            }
        }
        
        if (rerender)
        {
            MMAX2Document doc = markable.getMarkableLevel().getCurrentDiscourse().getDisplayDocument();      
            doc.startChanges(markable);
            markable.renderMe(MMAX2Constants.RENDER_SELECTED);
            doc.commitChanges();
        }
    }

    public final void removeAllAttributes()
    {
        Box tempbox = (Box) modifiablePanel.getComponent(0);
        tempbox.removeAll();
    }
  
    public final void addAttributes(MMAX2Attribute[] attributes)
    {        
        Box tempbox = (Box) this.modifiablePanel.getComponent(0);
        Box schemebox = null;
        
        if (tempbox.getComponentCount() !=1)
        {
            schemebox = Box.createVerticalBox();
            schemebox.setBackground(Color.yellow);
            tempbox.add(schemebox);
        }
        else 
        {
            schemebox = (Box) tempbox.getComponent(0);
        }
                
        for (int z=0;z<attributes.length;z++)
        {            
            schemebox.add(attributes[z]);            
        }
        
        rebuild();
        container.invalidate();
    }

    public final void addAttributesAfter(MMAX2Attribute[] attributes, MMAX2Attribute leader)
    {        
        Box tempbox = (Box) modifiablePanel.getComponent(0);
        Box schemebox = null;
        
        if (tempbox.getComponentCount() !=1)
        {
            schemebox = Box.createVerticalBox();
            schemebox.setBackground(Color.yellow);
            tempbox.add(schemebox);
        }
        else 
        {
            schemebox = (Box) tempbox.getComponent(0);
        }
        
        int offset = 0;
        // Iterate over schemebox to find attribute insertion position
        for (int o=0;o<schemebox.getComponentCount();o++)
        {
            if (schemebox.getComponent(o)==leader)
            {
                offset = o;
                break;
            }
        }
        
        for (int z=0;z<attributes.length;z++)
        {
            schemebox.add(attributes[z],offset+z+1);
        }

        rebuild();
        container.invalidate();
    }
    
    public final MMAX2Attribute getLastAttribute()
    {
        Box tempbox = (Box) modifiablePanel.getComponent(0);
        Box schemebox = (Box) tempbox.getComponent(0);
        System.err.println(((MMAX2Attribute) schemebox.getComponent(schemebox.getComponentCount()-1)).getDisplayAttributeName());
        return (MMAX2Attribute) schemebox.getComponent(schemebox.getComponentCount()-1);
    }
    
    /** Removes and returns MMAX2Attributes dependent on lastAttribute, or empty list. */
    public final MMAX2Attribute[] removeTrailingDependentAttributes(MMAX2Attribute lastAttribute)
    {
        MMAX2Attribute currentAttribute = null;
        ArrayList removedAsList = new ArrayList();
        Box tempbox = (Box) modifiablePanel.getComponent(0);
        Box attributebox = (Box) tempbox.getComponent(0);
        
        /* Get number of MMAX2Attribute objects in Box */
        int compCount = attributebox.getComponentCount();
        // Iterate over all attributes in attributebox, backwards
        for (int e=compCount-1;e>=0;e--)
        {            
            /* Get attribute object at current position */
            currentAttribute = (MMAX2Attribute) attributebox.getComponent(e);
            if (currentAttribute.dependsOn(lastAttribute))
            {
                removedAsList.add(currentAttribute);
                attributebox.remove(e);
            }
        }        
        MMAX2Attribute[] result = (MMAX2Attribute[]) removedAsList.toArray(new MMAX2Attribute[removedAsList.size()]);        
        {
            container.repaint();
        }
        return result;        
    }
    
    public final void rebuild()
    {
        Box tempbox = (Box) this.modifiablePanel.getComponent(0);
        tempbox.repaint();
        modifiablePanel.repaint();
        repaint();
    }

    
    public final void setApplyEnabled(boolean status)
    {
        container.setApplyEnabled(status);
    }

    public final void setUndoEnabled(boolean status)
    {
        container.setUndoEnabled(status);
    }     
    
    public final MMAX2AttributePanelContainer getContainer()
    {
        return container;
    }
}