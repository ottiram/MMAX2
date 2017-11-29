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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.xpath.NodeSet;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.gui.display.MMAX2ActionSelector;
import org.eml.MMAX2.gui.display.MMAX2LevelSelector;
import org.eml.MMAX2.gui.display.MMAX2OneClickAnnotationSelector;
import org.eml.MMAX2.gui.display.MarkableLevelControlPanel;
import org.eml.MMAX2.gui.display.MarkableLevelRenderer;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.gui.windows.MMAX2AttributePanelContainer;
import org.eml.MMAX2.gui.windows.MMAX2QueryWindow;
import org.eml.MMAX2.gui.windows.MarkableLevelControlWindow;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.Element;

public class MarkableChart 
{
    
    MMAX2ActionSelector selector = null;    
    MMAX2OneClickAnnotationSelector oneClickSelector = null;
        
    /** Reference to MarkableLayerControlPanel controlling this MarkableChart object. */
    public MarkableLevelControlPanel currentLevelControlPanel = null;        
    public MarkableLevelControlWindow currentLevelControlWindow = null;
    public MMAX2AttributePanelContainer attributePanelContainer = null;
    
    /** This HashMap contains all MarkableLevels in this MarkableChart, mapped to their level names as hash keys. */
    private HashMap levels = null;   
    /** Array of MarkableLevels contained in this MarkableChart, in relevant order. */
    private MarkableLevel[] orderedLevels = null;
    /** Reference to MMAX2Discourse object that this MarkableChart belongs to. */
    private MMAX2Discourse currentDiscourse = null;    
    /** Number of MarkableLayers in this MarkableChart. */
    private int size = 0;

    private int nextFreeMarkableSetNum=0;
    private int nextFreeMarkableIDNum=0;
    
    private int selectionStart=0;
    private int selectionEnd=0;
    
    private int dotsDiscoursePosition=0;
    private int marksDiscoursePosition=0;
    
    /** Creates new MarkableChart */
    public MarkableChart(MMAX2Discourse _discourse)
    {
        currentLevelControlPanel = new MarkableLevelControlPanel(_discourse);
        currentLevelControlWindow = null;
        try
        {
        	currentLevelControlWindow = new MarkableLevelControlWindow(currentLevelControlPanel);
        }
        catch (java.awt.HeadlessException ex)
        {
        	
        }
        currentDiscourse = _discourse;
        levels = new HashMap();
        orderedLevels = new MarkableLevel[0];
        size = 0;
        if (currentDiscourse.getHasGUI()) 
        {
            attributePanelContainer = new MMAX2AttributePanelContainer();
        }
    }

    public final int getSelectionStart()
    {
        return selectionStart;
    }
    
    public final int getSelectionEnd()
    {
        return selectionEnd;
    }

    
    public final void initAnnotationHints()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).getCurrentAnnotationScheme().setMMAX2(currentDiscourse.getMMAX2());
        }         
    }
    
    public final void saveAllMarkableLevels()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).saveMarkables("",false);
        } 
    }    

    /*
    public final void saveAllTransposedMarkableLevels(String oldLang, ArrayList absoluteWords)
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).saveTransposedMarkables(oldLang,absoluteWords);
        } 
    }    
    */
    
    public final void updateAllMarkableLevels()
    {
        for (int z=0;z<this.size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).updateMarkables();
        }  
    }    
    
    
    public final MMAX2Discourse getCurrentDiscourse()
    {
        return currentDiscourse;
    }
    
    public final Markable getMarkableByID(String nameSpacedID, MarkableLevel defaultLevel)
    {
        Markable result = null;
        if (nameSpacedID.indexOf(":")==-1)
        {
            result = defaultLevel.getMarkableByID(nameSpacedID);
        }
        else
        {
            String levelName = nameSpacedID.substring(0,nameSpacedID.indexOf(":"));
            String id = nameSpacedID.substring(nameSpacedID.indexOf(":")+1);
            result = getMarkableLevelByName(levelName, false).getMarkableByID(id);
        }                
        return result;
    }
    
    
    /** This method is called from the CaretListener when a caret event with unequal dot and mark values, i.e. a selection,
        has been detected. */
    public final void selectionOccurred(int dot, int mark)
    {        
        // Only if at least one level is available
        if (orderedLevels.length > 0)
        {
            MMAX2 mmax2 = this.currentDiscourse.getMMAX2();
            Rectangle tempRect = null;
        
            // Save position where mouse was released to display popup on
            int popUp=dot;
            if (dot > mark)
            {
                // Make sure dot is always smaller
                int temp=mark;
                mark=dot;
                dot=temp;                
            }
        
            mark--;
                    
            // Get some reference to mmax document
            MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        
            // Adjust dot and mark to cover fringe words completely
            String wordUnderDot = currentDiscourse.getDiscourseElementIDAtDiscoursePosition(currentDiscourse.getDiscoursePositionAtDisplayPosition(dot));
            while(wordUnderDot.equals("") && dot < mark)
            {
                dot++;                
                wordUnderDot = currentDiscourse.getDiscourseElementIDAtDiscoursePosition(currentDiscourse.getDiscoursePositionAtDisplayPosition(dot));
            }
            dotsDiscoursePosition = currentDiscourse.getDiscoursePositionAtDisplayPosition(dot);

            String wordUnderMark = currentDiscourse.getDiscourseElementIDAtDiscoursePosition(currentDiscourse.getDiscoursePositionAtDisplayPosition(mark));
            while(wordUnderMark.equals("") && mark > dot)
            {
                mark--;
                wordUnderMark = currentDiscourse.getDiscourseElementIDAtDiscoursePosition(currentDiscourse.getDiscoursePositionAtDisplayPosition(mark));
            }
            marksDiscoursePosition = currentDiscourse.getDiscoursePositionAtDisplayPosition(mark);
            if (wordUnderDot.equals("") || wordUnderMark.equals(""))
            {
                System.out.println("Invalid selection");
                return;
            }

            // Get start and end of selection in real display figures
            selectionStart = currentDiscourse.getDisplayStartPositionFromDiscoursePosition(dotsDiscoursePosition);
            selectionEnd = currentDiscourse.getDisplayStartPositionFromDiscoursePosition(marksDiscoursePosition);

            // Create shade
            doc.startChanges(selectionStart,(selectionEnd-selectionStart)+1);
            SimpleAttributeSet styleToUse = currentDiscourse.getMMAX2().getSelectionSpanStyle();
            StyleConstants.setFontSize(styleToUse, this.orderedLevels[0].getCurrentDiscourse().getMMAX2().currentDisplayFontSize);
            StyleConstants.setFontFamily(styleToUse, this.orderedLevels[0].getCurrentDiscourse().getMMAX2().currentDisplayFontName);        

            doc.bulkApplyStyleToDisplaySpanBackground(selectionStart, (selectionEnd-selectionStart)+1,styleToUse);
            doc.commitChanges();
        
            // Get current primary markable (if any)
            Markable currentPrimaryMarkable = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        
            if (currentPrimaryMarkable==null)
            {
                // No PrimaryMarkable currently selected, so only adding a new Markable to some level is possible
                String fragment = "";
            
                if (wordUnderDot.equals(wordUnderMark))
                {
                    fragment = wordUnderDot;
                }
                else
                {
                    fragment = wordUnderDot+".."+wordUnderMark;
                }
                MarkableLevel[] activeLevels = getActiveLevels();
                Markable newMarkable = null;
                if (activeLevels.length ==0)
                {
                    removeTemporarySelection();
                    return;
                }
                else if (activeLevels.length==1 && mmax2.getCreateSilently())
                {
                    newMarkable = ((MarkableLevel)activeLevels[0]).addMarkable(fragment);
                    if (getCurrentDiscourse().getMMAX2().getSelectAfterCreation())
                    {
                        markableLeftClicked(newMarkable);
                    }
                }
                else
                {
                    // Several levels are active, or silent creation is disabled, so use selector
                    MMAX2LevelSelector selector = new MMAX2LevelSelector(activeLevels, fragment, this);

                    // Some action is possible, so show selector                
                    try
                    {
                        tempRect = mmax2.getCurrentTextPane().modelToView(popUp);
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {
                        System.out.println("Error with display position determination of dot position "+dot);
                    }
                
                    int xPos = (int)tempRect.getX();
                    int yPos = (int)tempRect.getY()+MMAX2Constants.DEFAULT_FONT_SIZE+(MMAX2Constants.DEFAULT_FONT_SIZE/3);
                    int currentScreenWidth = mmax2.getScreenWidth();
                    int selectorWidth = selector.getWidth();
                    if ((xPos+mmax2.getX()) > currentScreenWidth/2)
                    {
                        xPos = xPos-selectorWidth;
                    }
                    selector.show(mmax2.getCurrentTextPane(),xPos,yPos );                        
                }            
            }
            else
            {
                // There is currently a PrimaryMarkable selected, so adding to its span and removing should be possible                
                ArrayList tempDEIDs = new ArrayList();
                int currentDiscPos = 0;
                String currentDEID = "";
                // Get List of DEs in  span
                // Iterate from dot to mark
                for (int z=dot;z<=mark;z++)
                {
                    // Get discpos at z
                    currentDiscPos = getCurrentDiscourse().getDiscoursePositionAtDisplayPosition(z);               
                    // Get de-id at discpos
                    currentDEID = getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(currentDiscPos);
                    // Get DE element as well
                    // Collect ids and elements in list
                    if (currentDEID.equals("")==false)
                    {
                        if (tempDEIDs.contains(currentDEID)==false) 
                        {
                            tempDEIDs.add(currentDEID);
                        }
                    }
                }
                int mode =-1;
                // Get array representation of selected DEIDs 
                String[] modifiables = (String[])tempDEIDs.toArray(new String[0]);                
                
                // Get disc pos of first de in markable
                int currentMarkablesFirstDiscoursePosition = getCurrentDiscourse().getDiscoursePositionAtDisplayPosition(currentPrimaryMarkable.getLeftmostDisplayPosition());
                // Get disc pos of last de in markable
                int currentMarkablesLastDiscoursePosition = getCurrentDiscourse().getDiscoursePositionAtDisplayPosition(currentPrimaryMarkable.getRightmostDisplayPosition());
                // Determine relation of selection to current Markable, decide on mode accordingly
            
                // Get list of IDs of markable                
                ArrayList markablesIDs = new ArrayList(java.util.Arrays.asList(currentPrimaryMarkable.getDiscourseElementIDs()));
                if (markablesIDs.contains(modifiables[0])==false || markablesIDs.contains(modifiables[modifiables.length-1])==false)
                {
                    // The selection's first or last element is not part of markable, so assume adding
                    mode = MMAX2Constants.ADD_DES;
                }
                else
                {
                    // The selections first or last element (or both) is part of markable, so assume removing
                    mode = MMAX2Constants.REMOVE_DES;
                }
                
                if (mode != -1)
                {
                    MMAX2ActionSelector actionSelector = new MMAX2ActionSelector(currentPrimaryMarkable,modifiables,this,mode);
                    // Some action is possible, so show selector                
                    try
                    {
                        tempRect = mmax2.getCurrentTextPane().modelToView(popUp);
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {
                        System.out.println("Error with display position determination of dot position "+dot);
                    }

                    int xPos = (int)tempRect.getX();
                    int yPos = (int)tempRect.getY()+MMAX2Constants.DEFAULT_FONT_SIZE+(MMAX2Constants.DEFAULT_FONT_SIZE/3);
                    int currentScreenWidth = mmax2.getScreenWidth();
                    int selectorWidth = actionSelector.getWidth();
                    if ((xPos+mmax2.getX()) > currentScreenWidth/2)
                    {
                        xPos = xPos-selectorWidth;
                    }
                    actionSelector.show(mmax2.getCurrentTextPane(),xPos,yPos );                        
                }
            }   
        }
    }
    
    public final void removeTemporarySelection()
    {
        if (orderedLevels.length > 0)
        {
            System.out.println("Remove temp selection");
            SimpleAttributeSet styleToUse = new SimpleAttributeSet();            
            // Problem: spaces between DiscourseElements remain highlighted, so first set everything to Color.white
            StyleConstants.setBackground(styleToUse,Color.white);
            
            MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
            doc.startChanges(selectionStart,(selectionEnd-selectionStart)+1);
            doc.bulkApplyStyleToDisplaySpanBackground(selectionStart, (selectionEnd-selectionStart)+1,styleToUse);
            styleToUse = null;           
            for (int z=dotsDiscoursePosition;z<=marksDiscoursePosition;z++)
            {
                // Get style at current pos
                // This will also handle size and family now
                styleToUse = getTopAttributesAtDiscourseElement(currentDiscourse.getDiscourseElementIDAtDiscoursePosition(z));
                
                doc.bulkApplyStyleToDiscourseElement(currentDiscourse.getDisplayStartPositionFromDiscoursePosition(z),styleToUse,true);
            }
            doc.commitChanges();
        }
    }
    
    public final void requestModifyMarkablesDEs(Markable modified, String[] DEs, int mode)
    {
        boolean success = false;
        
        // Determine range of markable before modification
        int start = modified.getLeftmostDisplayPosition();
        int end = modified.getRightmostDisplayPosition();        
        
        // Unregister in old, more complete state. This will be reversed by call to register if change did not succeed
        modified.getMarkableLevel().unregisterMarkable(modified);        
        // Now, modified does not have any mappings to discourse positions any more 
        
        if (mode == MMAX2Constants.REMOVE_DES)
        {
            /* Try to remove the desired DEs from modified. This will fail if
               - the markable would be empty afterwards */            
            success = modified.removeDiscourseElements(DEs);
        }
        else if (mode == MMAX2Constants.ADD_DES)//_BEFORE || mode == MMAX2.ADD_DES_AFTER)
        {
            success = modified.addDiscourseElements(DEs);
        }
        
        if (success)
        {
            // If the removal of DEs was successful, reflect change in the display
            SimpleAttributeSet styleToUse = new SimpleAttributeSet();
            // Problem: spaces between DiscourseElements remain highlighted, so first set everything to Color.white
            StyleConstants.setBackground(styleToUse,Color.white);
            StyleConstants.setFontSize(styleToUse, MMAX2.currentDisplayFontSize);
            StyleConstants.setFontFamily(styleToUse, MMAX2.currentDisplayFontName);        
            MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
            doc.startChanges(start,(end-start)+1);
            doc.bulkApplyStyleToDisplaySpanBackground(start, (end-start)+1,styleToUse);
            doc.commitChanges();    
            modified.getMarkableLevel().setIsDirty(true,true);
        }
        else
        {
            System.out.println("Markable modification failed");
            MarkableHelper.register(modified,true);
        }
        // At any rate, re-register (with hash update) and recalc display positions
        
        MarkableHelper.setDisplayPositions(modified);

        // Remove selection from mouse dragging
        removeTemporarySelection();
        // Re-render markable as selected
        modified.renderMe(MMAX2Constants.RENDER_SELECTED);
        // Re-display current markable to reflect changes to the string appearance
        attributePanelContainer.displayMarkableAttributes(modified);
    }
    
    
    public final void setNextFreeMarkableSetNum(int num)
    {
        nextFreeMarkableSetNum = num;
    }
    
    public final int getNextFreeMarkableSetNum()
    {
        return nextFreeMarkableSetNum;
    }
    
    public final void setNextFreeMarkableIDNum(int num)
    {
        nextFreeMarkableIDNum = num;
    }
    
    public final int getNextFreeMarkableIDNum()
    {
        return nextFreeMarkableIDNum;
    }
    
    
    public final void getMarkableLevelControlWindowToFront()
    {
        currentLevelControlWindow.toFront();
    }

    public final void getAttributeWindowToFront()
    {
        attributePanelContainer.toFront();
    }

    
    public final void destroyDependentComponents()
    {           
        if (currentLevelControlWindow != null)
        {
            currentLevelControlWindow.destroyDependentComponents();
            currentLevelControlWindow.setVisible(false);
            currentLevelControlWindow = null;
        }

        if (currentLevelControlPanel != null)
        {
            currentLevelControlPanel.destroyDependentComponents();
            currentLevelControlPanel.setVisible(false);
            currentLevelControlPanel = null;
        }
                
        
        attributePanelContainer.setVisible(false);
        attributePanelContainer = null;
        levels = null;
        
        for (int z=0;z<this.size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).destroyDependentComponents();
        }  
        orderedLevels = null;
        
        if (selector != null)
        {
            selector.setVisible(false);
        }
        selector = null;
        
        System.gc();
    }
    
    protected void finalize()
    {
        
        System.err.println("MarkableChart is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }     
               
    }
    
    
    public final void setShowMarkableLevelControlWindow(boolean show)
    {
        if (show) 
        {
            currentLevelControlWindow.pack();
            currentLevelControlWindow.setVisible(true);
        }
        else currentLevelControlWindow.setVisible(false);
    }
    
    public final void initMarkableRelations()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).initMarkableRelations();
        }          
    }
        
    public final Color getForegroundColorForLevel(String levelname)
    {
        return getMarkableLevelByName(levelname,false).getRenderer().getForegroundColor();
    }

    public final Color getHandleColorForLevel(String levelname)
    {
        return getMarkableLevelByName(levelname,false).getRenderer().getHandleColor();
    }

    /** This method returns the MarkableLevel object of name levelname, or null. */
    public final MarkableLevel getMarkableLevelByName(String levelname, boolean interactive)
    {
        MarkableLevel result = null;
        if (levelname.equalsIgnoreCase("basedata"))
        {
            result = new MarkableLevel(null, "","internal_basedata_representation", null, "");
            result.setCurrentDiscourse(getCurrentDiscourse());
        }
        else 
        {
            result = (MarkableLevel) levels.get(levelname.toLowerCase());        
        }
        if (result == null)
        {                        
            if (interactive)
            {
                JOptionPane.showMessageDialog(null,"No MarkableLevel with name "+levelname+"!","MarkableChart",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                System.err.println("No MarkableLevel with name "+levelname);
            }
        }
        return result;
    }

    
    public final int getSize()
    {
        return size;
    }
    
    public final void resetMarkablesForStyleSheetReapplication()
    {
        for (int z=0;z<this.size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).resetMarkablesForStyleSheetReapplication();
        }  
    }

    public final void initAttributePanelContainer()
    {
        for (int z=0;z<this.size;z++)
        {
            this.attributePanelContainer.addAttributePanel(((MarkableLevel) orderedLevels[z]).getCurrentAnnotationScheme().getCurrentAttributePanel(),((MarkableLevel) orderedLevels[z]).getMarkableLevelName());
        }  
        this.attributePanelContainer.pack();
        this.attributePanelContainer.setVisible(true);
        this.attributePanelContainer.disableAll();
        this.attributePanelContainer.setMMAX2(this.getCurrentDiscourse().getMMAX2());
    }
    
    public final void initShowInMarkableSelectorPopupMenu()
    {
        this.attributePanelContainer.initShowInMarkableSelectorPopupMenu();
    }
    
    /** This method adds the MarkableLayer layer to both this.orderedLayers and this.layers. */
    public final void addMarkableLevel(MarkableLevel level)
    {
        if (this.size == 0)
        {
            /** This is the first MarkableLayer added to this MarkableChart. */
            this.orderedLevels = new MarkableLevel[1];
            this.orderedLevels[0] = level;
        }
        else
        {
            /** There are already MarkableLayers in the MarkableChart. */
            /** Create new array to accept old MLs plus the new one. */
            MarkableLevel[] tempLevels = new MarkableLevel[this.size+1];
            /** Copy old layers to new array */
            System.arraycopy(this.orderedLevels,0,tempLevels,0,this.size);
            /** Append new layer at the end. */
            tempLevels[size] = level;
            this.orderedLevels = tempLevels;
        }
        level.setPosition(this.size);
        this.size++;
                        
        /** Put also in hash so layers are retrieveable by level name */
        levels.put(new String(level.getMarkableLevelName().toLowerCase()),level);      
        
        /** Create control in layerControlPanel */
        currentLevelControlPanel.addLevel(level);            
    }

    
    public final void initializeSaveMenu(JMenu menu)
    {
        menu.removeAll();
        MarkableLevel currentLevel = null;
        for (int z=0;z<this.size;z++)
        {
            currentLevel = (MarkableLevel) orderedLevels[z];
            menu.add(currentLevel.getSaveMarkableLevelItem());
        }          
    }
    
    public final MarkableLevel removeMarkableLevel(MarkableLevel level)
    {
        return (MarkableLevel)(levels.remove(level.getMarkableLevelName().toLowerCase()));
    }
        
    
    /** This method returns an array which contains for each MarkableLayer one array of markables 
        found on this layer at DiscoursePosition position. If a MarkableLayer does not have any markables at position,
        the corresponding array entry is a markable array of length 0. */
    public final Markable[][] getMarkablesAtDiscoursePosition(int position, boolean activeLevelsOnly)
    {     
        Markable[][] result = new Markable[this.size][0];
        Markable[] tempResult = null;
        MarkableLevel currentLevel = null;
        
        // Iterate over all layers
        for (int z=0;z<this.size;z++)
        {
            currentLevel = (MarkableLevel) orderedLevels[z];
            if ((!activeLevelsOnly) || currentLevel.getIsActive())
            {
                /** Get Array of Markables from each MarkableLayer separately, or empty markable array if none. */
                tempResult = currentLevel.getAllMarkablesAtDiscoursePosition(position);
                result[z] = tempResult;
            }
        }  
        return result;
    }

    
    public final boolean getIsAnyMarkableLevelModified()
    {
        boolean result = false;
        MarkableLevel currentLevel = null;
        for (int z=0;z<size;z++)
        {
            currentLevel = (MarkableLevel) orderedLevels[z];
            if (currentLevel.getIsDirty())
            {
                result = true;
                break;
            }
        }  
        return result;
    }
    
    /** This method returns the background color to be displayed at display position displayPosition, where the latter is assumed
        to be the position of a MarkableHandle. It is used for removing the highlighting from MarkableHandles, and returns either
        selectionBackgroundColor if the current MarkableHandle is in a currently selected Markable, or white otherwise. 
        This is selection-sensitive! MarkableLayers are not accessed, so no active/inactive distinction necessary. */
    public final Color getPrevailingBackgroundColorForMarkableHandle(int displayPosition)
    {        
        // Use normal background as default
        Color resultColor = Color.white;
        
        // This assumes that no attribute-dependent background colors under MarkableHandles exist for Markables!! 
        // That is correct, since no markables exist at markable handle positions (they are always at the borders of fragments). 
        try
        {
            if ((currentDiscourse.getMMAX2().getCurrentPrimaryMarkable().coversDisplayPosition(displayPosition)) ||
                (currentDiscourse.getMMAX2().getCurrentSecondaryMarkable().coversDisplayPosition(displayPosition)))
            {
                // displayPosition is in a currently selected Markable, so selectionbackgroundColor is prevailing there
                resultColor = StyleConstants.getBackground(currentDiscourse.getMMAX2().getSelectedStyle());
                
            }
        }
        catch (java.lang.NullPointerException ex)
        {
            // no currently selectecd markables
        }        
        return resultColor;
    }
    
    /** This method returns the attributes currently visible at the position of DiscourseElement de_id. It is the main method for
        markable-related display rendering, and takes into account customized Markable attributes. 
        Attributes for a given de_id are determined as follows:
        1. Each MarkableLevel is considered in turn, beginning with the DEEPEST level and moving up. This way, attributes
           on higher levels have precedence over lower levels, and attributes from lower levels can percolate up
           unless they are EXPLICITLY overwritten by incompatible ones on a higher level. 
          2. All Markables on the current MarkableLevel are retrieved (in document order, shorter before
             longer ones (why?)).
             3. Each Markable is considered in turn, beginning with the TOP one.
     */
    public final SimpleAttributeSet getTopAttributesAtDiscourseElement(String de_id)//, boolean activeLevelsOnly)
    {
        // This method uses renderer.getAttributesForMarkable();
        MarkableLevel currentLevel = null;
        Markable[] activeMarkables = null;
        SimpleAttributeSet result = new SimpleAttributeSet();
        SimpleAttributeSet tempResult = null;
        
        // Iterate over layers backwards, so that higher attributes have precedence, but deeper attributes can percolate up if
        // they are not EXPLICITLY overwritten by higher ones. 
        // Overwriting by setting a value to false is not possible!! 
        for (int z=size-1;z>=0;z--)
        {
            // Get current level
            currentLevel = orderedLevels[z];   
            if (currentLevel.getIsVisible())
            {
                // Get all Markables from the current layer that de_id is part of (efficient, since hashed)
                // Why sort here?
                activeMarkables = currentLevel.getAllMarkablesAtDiscourseElement(de_id, true);            
                if (activeMarkables != null)
                {
                    // If some Markables were found, iterate over them
                    for (int q=0;q<activeMarkables.length;q++)
                    {
                        // For each Markable, get the attributes it has based on its features. If no feature-dependent attributes
                        // exist, this will return the explicitlyAssociatedAttributes for the current layer.
                        // That means that for 'uncustomized' markables from transparent layers, attributes will not contain any colors ( ?? )                        
                        // Get Attributes for current markable on current level
                        tempResult = currentLevel.getRenderer().getAttributesForMarkable(activeMarkables[q]);
                        if (result == null)
                        {
                            // This can never happen, since getAttributesForMarkables always returns a valid SimpleAttributeSet
                            result = tempResult;
                        }
                        else
                        {
                            SimpleAttributeSet intermediate = MarkableLevelRenderer.mergeAttributes(tempResult,result);
                            result = intermediate;
                            intermediate = null;
                        }
                    }
                }
            } 
        }
        // If no color made it to the top, use default colors here
        if (result.isDefined(StyleConstants.Foreground)==false)
        {
            StyleConstants.setForeground(result,Color.black);
        }        
        if (result.isDefined(StyleConstants.Background)==false)
        {
            StyleConstants.setBackground(result,Color.white);
        }        
        if (result.isDefined(StyleConstants.FontSize)==false)
        {
            // If no attribute-dependent size exists, use current display size
            StyleConstants.setFontSize(result,MMAX2.currentDisplayFontSize);
        }             
        if (result.isDefined(StyleConstants.FontFamily)==false)
        {
            // If no attribute-dependent font name exists, use current display font
            StyleConstants.setFontFamily(result,MMAX2.currentDisplayFontName);
        }             

        return result;
    }

           
    /** This method returns a node list with all _active_ Markables started at DiscourseElement with ID discourseElementId. Markables 
        are grouped by levels in reverse MarkableChart layer order (cf. this.orderedLayers[]). Within each group, Markables are ordered 
        in discourse position order, with longer before shorter ones (for embedding visualization). */
    public final NodeSet getActiveStartedMarkables(String discourseElementId)
    {
        NodeSet result = new NodeSet();
        // Iterate over all levels in reverse order, 
        // so that deeper levels are processed before higher ones. 
        MarkableLevel level = null;
        for (int z=size-1;z>=0;z--)
        {
            // Get current level
            level = (MarkableLevel) orderedLevels[z];
            if ((level.getIsActive() || level.getIsVisible()) && level.isDefined())
            {
                // getStartedMarkables returns a list in the proper ordering for opening bracket insertion.
                level.getAllStartedMarkablesAsNodes(discourseElementId, result);
            }
        }        
        return result;
    }


    /** This method returns a node list with all _active_ Markables started at DiscourseElement with ID discourseElementId. Markables 
        are grouped by levels in reverse MarkableChart layer order (cf. this.orderedLayers[]). Within each group, Markables are ordered 
        in discourse position order, with longer before shorter ones (for embedding visualization). */
    public final NodeSet getActiveStartedMarkables(String discourseElementId, String levels)
    {
        NodeSet result = new NodeSet();       
        MarkableLevel level = null;
        String currentLevelName = "";
        // Iterate over all levels in reverse order, 
        // so that deeper levels are processed before higher ones. 
        for (int z=size-1;z>=0;z--)
        {
            // Get current level
            level = (MarkableLevel) orderedLevels[z];
            if ((level.getIsActive() || level.getIsVisible()) && level.isDefined())
            {
                currentLevelName = level.getMatchableMarkableLevelName();
                if (levels.indexOf(currentLevelName)==-1)
                {
                    // Skip if level name not in list to retrieve
                    continue;
                }                
                // getStartedMarkables returns a list in the proper ordering for opening bracket insertion.
                level.getAllStartedMarkablesAsNodes(discourseElementId, result);
            }            
        }        
        return result;
    }
        
    public final ArrayList getAllStartedMarkables(String deID)
    {
        ArrayList result = new ArrayList();
        Markable[] tempResult = null;
        for (int z=0;z<size;z++)
        {
            MarkableLevel level = (MarkableLevel) orderedLevels[z];
            // Get array of all markables ending at discourseElementId
            tempResult = level.getAllMarkablesStartedByDiscourseElement(deID);
            if (tempResult != null)
            {
                int len = tempResult.length;
                // Add them to temporary ArrayList
                for (int o=0;o<len;o++)
                {
                    if (tempResult[o] != null)
                    {
                        result.add((Markable)tempResult[o]);                    
                    }
                }
            }
        }
        return result;
    }
    
    public final ArrayList getAllEndedMarkables(String deID)
    {
        ArrayList result = new ArrayList();
        Markable[] tempResult = null;
        for (int z=0;z<size;z++)
        {
            MarkableLevel level = (MarkableLevel) orderedLevels[z];
            // Get array of all markables ending at discourseElementId
            tempResult = level.getAllMarkablesEndedByDiscourseElement(deID);
            if (tempResult != null)
            {
                int len = tempResult.length;
                // Add them to temporary ArrayList
                for (int o=0;o<len;o++)
                {
                    if (tempResult[o] != null)
                    {
                        result.add((Markable)tempResult[o]);                    
                    }
                }
            }
        }
        return result;
    }
        
    /** This method returns a node list with all _active_ Markables ended at DiscourseElement with ID discourseElementId. Markables 
        are grouped by levels in MarkableChart layer order (cf. this.orderedLayers[]). Within each group, Markables 
        are ordered in discourse position order, but with shorter before longer ones (for embedding visualization). */    
    public final NodeSet getActiveEndedMarkables(String discourseElementId)
    {
        NodeSet result = new NodeSet();
        MarkableLevel level = null;
        /** Iterate over all layers */
        for (int z=0;z<size;z++)
        {
            level = (MarkableLevel) orderedLevels[z];
            if ((level.getIsActive()|| level.getIsVisible()) && level.isDefined())
            {
                // Get array of all markables ending at discourseElementId
                level.getAllEndedMarkablesAsNodes(discourseElementId,result);
            }
        }        
        return result;
    }

    /** This method returns a node list with all _active_ Markables ended at DiscourseElement with ID discourseElementId. Markables 
        are grouped by levels in MarkableChart layer order (cf. this.orderedLayers[]). Within each group, Markables 
        are ordered in discourse position order, but with shorter before longer ones (for embedding visualization). */    
    public final NodeSet getActiveEndedMarkables(String discourseElementId, String levels)
    {
        NodeSet result = new NodeSet();
        MarkableLevel level = null;
        String currentLevelName = "";
        /** Iterate over all layers */
        for (int z=0;z<size;z++)
        {
            level = (MarkableLevel) orderedLevels[z];
            if ((level.getIsActive()|| level.getIsVisible()) && level.isDefined())
            {
                currentLevelName = level.getMatchableMarkableLevelName();
                if (levels.indexOf(currentLevelName)==-1)
                {
                    continue;
                }                
                // Get array of all markables ending at discourseElementId
                level.getAllEndedMarkablesAsNodes(discourseElementId,result);
            }            
        }        
        return result;
    }
    
    
    /** This method returns a node list with all _active_ Markables ended at DiscourseElement with ID discourseElementId. Markables 
        are grouped by levels in MarkableChart layer order (cf. this.orderedLayers[]). Within each group, Markables 
        are ordered in discourse position order, but with shorter before longer ones (for embedding visualization). */    
    public final NodeSet getActiveEndedMarkables_bak(String discourseElementId)
    {
        ArrayList tempCollection = new ArrayList();
        NodeSet result = new NodeSet();
        Markable[] tempResult = null;
        /** Iterate over all layers */
        for (int z=0;z<size;z++)
        {
            MarkableLevel level = (MarkableLevel) orderedLevels[z];
            if ((level.getIsActive()|| level.getIsVisible()) && level.isDefined())
            {
                // Get array of all markables ending at discourseElementId
                tempResult = level.getAllMarkablesEndedByDiscourseElement(discourseElementId);
                if (tempResult != null)
                {                                        
                    int len = tempResult.length;
                    // Add them to temporary ArrayList
                    for (int o=0;o<len;o++)
                    {
                        if (tempResult[o] != null)
                        {
                            tempCollection.add((Markable)tempResult[o]);                    
                        }
                    }
                }
            }
        }
        tempResult = null;
        tempResult = (Markable[]) tempCollection.toArray(new Markable[1]);        

        for (int o=0;o<tempResult.length;o++)
        {            
            if (tempResult[o] != null)
            {
                result.insertElementAt(tempResult[o].getNodeRepresentation(),o);
            }
        }
        return result;
    }
    
    
    public final void setMarkableLevelDisplayPositions()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).setMarkableDisplayPositions();
        }        
    }
        
    public final void createDiscoursePositionToMarkableMappings()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).createDiscoursePositionToMarkableMapping();
        }
    }
    
    public final void updateLabels()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).updateNameLabelText();
        }
    }    

    public final void validateAll()
    {
        for (int z=0;z<size;z++)
        {        
            ((MarkableLevel) orderedLevels[z]).validate();
        }
    }    

    public final void resetHasHandles()
    {
        for (int z=0;z<size;z++)
        {
            ((MarkableLevel) orderedLevels[z]).setHasHandles(false);
        }
    }    
    
    public final MarkableLevelControlPanel getCurrentLevelControlPanel()
    {
        return currentLevelControlPanel;
    }
            
    public final ArrayList getLevelAndAttributeNamesForValues(String valueList, String optionalAttributeName, boolean activeLevelsOnly)
    {
        ArrayList result = new ArrayList();
        // Iterate over all levels
        for (int z=0;z<size;z++)
        {
            // Get list of attribute names on the current level for which *all* values are defined
            // New: Consider currently ACTIVE levels only, if so desired!!
            if (((MarkableLevel) orderedLevels[z]).getIsActive()==false && activeLevelsOnly)
            {
                continue;
            }
            ArrayList temp = ((MarkableLevel) orderedLevels[z]).getAttributeNamesForValues(valueList, optionalAttributeName);
            // Iterate over all names found
            for (int b=0;b<temp.size();b++)
            {
                // Create list of level:attribute
                // Note: For freetext attributes which were passed in by means of optionalAttributeName,
                // entries in the list below will have a * prepended to the attribute name
                result.add(((MarkableLevel) orderedLevels[z]).getMarkableLevelName()+":"+(String)temp.get(b));
            }
        }                
        return result;
    }
    
    public final void reorderMarkableLayers(String command)
    {
        MarkableLevel movee = null;
        /** Get position of Layer to be moved */
        int posToChange = Integer.parseInt(command.substring(command.indexOf(":")+1));
        /** Get direction of move. */
        String direction = command.substring(0,command.indexOf(":"));
        if (((posToChange == 0) && direction.equals("up")) ||
           ((posToChange == size-1) && direction.equals("down")))
        {
            return;
        }
        /** Get layer to be replaced by the one at posToChange */
        if (direction.equals("up")) 
        {
            // Swap Layers 
            movee = orderedLevels[posToChange-1];            
            movee.setPosition(movee.getPosition()+1);
            orderedLevels[posToChange].setPosition(orderedLevels[posToChange].getPosition()-1);
            orderedLevels[posToChange-1] = orderedLevels[posToChange];
            orderedLevels[posToChange] = movee;
        }
        else
        {
            movee = orderedLevels[posToChange+1];
            movee.setPosition(movee.getPosition()-1);
            orderedLevels[posToChange].setPosition(orderedLevels[posToChange].getPosition()+1);            
            orderedLevels[posToChange+1] = orderedLevels[posToChange];
            orderedLevels[posToChange] = movee;            
        }
        
        currentLevelControlPanel.clear();
        currentLevelControlPanel.addLevels(orderedLevels);
        currentLevelControlPanel.revalidate();  
    }
    
    
    public final void requestCopyMarkableSetToLevel(MarkableSet setToCopy, MarkableLevel target)
    {    	
    	MMAX2Attribute targetAttribute = null;
        MMAX2Attribute[] targetAttributes =target.getCurrentAnnotationScheme().getAttributesByNameAndType("^"+setToCopy.getMarkableRelation().getAttributeName()+"$", AttributeAPI.MARKABLE_SET, AttributeAPI.MARKABLE_SET);
        if (targetAttributes.length > 0)
        {
        	targetAttribute = targetAttributes[0];
        }
        
        if (targetAttribute==null)
        {
            // The target level does not have an attribute to copy to
            System.err.println("Cannot copy to level "+target.getMarkableLevelName()+". No appropriate target attribute!");
            return;
        }
        
        MarkableRelation targetRelation = targetAttribute.getMarkableRelation();
        
        // Get list of all markables to copy to target level
        ArrayList markablesToCopy = (ArrayList)java.util.Arrays.asList(setToCopy.getOrderedMarkables());
        Markable currentToCopy = null;
        Markable chainInitial = null;
        // Iterate over all markables in set to be copied
        for (int u=0;u<markablesToCopy.size();u++)
        {
            // Get current markable
            currentToCopy = (Markable) markablesToCopy.get(u);
            // If an identical markables does not exist, re-create current on new level
            if (target.getMarkableAtSpan(MarkableHelper.getSpan(currentToCopy))==null)
            {
                requestCopyMarkableToLevel(currentToCopy, target.getMarkableLevelName());
            }
        }

        // Now, there is a markable on target level for each markable in setToCopy
        // It was either already there, or has been created above
                
        // Itersate over all markables to copy again
        for (int u=0;u<markablesToCopy.size();u++)
        {
            // Get current markable
            currentToCopy = (Markable) markablesToCopy.get(u);
            // Get twin markable on target level
            Markable copy = target.getMarkableAtSpan(MarkableHelper.getSpan(currentToCopy));
            if (chainInitial == null)
            {
                chainInitial = copy;
                addMarkableToNewMarkableSet(copy, targetRelation);
            }
            else
            {
                addMarkableToExistingMarkableSet(copy, targetRelation.getMarkableSetContainingMarkable(chainInitial),false);
            }            
        }                        
    }
    
    public final void requestModifyLiteralDisplayText(int displayPos, String newString, String oldString)
    {
        // Only if this was called by a right-click on a handle, and if option is selected
        if (displayPos != -1 && attributePanelContainer.attemptAutoUpdate()==true)
        {
            // Stop all other click events
            getCurrentDiscourse().getMMAX2().setIgnoreCaretUpdate(true);
            // Get element at clicked position
            javax.swing.text.Element ele = getCurrentDiscourse().getMMAX2().getCurrentDocument().getCharacterElement(displayPos);
            int start = ele.getStartOffset();
            int end = ele.getEndOffset();
            // Get len of text to be replaced which should be constant (as set via style sheet)
            int len = end-start;
            // Get text at clicked display pos element, this will contain any spaces used in the addHandleMethod!
            String textAtHandle = "";
            try
            {
                textAtHandle = getCurrentDiscourse().getMMAX2().getCurrentDocument().getText(start,len);
            }
            catch (javax.swing.text.BadLocationException ex)
            {

            }
            
            System.err.println("Text at handle: '"+textAtHandle+"'");
            
            // Check whether textAtHandle minus trailing spaces is the old value we have chosen to change
            if (textAtHandle.trim().equalsIgnoreCase(oldString))
            {
                // The text at the handle is indeed the old value of the att we want to change
                // New May 27th: Update display only if auto-apply is active
                if (attributePanelContainer.isAutoApply())
                {
                    // New May 27th: Make sure update is only attempted if new text fits into space of old
                    if (newString.length() > textAtHandle.length())
                    {
                        String message = "Warning: Cannot update display with new value (length mismatch).\nUse 'Reapply current style sheet' to update display!";
                        JOptionPane.showMessageDialog(getCurrentDiscourse().getMMAX2(),message,"One-click annotation",JOptionPane.WARNING_MESSAGE);
                        getCurrentDiscourse().getMMAX2().setIgnoreCaretUpdate(false);
                        return;
                    }
                    
                    // Fill newValue to be the same size as len
                    newString = newString+"   ";
                    newString = newString.substring(0,len);
                    ((MMAX2Document)getCurrentDiscourse().getMMAX2().getCurrentDocument()).startChanges(start,len);
                    try
                    {
                        getCurrentDiscourse().getMMAX2().getCurrentDocument().replace(start,len, newString, ele.getAttributes());
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {            
                    
                    }
                }
                else
                {
                    String message="Note: Activate 'Auto-apply' on the Attribute Window to automatically update the display!\nThe display will not be updated automatically unless you use this option.\nAlso, note that you have to select 'Apply' manually to apply your changes!";
                    JOptionPane.showMessageDialog(getCurrentDiscourse().getMMAX2(),message,"One-click annotation",JOptionPane.INFORMATION_MESSAGE);
                }
            }
            else
            {
                // Auto-update is impossible anyway
                System.err.println("Attribute and handle mismatch, cannot update display!");
                if (attributePanelContainer.isAutoApply()==false)
                {
                    // But show reminder for auto-apply
                    String message="Note: Activate 'Auto-apply' on the Attribute Window to automatically apply your changes!\n";
                    JOptionPane.showMessageDialog(getCurrentDiscourse().getMMAX2(),message,"One-click annotation",JOptionPane.INFORMATION_MESSAGE);                    
                }
            }
        }
        // Allow other click events
        getCurrentDiscourse().getMMAX2().setIgnoreCaretUpdate(false);
    }
    
    /** This method is called by the display caret listener whenever a markabls is right-clicked. */
    public final void markableRightClicked(Markable secondaryMarkable, int displayPos)
    {                
        MMAX2Attribute[] primaryMarkablesAttributes = null;
        MMAX2Attribute[] secondaryMarkablesAttributes = null;
        selector = null;
        // Set reference to secondary markable
        currentDiscourse.getMMAX2().setCurrentSecondaryMarkable(secondaryMarkable);
        // Get reference to primary markable, if any
        Markable primaryMarkable = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();        
        if (primaryMarkable != null && primaryMarkable != secondaryMarkable)
        {
            // If we have a primaryMarkable and it is not identical to second one
            // Get both markables' attributes (as input to ActionSelector constructor)            
                      
            primaryMarkablesAttributes = primaryMarkable.getValidatedAttributes();
            secondaryMarkablesAttributes = secondaryMarkable.getValidatedAttributes();            
            // Reset attribute window to prior state
            attributePanelContainer.displayMarkableAttributes(primaryMarkable);
            // Create action selector
            selector = new MMAX2ActionSelector(primaryMarkablesAttributes, primaryMarkable, secondaryMarkablesAttributes, secondaryMarkable, this);
        }
        else if (primaryMarkable != secondaryMarkable)
        {
            //System.out.println("No current primary or same!");
            // E.g. deleting should be possible       
            selector = new MMAX2ActionSelector(secondaryMarkable, primaryMarkable, this,true);
        }
        else if (primaryMarkable == secondaryMarkable)
        {
            if (oneClickSelector != null)
            {
                // Reset any old oneClickSelector that might be around
                oneClickSelector = null;
            }
            
            oneClickSelector = primaryMarkable.getMarkableLevel().createOneClickAnnotationSelector(primaryMarkable, this, displayPos);
            if (oneClickSelector != null)
            {                
                MMAX2 mmax2 = currentDiscourse.getMMAX2();
                // Some action is possible, so show selector
                int xPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getX();              
                int yPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getY();
                int currentScreenWidth = mmax2.getScreenWidth();
                int selectorWidth = oneClickSelector.getWidth();
                if ((xPos+mmax2.getX()) > currentScreenWidth/2)
                {
                    xPos = xPos-selectorWidth;
                }
                oneClickSelector.show(mmax2.getCurrentTextPane(),xPos,yPos );    
                mmax2.getCurrentTextPane().startAutoRefresh();
            }
            else
            {
                // Current primary and secondary are non-null, and no oneClickSelector is available
                selector = new MMAX2ActionSelector(secondaryMarkable, primaryMarkable, this,false);                
            }
            //String currentOneClickAttributeForLevel = primaryMarkable.getMarkableLevel().getCurrentOneClickAttributeName
        }
        if (selector != null && !selector.isEmpty())
        {
            MMAX2 mmax2 = this.currentDiscourse.getMMAX2();
            // Some action is possible, so show selector
            int xPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getX();              
            int yPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getY();
            int currentScreenWidth = mmax2.getScreenWidth();
            int selectorWidth = selector.getWidth();
            if ((xPos+mmax2.getX()) > currentScreenWidth/2)
            {
                xPos = xPos-selectorWidth;
            }
            selector.show(mmax2.getCurrentTextPane(),xPos,yPos );    
            mmax2.getCurrentTextPane().startAutoRefresh();
        }
        secondaryMarkable.renderMe(MMAX2Constants.RENDER_NO_HANDLES);
    }
    
    /** This method is called by the display caret listener whenever a markabls is left-clicked. */
    public final void markableLeftClicked(Markable newClicked)
    {
        // A Markable has been left-clicked. This selects a new primary markable, and resets an existing secondary markable. 
        // Maximally two Markables are concerned: newClicked and currentPrimary
        Markable[] concerned = new Markable[2];
        // The first one is the newly selected one
        concerned[0] = newClicked;
        // Get reference to document, via some arbitrary MarkableLevel object 
        MMAX2Document doc = orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        // Get reference to currentPrimaryMarkable (if any)
        Markable oldClicked = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        concerned[1] = oldClicked;
        // If newClicked was selecdted via its handles, these handles may still be highlighted
        // Remove highlighting before startChanges()
        newClicked.renderMe(MMAX2Constants.RENDER_NO_HANDLES);
        // Start new session of display changes
        doc.startChanges(concerned);
        
        // Reset old currentPrimaryMarkable, if any
        if (oldClicked != null) 
        {
            oldClicked.getMarkableLevel().setValidateButtonEnabled(true);
            oldClicked.renderMe(MMAX2Constants.RENDER_UNSELECTED);
            // Remove all lines from the display
            currentDiscourse.getMMAX2().emptyRenderingList();
            if (currentDiscourse.getMMAX2().getIsRendering())
            {
                currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
                currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
            }            
        }
        newClicked.getMarkableLevel().setValidateButtonEnabled(false);
        // Reset secondary Markable, if any
        currentDiscourse.getMMAX2().setCurrentSecondaryMarkable(null);
        // Set newly clicked Markable as new currentPrimaryMarkable
        currentDiscourse.getMMAX2().setCurrentPrimaryMarkable(newClicked);
        // Set 'selected' attributes to newly clicked Markable
        newClicked.renderMe(MMAX2Constants.RENDER_SELECTED);
        doc.commitChanges();
        
        attributePanelContainer.displayMarkableAttributes(newClicked);
    }
    
    public final void nothingClicked(int currentButton)
    {        
        Markable[] concerned = new Markable[1];
        if (orderedLevels.length > 0)
        {
            MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
            // Get reference to currentPrimaryMarkable (if any)
            Markable oldClicked = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
            concerned[0] = oldClicked;
            doc.startChanges(concerned);

            // Reset old currentPrimaryMarkable
            if (oldClicked != null) 
            {
                oldClicked.getMarkableLevel().setValidateButtonEnabled(true);
                oldClicked.renderMe(MMAX2Constants.RENDER_UNSELECTED);
            }
            // Set nothing selected 
            currentDiscourse.getMMAX2().setCurrentPrimaryMarkable(null);                
            currentDiscourse.getMMAX2().setCurrentSecondaryMarkable(null);                
            currentDiscourse.getMMAX2().emptyRenderingList();
            doc.commitChanges();
            if (currentDiscourse.getMMAX2().getIsRendering())
            {
                currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
                currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
            }
            attributePanelContainer.displayMarkableAttributes(null);
        }
        if (currentButton == MMAX2Constants.RIGHTMOUSE)
        {
            final MMAX2QueryWindow qw = this.getCurrentDiscourse().getMMAX2().getMMAX2QueryWindow();
            if (qw != null && qw.isResultAvailable())
            {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem item = new JMenuItem("Previous match");
                if (qw.isPreviousResultAvailable()==false)
                {
                    item.setEnabled(false);
                }

                item.setFont(MMAX2.getStandardFont());
                item.addActionListener(new ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent ae)
                    {
                        qw.moveToPreviousLineInResult();        
                    }            
                });
                menu.add(item);
                item = null;
                item = new JMenuItem("Next match");
                if (qw.isNextResultAvailable()==false)
                {
                    item.setEnabled(false);
                }
                item.setFont(MMAX2.getStandardFont());
                item.addActionListener(new ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent ae)
                    {
                        qw.moveToNextLineInResult();        
                    }            
                });
                menu.add(item);     
                int x = getCurrentDiscourse().getMMAX2().getCurrentTextPane().getCurrentMouseMoveEvent().getX();
                int y = getCurrentDiscourse().getMMAX2().getCurrentTextPane().getCurrentMouseMoveEvent().getY();                
                JViewport view = getCurrentDiscourse().getMMAX2().getCurrentViewport();
                
                Point p = view.getViewPosition();
                menu.show(getCurrentDiscourse().getMMAX2(),x-p.x,(y-p.y)+10);
            }
        }        
    }
    
    public final void rerender()
    {
        if (orderedLevels.length > 0)
        {
            orderedLevels[0].getRenderer().render(null,MMAX2Constants.RERENDER_EVERYTHING);
        }
        if (currentDiscourse.getMMAX2().getCurrentPrimaryMarkable() != null)
        {
            markableLeftClicked(currentDiscourse.getMMAX2().getCurrentPrimaryMarkable());
        }
    }        
    
    public final String getLevelNameByOrderPosition(int pos)
    {
        return ((MarkableLevel)orderedLevels[pos]).getMarkableLevelName();
    }
    
    public final void removeMarkablePointerWithSourceMarkable(Markable sourceMarkable, MarkableRelation relation, boolean refreshAttributeWindow )
    {
        // Get MarkablePointer with source markable        
        MarkablePointer pointer = relation.getMarkablePointerForSourceMarkable(sourceMarkable);
        // Get all targets
        ArrayList targets = (ArrayList) java.util.Arrays.asList(pointer.getTargetMarkables());
        // Iterate over all targets backwards
        for (int z=targets.size()-1;z>=0;z--)
        {
            // Remove each satellite individually. This will also remove the source finally
            removeTargetMarkableFromMarkablePointer((Markable)targets.get(z), pointer, refreshAttributeWindow);
        }
    }
    
    public final void removeTargetMarkableFromMarkablePointer(Markable removee, MarkablePointer pointer, boolean refreshAttributeWindow)
    {
        Markable[] concerned = new Markable[2];
        //Markable currentPrimary = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        Markable currentSource = pointer.getSourceMarkable();
        concerned[0] = removee;
        concerned[1] = currentSource;        
        MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(concerned);
        // Remove removee from markable pointer's target collection
        pointer.removeTargetMarkable(removee);
        // Render removee unselected 
        removee.renderMe(MMAX2Constants.RENDER_UNSELECTED);

        String constitutingAttributeName = pointer.getMarkableRelation().getAttributeName();
        // Remove set_x value from markable
        currentSource.setAttributeValue(constitutingAttributeName,pointer.getTargetSpan());
        // ... and Element
        ((Element)currentSource.getNodeRepresentation()).setAttribute(constitutingAttributeName,pointer.getTargetSpan());
        
        // Make attributewindow reflect the changes in the attributes
        if (currentSource == currentDiscourse.getMMAX2().getCurrentPrimaryMarkable() && refreshAttributeWindow)
        {
            this.attributePanelContainer.displayMarkableAttributes(currentSource);        
        }
        
        if (pointer.getSize()==0)
        {
            System.err.println("The last element from pointer is removed, pointer is destroyed!");
            // The second but last element has been removed, so the set ceases to exist            
            Markable last = pointer.getSourceMarkable();
            //System.out.println(last);
            pointer.removeMeFromMarkableRelation();
            currentDiscourse.getMMAX2().removeFromRenderingList(pointer);
            pointer=null;                 
            // Remove set-dependent highlighting from last markable in set. This may remove _underlying_ highlighting as well!
            // Replace with standard unselection highlighting            
            if (last == currentDiscourse.getMMAX2().getCurrentPrimaryMarkable()) 
            {
                currentDiscourse.getMMAX2().getCurrentPrimaryMarkable().renderMe(MMAX2Constants.RENDER_SELECTED);            
            }
            else
            {
                last.renderMe(MMAX2Constants.RENDER_UNSELECTED);    
            }
        }
        else
        {
            // The set is smaller now, so recalculate extent
            pointer.updateLinePoints();
        }
        // Force display to also redraw background of markables with next refresh, thus restoring any highlighting that may be missing
        currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
        // Make sure the display reflects the new state
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();
        currentSource.getMarkableLevel().setIsDirty(true,false);        
    }
    
    public final void removeMarkableFromMarkableSet(Markable removee, MarkableSet set, boolean refreshAttributeWindow)//, String constitutingAttribute)
    {        
        Markable[] concerned = new Markable[2];
        Markable currentPrimary = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        concerned[0] = removee;
        concerned[1] = currentPrimary;
        
        String constitutingAttribute = set.getMarkableRelation().getAttributeName();
        MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(concerned);
        
        // Remove markable from set's markable collection. This may render the set to have only one element!
        set.removeMarkable(removee);
        // Remove set_x value from markable
        //removee.removeAttribute(constitutingAttribute);
        removee.setAttributeValue(constitutingAttribute, MMAX2.defaultRelationValue);
        // ... and Element, _before_ rerendering !!
        //((Element)removee.getNodeRepresentation()).removeAttribute(constitutingAttribute);
        ((Element)removee.getNodeRepresentation()).setAttribute(constitutingAttribute,MMAX2.defaultRelationValue);
        // Remove set-dependent highlighting from markable just removed.
        removee.renderMe(MMAX2Constants.RENDER_UNSELECTED);

        if (set.getSize()==1)
        {           
            System.err.println("The second but last element is removed, set is destroyed!");
            // The second but last element has been removed, so the set ceases to exist            
            Markable last = set.getInitialMarkable();
            System.err.println("Last "+last.toString());
            doc.startChanges(last);
            
            set.removeMeFromMarkableRelation();
            currentDiscourse.getMMAX2().removeFromRenderingList(set);
            set=null;                 
            // Remove set_x value from markable
            last.setAttributeValue(constitutingAttribute,MMAX2.defaultRelationValue);
            // ... and Element
            ((Element)last.getNodeRepresentation()).setAttribute(constitutingAttribute,MMAX2.defaultRelationValue);            
            // Remove set-dependent highlighting from last markable in set. This may remove _underlying_ highlighting as well!
            // Replace with standard unselection highlighting
            last.renderMe(MMAX2Constants.RENDER_UNSELECTED);            
            if (refreshAttributeWindow)
            {
                // Make attributewindow reflect the changes the attributes
                attributePanelContainer.displayMarkableAttributes(currentPrimary);
            }
            if (last == currentPrimary) 
            {
                doc.startChanges(last);
                currentPrimary.renderMe(MMAX2Constants.RENDER_SELECTED);
                doc.commitChanges();
            }
            doc.commitChanges();
            // TODO: revalidate attributes of last Markable in set, in case the set attrib was branching
        }        
        // Force display to also redraw background of markables with next refresh, thus restoring any highlighting that may be missing
        currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
        // Make sure the display reflects the new state
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();
        removee.getMarkableLevel().setIsDirty(true,false);
    }
    
    public final void mergeMarkableSetsIntoNewSet(MarkableSet oldSet)
    {
        String constitutingAttribute = oldSet.getMarkableRelation().getAttributeName();
        // Get new for set to be created
        String newSetID = getNextFreeMarkableSetID();
        // Get current primary markable
        Markable currentPrimary = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        // Set attributes to markable and xml element 
        currentPrimary.setAttributeValue(constitutingAttribute,newSetID);
        ((Element)currentPrimary.getNodeRepresentation()).setAttribute(constitutingAttribute,newSetID);        
        
        // Create new set
        MarkableSet newSet = oldSet.getMarkableRelation().addMarkableWithAttributeValueToMarkableSet(currentPrimary,newSetID);
        // Merge new and old set
        mergeMarkableSets(newSet, oldSet);
        currentDiscourse.getMMAX2().putOnRenderingList(newSet);
        currentPrimary.renderMe(MMAX2Constants.RENDER_SELECTED);        
        // Force display to also redraw background of markables with next refresh, thus restoring any highlighting that may be missing
        currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
        // Make sure the display reflects the new state
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();        
    }
    
    public final void adoptMarkableToExistingMarkableSet(Markable adoptee, MarkableSet oldSet, MarkableSet newSet)
    {
        removeMarkableFromMarkableSet(adoptee,oldSet,true);
        addMarkableToExistingMarkableSet(adoptee,newSet,true);
    }
    
    public final void adoptMarkableToNewMarkableSet(Markable adoptee, MarkableSet oldSet)
    {
        removeMarkableFromMarkableSet(adoptee,oldSet,true);
        addMarkableToNewMarkableSet(adoptee, oldSet.getMarkableRelation());
    }
    
    public final void mergeMarkableSets(MarkableSet finalSet, MarkableSet oldSet)
    {
        //ArrayList movers = (ArrayList)java.util.Arrays.asList(oldSet.getOrderedMarkables());
        ArrayList movers = new ArrayList(java.util.Arrays.asList(oldSet.getOrderedMarkables()));
        int u=0;
        for (u=0;u<movers.size()-1;u++)
        {
            removeMarkableFromMarkableSet(((Markable)movers.get(u)),oldSet,true);
            addMarkableToExistingMarkableSet(((Markable)movers.get(u)), finalSet,true);
        }
        addMarkableToExistingMarkableSet(((Markable)movers.get(u)), finalSet,true);
    }
    
    
    public final void addTargetMarkableToNewMarkablePointer(Markable source, Markable target, MarkableRelation relation)
    {
        // Get name of MARKABLE_POINTER attribute
        String constitutingAttribute = relation.getAttributeName();
        
        // Create MarkablePointer object in MarkableRelation
        relation.createMarkablePointer(source,target);

        MMAX2Document doc = orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(target);
        target.renderMe(MMAX2Constants.RENDER_IN_SET);
        // Get reference to newly created MarkablePointer object
        MarkablePointer pointer = relation.getMarkablePointerForSourceMarkable(source);        
        
        // Set value of this to (list of) id(s) of satellite markable(s) in MarkablePointer
        source.setAttributeValue(constitutingAttribute,pointer.getTargetSpan());
        // ... and in element
        ((Element)source.getNodeRepresentation()).setAttribute(constitutingAttribute,pointer.getTargetSpan());

        currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
        source.renderMe(MMAX2Constants.RENDER_SELECTED);
        
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();        
        // Display markable's attributes
        attributePanelContainer.displayMarkableAttributes(source);
        source.getMarkableLevel().setIsDirty(true,false);
    }

    public final void addTargetMarkableToExistingMarkablePointer(Markable source, Markable target, MarkablePointer pointer)//MarkableRelation relation)
    {
        // Get name of MARKABLE_POINTER attribute
        String constitutingAttribute = pointer.getMarkableRelation().getAttributeName();
        
        // Create MarkablePointer object in MarkableRelation

        pointer.addTargetMarkable(target);
        
        MMAX2Document doc = orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(target);
        target.renderMe(MMAX2Constants.RENDER_IN_SET);
        // Get reference to newly created MarkablePointer object
        // MarkablePointer pointer = relation.getMarkablePointerForSourceMarkable(source);        
        
        // Set value of this to (list of) id(s) of satellite markable(s) in MarkablePointer
        source.setAttributeValue(constitutingAttribute,pointer.getTargetSpan());
        // ... and in element
        ((Element)source.getNodeRepresentation()).setAttribute(constitutingAttribute,pointer.getTargetSpan());        
        pointer.updateLinePoints();
        
        currentDiscourse.getMMAX2().setRedrawAllOnNextRefresh(true);
        source.renderMe(MMAX2Constants.RENDER_SELECTED);        
//        currentDiscourse.getMMAX2().putOnRenderingList(pointer);        
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();        
        // Display markable's attributes
        attributePanelContainer.displayMarkableAttributes(source);
        source.getMarkableLevel().setIsDirty(true,false);
    }
    
    
    public final void addMarkableToExistingMarkableSet(Markable addee, MarkableSet set, boolean setRendered)
    {
        String constitutingAttribute = set.getMarkableRelation().getAttributeName();
        // Add new markable to set
        set.addMarkable(addee);
        
        MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(addee);
        
        // Make addee member of set in terms of attributes
        addee.setAttributeValue(constitutingAttribute,set.getAttributeValue());
        ((Element)addee.getNodeRepresentation()).setAttribute(constitutingAttribute,set.getAttributeValue());
        // Re-render after attribute change, to enforce customizazion-dependent attribute changes
        if (setRendered)
        {
            addee.renderMe(MMAX2Constants.RENDER_IN_SET);
        }
        else
        {
            addee.renderMe(MMAX2Constants.RERENDER_THIS);
        }
        set.updateLinePoints(true);
        
        // Rerendering of current primary unnecessary, because the set existed already and no change can
        // have happened
        
        // Init repaint to make new line visible
        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();
        addee.getMarkableLevel().setIsDirty(true,false);
    }            
    
    public final void addMarkableToNewMarkableSet(Markable addee, MarkableRelation relation)
    {
        Markable[] concerned = new Markable[2];
        Markable currentPrimary = currentDiscourse.getMMAX2().getCurrentPrimaryMarkable();
        concerned[0] = addee;
        MMAX2Document doc = this.orderedLevels[0].getCurrentDiscourse().getDisplayDocument();
        doc.startChanges(concerned);
        
        // Create a new ID for the set to be created. This must be unique for all Levels (?)
        String newSetID = getNextFreeMarkableSetID();
        
        // Create and add to a new set for primaryMarkable
        relation.addMarkableWithAttributeValueToMarkableSet(currentPrimary,newSetID);
        
        // Set id of new set to current primary markable
        currentPrimary.setAttributeValue(relation.getAttributeName(),newSetID);
        ((Element)currentPrimary.getNodeRepresentation()).setAttribute(relation.getAttributeName(),newSetID);
        // Make attributewindow reflect the changes in the attributes
        attributePanelContainer.displayMarkableAttributes(currentPrimary);
        
        // Add addee to newly created set for primaryMarkable
        relation.addMarkableWithAttributeValueToMarkableSet(addee,newSetID);        
        addee.setAttributeValue(relation.getAttributeName(),newSetID);
        ((Element)addee.getNodeRepresentation()).setAttribute(relation.getAttributeName(),newSetID);
        addee.renderMe(MMAX2Constants.RENDER_IN_SET);        
        
        // Enforce customization-dependent attribs that may have become valid as a result of the set operation
        currentPrimary.renderMe(MMAX2Constants.RENDER_SELECTED);

        currentDiscourse.getMMAX2().getCurrentViewport().repaint();
        currentDiscourse.getMMAX2().getCurrentTextPane().startAutoRefresh();
        doc.commitChanges();        
        addee.getMarkableLevel().setIsDirty(true,false);
        MMAX2Attribute attrib = addee.getMarkableLevel().getCurrentAnnotationScheme().getUniqueAttributeByName("^"+relation.getAttributeName()+"$");
        addee.getMarkableLevel().getCurrentAnnotationScheme().valueChanged(attrib,attrib,null,0,new ArrayList());
    }
        
    
    public final Markable requestCopyMarkableToLevel(Markable markableToCopy, String levelToCopyTo)
    {
        // Todo: Optionally check for duplicates 
        MarkableLevel toAddTo = getMarkableLevelByName(levelToCopyTo, false);
        Markable newM = toAddTo.addMarkable(markableToCopy.getFragments(), new HashMap());
        if (getCurrentDiscourse().getMMAX2().getSelectAfterCreation())
        {
            markableLeftClicked(newM);
        }
        
        return newM;
    }
    
    public final void deleteMarkable(Markable deletee)
    {
        // Determine all Relations the deletee is part of 
        MarkableLevel level = deletee.getMarkableLevel();
        MarkableRelation[] relations = level.getActiveMarkableSetRelationsForMarkable(deletee);
        for (int z=0;z<relations.length;z++)
        {
            String tempAttribute = ((MarkableRelation)relations[z]).getAttributeName();
            MarkableSet tempSet = ((MarkableRelation)relations[z]).getMarkableSetWithAttributeValue(deletee.getAttributeValue(tempAttribute));
            removeMarkableFromMarkableSet(deletee,tempSet,true);
        }
        
        // Get MarkablePointers the current deletee is a target of 
        ArrayList allMarkablePointers = getAllMarkablePointersForTargetMarkable(deletee);        
        System.err.println("Found "+allMarkablePointers.size()+" pointers ");
        for (int z=0;z<allMarkablePointers.size();z++)
        {
            MarkablePointer currentMP = (MarkablePointer) allMarkablePointers.get(z);
            removeTargetMarkableFromMarkablePointer(deletee, currentMP,true);
        }
        
        // Delete deletee proper
        deletee.deleteMe();
        level.setIsDirty(true,true);
    }
    
    public final ArrayList getAllMarkablePointersForTargetMarkable(Markable target)
    {
        ArrayList result = new ArrayList();
        // Iterate over all levels, because each can have a pointer to deletee
        for (int z=0;z<orderedLevels.length;z++)
        {
            MarkableLevel currentLevel = orderedLevels[z];
            // Collect from each level the MarkablePointers with satellite satellite
            result.addAll(currentLevel.getMarkablePointersForTargetMarkable(target));
        }
        return result;
    }
     
    public final boolean inMarkableFromLevel(String DE_ID, String targetLevelName)
    {
        boolean result = false;
        // Get all markables at this position
        Markable[] allMarkables = getMarkableLevelByName(targetLevelName,true).getAllMarkablesAtDiscourseElement(DE_ID, false);
        if (allMarkables.length >0)
        {
            result = true;
        }
        return result;
    }    
    
    public final boolean inMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        boolean result = false;
        // Get markable that is required to be in some other markable
        Markable currentMarkable = getMarkableLevelByName(ownLevelName,true).getMarkableByID(markableID);
        // Get initial de of that
        String initial = currentMarkable.getFragments()[0][0];
        // Get all markables at this position
        Markable[] allMarkables = getMarkableLevelByName(targetLevelName,true).getAllMarkablesAtDiscourseElement(initial, false);
        // Iterate over allMarkables (only one has to be found to break)
        for (int b=0;b<allMarkables.length;b++)
        {
            if (allMarkables[b].getSize() >=currentMarkable.getSize())
            {
                result = true;
                break;
            }
        }
        return result;
    }
        
    public final boolean startsMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        // NEW: 23. February 2005
        Markable currentMarkable = getMarkableLevelByName(ownLevelName,true).getMarkableByID(markableID);
        String initial = currentMarkable.getFragments()[0][0];
        return getMarkableLevelByName(targetLevelName,true).hasMarkableStartingAt(initial);
    }

    public final boolean finishesMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        // NEW: 23. February 2005
        Markable currentMarkable = getMarkableLevelByName(ownLevelName,true).getMarkableByID(markableID);
        String[][] fragments = currentMarkable.getFragments();
        String[] finalFragment = fragments[fragments.length-1];
        String currentMarkablesEnd = finalFragment[finalFragment.length-1];
        return getMarkableLevelByName(targetLevelName,true).hasMarkableEndingAt(currentMarkablesEnd);
    }
    
    public final String getNextFreeMarkableSetID()
    {
        String newID = "set_"+nextFreeMarkableSetNum+"";
        nextFreeMarkableSetNum++;
        return newID;
    }

    public final String getNextFreeMarkableID()
    {
        String newID = "markable_"+nextFreeMarkableIDNum+"";
        nextFreeMarkableIDNum++;
        return newID;
    }
    
    public final MarkableLevel[] getMarkableLevels()
    {
        return this.orderedLevels;
    }
    
    public final MarkableLevel[] getLevels()
    {
        return getMarkableLevels();
    }
    
    
    public final MarkableLevel[] getActiveLevels()
    {
        MarkableLevel[] result = new MarkableLevel[0];
        ArrayList temp = new ArrayList();
        for (int z=0;z<this.size;z++)
        {
            if (((MarkableLevel)this.orderedLevels[z]).getIsActive() && ((MarkableLevel)this.orderedLevels[z]).isDefined())
            {
                temp.add((MarkableLevel)this.orderedLevels[z]);
            }
        }
        result = (MarkableLevel[]) temp.toArray(new MarkableLevel[0]);
        return result;
    }
}
