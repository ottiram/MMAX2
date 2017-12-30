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
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.windows.MMAX2MarkableBrowser;
import org.eml.MMAX2.utils.MMAX2Constants;


public class MMAX2ActionSelector extends javax.swing.JPopupMenu 
{ // begin class
    private boolean empty = true;
    private boolean initial = true;
    
    public MMAX2ActionSelector(final Markable currentPrimaryMarkable, final String[] DEs, final MarkableChart currentMarkableChart, final int mode)
    {
        JMenuItem item = null;
        empty = false;
        if (mode == MMAX2Constants.REMOVE_DES)
        {
            item = new JMenuItem("Remove from this Markable");
            item.setFont(currentPrimaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            item.addActionListener(new ActionListener()
            {
                public final void actionPerformed(ActionEvent ae)
                {
                    currentMarkableChart.requestModifyMarkablesDEs(currentPrimaryMarkable, DEs, mode);
                }                    
            });                                                    
            this.add(item);                                    
        }
        else //if (mode == MMAX2.ADD_DES_AFTER)
        {
            item = new JMenuItem("Add to this Markable");
            item.setFont(currentPrimaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            item.addActionListener(new ActionListener()
            {
                public final void actionPerformed(ActionEvent ae)
                {
                    currentMarkableChart.requestModifyMarkablesDEs(currentPrimaryMarkable, DEs, mode);
                }                    
            });                                                    
            add(item);                                                
        }
        if (!empty)
        {
            setVisible(true);
            setVisible(false);
        }
      
        addPopupMenuListener(new CancellationListener(currentMarkableChart));
    }
    
    public MMAX2ActionSelector(final Markable currentSecondaryMarkable,  Markable currentPrimaryMarkable, final MarkableChart currentMarkableChart, boolean allowDelete)
    {
                
        JMenuItem item = null;
        if (currentSecondaryMarkable != null)
        {            
            empty = false;
            // New May 18th: Show markable
            item = new JMenuItem(currentSecondaryMarkable.toTrimmedString(80)+" ("+currentSecondaryMarkable.getMarkableLevelName()+")");
            item.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            add(item);
            addSeparator();            
            item = null;
            if (allowDelete)
            {
                //item = null;
                item = new JMenuItem("Delete this Markable");
                item.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
                item.addActionListener(new ActionListener()
                {
                    public final void actionPerformed(ActionEvent ae)
                    {
                        currentMarkableChart.deleteMarkable(currentSecondaryMarkable);
                    }                    
                });                                                    
                add(item);                        
            }
            
            
            // New: Add menu item for markable copying
            JMenu menu = new JMenu("Copy this Markable to");            
            menu.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            // Get all levels to potentially copy the markable to
            MarkableLevel[] levels = currentMarkableChart.getMarkableLevels();
            if (levels.length > 1)
            {
                // There is at least one level to copy to
                // Iterate over all levels
                for (int v=0;v<levels.length;v++)
                {
                    // Get current level
                    MarkableLevel currentLevel = levels[v];                    
                    final String currentLevelName = currentLevel.getMarkableLevelName();
                    JMenuItem currentLevelItem = new JMenuItem(currentLevel.getMarkableLevelName());
                    currentLevelItem.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
                    currentLevelItem.addActionListener(new ActionListener()
                    {
                        public final void actionPerformed(ActionEvent ae)
                        {
                            currentMarkableChart.requestCopyMarkableToLevel(currentSecondaryMarkable,currentLevelName);
                        }                    
                    });
                    menu.add(currentLevelItem);
                    if (currentLevelName.equalsIgnoreCase(currentSecondaryMarkable.getMarkableLevelName())==false &&
                        currentLevel.getIsActive())
                    {
                        currentLevelItem.setEnabled(true);
                    }
                    else
                    {
                        currentLevelItem.setEnabled(false);
                    }                        
                }     
                menu.setEnabled(true);
            }
            else
            {
                menu.setEnabled(false);
            }
            add(menu);
            menu = null;
                        
            menu = null;
            menu = new JMenu("Open in Markable Browser");
            menu.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            JMenuItem alphaItem = new JMenuItem("Alphabetic order");
            alphaItem.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());            
            alphaItem.addActionListener(new ActionListener()
            {
                public final void actionPerformed(ActionEvent ae)
                {
                    new MMAX2MarkableBrowser(currentMarkableChart.getCurrentDiscourse().getMMAX2(), currentSecondaryMarkable.getMarkableLevelName(),"alpha",currentSecondaryMarkable);
                }                    
            });
            
            JMenuItem documentItem = new JMenuItem("Document order");
            documentItem.setFont(currentSecondaryMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getStandardFont());
            documentItem.addActionListener(new ActionListener()
            {
                public final void actionPerformed(ActionEvent ae)
                {
                    new MMAX2MarkableBrowser(currentMarkableChart.getCurrentDiscourse().getMMAX2(), currentSecondaryMarkable.getMarkableLevelName(),"document",currentSecondaryMarkable);
                }                    
            });
            
            menu.add(alphaItem);
            menu.add(documentItem);
            add(menu);                        
            menu = null;                                    
        } 
        final org.eml.MMAX2.core.MMAX2 tempMMAX = currentMarkableChart.getCurrentDiscourse().getMMAX2();
        final Markable marki = currentSecondaryMarkable;
        item = null;
        JMenu copyMenu = new JMenu("Copy to clipboard");
        copyMenu.setFont(currentMarkableChart.getCurrentDiscourse().getMMAX2().getStandardFont());
        item = new JMenuItem("Markable text");
        item.setFont(currentMarkableChart.getCurrentDiscourse().getMMAX2().getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                tempMMAX.copyMarkableToClipboard(marki,false);
            }                    
        });                        
        copyMenu.add(item);        
        
        item = null;
        item = new JMenuItem("Markable text and attributes");
        item.setFont(currentMarkableChart.getCurrentDiscourse().getMMAX2().getStandardFont());
        item.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                tempMMAX.copyMarkableToClipboard(marki,true);
            }                    
        });                        
        copyMenu.add(item);        
        
        add(copyMenu);
        
        if (!empty)
        {
            setVisible(true);
            setVisible(false);
        } 
        addPopupMenuListener(new CancellationListener(currentMarkableChart));        
    }
    
    public MMAX2ActionSelector(MMAX2Attribute[] primaryMarkablesAttributes, final Markable currentPrimaryMarkable, MMAX2Attribute[] secondaryMarkablesAttributes, final Markable currentSecondaryMarkable, final MarkableChart currentMarkableChart) 
    { 
        MMAX2Attribute currentAttribute = null;
        MarkableRelation currentRelation = null;
        JMenuItem item = null;
        
        addPopupMenuListener(new CancellationListener(currentMarkableChart));
        
        /** Iterate over all Attributes of currentPrimaryMarkable. */
        for(int u=0;u<primaryMarkablesAttributes.length;u++)
        {
            // Get current Attribute of primary Markable
            currentAttribute = (MMAX2Attribute) primaryMarkablesAttributes[u];

            if (currentAttribute.getType()==AttributeAPI.MARKABLE_SET)
            {
                // The current attribute is of type MARKABLE_SET
                MarkableSet currentPrimaryMarkablesSet = null;

                // Get MarkableRelation handling this Attribute (is known to be initialized) 
                currentRelation = currentAttribute.getMarkableRelation();

                // Try to get the MarkableSet that the currentPrimaryMarkable is a member of (if any)
                currentPrimaryMarkablesSet = currentRelation.getMarkableSetWithAttributeValue(currentPrimaryMarkable.getAttributeValue(currentAttribute.getLowerCasedAttributeName()));
                if (currentPrimaryMarkablesSet != null)
                {
                    // The current primary markable is member of a set
                    // Possible actions depend on status of secondaryMarkable
                    // If it is in the same set, REMOVE must be possible
                    // If not, and if a corresponding attribute is available
                    // If secondary is not in set itself, ADD must be possible
                    // If secondary is in set itself, MERGE/MIGRATE must be possible
                    if (currentPrimaryMarkablesSet.containsMarkable(currentSecondaryMarkable))
                    {
                        // REMOVE
                        empty = false;
                        item = null;
                        item = new JMenuItem("    "+currentAttribute.getRemoveFromMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                        item.setHorizontalTextPosition(SwingConstants.RIGHT);
                        item.setFont(MMAX2.getStandardFont());
                        final MarkableSet tempSet = currentPrimaryMarkablesSet;
                        item.addActionListener(new ActionListener()
                        {
                            public final void actionPerformed(ActionEvent ae)
                            {
                                currentMarkableChart.removeMarkableFromMarkableSet(currentSecondaryMarkable, tempSet,true);//, tempAttribute);
                            }                    
                        });                        
                        add(item);                            
                    }
                    else // The currentSecondaryMarkable is not part of the same MarkableSet
                    {
                        // If the currentAttribute is _valid_ for it, ADD/ADOPT/MERGE must be possible
                        if (containsAttribute(secondaryMarkablesAttributes,currentAttribute))
                        {
                            // The currentSecondary Markable MAY be part in currentRelation                           
                            // Check if it is another set of the same attribute
                            String temp = currentSecondaryMarkable.getAttributeValue(currentAttribute.getLowerCasedAttributeName());
                            if (temp != null && temp.equals("")==false  && temp.equals(MMAX2.defaultRelationValue)==false)
                            {
                                // The current secondaryMarkable is part in another set for this attribute already
                                // So ADOPT and MERGE must be possible
                                // ADOPT
                                empty = false;
                                item = null;
                                item = new JMenuItem("    "+currentAttribute.getAdoptIntoMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                                item.setHorizontalTextPosition(SwingConstants.RIGHT);
                                item.setFont(MMAX2.getStandardFont());
                                final MarkableSet adoptingSet = currentPrimaryMarkablesSet;
                                final MarkableSet leftSet = currentRelation.getMarkableSetWithAttributeValue(temp);

                                leftSet.setOpaque(true);
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().putOnRenderingList(leftSet);                                
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentViewport().repaint();
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh();                                

                                item.addActionListener(new ActionListener()
                                {
                                    public final void actionPerformed(ActionEvent ae)
                                    {
                                        currentMarkableChart.adoptMarkableToExistingMarkableSet(currentSecondaryMarkable, leftSet, adoptingSet);
                                    }                    
                                });                                                    
                                add(item);                        

                                // MERGE
                                empty = false;
                                item = null;
                                item = new JMenuItem("    "+currentAttribute.getMergeIntoMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                                item.setHorizontalTextPosition(SwingConstants.RIGHT);                                
                                item.setFont(MMAX2.getStandardFont());
                                final MarkableSet adoptingSet2 = currentPrimaryMarkablesSet;
                                final MarkableSet leftSet2 = currentRelation.getMarkableSetWithAttributeValue(temp);
                                leftSet2.setOpaque(true);
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().putOnRenderingList(leftSet2);
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentViewport().repaint();
                                currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh();                                

                                item.addActionListener(new ActionListener()
                                {
                                    public final void actionPerformed(ActionEvent ae)
                                    {
                                        currentMarkableChart.mergeMarkableSets(adoptingSet2, leftSet2);//, tempAttribute2);
                                    }                    
                                });                                                    
                                add(item);                                                        
                            }
                            else // The current secondary markable is not a member of some other set for this relation yet
                            {                            
                                // ADD
                                empty = false;
                                item = null;
                                item = new JMenuItem("    "+currentAttribute.getAddToMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                                item.setFont(MMAX2.getStandardFont());
                                item.setHorizontalTextPosition(SwingConstants.RIGHT);
                                final MarkableSet tempSet = currentPrimaryMarkablesSet;
                                item.addActionListener(new ActionListener()
                                {
                                    public final void actionPerformed(ActionEvent ae)
                                    {
                                        currentMarkableChart.addMarkableToExistingMarkableSet(currentSecondaryMarkable, tempSet,true);//, tempAttribute);
                                    }                    
                                });                                                    
                                add(item);                                                                               
                            }
                        }
                    }
                }
                else
                {
                    // The currentPrimaryMarkable is not part of a MarkableSet
                    // If the currentAttribute is _valid_ for the second, ADD must be possible
                    if (containsAttribute(secondaryMarkablesAttributes,currentAttribute))
                    {
                        // The currentSecondary Markable MAY be part in currentRelation                        
                        // Check if it is another set of the same attribute
                        String temp = currentSecondaryMarkable.getAttributeValue(currentAttribute.getLowerCasedAttributeName());
                        if (temp != null && temp.equals("")==false && temp.equals(MMAX2.defaultRelationValue)==false)
                        {
                            // The current secondaryMarkable is part in another set for this attribute already
                            // So ADOPT and MERGE must be possible
                            // ADOPT
                            empty = false;
                            item = null;
                            item = new JMenuItem("    "+currentAttribute.getAdoptIntoMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                            item.setHorizontalTextPosition(SwingConstants.RIGHT);                            
                            item.setFont(MMAX2.getStandardFont());
                            final MarkableSet leftSet = currentRelation.getMarkableSetWithAttributeValue(temp);
                            leftSet.setOpaque(true);
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().putOnRenderingList(leftSet);                                
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentViewport().repaint();
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh();                                

                            item.addActionListener(new ActionListener()
                            {
                                public final void actionPerformed(ActionEvent ae)
                                {
                                    currentMarkableChart.adoptMarkableToNewMarkableSet(currentSecondaryMarkable, leftSet);//, tempAttribute);
                                }                    
                            });                                                    
                            add(item);                        

                            // MERGE
                            empty = false;
                            item = null;
                            item = new JMenuItem("    "+currentAttribute.getMergeIntoMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                            item.setHorizontalTextPosition(SwingConstants.RIGHT);                            
                            item.setFont(MMAX2.getStandardFont());
                            final MarkableSet adoptingSet2 = currentPrimaryMarkablesSet;
                            final MarkableSet leftSet2 = currentRelation.getMarkableSetWithAttributeValue(temp);
                            leftSet2.setOpaque(true);
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().putOnRenderingList(leftSet2);
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentViewport().repaint();
                            currentMarkableChart.getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh();                                

                            item.addActionListener(new ActionListener()
                            {
                                public final void actionPerformed(ActionEvent ae)
                                {
                                    currentMarkableChart.mergeMarkableSetsIntoNewSet(leftSet2);//, tempAttribute2);
                                }                    
                            });                                                    
                            add(item);                                                
                        }
                        else
                        {
                            // ADD
                            empty = false;
                            item = null;
                            item = new JMenuItem("    "+currentAttribute.getAddToMarkablesetInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                            item.setHorizontalTextPosition(SwingConstants.RIGHT);                            
                            item.setFont(MMAX2.getStandardFont());
                            final MarkableRelation relation = currentAttribute.getMarkableRelation();
                            item.addActionListener(new ActionListener()
                            {
                                public final void actionPerformed(ActionEvent ae)
                                {
                                    currentMarkableChart.addMarkableToNewMarkableSet(currentSecondaryMarkable,relation);
                                }                    
                            });                                                
                            add(item);                        
                        }
                    }
                }
            }// MARKABLE_SET
            else if (currentAttribute.getType()==AttributeAPI.MARKABLE_POINTER)
            {
                // The current attribute is of type MARKABLE_POINTER
                MarkablePointer currentPrimaryMarkablePointer = null;

                // Get MarkableRelation handling this Attribute (is known to be initialized) 
                currentRelation = currentAttribute.getMarkableRelation();
                
                // Try to get the MarkablePointer that the currentPrimaryMarkable is the source of (if any)
                currentPrimaryMarkablePointer = currentRelation.getMarkablePointerForSourceMarkable(currentPrimaryMarkable);                
                if (currentPrimaryMarkablePointer != null)
                {
                    // There is a markable pointer associated with this primary markable
                    if (currentPrimaryMarkablePointer.isTargetMarkable(currentSecondaryMarkable))
                    {
                        // The current secondary markable is a target markable of it
                        // REMOVE pointer to markable
                        empty = false;
                        item = null;
                        item = new JMenuItem("    "+currentAttribute.getRemovePointerToMarkableInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                        item.setHorizontalTextPosition(SwingConstants.RIGHT);                        
                        item.setFont(MMAX2.getStandardFont());
                        final MarkablePointer pointer = currentPrimaryMarkablePointer;
                        item.addActionListener(new ActionListener()
                        {
                            public final void actionPerformed(ActionEvent ae)
                            {
                                currentMarkableChart.removeTargetMarkableFromMarkablePointer(currentSecondaryMarkable,pointer,true);
                            }                    
                        });                                                
                        add(item);                                                
                    }
                    else if (currentPrimaryMarkablePointer.hasMaxSize()==false && currentAttribute.inDomain(currentSecondaryMarkable.getMarkableLevelName()))
                    {
                        // The current secondary markable is not yet a target of it,
                        // and the existing set does not yet have its max size
                        empty = false;
                        item = null;
                        item = new JMenuItem("    "+currentAttribute.getPointToMarkableInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                        item.setHorizontalTextPosition(SwingConstants.RIGHT);                        
                        item.setFont(MMAX2.getStandardFont());
                        final MarkablePointer pointer = currentPrimaryMarkablePointer;
                        item.addActionListener(new ActionListener()
                        {
                            public final void actionPerformed(ActionEvent ae)
                            {
                                currentMarkableChart.addTargetMarkableToExistingMarkablePointer(currentPrimaryMarkable, currentSecondaryMarkable, pointer);
                            }                    
                        });                                                
                        add(item);                                            
                    }
                    else                        
                    {
                        System.err.println("MaxSize reached or illegal domain!");
                    }
                }
                else if (currentAttribute.inDomain(currentSecondaryMarkable.getMarkableLevelName()))
                {
                    // The current primary markable is not source of any pointer set
                    empty = false;
                    item = null;
                    item = new JMenuItem("    "+currentAttribute.getPointToMarkableInstruction(),new BoxIcon(currentAttribute.getLineColor()));
                    item.setHorizontalTextPosition(SwingConstants.RIGHT);                    
                    item.setFont(MMAX2.getStandardFont());
                    final MarkableRelation tempRelation = currentRelation;
                    item.addActionListener(new ActionListener()
                    {
                        public final void actionPerformed(ActionEvent ae)
                        {
                            currentMarkableChart.addTargetMarkableToNewMarkablePointer(currentPrimaryMarkable, currentSecondaryMarkable, tempRelation);
                        }                    
                    });          
                    add(item);                    
                }
            }
            else
            {
                // other relation types
            }
        } // for all primaryMarkablesAttributes        
        
        if (!empty)
        {
            setVisible(true);
            setVisible(false);
        }
        addPopupMenuListener(new CancellationListener(currentMarkableChart));        
    }

    public final boolean isEmpty()
    {
        return empty;
    }
    
    private final boolean containsAttribute(MMAX2Attribute[] attribs, MMAX2Attribute attrib)
    {
        boolean result =false;
        for (int o=0;o<attribs.length;o++)
        {
            if (((MMAX2Attribute)attribs[o]).equals(attrib))
            {
                result = true;
                break;
            }
        }
        return result;
    }
    /*
    private final boolean containsAttributeOfType(MMAX2Attribute[] attribs, int type)
    {
        boolean result =false;
        for (int o=0;o<attribs.length;o++)
        {
            if (((MMAX2Attribute)attribs[o]).getType()==type)
            {
                result = true;
                break;
            }
        }
        return result;
    }
    */
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
            chart.removeTemporarySelection();
        }
        
        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {            
            if (((MMAX2ActionSelector)popupMenuEvent.getSource()).initial==false)
            {
                chart.getCurrentDiscourse().getMMAX2().removeOpaqueRenderablesFromRenderingList();
                // Init repaint to make new line visible
                chart.getCurrentDiscourse().getMMAX2().getCurrentViewport().repaint();
                chart.getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh(); 
                ((MMAX2ActionSelector)popupMenuEvent.getSource()).initial=true;
            }
            else
            {
                ((MMAX2ActionSelector)popupMenuEvent.getSource()).initial=false;
            }            
        }
        
        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent popupMenuEvent) 
        {
        	
        }        
    }
    
    class BoxIcon implements Icon, SwingConstants 
    {
        Color color =null;
        public BoxIcon(Color _color) 
        {
            color = _color;
        }
   
    public void paintIcon(Component c, Graphics g, int x, int y) 
    {
        g.setColor(color);
        g.translate(x, y);
        g.fillRect(10,0, 10,10);
        g.translate(-x, -y);  //Restore Graphics object
        g.setColor(Color.black);
    }
    
    public int getIconHeight() 
    {
        return 10;
    }
    
    public int getIconWidth()
    {
        return 10;
    }        
}
   
} // end class
