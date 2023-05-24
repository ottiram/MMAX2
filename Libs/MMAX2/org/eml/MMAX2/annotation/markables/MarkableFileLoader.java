/*
 * Copyright 2021 Mark-Christoph Müller
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

/* re-worked for version 1.15 */
package org.eml.MMAX2.annotation.markables;

// XML Parsing
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.eml.MMAX2.annotation.scheme.MMAX2AnnotationScheme;
import org.xml.sax.InputSource;


/** Helper class for loading markable XML files and converting them to MarkableLevel objects. */
public class MarkableFileLoader 
{
	boolean VERBOSE = false;
	boolean DEBUG = false;

    protected MarkableLevel currentMarkableLevel=null;
    
	public MarkableFileLoader()
	{
		try { if (System.getProperty("verbose").equalsIgnoreCase("true")) {VERBOSE = true;} }
		catch (java.lang.NullPointerException x) { }

		try { if (System.getProperty("debug").equalsIgnoreCase("true")) {DEBUG = true;} }
		catch (java.lang.NullPointerException x) { }
	}

    public boolean isVerbose()
    {
    	return VERBOSE;
    }
	
    public boolean isDebug()
    {
    	return DEBUG;
    }
    
    /** Load and parse markable file of name fileName. */
    final public void load(String fileName, String levelName, String schemeFileName, String customizationFileName, String startupMode)
    {
        if (isVerbose()) System.err.println("\n  Loading markable file "+fileName+" ... ");    	
        DOMParser parser = new DOMParser();    
        DocumentImpl markableDOM = null;
        
        try
        { parser.setFeature("http://xml.org/sax/features/validation",false); }
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
                
        // New: Check if file is there, and create if not
        File tempFile = new File(fileName);
        if (tempFile.exists()==false)
        {
            System.err.println("   Level "+levelName+" not found, creating empty template ...");
            try
            {                
                FileWriter writer = new FileWriter(tempFile);
                String header="<?xml version=\"1.0\" ?>\n<!DOCTYPE markables SYSTEM \"markables.dtd\">\n<markables xmlns=\"www.eml.org/NameSpaces/"+levelName+"\">\n</markables>\n";
                writer.write(header);
                writer.flush();
                writer.close();
            }
            catch (java.io.IOException ex) { System.err.println(ex.toString()); }            
        }

        if (isVerbose()) System.err.print("   Loading "+levelName.toUpperCase()+" markables ... ");
        try { parser.parse(new InputSource(new File(fileName).toURI().toString())); }
        catch (org.xml.sax.SAXParseException exception)
        {
            String error = "Line: "+exception.getLineNumber()+" Column: "+exception.getColumnNumber()+"\n"+exception.toString();
            System.err.println(error);
            JOptionPane.showMessageDialog(null,error,"MarkableFileLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }                
        catch (org.xml.sax.SAXException exception)
        {
            String error = exception.toString();
            System.err.println(error);
            JOptionPane.showMessageDialog(null,error,"MarkableFileLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
        catch (java.io.IOException exception)
        {
            String error = exception.toString();
            System.err.println(error);
        }
        
        markableDOM =(DocumentImpl) parser.getDocument();
        int c=markableDOM.getElementsByTagName("markable").getLength();
        if (isVerbose()) System.err.println(c+" markables have been loaded!");                                        
        
        /** Create Annotation scheme object to pass to constructor */
        if (isVerbose()) System.err.println("   Creating annotation scheme for "+levelName.toUpperCase()+" markables ...");
        MMAX2AnnotationScheme currentScheme = new MMAX2AnnotationScheme(schemeFileName);
        if (isVerbose()) System.err.println("   Done creating annotation scheme");
        
        /** Create MarkableLevel object from currently loaded Markables. */
        if (isVerbose()) System.err.println("   Creating markable level "+levelName.toUpperCase()+" ...");
        currentMarkableLevel = new MarkableLevel(markableDOM, fileName, levelName, currentScheme, customizationFileName);
        if (isVerbose()) System.err.println("   Done creating markable level ...");
        
        if (startupMode.equalsIgnoreCase("visible")) 		{ currentMarkableLevel.setVisible(); }
        else if (startupMode.equalsIgnoreCase("inactive")) 	{ currentMarkableLevel.setInactive(); }
    }
    
    /** Get currently loaded MarkableLevel from this MarkableFileLoader. */
    final public MarkableLevel getMarkableLevel()
    {        
        return currentMarkableLevel;
    }  
}
