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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.annotation.query.MMAX2QueryTree;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2Discourse;


public class MMAX2MarkableSetBrowser extends javax.swing.JFrame implements java.awt.event.ActionListener, java.awt.event.WindowListener, java.awt.event.MouseListener
{    
    
    MMAX2 mmax2 = null;
    JLabel levelBoxLabel = null;
    JComboBox levelBox = null;
    JLabel attributeBoxLabel = null;
    JComboBox attributeBox = null;
    MMAX2Discourse discourse = null;
    JScrollPane treeViewPane = null;
    JTree tree = null;
    DefaultMutableTreeNode root = null;
        
    private JButton refreshButton=null;
    
    private JCheckBox expandAllButton=null;
    
    private ArrayList startMarkables = null;
    private int requiredSetSize=-1;
    
    public MMAX2MarkableSetBrowser(MMAX2 _mmax2) 
    {
        super();
        addWindowListener(this);
        mmax2 = _mmax2;
        discourse = mmax2.getCurrentDiscourse();
                               
        root = new DefaultMutableTreeNode("Document");
        tree = new JTree(root);
        tree.addMouseListener(this);
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
        tempControlBox.add(attributeBox);

        /*
        sizeBoxLabel = new JLabel("Size:");
        sizeBoxLabel.setFont(MMAX2.getStandardFont());
        tempLabelBox.add(sizeBoxLabel);        

        requiredSizeTextField = new JTextField("",5);
        tempControlBox.add(requiredSizeTextField);

        startsWithBoxLabel = new JLabel("Starts with:");
        startsWithBoxLabel.setFont(MMAX2.getStandardFont());
        tempLabelBox.add(startsWithBoxLabel);        
        
        requiredStartAttributes=new JTextField("",5);
        tempControlBox.add(requiredStartAttributes);
        */
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(MMAX2.getStandardFont());
        
        refreshButton.setActionCommand("refresh");
        refreshButton.addActionListener(this);
        
        tempLabelBox.add(new JLabel(" ")); 
        tempControlBox.add(refreshButton);        

        expandAllButton = new JCheckBox("Expand all");
        expandAllButton.setFont(MMAX2.getStandardFont());
        
        tempControlBox.add(expandAllButton);
        panel.add(tempLabelBox);
        panel.add(tempControlBox);        
        
        menu.add(panel);
        setJMenuBar(menu);
        setTitle("MMAX2 Markable Set Browser ["+mmax2.registerMarkableSetBrowser(this)+"]");
        setVisible(true);
        pack();
    }    
        
    public final void update()
    {
        updateSetDisplay((String)levelBox.getSelectedItem(),(String)attributeBox.getSelectedItem());
    }
    
    
    public final String getCurrentlyDisplayedMarkableLevelName()
    {
        return (String) levelBox.getSelectedItem();
    }

    public final String getCurrentlyDisplayedAttributeName()
    {
        return (String) attributeBox.getSelectedItem();
    }
    
    
    public final void updateSetDisplay(String levelName, String attributeName)
    {
        // This method is called when some modification in the attributeBox occurred.
        // It receives the name of the current level and the name of the attribute that
        // was selected on that level, and updates the displayed tree to reflect
        // all sets belonging to that attribute. 
               
        HashSet expanded = new HashSet();
        int setCount = root.getChildCount();
        for (int z=0;z<setCount;z++)
        {            
            DefaultMutableTreeNode node = ( DefaultMutableTreeNode)root.getChildAt(z);            
            if (tree.isExpanded(new TreePath(node.getPath())) || expandAllButton.isSelected())
            {
                expanded.add(((MarkableSet)node.getUserObject()).getAttributeValue());
            }
        }
        root.removeAllChildren();
        
        int resultSize = 0;
        if (attributeName.equals("<none>")==false)
        {                           
            // Get the level of name levelName
            MarkableLevel level = discourse.getCurrentMarkableChart().getMarkableLevelByName(levelName,false);
            // Get the attribute on that level with name attributeName
            MMAX2Attribute attribute = level.getCurrentAnnotationScheme().getUniqueAttributeByName("^"+attributeName.toLowerCase()+"$");
            // Get the relation associated with that attribute
            MarkableRelation relation =attribute.getMarkableRelation();
            MarkableSet[] sets = relation.getMarkableSets(true);
            MarkableSet currentSet = null;
            for (int z=0;z<sets.length;z++)
            {
                currentSet = sets[z];
                
                if (currentSet.matches(requiredSetSize, startMarkables)==false)
                {
                    continue;
                }
                resultSize++;
                DefaultMutableTreeNode setNode = new DefaultMutableTreeNode();
                setNode.setUserObject(currentSet);
                root.add(setNode);               
                ArrayList allElements = new ArrayList(java.util.Arrays.asList(currentSet.getOrderedMarkables()));
                for (int b=0;b<allElements.size();b++)
                {
                    DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode(((Markable)allElements.get(b)).toString());
                    elementNode.setUserObject((Markable)allElements.get(b));
                    setNode.add(elementNode);
                }
                if (expanded.contains(((MarkableSet)setNode.getUserObject()).getAttributeValue()))
                {
                    tree.expandPath(new TreePath(setNode.getPath()));                    
                }
            }
        }        
        tree.updateUI();       
        repaint();        
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
            MMAX2Attribute[] attribs = level.getCurrentAnnotationScheme().getAttributesByType(AttributeAPI.MARKABLE_SET);
            for (int z=0;z<attribs.length;z++)
            {
                attributeBox.addItem(attribs[z].getDisplayAttributeName());
            }
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
   
    
    public final void dismiss()
    {
        mmax2.unregisterMarkableSetBrowser(this);
        dispose();
    }
    

    public void updateSetFilterConditions()
    {
        MMAX2QueryTree tree = null;
        String startsWithCondition ="";
        //String startsWithCondition=requiredStartAttributes.getText().trim();
        if (startsWithCondition.equals("")==false)
        {
            try
            {
                tree = new MMAX2QueryTree(startsWithCondition, discourse.getMarkableLevelByName(getCurrentlyDisplayedMarkableLevelName(),false));
            }
            catch (org.eml.MMAX2.annotation.query.MMAX2QueryException ex)
            {
                ex.printStackTrace();
            }       
            startMarkables = tree.execute(null);            
        }
        else
        {
            startMarkables=null;
        }
        
        //String temp =requiredSizeTextField.getText().trim();
        String temp = "";
        if (temp.equals("")==false)
        {
            requiredSetSize = Integer.parseInt(temp);            
        }
        else
        {
            requiredSetSize=-1;
        }        
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        if (e.getSource() instanceof JButton)
        {
            if (e.getActionCommand().equalsIgnoreCase("refresh"))
            {
                updateSetFilterConditions();
                update();
            }
        }
        else
        {
            JComboBox source = (JComboBox) e.getSource();
            if (source == levelBox)
            {
                // Some action occurred on the box for selecting the levels
                updateAttributeList((String)source.getSelectedItem());            
            }
            else if (source == attributeBox)
            {
                updateSetDisplay((String)levelBox.getSelectedItem(),(String)source.getSelectedItem());
            }
        }
    }
            

    public boolean requestRemoveMarkableFromMarkableSet(MarkableSet sourceSet, Markable removee)
    {
        if (removee!=mmax2.getCurrentPrimaryMarkable())
        {
            mmax2.getCurrentDiscourse().getCurrentMarkableChart().removeMarkableFromMarkableSet(removee, sourceSet,false);
            if (mmax2.getCurrentPrimaryMarkable() != null)
            {
                mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(mmax2.getCurrentPrimaryMarkable());
            }
            return true;
        }
        else
        {
            return false;
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
    
    
    public void mouseClicked(java.awt.event.MouseEvent e) {
    }
    
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent e)
    {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null)
        {
            DefaultMutableTreeNode closestNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            tree.setSelectionPath(selPath);
            if (closestNode.isLeaf()==false && closestNode.isRoot()==false)
            {
                System.err.println(((MarkableSet)closestNode.getUserObject()).toString());
            }
            else if (closestNode.isRoot()==false)
            {                
                // A right-click on a markable in a set occurred
                final Markable selectedMarkable = (Markable)closestNode.getUserObject();
                final MarkableSet selectedSet = (MarkableSet)((DefaultMutableTreeNode)closestNode.getParent()).getUserObject();
                if (e.getButton()==3)
                {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem item = new JMenuItem("Remove from this set");
                    item.setFont(MMAX2.getStandardFont());
                    menu.add(item);
                    item.addActionListener(new ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent ae)
                    {
                        requestRemoveMarkableFromMarkableSet(selectedSet, selectedMarkable);
                    }
                    });
                    menu.show(tree,e.getX(), e.getY());
                }
                else if (e.getButton()==1)
                {
                    if (e.getClickCount()==2)
                    {                        
                        discourse.getCurrentMarkableChart().markableLeftClicked(selectedMarkable);
                        mmax2.setIgnoreCaretUpdate(true);
                        try
                        {
                            mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(selectedMarkable.getLeftmostDisplayPosition()));
                        }
                        catch (javax.swing.text.BadLocationException ex)
                        {
                            System.err.println("Cannot render modelToView()");
                        }   
                        mmax2.setIgnoreCaretUpdate(false);
                        mmax2.getCurrentTextPane().startAutoRefresh();                                                        
                    }
                }
            }
        }
    }
     
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
}
