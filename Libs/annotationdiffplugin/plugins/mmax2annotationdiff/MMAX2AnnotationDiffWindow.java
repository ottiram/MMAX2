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

package plugins.mmax2annotationdiff;

import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.annotation.markables.*;
import org.eml.MMAX2.annotation.scheme.*;
import org.eml.MMAX2.gui.document.MMAX2Document;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import java.math.*;

import org.eml.MMAX2.gui.windows.MMAXFileFilter;
import org.eml.MMAX2.plugin.MMAX2Plugin;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.discourse.MMAX2Discourse;

import org.eml.MMAX2.api.AttributeAPI;

public class MMAX2AnnotationDiffWindow extends MMAX2Plugin implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.KeyListener, javax.swing.event.ListSelectionListener, java.awt.event.WindowListener
{        
	String version = "2.1";
    HashSet permanentSets = new HashSet();
    
    boolean isShiftDown = false;
    Markable lastMarkableMovedTo = null;
    public MMAX2AttributeConflationWindow conflationWindow = null;
    ArrayList headers = null;
    MMAX2 mmax2 = null;
    MMAX2Discourse discourse = null;
    // Keep those two global
    MarkableLevel majorLevel = null;
    ArrayList allMinorLevels = new ArrayList();    
    
    JTable table = null;
    String[][] tableData = null;
    JScrollPane tablePane = null;   
    JScrollPane outputPane = null;
    JSplitPane splitPane = null;
    
    JMenu displayMenu = null;
    JMenu attributeMenu = null;
    JMenu nominalMenu = null;
    JMenu setMenu = null;
    JMenu pointerMenu = null;
    JMenu freetextMenu = null;
    JMenuItem noAttributeItem = null;
    
    String majorLevelName="";
    String majorLevelSchemeFileName="";
    String majorLevelCustomizationFileName="";
    
    ButtonGroup levelGroup = new ButtonGroup();
    ButtonGroup attributeGroup = new ButtonGroup();
    
    JTextArea outputArea = null;
    JComboBox queryField = null;
    JButton queryButton = null;
    JButton clearButton = null;
    
    JCheckBoxMenuItem suppressUnmatched = null;
    JCheckBoxMenuItem treatEmptySetsAsSingletons=null;
    JCheckBoxMenuItem treatMissingMarkablesAsSingletons=null;
    JCheckBoxMenuItem treatNonIdenticalSingletonsAsDisjunct=null;
    
    JCheckBoxMenuItem confusionMatrixUseNoMarkableAsMetaValue = null;
    JCheckBoxMenuItem confusionMatrixUseNoValueAsMetaValue = null;
    public JCheckBoxMenuItem confusionMatrixConflateValues = null;    
    
    JCheckBoxMenuItem kappaUseNoMarkableAsMetaValue = null;
    JCheckBoxMenuItem kappaUseNoValueAsMetaValue = null;    
    
    JCheckBox useQuery = null;
        
    String currentAttributeName="no_attribute";
    String currentAttributeType="no_attribute";
    
    ArrayList spanToIDMappingsForAllLevels = null;
    
    public static int IDENTITY = 0;
    public static int A_SUBSUMES_B = 1;
    public static int B_SUBSUMES_A = 2;
    public static int INTERSECTION = 3;
    public static int DISJUNCTION = 4;
    
    static Color IDENTITY_COLOR = new Color(0,200,0);
    static Color SUBSUMPTION_COLOR = new Color(0,160,0);
    static Color INTERSECTION_COLOR = new Color(0,130,0);
    static Color DISJUNCTION_COLOR = new Color(0,100,0);
    
    public MMAX2AnnotationDiffWindow() 
    {
    	
    }    
    
    /** Creates a new instance of MMAX2AnnotationDiffWindow */
    public MMAX2AnnotationDiffWindow(MMAX2Discourse _discourse) 
    {
        super();       
        addWindowListener(this);
        setTitle("MMAX2 Annotation Diff Window "+version);
        discourse = _discourse;        
        mmax2 = discourse.getMMAX2();
        JMenuBar menu = new JMenuBar();
        JMenu mainLevelMenu = new JMenu("Main level");
        mainLevelMenu.setFont(MMAX2.getStandardFont());
        JRadioButtonMenuItem item = new JRadioButtonMenuItem("<none>");
        item.setFont(MMAX2.getStandardFont());
        item.setActionCommand("select:<none>");
        item.setSelected(true);
        item.addActionListener(this);
        
        levelGroup = new ButtonGroup();
        levelGroup.add(item);
        mainLevelMenu.add(item);
        
        MarkableLevel[] levels = mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkableLevels();
        for (int b=0;b<levels.length;b++)
        {
            item = new JRadioButtonMenuItem(levels[b].getMarkableLevelName());
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand("select:"+levels[b].getMarkableLevelName());
            item.addActionListener(this);
            levelGroup.add(item);
            mainLevelMenu.add(item);
            item = null;
        }
        
        menu.add(mainLevelMenu);
        
        displayMenu = new JMenu("Added levels");
        displayMenu.setEnabled(false);
        displayMenu.setFont(MMAX2.getStandardFont());
        JMenuItem addLevelItem = new JMenuItem("Add level");
        addLevelItem.setFont(MMAX2.getStandardFont());
        addLevelItem.setActionCommand("add_level");
        addLevelItem.addActionListener(this);
        displayMenu.add(addLevelItem);
        JMenuItem clearAllItem = new JMenuItem("Remove all added levels");
        clearAllItem.setFont(MMAX2.getStandardFont());
        clearAllItem.setActionCommand("remove_all");
        clearAllItem.addActionListener(this);
        displayMenu.add(clearAllItem);        
        
        suppressUnmatched = new JCheckBoxMenuItem("Suppress unmatched markables");
        suppressUnmatched.setFont(MMAX2.getStandardFont());
        suppressUnmatched.setActionCommand("suppress_unmatched_changed");
        suppressUnmatched.addActionListener(this);
        displayMenu.add(suppressUnmatched);
        menu.add(displayMenu);        
        
        attributeMenu = new JMenu("Attributes");
        attributeMenu.setFont(MMAX2.getStandardFont());
        nominalMenu = new JMenu("Nominal");
        nominalMenu.setFont(MMAX2.getStandardFont());
        freetextMenu = new JMenu("Freetext");
        freetextMenu.setFont(MMAX2.getStandardFont());
        setMenu = new JMenu("Set");
        setMenu.setFont(MMAX2.getStandardFont());
        pointerMenu = new JMenu("Pointer");
        pointerMenu.setFont(MMAX2.getStandardFont());

        noAttributeItem = new JRadioButtonMenuItem("No attribute");
        noAttributeItem.setSelected(true);
        noAttributeItem.setFont(MMAX2.getStandardFont());
        noAttributeItem.setActionCommand("attribute:no_attribute:no_attribute");
        noAttributeItem.addActionListener(this);

        attributeMenu.add(noAttributeItem);
        
        attributeMenu.add(nominalMenu);
        attributeMenu.add(freetextMenu);
        attributeMenu.add(setMenu);
        attributeMenu.add(pointerMenu);

        
        attributeMenu.addSeparator();        
        treatEmptySetsAsSingletons = new JCheckBoxMenuItem("Treat markables with empty SET attributes as singleton sets");
        treatEmptySetsAsSingletons.setFont(MMAX2.getStandardFont());
        treatEmptySetsAsSingletons.setActionCommand("settings");
        treatEmptySetsAsSingletons.addActionListener(this);                
        attributeMenu.add(treatEmptySetsAsSingletons);

        treatMissingMarkablesAsSingletons = new JCheckBoxMenuItem("Treat missing markables with SET attributes as singleton sets");
        treatMissingMarkablesAsSingletons.setFont(MMAX2.getStandardFont());
        treatMissingMarkablesAsSingletons.setActionCommand("settings");
        treatMissingMarkablesAsSingletons.addActionListener(this);                
        attributeMenu.add(treatMissingMarkablesAsSingletons);
        
        treatNonIdenticalSingletonsAsDisjunct = new JCheckBoxMenuItem("Treat nonidentical singletons as disjunct");
        treatNonIdenticalSingletonsAsDisjunct.setFont(MMAX2.getStandardFont());
        treatNonIdenticalSingletonsAsDisjunct.setActionCommand("settings");
        treatNonIdenticalSingletonsAsDisjunct.addActionListener(this);                
        attributeMenu.add(treatNonIdenticalSingletonsAsDisjunct);        
        menu.add(attributeMenu);
        
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(MMAX2.getStandardFont());

        JMenu confusionMatrixMenu = new JMenu("Confusion Matrix");
        confusionMatrixMenu.setFont(MMAX2.getStandardFont());
        
        confusionMatrixUseNoValueAsMetaValue = new JCheckBoxMenuItem("Use 'NO VALUE' as meta value");
        confusionMatrixUseNoValueAsMetaValue.setFont(MMAX2.getStandardFont());
        confusionMatrixUseNoValueAsMetaValue.setActionCommand("settings");
        confusionMatrixUseNoValueAsMetaValue.addActionListener(this);
        confusionMatrixMenu.add(confusionMatrixUseNoValueAsMetaValue);

        confusionMatrixUseNoMarkableAsMetaValue = new JCheckBoxMenuItem("Use 'NO MARKABLE' as meta value");
        confusionMatrixUseNoMarkableAsMetaValue.setFont(MMAX2.getStandardFont());
        confusionMatrixUseNoMarkableAsMetaValue.setActionCommand("settings");
        confusionMatrixUseNoMarkableAsMetaValue.addActionListener(this);
        confusionMatrixMenu.add(confusionMatrixUseNoMarkableAsMetaValue);
        
        confusionMatrixConflateValues = new JCheckBoxMenuItem("Conflate Values");
        confusionMatrixConflateValues.setFont(MMAX2.getStandardFont());
        confusionMatrixConflateValues.setActionCommand("collapse");
        confusionMatrixConflateValues.addActionListener(this);
        confusionMatrixMenu.add(confusionMatrixConflateValues);
        
        settingsMenu.add(confusionMatrixMenu);
        
        JMenu kappaMenu = new JMenu("Kappa");
        kappaMenu.setFont(MMAX2.getStandardFont());
        
        kappaUseNoValueAsMetaValue = new JCheckBoxMenuItem("Use 'NO VALUE' as meta value");
        kappaUseNoValueAsMetaValue.setFont(MMAX2.getStandardFont());
        kappaUseNoValueAsMetaValue.setActionCommand("settings");
        kappaUseNoValueAsMetaValue.addActionListener(this);
        kappaMenu.add(kappaUseNoValueAsMetaValue);

        kappaUseNoMarkableAsMetaValue = new JCheckBoxMenuItem("Use 'NO MARKABLE' as meta value");
        kappaUseNoMarkableAsMetaValue.setFont(MMAX2.getStandardFont());
        kappaUseNoMarkableAsMetaValue.setActionCommand("settings");
        kappaUseNoMarkableAsMetaValue.addActionListener(this);
        kappaMenu.add(kappaUseNoMarkableAsMetaValue);
        
        settingsMenu.add(kappaMenu);
        
        menu.add(settingsMenu);
        
        tableData = new String[1][1];
        tableData[0][0]="";
        String[] header = new String[2];
        header[0]="SPAN";
        header[1]="STRING";
        DefaultTableModel dm = new DefaultTableModel() 
        {
             public Class getColumnClass(int columnIndex)
             {
                return String.class;
        }
        };

        dm.setDataVector(tableData,header); 
        headers = new ArrayList();
        headers.add("");
        headers.add("");
        if (majorLevel != null)
        {
            headers.add(majorLevel.getAbsoluteMarkableFileName());
        }
        if (allMinorLevels != null)
        {
            for (int b=0;b<allMinorLevels.size();b++)
            {
                headers.add(((MarkableLevel)allMinorLevels.get(b)).getAbsoluteMarkableFileName());
            }
        }
        table = new JTable(dm)
        {
            public boolean isCellEditable(int x, int y)
            {
                return false;
            }    
            
            protected JTableHeader createDefaultTableHeader() 
            {
                return new JTableHeader(columnModel) 
                {
                    public String getToolTipText(MouseEvent e) 
                    {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return (String)headers.get(realIndex);
                    }
                };
            }                                    
        };                
        
        table.addMouseListener(this);
        table.addKeyListener(this);
        table.getTableHeader().addMouseListener(this);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        
        tablePane = new JScrollPane(table);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        Box upperPanelBox = Box.createVerticalBox();
        upperPanelBox.add(tablePane);
        upperPanelBox.add(Box.createVerticalStrut(5));
        Box lowerBox = Box.createHorizontalBox();

        queryField = new JComboBox();
        queryField.addItem("");        
        queryField.addItem("(*markable_text={})");
        queryField.setEditable(true);
        queryField.setSelectedIndex(0);
        queryField.setFont(MMAX2.getStandardFont());

        queryField.addKeyListener(this);
        lowerBox.add(queryField);
        
        useQuery = new JCheckBox();
        useQuery.setActionCommand("use_query");
        useQuery.setSelected(true);
        useQuery.addActionListener(this);     
        lowerBox.add(useQuery);
        
        queryButton = new JButton("Apply");
        queryButton.setFont(MMAX2.getStandardFont());
        queryButton.setActionCommand("apply");
        queryButton.addActionListener(this);        
        lowerBox.add(queryButton);
        
        clearButton = new JButton("Clear");
        clearButton.setFont(MMAX2.getStandardFont());
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);        
        lowerBox.add(clearButton);                
                
        upperPanelBox.add(lowerBox);      
        upperPanelBox.add(Box.createVerticalStrut(5));        
        splitPane.setTopComponent(upperPanelBox);
                
        outputArea = new JTextArea();
        splitPane.setBottomComponent(new JScrollPane(outputArea));
        
        getContentPane().add(splitPane);
        setJMenuBar(menu);
        pack();
        setVisible(true);                
    }

    public static void show(MMAX2Discourse discourse)
    {
    	// To be called via plugin menu
    	MMAX2AnnotationDiffWindow diffWindow = new MMAX2AnnotationDiffWindow(discourse);
    	diffWindow.setVisible(true);
    }
    
    public final void dumpTableToFile()
    {
        String fileName = JOptionPane.showInputDialog(this, "Enter dump file name!");
        if (fileName.equals(""))
        {
            return;
        }
        
        String widthString = JOptionPane.showInputDialog(this, "Enter spanWidth (14),textWidth (25),colWidth (11), sepLength (100)!");
        if (widthString.equals(""))
        {
            widthString="14,25,11,100";
        }
        
        
        FileWriter dumpWriter = null;
        try
        {
            dumpWriter = new FileWriter(fileName+".txt");
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
        
        String separator="";
        
        int lines = table.getRowCount();
        int cols = table.getColumnCount();
        
        int spanWidth=0;
        int textWidth=0;
        int colWidth=0;
        int sepLength=0;
        
        StringTokenizer toki = new StringTokenizer(widthString,",");
        while (toki.hasMoreTokens())
        {
            String currentToken=(String)toki.nextToken();
            if (spanWidth==0)
            {
                spanWidth=Integer.parseInt(currentToken);
            }
            else if (textWidth==0)
            {
                textWidth=Integer.parseInt(currentToken);
            }
            else if (colWidth==0)
            {
                colWidth=Integer.parseInt(currentToken);
            }
            else
            {
                sepLength=Integer.parseInt(currentToken);
            }
        }
        
        // Iterate over all lines
        for (int a=0;a<lines;a++)
        {
            String currentLine="";
            // Iterate over all columns in current line
            for (int b=0;b<cols;b++)
            {
                String currentCell=(String)table.getValueAt(a, b);
                if (currentCell == null) currentCell = "";
                if (b==0)
                {
                    // This is the col with the spans                    
                    currentCell = currentCell.replaceAll("word_","");
                    currentCell = splitInChunks(currentCell,spanWidth,0);
                }
                else if (b == 1)
                {
                    // The cell is the one with the markable text                    
                    currentCell = splitInChunks(currentCell,textWidth,spanWidth+3);
                }
                else
                {
                    // The cell is one of a column
                    currentCell = splitInChunks(currentCell,colWidth,0);
                }
                
                currentLine = currentLine+currentCell+" | ";                
                if (separator.equals(""))
                {
                    for (int d=0;d<sepLength;d++)
                    {
                        separator=separator+"-";
                    }
                }                
            }            
            try
            {
                dumpWriter.write(currentLine+"\n");
                dumpWriter.write(separator+"\n");
            }
            catch (java.io.IOException ex)
            {
                
            }                
        }
        
        try
        {
            dumpWriter.flush();
            dumpWriter.close();
        }
        catch (java.io.IOException ex)
        {}                
        
    }
    
    public final String splitInChunks (String input, int chunkLen, int leftPad)
    {
        String result="";

        String leftFiller="";
        for (int a=0;a<leftPad;a++)
        {
            leftFiller=leftFiller+" ";
        }
                
        String filler="";
        for (int a=0;a<chunkLen;a++)
        {
            filler=filler+" ";
        }
        
        if (input.length() <= chunkLen)
        {
            input=input+filler;
            result = input.substring(0,chunkLen);
        }
        else
        {
            // The input string really has to be split
            ArrayList temp=new ArrayList();
            
            int iterations = input.length()/chunkLen;
            BigInteger tempInt = new BigInteger(input.length()+"");
            BigInteger rest = tempInt.mod(new BigInteger(chunkLen+""));
            
            for (int p=0;p<iterations;p++)
            {
                if (p>0)
                {
                    temp.add(leftFiller+input.substring(p*chunkLen,(p+1)*chunkLen));
                }
                else
                {
                    temp.add(input.substring(p*chunkLen,(p+1)*chunkLen));
                }
            }
            
            int smallRest = rest.intValue();
            if (smallRest != 0)
            {
                String finalString =input.substring(input.length()-smallRest); 
                temp.add(leftFiller+splitInChunks(finalString,chunkLen,0));
            }
            
            for (int p=0;p<temp.size();p++)
            {
                if (p<temp.size()-1)
                {
                    result=result+(String)temp.get(p)+"\n";
                }
                else
                {
                    result=result+(String)temp.get(p);
                }
            }
        }
        return result;
    }    
    
    public final ArrayList requestAddLevels()
    {
        MarkableLevel newLevel = null;
        ArrayList list = new ArrayList();
        String requestedFileName = "";
        JFileChooser chooser = new JFileChooser(mmax2.currentWorkingDirectory);
        chooser.setMultiSelectionEnabled(true);       
        
        chooser.setFileFilter(new MMAXFileFilter("xml","MMAX2 markable files",mmax2.getCurrentDiscourse().getNameSpace()));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {                    
            File[] temp = chooser.getSelectedFiles();
            for (int v=0;v<temp.length;v++)
            {
                requestedFileName = temp[v].getAbsolutePath();
                MarkableFileLoader mfl = new MarkableFileLoader();
            
                mfl.load(requestedFileName,majorLevelName,majorLevelSchemeFileName,majorLevelCustomizationFileName,"active");
                // Get markable level object. Up to now, this has only a non-null DOM, but no markables yet
                newLevel = mfl.getMarkableLevel();
                // Set reference to associated discourse. This is required for next call (cf. below)
                newLevel.setCurrentDiscourse(mmax2.getCurrentDiscourse());
                newLevel.createMarkables();
                newLevel.initMarkableRelations();
                
                newLevel.getRenderer().updateSimpleMarkableCustomizations(true);
                
                list.add(newLevel);
                newLevel=null;
            }
        }
        permanentSets.clear();
        return list;
    }
    
    public String doConfusionMatrix(int[][]kappaTable)
    {
        // This assumes that only two levels are displayed        
        MMAX2Attribute attrib = majorLevel.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType(currentAttributeName,AttributeAPI.NOMINAL_BUTTON,AttributeAPI.NOMINAL_LIST);
        // Get list of all defined possible values
        ArrayList tempValues = new ArrayList(attrib.getOrderedValues());                
        
        ArrayList possValues = new ArrayList();
        boolean collapse = confusionMatrixConflateValues.isSelected();
        if (collapse)
        {
            // Several values might be collapsed            
            possValues = conflationWindow.getConflatedAttributes();
            
            if (confusionMatrixUseNoValueAsMetaValue.isSelected())
            {
                possValues.add(";NO VALUE;");
            }

            if (confusionMatrixUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add(";NO MARKABLE;");
            }
        }
        else
        {
            // Collapse = false
            // Use normal values as poss values
            possValues = tempValues;
            if (confusionMatrixUseNoValueAsMetaValue.isSelected())
            {
                possValues.add("NO VALUE");
            }

            if (confusionMatrixUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add("NO MARKABLE");
            }            
        }        
                
        int[][] result = new int[possValues.size()+1][possValues.size()+1];
        HashMap map = new HashMap();
        String currentValueCombination = "";
        // Iterate over all rows (= markables) in displayed table
        for (int z=0;z<table.getRowCount();z++)
        {            
            // Get pair of values as selected by annos 1 and 2 for current markable
            if (collapse)
            {
                // Add ; needed for matching
                currentValueCombination=";"+(String)table.getValueAt(z, 2)+";::;"+(String)table.getValueAt(z, 3)+";";
            }
            else
            {
                currentValueCombination=(String)table.getValueAt(z, 2)+"::"+(String)table.getValueAt(z, 3);
            }            
            
            // Get Integer object reflecting count for current value combination, if any
            Integer currentValueCombinationCount=(Integer)map.get(currentValueCombination);            
            if (currentValueCombinationCount==null)
            {
                // The current instance is the first for this combination
                currentValueCombinationCount = new Integer(1);
            }
            else
            {
                currentValueCombinationCount = new Integer(currentValueCombinationCount.intValue()+1);
            }
            map.put(currentValueCombination, currentValueCombinationCount);
        }
        
        // Key = value combination
        ArrayList allKeys = new ArrayList(map.keySet());
        
        String currentKey="";
        String currentAnno0Key="";
        String currentAnno1Key="";
        int currentCount=0;
        int row=-1;
        int col=-1;
        // Iterate over all different value combinations found in table
        // These will always be two-part, never collapsed
        for (int b=0;b<allKeys.size();b++)
        {
            row=-1;
            col=-1;

            // Get current value combination
            currentKey = (String)allKeys.get(b);
            // Get both values separately; these may contain ; if collapse = true
            currentAnno0Key=currentKey.substring(0,currentKey.indexOf("::"));
            currentAnno1Key=currentKey.substring(currentKey.indexOf("::")+2);
            // Get count for current value combination
            currentCount = ((Integer)map.get(currentKey)).intValue();
            // Now, find the cell in the count table in which the current count has to be registered
            // There is exactly ONE cell for each combination, no matter if collapse is true or false
            if (collapse)
            {                                    
                // Iterate over all possValues. These are enclosed in ;
                for (int x=0;x<possValues.size();x++)
                {                    
                    // Get current possValue
                    String temp = (String)possValues.get(x);
                    // Match row if current anno0Key is substring of current poss value
                    if (temp.indexOf(currentAnno0Key)!=-1)
                    {
                        row = x;
                    }
                    // Match col if current anno1Key is substring of current poss value
                    if (temp.indexOf(currentAnno1Key)!=-1)
                    {
                        col = x;
                    }
                    if (row != -1 && col != -1)
                    {
                        break;
                    }
                }
            }
            else
            {
                // Row and col in the table can be found by exact matching
                row=possValues.indexOf(currentAnno0Key);
                col=possValues.indexOf(currentAnno1Key);                
            }
            if (row == -1 || col == -1)
            {
                toOutputArea("Warning: Value "+currentKey+" not mapped!\n");
                System.err.println("Error: Value "+currentKey+" not mapped!");
            }
            else
            {
                //result[row][col]=currentCount;
                result[row][col]=result[row][col]+currentCount;
            }            
        }
        
        ArrayList agreedValues = new ArrayList();
        ArrayList totalValues = new ArrayList();
        
        for (int b=0;b<possValues.size();b++)
        {
            totalValues.add(new Integer(0));
        }
                
        String tab="";
        
        // Create top row
        for (row=0;row<possValues.size();row++)
        {
            tab=tab+"\t"+row;
        }
        tab=tab+"\n";
        
        // Row sum is calculated for each row and then printed immediately, so no array neccessary
        int rowSum=0;
        // Col sum is collected and then printed in one go
        int[] colSum=new int[possValues.size()];
        
        for (row=0;row<possValues.size();row++)
        {
            // Add index of attribute as row header
            tab=tab+row;
            rowSum=0;
            for (col=0;col<possValues.size();col++)
            {
                colSum[col]=colSum[col]+result[row][col];
                rowSum=rowSum+result[row][col];
                if (result[row][col]!=0)
                {
                    tab=tab+"\t"+result[row][col];
                }
                else
                {
                    tab=tab+"\t-";
                }
                if (row==col)
                {
                    agreedValues.add(new Integer(result[row][col]));
                }       
                
                totalValues.set(col, new Integer(((Integer)totalValues.get(col)).intValue()+result[row][col]));
                totalValues.set(row, new Integer(((Integer)totalValues.get(row)).intValue()+result[row][col]));
            }
            String temp = (String)possValues.get(row);
            if (collapse)
            {
                temp = temp.substring(1,temp.length()-1);
            }

            tab=tab+"\t"+rowSum+"\t"+temp+"\n";
        }
        for (int b=0;b<colSum.length;b++)
        {
            tab=tab+"\t"+colSum[b];
        }
        tab=tab+"\n";
        System.err.println("agreed "+agreedValues);
        System.err.println("total "+totalValues);
        
        String head="\nConfusion matrix:";
        head=head+"\nData in rows: "+majorLevel.getAbsoluteMarkableFileName();
        head=head+"\nData in columns: "+((MarkableLevel)allMinorLevels.get(0)).getAbsoluteMarkableFileName();
        head=head+"\nFilter:"+(String)queryField.getSelectedItem();
        head=head+"\n\nKey to attribute numbers:\n";
        for (row=0;row<possValues.size();row++)
        {
            int totalCount = ((Integer)totalValues.get(row)).intValue();
            int agreedCount = ((Integer)agreedValues.get(row)).intValue();
            double agreedPercent = 0.0;
            String agreedPercentString="";
            if (totalCount!=0)
            {
                agreedPercent = (double)(((double)agreedCount*(double)100*(double)2/(double)((double)totalCount)));
                agreedPercentString=agreedPercent+"";
            }
            else
            {
                agreedPercentString = "---";
            }            
            String temp = (String)possValues.get(row);
            if (collapse)
            {
                temp = temp.substring(1,temp.length()-1);
            }
            head=head+(row+"")+":\t"+temp+"\t(total: "+(totalCount)+"\tagreed: "+agreedPercentString+" %)";
            if (kappaTable!=null)
            {
                head=head+" Kappa_j:"+doKappa_j(kappaTable, row, 2)+" \n";
            }
            else
            {
                head=head+"\n";
            }
        }

        head=head+"\n\n";

        tab=head+tab;
        
        return tab;
    }
    
    public void updateAttributeMenu()
    {
        attributeGroup = new ButtonGroup();
        attributeGroup.add(noAttributeItem);
        
        if (majorLevel == null)
        {
            nominalMenu.removeAll();
            nominalMenu.setEnabled(false);
            freetextMenu.removeAll();
            freetextMenu.setEnabled(false);
            setMenu.removeAll();
            setMenu.setEnabled(false);
            pointerMenu.removeAll();
            pointerMenu.setEnabled(false);
            return;
        }
                
        MMAX2AnnotationScheme scheme = majorLevel.getCurrentAnnotationScheme();
        nominalMenu.removeAll();
        MMAX2Attribute[] attributes = scheme.getAttributesByType(AttributeAPI.NOMINAL_BUTTON,AttributeAPI.NOMINAL_LIST);
        for (int v=0;v<attributes.length;v++)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(attributes[v].getDisplayAttributeName());
            attributeGroup.add(item);
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand("attribute:nominal:"+attributes[v].getDisplayAttributeName());
            item.addActionListener(this);
            nominalMenu.add(item);
        }
        if (nominalMenu.getItemCount()>0)
        {
            nominalMenu.setEnabled(true);
        }
        else
        {
            nominalMenu.setEnabled(false);
        }
        
        freetextMenu.removeAll();
        attributes = scheme.getAttributesByType(AttributeAPI.FREETEXT);
        for (int v=0;v<attributes.length;v++)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(attributes[v].getDisplayAttributeName());
            attributeGroup.add(item);
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand("attribute:freetext:"+attributes[v].getDisplayAttributeName());
            item.addActionListener(this);
            freetextMenu.add(item);
        }
        if (freetextMenu.getItemCount()>0)
        {
            freetextMenu.setEnabled(true);
        }
        else
        {
            freetextMenu.setEnabled(false);
        }
        
        setMenu.removeAll();
        attributes = scheme.getAttributesByType(AttributeAPI.MARKABLE_SET);
        for (int v=0;v<attributes.length;v++)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(attributes[v].getDisplayAttributeName());
            attributeGroup.add(item);
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand("attribute:set:"+attributes[v].getDisplayAttributeName());
            item.addActionListener(this);
            setMenu.add(item);
        }
        if (setMenu.getItemCount()>0)
        {
            setMenu.setEnabled(true);
        }
        else
        {
            setMenu.setEnabled(false);
        }
        
        pointerMenu.removeAll();
        attributes = scheme.getAttributesByType(AttributeAPI.MARKABLE_POINTER);
        for (int v=0;v<attributes.length;v++)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(attributes[v].getDisplayAttributeName());
            attributeGroup.add(item);
            item.setFont(MMAX2.getStandardFont());
            item.setActionCommand("attribute:pointer:"+attributes[v].getDisplayAttributeName());
            item.addActionListener(this);
            pointerMenu.add(item);
        }
        if (pointerMenu.getItemCount()>0)
        {
            pointerMenu.setEnabled(true);
        }
        else
        {
            pointerMenu.setEnabled(false);
        }        
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        String command = e.getActionCommand();
        if (command.startsWith("select:"))
        {
            String newName=command.substring(7);
                
            if ((majorLevel!=null && majorLevel.getMarkableLevelName().equalsIgnoreCase(newName)))
            {
                // Do nothing if current one was re-selected
                return;
            }                
                
            if (newName.equals("<none>"))
            {
                majorLevel = null;
                displayMenu.setEnabled(false);
            }
            else
            {
                // Some actual level was selected
                displayMenu.setEnabled(true);
                majorLevel = mmax2.getCurrentDiscourse().getCurrentMarkableChart().getMarkableLevelByName(newName, false);                
                majorLevelName = majorLevel.getMarkableLevelName();
                majorLevelSchemeFileName = majorLevel.getCurrentAnnotationScheme().getSchemeFileName();
                majorLevelCustomizationFileName = majorLevel.getCustomizationFileName();
            }                
            permanentSets.clear();
            updateAttributeMenu();
            updateMarkableDisplay();
        }
        else if (command.equals("add_level"))
        {
            ArrayList newLevels = requestAddLevels();
            if (newLevels.size()>0)
            {
                allMinorLevels.addAll(newLevels);
                permanentSets.clear();
                updateMarkableDisplay();
            }
        }
        else if (command.equals("remove_all"))
        {
            allMinorLevels = new ArrayList();
            permanentSets.clear();
            updateMarkableDisplay();
        }
        else if (command.equals("suppress_unmatched_changed"))
        {
            permanentSets.clear();
            updateMarkableDisplay();
        }
        else if (command.startsWith("attribute:"))
        {
            permanentSets.clear();
            command = command.substring(10);            
            currentAttributeType = command.substring(0,command.indexOf(":"));
            currentAttributeName = command.substring(command.indexOf(":")+1);
            updateMarkableDisplay();
        }
        else if (command.equals("apply"))
        {
            updateMarkableDisplay();
        }
        else if (command.equals("clear"))
        {
            queryField.setSelectedIndex(-1);
            updateMarkableDisplay();
        }
        else if (command.equals("dump"))
        {
            dumpTableToFile();
        }        
        else if (command.equals("settings"))
        {
            updateMarkableDisplay();
        }
        else if (command.equals("use_query"))
        {
            permanentSets.clear();
            queryButton.setEnabled(useQuery.isSelected());
            clearButton.setEnabled(useQuery.isSelected());
            queryField.setEnabled(useQuery.isSelected());
            updateMarkableDisplay();
        }
        else if (command.equals("collapse"))
        {
            if (confusionMatrixConflateValues.isSelected())
            {
                if (conflationWindow != null)
                {
                    conflationWindow.setVisible(true);
                }
                else if (majorLevel!=null)
                {
                    MMAX2Attribute attrib = majorLevel.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+currentAttributeName+"$",AttributeAPI.NOMINAL_BUTTON,AttributeAPI.NOMINAL_LIST);
                    if (attrib != null)
                    {
                    	// Get list of all defined possible values, ordered alphabetically                    
                    	ArrayList tempValues = new ArrayList(attrib.getOrderedValues());                    
                    	conflationWindow = new MMAX2AttributeConflationWindow(tempValues,this);
                    }
                }
            }
            else
            {
                if (conflationWindow != null)
                {
                    conflationWindow.setVisible(false);
                }                
            }
        }
    }
    
    public MarkableSet getMarkableSet(String attributeName, String markableID, MarkableLevel level)
    {
        if (markableID == null)
        {
            return null;
        }
        
        // Returns the MarkableSet from level level that the markable with id markableID is in for attribute attributeName
        attributeName = attributeName.toLowerCase();
        // Get attribute object for attributeName
        MMAX2Attribute currentSetAttribute = level.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        if (currentSetAttribute == null)
        {
            System.err.println("Warning: No set attribute '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }
        MarkableRelation currentSetRelation = currentSetAttribute.getMarkableRelation();
        if (currentSetRelation == null)
        {
            System.err.println("Warning: No set relation '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }

        return currentSetRelation.getMarkableSetWithAttributeValue(level.getMarkableByID(markableID).getAttributeValue(attributeName,""));
    }
    
    public HashMap getAllDistinctPartitionSets(String attributeName, int attributeType, MarkableLevel level, ArrayList allDistinctPartitionSets, String currentlyActiveQuery, ArrayList allSpansCurrentlyOnDisplay)
    {
        // This method returns a HashMap in which set IDs of the form 'set_3' (level-dependent) as keys are 
        // mapped to HashSets as values
        // which contain String representations of all the markables (spans) in the set (level-independent)
        // Each HashMap pertains to one level.
        
        // In addition, *all* distinct partitions are collected in allDistinctPartitionSets. The objects contained
        // in this list are the same HashSet objects as the ones returned in the result HashMap.
        HashMap mapping = new HashMap();
        attributeName = attributeName.toLowerCase();        
        // Get set attribute
        MMAX2Attribute currentSetAttribute = level.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        if (currentSetAttribute == null)
        {
            System.err.println("Warning: No set attribute '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }
        // Get pertaining relation
        MarkableRelation currentSetRelation = currentSetAttribute.getMarkableRelation();
        if (currentSetRelation == null)
        {
            System.err.println("Warning: No set relation '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }
        
        // Get all markable sets defined by current relation on current level
        MarkableSet[] allSets = currentSetRelation.getMarkableSets(false);
        // Iterate over them
        for (int z=0;z<allSets.length;z++)
        {
            // Get current set
            MarkableSet currentSet = allSets[z];
            
            // Get hash of strings of current set
            // This is an unordered SET! 
            HashSet temp = currentSet.getSetOfStrings();            
            if (allDistinctPartitionSets.contains(temp)==false)
            {
                allDistinctPartitionSets.add(temp);
            }
            
            // Create mapping from (level-dependent) set_ID to (level-independent) set-representation
            mapping.put(currentSet.getAttributeValue(), temp);
        }        
                        
        if (treatEmptySetsAsSingletons.isSelected() || treatMissingMarkablesAsSingletons.isSelected())
        {
            // Treat singletons or missing markables as one-element partitions
            
            // This is no good way of finding singletons, as it does not take into account
            // that there might be a filtering query in action. 
            // Doing it this way will produce many irrelevant partitions
            
            // This is better:
            
            ArrayList tempMarkableList = new ArrayList();
            if (currentlyActiveQuery.equals("")==false)
            {
                // If there is currently a query, apply it
                tempMarkableList = level.getMatchingMarkables(currentlyActiveQuery);
            }
            else
            {
                // Otherwise just take all
                tempMarkableList = level.getMarkables(new DiscourseOrderMarkableComparator());
            }
            
            if (treatEmptySetsAsSingletons.isSelected())
            {
                ArrayList singletons = new ArrayList();
            
                // Now, get markables with empty value in attributeName attribute, and collect them in singletons
                for (int t=0;t<tempMarkableList.size();t++)
                {
                    Markable tempMarkable = (Markable)tempMarkableList.get(t);
                    if (tempMarkable.getAttributeValue(attributeName,"empty").equalsIgnoreCase("empty"))
                    {
                        singletons.add(tempMarkable);
                    }
                }
            
                // Now, singletons contains all *relevant* singletons on this level
                for (int q=0;q<singletons.size();q++)
                {
                    HashSet temp = new HashSet();
                    // Add span of singleton markable
                    temp.add(MarkableHelper.getSpan(((Markable)singletons.get(q))));
                    // If not existing yet, add it as a new partition
                    // Attention! This will treat no value cases for the same word by different annos as agreement
                    // Is that desirable? For the two-annotator case, two e.g. vague prons should be counted as agreement.
                    if (allDistinctPartitionSets.contains(temp)==false)
                    {
                        allDistinctPartitionSets.add(temp);
                        System.err.println("NO VALUE: Adding singleton partition for "+MarkableHelper.getSpan(((Markable)singletons.get(q)))+" at pos "+(allDistinctPartitionSets.size()-1)); 
                    }                            
                    // There is no set_id to map this to, so no entry in mapping is created
                    // Create dummy id? No, as that would mean I have to modify the attributes of the singleton markable
                }
            }
                        
            if (treatMissingMarkablesAsSingletons.isSelected())
            {
                // Missing markables on this level are to be treated as singleton sets,
                // i.e. as if they existed but were unassigned.
                // This does only make sense under certain conditions.
                
                // allSpans contains all *displayed* spans, so we have to find those markables
                // in allSpans for which no markable is available in tempMarkableList.
                // It is wrong to just check for existence of any markable on level
                for (int b=0;b<allSpansCurrentlyOnDisplay.size();b++)
                {
                    boolean currentSpanFoundOnCurrentLevel=false;
                    // Get current span in list of all currently displayed spans
                    String currentSpan = (String) allSpansCurrentlyOnDisplay.get(b);                  
                    
                    // Now check whether tempMarkableList contains a markable with that span
                    for (int q=0;q<tempMarkableList.size();q++)
                    {
                        Markable currentMarkable = (Markable)tempMarkableList.get(q);
                        if (MarkableHelper.getSpan(currentMarkable).equalsIgnoreCase(currentSpan))
                        {
                            currentSpanFoundOnCurrentLevel=true;
                            break;
                        }
                    }
                    
                    //if (level.getMarkableAtSpan(currentSpan)==null)
                    if (currentSpanFoundOnCurrentLevel==false)
                    {
                        // No markable with span 'span' exists on current level
                        System.err.println("Markable "+currentSpan+" missing on level!");
                        HashSet temp = new HashSet();
                        // Add span of missing markable
                        temp.add(currentSpan);
                        // If not existing yet, add it as a new partition
                        // Attention! This will treat missing markable cases for the same word by different annos as agreement
                        // Is that desirable? For the two-annotator case, two e.g. vague prons should be counted as agreement.
                        if (allDistinctPartitionSets.contains(temp)==false)
                        {
                            allDistinctPartitionSets.add(temp);
                            System.err.println("NO MARKABLE: Adding singleton partition for "+currentSpan+" at pos "+(allDistinctPartitionSets.size()-1));
                        }                            
                        // There is no set_id to map this to, so no entry in mapping is created
                        // Create dummy id? No, as that would mean I have to modify the attributes of the singleton markable                        
                    }
                }
            }
        }
        
        // The returned mapping will be stored in a list and will be accessible via the index of its level
        return mapping;
    }

    public HashMap getAllDistinctPartitionSets(String attributeName, int attributeType, MarkableLevel level, ArrayList allDistinctPartitionSets)
    {
        // Not used any more 
        // This method returns a HashMap in which set IDs of the form 'set_3' (level-dependent) as keys are 
        // mapped to HashSets as values
        // which contain String representations of all the markables (spans) in the set (level-independent)
        // Each HashMap pertains to one level.
        
        // In addition, *all* distinct partitions are collected in allDistinctPartitionSets. The objects contained
        // in this list are the same HashSet objects as the ones returned in the result HashMap.
        HashMap mapping = new HashMap();
        attributeName = attributeName.toLowerCase();        
        // Get set attribute
        MMAX2Attribute currentSetAttribute = level.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+attributeName+"$", AttributeAPI.MARKABLE_SET);
        if (currentSetAttribute == null)
        {
            System.err.println("Warning: No set attribute '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }
        // Get pertaining relation
        MarkableRelation currentSetRelation = currentSetAttribute.getMarkableRelation();
        if (currentSetRelation == null)
        {
            System.err.println("Warning: No set relation '"+attributeName+"' on level '"+level.getMarkableLevelName()+"'!");
            return null;
        }
        
        // Get all markable sets defined by current relation on current level
        MarkableSet[] allSets = currentSetRelation.getMarkableSets(false);
        // Iterate over them
        for (int z=0;z<allSets.length;z++)
        {
            // Get current set
            MarkableSet currentSet = allSets[z];
            
            // Get hash of strings of current set
            // This is an unordered SET! 
            HashSet temp = currentSet.getSetOfStrings();            
            if (allDistinctPartitionSets.contains(temp)==false)
            {
                allDistinctPartitionSets.add(temp);
            }
            
            // Create mapping from (level-dependent) set_ID to (level-independent) set-representation
            mapping.put(currentSet.getAttributeValue(), temp);
        }        
        
        if (treatEmptySetsAsSingletons.isSelected())
        {
            // Treat singletons as partitions
            // This is no good way of finding singletons, as it does not take into account
            // that there might be a filtering query in action. 
            // Doing it this way will produce many irrelevant partitions
            ArrayList singletons = level.getMatchingMarkables("("+attributeName+"={empty})");
            for (int q=0;q<singletons.size();q++)
            {
                HashSet temp = new HashSet();
                temp.add(MarkableHelper.getSpan(((Markable)singletons.get(q))));
                if (allDistinctPartitionSets.contains(temp)==false)
                {
                    allDistinctPartitionSets.add(temp);
                }            
            }
        }
        // The returned mapping will be stored in a list and will be accessible via the index of its level
        return mapping;
    }
                
    public void updateMarkableDisplay()
    {                   
        boolean suppressUnmatchedMarkablesFromDisplay = suppressUnmatched.isSelected();
        
        ArrayList tempHeader = new ArrayList();
        tempHeader.add("SPAN");
        tempHeader.add("STRING");
        tempHeader.add("ANNO_0");                
    
        // Create new list to store table header tool tips in
        headers = new ArrayList();
        headers.add("");
        headers.add("");
        
        // Create some table-related stuff
        DefaultTableModel dm = new DefaultTableModel() 
        {
             public Class getColumnClass(int columnIndex)
             {
                return String.class;
             }
        };
        
        
        if (majorLevel != null)
        {
            headers.add(majorLevel.getAbsoluteMarkableFileName());
        }
        else
        {
            // No major level has been selected yet, so just tidy up
            tableData = new String[1][1];
            tableData[0][0]="";
            String[] header = new String[2];
            header[0]="SPAN";
            header[1]="STRING";
            
            dm.setDataVector(tableData,header); 
            table.setModel(dm);
            table.updateUI();
            return;            
        }
            
        // Here we know that there is a major level, and that its header has been set already
        if (allMinorLevels != null)
        {
            for (int b=0;b<allMinorLevels.size();b++)
            {
                headers.add(((MarkableLevel)allMinorLevels.get(b)).getAbsoluteMarkableFileName());
            }
        }        
        
        String currentlyActiveQuery="";
        String tempQuery = (String)queryField.getSelectedItem(); 
        if (tempQuery != null && tempQuery.trim().equals("")==false && queryField.isEnabled())
        {
            currentlyActiveQuery = ((String)queryField.getSelectedItem()).trim();
        }
        
        
        // First, get list of all Markables on majorLevel
        ArrayList majorMarkables = null;        
        //if (((String)queryField.getSelectedItem()).trim().equals("")==false && queryField.isEnabled())
        if (currentlyActiveQuery.equals("")==false)
        {
            // If there is currently a query, apply it
            //majorMarkables = majorLevel.getMatchingMarkables(((String)queryField.getSelectedItem()).trim());
            majorMarkables = majorLevel.getMatchingMarkables(currentlyActiveQuery);
        }
        else
        {
            // Otherwise just take all
            majorMarkables = majorLevel.getMarkables(new DiscourseOrderMarkableComparator());
        }
        
        // This list contains all distinct spans found on major level
        ArrayList majorSpans = new ArrayList();
        // This list contains for each minor level a list of all distint spans found on that level
        // Use that for NO MARKABLE, in combination with allSpans?
        ArrayList allMinorSpans = new ArrayList();
        // This list conztains all distinct spans
        ArrayList allSpans = new ArrayList();
        
        // This HashMap contains mappings from markable spans to the underlying markable text string
        HashMap spansToStrings=new HashMap();
        
        // This global list contains for each level at index 0 (major) - n a hashmap
        // mapping markable spans as keys to markable ids as value
        spanToIDMappingsForAllLevels = new ArrayList();
        
        // This contains mappings from a markable span as key to the ID of the pertaining markable as value
        // This is filled for each level, and then added to spanToIDMappingsFirAllLevels, such that
        // the list pos is the number of the pertaining markable level.
        HashMap currentLevelsSpanToIDMappings = new HashMap();
        
        // Iterate over all markables in major level (might be filtered if query was active)
        for (int b=0;b<majorMarkables.size();b++)
        {
            // Get each markable individually
            Markable temp = (Markable)majorMarkables.get(b);
            // Get its span
            String tempSpan = MarkableHelper.getSpan(temp);
            // Add its span to allSpans
            if (allSpans.contains(tempSpan)==false)
            {
                allSpans.add(tempSpan);
            }
            // Add its span to list of spans for major level
            // New: Check for duplicates within a level!
            if (majorSpans.contains(tempSpan)==false)
            {
                majorSpans.add(tempSpan);
            }
            else
            {
                System.err.println("Warning: Duplicate span "+tempSpan+" on ANNO_0 level!");
            }
            // Map markable string to span string (word_5 as value to 'man') Used for the display
            spansToStrings.put(tempSpan, temp.toString());
            // Map span of current major-level markable as key to this markable's ID
            // Note: if there was duplicate warning earlier, this will only contain 
            // the *last* of several instances
            currentLevelsSpanToIDMappings.put(tempSpan, temp.getID());
        }// end iteration over all major-level markables
        
        toOutputArea("\n\nDistinct markable spans in ANNO_0 (pot. filtered): "+majorSpans.size());
        
        // Store hash for major level at position 0 in list
        spanToIDMappingsForAllLevels.add(currentLevelsSpanToIDMappings);
        
        // Iterate over all minor levels
        for (int n=0;n<allMinorLevels.size();n++)
        {
            // Create new hash for each minor level
            currentLevelsSpanToIDMappings = new HashMap();
            // Create list to collect all spans of current minor level
            ArrayList currentMinorSpans = new ArrayList();
            tempHeader.add("ANNO_"+(n+1));
            // Get current minor level
            MarkableLevel tempMinorLevel = (MarkableLevel)allMinorLevels.get(n);
            ArrayList minorMarkables = null;
            
            //if (((String)queryField.getSelectedItem()).trim().equals("")==false && queryField.isEnabled())
            if (currentlyActiveQuery.equals("")==false)
            {
                // Apply current query, if any
                minorMarkables = tempMinorLevel.getMatchingMarkables(currentlyActiveQuery);
            }
            else
            {
                minorMarkables = tempMinorLevel.getMarkables(new DiscourseOrderMarkableComparator());
            }                        
            
            // Iterate over all markables on current minor level
            for (int b=0;b<minorMarkables.size();b++)
            {
                // Get current minor markable
                Markable temp = (Markable)minorMarkables.get(b);
                // Get its span
                String tempSpan = MarkableHelper.getSpan(temp);
                // Add span of current markable to list of spans for current minor
                if (currentMinorSpans.contains(tempSpan)==false)
                {
                    currentMinorSpans.add(tempSpan);
                }
                else
                {
                    System.err.println("Warning: Duplicate span "+tempSpan+" on ANNO_"+(n+1)+" level!");
                }
                // Add mapping of current minor's span to its ID
                currentLevelsSpanToIDMappings.put(tempSpan, temp.getID());                
                
                if (allSpans.contains(tempSpan)==false)
                {
                    // The current minor may contribute a span not yet in allSpans
                    allSpans.add(tempSpan);
                    spansToStrings.put(tempSpan, temp.toString());
                }     
            }
            allMinorSpans.add(currentMinorSpans);
            // Append hash for current minor level to list of hashes.
            spanToIDMappingsForAllLevels.add(currentLevelsSpanToIDMappings);
            toOutputArea("Distinct markable spans in ANNO_"+(n+1)+" (pot. filtered): "+currentMinorSpans.size());                            
        }        
        
        toOutputArea("Total distinct markable spans (pot. filtered): "+allSpans.size());                            
        
        
        // allSpans contains spans of all existing markables according to query. 
        // Maybe useful for NO_MARKABLE ?
        
        // Create list to accept all distinct partitions
        ArrayList allDistinctPartitionSets = new ArrayList();
        // Create a list to accept for each level a HashMap mapping a partition string to the set_ID on
        // the respective level
        ArrayList partitionToSetIDMappingsForAllLevels = new ArrayList();
        
        if (currentAttributeType.equals("set"))
        {
            // The currently selected attribute is of type MARKABLE_SET
            // Add mapping for major level as first in list
            partitionToSetIDMappingsForAllLevels.add(getAllDistinctPartitionSets(currentAttributeName, AttributeAPI.MARKABLE_SET, majorLevel, allDistinctPartitionSets,currentlyActiveQuery,allSpans));
            for (int z=0;z<allMinorLevels.size();z++)
            {
                partitionToSetIDMappingsForAllLevels.add(getAllDistinctPartitionSets(currentAttributeName, AttributeAPI.MARKABLE_SET, (MarkableLevel)allMinorLevels.get(z), allDistinctPartitionSets,currentlyActiveQuery,allSpans));
            }
            // Now, allDistinctPartitionSets contains one instance of each partition resulting from the union
            // of all partitions in all current markable levels
            // If no_values are to be treated as singletons, 
            // Also, partitionToSetIDMappingsForAllLevels contains for each level a mapping of set_IDs to sets
            // in hash representation
            
        }
        
        
        // Convert list of all distinct spans to array 
        String[] tempSpansToOrder = (String[])allSpans.toArray(new String[1]);
        // Sort list of distinct spans. Note: This will not be completely accurate for non-disconts
        java.util.Arrays.sort(tempSpansToOrder,new MarkableSpanComparator());
        // Re-convert to list
        allSpans = new ArrayList(java.util.Arrays.asList(tempSpansToOrder));
        
        ArrayList rowsToHighlightAsNonmatching = new ArrayList();
        ArrayList rowsToHighlightAsMatching = new ArrayList();
        
        // Create list in which to collect those lines that are actually to be displayed
        ArrayList rowsToDisplay = new ArrayList();
        
        // Create list in which to collect at index x the number of suppressed markables at level x
        ArrayList missingSpans = new ArrayList();
        for (int y=0;y<=allMinorLevels.size();y++)
        {
            missingSpans.add(new Integer(0));
        }
        
        // Iterate over all distinct spans
        // Each will produce one line in the display
        for (int line=0;line<allSpans.size();line++)
        {
            // Create array to accept setting for current line
            String[] currentTableLine = new String[allMinorLevels.size()+3];
            // Get current span
            String currentSpan = (String) allSpans.get(line);
            // Store span in first column
            currentTableLine[0] = currentSpan;
            // Store string in second column
            currentTableLine[1] = (String)spansToStrings.get(currentSpan);
             
            // All the following is for the *major* level only!
            
            if (currentAttributeName.equals("no_attribute"))
            {
                // No attribute is to be displayed, just + or - for markables
                // Determine status for major
                if (majorSpans.contains(currentSpan))
                {
                    currentTableLine[2]="+";
                }
                else
                {                    
                    // Count missing span on level 0
                    int t=((Integer)missingSpans.get(0)).intValue();
                    missingSpans.set(0,new Integer(t+1));
                    currentTableLine[2]="-";
                    // Store info that current line contains at least one minus, but store only once
                    if (suppressUnmatchedMarkablesFromDisplay && rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                    {
                        rowsToHighlightAsNonmatching.add(new Integer(line));
                    }
                }
            }
            else if (currentAttributeType.equals("nominal"))
            {
                // Some nominal attribute is selected
                // Get value of this attribute for display
                String val = getAttributeValue(currentAttributeName, currentSpan, 0,spanToIDMappingsForAllLevels);
                // val may be no value or no markable
                // Display it anyway
                currentTableLine[2]=val;
                // Store info that current line contains at least one minus, but store only once
                if (val.equals("NO MARKABLE") && rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                {
                    if (suppressUnmatchedMarkablesFromDisplay)
                    {
                        rowsToHighlightAsNonmatching.add(new Integer(line));
                    }
                    // Count missing span on level 0
                    int t=((Integer)missingSpans.get(0)).intValue();
                    missingSpans.set(0,new Integer(t+1));
                }                                
            }
            else if (currentAttributeType.equals("set"))
            {
                // Some set attribute is selected
                // Get value of this attribute for display. This should be the id of the set valid for major level
                String setID = getAttributeValue(currentAttributeName, currentSpan, 0, spanToIDMappingsForAllLevels);
                if (setID.equalsIgnoreCase("empty"))
                {
                    setID="NO VALUE";
                }                
                // setID may NO VALUE or NO MARKABLE or set_xxx
                if (setID.equals("NO MARKABLE") && rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                {
                    if (suppressUnmatchedMarkablesFromDisplay)
                    {
                        // Store info that current line contains at least one minus, but store only once
                        rowsToHighlightAsNonmatching.add(new Integer(line));                    
                    }
                    // setID is NO MARKABLE !!                    
                    
                    // Count missing span on level 0
                    int t=((Integer)missingSpans.get(0)).intValue();
                    missingSpans.set(0,new Integer(t+1));
                }
                
                // Try to get display value for the current markable
                // SetID may still be NO MARKABLE or NO VALUE or empty
                String val = getDisplayValueForSetID(setID,0,partitionToSetIDMappingsForAllLevels,allDistinctPartitionSets, currentSpan);
                // val is now either a number, or NO VALUE or NO MARKABLE
                
                currentTableLine[2]=val; 
            }          
            // End processing major level                        
            
            
            // Iterate over all minor level spans
            for (int q=0;q<allMinorSpans.size();q++)
            {                
                // Get list containing the spans for all markables on minor level q
                ArrayList tempSpans = (ArrayList) allMinorSpans.get(q);
                
                if (currentAttributeName.equals("no_attribute"))
                {
                    // Determine status for current minor                
                    if (tempSpans.contains(currentSpan))
                    {
                        currentTableLine[q+3]="+";
                    }
                    else
                    {
                        currentTableLine[q+3]="-";
                        // Count missing span on level q+1
                        int t=((Integer)missingSpans.get(q+1)).intValue();
                        missingSpans.set(q+1,new Integer(t+1));

                        if (suppressUnmatchedMarkablesFromDisplay && rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                        {
                            rowsToHighlightAsNonmatching.add(new Integer(line));
                        }                    
                    }
                }
                else if (currentAttributeType.equals("nominal"))
                {
                    String val = getAttributeValue(currentAttributeName, currentSpan, q+1,spanToIDMappingsForAllLevels);
                    currentTableLine[q+3]=val;
                    // Store info that current line contains at least one minus, but store only once
                    if (val.equals("NO MARKABLE") && rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                    {
                        // Count missing span on level q+1
                        int t=((Integer)missingSpans.get(q+1)).intValue();
                        missingSpans.set(q+1,new Integer(t+1));

                        if(suppressUnmatchedMarkablesFromDisplay)
                        {
                            rowsToHighlightAsNonmatching.add(new Integer(line));
                        }
                    }                    
                }
                else if (currentAttributeType.equals("set"))
                {
                    // Some set attribute is selected
                    // Get value of this attribute for display
                    String setID = getAttributeValue(currentAttributeName, currentSpan, q+1,spanToIDMappingsForAllLevels);
                    if (setID.equalsIgnoreCase("empty"))
                    {
                        setID="NO VALUE";
                    }

                    
                    if (setID.equals("NO MARKABLE"))
                    {
                    	// Count missing span on level q+1
                    	int t=((Integer)missingSpans.get(q+1)).intValue();
                    	missingSpans.set(q+1,new Integer(t+1));
                            
                    	if (rowsToHighlightAsNonmatching.contains(new Integer(line))==false && suppressUnmatchedMarkablesFromDisplay)
                    	{
                    		rowsToHighlightAsNonmatching.add(new Integer(line));
                    	}
                    }                    
                    
                    String val = getDisplayValueForSetID(setID,q+1,partitionToSetIDMappingsForAllLevels,allDistinctPartitionSets,currentSpan);
                    
                    currentTableLine[q+3]=val;                
                }  
            }
            
            if (suppressUnmatchedMarkablesFromDisplay == false)
            {
                // All lines are to be displayed
                rowsToDisplay.add(currentTableLine);
            }
            else
            {
                // Only completely matched lines are to be displayed
                // Completely matched are only those that are not marked as to be highlighted
                if (rowsToHighlightAsNonmatching.contains(new Integer(line))==false)
                {
                    rowsToDisplay.add(currentTableLine);
                }
            }
        }

        tableData = new String[rowsToDisplay.size()][allMinorLevels.size()+3];        
        for (int b=0;b<rowsToDisplay.size();b++)
        {
            tableData[b]=(String[])rowsToDisplay.get(b);
        }
        
        toOutputArea("\nDistinct markable spans in display: "+rowsToDisplay.size());
        for (int t=0;t<missingSpans.size();t++)
        {
            toOutputArea(" Missing spans for ANNO_"+t+": "+((Integer)missingSpans.get(t)).intValue());
        }
        
        String[] header = (String[])tempHeader.toArray(new String[1]);
        dm.setDataVector(tableData,header);         
        table.setModel(dm);        
        if (suppressUnmatchedMarkablesFromDisplay)
        {
            // If we do not show suppressed lines, clear list of table lines to be highlighted
            // (wouldn't be correct anyway).
            rowsToHighlightAsNonmatching = new ArrayList();
        }
        table.setDefaultRenderer(String.class, new MultiLineCellRenderer(rowsToHighlightAsNonmatching, allDistinctPartitionSets));
        table.updateUI();
        
        
        
        // Here, the table has been built completely
        int[][]kappaTable=null;
        if (currentAttributeType.equals("nominal"))// && suppressUnmatched.isSelected())
        {
            // If the current attribute is nominal, and suppress nonmatching is active, do Kappa
            kappaTable = doKappa();
            //toOutputArea("\nFilter for Kappa:"+queryField.getText());
            toOutputArea("\nFilter for Kappa:"+((String)queryField.getSelectedItem()));
            toOutputArea("\nKappa_J:\n "+doKappa_J(kappaTable)+"  ('No value' is meta value="+kappaUseNoValueAsMetaValue.isSelected()+" 'No markable' is meta value="+kappaUseNoMarkableAsMetaValue.isSelected()+")");            
        }
        
        if (currentAttributeType.equals("nominal") && allMinorLevels.size()==1)
        {
            toOutputArea("\n"+doConfusionMatrix(kappaTable));
        }        
    }
     
    
    
    
    
    
    public void updateTable()
    {
    	//table must exist
    	
        // Create some table-related stuff
        DefaultTableModel dm = new DefaultTableModel() 
        {
             public Class getColumnClass(int columnIndex)
             {
                return String.class;
             }
        };
                
        // Fill table
        tableData = new String[1][1];
        tableData[0][0]="";
        String[] header = new String[2];
        header[0]="SPAN";
        header[1]="STRING";
        
        dm.setDataVector(tableData,header); 
        table.setModel(dm);
                     
        ArrayList rowsToHighlight = new ArrayList(); // add row numbers to highlight here
        
        table.setDefaultRenderer(String.class, new SimpleMultiLineCellRenderer(rowsToHighlight));
        table.updateUI();                               
        
    }    
    
    
    
    
    
    public final double getSetDistanceWeight (HashSet set0, HashSet set1)
    {
        // Default: disjunct
        // row and col are 0-based, so no modification necessary
        double result = 1.0;
        
        int relation = determineItemIndependentSetRelation(set0, set1);        
                
        if (relation == IDENTITY)
        {
            result = 0.0;
        }
        else if (relation == A_SUBSUMES_B  || relation == B_SUBSUMES_A)
        {
            result = (double)((double)0.33);// * (double)0.33);
        }
        else if (relation == INTERSECTION)
        {
            result = (double)((double)0.67);// * (double)0.66);
        }                        
        return result;
    }
    
    
    public final double determineItemIndependentWeight (int row, int col, ArrayList allDistinctPartitionSets)
    {
        // Default: disjunct
        // row and col are 0-based, so no modification necessary
        double result = 1.0;
        
        HashSet set0 = (HashSet)allDistinctPartitionSets.get(row);
        HashSet set1 = (HashSet)allDistinctPartitionSets.get(col);
        
        int relation = determineItemIndependentSetRelation(set0, set1);        
        
        if (relation == IDENTITY)
        {
            result = 0.0;
        }
        else if (relation == A_SUBSUMES_B  || relation == B_SUBSUMES_A)
        {
            result = (double)((double)0.33);// * (double)0.33);
        }
        else if (relation == INTERSECTION)
        {
            result = (double)((double)0.67);// * (double)0.66);
        }                        
        
        return result;
    }

    public final double determineItemDependentWeight (int row, int col, ArrayList allDistinctPartitionSets, String currentItem)
    {
        // Default: disjunct
        // row and col are 0-based, so no modification necessary
        double result = 1.0;
        
        HashSet set0 = (HashSet)allDistinctPartitionSets.get(row);
        HashSet set1 = (HashSet)allDistinctPartitionSets.get(col);
        
        int relation = determineItemDependentSetRelation(set0, set1, currentItem);        
        
        if (relation == IDENTITY)
        {
            result = 0.0;
        }
        else if (relation == A_SUBSUMES_B  || relation == B_SUBSUMES_A)
        {
            result = (double)((double)0.33);// * (double)0.33);
        }
        else if (relation == INTERSECTION)
        {
            result = (double)((double)0.66);// * (double)0.66);
        }
                        
        return result;
    }
    
    
    
    
           
    
    public final double determineItemDependentSetDelta(HashSet set1, HashSet set2, String currentElement)
    {
        double result = 1.0; //default = disjunct, = max delta
        int relation = determineItemDependentSetRelation(set1, set2, currentElement);
        
        if (relation == IDENTITY)
        {
            result = 0.0;
        }
        else if (relation == A_SUBSUMES_B  || relation == B_SUBSUMES_A)
        {
            result = (double)((double)0.33);// * (double)0.33);
        }
        else if (relation == INTERSECTION)
        {
            result = (double)((double)0.67);// * (double)0.66);
        }
        
        return result;
    }
    
    public final void dumpSquareArray(double[][] array)
    {
    	System.err.println();
        for (int row=0;row<array.length;row++)
        {
            for (int col=0;col<array.length;col++)
            {
                System.err.print(array[row][col]+" ");
            }
            System.err.println();
        }        
    }
    
    
    public final int[][] doKappa()
    {
        double kappa = 0.0;
        // Get Attribute object from major level
        // This is valid for all minor levels as well
        MMAX2Attribute attrib = majorLevel.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+currentAttributeName+"$",AttributeAPI.NOMINAL_BUTTON,AttributeAPI.NOMINAL_LIST);
        // Get list of all defined possible values, ordered alphabetically
        ArrayList tempValues = new ArrayList(attrib.getOrderedValues());

        ArrayList possValues = new ArrayList();
        boolean collapse = confusionMatrixConflateValues.isSelected();
        if (collapse)
        {
            // Several values might be collapsed
            
            possValues = conflationWindow.getConflatedAttributes();
            
            if (kappaUseNoValueAsMetaValue.isSelected())
            {
                possValues.add(";NO VALUE;");
            }

            if (kappaUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add(";NO MARKABLE;");
            }
        }
        else
        {
            // Collapse = false
            // Use normal values as poss values
            possValues = tempValues;
            if (kappaUseNoValueAsMetaValue.isSelected())
            {
                possValues.add("NO VALUE");
            }

            if (kappaUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add("NO MARKABLE");
            }            
        }
                
        // Create major table with one row per span and one column per possible value
        // This table will also include NO VALUE and NO MARKABLE entries, if added above!
        
        int[][] kappaTable = new int[table.getRowCount()][possValues.size()];
        
        double Z = 0.0; // Z = sum over all percentage agreements
        int c = (allMinorLevels.size()+1); // Number of annotators
        int N = table.getRowCount(); // N = total number of markables: Init with full table, N may be reduced later

        // Iterate over all rows in table        
        for (int n=0;n<table.getRowCount();n++)
        {
            // Get Kappa row for current span and poss. values
            int[] tempRow = getKappaRow((String)table.getValueAt(n, 0), possValues, collapse);
                        
            String t ="";
            for (int q=0;q<tempRow.length;q++)
            {
                t=t+tempRow[q]+" ";
            }                                   
            
            if (kappaUseNoMarkableAsMetaValue.isSelected()==false || kappaUseNoValueAsMetaValue.isSelected()==false)
            {
                if (getRowSum(tempRow)!=c)
                {
                    // For the current token, not every annotator has selected a value
                    // So delete by overwriting with zeros
                    for (int q=0;q<tempRow.length;q++)
                    {
                        tempRow[q]=0;
                    }
                    // and reduce number of actual decisions 
                    N--;
                }
            }

            // Assign to actual Kappa table
            kappaTable[n] = tempRow;
            // Get S for current row
            double currentPercentageAgreement = calculateKappaPercentageAgreement(tempRow);
            // Sum over all S's
            Z = Z + currentPercentageAgreement;
        }
        // N may have been reduced by now
        System.err.println("N: "+N);        
        int T = N * c; // T = total number of classification decisions
        System.err.println("Z: "+Z);
        double PE = calculatePE(kappaTable,T,table.getRowCount(),possValues.size());
        System.err.println("PE: "+PE);
        double PA = (double)(Z/N);
        System.err.println("PA: "+PA);
        
        System.err.println("Ignored due to missing value/markable "+(table.getRowCount()-N));
        
        kappa = ((double)(PA - PE))/((double)(1 - PE));
        toOutputArea("\nKappa: "+kappa+" ('No value' is meta value="+kappaUseNoValueAsMetaValue.isSelected()+" 'No markable' is meta value="+kappaUseNoMarkableAsMetaValue.isSelected()+")");
        return kappaTable;
    }

    public final String doKappa_J(int[][]kappaTable)
    {
        double kappa_j = 0.0;
        
        // Get Attribute object from major level
        // This is valid for all minor levels as well
        MMAX2Attribute attrib = majorLevel.getCurrentAnnotationScheme().getUniqueAttributeByNameAndType("^"+currentAttributeName+"$",AttributeAPI.NOMINAL_BUTTON,AttributeAPI.NOMINAL_LIST);
        // Get list of all defined possible values, ordered alphabetically
        ArrayList tempValues = new ArrayList(attrib.getOrderedValues());

        ArrayList possValues = new ArrayList();
        boolean collapse = confusionMatrixConflateValues.isSelected();
        if (collapse)
        {
            // Several values might be collapsed
            
            possValues = conflationWindow.getConflatedAttributes();
            
            if (kappaUseNoValueAsMetaValue.isSelected())
            {
                possValues.add(";NO VALUE;");
            }

            if (kappaUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add(";NO MARKABLE;");
            }
        }
        else
        {
            // Collapse = false
            // Use normal values as poss values
            possValues = tempValues;
            if (kappaUseNoValueAsMetaValue.isSelected())
            {
                possValues.add("NO VALUE");
            }

            if (kappaUseNoMarkableAsMetaValue.isSelected())
            {
                possValues.add("NO MARKABLE");
            }            
        }
        
                
        // Create major table with one row per span and one column per possible value
        // This table will also include NO VALUE and NO MARKABLE entries, if added above!
        double Z = 0.0; // Z = sum over all percentage agreements
        int c = (allMinorLevels.size()+1); // Number of annotators
        int N = table.getRowCount(); // N = total number of markables: Init with full table, N may be reduced later

        String result="";
        for (int j=0;j<possValues.size();j++)
        {
            kappa_j = doKappa_j(kappaTable,j,c);
            String temp=(String)possValues.get(j);
            if (collapse)
            {
                temp = temp.substring(1,temp.length()-1);
            }
            result=result+temp+"\t"+kappa_j+"\n";
        }
        return result;
    }
    
    public final double doKappa_j(int[][]kappaTable, int j, int annos)
    {
        double kappa_j=0.0;
        double P_j=calculateKappaP_j(kappaTable, j, annos);
        double p_j=calculateKappap_j(kappaTable, j, annos);
        kappa_j = (P_j-p_j)/(1-p_j);        
        return kappa_j;
    }
    
    
    public final double calculateKappaP_j(int[][]table, int column, int annos)
    {
        int squaredColumnSum=0;
        int rows = table.length;
        // Move through all rows and sum squared counts
        for (int r=0;r<rows;r++)
        {
            squaredColumnSum=squaredColumnSum+(table[r][column]*table[r][column]);
        }
        double p_j = calculateKappap_j(table, column, annos);
        double numerator = (double)((double)squaredColumnSum-((double)rows*(double)annos*(double)p_j));
        double denominator = (double)((double)rows*(double)annos*(double)(annos-1)*(double)p_j);
        return (double)((double)numerator/(double)denominator);
    }
    
    public final double calculateKappap_j(int[][]table,int column, int annos)
    {
        double p_j=0;       
        int rows = table.length;
        for (int r=0;r<rows;r++)
        {
            p_j=p_j+table[r][column];
        }
        p_j = (double)((double)p_j/(rows*annos));
        return p_j;
    }
    
    public final double getRowSum(double[] row)
    {
        double sum =0;
        for (int b=0;b<row.length;b++)
        {
            sum=sum+row[b];
        }
        return sum;
    }

    
    public final int getRowSum(int[] row)
    {
        int sum =0;
        for (int b=0;b<row.length;b++)
        {
            sum=sum+row[b];
        }
        return sum;
    }
    
    public final double calculatePE(int[][] table, int t, int rows, int cols)
    {
        double PE = 0;
        // Iterate over all columns
        for (int col=0;col<cols;col++)
        {
            int currentSum=0;
            for (int row=0;row<rows;row++)
            {
                currentSum = currentSum+table[row][col];               
            }
            PE = (double)(((double)PE) + ((double)((double)currentSum/(double)t)*(double)((double)currentSum/(double)t)));
        }        
        return PE;
    }
    
    public final int[] getKappaRow(String span, ArrayList possValues, boolean collapse)
    {
        int[] row = new int[possValues.size()];
        // Iterate over all possible values
        for (int b=0;b<possValues.size();b++)
        {
            // Get current possible value
            // If collapse == true, this is possibly complex, and must always be matched as ;xxx;
            String currentValue = (String) possValues.get(b);
            // Reset count for current possible value
            int currentCount = 0;
            // Process major level first
            // Get ID of markable at span on major level
            String currentMarkableID = (String)((HashMap)spanToIDMappingsForAllLevels.get(0)).get(span);            
            String currentMarkableValue = "";
            if (collapse)
            {                
                try
                {
                    currentMarkableValue = majorLevel.getMarkableByID(currentMarkableID).getAttributeValue(currentAttributeName,";NO VALUE;");
                }
                catch (java.lang.NullPointerException ex)
                {
                    currentMarkableValue = ";NO MARKABLE;";
                }
            }
            else
            {
                try
                {
                    currentMarkableValue = majorLevel.getMarkableByID(currentMarkableID).getAttributeValue(currentAttributeName,"NO VALUE");
                }
                catch (java.lang.NullPointerException ex)
                {
                    currentMarkableValue = "NO MARKABLE";
                }                
            }
            
            if (collapse)
            {
                if (currentValue.indexOf(";"+currentMarkableValue+";")!=-1)
                {
                    currentCount++;
                }
            }
            else
            {
                if (currentMarkableValue.equalsIgnoreCase(currentValue))            
                {
                    currentCount++;
                }                
            }
            
            // Process all minor levels
            for (int o=0;o<allMinorLevels.size();o++)
            {
                currentMarkableID = (String)((HashMap)spanToIDMappingsForAllLevels.get(o+1)).get(span);
                if (collapse)
                {    
                    try
                    {
                        currentMarkableValue = ((MarkableLevel)allMinorLevels.get(o)).getMarkableByID(currentMarkableID).getAttributeValue(currentAttributeName,";NO VALUE;");
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        currentMarkableValue = ";NO MARKABLE;";
                    }
                }
                else
                {
                    try
                    {
                        currentMarkableValue = ((MarkableLevel)allMinorLevels.get(o)).getMarkableByID(currentMarkableID).getAttributeValue(currentAttributeName,"NO VALUE");
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        currentMarkableValue = "NO MARKABLE";
                    }                    
                }
                if (collapse)
                {
                    if (currentValue.indexOf(";"+currentMarkableValue+";")!=-1)
                    {
                        currentCount++;
                    }
                }
                else
                {
                    if (currentMarkableValue.equalsIgnoreCase(currentValue))
                    {
                        currentCount++;
                    }
                }
            }
            row[b] = currentCount;
        }        
        return row;
    }
    
    public final double calculateKappaPercentageAgreement(int[] row)
    {
        double agreement = 0.0;
        int rowSum=0;;
        for (int b=0;b<row.length;b++)
        {
            if (row[b]>0)
            {
                rowSum=rowSum+(row[b]*(row[b]-1));
            }
        }
        int annotators = 1 + allMinorLevels.size();
        agreement = (double)((double)1/(annotators*(annotators-1))*rowSum);
        return agreement;
    }
    
    public final void toOutputArea(String message)
    {
        outputArea.append("\n"+message);
    }
    
    public final String getDisplayValueForSetID(String setID, int level, ArrayList partitionToSetIDMappingsForAllLevels, ArrayList allDistinctPartitions, String currentSpan)
    {
        // Get the hashMap that contains ID to set mappings for the current level
        HashMap map = (HashMap)partitionToSetIDMappingsForAllLevels.get(level);
        // Get the partition set that is mapped to setID
        HashSet set = (HashSet)map.get(setID);
        // set may be null if setID was empty, no markable or no value !
        // Get the position of the current partition in the list of all partitions
        int val = allDistinctPartitions.indexOf(set);
        if (val==-1)
        {
            // The current partition is not mapped to any set, i.e. setID is no markable or no value
            if (treatEmptySetsAsSingletons.isSelected() && setID.equalsIgnoreCase("NO VALUE"))
            {
                // The current markable has an empty set, and those should be treated as singletons
                HashSet temp = new HashSet();
                temp.add(currentSpan);
                int tempIndex = allDistinctPartitions.indexOf(temp);
                if (tempIndex == -1)
                {
                    System.err.println("NO VALUE: No mapping of span "+currentSpan+" in allDistinctPartitions!");
                }
                return (tempIndex+1)+"";
            }
            else if (treatMissingMarkablesAsSingletons.isSelected() && setID.equalsIgnoreCase("NO MARKABLE"))
            {
                // The current markable is missing on this level, and those should be treated as singletons
                HashSet temp = new HashSet();
                temp.add(currentSpan);
                int tempIndex = allDistinctPartitions.indexOf(temp);
                if (tempIndex == -1)
                {
                    System.err.println("NO MARKABLE: No mapping of span "+currentSpan+" in allDistinctPartitions!");
                }
                return (tempIndex+1)+"";                
            }                        
            else
            {
                // Return no value or no markable if no mapping was allowed
                return setID;
            }
        }
        else
        {
            // setID was an actual set id
            // Then val is the index in allDistinctPartitions
            // Start set numbering at 1, not zero
            return (val+1)+"";
        }
    }
    
    public final Markable getMarkableFromSpan(String span, int levelPos, MarkableLevel level)
    {
        Markable result = null;
        String ID = "";
        // LevelPos is 0 for anno_0
        // Use global list spanToIDMappingsForAllLevels
        HashMap map = (HashMap) spanToIDMappingsForAllLevels.get(levelPos);
        ID = (String) map.get(span);
        if (ID != null)
        {
            result = level.getMarkableByID(ID);
        }
        return result;
    }
    
    public final String getAttributeValue(String attributeName, String span, int levelPos, ArrayList spanToIDMappingsForAllLevels)
    {        
        // This is used for nominal and set attributes
        // If no markable exists, it returns NO MARKABLE
        // If no value exists, it returns NO VALUE
        // Note: 'empty' is a valid value for set attributes
        // This has to be normalized by the caller
        
        MarkableLevel level = null;
        String ID="";
        // Get level at index levelPos
        if (levelPos == 0)
        {
            level = majorLevel;
        }
        else
        {
            level = (MarkableLevel) allMinorLevels.get(levelPos-1);
        }
        
        // Use global list spanToIDMappingsForAllLevels
        HashMap map = (HashMap)spanToIDMappingsForAllLevels.get(levelPos);
        // map now contains mappings from spans (level-independent) to markable IDs (level-dependent)
        // Get markable id for current span from current level
        ID = (String) map.get(span);
        if (ID == null)
        {
            // The hash for the current level levelPos did not contain a mapping for the current span
            // So there is no markable at this span for this level
            return "NO MARKABLE";
        }
        else
        {
            return level.getMarkableByID(ID).getAttributeValue(attributeName,"NO VALUE");
        }        
    }
    
    public final int determineItemDependentSetRelation(HashSet set1, HashSet set2, String currentItem)
    {
        // This determines the set relation between set 1 and set 2 wrt current item!
        
        // This treats two identical singleton sets as *equal*
     
    	// This is used for the calculation of D_o in the exclusive chain condition
    	
    	// If any of the two sets has one element only before removal, the result will always be *disjunct*
    	// Therefore, treatNonIdentsAsDisjunct should not have any effect on this method
    	
        // Make copy of set1
        HashSet temp1 = new HashSet(set1);
        // Remove ref element from copy, if any
        temp1.remove(currentItem);
        
        // Make copy of set2
        HashSet temp2 = new HashSet(set2);
        // Remove ref element from copy, if any
        temp2.remove(currentItem);
        
        // Make another copy of set1
        HashSet temp3 = new HashSet(set1);
        // Remove ref element from copy, if any
        temp3.remove(currentItem);
        
        int result = DISJUNCTION;
        
        if (temp1.containsAll(temp2) && temp2.containsAll(temp1))
        {
            result = IDENTITY;
        }
        else if (temp1.containsAll(temp2))
        {
            result = A_SUBSUMES_B;
        }
        else if (temp2.containsAll(temp1))
        {
            result = B_SUBSUMES_A;
        }        
        else             
        {
            temp1.retainAll(temp2);
            temp2.retainAll(temp3);
            if (temp1.size()!=0 || temp2.size()!=0)
            {
                result = INTERSECTION;
            }
        }                
        
        // There may be cases where one of the sets is a singleton and the other is a non-singleton
        // containing the singleton: {A} {A,B,C}. 
        // Before remove current: {A,B} {A,B,C}, current item A --> {B}, {B,C} inclusion OK
        // Normally, this would be counted as set inclusion. 
        
        if (treatNonIdenticalSingletonsAsDisjunct.isSelected())
        {
            if (result != IDENTITY && (set1.size()==1 || set2.size()==1))
            {
                result = DISJUNCTION;
            }
        }
        
        return result;
    }

    public final int determineItemIndependentSetRelation(HashSet set1, HashSet set2)
    {    	
        // This determines the set relation between set 1 and set 2.
    	
    	// This is used for the calculation of D_e, because there never is a current item
    	// and for the calculation of D_o in the inclusive condition
                
        // Make copy of set1
        HashSet temp1 = new HashSet(set1);
        
        // Make copy of set2
        HashSet temp2 = new HashSet(set2);
        
        // Make another copy of set1
        HashSet temp3 = new HashSet(set1);
                
        int result = DISJUNCTION;
        
        if (temp1.containsAll(temp2) && temp2.containsAll(temp1))
        {
            result = IDENTITY;
        }
        else if (temp1.containsAll(temp2))
        {
            result = A_SUBSUMES_B;
        }
        else if (temp2.containsAll(temp1))
        {
            result = B_SUBSUMES_A;
        }        
        else             
        {
            temp1.retainAll(temp2);
            temp2.retainAll(temp3);
            if (temp1.size()!=0 || temp2.size()!=0)
            {
                result = INTERSECTION;
            }
        }                
        
        // There may be cases where one of the sets is a singleton and the other is a non-singleton
        // containing the singleton: {A} {A,B,C}. 
        // Normally, this would be counted as set inclusion. However, this is not reasonable.
        
        if (treatNonIdenticalSingletonsAsDisjunct.isSelected())
        {
            // If either or both sets are singletons, do consider the pair disjunct unless it is identical
            if (result != IDENTITY && (set1.size() == 1 || set2.size()==1))
            {
                result = DISJUNCTION;
            }
        }
        
        return result;
    }
    
    
    public int determineIntersectionSize(HashSet set1, HashSet set2, String refElement)
    {
        // This assumes that we already know that both sets intersect!!
        HashSet temp1 = new HashSet(set1);
        temp1.remove(refElement);
        
        HashSet temp2 = new HashSet(set2);
        temp2.remove(refElement);
        
        temp1.retainAll(temp2);
        return temp1.size();
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e) 
    {        
    }
    
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent e) 
    {
        
        if (e.getSource() == table.getTableHeader())
        {
            // The click occurred in the table header
            if (e.getButton() == java.awt.event.MouseEvent.BUTTON3)
            {
                Point xy = e.getPoint();
                int clickedCol = table.columnAtPoint(xy);
                if (clickedCol > 2)
                {
                    requestRemoveColumn(clickedCol-3);
                }
            }
        }
    }
    
    public void requestRemoveColumn(int col)
    {
        allMinorLevels.remove(col);
        permanentSets.clear();
        updateMarkableDisplay();        
    }
    
    public void mouseReleased(java.awt.event.MouseEvent e) 
    {
    	valueChanged(new javax.swing.event.ListSelectionEvent(table, table.getSelectedRow(),table.getSelectedRow(),false));
    }
    
    public void keyPressed(java.awt.event.KeyEvent e) 
    {
        if (e.getSource() == queryField)
        {
            if (e.getKeyChar() == '\n')
            {
                queryButton.doClick();
            }
        }
        
        int code = e.getKeyCode();       
        if (code == KeyEvent.VK_SHIFT)
        {
        	isShiftDown = true;
        }                
        
        
    }
    
    public void keyReleased(java.awt.event.KeyEvent e) 
    { 
        int code = e.getKeyCode();       
        if (code == KeyEvent.VK_SHIFT)
        {
            isShiftDown = false;
        }                        
        else
        {
        	valueChanged(new javax.swing.event.ListSelectionEvent(table, table.getSelectedRow(),table.getSelectedRow(),false));
        }
    }
    
    public void keyTyped(java.awt.event.KeyEvent e) 
    {    	
    	
    }
    
       
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) 
    {    	
        if (e.getValueIsAdjusting())
        {
            return;
        }
        
        int clickedRow = table.getSelectedRow();
        int clickedCol = table.getSelectedColumn(); 
        
        Markable pertainingMarkable = null;
        
        if (clickedCol > 1)
        {
        	// A click occurred in the columns displaying the annotations
            MarkableLevel clickedLevel = null;
            if (clickedCol == 2)
            {
                // The user clicked on the column with the major level markables
                clickedLevel = majorLevel;
            }
            else
            {                       
                // The user clicked on some minor level column
                String tempPath = ((MarkableLevel) allMinorLevels.get(clickedCol-3)).getAbsoluteMarkableFileName();
                clickedLevel = mmax2.getCurrentDiscourse().getMarkableLevelFromAbsoluteFileName(tempPath);
            }
            if (clickedLevel != null && mmax2.getCurrentDiscourse().isCurrentlyLoaded(clickedLevel.getAbsoluteMarkableFileName())==false)
            {
                System.err.println("No clicking allowed here, external !");
                return;
            }                        
            
            // Here we know that the clicked column is internal
            // Get span string in first column of clicked row
            String spanInTable = (String)table.getValueAt(clickedRow,0);
            
            pertainingMarkable = getMarkableFromSpan(spanInTable,clickedCol-2,clickedLevel);
            if (pertainingMarkable == null)
            {
                System.err.println("No markable to display");
                mmax2.getCurrentDiscourse().getCurrentMarkableChart().nothingClicked(MMAX2Constants.LEFTMOUSE);
                return;
            }
            
            // Here we know that there is a markable to display
            mmax2.getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(pertainingMarkable);
            mmax2.setIgnoreCaretUpdate(true);
            try
            {
                mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(pertainingMarkable.getLeftmostDisplayPosition()));
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                System.err.println("Cannot render modelToView()");
            }   
            mmax2.setIgnoreCaretUpdate(false);
            mmax2.getCurrentTextPane().startAutoRefresh();
        }   
                
        else if (clickedCol == 1)
        {    
            // The user clicked on the column containing the markable string.        	
            // Only if currently a set attribute is selected            
            if (currentAttributeType.equals("set"))            
            {
            	// This is supposed to select the DE underlying the markable, and to
            	// unselect any markable

                // Simple clicks only hightlight the markable's DE, without selection
                // Only shift clicks cause permanent chain selection            	
                // This means we want to highlight all sets in the current row, if any
            	
                // Get span displayed at start of clicked row
            	// This is safe to always return a span
                String spanInTable = (String)table.getValueAt(clickedRow,0);
                
                // Get id of markable-to-move to
                String id = "";
                int levelCounter = 0;
                
                Markable moveToMarkable = null;
                MarkableSet moveToSet = null;
                
                // Iterate over all levels in table, until a markable ID is found which is mapped to spanInTable
                // One of them *must* match
                // We start at 0, which means the markable is on major level
                while ((id = (String)((HashMap)spanToIDMappingsForAllLevels.get(levelCounter)).get(spanInTable))==null)
                {
                	levelCounter++;
                }
                
                // Now, id is the ID of a valid markable on level levelCounter
                if (levelCounter == 0)
                {
                	// If levelCounter is 0, a markable was found on the majorLevel
                	// So get it from there
                    moveToMarkable = majorLevel.getMarkableByID(id);
                    
                    // Get corresponding set
                    moveToSet = getMarkableSet(currentAttributeName, id, majorLevel);
                }
                else
                {
                	// A markable was found on minorLevel levelCount-1;
                    moveToMarkable = ((MarkableLevel)allMinorLevels.get(levelCounter-1)).getMarkableByID(id);
                    
                    // Get corresponding set
                    moveToSet = getMarkableSet(currentAttributeName, id, (MarkableLevel)allMinorLevels.get(levelCounter-1));                                        
                }
                // moveToMarkable now is guaranteed to be a valid markable from either the major or some minor level
                // moveToSet is a set, if movetomarkable is a member                                                                                
                                
                MarkableSet set = null;
                if (isShiftDown)
                {
                	// A shift click occurred, so we want to permanently display/remove some sets 
                	String markableSponsoringLevelName = moveToMarkable.getMarkableLevel().getAbsoluteMarkableFileName();
                	
                	boolean markableSponsoringMarkableHasBeenRendered = false;

                	boolean clickToDisplay = false;
                	if (permanentSets.contains(clickedRow+"")==false && permanentSets.contains(clickedRow+"_")==false)
                	{
                		// The current row already is not permanently displayed, so we want to display it
                		clickToDisplay = true;
                	}
                	
                	// Iterate over all minor levels
                	for (int v=0;v<allMinorLevels.size();v++)
                	{
                		// Get the absolute path from the current minor level
                		String tempPath = ((MarkableLevel) allMinorLevels.get(v)).getAbsoluteMarkableFileName();
                		
                		if (tempPath.equalsIgnoreCase(markableSponsoringLevelName))
                		{
                			markableSponsoringMarkableHasBeenRendered = true;
                		}
                		
                		// Get the *actually displayed* level object (hashed from unique absolute file name) 
                		MarkableLevel actualMinorLevel = mmax2.getCurrentDiscourse().getMarkableLevelFromAbsoluteFileName(tempPath);

                		// Get ID of markable which is part in the current set (if any) for the current level
                		id = (String)((HashMap)spanToIDMappingsForAllLevels.get(v+1)).get(spanInTable);	                		
                    
                		// Get the set to highlight (if any)
                		set = getMarkableSet(currentAttributeName, id, actualMinorLevel);
                		
                		if (set != null)
                		{
                			if (clickToDisplay==false)
                			{
                				// A shift click occurred on a currently rendered set
                				// This means: remove
                				set.setIsPermanent(false);
                				mmax2.removeFromRenderingList((Renderable)set);
                				
                				ArrayList allRowsToRemove = getAllAssociatedRows(clickedRow);
                				
                				permanentSets.removeAll(allRowsToRemove);
                			}
                			else 
                			{
                				set.setIsPermanent(true);
                				mmax2.putOnRenderingList((Renderable)set);
                				
                				ArrayList allRowsToAdd = getAllAssociatedRows(clickedRow);
                				
                				permanentSets.addAll(allRowsToAdd);
                			}
                            table.updateUI();
                		}
                	}
                	
                	if (markableSponsoringMarkableHasBeenRendered == false && moveToSet !=null)
                	{
                		if (clickToDisplay==false)
                		{
                			// A shift click occurred on a currently rendered set
                			// This means: remove
                			moveToSet.setIsPermanent(false);
                			mmax2.removeFromRenderingList((Renderable)moveToSet);
                			
                			ArrayList allRowsToRemove = getAllAssociatedRows(clickedRow);
                			
                			permanentSets.removeAll(allRowsToRemove);
                		}
                		else
                		{
                			moveToSet.setIsPermanent(true);
                			mmax2.putOnRenderingList((Renderable)moveToSet);
                			
            				ArrayList allRowsToAdd = getAllAssociatedRows(clickedRow);
            				
            				permanentSets.addAll(allRowsToAdd);

                		}
                		table.updateUI();
                	}
                }               
                                
                // Now update display by scrolling the markable into view
                // This will also happen upon non-shift clicks
                
                if (lastMarkableMovedTo != null)
                {
                	int leftmostDisplayPosition = lastMarkableMovedTo.getLeftmostDisplayPosition();
                	int rightmostDisplayPosition = lastMarkableMovedTo.getRightmostDisplayPosition();
                	
                	if (leftmostDisplayPosition == 0 && rightmostDisplayPosition == 0)
                	{
                		// The current lastMarkableMovedTo is an external one, for which no
                		// displaypositions have been set
                		// So get display positions from this discourse
                		leftmostDisplayPosition = discourse.getDisplayStartPositionFromDiscoursePosition(lastMarkableMovedTo.getLeftmostDiscoursePosition());
                		rightmostDisplayPosition = discourse.getDisplayEndPositionFromDiscoursePosition(lastMarkableMovedTo.getRightmostDiscoursePosition());
                	}
                	                	
                    MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
                    doc.startChanges(leftmostDisplayPosition,(rightmostDisplayPosition-leftmostDisplayPosition)+1);
                    //doc.startChanges(lastMarkableMovedTo);
                    lastMarkableMovedTo.renderMe(MMAX2Constants.RENDER_UNSELECTED);                                
                    doc.commitChanges();                    
                }                
            	
                if (moveToMarkable != null)
                {
                	int leftmostDisplayPosition = moveToMarkable.getLeftmostDisplayPosition();
                	int rightmostDisplayPosition = moveToMarkable.getRightmostDisplayPosition();
                	
                	if (leftmostDisplayPosition == 0 && rightmostDisplayPosition == 0)
                	{
                		// The current moveToMarkable is an external one, for which no
                		// displaypositions have been set
                		// So get display positions from this discourse
                		leftmostDisplayPosition = discourse.getDisplayStartPositionFromDiscoursePosition(moveToMarkable.getLeftmostDiscoursePosition());
                		rightmostDisplayPosition = discourse.getDisplayEndPositionFromDiscoursePosition(moveToMarkable.getRightmostDiscoursePosition());
                	}
                	                	                	
                    MMAX2Document doc = mmax2.getCurrentDiscourse().getDisplayDocument();
                    doc.startChanges(leftmostDisplayPosition,(rightmostDisplayPosition-leftmostDisplayPosition)+1);
                    moveToMarkable.renderMe(MMAX2Constants.RENDER_IN_SEARCHRESULT);                                
                    doc.commitChanges();
                    
                    mmax2.setIgnoreCaretUpdate(true);
                    try
                    {
                        mmax2.getCurrentTextPane().scrollRectToVisible(mmax2.getCurrentTextPane().modelToView(rightmostDisplayPosition));
                    }
                    catch (javax.swing.text.BadLocationException ex)
                    {
                        System.err.println("Cannot render modelToView()");
                    }   
                    mmax2.setIgnoreCaretUpdate(false);
                    mmax2.getCurrentTextPane().startAutoRefresh();
                }
                lastMarkableMovedTo = moveToMarkable;
            }
        }
    }
    
    public void windowActivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosing(java.awt.event.WindowEvent e) 
    {
    	mmax2.emptyRenderingList(true);
    	discourse.getCurrentMarkableChart().nothingClicked(MMAX2Constants.RIGHTMOUSE);
    }
    
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowIconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowOpened(java.awt.event.WindowEvent e) {
    }
    
    
    public ArrayList getAllAssociatedRows(int row)
    {
    	ArrayList result = new ArrayList();
    	ArrayList toCollect = new ArrayList();
    	// Iterate over all columns in row row (Note: This may be more than two)
    	for (int b=2;b<table.getColumnCount();b++)
    	{
    		// Get the cell value
    		String val = (String)table.getValueAt(row,b);
    		// Skip meta value
    		if (val.equalsIgnoreCase("no value") || val.equalsIgnoreCase("no markable"))
    		{
    			continue;
    		}
    		// Retrieve set id
    		if (val.indexOf(' ')!=-1)
    		{
    			val = val.substring(0,val.indexOf(' '));
    		}
    		// Store set id only once
    		if (toCollect.contains(val)==false)
    		{
    			toCollect.add(val);
    		}
    	}    	

    	System.err.println("Sets to collect for row "+row+": "+toCollect);
    	
    	// Iterate over entire table
    	for (int u=0;u<table.getRowCount();u++)
    	{
    		// Iterate over all columns
    		for (int b=2;b<table.getColumnCount();b++)
    		{
    			// Get cell value
    			String val = (String)table.getValueAt(u,b);
    			// Skip meta values
    			if (val.equalsIgnoreCase("no value") || val.equalsIgnoreCase("no markable"))
    			{
    				continue;
    			}
    			if (val.indexOf(' ')!=-1)
    			{
    				val = val.substring(0,val.indexOf(' '));
    			}
    			if (toCollect.contains(val) && result.contains(u+"")==false && result.contains(u+"_")==false)
    			{
    				if (u != row)
    				{
    					result.add(u+"");
    				}
    				else
    				{
    					result.add(u+"_");
    				}
    			}
    		}
    	}    	    	
    	
    	System.err.println("Rows associated to row "+row+": "+result);
    	return result;
    }
    
    
    
    
    class MultiLineCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer 
    {
        ArrayList rowsToHighlightAsNonmatching = null;
        ArrayList allDistinctPartitionSets = null;
        
        public MultiLineCellRenderer(ArrayList _rowsToHighlightAsNonmatching, ArrayList _allDistinctPartitionSets) 
        {
            rowsToHighlightAsNonmatching = _rowsToHighlightAsNonmatching;
            allDistinctPartitionSets = _allDistinctPartitionSets;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
               boolean isSelected, boolean hasFocus, int row, int column) 
        {
            
        // Declare set-relation variable here to have it available for setting the value
        int rel = -1;
        int intersectionSize = -1;
        int set1Size=-1;
        int set2Size=-1;
                        
        String refExpression=(String)table.getValueAt(row, 0);
        
        if (rowsToHighlightAsNonmatching.contains(new Integer(row)))
        {
            // The current line is marked as non-matching            
            if (column > 1)
            {
                // The column is one that has to be displayed in red
                String temp = (String) value;
                if (currentAttributeType.equals("no_attribute") && temp.equals("-"))
                {
                    setForeground(table.getForeground());
                    setBackground(Color.RED);
                }
                else if (currentAttributeType.equals("nominal") && temp.equals("NO MARKABLE"))
                {
                    setForeground(table.getForeground());
                    setBackground(Color.RED);
                }                    
                else if (currentAttributeType.equals("set") && temp.equals("NO MARKABLE"))
                {
                    setForeground(table.getForeground());
                    setBackground(Color.RED);
                }   
                else if (currentAttributeType.equals("pointer") && temp.equals("NO MARKABLE"))
                {
                    setForeground(table.getForeground());
                    setBackground(Color.RED);
                }                                   
                else                    
                {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
            }
            else
            {
                // Column is not > 1
                // So the first part of the line is never displayed in red
                setForeground(table.getForeground());
                setBackground(table.getBackground());                
            }
        }
        else
        {
            
            // The current line is not marked as non-matching
            // Only then does any other cell highlighting apply
            if (table.getColumnCount() == 4 && currentAttributeType.equals("set"))
            {
                // Use set-relation-related coloring/markup only for two annotation of type set
                int val1 = -1;
                int val2 = -1;
                if (column == 2)
                {   
                    // The current column is the one of the major level
                    try
                    {
                        // Read string value from the current line, and convert to numbers (if possible)
                        // Set numbering starts at 1, so subtract 1 to get actual list index

                        val1 = Integer.parseInt((String) table.getValueAt(row,2))-1; // col 2
                        val2 = Integer.parseInt((String) table.getValueAt(row,3))-1; // col 3
                        // Get sets at positions val1 and val2, and determine set relation
                        // val1 is always left of val2 in the table
                        set1Size = ((HashSet)allDistinctPartitionSets.get(val1)).size();                        
                        rel = determineItemDependentSetRelation((HashSet)allDistinctPartitionSets.get(val1),(HashSet)allDistinctPartitionSets.get(val2),refExpression);
                    }
                    catch (java.lang.NumberFormatException ex)
                    {
                        //ex.printStackTrace();
                    }
                }
                else if (column == 3)
                {
                    // The current column is the one of the (only) minor level
                    try
                    {
                        // Read string value from the current line, and convert to numbers (if possible)
                        // Set numbering starts at 1, so subtract 1 to get actual list index                    
                        val1 = Integer.parseInt((String) table.getValueAt(row,2))-1; // col 2
                        val2 = Integer.parseInt((String) table.getValueAt(row,3))-1; // col 3

                        set2Size = ((HashSet)allDistinctPartitionSets.get(val2)).size();
                        // Get sets at positions val1 and val2, and determine set relation
                        // val1 is always left of val2 in the table
                        rel = determineItemDependentSetRelation((HashSet)allDistinctPartitionSets.get(val1),(HashSet)allDistinctPartitionSets.get(val2),refExpression);
                    }
                    catch (java.lang.NumberFormatException ex)
                    {
                        //ex.printStackTrace();
                    }                                
                }
                
                if (rel == IDENTITY)
                {
                    setBackground(IDENTITY_COLOR);
                    setForeground(table.getForeground());
                }
                else if (rel == A_SUBSUMES_B || rel == B_SUBSUMES_A)
                {
                    setBackground(SUBSUMPTION_COLOR);
                    setForeground(table.getForeground());
                }
                else if (rel == INTERSECTION)
                {
                    setBackground(INTERSECTION_COLOR);
                    setForeground(table.getForeground());
                    intersectionSize = determineIntersectionSize((HashSet)allDistinctPartitionSets.get(val1),(HashSet)allDistinctPartitionSets.get(val2),refExpression);
                }
                else if (rel == DISJUNCTION)
                {
                    setBackground(DISJUNCTION_COLOR);
                    setForeground(Color.WHITE);
                }
                else
                {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
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
        if (rel == -1)
        {
            // Set plain text
            setText((value == null) ? "" : value.toString());
        }
        else // we are paiting cols 2 or 3
        {
            if (column == 2)
            {
                if (rel == A_SUBSUMES_B)
                {
                    setText((value == null) ? "" : value.toString()+" subsumes ("+set1Size+")");
                }
                else if (rel == B_SUBSUMES_A)
                {
                    setText((value == null) ? "" : value.toString()+" subsumed by ("+set1Size+")");
                }
                else if (rel == DISJUNCTION)
                {
                    setText((value == null) ? "" : value.toString()+" disjoint ("+set1Size+")");
                }                                
                else if (rel == INTERSECTION)
                {
                    setText((value == null) ? "" : value.toString()+" intersects ("+set1Size+"/"+intersectionSize+")");
                }                
                else if (rel == IDENTITY)
                {
                    setText((value == null) ? "" : value.toString()+" equals ("+set1Size+")");
                }                                
                else
                {
                    // Set plain text
                    setText((value == null) ? "" : value.toString());                    
                }
            }
            else if (column == 3)
            {
                // Set text plus size in first minor column
                setText((value == null) ? "" : value.toString()+" ("+set2Size+")");
            }
            else
            {
                setText((value == null) ? "" : value.toString());
            }
        }
        
        if (column == 1 && permanentSets.contains(row+""))
        {
            setBackground(Color.ORANGE);
            setForeground(table.getForeground());
        }
        else if (column >=1 && permanentSets.contains(row+"_"))
        {
            setBackground(Color.ORANGE);
            setForeground(table.getForeground());
        }
        
        else
        {
            setForeground(table.getForeground());
            setBackground(table.getBackground());                                
        }
                
        return this;        
        }
    }     
    

    class SimpleMultiLineCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer 
    {
        ArrayList rowsToHighlight = null;
        
        public SimpleMultiLineCellRenderer(ArrayList _rowsToHighlight) 
        {
            rowsToHighlight = _rowsToHighlight;
            setOpaque(true);
        }

        // This method is called automacically by java for each tabel row and col
        public Component getTableCellRendererComponent(JTable table, Object value,
               boolean isSelected, boolean hasFocus, int row, int column) 
        {            
        
        if (rowsToHighlight.contains(new Integer(row)))
        {
        	
            setForeground(table.getForeground());
            setBackground(Color.RED);

        }
                
        return this;        
        }
    }     
    
    
    
    
    public class MMAX2AttributeConflationWindow extends JFrame implements WindowListener
    {
        Box rightBox = null;
        MMAX2AnnotationDiffWindow window = null;
        
        /** Creates a new instance of MMAX2AttributeCollapseWindow */
        public MMAX2AttributeConflationWindow(ArrayList _values, MMAX2AnnotationDiffWindow _window) 
        {
            setTitle("Conflate");
            window = _window;
            addWindowListener(this);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            rightBox = Box.createVerticalBox();        
            for (int z=0;z<_values.size();z++)
            {
                ArrayList indices = new ArrayList();
                for (int v=0;v<_values.size();v++)
                {
                    indices.add(new String(v+" "+(String)_values.get(z)));
                }                        
                JComboBox box = new JComboBox(indices.toArray());
                box.setSelectedIndex(z);
                box.setFont(MMAX2.getStandardFont());
                rightBox.add(box);
            }
            getContentPane().add(rightBox);   
            setVisible(true);
            pack();
        }   
        
        public ArrayList getConflatedAttributes()
        {
            HashMap mappings = new HashMap();
            ArrayList result = new ArrayList();
            // Get number of components in box
            int count = rightBox.getComponentCount();
            // Iterate over components in box (one per attribute)
            for (int b=0;b<count;b++)
            {
                JComboBox tempBox = (JComboBox)rightBox.getComponent(b);
                // Get entire text of combo box entry
                String text = (String)tempBox.getSelectedItem();
                // Get first part, indicating the index
                String index = text.substring(0,text.indexOf(" "));
                String attribute = text.substring(text.indexOf(" ")+1);
                System.err.println("'"+index+"' '"+attribute+"'");
                // See if some other attribute has been mapped to this index already
                String currentMapping = (String) mappings.get(index);
                if (currentMapping == null)
                {
                    mappings.put(index, attribute+";");
                }
                else
                {
                    mappings.put(index, currentMapping+attribute+";");
                }
            }
            Set keys = mappings.keySet();
            Iterator iti = keys.iterator();
            while (iti.hasNext())
            {
                String key = (String)iti.next();
                result.add(";"+(String)mappings.get(key));
            }
            
            String[] temp = (String[])result.toArray(new String[0]);
            // Sort alphabetically
            Arrays.sort(temp);
            result = new ArrayList(Arrays.asList(temp));
            return result;
        }
        
        public void windowActivated(java.awt.event.WindowEvent e) {
        }
        
        public void windowClosed(java.awt.event.WindowEvent e) 
        {
        }
        
        public void windowClosing(java.awt.event.WindowEvent e) 
        {        
            window.conflationWindow = null;
            // When the collapse window is closed, collapse is set to false
            window.confusionMatrixConflateValues.setSelected(false);
            dispose();        
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
    
}
