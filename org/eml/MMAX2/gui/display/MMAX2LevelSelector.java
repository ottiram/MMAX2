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

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.core.MMAX2;

public class MMAX2LevelSelector extends javax.swing.JPopupMenu 
{
    /** Creates new MMAX2LevelSelector */
    public MMAX2LevelSelector(MarkableLevel[] activeLevels, final String fragment, final MarkableChart chart) 
    {
        addPopupMenuListener(new CancellationListener(chart));        
        JMenuItem item = null;
        for (int z=0;z<activeLevels.length;z++)
        {
            item = new JMenuItem("Create Markable on level '"+((MarkableLevel)activeLevels[z]).getMarkableLevelName()+"'");
            item.setFont(MMAX2.getStandardFont());
            final MarkableLevel tempLevel = ((MarkableLevel)activeLevels[z]);
            final boolean selectAfterCreation = chart.getCurrentDiscourse().getMMAX2().getSelectAfterCreation();
            item.addActionListener(new ActionListener()
            {
                public final void actionPerformed(ActionEvent ae)
                {
                    Markable newMarkable = tempLevel.addMarkable(fragment);
                    if (selectAfterCreation)
                    {
                        chart.markableLeftClicked(newMarkable);
                    }
                }                    
            });                        
            add(item);
        } 
        addSeparator();
        final org.eml.MMAX2.core.MMAX2 tempMMAX = chart.getCurrentDiscourse().getMMAX2();
        final int start = chart.getSelectionStart();
        final int end = chart.getSelectionEnd();
        item = null;
        item = new JMenuItem("Copy to clipboard");
        item.setFont(MMAX2.getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                tempMMAX.copyDocumentSpanToClipboard(start,end);
            }                    
        });                        
        add(item);        
    }
    
    public class CancellationListener implements javax.swing.event.PopupMenuListener
    {
        MarkableChart chart = null;
        public CancellationListener(MarkableChart _chart)
        {
            super();
            chart = _chart;
        }
        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {
            
        }
        
        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {            
            chart.removeTemporarySelection();
        }
        
        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {
            
        }        
    }    
}
