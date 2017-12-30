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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eml.MMAX2.core.MMAX2;

public class MMAX2BatchPluginWindow extends JFrame implements ActionListener
{
    JButton allButton=null;
    JButton noneButton=null;
    JButton invertButton=null;
    JButton runButton=null;
    MMAX2 mmax2 = null;
    
    Box outerBox=null;
    
    /** Creates a new instance of MMAX2BatchPluginWindow */
    public MMAX2BatchPluginWindow(MMAX2 _mmax2) 
    {                
        super();
        mmax2 =_mmax2;
        setTitle("Batch Plugin Window");
        outerBox = Box.createVerticalBox();
        JMenu plugins = mmax2.getPluginMenu();
        for (int b=0;b<plugins.getItemCount()-2;b++)
        {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(((JMenuItem)plugins.getItem(b)).getText());
            item.setFont(MMAX2.getStandardFont());
            outerBox.add(item);
        }
        Box buttonBox = Box.createHorizontalBox();
        allButton = new JButton("All");
        allButton.setFont(MMAX2.getStandardFont());
        allButton.addActionListener(this);
        allButton.setActionCommand("all");        
        buttonBox.add(allButton);
        
        noneButton = new JButton("None");
        noneButton.setFont(MMAX2.getStandardFont());
        noneButton.addActionListener(this);
        noneButton.setActionCommand("none");        
        buttonBox.add(noneButton);

        invertButton = new JButton("Invert");
        invertButton.setFont(MMAX2.getStandardFont());
        invertButton.addActionListener(this);
        invertButton.setActionCommand("invert");        
        buttonBox.add(invertButton);        
        
        runButton = new JButton("Run");
        runButton.setFont(MMAX2.getStandardFont());
        runButton.addActionListener(this);
        runButton.setActionCommand("run");        
        buttonBox.add(runButton);
        
        outerBox.add(buttonBox);
        
        getContentPane().add(outerBox);
        pack();
        setResizable(false);        
        setVisible(true);
    }
        
    public void actionPerformed(ActionEvent e) 
    {
        String command = e.getActionCommand();
        if (command.equals("none"))
        {
            for (int b=0;b<outerBox.getComponentCount()-1;b++)
            {                
                ((JCheckBoxMenuItem)outerBox.getComponent(b)).setSelected(false);
            }
        }
        else if (command.equals("all"))
        {
            for (int b=0;b<outerBox.getComponentCount()-1;b++)
            {                
                ((JCheckBoxMenuItem)outerBox.getComponent(b)).setSelected(true);
            }
        }
        else if (command.equals("invert"))
        {
            for (int b=0;b<outerBox.getComponentCount()-1;b++)
            {                
                ((JCheckBoxMenuItem)outerBox.getComponent(b)).setSelected(!((JCheckBoxMenuItem)outerBox.getComponent(b)).isSelected());
            }
        }
        else if (command.equals("run"))
        {
            ArrayList toBeExecuted = new ArrayList();
            for (int b=0;b<outerBox.getComponentCount()-1;b++)
            {                
                JCheckBoxMenuItem item = ((JCheckBoxMenuItem)outerBox.getComponent(b));
                if (item.isSelected())
                {
                    toBeExecuted.add(item.getText());
                }
            }
            mmax2.executeBatchPlugins(toBeExecuted);            
        }            
    }    
}
