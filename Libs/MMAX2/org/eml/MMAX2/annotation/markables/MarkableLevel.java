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

package org.eml.MMAX2.annotation.markables;

// Nodes returned to StyleSheet
import java.awt.Color;
import java.awt.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xpath.NodeSet;
import org.eml.MMAX2.annotation.query.MMAX2MatchingCriterion;
import org.eml.MMAX2.annotation.query.MMAX2QueryException;
import org.eml.MMAX2.annotation.query.MMAX2QueryTree;
import org.eml.MMAX2.annotation.scheme.MMAX2AnnotationScheme;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.annotation.scheme.UIMATypeMapping;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.api.MarkableLevelAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import org.eml.MMAX2.discourse.MMAX2DiscourseElementSequence;
import org.eml.MMAX2.gui.display.MMAX2OneClickAnnotationSelector;
import org.eml.MMAX2.gui.display.MarkableLevelRenderer;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.gui.windows.MMAX2MarkableBrowser;
import org.eml.MMAX2.gui.windows.MMAX2MarkablePointerBrowser;
import org.eml.MMAX2.gui.windows.MMAX2MarkableSetBrowser;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class MarkableLevel implements java.awt.event.ActionListener, MarkableLevelAPI
{    
    /** Reference to the markableDOM that the markables on this layer come from. */    
    private DocumentImpl markableDOM;       
    
    /** Reference to the discourse this MarkableLayer belongs to. */
    private MMAX2Discourse currentDiscourse;    
    /** HashMap mapping all Markables in this layer to their IDs. */
    private HashMap markableHash;    
    /** Name of the markable xml file pertaining to this layer. */
    private String markableFileName="";    
    /** Name of the markable level (read from 'level' attribute). */
    private String markableLevelName="";    
    /** XML Header of the markable xml file pertaining to this layer. */
    private String markableFileHeader="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";    
    
    private String encoding = "UTF-8";
    /** HashMap which maps DE id string to arrays of markables associated with the DE. 
        Filled by this.registerMarkableAtDiscourseElement, used by getMarkablesAtDiscourseElement. */
    private String markableNameSpace="";
    private String dtdReference="<!DOCTYPE markables>";
    private HashMap markablesAtDiscourseElement;
    /** HashMap which maps DE id string to arrays of markables started by the DE. 
        Filled by this.registerMarkableAtDiscourseElement, used by getMarkablesStartedByDiscourseElement. */
    private HashMap startedMarkablesAtDiscourseElement;
    /** HashMap which maps DE id string to arrays of markables ended by the DE. 
        Filled by this.registerMarkableAtDiscourseElement, used by getMarkablesEndedByDiscourseElement. */
    private HashMap endedMarkablesAtDiscourseElement;    
    /** Array containing at index X an array of those Markables associated with the DE with discourse position x.
        Filled by this.createDisplayPositionToMarkableMapping, used by this.getMarkableAtDiscoursePosition. */
    private Markable[][] markablesAtDiscoursePosition;    
    /** Boolean variable reflecting the current activation status of this MarkableLayer. */
    private boolean active; 
    /** Boolean variable reflecting the current visibility status of this MarkableLayer. */
    private boolean visible;
    /** Current position (0 to len -1) of this MarkableLayer in current ordering in MarkableChart. */
    private int position;
    private JComboBox activatorComboBox = null;
    /** Arrow for moving this layer up in the hierarchy, appearing in MarkableLayerControlPanel. */
    private BasicArrowButton moveUp = null;
    /** Arrow for moving this layer down in the hierarchy, appearing in MarkableLayerControlPanel. */    
    private BasicArrowButton moveDown = null;
    
    private HashMap MarkableSetRelations = null;
    
    private HashMap MarkablePointerRelations = null;
    
    private JButton updateCustomization = null;
    private JButton validateButton = null;
    private JButton deleteAllButton = null;
    
    private JCheckBox switchCustomizations = null;
        
    private MarkableLevelRenderer renderer = null;
    
    private JLabel nameLabel = null;
    
    private String matchableLevelName="";
    private boolean hasHandles = false;   
    private MMAX2AnnotationScheme annotationscheme = null;
        
    private MMAX2 mmax2 = null;
    private boolean dirty = false;
    
    private JMenuItem saveMenuItem = null;
    
    private String customizationFileName="";
    
    private boolean readOnly = false;
    
    /** Creates new MarkableLevel */
    public MarkableLevel(DocumentImpl _markableDOM, String _markableFileName, String _markableLevelName, MMAX2AnnotationScheme _scheme, String _customizationFileName)
    {                        
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}
    	
        customizationFileName=_customizationFileName;
        matchableLevelName=","+_markableLevelName.toLowerCase()+",";        
         
        if (_markableDOM != null)
        {            
            // Start encding-handling
            encoding = _markableDOM.getEncoding();
            if (encoding == null)
            {
                encoding = "UTF-8";
            }
            if (encoding.equals("")==false)
            {
                markableFileHeader="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>";
            }            
            if (verbose) System.err.println("File header: "+markableFileHeader);
            // end encoding-handling
            
            markableDOM = _markableDOM;        
            
            // Check for name space availability
            try
            {
                // New March 30, 2005: Check if xmlns is available at all            
                if (markableDOM.getElementsByTagName("markables").item(0).getAttributes().getNamedItem("xmlns") == null)
                {
                    JOptionPane.showMessageDialog(null,"Missing name space declaration on markable level "+_markableLevelName+"!\nEvery markable level file MUST have a name space declaration!\nMarkables on this level will not work properly!\n\nPlease modify the file\n"+_markableFileName,"Missing name space declaration!",JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                JOptionPane.showMessageDialog(null,"Cannot access xmlns element in file "+_markableLevelName+"!\nProbably missing markables.dtd.","Cannot access name space!",JOptionPane.ERROR_MESSAGE);
            }

            // Try to set name space
            try
            {
                markableNameSpace = markableDOM.getElementsByTagName("markables").item(0).getAttributes().getNamedItem("xmlns").getNodeValue();
            }
            catch (java.lang.NullPointerException ex)
            {
                
            }
                
            
            // New March 30, 2005: Check if doctype is available at all            
            if (markableDOM.getDoctype() == null)
            {
                JOptionPane.showMessageDialog(null,"Missing DOCTYPE declaration on markable level "+_markableLevelName+"!\nEvery markable level file MUST have a DOCTYPE declaration!\nMarkables on this level will not work properly!\n\nPlease modify the file\n"+_markableFileName,"Missing DOCTYPE declaration!",JOptionPane.ERROR_MESSAGE);
            }
            else
            {            
                if (markableDOM.getDoctype().getPublicId() != null)
                {
                    dtdReference = "<!DOCTYPE markables PUBLIC \""+markableDOM.getDoctype().getPublicId()+"\">";
                }
                else if (markableDOM.getDoctype().getSystemId() != null)
                {
                    dtdReference = "<!DOCTYPE markables SYSTEM \""+markableDOM.getDoctype().getSystemId()+"\">";
                }
            }
        }
        
        markableFileName = _markableFileName;
        markableLevelName = _markableLevelName;
        annotationscheme = _scheme;
        markablesAtDiscourseElement = new HashMap();
        startedMarkablesAtDiscourseElement = new HashMap();
        endedMarkablesAtDiscourseElement = new HashMap();                   

        MarkableSetRelations = new HashMap();
        MarkablePointerRelations = new HashMap();        
        
        if (_markableLevelName.equalsIgnoreCase("internal_basedata_representation"))
        {
            return;
        }
        
        renderer = new MarkableLevelRenderer(this, customizationFileName);        
        
        // Init GUI elements
        moveUp = new BasicArrowButton(SwingConstants.NORTH);
        if (isDefined()==false)
        {
            moveUp.setEnabled(false);
        }
        moveUp.addActionListener(this);
        moveUp.setToolTipText("Move this level up in hierarchy");
        moveDown = new BasicArrowButton(SwingConstants.SOUTH);
        if (isDefined()==false)
        {
            moveDown.setEnabled(false);
        }        
        moveDown.addActionListener(this);           
        moveDown.setToolTipText("Move this level down in hierarchy");
        
        //activatorCheckBox = new JCheckBox();
        activatorComboBox = new JComboBox();
        activatorComboBox.setFont(activatorComboBox.getFont().deriveFont((float)10));
        activatorComboBox.setBorder(new EmptyBorder(0,0,1,1));
        
        if (isDefined()==false)
        {
            activatorComboBox.setEnabled(false);
        }
        
        activatorComboBox.setToolTipText("Activate/hide/deactivate this level");
        activatorComboBox.setActionCommand("activator");
        // Each MarkableLayer is active by default;
        active = true;
        visible = true;
        activatorComboBox.addItem("active");
        activatorComboBox.addItem("visible");
        activatorComboBox.addItem("inactive");
        activatorComboBox.addActionListener(this);
        
        nameLabel = new JLabel(markableLevelName);
        nameLabel.setOpaque(true);
        if (renderer.getForegroundIsTransparent()==false)
        {
            // Set foregroundcolor only if it is not transparent, otherwise black will be used
            nameLabel.setForeground(renderer.getForegroundColor());
        }
        else
        {
            nameLabel.setForeground(Color.black);
        }
        
        if (renderer.getBackgroundIsTransparent())
        {
            // If the background is transparent, set label background to white, which is transparent
            nameLabel.setBackground(Color.white);           
        }
        else
        {
            nameLabel.setBackground(renderer.getBackgroundColor());
        }        
        
        updateCustomization = new JButton("Update");
        updateCustomization.setFont(updateCustomization.getFont().deriveFont((float)10));
        updateCustomization.setBorder(new EmptyBorder(0,0,1,1));

        
        updateCustomization.setActionCommand("update");
        updateCustomization.addActionListener(this);        
        if (customizationFileName.equals(""))
        {
            updateCustomization.setEnabled(false);
        }
        else
        {   
            updateCustomization.setEnabled(true);
            updateCustomization.setToolTipText(customizationFileName);
        }
        
        if (isDefined()==false)
        {
            updateCustomization.setEnabled(false);
        }
                
        validateButton = new JButton("Validate");
        validateButton.setFont(validateButton.getFont().deriveFont((float)10));
        validateButton.setBorder(new EmptyBorder(0,0,1,1));
        
        if (isDefined()==false)
        {
            validateButton.setEnabled(false);
        }
        
        validateButton.setActionCommand("validate");
        validateButton.addActionListener(this);

        deleteAllButton = new JButton("Delete");
        deleteAllButton.setFont(deleteAllButton.getFont().deriveFont((float)10));
        deleteAllButton.setBorder(new EmptyBorder(0,0,1,1));
        
        if (isDefined()==false)
        {
            deleteAllButton.setEnabled(false);
        }
        
        deleteAllButton.setActionCommand("delete_all");
        deleteAllButton.addActionListener(this);
        
        
        switchCustomizations = new JCheckBox("");
        if (isDefined()==false)
        {
            switchCustomizations.setEnabled(false);
        }

        switchCustomizations.setActionCommand("switch");
        switchCustomizations.addActionListener(this);
        switchCustomizations.setToolTipText("Activate / deactivate markable customization for this level");
        if (customizationFileName.equals(""))
        {
            switchCustomizations.setEnabled(false);
        }
        else
        {
            switchCustomizations.setSelected(true);
            renderer.updateSimpleMarkableCustomizations(true);
        }
        
        saveMenuItem = new JMenuItem(markableLevelName);
        saveMenuItem.setFont(MMAX2.getStandardFont());
        saveMenuItem.addActionListener(this);
        saveMenuItem.setActionCommand("save_this_level");
        saveMenuItem.setEnabled(false);               
        
    }
    
/*    
    public HashSet getAllUIMATypeMappings()
    {
    	// Get mappings for all attributes in this level first
    	HashSet result = annotationscheme.getAllUIMAAttributeMappings();
    	// Add level-level mapping as well
    	if (uimaTypeMapping != null)
    	{
    		result.add(uimaTypeMapping);
    	}
    	return result;
    }
*/    
    public final boolean getIsReadOnly()
    {
        return readOnly;
    }
    
    public final void setIsReadOnly(boolean status)
    {
        readOnly = status;
        
    }    
    
    public final UIMATypeMapping getUIMATypeMapping()
    {
    	return annotationscheme.getUIMATypeMapping();
    }

    
    public final String getCustomizationFileName()
    {
        return customizationFileName;
    }
    
    public final MMAX2AnnotationScheme updateAnnotationScheme()
    {
        String temp = annotationscheme.getSchemeFileName();
        annotationscheme=null;
        System.gc();
        annotationscheme = new MMAX2AnnotationScheme(temp);
        annotationscheme.setMMAX2(mmax2);
        return annotationscheme;        
    }
    
    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
        annotationscheme.setMMAX2(_mmax2);
    }
    
    public final boolean isDefined()
    {
        return markableDOM != null;
    }
    
    public final JMenuItem getSaveMarkableLevelItem()
    {
        return saveMenuItem;
    }
    
    public final boolean hasMarkableStartingAt(String deID)
    {
        return (startedMarkablesAtDiscourseElement.get(deID)!=null);
    }

    public final boolean hasMarkableEndingAt(String deID)
    {        
        return (endedMarkablesAtDiscourseElement.get(deID)!=null);
    }
    /*
    public final boolean hasMarkableAtSpan(String span)
    {
        System.err.println("Error: hasMarkableAtSpan is deprecated!");
        return false;        
    }
    */
    public final Markable getMarkableAtSpan(String span)
    {
        Markable result = null;
        Iterator allMarkables = markableHash.values().iterator();
        while (allMarkables.hasNext())
        {
            Markable temp =(Markable) allMarkables.next();
            if (span.equalsIgnoreCase(MarkableHelper.getSpan(temp)))
            {
                result = temp;
                break;
            }
        }                
        return result;
    }
    
    
    public final void setIsDirty(boolean status, boolean refresh)
    {        
        if (dirty != status)
        {
            dirty = status;        
            System.err.println("MarkableLevel "+markableLevelName+" set to dirty="+status);
            if (currentDiscourse.getHasGUI())
            {
                saveMenuItem.setEnabled(status);
            }
        }
        else
        {
            // The status to be set was already active
        }
        
        if (getCurrentDiscourse()!=null && getCurrentDiscourse().getMMAX2() != null)
        {
            if (currentDiscourse.getHasGUI())
            {
                getCurrentDiscourse().getMMAX2().updateIsAnnotationModified();
            }
        }
        
        if (currentDiscourse.getHasGUI())
        {
            ArrayList activeBrowsers = null;
            if (refresh)
            {
                // New: check for and update currently active markable browsers
                activeBrowsers = getCurrentDiscourse().getMMAX2().getMarkableBrowsersForMarkableLevel(markableLevelName);
                for (int z=0;z<activeBrowsers.size();z++)
                {
                    ((MMAX2MarkableBrowser)activeBrowsers.get(z)).refresh();
                }                
            }
            if (getCurrentDiscourse().getMMAX2() != null)
            {
                activeBrowsers = getCurrentDiscourse().getMMAX2().getMarkableSetBrowsersForMarkableLevel(markableLevelName);
                for (int z=0;z<activeBrowsers.size();z++)
                {
                    ((MMAX2MarkableSetBrowser)activeBrowsers.get(z)).update();//IfDisplaying(markableLevelName, affectedAttribute);
                }
            }
            if (getCurrentDiscourse().getMMAX2() != null)
            {
                activeBrowsers = getCurrentDiscourse().getMMAX2().getMarkablePointerBrowsersForMarkableLevel(markableLevelName);
                for (int z=0;z<activeBrowsers.size();z++)
                {
                    ((MMAX2MarkablePointerBrowser)activeBrowsers.get(z)).update();//IfDisplaying(markableLevelName, affectedAttribute);
                }
            }
            
        }        
    }
    
    public final boolean getIsDirty()
    {
        return dirty;
    }
    
    
    /** This method returns a list of attribute names for which *all* the values in valueList are defined,
        or empty list. valueList is a comma-separated list of values, and the entire list is enclosed in 
        curly braces. */
    public final ArrayList getAttributeNamesForValues(String valueList, String optionalAttributeName)
    {
        return annotationscheme.getAttributeNamesForValues(valueList, optionalAttributeName);
    }
    
    public final void setValidateButtonEnabled(boolean status)
    {
        validateButton.setEnabled(status);
    }
    
    public final void validate()
    {
        System.err.println("Validating "+markableHash.size() +" markables from MarkableLevel "+getMarkableLevelName());
        Iterator allMarkables = markableHash.values().iterator();
        Markable current = null;
        // Hide attribute window during validation
        getCurrentAnnotationScheme().getCurrentAttributePanel().getContainer().setVisible(false);
        // Iterate over all Markables on this level
        while (allMarkables.hasNext())
        {
            // Get next Markable
            current =(Markable)allMarkables.next();
            // Leave validation and adding of defaults to Attribute Window
            getCurrentAnnotationScheme().getCurrentAttributePanel().displayMarkableAttributes(current);
        }
        getCurrentAnnotationScheme().getCurrentAttributePanel().getContainer().setVisible(true);
        getCurrentAnnotationScheme().getCurrentAttributePanel().displayMarkableAttributes(null);
    }
    
    public final void deleteAllMarkables()
    {
        ArrayList temp = new ArrayList();
        Iterator allMarkables = markableHash.values().iterator();
        while (allMarkables.hasNext())
        {
            temp.add((Markable)allMarkables.next());
        }
        allMarkables = null;
        
        for (int b=0;b<temp.size();b++)
        {
            deleteMarkable((Markable)temp.get(b));
        }
    }
    
    public final void deleteMarkable(Markable deletee)
    {                   
        // Get set relations deletee may be part of
        MarkableRelation[] currentRelations = getActiveMarkableSetRelationsForMarkable(deletee);
        // Iterate over all set relations
        for (int b=0;b<currentRelations.length;b++)
        {
            MarkableRelation relation = currentRelations[b];
            MarkableSet set = relation.getMarkableSetContainingMarkable(deletee);
            if (set!=null)
            {
                set.removeMarkable(deletee);
            }
        }
        
        // Get reference to root node
        Node root= markableDOM.getElementsByTagName("markables").item(0);
        // Remove deletee (as node) from this
        root.removeChild(deletee.getNodeRepresentation());
        // Remove deletee from markableHash, the sole markable repository
        markableHash.remove(deletee.getID());
        unregisterMarkable(deletee);
        if (getCurrentDiscourse().getHasGUI())
        {
            Integer[] positions = currentDiscourse.removeDisplayAssociationsForMarkable(deletee);
            if (positions.length != 0)
            {
                renderer.removeHandlesAtDisplayPositions(positions);        
            }       
            deletee.renderMe(MMAX2Constants.RENDER_REMOVED);
            // Destroy further references to deletee markable
            currentDiscourse.getMMAX2().setCurrentSecondaryMarkable(null);
            currentDiscourse.getMMAX2().getCurrentTextPane().setCurrentHoveree(null,0);            
        }                
        deletee = null;
        setIsDirty(true,true);
    }

    public final Markable addMarkable(String[] discourseElementIDs, HashMap attributes)
    {
    	if (attributes == null) attributes = new HashMap();
    	ArrayList discourseElements = new ArrayList();
    	for (int n=0;n<discourseElementIDs.length;n++)
    	{
    		discourseElements.add(currentDiscourse.getDiscourseElementByID(discourseElementIDs[n]));
    	}
    	return addMarkable(discourseElements,attributes);
    }
    
    public final Markable addMarkable(ArrayList discourseElements, HashMap attributes)
    {
    	String[][] fragments = MarkableHelper.toFragments(discourseElements);
    	return addMarkable(fragments, attributes);
    }

    public final Markable addMarkable(String[][] fragments, HashMap attributes)
    {
        // Create new ID String 
        String id = currentDiscourse.getCurrentMarkableChart().getNextFreeMarkableID();
        // Get independent attribute with default values, incl. those dependent on default, and so on
        MMAX2Attribute[] mmaxAttributes = (MMAX2Attribute[])annotationscheme.getInitialAttributes().toArray(new MMAX2Attribute[0]);
        // Create node representation
        ElementImpl node =(ElementImpl) markableDOM.createElementNS(markableNameSpace,"markable");
        Node root= markableDOM.getElementsByTagName("markables").item(0);
        root.insertBefore((Node)node,root.getFirstChild());
        for (int i=0;i<mmaxAttributes.length;i++)
        {
            String currentAttrib = ((MMAX2Attribute)mmaxAttributes[i]).getLowerCasedAttributeName();
            if (attributes.containsKey(currentAttrib)==false)
            {
                attributes.put(new String(currentAttrib),new String(((MMAX2Attribute)mmaxAttributes[i]).getSelectedValue()));
                ((Element)node).setAttribute(new String(currentAttrib),new String(((MMAX2Attribute)mmaxAttributes[i]).getSelectedValue()));
            }
            else
            {
                // The supplied attributes (via create) have precedence over default ones
            }
        }
        ((Element)node).setAttribute(new String("id"),new String(id));
        // Create new markable object from above parameters
        Markable newMarkable = new Markable((Node)node,id,fragments,attributes,this);        
        markableHash.put(id, newMarkable);
        MarkableHelper.setDisplayPositions(newMarkable);
        
        for (int z=0;z<fragments.length;z++)
        {
            String[] currentFragment = fragments[z];
            for (int y=0;y<currentFragment.length;y++)
            {
                updateDiscoursePositionToMarkableMapping(fragments[z][y]);
            }
        }
        
        // Make sure Markable is displayed properly
        if (currentDiscourse.getHasGUI())
        {
            MMAX2Document doc =currentDiscourse.getDisplayDocument();
            doc.startChanges(newMarkable);               
            newMarkable.renderMe(MMAX2Constants.RENDER_UNSELECTED);
            doc.commitChanges();
        }
        // NEW: 
        setIsDirty(true,true);
        
        return newMarkable;
    }
    
    
    public final Markable addMarkable(String fragment)
    {
        // Create new ID String 
        String id = currentDiscourse.getCurrentMarkableChart().getNextFreeMarkableID();
        // Create fragments array of arrays from word4..word12; this is never discontinuous
        String[][] fragments = parseMarkableSpan(fragment,currentDiscourse.getWordDOM(),this);
        // Create and get attributes for new markable        
        HashMap attributes = new HashMap();
        // Get independent attribute with default values, incl. those dependent on default, and so on
        MMAX2Attribute[] mmaxAttributes = (MMAX2Attribute[])annotationscheme.getInitialAttributes().toArray(new MMAX2Attribute[0]);
        // Create node representation
        ElementImpl node =(ElementImpl) markableDOM.createElementNS(markableNameSpace,"markable");
        Node root= markableDOM.getElementsByTagName("markables").item(0);
        root.insertBefore((Node)node,root.getFirstChild());
        for (int i=0;i<mmaxAttributes.length;i++)
        {
            attributes.put(new String(((MMAX2Attribute)mmaxAttributes[i]).getLowerCasedAttributeName()),new String(((MMAX2Attribute)mmaxAttributes[i]).getSelectedValue()));
            ((Element)node).setAttribute(new String(((MMAX2Attribute)mmaxAttributes[i]).getLowerCasedAttributeName()),new String(((MMAX2Attribute)mmaxAttributes[i]).getSelectedValue()));
        }
        ((Element)node).setAttribute(new String("id"),new String(id));
        // Create new markable object from above parameters
        Markable newMarkable = new Markable((Node)node,id,fragments,attributes,this);        
        markableHash.put(id, newMarkable);
        MarkableHelper.setDisplayPositions(newMarkable);
        
        for (int z=0;z<fragments.length;z++)
        {
            String[] currentFragment = fragments[z];
            for (int y=0;y<currentFragment.length;y++)
            {
                updateDiscoursePositionToMarkableMapping(fragments[z][y]);
            }
        }
        
        // Make sure Markable is displayed properly
        if (currentDiscourse.getHasGUI())
        {
            MMAX2Document doc =currentDiscourse.getDisplayDocument();
            doc.startChanges(newMarkable);               
            newMarkable.renderMe(MMAX2Constants.RENDER_UNSELECTED);
            doc.commitChanges();
        } 
        setIsDirty(true,true);
        
        
        return newMarkable;
    }
            
    public final void saveMarkables(String newFileName)
    {
    	saveMarkables(newFileName, false);
    }
    
    public final void saveMarkables(String newFileName, boolean autoSaveMode)
    {
        if (getIsDirty()==false)
        {
        	if (autoSaveMode) System.err.print("Auto-Save: ");
            System.err.println("Markable level "+getMarkableLevelName()+" is clean, not saving!");
            return;
        }
        
        if (getIsReadOnly()==true)
        {
        	if (autoSaveMode)
        	{
        		System.err.println("Auto-Save: "+"Markable level "+getMarkableLevelName()+" is READ-ONLY, not saving!");        		
        	}
        	else
        	{
        		JOptionPane.showMessageDialog(null,"Markable level "+getMarkableLevelName()+" is READ-ONLY, not saving!","Save problem",JOptionPane.INFORMATION_MESSAGE);
        	}
            return;
        }
        
        if (autoSaveMode) System.err.print("Auto-Save: ");
        System.err.println("Saving level "+getMarkableLevelName()+" ... ");
        if (newFileName.equals("")==false)
        {
            markableFileName = newFileName;
        }
                
        /* Test file for existence */
        File destinationFile = new File(markableFileName);
        
        if(destinationFile.exists())
        {        	
        	// The file does exist already
        	// Since it exists but is read-only, it is protected and should not be writable
            if (destinationFile.canWrite() == false)
            {
            	if (autoSaveMode)
            	{
            		System.err.println("Auto-Save: "+"Cannot save markables on level "+getMarkableLevelName()+"!'Write' not allowed!");
            	}
            	else
            	{
            		JOptionPane.showMessageDialog(null,"Cannot save markables on level "+getMarkableLevelName()+"!\n'Write' not allowed!","Save problem:"+markableFileName,JOptionPane.WARNING_MESSAGE);	
            	}                
                return;
            }
        	
            /* The file to be written is already existing and writable, so create backup copy first*/
            /* This should be the normal case */
        	// 
            if (autoSaveMode) System.err.print("Auto-Save: ");
            System.err.println("Filename "+destinationFile.getAbsolutePath()+" exists, creating backup (.bak) file!");
            File oldDestinationFile = new File(this.markableFileName +".bak");
            if (oldDestinationFile.exists())
            {
                System.err.println("Removing old .bak file!");
                oldDestinationFile.delete();
                oldDestinationFile = new File(markableFileName +".bak");
            }
            destinationFile.renameTo(oldDestinationFile);
        }                   
        
	/* Write DOM to file */
        if (autoSaveMode) System.err.print("Auto-Save: ");
        System.err.println("Writing to file " + markableFileName);
        
        BufferedWriter fw = null;
        try
        {
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(markableFileName),this.encoding));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
        
        String rootElement ="";
        if (markableNameSpace.equals(""))
        {
            rootElement = "<markables>";
        }
        else
        {
            rootElement = "<markables xmlns=\""+markableNameSpace+"\">";
        }
                
        try
        {
            fw.write(markableFileHeader+"\n"+dtdReference+"\n"+rootElement+"\n");
            fw.flush();
        }
        catch (java.io.IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        Set allMarkableIDsSet = this.markableHash.keySet();
        System.err.println("Saving "+allMarkableIDsSet.size()+" markables");
        Iterator allMarkableIDs = allMarkableIDsSet.iterator();
        Markable currentMarkable = null;

        while(allMarkableIDs.hasNext())
        {
            currentMarkable = (Markable) markableHash.get(allMarkableIDs.next());
            try
            {
                fw.write(MarkableHelper.toXMLElement(currentMarkable)+"\n");
            }
            catch (java.io.IOException ex)
            {
                System.err.println("Error saving "+(currentMarkable.getID()));
            }
        }
        
        try
        {
            fw.write("</markables>");
            fw.close();
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
        }
        
        setIsDirty(false,false);        
    }
               
   /* 
    public final void saveTransposedMarkables(String oldLang, ArrayList absoluteWords)
    {       
        String newFileName = markableFileName;
        String fileNameToUse = newFileName.substring(0,newFileName.indexOf("_"));
        fileNameToUse=fileNameToUse+"_merged"+newFileName.substring(newFileName.indexOf("_"));

        System.err.println("Transposing and saving level "+markableLevelName+" ... ");
        newFileName=fileNameToUse;
        
        File destinationFile = new File(newFileName);
        
        //Test file for existence 
        if(destinationFile.exists())
        {
            // The file to be written is already existing, so create backup copy first
            // This should be the normal case 
            System.err.println("Filename "+destinationFile.getAbsolutePath()+" exists, creating backup (.bak) file!");
            File oldDestinationFile = new File(newFileName +".bak");
            if (oldDestinationFile.exists())
            {
                System.out.println("Removing old .bak file!");
                oldDestinationFile.delete();
                oldDestinationFile = new File(newFileName +".bak");
            }
            destinationFile.renameTo(oldDestinationFile);
        }
        
	// Write DOM to file 
        System.out.println("Writing transposed markables to file " + newFileName);
        
        BufferedWriter fw = null;
        try
        {
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFileName),this.encoding));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        String rootElement ="";
        
        if (markableNameSpace.equals(""))
        {
            rootElement = "<markables>";
        }
        else
        {
            rootElement = "<markables xmlns=\""+markableNameSpace+"_"+oldLang+"\">";
        }
        
        try
        {
            fw.write(markableFileHeader+"\n"+dtdReference+"\n"+rootElement+"\n");
            fw.flush();
        }
        catch (java.io.IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        Set allMarkableIDsSet = this.markableHash.keySet();
        System.err.println("Saving "+allMarkableIDsSet.size()+" transposed markables");
        Iterator allMarkableIDs = allMarkableIDsSet.iterator();
        Markable currentMarkable = null;
        //for (int z=0;z<num;z++)
        while(allMarkableIDs.hasNext())
        {
            currentMarkable = (Markable) markableHash.get(allMarkableIDs.next());
            try
            {
                fw.write(MarkableHelper.transposeMarkable(currentMarkable, oldLang, absoluteWords)+"\n");
            }
            catch (java.io.IOException ex)
            {
                System.out.println("Error saving "+(currentMarkable.getID()));
            }
        }
        
        try
        {
            fw.write("</markables>");
            fw.close();
        }
        catch (java.io.IOException ex)
        {
            System.out.println(ex.getMessage());
        }       
        
    }
    
    */
    
    /** This method initializes one MarkableRelation object for each attribute of type MARKABLE_SET, MARKABLE_POINTER and SET_POINTER and
     *  adds it to this MarkableLevel's MarkableRelations list. */
    public final void initMarkableRelations()
    {
        MMAX2Attribute[] currentAttributes = null;
        MMAX2Attribute currentAttribute = null;
        Markable currentMarkable = null;
        String currentAttributeName = null;
        String currentValue = "";
        
        // Get all Attributes of type MARKABLE_SET
        currentAttributes = this.annotationscheme.getAttributesByType(AttributeAPI.MARKABLE_SET);
        // Iterate over all Attributes of type MARKABLE_SET
        for (int i=0;i<currentAttributes.length;i++)
        {
            // Get current Attribute, e.g. member
            currentAttribute = (MMAX2Attribute) currentAttributes[i];
            currentAttributeName = currentAttribute.getLowerCasedAttributeName();
            // Create one MarkableRelation for each Attribute of type MARKABLE_SET (always order, for now)
            MarkableRelation newRelation = new MarkableRelation(currentAttributeName,currentAttribute.getType(),true, currentAttribute.getLineWidth(), currentAttribute.getLineColor(), currentAttribute.getLineStyle(), currentAttribute.getMaxSize(),currentAttribute.getIsDashed(), currentAttribute.getAttributeNameToShowInMarkablePointerFlag());
            // Add newly created MarkableRelation to respective HashMap
            MarkableSetRelations.put(currentAttributeName,newRelation);
            // Associate currentAttribute with pertaining MarkableRelation
            currentAttribute.setMarkableRelation(newRelation);
            newRelation = null;
        }
                
        // Iterate over all MarkableSetRelations
        Iterator allAttributeNames = MarkableSetRelations.keySet().iterator();
        while (allAttributeNames.hasNext())
        {
            MarkableRelation currentRelation = (MarkableRelation) MarkableSetRelations.get((String)allAttributeNames.next());
            currentAttributeName = currentRelation.getAttributeName();
            
            // Iterate over all Markables on this MarkableLevel
            Set allMarkableIDsSet = markableHash.keySet();
            Iterator it = allMarkableIDsSet.iterator();
            while(it.hasNext())                
            {
                // Get current Markable
                currentMarkable = (Markable)markableHash.get(it.next());
                // Try to retrieve value of current attribute from current markable, e.g. from member
                currentValue = currentMarkable.getAttributeValue(currentAttributeName);
                if (currentValue != null && currentValue.equals("")==false && currentValue.equals(MMAX2.defaultRelationValue)==false)
                {
                    // The current Markable has a non-empty value for the current attribute, so add current markable to Relation for this attribute
                    currentRelation.addMarkableWithAttributeValueToMarkableSet(currentMarkable,currentValue);
                    // Determine numerical value of this set id
                    int currentNum = MMAX2Utils.parseID(currentValue);
                    if (this.currentDiscourse.getCurrentMarkableChart().getNextFreeMarkableSetNum() <= currentNum)
                    {
                        this.currentDiscourse.getCurrentMarkableChart().setNextFreeMarkableSetNum(currentNum+1);
                    }
                }
            }
        }// for all attributes
        
        // Get all Attributes of type MARKABLE_POINTER
        currentAttributes = this.annotationscheme.getAttributesByType(AttributeAPI.MARKABLE_POINTER);
        // Iterate over all Attributes of type MARKABLE_POINTER
        for (int i=0;i<currentAttributes.length;i++)
        {
            // Get current Attribute, e.g. antecedent
            currentAttribute = (MMAX2Attribute) currentAttributes[i];
            currentAttributeName = currentAttribute.getLowerCasedAttributeName();
            // Create one MarkableRelation for each Attribute of type MARKABLE_POINTER (always order, for now)
            MarkableRelation newRelation = new MarkableRelation(currentAttributeName,currentAttribute.getType(),true, currentAttribute.getLineWidth(), currentAttribute.getLineColor(), currentAttribute.getLineStyle(),currentAttribute.getMaxSize(),currentAttribute.getIsDashed(), currentAttribute.getAttributeNameToShowInMarkablePointerFlag());
            // Add newly created MarkableRelation to respective HashMap
            MarkablePointerRelations.put(currentAttributeName,newRelation);
            // Associate currentAttribute with pertaining MarkableRelation
            currentAttribute.setMarkableRelation(newRelation);            
            newRelation = null;
        }
        
        // Iterate over all MarkablePointerRelations
        allAttributeNames = MarkablePointerRelations.keySet().iterator();
        while (allAttributeNames.hasNext())
        {
            MarkableRelation currentRelation = (MarkableRelation) MarkablePointerRelations.get((String)allAttributeNames.next());
            currentAttributeName = currentRelation.getAttributeName();
            
            // Iterate over all Markables on this MarkableLevel
            Set allMarkableIDsSet = markableHash.keySet();
            Iterator it = allMarkableIDsSet.iterator();
            while(it.hasNext())
            {
                // Get current Markable
                currentMarkable = (Markable)markableHash.get(it.next());
                // Try to retrieve value of current attribute from current markable, e.g. from member
                currentValue = currentMarkable.getAttributeValue(currentAttributeName);
                if (currentValue != null && currentValue.equals("")==false && currentValue.equals(MMAX2.defaultRelationValue)==false)
                {
                    // The current Markable has a non-empty value for the current attribute, so add current markable to Relation for this attribute
                    currentRelation.createMarkablePointer(currentMarkable,this);
                }
            }
        }// for all attributes        
    }
    
    public final MarkableRelation[] getActiveMarkableSetRelationsForMarkable(Markable markable)
    {
        ArrayList templist = new ArrayList();
        MarkableRelation[] result = new MarkableRelation[0];
        Iterator allAttributeNames = MarkableSetRelations.keySet().iterator(); // slow
        while (allAttributeNames.hasNext())
        {
            MarkableRelation currentRelation = (MarkableRelation) MarkableSetRelations.get((String)allAttributeNames.next());
            String currentAttributeName = currentRelation.getAttributeName();
            // Todo: Optimize acces to getAttributeValue!
            if (markable.getAttributeValue(currentAttributeName)!=null && markable.getAttributeValue(currentAttributeName).equals("")==false && markable.getAttributeValue(currentAttributeName).equals(MMAX2.defaultRelationValue)==false)
            {
                // The currentMarkable has a valid value for the current MarkableRelation
                templist.add(currentRelation);
            }
        }
        if (templist.size()!=0)
        {
            result = new MarkableRelation[templist.size()];
            result = (MarkableRelation[]) templist.toArray(new MarkableRelation[1]);
        }
        return result;
    }

    public final MarkablePointer[] getActiveMarkablePointersForTargetMarkable(Markable markable, String pointerRelationName)
    {
        ArrayList templist = new ArrayList();
        MarkablePointer[] result = null;
        
        // Get MarkablePointer of name pointerRelationName
        MarkableRelation requiredPointerRelation = (MarkableRelation)MarkablePointerRelations.get(pointerRelationName);
        // Get list of all MarkablePointers pointing to markable
        templist = (ArrayList)Arrays.asList(requiredPointerRelation.getMarkablePointersWithTargetMarkable(markable));
        
        if (templist.size()!=0)
        {
            //result = new MarkableRelation[templist.size()];
            result = (MarkablePointer[]) templist.toArray(new MarkablePointer[0]);
        }
        else
        {
            result = new MarkablePointer[0];
        }
        return result;        
    }
    
    public final MarkableRelation[] getActiveMarkablePointerRelationsForSourceMarkable(Markable markable)
    {
        ArrayList templist = new ArrayList();
        MarkableRelation[] result = new MarkableRelation[0];
        Iterator allAttributeNames = MarkablePointerRelations.keySet().iterator();//slow
        // Iterate over all pointer relations (by name)
        while (allAttributeNames.hasNext())
        {
            // Get current MarkablePointerRelation
            MarkableRelation currentRelation = (MarkableRelation) MarkablePointerRelations.get((String)allAttributeNames.next());
            // Get attribute name associated with relation
            String currentAttributeName = currentRelation.getAttributeName();
            // Todo: Optimize access to getAttributeValue!
            if (markable.getAttributeValue(currentAttributeName) !=null && 
                markable.getAttributeValue(currentAttributeName).equals("")==false && 
                markable.getAttributeValue(currentAttributeName).equals(MMAX2.defaultRelationValue)==false)
            {
                // The currentMarkable has a valid value for the current MarkableRelation
                // So, it is the source markable os a pointer relation
                templist.add(currentRelation);
            }
        }
        if (templist.size()!=0)
        {
            result = new MarkableRelation[templist.size()];
            result = (MarkableRelation[]) templist.toArray(new MarkableRelation[1]);
        }
        return result;
    }

    public final ArrayList getMarkablePointersForTargetMarkable(Markable markable)
    {
        ArrayList result = new ArrayList();
        Iterator allAttributeNames = MarkablePointerRelations.keySet().iterator();//slow
        while (allAttributeNames.hasNext())
        {
            // Get current MarkablePointerRelation
            MarkableRelation currentRelation = (MarkableRelation) MarkablePointerRelations.get((String)allAttributeNames.next());
            result.addAll(Arrays.asList(currentRelation.getMarkablePointersWithTargetMarkable(markable)));
        }
        return result;
    }
    
    
    public final void destroyDependentComponents()
    {
        // Iterate over all Markables on this MarkableLevel
        Set allMarkableIDsSet = markableHash.keySet();
        Iterator it = allMarkableIDsSet.iterator();
        while(it.hasNext())
        {
            ((Markable)markableHash.get(it.next())).destroyDependentComponents();
        }
    
        currentDiscourse = null;
        markableDOM = null;
        endedMarkablesAtDiscourseElement.clear();
        endedMarkablesAtDiscourseElement = null;
        startedMarkablesAtDiscourseElement.clear();
        startedMarkablesAtDiscourseElement = null;
        markableHash.clear();
        markableHash = null;
        markablesAtDiscourseElement.clear();
        markablesAtDiscourseElement = null;        
        markablesAtDiscoursePosition = null;
        renderer.destroyDependentComponents();
        renderer = null;
        moveUp.removeActionListener(this);
        moveUp = null;
        moveDown.removeActionListener(this);
        moveDown = null;
        updateCustomization.removeActionListener(this);
        updateCustomization = null;
        activatorComboBox.removeActionListener(this);
        activatorComboBox = null;
        switchCustomizations.removeActionListener(this);
        switchCustomizations = null;
        MarkableSetRelations.clear();
        MarkableSetRelations = null;
        MarkablePointerRelations.clear();
        MarkablePointerRelations = null;
        annotationscheme.destroyDependentComponents();
        annotationscheme = null;
        System.gc();        
    }
    
    public final MMAX2AnnotationScheme getCurrentAnnotationScheme()
    {
        return this.annotationscheme;
    }
    
    protected final void resetMarkablesForStyleSheetReapplication()
    {
        // TODO: reset this.markablesAtDiscoursePosition, because discourse positions may change as the result of style sheet reapplication
        // Iterate over all Markables on this MarkableLevel
        Set allMarkableIDsSet = markableHash.keySet();
        Iterator it = allMarkableIDsSet.iterator();
        while(it.hasNext())        

        {
            ((Markable) markableHash.get(it.next())).resetHandles();
        }
    }
    
    /** This method returns the currently valid background color defined for DisplayPosition displayPosition. 
        It is determined on the basis of the markable(s) this MarkableLayer has on this position. If no markables exist on this 
        position, it is assumjed to be a MarkableHandle, and the color is determined ...*/
    public final Color getBackgroundColorAtDisplayPosition(int displayPosition)
    {
        Color resultColor = null;
        int discoursePosition = 0;
        
        Markable[] markablesAtDisplayPosition = getAllMarkablesAtDiscoursePosition(currentDiscourse.getDiscoursePositionAtDisplayPosition(displayPosition));
        return resultColor;
    }
        
    public final int getSize()
    {
        System.err.println("MarkableLevel.getSize() is deprecated! Use getMarkableCount() instead!");
        return this.markableHash.size();
    }

    public final int getMarkableCount()
    {
        return this.markableHash.size();
    }
    
    
    public final JLabel getNameLabel()
    {
        return this.nameLabel;
    }
    
    public final JComboBox getActivatorComboBox()
    {
        return activatorComboBox;
    }

    public final JCheckBox getSwitchCheckBox()
    {
        return this.switchCustomizations;
    }
    
    public final BasicArrowButton getMoveUpButton()
    {
        return this.moveUp;
    }

    public final BasicArrowButton getMoveDownButton()
    {
        return this.moveDown;
    }
    
    public final JButton getUpdateButton()
    {
        return this.updateCustomization;
    }
   
    public final JButton getValidateButton()
    {
        return validateButton;
    }

    public final JButton getDeleteButton()
    {
        return deleteAllButton;
    }
    
    public final void setPosition(int pos)
    {        
        position = pos;
        moveUp.setActionCommand("up:"+pos);
        moveDown.setActionCommand("down:"+pos);
    }
    
    public final int getPosition()
    {
        return position;
    }
    
    public final String getMarkableFileName()
    {
        return markableFileName;
    }
    
    public final String getAbsoluteMarkableFileName()
    {
        File temp = new File(markableFileName);
        return temp.getAbsolutePath();
    }
    
    public final String getMarkableLevelName()
    {
        return markableLevelName;
    }
    
    public final String getMatchableMarkableLevelName()
    {
        return matchableLevelName;
    }
    
    public final MarkableLevelRenderer getRenderer()
    {
        return renderer;
    }
    
    public final boolean getIsActive()
    {
        return active;
    }

    public final boolean getIsVisible()
    {
        return visible;
    }
    
    public final boolean getHasHandles()
    {
        return hasHandles;
    }
    
    public final void setHasHandles(boolean status)
    {
        hasHandles = status;        
    }
    
    /** Used by MMAX query. */
    public final ArrayList getMarkablesMatchingAll(MMAX2MatchingCriterion criterion)
    {
        ArrayList resultList = new ArrayList();
        if (markableLevelName.equalsIgnoreCase("internal_basedata_representation")==false)
        {
            Markable currentMarkable = null;
            ArrayList list = new ArrayList(markableHash.values());
            for (int t=0;t<list.size();t++)
            {
                currentMarkable = (Markable)list.get(t);
                if (MarkableHelper.matchesAll(currentMarkable,criterion))
                {
                    resultList.add(currentMarkable);
                }
            }
        }
        else
        {
            MMAX2DiscourseElement currentDE = null;
            ArrayList list = (ArrayList)java.util.Arrays.asList(getCurrentDiscourse().getDiscourseElements());
            for (int t=0;t<list.size();t++)
            {
                currentDE = (MMAX2DiscourseElement)list.get(t);
                if (MarkableHelper.matchesAll(currentDE, criterion))
                {
                    resultList.add(currentDE);
                }
            }                        
        }
        return resultList;
    }
    
    /** Used by MMAX query. */
    public final ArrayList getMarkablesMatchingAny(MMAX2MatchingCriterion criterion)
    {        
        ArrayList resultList = new ArrayList();
        if (markableLevelName.equalsIgnoreCase("internal_basedata_representation")==false)
        {
            Markable currentMarkable = null;
            ArrayList list = new ArrayList(markableHash.values());
            for (int t=0;t<list.size();t++)
            {
                currentMarkable = (Markable)list.get(t);
                if (MarkableHelper.matchesAny(currentMarkable, criterion))
                {
                    resultList.add(currentMarkable);
                }
            }
        }
        else
        {
            // This level is a de level!
            MMAX2DiscourseElement currentDE = null;
            ArrayList list = (ArrayList)java.util.Arrays.asList(getCurrentDiscourse().getDiscourseElements());
            for (int t=0;t<list.size();t++)
            {
                currentDE = (MMAX2DiscourseElement)list.get(t);
                if (MarkableHelper.matchesAny(currentDE, criterion))
                {
                    resultList.add(currentDE);
                }
            }            
        }
        return resultList;
    }
   
    
    public final ArrayList getMarkables()
    {
        return new ArrayList(markableHash.values());
    }

    public final ArrayList getMarkables(Comparator comp)
    {
        ArrayList temp =  new ArrayList(markableHash.values());
        if (comp != null)
        {
            Markable[] tempArray =  (Markable[])temp.toArray(new Markable[0]);;
            java.util.Arrays.sort(tempArray,comp);
            temp =  new ArrayList(java.util.Arrays.asList(tempArray));
        }
        return temp;
    }
    

    

    /** AdmissibleCategories is a list of those REs that should be included in the list as source.
     * The Category of the RE that a Markable is mapped to is stored as attribute 'mapped_to_re'. 
       Markables that are unmapped to any any RE are always included*/
/*    public final String[] createCleansableKeyList(String memberAttributeName, ArrayList admissibleCategories)
    {
        HashMap sets = new HashMap();
        ArrayList allMarkables = getMarkables();
        for (int e=0;e<allMarkables.size();e++)
        {
            String currentValue = ((Markable)allMarkables.get(e)).getAttributeValue(memberAttributeName,"");
            if (currentValue.equals(""))continue;
            // currentMarkable has a valid value in memberAttributeName
            // Get ArrayList mapped to it, if any
            ArrayList temp = (ArrayList) sets.get(currentValue);
            if (temp==null) temp = new ArrayList();
            temp.add((Markable)allMarkables.get(e));
            sets.put(currentValue,temp);
            temp = null;
        }
        ArrayList result = new ArrayList();
        // Here, all Markables have been processed
        Iterator allSetValues = sets.keySet().iterator();
        while (allSetValues.hasNext())
        {
            ArrayList current = (ArrayList) sets.get((String) allSetValues.next());
            if (current.size()==1) continue;
            
            Markable[] temp = (Markable[])current.toArray(new Markable[0]);
            //java.util.Arrays.sort(temp,new ShorterBeforeLongerComparator());
            java.util.Arrays.sort(temp,new DiscourseOrderMarkableComparator());
            current = new ArrayList(java.util.Arrays.asList(temp));            
            
            // Iterate over all Markables mapped to current member value, from bottom to top
            for (int s=current.size()-1;s>=0;s--)
            {
                // Get Markable at current position
                Markable currentAna = (Markable)current.get(s);
                if (currentAna.getAttributeValue("mapped_to_re","").equals("") ||
                    admissibleCategories.contains(currentAna.getAttributeValue("mapped_to_re","XXX")))
                {
                    // CurrentAna is of the type that is admissible for the keylist,
                    // or it is not mapped and has to be added as well to produce a recall error.
                    // Iterate over all set members before current ana, and add the pairs
                    for (int e=s-1;e>=0;e--)
                    {
                        Markable currentAnte = (Markable) current.get(e);
                        result.add(currentAnte.getID()+" "+currentAna.getID());    
//                        System.out.println("Adding to ckl: "+currentAnte.getID()+" "+currentAna.getID()+" "+currentAna.getAttributeValue("mapped_to_re","***"));
                    }
                }                
            }
        }
        return (String[]) result.toArray(new String[0]);
    }
*/
    
    public final ArrayList getMatchingMarkables(String queryString)
    {
        MMAX2QueryTree tree = null;
        try
        {
            tree = new MMAX2QueryTree(queryString, this);
        }
        catch (MMAX2QueryException ex)
        {
        	ex.printStackTrace();
            return new ArrayList();
        }

        if (tree != null)
        {        
            return tree.execute(new DiscourseOrderMarkableComparator());
        }
        else
        {
            return new ArrayList();
        }
    }
    
    public final void updateMarkables()
    {        
        startedMarkablesAtDiscourseElement = null;
        startedMarkablesAtDiscourseElement = new HashMap();
        endedMarkablesAtDiscourseElement = null;
        endedMarkablesAtDiscourseElement = new HashMap();
        
        markablesAtDiscourseElement=null;
        markablesAtDiscourseElement=new HashMap();       
        
        NodeList allMarkableNodes = markableDOM.getElementsByTagName("markable");
        Node currentMarkableNode = null;
        int len = allMarkableNodes.getLength();
        
        String currentID = "";
        String currentSpan = "";
        
        Markable currentMarkable=null;
        // Iterate over all Markable elements
        for (int z=0;z<len;z++)
        {
            // Get current markable element
            currentMarkableNode = allMarkableNodes.item(z);
            currentID = currentMarkableNode.getAttributes().getNamedItem("id").getNodeValue();            

            currentMarkable = getMarkableByID(currentID);

            currentSpan = MarkableHelper.getSpan(currentMarkable);
            currentMarkable.update(parseMarkableSpan(currentSpan, currentDiscourse.getWordDOM(),this));
        }                
    }
    
    /** This method is called from the MMAX2DiscourseLoader after the MMAX2Discourse field has been set
        on this level. Markable spans are expanded on the basis of the element IDs actually
        contained in the base data, and not by mere interpolation of integer IDs! 
        
        Synopsis: This method basically iterates over all elements in this.markableDOM, creates a Markable object
        for each by means of the Markable constructor, and adds that to this.markableHash. 
     
     */
    public final int createMarkables()
    {        
        boolean readOnlyAtStart=getIsReadOnly();
        
        int maxIDNum = 0;
        boolean added = false;
        if (isDefined())
        {            
            // Get list of all markable elements currently existing on this level
            NodeList allMarkableNodes = markableDOM.getElementsByTagName("markable");
            Node currentMarkableNode = null;
            int len = allMarkableNodes.getLength();
        
            // New: Make hash sufficiently large from the beginning
            markableHash = new HashMap(len);
            
            String currentID = "";
            int currentIDNum =0;
            String currentSpan = "";
        
            Markable newMarkable=null;
            HashMap attributes = null;
            // Iterate over all Markable elements
            for (int z=0;z<len;z++)
            {
                // Get current markable element
                currentMarkableNode = allMarkableNodes.item(z);
                // For each markable check whether the mmax_level attribute is present
                if (currentMarkableNode.getAttributes().getNamedItem("mmax_level")==null)
                {
                    // Remember that soth. was added
                    added = true;
                    ((Element)currentMarkableNode).setAttribute("mmax_level",getMarkableLevelName());
                    setIsDirty(true, false);
                }
                
                // Create attributes HashMap for Markable object attributes. 
                attributes = MMAX2Utils.convertNodeMapToHashMap(currentMarkableNode.getAttributes());
                // Extract numerical part of current id
                currentIDNum = MMAX2Utils.parseID((String)attributes.get("id"));
                if (currentIDNum > maxIDNum) maxIDNum = currentIDNum;
                // Remove id and span attributes.
                attributes.remove("id");
                attributes.remove("span");            
                try
                {
                    currentID = currentMarkableNode.getAttributes().getNamedItem("id").getNodeValue();
                }
                catch (java.lang.NullPointerException ex)
                {
                    JOptionPane.showMessageDialog(null,"Missing ID attribute on markable!","MarkableLevel: "+markableFileName,JOptionPane.ERROR_MESSAGE);                
                }
            
                try
                {
                    currentSpan = currentMarkableNode.getAttributes().getNamedItem("span").getNodeValue();
                }
                catch (java.lang.NullPointerException ex)
                {
                    JOptionPane.showMessageDialog(null,"Missing span attribute on markable!","MarkableLevel: "+markableFileName,JOptionPane.ERROR_MESSAGE);
                    //System.exit(0);                                
                }            
                // Create new Markable object
                // parseSpan is negligible
                //newMarkable = new Markable(currentMarkableNode, currentID, MarkableHelper.parseSpan(currentSpan), attributes,this);
                newMarkable = new Markable(currentMarkableNode, currentID, parseMarkableSpan(currentSpan,this.currentDiscourse.getWordDOM(),this), attributes,this);
                
                // Create mapping of Markable to its ID                
                markableHash.put(currentID, newMarkable);
                newMarkable = null;            
            }
        }  
        else
        {
            markableHash = new HashMap();
        }
        
        if (added)
        {
            if (getCurrentDiscourse().getHasGUI())
            {
                JOptionPane.showMessageDialog(null,"The attribute 'mmax_level' has been added to at least one markable on level "+getMarkableLevelName()+"!\nPlease make sure to save this level later.","Markable level has been modified!",JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                System.err.println("The attribute 'mmax_level' has been added to at least one markable on level "+getMarkableLevelName()+"!");
            }
        }
        
        if (readOnlyAtStart == false && getIsReadOnly())
        {
            System.err.println("Level "+getMarkableLevelName()+" has been set to read-only!");
        }
        
        //System.err.println(markableHash);
        
        return maxIDNum;
    }

    public void setCurrentDiscourse(MMAX2Discourse _discourse)
    {
        currentDiscourse = _discourse;
    }
    
    public MMAX2Discourse getCurrentDiscourse()
    {
        return currentDiscourse;
    }        
    
    public Markable getMarkableByID(String markableId)
    {
        return (Markable) this.markableHash.get(markableId);
    }

    public final Markable[] getAllMarkablesStartingWith(MMAX2DiscourseElementSequence sequence)
    {       
        MMAX2DiscourseElement[] elements = sequence.getContent();
        ArrayList temp = new ArrayList();
        // Get disc pos of last element in sequence
        int lastDiscPosInElements = elements[elements.length-1].getDiscoursePosition();
        // Get all markables started by the first DE in parameter list
        Markable[] started = getAllMarkablesStartedByDiscourseElement(elements[0].getID());
        // Iterate over all markables started at first DE
        for (int z=0;z<started.length;z++)
        {
            // Get final disc pos of current markable
            int currentFinalDiscPos = started[z].getRightmostDiscoursePosition();
            if (currentFinalDiscPos <= lastDiscPosInElements)
            {
                // Add it if it is earlier or equal
                temp.add(started[z]);
            }
        }
        return (Markable[]) temp.toArray(new Markable[0]);
    }

    public final Markable getSingleLongestMarkableStartingWith(MMAX2DiscourseElementSequence sequence)
    {
        Markable result = null;
        Markable[] candidates = getAllMarkablesStartingWith(sequence);
        if (candidates.length==1)
        {
            result = candidates[0];
        }
        else if (candidates.length > 0)
        {
            //java.util.Arrays.sort(candidates,new ShorterBeforeLongerComparator());
            java.util.Arrays.sort(candidates,new DiscourseOrderMarkableComparator());
            if (candidates[candidates.length-1].getSize() != candidates[candidates.length-2].getSize())
            {
                result = candidates[candidates.length-1];
            }
        }
        if (result != null && result.getSize() < sequence.getLength())
        {
            result = null;
        }            
        return result;
    }
    
    
    /** This method returns an array of those Markable objects associated with discourseElement Id, or empty array if none. 
        Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. 
        The retrieved Array comes from a hash, so this method is efficient (a little less so if sort==true,
        which causes the markables to be sorted in discourse order, shorter before longer ones). */
    public Markable[] getAllMarkablesAtDiscourseElement(String discourseElementId, boolean sort)
    {
        Markable[] result = (Markable[]) markablesAtDiscourseElement.get(discourseElementId);
        if (result == null) result = new Markable[0];
        if (sort)
        {
            Arrays.sort(result,getCurrentDiscourse().DISCOURSEORDERCOMP);
        }
        return result;
    }

    
    /** This method returns an ArrayList of those Markable objects associated with discourseElement Id, or empty list if none. 
        Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. 
        The retrieved Array comes from a hash, so this method is efficient (a little less so if sort==true,
        which causes the markables to be sorted in discourse order, shorter after longer ones). */
    public ArrayList getMarkablesAtDiscourseElementID(String discourseElementId, Comparator comp)
    {
        Markable[] result = (Markable[]) markablesAtDiscourseElement.get(discourseElementId);
        if (result == null) result = new Markable[0];       
        if (comp != null)
        {
            Arrays.sort(result,comp);
        }
        return new ArrayList(java.util.Arrays.asList(result));
    }
    

    /** This method returns an ArrayList of those Markable objects associated with discourseElement Id, or empty list if none. 
        Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. 
        The retrieved Array comes from a hash, so this method is efficient (a little less so if sort==true,
        which causes the markables to be sorted in discourse order, shorter after longer ones). */
    public ArrayList getMarkablesAtDiscoursePosition(int discPos, Comparator comp)
    {
        String discourseElementId = getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(discPos);
        Markable[] result = (Markable[]) markablesAtDiscourseElement.get(discourseElementId);
        if (result == null) result = new Markable[0];       
        if (comp != null)
        {
            Arrays.sort(result,comp);
        }
        return new ArrayList(java.util.Arrays.asList(result));
    }
    
    
    /** This method returns an array of those Markable objects started at discourseElement Id, or empty array if none. 
        Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. */
    public Markable[] getAllMarkablesStartedByDiscourseElement(String discourseElementId)
    {
        // TODO: Why not sort here??
        Markable[] result = null;
    	//System.err.println(discourseElementId);
        result =  (Markable[]) startedMarkablesAtDiscourseElement.get(discourseElementId);
        if (result == null) result = new Markable[0];
        return result;
    }
    
    public Markable getSingleMarkableExactlyAtDiscourseElement(String discourseElementId)
    {
        Markable result=null;
        Markable[] started = getAllMarkablesStartedByDiscourseElement(discourseElementId);
        Markable[] ended = getAllMarkablesEndedByDiscourseElement(discourseElementId);
        ArrayList startedAsList = new ArrayList(java.util.Arrays.asList(started));
        startedAsList.retainAll(java.util.Arrays.asList(ended));
        if (startedAsList.size()==1)
        {
            result=(Markable)startedAsList.get(0);
        }
        return result;
    }
    
    /** This method returns an array of those Markable objects ended at discourseElement Id, or empty array if none. 
        Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. */
    public Markable[] getAllMarkablesEndedByDiscourseElement(String discourseElementId)
    {
        Markable[] result = null;
        result = (Markable[]) endedMarkablesAtDiscourseElement.get(discourseElementId);
        if (result == null) 
        {
            result = new Markable[0]; // un-uncommented Oct. 17
        }
        else
        {
            java.util.Arrays.sort(result,currentDiscourse.IDCOMP);
            java.util.Arrays.sort(result,currentDiscourse.ENDCOMP);
        }
        return result;
    }
    
    /** Main method for layer-wise retrieval of Markables from discourse positions. Returns empty Markable array if no 
        Markables are found. Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. */
    public Markable[] getAllMarkablesAtDiscoursePosition(int pos)
    {
        return markablesAtDiscoursePosition[pos];
    }
    
    /** This method updates a NodeSet of those Markables at the current MarkableLayer beginning at discourseElementId, with
        longer Markables before shorter ones. Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. */
    public final void getAllStartedMarkablesAsNodes(String discourseElementId, NodeSet result)
    {                       
        // Get Array of Markables, or null if none 
//        long time = System.currentTimeMillis();
        Markable[] temp = getAllMarkablesStartedByDiscourseElement(discourseElementId);        
        if (temp != null)
        {
            // Sort markables in discourse position order, 
            // with longer before shorter ones (for embedding visualization) 
            // TODO: Sort only once!
            Arrays.sort(temp,getCurrentDiscourse().STARTCOMP);
            // Add markables to node list in ordering sequence      
            int len = temp.length;
            for (int o=0;o<len;o++)
            {                
                result.addNode(temp[o].getNodeRepresentation());
            }
        }
    }

    
    /** This method updates a NodeSet of those Markables at the current MarkableLayer ending at discourseElementId, with
        shorter Markables before longer ones. Since this is on MarkableLayer level, no distinction is made wrt to active/inactive. */
    public final void getAllEndedMarkablesAsNodes(String discourseElementId, NodeSet result)
    {                
        // Get Array of Markables, or null if none 
        Markable[] temp = getAllMarkablesEndedByDiscourseElement(discourseElementId);
        if (temp != null)
        {
            // Sort markables in rev discourse position order, 
            // with shorter before longer ones (for embedding visualization) 
            // TODO: Sort only once!
            Arrays.sort(temp,getCurrentDiscourse().ENDCOMP);
            // Add markables to node list in ordering sequence
            int len = temp.length;
            for (int o=0;o<len;o++)
            {
                result.addNode(temp[o].getNodeRepresentation());
            }
        }
    }
    
       
    public final void unregisterMarkable(Markable unregisteree)
    {
        String frags[][] = unregisteree.getFragments();
        String[] currentFragment = null;
        int singleFragments = frags.length;
        for (int z=0;z<singleFragments;z++)
        {
            currentFragment = frags[z];
            // Inform the current layer that Markable unregisteree does no longer start at its start position
            unregisterMarkableAtStartOfFragment(unregisteree, currentFragment[0]);
            // Inform the current layer that Markable unregisteree does no longer end at its end position
            unregisterMarkableAtEndOfFragment(unregisteree, currentFragment[currentFragment.length-1]);
            for (int o=0;o<currentFragment.length;o++)
            {
                // Remove mapping from each of unregisterees DEs. unregisteree will have NO mappings afterwards!!
                unregisterMarkableAtDiscourseElement(unregisteree, currentFragment[o]);
            }
        }
    }
    
    public final void unregisterMarkableAtDiscourseElement(Markable unregisteree, String de)
    {
        /** Get array of those markables associated as with discourseElement de. */
        Markable[] markables = (Markable[]) getAllMarkablesAtDiscourseElement(de,false);
        /** Create new array with one element less */
        Markable[] newMapping = new Markable[markables.length-1];
        int filler=0;
        // Iterate over all markables still associated with de
        for (int u=0;u<markables.length;u++)
        {
            if (markables[u]==unregisteree)
            {
                filler=1;
                continue;
            }
            newMapping[u-filler]=markables[u];
        }        
        markablesAtDiscourseElement.remove(de);        
        if (newMapping.length > 0)
        {
            markablesAtDiscourseElement.put(de,newMapping);                 
        }
        updateDiscoursePositionToMarkableMapping(de);
    }
    
    public final void unregisterMarkableAtStartOfFragment(Markable unregisteree, String de)
    {
        /** Get array of those markables associated as starting with discourseElement de. */
        Markable[] markables = (Markable[]) getAllMarkablesStartedByDiscourseElement(de);                
        
        /** Create new array with one element less */
        Markable[] newMapping = new Markable[markables.length-1];
        int filler=0;
        // Iterate over all markables still associated with de
        for (int u=0;u<markables.length;u++)
        {
            if (markables[u]==unregisteree)
            {
                filler=1;
                continue;
            }
            newMapping[u-filler]=markables[u];
        }        
        startedMarkablesAtDiscourseElement.remove(de);        
        if (newMapping.length > 0)
        {
            startedMarkablesAtDiscourseElement.put(de,newMapping);                 
        }
    }
    
    public final void unregisterMarkableAtEndOfFragment(Markable unregisteree, String de)
    {
        /** Get array of those markables associated as ending with discourseElement de. */
        Markable[] markables = (Markable[]) getAllMarkablesEndedByDiscourseElement(de);
        /** Create new array with one element less */
        Markable[] newMapping = new Markable[markables.length-1];
        int filler=0;
        // Iterate over all markables still associated with de
        for (int u=0;u<markables.length;u++)
        {
            if (markables[u]==unregisteree)
            {
                filler=1;
                continue;
            }
            newMapping[u-filler]=markables[u];
        }        
        endedMarkablesAtDiscourseElement.remove(de);        
        if (newMapping.length > 0)
        {
            endedMarkablesAtDiscourseElement.put(de,newMapping);                 
        }
    }
    
    
    /** This method informs the current layer that Markable markable starts at DiscourseElement id. 
        It is called by the Markable constructor upon Markable creation. It has to be executed BEFORE style sheet application,
        because the method getMarkablesStartedByDiscourseElement(id) is required during style sheet execution. */
    public final void registerMarkableAtStartOfFragment(String discourseElementId, Markable markable)
    {        
    	//System.err.println(discourseElementId);
    	
        /** Get array of those markables already associated as starting with discourseElement Id, or null. */
        Markable[] markables = (Markable[]) getAllMarkablesStartedByDiscourseElement(discourseElementId);
        Markable[] mapping = null;
        if (markables == null)
        {
            // There has not yet been any markable associated as starting with this discourseElement Id
            // So create new mapping            
            mapping = new Markable[1];
            mapping[0] = markable;
        }
        else
        {
            // There is already a Markable associated as starting with this discourseElement Id
            // Create new Array with len + 1
            mapping = new Markable[markables.length+1];
            System.arraycopy(markables,0,mapping,0,markables.length);
            mapping[markables.length] = markable;
            this.startedMarkablesAtDiscourseElement.remove(discourseElementId);
        }
        this.startedMarkablesAtDiscourseElement.put(discourseElementId,mapping);                 
    }    

    /** This method informs the current layer that Markable markable ends at DiscourseElement id. 
        It is called by the Markable constructor upon Markable creation. It has to be executed BEFORE style sheet application,
        because the method getMarkablesEndedByDiscourseElement(id) is required during style sheet execution. */    
    public final void registerMarkableAtEndOfFragment(String discourseElementId, Markable markable)
    {
        /** Get array of those markables already associated as ending with discourseElement Id, or null. */
        Markable[] markables = (Markable[]) getAllMarkablesEndedByDiscourseElement(discourseElementId);
        Markable[] mapping = null;
        if (markables == null)
        {
            // There has not yet been any markable associated as ending with this discourseElement Id
            // So create new mapping            
            mapping = new Markable[1];
            mapping[0] = markable;
        }
        else
        {
            // There is already a Markable associated as ending with this discourseElement Id
            // Create new Array with len + 1
            mapping = new Markable[markables.length+1];
            System.arraycopy(markables,0,mapping,0,markables.length);
            mapping[markables.length] = markable;
            endedMarkablesAtDiscourseElement.remove(discourseElementId);
        }
        endedMarkablesAtDiscourseElement.put(discourseElementId,mapping);                 
    }    
    
    /** This method is called by each Markable constructor and updates this.markablesAtDiscourseElement to reflect that Markable
        markable is associated with the DE with ID discourseElementId. */
    public final void registerMarkableAtDiscourseElement(String discourseElementId, Markable markable)
    {
        /** Get array of those markables already associated with discourseElement Id, or null. */
        Markable[] markables = (Markable[]) getAllMarkablesAtDiscourseElement(discourseElementId,false);
        Markable[] mapping = null;
        if (markables == null)
        {
            // There has not yet been any markable associated with this discourseElement Id
            // So create new mapping            
            mapping = new Markable[1];
            mapping[0] = markable;
        }
        else
        {
            // There is already a Markable associated with this discourseElement Id
            // Create new Array with len + 1
            mapping = new Markable[markables.length+1];
            System.arraycopy(markables,0,mapping,0,markables.length);
            mapping[markables.length] = markable;
            markablesAtDiscourseElement.remove(discourseElementId);
        }
        markablesAtDiscourseElement.put(discourseElementId,mapping);                    
    }
    
    
    /** This method sets for all Markables on this level the fields displayStartPosition and displayEndPosition. */
    public final void setMarkableDisplayPositions()
    {
        // Iterate over all Markables on this MarkableLevel
        Set allMarkableIDsSet = markableHash.keySet();
        Iterator it = allMarkableIDsSet.iterator();
        while(it.hasNext())
        {
            MarkableHelper.setDisplayPositions(((Markable)markableHash.get(it.next())));
        }
    }
    
    public final MMAX2OneClickAnnotationSelector createOneClickAnnotationSelector(Markable currentPrimary, MarkableChart _chart, int displayPos)
    {
        MMAX2OneClickAnnotationSelector selector = null;
        String currentOneClickAttributeName = getCurrentAnnotationScheme().getCurrentAttributePanel().getOneClickAnnotationAttributeName();
        if (currentOneClickAttributeName.equals("<none>")==false)
        {
            // There is a oneclickanno attribute defined for the current level
            // Now see if this attribute is available to the currently selected markable
            if (currentPrimary.isDefined(currentOneClickAttributeName))
            {
                // The oneclickattribute is available for the current markable
                // isDefined suffices here because we call this only for selected markables, 
                // which have correct defaults already
                MMAX2Attribute realAttribute = getCurrentAnnotationScheme().getUniqueAttributeByName("^"+currentOneClickAttributeName.toLowerCase()+"$");
                selector = new MMAX2OneClickAnnotationSelector(currentPrimary, realAttribute, _chart, displayPos);
            }            
        }        
        return selector;
    }
    
    
    /** This method is called by the MarkableChart for each MarkableLayer associated with it, AFTER the style sheet has been applied.
        It uses getDiscoursePositionFromDiscourseElementId to fill the array markablesAtDiscoursePosition with arrays of Markables
        associated with the DE at this discourse position.
        The method must be called AFTER style sheet execution because it is only there that DiscourseElements are associated with
        discourse positions. 
        As a result, direct (constant time) access from discourse positions to associated markables is possible. */
    public final void createDiscoursePositionToMarkableMapping()
    {
        //markablesAtDiscoursePosition = new Markable[this.getCurrentDiscourse().getTotalNumberOfDiscourseElements()][0];
    	markablesAtDiscoursePosition = new Markable[getCurrentDiscourse().getDiscourseElementCount()][0];
        int tempDiscPos = 0;
        String tempDE = "";
        /** Get array of all discourse element IDs this MarkableLayer has markables on. */
        // New October 1st, 2004: Use 0 as array template to correctly get zero-length array in case of empty level
        String[] allDEs = (String[]) this.markablesAtDiscourseElement.keySet().toArray(new String[0]);
        int numDEs = allDEs.length;        
        /** Iterate over all DEs this MarkableLayer has Markables on. */
        for (int z=0;z<numDEs;z++)
        {
            tempDE = (String)allDEs[z];
            tempDiscPos = this.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(tempDE);
            if (tempDiscPos != -1)
            {            
                this.markablesAtDiscoursePosition[tempDiscPos] = this.getAllMarkablesAtDiscourseElement(tempDE, true);            
            }
        }                
    }
    
    protected void finalize()
    {
        System.err.println("MarkableLevel "+this.getMarkableLevelName()+" is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }
    
    public final void updateDiscoursePositionToMarkableMapping(String tempDE)
    {
        int tempDiscPos = getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(tempDE);
        markablesAtDiscoursePosition[tempDiscPos] = getAllMarkablesAtDiscourseElement(tempDE, true);            
    }
    
    protected final void updateNameLabelText()
    {
        boolean doit = false;
        String HTMLText ="";
        try
        {
            doit = currentDiscourse.getMMAX2().getUseFancyLabels();
        }
        catch (java.lang.NullPointerException ex)   
        {
            doit = true;
        }
        
        if (doit)
        {
            if (renderer.getForegroundIsTransparent()==false)                
            {
                HTMLText = "<html><font face=\"monospace\" color=\"" +MMAX2Utils.colorToHTML(renderer.getForegroundColor())+"\">";
            }
            else
            {
                HTMLText = "<html><font face=\"monospace\">";
            }
            if (this.getHasHandles())
            {
                HTMLText = HTMLText + "<font color=\""+MMAX2Utils.colorToHTML(renderer.getHandleColor())+"\"><b>[</b></font>";
            }
            if (renderer.getIsBold()) HTMLText = HTMLText+"<b>";
            if (renderer.getIsItalic()) HTMLText = HTMLText +"<i>";
            if (renderer.getIsSuperscript()) HTMLText = HTMLText + "<sup>";
            if (renderer.getIsSubscript()) HTMLText = HTMLText + "<sub>";
            if (renderer.getIsUnderline()) HTMLText = HTMLText + "<u>";
            if (renderer.getIsStrikethrough()) HTMLText = HTMLText + "<strike>";
            HTMLText = HTMLText+this.getMarkableLevelName();
            if (renderer.getIsStrikethrough()) HTMLText = HTMLText + "</strike>";
            if (renderer.getIsUnderline()) HTMLText = HTMLText + "</u>";
            if (renderer.getIsSubscript()) HTMLText = HTMLText + "</sub>";
            if (renderer.getIsSuperscript()) HTMLText = HTMLText + "</sup>";
            if (renderer.getIsItalic()) HTMLText = HTMLText +"</i>";
            if (renderer.getIsBold()) HTMLText = HTMLText+"</b>";
            if (this.getHasHandles())
            {
                HTMLText = HTMLText + "<font color=\""+MMAX2Utils.colorToHTML(renderer.getHandleColor())+"\"><b>]</b></font>";
            }

            HTMLText=HTMLText+"<font></html>";
            nameLabel.setText(HTMLText);
            nameLabel.setOpaque(true);
        }
        else
        {
            nameLabel.setText(getMarkableLevelName());
        }

    }
   
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) 
    {        
        String command = actionEvent.getActionCommand();
        if (command.equals("activator"))
        {
            JComboBox control = (JComboBox)actionEvent.getSource();
            String val = control.getSelectedItem().toString();
            if (val.equalsIgnoreCase("inactive"))
            {
                setInactive();                
            }
            else if (val.equalsIgnoreCase("visible"))
            {
                setVisible();
            }
            else if (val.equalsIgnoreCase("active"))
            {
                setActive();                
            }

            if(getCurrentDiscourse().getMMAX2().getAutoRefreshUponPanelAction())
            {
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().rerender();
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                
            }
        }
        else if (command.equals("update"))
        {
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            renderer.updateSimpleMarkableCustomizations(true);
            getCurrentDiscourse().getCurrentMarkableChart().rerender();
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
        }
        else if (command.equals("switch"))
        {
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            renderer.updateSimpleMarkableCustomizations(((JCheckBox)actionEvent.getSource()).isSelected());
            updateCustomization.setEnabled(((JCheckBox)actionEvent.getSource()).isSelected());
            getCurrentDiscourse().getCurrentMarkableChart().rerender();
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
        }
        
        else if (command.equals("validate"))
        {
            int result = JOptionPane.showConfirmDialog(null,"This will start the validation of "+getMarkableCount()+" markables!\nAre you sure?",getMarkableLevelName(),JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                validate();
            }
        }
        else if (command.equals("delete_all"))
        {
            int result = JOptionPane.showConfirmDialog(null,"This will delete "+getMarkableCount()+" markables!\nAre you sure?",getMarkableLevelName(),JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                deleteAllMarkables();
                if (mmax2 != null)
                {
                    mmax2.requestRefreshDisplay();
                    mmax2.requestReapplyDisplay();
                }
            }
        }        
        else if (command.equals("save_this_level"))
        {
            // Save
            saveMarkables("",false);
            // Remove dirty tag, without browser refresh
            setIsDirty(false,false);
        }
        else
        {
            getCurrentDiscourse().getCurrentMarkableChart().reorderMarkableLayers(command);
            if(getCurrentDiscourse().getMMAX2().getAutoRefreshUponPanelAction())
            {
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().rerender();
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));                
            }
        }
    }
    
    
    public final void setActive()
    {
        activatorComboBox.removeActionListener(this);
        active = true;
        System.err.println("Layer "+markableLevelName+" has been set to active");
        nameLabel.setEnabled(true);                
        visible = true;
        switchCustomizations.setEnabled(true);
        if (switchCustomizations.isSelected())
        {
            updateCustomization.setEnabled(true);
        }             
        
        deleteAllButton.setEnabled(true);
        validateButton.setEnabled(true);
        
        activatorComboBox.setSelectedItem("active");
        activatorComboBox.addActionListener(this);
    }
    
    public final void setInactive()
    {
        activatorComboBox.removeActionListener(this);
        active = false;
        System.err.println("Layer "+markableLevelName+" has been set to inactive");
        nameLabel.setEnabled(false);               
        visible = false;
        switchCustomizations.setEnabled(false);
        updateCustomization.setEnabled(false);      
        
        deleteAllButton.setEnabled(false);
        validateButton.setEnabled(false);
        
        activatorComboBox.setSelectedItem("inactive");
        activatorComboBox.addActionListener(this);
    }
    
    public final void setVisible()
    {
        activatorComboBox.removeActionListener(this);
        active = false;
        System.err.println("Layer "+markableLevelName+" has been set to visible!");
        nameLabel.setEnabled(false);                               
        visible = true;
        switchCustomizations.setEnabled(true);
        if (switchCustomizations.isSelected())
        {
            updateCustomization.setEnabled(true);
        }             
        
        deleteAllButton.setEnabled(false);
        validateButton.setEnabled(false);
        
        
        activatorComboBox.setSelectedItem("visible");
        activatorComboBox.addActionListener(this);
    }
    /** Inner class for handling mouse clicks on MarkableLayer's name labels */
    /*
    class MarkableLevelMouseListener extends java.awt.event.MouseAdapter
    {
        public MarkableLevelMouseListener()
        {
            super();
        }
        
        public final void mousePressed(java.awt.event.MouseEvent me)
        {            
            System.out.println(renderer.getExplicitlyAssociatedAttributes());
            ColorSelectionListener colorSelectionListener = new ColorSelectionListener();
            JPopupMenu menu = new JPopupMenu();
            JMenuItem colorMenu = new JMenu("Colors");
            JMenu foregroundColorMenu = new JMenu("Foreground");
            
            JMenuItem item = new JMenuItem("black (transparent)");
            if (renderer.getForegroundIsTransparent()) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);            
            
            item = null;            
            item = new JMenuItem("blue");
            if (renderer.getForegroundColor()==Color.blue) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.blue);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("cyan");
            if (renderer.getForegroundColor()==Color.cyan) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.cyan);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("darkGray");            
            if (renderer.getForegroundColor()==Color.darkGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.darkGray);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("gray");            
            if (renderer.getForegroundColor()==Color.gray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);            
            item.setBackground(Color.gray);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("green");
            if (renderer.getForegroundColor()==Color.green) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.green);
            item.setForeground(Color.black);    
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("lightGray");            
            if (renderer.getForegroundColor()==Color.lightGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.lightGray);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("magenta");            
            if (renderer.getForegroundColor()==Color.magenta) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.magenta);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("orange");            
            if (renderer.getForegroundColor()==Color.orange) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.orange);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("pink");            
            if (renderer.getForegroundColor()==Color.pink) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.pink);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("red");            
            if (renderer.getForegroundColor()==Color.red) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.red);
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("white");            
            if (renderer.getForegroundColor()==Color.white) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.white);            
            item.setForeground(Color.black);
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("yellow");            
            if (renderer.getForegroundColor()==Color.yellow) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.yellow);
            item.setForeground(Color.black);    
            item.setActionCommand("foreground");
            foregroundColorMenu.add(item);

            colorMenu.add(foregroundColorMenu);
            
            JMenu backgroundColorMenu = new JMenu("Background");
            
            item = new JMenuItem("black");
            if (renderer.getBackgroundColor()==Color.black) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.black);
            item.setForeground(Color.white);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);            
            
            item = null;            
            item = new JMenuItem("blue");
            if (renderer.getBackgroundColor()==Color.blue) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.blue);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("cyan");
            if (renderer.getBackgroundColor()==Color.cyan) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.cyan);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("darkGray");            
            if (renderer.getBackgroundColor()==Color.darkGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.white);
            item.setBackground(Color.darkGray);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("gray");            
            if (renderer.getBackgroundColor()==Color.gray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);            
            item.setBackground(Color.gray);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("green");
            if (renderer.getBackgroundColor()==Color.green) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.green);
            item.setForeground(Color.black);    
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("lightGray");            
            if (renderer.getBackgroundColor()==Color.lightGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.lightGray);
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("magenta");            
            if (renderer.getBackgroundColor()==Color.magenta) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.magenta);
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("orange");            
            if (renderer.getBackgroundColor()==Color.orange) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.orange);
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("pink");            
            if (renderer.getBackgroundColor()==Color.pink) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.pink);
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("red");            
            if (renderer.getBackgroundColor()==Color.red) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.red);
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("white (transparent)");
            if (renderer.getBackgroundColor()==Color.white) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.white);            
            item.setForeground(Color.black);
            item.setActionCommand("background");
            backgroundColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("yellow");            
            if (renderer.getBackgroundColor()==Color.yellow) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.yellow);
            item.setForeground(Color.black);    
            item.setActionCommand("background");
            backgroundColorMenu.add(item);                        
            colorMenu.add(backgroundColorMenu);
            
            JMenu handleColorMenu = new JMenu("Handles");
            
            item = new JMenuItem("black");
            if (renderer.getHandleColor()==Color.black) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.black);
            item.setForeground(Color.white);
            item.setActionCommand("handle");
            handleColorMenu.add(item);            
            
            item = null;            
            item = new JMenuItem("blue");
            if (renderer.getHandleColor()==Color.blue) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.blue);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("cyan");
            if (renderer.getHandleColor()==Color.cyan) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);
            item.setBackground(Color.cyan);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("darkGray");            
            if (renderer.getHandleColor()==Color.darkGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.white);
            item.setBackground(Color.darkGray);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("gray");            
            if (renderer.getHandleColor()==Color.gray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setForeground(Color.black);            
            item.setBackground(Color.gray);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("green");
            if (renderer.getHandleColor()==Color.green) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.green);
            item.setForeground(Color.black);    
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("lightGray");            
            if (renderer.getHandleColor()==Color.lightGray) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.lightGray);
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("magenta");            
            if (renderer.getHandleColor()==Color.magenta) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.magenta);
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("orange");            
            if (renderer.getHandleColor()==Color.orange) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.orange);
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("pink");            
            if (renderer.getHandleColor()==Color.pink) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.pink);
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("red");            
            if (renderer.getHandleColor()==Color.red) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.red);
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("white (transparent)");
            if (renderer.getHandleColor()==Color.white) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.white);            
            item.setForeground(Color.black);
            item.setActionCommand("handle");
            handleColorMenu.add(item);
            
            item = null;
            item = new JMenuItem("yellow");            
            if (renderer.getHandleColor()==Color.yellow) item.setEnabled(false);
            item.addActionListener(colorSelectionListener);
            item.setBackground(Color.yellow);
            item.setForeground(Color.black);    
            item.setActionCommand("handle");
            handleColorMenu.add(item);
                        
            colorMenu.add(handleColorMenu);
            
            menu.add(colorMenu);
            
            JMenu decorationMenu = new JMenu("Decoration");
            JMenuItem boldMenuItem = new JMenuItem("Bold");
            if (renderer.getIsBold()) boldMenuItem.setEnabled(false);
            boldMenuItem.addActionListener(colorSelectionListener);
            boldMenuItem.setActionCommand("bold");
            decorationMenu.add(boldMenuItem);
            
            JMenuItem italicMenuItem = new JMenuItem("Italic");
            italicMenuItem.addActionListener(colorSelectionListener);
            if (renderer.getIsItalic()) italicMenuItem.setEnabled(false);
            italicMenuItem.setActionCommand("italic");
            decorationMenu.add(italicMenuItem);
            
            JMenuItem none1MenuItem = new JMenuItem("None");
            none1MenuItem.addActionListener(colorSelectionListener);
            if (renderer.getIsBold()==false && renderer.getIsItalic()==false) none1MenuItem.setEnabled(false);
            none1MenuItem.setActionCommand("none1");
            decorationMenu.add(none1MenuItem);
            
            JMenu otherDecorationMenu = new JMenu("Other");
            JMenuItem underlineMenuItem = new JMenuItem("Underline");
            if(renderer.getIsUnderline()) underlineMenuItem.setEnabled(false);
            underlineMenuItem.addActionListener(colorSelectionListener);
            underlineMenuItem.setActionCommand("underline");
            otherDecorationMenu.add(underlineMenuItem);
            
            JMenuItem strikethroughMenuItem = new JMenuItem("Strike through");
            if(renderer.getIsStrikethrough()) strikethroughMenuItem.setEnabled(false);
            strikethroughMenuItem.addActionListener(colorSelectionListener);
            strikethroughMenuItem.setActionCommand("strikethrough");
            otherDecorationMenu.add(strikethroughMenuItem);

            JMenuItem none2MenuItem = new JMenuItem("None");
            none2MenuItem.addActionListener(colorSelectionListener);
            if (renderer.getIsUnderline()==false && renderer.getIsStrikethrough()==false) none2MenuItem.setEnabled(false);
            none2MenuItem.setActionCommand("none2");
            otherDecorationMenu.add(none2MenuItem);
            
            decorationMenu.add(otherDecorationMenu);
            menu.add(decorationMenu);
            
            JMenu positionMenu = new JMenu("Position");
            JMenuItem superscriptMenuItem = new JMenuItem("Super");
            superscriptMenuItem.addActionListener(colorSelectionListener);
            if(renderer.getIsSuperscript()) superscriptMenuItem.setEnabled(false);
            superscriptMenuItem.setActionCommand("super");
            positionMenu.add(superscriptMenuItem);
            
            JMenuItem normalMenuItem = new JMenuItem("Normal");
            if(renderer.getIsSubscript()==false && renderer.getIsSuperscript()==false) normalMenuItem.setEnabled(false);
            normalMenuItem.addActionListener(colorSelectionListener);
            normalMenuItem.setActionCommand("normal");
            positionMenu.add(normalMenuItem);
            
            JMenuItem subscriptMenuItem = new JMenuItem("Sub");
            subscriptMenuItem.addActionListener(colorSelectionListener);
            if(renderer.getIsSubscript()) subscriptMenuItem.setEnabled(false);
            subscriptMenuItem.setActionCommand("sub");
            positionMenu.add(subscriptMenuItem);
                        
            menu.add(positionMenu);            
            menu.show((JLabel)me.getSource(),me.getX(),me.getY());
            //item.setFont();            
        }
    }*/
    
    /** Inner class for processing Popup menu selection events. */
    /*
    class ColorSelectionListener implements java.awt.event.ActionListener
    {        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) 
        {
            String command = actionEvent.getActionCommand();
            JMenuItem source = (JMenuItem) actionEvent.getSource();
            if (command.equals("foreground"))
            {
                if (source.getText().indexOf("transparent")!=-1)
                {
                    System.out.println("Transparent foreground selected!");
//                    renderer.setForegroundIsTransparent(true);
                    renderer.setForegroundColor(null);
                    nameLabel.setForeground(Color.black);
                }
                else
                {
                    System.out.println("Opaque foreground selected!");
//                    renderer.setForegroundIsTransparent(false);
                    renderer.setForegroundColor(source.getBackground());
                    nameLabel.setForeground(source.getBackground());
                }
            }
            else if (command.equals("background"))
            {
                if (source.getText().indexOf("transparent")!=-1)
                {
                    System.out.println("Transparent background selected!");
                    //renderer.setBackgroundIsTransparent(true);
                    renderer.setBackgroundColor(null);                   
                    nameLabel.setBackground(Color.white);
                }
                else
                {
                    System.out.println("Opaque background selected!");
                    //renderer.setBackgroundIsTransparent(false);
                    renderer.setBackgroundColor(source.getBackground());                    
                    nameLabel.setBackground(source.getBackground());
                }
            }
            else if (command.equals("handle"))
            {
                System.out.println("Setting handle to "+source.getBackground());
                renderer.setHandleColor(source.getBackground());
            }
            else if (command.equals("bold"))
            {
                renderer.setIsBold(true);
            }
            else if (command.equals("italic"))
            {
                renderer.setIsItalic(true);
            }
            else if (command.equals("none1"))
            {
                renderer.setIsBold(false);
                renderer.setIsItalic(false);
            }
            else if (command.equals("underline"))
            {
                renderer.setIsUnderline(true);
                renderer.setIsStrikethrough(false);
            }
            else if (command.equals("strikethrough"))
            {
                renderer.setIsUnderline(false);
                renderer.setIsStrikethrough(true);
            }
            else if (command.equals("none2"))
            {
                renderer.setIsUnderline(false);
                renderer.setIsStrikethrough(false);
            }
            else if (command.equals("super"))
            {
                renderer.setIsSuperscript(true);
                renderer.setIsSubscript(false);
            }
            else if (command.equals("sub"))
            {
                renderer.setIsSuperscript(false);
                renderer.setIsSubscript(true);
            }
            else if (command.equals("normal"))
            {
                renderer.setIsSuperscript(false);
                renderer.setIsSubscript(false);
            }
            
            updateNameLabelText();
            
            
            if (getCurrentDiscourse().getMMAX2().getAutoRefreshUponPanelAction())
            {
                getCurrentDiscourse().getCurrentMarkableChart().rerender();
            }                                                
        }        
    } 
    */
    /** This method parses the value of a span attribute and returns an Array with one Array per fragment. 
        Spans of the form word_x..word_y will be expanded to include all intermediate ids.
        Note: This works be retrieving the first element in the span and iterating until the last
        element in the span is found. That means that this does not assume the numerical ID parts to
        be integers. */
    private final static String[][] parseMarkableSpan(String span, DocumentImpl dom, MarkableLevel _level)
    {
        String currentspan="";
        ArrayList spanlist = new ArrayList();
        String[] fragArray= null;

        /* Get overall length of span attribute */
        int spanlen = span.length();

        /** Iterate over entire span String */
        for (int i=0;i<spanlen;i++)
        {
            /** Move to next span separator (i.e. ,) */
            if(span.charAt(i) != ',')
            {
                currentspan=currentspan+span.charAt(i);
                continue;
            }
            currentspan.trim();
            fragArray=parseMarkableSpanFragmentToArray(currentspan, dom, _level);
            spanlist.add(fragArray);
            currentspan="";
            fragArray=null;
        }// for
        
        currentspan.trim();
        fragArray=parseMarkableSpanFragmentToArray(currentspan,dom,_level);
        spanlist.add(fragArray);
        currentspan="";
        fragArray=null;
        
        return (String[][]) spanlist.toArray(new String[1][1]);
    }
    

    
    /** This method parses the value of a span fragment (either word_1..word_4 or word_3) and returns an array of all elements.
        Spans of the form word_x..word_y are expanded to include all intermediate ids. 
        Note: This works be retrieving the first element in the span and iterating until the last
        element in the span is found. That means that this does not assume the numerical ID parts to
        be integers.*/
    private final static String[] parseMarkableSpanFragmentToArray(String span, DocumentImpl dom, MarkableLevel _level)
    {        
	String firstIDString;
	String lastIDString;

        // Create list to receive intermediate result
	ArrayList newWordsIDList = new ArrayList();

	if (span.indexOf("..") == -1)
	{
            /* No .. found, so span is one element only */
            newWordsIDList.add(span);
	}
        else
        {            
            /* Extract leftmost id string from span */
            firstIDString=span.substring(0,span.indexOf(".."));
            /* Extract rightmost id string from span */
            lastIDString=span.substring(span.lastIndexOf("..") + 2);

            double lastIDValue = Double.parseDouble(lastIDString.substring(lastIDString.indexOf("_")+1));
            // Get first element in span  
            Node currentNode = getWordNodeOrClosestSuccessor(dom, firstIDString);
            
            
            if (currentNode == null)
            {                
                String message = "A markable on level "+_level.getMarkableLevelName()+" references an element with id "+firstIDString+",\n";
                message = message + "but an element with ID "+firstIDString+" could not be found!\n";
                message = message + "This is a serious error, and might be caused by a missing DTD declaration in the word file.";
                JOptionPane.showMessageDialog(null,message,"ID not found !",JOptionPane.ERROR_MESSAGE);
                System.exit(0);                                
            }
             
            newWordsIDList.add(firstIDString);
            Node nextNode = null;
            while(true)
            {
                // Set next to next after current
                nextNode = currentNode.getNextSibling();
                if (nextNode == null)
                {
                    break;
                }
                // If next is no element, do nothing with next node
                if (nextNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    // 
                    currentNode = nextNode;
                    continue;
                }
                
                // nextNode is an element node
                // add its id to the list
                newWordsIDList.add(nextNode.getAttributes().getNamedItem("id").getNodeValue());
                String justAddedID = nextNode.getAttributes().getNamedItem("id").getNodeValue();
                double justAddedValue = Double.parseDouble(justAddedID.substring(justAddedID.indexOf("_")+1));
                // Break if just added was last in span
                if (justAddedID.equalsIgnoreCase(lastIDString))
                {
                    break;
                }
                // ... or even later
                if (justAddedValue > lastIDValue)
                {
                    newWordsIDList.remove(newWordsIDList.size()-1);
                    break;
                }                
                currentNode = nextNode;
            }                        
        }
        
	return (String[]) newWordsIDList.toArray(new String[0]);
   }    
    
    
    private final static Node getWordNodeOrClosestSuccessor(DocumentImpl dom, String id)
    {
        Node result = null;
        result = dom.getElementById(id);
        if (result == null)
        {
            // The node with id id could not be retrieved
            // Extract name space from id
            String nameSpace = id.substring(0,id.indexOf("_"));
            int requiredID = Integer.parseInt(id.substring(id.indexOf("_")+1));
            Node node = dom.getDocumentElement();
            node = node.getFirstChild();
            // Iterate over all elements in supplied dom
            while(true)
            {       
                if (node != null && node.getNodeType()==Node.ELEMENT_NODE && node.getAttributes() != null)
                {
                    if (node.getAttributes() != null)
                    {
                        if (node.getAttributes().getNamedItem("id")!= null)
                        {
                            // The current element has an id
                            String currentIDString =node.getAttributes().getNamedItem("id").getNodeValue();
                            // get numerical value
                            int currentIDNum = Integer.parseInt(currentIDString.substring(currentIDString.indexOf("_")+1));
                            // If current element has higher id then required one, break
                            if (currentIDNum >= requiredID)
                            {
                                result=node;
                                break;
                            }
                        }
                    }
                }
                else if (node == null)
                {
                    break;
                }            
                Node tmpnode = node.getNextSibling();
                node = tmpnode;
            }
        }
        return result;
    }    
}
