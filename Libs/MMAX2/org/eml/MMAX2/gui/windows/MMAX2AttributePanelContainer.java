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

package org.eml.MMAX2.gui.windows;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.scheme.MMAX2AnnotationScheme;
import org.eml.MMAX2.core.MMAX2;

public class MMAX2AttributePanelContainer extends javax.swing.JFrame implements java.awt.event.ActionListener
{    
    private MMAX2 mmax2 = null;
    /** Tabbed pane to contain one tab per Attribute Panel. */
    private JTabbedPane tabbedPane = null;
    /** There is one apply Button for all Attribute Panels. */
    private JButton applyButton = null;
    /** There is one undo Button for all Attribute Panels. */
    private JButton undoButton = null;
    /** The is one toFront Button for all Attribute Panels. */
    private JCheckBoxMenuItem toFront = null;  
    /** There is one auto apply button for all Attribute Panels. */
    private JCheckBoxMenuItem autoApply = null;
    
    public JCheckBoxMenuItem hintToFront = null;
    
    /** The Markable object whose attributes are currently displayed in the appropriate tab. */
    private Markable currentMarkable = null;
    private JLabel autoApplyWarning = null;
        
    private JCheckBoxMenuItem attemptAutoUpdate = null;

    private JCheckBoxMenuItem useAnnotationHint = null;

    private HashMap levelToIndex = new HashMap();
    
    private JMenu oneClickAnnotationMenu = null;
    private JMenu settingsMenu = null;
    private ArrayList thisPanelsButtonGroups = new ArrayList();
    
    private JMenuItem updatePanel = null;
    
    /** Creates new MMAX2AttributePanelContainer */
    public MMAX2AttributePanelContainer() 
    {        
        super();                        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);       
        
        // Get reference to content pane
        Container cp = getContentPane();
        
        Box outerBox = Box.createVerticalBox();//
        
        tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);       
        outerBox.add(tabbedPane);        
        Box annoButtonBox = Box.createHorizontalBox();        
        undoButton = new JButton("Undo changes");
        undoButton.setActionCommand("undo");
        undoButton.addActionListener(this);
        undoButton.setFont(MMAX2.getStandardFont());
        applyButton = new JButton("Apply");
        applyButton.setActionCommand("apply");
        applyButton.addActionListener(this);
        applyButton.setFont(MMAX2.getStandardFont());
        annoButtonBox.add(applyButton);        
        annoButtonBox.add(Box.createHorizontalStrut(20));
        annoButtonBox.add(undoButton);
        Box outerButtonBox = Box.createVerticalBox();
        outerButtonBox.add(annoButtonBox);        
        
        Box smallBox = Box.createHorizontalBox();
            
        outerButtonBox.add(Box.createHorizontalStrut(10));
        outerButtonBox.add(smallBox);
        outerBox.add(outerButtonBox);       
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        autoApplyWarning = new JLabel("Auto-apply is OFF");        
        autoApplyWarning.setForeground(Color.green);
        autoApplyWarning.setFont(new Font(MMAX2.getStandardFont().getFontName(),Font.BOLD,MMAX2.getStandardFont().getSize()+5));
        tempPanel.add(autoApplyWarning);
        outerBox.add(tempPanel);
        cp.add(outerBox);        
        setLocation(0,0);
        
        JMenuBar menu = new JMenuBar();
        oneClickAnnotationMenu = new JMenu("One-click annotation");
        oneClickAnnotationMenu.setFont(MMAX2.getStandardFont());

        attemptAutoUpdate = new JCheckBoxMenuItem("Attempt auto-update",true);
        attemptAutoUpdate.setFont(MMAX2.getStandardFont());
        oneClickAnnotationMenu.add(attemptAutoUpdate);
        oneClickAnnotationMenu.addSeparator();                
        
        ButtonGroup group = new ButtonGroup();
        
        JMenu groupValuesMenu = new JMenu("Group values");        
        groupValuesMenu.setFont(MMAX2.getStandardFont());
        JRadioButtonMenuItem item = new JRadioButtonMenuItem("Do not group values");
        item.setSelected(true);
        item.addActionListener(this);
        item.setFont(MMAX2.getStandardFont());
        item.setActionCommand("0");
        group.add(item);
        groupValuesMenu.add(item);
        item=null;
        for (int z=3;z<=24;z+=3)
        {
            item = null;
            item = new JRadioButtonMenuItem(z+"");
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand(z+"");
            item.addActionListener(this);
            group.add(item);
            groupValuesMenu.add(item);
        }
        oneClickAnnotationMenu.add(groupValuesMenu);
        oneClickAnnotationMenu.addSeparator(); 
        menu.add(oneClickAnnotationMenu);                     
        
        JMenu update = new JMenu("Panel");
        update.setFont(MMAX2.getStandardFont());
        updatePanel = new JMenuItem("Update current panel");
        updatePanel.setFont(MMAX2.getStandardFont());
        updatePanel.setActionCommand("update_panel");
        updatePanel.addActionListener(this);
        update.add(updatePanel);
        menu.add(update);
        
        settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(MMAX2.getStandardFont());
        
        toFront = new JCheckBoxMenuItem("Send attribute window to front");
        toFront.setFont(MMAX2.getStandardFont());        
        settingsMenu.add(toFront);
        
        autoApply = new JCheckBoxMenuItem("Auto-apply");
        autoApply.setActionCommand("auto-apply");
        autoApply.addActionListener(this);
        autoApply.setFont(MMAX2.getStandardFont());        
        settingsMenu.add(autoApply);
        
        useAnnotationHint = new JCheckBoxMenuItem("Use anno hint");
        useAnnotationHint.setFont(MMAX2.getStandardFont());        
        settingsMenu.add(useAnnotationHint);
        
        hintToFront = new JCheckBoxMenuItem("Send anno hint window to front");
        hintToFront.setSelected(true);
        hintToFront.setFont(MMAX2.getStandardFont());        
        settingsMenu.add(hintToFront);
        
        menu.add(settingsMenu);
        setJMenuBar(menu);
        
    }

    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
        final MMAX2 finalMMAX2 = _mmax2;
        this.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToBack();
                }
            });        
            
        autoApply.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    
                }
            }); 
        toFront.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    
                }
            }); 
        applyButton.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    
                }
            }); 
      
        undoButton.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    finalMMAX2.annotationHintToFront();
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    
                }
            });                 
    }
    
    public final void setUndoEnabled(boolean status)
    {
        undoButton.setEnabled(status);
    }
    
    public final void setApplyEnabled(boolean status)
    {
        applyButton.setEnabled(status);
    }
    
    public final MMAX2 getMMAX2()
    {
        return mmax2;
    }

    public final boolean isHintToFront()
    {
        return hintToFront.isSelected();
    }

    
    public final boolean isToFront()
    {
        return toFront.isSelected();
    }

    public final boolean isAutoApply()
    {
       return autoApply.isSelected();
    }
    
    public final boolean attemptAutoUpdate()
    {
        return attemptAutoUpdate.isSelected();
    }
    
    protected final void setAutoApply(boolean status)
    {
        if (status)
        {
            autoApplyWarning.setText("Auto-apply is ON ");
            autoApplyWarning.setForeground(Color.red);
        }
        else
        {
            autoApplyWarning.setText("Auto-apply is OFF");
            autoApplyWarning.setForeground(Color.green);            
        }
    }    
    
    public final void initShowInMarkableSelectorPopupMenu()
    {
        int levels = oneClickAnnotationMenu.getItemCount();
        // New: start at 4
        for (int z=4;z<levels;z++)
        {
            JMenu nameMenu = (JMenu) oneClickAnnotationMenu.getItem(z);
            String name = nameMenu.getText();
            int values = nameMenu.getItemCount();
            ArrayList allValues = new ArrayList();
            for (int y=0;y<values;y++)
            {
                allValues.add(nameMenu.getItem(y).getText());
            }
            getMMAX2().addShowInMarkableSelectorEntry(name, allValues);
        }        
    }
    
    public final void addAttributePanel(MMAX2AttributePanel _panel, String levelName)
    {
        // New: Use tab count as index, not componentcount
        levelToIndex.put(levelName,new Integer(tabbedPane.getTabCount()));        
        // New: use addTab, not addComment
        JScrollPane pane = new JScrollPane(_panel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        
        _panel.setScrollPane(pane);
        tabbedPane.addTab(levelName,pane);        
        
        _panel.setAttributePanelContainer(this);
        repaint();
        // Create new menu item for this level
        JMenu item = new JMenu(levelName);
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem temp = new JRadioButtonMenuItem("<none>");
        temp.setSelected(true);
        group.add(temp);
        temp.setFont(MMAX2.getStandardFont());
        temp.setActionCommand("set_oneclick_attribute "+levelName+":::<none>");
        temp.addActionListener(this);
        item.add(temp);
        temp = null;
        
        _panel.setOneClickAnnotationAttributeName("<none>");
        
        item.setFont(MMAX2.getStandardFont());
        String [] allNominals = _panel.getAllNominalMMAX2AttributeNames();        
        for (int z=0;z<allNominals.length;z++)
        {
            temp = new JRadioButtonMenuItem(allNominals[z]);
            group.add(temp);
            temp.setFont(MMAX2.getStandardFont());
            temp.setActionCommand("set_oneclick_attribute "+levelName+":::"+allNominals[z]);
            temp.addActionListener(this);
            item.add(temp);
            temp = null;
        }
        thisPanelsButtonGroups.add(group);        
        oneClickAnnotationMenu.add(item);
    }
    
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) 
    {
        String command = (String)actionEvent.getActionCommand();
        int index = 0;
        if (command.startsWith("set_oneclick_attribute"))
        {
            String level = command.substring(command.indexOf(" "),command.indexOf(":::")).trim();
            String attribute = command.substring(command.indexOf(":::")+3).trim();
            // Get index of panel from level level
            int tmpindex = ((Integer)levelToIndex.get(level)).intValue();
            // get panel itself
            JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(tmpindex);
            JViewport port = (JViewport)pane.getComponent(0);
            MMAX2AttributePanel tempPanel = (MMAX2AttributePanel) port.getView();
            tempPanel.setOneClickAnnotationAttributeName(attribute);
        }        
        else if (command.equals("auto-apply"))
        {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem) actionEvent.getSource();
            setAutoApply(source.isSelected());
        }
        else if (command.equals("apply"))
        {
            // The apply button was clicked, so call setMarkableAttributes on the correct MMAX2AttributePanel and set 
            // attributes for currently selected Markable
            // Get index of correct panel to set
            index = ((Integer)levelToIndex.get(currentMarkable.getMarkableLevelName())).intValue();
            // Get pane at index to front
            tabbedPane.setSelectedIndex(index);
            // Do actual setting 
            // NEW: 25th February 2005: Explicit apply clears keepables list
            
            JScrollPane pane = (JScrollPane) tabbedPane.getSelectedComponent();            
            JViewport port = (JViewport)pane.getComponent(0);
            MMAX2AttributePanel selectedPanel = (MMAX2AttributePanel)port.getView();
            
            selectedPanel.keepables.clear();
            selectedPanel.setMarkableAttributes(currentMarkable,true);
            selectedPanel.displayMarkableAttributes(currentMarkable);
            selectedPanel.setHasUncommittedChanges(false);

            selectedPanel.invalidate();
            selectedPanel.rebuild();
            selectedPanel.repaint();

            invalidate();
            repaint();            
                                    
            // *Always* let apply cause to make annos dirty
            currentMarkable.getMarkableLevel().setIsDirty(true,false);
            currentMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getCurrentTextPane().startAutoRefresh();
        }
        else if (command.equals("undo"))
        {
            index = ((Integer)levelToIndex.get(currentMarkable.getMarkableLevelName())).intValue();
            tabbedPane.setSelectedIndex(index);
            JScrollPane pane = (JScrollPane) tabbedPane.getSelectedComponent();
            JViewport port = (JViewport)pane.getComponent(0);
            MMAX2AttributePanel selectedPanel = (MMAX2AttributePanel)port.getView();
            
            selectedPanel.displayMarkableAttributes(currentMarkable);
            selectedPanel.setHasUncommittedChanges(false);
            
            selectedPanel.invalidate();
            selectedPanel.rebuild();
            selectedPanel.repaint();

            invalidate();
            repaint();                                   
        }
        else if (command.equals("update_panel"))
        {
            String levelName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            MarkableLevel affectedLevel = mmax2.getCurrentDiscourse().getMarkableLevelByName(levelName,false);            
            int tabIndex = tabbedPane.getSelectedIndex();
            tabbedPane.removeTabAt(tabIndex);
            // This will also create a panel!
            MMAX2AnnotationScheme tempScheme = affectedLevel.updateAnnotationScheme();
            tempScheme.setAttributePanelContainer(this);
            tempScheme.setMMAX2(affectedLevel.getCurrentDiscourse().getMMAX2());
            
            JScrollPane pane = new JScrollPane(tempScheme.getAttributePanel(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            tempScheme.getAttributePanel().setScrollPane(pane);
            tabbedPane.insertTab(levelName, null,pane ,"",tabIndex);
            disableAll();
            tabbedPane.setSelectedIndex(tabIndex);
            System.err.println("Panel has been successfully updated!");
        }
        else
        {
            // We expect the actioncommand to be a number
            mmax2.setOneClickAnnotationGroupValue(new Integer(command).intValue());            
        }
    }

    public final boolean getUseAnnotationHint()
    {
        return useAnnotationHint.isSelected();
    }
    
    
    public final void disableAll()
    {
        // New: use tab count
        for (int z=0;z<tabbedPane.getTabCount();z++)
        {            
            JScrollPane pane = ((JScrollPane)tabbedPane.getComponentAt(z));
            JViewport port = (JViewport)pane.getComponent(0);
            ((MMAX2AttributePanel)port.getView()).setEnabled(false);
            ((MMAX2AttributePanel)port.getView()).rebuild();
            tabbedPane.setEnabledAt(z,true);
        }
        applyButton.setEnabled(false);
        undoButton.setEnabled(false);
    }
    
    public final ArrayList getAvailableAttributes(Markable clickedMarkable)
    {
        int index = 0;
        MMAX2AttributePanel panel = null;
        index = ((Integer)this.levelToIndex.get(clickedMarkable.getMarkableLevelName())).intValue();
        JScrollPane pane =(JScrollPane) tabbedPane.getComponentAt(index);
        JViewport port = (JViewport)pane.getComponent(0);
        panel = (MMAX2AttributePanel)port.getView();
        return panel.getAllCurrentAttributes();
        
    }
    /** This method is called when a Markable has been selected on the display. */
    public final void displayMarkableAttributes(Markable selectedMarkable)
    {      
        int index = 0;
        MMAX2AttributePanel panel = null;
        
        // Check whether some Markable is currently selected
        if (currentMarkable != null)
        {
            // There is currently some Markable selected, so unselect it first
            // Get index of panel the markable is displayed on
            index = ((Integer)levelToIndex.get(currentMarkable.getMarkableLevelName())).intValue();
            JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(index);
            JViewport port = (JViewport)pane.getComponent(0);
            // Get panel at this position
            panel = (MMAX2AttributePanel) port.getView();
            // Call this to reset the currently active panel
            panel.displayMarkableAttributes(null);
            // Disable attributes on the panel
            panel.setEnabled(false);
        }
                
        if (selectedMarkable != null)
        {
            // The attributes of a valid Markable are to be displayed
            currentMarkable = selectedMarkable;
            // Get index of MMAX2AttributePanel the selected Markable belongs to
            index = ((Integer)levelToIndex.get(currentMarkable.getMarkableLevelName())).intValue();
            // Enable the corresponding tab
            tabbedPane.setEnabledAt(index,true);
            // Move corresponding tab to front
            tabbedPane.setSelectedIndex(index);
            // Get panel on the correspnding component
            JScrollPane pane = (JScrollPane) tabbedPane.getComponentAt(index);
            JViewport port = (JViewport)pane.getComponent(0);
            panel = (MMAX2AttributePanel) port.getView();
            
            panel.invalidate();
            panel.rebuild();
            panel.repaint();
            
            invalidate();
            repaint();                      
            
            // Enable panel itself
            panel.setEnabled(true);
            // Show attributes
            panel.displayMarkableAttributes(currentMarkable);
            updatePanel.setEnabled(false);
        }
        else
        {
            invalidate();
            repaint();            
            
            setTitle("");
            disableAll();
            updatePanel.setEnabled(true);
        }
    }     
}
