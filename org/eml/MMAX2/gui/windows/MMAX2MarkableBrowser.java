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
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableHelper;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2MarkableBrowser extends javax.swing.JFrame implements javax.swing.event.ListSelectionListener, java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.WindowListener
{
    MMAX2 mmax2 = null;
    MarkableChart chart = null;
    JMenu sortOrdersMenu = null;
    JMenu levelMenu = null;
    JList markableList = null;
    JScrollPane scrollPane = null;
    MarkableLevel[] availableLevels = null;    
    String currentLevelName = "";
    String currentSortOrder = "";
    Markable[] markables =  null;
    int selectedIndex = -1;
    JCheckBox KWICView = null;
    JCheckBox easySelect = null;
    boolean autoSelect=false;
    
    Font kwicDisplayFont = null;
    
    JMenu kwicFontMenu = null;
    
    ButtonGroup kwicFontButtonGroup = null;
    
    boolean ignore = false;
    boolean selectCurrent = false;
    
    ButtonGroup sortorderButtonGroup = null;
    ButtonGroup levelButtonGroup = null;   
    
    public MMAX2MarkableBrowser(MMAX2 _mmax2, String initialLevelName, String sorting, Markable initialSelection)     
    {
        currentLevelName = initialLevelName;
        currentSortOrder = sorting;
        addWindowListener(this);
        setResizable(true);
        // Assign local copy of pointer to current MMAX2 instance        
        mmax2 = _mmax2;
        // Get reference to MarkableChart
        chart = mmax2.getCurrentDiscourse().getCurrentMarkableChart();
        // Create menu
        JMenuBar menu = new JMenuBar();
        
        // Get array of available levels
        availableLevels = chart.getMarkableLevels();
        levelButtonGroup = new ButtonGroup();
        levelMenu = new JMenu("Levels");
        levelMenu.setFont(MMAX2.getStandardFont());
        for (int z=0;z<availableLevels.length;z++)
        {
            JRadioButtonMenuItem currentLevel = new JRadioButtonMenuItem(availableLevels[z].getMarkableLevelName());
            currentLevel.setFont(MMAX2.getStandardFont());            
            if (currentLevelName.equals("") && z==0) 
            {
                currentLevel.setSelected(true);
                currentLevelName = availableLevels[z].getMarkableLevelName();
            }
            else if (currentLevelName.equals(availableLevels[z].getMarkableLevelName()))
            {
                currentLevel.setSelected(true);
            }                            
            currentLevel.setActionCommand("levelselection:"+availableLevels[z].getMarkableLevelName());
            currentLevel.addActionListener(this);
            levelButtonGroup.add(currentLevel);
            levelMenu.add(currentLevel);
            currentLevel = null;
        }
        menu.add(levelMenu);                
        
        sortorderButtonGroup = new ButtonGroup();
        sortOrdersMenu = new JMenu("Order");
        sortOrdersMenu.setFont(MMAX2.getStandardFont());
        JRadioButtonMenuItem alphaOrder = new JRadioButtonMenuItem("alpha");
        alphaOrder.setFont(MMAX2.getStandardFont());
        sortorderButtonGroup.add(alphaOrder);
        alphaOrder.setActionCommand("alphaorder");
        alphaOrder.addActionListener(this);
        sortOrdersMenu.add(alphaOrder);
        JRadioButtonMenuItem documentOrder = new JRadioButtonMenuItem("document");
        documentOrder.setFont(MMAX2.getStandardFont());
        sortorderButtonGroup.add(documentOrder);
        documentOrder.setActionCommand("documentorder");
        documentOrder.addActionListener(this);

        if (currentSortOrder.equals("") || currentSortOrder.equals("alpha"))
        {
            alphaOrder.setSelected(true);
            currentSortOrder="alpha";
        }
        else
        {
            documentOrder.setSelected(true);
            currentSortOrder="document";
        }        
        sortOrdersMenu.add(documentOrder);        
        menu.add(sortOrdersMenu);
        
        KWICView = new JCheckBox("KWIC view");
        KWICView.addActionListener(this);
        KWICView.setActionCommand("kwic");
        KWICView.setSelected(false);
        KWICView.setFont(MMAX2.getStandardFont());        
        menu.add(KWICView);                
               
        kwicFontMenu = new JMenu("Font");
        kwicFontMenu.setFont(MMAX2.getStandardFont());
        
        kwicFontButtonGroup = new ButtonGroup();          
        
        JRadioButtonMenuItem standardKwicFont = new JRadioButtonMenuItem("Standard KWIC font");
        standardKwicFont.setFont(MMAX2.getStandardFont());
        standardKwicFont.setActionCommand("standardKwicFont");
        standardKwicFont.addActionListener(this);        
        standardKwicFont.setSelected(true);
        kwicFontButtonGroup.add(standardKwicFont);
        kwicFontMenu.add(standardKwicFont);
        
        kwicDisplayFont = Font.decode("COURIER-BOLD-11");
        
        JRadioButtonMenuItem currentDisplayKwicFont = new JRadioButtonMenuItem("Current display font");
        currentDisplayKwicFont.setFont(MMAX2.getStandardFont());
        currentDisplayKwicFont.setActionCommand("currentDisplayFont");
        currentDisplayKwicFont.addActionListener(this);
        kwicFontButtonGroup.add(currentDisplayKwicFont);
        kwicFontMenu.add(currentDisplayKwicFont);
        kwicFontMenu.setEnabled(false);

        JMenu otherKwicFont = new JMenu("Other");
        otherKwicFont.setFont(MMAX2.getStandardFont());
        JRadioButtonMenuItem temp = null;
    
        GraphicsEnvironment myEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = myEnv.getAvailableFontFamilyNames();
    
        int fontNum = fontNames.length;
    
        for (int z=0;z<fontNum;z++)
        {
            final String currentFontName = fontNames[z];
            temp = new JRadioButtonMenuItem(currentFontName);
            temp.setFont(MMAX2.getStandardFont());
            temp.setActionCommand("kwicFont:"+currentFontName);
            kwicFontButtonGroup.add(temp);
            temp.addActionListener(this);
            kwicFontButtonGroup.add(temp);
            otherKwicFont.add(temp);
            temp=null;
        }
        
        kwicFontMenu.add(otherKwicFont);        
        
        menu.add(kwicFontMenu);
        setJMenuBar(menu);
       
        easySelect = new JCheckBox("Auto-select");
        easySelect.addActionListener(this);
        easySelect.setActionCommand("easyselect");
        easySelect.setSelected(false);
        easySelect.setFont(MMAX2.getStandardFont());        
        menu.add(easySelect);        
                
        markableList = new JList();
        markableList.addListSelectionListener(this);
        markableList.addMouseListener(this);
        markableList.setVisibleRowCount(25);     
        markableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if (mmax2.getUseDisplayFontInPopups())
        {
            markableList.setFont(MMAX2.getMarkableSelectorFont());
        }
        else
        {
            markableList.setFont(MMAX2.getStandardFont());  
        }
                
        scrollPane = new JScrollPane(markableList);        
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Box tempBox = Box.createVerticalBox();
        tempBox.add(scrollPane);
        
        getContentPane().add(tempBox);
        //updateList(initialSelection);
        updateList(-1);
        pack();
        setVisible(true);
        setTitle("MMAX2MarkableBrowser ["+mmax2.registerMarkableBrowser(this)+"]");        
    }
    
    
    public final void refresh()
    {
        if (selectedIndex!=-1)
        {
            updateListQuietly(selectedIndex);
        }
        else
        {
            updateListQuietly(-1);
        }
        invalidate();
        repaint();
    }
    
    public final String getCurrentlyDisplayedMarkableLevelName()
    {
        return currentLevelName;
    }
    
    public final void dismiss()
    {
        mmax2.unregisterMarkableBrowser(this);
        clear();
        dispose();
    }

    
    public final void clear()
    {
        if (selectedIndex != -1)
        {
            MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
            Markable[] concerned = new Markable[1];
            concerned[0] = markables[selectedIndex];
            doc.startChanges(concerned);
            // There is sth. to reset
            if (concerned[0] == mmax2.getCurrentPrimaryMarkable())
            {
                concerned[0].renderMe(MMAX2Constants.RENDER_SELECTED);
            }
            else if (mmax2.getCurrentlyRenderedMarkableRelationContaining(concerned[0])!= null)
            {
                concerned[0].renderMe(MMAX2Constants.RENDER_IN_SET);
            }
            else
            {
                concerned[0].renderMe(MMAX2Constants.RENDER_UNSELECTED);
            }
            doc.commitChanges();
            ignore = true;
            markableList.clearSelection();
            selectedIndex = -1;
        }
    }
    
    public final void updateList(int preselectionIndex)
    {
        Markable preselection = null;
        if (preselectionIndex != -1)
        {
            preselection= markables[preselectionIndex];
        }
        clear();
        markables = (Markable[]) chart.getMarkableLevelByName(currentLevelName,true).getMarkables().toArray(new Markable[0]);
        String[] markablesForDisplay = new String[markables.length];        
        if (currentSortOrder.equals("alpha"))
        {
            java.util.Arrays.sort(markables,MMAX2Discourse.DISCOURSEORDERCOMP);
            java.util.Arrays.sort(markables,MMAX2Discourse.ALPHACOMP);
        }
        else if (currentSortOrder.equals("document"))
        {
            java.util.Arrays.sort(markables,MMAX2Discourse.DISCOURSEORDERCOMP);
        }        
        if (KWICView.isSelected())
        {
            // Fill array with KWIC-versions of markables
            for (int z=0;z<markables.length;z++)
            {
                markablesForDisplay[z] = MarkableHelper.toKWICString(markables[z]);
            }             
            markableList.setListData(markablesForDisplay);
        }
        else
        {
            // Directly use array of markables
            markableList.setListData(markables);
        }
        if (preselection != null)
        {
            ignore=true;
            markableList.setSelectedValue(preselection,true);
            ignore=false;
            selectCurrent = false;
            valueChanged(new ListSelectionEvent(markableList, markableList.getSelectedIndex(),markableList.getSelectedIndex(),false));            
        }
    }

    public final void updateListQuietly(int preselectionIndex)
    {
        Markable preselection = null;
        if (preselectionIndex != -1)
        {
            preselection= markables[preselectionIndex];
        }
        clear();
        markables = (Markable[]) chart.getMarkableLevelByName(currentLevelName,true).getMarkables().toArray(new Markable[0]);
        String[] markablesForDisplay = new String[markables.length];        
        if (currentSortOrder.equals("alpha"))
        {
            java.util.Arrays.sort(markables,MMAX2Discourse.DISCOURSEORDERCOMP);
            java.util.Arrays.sort(markables,MMAX2Discourse.ALPHACOMP);
        }
        else if (currentSortOrder.equals("document"))
        {
            java.util.Arrays.sort(markables,MMAX2Discourse.DISCOURSEORDERCOMP);
        }
        
        if (KWICView.isSelected())
        {
            // Fill array with KWIC-versions of markables
            for (int z=0;z<markables.length;z++)
            {
                markablesForDisplay[z] = MarkableHelper.toKWICString(markables[z]);
            }             
            markableList.setListData(markablesForDisplay);
        }
        else
        {
            // Directly use array of markables
            markableList.setListData(markables);
        }
        if (preselection != null)
        {
            ignore=true;
            markableList.setSelectedValue(preselection,true);
            ignore=false;
            selectCurrent = false;            
        }
    }      
        
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent)
    {
        if (ignore) 
        {
            ignore = false;
            return;
        }        
        if (easySelect.isSelected())
        {
            selectCurrent = true;           
        }
        MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
        Markable[] concerned = new Markable[2];
        JList source = (JList) listSelectionEvent.getSource();       
        {            
            // This is true for the initial setting as well
            if (selectedIndex != -1)
            {
                // There is a currently selected item that is to be overridden
                // Store it in 'concerned' array
                concerned[0]=markables[selectedIndex];                
            }
            selectedIndex = source.getSelectedIndex();
            if (selectedIndex != -1)
            {
                // Store the newly selected one in 'concerned' array as well
                concerned[1]=markables[selectedIndex];
            }
            doc.startChanges(concerned);
            if (concerned[0] != null) 
            {
                // There is sth. to reset
                if (concerned[0] == mmax2.getCurrentPrimaryMarkable())
                {
                    concerned[0].renderMe(MMAX2Constants.RENDER_SELECTED);
                }
                else if (mmax2.getCurrentlyRenderedMarkableRelationContaining(concerned[0])!= null)
                {
                    concerned[0].renderMe(MMAX2Constants.RENDER_IN_SET);
                }
                else
                {
                    concerned[0].renderMe(MMAX2Constants.RENDER_UNSELECTED);
                }
            }
            if (concerned[1] != null) 
            {
                concerned[1].renderMe(MMAX2Constants.RENDER_IN_SEARCHRESULT);
                mmax2.setIgnoreCaretUpdate(true);
                try
                {
                    mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(concerned[1].getLeftmostDisplayPosition()));
                }
                catch (javax.swing.text.BadLocationException ex)
                {
                    System.out.println("Cannot render modelToView()");
                }   
                mmax2.setIgnoreCaretUpdate(false);
                mmax2.getCurrentTextPane().startAutoRefresh();
            }
            doc.commitChanges();
            if (selectCurrent && concerned[1] != null)
            {
                mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(concerned[1]);
                selectCurrent = false;
            }
        }
    }
        
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) 
    {
        if (actionEvent.getSource() instanceof JRadioButtonMenuItem)
        {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem) actionEvent.getSource();
            String action = item.getActionCommand();
            if (action.equals("alphaorder"))
            {
                currentSortOrder = "alpha";
                if (selectedIndex == -1)
                {
                    updateList(-1);
                }
                else
                {
                    updateList(markableList.getSelectedIndex());
                }                       
            }
            else if (action.equals("documentorder"))
            {
                currentSortOrder = "document";
                if (selectedIndex == -1)
                {
                    updateList(-1);
                }
                else
                {
                    updateList(markableList.getSelectedIndex());                    
                }                       
            }
            else if (action.startsWith("levelselection:"))
            {
                currentLevelName = action.substring(action.indexOf(":")+1);
                updateList(-1);
                pack();                
            }
            else if(action.equals("standardKwicFont"))
            {
                kwicDisplayFont = Font.decode("COURIER-BOLD-11");
                markableList.setFont(kwicDisplayFont);
                updateList(markableList.getSelectedIndex());
            }
            else if(action.equals("currentDisplayFont"))
            {
                kwicDisplayFont = MMAX2.getMarkableSelectorFont();
                markableList.setFont(kwicDisplayFont);
                updateList(markableList.getSelectedIndex());
            }
            else if (action.startsWith("kwicFont:"))
            {
                String name = action.substring(action.indexOf(":")+1);
                kwicDisplayFont = Font.decode(name.toUpperCase()+"-BOLD-11");
                markableList.setFont(kwicDisplayFont);
                updateList(markableList.getSelectedIndex());
            }
        }
        else if(actionEvent.getActionCommand().equals("kwic"))
        {
            JCheckBox temp = (JCheckBox)actionEvent.getSource();
            if (temp.isSelected())
            {
                // For KWIC display, always use explicitly selected font
                markableList.setFont(kwicDisplayFont);
                kwicFontMenu.setEnabled(true);
            }
            else
            {
                // For normal display, use displayFont if this font is used in markable selectors as wll
                if (mmax2.getUseDisplayFontInPopups())
                {
                    markableList.setFont(MMAX2.getMarkableSelectorFont());
                }
                else
                    // else, use standard font
                {
                    markableList.setFont(MMAX2.getStandardFont());  
                }
                kwicFontMenu.setEnabled(false);
            }
            updateList(markableList.getSelectedIndex());
        }
    }
    
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) 
    {        
        if (mouseEvent.getClickCount()==1)
        {
            if (easySelect.isSelected())
            {
                selectCurrent = true;
            }
            else
            {
                selectCurrent = false;
            }
        }
        else if (mouseEvent.getClickCount()==2)
        {
            selectCurrent = true;
        }
        valueChanged(new ListSelectionEvent(markableList, markableList.getSelectedIndex(),markableList.getSelectedIndex(),false));
    }
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void windowActivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) 
    {
    }
    
    public void windowClosing(java.awt.event.WindowEvent e) 
    {
        dismiss();
    }
    
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowIconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowOpened(java.awt.event.WindowEvent e) {
    }        
}
