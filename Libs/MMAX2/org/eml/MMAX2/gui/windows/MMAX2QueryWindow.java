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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.eml.MMAX2.annotation.markables.DiscourseOrderMarkableComparator;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableHelper;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.annotation.markables.QueryResultTupleComparator;
import org.eml.MMAX2.annotation.query.Constants;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultList;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTuple;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTupleElement;
import org.eml.MMAX2.annotation.query.MMAX2QueryTree;
import org.eml.MMAX2.annotation.query.simplified.SimplifiedMMAXQLConverter;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.api.QueryAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.DiscourseOrderDiscourseElementComparator;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;

public class MMAX2QueryWindow extends javax.swing.JFrame implements java.awt.event.KeyListener , java.awt.event.ActionListener, javax.swing.event.ListSelectionListener, java.awt.event.MouseListener, java.awt.event.WindowListener, QueryAPI
{            
    private String currentMMAXQLPath="";
    
    private String nameSpace = "";
    
    int selectedIndex = -1;
    boolean selectCurrent = false;
    JCheckBox easySelect = null;
    JCheckBox trimLine = null;
    
    int currentlySelectedRow = 0;    
    int currentlySelectedColumn = 0; 
    MMAX2QueryResultTupleElement[][] currentlyDisplayedResultElements = null;
    
    MMAX2QueryResultTupleElement currentlyHighlightedResultElement = null;
    
    boolean debugMode = false;
    boolean consoleMode = false;
    
    JTextArea inputTextArea = null;
    JTable resultMarkableTupleTable = null;
    JTextArea statisticsOutputArea = null;
    JTabbedPane resultPane = null;
    MarkableChart chart = null;
    String lastResult = "";

    HashMap currentAssignments = new HashMap();
    HashMap creationHandles = new HashMap();
    HashMap creationHandlesToLevelNames = new HashMap();
    
    JButton searchButton = null;
    JButton clearButton = null;
    JButton loadQueryButton = null;

    String currentDefaultLevelName = "";

    MarkableLevel level=null;
    JScrollPane lowerTempPane = null;
    JScrollPane upperTempPane = null;
    JSplitPane splitPane = null;

    JMenuBar menuBar = null;
    
    MMAX2 mmax2 = null;
    
    boolean doLookAheadOptimization = true;
    boolean doBreakOptimization = true;
        
    /** Creates new MMAX2QueryWindow */
    public MMAX2QueryWindow(MarkableChart _chart) 
    {                        
        super("MMAXQL Simple Query Console");
        setIconImage(Toolkit.getDefaultToolkit().getImage("query.gif"));
        addWindowListener(this);
        if (_chart != null)
        {
            // Set reference to current MarkableChart 
            chart = _chart;        
            // Set reference to current MMAX2 
            mmax2 = chart.getCurrentDiscourse().getMMAX2();
            if (mmax2!=null)
            {
                currentMMAXQLPath = mmax2.getCommonQueryPath();
            }
        }
        setResizable(true);
        // Create input area
        inputTextArea = new JTextArea();
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setColumns(50);

        inputTextArea.addKeyListener(this);
        Box tempBox = Box.createVerticalBox();
        
        upperTempPane = new JScrollPane(inputTextArea);
        upperTempPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tempBox.add(upperTempPane);        
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        loadQueryButton = new JButton("Load query");
        
        loadQueryButton.setActionCommand("load_query");
        loadQueryButton.addActionListener(this);
        tempPanel.add(loadQueryButton);
        
        easySelect = new JCheckBox("Auto-select");
        easySelect.setSelected(false);
        tempPanel.add(easySelect);

        trimLine = new JCheckBox("Trim");
        trimLine.setSelected(true);
        tempPanel.add(trimLine);
                
        controlPanel.add(tempPanel);
        searchButton = new JButton("Search");
        searchButton.setLayout(new FlowLayout(FlowLayout.RIGHT));
        searchButton.addActionListener(this);
        searchButton.setActionCommand("search");
        searchButton.setEnabled(true);
        controlPanel.add(searchButton);

        clearButton = new JButton("Clear");
        clearButton.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        clearButton.addActionListener(this);
        clearButton.setActionCommand("clear");
        clearButton.setEnabled(false);
        controlPanel.add(clearButton);
        tempBox.add(controlPanel);
        controlPanel.setMaximumSize(new Dimension(1000, 45));
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);        
        splitPane.setLeftComponent(tempBox);
        
        resultPane = new JTabbedPane(JTabbedPane.BOTTOM);
                
        resultPane.addTab("Markable Tuples",new JScrollPane(null));
        
        statisticsOutputArea = new JTextArea();
        statisticsOutputArea.setFont(Font.decode("COURIER-PLAIN-13"));
        statisticsOutputArea.setEditable(false);
        resultPane.addTab("Statistics",new JScrollPane(statisticsOutputArea));
        
        splitPane.setRightComponent(resultPane);
        getContentPane().add(splitPane);
        
        String greeting = "Welcome to MMAXQL, the MMAX Multi-Level Query Language!\n\nThere are\n";
        MarkableLevel[] levels = new MarkableLevel[0];
        if (chart != null)
        {
            levels = chart.getMarkableLevels();
        }

        JMenu settingsMenu = new JMenu("Settings");
        JMenu defaultQueryLevelMenu = new JMenu("Default Level");

        JRadioButtonMenuItem currentItem = null;
        ButtonGroup levelGroup = new ButtonGroup();
        for (int z=0;z<levels.length;z++)
        {
            String currentName = levels[z].getMarkableLevelName();
            currentItem = new JRadioButtonMenuItem(currentName,z==0);
            if (chart != null && mmax2 != null)
            {
                currentItem.setFont(MMAX2.getStandardFont().deriveFont(8));
            }
            levelGroup.add(currentItem);
            if (z ==0)
            {
                currentDefaultLevelName = currentName;
            }
            currentItem.setActionCommand("defaultlevel:"+currentName);
            currentItem.addActionListener(this);
            defaultQueryLevelMenu.add(currentItem);

            int currentSize = levels[z].getMarkableCount();
            if (z == 0)
            {
                greeting = greeting + currentSize+" markables on level '"+currentName+"' (default) \n";
            }
            else
            {
                greeting = greeting + currentSize+" markables on level '"+currentName+"'\n";
            }
            currentItem = null;
        }

        settingsMenu.add(defaultQueryLevelMenu);
        
        JMenu optimizationMenu = new JMenu("Query Optimization");
        
        JCheckBoxMenuItem lookaheadMenuItem = new JCheckBoxMenuItem("Lookahead");
        lookaheadMenuItem.setSelected(true);
        lookaheadMenuItem.setActionCommand("lookahead");
        lookaheadMenuItem.addActionListener(this);       
        optimizationMenu.add(lookaheadMenuItem);
        
        JCheckBoxMenuItem breakMenuItem = new JCheckBoxMenuItem("Break");
        
        breakMenuItem.setSelected(true);
        breakMenuItem.setActionCommand("break");
        breakMenuItem.addActionListener(this);       
        optimizationMenu.add(breakMenuItem);
        
        settingsMenu.add(optimizationMenu);
        menuBar = new JMenuBar();
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        inputTextArea.setText(greeting+"\n");
        
        if (mmax2 != null)
        {
            inputTextArea.setFont(MMAX2.getStandardFont().deriveFont((float)15));            
            loadQueryButton.setFont(MMAX2.getStandardFont());         
            easySelect.setFont(MMAX2.getStandardFont().deriveFont(8));            
            trimLine.setFont(MMAX2.getStandardFont().deriveFont(8));            
            searchButton.setFont(MMAX2.getStandardFont());
            clearButton.setFont(MMAX2.getStandardFont());
            resultPane.setFont(MMAX2.getStandardFont());
            settingsMenu.setFont(MMAX2.getStandardFont().deriveFont(8));
            defaultQueryLevelMenu.setFont(MMAX2.getStandardFont().deriveFont(8));
            optimizationMenu.setFont(MMAX2.getStandardFont().deriveFont(8));
            lookaheadMenuItem.setFont(MMAX2.getStandardFont().deriveFont(8));
            breakMenuItem.setFont(MMAX2.getStandardFont().deriveFont(8));            
        }
        
    }
    
    public final void moveToNextLineInResult()
    {
        if (resultMarkableTupleTable != null)
        {
            currentlySelectedRow++;
            if (currentlySelectedRow == resultMarkableTupleTable.getRowCount())
            {
                currentlySelectedRow--;
            }                                        
            resultMarkableTupleTable.setRowSelectionInterval(currentlySelectedRow,currentlySelectedRow);
            valueChanged(null);
        }
    }

    public final void moveToPreviousLineInResult()
    {
        if (resultMarkableTupleTable != null)
        {
            currentlySelectedRow--;
            if (currentlySelectedRow == -1)
            {
                currentlySelectedRow=0;
            }                                        
            resultMarkableTupleTable.setRowSelectionInterval(currentlySelectedRow,currentlySelectedRow);
            valueChanged(null);
        }
    }
    
    public final void setConsoleMode()
    {
        consoleMode = true;
    }
    
    public final void setCurrentNameSpace(String _namespace)
    {
        nameSpace = _namespace;
    }

    public final String getCurrentNameSpace()
    {
        return nameSpace;
    }
    
    public void keyPressed(java.awt.event.KeyEvent keyEvent)
    {        
        if (keyEvent.getSource() == inputTextArea)
        {
            if (keyEvent.getKeyCode() == 38)
            {
                boolean isInLastLine = false;
                try
                {
                    if (inputTextArea.getLineOfOffset(inputTextArea.getCaretPosition())==inputTextArea.getLineCount()-1)
                    {
                        isInLastLine = true;
                    }
                }
                catch (javax.swing.text.BadLocationException ex)
                {
                    ex.printStackTrace();
                }                
                if (lastResult.equals("")==false && isInLastLine)
                {                               
                    // Do command line buffering
                    inputTextArea.setText(inputTextArea.getText()+"\n"+lastResult);
                    lastResult = "";
                    keyEvent.consume();
                }
            }
        }
        else if (keyEvent.getSource() instanceof JTable)
        {
            if (keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN)
            {
                currentlySelectedRow++;
                if (currentlySelectedRow == resultMarkableTupleTable.getRowCount() || currentlyDisplayedResultElements[currentlySelectedRow][currentlySelectedColumn]==null)
                {
                    currentlySelectedRow--;
                    keyEvent.consume();
                }                
            }
            else if (keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_UP)
            {
                currentlySelectedRow--;
                if (currentlySelectedRow == -1  || currentlyDisplayedResultElements[currentlySelectedRow][currentlySelectedColumn]==null)
                {
                    currentlySelectedRow = 0;
                    keyEvent.consume();
                }
            }
            else if (keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT)
            {                
                currentlySelectedColumn++;
                if (currentlySelectedColumn == resultMarkableTupleTable.getColumnCount() || currentlyDisplayedResultElements[currentlySelectedRow][currentlySelectedColumn]==null)
                {
                    currentlySelectedColumn--;
                    keyEvent.consume();
                }
                
            }
            else if (keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT)
            {
                currentlySelectedColumn--;
                if (currentlySelectedColumn == -1 || currentlyDisplayedResultElements[currentlySelectedRow][currentlySelectedColumn]==null)
                {
                    currentlySelectedColumn = 0;
                    keyEvent.consume();
                }
            }            
            valueChanged(null);
        }
    }

    public void keyReleased(java.awt.event.KeyEvent keyEvent)
    {}

    public void keyTyped(java.awt.event.KeyEvent keyEvent)
    {
        if (keyEvent.getSource()==inputTextArea)
        {
        
            if (keyEvent.getKeyChar() == '\n')
            {
                searchButton.doClick();
            }
        }
    }

    /** This method receives a complete query string constituting a (potentially complex) 
        MMAX2 Query, and returns as a result an ArrayList of Markables. */
    private final ArrayList executeMMAX2QueryTree(String command)
    {
        String levelName = "";
        String term = "";
        // Make sure result is always non-null
        ArrayList result = new ArrayList();
        // Strip any white space
        command = command.trim();
        if (command.startsWith(".")==false)
        {
            // Get position between level name and query proper
            int pos = command.indexOf("(");
            if (pos == -1)
            {
                return result;
            }
            // Trim to cut off any white space between 'name' and '('
            levelName = command.substring(0,pos).trim();
            term = command.substring(pos).trim();
        }
        else
        {
            // Command was ".", which means default level
            // Extract it
            levelName = currentDefaultLevelName;
            // and cut off leading . and any whitespace
            term = command.substring(1).trim();
        }

        MMAX2QueryTree tree = null;
        // Try to retrieve level of that name
        level = chart.getMarkableLevelByName(levelName,false);
        if (level == null)
        {
            // No level with desired name was found
            JOptionPane.showMessageDialog(null,"Query Semantics Error: No markable level with name '"+levelName+"'!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);            
        }
        else
        {
            // Try to build query tree from query
            try
            {
                tree = new MMAX2QueryTree(term, level);
            }
            catch (org.eml.MMAX2.annotation.query.MMAX2QueryException ex)
            {
                JOptionPane.showMessageDialog(null,ex.getMessage(),"MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
            }
        }
        if (tree != null)
        {
            result = tree.execute();
        }
        return result;
    }

    private final void executeUncreateCommand(String command)
    {        
        // The only parameter in an uncreate is the name of a createnhandle, in ''.
        command = command.substring(1,command.length()-1).trim();
        ArrayList uncreatables = (ArrayList)creationHandles.get(command);
        MarkableLevel targetLevel = mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkableLevelByName((String)creationHandlesToLevelNames.get(command),true);
        if (uncreatables != null)
        {
            for (int z=0;z<uncreatables.size();z++)
            {
                targetLevel.deleteMarkable((Markable)uncreatables.get(z));
            }
            creationHandles.remove(command);
        }
        else
        {
            System.err.println("No handle with name "+command);
        }
    }
    
    private final void executeCreateCommand(String command, boolean allowEmbedding)
    {
        String restCommand="";        
        // Find first ( in command, which is supposed to be the one around the parameter expressions
        int paraStart = command.indexOf("(");
        if (paraStart == -1) 
        {
            System.err.println("Error: Expected '('!");
            return;
        }        
        
        // Get matching top level bracket, i.e. the one after the last parameter expression
        int paraEnd = getMatchingBracketPosition(command,paraStart);
        if (paraEnd == -1)
        {
            System.err.println("Error: Expected ')'!");
            return;            
        }
                                
        restCommand = command.substring(paraStart,paraEnd).trim();        
        // Get list of parameters
        ArrayList temp =  getParameters(restCommand.trim());
        if (temp.size() < 3)
        {
            System.err.println("Error: Use create_with_duplicates|create_no_duplicates ('levelName', queryResult, 'attributeList' [,'columns']");
            return;
        }
        String targetLevelName = (String)temp.get(0);
        MMAX2QueryResultList templates = (MMAX2QueryResultList) temp.get(1);
        String attributeString = (String) temp.get(2);
        ArrayList columns = null;
        if (temp.size()==4 && ((String)temp.get(3)).equals("")==false)
        {
            // a (list of) column specifiers has been supplied
            columns = parseColumnSpecifiers((String)temp.get(3));
        }
        else
        {
            columns = new ArrayList();
            // No column specifiers, so take all
            for (int t=0;t<templates.getWidth();t++)
            {
                columns.add(new Integer(t));
            }
        }
        String creationHandle = "";
        String tempTime = System.currentTimeMillis()+"";
        tempTime = tempTime.substring(tempTime.length()-8);
        
        MarkableLevel targetLevel = mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkableLevelByName(targetLevelName, true);
        
        ArrayList newMarkables = new ArrayList();
        Markable newMarkable = null;
        MMAX2QueryResultTupleElement currentElement = null;
        String[][] currentFragment = null;
        
        // Iterate over all tuples for which to create a markable
        for (int b=0;b<templates.size();b++)
        {
            // Build new list to accept entire span of current tuple
            ArrayList currentAccumulatedDEsAsList = new ArrayList();
            // Iterate over all columns to use for creation (may be all, if no spec was supplied)
            for (int c=0;c<columns.size();c++)
            {
                // Get current element to use, and add up all DiscourseElements
                currentAccumulatedDEsAsList.addAll(java.util.Arrays.asList(templates.getElementAtIndexFromColumn(b,((Integer)columns.get(c)).intValue()).getDiscourseElements(mmax2.getCurrentDiscourse())));
            }       
            // Convert to array
            MMAX2DiscourseElement[] currentAccumulatedDEsAsArray = (MMAX2DiscourseElement[])currentAccumulatedDEsAsList.toArray(new MMAX2DiscourseElement[0]);
            // Sort
            java.util.Arrays.sort(currentAccumulatedDEsAsArray,new DiscourseOrderDiscourseElementComparator());
            // Convert back to list
            currentAccumulatedDEsAsList = new ArrayList(java.util.Arrays.asList(currentAccumulatedDEsAsArray));
            // Remove duplicates (if any)
            MarkableHelper.removeDuplicateDiscoursePositions(currentAccumulatedDEsAsList);            
            
            // Get its fragments (New: This works for (discont) markables as well!)
            currentFragment = MarkableHelper.toFragments(currentAccumulatedDEsAsList);
            
            // New: Real Duplicates should *always* be disallowed!
            // allowDuplicates flag is used to control creation of *embedded* markables
            
            boolean duplicateFound = false;
            boolean embeddedsFound = false;
            // Use only initial element to retrieve markables, but do complete check later
            ArrayList tempList = targetLevel.getMarkablesAtDiscourseElementID(currentFragment[0][0],null);
            if (tempList != null && tempList.size()>0)
            {
                // Some competing markable has been found
                // Iterate over all competing markables
                for (int z=0;z<tempList.size();z++)
                {
                    Markable competitor = (Markable)tempList.get(z);
                    // Check whether we are about to create an already existing markable
                    if (MarkableHelper.equalFragments(competitor.getFragments(), currentFragment))
                    {
                        // There is already an identical markable                            
                        duplicateFound = true;
                        // This will stop creation of current markable
                        break;
                    }               
                    // If we are here, the current candidate is not a duplicate
                    // See if we have to check against embedding
                    if (!allowEmbedding)
                    {
                        // Check whether we are about to create an embedded / embedding markable
                        if (MarkableHelper.duringFragments(competitor.getFragments(), currentFragment) ||
                            MarkableHelper.duringFragments(currentFragment,competitor.getFragments()))
                        {
                            // There is already an identical markable                            
                            embeddedsFound = true;
                            // This will stop creation of current markable
                            break;
                        }                                              
                    }
                }
                    
                if (duplicateFound || embeddedsFound)
                {
                    continue;
                }                                                                
            }
            newMarkable = targetLevel.addMarkable(currentFragment,MarkableHelper.parseAttributesToHashMap(attributeString));            
            newMarkables.add(newMarkable);
            newMarkable = null;            
        }
        
        creationHandle = targetLevelName+"_"+tempTime;
        if (creationHandles.containsKey(creationHandle))
        {
        	tempTime = System.currentTimeMillis()+"";
        	tempTime = tempTime.substring(tempTime.length()-8);                
        	creationHandle = targetLevelName+"_"+tempTime;
        }
        // Some creation handle has been passed in
        System.err.println("Storing creation result in handle "+creationHandle+" for uncreate!");
        inputTextArea.append("\nAdded "+newMarkables.size()+" markables with handle '"+creationHandle+"'\n");
            
        // Use it, potentially overwriting older mappings
        creationHandles.put(creationHandle,newMarkables);
        creationHandlesToLevelNames.put(creationHandle, targetLevelName);
    }
    
    private final void executeSetValuesCommand(String command)
    {
        String restCommand="";        
        // Find first ( in command, which is supposed to be the one around the parameter expressions
        int paraStart = command.indexOf("(");
        if (paraStart == -1) 
        {
            System.err.println("Error: Expected '('!");
            return;
        }        
        System.err.println("Set values command!");
        
        // Get matching top level bracket, i.e. the one after the last parameter expression
        int paraEnd = getMatchingBracketPosition(command,paraStart);
        if (paraEnd == -1)
        {
            System.err.println("Error: Expected ')'!");
            return;            
        }
                                
        restCommand = command.substring(paraStart,paraEnd).trim();        
        // Get list of parameters
        ArrayList temp =  getParameters(restCommand.trim());
        if (temp.size() != 2)
        {
            System.err.println("Error: Use set_value (queryResult, 'attributeList'");
            return;
        }
        MMAX2QueryResultList templates = (MMAX2QueryResultList) temp.get(0);
        String attributeString = (String) temp.get(1);        
                          
        // Get attributes and values as to synchronized lists
        ArrayList[] attVals = MarkableHelper.parseAttributesToLists(attributeString);
        ArrayList attributes = attVals[0];
        ArrayList values = attVals[1];
        int attNum = attributes.size();
        
        // Iterate over all markables for which to set values
        for (int b=0;b<templates.size();b++)
        {
            Markable currentMarkable = templates.getElementAtIndexFromColumnToUse(b).getMarkable();
            MarkableLevel level = currentMarkable.getMarkableLevel();
            level.setIsDirty(true,false);
            // Iterate over attribute-value list
            for (int q=0;q<attNum;q++)
            {               
                currentMarkable.setAttributeValue((String)attributes.get(q),(String)values.get(q));
                currentMarkable.setAttributeValueToNode((String)attributes.get(q),(String)values.get(q));
            }
        }
    }
    
    
    private final void setQueryResult(MMAX2QueryResultList queryResultTuples)
    {   
    	if (mmax2 == null) return;
    	
        if (queryResultTuples != null && queryResultTuples.size()>0)
        {
            clear();
            resultMarkableTupleTable = null;
            System.gc();
            resultMarkableTupleTable = createQueryResultTable(queryResultTuples);

            resultMarkableTupleTable.changeSelection(0,0, false,false);
            currentlySelectedRow = 0;
            currentlySelectedColumn = 0;
            valueChanged(null);
            JScrollPane pane = new JScrollPane(resultMarkableTupleTable);
            pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            resultPane.setComponentAt(0, pane);
            resultPane.setTitleAt(0, "Markable Tuples ("+queryResultTuples.size()+")");
            clearButton.setEnabled(true);
        }
        else if (queryResultTuples != null && queryResultTuples.size()==0)
        {
            clear();
            resultPane.setComponentAt(0, new JScrollPane(null));
            resultPane.setTitleAt(0, "Markable Tuples (0)");                        
            clearButton.setEnabled(false);
        }        
        else
        {
            clear();
            resultPane.setComponentAt(0, new JScrollPane(null));
            resultPane.setTitleAt(0, "Markable Tuples");                        
            clearButton.setEnabled(false);
        }        
        
        resultPane.setSelectedIndex(0);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
        getGlassPane().setVisible(false);
    }
        
    private final static MMAX2QueryResultList sort(MMAX2QueryResultList list, int index)
    {
        // Convert MMAX2QueryResultList into array of MMAX2QueryResultTuples
        MMAX2QueryResultTuple[] temp = (MMAX2QueryResultTuple[]) list.toArray(new MMAX2QueryResultTuple[0]);
        // If this field is identical, and more fields are available, these will be used recursively
        Arrays.sort(temp,new QueryResultTupleComparator(index));
        // Re-convert into list of MMAX2QueryResultTuples
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        result.addAll(Arrays.asList(temp));
        // New April 4th, 2005: Retain indexToUse on new sorted list, if it was explicitly set
        // This will only continue an earlier setting of this feature, OK
        if (list.isIndexSetByUser())
        {
            result.setIndexToUse(list.getIndexToUse());
        }
        // New 
        result.setCommand(list.getCommand());
        return result;
    }
    
    
    /** This method accepts a MMAX2QueryResultList and creates from it a JTable for display purposes. */
    private final JTable createQueryResultTable(MMAX2QueryResultList tupelList)
    {        
        // Get list of attributes to be displayed in the table (besides/below markable text)
        ArrayList attributeNamesToDisplay = tupelList.getAttributeNamesToDisplay();       
        
        // This is either 0, or the index set explicitly by the user
        int defaultIndex = tupelList.getIndexToUse();
        //ArrayList orderedList = sort(tupelList);
        // New April 1st: Always use index 0 for sorting for final display
        MMAX2QueryResultList orderedList = sort(tupelList,0);
        
        // Get max. width of tuples in tupelList (i.e. number of columns to display)
        int width = tupelList.getWidth();        
        boolean headerFinished = false;

        // Check if non-default index is set (via column specifier)
        //if (tupelList.getIndexToUse()!=0)
        // This is for output only, OK
        if (tupelList.isIndexSetByUser())
        {
            // If yes, display only one column
            width = 1;
        }
        
        // If index is not set explicitly, width == maximum width, i.e. there may be null elements
        
        ArrayList colsToHighlight = new ArrayList();
        for (int b=0;b<width;b+=2)
        {
            colsToHighlight.add(new Integer(b));
        }
                
        // Create n-dim array to accept tuples
        currentlyDisplayedResultElements = null;
        currentlyDisplayedResultElements = new MMAX2QueryResultTupleElement[orderedList.size()][width];
        // Create max. array to accept data for display
        // Some of its cells might remain empty !!
        String[][] tableData = new String[orderedList.size()][width];
        // Create array for header
        String header[] = new String[width];
        // Iterate over all MMAX2QueryResultTuples
        for (int z=0;z<orderedList.size();z++)
        {
            // Get current tuple
            MMAX2QueryResultTuple currentTuple = (MMAX2QueryResultTuple)orderedList.get(z);
            // This is for output only, OOK
            if (tupelList.isIndexSetByUser())
            {
                // The supplied list had a column specifier, so display only column at indexToUse
                //Markable markableToDisplay = currentTuple.getValueAt(defaultIndex);
                MMAX2QueryResultTupleElement elementToDisplay = currentTuple.getValueAt(defaultIndex);
                // indexToUse might by larger than width of current tuple, 
                // so markableToDisplay might be null!!
                // Use empty as default value to display in table
                String valueToSet = " ";
                if (elementToDisplay != null)
                {
                    // There is a markable at indexToUse in the current tuple
                    // So use its string for display
                    // New April 7th: Use trimmed string
                    if (trimLine.isSelected())
                    {
                        valueToSet = elementToDisplay.toTrimmedString(60);
                    }
                    else
                    {
                        valueToSet = elementToDisplay.toString();
                    }
                    // Check for additional attributes to be displayed in table
                    if (attributeNamesToDisplay != null)
                    {
                        valueToSet = valueToSet+"\n ";
                        String currentAttribute="";
                        // Iterate over all attribute to be displayed in table
                        for (int m=0;m<attributeNamesToDisplay.size();m++)
                        {
                            currentAttribute = (String)attributeNamesToDisplay.get(m);
                            valueToSet=valueToSet+currentAttribute+"=\""+elementToDisplay.getAttributeValue(currentAttribute,"UNDEFINED")+"\"";
                            if (m<attributeNamesToDisplay.size()-1)
                            {
                                valueToSet=valueToSet+",";
                            }
                        }                    
                    }
                }
                tableData[z][0] = valueToSet;
                // This may cause a null assignment !!
                if (elementToDisplay != null)// && elementToDisplay.isMarkable())
                {
                    currentlyDisplayedResultElements[z][0] = elementToDisplay;
                }
                if (!headerFinished && elementToDisplay != null)
                {
                    header[0] = elementToDisplay.getLevelName()+" ("+defaultIndex+")";
                    headerFinished = true;    
                }                
            }
            else
            {
                // Index is not set by user, so display all columns
                for (int w=0;w<width;w++)
                {
                    // Get markable at current position
                    MMAX2QueryResultTupleElement elementToDisplay = currentTuple.getValueAt(w);
                    // This may cause a null assignment !!
                    if (elementToDisplay != null)// && elementToDisplay.isMarkable())
                    {
                        currentlyDisplayedResultElements[z][w] = elementToDisplay;
                    }
                    
                    String valueToSet = " ";

                    if (elementToDisplay!=null)
                    {
                        // New April 7th: Use trimmed string
                        if (trimLine.isSelected())
                        {
                            valueToSet = elementToDisplay.toTrimmedString(60);
                        }
                        else
                        {
                            valueToSet = elementToDisplay.toString();
                        }
                        
                        if (attributeNamesToDisplay != null)
                        {
                            valueToSet = valueToSet+"\n ";
                            String currentAttribute="";
                            for (int m=0;m<attributeNamesToDisplay.size();m++)
                            {
                                currentAttribute = (String)attributeNamesToDisplay.get(m);
                                valueToSet=valueToSet+currentAttribute+"=\""+elementToDisplay.getAttributeValue(currentAttribute,"UNDEFINED")+"\"";
                                if (m<attributeNamesToDisplay.size()-1)
                                {
                                    valueToSet=valueToSet+",";
                                }
                            }                    
                        }
                    }                    
                    tableData[z][w] = valueToSet;
                    if (!headerFinished  && elementToDisplay != null)
                    {
                        if (tableData[z][w] != null)
                        {
                            header[w] = elementToDisplay.getLevelName()+" ("+w+")";
                        }
                    }
                }
            }
        }
        
        DefaultTableModel dm = new DefaultTableModel() 
        {
             public Class getColumnClass(int columnIndex)
             {
                return String.class;
        }
        };
        
        dm.setDataVector(tableData,header);        
        JTable result = new JTable(dm)        
        {
            public boolean isCellEditable(int x, int y)
            {
                return false;
            }    
        };                
              
        if (attributeNamesToDisplay!=null)
        {
            result.setRowHeight( result.getRowHeight() * 2);
        }
                
        result.setDefaultRenderer(String.class, new MultiLineCellRenderer(colsToHighlight));
        result.addMouseListener(this);
        result.addKeyListener(this);
        
        result.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        result.setCellSelectionEnabled(true);
        result.setSelectionBackground(Color.ORANGE);
        
        return result;
    }
       
    public final void setMarkableChart(MarkableChart _chart)
    {
        chart = _chart;
    }
    
    private final ArrayList getAttributeNames(String para)
    {
        ArrayList result = null;
        StringTokenizer toki = new StringTokenizer(para,",");
        while(toki.hasMoreTokens())
        {
            if (result == null)
            {
                result = new ArrayList();
            }
            result.add(toki.nextToken().trim());
        }
        return result;
    }
    
    /** This method is used to recursively execute a separate command, as tokenized from rawQueryString.
        The return value of each iteration is a MMAX2QueryResultList. */
    private final MMAX2QueryResultList executeRecursively(String command)
    {        
        getGlassPane().addMouseListener( new MouseAdapter() {});
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);

        MMAX2QueryResultList result = null;
        command = command.trim();
        ArrayList paras = null;                
                
        // New May 15th: Do not sort all results, in particular, do not dort result of getAssignment
        boolean sort = false;
        
        if (command.startsWith("["))
        {
            int paraStart = command.indexOf("[");
            int paraEnd = command.indexOf("]");
            if (paraStart != -1 && paraEnd != -1)
            {
                paras = getAttributeNames(command.substring(paraStart+1,paraEnd));
                command = command.substring(command.indexOf("]")+1).trim();
            }            
        }        
        if (command.startsWith("."))
        {
            // The command is the shorthand for the default markable level
            // So just return all Markables on that level as a list of MMAX2QueryResultTuples
            //result = toMMAX2QueryResultTuple(chart.getMarkableLevelByName(currentDefaultLevelName, false).getMarkables());
            if (currentDefaultLevelName.equals(""))
            {
                currentDefaultLevelName = chart.getLevelNameByOrderPosition(0);
            }
            result = toMMAX2QueryResultList(chart.getMarkableLevelByName(currentDefaultLevelName, false).getMarkables());
        }
        else if (command.startsWith("$"))
        {            
            // The command is a variable name
            // So just retrieve and return mapped ArrayList
            // If nothing is mapped, null will be returned !
            result = getAssignment(command);
            // New May 15th
            // DO NOT sort !!
        }        
        else if (command.toLowerCase().startsWith("unique"))
        {
            command = command.substring(6).trim();
            result = removeDuplicates(executeRecursively(command));
            // Sort afterwards (Is this really necessary)
            sort = true;
        }
        else if (command.toLowerCase().startsWith("invert"))
        {
            command = command.substring(6).trim();            
            result = invert(executeRecursively(command));
            sort =true;
        }        
        else if (command.equalsIgnoreCase("basedata"))
        {
            // The command is the name of the basedata level
            // So just return all base data elements as list of MMAX2QueryResultTuples           
            //result = toMMAX2QueryResultList(chart.getMarkableLevelByName(command, false).getMarkables());
            result = toMMAX2QueryResultList((ArrayList)java.util.Arrays.asList(chart.getCurrentDiscourse().getDiscourseElements()));
            sort = true;
        }        
        else if (chart.getMarkableLevelByName(command, false)!=null)
        {
            // The command is the name of a markable level
            // So just return all markables on that level as list of MMAX2QueryResultTuples
            //result = toMMAX2QueryResultTuple(chart.getMarkableLevelByName(command, false).getMarkables());
            result = toMMAX2QueryResultList(chart.getMarkableLevelByName(command, false).getMarkables());
            sort = true;
        }
        else if ((result = executeRelationQuery(command))!=null)
        {
            // The command is a RelationQuery
            // Result is already assigned now
            sort = true;
        }
        else if ((result = executeSetQuery(command))!=null)
        {
            // The command is a SetQuery
            // Result is already assigned now
            sort = true;
        }        
        else
        {
            // Interpret command as complex query
            result = toMMAX2QueryResultList(executeMMAX2QueryTree(command));
            sort = true;
        }        
        if (paras != null)
        {
            result.setAttributeNamesToDisplay(paras);
        }            
        
        if (result != null && sort)
        {
            // New April 1st, 2005: Always sort results of all queries
            return sort(result,result.getIndexToUse());
        }
        else
        {
            return result;
        }        
    }
    
    private final MMAX2QueryResultList invert(MMAX2QueryResultList inlist)
    {
        ArrayList backgroundList = null;
        // Invert works on single columns only        
        // Works on top level only, OK
        if (inlist.isIndexSetByUser() || inlist.getWidth() ==1)
        {
            // There either is only one column, or a single column has been selected by the user
            // Get reference list
            MMAX2QueryResultTupleElement initial = inlist.getElementAtIndexFromColumnToUse(0);
            if (initial.isMarkable())
            {
                // from first markable in list
                backgroundList = toResultTupleElementList(initial.getMarkable().getMarkableLevel().getMarkables(new DiscourseOrderMarkableComparator()));
            }
            else
            {
                // Reference list is basedata
                backgroundList = toResultTupleElementList((ArrayList)Arrays.asList(chart.getCurrentDiscourse().getDiscourseElements()));
            }
            // Now, backgroundlist contains either markables or discourse elements
            // Iterate over list to be inverted
            for (int z=inlist.size()-1;z>=0;z--)
            {
                backgroundList.remove(inlist.getElementAtIndexFromColumnToUse(z));
            }                
            return toMMAX2QueryResultList(backgroundList);
        }
        else
        {
            System.err.println("Error: Invert can be applied to single columns only!");
            return new MMAX2QueryResultList();
        }        
    }

    private final MMAX2QueryResultList subtract(MMAX2QueryResultList inlist, MMAX2QueryResultList backgroundList)
    {        
        if (backgroundList.size()==0)
        {
            System.err.println("Empty argument list in subtract!");
            return inlist;
        }
        inlist = new MMAX2QueryResultList(inlist,inlist.getIndexToUse());
        backgroundList = new MMAX2QueryResultList(backgroundList,backgroundList.getIndexToUse());
        // Invert works on single columns only        
        if ((inlist.isIndexSetByUser() || inlist.getWidth() ==1) && (backgroundList.isIndexSetByUser() || backgroundList.getWidth() ==1))
        {
            // There either is only one column in each list, or a single column has been selected by the user
            // Iterate over list to be subtracted from
            for (int z=backgroundList.size()-1;z>=0;z--)
            {
                inlist.remove(backgroundList.getTupleAtIndex(z));
            }                
        }
        else
        {
            System.err.println("Error: Subtract can be applied to single columns only!");
        }   
        return inlist;
    }
    
    private final MMAX2QueryResultList merge(MMAX2QueryResultList list1, MMAX2QueryResultList list2)
    {
        MMAX2QueryResultList tempresult = new MMAX2QueryResultList();        
        tempresult.addAll(list1);
        tempresult.addAll(list2);
        MMAX2QueryResultList result = null;
        // The following will also sort the list, so no prior sorting necessary
        result = removeDuplicates(tempresult);        
        return result;
    }
    
    
    private final MMAX2QueryResultList removeDuplicates(MMAX2QueryResultList inlist)
    {                
        MMAX2QueryResultList sortedList = null;
        if (inlist.isIndexSetByUser())
        {
            // Make sure the list is correctly sorted
            sortedList = sort(inlist,inlist.getIndexToUse());
            sortedList.setIndexToUse(inlist.getIndexToUse());
        }
        else
        {
            sortedList = sort(inlist,0);
        }
        // New April 25th: Make sure reduced list has original command
        sortedList.setCommand(inlist.getCommand());
        
        String lastElement = "";
        String currentElement = "";
        if (sortedList.isIndexSetByUser())
        {
            // Index is set by user, so consider only field at index
            // Iterate over list backwards
            for (int z=sortedList.size()-1;z>=0;z--)
            {
                if (sortedList.getElementAtIndexFromColumnToUse(z).getID().equals(lastElement))
                {
                    sortedList.remove(z);    
                    continue;
                }
                lastElement = sortedList.getElementAtIndexFromColumnToUse(z).getID();               
            }            
        }
        else
        {
            // No index set by user, so consider all fields
            // Iterate over list backwards
            for (int z=sortedList.size()-1;z>=0;z--)
            {
                currentElement = "";
                // Iterate over width of tuple
                //int y=sortedList.getWidth();
                for (int b=0;b<sortedList.getWidth();b++)
                {
                    // Collect all IDs in a string
                    currentElement = currentElement+sortedList.getElementAtIndexFromColumn(z, b).getID();
                }
                if (currentElement.equals(lastElement))
                {
                    // Remove if duplicate
                    sortedList.remove(z);    
                    continue;
                }
                lastElement = currentElement;
            }                        
        }
        return sortedList;
    }

    public final MMAX2QueryResultList executeQuery(String rawQueryString, boolean interactive)
    {        
        if (rawQueryString.endsWith(";")==false)
        {
            rawQueryString = rawQueryString+";";
        }
        
        // Define var to accept overall result
        MMAX2QueryResultList result = null;
        SimplifiedMMAXQLConverter convi = new SimplifiedMMAXQLConverter(chart,interactive);             
        
        ArrayList allCommands = new ArrayList();
        String currentCommand = "";
                        
        String tempRestCommand = rawQueryString;
        
        // Iterate over all tokens
        while(true)
        {
            // Get next substring to next pos of tl-semicolon
            int end=getNextCommandSeparator(tempRestCommand,";");            
            // Get next command, trimmed
            currentCommand = tempRestCommand.substring(0,end).trim();
            // Check whether current command starts with a s!
            if (currentCommand.startsWith("s! "))
            {
                // The current command starts with the special char signalling that
                // simplified MMAXQL is to be used
                // This means that no top level command has been supplied
                
                // New: April 29th: If final part of command is -> $r, use assignment 
                // as default top level command, else use 'display'
                String lastPart = "";
                String secondLastPart = "";
                // Get pos of last space, if any
                int lastSep = currentCommand.lastIndexOf(" ");
                boolean found = false;
                if (lastSep!=-1)
                {
                    // Look at last element of entire query
                    lastPart = currentCommand.substring(lastSep).trim();                    
                    if (lastPart.startsWith("$"))
                    {
                        // The last part is a variable
                        // Get part before var name, trimmed
                        String temp = currentCommand.substring(0,lastSep).trim();
                        // Get pos of second but last space, if any 
                        int secondLastSep = temp.lastIndexOf(" ");
                        if (secondLastSep!=-1)
                        {
                            //secondLastPart = currentCommand.substring(secondLastSep,lastSep).trim();
                            secondLastPart = temp.substring(secondLastSep).trim();
                            // Look at secondButLast part
                            if (secondLastPart.equals("->"))
                            {
                                // This part is an assignment operator,
                                // so use assignment as top level command
                                currentCommand = "let "+lastPart+" = "+ currentCommand.substring(0,secondLastSep);
                                found = true;
                            }
                        }
                    }
                }
                // Use default command 'display'
                if (!found)
                {
                    // else use display as default command
                    currentCommand = "display "+currentCommand;
                }                            
            }
            // Now, current command is certain to have a tl command
            String topLevelVariable = "";
            int temp = (" "+currentCommand).indexOf(" s! ");
            if (temp != -1)
            {
                // The current command uses simplified MMAXQL
                // Cut off 
                String toBeConverted = currentCommand.substring(temp+3).trim();
                String topLevelCommand = currentCommand.substring(0,temp);

                // Convert to actual MMAXQL
                String converted = convi.convertFromSimplifiedMMAXQL(toBeConverted);
                if (converted.equals(""))
                {
                    return null;
                }
                // converted now starts with a string of the name of the final variable,
                // to which the tl command is to be applied
                topLevelVariable = converted.substring(0,converted.indexOf(":"));
                converted = converted.substring(converted.indexOf(":")+1);
                currentCommand = converted+topLevelCommand+" $"+topLevelVariable+";";                                                
                String tempCommand = currentCommand;
                allCommands.add("clearsystemvars");
                while (true)
                {
                    int q = getNextCommandSeparator(tempCommand,";");
                    allCommands.add(tempCommand.substring(0,q));
                    tempCommand = tempCommand.substring(q+1).trim();
                    if (tempCommand.equals(""))
                    {
                        break;
                    }                    
                }
                allCommands.add("clearsystemvars");                
            }
            else
            {
                // No simplified command to expand, so add org. command to list
                allCommands.add(currentCommand);   
            }
            tempRestCommand = tempRestCommand.substring(end+1).trim();
            if (tempRestCommand.equals(""))
            {
                break;
            }
        }                 
        
        allCommands = convi.removeRedundantVariableAssigments(allCommands);       
        
        String restCommand = "";
        // Now iterate over entire list of commands. Individual simplified commands have already been expanded,
        // so allCommands containss only plain MMAXQL statements
        for (int z=0;z<allCommands.size();z++)
        {
            // Get current command
            currentCommand = ((String) allCommands.get(z)).trim();
            if (currentCommand.equals(""))
            {
                continue;
            }
            
            if (currentCommand.startsWith("%"))
            {
                // Skip comment
                continue;
            }
            
            // Check for Assignment
            if (currentCommand.length()>5 && currentCommand.substring(0,5).equalsIgnoreCase("let $"))
            {
                // The current command is an assignment
                // Extract name of target variable
                // Find first '=' (if any)
                int varEnd = currentCommand.indexOf("=");                
                if (varEnd==-1)
                {
                    System.err.println("Error: '=' expected");
                    return null;
                }                
                // Extract var name
                String varName = currentCommand.substring(4,varEnd).trim();
                // Var name MUST NOT contain a col spec!!
                // Extract command producing the assigned data
                restCommand = currentCommand.substring(varEnd+1).trim();
                
                // execute rest of query recursively and assign result to var
                // Note: restCommand may contain a col spec (NO!)
                // Only in case of $e = $w.1, which are removed automatically.
                // No other col specs can occur, since sMMAXQL doesnot create them
                // Todo: Maybe allow queries like
                // (/coref in /sentence).1 before:10- /coref,
                // which are disallowed currently
                MMAX2QueryResultList assignedList = executeRecursively(restCommand);                
                if (assignedList != null)
                {
                    // The execution of restcommand did produce some result
                    // New: Set orgIndices upon original creation
                    // but only for non-system vars
                    if (isSystemVariable(varName)==false)
                    {
                        for (int u=0;u<assignedList.size();u++)
                        {
                            ((MMAX2QueryResultTuple)assignedList.get(u)).setOriginalIndex(u);
                        }
                    }
                    
                    // Create assignment of list to varName
                    // var name cannot contain a col spec here
                    setAssignment(varName,assignedList);
                }                    
                
                // Display result in result table, if we are in debugmode, 
                // unless we are also in console mode
                if (debugMode && !consoleMode && isSystemVariable(varName)==false)
                {
                    // This will also be executed if result is null
                    setQueryResult(assignedList);
                }
                else
                {
                    // If no result is displayed, set cursor back to normal here
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                    getGlassPane().setVisible(false);                       
                }
                // An assignment returns as result the assigned value
                result = assignedList;
            }            
            else if (currentCommand.length()>8 && currentCommand.substring(0,8).equalsIgnoreCase("display "))
            {
                // Cut off leading tl command
                restCommand = currentCommand.substring(8).trim();
                // Execute command
                MMAX2QueryResultList outputList = executeRecursively(restCommand);               
                if (!consoleMode)
                {
                    // Send to output even if null
                    setQueryResult(outputList);
                }
                else
                {
                    // NEW: output to console directly if in consoleMode
                    toConsole(outputList,currentCommand.substring(8).trim());                    
                }
                result = outputList;
            }   
            else if (currentCommand.length()>21 && currentCommand.substring(0,21).equalsIgnoreCase("create_no_duplicates "))
            {
                restCommand = currentCommand.substring(21).trim();
                getGlassPane().addMouseListener( new MouseAdapter() {});
                getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                getGlassPane().setVisible(true);                
                executeCreateCommand(restCommand,false);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                getGlassPane().setVisible(false);
            }   
            else if (currentCommand.length()>23 && currentCommand.substring(0,23).equalsIgnoreCase("create_with_duplicates "))
            {
                restCommand = currentCommand.substring(23).trim();
                getGlassPane().addMouseListener( new MouseAdapter() {});
                getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                getGlassPane().setVisible(true);                
                executeCreateCommand(restCommand,true);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                getGlassPane().setVisible(false);
                
            }               
            else if (currentCommand.length()>11 && currentCommand.substring(0,11).equalsIgnoreCase("set_values "))
            {
                restCommand = currentCommand.substring(11).trim();
                getGlassPane().addMouseListener( new MouseAdapter() {});
                getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                getGlassPane().setVisible(true);                
                executeSetValuesCommand(restCommand);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                getGlassPane().setVisible(false);
                
            }                           
            else if (currentCommand.length()>9 && currentCommand.substring(0,9).equalsIgnoreCase("uncreate "))
            {
                restCommand = currentCommand.substring(9).trim();
                getGlassPane().addMouseListener( new MouseAdapter() {});
                getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                getGlassPane().setVisible(true);                
                executeUncreateCommand(restCommand);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                getGlassPane().setVisible(false);                
            }   
            
            else if (currentCommand.length()>11 && currentCommand.substring(0,11).equalsIgnoreCase("statistics "))
            {
                restCommand = currentCommand.substring(11).trim();
                String statistic = createBasicStatistics(executeRecursively(restCommand));
                if (!consoleMode)
                {
                    statisticsOutputArea.setText(statistic);
                    resultPane.setSelectedIndex(1);
                }
                else
                {
                    toConsole(statistic);
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
                getGlassPane().setVisible(false);                                
            }
            else if (currentCommand.equalsIgnoreCase("debug"))
            {
                debugMode = true;
            }
            else if (currentCommand.equalsIgnoreCase("nodebug"))
            {
                debugMode = false;
            }        
            else if (currentCommand.equalsIgnoreCase("clearsystemvars"))
            {
                clearSystemVariables();
            }                    
            else
            {
                System.err.println("No valid command!");
            }
        }
        return result;
    }
    
    /** This method accepts an ArrayList of Markables or MMAX2DiscourseElements 
        or MMAX2QueryResultTupleElements and returns an equal-sized MMAX2QueryResultList 
        of width = 1. */
    private final MMAX2QueryResultList toMMAX2QueryResultList(ArrayList elements)
    {
        return new MMAX2QueryResultList((ArrayList)elements);
    }
    
    private final ArrayList toResultTupleElementList(ArrayList elements)
    {
        ArrayList result = new ArrayList();
        for (int z=0;z<elements.size();z++)
        {
            result.add(new MMAX2QueryResultTupleElement(elements.get(z)));
        }
        return result;
    }
    
    /** This method receives a bracketed String of MMAX2QueryResultList-producing expressions and returns 
        a two-element ArrayList which contains one MMAX2QueryResultList object for each of those. */
    private final ArrayList getParameterLists(String paraString)
    {
        int inRoundBrackets = 0;
        int inCurlyBrackets = 0;
        ArrayList result = new ArrayList();
        if (paraString.startsWith("(")==false || paraString.endsWith(")")==false)
        {
            System.err.println("Illegal parameter String!");            
            return result;
        }
        // Cut off leading and trailing brackets
        paraString = paraString.substring(1,paraString.length()-1).trim();
        
        String currentPara = "";
        int lastParaEnd = 0;
        
        // Iterate over parameter string
        // String tokenizer does not work here because tokens need not be atomic, but can contain separators
        
        for (int z=0;z<paraString.length();z++)
        {            
            if (paraString.substring(z,z+1).equals("(")) inRoundBrackets++;
            if (paraString.substring(z,z+1).equals(")")) inRoundBrackets--;
            if (paraString.substring(z,z+1).equals("{")) inCurlyBrackets++;
            if (paraString.substring(z,z+1).equals("}")) inCurlyBrackets--;
                            
            if (paraString.substring(z,z+1).equals(",") && 
                inRoundBrackets == 0 && 
                inCurlyBrackets == 0)
            {
                // A top-level separator was found
                // Execute the identified MMAX2QueryResultList-producing expression,
                // and add its result to list of results
                result.add(executeRecursively(currentPara));                
                ((MMAX2QueryResultList)result.get(result.size()-1)).setCommand(currentPara.trim());                
                currentPara = "";
                lastParaEnd=z+1;
                continue;
            }
            currentPara = currentPara+paraString.substring(z,z+1);
        }     

        // Execute the last MMAX2QueryResultList-producing expression,
        // and add its result to list of results
        result.add(executeRecursively(paraString.substring(lastParaEnd)));        
        ((MMAX2QueryResultList)result.get(result.size()-1)).setCommand(paraString.substring(lastParaEnd).trim());
        
        // Return list of results 
        return result;
    }

    /** This method receives a bracketed String of MMAX2QueryResultList-producing expressions and returns 
        an ArrayList which contains one MMAX2QueryResultList object for each of those. */
    private final ArrayList getParameterListsAndString(String paraString)
    {
        int inRoundBrackets = 0;
        int inCurlyBrackets = 0;
        ArrayList result = new ArrayList();
        if (paraString.startsWith("(")==false || paraString.endsWith(")")==false)
        {
            System.err.println("Illegal parameter String!");            
            return result;
        }
        // Cut off leading and trailing brackets
        paraString = paraString.substring(1,paraString.length()-1).trim();
        
        String currentPara = "";
        int lastParaEnd = 0;
        
        // Iterate over parameter string
        // String tokenizer does not work here because tokens need not be atomic, but can contain separators
        
        for (int z=0;z<paraString.length();z++)
        {            
            if (paraString.substring(z,z+1).equals("(")) inRoundBrackets++;
            if (paraString.substring(z,z+1).equals(")")) inRoundBrackets--;
            if (paraString.substring(z,z+1).equals("{")) inCurlyBrackets++;
            if (paraString.substring(z,z+1).equals("}")) inCurlyBrackets--;
                            
            if (paraString.substring(z,z+1).equals(",") && 
                inRoundBrackets == 0 && 
                inCurlyBrackets == 0)
            {
                // A top-level separator was found
                // Execute the identified MMAX2QueryResultList-producing expression,
                // and add its result to list of results
                result.add(executeRecursively(currentPara));
                currentPara = "";
                lastParaEnd=z+1;
                continue;
            }
            currentPara = currentPara+paraString.substring(z,z+1);
        }     

        // Execute the last MMAX2QueryResultList-producing expression,
        // and add its result to list of results
        result.add(paraString.substring(lastParaEnd).trim());
        // Return list of results 
        return result;
    }
    

    public final static int getNextCommandSeparator(String restCommand, String sep)
    {
        int inRound=0;
        int inSquare=0;
        int inCurly=0;
        for (int z=0;z<restCommand.length();z++)
        {
            String currentChar = restCommand.substring(z,z+1);
            if (currentChar.equals("("))
            {
                inRound++;
                continue;
            }
            if (currentChar.equals("["))
            {
                inSquare++;
                continue;
            }
            if (currentChar.equals("{"))
            {
                inCurly++;
                continue;
            }
            if (currentChar.equals(")"))
            {
                inRound--;
                continue;
            }
            if (currentChar.equals("]"))
            {
                inSquare--;
                continue;
            }
            if (currentChar.equals("}"))
            {
                inCurly--;
                continue;
            }
            if (currentChar.equals(sep))
            {
                if (inCurly==0 && inSquare==0 && inRound==0)
                {
                    return z;
                }
                else
                {
                	
                }
            }
        }
        return restCommand.length();
    }

    public final static int getFollowingMatchingBracket(String restCommand, String sep, int startAfter)
    {
        int inRound=0;
        int inSquare=0;
        int inCurly=0;
        for (int z=startAfter+1;z<restCommand.length();z++)
        {
            String currentChar = restCommand.substring(z,z+1);
            
            if (currentChar.equals("("))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                                        
                inRound++;
            }
            if (currentChar.equals("["))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }
                inSquare++;
            }                            
            if (currentChar.equals("{"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inCurly++;
            }
            if (currentChar.equals(")"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inRound--;                
            }
            if (currentChar.equals("]"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inSquare--;                
            }
            if (currentChar.equals("}"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inCurly--;
            }
        }
        return restCommand.length();
    }
    

    public final static int getPreceedingMatchingBracket(String restCommand, String sep, int startBefore)
    {
        int inRound=0;
        int inSquare=0;
        int inCurly=0;
        for (int z=startBefore-1;z>=0;z--)
        {
            String currentChar = restCommand.substring(z,z+1);
            
            if (currentChar.equals("("))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                                        
                inRound++;
            }
            if (currentChar.equals("["))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }
                inSquare++;
            }                            
            if (currentChar.equals("{"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inCurly++;
            }
            if (currentChar.equals(")"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inRound--;                
            }
            if (currentChar.equals("]"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inSquare--;                
            }
            if (currentChar.equals("}"))
            {
                if (currentChar.equals(sep))
                {
                    if (inCurly==0 && inSquare==0 && inRound==0)
                    {
                        return z;
                    }
                }                            
                inCurly--;
            }
        }
        return restCommand.length();
    }
    
    
    /** This method parses the comma-separated parameters in the supplied string and returns an ArrayList 
        containing either strings (for string parameters) or MMAX2QueryResultLists (for expressions evaluating
        to those). */
    private final ArrayList getParameters(String paraString)
    {
        int inRoundBrackets = 0;
        int inCurlyBrackets = 0;
        int inSingleQuotes = 0;
        
        ArrayList result = new ArrayList();
        MMAX2QueryResultList list = null;
        if (paraString.startsWith("(")==false || paraString.endsWith(")")==false)
        {
            System.err.println("Illegal parameter String!");            
            return result;
        }
        
        // Cut off leading and trailing parentheses
        paraString = paraString.substring(1,paraString.length()-1).trim();
        
        String currentPara = "";
        int lastParaEnd = 0;
        
        // Iterate over parameter string
        // String tokenizer does not work here because tokens need not be atomic, but can contain separators        
        for (int z=0;z<paraString.length();z++)
        {            
            if (paraString.substring(z,z+1).equals("(")) inRoundBrackets++;
            if (paraString.substring(z,z+1).equals(")")) inRoundBrackets--;
            if (paraString.substring(z,z+1).equals("{")) inCurlyBrackets++;
            if (paraString.substring(z,z+1).equals("}")) inCurlyBrackets--;
            if (paraString.substring(z,z+1).equals("'")) 
            {
                if (inSingleQuotes==0)
                {
                    inSingleQuotes--;
                }
                else
                {
                    inSingleQuotes++;
                }
            }
                            
            if (paraString.substring(z,z+1).equals(",") && 
                inRoundBrackets == 0 && 
                inCurlyBrackets == 0 &&
                inSingleQuotes == 0)
            {
                // A top-level separator was found
                // Now, currentPara is either a string, or an evaluating expression
                currentPara = currentPara.trim();
                if (currentPara.startsWith("'") && currentPara.endsWith("'"))
                {
                    result.add(currentPara.substring(1,currentPara.length()-1).trim());
                }
                else
                {
                    // Execute the identified MMAX2QueryResultList-producing expression,
                    // and add its result to list of results
                    list = executeRecursively(currentPara);
                    // Store name under which para was executed (may be a var name)
                    if (list.getCommand().equals(""))
                    {
                        list.setCommand(currentPara);
                    }
                    result.add(list);    
                    list=null;
                }                
                currentPara = "";
                lastParaEnd=z+1;
                continue;
            }
            currentPara = currentPara+paraString.substring(z,z+1);
        }     
        
        currentPara = paraString.substring(lastParaEnd).trim();
        if (currentPara.startsWith("'") && currentPara.endsWith("'"))
        {
            result.add(currentPara.substring(1,currentPara.length()-1).trim());
        }
        else
        {
            // Execute the identified MMAX2QueryResultList-producing expression,
            // and add its result to list of results    
            list = executeRecursively(currentPara);
            // Store name under which para was executed (may be a var name)
            if (list.getCommand().equals(""))
            {
                list.setCommand(currentPara);
            }
            result.add(list);    
            list=null;
        }                        
        // Return list of results 
        return result;        
    }
            
    /** This method returns a MMAX2QueryResultList. It also expects MMAX2QueryResultLists.
        For matching, the column specified in the parameter list's indexToUse file is used.
        This returns null if command could not be interpreted as a set query. */
    private final MMAX2QueryResultList executeSetQuery(String command)
    {        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
                
        String restCommand="";
        // Find first ( in command, which is supposed to be the one around the parameter expressions
        int paraStart = command.indexOf("(");
        if (paraStart == -1)
        {
            System.err.println("Error: Expected '('!");
            return null;
        }        
        // Extract command
        String setCommand = command.substring(0,paraStart).trim();
        // and set to lower case 
                
        // Flag for negated query. Negation of relation queries is non-trivial!!
        boolean negated = false;
    
        // Check for signs of negation first
        if (setCommand.startsWith("!"))
        {
            negated = true;
            setCommand = setCommand.substring(1).trim();           
        }
        else if (setCommand.startsWith("not"))
        {
            negated = true;
            setCommand = setCommand.substring(3).trim();
        }                    
        
        // default:joiner
        boolean useAsFilter = false;
        // Now check for filter
        if (setCommand.startsWith("#"))
        {
            useAsFilter = true;
            setCommand = setCommand.substring(1).trim();
        }
                        
        
        if (" matching_markable_sets all_markable_sets next_peer peer ".indexOf(" "+setCommand+" ")==-1)
        {
            return null;
        }

        // Get matching top level bracket, i.e. the one after the second parameter expression
        int paraEnd = getMatchingBracketPosition(command,paraStart);
        if (paraEnd == -1)
        {
            System.err.println("Error: Expected ')'!");
            return null;            
        }

        // Cut off query command
        restCommand = command.substring(paraStart,paraEnd).trim();
        
        // Get all parameters as supplied with the command
        ArrayList temp =  getParameters(restCommand.trim());
        
        // Define col spec
        String spec = "";
        
        if (command.length() > paraEnd && command.substring(paraEnd,paraEnd+1).equals("."))
        {
            // The current set query has a column specifier
            // Extract it
            spec = command.substring(paraEnd+1);
        }

        if (!negated)
        {        
            if (setCommand.equals("all_markable_sets"))
            {
                result = executeSetQuery_AllMarkableSets(temp);
            }                        
            else if (setCommand.equals("matching_markable_sets"))
            {
                result = executeSetQuery_MatchingMarkableSets(temp);
            }                                    
            else if (setCommand.equals("next_peer"))
            {                
                result = executeSetQuery_NextPeer(temp,useAsFilter);
            }             
                        
            else if (setCommand.equals("peer"))
            {
                result = executeSetQuery_AnyPeer(temp, useAsFilter);                
            }                 
            else
            {
                // No set query
                result = null;
            }
        }// negated
        else
        {            
            // Put negated set query code here
        }
        
        if (result != null)
        {
            int index = -1;
            try
            {
                index = Integer.parseInt(spec);
            }
            catch (java.lang.NumberFormatException ex)
            {
                
            }
            
            if (index != -1)
            {
                // The spec could be parsed in a number
                if (index >= result.getWidth())
                {
                    JOptionPane.showMessageDialog(null,"Cannot access index "+index+" in result list! Result list width is "+result.getWidth()+", using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    result.setIndexToUse(index);
                    // This is only called if a col spec was used earlier, OK
                    result.setIndexSetByUser();
                }
            }            
            else
            {
                // spec could not be parsed as a number
                if (spec.equals("")==false)
                {
                    // Try to get numerical index for text spec
                    index = result.getColumnIndexByColumnName(spec);
                    if (index != -1)
                    {
                        result.setIndexToUse(index);
                        // This is only called if a col spec was used earlier, OK
                        result.setIndexSetByUser();                    
                    }
                    else
                    {                       
                        JOptionPane.showMessageDialog(null,"Column specifier '"+spec+"' not found or not unique, using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }                
        return result;
    }

    private final MMAX2QueryResultList executeSetQuery_MatchingMarkableSets(ArrayList parameters)
    {
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        String firstLevelName="";
        String secondLevelName="";
        String attributeName="";
        if (parameters.size()!=3)
        {
            System.err.println("Query Syntax Error. Use: matching_markable_sets (String 'levelName 1', String 'levelName 2', String 'attribute')");
            return result;            
        }
        
        if (parameters.get(0) instanceof String)
        {
            firstLevelName = (String) parameters.get(0);
        }
        else
        {
            System.err.println("Query Syntax Error. Use: matching_markable_sets (String 'levelName 1', String 'levelName 2', String 'attribute')");
            return result;
        }                

        if (parameters.get(1) instanceof String)
        {
            secondLevelName = (String) parameters.get(1);
        }
        else
        {
            System.err.println("Query Syntax Error. Use: matching_markable_sets (String 'levelName 1', String 'levelName 2', String 'attribute')");
            return result;
        }                
        
        if (parameters.get(2) instanceof String)
        {
            attributeName = (String) parameters.get(2);
        }
        else
        {
            System.err.println("Query Syntax Error. Use: matching_markable_sets (String 'levelName 1', String 'levelName 2', String 'attribute')");
            return result;                
        }
        
        MarkableLevel level1 = chart.getMarkableLevelByName(firstLevelName,true);
        MMAX2Attribute setAttribute1 = level1.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        MarkableRelation relation1 = setAttribute1.getMarkableRelation();
        ArrayList anno1Sets = new ArrayList(Arrays.asList(relation1.getMarkableSets(true)));
        ArrayList anno1SetStrings = new ArrayList();
        for (int b=0;b<anno1Sets.size();b++)
        {
            anno1SetStrings.add(((MarkableSet)anno1Sets.get(b)).getSetOfStrings());
        }        
        
        MarkableLevel level2 = chart.getMarkableLevelByName(secondLevelName,true);
        MMAX2Attribute setAttribute2 = level2.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        MarkableRelation relation2 = setAttribute2.getMarkableRelation();
        ArrayList anno2Sets = new ArrayList(Arrays.asList(relation2.getMarkableSets(true)));
        ArrayList anno2SetStrings = new ArrayList();
        for (int b=0;b<anno2Sets.size();b++)
        {
            anno2SetStrings.add(((MarkableSet)anno2Sets.get(b)).getSetOfStrings());
        }
                
        ArrayList list1Indices = new ArrayList();
        ArrayList list2Indices = new ArrayList();
        // Iterate over all sets of strings representing relation 1
        for (int b=0;b<anno1SetStrings.size();b++)
        {
            int temp = anno2SetStrings.indexOf((HashSet)anno1SetStrings.get(b));
            if (temp != -1)
            {
                list1Indices.add(new Integer(b));
                list2Indices.add(new Integer(temp));
            }
        }
        
        for (int n=0;n<list1Indices.size();n++)
        {
            int index = ((Integer)list1Indices.get(n)).intValue();
            MarkableSet set1 = (MarkableSet)anno1Sets.get(index);
            ArrayList toBuildTupleFrom = new ArrayList();
            // Get ordered list of all markables in the retrieved set
            ArrayList tmp = (ArrayList)java.util.Arrays.asList(set1.getOrderedMarkables());
            // Iterate over all elements in current set
            for (int b=0;b<tmp.size();b++)
            {
                // Add every markable in set
                toBuildTupleFrom.add(new MMAX2QueryResultTupleElement((Markable)tmp.get(b)));
            }
            result.add(new MMAX2QueryResultTuple(toBuildTupleFrom));
            
            index = ((Integer)list2Indices.get(n)).intValue();
            MarkableSet set2 = (MarkableSet)anno2Sets.get(index);
            toBuildTupleFrom = new ArrayList();
            // Get ordered list of all markables in the retrieved set
            tmp = (ArrayList)java.util.Arrays.asList(set2.getOrderedMarkables());
            // Iterate over all elements in current set
            for (int b=0;b<tmp.size();b++)
            {
                // Add every markable in set
                toBuildTupleFrom.add(new MMAX2QueryResultTupleElement((Markable)tmp.get(b)));
            }
            result.add(new MMAX2QueryResultTuple(toBuildTupleFrom));            
        }
        return result;
    }
    
    private final MMAX2QueryResultList executeSetQuery_AllMarkableSets(ArrayList parameters)
    {
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        // The requested relation is 'all_markable_sets'
        // This requires two parameters:
        // String levelName
        // String attributeName
        // This produces one tuple for each set of name name on level level, with 
        // the members of the set at 0 to n
        String levelName="";
        ArrayList inputList = null;
        String attributeName="";
        if (parameters.get(0) instanceof String)
        {
            levelName = (String) parameters.get(0);
        }
        else
        {
            System.err.println("Query Syntax Error. Use: all_markable_sets (String 'levelName', String 'attribute')");
            return result;
        }                

        if (parameters.get(1) instanceof String)
        {
            attributeName = (String) parameters.get(1);
        }
        else
        {
            System.err.println("Query Syntax Error. Use: all_markable_sets (String 'levelName', String 'attribute')");
            return result;                
        }

        MarkableSet set = null;
        MarkableLevel level = null;

        // Get level
        level = chart.getMarkableLevelByName(levelName,true);
        if (level == null)
        {
            return result;
        }
        // Get markables on level, sorted in discourse order, shorter before longer
        ArrayList allMarkables = level.getMarkables(new DiscourseOrderMarkableComparator());                

        MMAX2Attribute attributeElement = level.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        if (attributeElement == null)
        {
            System.err.println("No such MARKABLE_SET attribute: "+attributeName);
            return result;
        }
        // Get pertaining relation object
        Markable currentMarkable = null;
        HashSet finished = new HashSet();
        // Iterate over all markables on level level
        for (int a=0;a<allMarkables.size();a++)
        {                
            // Get current markable
            currentMarkable = (Markable)allMarkables.get(a);

            set = null;
            // Get all Relations for this Markable (i.e. one for each relation attribute)
            MarkableRelation[] relations = currentMarkable.getMarkableLevel().getActiveMarkableSetRelationsForMarkable(currentMarkable);
            // Iterate over all Relations for this Markable
            for (int z=0;z<relations.length;z++)
            {
                // Check if the current Relation is the one accessed in this query
                if (relations[z].getAttributeName().equalsIgnoreCase(attributeName))
                {
                    // Try to get the set that this Markable is a member of
                    set = relations[z].getMarkableSetWithAttributeValue(currentMarkable.getAttributeValue(attributeName));
                    // One  markable can only be a member in one set of a given name
                    // So if we found sth. here, we are done searching
                    break;
                }
            }
            String currentVal = "";
            if (set != null)
            {
                // If set != null, current markable is a member of a set belonging to the required attribute name
                // Get name of the set
                currentVal = set.getAttributeValue();
                if (finished.contains(currentVal))
                {
                    // Add each set only ONCE!
                    continue;
                }
                finished.add(currentVal);
                // A set of the required name could be retrieved for the current markable
                // Create empty list to accept result to build tuple from
                ArrayList toBuildTupleFrom = new ArrayList();
                // Get ordered list of all markables in the retrieved set
                ArrayList tmp = (ArrayList)java.util.Arrays.asList(set.getOrderedMarkables());
                // Iterate over all elements in current set
                for (int b=0;b<tmp.size();b++)
                {
                    // Add every markable in set
                    toBuildTupleFrom.add(new MMAX2QueryResultTupleElement((Markable)tmp.get(b)));
                }
                result.add(new MMAX2QueryResultTuple(toBuildTupleFrom));
            }
        } 
        result.setCommand("all_markable_sets");
        return result;
    }
    
    private final MMAX2QueryResultList executeSetQuery_AnyPeer(ArrayList parameters, boolean useAsFilter)
    {
        // The required relation is 'peer' (i.e. anypeer)
        // It takes the current markable in list 1 and determines its peer in the set 'attribute',
        // For independent input lists:
        // It searches the entire list 2 for current peer, and adds every tuple of m1 and m2 in the same set

        // For dependent input lists:
        // It adds the tuple of m1 and the corresponding markable in list2 if being in the same set
        // This requires a string and two lists
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        
        String attribute = (String) parameters.get(0);
        MMAX2QueryResultList list1 = (MMAX2QueryResultList)parameters.get(1);
        MMAX2QueryResultList list2 = (MMAX2QueryResultList)parameters.get(2);

        boolean list1ColSpec = false;
        boolean list2ColSpec = false;
        
        //Determine whether any of the input lists has a column specifier
        if (list1.isIndexSetByUser())
        {
            list1ColSpec = true;
        }

        if (list2.isIndexSetByUser())
        {
            list2ColSpec = true;
        }        
        
        // By default, assume independence of input columns
        boolean dependentParameters = false;            
        if (list1.getCommand().startsWith("$") && list2.getCommand().startsWith("$") &&
            list1.getCommand().indexOf(".")!=-1 && list2.getCommand().indexOf(".")!=-1)
        {
            // Both input columns are variables with col specs, so they might be dependent
            String firstCommand = list1.getCommand();
            String secondCommand = list2.getCommand();
            String firstVariable = firstCommand.substring(0,firstCommand.indexOf("."));
            if (secondCommand.startsWith(firstVariable+"."))
            {
                System.err.println("Any peer: Input parameters are dependent!");
                dependentParameters = true;
            }
        }

        MMAX2QueryResultTuple currentAnteTuple = null;
        Markable currentAnte = null;
        MMAX2QueryResultTuple currentAnaTuple = null;
        Markable currentAna = null;
        String setValue="";    
        
        if (!dependentParameters)
        {
            // The two input columns are not dependent
            currentAnteTuple = null;
            currentAnte = null;
            currentAnaTuple = null;
            currentAna = null;
            setValue = "";
            
            // Iterate over all markables in first list
            for (int a=0;a<list1.size();a++)
            {                
                if (list1ColSpec == false)
                {
                    // List 1 did not have a col spec, so get next appropriate element for matching
                    currentAnteTuple = list1.getTupleAtIndex(a);
                    currentAnte = currentAnteTuple.getLastMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);                    
                }
                else
                {
                    // List 1 did have a col spec, so take that for matching
                    currentAnte = list1.getElementAtIndexFromColumnToUse(a).getMarkable();
                }
                
                // Get id of set that curent ante is in, if any
                setValue=currentAnte.getAttributeValue(attribute,"empty");
                if (setValue.equals("empty")==false)
                {       
                    // Only if current ante is in some set
                    // Iterate over second list
                    for (int o=0;o<list2.size();o++)
                    {    
                        // Get elements from second list
                        if (list2ColSpec == false)
                        {
                            // List 2 did not have a col spec, so get next appropriate element for matching
                            currentAnaTuple = list2.getTupleAtIndex(o);
                            currentAna = currentAnaTuple.getFirstMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);
                        }
                        else
                        {
                            // List 2 did have a col spec, so take that for matching
                            currentAna = list2.getElementAtIndexFromColumnToUse(o).getMarkable();
                        }
                        
                        if (currentAna.getAttributeValue(attribute,"empty").equals(setValue))
                        {
                            // The current element in list 2 is in the same set as currentAnte
                            // Add both to result
                            if (currentAna != currentAnte)
                            {   
                                if (useAsFilter)
                                {
                                	result.addSingleTuple(currentAnteTuple);
                                }
                                else
                                {
                                    // Always use entire result, no matter if col specs were used
                                	result.mergeAndAdd(currentAnteTuple, currentAnaTuple, Constants.AStart_BEnd);
                                }
                                currentAnaTuple = null;
                                currentAna = null;
                            }
                        }
                    }// for all markables in list 2
                }
            }// for all markables in list 1            
        }
        else
        {
            // Dependent input lists
            currentAnteTuple = null;
            currentAnte = null;
            currentAnaTuple = null;            
            currentAna = null;
            int size1 = list1.size();
            String val1 = "";
            
            // The two input columns are dependent, so iterate pairwise, counting on any
            for (int a=0;a<size1;a++)
            {
                // Here, both elements MUST have colspecs, otherwise they could not be dependent                
            	currentAnte = list1.getElementAtIndexFromColumnToUse(a).getMarkable();
            	currentAna = list2.getElementAtIndexFromColumnToUse(a).getMarkable();
                
                val1 = currentAnte.getAttributeValue(attribute,"empty");
                if (val1.equals("empty"))
                {
                    // currentAnte is in no set, so continue with next
                    continue;
                }
                if (currentAna.getAttributeValue(attribute,"empty").equals(val1))
                {
                    if (useAsFilter)
                    {
                    	result.addSingleTuple(currentAnteTuple);
                    }
                    else
                    {
                    	result.mergeAndAdd(currentAnteTuple, currentAnaTuple, Constants.AStart_BEnd);
                    }
                }
            }                
        }
        result.setCommand("peers");        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;        
        }
    }

    private final MMAX2QueryResultList executeSetQuery_NextPeer(ArrayList parameters, boolean useAsFilter)
    {
        // The required relation is 'next_peer'
        // It takes the current markable in list 1 and determines its direct peer in the set 'attribute'.
        // For independent input lists:
        // It searches the *entire* list 2 for current peer, and adds the tuple if found, else not.

        // For dependent input lists:
        // It adds the tuple if the *corresponding* markable in list2 is current peer, else not
        
        // This requires a string and two lists
        
        // Create list to accept result
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        // Extract attributes
        String attribute = (String) parameters.get(0);
        MMAX2QueryResultList list1 = (MMAX2QueryResultList)parameters.get(1);
        MMAX2QueryResultList list2 = (MMAX2QueryResultList)parameters.get(2);

        boolean list1ColSpec = false;
        boolean list2ColSpec = false;
        
        //Determine whether any of the input lists has a column specifier
        if (list1.isIndexSetByUser())
        {
            list1ColSpec = true;
        }

        if (list2.isIndexSetByUser())
        {
            list2ColSpec = true;
        }
        
        // Determine whether the two input lists are dependent, i.e. if they access different 
        // columns of the same variable
        // By default, assume independence of input columns
        boolean dependentParameters = false;
        
        String firstVariable="";
                
        if (list1.getCommand().startsWith("$") && list2.getCommand().startsWith("$") &&
            list1.getCommand().indexOf(".")!=-1 && list2.getCommand().indexOf(".")!=-1)
        {
            // Both input columns are variables with col specs, so they might be dependent
            String firstCommand = list1.getCommand();
            String secondCommand = list2.getCommand();
            firstVariable = firstCommand.substring(0,firstCommand.indexOf("."));
            if (secondCommand.startsWith(firstVariable+"."))
            {
                dependentParameters = true;
            }
        }
        
        // Declare all variables here only once
        MMAX2QueryResultTuple currentAnteTuple = null;
        Markable currentAnte = null;
        MMAX2QueryResultTuple currentCandidateTuple = null;
        Markable currentCandidate = null;
        Markable currentAna = null;
        MarkableSet set = null;
        MarkableRelation[] relations = null;
        
        if (!dependentParameters)
        {
            // The two parameter lists are independent of each other
            currentAnteTuple = null;
            currentAnte = null;
            currentCandidateTuple = null;
            currentCandidate = null;
            currentAna = null;
            set = null;
            relations = null;

            // Iterate over entire list 1, tuple-wise
            for (int a=0;a<list1.size();a++)
            {                
                // New: Get tuple at any rate
                // Get resultTuple at position a
                currentAnteTuple = list1.getTupleAtIndex(a);
                if (list1ColSpec == false)
                {                    
                    // For the ante, get the *last* markable in tuple for which set-attribute
                    // 'attribute' is defined                    
                    currentAnte = currentAnteTuple.getLastMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);                
                    if (currentAnte == null)
                    {
                        // No compatible markable could be found in current ante tuple
                        continue;
                    }
                }
                else
                {                   
                    currentAnte = list1.getElementAtIndexFromColumnToUse(a).getMarkable();
                }                     
                
                if (currentAnte == null)
                {
                    continue;
                }
                
                // Reset set
                set = null;
                // Get all Relations for current ante
                relations = currentAnte.getMarkableLevel().getActiveMarkableSetRelationsForMarkable(currentAnte);
                // Iterate over all Relations for current ante
                for (int z=0;z<relations.length;z++)
                {
                    // Check if the current Relation is the one accessed in this query
                    if (relations[z].getAttributeName().equalsIgnoreCase(attribute))
                    {
                        // Try to get the set that current ante is a member of
                        set = relations[z].getMarkableSetWithAttributeValue(currentAnte.getAttributeValue(attribute));
                        // Break here, because only one relation with name attribute is allowed at a time (?)
                        break;
                    }
                }                    
                // Now, set is either null or the set that current ante is in
                if (set != null)
                {       
                    // Set is not null, so current ante is part in some set
                    // Get entire set that current ante is part of
                    ArrayList currentAntesPeers = (ArrayList)java.util.Arrays.asList(set.getOrderedMarkables());

                    // Get position of current starter relative to its peers
                    int currentAntesStart = set.getMarkableIndex(currentAnte);

                    if (currentAntesStart<set.getSize()-1)
                    {
                        // Only if current ante is not final in its set
                        // Get current peer, closest to currentAnte
                        currentAna = (Markable)currentAntesPeers.get(currentAntesStart+1);

                        // Now try to find current peer (and only this!) in second argument list                                            
                        // Iterate over second list
                        for (int o=0;o<list2.size();o++)
                        {    
                            // New: get tuple at any rate
                            currentCandidateTuple = list2.getTupleAtIndex(o);
                            
                            if (list2ColSpec == false)
                            {
                               // For the ana candidate, get the *first* markable in tuple for which set-attribute
                               // 'attribute' is defined in list 2
                                currentCandidate = currentCandidateTuple.getFirstMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);
                                if (currentCandidate == null)
                                {
                                    // No compatible markable could be found in current ante tuple
                                    continue;
                                }
                            }
                            else
                            {
                                currentCandidate = list2.getElementAtIndexFromColumnToUse(o).getMarkable();
                            }

                            if (currentCandidate == currentAna)
                            {
                                // currentAna, i.e. the single next peer of current ante has been found in list 2
                                if (useAsFilter)
                                {
                                    result.addSingleTuple(currentAnteTuple);
                                }
                                else
                                {                                    
                                    result.mergeAndAdd(currentAnteTuple, currentCandidateTuple, Constants.AStart_BEnd);
                                }
                                currentAnteTuple = null;
                                currentAnte = null;
                                currentCandidateTuple = null;
                                currentCandidate = null;
                                // Stop searching for current peer
                                break;
                            }
                        }// for all markables in list 2
                        // In the worst case, we have to search the entire list 2
                    }
                }
            }// for all markables in list 1                            
        }
        else                
        {
            // Dependent parameters
            currentAnteTuple = null;
            currentAnte = null;
            currentCandidateTuple = null;
            currentCandidate = null;
            currentAna = null;                    
            set = null;
            relations = null;

            // Iterate over all markables in first list
            for (int a=0;a<list1.size();a++)
            {
                // New: Get entire ante tuple at any rate
                // If parameters are dependent, both MUST have a col spec
                // Wrong for complex ante tuples
                currentAnteTuple = null;
                currentAnteTuple = list1.getTupleAtIndex(a);
                                
                if (list1ColSpec == false)
                {                    
                    // For the ante, get the *last* markable in tuple for which set-attribute
                    // 'attribute' is defined                    
                    currentAnte = currentAnteTuple.getLastMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);                
                    if (currentAnte == null)
                    {
                        // No compatible markable could be found in current ante tuple
                        continue;
                    }
                }
                else
                {                   
                    // Get element at col spec for matching     
                    // This should be the normla case for dependent attributes
                    currentAnte = list1.getElementAtIndexFromColumnToUse(a).getMarkable();
                }                     
                                
                set = null;
                // Get all Relations for this Markable
                relations = currentAnte.getMarkableLevel().getActiveMarkableSetRelationsForMarkable(currentAnte);
                // Iterate over all Relations for this Markable
                for (int z=0;z<relations.length;z++)
                {
                    // Check if the current Relation is the one accessed in this query
                    if (relations[z].getAttributeName().equalsIgnoreCase(attribute))
                    {
                        // Try to get the set that this Markable is a member of
                        set = relations[z].getMarkableSetWithAttributeValue(currentAnte.getAttributeValue(attribute));
                        break;
                    }
                }
                // Now, set is either null or the set that current ante is in
                if (set != null)
                {       
                    // Set is not null, so current ante is part in some set
                    // Get entire set that current ante is part of
                    ArrayList currentAntesPeers = (ArrayList)java.util.Arrays.asList(set.getOrderedMarkables());

                    // Get position of current ante relative to its peers
                    int currentAntesStart = set.getMarkableIndex(currentAnte);

                    if (currentAntesStart<set.getSize()-1)
                    {
                        // Only if current starter is not final in its set
                        // Get current closest peer
                        currentAna = (Markable)currentAntesPeers.get(currentAntesStart+1);
                        
                        // New: Get whole tuple at any rate
                        currentCandidateTuple = null;
                        currentCandidateTuple = list2.getTupleAtIndex(a);

                        if (list2ColSpec == false)
                        {
                            // For the ana candidate, get the *first* markable in tuple for which set-attribute
                            // 'attribute' is defined in list 2
                            currentCandidate = currentCandidateTuple.getFirstMarkableWithAttribute(attribute,AttributeAPI.MARKABLE_SET);
                            if (currentCandidate == null)
                            {
                                // No compatible markable could be found in current ante tuple
                                continue;
                            }
                        }
                        else
                        {
                            currentCandidate = list2.getElementAtIndexFromColumnToUse(a).getMarkable();
                        }                        
                        
                        // Now check if current peer is at pos a in list 2
                        if (currentCandidate == currentAna)
                        {
                            // currentCandidate from list 2 is the direct peer of current ante
                            // New May 15th: Since parameters are dependent, both tuples are identical
                            // So no need to merge them. Rather, always return only first tuple.
                            // So: Dependent queries are NEVER joiners
                            result.addSingleTuple(currentAnteTuple);
                        }
                    }
                }
            }// for all markables in list 1  
        }// end dependent parameters
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;        
        }
    }
    
        
    /** This method returns a MMAX2QueryResultList. It also expects MMAX2QueryResultLists.
        For matching, the column specified in the parameter list's indexToUse field is used.
        This returns null if command could not be interpreted as a relation query. */
    private final MMAX2QueryResultList executeRelationQuery(String command)
    {        
        // Create list to accept result
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        
        MMAX2QueryResultList list1 = null;
        MMAX2QueryResultList list2 = null;
                
        // Check for sign of filter
        // default:joiner
        boolean useAsFilter = false;
        // Now check for filter
        if (command.startsWith("#"))
        {
            useAsFilter = true;
            command = command.substring(1).trim();
        }        
        
        // Flag for negated query. Negation of relation queries is non-trivial!!
        boolean negated = false;
    
        // Check for signs of negation *after* signs of filtering
        if (command.startsWith("!"))
        {
            negated = true;
            command = command.substring(1).trim();           
        }
        else if (command.startsWith("not"))
        {
            negated = true;
            command = command.substring(3).trim();
        }                                    

        // Find first ( in command, which is supposed to be the one around the two parameter expressions
        int paraStart = command.indexOf("(");
        while (MMAX2QueryWindow.isInReferenceListBrackets(command,paraStart))
        {
            paraStart++;
            paraStart = command.indexOf("(",paraStart);
        }

        if (paraStart == -1) 
        {
            System.err.println("Error: Expected '('!");
            return null;
        }                
        // Extract command
        String relationCommand = command.substring(0,paraStart).trim();
        // and set to lower case        

        System.err.println("RC: "+relationCommand);

        // Init string to accept (optional) distance specification for before relation
        
        String distString = "";
        if (relationCommand.startsWith("before:"))
        {
            distString = relationCommand.substring(relationCommand.indexOf(":")+1);
            System.err.println("DIST: "+distString);
            relationCommand = "before";
        }
        
        // Check for relation command
        if (" merge subtract spans starts starts_strict starts_with starts_with_strict started_by started_by_strict ends ends_strict ends_with ends_with_strict finishes finishes_strict finished_by finished_by_strict during during_strict contains contains_strict equals meets before before_strict after overlaps_right overlaps_left ".indexOf(" "+relationCommand+" ")==-1)
        {
            System.err.println("No relation query!");
            return null;
        }
        
        // Get matching top level bracket, i.e. the one after the second parameter expression
        int paraEnd = getMatchingBracketPosition(command,paraStart);
        if (paraEnd == -1)
        {
            System.err.println("Error: Expected ')'!");
            return null;            
        }
                
        // Extract sub-query that produces the tuple lists to apply relation query to
        String parameterProducingCommandList = command.substring(paraStart,paraEnd).trim();
        // Get the two lists to be matched by evaluating the parameter expressions
        // The two expressions can be any MMAX2QueryResultList-producing expressions!
        ArrayList allParameterLists = getParameterLists(parameterProducingCommandList);
        
        if (allParameterLists.size() != 2)
        {
            System.err.println("Error! Illegal number of parameter lists for relation query! ("+allParameterLists.size()+")");
            return result;
        }

        // Get tuple list produced by first parameter
        list1 = (MMAX2QueryResultList) allParameterLists.get(0);
        // Get tuple list produced by secoond parameter
        list2 = (MMAX2QueryResultList) allParameterLists.get(1);
        
        if (list1 == null || list2 == null)
        {
            return result;
        }
        
        String spec = "";
        
        if (command.length() > paraEnd && command.substring(paraEnd,paraEnd+1).equals("."))
        {
            // The current relation query has a column specifier
            // Extract it
            spec = command.substring(paraEnd+1);
            // This is the specifier which will be applied to the RESULT of this query
            // It does not say anything about the input parameters!!
        }        
                        
        // Store whether the input lists have col specs
        // This info must be passed to the compare-method
        int list1ColSpec = -1;
        if (list1.isIndexSetByUser())
        {
            list1ColSpec = list1.getIndexToUse();
        }
        
        int list2ColSpec = -1;        
        if (list2.isIndexSetByUser())
        {
            list2ColSpec = list2.getIndexToUse();
        }
                        
        // Determine whether input arguments are dependent
        // Dependent means: They access different columns of the same variable
        boolean dependentParameters = false;
        if (list1.getCommand().startsWith("$") && list2.getCommand().startsWith("$") &&
            list1.getCommand().indexOf(".")!=-1 && list2.getCommand().indexOf(".")!=-1)
        {
            // Both input columns are variables with col specs, so they might be dependent
            String firstCommand = list1.getCommand();
            String secondCommand = list2.getCommand();
            String firstVariable = firstCommand.substring(0,firstCommand.indexOf("."));
            if (secondCommand.startsWith(firstVariable+"."))
            {
                System.err.println("Relation query: Input parameters are dependent!");
                dependentParameters = true;
            }
        }

        // Branch at top level to treat negated and non-negated relation queries differently
        // This is also more efficient as it spares us from checking 'negated' in every tuple iteration
        if (!negated)
        {
            // The relation query is not negated
            // This means that result tuples will always consist of the *merge* of the two matched tuples
            if (relationCommand.equals("subtract"))
            {               
                result = subtract(list1,list2);
            }     
            else if (relationCommand.equals("merge"))
            {
                result = merge(list1,list2);
            }
            else if (relationCommand.equals("during"))            
            {                
                /// OK filter, dependencies
                result = executeRelationQuery_During(list1, list1ColSpec, list2, list2ColSpec, useAsFilter, dependentParameters);                
            }
            else if (relationCommand.equals("during_strict"))
            {                
                /// OK filter, dependencies
                result = executeRelationQuery_DuringStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }    
            else if (relationCommand.equals("contains"))
            {
                /// OK filter, dependencies
                result = executeRelationQuery_Contains(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("contains_strict"))
            {
                /// OK filter, dependencies
                result = executeRelationQuery_ContainsStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);                
            }            
            else if (relationCommand.equals("meets"))
            {   
                /// OK filter, dependencies
                result = executeRelationQuery_Meets(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("before"))
            {
                if (distString.equals(""))
                {
                    /// 
                    result = executeRelationQuery_Before(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);                                
                }
                else
                {
                    /// 
                    result = executeRelationQuery_Distance(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters,distString);                    
                }
            }            
            else if (relationCommand.equals("before_strict"))
            {
                /// 
                result = executeRelationQuery_BeforeStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);                                
            }                        
            else if (relationCommand.equals("starts"))
            {
                result = executeRelationQuery_Starts(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("starts_strict"))
            {
                result = executeRelationQuery_StartsStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }        
            else if (relationCommand.equals("starts_with") || relationCommand.equals("started_by"))
            {
                result = executeRelationQuery_StartsWith(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("starts_with_strict") || relationCommand.equals("started_by_strict"))
            {
                result = executeRelationQuery_StartsWithStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }        
            else if (relationCommand.equals("ends") || relationCommand.equals("finishes"))
            {
                result = executeRelationQuery_Ends(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_strict") || relationCommand.equals("finishes_strict"))
            {
                result = executeRelationQuery_EndsStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_with") || relationCommand.equals("finished_by"))
            {
                result = executeRelationQuery_EndsWith(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_with_strict") || relationCommand.equals("finished_by_strict"))
            {
                result = executeRelationQuery_EndsWithStrict(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("equals"))
            {
                result = executeRelationQuery_Equals(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("spans"))
            {
                result = executeRelationQuery_Spans(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }            
            else if (relationCommand.equals("overlaps_right"))
            {
                result = executeRelationQuery_Overlaps_right(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("overlaps_left"))
            {
                result = executeRelationQuery_Overlaps_left(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }            
            else if (relationCommand.equals("after"))
            {
                // The required relation is 'after'
                // Iterate over all potential second tuples (first para)
                for (int a=0;a<list1.size();a++)
                {                 
                    MMAX2QueryResultTuple currentSecond = list1.getTupleAtIndex(a);
                    // Iterate over all potential first tuples (second para)
                    for (int b=0;b<list2.size();b++)
                    {                 
                        MMAX2QueryResultTuple currentFirst = list2.getTupleAtIndex(b);
                        boolean match = MarkableHelper.before(currentFirst,currentSecond,list2ColSpec,list1ColSpec);
                        if (match)
                        {
                            result.mergeAndAdd(currentFirst,currentSecond,Constants.BStart_AEnd);
                        }
                    }
                }            
            }
            else
            {
                // No relation query
                result = null;
            } 
        }
        else
        {
            // The current relation query is negated
            // Negation serves as a filter only, i.e. it does not produce tuples of elements standing
            // NOT in some relation!! Instead, it adds to the result only the first element of the tuple            
            
            // New: For dependent parameters, negation will also add the entire tuple unless # is used!!
            // Also, duplicates are always removed for independent parameters, but for dependent only if
            // useAsFilter is set
            if (relationCommand.equals("meets"))
            {   
                /// OK dependent, filtering
                result = executeRelationQuery_Meets_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("during"))
            {
                /// OK dependent, filtering
                result = executeRelationQuery_During_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);                                
            }                               
            else if (relationCommand.equals("during_strict"))
            {
                /// OK dependent, filtering
                result = executeRelationQuery_DuringStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }                               
            else if (relationCommand.equals("contains"))
            {
                /// OK dependent, filtering
                result = executeRelationQuery_Contains_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("contains_strict"))
            {
                /// OK dependent, filtering
                result = executeRelationQuery_ContainsStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter, dependentParameters);
            }
            else if (relationCommand.equals("before"))
            {
                /// 
                result = executeRelationQuery_Before_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("before_strict"))
            {
                /// 
                result = executeRelationQuery_BeforeStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("starts"))
            {
                /// 
                result = executeRelationQuery_Starts_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("starts_strict"))
            {
                /// 
                result = executeRelationQuery_StartsStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends") || relationCommand.equals("finishes"))
            {
                /// 
                result = executeRelationQuery_Ends_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_strict") || relationCommand.equals("finishes_strict"))
            {
                /// 
                result = executeRelationQuery_EndsStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_with") || relationCommand.equals("finished_by"))
            {
                /// 
                result = executeRelationQuery_EndsWith_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("ends_with_strict") || relationCommand.equals("finished_by_strict"))
            {
                /// 
                result = executeRelationQuery_EndsWithStrict_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("equals"))
            {
                result = executeRelationQuery_Equals_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("spans"))
            {
                result = executeRelationQuery_Spans_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }            
            else if (relationCommand.equals("overlaps_right"))
            {
                result = executeRelationQuery_Overlaps_right_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            else if (relationCommand.equals("overlaps_left"))
            {
                result = executeRelationQuery_Overlaps_left_Negated(list1, list1ColSpec,list2, list2ColSpec,useAsFilter,dependentParameters);
            }
            
            // Put code for negated relation queries here
            System.err.println("Negated relation queries not implemented yet!");
        }
                
        // Check if some result has been produced at all
        if (result != null)
        {
            int index = -1;
            try
            {
                // Try if identified spec (if any) can be parsed as number
                index = Integer.parseInt(spec);
            }
            catch (java.lang.NumberFormatException ex)
            {
                // Do nothing here
            }
            
            if (index != -1)
            {
                // The spec could be parsed in a number
                if (index >= result.getWidth())
                {
                    JOptionPane.showMessageDialog(null,"Cannot access index "+index+" in result list! Result list width is "+result.getWidth()+", using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    result.setIndexToUse(index);
                    // This is only used if a col spec was used earlier, OK
                    result.setIndexSetByUser();
                }
            }            
            else
            {
                // spec could not be parsed as a number
                if (spec.equals("")==false)
                {
                    // Try to get numerical index for text spec
                    index = result.getColumnIndexByColumnName(spec);
                    if (index != -1)
                    {
                        result.setIndexToUse(index);
                        // This is only used if a col spec was used earlier, OK
                        result.setIndexSetByUser();                    
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,"Column specifier '"+spec+"' not found or not unique, using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        return result;
    }
       
    private int getMatchingBracketPosition(String string, int startposition)
    {
        int openedBrackets = 0;
        int closedBrackets = 0;
        String currentChar="";
        /* Iterate over rest of tree from startposition on */
        for (int z=startposition+1;z<string.length();z++)
        {
            currentChar = string.substring(z,z+1);
            if (currentChar.equals(")"))
            {
                /* The character at the current position is ) */
                if (openedBrackets == closedBrackets)
                {
                    /* We are on par */
                    return z+1;
                }
                else
                {
                    closedBrackets++;
                }
            }
            else if (currentChar.equals("("))
            {
                openedBrackets++;
            }
        }// for z        
        return -1;
    }
            
    
    private final MMAX2QueryResultList executeRelationQuery_Starts(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'starts'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential starter tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current starter starts
                    int firstTuplesInitialDiscPos = currentStarter.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential started tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarted = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        {
                            // The current started begins after current starter, and all following starteds
                            // will start there import later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.starts(currentStarter,currentStarted,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentStarter);
                        }
                        else
                        {
                            result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                currentStarted = list2.getTupleAtIndex(a);
                if (MarkableHelper.starts(currentStarter,currentStarted,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarter);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }

    private final MMAX2QueryResultList executeRelationQuery_Starts_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'starts'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential starter tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current starter starts
                    int firstTuplesInitialDiscPos = currentStarter.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential started tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarted = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        {
                            // The current started begins after current starter, and all following starteds
                            // will start there import later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.starts(currentStarter,currentStarted,list1ColSpec,list2ColSpec);
                    if (anyMatch) break;
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentStarter);
                }
                anyMatch=false;                                         
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                currentStarted = list2.getTupleAtIndex(a);
                if (MarkableHelper.starts(currentStarter,currentStarted,list1ColSpec,list2ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarter);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }        
    }
    
    
    private final MMAX2QueryResultList executeRelationQuery_StartsStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {    
        // The required relation is 'starts_strict'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential starter tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current starter starts
                    int firstTuplesInitialDiscPos = currentStarter.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential started tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarted = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        {
                            // The current started begins after current starter, and all following starteds
                            // will start there import later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.startsStrict(currentStarter,currentStarted,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentStarter);
                        }
                        else
                        {
                            result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                currentStarted = list2.getTupleAtIndex(a);
                if (MarkableHelper.startsStrict(currentStarter,currentStarted,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarter);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }
    
    private final MMAX2QueryResultList executeRelationQuery_StartsStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'starts_strict'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential starter tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current starter starts
                    int firstTuplesInitialDiscPos = currentStarter.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential started tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarted = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        {
                            // The current started begins after current starter, and all following starteds
                            // will start there import later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.startsStrict(currentStarter,currentStarted,list1ColSpec,list2ColSpec);
                    if (anyMatch) break;
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentStarter);
                }
                anyMatch=false;                                         
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarter = list1.getTupleAtIndex(a);
                currentStarted = list2.getTupleAtIndex(a);
                if (MarkableHelper.startsStrict(currentStarter,currentStarted,list1ColSpec,list2ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarter);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarter,currentStarted,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }        
    }
    
    
    private final MMAX2QueryResultList executeRelationQuery_StartsWith(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'starts_with' (i.e. started_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential started tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current started starts
                    int firstTuplesInitialDiscPos = currentStarted.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list1.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential starting tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarter = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        //if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        if (currentStarter.getLeftmostDiscoursePosition() > currentStarted.getLeftmostDiscoursePosition())
                        {
                            // The current starter begins after current started, and all following starters
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.starts(currentStarter,currentStarted,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentStarted);
                        }
                        else
                        {
                            result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                currentStarter = list2.getTupleAtIndex(a);
                if (MarkableHelper.starts(currentStarter,currentStarted,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarted);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }
    
    private final MMAX2QueryResultList executeRelationQuery_StartsWithStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'starts_with_strict' (i.e. started_by_strict)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential started tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current started starts
                    int firstTuplesInitialDiscPos = currentStarted.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list1.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential starting tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarter = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        //if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        if (currentStarter.getLeftmostDiscoursePosition() > currentStarted.getLeftmostDiscoursePosition())
                        {
                            // The current starter begins after current started, and all following starters
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.startsStrict(currentStarter,currentStarted,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentStarted);
                        }
                        else
                        {
                            result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                currentStarter = list2.getTupleAtIndex(a);
                if (MarkableHelper.startsStrict(currentStarter,currentStarted,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarted);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }

    /*
    private final MMAX2QueryResultList executeRelationQuery_StartsWithStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'starts_with_strict' (i.e. started_by_strict)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential started tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current started starts
                    int firstTuplesInitialDiscPos = currentStarted.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list1.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential starting tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarter = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        //if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        if (currentStarter.getLeftmostDiscoursePosition() > currentStarted.getLeftmostDiscoursePosition())
                        {
                            // The current starter begins after current started, and all following starters
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.startsStrict(currentStarter,currentStarted,list2ColSpec,list1ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentStarted);
                }
                anyMatch = false;
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                currentStarter = list2.getTupleAtIndex(a);
                if (MarkableHelper.startsStrict(currentStarter,currentStarted,list2ColSpec,list1ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarted);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }        
    }
    */
    
    /*
    private final MMAX2QueryResultList executeRelationQuery_StartsWith_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'starts_with' (i.e. started_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentStarter = null;
        MMAX2QueryResultTuple currentStarted = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential started tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // get the disc pos at which current started starts
                    int firstTuplesInitialDiscPos = currentStarted.getLeftmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list1.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                
                
                // Iterate over all potential starting tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentStarter = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        //if (currentStarted.getLeftmostDiscoursePosition() > currentStarter.getLeftmostDiscoursePosition())
                        if (currentStarter.getLeftmostDiscoursePosition() > currentStarted.getLeftmostDiscoursePosition())
                        {
                            // The current starter begins after current started, and all following starters
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.starts(currentStarter,currentStarted,list2ColSpec,list1ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentStarted);
                }
                anyMatch=false;
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentStarted = list1.getTupleAtIndex(a);
                currentStarter = list2.getTupleAtIndex(a);
                if (MarkableHelper.starts(currentStarter,currentStarted,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentStarted);
                    }
                    else
                    {
                        result.mergeAndAdd(currentStarted,currentStarter,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }        
    }    
    */
    
    private final MMAX2QueryResultList executeRelationQuery_Ends(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential ender tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ended tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnded = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnded.getLeftmostDiscoursePosition() > currentEnder.getLeftmostDiscoursePosition())
                        {
                            // The current ended begins after current enderer, and all following endeds
                            // will start there or lazer
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.finishes(currentEnder,currentEnded,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentEnder);
                        }
                        else
                        {
                            result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                currentEnded = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishes(currentEnder,currentEnded,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnder);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }    

    private final MMAX2QueryResultList executeRelationQuery_EndsWith(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_with' (i.e. finished_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential ended tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ender tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnder = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnder.getLeftmostDiscoursePosition() > currentEnded.getRightmostDiscoursePosition())                      
                        {
                            // The current ender begins after current ended, and all following enders
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.finishes(currentEnder,currentEnded,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentEnded);
                        }
                        else
                        {
                            result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                currentEnder = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishes(currentEnder,currentEnded,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }    

    private final MMAX2QueryResultList executeRelationQuery_EndsWith_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_with' (i.e. finished_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential ended tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ender tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnder = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnder.getLeftmostDiscoursePosition() > currentEnded.getRightmostDiscoursePosition())                      
                        {
                            // The current ender begins after current ended, and all following enders
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.finishes(currentEnder,currentEnded,list2ColSpec,list1ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentEnded);
                }
                anyMatch=false;                
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                currentEnder = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishes(currentEnder,currentEnded,list2ColSpec,list1ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }                
    }    

    private final MMAX2QueryResultList executeRelationQuery_EndsWithStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_with' (i.e. finished_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential ended tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ender tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnder = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnder.getLeftmostDiscoursePosition() > currentEnded.getRightmostDiscoursePosition())                      
                        {
                            // The current ender begins after current ended, and all following enders
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.finishesStrict(currentEnder,currentEnded,list2ColSpec,list1ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentEnded);
                }
                anyMatch=false;                
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                currentEnder = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list2ColSpec,list1ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }                
    }    
    
    
    private final MMAX2QueryResultList executeRelationQuery_EndsWithStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_with' (i.e. finished_by)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential ended tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ended tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnder = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnder.getLeftmostDiscoursePosition() > currentEnded.getRightmostDiscoursePosition())                      
                        {
                            // The current ender begins after current ended, and all following enders
                            // will start there or later
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentEnded);
                        }
                        else
                        {
                            result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnded = list1.getTupleAtIndex(a);
                currentEnder = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnded,currentEnder,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }    
    

    
    private final MMAX2QueryResultList executeRelationQuery_Ends_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'ends'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential ender tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ended tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnded = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnded.getLeftmostDiscoursePosition() > currentEnder.getLeftmostDiscoursePosition())
                        {
                            // The current ended begins after current enderer, and all following endeds
                            // will start there or lazer
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.finishes(currentEnder,currentEnded,list1ColSpec,list2ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentEnder);
                }
                anyMatch=false;
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                currentEnded = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishes(currentEnder,currentEnded,list1ColSpec,list2ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnder);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }                
    }    
    
    
    private final MMAX2QueryResultList executeRelationQuery_EndsStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_strict' (i.e. finishes_strict)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potential ender tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ended tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnded = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnded.getLeftmostDiscoursePosition() > currentEnder.getLeftmostDiscoursePosition())
                        {
                            // The current ended begins after current enderer, and all following endeds
                            // will start there or lazer
                            break;
                        }
                    }
                    // Get result of actual matching
                    if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentEnder);
                        }
                        else
                        {
                            result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                currentEnded = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnder);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }                    
    }    

    private final MMAX2QueryResultList executeRelationQuery_EndsStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is 'ends_strict' (i.e. finishes_strict)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEnder = null;
        MMAX2QueryResultTuple currentEnded = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        boolean anyMatch = false;
        if (!dependentParameters)
        {
            // Iterate over all potential ender tuples (first para)                
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                
                // Iterate over all potential ended tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentEnded = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        if (currentEnded.getLeftmostDiscoursePosition() > currentEnder.getLeftmostDiscoursePosition())
                        {
                            // The current ended begins after current enderer, and all following endeds
                            // will start there or lazer
                            break;
                        }
                    }
                    // Get result of actual matching
                    anyMatch = MarkableHelper.finishesStrict(currentEnder,currentEnded,list1ColSpec,list2ColSpec);
                    if (anyMatch) break;
                }
                if (anyMatch == false)
                {
                    result.addSingleTuple(currentEnder);
                }
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {   
                currentEnder = list1.getTupleAtIndex(a);
                currentEnded = list2.getTupleAtIndex(a);
                if (MarkableHelper.finishesStrict(currentEnder,currentEnded,list1ColSpec,list2ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEnder);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEnder,currentEnded,Constants.AStart_AEnd);
                    }
                }
            }
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }                        
    }    
    
    
    private final MMAX2QueryResultList executeRelationQuery_Meets(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!        
        // Optimization OK
        // filter-support ok April 20th, 2005
        // The required relation is 'meets'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentTuple1 = null;
        MMAX2QueryResultTuple currentTuple2 = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all elements in first parameter list
            for (int a=0;a<list1Size;a++)
            {
                // Get current element in first parameter list
                // This is the left tuple in the 'meeting' pair, potentially complex
                currentTuple1 = list1.getTupleAtIndex(a);

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Get disc pos at which left element of 'meeting' pair ends
                    int firstTuplesFinalDiscPos = currentTuple1.getRightmostDiscoursePosition();                    
                    // Get index of that element in list 2 that is the earliest candidate to meet element 1
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }                        
                }

                // Iterate over second parameter list, starting at first plausible candidate as determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)               
                {
                    // Get current element in second parameter list
                    currentTuple2 = list2.getTupleAtIndex(b);                    

                    if (doBreakOptimization)
                    {
                        // New April 1st, 2005: Do some optimization                    
                        // meets is true if m2 starts at m1.end+1
                        // We assume that all markables are sorted by their first disc pos                    
                        if (currentTuple2.getLeftmostDiscoursePosition() > currentTuple1.getRightmostDiscoursePosition()+1)
                        {
                            // e2 starts after e1 ends+1, so elements after e2 can never meet e1
                            break;
                        }
                    }

                    if (MarkableHelper.meets(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec))
                    {   
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentTuple1);
                        }
                        else
                        {
                            // Merge and add. The width of the result will be from AStart to BEnd
                            result.mergeAndAdd(currentTuple1,currentTuple2,Constants.AStart_BEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentTuple1 = list1.getTupleAtIndex(a);
                currentTuple2 = list2.getTupleAtIndex(a);
                if (MarkableHelper.meets(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec))
                {   
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentTuple1);
                    }
                    else
                    {
                        // Merge and add. The width of the result will be from AStart to BEnd
                        result.mergeAndAdd(currentTuple1,currentTuple2,Constants.AStart_BEnd);
                    }
                }
            }            
        }
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }            
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Meets_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'meets'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentTuple1 = null;
        MMAX2QueryResultTuple currentTuple2 = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Parameters are not dependent!
            // Iterate over all elements in first parameter list
            for (int a=0;a<list1Size;a++)
            {
                // Get current element in first parameter list
                // This is the left tuple in the 'meeting' pair, potentially complex
                currentTuple1 = list1.getTupleAtIndex(a);                

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;                                        

                // Rationale behind negated meets optimization:
                // Query tries to falsify assumption that no match exists. 
                // Optimization now skips those elements known a priori to not match
                if (doLookAheadOptimization)
                {
                    // Get disc pos at which left element of 'meeting' pair ends
                    int firstTuplesFinalDiscPos = currentTuple1.getRightmostDiscoursePosition();                    
                    // Get index of that element in list 2 that is the earliest candidate to meet element 1
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }                        
                }

                // Iterate over second parameter list, starting at first plausible candidate as determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)               
                {
                    // Get current element in second parameter list
                    currentTuple2 = list2.getTupleAtIndex(b);         

                    if (doBreakOptimization)
                    {
                        // New April 1st, 2005: Do some optimization                    
                        // meets is true if m2 starts at m1.end+1
                        // We assume that all markables are sorted by their first disc pos                    
                        if (currentTuple2.getLeftmostDiscoursePosition() > currentTuple1.getRightmostDiscoursePosition()+1)
                        {
                            // e2 starts after e1 ends+1, so elements after e2 can never meet e1
                            // anyMatch remains false
                            break;
                        }
                    }                                                
                    // Get match
                    anyMatch = MarkableHelper.meets(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec);
                    // If match is true, break to false
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {   
                    // This is the independent case, in which always only the first is returned
                    // Add. The width of the result will be the same as currentTuple1
                    result.addSingleTuple(currentTuple1);
                }
                anyMatch=false;                     
            }
        }    
        else
        {
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentTuple1 = list1.getTupleAtIndex(a);                
                currentTuple2 = list2.getTupleAtIndex(a);
                if (MarkableHelper.meets(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec)==false)
                {
                    // The negated condition matches for this dependent pair
                    if (useAsFilter)
                    {
                        // The filter is set explicitly, so return first only
                        result.addSingleTuple(currentTuple1);
                    }
                    else
                    {
                        // No filter is set explicitly, so return all
                        result.mergeAndAdd(currentTuple1,currentTuple2,Constants.AStart_BEnd);
                    }
                }
            }            
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Before(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)    
    {
        // The required relation is 'before'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potential first tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // Get pos at which current first ends.
                    // If it should be before currentSecond, that can start at its end, earliest
                    int firstTuplesFinalDiscPos = currentFirst.getRightmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }
                
                // Iterate over all potential second tuples (second para)
                // starting at first plausible candidate determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentSecond = list2.getTupleAtIndex(b);
                    
                    // No BreakOpt possible, since everything after first is a match
                    if (MarkableHelper.before(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentFirst);
                        }
                        else
                        {
                            result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_BEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter = true;
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                currentSecond = list2.getTupleAtIndex(a); 
                if (MarkableHelper.before(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_BEnd);
                    }
                }
            }
        }        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }        
    }

    private final MMAX2QueryResultList executeRelationQuery_Distance(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters, String distString)
    {
        // The required relation is 'distance' (i.e. before:m-a)
        // By default, distance is measured in discourse position, which is equiv. to number of
        // words/tokens. 
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean useSuppliedReferenceList = false;
        MMAX2QueryResultList[] refLists = null;
        
        MMAX2QueryResultList modRefList = null;
        MMAX2QueryResultTuple[] modRefArray = null;
        int modRefWidth = -1;
        
        MMAX2QueryResultList fullRefList = null;
        MMAX2QueryResultTuple[] fullRefArray = null;
        
        int minDist = 0;
        int maxDist = Integer.MAX_VALUE;  
                        
        if (distString.endsWith("]"))
        {
            // Dist string brings its own reference list
            int refStart=distString.indexOf("[");
            if (refStart==-1)
            {
                System.err.println("Dist string error!");
                return null;
            }
            String refListCommand = distString.substring(refStart+1,distString.length()-1);
            
            refLists = executeEmbeddedSimplifiedQuery(refListCommand);
            if (refLists == null)
            {
                System.err.println("Error executing embedded query!");
                return null;
            }
            if (refLists[0].size()==0)
            {
                System.err.println("Empty ref list, breaking!");
                return result;
            }
                
            modRefList = refLists[0];            
            modRefArray = (MMAX2QueryResultTuple[]) modRefList.toArray(new MMAX2QueryResultTuple[0]);
            modRefWidth = modRefList.getWidth();

            fullRefList = refLists[1];            
            fullRefArray = (MMAX2QueryResultTuple[]) fullRefList.toArray(new MMAX2QueryResultTuple[0]);

            // Destroy references not needed any more
            refLists = null;
            modRefList = null;
            fullRefList = null;
            
            // Cut off trailing ref list command
            distString=distString.substring(0,refStart);
            // remember to use supplied ref list
            useSuppliedReferenceList = true;
        }                
        
        if (distString.indexOf("-")==-1)
        {
            // The distance is only a number and no range
            minDist = Integer.parseInt(distString);
            maxDist = minDist;
        }
        else if (distString.startsWith("-"))
        {
            // -1 (less or equal to 1)
            // The distance is only a maximum distance
            maxDist = Integer.parseInt(distString.substring(1));
        }
        else if (distString.endsWith("-"))
        {
            // 1- (1 or more)
            // The distance is only a minimum distance
            minDist = Integer.parseInt(distString.substring(0,distString.length()-1));
        }        
        else
        {
            // The distance is a range
            minDist = Integer.parseInt(distString.substring(0,distString.indexOf("-")));
            maxDist = Integer.parseInt(distString.substring(distString.indexOf("-")+1));            
        }
                 
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        
        QueryResultTupleComparator compi = new QueryResultTupleComparator(0);
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        int currentDistance = 0;
        
        if (!dependentParameters)
        {
            // Iterate over all potential first tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a);
                int indexOfEarliestCandidateInSecondList = 0;
                                
                if (doLookAheadOptimization)
                {
                    // Get pos at which current first ends.
                    // If it should be before currentSecond, that can start at its end, earliest
                    int firstTuplesFinalDiscPos = currentFirst.getRightmostDiscoursePosition();
                    // New: This is on the safe side already
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                }
                
                // Iterate over all potential second tuples (second para)
                // starting at first plausible candidate determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    // Get current second from tuple 2
                    currentSecond = list2.getTupleAtIndex(b);
                                        
                    // No BreakOpt possible, since everything after first is a match
                    if (MarkableHelper.before(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                    {
                        if (useSuppliedReferenceList)
                        {
                            // The distance is to be determined wrt the refList
                            // Here, col specs are supposed to be -1
                            currentDistance = getDistanceFromReferenceList(currentFirst, list1ColSpec,currentSecond,list1ColSpec,modRefArray,modRefWidth,fullRefArray,compi,maxDist);
                        }
                        else
                        {
                            // The normal word dist is to be used
                            // Meeting elements have distance 3-2=1, which is OK
                            currentDistance = currentSecond.getLeftmostDiscoursePosition()-currentFirst.getRightmostDiscoursePosition();
                        }
                        if (currentDistance <= maxDist && currentDistance >= minDist)
                        {
                            // The current pair of tuples is in the desired distance from each other
                            if (useAsFilter)
                            {
                                result.addSingleTuple(currentFirst);
                            }
                            else
                            {
                                result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_BEnd);
                            }
                            
                            // Do some optimization: Make this search greedy!
                            break;
                        }
                        else
                        {                            
                            if (currentDistance > maxDist)
                            {
                                // Stop searching if current tuple pair is already too far apart
                                break;
                            }
                        }
                    }
                }
            }
        }
        else
        {                      
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a);
                currentSecond = list2.getTupleAtIndex(a); 
                                
                if (MarkableHelper.before(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                {
                    if (useSuppliedReferenceList)
                    {
                        // The distance is to be determined wrt the refList
                        // Here, col specs are NOT -1
                        currentDistance = getDistanceFromReferenceList(currentFirst, list1ColSpec, currentSecond,list2ColSpec, modRefArray, modRefWidth, fullRefArray, compi, maxDist);
                    }
                    else
                    {
                        // The normal word dist is to be used
                        // Meeting elements have distance 3-2=1, which is OK
                        currentDistance = currentSecond.getValueAt(list2ColSpec).getLeftmostDiscoursePosition()-currentFirst.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
                    }
                    if (currentDistance <= maxDist && currentDistance >= minDist)
                    {
                        // New: Dependent queries are never joiners
                        result.addSingleTuple(currentFirst);
                    }
                }
            }
        }        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }        
    }
        
    private final int getDistanceFromReferenceList(MMAX2QueryResultTuple tuple1,  int list1ColSpec, MMAX2QueryResultTuple tuple2, int list2ColSpec, MMAX2QueryResultTuple[] modRefArray, int modRefWidth, MMAX2QueryResultTuple[] fullRefArray, QueryResultTupleComparator compi, int upperLimit)
    {        
        // Distance is measured as number of intervening elements +1, such that
        // each element has distance 0 to itself. 
        
        // Note: This does not use discourse positions, so start and end of an expression is
        // irrelevant.
        
        // Note: This uses col spec -1 to assure that entire span of tuple is always taken
        // (However, we encourage that only 1-element tuples be used with this query anyway)
        
        // Actually, any col specs passed in from above will be ignored (?) So what about dependent
        // queries then??
        
        int firstIndex = -1;
        int secondIndex = -1;
        int distance = -1;             
                      
        boolean dependentParameters = false;
                        
        String levelAccessedByTuple1="";
        
        // Check if both col specs are -1, which is the case for non-dependent parameters
        if (list1ColSpec == -1 && list2ColSpec == -1)
        {        
            // Parameters are non-dependent
            // So check for correct width == 1
            // (in case of dependent parameters, both widths are 1 due to col specs)
            if ((tuple1.getWidth() == tuple2.getWidth()) && tuple1.getWidth() == 1)
            {   
                // Both input tuples are of width 1. Thats a necessity!
            }       
            else
            {
                System.err.println("Warning: Input tuple width mismatch, or width != 1!");            
                return distance;
            }

            levelAccessedByTuple1 = tuple1.getValueAt(0).getLevelName();
            // Check if both tuples come from the same level, using default index 0
            if (tuple2.getValueAt(0).getLevelName().equals(levelAccessedByTuple1))
            {   
                // Both input tuples are of the same level. Thats a necessity!
            }
            else
            {
                System.err.println("Warning: Input tuple level mismatch!");            
                return distance;
            }                           
        }
        else
        {                       
            // Parameters are dependent, so no check for correct width == 1 necessary
            dependentParameters = true;
            levelAccessedByTuple1 = tuple1.getValueAt(list1ColSpec).getLevelName();
            // This means that we have to use col specs for matching
            // Check if both come from the same level, using col spec columns
            if (tuple2.getValueAt(list2ColSpec).getLevelName().equals(levelAccessedByTuple1))
            {   
                // Both input tuples are of the same level. Thats a necessity!
            }
            else
            {
                System.err.println("Warning: Input tuple level mismatch!");            
                return distance;
            }
        }        

        // Now we know that both tuples are from the same level
        
        // Check if width of mod ref list == 1, which is a necessity
        if (modRefWidth != 1)
        {           
            System.err.println("Warning: modRefList width != 1 ("+modRefWidth+")");
            return distance;
        }
        
        // Now we know that modRefList is of width 1
                       
        
        // Check whether the ref list and the accessed tuples are from the same level
        //if (modRefArray[0].getValueAt(0).getLevelName().equals(tuple1.getValueAt(list1ColSpec).getLevelName()))
        
        // Use index 0 for array, because that has to be of width 1
        // Compare only to levelAccessedByTuple1, because that is known to be identical to level 2
        if (modRefArray[0].getValueAt(0).getLevelName().equals(levelAccessedByTuple1))
        {
            // ref level and tuple level are identical
            // e.g. /coref before:-10[/coref] /coref
            // That both tuples access the same level, we know already            
            // Get indices of tuples in FULL list
            if (dependentParameters==false)
            {
                firstIndex = Arrays.binarySearch(fullRefArray,tuple1,compi);
                secondIndex = Arrays.binarySearch(fullRefArray,tuple2,compi);
            }
            else
            {
                // The input parameters are dependent
                // So what we must find in reflist is not entire tuples, but only elements
                // at the col specs
                firstIndex = Arrays.binarySearch(fullRefArray,new MMAX2QueryResultTuple(tuple1.getValueAt(list1ColSpec)),compi);
                secondIndex = Arrays.binarySearch(fullRefArray,new MMAX2QueryResultTuple(tuple2.getValueAt(list2ColSpec)),compi);                
            }
            // Only if none of the indices is -1 or less, both were found
            if (firstIndex>=0 && secondIndex>=0)
            {
                // Both tuples were found in full list
                if (fullRefArray.length == modRefArray.length)
                {
                    // full and modRefList are of identical lengths
                    // This means that modRef was not modified at all
                    // Correct distance can be calculated like this
                    distance = secondIndex - firstIndex;
                }
                else
                {
                    // full and refModList are not identical in length
                    // So the distance has to be determined on the basis of those elements in between
                    // that are also in modRefList
                    
                    // Increment distance to get distance right later
                    distance = 1;
                    
                    // Iterate over all elements between indices
                    for (int b=firstIndex+1;b<secondIndex;b++)
                    {
                        // If the current *intervening* element is in modRefList, count that as distance
                        // Thus way, only those intervening elements that are also in mod ref list
                        // count for distance
                        if (Arrays.binarySearch(modRefArray,fullRefArray[b],compi)>=0)
                        {
                            distance++;
                            if (distance > upperLimit)
                            {
                                // Make sure that this is not counted as a hit, and enable break on inner loop
                                // This will return 'distance'
                                break;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            // ref level and tuple level are not identical
            // That both tuples access the same level, we know already            
            // Get level of elements the ref list comes from            
            String refLevelName = modRefArray[0].getValueAt(0).getLevelName();
            
            Markable refMarkable1 = null;
            Markable refMarkable2 = null;            
            
            if (dependentParameters == false)
            {
                refMarkable1 = chart.getMarkableLevelByName(refLevelName, false).getAllMarkablesAtDiscoursePosition(tuple1.getLeftmostDiscoursePosition())[0];
                refMarkable2 = chart.getMarkableLevelByName(refLevelName, false).getAllMarkablesAtDiscoursePosition(tuple2.getLeftmostDiscoursePosition())[0];            
            }
            else
            {
                refMarkable1 = chart.getMarkableLevelByName(refLevelName, false).getAllMarkablesAtDiscoursePosition(tuple1.getValueAt(list1ColSpec).getLeftmostDiscoursePosition())[0];
                refMarkable2 = chart.getMarkableLevelByName(refLevelName, false).getAllMarkablesAtDiscoursePosition(tuple2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition())[0];                            
            }
            
            MMAX2QueryResultTuple refTuple1 = new MMAX2QueryResultTuple(new MMAX2QueryResultTupleElement(refMarkable1));
            MMAX2QueryResultTuple refTuple2 = new MMAX2QueryResultTuple(new MMAX2QueryResultTupleElement(refMarkable2));            
            
            // Now, refTuple 1 and refTuple2 are the tuples belonging to the required input tuples
            // Get indices of both in full ref list
            firstIndex = Arrays.binarySearch(fullRefArray,refTuple1,compi);
            secondIndex = Arrays.binarySearch(fullRefArray,refTuple2,compi);
            
            // Only if none of the indices is -1 or less, both were found
            if (firstIndex>=0 && secondIndex>=0)
            {
                // Both tuples were found in full list
                if (fullRefArray.length == modRefArray.length)
                {
                    // full and modRefList are of identical lengths
                    // This means that modRef was not modified at all
                    // Correct distance can be calculated like this
                    distance = secondIndex - firstIndex;
                }
                else
                {
                    // full and refModList are not identical in length
                    // So the distance has to be determined on the basis of those elements in between
                    // that are also in modRefList
                    
                    // Increment distance to get distance right later
                    distance = 1;
                    
                    // Iterate over all elements between indices
                    for (int b=firstIndex+1;b<secondIndex;b++)
                    {
                        // If the current *intervening* element is in modRefList, count that as distance
                        // Thus way, only those intervening elements that are also in mod ref list
                        // count for distance
                        if (Arrays.binarySearch(modRefArray,fullRefArray[b],compi)>=0)
                        {
                            distance++;
                            if (distance > upperLimit)
                            {
                                // Make sure that this is not counted as a hit, and enable break on inner loop
                                // This will return 'distance'
                                break;
                            }
                        }
                    }
                }            
            }            
        }        
        return distance;       
    }
    
    
    public static boolean isInReferenceListBrackets(String inString, int pos)
    {
        boolean result = false;
        // Use pos -1 to count triggering ( correctly
        int nextBracketPos = getFollowingMatchingBracket(inString, "]",pos-1);
        int previousBracketPos = getPreceedingMatchingBracket(inString, "[",pos);
        if (nextBracketPos != inString.length() && previousBracketPos != 0)
        {
            result = true;
        }
        return result;
    }
    
    
    private final MMAX2QueryResultList[] executeEmbeddedSimplifiedQuery(String query)
    {
        // New: This will return an array of two lists:
        MMAX2QueryResultList[] result = new MMAX2QueryResultList[2];
        // At index 0 the modRefList, produced from the query as it was passed in,
        // and at index 1 the fullRefList, containing all elements from the level the modRefList accessed
        SimplifiedMMAXQLConverter convi = new SimplifiedMMAXQLConverter(chart,false);
        
        String conversionResult = convi.convertFromSimplifiedMMAXQL(query);
        // ConversionResult will contain at the top the name of the top level system var
        conversionResult = conversionResult.substring(conversionResult.indexOf(":")+1);
        // Create new querywindow to execute the embedded query in
        MMAX2QueryWindow tempWindow = new MMAX2QueryWindow(chart);
        
        // Create and store modRefList
        result[0] = tempWindow.executeQuery(conversionResult,false);
        
        if (result[0] == null)
        {
            // If the modefList could not be produced, quit here
            return null;
        }
        if (result[0].size()==0)
        {
            // If modList is empty, we cannot determine the level accessed.
            // But then using an empty modList does not make any sense
            return result;
        }
        // Determine level accessed by mod
        // Try to get the first tuple
        MMAX2QueryResultTuple t = result[0].getTupleAtIndex(0);
        if (t.getWidth()!=1)
        {
            // The ref list has more then one element, which is illegal
            // Do not do anythig here, this will be handled later
        }
        
        MMAX2QueryResultTupleElement e = t.getValueAt(0);
        // Get name of level accessed by query for modRefList
        String name = e.getLevelName();        
        conversionResult = convi.convertFromSimplifiedMMAXQL("/"+name);
        conversionResult = conversionResult.substring(conversionResult.indexOf(":")+1);
        result[1] = tempWindow.executeQuery(conversionResult,false);
        tempWindow = null;
        System.gc();
        return result;
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Before_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'before'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentTuple1 = null;
        MMAX2QueryResultTuple currentTuple2 = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Parameters are not dependent!
            // Iterate over all elements in first parameter list
            for (int a=0;a<list1Size;a++)
            {
                // Get current element in first parameter list
                // This is the left tuple in the 'meeting' pair, potentially complex
                currentTuple1 = list1.getTupleAtIndex(a);                

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;                                        

                // Rationale behind negated meets optimization:
                // Query tries to falsify assumption that no match exists. 
                // Optimization now skips those elements known a priori to not match
                if (doLookAheadOptimization)
                {
                    // Get disc pos at which left element of 'meeting' pair ends
                    int firstTuplesFinalDiscPos = currentTuple1.getRightmostDiscoursePosition();                    
                    // Get index of that element in list 2 that is the earliest candidate to meet element 1
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }                        
                }

                // Iterate over second parameter list, starting at first plausible candidate as determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)               
                {
                    // Get current element in second parameter list
                    currentTuple2 = list2.getTupleAtIndex(b);         

                    if (doBreakOptimization)
                    {
                        // New April 1st, 2005: Do some optimization                    
                        // meets is true if m2 starts at m1.end+1
                        // We assume that all markables are sorted by their first disc pos                    
                        if (currentTuple2.getLeftmostDiscoursePosition() > currentTuple1.getRightmostDiscoursePosition()+1)
                        {
                            // e2 starts after e1 ends+1, so elements after e2 can never meet e1
                            // anyMatch remains false
                            break;
                        }
                    }                                                
                    // Get match
                    anyMatch = MarkableHelper.before(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec);
                    // If match is true, break to false
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here any anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {   
                    // This is the independent case, in which always only the first is returned
                    // Add. The width of the result will be the same as currentTuple1
                    result.addSingleTuple(currentTuple1);
                }
                anyMatch=false;                     
            }
        }    
        else
        {
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentTuple1 = list1.getTupleAtIndex(a);                
                currentTuple2 = list2.getTupleAtIndex(a);
                if (MarkableHelper.before(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec)==false)
                {
                    // The negated condition matches for this dependent pair
                    if (useAsFilter)
                    {
                        // The filter is set explicitly, so return first only
                        result.addSingleTuple(currentTuple1);
                    }
                    else
                    {
                        // No filter is set explicitly, so return all
                        result.mergeAndAdd(currentTuple1,currentTuple2,Constants.AStart_BEnd);
                    }
                }
            }            
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    
    private final MMAX2QueryResultList executeRelationQuery_BeforeStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)    
    {
        // The required relation is 'before_strict'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potential first tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                
                int indexOfEarliestCandidateInSecondList = 0;
                
                if (doLookAheadOptimization)
                {
                    // Get pos at which current first ends.
                    // If it should be before currentSecond, that can start at its end, earliest
                    int firstTuplesFinalDiscPos = currentFirst.getRightmostDiscoursePosition();
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    if (indexOfEarliestCandidateInSecondList > 0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }
                
                // Iterate over all potential second tuples (second para)
                // starting at first plausible candidate determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentSecond = list2.getTupleAtIndex(b);
                    
                    // No BreakOpt possible, since everything after first is a match
                    if (MarkableHelper.beforeStrict(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentFirst);
                        }
                        else
                        {
                            result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_BEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                currentSecond = list2.getTupleAtIndex(a); 
                if (MarkableHelper.beforeStrict(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_BEnd);
                    }
                }
            }
        }        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }        
    }

    private final MMAX2QueryResultList executeRelationQuery_BeforeStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'before'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentTuple1 = null;
        MMAX2QueryResultTuple currentTuple2 = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Parameters are not dependent!
            // Iterate over all elements in first parameter list
            for (int a=0;a<list1Size;a++)
            {
                // Get current element in first parameter list
                // This is the left tuple in the 'meeting' pair, potentially complex
                currentTuple1 = list1.getTupleAtIndex(a);                

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;                                        

                // Rationale behind negated meets optimization:
                // Query tries to falsify assumption that no match exists. 
                // Optimization now skips those elements known a priori to not match
                if (doLookAheadOptimization)
                {
                    // Get disc pos at which left element of 'meeting' pair ends
                    int firstTuplesFinalDiscPos = currentTuple1.getRightmostDiscoursePosition();                    
                    // Get index of that element in list 2 that is the earliest candidate to meet element 1
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesFinalDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }                        
                }

                // Iterate over second parameter list, starting at first plausible candidate as determined above
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)               
                {
                    // Get current element in second parameter list
                    currentTuple2 = list2.getTupleAtIndex(b);         

                    if (doBreakOptimization)
                    {
                        // New April 1st, 2005: Do some optimization                    
                        // meets is true if m2 starts at m1.end+1
                        // We assume that all markables are sorted by their first disc pos                    
                        if (currentTuple2.getLeftmostDiscoursePosition() > currentTuple1.getRightmostDiscoursePosition()+1)
                        {
                            // e2 starts after e1 ends+1, so elements after e2 can never meet e1
                            // anyMatch remains false
                            break;
                        }
                    }                                                
                    // Get match
                    anyMatch = MarkableHelper.beforeStrict(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec);
                    // If match is true, break to false
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here any anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {   
                    // This is the independent case, in which always only the first is returned
                    // Add. The width of the result will be the same as currentTuple1
                    result.addSingleTuple(currentTuple1);
                }
                anyMatch=false;                     
            }
        }    
        else
        {
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentTuple1 = list1.getTupleAtIndex(a);                
                currentTuple2 = list2.getTupleAtIndex(a);
                if (MarkableHelper.beforeStrict(currentTuple1,currentTuple2,list1ColSpec,list2ColSpec)==false)
                {
                    // The negated condition matches for this dependent pair
                    if (useAsFilter)
                    {
                        // The filter is set explicitly, so return first only
                        result.addSingleTuple(currentTuple1);
                    }
                    else
                    {
                        // No filter is set explicitly, so return all
                        result.mergeAndAdd(currentTuple1,currentTuple2,Constants.AStart_BEnd);
                    }
                }
            }            
        }
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Contains(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'contains' (dom)
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentContainer = null;
        MMAX2QueryResultTuple currentContained = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)        
            for (int a=0;a<list1Size;a++)
            {
                // Get entire tuple here
                currentContainer = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for contained element: It must start earliest at container start
                    // Get disc pos at which container starts
                    int firstTuplesInitialDiscPos = currentContainer.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be contained by currentContainer
                    // Note: This will return a smaller than the actual value, to be on the safe side
                    // Maximally 5 redundant positions will be visited
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }

                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentContained = list2.getTupleAtIndex(b);
                    
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        // Minimal condition for contained: It must start before container ends
                        if (currentContained.getLeftmostDiscoursePosition() > currentContainer.getRightmostDiscoursePosition())
                        {                   
                            // The contained element starts after the end of the current potential container
                            // Since all elements following potential contained start even later,
                            // none of them can be contained in container.
                            // Do not look at next currentContained
                            break;
                        }
                    }                        

                    if (MarkableHelper.during(currentContained,currentContainer,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentContainer);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A dom B is A, i.e. the larger
                            System.err.println(currentContainer.toString(null)+" contains "+currentContained.toString(null));
                            result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 
                currentContained = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.during(currentContained,currentContainer,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentContainer);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                    }
                }
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }

    
    private final MMAX2QueryResultList executeRelationQuery_Overlaps_right(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'overlaps_right'
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentOverlapper = null;
        MMAX2QueryResultTuple currentOverlapped = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)        
            for (int a=0;a<list1Size;a++)
            {
                // Get entire tuple here
                currentOverlapper = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;
                
                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentOverlapped = list2.getTupleAtIndex(b);
                    if (MarkableHelper.overlaps_right(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentOverlapper);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A olr B is A+B, i.e. the sum
                            result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_BEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentOverlapper = list1.getTupleAtIndex(a); 
                currentOverlapped = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.overlaps_right(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentOverlapper);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_BEnd);
                    }
                }
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Overlaps_left(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'overlaps_left'
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentOverlapper = null;
        MMAX2QueryResultTuple currentOverlapped = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)        
            for (int a=0;a<list1Size;a++)
            {
                // Get entire tuple here
                currentOverlapper = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;
                
                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentOverlapped = list2.getTupleAtIndex(b);
                    if (MarkableHelper.overlaps_left(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentOverlapper);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A olr B is A+B, i.e. the sum
                            result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_BEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentOverlapper = list1.getTupleAtIndex(a); 
                currentOverlapped = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.overlaps_left(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentOverlapper);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_BEnd);
                    }
                }
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }    
    
    private final MMAX2QueryResultList executeRelationQuery_Equals(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'equals'
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all first tuples (first para)        
            for (int a=0;a<list1Size;a++)
            {
                // Get entire tuple, use col specs only for actual matching
                currentFirst = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList=0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for second element: It must start earliest at first start
                    // Get disc pos at which first starts
                    int firstTuplesInitialDiscPos = currentFirst.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be identical with currentFirst
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }
                // Iterate over all potentially equal tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentSecond = list2.getTupleAtIndex(b);
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        if (currentSecond.getLeftmostDiscoursePosition() > currentFirst.getRightmostDiscoursePosition())
                        {                                
                            break;
                        }
                    }                        

                    if (MarkableHelper.equals(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                    {                        
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentFirst);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // since both are the same size anyway
                            result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                currentSecond = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.equals(currentFirst,currentSecond,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd);
                    }
                }
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }    
    
    private final MMAX2QueryResultList executeRelationQuery_Equals_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'equals'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        useAsFilter=true;
        
        if (!dependentParameters)
        {
            // Iterate over all first elements (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get first element (from first list)
                currentFirst = list1.getTupleAtIndex(a);

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for second element: It must start earliest at container start
                    // Get disc pos at which first starts
                    int firstTuplesInitialDiscPos = currentFirst.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be identical with currentFirst
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }
                                                
                // Iterate over all potentially embedding tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentSecond = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.

                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentSecond.getLeftmostDiscoursePosition() > currentFirst.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            // anyMatch will remain false
                            break;
                        }
                    }                                             

                    anyMatch = MarkableHelper.equals(currentFirst,currentSecond,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentFirst);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // useAsFilter has been set to true already
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a);
                currentSecond = list2.getTupleAtIndex(a);
                if (MarkableHelper.equals(currentFirst,currentSecond,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd); 
                    }
                }
            }       
        } 
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else         
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
        

    private final MMAX2QueryResultList executeRelationQuery_Spans(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'equals'
        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all first tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                // Get entire tuple, use col specs only for actual matching
                currentFirst = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList=0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for second element: It must start earliest at first start
                    // Get disc pos at which first starts
                    int firstTuplesInitialDiscPos = currentFirst.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be identical with currentFirst
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }
                // Iterate over all potentially equal tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentSecond = list2.getTupleAtIndex(b);
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        if (currentSecond.getLeftmostDiscoursePosition() > currentFirst.getRightmostDiscoursePosition())
                        {                                
                            break;
                        }
                    }                        

                    if (MarkableHelper.spans(currentFirst,currentSecond,list1ColSpec,list2ColSpec))
                    {                        
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentFirst);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // since both are the same size anyway
                            result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a); 
                currentSecond = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.spans(currentFirst,currentSecond,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd);
                    }
                }
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }    
    
    private final MMAX2QueryResultList executeRelationQuery_Spans_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'equals'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentFirst = null;
        MMAX2QueryResultTuple currentSecond = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        useAsFilter=true;
        
        if (!dependentParameters)
        {
            // Iterate over all first elements (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get first element (from first list)
                currentFirst = list1.getTupleAtIndex(a);

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for second element: It must start earliest at container start
                    // Get disc pos at which first starts
                    int firstTuplesInitialDiscPos = currentFirst.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be identical with currentFirst
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }
                                                
                // Iterate over all potentially embedding tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentSecond = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.

                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentSecond.getLeftmostDiscoursePosition() > currentFirst.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            // anyMatch will remain false
                            break;
                        }
                    }                                             

                    anyMatch = MarkableHelper.spans(currentFirst,currentSecond,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentFirst);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // useAsFikter has been set to true already
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentFirst = list1.getTupleAtIndex(a);
                currentSecond = list2.getTupleAtIndex(a);
                if (MarkableHelper.spans(currentFirst,currentSecond,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentFirst);
                    }
                    else
                    {
                        result.mergeAndAdd(currentFirst,currentSecond,Constants.AStart_AEnd); 
                    }
                }
            }       
        } 
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else         
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }

    
    private final MMAX2QueryResultList executeRelationQuery_Contains_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // The required relation is negated 'contains'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentContainer = null;
        MMAX2QueryResultTuple currentContained = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for contained element: It must start earliest at container start
                    // Get disc pos at which container starts
                    int firstTuplesInitialDiscPos = currentContainer.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be contained by currentContainer
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                }                                        

                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentContained = list2.getTupleAtIndex(b);

                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        // Minimal condition for contained: It must start before container ends
                        if (currentContained.getLeftmostDiscoursePosition() > currentContainer.getRightmostDiscoursePosition())
                        {                                
                            // The contained element starts after the end of the current potential container
                            // Since all elements following potential contained start even later,
                            // none of them can be contained in container.
                            // Do not look at next currentContained
                            break;
                        }
                    }                                                                                                
                    anyMatch = MarkableHelper.during(currentContained,currentContainer,list2ColSpec,list1ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentContainer
                    result.addSingleTuple(currentContainer);
                }
                anyMatch=false;                                         
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 
                currentContained = list2.getTupleAtIndex(a); 
                if (MarkableHelper.during(currentContained,currentContainer,list2ColSpec,list1ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentContainer);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                    }
                }
            }            
        }
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }

    }
    
    private final MMAX2QueryResultList executeRelationQuery_ContainsStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!        
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'contains_strict' (DOM)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentContainer = null;
        MMAX2QueryResultTuple currentContained = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for contained element: It must start earliest at container start
                    // Get disc pos at which container starts
                    int firstTuplesInitialDiscPos = currentContainer.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be contained by currentContainer
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }
                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentContained = list2.getTupleAtIndex(b);

                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        // Minimal condition for contained: It must start before container ends
                        if (currentContained.getLeftmostDiscoursePosition() > currentContainer.getRightmostDiscoursePosition())
                        {                                 
                            // The contained element starts after the end of the current potential container
                            // Since all elements following potential contained start even later,
                            // none of them can be contained in container.
                            // Do not look at next currentContained
                            break;
                        }
                    }                        

                    if (MarkableHelper.duringStrict(currentContained,currentContainer,list2ColSpec,list1ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentContainer);
                        }
                        else
                        {                                                
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A DOM B is A, i.e. the larger
                            result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 
                currentContained = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.duringStrict(currentContained,currentContainer,list2ColSpec,list1ColSpec))
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentContainer);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                    }
                }                                    
            }
        }                        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }        
    }
    
    private final MMAX2QueryResultList executeRelationQuery_ContainsStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'contains_strict'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentContainer = null;
        MMAX2QueryResultTuple currentContained = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially containing tuples (first para)
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 

                // Declare here to have available also in case of non-optimization
                int indexOfEarliestCandidateInSecondList =0;

                if (doLookAheadOptimization)
                {
                    // Minimal condition for contained element: It must start earliest at container start
                    // Get disc pos at which container starts
                    int firstTuplesInitialDiscPos = currentContainer.getLeftmostDiscoursePosition();

                    // Get index of that element in list 2 that is the earliest candidate to 
                    // be contained by currentContainer
                    indexOfEarliestCandidateInSecondList = list2.getElementIndexBeforeDiscoursePosition(firstTuplesInitialDiscPos);
                    // Be on the safe side
                    if (indexOfEarliestCandidateInSecondList>0)
                    {
                        indexOfEarliestCandidateInSecondList--;
                    }
                }                                        

                // Iterate over all potentially contained tuples (second para)
                for (int b=indexOfEarliestCandidateInSecondList;b<list2Size;b++)
                {
                    currentContained = list2.getTupleAtIndex(b);

                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize
                        // Minimal condition for contained: It must start before container ends
                        if (currentContained.getLeftmostDiscoursePosition() > currentContainer.getRightmostDiscoursePosition())
                        {                                
                            // The contained element starts after the end of the current potential container
                            // Since all elements following potential contained start even later,
                            // none of them can be contained in container.
                            // Do not look at next currentContained
                            break;
                        }
                    }                        

                    anyMatch = MarkableHelper.duringStrict(currentContained,currentContainer,list2ColSpec,list1ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {                                          
                    // Add. The width of the result will be the same as currentContainer
                    result.addSingleTuple(currentContainer);
                }
                anyMatch=false;                                         
            }
        }
        else
        {
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentContainer = list1.getTupleAtIndex(a); 
                currentContained = list2.getTupleAtIndex(a); 
                
                if (MarkableHelper.duringStrict(currentContained,currentContainer,list2ColSpec,list1ColSpec)==false)
                {
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentContainer);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A dom B is A, i.e. the larger
                        result.mergeAndAdd(currentContainer,currentContained,Constants.AStart_AEnd);
                    }
                }                
            }            
        }
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_During(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {        
        // New April 25th: Dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'during', (in)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEmbedded = null;
        MMAX2QueryResultTuple currentEmbedder = null;
        //boolean match = false;
        int list1Size = list1.size();
        int list2Size = list2.size();
                
        // Only lists with col specs can be dependent, so *independent* ones never have colspecs
        if (!dependentParameters)
        {    
            // Iterate over all potentially smaller, embedded tuples (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentEmbedded = list1.getTupleAtIndex(a);

                // Lookahead optimization not possible for during, because neither start 
                // nor end of embedded does give a clue to start of embedder.

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentEmbedder = list2.getTupleAtIndex(b);   

                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.
                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentEmbedder.getLeftmostDiscoursePosition() > currentEmbedded.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            break;
                        }
                    }
                    // Colspecs are always -1 here, because accessed tuples are independent
                    if (MarkableHelper.during(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            // Since relation is to be used as filter, keep first para only
                            result.addSingleTuple(currentEmbedded);                                
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A in B is A, i.e. the smaller
                            result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // Dependent parameters
            // Iterate over first list
            for (int a=0;a<list1Size;a++)
            {
                currentEmbedded = list1.getTupleAtIndex(a);
                currentEmbedder = list2.getTupleAtIndex(a);
                if (MarkableHelper.during(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec))
                {
                    if (useAsFilter)
                    {
                        // Since relation is to be used as filter, keep first para only
                        result.addSingleTuple(currentEmbedded);
                    }
                    else
                    {
                        // Merge and add. The result width will be from AStart to AEnd.
                        // i.e. the result span of A in B is A, i.e. the smaller
                        result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                    }
                }                
            }
        }// end dependent
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_During_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'during'        
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentEmbedded = null;
        MMAX2QueryResultTuple currentEmbedder = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Iterate over all potentially smaller, embedded tuples (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentEmbedded = list1.getTupleAtIndex(a);

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentEmbedder = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.

                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentEmbedder.getLeftmostDiscoursePosition() > currentEmbedded.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            // anyMatch will remain false
                            break;
                        }
                    }                                             

                    anyMatch = MarkableHelper.during(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentEmbedded);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentEmbedded = list1.getTupleAtIndex(a);
                currentEmbedder = list2.getTupleAtIndex(a);
                if (MarkableHelper.during(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEmbedded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                    }
                }
            }       
        } 
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Overlaps_right_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'overlaps_right'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentOverlapper = null;
        MMAX2QueryResultTuple currentOverlapped = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Iterate over all potentially smaller, embedded tuples (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentOverlapper = list1.getTupleAtIndex(a);

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentOverlapped = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    anyMatch = MarkableHelper.overlaps_right(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentOverlapper);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentOverlapper = list1.getTupleAtIndex(a);
                currentOverlapped = list2.getTupleAtIndex(a);
                if (MarkableHelper.overlaps_right(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentOverlapper);
                    }
                    else
                    {
                        result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_AEnd); 
                    }
                }
            }       
        } 
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }
    
    private final MMAX2QueryResultList executeRelationQuery_Overlaps_left_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'overlaps_left'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentOverlapper = null;
        MMAX2QueryResultTuple currentOverlapped = null;
        int list1Size = list1.size();
        int list2Size = list2.size();
        if (!dependentParameters)
        {
            // Iterate over all potentially smaller, embedded tuples (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentOverlapper = list1.getTupleAtIndex(a);

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentOverlapped = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    anyMatch = MarkableHelper.overlaps_left(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentOverlapper);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentOverlapper = list1.getTupleAtIndex(a);
                currentOverlapped = list2.getTupleAtIndex(a);
                if (MarkableHelper.overlaps_left(currentOverlapper,currentOverlapped,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentOverlapper);
                    }
                    else
                    {
                        result.mergeAndAdd(currentOverlapper,currentOverlapped,Constants.AStart_AEnd); 
                    }
                }
            }       
        } 
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }
    }

    
    private final MMAX2QueryResultList executeRelationQuery_DuringStrict(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {                
        // New April 25th: dependent parameters
        // New April 24th, 2005: If useAsFilter, duplicates are removed!                
        // Optimization OK April 15th, 2005
        // filter-support ok April 20th, 2005
        // The required relation is 'during_strict'
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        MMAX2QueryResultTuple currentEmbedded = null;
        MMAX2QueryResultTuple currentEmbedder = null;
        
        int list1Size = list1.size();
        int list2Size = list2.size();
        
        if (!dependentParameters)
        {
            // Iterate over all potentially smaller, embedded tuples (first para)
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentEmbedded = list1.getTupleAtIndex(a);

                // Lookahead optimization not possible for during_strict, because neither start nor end of embedded 
                // does give a clue to start of embedder.

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentEmbedder = list2.getTupleAtIndex(b);   

                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.

                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentEmbedder.getLeftmostDiscoursePosition() > currentEmbedded.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            break;
                        }
                    }
                    if (MarkableHelper.duringStrict(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec))
                    {
                        if (useAsFilter)
                        {
                            result.addSingleTuple(currentEmbedded);
                        }
                        else
                        {
                            // Merge and add. The result width will be from AStart to AEnd.
                            // i.e. the result span of A IN B is A, i.e. the smaller
                            result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                        }
                    }
                }
            }
        }
        else
        {
            useAsFilter=true;
            // dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentEmbedded = list1.getTupleAtIndex(a);
                currentEmbedder = list2.getTupleAtIndex(a);
                if (MarkableHelper.duringStrict(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec))
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEmbedded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                    }
                }                
            }
        }
        
        if (useAsFilter)
        {
            return removeDuplicates(result);
        }
        else
        {
            return result;
        }        
    }    
    
    private final MMAX2QueryResultList executeRelationQuery_DuringStrict_Negated(MMAX2QueryResultList list1, int list1ColSpec, MMAX2QueryResultList list2, int list2ColSpec, boolean useAsFilter, boolean dependentParameters)
    {
        // The required relation is negated 'during_strict'
        // Iterate over all potentially smaller, embedded tuples (first para)
        MMAX2QueryResultList result = new MMAX2QueryResultList();
        boolean anyMatch = false;
        MMAX2QueryResultTuple currentEmbedded = null;
        MMAX2QueryResultTuple currentEmbedder = null;
        int list1Size = list1.size();
        int list2Size = list2.size();

        if (!dependentParameters)
        {
            for (int a=0;a<list1Size;a++)
            {                
                // Get (potentially smaller) embedded element (from first list)
                currentEmbedded = list1.getTupleAtIndex(a);

                // Iterate over all potentially embedding tuples (second para)
                for (int b=0;b<list2Size;b++)
                {
                    // Get (potentially larger) embedding element (from second list)
                    currentEmbedder = list2.getTupleAtIndex(b);   

                    // Rationale behind optimizing negated during_strict:
                    // Query tries to falsify the assumption that there is no match
                    // Optimization now skips those elements known a priori to not match
                    if (doBreakOptimization)
                    {
                        // Optimize: Since both lists are sorted at index 0, we can optimize.
                    
                        // Minimal condition for embedder: It must start *at latest* at the start of embedded,
                        // or earlier. So we can break the embedder search if the current embedder starts
                        // after the current embedded starts. 
                        if (currentEmbedder.getLeftmostDiscoursePosition() > currentEmbedded.getLeftmostDiscoursePosition())
                        {
                            // Do not look at following embedders, which will all start after current embedder
                            // anyMatch will remain false
                            break;
                        }
                    }                                                                     

                    anyMatch = MarkableHelper.duringStrict(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec);
                    if (anyMatch)
                    {
                        break;
                    }
                }
                // When we came here and anymatch is still false, the current t1 is a match
                if (anyMatch==false)
                {      
                    // Add. The width of the result will be the same as currentEmbedded
                    result.addSingleTuple(currentEmbedded);
                }
                anyMatch=false;                     
            }
        }
        else
        {
            // Dependent parameters
            for (int a=0;a<list1Size;a++)
            {
                currentEmbedded = list1.getTupleAtIndex(a);
                currentEmbedder = list2.getTupleAtIndex(a);
                if (MarkableHelper.duringStrict(currentEmbedded,currentEmbedder,list1ColSpec,list2ColSpec)==false)
                {
                    // The current pair of dependent elements is a match
                    if (useAsFilter)
                    {
                        result.addSingleTuple(currentEmbedded);
                    }
                    else
                    {
                        result.mergeAndAdd(currentEmbedded,currentEmbedder,Constants.AStart_AEnd); 
                    }
                }
            }
        }
        
        if (dependentParameters==false)
        {
            // If the parameters were independent, remove duplicates from 1-column result of negated query
            return removeDuplicates(result);
        }
        else if (useAsFilter==true)
        {
            // If parameters were dependent, remove only if filter has been set explicitly
            return removeDuplicates(result);
        }
        else
        {
            // if parameters are dependent and no filter, do not remove duplicates
            return result;
        }

    }   
    
    private final String createBasicStatisticsForSetCollection(MMAX2QueryResultList tuples, ArrayList attributesToConsider)
    {
        HashMap lengthHash = new HashMap();
        HashMap distHash = new HashMap();
        
        int minLen = Integer.MAX_VALUE;
        int maxLen = Integer.MIN_VALUE;
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;
        int totalLength=0;
        int totalDist=0;
        int totalLinks = 0;
        String statistic="";        
        
        // Iterate over all tuples, each of which represents a markable_set
        for (int z=0;z<tuples.size();z++)
        {
            // Get current tuple
            MMAX2QueryResultTuple currentTuple = (MMAX2QueryResultTuple)tuples.get(z);
            int currentValidLength = currentTuple.getWidth();
            
            totalLength = totalLength+currentValidLength;
            // Update min/max lengths
            if (currentValidLength > maxLen)
            {
                maxLen = currentValidLength;
            }
            if (currentValidLength < minLen)
            {
                minLen = currentValidLength;
            }
            // Do total count of set lengths
            Integer countSoFar = (Integer) lengthHash.get(currentValidLength+"");
            if (countSoFar == null)
            {
                lengthHash.put(currentValidLength+"",new Integer(1));
            }
            else
            {
                lengthHash.put(currentValidLength+"",new Integer(countSoFar.intValue()+1));
            }
            int currentValidDistance = 0;
            // Iterate from first to last-1
            for (int v=0;v<currentTuple.getWidth()-1;v++)
            {
                if (currentTuple.getValueAt(v)==null)
                {
                    break;
                }
                totalLinks++;
                currentValidDistance = MarkableHelper.getDistanceInDiscoursePositions(currentTuple.getValueAt(v),currentTuple.getValueAt(v+1));
                totalDist = totalDist+currentValidDistance;
                // Update min/max distance
                if (currentValidDistance > maxDistance)
                {
                    maxDistance = currentValidDistance;
                }
                if (currentValidDistance < minDistance)
                {
                    minDistance = currentValidDistance;
                }
                Integer distSoFar = (Integer) distHash.get(currentValidDistance+"");
                if (distSoFar == null)
                {
                    distHash.put(currentValidDistance+"",new Integer(1));
                }
                else
                {
                    distHash.put(currentValidDistance+"",new Integer(distSoFar.intValue()+1));
                }                
            }                                    
        }
        statistic="No. of sets: "+tuples.size();
        statistic = statistic+"\nMin. set size: "+minLen;
        statistic = statistic+"\nMax. set size: "+maxLen;
        statistic = statistic+"\nAvg. set size: "+(double)(totalLength/tuples.size());
        
        statistic = statistic+"\nSet size distribution: Size Freq.\n";                                                                   
        // Get array of all different set sizes
        String[] allLengths = (String[])lengthHash.keySet().toArray(new String[0]);
        int[] allLengthsNumerical = new int[allLengths.length];
        for (int z=0;z<allLengths.length;z++)
        {
            allLengthsNumerical[z]=new Integer((allLengths[z])).intValue();
        }
        java.util.Arrays.sort(allLengthsNumerical);
        for (int t=0;t<allLengthsNumerical.length;t++)
        {
            String temp = allLengthsNumerical[t]+"";
            statistic=statistic+"                       "+padLeft(temp, 4, " ")+"  "+padLeft(((Integer)lengthHash.get(temp))+"",4, " ")+"\n";
        }
        
        statistic = statistic+"\nMin. markable distance(*): "+minDistance;
        statistic = statistic+"\nMax. markable distance(*): "+maxDistance;
        statistic = statistic+"\nAvg. markable distance(*): "+(double)(totalDist/totalLinks);        
        
        statistic = statistic+"\nDistance distribution: Dist. Freq.\n";                                                                   
        // Get array of all different link distances
        String[] allDistances = (String[])distHash.keySet().toArray(new String[0]);
        int[] allDistancesNumerical = new int[allDistances.length];
        for (int z=0;z<allDistances.length;z++)
        {
            allDistancesNumerical[z]=new Integer((allDistances[z])).intValue();
        }
        java.util.Arrays.sort(allDistancesNumerical);
        for (int t=0;t<allDistancesNumerical.length;t++)
        {
            String temp = allDistancesNumerical[t]+"";
            statistic=statistic+"                        "+padLeft(temp, 4, " ")+"  "+padLeft(((Integer)distHash.get(temp))+"",4, " ")+"\n";
        }

        statistic = statistic+"\n\n(*) in discourse positions, counted from markable start\n";
        return statistic;
    }
       
    
    private final String createBasicStatistics(MMAX2QueryResultList tuples)
    {        
        
        String stats = "";
        if (tuples == null)
        {
            return stats;
        }
        // Get list of names to consider (all are used if this is empty)
        ArrayList attributesToConsider = tuples.getAttributeNamesToDisplay();
        
        
        if (tuples.getCommand().equalsIgnoreCase("all_markable_sets"))
        {
            stats = createBasicStatisticsForSetCollection(tuples, attributesToConsider);
            return stats;
        }
        
        int totalNumberOfElements = tuples.size();
        
        int index = tuples.getIndexToUse();
        int maxValueLen = 0;
        int maxLen = 25;
        int minLen = 10;
        HashMap allAttributeHashes = new HashMap();
        HashMap namesToAttributeObjectsHash = new HashMap();
        MMAX2QueryResultTupleElement currentElement = null;
        Markable currentMarkable = null;
        ArrayList tempList = new ArrayList();
        
        // Iterate over all supplied tuples once
        for (int z=0;z<tuples.size();z++)
        {
            // Get current Markable at index index
            currentElement = ((MMAX2QueryResultTuple)tuples.get(z)).getValueAt(index);
            if (currentElement.isMarkable()==false)
            {
                return "Cannot create statistics for basedata elements!";
            }
            // Get current Attributes
            // This is now certain to also return default attributes, due to initial validation prompt!
            // m.getAttributes does not work here because we need the attrib's type as well !!
            
            // NEW: Do not use Attribute Window any more
            HashMap tempAttributes = currentElement.getAttributes();
            Iterator attribNames = tempAttributes.keySet().iterator();
            MMAX2Attribute[] attribs = new MMAX2Attribute[tempAttributes.size()];

            int w =0;
            currentMarkable = currentElement.getMarkable();
            while(attribNames.hasNext())
            {
                String temp =(String)attribNames.next();
                attribs[w] = currentMarkable.getMarkableLevel().getCurrentAnnotationScheme().getUniqueAttributeByName("^"+temp+"$");
                w++;
            }
                        
            // Iterate over all attributes
            for (int b=0;b<attribs.length;b++)
            {
                if (attribs[b]==null)
                {
                    continue;
                }
                if (tempList.contains(attribs[b].getLowerCasedAttributeName())==false)
                {
                    if (attributesToConsider == null || attributesToConsider.contains(attribs[b].getLowerCasedAttributeName()))
                    {
                        // Collect all different attribute names in list
                        tempList.add(attribs[b].getLowerCasedAttributeName());
                        // Create mapping from name to attribute object 
                        namesToAttributeObjectsHash.put(attribs[b].getLowerCasedAttributeName(),attribs[b]);
                    }
                }
            }
        }          
        
        
        // Arrayfy and sort alphabetically to ensure correct display order
        String[] allDefinedNames= (String[])tempList.toArray(new String[0]);                
        java.util.Arrays.sort(allDefinedNames);
        
        // Iterate over all supplied tuples again
        for (int z=0;z<tuples.size();z++)
        {
            // Get current Markable at index index
            // This is safe since we know it to be markable here
            currentMarkable = ((MMAX2QueryResultTuple)tuples.get(z)).getValueAt(index).getMarkable();
            String currentAttributeName = "";
            String currentAttributeValue = "";
            
            // Iterate over all attribute names from array (in alpha order now)
            for (int b=0;b<allDefinedNames.length;b++)
            {
                // Get next attribute name
                currentAttributeName = allDefinedNames[b];
                currentAttributeValue = currentMarkable.getAttributeValue(currentAttributeName,"* na *");
                // Store length of longest attribute value
                if (currentAttributeValue.length() > maxValueLen)
                {
                    maxValueLen = currentAttributeValue.length();
                }
                // Get HashMap to which all values for current attribute have been mapped so far, if any
                HashMap tempAttributeHash = (HashMap) allAttributeHashes.get(currentAttributeName);
                if (tempAttributeHash == null)
                {
                    // If attribute did not ocur yet, create new hash map
                    tempAttributeHash = new HashMap();
                }
                // Get hashmap of all values that ocurred for current attribute yet
                Integer tempValueFreq = (Integer)tempAttributeHash.get(currentAttributeValue);                
                if (tempValueFreq == null)
                {                    
                    // The current value did not yet ocur
                    tempAttributeHash.put(currentAttributeValue,new Integer(1));
                }
                else
                {
                    tempAttributeHash.put(currentAttributeValue,new Integer(tempValueFreq.intValue()+1));
                }   
                allAttributeHashes.put(currentAttributeName, tempAttributeHash);
            }            
        }
        
        // Cut off if longest attribute name is longer than max allowed length
        if (maxValueLen > maxLen) maxValueLen = maxLen;
        if (maxValueLen < minLen) maxValueLen = minLen;
        
        // Create output string        
        for (int q=0;q<allDefinedNames.length;q++)
        {            
            ArrayList allLinesForCurrentAttribute = null;
            if (((MMAX2Attribute)namesToAttributeObjectsHash.get(allDefinedNames[q])).getType()==AttributeAPI.NOMINAL_LIST ||
                ((MMAX2Attribute)namesToAttributeObjectsHash.get(allDefinedNames[q])).getType()==AttributeAPI.NOMINAL_BUTTON)
            {
                allLinesForCurrentAttribute = createBasicStatisticsForNominalAttribute(allDefinedNames[q], namesToAttributeObjectsHash, maxValueLen, allAttributeHashes, totalNumberOfElements);
            }
            else if (((MMAX2Attribute)namesToAttributeObjectsHash.get(allDefinedNames[q])).getType()==AttributeAPI.MARKABLE_SET)
            {
                allLinesForCurrentAttribute = createBasicStatisticsForSetAttribute(allDefinedNames[q], namesToAttributeObjectsHash, maxValueLen, allAttributeHashes, totalNumberOfElements);
            }
            else if (((MMAX2Attribute)namesToAttributeObjectsHash.get(allDefinedNames[q])).getType()==AttributeAPI.MARKABLE_POINTER)
            {
                allLinesForCurrentAttribute = new ArrayList();
            }
            else if (((MMAX2Attribute)namesToAttributeObjectsHash.get(allDefinedNames[q])).getType()==AttributeAPI.FREETEXT)
            {
                allLinesForCurrentAttribute = createBasicStatisticsForFreetextAttribute(allDefinedNames[q], namesToAttributeObjectsHash, maxValueLen, allAttributeHashes, totalNumberOfElements);
            }
            else
            {
                System.err.println("Error: Unknown attribute type!");
                allLinesForCurrentAttribute = new ArrayList();
            }
           
            
            for (int n=0;n<allLinesForCurrentAttribute.size();n++)
            {
                stats = stats + (String)allLinesForCurrentAttribute.get(n)+"\n";
            }
        }        
        return stats;
    }

    private final ArrayList createBasicStatisticsForNominalAttribute(String currentAttribute, HashMap namesToAttributeObjectsHash, int maxValueLen, HashMap allAttributeHashes, int totalNumberOfMarkables)
    {
        ArrayList allLinesForCurrentAttribute = new ArrayList();            
        int freqSum = 0;
        String sep = " "+padRight("",maxValueLen+1," ");
            
        // Create attribute table header
        sep = sep +"|    N(all): |   %(all): ||    N(app): |   %(app): ||";
        // Add first line
        allLinesForCurrentAttribute.add(" "+currentAttribute.toUpperCase()+ " "+((MMAX2Attribute)namesToAttributeObjectsHash.get(((String)currentAttribute.toLowerCase()))).decodeAttributeType());
        // Add separator
        allLinesForCurrentAttribute.add(sep);        
        // Get all value counts for current attribute
        HashMap allValues = (HashMap)allAttributeHashes.get(currentAttribute);            
        Iterator allAttributeValues = allValues.keySet().iterator();
        int thisAttributesNACases = 0;
        // Determine number of nas in advance
        while(allAttributeValues.hasNext())
        {
            String currentValue = (String) allAttributeValues.next();
            if (currentValue.equals("* na *"))
            {
                thisAttributesNACases = ((Integer)allValues.get(currentValue)).intValue();
            }
        }
        allAttributeValues = null;
        allAttributeValues = allValues.keySet().iterator();            
        while(allAttributeValues.hasNext())
        {
            String tempLine = "";
            // Get current value
            String currentValue = (String) allAttributeValues.next();                
            // Make copy
            String currentValue2 = new String(currentValue);
            // Get count for current value
            int currentFreq = ((Integer)allValues.get(currentValue)).intValue();
            // Sum over all freqs
            freqSum = freqSum + currentFreq;
            // Pad value to make it aligned
            currentValue=padRight(currentValue,maxValueLen," ");
            String currentAllFreq = padLeft(currentFreq+"",10," ");
            String currentAllPercentage = getPercentageString(currentFreq,totalNumberOfMarkables,7);
            String currentAppFreq = "**********";
            if (currentValue2.equals("* na *")==false)
            {
                currentAppFreq = currentAllFreq;
            }
            String currentAppPercentage = "*********";
            if (currentValue2.equals("* na *")==false)
            {
                currentAppPercentage = getPercentageString(currentFreq,totalNumberOfMarkables-thisAttributesNACases,7);
            }                 
            tempLine = " "+currentValue+" | "+currentAllFreq+" | "+currentAllPercentage+" || "+currentAppFreq+ " | "+currentAppPercentage+" ||";
            allLinesForCurrentAttribute.add(tempLine);
        }  

        allLinesForCurrentAttribute.add(" "+padLeft("",maxValueLen+51,"-")+" ||");
        String currentAllFreqSum = padLeft(totalNumberOfMarkables+"", 10, " ");
        String currentAllPercentageSum = "100.00000";
        String currentAppFreqSum = padLeft(totalNumberOfMarkables-thisAttributesNACases+"", 10, " ");
        String currentAppPercentageSum = getPercentageString(freqSum-thisAttributesNACases,totalNumberOfMarkables,7);
        allLinesForCurrentAttribute.add(" "+padRight("N/% of all",maxValueLen," ")+" | "+currentAllFreqSum+" | "+currentAllPercentageSum+" || "+currentAppFreqSum+" | "+currentAppPercentageSum+" ||");
        allLinesForCurrentAttribute.add(" ");

        return allLinesForCurrentAttribute;
    }

    private final ArrayList createBasicStatisticsForSetAttribute(String currentAttribute, HashMap namesToAttributeObjectsHash, int maxValueLen, HashMap allAttributeHashes, int totalNumberOfMarkables)    
    {
        ArrayList allLinesForCurrentAttribute = new ArrayList();            
        int freqSum = 0;
        int validSets = 0;
        String sep = " "+padRight("",maxValueLen+1," ");
            
        // Create attribute table header
        sep = sep +"| Size(all): |   %(all): || Size(set): |   %(set): || Size(app): |   %(app): ||";
        // Add first line
        allLinesForCurrentAttribute.add(" "+currentAttribute.toUpperCase()+ " "+((MMAX2Attribute)namesToAttributeObjectsHash.get(((String)currentAttribute.toLowerCase()))).decodeAttributeType());
        // Add separator
        allLinesForCurrentAttribute.add(sep);        
        // Get all value counts for current attribute
        HashMap allValues = (HashMap)allAttributeHashes.get(currentAttribute);            
        Iterator allAttributeValues = allValues.keySet().iterator();
        int thisAttributesNACases = 0;
        int thisAttributesEmptyCases = 0;
        // Determine number of nas and empty entries in advance
        while(allAttributeValues.hasNext())
        {
            String currentValue = (String) allAttributeValues.next();
            if (currentValue.equals("* na *"))
            {
                thisAttributesNACases = ((Integer)allValues.get(currentValue)).intValue();
            }
            if (currentValue.equals("empty"))
            {
                thisAttributesEmptyCases = ((Integer)allValues.get(currentValue)).intValue();
            }
            
        }
        allAttributeValues = null;
        allAttributeValues = allValues.keySet().iterator();            
        while(allAttributeValues.hasNext())
        {
            String tempLine = "";
            // Get current value
            String currentValue = (String) allAttributeValues.next();                
            boolean na = false;
            boolean empty = false;
            // Determine if the respective cell is to be filled
            if (currentValue.equals("* na *"))
            {
                na = true;
                empty = true;
            }            
            
            if (currentValue.equals("empty")) empty = true;            
            if (!(empty) && !(na)) validSets++;
            // Get count for current value
            int currentFreq = ((Integer)allValues.get(currentValue)).intValue();
            // Sum over all freqs
            freqSum = freqSum + currentFreq;
            // Pad value to make it aligned
            currentValue=padRight(currentValue,maxValueLen," ");
            String currentAllFreq = padLeft(currentFreq+"",10," ");
            String currentAllPercentage = getPercentageString(currentFreq,totalNumberOfMarkables,7);
            
            String currentAppFreq = "**********";
            if (na==false)
            {
                currentAppFreq = currentAllFreq;
            }
            String currentAppPercentage = "*********";
            if (na==false)
            {
                currentAppPercentage = getPercentageString(currentFreq,totalNumberOfMarkables-thisAttributesNACases,7);
            }
            
            String currentSetFreq = "**********";
            if (empty==false)
            {
                currentSetFreq = currentAllFreq;
            }
            String currentSetPercentage = "*********";
            if (empty==false)
            {
                currentSetPercentage = getPercentageString(currentFreq,totalNumberOfMarkables-thisAttributesEmptyCases,7);
            }     
            
            tempLine = " "+currentValue+" | "+currentAllFreq+" | "+currentAllPercentage+" || "+currentSetFreq+ " | "+currentSetPercentage+" || "+currentAppFreq+" | "+currentAppPercentage+" ||";

            allLinesForCurrentAttribute.add(tempLine);
        }  

        allLinesForCurrentAttribute.add(" "+padLeft("",maxValueLen+77,"-")+" ||");
        String currentAllFreqSum = padLeft(totalNumberOfMarkables+"", 10, " ");
        String currentAllPercentageSum = "100.00000";
        String currentAppFreqSum = padLeft(totalNumberOfMarkables-thisAttributesNACases+"", 10, " ");
        String currentAppPercentageSum = getPercentageString(freqSum-thisAttributesNACases,totalNumberOfMarkables,7);
        String currentSetFreqSum = padLeft(totalNumberOfMarkables-thisAttributesEmptyCases-thisAttributesNACases+"", 10, " ");
        String currentSetPercentageSum = getPercentageString(freqSum-thisAttributesEmptyCases-thisAttributesNACases,totalNumberOfMarkables,7);        
        allLinesForCurrentAttribute.add(" "+padRight("N/% of all",maxValueLen," ")+" | "+currentAllFreqSum+" | "+currentAllPercentageSum+" || "+currentSetFreqSum+" | "+currentSetPercentageSum+" || "+currentAppFreqSum+" | "+currentAppPercentageSum+" ||");
        freqSum = freqSum-(thisAttributesNACases+thisAttributesEmptyCases);
        if (validSets != 0)
        {
            allLinesForCurrentAttribute.add(" No. of sets: "+validSets+", avg. set size: "+((float)(float)freqSum/(float)validSets));
        }
        allLinesForCurrentAttribute.add(" ");

        return allLinesForCurrentAttribute;
    }

    private final ArrayList createBasicStatisticsForFreetextAttribute(String currentAttribute, HashMap namesToAttributeObjectsHash, int maxValueLen, HashMap allAttributeHashes, int totalNumberOfMarkables)    
    {
        ArrayList allLinesForCurrentAttribute = new ArrayList();            
        int freqSum = 0;
        int validSets = 0;
        String sep = " "+padRight("",maxValueLen+1," ");
            
        // Create attribute table header
        sep = sep +"|    N(all): |   %(all): ||    N(set): |   %(set): ||    N(app): |   %(app): ||";
        // Add first line
        allLinesForCurrentAttribute.add(" "+currentAttribute.toUpperCase()+ " "+((MMAX2Attribute)namesToAttributeObjectsHash.get(((String)currentAttribute.toLowerCase()))).decodeAttributeType());
        // Add separator
        allLinesForCurrentAttribute.add(sep);        
        // Get all value counts for current attribute
        HashMap allValues = (HashMap)allAttributeHashes.get(currentAttribute);            
        Iterator allAttributeValues = allValues.keySet().iterator();
        int thisAttributesNACases = 0;
        int thisAttributesEmptyCases = 0;
        // Determine number of nas and empty entries in advance
        while(allAttributeValues.hasNext())
        {
            String currentValue = (String) allAttributeValues.next();
            if (currentValue.equals("* na *"))
            {
                thisAttributesNACases = ((Integer)allValues.get(currentValue)).intValue();
            }
            if (currentValue.equals(""))
            {
                thisAttributesEmptyCases = ((Integer)allValues.get(currentValue)).intValue();
            }
            
        }
        allAttributeValues = null;
        allAttributeValues = allValues.keySet().iterator();            
        while(allAttributeValues.hasNext())
        {
            String tempLine = "";
            // Get current value
            String currentValue = (String) allAttributeValues.next();                
            boolean na = false;
            boolean empty = false;
            // Determine if the respective cell is to be filled
            if (currentValue.equals("* na *"))
            {
                na = true;
                empty = true;
            }            
            
            if (currentValue.equals("")) empty = true;            

            // Get count for current value
            int currentFreq = ((Integer)allValues.get(currentValue)).intValue();
            // Sum over all freqs
            freqSum = freqSum + currentFreq;
            // Pad value to make it aligned
            currentValue=padRight(currentValue,maxValueLen," ");
            String currentAllFreq = padLeft(currentFreq+"",10," ");
            String currentAllPercentage = getPercentageString(currentFreq,totalNumberOfMarkables,7);
            
            String currentAppFreq = "**********";
            if (na==false)
            {
                currentAppFreq = currentAllFreq;
            }
            String currentAppPercentage = "*********";
            if (na==false)
            {
                currentAppPercentage = getPercentageString(currentFreq,totalNumberOfMarkables-thisAttributesNACases,7);
            }
            
            String currentSetFreq = "**********";
            if (empty==false)
            {
                currentSetFreq = currentAllFreq;
            }
            String currentSetPercentage = "*********";
            if (empty==false)
            {
                currentSetPercentage = getPercentageString(currentFreq,totalNumberOfMarkables-thisAttributesEmptyCases,7);
            }     
            
            tempLine = " "+currentValue+" | "+currentAllFreq+" | "+currentAllPercentage+" || "+currentSetFreq+ " | "+currentSetPercentage+" || "+currentAppFreq+" | "+currentAppPercentage+" ||";

            allLinesForCurrentAttribute.add(tempLine);
        }  

        allLinesForCurrentAttribute.add(" "+padLeft("",maxValueLen+77,"-")+" ||");
        String currentAllFreqSum = padLeft(totalNumberOfMarkables+"", 10, " ");
        String currentAllPercentageSum = "100.00000";
        String currentAppFreqSum = padLeft(totalNumberOfMarkables-thisAttributesNACases+"", 10, " ");
        String currentAppPercentageSum = getPercentageString(freqSum-thisAttributesNACases,totalNumberOfMarkables,7);
        String currentSetFreqSum = padLeft(totalNumberOfMarkables-thisAttributesEmptyCases-thisAttributesNACases+"", 10, " ");
        String currentSetPercentageSum = getPercentageString(freqSum-thisAttributesEmptyCases-thisAttributesNACases,totalNumberOfMarkables,7);        
        allLinesForCurrentAttribute.add(" "+padRight("N/% of all",maxValueLen," ")+" | "+currentAllFreqSum+" | "+currentAllPercentageSum+" || "+currentSetFreqSum+" | "+currentSetPercentageSum+" || "+currentAppFreqSum+" | "+currentAppPercentageSum+" ||");
        freqSum = freqSum-(thisAttributesNACases+thisAttributesEmptyCases);
        allLinesForCurrentAttribute.add(" ");

        return allLinesForCurrentAttribute;
    }
    
    private final String getPercentageString(int freq, int n, int lenToPad)
    {
        float numericalResult = (float)((float)((float)freq*(float)100))/(float)n;
        String temp = numericalResult+"";
        if (numericalResult < 10) temp="  "+temp;
        else if (numericalResult < 100) temp = " "+temp;
        if (temp.length() < 9) temp = padRight(temp, 9,"0");            
        return temp.substring(0,9);
    }
    
    private final String padRight (String input, int lenToReturn, String paddy)
    {
        while (input.length() < lenToReturn) input = input+paddy;
        return input;
    }

    private final String padLeft (String input, int lenToReturn, String paddy)
    {
        while (input.length() < lenToReturn) input = paddy+ input;
        return input;
    }
    
    private final static ArrayList parseColumnSpecifiers(String columnSpec)
    {
        ArrayList result = new ArrayList();
        StringTokenizer toki = new StringTokenizer(columnSpec,",");
        String currentToken = "";
        while (toki.hasMoreTokens())
        {
            currentToken = toki.nextToken().trim();
            result.add(currentToken);
        }
        return result;
    }    
    
    private final void setAssignment(String variable, MMAX2QueryResultList value)
    {
        currentAssignments.put(variable, value); 
        System.err.println("Storing list of "+value.size()+" under name "+variable);
    }
    
    private final MMAX2QueryResultList getAssignment(String variable)
    {
        String varName="";
        String spec="";
        boolean specifier=false;
        if (variable.indexOf(".")==-1)
        {        
            // There is no column specifier, so use entire variable as hash key
            varName = variable;
        }
        else
        {
            // The variable name contains a column specifier
            // Extract variable name without specifier as hash key
            varName = variable.substring(0,variable.indexOf(".")).trim();
            // Extract specifier
            spec = variable.substring(variable.indexOf(".")+1).trim();
            specifier = true;
        }
        
        // Try to get list specified by list name
        MMAX2QueryResultList tempResultList = (MMAX2QueryResultList) currentAssignments.get(varName);
        MMAX2QueryResultList realResultList = null;
        int index =0;
        boolean specifierIsNumerical=false;
        // Only if some list really exists under that name
        if (tempResultList != null)
        {
            // Create clone to return
            realResultList = new MMAX2QueryResultList((MMAX2QueryResultList)tempResultList);
            // The clone thus returned will not have any col spec
            if (specifier)
            {
                // Check whether column specifier is numeric
                try
                {
                    index = Integer.parseInt(spec);
                    specifierIsNumerical = true;
                }
                catch (java.lang.NumberFormatException ex)
                {
                    // 
                }
                
                if (specifierIsNumerical == false)
                {
                    // Try if the spec maps uniquely to a markable level name 
                    index = tempResultList.getColumnIndexByColumnName(spec);
                }
                
                if (index ==-1)
                {
                    //System.out.println("Cannot parse column specifier '"+spec+"', using default (0)!");
                    JOptionPane.showMessageDialog(null,"Column specifier '"+spec+"' not found or not unique, using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                    index = 0;                           
                }
                else
                {
                    // A col spec was found finally, so we kow that index will be set by user
                    // so signal explicit user choice
                    realResultList.setIndexSetByUser();
                }
                
                if (index >= realResultList.getWidth())
                {
                    if (realResultList.getWidth()!=-1)
                    {
                        JOptionPane.showMessageDialog(null,"Cannot access index "+index+" in result list "+varName+"! Result list width is "+realResultList.getWidth()+", using default index (0)!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
                    }
                    realResultList.toDefaultIndexToUse();
                }
                else
                {
                    // Some index could be identified, so set to it, 
                    realResultList.setIndexToUse(index);                                       
                }
            }
        }
        else if(isSystemVariable(varName)==false)
        {
            JOptionPane.showMessageDialog(null,"Cannot find variable "+varName+"!","MMAXQL Simple Query Console",JOptionPane.ERROR_MESSAGE);
        }
        return realResultList;
    }
        
    public static final boolean isSystemVariable(String varName)
    {
        // Default: assume var is system (i.e. numerical name)
        boolean system = true;
        // Get current variable without leading $ sign
        varName = varName.substring(1);
        try
        {
            Integer.parseInt(varName);
        }
        catch (java.lang.NumberFormatException ex)
        {
            // The current var is not a number, so not a system var
            system = false;
        }        
        return system;
    }
    
    private final void clearSystemVariables()
    {
        ArrayList toClear = new ArrayList();
        String temp ="";
        String currentKey = "";
        Iterator keys = currentAssignments.keySet().iterator();
        while(keys.hasNext())
        {
            currentKey = (String)keys.next();
            if (isSystemVariable(currentKey))
            {
                toClear.add(currentKey);
            }
        }
        for (int z=0;z<toClear.size();z++)
        {
            temp = (String) toClear.get(z);
            currentAssignments.remove(temp);            
        }
    }
    
    /** This method extracts the command string as entered in the text field. It is called 
        when the user presses enter in the textInputArea. */
    private final String extractString()
    {
        String result = "";
        Document doc = inputTextArea.getDocument();
        int lastPos = inputTextArea.getCaretPosition();
        boolean isInLastLine = false;
        for (int z=lastPos-2;z>=0;z--)
        {            
            try
            {
                if (doc.getText(z,1).equals("\n")==false)
                {
                    result = doc.getText(z,1)+result;
                }
                else
                {
                    break;
                }
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                ex.printStackTrace();
            }            
            try
            {
                if (inputTextArea.getLineOfOffset(inputTextArea.getCaretPosition())==inputTextArea.getLineCount())
                {
                    isInLastLine = true;
                }
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                ex.printStackTrace();
            }                                        
        }    
        lastResult = result;        
        if (isInLastLine==false)
        {
            inputTextArea.setText(inputTextArea.getText()+"\n"+result+"\n");
        }
        return result;
    }

    public final void actionPerformed(ActionEvent ae)
    {
        String command = ae.getActionCommand();
        
        if (command.equals("lookahead"))
        {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
            doLookAheadOptimization = item.isSelected();
        }
        else if (command.equals("break"))
        {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) ae.getSource();
            doBreakOptimization = item.isSelected();
        }        
        else if (command.equals("search"))
        {
            executeQuery(extractString(),true);
        }
        else if (command.equals("clear"))
        {
            clear();
            // New Mai 1st: Clear does also clear all variables
            currentAssignments = null;
            currentAssignments = new HashMap();
        }
        else if (command.startsWith("defaultlevel:"))
        {
            String newDefaultLevel = command.substring(command.indexOf(":")+1);
            inputTextArea.append("\nSetting new default level to "+newDefaultLevel+"\n");
            currentDefaultLevelName = newDefaultLevel;
        }
        else if (command.equals("load_query"))
        {
            loadMMAXQLQuery();
        }
    }
    
    public final void loadMMAXQLQuery()
    {
        String requestedFileName = "";
        JFileChooser chooser = new JFileChooser(currentMMAXQLPath);
        chooser.setFileFilter(new MMAXFileFilter("MMAXQL","MMAX query language files"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            requestedFileName = chooser.getSelectedFile().getAbsolutePath();            
            currentMMAXQLPath = requestedFileName.substring(0,requestedFileName.lastIndexOf(File.separator));
            BufferedReader textFileReader = null;
            String currentLine="";                                                       
            try
            {
                textFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(requestedFileName),"US-ASCII"));
            }
            catch (java.io.FileNotFoundException ex)
            {
                System.err.println("Error: Couldn't find file "+requestedFileName);
                return;
                //System.exit(0);
            }           
            catch (java.io.UnsupportedEncodingException ex)
            {
            
            }
            catch (java.io.IOException ex)
            {
            
            }
                    
            String entireQuery = "";
            String temporaryQuery="";
        
            // Iterate over all lines in query
            while (true)
            {            
                try
                {
                    // Get next line
                    currentLine = textFileReader.readLine();
                }
                catch (java.io.IOException ex)
                {
                
                }
                if (currentLine != null)
                {
                    // Collect all non-null lines
                    currentLine = currentLine.trim();
                    if (currentLine.equals("")==false && currentLine.startsWith("#")==false)
                    {
                        temporaryQuery = temporaryQuery+" "+currentLine;
                    }
                    continue;
                }
                break;
            }
            entireQuery = temporaryQuery;
            
            try
            {
                textFileReader.close();
            }
            catch (java.io.IOException ex)
            {
                
            }
            
            inputTextArea.append("\n\n"+entireQuery);   
            try
            {
                inputTextArea.scrollRectToVisible(inputTextArea.modelToView(inputTextArea.getLineEndOffset(inputTextArea.getLineCount())));
                inputTextArea.setCaretPosition(inputTextArea.getLineEndOffset(inputTextArea.getLineCount()));
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                
            }
            
        }                
        
    }
    
    private final void clearResultTupleTable()
    {
        resultPane.setComponentAt(0, new JScrollPane(null));
        resultPane.setTitleAt(0, "Markable Tuples");
        clearButton.setEnabled(false);  
        
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
        valueChanged(new ListSelectionEvent(resultMarkableTupleTable, resultMarkableTupleTable.getSelectedRow(),resultMarkableTupleTable.getSelectedRow(),false));
    }
    
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent e) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent)
    {    
        // This is triggered by clicking in the result table or moving the cursor,
        // or by means of selecting a previous/next item in the display popup.
        MMAX2QueryResultTupleElement elementToBeHighlighted = null;
        int row = 0;
        int col = 0;
        if (listSelectionEvent == null)
        {
            // If event is null, a key action has caused this event
            // Get selectedMarkable
            row = currentlySelectedRow;
            col = currentlySelectedColumn;            
        }
        else
        {
            // This event was caused by a mouse click
            row = resultMarkableTupleTable.getSelectedRow();
            col = resultMarkableTupleTable.getSelectedColumn();
            currentlySelectedRow = row;
            currentlySelectedColumn = col;
        }
        invalidate();
        repaint();
        
        // Get currently selected element
        elementToBeHighlighted = currentlyDisplayedResultElements[row][col];
        
        int deToHighlight = -1;
        int deToUnHighlight = -1;
        Markable markableToHighlight = null;
        Markable markableToUnHighlight = null;
        
        if (elementToBeHighlighted != null)
        {
            // This should always be the case (?)
            if (elementToBeHighlighted.isMarkable())
            {
                markableToHighlight = elementToBeHighlighted.getMarkable();
            }
            else
            {
                deToHighlight = mmax2.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(elementToBeHighlighted.getLeftmostDiscoursePosition());
            }
        }
        if (currentlyHighlightedResultElement != null)
        {
            if (currentlyHighlightedResultElement.isMarkable())
            {
                markableToUnHighlight = currentlyHighlightedResultElement.getMarkable();
            }
            else
            {
                deToUnHighlight = mmax2.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(currentlyHighlightedResultElement.getLeftmostDiscoursePosition());                
            }            
        }
                        
        if (easySelect.isSelected() && elementToBeHighlighted != null && elementToBeHighlighted.isMarkable())
        {
            // easySelect has an effect on a newly selected MARKABLE only
            selectCurrent = true;
        }
        
        if (mmax2 != null)
        {
            // Get reference to document, if any
            MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
        
            if (markableToUnHighlight != null)
            {
                // The previously highlighted element was a markable, which now has to be 
                // unhighlighted
                doc.startChanges(markableToUnHighlight);
                if (markableToUnHighlight == mmax2.getCurrentPrimaryMarkable())
                {
                    markableToUnHighlight.renderMe(MMAX2Constants.RENDER_SELECTED);
                }
                else if (mmax2.getCurrentlyRenderedMarkableRelationContaining(markableToUnHighlight)!= null)
                {
                    markableToUnHighlight.renderMe(MMAX2Constants.RENDER_IN_SET);
                }
                else
                {
                    markableToUnHighlight.renderMe(MMAX2Constants.RENDER_UNSELECTED);
                }
                doc.commitChanges();
                // It is possible that the markable just unselected is part of the currentPrimaryMarkable, if any
                if (mmax2.getCurrentPrimaryMarkable()!=null)
                {
                    // So just redraw currentPrimary Markable
                    // TODO: Maybe just if markable is in it?
                    mmax2.getCurrentPrimaryMarkable().renderMe(MMAX2Constants.RENDER_SELECTED);
                }           
            }
            if (deToUnHighlight != -1)
            {
                // The previously highlighted element was a de, which now has to be 
                // unhighlighted
                doc.startChanges(deToUnHighlight, currentlyHighlightedResultElement.toString().length()+1);
                SimpleAttributeSet styleToUse = null;
                styleToUse=chart.getTopAttributesAtDiscourseElement(currentlyHighlightedResultElement.getID());
            
                doc.bulkApplyStyleToDiscourseElement(deToUnHighlight,styleToUse,true);           
                doc.commitChanges();
                // It is possible that the de just unselected is part of the currentPrimaryMarkable, if any
                if (mmax2.getCurrentPrimaryMarkable()!=null)
                {
                    // So just redraw currentPrimary Markable
                    // TODO: Maybe just if de is in it?
                    mmax2.getCurrentPrimaryMarkable().renderMe(MMAX2Constants.RENDER_SELECTED);
                }
                // Get markable at recently cleared position that is currently in a set
                Markable potential = mmax2.getMarkableFromCurrentlyRenderedMarkableSetContaining(currentlyHighlightedResultElement.getID());
                if (potential != null && potential != mmax2.getCurrentPrimaryMarkable())
                {
                    doc.startChanges(potential);
                    potential.renderMe(MMAX2Constants.RENDER_IN_SET);
                    doc.commitChanges();
                }
            }        
            if (markableToHighlight != null)
            {
                if (selectCurrent)
                {
                    // Auto-select next markable
                    mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(markableToHighlight);
                    mmax2.setIgnoreCaretUpdate(true);
                    try
                    {
                        mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(markableToHighlight.getLeftmostDisplayPosition()));
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {
                        System.out.println("Cannot render modelToView()");
                    }   
                    mmax2.setIgnoreCaretUpdate(false);
                    mmax2.getCurrentTextPane().startAutoRefresh();                                
                }
                else
                {
                    doc.startChanges(markableToHighlight);
                    markableToHighlight.renderMe(MMAX2Constants.RENDER_IN_SEARCHRESULT);            
                    mmax2.setIgnoreCaretUpdate(true);
                    try
                    {
                        mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(markableToHighlight.getLeftmostDisplayPosition()));
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {
                        System.out.println("Cannot render modelToView()");
                    }   
                    mmax2.setIgnoreCaretUpdate(false);
                    mmax2.getCurrentTextPane().startAutoRefresh();
                    doc.commitChanges();
                }
            }
            if (deToHighlight != -1)
            {
                doc.startChanges(deToHighlight, elementToBeHighlighted.toString().length()+1);
                SimpleAttributeSet styleToUse = new SimpleAttributeSet();

                StyleConstants.setBackground(styleToUse,Color.orange);
                StyleConstants.setFontSize(styleToUse, mmax2.currentDisplayFontSize);
                StyleConstants.setFontFamily(styleToUse, mmax2.currentDisplayFontName);                                
                doc.bulkApplyStyleToDisplaySpanBackground(deToHighlight, elementToBeHighlighted.toString().length(),styleToUse);

                mmax2.setIgnoreCaretUpdate(true);
                try
                {
                    mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(deToHighlight));
                }
                catch (javax.swing.text.BadLocationException ex)
                {
                    System.out.println("Cannot render modelToView()");
                }   
                mmax2.setIgnoreCaretUpdate(false);
                mmax2.getCurrentTextPane().startAutoRefresh();
                doc.commitChanges();
            }                                
            currentlyHighlightedResultElement = elementToBeHighlighted;
        }
    }
    
    public final void dismiss()
    {
        clear();
        mmax2.unregisterQueryWindow();
        dispose();
    }
    
    public final void clear()
    {
        clearResultTupleTable();
        resultMarkableTupleTable = null;
        currentlyDisplayedResultElements = null;
        if (currentlyHighlightedResultElement != null)
        {
            MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
            if (currentlyHighlightedResultElement.isMarkable())
            {
                doc.startChanges(currentlyHighlightedResultElement.getMarkable());
                currentlyHighlightedResultElement.getMarkable().renderMe(MMAX2Constants.RENDER_UNSELECTED);
                doc.commitChanges();
            }
            else
            {
                int deToUnHighlight = mmax2.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(currentlyHighlightedResultElement.getLeftmostDiscoursePosition());
                doc.startChanges(deToUnHighlight, currentlyHighlightedResultElement.toString().length()+1);
                SimpleAttributeSet styleToUse = null;
                styleToUse=chart.getTopAttributesAtDiscourseElement(currentlyHighlightedResultElement.getID());
                StyleConstants.setFontSize(styleToUse, mmax2.currentDisplayFontSize);
                StyleConstants.setFontFamily(styleToUse, mmax2.currentDisplayFontName);                                
                doc.bulkApplyStyleToDiscourseElement(deToUnHighlight,styleToUse,true);           
                doc.commitChanges();
                // It is possible that the de just unselected is part of the currentPrimaryMarkable, if any
            }
            if (mmax2.getCurrentPrimaryMarkable()!=null)
            {
                // So just redraw currentPrimary Markable
                // TODO: Maybe just if de is in it?
                mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(mmax2.getCurrentPrimaryMarkable());
            }                

        }
        currentlyHighlightedResultElement = null;
    }
    
    public boolean isPreviousResultAvailable()
    {
        if (isResultAvailable() && currentlySelectedRow > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean isNextResultAvailable()
    {
        if (isResultAvailable() && currentlySelectedRow < resultMarkableTupleTable.getRowCount()-1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    
    public boolean isResultAvailable()
    {
        if (resultMarkableTupleTable != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void windowActivated(WindowEvent e) {
    }    
    
    public void windowClosed(WindowEvent e) {
    }
    
    public void windowClosing(WindowEvent e) 
    {
        dismiss();
    }
    
    public void windowDeactivated(WindowEvent e) {
    }
    
    public void windowDeiconified(WindowEvent e) {
    }
    
    public void windowIconified(WindowEvent e) {
    }
    
    public void windowOpened(WindowEvent e) {
    }
    
        
    public final void toConsole(MMAX2QueryResultList output, String query)
    {
        ArrayList attributes = output.getAttributeNamesToDisplay(); 
        // Always sort on index 0 for final display
        ArrayList result = sort(output,0);
        String currentNameSpace = getCurrentNameSpace();
        System.out.println("\n*** "+query+" ***\n");
        int size = result.size();
        for (int q=0;q<size;q++)
        {                        
            System.out.println(currentNameSpace+":::"+size+((MMAX2QueryResultTuple)result.get(q)).toString(attributes));
        }
    }

    public final void toConsole(String statistic)
    {
        System.out.println("Document: "+getCurrentNameSpace());
        System.out.println(statistic);
    }
        
    class MultiLineCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer 
    {
        ArrayList colsToHighlight = null;
        public MultiLineCellRenderer(ArrayList _colsToHighlight) 
        {
            colsToHighlight = _colsToHighlight;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
               boolean isSelected, boolean hasFocus, int row, int column) 
        {                
            if (isSelected) 
            {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } 
            else 
            {
                if (colsToHighlight.contains(new Integer(column)))
                {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
                else
                {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());                    
                }
            }
            setFont(table.getFont());
            if (hasFocus) 
            {
                setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
                if (table.isCellEditable(row, column)) 
                {
                    setForeground( UIManager.getColor("Table.focusCellForeground") );
                    setBackground( UIManager.getColor("Table.focusCellBackground") );
                }
            } 
            else 
            {
                setBorder(new EmptyBorder(1, 2, 1, 2));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }        
}
