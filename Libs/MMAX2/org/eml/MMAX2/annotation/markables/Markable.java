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

package org.eml.MMAX2.annotation.markables;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
// Attributes
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.text.SimpleAttributeSet;

import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.api.MarkableAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class Markable implements java.io.Serializable, MarkableAPI
{       
    /** ID of this markable, copied from XML file. */
    private String ID = "";    
    String string = "";
    /** Array of Arrays of IDs of consecutive Discourse Elements. */
    private String[][] fragments;    
    /** Number of single fragments this markable consists of, > 1 only for discont. Markables */
    int singleFragments;
    /** Number of Discourse Elements this Markable is part of (calculated from this.fragments). */    
    int size;    
    /** Attributes of this markable. After creation, this will include the _span_ attribute! */
    // 1.15 Names are in canonical fom here !
    HashMap<String, String> attributes;        
    
    private MarkableLevel level;    
    private Node nodeRepresentation=null;

    private int[] leftHandlePositions;
    private int[] rightHandlePositions;
    
    int[] displayStartPositions;
    int[] displayEndPositions;    

    // Array with the displayStartPosition for every Discourse Element
    int[] discourseElementStartPositions;
    
    private boolean discontinuous = false;    
    
    int leftmostDisplayPosition;
    int rightmostDisplayPosition;
    
    private boolean isInSearchResult = false;
    
    /** Creates new Markable. During initialization, this method is called during execution of 
        MarkableLevel.createMarkables(). 
     
       Synopsis: This method basically sets a number of Markable object properties, and then calls 
       MarkableHelper.register(this,false). 
     
     */
    public Markable(Node _nodeRepresentation, String _ID, String[][] _fragments, HashMap<String, String> _attributes, MarkableLevel _level) 
    {
        nodeRepresentation = _nodeRepresentation;
        ID = _ID;
        attributes = _attributes; 
        fragments = _fragments;        
        level = _level;
                
        
        // Make level name accessible from style sheet via 'mmax_level' attribute.
        if (attributes.containsKey("mmax_level")==false)
        {
            ((Element)nodeRepresentation).setAttribute("mmax_level",level.getMarkableLevelName());
            attributes.put("mmax_level", level.getMarkableLevelName());
        }
        else
        {
            // The current markable does have a mmax_level attribute
            // Check whether it matches the level name
            if (((String)attributes.get("mmax_level")).equalsIgnoreCase(level.getMarkableLevelName())==false)
            {
                // The current markable has a mmax_level attribute, but is different than
                // the level name
                ((Element)nodeRepresentation).setAttribute("mmax_level",level.getMarkableLevelName());
                attributes.put("mmax_level", level.getMarkableLevelName());               
                level.setIsReadOnly(true);
                System.err.println("Mismatch in mmax_level attribute: "+level.getMarkableLevelName());
            }
        }
        
        /** Set size attribute as number of single fragments. */
        singleFragments = fragments.length;
        
        /** Set discontinuity convenience field */
        if (singleFragments > 1) discontinuous = true;
                 
        /** Init arrays to have space for one left and right handle per fragment */
        // Mod Jan 8 2021: was '0'
//        leftHandlePositions = new int[singleFragments];
//        rightHandlePositions = new int[singleFragments];

        leftHandlePositions = new int[0];
        rightHandlePositions = new int[0];
        
        /** Init arrays to have space for one start and one end position per fragment */
        displayStartPositions = new int[singleFragments];
        displayEndPositions = new int[singleFragments];                        
        // Register without has update
        MarkableHelper.register(this,false);
    }
    
      
    /** This method is called on each markable after a change in the base data */
    public final void update(String[][] _fragments)
    {        
        // Override existing fragments
        fragments = _fragments;
        /** Set size attribute as number of single fragments. */
        singleFragments = fragments.length;
                    
        /** Set discontinuity convenience field */
        if (singleFragments > 1) discontinuous = true;
                 
        /** Init arrays to have space for one left and right handle per fragment */
        /** WRONG: These arrays must be empty, unless handles ared added by style sheet**/
        /** Mod on Jan 8th 2021: init with 0**/ 
        leftHandlePositions = new int[0];
        rightHandlePositions = new int[0];
    
        /** Init arrays to have space for one start and one end position per fragment */
        displayStartPositions = new int[singleFragments];
        displayEndPositions = new int[singleFragments];                        
        // Register with has update
        MarkableHelper.register(this,true);
        //MarkableHelper.register(this,false);
        
    }
    
    public final String toString()
    {
    	return string;
    }
    
    // 1.15 Tentatively removed
//    public final String[] getDiscourseElements()
//    {
//        String[] des = getDiscourseElementIDs();
//        ArrayList temp = new ArrayList();
//        for (int z=0;z<des.length;z++)
//        {
//            temp.add(level.getCurrentDiscourse().getDiscourseElementNode(des[z]).getFirstChild().getNodeValue());
//        }
//        return (String[])temp.toArray(new String[0]);
//    }

    
    
    public final String[] getDiscourseElementIDs()
    {
        // For discontinuous markables as well!!
        ArrayList<String> temp = new ArrayList<String>();
        for (int z=0;z<singleFragments;z++)
        {
            // Get currentFragment
            String[] currentFragment = fragments[z];
            for (int o=0;o<currentFragment.length;o++)
            {
                if (temp.contains(currentFragment[o])==false)
                {
                    temp.add(currentFragment[o]);
                }
            }            
        }
        return (String[])temp.toArray(new String[0]);
    }

    public final boolean removeDiscourseElements(String[] removees)
    {
        // removees are certain to be continuous, but may contain more than the actual markable
//        String[] currentFragment = null;
        boolean result = false;
        
        // getDiscourseElements is discont-ready!
        MMAX2DiscourseElement[] sequence = level.getCurrentDiscourse().getDiscourseElements(this);
        ArrayList<MMAX2DiscourseElement> DEsAsList = new ArrayList<MMAX2DiscourseElement>(java.util.Arrays.asList(sequence));        
        
        // Now, desAsList contains all of the current markables des        
        // iterate over removees
        for (int o=0;o<removees.length;o++)
        {
            // Get current removee
            String currentRemovee = removees[o];
            // iterate over all des in markable
            for (int u=0;u<DEsAsList.size();u++)
            {
                // If the removee is found, remove it and break
                if (((MMAX2DiscourseElement)DEsAsList.get(u)).getID().equalsIgnoreCase(currentRemovee))
                {
                    DEsAsList.remove(u);
                    break;
                }
            }
        }
        // Now, DEsAsList has been cleared of removees
        if (DEsAsList.size()!=0)
        {            
            update(MarkableHelper.toFragments(DEsAsList));
            result=true;
        }
        level.setIsDirty(true,false);
        return result;
    }

/*    
    public final void addDiscourseElementsBulk(ArrayList addeesIDs)
    {
        // Note: addessIDs may contains duplicates
        // Comparing DE objects for identity does not work as they are never identical
        
        ArrayList DEsToBeAdded = new ArrayList();
        // Get list of MMAX2DiscourseElement objects to be added
        for (int z=0;z<addeesIDs.size();z++)
        {
            DEsToBeAdded.add(level.getCurrentDiscourse().getDiscourseElementByID((String)addeesIDs.get(z)));
        }
        
        
        // Get array of DEs that markable already consists of
        ArrayList DEsAlreadyThere = new ArrayList(java.util.Arrays.asList(level.getCurrentDiscourse().getDiscourseElements(this)));
        // Merge both, ignoring duplicates for now
        DEsAlreadyThere.addAll(DEsToBeAdded);
/*        
        for (int b=0;b<DEsToBeAdded.size();b++)
        {
            MMAX2DiscourseElement de = (MMAX2DiscourseElement) DEsToBeAdded.get(b);
                DEsAlreadyThere.add(de);
        }
*/
/*
        MMAX2DiscourseElement[] toSort = (MMAX2DiscourseElement[]) DEsAlreadyThere.toArray(new MMAX2DiscourseElement[0]);;
        java.util.Arrays.sort(toSort,new DiscourseOrderDiscourseElementComparator());
        
        DEsAlreadyThere = new ArrayList(java.util.Arrays.asList(toSort));
        
        // Remove any duplicates 
        MarkableHelper.removeDuplicateDiscoursePositions(DEsAlreadyThere);
        
        // Now, DEsAlreadyThere contains the new span
        update(MarkableHelper.toFragments(DEsAlreadyThere));               
        level.setIsDirty(true,false);
        return;
    }
  */  
    public final boolean addDiscourseElements(String[] addees)
    {
        ArrayList<MMAX2DiscourseElement> addeesAsList = new ArrayList<MMAX2DiscourseElement>();
        // Get list of MMAX2DiscourseElement objects to be added
        for (int z=0;z<addees.length;z++)
        {
            addeesAsList.add(level.getCurrentDiscourse().getDiscourseElementByID(addees[z]));
        }
        
        // Find discPos of first and last DE to add
        int firstDiscPosToAdd = ((MMAX2DiscourseElement)addeesAsList.get(0)).getDiscoursePosition();
        int lastDiscPosToAdd = ((MMAX2DiscourseElement)addeesAsList.get(addeesAsList.size()-1)).getDiscoursePosition();
        
        // Get array of DEs that markable already consists of
        MMAX2DiscourseElement[] sequence = level.getCurrentDiscourse().getDiscourseElements(this);
        
        // Get list of DEs already in Markable
        ArrayList<MMAX2DiscourseElement> DEsAsList = new ArrayList<MMAX2DiscourseElement>(java.util.Arrays.asList(sequence));        
        // Get range of markable
        int firstDiscPosInMarkable = ((MMAX2DiscourseElement)DEsAsList.get(0)).getDiscoursePosition();
        int lastDiscPosInMarkable = ((MMAX2DiscourseElement)DEsAsList.get(DEsAsList.size()-1)).getDiscoursePosition();
        
        if (firstDiscPosToAdd < firstDiscPosInMarkable)
        {
        	// The sequence of elements to be added starts before current markable
            // addAll is safe here, because duplicates will be removed later
            DEsAsList.addAll(0, addeesAsList);
            MarkableHelper.removeDuplicateDiscoursePositions(DEsAsList);
        }
        else if (lastDiscPosInMarkable < firstDiscPosToAdd )
        {
            // The sequence of elements to be added starts after current markable ends
            // addAll is safe because duplicates are not possible
            DEsAsList.addAll(addeesAsList);            
            // better safe than sorry
            MarkableHelper.removeDuplicateDiscoursePositions(DEsAsList);
        }
        else
        {
            // We add somewhere in between
            MMAX2DiscourseElement currentDE = null;
            // Now all we have to do is find the insertion point in DEsAsList
            // Iterate over it
            for (int z=0;z<DEsAsList.size();z++)
            {
                // Get currentDE
                currentDE = (MMAX2DiscourseElement)DEsAsList.get(z);
                if (currentDE.getDiscoursePosition() > firstDiscPosToAdd)
                {
                    // Add entire list here, duplicates will be removed later
                    DEsAsList.addAll(z, addeesAsList);
                    break;
                }
            }
            MarkableHelper.removeDuplicateDiscoursePositions(DEsAsList);
        }
                
        update(MarkableHelper.toFragments(DEsAsList));               

               
        level.setIsDirty(true,false);
        return true;
    }
    
    public final void deleteMe()
    {
        level.deleteMarkable(this);
    }
    
    public final SimpleAttributeSet getAttributedependentStyle()
    {
        return level.getRenderer().getAttributesForMarkable(this);
    }
    
    public final void destroyDependentComponents()
    {
        attributes = null;
        discourseElementStartPositions = null;
        displayStartPositions = null;
        displayEndPositions = null;
        fragments = null;
        leftHandlePositions = null;
        rightHandlePositions = null;
        nodeRepresentation = null;
        string = null;
        level = null;
        
    }

    public final int getLeftmostDiscoursePosition()
    {
        return level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(fragments[0][0]);
    }

    public final int getRightmostDiscoursePosition()
    {
        String[] finalFrag = fragments[fragments.length-1];
        return level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(finalFrag[finalFrag.length-1]);
    }
    
    
    public final int getLeftmostDisplayPosition()
    {
        return leftmostDisplayPosition;
    }

    public final int getRightmostDisplayPosition()
    {
        return rightmostDisplayPosition;
    }
    
    public final int[] getDisplayStartPositions()
    {
        return displayStartPositions;
    }

    public final int[] getDisplayEndPositions()
    {
        return displayEndPositions;
    }
    
    public final int[] getDiscourseElementStartPositions()
    {
        return discourseElementStartPositions;
    }
    

    protected final void resetHandles()
    {
        leftHandlePositions = null;
        leftHandlePositions = new int[0];
        rightHandlePositions = null;
        rightHandlePositions = new int[0];        
    }
    
    public final void renderMe(int mode)
    {
        if (isInSearchResult && mode == MMAX2Constants.RENDER_UNSELECTED)
        {
            level.getRenderer().render(this, MMAX2Constants.RENDER_IN_SEARCHRESULT);
        }
        else
        {
            level.getRenderer().render(this, mode);
        }
    }
    
    public final boolean isDiscontinuous()
    {
        return discontinuous;
    }
        
    public final int getSize()
    {
        return this.size;
    }
    
    public final void addLeftHandlePosition(int pos)
    {        
        if (leftHandlePositions.length == 0)
        {
            leftHandlePositions = null;
            leftHandlePositions = new int[singleFragments];            
            level.setHasHandles(true);            
        }
        
        /* Search array of leftHandlePositions */
        //for (int p=0;p<size;p++)
        for (int p=0;p<singleFragments;p++)
        {
            if (leftHandlePositions[p]==0)
            {
                leftHandlePositions[p]=pos;
                break;
            }
        }
    }
    
    public final int[] getLeftHandlePositions()
    {
        return leftHandlePositions;
    }

    public final void addRightHandlePosition(int pos)
    {
        if (rightHandlePositions.length == 0)
        {
            rightHandlePositions = null;
            rightHandlePositions = new int[singleFragments];            
        }        
        /** Search array of rightHandlePositions */
        for (int p=0;p<singleFragments;p++)
        {
            if (rightHandlePositions[p]==0)
            {
                rightHandlePositions[p]=pos;
                break;
            }
        }
    }

    public final int[] getRightHandlePositions()
    {
        return rightHandlePositions;
    }
    
    public final Node getNodeRepresentation()
    {
        return this.nodeRepresentation; 
    }
    
    final public String getID()
    {
        return this.ID;
    }
    
    public final String getMarkableLevelName()
    {
        return this.level.getMarkableLevelName();
    }
    
    public final MarkableLevel getMarkableLevel()
    {
        return level;
    }

    /** This does return attributes only, values are changed back immediately afterwards!! (Used for creation of ActionSelector) 
        Important: This does NOT make sure to set the attribute window to prior display state!! */
    public final MMAX2Attribute[] getValidatedAttributes()
    {
        MMAX2Attribute[] result = null;
        // Store old dirty status, because validation may change that
        boolean oldStat = false;
        if (level.getCurrentDiscourse().getMMAX2()!= null)
        {
            oldStat = level.getCurrentDiscourse().getMMAX2().getIsAnnotationModified();
        }
        // Get attributes for this markable, validated by attribute window
        result = level.getCurrentAnnotationScheme().getAttributes(this);
        // Restore old attribute window status (if one existed)
        
        // If the anno was not originally dirty, reset it to clean
        if (level.getCurrentDiscourse().getMMAX2()!= null)
        {
            if (!oldStat) level.getCurrentDiscourse().getMMAX2().setIsAnnotationModified(false);
        }
        return result;
    }
    
    
    /** This method returns true if displayPosition is covered by the Markable, false otherwise. */
    public final boolean coversDisplayPosition(int displayPosition)
    {
        boolean result = false;
        // Iterate over all fragments of this Markable 
        for (int z=0;z<singleFragments;z++)
        {
            if (displayPosition == displayStartPositions[z] || displayPosition == displayEndPositions[z])
            {
                // displayPosition is directly on either displayStart-or EndPosition
                result = true;
                break;
            }
            if (displayPosition > displayStartPositions[z] && displayPosition < displayEndPositions[z])
            {
                result = true;
                break;
            }
        }
        return result;
    }

    public final String[][] getFragments()
    {
        return fragments;
    }

    public final boolean getIsInSearchResult()
    {
        return this.isInSearchResult;
    }
    public final void setIsInSearchResult(boolean status)
    {
        isInSearchResult = status;
    }
    public String toTrimmedString(int maxLen)
    {
        String complete = toString(); // This includes fragment boundaries
        String copyofmarkabletext=complete;
        if (complete.length() > maxLen)
        {
            /* Trim current markabletext */
            /* First part */
            complete=complete.substring(0,(maxLen-5)/2);
            /* Omission symbols */
            complete=complete+" [...] ";
            /* Last part */
            complete=complete+(copyofmarkabletext.substring(copyofmarkabletext.length()-((maxLen-5)/2)+1,copyofmarkabletext.length()));
        }
        complete.trim();        
        return complete;
    }

        
    public final Point getPoint()
    {
    	// Point is the top left coord of the markable. If no handles are active, we use the display start position, else the
    	// left handle position
        Point resultPoint = null;
        Rectangle2D tempRect = null;
        try
        {
            if (leftHandlePositions.length==0)
            {
            	// Use display pos if no handles are available
            	// This is wrong: HandlePositions are currently initialized as a one-elem-array,
            	// and so never have length 0
            	// Changed: handlepositions are now inited with len 0 array
            	tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(displayStartPositions[0]);
            }
            else
            {
            	// else use left handle pos
                tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(leftHandlePositions[0]);
            }
        }
        catch (javax.swing.text.BadLocationException ex)
        {
            System.out.println("Error with display position determination for Markable "+getID());
        }
          
        resultPoint = new Point((int)tempRect.getX(),(int)tempRect.getY());        
        return resultPoint;
    }
    
    public final Point[] getRectangle()
    {
        Graphics2D graphics = (Graphics2D) this.getMarkableLevel().getCurrentDiscourse().getMMAX2().getCurrentTextPane().getGraphics();         
        FontMetrics m = graphics.getFontMetrics();
        int brackWidth = m.charWidth('[');
        int hOffSet=(int)(brackWidth/2);
        int lineHeight = m.getHeight();
        int vOffSet = (int)this.level.getCurrentDiscourse().getMMAX2().currentDisplayFontSize/5;
                
    	// Rectangle describes the four corners of a markable
    	// Point is the top left coord of the markable. If no handles are active, we use the display start position, else the left handle position
    	Point[] res = new Point[4];
        Rectangle2D tempRect = null;
        
        try
        {
            if (leftHandlePositions.length==0)
            {
            	// x,y of top left, width = 0, height depends on line height
            	tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(displayStartPositions[0]);
            }
            else
            {
            	// else use left handle pos
                tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(leftHandlePositions[0]);
            }
        	//topLeft
        	res[0] = new Point((int)tempRect.getX()+hOffSet, (int) tempRect.getY() + vOffSet);
        	//bottomLeft 
        	res[3] = new Point((int)tempRect.getX()+hOffSet, (int) tempRect.getY() + lineHeight - vOffSet);

            if (rightHandlePositions.length==0)
            {
            	// x,y of top right, width = 0, height depends on line height
            	tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(displayEndPositions[displayEndPositions.length-1]);
            }
            else
            {
            	// else use right handle pos
                tempRect = level.getCurrentDiscourse().getMMAX2().getCurrentTextPane().modelToView2D(rightHandlePositions[rightHandlePositions.length-1]);
            }
            //topRight 
        	res[1] = new Point((int)tempRect.getX()+hOffSet, (int) tempRect.getY() + vOffSet);
        	// bottomRight 
        	res[2] = new Point((int)tempRect.getX()+hOffSet, (int) tempRect.getY() + lineHeight - vOffSet);        	        	
        }
        catch (javax.swing.text.BadLocationException ex)
        {
            System.out.println("Error with display position determination for Markable "+getID());
        }        
        return res;
    }

    
    /** This method returns all of this Markable's attributes (except ID and SPAN, which are system-attributes) as a HashMap. */
    public final HashMap getAttributes()
    {
        return this.attributes;
    }
    
    public final void setAttributes(HashMap newAttributes)
    {
        this.attributes = null;
        this.attributes = newAttributes;
        String current = "";
        Iterator allKeys = newAttributes.keySet().iterator();
        while(allKeys.hasNext())
        {
            current = (String) allKeys.next();          
            ((Element)nodeRepresentation).setAttribute(current,(String)newAttributes.get(current));
        }             
        ((Element)nodeRepresentation).setAttribute("mmax_level",this.level.getMarkableLevelName());
        // Removed on March 19, 2010: This caused all markable selections to make the annotation dirty
        // level.setIsDirty(true,false);
        // Dirtying the respective level will be handled by the calling method
    }
    
    
    public final void selectMe()
    {
        MMAX2 localMMAX2ref = getMarkableLevel().getCurrentDiscourse().getMMAX2();
        if (localMMAX2ref.getCurrentDiscourse().getHasGUI())
        {
            getMarkableLevel().getCurrentDiscourse().getCurrentMarkableChart().markableLeftClicked(this);
            localMMAX2ref.setIgnoreCaretUpdate(true);
            try
            {
                localMMAX2ref.getCurrentTextPane().scrollRectToVisible(localMMAX2ref.getCurrentTextPane().modelToView(this.getLeftmostDisplayPosition()));
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                System.err.println("Cannot render modelToView()");
            }   
            localMMAX2ref.setIgnoreCaretUpdate(false);
            localMMAX2ref.getCurrentTextPane().startAutoRefresh();                                
        }
        else
        {
            System.err.println("No selectMe() in non-GUI mode!");
        }
    }
    
    
    /** This method returns the value this Markable has for attribute attributeName, or null if attribute is not defined for
        Markable. attributeName is set to lowercase before its value is retrieved, and value is set to lower case before 
        it is returned. 
        1.15: Casing is maintained now!*/
    public final String getAttributeValue(String attributeName)
    {
        String result = null;
        //attributeName = attributeName.toLowerCase();
        if (this.attributes.containsKey(attributeName))
        {
            result = (String) this.attributes.get(attributeName);
        }
        return result;
    }
    
    public final String getAttributeValue(String name, String defaultIfUndefined)
    {
        //String result = (String) attributes.get(name.toLowerCase());
        String result = (String) attributes.get(name);
        if (result == null) result = defaultIfUndefined;
        return result;
    }

    
    public final void setAttributeValue(String attributeName, String value)
    {
    	// This is apparently only used in relation to relation-activities, so dirty = true is probably ok
        //attributes.put(attributeName.toLowerCase(), value.toLowerCase());
    	// For 1.15
        //attributes.put(attributeName.toLowerCase(), value);    	
        attributes.put(attributeName, value);
        level.setIsDirty(true,false);
    }
    
    public final void setAttributeValueToNode(String attributeName, String value)
    {
        // Todo: Make sure that attribute really exists!!
        //nodeRepresentation.getAttributes().getNamedItem(attributeName).setNodeValue(value.toLowerCase());
    	// For 1.15
    	nodeRepresentation.getAttributes().getNamedItem(attributeName).setNodeValue(value);
    }

    // 1.15 Tentatively removed
//    public final void removeAttribute(String attributeName)
//    {
//        attributeName = attributeName.toLowerCase();
//        this.attributes.remove(attributeName);
//        this.nodeRepresentation.getAttributes().removeNamedItem(attributeName);
//        level.setIsDirty(true,false);
//    }
    
    
    /** This method returns true if an attribute with name attributeName is defined for this Markable, i.e. if it 
       has a non-null value in this.attributes, false otherwise. */
    public final boolean isDefined(String attributeName)
    {
//        attributeName = attributeName.toLowerCase();
        return this.attributes.containsKey(attributeName);
    }

    // 1.15 Tentatively removed    
//    public final Markable cloneMarkable()
//    {
//        System.out.println(nodeRepresentation.getAttributes().toString());
//        Markable result = new Markable(this.nodeRepresentation,this.ID,this.fragments,new HashMap<String, String>(this.attributes),this.level);
//        return result;
//    }
    
  
}
