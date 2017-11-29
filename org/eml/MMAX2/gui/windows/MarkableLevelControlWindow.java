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

package org.eml.MMAX2.gui.windows;

import java.awt.Cursor;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.display.MarkableLevelControlPanel;

public class MarkableLevelControlWindow extends javax.swing.JFrame implements java.awt.event.ActionListener 
{
    MarkableLevelControlPanel panel = null;
    JCheckBoxMenuItem fancy = null;
    JCheckBoxMenuItem activeOnly = null;
    JCheckBoxMenuItem group = null;
    JMenu styleSheets = null;   
    ButtonGroup styleSheetGroup = new ButtonGroup();
    
    /** Creates new MarkableLevelControlWindow */
    public MarkableLevelControlWindow(MarkableLevelControlPanel _panel) throws java.awt.HeadlessException 
    {        
        super();      
        panel = _panel;
        Box contentBox = Box.createVerticalBox();
        contentBox.add(_panel);
        setTitle("Markable level control panel");       
        setFont(MMAX2.getStandardFont());
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        Box buttons = Box.createVerticalBox();
        outerPanel.add(buttons);
        
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(MMAX2.getStandardFont());
        fancy = new JCheckBoxMenuItem("Fancy labels");
        fancy.setSelected(true);
        fancy.setFont(MMAX2.getStandardFont());
        fancy.addActionListener(this);
        fancy.setActionCommand("fancy");
        settingsMenu.add(fancy);
        
        activeOnly = new JCheckBoxMenuItem("Active levels only");
        activeOnly.setSelected(true);
        activeOnly.setFont(MMAX2.getStandardFont());
        activeOnly.addActionListener(this);
        activeOnly.setActionCommand("activeOnly");
        settingsMenu.add(activeOnly);
        
        group = new JCheckBoxMenuItem("Group by level");
        // New March 31st, 2006: Group by default
        group.setSelected(false);        
        group.setFont(MMAX2.getStandardFont());
        group.addActionListener(this);
        group.setActionCommand("group");
        settingsMenu.add(group);

        styleSheets = new JMenu("StyleSheet");
        styleSheets.setFont(MMAX2.getStandardFont());
        settingsMenu.add(styleSheets);
     
        JMenu setAllMenu = new JMenu("Set all levels to");
        setAllMenu.setFont(MMAX2.getStandardFont());
        
        JMenuItem toActiveMenuItem = new JMenuItem("active");
        toActiveMenuItem.setFont(MMAX2.getStandardFont());
        toActiveMenuItem.setActionCommand("set_all_to_active");
        toActiveMenuItem.addActionListener(this);
        setAllMenu.add(toActiveMenuItem);
        
        JMenuItem toVisibleMenuItem = new JMenuItem("visible");
        toVisibleMenuItem.setFont(MMAX2.getStandardFont());
        toVisibleMenuItem.setActionCommand("set_all_to_visible");
        toVisibleMenuItem.addActionListener(this);
        setAllMenu.add(toVisibleMenuItem);
        
        JMenuItem toInactiveMenuItem = new JMenuItem("inactive");
        toInactiveMenuItem.setFont(MMAX2.getStandardFont());
        toInactiveMenuItem.setActionCommand("set_all_to_inactive");
        toInactiveMenuItem.addActionListener(this);
        setAllMenu.add(toInactiveMenuItem);
        
        settingsMenu.add(setAllMenu);
        
        getContentPane().add(contentBox);
        panel.setContainer(this);
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);                
        JMenuBar menu = new JMenuBar();
        menu.add(settingsMenu);
        setJMenuBar(menu);
        setVisible(false);
    }

    public final void setStyleSheetSelector(String[] names)
    {
        for (int p=0;p<names.length;p++)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem((String)names[p]);
            if (p==0) item.setSelected(true);
            styleSheetGroup.add(item);
            item.setFont(MMAX2.getStandardFont());
            item.addActionListener(this);
            item.setActionCommand("stylesheetchange:"+(String)names[p]);
            styleSheets.add(item);
            item = null;
        }        
    }
    
    public final void destroyDependentComponents()
    {
        fancy.removeActionListener(this);
        fancy = null;
        activeOnly.removeActionListener(this);
        activeOnly = null;
        group.removeActionListener(this);
        group = null;
        removeAll();
        panel = null;      
    }
    
    protected void finalize()
    {
        System.err.println("MarkableLevelControlWindow is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }


    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) 
    {       
        if (actionEvent.getSource() instanceof JCheckBoxMenuItem)
        {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem) actionEvent.getSource();
            String command = actionEvent.getActionCommand();
            if (command.equals("activeOnly"))
            {                       
                panel.getMMAX2().setSelectFromActiveLevelsOnly(source.isSelected());
            }
            else if (command.equals("group"))
            {                       
                panel.getMMAX2().setGroupMarkablesByLevel(source.isSelected());
            }        
            else if (command.equals("fancy"))
            {                       
                panel.getMMAX2().setUseFancyLabels(source.isSelected());
            }
        }
        else if (actionEvent.getSource() instanceof JRadioButtonMenuItem)
        {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem) actionEvent.getSource();
            String command = item.getActionCommand();
            if (command.startsWith("stylesheetchange:"))
            {
                String newSheet = command.substring(17);
                if (!panel.getMMAX2().initializing && newSheet.equalsIgnoreCase(panel.getMMAX2().getCurrentDiscourse().getCurrentStyleSheet())==false)
                {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    panel.getMMAX2().getCurrentDiscourse().setCurrentStyleSheet(newSheet);
                    panel.getMMAX2().getCurrentDiscourse().reapplyStyleSheet();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }                
            }
        }
        else
        {
            String command = actionEvent.getActionCommand();
            if (command != null)
            {
                if (command.startsWith("set_all_to"))
                {                    
                    MarkableLevel[] levels = panel.getMMAX2().getCurrentDiscourse().getCurrentMarkableChart().getMarkableLevels();
                    for (int b=0;b<levels.length;b++)
                    {                        
                        if (command.endsWith("_active"))
                        {                        
                            levels[b].setActive();
                        }
                        else if (command.endsWith("_inactive"))
                        {                        
                            levels[b].setInactive();
                        }
                        else if (command.endsWith("_visible"))
                        {                        
                            levels[b].setVisible();
                        }                                                    
                    }
                }
            }
        }        
    }
}
