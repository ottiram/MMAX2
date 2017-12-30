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

package org.eml.MMAX2.gui.display;

// XML Parsing
import java.awt.Color;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.xerces.parsers.DOMParser;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.Renderable;
import org.eml.MMAX2.annotation.markables.SimpleMarkableCustomization;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MarkableLevelRenderer 
{
    /** Reference to the MarkableLayer object that this MarkableLayerRenderer is associated with. */
    private MarkableLevel level=null;
            
    private Color backgroundColor = null;
    private Color foregroundColor = null;
    private Color handleColor = null;
                   
    private SimpleAttributeSet defaultActiveHandleStyle=new SimpleAttributeSet();    
            
    private boolean foregroundIsTransparent = true;
    private boolean backgroundIsTransparent = true;
    
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private boolean subscript = false;
    private boolean superscript = false;

    private String customizationFileName = "";
    private SimpleMarkableCustomization[] customizations = new SimpleMarkableCustomization[0];            
    private int customizationCount=0;
        
    /** Creates new MarkableLayerRenderer */ 
    public MarkableLevelRenderer(MarkableLevel _level, String _customizationFileName)     
    {
        /** Set reference to associated MarkableLayer. */
        level = _level;        
                        
        /** Initialize default rendering styles. */
        
        if (handleColor != null)
        {
            StyleConstants.setBackground(defaultActiveHandleStyle, handleColor);
        }
        else
        {
            /** Use foreground Color as background color for MarkableHandle hovering, if no explicit color is set for handles */
            if (foregroundIsTransparent==false)
            {
                StyleConstants.setBackground(defaultActiveHandleStyle, foregroundColor);
            }
            else
            {
                StyleConstants.setBackground(defaultActiveHandleStyle, Color.black);
            }
        }
        
        customizationFileName = _customizationFileName;
    }
      
    public final void destroyDependentComponents()
    {
        level = null;
        customizations = null;
        
    }
    
    
    protected void finalize()
    {
        System.err.println("MarkableLevelRenderer is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }
    
    
    public final int getCustomizationCount()
    {
        return customizationCount;
    }
    
    public final void updateSimpleMarkableCustomizations(boolean status)
    {        
        if (status == false)
        {
            customizationCount = 0;
        }
        else
        {
            if (customizationFileName.equals("")==false)
            {
                customizations = null;
                customizations = new SimpleMarkableCustomization[0];            
                DOMParser parser = new DOMParser();            
                Document customizationDOM = null;            
                try
                {
                    parser.setFeature("http://xml.org/sax/features/validation",false);
                }
                catch (org.xml.sax.SAXNotRecognizedException ex)
                {
                    ex.printStackTrace();            
                    return;
                }
                catch (org.xml.sax.SAXNotSupportedException ex)
                {
                    ex.printStackTrace();
                    return;
                }

                try
                {
                    //parser.parse(new InputSource("FILE:"+customizationFileName));
                    parser.parse(new InputSource(new File(customizationFileName).toURI().toString()));
                }
                catch (org.xml.sax.SAXParseException exception)
                {
                    String error = "Line: "+exception.getLineNumber()+" Column: "+exception.getColumnNumber()+"\n"+exception.toString();            
                    JOptionPane.showMessageDialog(null,error,"MarkableFileLoader: "+customizationFileName,JOptionPane.ERROR_MESSAGE);
                }                
                catch (org.xml.sax.SAXException exception)
                {
                    String error = exception.toString();
                    JOptionPane.showMessageDialog(null,error,"MarkableFileLoader: "+customizationFileName,JOptionPane.ERROR_MESSAGE);
                }
                catch (java.io.IOException exception)
                {
                    String error = exception.toString();
                    JOptionPane.showMessageDialog(null,error,"MarkableFileLoader: "+customizationFileName,JOptionPane.ERROR_MESSAGE);
                }

                customizationDOM =parser.getDocument();
                NodeList allRules = customizationDOM.getElementsByTagName("rule");

                int custNum = allRules.getLength();
                customizations = new SimpleMarkableCustomization[custNum];

                for (int p=0;p<custNum;p++)
                {
                    String pattern = "";
                    String style="";
                    String connector = "";
                    try
                    {
                        pattern =allRules.item(p).getAttributes().getNamedItem("pattern").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                     // 
                    }
                    try
                    {
                        style =allRules.item(p).getAttributes().getNamedItem("style").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        // 
                    }
                    try
                    {
                        connector =allRules.item(p).getAttributes().getNamedItem("connector").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        connector = "and";
                    }
                    int numConnector = MMAX2Constants.AND;
                    if (connector.equalsIgnoreCase("or"))numConnector = MMAX2Constants.OR;
                    customizations[p] = new SimpleMarkableCustomization(level, pattern,numConnector,MMAX2Utils.createSimpleAttributeSet(style,false));                
                } // for p
            }// if not ""                        
            customizationCount = customizations.length;         
        }
    }
    
    public final Color getHandleColor()
    {
        return StyleConstants.getBackground(defaultActiveHandleStyle);
    }
    
    public final void render(Markable markable, int mode)
    {
        SimpleAttributeSet styleToUse = null;
        
        // Get references to document, textpane and markable chart once 
        MMAX2Document doc = level.getCurrentDiscourse().getDisplayDocument();
        MMAX2TextPane pane = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane();
        MarkableChart chart = level.getCurrentDiscourse().getCurrentMarkableChart();

        int[] displayStartPositions = null;
        int[] displayEndPositions = null;
        String[][] fragments = null;
        
        if (markable != null)
        {
            // Get each fragment and its resp. display start and end position
            displayStartPositions = markable.getDisplayStartPositions();
            displayEndPositions = markable.getDisplayEndPositions();
            fragments = markable.getFragments(); // ok
        }
        // The current markable was deleted
        if (mode == MMAX2Constants.RENDER_REMOVED)
        {
            String[] currentFrag = null;
            doc.startChanges(markable);
            // Get array of fragments
            fragments = markable.getFragments();
            // Iterate over all fragments
            for (int z=0;z<fragments.length;z++)
            {
                // Get current fragment
                currentFrag = fragments[z];
                int discPos =0;
                // Iterate over current fragment
                for (int r=0;r<currentFrag.length;r++)
                {                    
                    // Get attributes to be displayed in this position.
                    // Since the markable has been removed already, it will not yield any attributes, 
                    // and thus will not show up any more.                     
                    styleToUse=chart.getTopAttributesAtDiscourseElement(currentFrag[r]);
                    discPos = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentFrag[r]);
                    doc.bulkApplyStyleToDiscourseElement(level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(discPos),styleToUse,true);
                }
            }
            doc.commitChanges();
        }
        else if (mode == MMAX2Constants.RENDER_IN_SET)
        {
            // Modified for discont: 30.01.05
            // First, reapply currently valid attributes
            // This will enforce any customization-dependent attribute changes, including size and font
            markable.renderMe(MMAX2Constants.RERENDER_THIS);            
            
            // Then, add set-related selection attributes on top
            styleToUse = getAttributesForMarkable(markable);
            
            if (styleToUse.isDefined(StyleConstants.FontSize)==false)
            {
                // If no attribute-dependent size exists, use current display size
                StyleConstants.setFontSize(styleToUse,MMAX2.currentDisplayFontSize);
            }
            if (styleToUse.isDefined(StyleConstants.FontFamily)==false)
            {
                // If no attribute-dependent size exists, use current display size
                StyleConstants.setFontFamily(styleToUse,MMAX2.currentDisplayFontName);
            }
            
            StyleConstants.setBackground(styleToUse,Color.lightGray);
            
            // Iterate over all fragments of currentmarkable
            for (int b=0;b<displayStartPositions.length;b++)
            {
                doc.bulkApplyStyleToDisplaySpanBackground(displayStartPositions[b],displayEndPositions[b]-displayStartPositions[b]+1, styleToUse);
            }
        }
        else if (mode == MMAX2Constants.RENDER_IN_SEARCHRESULT)
        {
            // Modified for discont: 30.01.05
            doc.startChanges(markable);

            styleToUse = getAttributesForMarkable(markable);
            if (styleToUse.isDefined(StyleConstants.FontSize)==false)
            {
                // If no attribute-dependent size exists, use current display size
                StyleConstants.setFontSize(styleToUse,MMAX2.currentDisplayFontSize);
            }
            if (styleToUse.isDefined(StyleConstants.FontFamily)==false)
            {
                // If no attribute-dependent size exists, use current display size
                StyleConstants.setFontFamily(styleToUse,MMAX2.currentDisplayFontName);
            }
            
            StyleConstants.setBackground(styleToUse,Color.orange);
            
            // Iterate over all fragments of currentmarkable
            for (int b=0;b<displayStartPositions.length;b++)
            {            
                doc.bulkApplyStyleToDisplaySpanBackground(displayStartPositions[b],displayEndPositions[b]-displayStartPositions[b]+1, styleToUse);
            }
            doc.commitChanges();
        }
        else if (mode == MMAX2Constants.RERENDER_THIS)
        {
            // No mod for discont necessary
            String[] currentFragment = null;
            String currentDE = "";
            int currentStart = 0;
            for (int z=0;z<fragments.length;z++)
            {
                // Get current fragment
                currentFragment = fragments[z];
                // Iterate over each element in current fragment
                for (int p=0;p<currentFragment.length;p++)
                {
                    currentDE = currentFragment[p]; //                    
                    // New: This will add current font size as default
                    styleToUse=chart.getTopAttributesAtDiscourseElement(currentDE);
                    
                    currentStart = level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE));
                    doc.bulkApplyStyleToDiscourseElement(currentStart,styleToUse,true);
                }// for each element in fragment
            }// for each fragment                
        }
        else if (mode == MMAX2Constants.RERENDER_EVERYTHING)
        {
            // No mod for discont necessary
            // The entire display is to be rerendered, e.g. as the result of a change in the layer hierarchy
            // Get ordered array of all Discourse Elements' IDs
            String[] allDiscourseElements = level.getCurrentDiscourse().getAllDiscourseElementIDs();            
            doc.startChanges(0,doc.getLength());
            // Iterate over all DiscourseElements
            for (int z=0;z<allDiscourseElements.length;z++)
            {
                // Get start position of DE at discourse position z. This is the first letter of the DE, 
                // and suffices to retrieve the run in the document later
                int currentStart = level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(z);
                // New May 21st: This will add current font size and name as default
                SimpleAttributeSet attribs = chart.getTopAttributesAtDiscourseElement(allDiscourseElements[z]);               
                
                doc.bulkApplyStyleToDiscourseElement(currentStart,attribs,true);
            }           

            // Array of all displayassociations (i.e. things underlying handles)
            Integer[] allHandlePositions = level.getCurrentDiscourse().getAllDisplayAssociations();
            Markable tempM = null;
            SimpleAttributeSet handleStyle = null;
            SimpleAttributeSet markableAttributes = null;
            if (allHandlePositions != null)
            {
                // Update handles only if any exist
                // Iterate over all handles
                for (int q=0;q<allHandlePositions.length;q++)
                {                    
                    // Create new style to use for handle
                    handleStyle = new SimpleAttributeSet();
                    // Get current associates markable
                    tempM = (Markable) level.getCurrentDiscourse().getMarkableAtDisplayAssociation(((Integer)allHandlePositions[q]).intValue());                    
                    // Get attribute dependent style for current markable
                    markableAttributes = tempM.getAttributedependentStyle();
                    // Get attribute-dependent color from associated markable, if any is defined
                    if (markableAttributes.isDefined("handles"))
                    {
                        StyleConstants.setForeground(handleStyle, (Color) markableAttributes.getAttribute("handles"));
                    }
                    else                        
                    {
                        // Use black as default
                        StyleConstants.setForeground(handleStyle,Color.BLACK);
                    }
                    
                    if (markableAttributes.isDefined(StyleConstants.FontSize))
                    {
                        StyleConstants.setFontSize(handleStyle, StyleConstants.getFontSize(markableAttributes));
                    }
                    else                        
                    {
                        StyleConstants.setFontSize(handleStyle, MMAX2.currentDisplayFontSize);
                    }
                    if (markableAttributes.isDefined(StyleConstants.FontFamily))
                    {
                        StyleConstants.setFontFamily(handleStyle, StyleConstants.getFontFamily(markableAttributes));
                    }
                    else                        
                    {
                        StyleConstants.setFontFamily(handleStyle, MMAX2.currentDisplayFontName);
                    }
                    
                    int[] currentHandles = tempM.getRightHandlePositions();                    
                    if (currentHandles != null)
                    {
                        for (int u=0;u<currentHandles.length;u++)
                        {
                            doc.bulkApplyStyleToMarkableHandle(currentHandles[u],handleStyle,false);                            
                        }
                    }
                    currentHandles = tempM.getLeftHandlePositions();                    
                    if (currentHandles != null)
                    {
                        for (int u=0;u<currentHandles.length;u++)
                        {
                            doc.bulkApplyStyleToMarkableHandle(currentHandles[u],handleStyle,false);                            
                        }
                    }                    
                }
            }
            doc.commitChanges();
        }
        
        else if (mode==MMAX2Constants.RENDER_SELECTED) // checked
        {               
            // modified for discont: 30.01.05
            doc.startChanges(markable);
            // First simply reapply currently valid attributes
            markable.renderMe(MMAX2Constants.RERENDER_THIS);            
            
            // Get style for selection (normally: backgroundcolor)            
            styleToUse = new SimpleAttributeSet(level.getCurrentDiscourse().getMMAX2().getSelectedStyle());
            
            // Iterate over all fragments of currentmarkable
            for (int b=0;b<displayStartPositions.length;b++)
            {
                // Apply style for selection to entire display from first to last character in fragment, incl. spaces
                doc.bulkApplyStyleToDisplaySpanBackground(displayStartPositions[b],displayEndPositions[b]-displayStartPositions[b]+1, styleToUse);
            }
            doc.commitChanges();            
            
            // Get all MarkableSetRelations for current markable
            MarkableRelation[] thisMarkablesMarkableSetRelations = this.level.getActiveMarkableSetRelationsForMarkable(markable);
            if (thisMarkablesMarkableSetRelations != null)
            {
                // The current Markable is in at least one MarkableSet relation
                // Iterate over all MarkableSetRelations for current Markable
                for (int e=0;e<thisMarkablesMarkableSetRelations.length;e++)
                {
                    MarkableRelation currentRelation = (MarkableRelation) thisMarkablesMarkableSetRelations[e];
                    String currentAttributeName = currentRelation.getAttributeName();
                    level.getCurrentDiscourse().getMMAX2().putOnRenderingList((Renderable)currentRelation.getMarkableSetWithAttributeValue(markable.getAttributeValue(currentAttributeName)));
                }
            }

            // Get all MarkablePointerRelations for current markable            
            MarkableRelation[] thisMarkablesMarkablePointerRelations = level.getActiveMarkablePointerRelationsForSourceMarkable(markable);
            if (thisMarkablesMarkablePointerRelations != null)
            {
                // The current Markable is the source of a markable pointer relation
                // Iterate over all MarkablePointerRelations for current Markable
                // Note: This considers only those relations that are activated by the current markable
                // being selected. They do not consider other, *permanently* displayed pointers.
                int flagLevel =0;
                for (int e=0;e<thisMarkablesMarkablePointerRelations.length;e++)
                {
                    // Get relation that models the current pointer relation
                    MarkableRelation currentRelation = (MarkableRelation) thisMarkablesMarkablePointerRelations[e];
                    // Get set to display
                    Renderable currentRenderable = currentRelation.getMarkablePointerForSourceMarkable(markable);
                    // Set flagLevel in relation to all relations caused by current attribute being selected
                    currentRenderable.setFlagLevel(flagLevel);
                    // Increase flaglevel for as many levels as just added renderable has elements
                    flagLevel += ((MarkablePointer)currentRenderable).getSize();
                    level.getCurrentDiscourse().getMMAX2().putOnRenderingList((Renderable)currentRenderable);
                }
            }                        
        }
        else if (mode==MMAX2Constants.RENDER_UNSELECTED) // checked
        {
            // Mod for discont: 30.01.05
            // A selection is to be removed from markable
            // Just apply normal unselected, but potentially customized attribs
            // Problem: spaces between DiscourseElements remain highlighted, 
            // so first set everything to Color.white ...
            
        	doc.startChanges(markable); // added July 18, 2008
        	
            styleToUse = getAttributesForMarkable(markable);
            if (styleToUse.isDefined(StyleConstants.FontSize)==false)
            {
                // If no attribute-dependent size exists, use current display size
                StyleConstants.setFontSize(styleToUse,MMAX2.currentDisplayFontSize);
            }
                        
            StyleConstants.setBackground(styleToUse,Color.white);            
            StyleConstants.setFontFamily(styleToUse, level.getCurrentDiscourse().getMMAX2().currentDisplayFontName);
            // Iterate over all fragments of currentmarkable
            for (int b=0;b<displayStartPositions.length;b++)
            {            
                doc.bulkApplyStyleToDisplaySpanBackground(displayStartPositions[b],displayEndPositions[b]-displayStartPositions[b]+1, styleToUse);
            }
            
            // Then simply reapply currently valid attributes
            markable.renderMe(MMAX2Constants.RERENDER_THIS);    
            
            doc.commitChanges(); // added July 18, 2008
            
        }        
        else if (mode==MMAX2Constants.RENDER_ALL_HANDLES)
        {                        
            // We want to highlight the handles of all fragments
            // Use the defaultStyle for MarkableHandle rendering
            styleToUse = defaultActiveHandleStyle;
            StyleConstants.setFontFamily(styleToUse, level.getCurrentDiscourse().getMMAX2().currentDisplayFontName);
            
            // Get start and end for each handle for current markable
            int[] startHandles = markable.getLeftHandlePositions();
            int[] endHandles = markable.getRightHandlePositions();                
            
            /** Iterate over all fragments of current Markable */
            for (int z=0;z<startHandles.length;z++)
            {
                // Set attributes so that handles are highlighted 
                doc.setCharacterAttributes(startHandles[z],1,styleToUse,false);
                doc.setCharacterAttributes(endHandles[z],1,styleToUse,false);
            }// for all fragments of current Markable
            
        }        
        else if (mode==MMAX2Constants.RENDER_NO_HANDLES) // Checked
        {
            // We want to hide the handles of all fragments
            // Create new (empty) style, we want to reset colors only, therefore use false in setCharacterAttributes
            styleToUse = new SimpleAttributeSet();            

            StyleConstants.setFontFamily(styleToUse, level.getCurrentDiscourse().getMMAX2().currentDisplayFontName);
            
            int[] startHandles = markable.getLeftHandlePositions();
            int[] endHandles = markable.getRightHandlePositions();            
                                    
            /** Iterate over all fragments in current markable */
            for (int z=0;z<startHandles.length;z++)
            {                
                StyleConstants.setBackground(styleToUse,level.getCurrentDiscourse().getCurrentMarkableChart().getPrevailingBackgroundColorForMarkableHandle(startHandles[z]));
                // Display modification are few, so do it non-bulk
                doc.setCharacterAttributes(startHandles[z],1,styleToUse,false);                
                StyleConstants.setBackground(styleToUse,level.getCurrentDiscourse().getCurrentMarkableChart().getPrevailingBackgroundColorForMarkableHandle(endHandles[z]));
                // Display modification are few, so do it non-bulk
                doc.setCharacterAttributes(endHandles[z],1,styleToUse,false);
            }                            
        }        
        else if (mode==MMAX2Constants.RENDER_CURRENT_HANDLE)
        {                        
            // We want to highlight the handles of the current fragment only 
            
            /** First reset highlighting of the entire set of handles. */
            // Create new (empty) style, we want to reset colors only, therefore use false in setCharacterAttributes
            styleToUse = new SimpleAttributeSet();            

            StyleConstants.setFontFamily(styleToUse, level.getCurrentDiscourse().getMMAX2().currentDisplayFontName);
            
            int[] startHandles = markable.getLeftHandlePositions();
            int[] endHandles = markable.getRightHandlePositions();
            
            /** Iterate over all fragments in current markable */
            for (int z=0;z<startHandles.length;z++)
            {
                StyleConstants.setBackground(styleToUse,level.getCurrentDiscourse().getCurrentMarkableChart().getPrevailingBackgroundColorForMarkableHandle(startHandles[z]));
                doc.setCharacterAttributes(startHandles[z],1,styleToUse,false);
                StyleConstants.setBackground(styleToUse,level.getCurrentDiscourse().getCurrentMarkableChart().getPrevailingBackgroundColorForMarkableHandle(endHandles[z]));
                doc.setCharacterAttributes(endHandles[z],1,styleToUse,false);
            }                
            styleToUse = defaultActiveHandleStyle;
            
            int FragStart = -1;
            int FragEnd = -1;
            int currentStart = -1;
            int currentEnd = -1;
            int currentMinIndex = -1;
            int currentMinDistance = Integer.MAX_VALUE;
            int distToNextLeftHandle = -1;
            int distToNextRightHandle = -1;
            
            for (int z=0;z<startHandles.length;z++)
            {                
                currentStart = startHandles[z];
                currentEnd = endHandles[z];
                
                if (currentStart == pane.getCurrentDot() || currentEnd == pane.getCurrentDot())
                {
                    // The mouse was directly on a left or right handle
                    FragStart = currentStart;
                    FragEnd = currentEnd;
                    doc.setCharacterAttributes(FragStart,1,styleToUse,false);
                    doc.setCharacterAttributes(FragEnd,1,styleToUse,false);                   
                    break;
                }
                else
                {
                    // Try to find the handle which is closest to currentDot !
                    // Determine if a left or a right handle is closest to currentDot
                    distToNextLeftHandle = Math.abs(currentStart-pane.getCurrentDot());
                    distToNextRightHandle = Math.abs(currentEnd-pane.getCurrentDot());
                    
                    if (distToNextRightHandle < distToNextLeftHandle)
                    {
                        // The closest handle is a rightHandle
                        if (distToNextRightHandle < currentMinDistance)
                        {
                            currentMinDistance = distToNextRightHandle;
                            currentMinIndex = z;
                        }                                                                                                       
                    }
                    else
                    {
                        // The closest handle is a leftHandle
                        if (distToNextLeftHandle < currentMinDistance)
                        {
                            currentMinDistance = distToNextLeftHandle;
                            currentMinIndex = z;
                        }                        
                    }
                }                
            }// for all handles
            if (FragStart == -1 && FragEnd == -1)
            {
                // The fragment was found by determining the min dist
                if (currentMinIndex != -1)
                {
                    FragStart = startHandles[currentMinIndex];
                    FragEnd = endHandles[currentMinIndex];
                    doc.setCharacterAttributes(FragStart,1,defaultActiveHandleStyle,false);
                    doc.setCharacterAttributes(FragEnd,1,defaultActiveHandleStyle,false);
                }
                else
                {
                    System.err.println("Couldn't determine fragment!");
                }
            }
        }
    }    
    
    public final void removeHandlesAtDisplayPositions(Integer[] positions)
    {
        if (positions.length==0) return;
        MMAX2Document doc = level.getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(positions[0].intValue(),positions[positions.length-1].intValue());
        SimpleAttributeSet attribs = new SimpleAttributeSet();
        // Todo: Make sure deleted handles appear in correct size and font as well
        StyleConstants.setFontSize(attribs, level.getCurrentDiscourse().getMMAX2().currentDisplayFontSize);
        StyleConstants.setFontFamily(attribs, level.getCurrentDiscourse().getMMAX2().currentDisplayFontName);
        
        StyleConstants.setForeground(attribs,Color.lightGray);
        StyleConstants.setBackground(attribs,Color.white);
        StyleConstants.setStrikeThrough(attribs,true);
        for (int o=0;o<positions.length;o++)
        {
            doc.bulkApplyStyleToMarkableHandle(positions[o].intValue(),attribs,true);
        }
        doc.commitChanges();
    }
    
    /** This method returns a SimpleAttributeSet with the attributes that Markable _markable has depending on 
        its attributes. The method uses a (potentially empty) set of SimpleMarkableCustomization objects. */
    public final SimpleAttributeSet getAttributesForMarkable(Markable _markable)
    {
        SimpleAttributeSet tempResult = null;
        SimpleAttributeSet result = new SimpleAttributeSet();
        SimpleAttributeSet intermediateResult = null;
        
        if (customizationCount != 0)
        {                   
            // There is at least one customization for Markables on this level. 
            // Iterate over all customizations for this level (in order of precedence, i.e. top to bottom !)
            for (int u=0;u<customizationCount;u++)
            {
                // Does the current customization match the current markable?
                tempResult = customizations[u].matches(_markable);
                if (tempResult != null)
                {
                    // A non-null tempResult was returned, which contains the attribute-dependent styles for the current Markable
                    // Merge them with the attributes collected so far (none in first run)
                    intermediateResult = mergeAttributes(tempResult,result);
                    result = intermediateResult;
                    intermediateResult = null;
                }
            }
        }
        return result;
    }

   
    public final static SimpleAttributeSet mergeAttributes(SimpleAttributeSet superiorSet, SimpleAttributeSet inferiorSet)
    {
        // Create a new set from the supplied ones, adding those elements that are not contradictory
        // If mutually exlusive attributes occur, take values from superiorSet
        // Strategy: Take all in superior set and add non-contradictory ones from inferior set
        
        SimpleAttributeSet resultSet = new SimpleAttributeSet(superiorSet);
        
        if (StyleConstants.isSuperscript(inferiorSet))
        {
            // The inferior set has superscript on
            if (StyleConstants.isSubscript(resultSet)==false)
            {
                // Since the superior set does not have sth. else in the same category, keep it
                StyleConstants.setSuperscript(resultSet,true);
            }
        }

        if (superiorSet.isDefined("handles")==false)
        {
            if (inferiorSet.isDefined("handles"))
            {
                resultSet.addAttribute("handles", inferiorSet.getAttribute("handles"));
            }
        }
        
        if (StyleConstants.isSubscript(inferiorSet))
        {
            // The inferior set has subscript on
            if (StyleConstants.isSuperscript(resultSet)==false)
            {
                // Since the superior set does not have sth. else in the same category, keep it
                StyleConstants.setSubscript(resultSet,true);
            }
        }

        if (StyleConstants.isStrikeThrough(inferiorSet))
        {
            // The inferior set has strikethrough on
            if (StyleConstants.isUnderline(resultSet)==false)
            {
                // Since the superior set does not have sth. else in the same category, keep it
                StyleConstants.setStrikeThrough(resultSet,true);
            }
        }

        if (StyleConstants.isUnderline(inferiorSet))
        {
            // The inferior set has underline on
            if (StyleConstants.isStrikeThrough(resultSet)==false)
            {
                // Since the superior set does not have sth. else in the same category, keep it
                StyleConstants.setUnderline(resultSet,true);
            }
        }

        if (StyleConstants.isBold(inferiorSet))
        {
            StyleConstants.setBold(resultSet,true);
        }
        
        if (StyleConstants.isItalic(inferiorSet))
        {
            StyleConstants.setItalic(resultSet,true);
        }
        
        
        if (superiorSet.isDefined(StyleConstants.Foreground))
        {
            StyleConstants.setForeground(resultSet, StyleConstants.getForeground(superiorSet));            
        }
        else if (inferiorSet.isDefined(StyleConstants.Foreground))
        {
            StyleConstants.setForeground(resultSet, StyleConstants.getForeground(inferiorSet));            
        }


        if (superiorSet.isDefined(StyleConstants.Background))
        {
            StyleConstants.setBackground(resultSet, StyleConstants.getBackground(superiorSet));            
        }
        else if (inferiorSet.isDefined(StyleConstants.Background))
        {
            StyleConstants.setBackground(resultSet, StyleConstants.getBackground(inferiorSet));            
        }
        
        if (inferiorSet.isDefined(StyleConstants.FontSize))
        {
            if (superiorSet.isDefined(StyleConstants.FontSize)==false)
            {
                StyleConstants.setFontSize(resultSet,StyleConstants.getFontSize(inferiorSet));
            }
        }
       
        return resultSet;        
    }
    
    /********************************/
  
    public final void setHandleColor(Color _color)
    {
        StyleConstants.setBackground(defaultActiveHandleStyle, _color);
    }
    
    public final void setBackgroundColor(Color _color)
    {
        backgroundColor =_color;
        if (_color == null)
        {
            backgroundIsTransparent = true;
        }
        else
        {
            backgroundIsTransparent = false;
        }
    }    
    public final Color getBackgroundColor()
    {
        return backgroundColor;
    }
    
    public final void setForegroundColor(Color _color)
    {
        foregroundColor =_color;
        if (_color == null)
        {
            foregroundIsTransparent = true;            
        }
        else
        {
            foregroundIsTransparent = false;
        }
    }    
    
    public final Color getForegroundColor()
    {
        return foregroundColor;
    }
        
    public final boolean getForegroundIsTransparent()
    {
        return foregroundIsTransparent;
    }

    public final boolean getBackgroundIsTransparent()
    {
        return backgroundIsTransparent;
    }    
    
    public final boolean getIsBold()
    {
        return bold;
    }
    public final void setIsBold(boolean status)
    {
        bold = status;        
    }
    
    public final boolean getIsItalic()
    {
        return italic;
    }
    public final void setIsItalic(boolean status)
    {
        italic = status;
    }
    
    public final boolean getIsUnderline()
    {
        return underline;
    }
    public final void setIsUnderline(boolean status)
    {
        underline = status;        
    }
    
    public final boolean getIsSubscript()
    {
        return subscript;
    }
    public final void setIsSubscript(boolean status)
    {
        subscript = status;
    }
    
    public final boolean getIsSuperscript()
    {
        return superscript;
    }
    public final void setIsSuperscript(boolean status)
    {
        superscript = status;
    }
    
    public final boolean getIsStrikethrough()
    {
        return strikethrough;
    }
    public final void setIsStrikethrough(boolean status)
    {
        strikethrough = status;
    }     
    
}
