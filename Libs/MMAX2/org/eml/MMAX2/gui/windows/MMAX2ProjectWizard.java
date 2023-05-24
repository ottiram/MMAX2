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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.utils.MMAX2Utils;

/**
 *
 * @author  mueller
 */
public class MMAX2ProjectWizard extends javax.swing.JFrame implements java.awt.event.ActionListener, java.awt.event.WindowListener, javax.swing.event.DocumentListener, java.awt.event.MouseListener
{
    boolean bulkImportIsRunning = false;
    ArrayList batchFiles = null;
    HashMap manualDecisions = new HashMap();    
    
    MMAX2 mmax2=null;
    // Contains the name of the text file to tokenize
    JTextField infileName=null;
    // Contains the complete path to the text file to tokenize
    String infilePath="";
    // Triggers selection of infileName
    JButton selectInfileNameButton=null;
    // Contains all available text file encodings
    JComboBox inputEncodings=null;
    // Triggers pre-read and shallow analysis of selected text file to tokenize
    JButton analyseInfile=null;    
    // Triggers tokenization of selected infile
    JButton testTokenization=null;
    // Triggers addition of another markable level
    JButton addMarkableLevelButton=null;
    // Triggers creation of .mmax project
    JButton createProjectButton=null;
    
    JTextField projectFileName=null;    
    JTextField projectPathName=null;
    JButton selectProjectPathName=null;
    JButton setAllPaths=null;
    
    JTextField wordFileName=null;

    JTextField basedataPath=null;
    JButton chooseBasedataPath=null;
    JTextField schemePath=null;
    JButton chooseSchemePath=null;    
    JTextField stylePath=null;
    JButton chooseStylePath=null;
    JTextField customizationPath=null;
    JButton chooseCustomizationPath=null;
    JTextField markablePath=null;
    JButton chooseMarkablePath=null;

    JPanel markableLevelPanel=null;
    
    String tokenFileName="";
    
    JTextField leadingTokens=null;
    JLabel splitLeadingLabel=null;
    JTextField trailingTokens=null;
    JLabel splitTrailingLabel=null;
    
    JCheckBox convertLeadingQuote=null;
    JCheckBox convertTrailingQuote=null;
    JCheckBox useGlobalReplacementList = null;
    JCheckBox useAbbreviationList=null;
    JCheckBox useAbbreviationHeuristics=null;
    JCheckBox allowMultipleInputFiles=null;
    JCheckBox treatAsXML=null;
    JCheckBox inputAlreadyTokenized=null;
    
    JCheckBox doNotAskBeforeOverwriting=null;
    
    boolean validTokenization=false;
    int numMarkableLevels=0;
    
    JCheckBox createDefaultCustomizations=null;
    JCheckBox createDefaultSchemes=null;
    
    JLabel handleColumnHeader=null;
    JLabel colorColumnHeader=null;
    JLabel styleColumnHeader=null;
    JLabel crColumnHeader=null;
    
    ArrayList tagsInInput=null;
    
    Box markableLevelBox=null;
    
    int emptyLineCount=0;
    ArrayList completedTokens=null;
    
    boolean bulkMode = false;
        
    /** Creates a new instance of MMAX2ProjectWizard */
    public MMAX2ProjectWizard(MMAX2 _mmax2) 
    {
        super();
        
        tagsInInput= new ArrayList();
        
        addWindowListener(this);
        mmax2 = _mmax2;
        setTitle("MMAX2 Project Wizard");
        setResizable(false);
        
        // Stack panels in this box from top to bottom. This is added to contentPane!
        Box outerBox = Box.createVerticalBox();
        
        // Upper panel to contain info about input file
        JPanel infilePanel = new JPanel();      
        infilePanel.setBorder(new TitledBorder("Text Input File"));                       
        // Make components IN this panel left-align
        infilePanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        
        Box tempOuterBox = Box.createVerticalBox();
        
        // Box to contain components in infilePanel from left to right
        Box tempBox= Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(20));
        JLabel tempLabel = new JLabel("Input File:");
        tempBox.add(tempLabel);
        tempBox.add(Box.createHorizontalStrut(10));
        infileName = new JTextField("",20);
        infileName.setPreferredSize(new Dimension(80, infileName.getHeight()));
        infileName.setEditable(false);
        tempBox.add(infileName);
        selectInfileNameButton = new JButton("Pick");
        selectInfileNameButton.setFont(selectInfileNameButton.getFont().deriveFont((float)10));
        selectInfileNameButton.setBorder(new EmptyBorder(0,0,1,1));
        
        selectInfileNameButton.setActionCommand("choose_infile");
        selectInfileNameButton.addActionListener(this);
        tempBox.add(Box.createHorizontalStrut(10));
        tempBox.add(selectInfileNameButton);
        tempBox.add(Box.createHorizontalStrut(20));
        tempLabel = null;
        tempLabel = new JLabel("Encoding:");
        tempBox.add(tempLabel);        
        tempBox.add(Box.createHorizontalStrut(10));                
        inputEncodings = new JComboBox(Charset.availableCharsets().keySet().toArray());
        inputEncodings.setActionCommand("encoding_changed");
        inputEncodings.setSelectedItem("US-ASCII");
        inputEncodings.addActionListener(this);
        tempBox.add(inputEncodings);
        tempBox.add(Box.createHorizontalStrut(20));
        tempOuterBox.add(tempBox);
        
        JPanel tempPanel = new JPanel();
        tempBox = null;
        tempBox = Box.createHorizontalBox();

        tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0, 5));
        
        allowMultipleInputFiles = new JCheckBox("Do not reset after input file selection");
        tempBox.add(allowMultipleInputFiles);
        tempBox.add(Box.createHorizontalStrut(10));
        
 //       tempPanel.add(Box.createHorizontalStrut(10));
 //      tokenizationResultPane = new JTextPane();
 //       tokenizationResultPane.setPreferredSize(new Dimension(300,100));
 //       tokenizationResultPane.setEditable(false);
 //       DefaultStyledDocument resultDocument = new DefaultStyledDocument();
 //       AttributeSet attribs = new SimpleAttributeSet();
 //       try
 //       {
 //       	resultDocument.insertString(0,"\n\n\n\n\n\n\n\n\n\n", attribs);
 //       }
 //       catch (Exception ex)
 //       {
 //       	ex.printStackTrace();
 //       }
 //       tempPanel.add(tokenizationResultPane);

        treatAsXML = new JCheckBox("XML");
        treatAsXML.setActionCommand("treat_as_xml");
        treatAsXML.addActionListener(this);
        tempBox.add(treatAsXML);
        tempBox.add(Box.createHorizontalStrut(10));
        analyseInfile = new JButton("Analyse File");
        analyseInfile.setSize(inputEncodings.getWidth(),analyseInfile.getHeight());
        analyseInfile.setActionCommand("analyse_infile");
        analyseInfile.addActionListener(this);
        analyseInfile.setEnabled(false);

        tempBox.add(analyseInfile);
        tempBox.add(Box.createHorizontalStrut(20));
        tempPanel.add(tempBox);

        tempOuterBox.add(Box.createHorizontalStrut(650));
        tempOuterBox.add(tempPanel);
        infilePanel.add(tempOuterBox);
        outerBox.add(infilePanel);

        JPanel tokenizationPanel = new JPanel();
        tokenizationPanel.setBorder(new TitledBorder("Tokenization"));
        tokenizationPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        
        tempOuterBox = null;
        tempOuterBox = Box.createVerticalBox();        
        tempOuterBox.add(Box.createHorizontalStrut(650));
        JPanel tempPanel2 = new JPanel();
        tempPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        
        tempBox = null;
        tempBox= Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(20));
        tempLabel = null;
        splitLeadingLabel = new JLabel("Split leading");
        tempBox.add(splitLeadingLabel);
        tempBox.add(Box.createHorizontalStrut(10));        
        leadingTokens = new JTextField("( \"",8);
        leadingTokens.setEditable(true);
        leadingTokens.getDocument().addDocumentListener(this);
        tempBox.add(leadingTokens);
        tempBox.add(Box.createHorizontalStrut(10));
        convertLeadingQuote = new JCheckBox("Convert \" to _oq_");
        convertLeadingQuote.setActionCommand("tokenization_parameters_changed");
        convertLeadingQuote.addActionListener(this);
        convertLeadingQuote.setSelected(false);        
        tempBox.add(convertLeadingQuote);
        tempBox.add(Box.createHorizontalStrut(10));
        useGlobalReplacementList = new JCheckBox("Use global replacement list");
        useGlobalReplacementList.setActionCommand("tokenization_parameters_changed");
        useGlobalReplacementList.addActionListener(this);
        tempBox.add(useGlobalReplacementList);
        tempPanel2.add(tempBox);
        tempOuterBox.add(tempPanel2);
        
        tempOuterBox.add(Box.createVerticalStrut(10));
        tempPanel2=null;
        tempPanel2 = new JPanel();
        tempPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        tempBox = null;
        tempBox= Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(20));
        tempLabel = null;
        splitTrailingLabel = new JLabel("Split trailing");
        tempBox.add(splitTrailingLabel);
        tempBox.add(Box.createHorizontalStrut(12));
        trailingTokens = new JTextField(", ; \" ) ! ? : .",8);
        trailingTokens.setEditable(true);
        trailingTokens.getDocument().addDocumentListener(this);
        tempBox.add(trailingTokens);
        tempBox.add(Box.createHorizontalStrut(10));
        convertTrailingQuote = new JCheckBox("Convert \" to _cq_");
        convertTrailingQuote.setActionCommand("tokenization_parameters_changed");
        convertTrailingQuote.setSelected(false);
        convertTrailingQuote.addActionListener(this);
        tempBox.add(convertTrailingQuote);        
        tempBox.add(Box.createHorizontalStrut(10));
        useAbbreviationList = new JCheckBox("Use abbrev. list");
        useAbbreviationList.setActionCommand("tokenization_parameters_changed");
        useAbbreviationList.addActionListener(this);

        tempBox.add(useAbbreviationList);
        tempBox.add(Box.createHorizontalStrut(10));
        useAbbreviationHeuristics = new JCheckBox("Use abbrev. heuristics");
        useAbbreviationHeuristics.setActionCommand("tokenization_parameters_changed");
        useAbbreviationHeuristics.addActionListener(this);
        tempBox.add(useAbbreviationHeuristics);
        tempPanel2.add(tempBox);
        tempOuterBox.add(tempPanel2);        
               
        testTokenization = new JButton("Tokenize");
        testTokenization.setActionCommand("tokenize");
        testTokenization.addActionListener(this);
        testTokenization.setEnabled(false);
       
        inputAlreadyTokenized = new JCheckBox("Input file is one token per line");
        inputAlreadyTokenized.setActionCommand("input_already_tokenized");
        inputAlreadyTokenized.addActionListener(this);
        inputAlreadyTokenized.setToolTipText("Check this to let input file pass through tokenizer unaltered");
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(inputAlreadyTokenized);
        tempBox.add(Box.createHorizontalStrut(10));       
        tempBox.add(testTokenization);      
        tempBox.add(Box.createHorizontalStrut(20));       
        tempPanel = null;
        tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,0, 5));  
        tempPanel.add(tempBox);
        tempOuterBox.add(tempPanel);        
        tokenizationPanel.add(tempOuterBox);       
        outerBox.add(tokenizationPanel);                        
        
        markableLevelPanel = new JPanel();      
        markableLevelPanel.setBorder(new TitledBorder("Markable Levels"));                       
        // Make components IN this panel left-align
        markableLevelPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        
        // This box receives the markable levels
        markableLevelBox = Box.createVerticalBox();
        markableLevelBox.add(Box.createHorizontalStrut(650));
        tempPanel = null;
        tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0, 0));  
        
        Box headerBox = Box.createHorizontalBox();
        headerBox.add(Box.createHorizontalStrut(20));
        Box partBox = Box.createVerticalBox();
        JLabel tmpLabel = new JLabel("Name:");
        tmpLabel.setVisible(false);
        partBox.add(tmpLabel);
        headerBox.add(partBox);
        partBox=null;
        
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        tmpLabel=null;
        tmpLabel = new JLabel("Source:");
        tmpLabel.setVisible(false);
        partBox.add(tmpLabel);        

        headerBox.add(partBox);
        partBox=null;        

        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        tmpLabel=null;
        tmpLabel = new JLabel("Specify:");
        tmpLabel.setVisible(false);
        partBox.add(tmpLabel);        
       
        headerBox.add(partBox);
        partBox=null;        
               
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        tmpLabel=null;
        tmpLabel = new JLabel("Handles:");
        tmpLabel.setVisible(false);
        partBox.add(tmpLabel);        
        
        headerBox.add(partBox);
        partBox=null;        
        headerBox.add(Box.createHorizontalStrut(3));        
        partBox = Box.createVerticalBox();
        colorColumnHeader = new JLabel("Color:");                
        colorColumnHeader.setVisible(false);
        partBox.add(colorColumnHeader);
        headerBox.add(partBox);
        partBox=null;        

        headerBox.add(Box.createHorizontalStrut(3));        
        partBox = Box.createVerticalBox();
        styleColumnHeader = new JLabel("Style:");        
        styleColumnHeader.setVisible(false);
        partBox.add(styleColumnHeader);
        headerBox.add(partBox);
        partBox=null;        
        
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        crColumnHeader = new JLabel("CR:"); 
        crColumnHeader.setVisible(false);
        partBox.add(crColumnHeader);
        headerBox.add(partBox);
        partBox=null;        
        
        // up
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        partBox.add(new JLabel(" "));
        headerBox.add(partBox);
        partBox=null;        

        // down
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        partBox.add(new JLabel(" "));
        headerBox.add(partBox);
        partBox=null;        
        
        // delete
        headerBox.add(Box.createHorizontalStrut(3));
        partBox = Box.createVerticalBox();
        partBox.add(new JLabel(" "));
        headerBox.add(partBox);
        partBox=null;                        

        tempPanel.add(headerBox);
        markableLevelBox.add(tempPanel);
        markableLevelPanel.add(markableLevelBox);        
                
        // This panel contains buttons to add/delete/move levels
        JPanel markableLevelControlPanel = new JPanel();
        markableLevelControlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,0,5)); 
        
        tempBox = null;
        tempBox = Box.createHorizontalBox();
       
        addMarkableLevelButton= new JButton("Add level");
        addMarkableLevelButton.setActionCommand("add_level");
        addMarkableLevelButton.addActionListener(this);
        tempBox.add(addMarkableLevelButton);
        tempBox.add(Box.createHorizontalStrut(20));
        markableLevelControlPanel.add(tempBox);
        markableLevelBox.add(markableLevelControlPanel);
        outerBox.add(markableLevelPanel);
        
        JPanel projectPanel = new JPanel();      
        projectPanel.setBorder(new TitledBorder(".MMAX Project"));                       
        // Make components IN this panel left-align
        projectPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        
        tempOuterBox = null;
        tempOuterBox = Box.createVerticalBox();
        tempOuterBox.add(Box.createHorizontalStrut(650));
        
        JPanel temperPanel = new JPanel();
        temperPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));                 
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Project File Name:"));
        tempBox.add(Box.createHorizontalStrut(16));
        projectFileName = new JTextField();
        projectFileName.getDocument().addDocumentListener(this);
        projectFileName.setPreferredSize(new Dimension(80, projectFileName.getHeight()));
        tempBox.add(projectFileName);
        tempBox.add(new JLabel(".mmax"));
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Project Path:"));
        tempBox.add(Box.createHorizontalStrut(10));
        projectPathName = new JTextField();
        projectPathName.setPreferredSize(new Dimension(150, projectPathName.getHeight()));
        projectPathName.setEditable(false);

        tempBox.add(projectPathName);
        tempBox.add(Box.createHorizontalStrut(10));
        selectProjectPathName = new JButton("Pick");
        selectProjectPathName.setFont(selectProjectPathName.getFont().deriveFont((float)10));
        selectProjectPathName.setBorder(new EmptyBorder(0,0,1,1));
        selectProjectPathName.setActionCommand("choose_projectpath");
        selectProjectPathName.addActionListener(this);
        tempBox.add(selectProjectPathName);
        tempBox.add(Box.createHorizontalStrut(10));
        setAllPaths=new JButton("Use for all");
        setAllPaths.setFont(setAllPaths.getFont().deriveFont((float)10));
        setAllPaths.setBorder(new EmptyBorder(0,0,1,1));
        setAllPaths.setActionCommand("set_all_paths");
        setAllPaths.addActionListener(this);
        setAllPaths.setEnabled(false);
        tempBox.add(setAllPaths);
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(Box.createVerticalStrut(25));
        temperPanel.add(tempBox);
        tempOuterBox.add(temperPanel);
        
        tempOuterBox.add(Box.createVerticalStrut(10));        
        temperPanel=null;
        temperPanel = new JPanel();
        temperPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));                 
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createVerticalStrut(25));
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("WORDS File Name:"));
        tempBox.add(Box.createHorizontalStrut(10));
        wordFileName = new JTextField();
        wordFileName.getDocument().addDocumentListener(this);
        wordFileName.setPreferredSize(new Dimension(100, wordFileName.getHeight()));
        tempBox.add(wordFileName);
        tempBox.add(Box.createHorizontalStrut(34));

        createDefaultCustomizations = new JCheckBox("Create customizations");
        createDefaultCustomizations.setActionCommand("create_default_customizations");
        createDefaultCustomizations.setSelected(true);
        createDefaultCustomizations.addActionListener(this);
        tempBox.add(createDefaultCustomizations);        
        tempBox.add(Box.createHorizontalStrut(10));
        createDefaultSchemes = new JCheckBox("Create default scheme files");
        createDefaultSchemes.setSelected(true);
        tempBox.add(createDefaultSchemes);        
        tempBox.add(Box.createHorizontalStrut(20));
        
        temperPanel.add(tempBox);
        tempOuterBox.add(temperPanel);
        tempOuterBox.add(Box.createVerticalStrut(20));        
        
        temperPanel=null;
        temperPanel = new JPanel();
        temperPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));                 
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createVerticalStrut(25));
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Basedata path:"));
        tempBox.add(Box.createHorizontalStrut(10));
        basedataPath = new JTextField();
        basedataPath.setEditable(false);
        basedataPath.setPreferredSize(new Dimension(150, basedataPath.getHeight()));
        tempBox.add(basedataPath);
        tempBox.add(Box.createHorizontalStrut(10));
        chooseBasedataPath = new JButton("Pick");
        chooseBasedataPath.setActionCommand("choose_basedatapath");
        chooseBasedataPath.addActionListener(this);
        chooseBasedataPath.setFont(chooseBasedataPath.getFont().deriveFont((float)10));
        chooseBasedataPath.setBorder(new EmptyBorder(0,0,1,1));
        
        tempBox.add(chooseBasedataPath);
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Scheme path:"));
        tempBox.add(Box.createHorizontalStrut(44));
        schemePath = new JTextField();
        schemePath.setEditable(false);
        schemePath.setPreferredSize(new Dimension(150, schemePath.getHeight()));
        tempBox.add(schemePath);
        tempBox.add(Box.createHorizontalStrut(10));
        chooseSchemePath = new JButton("Pick");
        chooseSchemePath.setActionCommand("choose_schemepath");
        chooseSchemePath.addActionListener(this);
        chooseSchemePath.setFont(chooseSchemePath.getFont().deriveFont((float)10));
        chooseSchemePath.setBorder(new EmptyBorder(0,0,1,1));
        
        tempBox.add(chooseSchemePath);
        tempBox.add(Box.createHorizontalStrut(20));        
        temperPanel.add(tempBox);
        tempOuterBox.add(temperPanel);
        
        tempOuterBox.add(Box.createVerticalStrut(10));
        
        temperPanel=null;
        temperPanel = new JPanel();
        temperPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));                 
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createVerticalStrut(25));
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Style path:"));
        tempBox.add(Box.createHorizontalStrut(36));
        stylePath = new JTextField();
        stylePath.setEditable(false);
        stylePath.setPreferredSize(new Dimension(150, stylePath.getHeight()));
        tempBox.add(stylePath);
        tempBox.add(Box.createHorizontalStrut(10));
        chooseStylePath = new JButton("Pick");
        chooseStylePath.setActionCommand("choose_stylepath");
        chooseStylePath.addActionListener(this);
        chooseStylePath.setFont(chooseStylePath.getFont().deriveFont((float)10));
        chooseStylePath.setBorder(new EmptyBorder(0,0,1,1));
        
        tempBox.add(chooseStylePath);
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Customization path:"));
        tempBox.add(Box.createHorizontalStrut(10));
        customizationPath = new JTextField();
        customizationPath.setEditable(false);
        customizationPath.setPreferredSize(new Dimension(150, customizationPath.getHeight()));
        tempBox.add(customizationPath);
        tempBox.add(Box.createHorizontalStrut(10));
        chooseCustomizationPath = new JButton("Pick");
        chooseCustomizationPath.setActionCommand("choose_customizationpath");
        chooseCustomizationPath.addActionListener(this);
        chooseCustomizationPath.setFont(chooseCustomizationPath.getFont().deriveFont((float)10));
        chooseCustomizationPath.setBorder(new EmptyBorder(0,0,1,1));
        
        tempBox.add(chooseCustomizationPath);
        tempBox.add(Box.createHorizontalStrut(20));        
        temperPanel.add(tempBox);
        tempOuterBox.add(temperPanel);

        tempOuterBox.add(Box.createVerticalStrut(10));
        
        temperPanel=null;
        temperPanel = new JPanel();
        temperPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));                 
        tempBox = null;
        tempBox = Box.createHorizontalBox();
        tempBox.add(Box.createVerticalStrut(25));
        tempBox.add(Box.createHorizontalStrut(20));
        tempBox.add(new JLabel("Markable path:"));
        tempBox.add(Box.createHorizontalStrut(10));
        markablePath = new JTextField();
        markablePath.setEditable(false);
        markablePath.setPreferredSize(new Dimension(150, markablePath.getHeight()));
        tempBox.add(markablePath);
        tempBox.add(Box.createHorizontalStrut(10));
        chooseMarkablePath = new JButton("Pick");
        chooseMarkablePath.setActionCommand("choose_markablepath");
        chooseMarkablePath.addActionListener(this);
        chooseMarkablePath.setFont(chooseMarkablePath.getFont().deriveFont((float)10));
        chooseMarkablePath.setBorder(new EmptyBorder(0,0,1,1));
        
        tempBox.add(chooseMarkablePath);
        
        temperPanel.add(tempBox);
        tempOuterBox.add(temperPanel);                
        
        tempBox = null;
        tempBox = Box.createHorizontalBox();    
        tempBox.add(Box.createVerticalStrut(25));
        doNotAskBeforeOverwriting = new JCheckBox("Overwrite without asking");
        tempBox.add(doNotAskBeforeOverwriting);
        tempBox.add(Box.createHorizontalStrut(10));
        createProjectButton = new JButton("Create project");
        createProjectButton.setActionCommand("create_project");
        createProjectButton.addActionListener(this);        
        createProjectButton.setEnabled(false);
        tempBox.add(createProjectButton);
        tempBox.add(Box.createHorizontalStrut(20));
        tempPanel = null;
        tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,0, 5));  
        
        tempPanel.add(tempBox);        
        tempOuterBox.add(tempPanel);                
        
        projectPanel.add(tempOuterBox);
        outerBox.add(projectPanel);
        
        getContentPane().add(outerBox);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

            
    public void startAutomaticBulkImport()
    {
        // Skip first one, which has been processed already manually
        for (int n=1;n<this.batchFiles.size();n++)
        {
            String currentName = (String)this.batchFiles.get(n);
            File file = new File(currentName);
            String requestedFileName = file.getAbsolutePath();
            int sepPos = requestedFileName.lastIndexOf(File.separator);
            if (sepPos != -1)
            {
                requestedFileName=requestedFileName.substring(sepPos+1);
            }
            infileName.setText(requestedFileName);                
            infilePath = file.getPath();
            infileName.setToolTipText(infilePath);
            analyseInfile.setEnabled(true);    
            testTokenization.setEnabled(true);
            
            completedTokens = null;
            completedTokens = tokenizeFile(infilePath, getSelectedCharset());
            String temp = this.infileName.getText();
            projectFileName.setText(temp.substring(0,temp.indexOf(".")));
            wordFileName.setText(temp.substring(0,temp.indexOf("."))+"_words.xml");
            validTokenization=true;
            updateCreateProjectButton();
            createProject();  
            System.gc();
        }
        
    }
    
    public static void main (String[] args)
    {
        MMAX2ProjectWizard wizard = new MMAX2ProjectWizard(null);
        wizard.setBulkMode();
        wizard.batchFiles = new ArrayList();
        for (int o=0;o<args.length;o++)
        {
            if (args[o].equalsIgnoreCase("-infiles"))
            {
                for (int z=o+1;z<args.length;z++)
                {
                    wizard.batchFiles.add(args[z]);
                }
            }
        }
        
        if (wizard.batchFiles.size()==0)
        {
            System.err.println("No input files specified! Use the -infiles option to specify list of files to be converted!");
            System.exit(0);
        }
        String currentName = (String)wizard.batchFiles.get(0);
        File file = new File(currentName);
        String requestedFileName = file.getAbsolutePath();
        int sepPos = requestedFileName.lastIndexOf(File.separator);
        if (sepPos != -1)
        {
            requestedFileName=requestedFileName.substring(sepPos+1);
        }
        wizard.infileName.setText(requestedFileName);                
        wizard.infilePath = file.getPath();
        wizard.infileName.setToolTipText(wizard.infilePath);
        wizard.analyseInfile.setEnabled(true);    
        wizard.testTokenization.setEnabled(true);        
    }
    
    private final void setBulkMode()
    {
        bulkMode = true;
        allowMultipleInputFiles.setSelected(true);
        doNotAskBeforeOverwriting.setSelected(true);
    }
         
    private final boolean canWriteTo(String fileToBeWritten)
    {
        boolean result = true;
        File temp = new File(fileToBeWritten);
        if (temp.exists() && doNotAskBeforeOverwriting.isSelected()==false)
        {
            String message = "The file "+fileToBeWritten+" does exist! Overwrite?";
            // The file with the given name exists
            int option = JOptionPane.showConfirmDialog(this,message,"Overwrite File?",JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.OK_OPTION)
            {
                result=false;
            }            
        }
        return result;
    }

    private final ArrayList createMarkableLevel(ArrayList tagsFound)
    {
        ArrayList result = new ArrayList();
        tagsFound.add(0, "NONE");
        tagsFound.add(1, "WORD");
        tagsFound.add(2, "EOL");
        tagsFound.add(3, "EMPTY");
        if (tagsFound.size() > 4)
        {
            // make 'specify' appear always as last
            // but only is tagsFound was not empty, i.e. only if the input did contain some tags
            tagsFound.add("specify ...");            
        }
        
        ArrayList handles = new ArrayList();
        handles.add(new String("NONE"));
        handles.add("[ ]");
        handles.add("[ ] bold");
        handles.add("( )");
        handles.add("( ) bold");

        ArrayList styles = new ArrayList();
        styles.add("plain");
        styles.add("bold");
        styles.add("italic");
        styles.add("bold italic");
        
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0)); 
        Box innerBox = Box.createHorizontalBox();
        innerBox.add(Box.createHorizontalStrut(20));
        JTextField nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);
        nameField.setPreferredSize(new Dimension(80, nameField.getHeight()));
        nameField.setToolTipText("Specify the name for this markable level");
        result.add(nameField);
        innerBox.add(nameField);
        innerBox.add(Box.createHorizontalStrut(20));
        JComboBox box = new JComboBox(tagsFound.toArray());        
        box.setToolTipText("Select a source for this markable level");
        innerBox.add(box);
        result.add(box);
        box.setActionCommand("level_source_changed");
        box.addActionListener(this);
        innerBox.add(Box.createHorizontalStrut(10));
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(120, field.getHeight()));
        field.setEnabled(false);
        field.getDocument().addDocumentListener(this);
        innerBox.add(field);        
        result.add(field);
        innerBox.add(Box.createHorizontalStrut(10));
        JComboBox handleBox = new JComboBox(handles.toArray());
        handleBox.setToolTipText("Select types of handles for this markable level");
        innerBox.add(handleBox);
        result.add(handleBox);
        innerBox.add(Box.createHorizontalStrut(10));
        JTextField temp = new JTextField(" color ");
        temp.setEnabled(colorColumnHeader.isEnabled());
        temp.setToolTipText("Click to select text color for this markable level");
        temp.addMouseListener(this);
        temp.setEditable(false);
        temp.setForeground(Color.black);
        temp.setBackground(Color.white);
        innerBox.add(temp);
        result.add(temp);
        innerBox.add(Box.createHorizontalStrut(10));
        JComboBox styleBox = new JComboBox(styles.toArray());
        styleBox.setEnabled(styleColumnHeader.isEnabled());
        styleBox.setToolTipText("Select text style for this markable level");
        innerBox.add(styleBox);
        result.add(styleBox);
        innerBox.add(Box.createHorizontalStrut(5));
        JCheckBox crBox = new JCheckBox(" ");
        crBox.setToolTipText("Check to add line breaks after each markable from this level");        
        innerBox.add(crBox);
        result.add(crBox);
        innerBox.add(Box.createHorizontalStrut(5));
        BasicArrowButton moveUpButton = new BasicArrowButton(SwingConstants.NORTH);
        moveUpButton.setToolTipText("Move this level up");
        moveUpButton.setActionCommand("up");
        moveUpButton.addActionListener(this);
        innerBox.add(moveUpButton);
        result.add(moveUpButton);
        innerBox.add(Box.createHorizontalStrut(5));
        BasicArrowButton moveDownButton = new BasicArrowButton(SwingConstants.SOUTH);
        moveDownButton.setToolTipText("Move this level down");
        moveDownButton.setActionCommand("down");
        moveDownButton.addActionListener(this);
        innerBox.add(moveDownButton);
        result.add(moveDownButton);
        innerBox.add(Box.createHorizontalStrut(10));
        JButton deleteButton = new JButton("X");
        deleteButton.setFont(deleteButton.getFont().deriveFont((float)18));
        deleteButton.setBorder(new EmptyBorder(0,0,1,1));
        deleteButton.setToolTipText("Delete this markable level");
        deleteButton.setActionCommand("remove_level");
        deleteButton.addActionListener(this);
        innerBox.add(deleteButton);
        result.add(deleteButton);
        innerBox.add(Box.createHorizontalStrut(20));
        panel.add(innerBox);       
        return result;          
    }          
    
    public void actionPerformed(java.awt.event.ActionEvent e) 
    {
        String command = e.getActionCommand();
        if (command.equals("choose_infile"))
        {
            String requestedFileName = "";
            JFileChooser chooser = new JFileChooser(infilePath);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedFileName = chooser.getSelectedFile().getAbsolutePath();
                int sepPos = requestedFileName.lastIndexOf(File.separator);
                if (sepPos != -1)
                {
                    requestedFileName=requestedFileName.substring(sepPos+1);
                }
                infileName.setText(requestedFileName);                
                infilePath = chooser.getSelectedFile().getPath();
                infileName.setToolTipText(infilePath);
                analyseInfile.setEnabled(true);    
                testTokenization.setEnabled(true);
                forceReanalyze();
                forceRetokenize();
            }                                    
        }
        else if (command.equals("input_already_tokenized"))
        {
            boolean state = ((JCheckBox)e.getSource()).isSelected();
            
            leadingTokens.setEnabled(!state);
            splitLeadingLabel.setEnabled(!state);
            splitTrailingLabel.setEnabled(!state);
            trailingTokens.setEnabled(!state);
            if (trailingTokens.getText().indexOf(".")!=-1)
            {
                useAbbreviationHeuristics.setEnabled(!state);
                useAbbreviationList.setEnabled(!state);
            }
            else
            {
                useAbbreviationHeuristics.setEnabled(false);
                useAbbreviationList.setEnabled(false);
            }

            if (leadingTokens.getText().indexOf("\"")!=-1)
            {
                convertLeadingQuote.setEnabled(!state);
            }
            else
            {
                convertLeadingQuote.setEnabled(false);
            }
            if (trailingTokens.getText().indexOf("\"")!=-1)
            {
                convertTrailingQuote.setEnabled(!state);
            }
            else
            {
                convertTrailingQuote.setEnabled(false);
            }
            
            useGlobalReplacementList.setEnabled(!state);
            forceRetokenize();
                        
        }
        else if (command.equals("analyse_infile"))
        {
            analyseFile(infilePath,getSelectedCharset());
            testTokenization.setEnabled(true);
        }
        else if (command.equals("encoding_changed"))
        {
            forceRetokenize();
            forceReanalyze();
        }
        else if (command.equals("tokenization_parameters_changed"))
        {
            forceRetokenize();
        }     
        else if (command.equals("treat_as_xml"))
        {
            forceRetokenize();
            forceReanalyze();
        }             
        else if (command.equals("tokenize"))
        {
            completedTokens = tokenizeFile(infilePath, getSelectedCharset());
            if (completedTokens != null)
            {
                String temp = this.infileName.getText();
                projectFileName.setText(temp.substring(0,temp.indexOf(".")));
                wordFileName.setText(temp.substring(0,temp.indexOf("."))+"_words.xml");
                validTokenization=true;
                updateCreateProjectButton();
            }
        }
        else if (command.equals("add_level"))
        {
            ArrayList newLevel = createMarkableLevel(new ArrayList(tagsInInput));
            addMarkableLevel(markableLevelBox,newLevel);
            
            int count = markableLevelBox.getComponentCount();
            markableLevelBox.repaint();
            numMarkableLevels++;            
            updateCreateProjectButton();
            setVisible(true);
            pack();
        }
        else if (command.equals("remove_level"))
        {                     
            // Get pressed button
            JButton source = (JButton) e.getSource();
            Box headerBox = (Box)((JPanel)markableLevelBox.getComponent(1)).getComponent(0);
            Box sourceContainer = (Box)headerBox.getComponent(19);
            int levelToRemove = 0;            
            for (int z=0;z<sourceContainer.getComponentCount();z++)
            {
                if (sourceContainer.getComponent(z)==source)
                {
                    levelToRemove = z;
                    break;
                }
            }
                                    
            for (int b=1;b<headerBox.getComponentCount();b+=2)
            {               
                Box container = (Box)headerBox.getComponent(b);
                container.remove(levelToRemove);
                if (numMarkableLevels == 1)
                {
                    // We are removing the last markable
                    ((java.awt.Component)container.getComponent(0)).setVisible(false);                
                }
            }        
            markableLevelBox.repaint();
            numMarkableLevels--;
            updateCreateProjectButton();
            setVisible(true);
            pack();            
        } 
        else if (command.equals("create_project"))
        {
            createProject();
            if (bulkMode && bulkImportIsRunning==false)
            {
                bulkImportIsRunning=true;
                startAutomaticBulkImport();
            }
        }
        else if (command.equals("level_source_changed"))
        {
            // On some level, the box containing the level's source was modified
            // Determine box that caused the event
            // Events are selections of different markable levels sources
            JComboBox source = (JComboBox) e.getSource();
            
            // Determine level that changed box belongs to 
            // Get box containing left to right sequence of boxes
            Box headerBox = (Box)((JPanel)markableLevelBox.getComponent(1)).getComponent(0);
            // index 3 = position of 'source' box
            Box sourceContainer = (Box)headerBox.getComponent(3);
            int levelToChange = 0;            
            for (int z=0;z<sourceContainer.getComponentCount();z++)
            {
                if (sourceContainer.getComponent(z)==source)
                {
                    levelToChange = z;
                    break;
                }
            }

            // Get selected value
            String selected = (String)source.getSelectedItem();            
            // index 5 = position 'specify' box
            Box specifyBox = (Box)headerBox.getComponent(5);
            Box nameBox = (Box)headerBox.getComponent(1);
            if(selected.equalsIgnoreCase("specify ..."))
            {                
                // Enable 
                ((JTextField)specifyBox.getComponent(levelToChange)).setEnabled(true);
                if (((JTextField)specifyBox.getComponent(levelToChange)).getText().equals(""))
                {
                    ((JTextField)specifyBox.getComponent(levelToChange)).setText(getTagNameList(source,4,source.getItemCount()-2));
                }
            }
            else
            {
                ((JTextField)specifyBox.getComponent(levelToChange)).setText("");
                ((JTextField)specifyBox.getComponent(levelToChange)).setEnabled(false);
                if (((JTextField)nameBox.getComponent(levelToChange)).getText().equals(""))
                {
                    ((JTextField)nameBox.getComponent(levelToChange)).setText(selected.toLowerCase());
                }                        
            }
            updateCreateProjectButton();
        }
        else if (command.equals("level_source_changed_44"))
        {
            // On some level, the box containing the level's source was modified
            // Determine box that caused the event
            // Events are selections of different markable levels sources
            JComboBox source = (JComboBox) e.getSource();
            // Iterate over all currently displayed markable levels
            // Start at 1 to skip strut and header
            for (int z=2;z<markableLevelBox.getComponentCount();z++)
            {
                JPanel currentPanel = (JPanel)markableLevelBox.getComponent(z);
                Box currentBox = (Box) currentPanel.getComponent(0);
                if (((JComboBox)currentBox.getComponent(3)).equals(source))
                {
                    String selected = (String)source.getSelectedItem();
                    
                    if(selected.equalsIgnoreCase("specify ..."))
                    {
                        ((JTextField)currentBox.getComponent(5)).setEnabled(true);
                        if (((JTextField)currentBox.getComponent(5)).getText().equals(""))
                        {
                            ((JTextField)currentBox.getComponent(5)).setText(getTagNameList(source,4,source.getItemCount()-2));
                        }
                    }
                    else
                    {
                        ((JTextField)currentBox.getComponent(5)).setEnabled(false);
                        if (((JTextField)currentBox.getComponent(1)).getText().equals(""))
                        {
                            ((JTextField)currentBox.getComponent(1)).setText(selected.toLowerCase());
                        }                        
                    }
                    break;
                }
            }
            updateCreateProjectButton();
        }
        
        else if (command.equals("choose_projectpath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as project path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath().trim();                
                projectPathName.setText(requestedPathName);                
                projectPathName.setToolTipText(requestedPathName);
            }    
            if (requestedPathName.equals("")==false)
            {
                setAllPaths.setEnabled(true);                
            }
            
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                projectPathName.setText("");
                projectPathName.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            updateCreateProjectButton();
        }
        else if (command.equals("set_all_paths"))
        {
            String requestedPathName=projectPathName.getText();
            basedataPath.setText(requestedPathName);                
            basedataPath.setToolTipText(requestedPathName);
            schemePath.setText(requestedPathName);                
            schemePath.setToolTipText(requestedPathName);
            stylePath.setText(requestedPathName);                
            stylePath.setToolTipText(requestedPathName);
            customizationPath.setText(requestedPathName);                
            customizationPath.setToolTipText(requestedPathName);
            markablePath.setText(requestedPathName);                
            markablePath.setToolTipText(requestedPathName);
            
            updateCreateProjectButton();
        }
        else if (command.equals("create_default_customizations"))
        {
            // Get its status
            boolean status = createDefaultCustomizations.isSelected();
            styleColumnHeader.setEnabled(status);
            colorColumnHeader.setEnabled(status);
            // Iterate over all currently displayed markable levels
            // Start at 2 to skip strut and header
            for (int z=2;z<markableLevelBox.getComponentCount();z++)
            {
                JPanel currentPanel = (JPanel)markableLevelBox.getComponent(z);                
                Box currentBox = (Box) currentPanel.getComponent(0);
                if (currentBox.getComponentCount()>3)
                {
                    ((JComboBox)currentBox.getComponent(11)).setEnabled(status);
                    ((JTextField)currentBox.getComponent(9)).setEnabled(status);
                }
            }                        
        }
        else if (command.equals("up"))
        {
            BasicArrowButton source = (BasicArrowButton) e.getSource();
            
            Box headerBox = (Box)((JPanel)markableLevelBox.getComponent(1)).getComponent(0);
            Box sourceContainer = (Box)headerBox.getComponent(15);
            int levelToMove = 0;            
            for (int z=0;z<sourceContainer.getComponentCount();z++)
            {
                if (sourceContainer.getComponent(z)==source)
                {
                    levelToMove = z;
                    break;
                }
            }
                                 
            if (levelToMove!=1)
            {
                for (int b=1;b<headerBox.getComponentCount();b+=2)
                {                                   
                	Box container = (Box)headerBox.getComponent(b);
                    java.awt.Component comp = container.getComponent(levelToMove);
                    container.remove(levelToMove);
                    container.add(comp,levelToMove-1);
                }        
                markableLevelBox.repaint();
                updateCreateProjectButton();
                setVisible(true);
                pack();
            }
            else
            {
                System.err.println("Can't move this one up!");
            }                                           
        }
        else if (command.equals("down"))
        {
            BasicArrowButton source = (BasicArrowButton) e.getSource();
            
            Box headerBox = (Box)((JPanel)markableLevelBox.getComponent(1)).getComponent(0);
            Box sourceContainer = (Box)headerBox.getComponent(17);
            int levelToMove = 0;            
            for (int z=0;z<sourceContainer.getComponentCount();z++)
            {
                if (sourceContainer.getComponent(z)==source)
                {
                    levelToMove = z;
                    break;
                }
            }
                                 
            if (levelToMove<sourceContainer.getComponentCount()-1)
            {
                for (int b=1;b<headerBox.getComponentCount();b+=2)
                {               
                    //java.awt.Component toAdd = (java.awt.Component)newLevel.get(b);
                    Box container = (Box)headerBox.getComponent(b);
                    java.awt.Component comp = container.getComponent(levelToMove);
                    container.remove(levelToMove);
                    container.add(comp,levelToMove+1);
                }        
                markableLevelBox.repaint();
                updateCreateProjectButton();
                setVisible(true);
                pack();
            }
            else
            {
                System.err.println("Can't move this one down!");
            }                        
        }        
        else if (command.equals("choose_basedatapath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser(basedataPath.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as basedata path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath();
                basedataPath.setText(requestedPathName);                
                basedataPath.setToolTipText(requestedPathName);
            }    
            
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                basedataPath.setText("");
                basedataPath.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            
            updateCreateProjectButton();
        }
        else if (command.equals("choose_schemepath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser(schemePath.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as scheme path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath();
                schemePath.setText(requestedPathName);                
                schemePath.setToolTipText(requestedPathName);
            } 
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                schemePath.setText("");
                schemePath.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            
            updateCreateProjectButton();
        }        
        else if (command.equals("choose_stylepath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser(stylePath.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as style path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath();
                stylePath.setText(requestedPathName);                
                stylePath.setToolTipText(requestedPathName);
            } 
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                stylePath.setText("");
                stylePath.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            
            updateCreateProjectButton();
        }        
        else if (command.equals("choose_customizationpath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser(customizationPath.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as customization path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath();
                customizationPath.setText(requestedPathName);                
                customizationPath.setToolTipText(requestedPathName);
            } 
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                customizationPath.setText("");
                customizationPath.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            
            updateCreateProjectButton();
        }        
        else if (command.equals("choose_markablepath"))
        {
            String requestedPathName = "";
            JFileChooser chooser = new JFileChooser(markablePath.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);            
            int result = chooser.showDialog(this,"Set as markable path");
            if (result == JFileChooser.APPROVE_OPTION)
            {
                requestedPathName = chooser.getSelectedFile().getAbsolutePath();
                markablePath.setText(requestedPathName);                
                markablePath.setToolTipText(requestedPathName);
            } 
            if (requestedPathName.indexOf(" ")!=-1)
            {
                JOptionPane.showMessageDialog(null,"The selected path contains problematic white space.\nPlease select a different path!","MMAX2 Project Wizard",JOptionPane.ERROR_MESSAGE);
                markablePath.setText("");
                markablePath.setToolTipText("");
                setAllPaths.setEnabled(false);                
            }
            
            updateCreateProjectButton();
        }        
        
    }    
 
    
    private final void addMarkableLevel(Box box, ArrayList newLevel)
    {
        Box headerBox = (Box)((JPanel)box.getComponent(1)).getComponent(0);
        
        for (int b=0;b<newLevel.size();b++)
        {            
            java.awt.Component toAdd = (java.awt.Component)newLevel.get(b);
            Box container = (Box)headerBox.getComponent((b*2)+1);
            container.add(toAdd);
            ((java.awt.Component)container.getComponent(0)).setVisible(true);
        }        
    }
    
    private final String getTagNameList(JComboBox box, int from, int to)
    {
        String result = "";
        for (int z=from;z<=to;z++)
        {
            result = result + box.getItemAt(z)+", ";
        }
        
        result = result.trim();
        return result.trim().substring(0,result.length()-1);
    }
    
    private final String getSelectedCharset()
    {
        return (String) inputEncodings.getSelectedItem();
    }
    
    
    private final String getCommonPath(String projectPath, String subPath)
    {
        String result = "";
        ArrayList projectParts = new ArrayList();
        ArrayList subParts = new ArrayList();        
        
        // Convert project path in list of directory names
        StringTokenizer toki = new StringTokenizer(projectPath,File.separator);
        while(toki.hasMoreTokens())
        {
            projectParts.add((String)toki.nextToken());
        }
        toki = null;
        
        // Convert sub path in list of directory names
        toki = new StringTokenizer(subPath,File.separator);
        while(toki.hasMoreTokens())
        {
            subParts.add((String)toki.nextToken());
        }
        
        int maxElems = subParts.size();
        if (projectParts.size() > maxElems)
        {
            maxElems = projectParts.size();
        }
        
        // Iterate over all parts
        for (int z=0;z<maxElems;z++)
        {
            // Get current part
            String currentSubPart = "";
            if (subParts.size()>z)
            {
                currentSubPart = (String)subParts.get(z);
            }

            String currentProjectPart = "";
            if (projectParts.size()>z)
            {
                currentProjectPart = (String)projectParts.get(z);
            }

            
            if (currentProjectPart.equalsIgnoreCase(currentSubPart)==false)
            {
                // The two paths are distinct from this position on
                if (currentProjectPart.equals(""))
                {
                    // The two paths are different because the (shorter) project path ends here
                    // Iterate over rest of sub path
                    for (int b=z;b<subParts.size();b++)
                    {
                        result = result+(String)subParts.get(b)+File.separator;
                    }
                    break;
                }
                else
                {
                    // The shorter path is not completely contained in the longer
                    // Iterate over rest of project path
                    for (int b=z;b<projectParts.size();b++)
                    {
                        // Add one step back for each different parent directory
                        result=result+".."+File.separator;                            
                    }                        
                    for (int p=z;p<subParts.size();p++)
                    {
                        result=result+(String)subParts.get(p)+File.separator;
                    }
                    break;
                }
            }
        }
        return result;
    }
    
    private final ArrayList tokenizeFile (String fileName, String encoding)
    {                        
        tagsInInput = new ArrayList();
        boolean convertLeading=convertLeadingQuote.isSelected();
        boolean convertTrailing=convertTrailingQuote.isSelected();
        boolean useHeuristics=useAbbreviationHeuristics.isSelected();
        boolean useList=useAbbreviationList.isSelected();
        boolean useReplacements=useGlobalReplacementList.isSelected();
        boolean xml=treatAsXML.isSelected();
        
        ArrayList abbrevList = new ArrayList();
        
        ArrayList replacables = new ArrayList();
        ArrayList replacees = new ArrayList();

        String abbreviationFileName = "abbrevs.txt";
        String replacementFileName = "replacements.txt";
        
        // Read abbrev. list, if required
        if (useList)
        {
            BufferedReader abbrevReader = null;
            FileInputStream inStream = null;
            try
            {                
                inStream = new FileInputStream(abbreviationFileName);
                abbrevReader = new BufferedReader(new InputStreamReader(inStream));//,"windows-1252"));
            
            }
            catch (java.io.FileNotFoundException ex)
            {
                System.err.println("Error: Couldn't find file "+abbreviationFileName);
                return null;
            }           
            
            String currentAbbrev="";
            try
            {
                while((currentAbbrev=abbrevReader.readLine())!=null)
                {
                    abbrevList.add(currentAbbrev);
                }                        
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
        }                        
        // End reading abbrev list

        // Read replacement list, if required
        if (useReplacements)
        {
            BufferedReader repReader = null;
            try
            {
                repReader = new BufferedReader(new InputStreamReader(new FileInputStream(replacementFileName)));//,"windows-1252"));
            
            }
            catch (java.io.FileNotFoundException ex)
            {
                System.err.println("Error: Couldn't find file "+replacementFileName);
            }           
            
            String currentRep="";
            try
            {
                while((currentRep=repReader.readLine())!=null)
                {
                    String currentReplacable = currentRep.substring(0,currentRep.indexOf(":::"));
                    replacables.add(currentReplacable);
                    String currentReplacee = currentRep.substring(currentRep.indexOf(":::")+3);
                    // Check if replacee contains %%, which marks comment                    
                    if (currentReplacee.indexOf("%%")!=-1)
                    {
                        currentReplacee = currentReplacee.substring(0,currentReplacee.indexOf("%%"));
                    }
                    replacees.add(currentReplacee);
                }                        
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
        }                        
        // End reading abbrev list
        
        
        ArrayList tokens = new ArrayList();        
        BufferedReader textFileReader = null;
        
        System.err.println("Tokenizing "+fileName+" with encoding "+encoding);
        
        try
        {
            textFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),encoding));
            
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
            return null;
        }
        
        // Add opening <empty> tag, if empty lines do exist in input file
        if (emptyLineCount != 0)
        {
            // But only if this is not treated as xml
            if (!xml)
            {
                tokens.add("<EMPTY>");
            }
        }
        
        // But only if this is not treated as xml
        if (!xml)
        {
            tokens.add("<EOL>");
        }
        
        boolean doAnyTokenization = !(inputAlreadyTokenized.isSelected());
        
        if (!bulkMode)
        {
            manualDecisions = new HashMap();
        }        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        String currentLine="";
        // Iterate forever
        while (true)
        {
            try
            {
                // Get next line
                currentLine = textFileReader.readLine();
            }
            catch (java.io.IOException ex)
            {
                System.err.println(ex.getMessage());
            }
            if (currentLine==null)
            {
                // The file has been read
                try
                {
                    textFileReader.close();
                }
                catch (java.io.IOException ex)
                {
                    System.err.println(ex.getMessage());           
                }     
                // Quit infinite loop
                break;
            }
            
            currentLine = currentLine.trim();
            
            if (doAnyTokenization)
            {
                // Normal tokenization is to be performed
                if (currentLine.equals(""))
                {
                    if (!xml)
                    {
                        // Set current line to empty line tag
                        currentLine="</EMPTY> <EMPTY>";                
                    }
                }
                else
                {
                    if (!xml)
                    {
                        // Append eol tag to current non-empty line
                        currentLine = currentLine + " </EOL> <EOL>";
                    }
                }
                
                
                if (currentLine.equals("")==false)
                {
                    if (!xml)
                    {
                        // Split all at white space, incl. tags just added
                        tokens.addAll(getSpaceSeparatedTokens(currentLine, replacables, replacees));                
                    }
                    else
                    {
                        // The input is to be treated as XML
                        // Add line breaks between tag borders
                        ArrayList temp = breakTags(currentLine);
                        if (temp != null)
                        {
                            tokens.addAll(temp);
                        }
                        else
                        {
                            tokens.add(currentLine);
                        }
                    }
                }
            }
            else
            {
                // no tokenization is to be performed
                if (currentLine.equals(""))
                {
                    if (!xml)
                    {
                        // Add empty end and new start tag to token list
                        tokens.add("</EMPTY>");
                        tokens.add("<EMPTY>");
                    }
                }
                else
                {
                    // Add current, non-empty line plus eol end and new start tag
                    tokens.add(currentLine);
                    if (!xml)
                    {
                        tokens.add("</EOL>");
                        tokens.add("<EOL>");                    
                    }
                }                
            }                        
        }
        
        // Here, the entire document to be read is in the tokens list        
        
        if (xml)
        {
        	// Iterate over all tokens
            for (int p=0;p<tokens.size();p++)
            {
            	// Get current token
                String tt = (String)tokens.get(p);
                //~~ is used to mask the string which results from externalising empty xml tags 
                if (tt.startsWith("<")==false && tt.startsWith("~~")==false)
                {
                	// Expand current token in several ones. Expansion may result from splitting
                    ArrayList t = getSpaceSeparatedTokens(tt, replacables, replacees);
                    // If a replacement has happened, replce original tag with sequence of new ones
                    if (t.size() > 1)
                    {
                        tokens.remove(p);
                        tokens.addAll(p, t);
                        p--;
                        continue;
                    }
                }
            }            
        }
        
        // Now, tokens contains roughly pre-tokenized tokens        
        ArrayList toBeSplitWhenLeading=new ArrayList();
        String temp="";
        StringTokenizer toki=null;
        if (doAnyTokenization)
        {
        	// Put value of parameter 'split leading' into list for easier processing
            temp = leadingTokens.getText();
            toki = new StringTokenizer(temp," ");
            while (toki.hasMoreTokens())
            {
                toBeSplitWhenLeading.add((String)toki.nextToken());
            }
        }
        
        ArrayList toBeSplitWhenTrailing=new ArrayList();
        if (doAnyTokenization)
        {
        	// Put value of parameter 'split trailing' into list for easier processing
            temp = trailingTokens.getText();
            toki = null;
            toki = new StringTokenizer(temp," ");
            while (toki.hasMoreTokens())
            {
                toBeSplitWhenTrailing.add((String)toki.nextToken());
            }
        }
        boolean splitFound=false;
        
        // Tokenization is skipped by having empty list of splitables
        
        // Iterate over all tokens
        for (int z=0;z<tokens.size();z++)
        {
            // New: trim here to prevent empty >< words
            String currentToken = ((String) tokens.get(z)).trim();
            if (currentToken.equals("") || currentToken.startsWith("<?") || currentToken.startsWith("<!"))
            {
            	// Remove xml meta tags and empty tags
                tokens.remove(z);
                z--;
                continue;
            }
            
            if (currentToken.startsWith("<") && currentToken.startsWith("</")==false)
            {
            	// The current tag is an xml tag (might also be sth like <empty>
                // Get only opening tags (without leading <)
                String tagToStore = currentToken.substring(1);
                if (tagToStore.indexOf(" ")==-1)
                {
                	// tagToStore does not contain a space, so just chop off last char (<text>)
                    tagToStore = tagToStore.substring(0,tagToStore.length()-1);
                }
                else
                {
                	// tagToStore contains a space, so chop of after that (<text />
                    tagToStore = tagToStore.substring(0,tagToStore.indexOf(" "));
                }
                tagToStore = tagToStore.trim();
                // This list just collects distinct tags in input, to be offered during xml to markable mapping
                if (tagsInInput.contains(tagToStore)==false)
                {
                    tagsInInput.add(tagToStore);
                }
                continue;
            }
            if (currentToken.startsWith("<") || currentToken.startsWith("~~"))
            {
                continue;
            }
            // Iterate over all characters to be split off when leading
            for (int n=0;n<toBeSplitWhenLeading.size();n++)
            {                
                String currentToBeSplit = (String) toBeSplitWhenLeading.get(n);
                if (currentToken.startsWith(currentToBeSplit) && currentToken.equals(currentToBeSplit)==false)
                {
                    tokens.remove(z);
                    if (convertLeading && currentToBeSplit.equals("\""))
                    {
                        currentToBeSplit="_oq_";
                    }
                    tokens.add(z,currentToBeSplit);
                    tokens.add(z+1, currentToken.substring(1));
                    
                    splitFound=true;
                    break;
                }
            }
            if (splitFound)
            {
                z--;
                splitFound=false;
                continue;
            }
            // Iterate over all characters to be split off when trailing
            for (int n=0;n<toBeSplitWhenTrailing.size();n++)
            {
                String currentToBeSplit = (String) toBeSplitWhenTrailing.get(n);
                if (currentToken.endsWith(currentToBeSplit) && currentToken.equals(currentToBeSplit)==false)
                {
                    if (currentToBeSplit.equals(".")==false)
                    {
                        tokens.remove(z);
                        tokens.add(z, currentToken.substring(0,currentToken.length()-1));
                        if (convertTrailing && currentToBeSplit.equals("\""))
                        {
                            currentToBeSplit="_cq_";
                        }
                        tokens.add(z+1,currentToBeSplit);
                        splitFound=true;
                        break;
                    }
                    else
                    {
                        // The current char to be split is a .
                        // Test for abbrev in list
                        if (useList)
                        {
                            if (abbrevList.contains(currentToken))
                            {
                                // Do nothing with current token if it is in abbrev list
                                continue;
                            }
                        }
                        // Test for some heuristics
                        if (useHeuristics)
                        {
                            // Do not split trailing . if currentToken contains other .s
                            if (currentToken.indexOf(".")!=currentToken.length()-1)
                            {
                                continue;
                            }
                        }

                        tokens.remove(z);
                        tokens.add(z, currentToken.substring(0,currentToken.length()-1));
                        tokens.add(z+1,currentToBeSplit);
                        splitFound=true;
                        break;                        
                    }
                }
            }
            if (splitFound)
            {
                z--;
                splitFound=false;
                continue;
            }                        
        }
        
        String nameSpace=infileName.getText();
        nameSpace = nameSpace.substring(0,nameSpace.lastIndexOf("."));
        
        tokenFileName = nameSpace+".tokens";
        
        BufferedWriter tokenWriter = null;
        try
        {
            tokenWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tokenFileName),encoding));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
                                 
        JFrame result = new JFrame("Token preview (first 5000)");
        DefaultStyledDocument resultDocument = new DefaultStyledDocument();
        JTextPane resultPane = new JTextPane();
        resultPane.setDocument(resultDocument);
        result.getContentPane().add(new JScrollPane(resultPane));                                
        AttributeSet attribs = new SimpleAttributeSet();
        String doc = "";
        for (int p=0;p<tokens.size();p++)
        {
            if (p<5000)
            {
                doc = doc + tokens.get(p)+"\n";            
            }
            toWriter(tokenWriter,tokens.get(p)+"\n");
        }
        closeWriter(tokenWriter);

        try
        {
            resultDocument.insertString(0,doc, attribs);
        }
        catch (javax.swing.text.BadLocationException ex)
        {

        }
        resultPane.setCaretPosition(0);
        result.setSize(200,500);
        if (!bulkMode)
        {
            result.setVisible(true);
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));               
        return tokens;
        
    }
    
    private final ArrayList getSpaceSeparatedTokens(String instring, ArrayList replacables, ArrayList replacees)
    {
        if (replacables.size() > 0)
        {
            for (int u=0;u<replacables.size();u++)
            {
                instring = instring.replaceAll((String)replacables.get(u),(String)replacees.get(u));
            }
        }
        
        ArrayList result = new ArrayList();
        StringTokenizer toki = new StringTokenizer(instring," ");
        while(toki.hasMoreTokens())
        {
            result.add(toki.nextToken());
        }
        return result;
    }
        
    private final void analyseFile(String fileName, String encoding)
    {
        int lineCount = 0;
        emptyLineCount=0;
        
        int charCount = 0;
        ArrayList header = new ArrayList();
        
        BufferedReader textFileReader = null;
        
        System.err.println("Analysing "+fileName+" with encoding "+encoding);
        
        try
        {
            textFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),encoding));
            
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
            return;
        }
        
        String currentLine="";
        while (true)
        {
            try
            {
                currentLine = textFileReader.readLine();
            }
            catch (java.io.IOException ex)
            {
                System.err.println(ex.getMessage());
            }
            if (currentLine==null)
            {
                try
                {
                    textFileReader.close();
                }
                catch (java.io.IOException ex)
                {
                    System.err.println(ex.getMessage());           
                }                
                break;
            }
            
            lineCount++;
            charCount=charCount+currentLine.length();            
            
            // Fill up with spaces to allow trimming
            currentLine = currentLine+"                                                  ";
            
            if (currentLine.trim().equals(""))
            {
                emptyLineCount++;
            }
            
            if (header.size() < 15)
            {
                if (currentLine.trim().equals(""))
                {
                    header.add("<EMPTY>");                       
                }
                else
                {
                    header.add(currentLine.substring(0,50));
                }
            }           
        }
        
        JFrame resultFrame = new JFrame();
        resultFrame.setTitle("File analysis result");
        resultFrame.setResizable(false);
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5)); 
        Box outerBox = Box.createHorizontalBox();
        Box leftBox = Box.createVerticalBox();
        Box rightBox = Box.createVerticalBox();
        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("File:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(fileName));
        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("Read as:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(encoding));
        
        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("Lines:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(lineCount+""));

        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("   Non-Empty:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(lineCount-emptyLineCount+""));
        
        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("   Empty:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(emptyLineCount+""));
        
        leftBox.add(Box.createVerticalStrut(10));        
        leftBox.add(new JLabel("Characters:"));
        rightBox.add(Box.createVerticalStrut(10));
        rightBox.add(new JLabel(charCount+""));
        
        leftBox.add(Box.createVerticalStrut(10));
        rightBox.add(Box.createVerticalStrut(10));
        leftBox.add(new JLabel("Header:"));
        for (int z=0;z<header.size();z++)
        {
            rightBox.add(new JLabel((String)header.get(z)));
            if (z<header.size()-1)
            {
                leftBox.add(new JLabel(" "));
            }
        }
        
        outerBox.add(leftBox);
        outerBox.add(Box.createHorizontalStrut(20));
        outerBox.add(rightBox);
        outerPanel.add(outerBox);
        resultFrame.getContentPane().add(outerPanel);
        resultFrame.pack();        
        resultFrame.setVisible(true);
        resultFrame.setLocationRelativeTo(this);
        
    }
    
    public void windowActivated(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosed(java.awt.event.WindowEvent e) {
    }
    
    public void windowClosing(java.awt.event.WindowEvent e) 
    {
        if (!bulkMode)
        {
            mmax2.wizardClosed();
        }
        else
        {
            dispose();
        }
    }
    
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    	//this.toFront();
    }
    
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowIconified(java.awt.event.WindowEvent e) {
    }
    
    public void windowOpened(java.awt.event.WindowEvent e) {
    }
    
    
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) 
    {
        tokensChanged(e);
    }
        
    public void insertUpdate(javax.swing.event.DocumentEvent e) 
    {
        tokensChanged(e);
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) 
    {
        tokensChanged(e);
    }
    
    private final void tokensChanged(javax.swing.event.DocumentEvent e)
    {
        if (leadingTokens.getText().indexOf("\"")==-1)
        {
            // Disable convert option if " is not available any more
            convertLeadingQuote.setSelected(false);
            convertLeadingQuote.setEnabled(false);
        }
        else
        {
            // Enable convert option if " is available
            // But only if alreadyTokenized is not set
            if (inputAlreadyTokenized.isSelected()==false)
            {
                convertLeadingQuote.setEnabled(true);                
            }
        }
        
        if (trailingTokens.getText().indexOf("\"")==-1)
        {
            // Disable convert option if " is not available any more
            convertTrailingQuote.setSelected(false);
            convertTrailingQuote.setEnabled(false);
        }
        else
        {
            // Enable convert option if " is available 
            // But only if alreadyTokenized is not set
            if (inputAlreadyTokenized.isSelected()==false)
            {
                convertTrailingQuote.setEnabled(true);                
            }

        }                                    
        if (trailingTokens.getText().indexOf(".")==-1)
        {
            // Disable use abbrevlist option if . is not available any more
            useAbbreviationList.setSelected(false);
            useAbbreviationList.setEnabled(false);
            useAbbreviationHeuristics.setSelected(false);
            useAbbreviationHeuristics.setEnabled(false);
            
        }
        else
        {
            if (inputAlreadyTokenized.isSelected()==false)
            {
                useAbbreviationList.setEnabled(true);                
                useAbbreviationHeuristics.setEnabled(true);
            }
        }           
        updateCreateProjectButton();
    }
    private final static void toWriter (BufferedWriter writer, String text)
    {
        try
        {
            writer.write(text);
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    private final static void closeWriter(BufferedWriter writer)
    {
        try
        {
            writer.flush();
            writer.close();
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e)
    {
        JTextField source = (JTextField) e.getSource();        
        Color c = JColorChooser.showDialog(source.getParent(),"Choose markable level color",source.getForeground());
        source.setForeground(c);               
    }
    
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent e) {
    }
    
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
    private final void updateCreateProjectButton()
    {
        boolean newStatus = true;
        if (validTokenization==false)
        {
            newStatus=false;
        }
        if (numMarkableLevels==0)
        {
            newStatus=false;
        }
        
        if (allMarkableLevelsSpecified()==false)
        {
            newStatus=false;
        }
        
        if (basedataPath.getText().trim().equals(""))
        {
            newStatus = false;
        }
        if (schemePath.getText().trim().equals(""))
        {
            newStatus = false;
        }
        if (stylePath.getText().trim().equals(""))
        {
            newStatus = false;
        }
        if (customizationPath.getText().trim().equals(""))
        {
            newStatus = false;
        }
        if (markablePath.getText().trim().equals(""))
        {
            newStatus = false;
        }
        
        if (projectFileName.getText().trim().equals(""))
        {
            newStatus = false;
        }
        if (projectPathName.getText().trim().equals(""))
        {
            newStatus = false;
        }
        
        if (wordFileName.getText().trim().equals(""))
        {
            newStatus = false;
        }           
        
        createProjectButton.setEnabled(newStatus);
    }
    
    private final void forceRetokenize()
    {
        if (allowMultipleInputFiles.isSelected()==false)
        {
            validTokenization=false;
            projectFileName.setText("");
            wordFileName.setText("");
            createProjectButton.setEnabled(false);
        }
    }
    
    private final void forceReanalyze()
    {
        if (allowMultipleInputFiles.isSelected()==false)
        {
            testTokenization.setEnabled(false);
            forceRetokenize();
        }
    }
    
    private final void createProject()
    {
        String projectNameSpace=projectFileName.getText().trim();
        String outFileName = projectPathName.getText()+File.separator+projectFileName.getText()+".mmax";
        String wordFileNameString = wordFileName.getText();
        
        ArrayList allLevelNames=new ArrayList();
        ArrayList allLevelFiles=new ArrayList();
        ArrayList allCustomizationFiles=new ArrayList();
        ArrayList allSchemeFiles = new ArrayList();
        ArrayList selectedHandleTypes = new ArrayList();
        ArrayList allLevelColors=new ArrayList();
        ArrayList allLevelStyles=new ArrayList();
        ArrayList allLevelSources=new ArrayList();
        ArrayList allCarriageReturns=new ArrayList();
        
        Box headerBox = (Box)((JPanel)markableLevelBox.getComponent(1)).getComponent(0);
        
        Box levelNameBox = (Box)headerBox.getComponent(1);
        // Get levelNames
        for (int z=1;z<levelNameBox.getComponentCount();z++)
        {
            allLevelNames.add(((JTextField) levelNameBox.getComponent(z)).getText().trim());            
            allLevelFiles.add("_"+((String)allLevelNames.get(allLevelNames.size()-1))+"_level.xml");                        
            allCustomizationFiles.add(((String)allLevelNames.get(allLevelNames.size()-1))+"_customization.xml");
            allSchemeFiles.add(((String)allLevelNames.get(allLevelNames.size()-1))+"_scheme.xml");            
        }
        
        Box sourceBox = (Box) headerBox.getComponent(3);
        Box specifyBox = (Box) headerBox.getComponent(5);
        for (int z=1;z<sourceBox.getComponentCount();z++)
        {
            String currentLevelSource = (String)((JComboBox) sourceBox.getComponent(z)).getSelectedItem();
            if (currentLevelSource.equals("specify ..."))
            {
                currentLevelSource = currentLevelSource+":"+((JTextField)specifyBox.getComponent(z)).getText();
            }
            allLevelSources.add(currentLevelSource);            
        }
        
        Box handleBox = (Box) headerBox.getComponent(7);
        for (int z=1;z<handleBox.getComponentCount();z++)
        {
            selectedHandleTypes.add(((String)((JComboBox) handleBox.getComponent(z)).getSelectedItem()).trim());
        }
        
        Box colorBox = (Box) headerBox.getComponent(9);
        for (int z=1;z<colorBox.getComponentCount();z++)
        {
            allLevelColors.add(((JTextField) colorBox.getComponent(z)).getForeground());
        }
        
        Box styleBox = (Box) headerBox.getComponent(11);
        for (int z=1;z<styleBox.getComponentCount();z++)
        {
            allLevelStyles.add(((String)((JComboBox) styleBox.getComponent(z)).getSelectedItem()).trim());
        }
        
        Box crBox = (Box) headerBox.getComponent(13);
        for (int z=1;z<crBox.getComponentCount();z++)
        {
            if (((JCheckBox) crBox.getComponent(z)).isSelected())
            {
                allCarriageReturns.add("true");
            }
            else
            {
                allCarriageReturns.add("false");
            }            
        }
        
        // writeWordFile just returns a list of all word ids written to the base data file
        // When writing this file, xml tags are skipped and not written!
        ArrayList allWordIDs = writeWordFile(wordFileNameString, tokenFileName, (String)inputEncodings.getSelectedItem());
        if (allWordIDs == null)
        {
            System.out.println("Could not find "+tokenFileName+"! Retokenize input file!");
            forceRetokenize();
            return;
        }
        HashMap levelNamesToAttributeLists = writeMarkableFiles(allLevelNames, allLevelFiles,allLevelSources,allWordIDs,(String)inputEncodings.getSelectedItem(),projectNameSpace);
        writeMMAXFile(outFileName, wordFileNameString, allLevelNames, allLevelFiles, allCustomizationFiles, allSchemeFiles, projectNameSpace);
        writeStyleSheet(stylePath.getText()+File.separator+"default_style.xsl", allLevelNames, selectedHandleTypes,allCarriageReturns);
        
        if (createDefaultSchemes.isSelected())
        {
            writeSchemeFiles(allSchemeFiles, levelNamesToAttributeLists);
        }
        
        if (createDefaultCustomizations.isSelected())
        {
            writeCustomizations(allLevelNames,allLevelColors, allLevelStyles);
        }
        
        writeCommonPathsFile(allLevelNames, allLevelFiles, allCustomizationFiles, allSchemeFiles);
        
        if (!bulkMode)
        {        
            if (JOptionPane.showConfirmDialog(this, "The project has been created. Do you wish to load it now?",
                                                    "Load new MMAX2 project?" , JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
            {
                mmax2.requestLoadFile(outFileName);
            }
        }
               
    }
            
    private final void writeCommonPathsFile(ArrayList levelNames, ArrayList levelFiles, ArrayList customizationFileNames, ArrayList schemeFileNames)
    {
        
        if (canWriteTo(projectPathName.getText()+File.separator+"common_paths.xml")==false)
        {
            return;
        }
        
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(projectPathName.getText()+File.separator+"common_paths.xml"),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        toWriter(writer,"<?xml version=\"1.0\"?>\n<!DOCTYPE common_paths>\n<common_paths>\n");        
        toWriter(writer,"<basedata_path>"+getCommonPath(projectPathName.getText(),basedataPath.getText())+"</basedata_path>\n");
        toWriter(writer,"<scheme_path>"+getCommonPath(projectPathName.getText(),schemePath.getText())+"</scheme_path>\n");
        toWriter(writer,"<style_path>"+getCommonPath(projectPathName.getText(),stylePath.getText())+"</style_path>\n");
        toWriter(writer,"<customization_path>"+getCommonPath(projectPathName.getText(),customizationPath.getText())+"</customization_path>\n");
        toWriter(writer,"<markable_path>"+getCommonPath(projectPathName.getText(),markablePath.getText())+"</markable_path>\n");
        
        toWriter(writer,"<views>\n<stylesheet>default_style.xsl</stylesheet>\n</views>\n");
        // Write header and word file name
        toWriter(writer,"<annotations>\n");
        String currentLine="<level name=\"";
        // Iterate over all levels
        for (int z=0;z<levelNames.size();z++)
        {
            currentLine = currentLine+(String)levelNames.get(z)+"\" schemefile=\""+(String)schemeFileNames.get(z)+"\" customization_file=\""+(String)customizationFileNames.get(z)+"\">$"+(String)levelFiles.get(z)+"</level>\n";
            toWriter(writer,currentLine);
            currentLine="<level name=\"";
        }
        
        toWriter(writer,"</annotations>\n");

        toWriter(writer,"</common_paths>");
        closeWriter(writer);
    }
    
    private final HashMap writeMarkableFiles(ArrayList allLevelNames, ArrayList allLevelFiles, ArrayList allLevelSources, ArrayList allWordIDs, String encoding, String projectNameSpace)
    {
        HashMap levelNamesToListsOfPertainingAttributes = new HashMap();
        BufferedWriter writer = null;
        // Iterate over all markable files to be created
        for (int z=0;z<allLevelFiles.size();z++)
        {
            if (canWriteTo(markablePath.getText()+File.separator+projectNameSpace+(String)allLevelFiles.get(z))==false)
            {
                continue;
            }            

            try
            {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(markablePath.getText()+File.separator+projectNameSpace+(String)allLevelFiles.get(z)),"UTF-8"));
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
                        
            String entry = "";
            // Create markable file header
            String header="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n<!DOCTYPE markables SYSTEM \"markables.dtd\">\n<markables xmlns=\"www.eml.org/NameSpaces/"+allLevelNames.get(z)+"\">\n";

            int markableCount=1;
            
            toWriter(writer,header);
            if (((String)allLevelSources.get(z)).equalsIgnoreCase("NONE"))
            {
                // do nothing, will produce an empty markable file
            }
            else if (((String)allLevelSources.get(z)).equalsIgnoreCase("WORD"))
            {                
                // Write a markable file that contains one markable per word
                for (int n=0;n<allWordIDs.size();n++)
                {
                    String markable = "<markable mmax_level=\""+(String)allLevelNames.get(z)+"\" id=\"markable_"+(int)(n+1)+"\" span=\""+(String)allWordIDs.get(n)+"\"/>\n";
                    toWriter(writer,markable);
                }                
            }
            else
            {
                writeMarkablesFromTags(completedTokens, (String)allLevelSources.get(z),(String)allLevelNames.get(z),  writer, levelNamesToListsOfPertainingAttributes);
            }                                    
            toWriter(writer,"</markables>");
            closeWriter(writer);
        }
        writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(markablePath.getText()+File.separator+"markables.dtd"),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        toWriter(writer,"<!ELEMENT markables (markable*)>\n<!ATTLIST markable id ID #REQUIRED>\n");        
        closeWriter(writer);
        
        return levelNamesToListsOfPertainingAttributes;
    }
    
    /** This method reads the *pre-tokenized* file tokenFileName using encoding encoding, and writes an XML words file wordFileName
        with one entry per *non-tag*. It returns the list of assigned word ids. */
    private final ArrayList writeWordFile(String wordFileName, String tokenFileName, String encoding)
    {        
        ArrayList allIDs = new ArrayList();
        
        if (canWriteTo(basedataPath.getText()+File.separator+wordFileName)==false)
        {
            return allIDs;
        }            

        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(basedataPath.getText()+File.separator+wordFileName),encoding));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        toWriter(writer,"<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n<!DOCTYPE words SYSTEM \"words.dtd\">\n<words>\n");        
        
        BufferedReader tokenFileReader = null;               
        try
        {
            tokenFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(tokenFileName),encoding));
            
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
            return null;
        }
        
        String currentLine="";
        int wordCount=1;
        while (true)
        {
            try
            {
                currentLine = tokenFileReader.readLine();
            }
            catch (java.io.IOException ex)
            {
                System.err.println(ex.getMessage());
            }
            if (currentLine==null)
            {
                break;
            }
    
            if (currentLine.startsWith("<") && currentLine.endsWith(">"))
            {
                // Skip all tags in token file
                continue;
            }            
            else
            {
                if (currentLine.startsWith("~~"))
                {
                    currentLine = currentLine.substring(2);                    
                    toWriter(writer,"<word meta=\"true\" id=\"word_"+wordCount+"\">"+currentLine.trim()+"</word>\n");
                }
                else
                {
                    toWriter(writer,"<word id=\"word_"+wordCount+"\">"+currentLine.trim()+"</word>\n");
                }
                allIDs.add(new String("word_"+wordCount));
                wordCount++;
            }        
        }    
        toWriter(writer,"</words>");
        closeWriter(writer);     
        
        writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(basedataPath.getText()+File.separator+"words.dtd"),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        toWriter(writer,"<!ELEMENT words (word*)>\n<!ELEMENT word (#PCDATA)>\n<!ATTLIST word id ID #REQUIRED>\n");        
        closeWriter(writer);  
        return allIDs;
    }
    
    private final void writeCustomizations(ArrayList allLevelNames, ArrayList allLevelColors, ArrayList allLevelStyles)
    {
        BufferedWriter writer = null;
        for (int z=0;z<allLevelNames.size();z++)
        {
            if (canWriteTo(customizationPath.getText()+File.separator+(String)allLevelNames.get(z)+"_customization.xml")==false)
            {
                continue;
            }
                
            try
            {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.customizationPath.getText()+File.separator+(String)allLevelNames.get(z)+"_customization.xml"),"UTF-8"));
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
                        
            String entry = "";
            toWriter(writer,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<customization>\n");
            String currentColor = MMAX2Utils.colorToHTML((Color) allLevelColors.get(z));
            String currentStyle = (String) allLevelStyles.get(z);            
            if (currentColor.equals("#000000")==false || currentStyle.equals("plain")==false)
            {
                // We have to create an actual entry
                entry = "<rule pattern=\"{all}\" style=\"";
                if (currentColor.equals("#000000")==false)
                {
                    entry=entry+"foreground=x:"+currentColor.substring(1);
                }
                if (currentStyle.indexOf("bold")!=-1)
                {
                    entry=entry+" bold=true";
                }
                if (currentStyle.indexOf("italic")!=-1)
                {
                    entry=entry+" italic=true";
                }
                entry = entry + "\" />";
            }
            toWriter(writer,entry+"\n</customization>");
            
            closeWriter(writer);
        }
        
    }
    
    private final void writeSchemeFiles(ArrayList schemeFiles, HashMap levelNamesToAttributeLists)    
    {
        
        BufferedWriter writer = null;        
        for (int z=0;z<schemeFiles.size();z++)
        {
            String currentSchemeFile = (String)schemeFiles.get(z);
            if (canWriteTo(schemePath.getText()+File.separator+currentSchemeFile)==false)
            {
                continue;
            }
                                
            try
            {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.schemePath.getText()+File.separator+currentSchemeFile),"UTF-8"));
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
            toWriter(writer,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<annotationscheme>\n");
            String temp = currentSchemeFile.substring(0,currentSchemeFile.indexOf("_scheme"));
            ArrayList attributesOnLevel = (ArrayList)levelNamesToAttributeLists.get(temp);
            if (attributesOnLevel != null)
            {
                for (int n=0;n<attributesOnLevel.size();n++)
                {
                    String att = (String)attributesOnLevel.get(n);
                    toWriter(writer,"<attribute id=\""+att+"_level\" name=\""+att+"\" type=\"freetext\">\n");
                    toWriter(writer,"<value id=\""+att+"_level_value\" name=\""+att+"\"/>\n");
                    toWriter(writer,"</attribute>\n");
                }
            }
            toWriter(writer,"</annotationscheme>\n");
            closeWriter(writer);
        }
    }
    
    private final void writeStyleSheet(String styleSheetName, ArrayList levelNames, ArrayList handleTypes, ArrayList carriageReturns)
    {
        
        if (canWriteTo(styleSheetName)==false)
        {
            return;
        }

        // Default: no handles are required
        boolean handlesRequired=false;
        // Iterate over all levels
        for (int b=0;b<handleTypes.size();b++)
        {
            // if at least one has handles and / or cr at the end, handles are required
            if (((String)handleTypes.get(b)).equalsIgnoreCase("none")==false || ((String)carriageReturns.get(b)).equalsIgnoreCase("true"))
            {
                handlesRequired=true;
                break;
            }
        }
        
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(styleSheetName),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }

        String header = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"\n";
        header = header + "xmlns:mmax=\"org.eml.MMAX2.discourse.MMAX2DiscourseLoader\"";
        for (int z=0;z<levelNames.size();z++)
        {
            header = header+"\nxmlns:"+(String)levelNames.get(z)+"=\"www.eml.org/NameSpaces/"+(String)levelNames.get(z)+"\"";
        }

        header = header + ">\n";
        header = header + "<xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\"/>\n";
        header = header + "<xsl:strip-space elements=\"*\"/>\n";
        
        header = header + "\n<xsl:template match=\"words\">\n  <xsl:apply-templates/>\n</xsl:template>\n";
        
        header = header + "\n<xsl:template match=\"word\">\n <xsl:value-of select=\"mmax:registerDiscourseElement(@id)\"/>\n";
        if (handlesRequired)
        {
            header = header +  "  <xsl:apply-templates select=\"mmax:getStartedMarkables(@id)\" mode=\"opening\"/>\n";
        }
        else
        {
            header = header + "\n";
        }
        header = header + "  <xsl:value-of select=\"mmax:setDiscourseElementStart()\"/>\n   <xsl:apply-templates/>\n  <xsl:value-of select=\"mmax:setDiscourseElementEnd()\"/>\n";
        
        if (handlesRequired)
        {
            header = header +"  <xsl:apply-templates select=\"mmax:getEndedMarkables(@id)\" mode=\"closing\"/>\n";
        }
        else
        {
            header = header + "\n";
        }

        header=header+"<xsl:text> </xsl:text>\n";
        header = header+"</xsl:template>\n";
        for (int z=0;z<levelNames.size();z++)
        {
            if (((String)handleTypes.get(z)).equalsIgnoreCase("none") && ((String)carriageReturns.get(z)).equalsIgnoreCase("false"))
            {
                // Skip if the current level does not have any handles
                continue;
            }
            
            String currentLevel=(String)levelNames.get(z);
            header = header+"\n\n<xsl:template match=\""+currentLevel+":markable\" mode=\"opening\">\n";
            
            String currentHandleType=(String) handleTypes.get(z);
            
            if (currentHandleType.indexOf("bold")!=-1)
            {
                header = header + "<xsl:value-of select=\"mmax:startBold()\"/>\n";
            }
            
            String handle=currentHandleType.substring(0,1);
            if (handle.equalsIgnoreCase("n")==false)
            {
                // Call add handle only if actual handle is requested
                header=header+ "<xsl:value-of select=\"mmax:addLeftMarkableHandle(@mmax_level, @id, '"+handle+"')\"/>\n";
            }
            
            if (currentHandleType.indexOf("bold")!=-1)
            {
                header = header + "<xsl:value-of select=\"mmax:endBold()\"/>\n";
            }            
            header=header+"</xsl:template>";
            header = header+"\n\n<xsl:template match=\""+currentLevel+":markable\" mode=\"closing\">\n";

            if (currentHandleType.indexOf("bold")!=-1)
            {
                header = header + "<xsl:value-of select=\"mmax:startBold()\"/>\n";
            }
            
            if (handle.equalsIgnoreCase("n")==false)
            {
                handle=currentHandleType.substring(2,3);
                header=header+ "<xsl:value-of select=\"mmax:addRightMarkableHandle(@mmax_level, @id, '"+handle+"')\"/>\n";
            }            

            if (currentHandleType.indexOf("bold")!=-1)
            {
                header = header + "<xsl:value-of select=\"mmax:endBold()\"/>\n";
            }            
            
            if (((String)carriageReturns.get(z)).equalsIgnoreCase("true"))
            {
                header=header+"<xsl:text>\n</xsl:text>\n";
            }
            header=header+"</xsl:template>"; 
        }
        
        header = header + "\n</xsl:stylesheet>";
        toWriter(writer,header);
        closeWriter(writer);
        
    }
    
    private final boolean allMarkableLevelsSpecified()
    {
        boolean allSpecified=true;
        for (int z=2;z<markableLevelBox.getComponentCount()-1;z++)
        {
            Box currentBox =(Box) ((JPanel)markableLevelBox.getComponent(z)).getComponent(0);
            if (((JTextField) currentBox.getComponent(1)).getText().trim().equals(""))
            {
                allSpecified=false;
                break;
            }
            if (((JTextField) currentBox.getComponent(5)).getText().trim().equals("") && 
                  ((String)((JComboBox) currentBox.getComponent(3)).getSelectedItem()).equals("other ..."))
            {
                allSpecified=false;
                break;                
            }            
        }
        return allSpecified;
    }
    
    private final void writeMMAXFile(String outFileName, String wordFileName, ArrayList levelNames, ArrayList levelFiles, ArrayList customizationFileNames, ArrayList schemeFileNames, String projectNameSpace)
    {
        // Create file
        
        BufferedWriter writer = null;
        
        if (canWriteTo(outFileName)==false)
        {
            return;
        }
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
        

        // Write header and word file name
        toWriter(writer,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mmax_project>\n<words>"+wordFileName+"</words>\n<keyactions></keyactions>\n<gestures></gestures>\n");
        toWriter(writer,"</mmax_project>");        
        closeWriter(writer);
    }
    

    private final void writeMarkablesFromTags(ArrayList allTokens, String tagNameToWrite, String levelName, BufferedWriter writer, HashMap levelNamesToPertainingAttributeLists)
    {    
    	// This is the central method for mapping xml tags from the input file to markables
        ArrayList allTagsToWrite = new ArrayList();
        int nextMarkableID=1;
        
        if(tagNameToWrite.startsWith("specify ..."))
        {
            // Cut off leading 'specify ...'
        	// This is the case when the xml tag to store on the current level has been specified using the specify... option
        	// tagNameToWrite may be a list of names
            tagNameToWrite = tagNameToWrite.substring(12);            
            StringTokenizer toki = new StringTokenizer(tagNameToWrite,",");
            while(toki.hasMoreTokens())
            {
                allTagsToWrite.add(toki.nextToken().trim());
            }            
        }
        else
        {
            allTagsToWrite.add(tagNameToWrite);
        }
        
        // allTagsToWrite now contains the names of all xml tags to be mapped to this markable level
        
        ArrayList attributesOnCurrentLevel = new ArrayList();
        // Iterate over all tag names to write, extracted from tagnametowrite
        for (int q=0;q<allTagsToWrite.size();q++)
        {
        	// Get current tage name to write
            tagNameToWrite = (String) allTagsToWrite.get(q);
        
            String currentToken="";
            int currentValidWordCount=0;
        
            int currentTagStartIndex=0;
            int currentTagEndIndex=0;
        
            // Iterate over all tokens, incl. all tags
            for (int z=0;z<allTokens.size();z++)
            {
                // Get current token or tag
                currentToken = (String)allTokens.get(z);
                if (currentToken.startsWith("<")==false)
                {
                    // The current token is a normal word
                    // count it
                    currentValidWordCount++;
                    continue;
                }
                if (currentToken.startsWith("<"+tagNameToWrite+">") || currentToken.startsWith("<"+tagNameToWrite+" "))
                {
                    // The current token is a start tag of the type that is to be written currently
                
                    // Store currentValidWordCount as span start string                                                
                    currentTagStartIndex = currentValidWordCount;
                    // Store opening tag, which is the one with the attributes (if any)
                    String openingTag = currentToken;
                    int temporalValidWordCount=currentValidWordCount;
                    // Search forward for closing tag
                    // Note: Modify so that it will handle embedded tags of the same name correctly
                    int embeddedTagsOfTheSameName = 0;
                    // Iterate forward through token list
                    for (int o=z+1;o<allTokens.size();o++)
                    {
                        // Get current potential closing tag
                        String currentPotentialClosingTag=(String)allTokens.get(o);                        
                        if (currentPotentialClosingTag.startsWith("<"+tagNameToWrite+">") || currentPotentialClosingTag.startsWith("<"+tagNameToWrite+" "))
                        {
                        	// While searching for the closing tag of currentToken, we encountered an embedded tag of the same name
                        	embeddedTagsOfTheSameName++;
                        	System.err.println("Found an embedded tag "+currentPotentialClosingTag);
                        }
                        
                        if (currentPotentialClosingTag.startsWith("</"+tagNameToWrite+">") || currentPotentialClosingTag.startsWith("</"+tagNameToWrite+" "))
                        {
                        	// We found a tag that might be the closing tag of current token
                        	if (embeddedTagsOfTheSameName !=0)
                        	{
                        		// No, it closes some other embedded tag of the same name
                        		embeddedTagsOfTheSameName--;
                        		// Move on to inspect next token
                        		continue;
                        	}

                            // The currently open tag is closed here
                            currentTagEndIndex=temporalValidWordCount-1;
                            String span="";
                            if (currentTagStartIndex==currentTagEndIndex)
                            {
                                span="word_"+(currentTagStartIndex+1);
                            }
                            else
                            {
                                span="word_"+(currentTagStartIndex+1)+"..word_"+(currentTagEndIndex+1);
                            }                                                
                            ArrayList attributeList = toAttributeList(openingTag.substring(tagNameToWrite.length()+1));
                            String attributeString="";
                            attributeList.add("imported_tag_type___"+tagNameToWrite.toLowerCase());
                            for (int u=0;u<attributeList.size();u++)
                            {
                                String currentEntry = (String) attributeList.get(u);
                                String currentAttribute = currentEntry.substring(0,currentEntry.indexOf("___")).toLowerCase();
                                if (currentAttribute.equalsIgnoreCase("id"))
                                {
                                    currentAttribute=tagNameToWrite+"_id";
                                }
                                if (attributesOnCurrentLevel.contains(currentAttribute)==false)
                                {
                                    attributesOnCurrentLevel.add(currentAttribute);
                                }
                                
                                String currentValue = currentEntry.substring(currentEntry.indexOf("___")+3).toLowerCase();
                                attributeString = attributeString + currentAttribute+"=\""+currentValue+"\" ";
                            }
                            String markable = "<markable mmax_level=\""+levelName+"\" id=\"markable_"+nextMarkableID+"\" span=\""+span+"\" "+attributeString+"/>";
                            toWriter(writer,markable+"\n");
                            nextMarkableID++;
                            break;
                        }
                        else
                        {
                            // The currently open tag is not closed
                            if (currentPotentialClosingTag.startsWith("<")==false)
                            {
                                // The current token is a normal word
                                // count it
                                temporalValidWordCount++;
                                continue;
                            }
                        }                     
                    }
                }            
            }// end iteration over all tokens
        }      
        levelNamesToPertainingAttributeLists.put(levelName, attributesOnCurrentLevel);
        return;
    }
        
    /** This is called for each line as read from the infile, to break multi-tag lines into separate lines. */
    private final  ArrayList breakTags(String line)
    {
        // Create list to store resulting tags
        ArrayList result = new ArrayList();
        String currentSubLine="";
        // Run through line from left to right
        for (int z=0;z<line.length();z++)
        {
            if (line.charAt(z)=='<')
            {
                // The char at the current pos is a <, which starts a tag
                if (currentSubLine.equals("")==false)
                {
                    // If there is some old tag, store it
                    result.add(currentSubLine.trim());
                }
                // Start new tag
                currentSubLine = "<";
                continue;
            }
            else if (line.charAt(z)=='>')
            {
                // The char at the current pos is a >, which ends a tag
                currentSubLine = currentSubLine + ">";
                currentSubLine = currentSubLine.trim();
                if (currentSubLine.endsWith("/>"))
                {
                    // The current attribute is an empty one
                    // So one of its attributes (or its tag name) has to be externalized
                    // This stores the empty tag as a sequence of open tag, content and close-tag
                    result.addAll(externalizeAttribute(currentSubLine));
                }
                else
                {
                    // The current tag is not an empty one, so just store it
                    if (currentSubLine.equals("")==false)
                    {
                        result.add(currentSubLine);
                    }
                }
                currentSubLine = "";
                continue;
            }
            else
            {
                currentSubLine = currentSubLine + line.charAt(z);
            }
        }
         
        if (currentSubLine.equals("")==false)
        {
            result.add(currentSubLine);
        }
        
        if (result.size()<=1) result = null;
        return result;
    }
    
    /** This method receives the string of an empty (i.e. />-ending) tag and returns the string to be
        inserted into the base data. 
        If the tags has no attributes, the name of the tag is returned.
        If the tag has only one attribute, this attribute's value is returned.
        If the tag has more than one attributes, it asks the user to select one. The result of this
        selection is stored, so that for each tag it is asked only once. */
    private final  ArrayList externalizeAttribute(String emptyTag)
    {
        ArrayList result = new ArrayList();

        // Try if element has a space (first space must be after tag name)
        int spacePos = emptyTag.indexOf(" ");
        String tagName = "";
        if (spacePos == -1)
        {
            // There is no space in the tag name
            tagName = emptyTag.substring(1,emptyTag.length()-2).trim();
        }
        else
        {
            tagName = emptyTag.substring(1,spacePos).trim();
        }
        // Create identical copy of org. tag, with only closing > at end
        String openingTag =emptyTag.substring(0,emptyTag.length()-2)+">";
        
        if (emptyTag.indexOf("=")==-1)
        {            
            // The tag has no attributes <Pause/> --> <Pause>Pause</Pause>
            result.add("<"+tagName+">");
            result.add("~~"+tagName);
            result.add("</"+tagName+">");
        }
        else
        {
        	// The tag has at least one attribute
            ArrayList attributeValuePairs = toAttributeList(emptyTag);
            
            // New: Ask anyway
                            
            String message = "The tag "+tagName+" is empty, i.e. has no textual content element. ";
            message = message + "Select an attribute to use as textual element for this tag.\n";
            message = message + "Note: The base data element created from this attribute's value will be tagged as 'meta=true'. \n";
            String[] options = new String[attributeValuePairs.size()+1];                
            // Fill array of options, use tagName as first options
            options[0] = tagName;
            for (int q=0;q<attributeValuePairs.size();q++)
            {                    
            	options[q+1] = (String)attributeValuePairs.get(q);
            }
                
            int r = -1;
                
            // The empty tag has more than one attribute
            if (manualDecisions.containsKey(tagName)==false)
            {
            	// No earlier decision about tagName is available
            	r = JOptionPane.showOptionDialog(null, message, "Select attribute", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
            	while (r==-1)
            	{   
            		r = JOptionPane.showOptionDialog(null, message, "Select attribute", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
            	}
            }
            else
            {
            	// An answer about which attribute to externalize for tagName has been given before
            	String earlierSelectedAttributeName = (String)manualDecisions.get(tagName);
            	for (int h=0;h<options.length;h++)
            	{
            		if (options[h].equals(earlierSelectedAttributeName) || options[h].startsWith(earlierSelectedAttributeName+"_"))
            		{
            			r=h;
            			break;
            		}                            
            	}
            }
            String selectedAttributeName=options[r];
            if (r!=0)
            {
            	selectedAttributeName = selectedAttributeName.substring(0,selectedAttributeName.indexOf("___"));    
            }                                
            manualDecisions.put(tagName, selectedAttributeName);
                
            result.add(openingTag);
            if (r==0)
            {
            	result.add("~~"+tagName);
            }
            else
            {
            	String temp = options[r];
            	// add VALUE of selected attribute as externalized string
            	result.add("~~"+temp.substring(temp.indexOf("___")+3));
            }
            result.add("</"+tagName+">");                           
        }        
        return result;
    }

    /** This method converts a string of the form "attribute1=value1 attribute2=value2 ... in a list of
     *  entries of the form attribute1="value1" etc. */
    private final static ArrayList toAttributeList(String attributeString)
    {        
        ArrayList result = new ArrayList();
        if (attributeString.endsWith("/>"))
        {
            attributeString = attributeString.substring(0,attributeString.length()-2);
        }
        
        if (attributeString.startsWith("<"))
        {
            attributeString = attributeString.substring(1);
        }
        if (attributeString.indexOf("=")!=-1)
        {
            attributeString = attributeString.substring(attributeString.indexOf(" "));
        }
        else
        {
            return result;
        }

        attributeString = attributeString.trim();
                        
        boolean inAttributeName = true;
        boolean inValue = false;
        
        String currentAttributeName="";
        String currentValue="";
        
        for (int m=0;m<attributeString.length();m++)
        {    
            if (attributeString.charAt(m)=='=')
            {
                inAttributeName=false;
                continue;
            }
            
            if (inAttributeName)
            {
                currentAttributeName = currentAttributeName + attributeString.charAt(m);
                continue;
            }
            if (attributeString.charAt(m)=='"')
            {
                if (inValue)
                {
                    // We found the " ending a value
                    result.add(currentAttributeName+"___"+currentValue);
                    currentAttributeName="";
                    currentValue="";
                    //inAttributeName=false;
                    m++;
                    if (m>=attributeString.length())
                    {
                        break;
                    }
                    while(attributeString.charAt(m)==' ')
                    {
                        m++;
                        if (m>=attributeString.length())
                        {
                            break;
                        }
                    }
                    inValue=false;
                    inAttributeName=true;
                    currentAttributeName = currentAttributeName+attributeString.charAt(m);
                    continue;
                }
                else
                {
                    // We found = " beginning a value
                    inValue = true;
                    continue;
                }
            }
            if (inValue)
            {
                currentValue = currentValue+attributeString.charAt(m);
                continue;
            }
        }        
        return result;       
    }       
}
