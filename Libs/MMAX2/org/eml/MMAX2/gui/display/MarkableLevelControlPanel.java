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

// Panel
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.gui.windows.MarkableLevelControlWindow;

public class MarkableLevelControlPanel extends javax.swing.JPanel implements java.awt.event.ActionListener
{
    private Box outerBox = null;
    private Box innerBox = null;
    private Box leftBox = null;
    private Box middleBox = null;
    private Box rightBox = null;

    private String[] styleSheetNames = null;

    private MMAX2Discourse currentDiscourse = null;
    
    private MarkableLevelControlWindow container = null;
    
    /** Creates new form MarkableLayerControlPanel */
    public MarkableLevelControlPanel(MMAX2Discourse _discourse) 
    {        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        currentDiscourse = _discourse;
        TitledBorder tempBorder = new TitledBorder(new LineBorder(Color.black)," Levels ");              
        tempBorder.setTitleColor(Color.black);
        tempBorder.setTitleFont(MMAX2.getStandardFont());
        setBorder(tempBorder);
        outerBox = Box.createVerticalBox();
        innerBox = Box.createHorizontalBox();        
        add(outerBox);
        rightBox = Box.createVerticalBox();
        middleBox = Box.createVerticalBox();
        leftBox = Box.createVerticalBox();
        innerBox.add(leftBox);
        innerBox.add(Box.createHorizontalStrut(6));
        innerBox.add(middleBox);        
        innerBox.add(Box.createHorizontalStrut(6));
        innerBox.add(rightBox);
        outerBox.add(innerBox);
        
        // Create panel to contain buttons
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
        Box tempBox = Box.createHorizontalBox();
        
        tempPanel.add(tempBox);
        // Add distance before buttons 
        outerBox.add(tempPanel);
    }
       
    protected void finalize()
    {
        System.err.println("MarkableLevelControlPanel is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }
        
    public final void destroyDependentComponents()
    {    	
        currentDiscourse = null;
        container = null;        
        clear();
        removeAll();
    }
    
    
    public final MMAX2 getMMAX2()
    {
        return currentDiscourse.getMMAX2();
    }
    
    public final void setContainer(MarkableLevelControlWindow _container)
    {
        container = _container;
    }
    
    public final Dimension getSize()
    {
        return outerBox.getSize();
    }
        
    public final String[] getStyleSheetFileNames()
    {
        return styleSheetNames;
    }
    
    public final void setStyleSheetFileNames(String[] names)
    {
        styleSheetNames = names;
    }
    
    public final void clear()
    {
        int count = leftBox.getComponentCount();
        for (int i=0;i<count;i++)
        {
            ((JPanel) leftBox.getComponent(i)).removeAll();
        }
        
        count = middleBox.getComponentCount();
        for (int i=0;i<count;i++)
        {
            ((JPanel) middleBox.getComponent(i)).removeAll();
        }
        
        count = rightBox.getComponentCount();
        for (int i=0;i<count;i++)
        {
            ((JPanel) rightBox.getComponent(i)).removeAll();
        }        
    }
    
    public final void addLevels(MarkableLevel[] levels)
    {
        for (int z=0;z<levels.length;z++)
        {
            this.addLevel(levels[z]);
        }
    }
   
    public final void addLevel(MarkableLevel level)
    {        
        // Create panel to contain activation selection check box
        JPanel tempPanel = new JPanel();
        // Make panel left-aligned
        tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        // Add activation selection box as first component in panel
        tempPanel.add(level.getActivatorComboBox());
        
        Box tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(level.getUpdateButton());
        tempBox.add(Box.createHorizontalStrut(3));
        tempBox.add(level.getValidateButton());
        tempBox.add(Box.createHorizontalStrut(3));
        tempBox.add(level.getDeleteButton());
        tempBox.add(Box.createHorizontalStrut(3));
        
        tempBox.add(level.getSwitchCheckBox());
        tempPanel.add(tempBox);        
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(level.getMoveUpButton());
        tempBox.add(Box.createHorizontalStrut(1));
        tempBox.add(level.getMoveDownButton());
        tempBox.add(Box.createHorizontalStrut(5));
        tempPanel.add(tempBox);
        // Get label with layer name
        JLabel tempLabel = level.getNameLabel();
        tempLabel.setFont(MMAX2.getStandardFont());
        // Add layer name label as second component in panel
        tempPanel.add(tempLabel);                
        
        middleBox.add(tempPanel);
    }
    
    public final void actionPerformed(java.awt.event.ActionEvent actionEvent) 
    {
        String command = actionEvent.getActionCommand();
        if (command.equals("refresh"))
        {
            currentDiscourse.reapplyStyleSheet();
        }
        else if (command.equals("stylesheetchange"))
        {
            if (!getMMAX2().initializing)
            {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                container.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                currentDiscourse.reapplyStyleSheet();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                container.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}