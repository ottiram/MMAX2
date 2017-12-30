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

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;

public class MMAX2MarkablePointerBrowser extends javax.swing.JFrame implements java.awt.event.ActionListener, java.awt.event.WindowListener
{        
    MMAX2 mmax2 = null;
    JLabel levelBoxLabel = null;
    JComboBox levelBox = null;
    JLabel attributeBoxLabel = null;
    JComboBox attributeBox = null;
    JCheckBox permanentBox = null;
    MMAX2Discourse discourse = null;
    JScrollPane treeViewPane = null;
    JTree tree = null;
    DefaultMutableTreeNode root = null;        
    
    public MMAX2MarkablePointerBrowser(MMAX2 _mmax2) 
    {
        super();
        addWindowListener(this);
        mmax2 = _mmax2;
        discourse = mmax2.getCurrentDiscourse();
                               
        root = new DefaultMutableTreeNode("Document");
        tree = new JTree(root);
        
        treeViewPane = new JScrollPane(tree);
                        
        getContentPane().add(treeViewPane);
        
        levelBox = new JComboBox();
        levelBox.setFont(MMAX2.getStandardFont());
        levelBox.addItem("<none>");
        MarkableLevel[] levels = discourse.getCurrentMarkableChart().getMarkableLevels();
        for (int b=0;b<levels.length;b++)
        {
            levelBox.addItem(levels[b].getMarkableLevelName());
        }
        levelBox.addActionListener(this);
        
        Box tempLabelBox = Box.createVerticalBox();
        Box tempControlBox = Box.createVerticalBox();
        JMenuBar menu = new JMenuBar();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        levelBoxLabel = new JLabel("Level:");
        levelBoxLabel.setFont(MMAX2.getStandardFont());
        Box tempBox = Box.createVerticalBox();
        tempLabelBox.add(levelBoxLabel);   
        tempLabelBox.add(Box.createVerticalStrut(10));
        tempControlBox.add(levelBox);
        
        attributeBox = new JComboBox();
        attributeBox.setFont(MMAX2.getStandardFont());
        attributeBox.addItem("<none>");
        attributeBox.addActionListener(this);
        attributeBoxLabel = new JLabel("Attribute:");
        attributeBoxLabel.setFont(MMAX2.getStandardFont());
        tempLabelBox.add(attributeBoxLabel);        
        tempLabelBox.add(Box.createVerticalStrut(20));
        tempControlBox.add(attributeBox);
        
        permanentBox = new JCheckBox("Permanent");
        permanentBox.setFont(MMAX2.getStandardFont());
        permanentBox.addActionListener(this);
        permanentBox.setEnabled(false);
        tempControlBox.add(permanentBox);
        panel.add(tempLabelBox);
        panel.add(tempControlBox);        
        
        menu.add(panel);
        setJMenuBar(menu);
        setTitle("MMAX2 Markable Pointer Browser ["+mmax2.registerMarkablePointerBrowser(this)+"]");
        setVisible(true);
        pack();
    }    
    
    public final void update()
    {
        updatePointerDisplay((String)levelBox.getSelectedItem(),(String)attributeBox.getSelectedItem());
    }
    
    public final String getCurrentlyDisplayedMarkableLevelName()
    {
        return (String) levelBox.getSelectedItem();
    }

    public final String getCurrentlyDisplayedAttributeName()
    {
        return (String) attributeBox.getSelectedItem();
    }
    
    
    public final void updatePointerDisplay(String levelName, String attributeName)
    {
        // This method is called when some modification in the attributeBox occurred.
        // It receives the name of the current level and the name of the attribute that
        // was selected on that level, and updates the displayed tree to reflect
        // all pointers belonging to that attribute.                
        
        HashSet expanded = new HashSet();
        int setCount = root.getChildCount();
        for (int z=0;z<setCount;z++)
        {            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(z);            
            if (tree.isExpanded(new TreePath(node.getPath())))
            {
                expanded.add(((MarkablePointer)node.getUserObject()).getSourceMarkable().getID());
            }
        }
        root.removeAllChildren();
        
        if (attributeName.equals("<none>")==false)
        {                           
            // Get the level of name levelName
            MarkableLevel level = discourse.getCurrentMarkableChart().getMarkableLevelByName(levelName,false);
            // Get the attribute on that level with name attributeName
            MMAX2Attribute attribute = level.getCurrentAnnotationScheme().getUniqueAttributeByName("^"+attributeName.toLowerCase()+"$");
            // Get the relation associated with that attribute
            MarkableRelation relation =attribute.getMarkableRelation();
            MarkablePointer[] pointers = relation.getMarkablePointers(true);
            MarkablePointer currentPointer = null;
            for (int z=0;z<pointers.length;z++)
            {
                currentPointer = pointers[z];
                if (permanentBox.isSelected())
                {
                    currentPointer.setIsPermanent(true);
                    mmax2.putOnRenderingList(currentPointer);
                }
                else
                {
                    mmax2.removeFromRenderingList(currentPointer);
                }
                
                DefaultMutableTreeNode pointerNode = new DefaultMutableTreeNode();
                pointerNode.setUserObject(currentPointer);
                root.add(pointerNode);               
                ArrayList allTargets = new ArrayList(java.util.Arrays.asList(currentPointer.getTargetMarkables()));
                for (int b=0;b<allTargets.size();b++)
                {
                    DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode(((Markable)allTargets.get(b)).toString());
                    elementNode.setUserObject((Markable)allTargets.get(b));
                    pointerNode.add(elementNode);
                }
                
                if (expanded.contains(((MarkablePointer)pointerNode.getUserObject()).getSourceMarkable().getID()))
                {
                    tree.expandPath(new TreePath(pointerNode.getPath()));                    
                }                
            }
        }
        tree.updateUI();       
        repaint();        
        if (mmax2.getIsRendering())
        {
            mmax2.setRedrawAllOnNextRefresh(true);
            mmax2.getCurrentTextPane().startAutoRefresh();
        }
    }
    
    public final void updateAttributeList(String levelName)
    {
        // This method is called when some modification in the levelBox occurred.
        // It receives the name of the level that was selected, and updates
        // attributeBox so that it contains the names of all markable_set attributes
        // on this level.
        
        attributeBox.removeActionListener(this);
        // Clear attributeBox in any case
        attributeBox.removeAllItems();
        attributeBox.addItem("<none>");
        
        if (levelName.equals("<none>")==false)
        {           
            MarkableLevel level = discourse.getCurrentMarkableChart().getMarkableLevelByName(levelName, false);
            MMAX2Attribute[] attribs = level.getCurrentAnnotationScheme().getAttributesByType(AttributeAPI.MARKABLE_POINTER);
            for (int z=0;z<attribs.length;z++)
            {
                attributeBox.addItem(attribs[z].getDisplayAttributeName());
            }
        }
        else
        {
            permanentBox.setEnabled(false);
        }
        attributeBox.addActionListener(this);
        
        root.removeAllChildren();
        tree.updateUI();       
        repaint();     
        
        if (attributeBox.getItemCount()==2)
        {
            attributeBox.setSelectedIndex(1);
        }
        pack();
    }
   
    public final void resetPermanentDisplay()
    {
        int setCount = root.getChildCount();
        for (int z=0;z<setCount;z++)
        {            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(z);            
            mmax2.removeFromRenderingList(((MarkablePointer)node.getUserObject()));
        }                        
    }
    
    public final void dismiss()
    {
        mmax2.unregisterMarkablePointerBrowser(this);
        dispose();
    }
    

    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        if (e.getSource() instanceof JCheckBox)
        {
            update();
            return;
        }
        JComboBox source = (JComboBox) e.getSource();
        if (source == levelBox)
        {
            // Some action occurred on the box for selecting the levels
            resetPermanentDisplay();
            updateAttributeList((String)source.getSelectedItem());            
        }
        else if (source == attributeBox)
        {
            resetPermanentDisplay();
            if (source.getSelectedIndex()!=0)
            {
                permanentBox.setEnabled(true);
            }
            else
            {
                permanentBox.setEnabled(false);
            }
            updatePointerDisplay((String)levelBox.getSelectedItem(),(String)source.getSelectedItem());
        }
    }        
            
    public void windowActivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) {
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
