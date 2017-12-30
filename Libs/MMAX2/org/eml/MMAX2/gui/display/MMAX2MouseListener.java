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

package org.eml.MMAX2.gui.display;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2MouseListener extends MouseAdapter
{
    private MMAX2 mmax2 = null;
    
    public void mousePressed(MouseEvent me)
    {  
            
        if (mmax2 != null)
        {
            evaluateMouseClick(me,MMAX2Constants.MOUSE_PRESSED);
        }
    }

    public void mouseReleased(MouseEvent me)
    {
     
        if (mmax2 != null)
        {
            evaluateMouseClick(me,MMAX2Constants.MOUSE_RELEASED);
        }
    }

    public void mouseExited(MouseEvent me)
    {
        if (mmax2 != null)
        {
            mmax2.getCurrentTextPane().deactivateMarkableHandleHighlight(true);
            mmax2.getCurrentTextPane().setMouseInPane(false);
        }
    }
    
    public void mouseEntered(MouseEvent me)
    {
        if (mmax2 != null)
        {
            mmax2.getCurrentTextPane().setMouseInPane(true);
        }
    }
    
    public void evaluateMouseClick(MouseEvent me, int mode)
    {        
        if (mmax2.getBlockAllInput())
        {
            if (mode == MMAX2Constants.MOUSE_PRESSED)
            {
                // Toolkit.getDefaultToolkit().beep();
            }
            return;
        }
        
        if (mmax2.getCurrentTextPane().getIsDraggingGoingOn())
        {
            mmax2.getCurrentTextPane().setIsDraggingGoingOn(false);
            return;
        }
        
        int currentModifier = me.getModifiers();
       
        if (((currentModifier & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) || me.isPopupTrigger())
        {
            mmax2.getCurrentTextPane().getCurrentCaretListener().setMouseButton(MMAX2Constants.RIGHTMOUSE);
        }        
        else if ((currentModifier & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
        {
            mmax2.getCurrentTextPane().getCurrentCaretListener().setMouseButton(MMAX2Constants.LEFTMOUSE);
        }        
        mmax2.getCurrentTextPane().getCurrentCaretListener().setUpdateMode(mode);        
        
        ((MMAX2Caret)mmax2.getCurrentTextPane().getCaret()).positionCaret(me);
        
    }
        
    /** Called by MMAX2TextPane.setMMAX2(MMAX2 _mmax2) ! */
    protected void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
    }
}