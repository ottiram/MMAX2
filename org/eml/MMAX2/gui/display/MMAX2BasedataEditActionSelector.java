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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.Node;

/**
 *
 * @author  mueller
 */
public class MMAX2BasedataEditActionSelector extends javax.swing.JPopupMenu 
{
    MMAX2Discourse discourse=null;
    
    /** Creates a new instance of MMAX2BasedataEditActionSelector */
    public MMAX2BasedataEditActionSelector(final MMAX2Discourse _discourse, final Node referenceNode) 
    {        
        super();
        addPopupMenuListener(new CancellationListener());
        discourse = _discourse;
        JMenuItem item = null;
        
        item = new JMenuItem("Modify this element");
        item.setFont(MMAX2.getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                _discourse.requestAddBasedataElement(referenceNode,MMAX2Constants.EDIT_DE);
            }                    
        });                                                    
        this.add(item);                                            
                        
        addSeparator();
        
        item = new JMenuItem("Delete this element");
        item.setFont(MMAX2.getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                _discourse.requestDeleteBasedataElement(referenceNode);
            }                    
        });                                                    
        this.add(item);                                            
                        
        addSeparator();
        
        
        item = null;
        item = new JMenuItem("Insert before this element");
        item.setFont(MMAX2.getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                _discourse.requestAddBasedataElement(referenceNode,MMAX2Constants.INSERT_DE_BEFORE);
            }                    
        });                                                    
        this.add(item);
        
        item = null;              
        item = new JMenuItem("Insert after this element");
        item.setFont(MMAX2.getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                _discourse.requestAddBasedataElement(referenceNode,MMAX2Constants.INSERT_DE_AFTER);
            }                    
        });                                                    
        this.add(item);                                            
        
        setVisible(true);
        setVisible(false);
    }
    
    
    public class CancellationListener implements javax.swing.event.PopupMenuListener
    {        
        public CancellationListener()
        {
        }
        
        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {            
            discourse.getMMAX2().getCurrentTextPane().setControlIsPressed(false);
        }
        
        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {            
            discourse.getMMAX2().getCurrentTextPane().setControlIsPressed(false);
        }
        
        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {
        }        
    }
    
}
