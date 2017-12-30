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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2MarkableSelector extends JPopupMenu
{
    private int maxStringWidth = 80;
    private int size = 0;
    private Markable onlyMarkable = null;
    
    /** Creates new MMAX2MarkableSelector */
    public MMAX2MarkableSelector(Markable[][] markables, MMAX2Discourse _discourse, boolean groupByLevel, final int button) 
    {
        JMenuItem item = null;
        
        Markable[] tempResult = null;
        final MMAX2Discourse currentDiscourse = _discourse;                        
        boolean useDisplayFontInPopups = currentDiscourse.getMMAX2().getUseDisplayFontInPopups();
        Font standardFont = MMAX2.getStandardFont();
        Font displayFont = MMAX2.getMarkableSelectorFont();
        if (!groupByLevel)
        {
            /** Iterate over results for each level */
            for (int p=0;p<markables.length;p++)
            {                    
                tempResult = (Markable[]) markables[p];
                size = size + tempResult.length;
                for (int q=0;q<tempResult.length;q++)
                {   
                    final Markable current = tempResult[q];
                    onlyMarkable = tempResult[q];
                    // Get markable string
                    String entry = tempResult[q].toTrimmedString(maxStringWidth);
                    String prefix = getMarkableSelectorPrefix(current,currentDiscourse);
                    String levelName = (_discourse.getCurrentMarkableChart().getLevelNameByOrderPosition(p)).trim();
                    if (prefix.equals(""))
                    {
                        entry = levelName+ " : "+entry;
                    }
                    else
                    {
                        entry = levelName+ " : "+prefix+" "+entry;
                    }
                    item = new JMenuItem(entry);
                    item.addActionListener(new ActionListener()
                    {
                        public final void actionPerformed(ActionEvent ae)
                        {
                            if (button == MMAX2Constants.LEFTMOUSE)
                            {
                                currentDiscourse.getCurrentMarkableChart().markableLeftClicked(current);
                            }
                            else if (button == MMAX2Constants.RIGHTMOUSE)
                            {
                                currentDiscourse.getCurrentMarkableChart().markableRightClicked(current,-1);
                            }
                        }                    
                    });
                    if (useDisplayFontInPopups)
                    {
                        item.setFont(displayFont);
                    }
                    else
                    {
                        item.setFont(standardFont);
                    }
                    this.add(item);
                    item = null;
                }
            }
        }
        else
        {         
            // Group By Levels
            JMenu levelMenu = null;
            /** Iterate over results for each level */
            for (int p=0;p<markables.length;p++)
            {                    
                // Get set of markables from level p
                tempResult = (Markable[]) markables[p];
                size = size + tempResult.length;
                levelMenu = new JMenu(_discourse.getCurrentMarkableChart().getLevelNameByOrderPosition(p));
                levelMenu.setFont(MMAX2.getMarkableSelectorFont());
                this.add(levelMenu);
                if (tempResult.length ==0) levelMenu.setEnabled(false);
                
                for (int q=0;q<tempResult.length;q++)
                {   
                    final Markable current = tempResult[q];
                    onlyMarkable = tempResult[q];
                    
                    String entry = tempResult[q].toTrimmedString(maxStringWidth);
                    String prefix = getMarkableSelectorPrefix(current,currentDiscourse);                    
                    if (prefix.equals("")==false)
                    {
                        entry = prefix+" "+entry;
                    }
                    item = new JMenuItem(entry);
                    item.addActionListener(new ActionListener()
                    {
                        public final void actionPerformed(ActionEvent ae)
                    {
                            if (button == MMAX2Constants.LEFTMOUSE)
                            {
                                currentDiscourse.getCurrentMarkableChart().markableLeftClicked(current);
                            }
                            else if (button == MMAX2Constants.RIGHTMOUSE)
                            {
                                currentDiscourse.getCurrentMarkableChart().markableRightClicked(current,-1);
                            }
                    }                    
                    });
                    
                    if (useDisplayFontInPopups)
                    {
                        item.setFont(displayFont);
                    }
                    else
                    {
                        item.setFont(standardFont);
                    }
                    levelMenu.add(item);
                    item = null;
                }
                levelMenu = null;
            }            
        }
        setVisible(true);
        setVisible(false);
    }

    private final String getMarkableSelectorPrefix(Markable _current, MMAX2Discourse _discourse)
    {
        String prefix = "";
        String attributeToShow = _discourse.getMMAX2().getShowInMarkableSelectorAttribute(_current.getMarkableLevelName());
        if (attributeToShow.equals("")==false)
        {
            prefix=attributeToShow+"="+_current.getAttributeValue(attributeToShow,"");
        }
        return prefix;
    }
    
    public final Markable getOnlyMarkable()
    {
        return onlyMarkable;
    }
    public final int getItemCount()
    {
        return size;
    }        
}
