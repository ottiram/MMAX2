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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import org.eml.MMAX2.core.MMAX2;

public class MMAX2MouseMotionListener extends MouseMotionAdapter
{
    private MMAX2 mmax2 = null;
    private boolean highlightHandles = true;
    
    public void mouseMoved(MouseEvent me)
    {
        if (!highlightHandles)
        {
            /** Store current MouseEvent on MMAX2TextPane. This will be needed for the positionCaret method.  */
            mmax2.getCurrentTextPane().setCurrentMouseMoveEvent(me);
            return;
        }
        else
        {
            try
            {
                if (me.getPoint().equals(mmax2.getCurrentTextPane().getCurrentMouseMoveEvent().getPoint())) return;
            }
            catch (java.lang.NullPointerException ex)
            {
            
            }
            if (mmax2 != null)
            {
                /** Store current MouseEvent on MMAX2TextPane. This will be needed for the positionCaret method.  */
                mmax2.getCurrentTextPane().setCurrentMouseMoveEvent(me);
                /** When the mouse has been moved, the activateHoveringLatencyTimer is reset. */
                mmax2.getCurrentTextPane().getHoveringLatencyTimer().restart();
                /** Reset any hovering-related display stuff. */
                mmax2.getCurrentTextPane().deactivateFloatingAttributeWindow();
                mmax2.getCurrentTextPane().setCurrentHoveree(null,-1);        
                mmax2.clearStatusBar();        
            }
        }
    }
        
    /** Called by MMAX2TextPane.setMMAX2(MMAX2 _mmax2) ! */    
    protected void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
    }           
}


