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

import javax.swing.JOptionPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.Element;

public class MMAX2CaretListener implements CaretListener
{            
    private MMAX2 mmax2 = null;
    private int updateMode;
    private int currentMouseButton;
    private boolean isSelection = false;
    private boolean ignoreNext = false; 
    
    MMAX2BasedataEditActionSelector selector = null;
       
    public final void caretUpdate(CaretEvent caretEvent) 
    {               
        if (mmax2.getIgnoreCaretUpdate()) return;
        boolean ambiguousHovering = false;
        
        int currentDot = caretEvent.getDot();
        int currentMark = caretEvent.getMark();
        
        Markable hoveringTargetMarkable = null;
        
        String hotSpot = mmax2.getCurrentDiscourse().getHotSpotAtDisplayAssociation(currentDot);
        if (hotSpot != null && updateMode == MMAX2Constants.MOUSE_RELEASED)
        {
            mmax2.executeHotSpot(hotSpot);
            // Do not do anything else
            return;
        }
        
        /** Get discourse position the mouse is currently on (if any) */
        int discoursePositionAtCaret = mmax2.getCurrentDiscourse().getDiscoursePositionAtDisplayPosition(currentDot);        
        
        if (mmax2.getCurrentTextPane().getIsControlDown())
        {
            if (currentMouseButton == MMAX2Constants.RIGHTMOUSE && updateMode == MMAX2Constants.MOUSE_RELEASED && mmax2.getCurrentPrimaryMarkable()==null)
            {
                if (mmax2.getIsBasedataEditingEnabled()==false)
                {
                    JOptionPane.showMessageDialog(mmax2,"Base data editing currently not enabled!","MMAX2",JOptionPane.INFORMATION_MESSAGE);
                    mmax2.getCurrentTextPane().setControlIsPressed(false);   
                    return;                    
                }
                // Get ID of DE that has been clicked
                String deID = mmax2.getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(discoursePositionAtCaret);
                //System.out.println(deID);
                if (deID.equals(""))
                {
                    return;
                }
                // Get element corresponding to it
                Element word = (Element)mmax2.getCurrentDiscourse().getDiscourseElementNode(deID);                
                // Create action selector
                selector = new MMAX2BasedataEditActionSelector(mmax2.getCurrentDiscourse(), word);
                // Determine pos to show selector at
                int xPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getX();              
                int yPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getY();
                int currentScreenWidth = mmax2.getScreenWidth();
                int selectorWidth = selector.getWidth();
                if ((xPos+mmax2.getX()) > currentScreenWidth/2)
                {
                    xPos = xPos-selectorWidth;
                }
                // Show selector
                selector.show(mmax2.getCurrentTextPane(),xPos,yPos );    
            }            
            // Do not process any other events
            return;
        }
                
        // Override discourse element at position if markable association already exists
        if (mmax2.getCurrentDiscourse().getMarkableAtDisplayAssociation(currentDot)!= null)
        {
            discoursePositionAtCaret = -1;
        }
        
        if (currentDot != currentMark)
        {
            isSelection = true;
            ignoreNext = true;
            if (mmax2.getBlockAllInput()==false)
            {
                mmax2.getCurrentDiscourse().getCurrentMarkableChart().selectionOccurred(currentDot, currentMark);
            }
            else
            {
                return;
            }
        }

        else if (updateMode == MMAX2Constants.MOUSE_HOVERED && isSelection == false)
        {            
            if (discoursePositionAtCaret != -1)
            {
                // The mouse hovers over an element with a valid discourse position
                // Hovering is possible only if only ONE markable is currently active at this discourse position
                // Get all _active_ markables at current discourse position
                Markable[][] markablesAtDiscoursePosition = (Markable[][]) mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkablesAtDiscoursePosition(discoursePositionAtCaret,mmax2.getSelectFromActiveLevelsOnly());
                // Iterate over all markables at current discourse position
                for (int p=0;p<markablesAtDiscoursePosition.length;p++)
                {
                    // Get each layer separately. One entry is returned for each layer, even if it is empty 
                    Markable[] tempResult = (Markable[]) markablesAtDiscoursePosition[p];
                    if (tempResult.length > 1)
                    {
                        // The current level contains more than one Markable, so hovering is ambiguous
                        ambiguousHovering = true;
                        break;
                    }
                    else
                    {
                        // The current level contains maximally one Markable
                        if (tempResult.length == 0)
                        {
                            // The current level is empty, so proceed with next 
                            continue;
                        }
                        else
                        {
                            if (hoveringTargetMarkable == null)
                            {
                                hoveringTargetMarkable = (Markable) tempResult[0];
                            }
                            else
                            {
                                ambiguousHovering = true;
                                break;
                            }
                        }
                    }
                }
                if (!ambiguousHovering) 
                {
                    // We are hoovering over an unambiguously identified markable
                    mmax2.getCurrentTextPane().setCurrentHoveree(hoveringTargetMarkable, currentDot);
                    hoveringTargetMarkable = null;
                }
            }
            else
            {
                // The mouse hoovers over a position which does not have a discourse position associated with it
                // So this is either a Markable handle, or non-clickable display stuff
                // In the latter case, hooveree will be null, else a valid markable
                Markable hoveree = mmax2.getCurrentDiscourse().getMarkableAtDisplayAssociation(currentDot);
                mmax2.getCurrentTextPane().setCurrentHoveree(hoveree, currentDot);  
            }
        }
        else if (updateMode == MMAX2Constants.MOUSE_RELEASED && isSelection == false)
        {
            if (ignoreNext)
            {
                ignoreNext = false;
                return;
            }
            if (discoursePositionAtCaret == -1)
            {
                // The click ocurred either on a MarkableHandle or in empty space
                // So check if there is at least a MarkableHandle
                Markable singleMarkable = mmax2.getCurrentDiscourse().getMarkableAtDisplayAssociation(currentDot);
                if (singleMarkable != null)
                {
                    if (currentMouseButton == MMAX2Constants.LEFTMOUSE)
                    {
                        // A Markable has been selected by left-clicking its handle
                        mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(singleMarkable);
                    }
                    else if (currentMouseButton == MMAX2Constants.RIGHTMOUSE)
                    {
                        // A Markable has been selected by right-clicking its handle
                        mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableRightClicked(singleMarkable,currentDot);
                    }                    
                }
                else
                {
                    // Nothing has been clicked 
                    mmax2.getCurrentDiscourse().getCurrentMarkableChart().nothingClicked(currentMouseButton);
                }
            }
            else
            {
                // The click ocurred on a valid discourse position, so there may be Markables there 
                // Try to get _active_ Markables on this discourse position
                Markable[][] resultSet = (Markable[][]) mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkablesAtDiscoursePosition(discoursePositionAtCaret, mmax2.getSelectFromActiveLevelsOnly());
                MMAX2MarkableSelector selector = new MMAX2MarkableSelector(resultSet, this.mmax2.getCurrentDiscourse(),mmax2.getGroupMarkablesByLevel(),currentMouseButton);
                if (selector.getItemCount()>1)
                {
                    // More than one Markable has been found at discoursePositionAtCaret, so show MarkableSelector
                    int xPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getX();
                    int yPos = mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getY();
                    int currentScreenWidth = mmax2.getScreenWidth();
                    int selectorWidth = selector.getWidth();
                    if ((xPos+mmax2.getX()) > currentScreenWidth/2)
                    {
                        xPos = xPos-selectorWidth;
                    }
                    selector.show(mmax2.getCurrentTextPane(),xPos,yPos );
                }
                else if (selector.getItemCount() == 1)
                {
                    // There is only one Markable at discoursePositionAtCaret, so select directly 
                    if (currentMouseButton == MMAX2Constants.LEFTMOUSE)
                    {                    
                        mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(selector.getOnlyMarkable());
                    }
                    else if (currentMouseButton == MMAX2Constants.RIGHTMOUSE)
                    {                    
                        mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableRightClicked(selector.getOnlyMarkable(),-1);
                    }
                }
                else
                {
                    // Selector was empty, so nothing has been clicked 
                    mmax2.getCurrentDiscourse().getCurrentMarkableChart().nothingClicked(-1);                    
                }
            selector = null;
            }
        }
        else
        {
            isSelection = false;
        }
    } // end caretUpdate

    /** Called by MMAX2TextPane.setMMAX2(MMAX2 _mmax2) ! */
    protected final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
    }
    
    protected final void setUpdateMode(int mode)
    {
        updateMode = mode;
    }
    
    protected final void setMouseButton(int button)
    {
        currentMouseButton = button;
    }            
}// end class

