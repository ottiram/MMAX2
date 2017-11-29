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

// XSL transformation
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.DocumentImpl;
import org.eml.MMAX2.annotation.markables.AlphabeticMarkableComparator;
import org.eml.MMAX2.annotation.markables.DiscourseOrderMarkableComparator;
import org.eml.MMAX2.annotation.markables.EndingMarkableComparator;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.annotation.markables.MarkableIDComparator;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkableLevelPositionComparator;
import org.eml.MMAX2.annotation.markables.StartingMarkableComparator;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.DiscourseAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MMAX2Discourse implements DiscourseAPI
{   
    private String nameSpace = null;
    
    private HashMap hash = null;   
    
    private ArrayList recentTextEntries = new ArrayList();
    private ArrayList recentAttributeEntries = new ArrayList();
    
    private String commonBasedataPath="";
    
    protected DocumentImpl wordDOM = null;
    protected String wordFileName="";
    
    protected String[] styleSheetFileNames;
    protected String currentStyleSheet;

    /** Contains at position X the ID of the Discourse Element with Discourse position X */
    protected String[] discourseElementAtPosition = null;        
    protected ArrayList temporaryDiscourseElementAtPosition=null;

    /** Maps IDs of DiscourseElements to their numerical Discourse positions. Used by getDiscoursePositionFromDiscourseElementId(String id). */
    protected HashMap discoursePositionOfDiscourseElement=null;    
    
    /** Contains at position X the display start position (i.e. character position in display string) of the DE with Discourse position X. */   
    protected Integer[] displayStartPosition = null;    
    protected ArrayList temporaryDisplayStartPosition=null;
    
    /** Contains at position X the display start position (i.e. character position in display string) of the DE with Discourse position X. */    
    protected Integer[] displayEndPosition = null;
    protected ArrayList temporaryDisplayEndPosition=null;        
    
    protected StringWriter incrementalTransformationResult = null; 
    
    /** Position up to which the incrementalTransformationResult has already been processed, incremented by method getNextDocumentChunk(). */
    protected int lastStart = 0;
    
    protected MarkableChart chart;
    
    protected HashMap markableDisplayAssociation=null;
    protected HashMap hotSpotDisplayAssociation=null;
    
    public static StartingMarkableComparator STARTCOMP=null;
    public static EndingMarkableComparator ENDCOMP=null;
    public static AlphabeticMarkableComparator ALPHACOMP=null;
    public static DiscourseOrderMarkableComparator DISCOURSEORDERCOMP=null;
    public static MarkableLevelPositionComparator LEVELCOMP=null;
    public static MarkableIDComparator IDCOMP=null;
    
    protected MMAX2 mmax2 = null;
        
    protected boolean hasGUI = false;            
    
    /** Creates new Discourse */
    public MMAX2Discourse(boolean withGUI) 
    {    	
        hasGUI = withGUI;
        discoursePositionOfDiscourseElement = new HashMap();
        chart = new MarkableChart(this);
        temporaryDisplayStartPosition = new ArrayList();
        temporaryDisplayEndPosition = new ArrayList();
        temporaryDiscourseElementAtPosition = new ArrayList();
        markableDisplayAssociation = new HashMap();
        hotSpotDisplayAssociation = new HashMap();
        
        STARTCOMP = new StartingMarkableComparator();
        ENDCOMP = new EndingMarkableComparator();
        ALPHACOMP = new AlphabeticMarkableComparator();
        DISCOURSEORDERCOMP = new DiscourseOrderMarkableComparator();
        LEVELCOMP = new MarkableLevelPositionComparator();
        IDCOMP = new MarkableIDComparator();        
    }


    public MMAX2Discourse _buildDiscourse(String infile,String commonPathsFile)
    {
    	System.err.println("Warning: This is a dummy implementation only. Use the *static* method\n\tMMAX2Discourse buildDiscourse(String infile,String commonPathsFile)!");
    	return null;
    }
    
    public static MMAX2Discourse buildDiscourse(String infile,String commonPathsFile)
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}
    	
        long start = System.currentTimeMillis();
        if (verbose) System.err.print("   loading ... ");
        MMAX2DiscourseLoader loader = new MMAX2DiscourseLoader(infile, false,commonPathsFile);
        if (verbose)  System.err.println("("+(System.currentTimeMillis()-start)+")");
        MMAX2Discourse currentDiscourse = loader.getCurrentDiscourse();        
        
        start = System.currentTimeMillis();
        if (verbose)  System.err.print("   style sheet ... ");
        currentDiscourse.applyStyleSheet(loader.getCommonStylePath()+"generic_nongui_style.xsl");
        if (verbose)  System.err.println("("+(System.currentTimeMillis()-start)+")");
        start = System.currentTimeMillis();
        if (verbose)  System.err.print("   initializing ... ");
        currentDiscourse.performNonGUIInitializations();
        if (verbose)  System.err.println("("+(System.currentTimeMillis()-start)+")");
        return currentDiscourse;
    }
    
    public static MMAX2Discourse buildDiscourse(String infile)
    {
    	return buildDiscourse(infile,"");
    }
    
    /*
    public static MMAX2Discourse buildDiscourse(String infile)
    {
        long start = System.currentTimeMillis();
        System.err.print("   loading ... ");
        MMAX2DiscourseLoader loader = new MMAX2DiscourseLoader(infile, false,"");
        System.err.println("("+(System.currentTimeMillis()-start)+")");
        MMAX2Discourse currentDiscourse = loader.getCurrentDiscourse();                
        
        start = System.currentTimeMillis();
        System.err.print("   style sheet ... ");
        currentDiscourse.applyStyleSheet(loader.getCommonStylePath()+"generic_nongui_style.xsl");
        System.err.println("("+(System.currentTimeMillis()-start)+")");
        start = System.currentTimeMillis();
        System.err.print("   initializing ... ");
        currentDiscourse.performNonGUIInitializations();
        System.err.println("("+(System.currentTimeMillis()-start)+")");
        return currentDiscourse;
    }
    */
    public final void setNameSpace(String _nameSpace)
    {
        nameSpace = _nameSpace;
    }
        
    public final String getNameSpace()
    {
        return nameSpace;
    }
    
    public final void setWordFileName (String name)
    {
        wordFileName = name;
    }
    public final String getWordFileName()
    {
        return wordFileName;
    }        
    
    public final void addWithID(String id, Element node)
    {
        wordDOM.putIdentifier(id, node);
    }
        
    public final void destroyDependentComponents()
    {
        discoursePositionOfDiscourseElement.clear();
        discoursePositionOfDiscourseElement = null;
        discoursePositionOfDiscourseElement = new HashMap();
        chart = null;
        temporaryDisplayStartPosition = null;
        temporaryDisplayEndPosition = null;
        temporaryDiscourseElementAtPosition = null;
        markableDisplayAssociation.clear();        
        markableDisplayAssociation = null;

        hotSpotDisplayAssociation.clear();        
        hotSpotDisplayAssociation = null;
        
        STARTCOMP =  null;
        ENDCOMP = null;
        DISCOURSEORDERCOMP = null;
        LEVELCOMP = null;        
        wordDOM = null;
        hash = null;
        System.gc();
    }
    
    public final boolean getHasGUI()
    {
        return hasGUI;
    }
    
    protected void finalize()
    {
        System.err.println("MMAX2Discourse is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }
    
        
    public final MMAX2Document getDisplayDocument()
    {
        return mmax2.getCurrentDocument();
    }
   
    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;             
    }
    
    public final MMAX2 getMMAX2()
    {
        return mmax2;
    }
    
    public final Integer[] getAllDisplayAssociations()
    {
        if (markableDisplayAssociation.size()!=0)
        {
            return (Integer[]) (((Set)markableDisplayAssociation.keySet()).toArray(new Integer[1]));
        }
        else
        {
            return null;
        }
    }
    
    /*
    public ArrayList<String> getAllUIMATypeMappings()
    {
    	HashSet tempResult = new HashSet();
    	// Iterate over all levels
    	MarkableLevel[] levels = chart.getLevels();
    	for (int z=0;z<levels.length;z++)
    	{
    		tempResult.addAll(levels[z].getAllUIMATypeMappings());
    	}    	
    	
    	ArrayList result = new ArrayList();
    	Iterator iti = tempResult.iterator();
    	while (iti.hasNext())
    	{
    		result.add((String)iti.next());
    	}
    	return result;
    }
*/
    
    public final Markable getMarkableAtDisplayAssociation(int displayPosition)
    {
        // No active/inactive distinction necessary, because only active Markables will have MarkableHandles anyway
        // WRONG: Handles of deactivated layers will stay around until next re-application !!
        Markable result = (Markable) markableDisplayAssociation.get(new Integer(displayPosition));
        return result;
    }
     
    public final String getHotSpotAtDisplayAssociation(int displayPosition)
    {
        String result = (String) hotSpotDisplayAssociation.get(new Integer(displayPosition));
        return result;        
    }
    
    public final Integer[] removeDisplayAssociationsForMarkable(Markable removee)
    {
        Set all = markableDisplayAssociation.entrySet();
        Iterator it = all.iterator();
        ArrayList positions = new ArrayList();
        Integer currentPos = null;
        boolean added = false;
        while (it.hasNext())
        {
            Entry current = (Entry) it.next();
            if (current.getValue().equals(removee))
            {
                it.remove();
                // Get current position
                currentPos = (Integer) current.getKey();
                if (positions.size()==0)
                {
                    // The list is still empty, so simply add 
                    positions.add(currentPos);
                }
                else
                {
                    // Find insertion point
                    for (int p=0;p<positions.size();p++)
                    {
                        if ((((Integer)positions.get(p)).intValue())>currentPos.intValue())
                        {
                            positions.add(p,currentPos);
                            added=true;
                            break;
                        }
                    }
                }
                if(!added) positions.add(currentPos);
            }
        }
        return (Integer[]) positions.toArray(new Integer[0]);
    }
    
    public final DocumentImpl getWordDOM()
    {
        return wordDOM;
    }
    
    public final int getDisplayStartPositionFromDiscoursePosition(int discoursePosition)
    {
        int result = -1;
        try
        {
            result = displayStartPosition[discoursePosition].intValue();
        }        
        catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {
            ex.printStackTrace();
        }
        return result;
    }
    
    public final int getDisplayEndPositionFromDiscoursePosition(int discoursePosition)
    {        
        int result = -1;
        try
        {
            result = displayEndPosition[discoursePosition].intValue();
        }  
        catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {
            ex.printStackTrace();
        }        
        return result;        
    }
        
    public final int getDiscoursePositionAtDisplayPosition(int _displayPosition)
    {
        int DiscPos = -1;
        int startPos = 0;
        int endPos = 0;
        Integer displayPosition = new Integer(_displayPosition);
        // Try if pos is the exact beginning of a Discourse Element
        startPos = Arrays.binarySearch(displayStartPosition,displayPosition);
        if (startPos >= 0 && startPos < displayStartPosition.length)
        {
        	try
        	{
        		if (displayStartPosition[startPos].equals(displayPosition))
        		{
        			// The user clicked the first character
        			return startPos;
        		}
        	}
        	catch (java.lang.ArrayIndexOutOfBoundsException ex)
        	{
        		ex.printStackTrace();
        	}
        }
                
        // Try if pos is the exact end of a Discourse Element        
        endPos = Arrays.binarySearch(displayEndPosition,displayPosition);
        if (endPos >=0 && endPos < displayEndPosition.length)
        {
        	try
        	{
        		if (displayEndPosition[endPos].equals(displayPosition))
        		{
        			// The user clicked the first character
        			return endPos;
        		}
        	}
        	catch (java.lang.ArrayIndexOutOfBoundsException ex)
        	{
        		ex.printStackTrace();
        	}
        }
        // When we are here, the click occurred either in the middle of a Discourse Element, 
        // or in empty space
        if (startPos == endPos)
        {
            // The click occurred in empty space
            return DiscPos;
        }
        else
        {
            return (endPos * (-1))-1;
        }            
    }
    
    public final MMAX2DiscourseElement getDiscourseElementByID(String id)
    {
        MMAX2DiscourseElement result = null;
        Node temp = getDiscourseElementNode(id);
        if (temp != null)
        {
            result = new MMAX2DiscourseElement(temp.getFirstChild().getNodeValue(), temp.getAttributes().getNamedItem("id").getNodeValue(),this.getDiscoursePositionFromDiscourseElementID(temp.getAttributes().getNamedItem("id").getNodeValue()),MMAX2Utils.convertNodeMapToHashMap(temp.getAttributes()));        
        }
        return result;
        
    }
    
    
    /** This method receives the String id of a DiscourseElement (word_x) and returns the (0-based)
        discourse position, i.e. the running number of DiscourseElements that are registered in the current
        display. The discourse position is determined using the Hash DiscoursePositionOfDiscourseElement
        which has been filled by calls to this.registerDiscourseElement(String id) during style sheet
        execution. (DiscoursePositionOfDiscourseElement won't be filled until after stylesheet execution!!) 
        This will be filled incrementally during style sheet execution, and is thus available during that. */
    public final int getDiscoursePositionFromDiscourseElementID(String id)
    {               
        int result = -1;
        try
        {
            return ((Integer)discoursePositionOfDiscourseElement.get(id)).intValue();
        }
        catch (java.lang.NullPointerException ex)
        {
            System.err.println("No disc pos for "+id);
            // This should not happen any more, since now even supressed des have a discourse position
            ex.printStackTrace();
        }
        return result;
    }
    
    public final String getDiscourseElementIDAtDiscoursePosition(int pos)
    {
        String result = "";
        if (pos != -1)
        {
            try
            {
                result = discourseElementAtPosition[pos];
            }
            catch (java.lang.ArrayIndexOutOfBoundsException ex)
            {
                //ex.printStackTrace( );
            }
        }
        return result;
    }
            
     public final void registerAllDiscourseElements()
     {
         NodeList allWords = wordDOM.getElementsByTagName("word");
         for (int z=0;z<allWords.getLength();z++)
         {
             registerDiscourseElement(allWords.item(z).getAttributes().getNamedItem("id").getNodeValue());
         }
     }
     
    /** This method receives a DiscourseElement id (word_x), assigns it a discourse position (0-based) and
        stores both values in the Hash DiscoursePositionOfDiscourseElement. This Hash is later used by 
        this.getDiscoursePositionFromDiscourseElementID(String id). This method also adds word_x to the 
        list this.temporaryDiscourseElementAtPosition. This list's size is used as the discourse position
        determiner for the _NEXT_ element to be added. This method must be called for every de, incl. 
        those that are suppressed from the display! <b>Internal use only!</b>*/
     public final void registerDiscourseElement(String id)
     {    	          
         if (discoursePositionOfDiscourseElement == null)
         {
             discoursePositionOfDiscourseElement = new HashMap();
         }          

         if (temporaryDiscourseElementAtPosition == null)
         {
             temporaryDiscourseElementAtPosition = new ArrayList();
         }
        // Map id to discourse position
        discoursePositionOfDiscourseElement.put(id,new Integer(temporaryDiscourseElementAtPosition.size()));
        
        temporaryDiscourseElementAtPosition.add(id);                
     }
                 
    public final MMAX2DiscourseElement[] getDiscourseElements()
    {
        ArrayList result = new ArrayList();
        int pos = 0;
        MMAX2DiscourseElement currentElement = null;
        while(true)
        {
            currentElement = getDiscourseElementAtDiscoursePosition(pos);
            if (currentElement !=null)
            {
                result.add(currentElement);
                currentElement = null;
                pos++;
                continue;
            }
            else
            {
                break;
            }
        }
        return (MMAX2DiscourseElement[]) result.toArray(new MMAX2DiscourseElement[0]);
    }
    
    public final MMAX2DiscourseElement[] getDiscourseElements(Markable _markable)
    {
        ArrayList tempList = new ArrayList();
        String[] markablesDEIDs = _markable.getDiscourseElementIDs();
        for (int z=0;z<markablesDEIDs.length;z++)
        {
            Node temp = getDiscourseElementNode(markablesDEIDs[z]);
            tempList.add(new MMAX2DiscourseElement(temp.getFirstChild().getNodeValue(), temp.getAttributes().getNamedItem("id").getNodeValue(),this.getDiscoursePositionFromDiscourseElementID(temp.getAttributes().getNamedItem("id").getNodeValue()),MMAX2Utils.convertNodeMapToHashMap(temp.getAttributes())));
        }        
        return (MMAX2DiscourseElement[]) tempList.toArray(new MMAX2DiscourseElement[0]);
    }
    
    public final MMAX2DiscourseElement getDiscourseElementAtDiscoursePosition(int discPos)
    {
        MMAX2DiscourseElement result = null;
        String id = getDiscourseElementIDAtDiscoursePosition(discPos);
        Node temp = getDiscourseElementNode(id);
        if (temp != null)
        {
            result = new MMAX2DiscourseElement(temp.getFirstChild().getNodeValue(), temp.getAttributes().getNamedItem("id").getNodeValue(),this.getDiscoursePositionFromDiscourseElementID(temp.getAttributes().getNamedItem("id").getNodeValue()),MMAX2Utils.convertNodeMapToHashMap(temp.getAttributes()));        
        }
        return result;
    }
    
    /*
    public final ArrayList getAllMatchingDiscourseElementSequences(MMAX2DiscourseElementSequence entireInputSequence, String regExp, String toMatch)
    {
        // Todo: This is not very efficient
        ArrayList tempResult = new ArrayList();
        // Create pattern to match only once
        Pattern pattern = Pattern.compile(regExp);
        // Iterate over entireINputSequence, one de at a time
        for (int leftBorder=0;leftBorder<entireInputSequence.getLength();leftBorder++)
        {
            // Create subsequence starting at pos leftBorder
            // 0 at the beginning, moving right with each iteration
            for (int len=entireInputSequence.getLength()-leftBorder;len>0;len--)
            {
                MMAX2DiscourseElementSequence inputSequence = entireInputSequence.getSubSequence(leftBorder,len);
                    
                // Get array version of current subsequence
                MMAX2DiscourseElement[] input = inputSequence.getContent();
                String temp = "";
                // Create list to accept mapping from string positions to DEs at these positions
                ArrayList stringPosToDiscourseElement = new ArrayList();
        
                // Iterate over all DiscourseElements in current subsequence
                for (int z=0;z<input.length;z++)
                {
                    // Get current DE
                    MMAX2DiscourseElement currentElement = input[z];
                        
                    if (toMatch.equalsIgnoreCase(""))
                    {
                        // toMatch is empty, so the de *text* is to be matched                
                        for (int p=0;p<=currentElement.toString().length();p++)
                        {
                            // put currentElement reference at each pos in temp string, plus trailing space
                            stringPosToDiscourseElement.add(currentElement);
                        }
                        // Create temp String to match
                        if (currentElement.getAttributeValue("ignore", "false").equals("true"))
                        {
                            String temp2 = currentElement.toString();
                            for (int i=0;i<=temp2.length();i++)
                            {
                                temp = temp + " ";
                            }                    
                        }
                        else
                        {
                            temp = temp +currentElement.toString()+" ";
                        }
                    }
                    else
                    {
                        // Some attribute value is to be used
                        // Get val of attribute to be used (e.g. uh)
                        String val = currentElement.getAttributeValue(toMatch, "+++");
                        // Iterate over length of value to be added to string
                        for (int p=0;p<val.length();p++)
                        {
                            // put currentElement reference at each pos in temp string
                            stringPosToDiscourseElement.add(currentElement);
                            // For a 3-character string, this fills pos's 0, 1 and 2
                        }
                
                        // Create temp String to match                
                        if (currentElement.getAttributeValue("ignore", "false").equals("true"))
                        {
                            // Iterate over length of value to be added to string + 1
                            for (int i=0;i<=val.length();i++)
                            {
                                temp = temp + " ";
                            }                    
                        }
                        else
                        {
                            temp = temp + val+" ";
                        }                                                
                        stringPosToDiscourseElement.add(null);
                    }
                }

                Matcher m = pattern.matcher(temp);                       
                ArrayList currentMatchedSequence = new ArrayList();
                if (m.matches())            
                {
                    // Some strict match has been found
                    // Retrieve start and end of it
                    int start = m.start();
                    int end = m.end();

                    // Iterate over match span
                    for (int q=start;q<end;q++)
                    {
                        // If this loop is entered, a match was found
                        if (stringPosToDiscourseElement.get(q)==null)
                        {
                            continue;
                        }
                        if (currentMatchedSequence.contains((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q))==false)
                        {
                            //tempResult.add((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q));
                            currentMatchedSequence.add((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q));
                        }
                    }
                    // Now, the current match (if any) has been collected in currentMatchedSequence
                    if (currentMatchedSequence.size() > 0)
                    {
                        MMAX2DiscourseElementSequence newSequence = new MMAX2DiscourseElementSequence((MMAX2DiscourseElement[])currentMatchedSequence.toArray(new MMAX2DiscourseElement[0]));
                        tempResult.add(newSequence);
                    }        
                }
                if (currentMatchedSequence.size()>0)
                {
                    leftBorder = leftBorder + currentMatchedSequence.size()-1;
                    break;
                }            
                currentMatchedSequence = null;   
            }
        }
        return tempResult;
        
    }
*/

    public final MMAX2DiscourseElement getNextDiscourseElement(MMAX2DiscourseElement element)    
    {
        MMAX2DiscourseElement currentElement = null;
        int currentDiscPos = 0;
        if (element != null)
        {
            currentDiscPos = element.getDiscoursePosition()+1;
        }
        currentElement = getDiscourseElementAtDiscoursePosition(currentDiscPos);
        return currentElement;
    }
    
    public final MMAX2DiscourseElement getPreviousDiscourseElement(MMAX2DiscourseElement element)    
    {
        MMAX2DiscourseElement currentElement = null;
        int currentDiscPos = getDiscourseElementCount();
        if (element != null)
        {
            currentDiscPos = element.getDiscoursePosition()-1;
        }
        currentElement = getDiscourseElementAtDiscoursePosition(currentDiscPos);
        return currentElement;
    }
    
    /*
    public final MMAX2DiscourseElementSequence getPreceedingDiscourseElements(int currentDiscPos, int len)    
    {
        // The element at currentDiscPos is NOT itself retrieved!
        ArrayList tempresult = new ArrayList();
        MMAX2DiscourseElement currentElement = null;
        if (currentDiscPos > 0)
        {
            while(true)
            {
                // Move one position left
                currentDiscPos--;                
                currentElement = getDiscourseElementAtDiscoursePosition(currentDiscPos);
                // currentElement will be null if no element could be retrieved from pos currentDiscPos
                if (currentElement==null)
                {
                    // No more elements could be retrieved
                    break;
                }
                if (currentElement.getAttributeValue("ignore","+++").equalsIgnoreCase("true")==false)
                {
                    // Add current element to *beginning of* tempresult if it is not ignorable (we move backwards here!)
                    tempresult.add(0,currentElement);
                    currentElement=null;
                    if (tempresult.size()==len)
                    {
                        // len elements have been retrieved
                        break;
                    }
                    continue;
                }
            }
        }
        if (tempresult.size()!=len)
        {
            System.err.println("Warning: Could not retrieve required "+len+" elements ("+tempresult.size()+" only) !");
        }
        return new MMAX2DiscourseElementSequence((MMAX2DiscourseElement[])tempresult.toArray(new MMAX2DiscourseElement[0]));
    }
    */
    
/*    
    public final MMAX2DiscourseElement[] getMatchingDiscourseElementSequence(MMAX2DiscourseElementSequence inputSequence, String regExp, String startAfterDE, String toMatch)
    {
        MMAX2DiscourseElement[] input = inputSequence.getContent();
        boolean started = false;
        String temp = "";
        ArrayList stringPosToDiscourseElement = new ArrayList();
        
        // Iterate over all input DiscourseElements
        for (int z=0;z<input.length;z++)
        {
            // Get current DE
            MMAX2DiscourseElement currentElement = input[z];
            if (startAfterDE.equals("")==false)
            {
                // Only if some start offset was given at all
                if (!started && currentElement.getID().equalsIgnoreCase(startAfterDE)==false)
                {
                    // Move to next and keep ignoring
                    continue;
                }
                else
                {
                    // Ignore this one, but none afterwards
                    if (!started)
                    {
                        started = true;
                        continue;
                    }
                }                
            }
            
            if (toMatch.equalsIgnoreCase(""))
            {
                // The de text is to be matched                
                for (int p=0;p<=currentElement.toString().length();p++)
                {
                    // put currentElement reference at each pos in temp string, plus trailing space
                    stringPosToDiscourseElement.add(currentElement);
                }
                // Create temp String to match
                temp = temp + input[z].toString()+" ";
            }
            else
            {
                // Some attribute value is to be used
                // Get val of attribute to be used
                String val = input[z].getAttributeValue(toMatch, "+++");
                for (int p=0;p<val.length();p++)
                {
                    // put currentElement reference at each pos in temp string
                    stringPosToDiscourseElement.add(currentElement);
                }
                // Create temp String to match
                temp = temp + val+" ";
                stringPosToDiscourseElement.add(null);
            }            
        }
        temp = temp.trim();
//        System.out.println(temp);
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(temp);
            
        ArrayList tempResult = new ArrayList();
            
        //while(m.find())
        if(m.find())
        {
//            System.out.println("Match");
            int start = m.start();
//            System.out.println(start);
            int end = m.end();
//            System.out.println(end);
            for (int q=start;q<end;q++)
            {
                if (stringPosToDiscourseElement.get(q)==null)
                {
                    continue;
                }
//                System.out.println(((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q)).getString());
                if (tempResult.contains((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q))==false)
                {
                    tempResult.add((MMAX2DiscourseElement)stringPosToDiscourseElement.get(q));
                }
            }
        }                                
        return (MMAX2DiscourseElement[])tempResult.toArray(new MMAX2DiscourseElement[0]);            
    }
  
*/    
    
    /** Returns the Node representation of the discourse element with id ID.*/
    public final Node getDiscourseElementNode(String ID)
    {
        Node result = null;         
        try        
        {            
            result = wordDOM.getElementById(ID);
        }
        catch (java.lang.NullPointerException ex)
        {
        	ex.printStackTrace();
        }
        return result;        
    }
    
    protected final void setWordDOM(DocumentImpl dom)
    {
        wordDOM = dom;
    }
    
    public final void resetForStyleSheetReapplication()
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

    	
        if (verbose) System.err.print("Resetting ... ");
        long time = System.currentTimeMillis();
        
        temporaryDiscourseElementAtPosition = new ArrayList();
        temporaryDisplayEndPosition = new ArrayList();
        temporaryDisplayStartPosition = new ArrayList();
        lastStart = 0;
        markableDisplayAssociation.clear();
        markableDisplayAssociation = new HashMap();
        hotSpotDisplayAssociation.clear();
        hotSpotDisplayAssociation = new HashMap();
        
        hash = null;
        
        chart.resetMarkablesForStyleSheetReapplication();
        chart.resetHasHandles();
        System.gc();        
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-time)+" milliseconds");

    }
    
    /** This method is called when the deep refresh button on the MarkableLevelControlPanel is pressed. */
    public final void reapplyStyleSheet()
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}
    	
        /* Reset currentDocument to new (empty) one .*/
        mmax2.setCurrentDocument(new MMAX2Document(mmax2.currentDisplayFontName,mmax2.currentDisplayFontSize));
        /* Set currentDocument's mmax2 reference. */
        mmax2.getCurrentDocument().setMMAX2(mmax2);
       
        resetForStyleSheetReapplication();
        
        if (verbose) System.err.print("Reapplying stylesheet "+currentStyleSheet+" ... ");
        long time = System.currentTimeMillis();
        applyStyleSheet("");
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-time)+" milliseconds");     
        
        if (verbose) System.err.print("Recreating Markable mappings ... ");
        time = System.currentTimeMillis();        
        // Call to create e.g. DiscoursePositionToMarkableMappings
        chart.createDiscoursePositionToMarkableMappings();
        chart.setMarkableLevelDisplayPositions();
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-time)+" milliseconds");
        chart.updateLabels();
        mmax2.getCurrentTextPane().setStyledDocument((DefaultStyledDocument) mmax2.getCurrentDocument());
        chart.initMarkableRelations(); 
        mmax2.requestRefreshDisplay();
        if (mmax2.getCurrentPrimaryMarkable()!= null)
        {
            chart.markableLeftClicked(mmax2.getCurrentPrimaryMarkable());
        }
    }
    
    /** Apply the XSL style sheet in this.styleSheetFileName to this.structureDOM. */ 
    public final void applyStyleSheet(String overrideStyleFileName)
    {        
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

    	
        if (overrideStyleFileName.equals(""))
        {
            overrideStyleFileName = currentStyleSheet;
        }
        
        /* Create string writer to accept XSL processor output */
        incrementalTransformationResult = new StringWriter();

        /* Create XSL processor */
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        
        try
        {            
            //transformer = tFactory.newTransformer(new StreamSource("FILE:"+overrideStyleFileName)); // This does not throw any errors!            
        	transformer = tFactory.newTransformer(new StreamSource(new File(overrideStyleFileName).toURI().toString())); // This does not throw any errors!
        }
        catch (javax.xml.transform.TransformerConfigurationException ex )
        {          
            String error = ex.toString();
            System.err.println(error);
            JOptionPane.showMessageDialog(null,error,"Discourse: "+overrideStyleFileName,JOptionPane.ERROR_MESSAGE);
        }
        catch (java.lang.Exception ex)
        {
        	ex.printStackTrace();
        }
                             
        try
        {
        	transformer.transform(new DOMSource(wordDOM ), new StreamResult(incrementalTransformationResult));
        }
        catch (javax.xml.transform.TransformerException ex)
        {
        	String error = ex.toString();
        	System.err.println(error);
        	System.err.println(ex.getMessageAndLocation());
        	JOptionPane.showMessageDialog(null,error,"Discourse: "+overrideStyleFileName,JOptionPane.ERROR_MESSAGE);
        	ex.printStackTrace();
        }
        
        discourseElementAtPosition = (String[]) temporaryDiscourseElementAtPosition.toArray(new String[1]);
        displayStartPosition = (Integer[]) temporaryDisplayStartPosition.toArray(new Integer[1]);
        displayEndPosition = (Integer[]) temporaryDisplayEndPosition.toArray(new Integer[1]);
        
        temporaryDiscourseElementAtPosition.clear();
        temporaryDisplayEndPosition.clear();
        temporaryDisplayStartPosition.clear();
        try
        {
            incrementalTransformationResult.close();
        }
        catch (java.io.IOException ex)
        {
            
        }
        
        System.gc();            
    }
    
    public final void setCurrentStyleSheet(String name)
    {
        currentStyleSheet = name;
        try
        {
            mmax2.setReapplyBarToolTip(name);
        }
        catch (java.lang.NullPointerException ex)
        {
            //
        }
    }
    
    public final String getCurrentStyleSheet()
    {
        return currentStyleSheet;
    }
    
    public final void setStyleSheetFileNames(String[] names)
    {
        styleSheetFileNames = names;
    }

    public final String[] getStyleSheetFileNames()
    {
        return styleSheetFileNames;
    }
    
       
    /** This method returns the current length of this.incrementalTransformationResult. It is used to associate Discourse Elements
        with display string positions during stylesheet execution. */
    public final int getCurrentDocumentPosition()
    {
        incrementalTransformationResult.flush();
        return incrementalTransformationResult.getBuffer().length();
    }   
    
    /** This method returns the next chunk of the incremental transformation result that has not yet been processed. */
    public final String getNextDocumentChunk()
    {        
        incrementalTransformationResult.flush();
        String temp = incrementalTransformationResult.toString();                
        String result =  temp.substring(lastStart);
        lastStart = temp.length();
        return result;                
    }
    
    public final MarkableLevel getMarkableLevelFromAbsoluteFileName(String absFileName)
    {
        MarkableLevel result = null;
        MarkableLevel[] levels = getCurrentMarkableChart().getActiveLevels();
        for (int z=0;z<levels.length;z++)
        {
            if(levels[z].getAbsoluteMarkableFileName().equals(absFileName))
            {
                result = levels[z];
                break;
            }
        }
        return result;
    }
    
    public final boolean isCurrentlyLoaded(String absoluteMarkableFileName)
    {
        boolean found = false;
        MarkableLevel[] levels = getCurrentMarkableChart().getActiveLevels();
        for (int z=0;z<levels.length;z++)
        {
            if(levels[z].getAbsoluteMarkableFileName().equals(absoluteMarkableFileName))
            {
                found = true;
                break;
            }
        }
        return found;
    }
    
    public final String getStyleSheetOutput()
    {
        incrementalTransformationResult.flush();
        return incrementalTransformationResult.toString();
    }
    
    public final void putInHash(String key, String value)
    {
        if (hash==null) hash = new HashMap();
        hash.put(key,value);
    }
    
    public final String getFromHash(String key)
    {
        String value="";
        if (hash != null)
        {
            value = (String) hash.get(key);            
            if (value == null)
            {
                value="";
            }
        }       
        return value;
    }
    
    
    public final MarkableLevel getMarkableLevelByName(String name, boolean interactive)
    {
        return getCurrentMarkableChart().getMarkableLevelByName(name,interactive);
    }
    
    public final MarkableChart getCurrentMarkableChart()
    {
        return chart;
    }
    
    public final String[] getAllDiscourseElementIDs()
    {
        return discourseElementAtPosition;
    }

    public final int getDiscourseElementCount()
    {
        return discourseElementAtPosition.length;
    }
    
    public final void performNonGUIInitializations()
    {                
        getCurrentMarkableChart().createDiscoursePositionToMarkableMappings();
        getCurrentMarkableChart().setMarkableLevelDisplayPositions();
        getCurrentMarkableChart().initMarkableRelations();        
        getCurrentMarkableChart().updateLabels();                        
    }
       
    public final void setCommonBasedataPath(String path)
    {
        commonBasedataPath = path;
    }
    
    public final String getCommonBasedataPath()
    {
        return commonBasedataPath;
    }
    
    public final void requestDeleteBasedataElement(Node deletee)
    {
        // Get string id of base data element to be removed
        String deleteesID = deletee.getAttributes().getNamedItem("id").getNodeValue();
        // Get list of markables started by it
        ArrayList startedMarkables = getCurrentMarkableChart().getAllStartedMarkables(deleteesID);
        // Get list of markables ended by it
        ArrayList endedMarkables = getCurrentMarkableChart().getAllEndedMarkables(deleteesID);
        
        ArrayList entireMarkables = new ArrayList();
        
        // Determine if there is a markable that consists of the deletee only
        // Iterate over all started markables backwards
        for (int b=startedMarkables.size()-1;b>=0;b--)
        {
            if (endedMarkables.contains((Markable)startedMarkables.get(b)))
            {
                // The current started markable is also finished by the same de
                entireMarkables.add((Markable)startedMarkables.get(b));
                // Remove
                endedMarkables.remove((Markable)startedMarkables.get(b));
                startedMarkables.remove(b);
            }
        }
        
        if (startedMarkables.size() > 0 || endedMarkables.size()>0 || entireMarkables.size()>0)
        {
            // The deletee is the left or right border of or identical to at least one markable
            String message = "The base data element to be deleted is contained in the following markable(s):\n";
            if (entireMarkables.size()>0)
            {
                message = message + "Completely:\n";
            }
            for (int z=0;z<entireMarkables.size();z++)
            {
                    message = message + ((Markable)entireMarkables.get(z)).toString()+"\n";
            }
            
            if (startedMarkables.size()>0)
            {
                message = message + "As first element:\n";
            }
            for (int z=0;z<startedMarkables.size();z++)
            {
                    message = message + ((Markable)startedMarkables.get(z)).toString()+"\n";
            }
            
            if (endedMarkables.size()>0)
            {
                message = message + "As last element:\n";
            }
            for (int z=0;z<endedMarkables.size();z++)
            {
                    message = message + ((Markable)endedMarkables.get(z)).toString()+"\n";
            }
            message = message + "\nPress 'OK' to delete anyway and adapt/delete these markables, or 'Cancel' to cancel deletion!";
            // Show dialogue
            int choice = JOptionPane.showConfirmDialog(this.getMMAX2(),message,"Confirm base data deletion",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);           
            // Do sth. only if user pressed OK
            if (choice != JOptionPane.OK_OPTION)
            {
                return;
            }  
            else
            {
                // The user opted to remove the deletee nonetheless
                // So update markables containing it
                for (int z=0;z<entireMarkables.size();z++)
                {        
                    ((Markable)entireMarkables.get(z)).getMarkableLevel().setIsDirty(true,false);
                    getCurrentMarkableChart().deleteMarkable(((Markable)entireMarkables.get(z)));
                }
                String[] deleteeArray = new String[1];
                deleteeArray[0] = deleteesID;
                for (int z=0;z<startedMarkables.size();z++)
                {
                    ((Markable)startedMarkables.get(z)).getMarkableLevel().setIsDirty(true,false);
                    ((Markable)startedMarkables.get(z)).getMarkableLevel().unregisterMarkable(((Markable)startedMarkables.get(z)));        
                    ((Markable)startedMarkables.get(z)).removeDiscourseElements(deleteeArray);
                }
                for (int z=0;z<endedMarkables.size();z++)
                {
                    ((Markable)endedMarkables.get(z)).getMarkableLevel().setIsDirty(true,false);
                    ((Markable)endedMarkables.get(z)).getMarkableLevel().unregisterMarkable(((Markable)endedMarkables.get(z)));        
                    ((Markable)endedMarkables.get(z)).removeDiscourseElements(deleteeArray);
                }                                        
            }
        }// No intervening markables were found
        
        // Remove node to be deleted
        deletee.getParentNode().removeChild(deletee);        
        mmax2.getCurrentTextPane().setControlIsPressed(false);   
        getCurrentMarkableChart().updateAllMarkableLevels();
        mmax2.requestReapplyDisplay();
        getCurrentMarkableChart().createDiscoursePositionToMarkableMappings();
        mmax2.requestRefreshDisplay();
        // New: Make modification saveable
        mmax2.setIsBasedataModified(true,true);               
    }
    
    public final void showEditBasedataElementWindow(ArrayList recentTexts, ArrayList recentAttributes, final Node referenceNode, final int mode)
    {         
        // Close any existing window
        if (mmax2.editBasedataWindow != null)
        {
            mmax2.editBasedataWindow.setVisible(false);
            mmax2.editBasedataWindow = null;
        }
        // Create new window
        mmax2.editBasedataWindow = new JFrame("Edit base data");
        mmax2.editBasedataWindow.addWindowFocusListener(new WindowFocusListener()
        {
            public final void windowGainedFocus(WindowEvent we)
            {
                
            }   
            
            public final void windowLostFocus(WindowEvent we)
            {
                if (mmax2.editBasedataWindow != null)
                {
                    mmax2.editBasedataWindow.toFront();
                }
            }
        });                                                    
        
        mmax2.editBasedataWindow.setResizable(false);
        mmax2.editBasedataWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        Box outerBox = Box.createVerticalBox();

        Box upperBox = Box.createVerticalBox();
        final JComboBox recentTextsBox = new JComboBox(recentTexts.toArray(new String[0]));
        upperBox.add(recentTextsBox);
        upperBox.add(Box.createVerticalStrut(10));
        final JTextField textField = new JTextField("",20);
        // If the window was called for edit, preset current word text
        if (mode == MMAX2Constants.EDIT_DE)
        {
            textField.setText(referenceNode.getChildNodes().item(0).getNodeValue());
        }
        upperBox.add(textField);

        Box middleBox = Box.createVerticalBox();
        final JComboBox recentAttributesBox = new JComboBox(recentAttributes.toArray(new String[0]));
        middleBox.add(recentAttributesBox);
        middleBox.add(Box.createVerticalStrut(10));
        final JTextField attributeField = new JTextField("",20);
        // If the window was called to edit, preset current word attributes
        if (mode==MMAX2Constants.EDIT_DE)
        {
            attributeField.setText(MMAX2Utils.toAttributeString(referenceNode.getAttributes(),true));
        }
        
        middleBox.add(attributeField);

        recentTextsBox.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                textField.setText((String)recentTextsBox.getSelectedItem());
            }                    
        });                                                    

        recentAttributesBox.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                attributeField.setText((String)recentAttributesBox.getSelectedItem());
            }                    
        });                                                    
        
        Box bottomBox = Box.createHorizontalBox();
        JButton okButton = new JButton("OK");
        okButton.setFont(okButton.getFont().deriveFont((float)12));
        okButton.setBorder(new EmptyBorder(0,0,1,1));
        bottomBox.add(okButton);
        bottomBox.add(Box.createHorizontalStrut(10));
        
        okButton.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                mmax2.editBasedataWindow.setVisible(false);
                mmax2.editBasedataWindow=null;
                mmax2.setBlockAllInput(false);
                basedataTextAndAttributeSelected(referenceNode, mode, textField.getText().trim(),attributeField.getText().trim());
            }                    
        });                                                    
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(cancelButton.getFont().deriveFont((float)12));
        cancelButton.setBorder(new EmptyBorder(0,0,1,1));        
        bottomBox.add(cancelButton);
        
        cancelButton.addActionListener(new ActionListener()
        {
            public final void actionPerformed(ActionEvent ae)
            {
                mmax2.editBasedataWindow.setVisible(false);
                mmax2.editBasedataWindow=null;
                mmax2.setBlockAllInput(false);
                basedataTextAndAttributeSelected(referenceNode, MMAX2Constants.BASEDATA_EDIT_CANCEL, "","");
            }
        });
        
        JPanel wordPanel = new JPanel();
        wordPanel.setBorder(new TitledBorder("Enter element text (required)"));
        wordPanel.add(upperBox);
        outerBox.add(wordPanel);

        JPanel attributePanel = new JPanel();
        attributePanel.setBorder(new TitledBorder("Enter element attributes (optional)"));
        attributePanel.add(middleBox);
        outerBox.add(attributePanel);                        
        
        JPanel controlPanel = new JPanel();
        controlPanel.add(bottomBox);
        outerBox.add(controlPanel);
        
        mmax2.editBasedataWindow.getContentPane().add(outerBox);
        mmax2.editBasedataWindow.pack();
        mmax2.editBasedataWindow.setLocationRelativeTo(getMMAX2());
        mmax2.setBlockAllInput(true);
        mmax2.editBasedataWindow.setVisible(true);
    }
    
    
    public final void basedataTextAndAttributeSelected(Node referenceNode, int mode, String text, String attribute)
    {        
        if (mode == MMAX2Constants.BASEDATA_EDIT_CANCEL)
        {
            return;
        }
        
        // Update recency lists
        if (recentTextEntries.contains(text)==false && text.equals("")==false)
        {
            recentTextEntries.add(text);
        }
        if (recentAttributeEntries.contains(attribute)==false && attribute.equals("")==false)
        {
            recentAttributeEntries.add(attribute);
        }
        
        // Convert received attribute string to list 
        ArrayList attributesAsList = MMAX2Utils.toAttributeList(attribute);
                
        if (mode == MMAX2Constants.INSERT_DE_BEFORE || mode == MMAX2Constants.INSERT_DE_AFTER)
        {
            // A new element is to be added either before or after referenceNode
            // Create new element
            Element newWord = referenceNode.getParentNode().getOwnerDocument().createElement("word");
            // Add word text as textual child to new element
            newWord.appendChild(referenceNode.getParentNode().getOwnerDocument().createTextNode(text));
            
            // Add attributes as received from the Window. ID is certain to be not contained here!
            for (int z=0;z<attributesAsList.size();z++)
            {
                // Get current entry of form attribute="value"
                String currentEntry = (String)attributesAsList.get(z);
                newWord.setAttribute(currentEntry.substring(0,currentEntry.indexOf("=")),currentEntry.substring(currentEntry.indexOf("=")+1));
            }
                        
            String newID="";          
        
            if (mode == MMAX2Constants.INSERT_DE_BEFORE)
            {
                // If we want to insert _before_ referenceNode, we must find its left neighbour as leftReferenceNode
                Node leftReferenceNode = referenceNode.getPreviousSibling();
                
                // Move left to the next ELEMENT type
                while(leftReferenceNode != null && leftReferenceNode.getNodeType()!=Node.ELEMENT_NODE)
                {
                    leftReferenceNode = leftReferenceNode.getPreviousSibling();
                }
                
                // Create new id
                newID = createBasedataID(leftReferenceNode, referenceNode);
                
                // NewID will be "" when none could be created
                if (newID.equals("")==false)
                {
                    // Set new id
                    newWord.setAttribute("id",newID);
                    referenceNode.getParentNode().insertBefore(newWord, referenceNode);                    
                    // Make new element retrievable by id
                    mmax2.getCurrentDiscourse().addWithID(newID, newWord);                                
                }
            }
            else if (mode == MMAX2Constants.INSERT_DE_AFTER)
            {
                // If we want to insert _after_ referenceNode, we must find its right neighbour as rightReferenceNode
                Node rightReferenceNode = referenceNode.getNextSibling();
                
                // Move right to the next ELEMENT type
                while(rightReferenceNode != null && rightReferenceNode.getNodeType()!=Node.ELEMENT_NODE)
                {
                    rightReferenceNode=rightReferenceNode.getNextSibling();
                }

                // Create new id
                newID = createBasedataID(referenceNode,rightReferenceNode);
                
                // NewID will be "" when none could be created
                if (newID.equals("")==false)
                {
                    newWord.setAttribute("id",newID);
                    referenceNode.getParentNode().insertBefore(newWord, rightReferenceNode);
                    // Make new element retrievable by id
                    mmax2.getCurrentDiscourse().addWithID(newID, newWord);                    
                }
            }
        }
        // no insert
        else if (mode == MMAX2Constants.EDIT_DE)
        {
            // the element was only edited
            // Remove old textual child
            referenceNode.removeChild(referenceNode.getFirstChild());
            // Create and add new textual child
            referenceNode.appendChild(referenceNode.getParentNode().getOwnerDocument().createTextNode(text));
            // Remove all attributes           
            NamedNodeMap attribs = referenceNode.getAttributes();
            for (int z=attribs.getLength()-1;z>=0;z--)
            {
                if (attribs.item(z).getNodeName().equalsIgnoreCase("id"))
                {
                    // Skip id attribute when removing attributes
                    continue;
                }
                attribs.removeNamedItem((String)attribs.item(z).getNodeName());
            }
            // Add attributes            
            for (int z=0;z<attributesAsList.size();z++)
            {                
                String currentEntry = (String)attributesAsList.get(z);                
                ((Element)referenceNode).setAttribute(currentEntry.substring(0,currentEntry.indexOf("=")),currentEntry.substring(currentEntry.indexOf("=")+1));
            }                        
        }
        mmax2.getCurrentTextPane().setControlIsPressed(false);
        //System.err.println(0);
        if (mmax2.getIsAutoReapplyEnabled())
        {
        	System.err.println("Auto-reapplying ...");
        	mmax2.requestReapplyDisplay();
        }

        getCurrentMarkableChart().updateAllMarkableLevels();
        //System.err.println(1);
        mmax2.requestReapplyDisplay();
        //System.err.println(2);
        getCurrentMarkableChart().createDiscoursePositionToMarkableMappings();
        //System.err.println(3);
        // New July 24th 2006: This fix will let bd-editing work
        mmax2.requestRefreshDisplay();
        //System.err.println(4);
        // New: Make modification saveable
        mmax2.setIsBasedataModified(true,true);
    }
       

    /** This method receives two Nodes with IDs and creates a new ID in between these two. */
    private final String createBasedataID(Node leftNeighbour, Node rightNeighbour)
    {
        String leftID="";
        String rightID="";
        String commonElementNameSpace="";
        String newID="";
        
        BigDecimal leftNumber= new BigDecimal("0.00000000000000000000");
        BigDecimal rightNumber= new BigDecimal("0.00000000000000000000");
        BigDecimal difference= new BigDecimal("0.00000000000000000000");
                
        if (leftNeighbour != null && rightNeighbour != null)
        {
            // Normal case, a new element is to be added in between two existing ones
            leftID = leftNeighbour.getAttributes().getNamedItem("id").getNodeValue();
            rightID = rightNeighbour.getAttributes().getNamedItem("id").getNodeValue();
            commonElementNameSpace = leftID.substring(0,leftID.indexOf("_"));
            if (commonElementNameSpace.equalsIgnoreCase(rightID.substring(0,rightID.indexOf("_")))==false)
            {
                System.err.println("Error: Different base data element name spaces!");
                return null;
            }
            
            // Get numerical value of id left of insertion point
            leftNumber = new BigDecimal(leftID.substring(leftID.indexOf("_")+1));
            // Get numerical value of id left of insertion point
            rightNumber = new BigDecimal((rightID.substring(rightID.indexOf("_")+1)));
            // Calculate difference
            difference = rightNumber.subtract(leftNumber);
            difference = difference.divide(new BigDecimal("2.00000000000000000000"));
            newID = commonElementNameSpace+"_"+((BigDecimal)(leftNumber.add(difference)));            
            
        }
        else
        {
            // One of the nodes is null
            if (leftNeighbour == null)
            {
                // The new element is to be added before the first one
                // So its Number must be smaller than that of rightNeighbour
                // RightID must exist
                rightID = rightNeighbour.getAttributes().getNamedItem("id").getNodeValue();
                rightNumber = new BigDecimal(rightID.substring(rightID.indexOf("_")+1));
                difference = rightNumber.divide(new BigDecimal("2.00000000000000000000"));
                commonElementNameSpace = rightID.substring(0,rightID.indexOf("_"));
                newID = commonElementNameSpace+"_"+difference;
            }
            else
            {
                // The new element is to be added after the last one
                // So its Number must be higher than that of leftNeighbour
                // LeftID must exist
                leftID = leftNeighbour.getAttributes().getNamedItem("id").getNodeValue();
                leftNumber = new BigDecimal(leftID.substring(leftID.indexOf("_")+1));
                difference = leftNumber.add(new BigDecimal("1.00000000000000000000"));
                
                commonElementNameSpace = leftID.substring(0,leftID.indexOf("_"));                
                newID = commonElementNameSpace+"_"+difference;
            }            
        }  
        
        if (newID.endsWith(".0"))
        {
            newID = newID.substring(0,newID.length()-2);
        }
    
        System.err.println(newID);        
        return newID;
    }
    
    
    /** This method is called by the MMAX2BasedataEditActionSelector upon selection of an 'add' action.*/
    public final void requestAddBasedataElement(Node referenceNode, int mode)
    {        
        // Just delegate the call to the method for displaying the edit window
        showEditBasedataElementWindow(recentTextEntries, recentAttributeEntries, referenceNode, mode);
    }
    
    public final void saveBasedata(String newFileName)
    {
        if (mmax2 != null)
        {
            if (mmax2.getIsBasedataModified()==false)
            {
                System.err.println("Basedata is clean, not saving!");
                return;
            }
        }
        System.err.println("Saving basedata ... ");
        if (newFileName.equals("")==false)
        {
            wordFileName= newFileName;
        }
                
        /* Test file for existence */
        File destinationFile = new File(wordFileName);
        if(destinationFile.exists())
        {
            /* The file to be written is already existing, so create backup copy first*/
            /* This should be the normal case */
            System.err.println("Filename "+destinationFile.getAbsolutePath()+" exists, creating backup (.bak) file!");
            File oldDestinationFile = new File(wordFileName +".bak");
            if (oldDestinationFile.exists())
            {
                System.err.println("Removing old .bak file!");
                oldDestinationFile.delete();
                oldDestinationFile = new File(wordFileName +".bak");
            }
            destinationFile.renameTo(oldDestinationFile);
        }                   
        
	/* Write DOM to file */
        System.out.println("Writing to file " + wordFileName);
      
        BufferedWriter fw = null;
        
        try
        {
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wordFileName),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }                
  
        String dtdReference = "";
        
        if (wordDOM.getDoctype().getPublicId() != null)
        {
            dtdReference = "<!DOCTYPE words PUBLIC \""+wordDOM.getDoctype().getPublicId()+"\">";
        }
        else if (wordDOM.getDoctype().getSystemId() != null)
        {
            dtdReference = "<!DOCTYPE words SYSTEM \""+wordDOM.getDoctype().getSystemId()+"\">";
        }        

        String encoding = wordDOM.getEncoding();
        if (encoding == null)
        {
            encoding = "UTF-8";
        }
        String wordFileHeader = "<?xml version='1.0' encoding='"+encoding+"'?>";
        
        try
        {
            fw.write(wordFileHeader+"\n"+dtdReference+"\n<words>\n");
            fw.flush();
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
        }
                
        // Item 0 is the doctype !!
        Node currentWordNode = wordDOM.getChildNodes().item(1).getFirstChild();
        
        String currentAttributes = "";
        while(currentWordNode != null)
        {            
            String id = currentWordNode.getAttributes().getNamedItem("id").getNodeValue();
            currentAttributes = MMAX2Utils.toAttributeString(getDiscourseElementByID(id).getAttributes() ,false);
            try
            {
                fw.write("<word "+currentAttributes.trim()+">");
                Node childNode = currentWordNode.getFirstChild();
                String childText = childNode.getNodeValue();
//                <		ersetzen Sie durch		&lt;
//                >		ersetzen Sie durch		&gt;
//                &		ersetzen Sie durch		&amp;
//                "		ersetzen Sie durch		&quot;
//                '		ersetzen Sie durch		&apos;

                if (childText.equals("&"))
                {
                    childText = "&amp;";
                }
                else if (childText.equals(">"))
                {
                    childText = "&gt;";
                }
                else if (childText.equals("<"))
                {
                    childText = "&lt;";
                }
                else if (childText.equals("\""))
                {
                    childText = "&quot;";
                }
                else if (childText.equals("'"))
                {
                    childText = "&apos;";
                }
                                
                fw.write(childText);

                fw.write("</word>\n");
            }
            catch (java.io.IOException ex)
            {
                
            }
            currentWordNode = currentWordNode.getNextSibling();
        }        
        
        try
        {
            fw.write("</words>");
            fw.flush();
            fw.close();
        }
        catch (java.io.IOException ex)
        {
            System.err.println(ex.getMessage());
        }
        if (mmax2 != null)
        {
            getMMAX2().setIsBasedataModified(false,false);            
        }
    }    
}
