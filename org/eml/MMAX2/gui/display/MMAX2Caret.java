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

import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

public class MMAX2Caret extends DefaultCaret implements Caret
{   
    public MMAX2Caret()
    {
        super();
    }
    
    protected void positionCaret(MouseEvent me)
    {
        super.positionCaret(me);
        /** Trigger change event even if Caret position has not changed! */
        fireStateChanged();
    }       
}

