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

package org.eml.MMAX2.discourse;

// Display Attributes
import java.awt.Color;
import java.io.File;

import javax.swing.JOptionPane;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.NodeSet;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableFileLoader;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class MMAX2DiscourseLoader 
{
    protected String commonPathsFile = "common_paths.xml";
    
    protected String commonSchemePath = "";
    protected String commonStylePath = "";
    protected String commonBasedataPath = "";
    protected String commonCustomizationPath = "";
    protected String commonMarkablePath="";
    protected String commonQueryPath="";
    
    protected String rootPath = "";    
    protected String wordFileName = "";
    
    protected String[] markableFileNames;
    protected String[] markableLevelNames;
    protected String[] schemeFileNames;
//    protected String[] attributeStrings;    
    protected String[] customizationFileNames;
    protected String[] startupModes;
    
    /** Number of annotation levels (i.e. markable files) in .mmax file. */
    protected int levelCount = 0;
    
    protected String[] styleSheetFileNames = new String[0];
    protected String[] userSwitches = new String[0];        
        
    private String nameSpace="";    
    /** This has to be static so it can be called from static method getDiscourseElementNodes(String span). */
    protected static MMAX2Discourse currentDiscourse;

    /** Creates new DiscourseLoader from the supplied .MMAX file. */
    public MMAX2DiscourseLoader(String fileName, boolean withGUI, String suppliedCommonPathsFile) 
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

        // Suppose that default is to be used
        boolean useDefault = true;
        if (suppliedCommonPathsFile.equals("")==false)
        {
            // Sth. has been supplied on the command line, so use that and not default
            commonPathsFile = suppliedCommonPathsFile;
            // Remember that some supplied file is to be used
            useDefault = false;
        }
        
        DOMParser parser = new DOMParser();
        DocumentImpl MMAXDOM = null;
        
        // Extract root path, which is the path of the .mmax file
        rootPath = fileName.substring(0,fileName.lastIndexOf(java.io.File.separator)+1);
        // Set all paths to root by default; may be overwritten later
        commonSchemePath = rootPath;
        commonStylePath = rootPath;
        commonBasedataPath = rootPath;
        commonCustomizationPath = rootPath;
        commonMarkablePath = rootPath;
        commonQueryPath = rootPath;
        
        try
        {
            parser.setFeature("http://xml.org/sax/features/validation",false);
        }
        catch (org.xml.sax.SAXNotRecognizedException ex)
        {
            ex.printStackTrace();            
            return;
        }
        catch (org.xml.sax.SAXNotSupportedException ex)
        {
            ex.printStackTrace();
            return;
        }
                
        try
        {            
            //parser.parse(new InputSource("FILE:"+fileName));
        	parser.parse(new InputSource(new File(fileName).toURI().toString()));
        }
        catch (org.xml.sax.SAXParseException exception)
        {
            String error = "Line: "+exception.getLineNumber()+" Column: "+exception.getColumnNumber()+"\n"+exception.toString();            
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }                
        catch (org.xml.sax.SAXException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
        catch (java.io.IOException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
              
        MMAXDOM = (DocumentImpl) parser.getDocument();
                
        try
        {
            if (useDefault)
            {
                // Look for common_paths wrt to root
                //parser.parse(new InputSource("FILE:"+rootPath+commonPathsFile));
            	parser.parse(new InputSource(new File(rootPath+commonPathsFile).toURI().toString()));
            }
            else
            {
                // Try to load the supplied common_paths (NO rootptah here!)
                //parser.parse(new InputSource("FILE:"+commonPathsFile));                
                parser.parse(new InputSource(new File(commonPathsFile).toURI().toString()));
            }
        }
        catch (org.xml.sax.SAXParseException exception)
        {   
            String error = "Line: "+exception.getLineNumber()+" Column: "+exception.getColumnNumber()+"\n"+exception.toString();            
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }                
        catch (org.xml.sax.SAXException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
        catch (java.io.FileNotFoundException exception)
        {   
            // The common path was not found
            if (!useDefault)
            {
                // and it was a supplied one
                String error = exception.toString();
                JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
                return;
                //System.exit(0);
            }
            else
            {
                // It was the default one that was not found
                JOptionPane.showMessageDialog(null,"No default common_paths.xml found!","DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
                return;
                //System.exit(0);
            }
        }
        catch (java.io.IOException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
                        
        if (parser.getDocument()!=null)
        {
            DocumentImpl CPFDOM = null;
            CPFDOM = (DocumentImpl) parser.getDocument();
            NodeList currentCommonPathNodeList = null;
            currentCommonPathNodeList = CPFDOM.getElementsByTagName("scheme_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempSchemePath = "";
                try
                {
                    tempSchemePath = toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());
                }
                catch (java.lang.NullPointerException ex)
                {
                    //ex.printStackTrace();
                }

                if (new java.io.File(tempSchemePath).isAbsolute())
                {
                    commonSchemePath = tempSchemePath;
                }
                else
                {
                    commonSchemePath = commonSchemePath+tempSchemePath;
                }     
            }
            currentCommonPathNodeList = null;
            
            currentCommonPathNodeList = CPFDOM.getElementsByTagName("style_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempStylePath = "";
                try
                {
                    tempStylePath = toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());
                }
                catch (java.lang.NullPointerException ex)
                {
                	//ex.printStackTrace();
                }
                
                if (new java.io.File(tempStylePath).isAbsolute())
                {
                    commonStylePath = tempStylePath;
                }
                else
                {
                    commonStylePath = commonStylePath+tempStylePath;
                }                     
            }
            currentCommonPathNodeList = null;
            
            currentCommonPathNodeList = CPFDOM.getElementsByTagName("basedata_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempBasedataPath="";
                try
                {
                    tempBasedataPath =toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());
                }
                catch (java.lang.NullPointerException ex)
                {
                    //ex.printStackTrace();
                }
                
                if (new java.io.File(tempBasedataPath).isAbsolute())
                {
                    commonBasedataPath = tempBasedataPath;
                }
                else
                {
                    commonBasedataPath = commonBasedataPath+tempBasedataPath;
                }                     
            }
            currentCommonPathNodeList = null;

            currentCommonPathNodeList = CPFDOM.getElementsByTagName("customization_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempCustomizationPath="";
                try
                {
                    tempCustomizationPath = toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());                    
                }
                catch (java.lang.NullPointerException ex)
                {
                    //ex.printStackTrace();
                }
                
                if (new java.io.File(tempCustomizationPath).isAbsolute())
                {
                    commonCustomizationPath = tempCustomizationPath;
                }
                else
                {
                    commonCustomizationPath = commonCustomizationPath+tempCustomizationPath;
                }                                     
            }
            currentCommonPathNodeList = null;                        

            currentCommonPathNodeList = CPFDOM.getElementsByTagName("markable_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempMarkablePath="";
                try
                {
                    tempMarkablePath =toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());
                }
                catch (java.lang.NullPointerException ex)
                {
                    //ex.printStackTrace();
                }
                
                if (new java.io.File(tempMarkablePath).isAbsolute())
                {
                    commonMarkablePath = tempMarkablePath;
                }
                else
                {
                    commonMarkablePath = commonMarkablePath+tempMarkablePath;
                }                                     
                
            }
            currentCommonPathNodeList = null;                        

            currentCommonPathNodeList = CPFDOM.getElementsByTagName("query_path");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                String tempQueryPath="";
                try
                {
                    tempQueryPath = toPlatformdependentPath(currentCommonPathNodeList.item(0).getFirstChild().getNodeValue());
                }
                catch (java.lang.NullPointerException ex)
                {
                	ex.printStackTrace();
                }
                
                if (new java.io.File(tempQueryPath).isAbsolute())
                {
                    commonQueryPath = tempQueryPath;
                }
                else
                {
                    commonQueryPath = commonQueryPath+tempQueryPath;
                }                                                     
            }
            currentCommonPathNodeList = null;                        

            
            currentCommonPathNodeList = CPFDOM.getElementsByTagName("views");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                // Get all <views> elements
                NodeList views = CPFDOM.getElementsByTagName("stylesheet");
                if (views.getLength() !=0)
                {
                    // Get number of <level> elements.
                    int viewCount = views.getLength();
                        
                    styleSheetFileNames = new String[viewCount];
            
                    for (int z=0;z<viewCount;z++)
                    {
                        try
                        {
                            styleSheetFileNames[z] = commonStylePath+views.item(z).getFirstChild().getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Empty <stylesheet> entry in .mmax file!","DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
                        }                       
                    }
                }            
            }
            
            currentCommonPathNodeList = CPFDOM.getElementsByTagName("user_switches");
            if (currentCommonPathNodeList.getLength()!=0)
            {
                // Get all user switches
                NodeList switches = CPFDOM.getElementsByTagName("user_switch");
                // Create array to accept user switch strings
                userSwitches = new String[switches.getLength()];
                for (int z=0;z<switches.getLength();z++)
                {                    
                    String name="";
                    String desc="";
                    String def="";
                    try
                    {
                        name=switches.item(z).getAttributes().getNamedItem("name").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        System.err.println("User switch with empty 'name' attribute, ignored!");
                        continue;
                    }
                    
                    try
                    {
                        desc=switches.item(z).getAttributes().getNamedItem("description").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        desc=" ";
                    }   
                    try
                    {
                        def=switches.item(z).getAttributes().getNamedItem("default").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        def="off";
                    }                                                            
                    userSwitches[z] = name+":::"+desc+":::"+def;
                }                                
            }
            
            // New: Try to read <annotations> tag from common_paths first
            NodeList temp = CPFDOM.getElementsByTagName("annotations");
            if (temp.getLength()!=0)
            {
                if (verbose) System.err.println("\nReading <annotation> tags from common paths file "+commonPathsFile);
                nameSpace = fileName.substring(fileName.lastIndexOf(java.io.File.separator)+1);
                nameSpace = nameSpace.substring(0,nameSpace.indexOf(".mmax"));                
            
                /** Get all <level> elements from common_path file. */
                NodeList annotations = CPFDOM.getElementsByTagName("level");
                if (annotations.getLength() !=0)
                {
                    /** Get number of <level> elements. */
                    levelCount = annotations.getLength();
                        
                    markableFileNames = new String[levelCount];
                    markableLevelNames = new String[levelCount];
                    schemeFileNames = new String[levelCount];
                    //attributeStrings = new String[levelCount];
                    customizationFileNames = new String[levelCount];
                    startupModes = new String[levelCount];
                
                    for (int z=0;z<levelCount;z++)
                    {
                        String tempMarkablePath = "";
                        try
                        {
                            // Get path as it is given in the level tag
                            tempMarkablePath = annotations.item(z).getFirstChild().getNodeValue();
                            if (new java.io.File(tempMarkablePath).isAbsolute())
                            {
                                // The path in the tag is absolute, so do not prepend common path
                                markableFileNames[z] = tempMarkablePath;
                            }
                            else
                            {
                                markableFileNames[z] = commonMarkablePath+tempMarkablePath;
                            }
                            // NEW: Substitute $ with actual name to be used for this document
                            markableFileNames[z] = markableFileNames[z].replaceAll("\\$",nameSpace);
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Empty <level> entry in common paths file "+commonPathsFile+"!","DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        }
                
                        try
                        {
                            markableLevelNames[z] = annotations.item(z).getAttributes().getNamedItem("name").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Missing 'name' attribute for level "+markableFileNames[z],"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);                
                            System.exit(0);                
                        }

                        try
                        {
                            schemeFileNames[z] = commonSchemePath+annotations.item(z).getAttributes().getNamedItem("schemefile").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Missing 'schemefile' attribute for level "+markableFileNames[z],"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);                
                            System.exit(0);                                
                        }
                    
                        /*
                        try
                        {
                            attributeStrings[z] = annotations.item(z).getAttributes().getNamedItem("default").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            attributeStrings[z] = "";
                        }                                        
                        */
                        try
                        {
                            customizationFileNames[z] = commonCustomizationPath+annotations.item(z).getAttributes().getNamedItem("customization_file").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            customizationFileNames[z] = "";
                        }
                        
                        try
                        {
                            startupModes[z] = annotations.item(z).getAttributes().getNamedItem("at_startup").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            startupModes[z] = "active";
                        }                              
                    }
                }
            }                                                        
        }
        
        
        
        NodeList temp = null;
        temp = MMAXDOM.getElementsByTagName("root_path");
        if (temp.getLength()!=0)
        {
            System.err.println("Found deprecated <root_path> tag, ignored!");
        }
 
        temp = MMAXDOM.getElementsByTagName("words");
        if (temp.getLength()!=0)
        {
            try
            {
                wordFileName = commonBasedataPath+temp.item(0).getFirstChild().getNodeValue();
            }
            catch (java.lang.NullPointerException ex)
            {
            	ex.printStackTrace();
            }
        }                             
        if (nameSpace.equals("")==true)
        {
            // Read <annotations> from this file only if they were not found in <common_paths> earlier
            temp = MMAXDOM.getElementsByTagName("annotations");
            if (temp.getLength()!=0)
            {
                /** Get all <level> elements. */
                NodeList annotations = MMAXDOM.getElementsByTagName("level");
                if (annotations.getLength() !=0)
                {
                    /** Get number of <level> elements. */
                    levelCount = annotations.getLength();
                        
                    markableFileNames = new String[levelCount];
                    markableLevelNames = new String[levelCount];
                    schemeFileNames = new String[levelCount];
                    //attributeStrings = new String[levelCount];
                    customizationFileNames = new String[levelCount];
                    startupModes = new String[levelCount];
                    
                    for (int z=0;z<levelCount;z++)
                    {
                        try
                        {
                            markableFileNames[z] =commonMarkablePath+ annotations.item(z).getFirstChild().getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Empty <level> entry in .mmax file!","DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
                            System.exit(0);                
                        }
                
                        try
                        {
                            markableLevelNames[z] = annotations.item(z).getAttributes().getNamedItem("name").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Missing 'name' attribute for level "+markableFileNames[z],"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);                
                            System.exit(0);                
                        }

                        try
                        {
                            schemeFileNames[z] = commonSchemePath+annotations.item(z).getAttributes().getNamedItem("schemefile").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            JOptionPane.showMessageDialog(null,"Missing 'schemefile' attribute for level "+markableFileNames[z],"DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);                
                            System.exit(0);                                
                        }
                    
                        /*
                        try
                        {
                            attributeStrings[z] = annotations.item(z).getAttributes().getNamedItem("default").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            attributeStrings[z] = "";
                        }                                        
                    */
                        try
                        {   
                            customizationFileNames[z] = commonCustomizationPath+annotations.item(z).getAttributes().getNamedItem("customization_file").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            customizationFileNames[z] = "";
                        }
                        try
                        {
                            startupModes[z] = annotations.item(z).getAttributes().getNamedItem("at_startup").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            startupModes[z] = "active";
                        }                          
                    }
                }
            }
        }
        
        /* Create MMAX2Discourse object */
        currentDiscourse = new MMAX2Discourse(withGUI);
        
        currentDiscourse.setCommonBasedataPath(commonBasedataPath);
        
        if (withGUI)
        {
        	currentDiscourse.getCurrentMarkableChart().currentLevelControlWindow.setStyleSheetSelector(styleSheetFileNames);
        }
        currentDiscourse.setCurrentStyleSheet(styleSheetFileNames[0]);
        
        currentDiscourse.setNameSpace(nameSpace);
        
        DiscourseElementFileLoader deloader = new DiscourseElementFileLoader();
        if (wordFileName.equals("")==false) 
        {
            deloader.load(wordFileName);
            currentDiscourse.setWordDOM(deloader.getDOM());
            currentDiscourse.setWordFileName(wordFileName);
        }
        else
        {
        	System.err.println("No word file name given!");
        }
        
        
        if (styleSheetFileNames.length ==0)
        {
            JOptionPane.showMessageDialog(null,"You must specify at least one XSL style sheet file!","DiscourseLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
            System.exit(0);                        
        }
        else
        {
            currentDiscourse.setStyleSheetFileNames(this.styleSheetFileNames);
        }
        
        // Create one instance of mfl
        MarkableFileLoader mfl = new MarkableFileLoader();
        int currentMaxID =0;
        int totalMaxID = 0;
        // Iterate over all markable file names found
        for (int p=0;p<levelCount;p++)
        {
            // Use mfl to load current markable file
            // This will produec a markable level object
            if (verbose) System.err.println("Loading level "+markableLevelNames[p]+" ... ");
            //mfl.load(markableFileNames[p],markableLevelNames[p],schemeFileNames[p], attributeStrings[p],customizationFileNames[p], startupModes[p]);
            mfl.load(markableFileNames[p],markableLevelNames[p],schemeFileNames[p],customizationFileNames[p], startupModes[p]);
            // Get markable level object. Up to now, this has only a non-null DOM, but no markables yet
            MarkableLevel newLevel = mfl.getMarkableLevel();
            // Set reference to associated discourse. This is required for next call (cf. below)
            newLevel.setCurrentDiscourse(currentDiscourse);
            currentMaxID = newLevel.createMarkables();            
            if (currentMaxID > totalMaxID) totalMaxID = currentMaxID;
            currentDiscourse.getCurrentMarkableChart().addMarkableLevel(newLevel);
        }                
        currentDiscourse.getCurrentMarkableChart().setNextFreeMarkableIDNum(totalMaxID+1);
    }
    
    private final String toPlatformdependentPath(String inPath)
    {
        String result = "";
        // Determine current platform
        if (java.io.File.separator.equals("/"))
        {
            // The current platform is UNIX
            result = inPath.replace('\\', '/');
            
        }
        else
        {
            // The current platform is Windows
            result = inPath.replace('/', '\\');
        }        
        return result;
    }
    
    public final String getCommonQueryPath()
    {
        return commonQueryPath;
    }

    
    public final String[] getUserSwitches()
    {
        return userSwitches;
    }
        
    public final String getWorkingDirectory()
    {
        return rootPath;
    }
    
    /** Returns the Discourse object currently loaded by this loader. */
    public final MMAX2Discourse getCurrentDiscourse()
    {
        return currentDiscourse;
    }    
               
    public static String addHotSpot(String toDisplay, String hotSpotText)
    {
        int extent = toDisplay.length();
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.hotSpotDisplayAssociation.put(new Integer(currentDocumentPosition+temp),hotSpotText);
        }
        return toDisplay;
    }
    
    
    public static String concat(String string1, String string2)
    {
        return string1+" "+string2;
    }
    
    public static String concat(String string1, String string2, String string3)
    {
        return string1+" "+string2+" "+string3;
    }
    
    public static String concat(String string1, String string2, String string3, String string4)
    {
        return string1+" "+string2+" "+string3+" "+string4;
    }
    
    public static String concat(String string1, String string2, String string3, String string4, String string5)
    {
        return string1+" "+string2+" "+string3+" "+string4+" "+string5;
    }
    
    /** Adds a left markable handle (clickable area directly associated with a markable) of size extent. */    
    public static void addLeftMarkableHandle(String layerName, String markableId, int extent)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        currentMarkable.addLeftHandlePosition(currentDocumentPosition);        
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
    }        
    
    /** Adds a right markable handle (clickable area directly associated with a markable) of size extent. */    
    public static void addRightMarkableHandle(String layerName, String markableId, int extent)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();       
        currentMarkable.addRightHandlePosition(currentDocumentPosition+extent-1);        
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
    }    


    
    
    
    

    /** Adds handleText as a left markable handle (clickable area directly associated with a markable). */    
    public static String addLeftMarkableHandle(String layerName, String markableId, String handleText, int highlightPos)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        if (highlightPos == 1)
        {
            currentMarkable.addLeftHandlePosition(currentDocumentPosition);    
        }
        else
        {
            currentMarkable.addLeftHandlePosition(currentDocumentPosition+highlightPos-1);
        }
        int extent = handleText.length();
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
        return handleText;
    }        

    /** Adds handleText as a left markable handle (clickable area directly associated with a markable). */    
    public static String addLeftMarkableHandle(String layerName, String markableId, String handleText)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        currentMarkable.addLeftHandlePosition(currentDocumentPosition);    
        int extent = handleText.length();
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
        return handleText;
    }        
    
    
    /** Adds handleText as a right markable handle (clickable area directly associated with a markable). */    
    public static String addRightMarkableHandle(String layerName, String markableId, String handleText, int highlightPos)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        int extent = handleText.length();
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        if (highlightPos == extent)
        {
            currentMarkable.addRightHandlePosition(currentDocumentPosition+extent-1);       
        }
        else
        {
            currentMarkable.addRightHandlePosition(currentDocumentPosition+extent-1-(extent-highlightPos));
        }
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
        return handleText;
    }    
    
    /** Adds handleText as a right markable handle (clickable area directly associated with a markable). */    
    public static String addRightMarkableHandle(String layerName, String markableId, String handleText)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        int extent = handleText.length();
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        currentMarkable.addRightHandlePosition(currentDocumentPosition+extent-1);       
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
        return handleText;
    }    
    
    
    /** Adds a right markable handle (clickable area directly associated with a markable) of size extent, where the actual handle is at position leftHandle relative to extent. */    
    public static void addLeftMarkableHandle(String layerName, String markableId, int extent, int leftHandle)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        currentMarkable.addLeftHandlePosition(currentDocumentPosition+leftHandle-1);        
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
    }    
    
    /** Adds a right markable handle (clickable area directly associated with a markable) of size extent, where the actual handle is at position rightHandle relative to extent. */    
    public static void addRightMarkableHandle(String layerName, String markableId, int extent, int rightHandle)    
    {
        /** Get reference to markable to which handle is added. */
        Markable currentMarkable = currentDiscourse.getCurrentMarkableChart().getMarkableLevelByName(layerName,true).getMarkableByID(markableId);
        /** Get current document position, i.e. character stream position. */
        int currentDocumentPosition = currentDiscourse.getCurrentDocumentPosition();
        currentMarkable.addRightHandlePosition(currentDocumentPosition+rightHandle-1);        
        for (int temp=0;temp<extent;temp++)
        {
            currentDiscourse.markableDisplayAssociation.put(new Integer(currentDocumentPosition+temp),currentMarkable);
        }
    }    
            
    public static void registerDiscourseElement(String id)
    {      
       currentDiscourse.registerDiscourseElement(id);
    }
        
    public static boolean isOn(String switchName)
    {
        if (currentDiscourse.getMMAX2() != null)
        {
            return currentDiscourse.getMMAX2().isOn(switchName);
        }
        else
        {
            return false;
        }
    }
    
    
    /** This method has to be called from the XSL style sheet from within the word template, AFTER the call to
        getStartedMarkables() (if any)! This order is important because markable handles are supposed to
        begin BEFORE the actual word string. The method's function is mainly to create an association 
        between the running number of the currently processed <word> element and the position in the 
        display that this element starts at. <b>Internal use only!</b>*/
    public static void setDiscourseElementStart()
    {
        try
        {
            // Get index in document string up to which the document has been transformed already.        
            // I.E. the DisplayPosition
            int start = currentDiscourse.getCurrentDocumentPosition();
            
            // Add the Display Position as the display start of the current DiscourseElement (i.e. the last one added)             
            currentDiscourse.temporaryDisplayStartPosition.add(new Integer(start));
            
            // Now, temporaryDisplayStartPosition contains at index X the discPos of the DE starting at display
            // pos X.
            if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setDefaultColor();
        }
        catch (java.lang.Exception ex)
        {
            ex.printStackTrace();
        }
        if (currentDiscourse.getHasGUI())  currentDiscourse.getDisplayDocument().flush();
   }
    
       /** This method has to be called from the XSL style sheet from within the word template, BEFORE the call to
        getEndedMarkables() (if any)! This order is important because markable handles are supposed to
        end AFTER the actual word string. The method's function is mainly to create an association 
        between the running number of the currently processed <word> element and the position in the 
        display that this element ends at. <b>Internal use only!</b>*/
    public static void setDiscourseElementEnd()
    {       
        try
        {        
            // Get index in document string up to which the document has been transformed already.
            int end = currentDiscourse.getCurrentDocumentPosition()-1;
            // Add the document string index as the display end of the current DiscourseElement (i.e. the last one added) 
            currentDiscourse.temporaryDisplayEndPosition.add(new Integer(end));           
            if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().flush();            
        }
        catch (java.lang.Exception ex)
        {
            ex.printStackTrace();
        }
   }
               
    public static void startItalic()
    {
       if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setItalic(true);
    }
    
    public static void endItalic()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setItalic(false);
    }
        
    public static void startBold()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setBold(true);
    }  
    
    public static void endBold()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setBold(false);
    }

    public static void startUnderline()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setUnderline(true);
    }    
    
    public static void endUnderline()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setUnderline(false);
    }
    
    public static void startSubscript()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setSubscript(true);
    }
    
    public static void endSubscript()
    {
        if (currentDiscourse.getHasGUI())    currentDiscourse.getDisplayDocument().setSubscript(false);
    }
    
    public static void startStrikeThrough()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setStrikeThrough(true);
    }
    
    public static void endStrikeThrough()
    {
        if (currentDiscourse.getHasGUI())    currentDiscourse.getDisplayDocument().setStrikeThrough(false);
    }

    public static void startSuperscript()
    {
        if (currentDiscourse.getHasGUI())   currentDiscourse.getDisplayDocument().setSuperscript(true);
    }
    
    public static void endSuperscript()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setSuperscript(false);
    }
        
    
    public static void startColor(String colorName)
    {      
        // If no color is explicitly associated with a level, use default foreground color black
        Color color = MMAX2Utils.getColorByName(colorName);
        if (color == null) color = Color.black;
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setColor(color, true);
    }
    
    public static void endColor(String colorName)
    {
        // If no color is explicitly associated with a level, use default foreground color black
        Color color = MMAX2Utils.getColorByName(colorName);
        if (color == null) color = Color.black;
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setColor(color, false);
    }    
    
    
    public static void startAssociatedColor(String levelname)
    {
        // If no color is explicitly associated with a level, use default foreground color black
        Color color = currentDiscourse.getCurrentMarkableChart().getForegroundColorForLevel(levelname);
        if (color == null) color = Color.black;
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setColor(color, true);
    }
    
    public static void endAssociatedColor(String levelname)
    {
        // If no color is explicitly associated with a level, use default foreground color black
        Color color = currentDiscourse.getCurrentMarkableChart().getForegroundColorForLevel(levelname);
        if (color == null) color = Color.black;
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().setColor(color, false);
    }

    public final static String getFromHash(String key)
    {
        return currentDiscourse.getFromHash(key);
    }
    
    public final static void putInHash(String key, String value)
    {
        currentDiscourse.putInHash(key, value);
    }
        

    public final static boolean inMarkableFromLevel(String DE_ID, String targetLevelName)
    {
        return currentDiscourse.getCurrentMarkableChart().inMarkableFromLevel(DE_ID, targetLevelName);
    }

    
    public final static boolean inMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        return currentDiscourse.getCurrentMarkableChart().inMarkableFromLevel(markableID, ownLevelName, targetLevelName);                
    }
    
    
    public final static boolean startsMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        return currentDiscourse.getCurrentMarkableChart().startsMarkableFromLevel(markableID, ownLevelName, targetLevelName);                
    }

    public final static boolean finishesMarkableFromLevel(String markableID, String ownLevelName, String targetLevelName)
    {
        return currentDiscourse.getCurrentMarkableChart().finishesMarkableFromLevel(markableID, ownLevelName, targetLevelName);                
    }
    
    public final String getCommonStylePath()
    {
        return commonStylePath;
    }
    
    public static void flushDocument()
    {
        if (currentDiscourse.getHasGUI()) currentDiscourse.getDisplayDocument().flush();
        if (currentDiscourse.getHasGUI()) currentDiscourse.getCurrentDocumentPosition();
    }
    
    /** This method returns a NodeSet of all Markables from all ACTIVE layers beginning at the DiscourseElement with ID 
        discourseElementId. If more than one Markable starts at a given DiscourseElement, the Markables returned are sorted 
        in the following way: Markables from a LOWER MarkableLayer (i.e. higher position) are returned before Markables from
        a HIGHER MarkableLayer (i.e. one with a lower position). Within the same MarkableLayer, longer Markables are 
        ordered before shorter ones. This way, MarkableLayers are added on top of each other. */        
    public final static NodeList getStartedMarkables(String discourseElementId)
    {
        return currentDiscourse.getCurrentMarkableChart().getActiveStartedMarkables(discourseElementId);
    }

    /** This method returns a NodeSet of all Markables from all ACTIVE layers beginning at the DiscourseElement with ID 
        discourseElementId. If more than one Markable starts at a given DiscourseElement, the Markables returned are sorted 
        in the following way: Markables from a LOWER MarkableLayer (i.e. higher position) are returned before Markables from
        a HIGHER MarkableLayer (i.e. one with a lower position). Within the same MarkableLayer, longer Markables are 
        ordered before shorter ones. This way, MarkableLayers are added on top of each other. */        
    public final static NodeList getStartedMarkables(String discourseElementId, String levels)
    {
        return currentDiscourse.getCurrentMarkableChart().getActiveStartedMarkables(discourseElementId,levels);
    }
    
    
    public final static NodeSet getEndedMarkables(String discourseElementId)
    {
        return currentDiscourse.getCurrentMarkableChart().getActiveEndedMarkables(discourseElementId);
    }   

    public final static NodeSet getEndedMarkables(String discourseElementId, String levels)
    {
        return currentDiscourse.getCurrentMarkableChart().getActiveEndedMarkables(discourseElementId,levels);
    }   
    
    
    protected void finalize()
    {        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    } 
}
