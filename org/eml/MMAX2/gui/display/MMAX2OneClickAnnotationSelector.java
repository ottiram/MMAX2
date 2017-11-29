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
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.core.MMAX2;

public class MMAX2OneClickAnnotationSelector extends javax.swing.JPopupMenu 
{    
    /** Creates a new instance of MMAX2OneClickAnnotationSelector */
    public MMAX2OneClickAnnotationSelector(final Markable currentPrimary, final MMAX2Attribute oneClickAttribute, final MarkableChart chart, final int displayPos) 
    {
        // Create final copy of current attribute value
        final String currentValue = oneClickAttribute.getSelectedValue();
        // Get list of values to offer in popup menu
        ArrayList allVals = oneClickAttribute.getOrderedValues();
        // Create dummy menu item with attribute name (for display only)
        JMenuItem item = new JMenuItem(oneClickAttribute.getDisplayAttributeName());
        item.setFont(MMAX2.getStandardFont().deriveFont(Font.BOLD));
        add(item);
        item = null;                                 
        
        int stepSize = chart.getCurrentDiscourse().getMMAX2().getOneClickAnnotationGroupValue();
                                
        if (stepSize != 0 && stepSize < allVals.size())
        {
            int steps = allVals.size()/stepSize;
            int rest = allVals.size()-(steps*stepSize);
            
            // Iterate over values to offer, in steps of stepSize
            for (int z=0;z<steps*stepSize;z+=stepSize)
            {
                // Create menu to accept next stepSize values
                String name = "   "+(String)allVals.get(z)+" - "+(String)allVals.get(z+(stepSize-1));            
                JMenu tempMenu = new JMenu(name);
                tempMenu.setFont(MMAX2.getStandardFont());
                for (int b=z;b<z+stepSize;b++)
                {
                    final String newVal = (String)allVals.get(b);
                    // Create menu item with some indentation
                    item = new JMenuItem(newVal);
                    item.setFont(MMAX2.getStandardFont());
                    // Disable current value
                    if (newVal.equalsIgnoreCase(currentValue))
                    {
                        item.setEnabled(false);
                    }
                    else
                    {
                        item.addActionListener(new ActionListener()
                        {
                            public final void actionPerformed(ActionEvent ae)
                            {
                                // Set selected value (this will NOT apply automatically)
                                oneClickAttribute.setSelectedValue(newVal,false);
                                // Try to update the display
                                chart.requestModifyLiteralDisplayText(displayPos, newVal, currentValue);
                            }                    
                        });                                                                    
                    }
                    tempMenu.add(item);
                }
                add(tempMenu);
            }
        
            if (rest!=0)
            {
                // Create menu to accept next stepSize values
                String name="";
                if (rest>1)
                {
                    name = "   "+(String)allVals.get(steps*stepSize)+" - "+(String)allVals.get((steps*stepSize-1)+rest);
                }
                else
                {
                    name = "   "+(String)allVals.get(steps*stepSize);
                }
                JMenu tempMenu = new JMenu(name);
                tempMenu.setFont(MMAX2.getStandardFont());
                for (int b=steps*stepSize;b<steps*stepSize+rest;b++)
                {
                    final String newVal = (String)allVals.get(b);
                    // Create menu item with some indentation
                    item = new JMenuItem(newVal);
                    item.setFont(MMAX2.getStandardFont());
                    // Disable current value
                    if (newVal.equalsIgnoreCase(currentValue))
                    {
                        item.setEnabled(false);
                    }
                    else
                    {
                        item.addActionListener(new ActionListener()
                        {
                            public final void actionPerformed(ActionEvent ae)
                            {
                                // Set selected value (this will NOT apply automatically)
                                oneClickAttribute.setSelectedValue(newVal,false);
                                // Try to update the display
                                chart.requestModifyLiteralDisplayText(displayPos, newVal, currentValue);
                            }                    
                        });                                                                    
                    }
                    tempMenu.add(item);
                }
                add(tempMenu);                
            }
        }
        else
        {
            // Step size is 0, so use normal popup
            // Iterate over values to offer
            for (int z=0;z<allVals.size();z++)
            {
                final String newVal = (String)allVals.get(z);
                // Create menu item with some indentation
                item = new JMenuItem("   "+newVal);
                // Use normal font for shorter lists
                if (allVals.size()<15)
                {
                    item.setFont(MMAX2.getStandardFont());
                }
                else
                {
                    // Use smaller font for longer list
                    item.setFont(MMAX2.getStandardFont().deriveFont(10));
                }
                // Disable current value
                if (newVal.equalsIgnoreCase(currentValue))
                {
                    item.setEnabled(false);
                }
                else
                {
                    item.addActionListener(new ActionListener()
                    {
                        public final void actionPerformed(ActionEvent ae)
                        {
                            // Set selected value (this will NOT apply automatically)
                            oneClickAttribute.setSelectedValue(newVal,false);
                            // Try to update the display
                            chart.requestModifyLiteralDisplayText(displayPos, newVal, currentValue);
                        }                    
                    });                                                                    
                }
                add(item);
            }                                
        }
        setVisible(true);
        setVisible(false);
    }   
}
