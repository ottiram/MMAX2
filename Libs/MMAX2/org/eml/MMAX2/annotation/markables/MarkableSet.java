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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.HashSet;

import org.eml.MMAX2.annotation.query.MMAX2QueryTree;
import org.eml.MMAX2.api.MarkableSetAPI;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;

public class MarkableSet implements Renderable, MarkableSetAPI
{
    private MarkableRelation markableRelation;
    /** Name of the attribute value all Markables in this MarkableSet share (e.g. 'set_4'). */
    private String attributeValue;

    /** Whether this set is ordered or unordered. */
    private boolean ordered;
    
    /** Used only if this.ordered. */
    private HashSet<Markable> unorderedSet;
    /** Used only if !this.ordered. */
    private ArrayList<Markable> orderedSet;

    /** Number of Markables in this set, either ordered or unordered. */
    private int size;
    /** Width of the line drawn when rendering this set. */
    private int lineWidth;
        
    /** TODO: Support MAXSIZE */
    // For 'classic' rendering
    int[] X_points=null;
    int[] Y_points=null;
    
    Point[][] rects=null;
    
    private int leftMostPosition = Integer.MAX_VALUE;
    private int rightMostPosition = -1;            
    private boolean ambient = false;    // an ambient set is one that is rendered as a potential merge / adopt candidate 
    private boolean permanent = false;
    
    /** Creates a new MarkableSet for attributeValue _attributeValue (e.g. set_4), with ordering set to _ordered. */
    public MarkableSet(String _attributeValue, boolean _ordered, int _lineWidth, Color _color, int _lineStyle, MarkableRelation relation)
    {
        attributeValue = _attributeValue;
        ordered = _ordered;
        // Init appropriate list 
        if (ordered)  { orderedSet = new ArrayList<Markable>(); }
        else          { unorderedSet = new HashSet<Markable>(); }

        /** Set rendering attributes. */
        lineWidth = _lineWidth;
        markableRelation = relation;
    }
   
    public final void setIsPermanent(boolean _permanent)
    {
        permanent = _permanent;
    }
    
    public final boolean getIsPermanent()
    {
        return permanent;
    }
        
    public final void setAmbient(boolean status)
    {
        this.ambient = status;
    }
    
    public final boolean isAmbient()
    {
        return ambient;
    }
    
    /** Returns the value of the MARKABLE_SET-type MarkableRelation that this set belongs to, e.g. 'set_4'.*/
    public final String getAttributeValue()
    {
        return this.attributeValue;
    }
    
    public final void removeMarkable(Markable _markable)
    {
        if (!ordered)
        {
            unorderedSet.remove(_markable);
        }
        else
        {
            orderedSet.remove(_markable);
        }
        size--;
    }
    
    /** Adds a Markable to this MarkableSet. If this.ordered, the Markable will be inserted at the correct (document order) 
        position in this.orderedSet, otherwise it is just added to this.unorderedSet. Document order position is determined on 
        the basis of the displayStartPosition of the first fragment in the Markable. */
    public final void addMarkable(Markable _markable)
    {       
        if (_markable.getLeftmostDisplayPosition() < this.leftMostPosition)
        {
            this.leftMostPosition = _markable.getLeftmostDisplayPosition();
        }
        if (_markable.getRightmostDisplayPosition() > this.rightMostPosition)
        {
            this.rightMostPosition = _markable.getRightmostDisplayPosition();
        }
        
        boolean added = false;
        if (!ordered)
        {
            // Since this set is unordered, simply put Markable somewhere
            unorderedSet.add(_markable);
        }
        else
        {            
            // This set is ordered, so insert Markable at correct position
            if (size==0)
            {
                // This is still empty, so just add // This is unnecessary !!!
                orderedSet.add(_markable);
            }
            else
            {
                Markable markableAtCurrentPos=null;
                // Position is determined from displayStartPosition of first fragment
                // If two DEs have the same displayStartPosition, the shorter is inserted before the longer
                for (int o=0;o<size;o++)
                {
                    markableAtCurrentPos = (Markable) orderedSet.get(o);
                    if (_markable.getDisplayStartPositions()[0] < markableAtCurrentPos.getDisplayStartPositions()[0])
                    {
                        // If the addee starts earlier, add it before current one
                        orderedSet.add(o,_markable);
                        added = true;
                        break;
                    }
                    else if (_markable.getDisplayStartPositions()[0] == markableAtCurrentPos.getDisplayStartPositions()[0])
                    {
                        // Addee and current start at the same position
                        if (_markable.getSize() < markableAtCurrentPos.getSize())
                        {
                            // If addee is shorter, add it before current one
                            orderedSet.add(o,_markable);
                            added = true;
                            break;
                        }
                    }
                }
                if (!added) orderedSet.add(_markable);
            }
        }    
        // Increment size of this set
        size++;
    }
    
    public final void updateLinePoints()
    {
        updateLinePoints(false);
    }
    
    /** This method updates the cached XY-coordinates of the top left corners of each Markable in this MarkableSet. The method is called
        after display changes. */
    public final void updateLinePoints(boolean recalcExtent)
    {    	    	
        Point currentPoint=null;
        X_points = new int[size];
        Y_points = new int[size];

        // Store four ints for each elem in size
        rects = new Point[size][4];
        
        if (recalcExtent) 
        {
            leftMostPosition = Integer.MAX_VALUE;
            rightMostPosition = -1;        
        }
        Markable currentMarkable = null;
        // Iterate over all Markables in this set
        for (int z=0;z<size;z++)
        {
            currentMarkable = ((Markable)orderedSet.get(z));
            // Get and store points of line
            currentPoint = currentMarkable.getPoint();
            X_points[z] = (int)currentPoint.getX();
            Y_points[z] = (int)currentPoint.getY();            

            rects[z] = currentMarkable.getRectangle();
            
            if (recalcExtent)
            {
	            if (currentMarkable.getLeftmostDisplayPosition() < this.leftMostPosition)
	            {
	                this.leftMostPosition = currentMarkable.getLeftmostDisplayPosition();
	            }
	            if (currentMarkable.getRightmostDisplayPosition() > this.rightMostPosition)
	            {
	                this.rightMostPosition = currentMarkable.getRightmostDisplayPosition();
	            }
            }
            
        }
    }    

//    /** This method updates the cached XY-coordinates of the top left corners of each Markable in this MarkableSet. The method is called
//    after display changes. */
//public final void updateLinePoints_bak(boolean recalcExtent)
//{
//    Point currentPoint=null;
//    Point[] currentRectangle=null;
//    
//    top_left_points=new Point[size];
//    top_right_points=new Point[size];
//    bottom_left_points=new Point[size];    		
//    bottom_right_points=new Point[size];
//    
//    X_points = new int[size];
//    Y_points = new int[size];
//    
//    if (recalcExtent)
//    {
//        Markable currentMarkable = null;
//        leftMostPosition = Integer.MAX_VALUE;
//        rightMostPosition = -1;
//        // Iterate over all Markables in this set
//        for (int z=0;z<size;z++)
//        {
//            currentMarkable = ((Markable)orderedSet.get(z));
//            // Get and store points of line
//            currentPoint = currentMarkable.getPoint();
//            X_points[z] = (int)currentPoint.getX();
//            Y_points[z] = (int)currentPoint.getY();            
//            
//            if (currentMarkable.getLeftmostDisplayPosition() < this.leftMostPosition)
//            {
//                this.leftMostPosition = currentMarkable.getLeftmostDisplayPosition();
//            }
//            if (currentMarkable.getRightmostDisplayPosition() > this.rightMostPosition)
//            {
//                this.rightMostPosition = currentMarkable.getRightmostDisplayPosition();
//            }
//            
//            currentRectangle=currentMarkable.getRectangle();
//            top_left_points[z]=currentRectangle[0];
//            top_right_points[z]=currentRectangle[1];
//            bottom_right_points[z]=currentRectangle[2];
//            bottom_left_points[z]=currentRectangle[3];
////            System.err.println(top_left_points[z]);
////            System.err.println(top_right_points[z]);
////            System.err.println(bottom_right_points[z]);
////            System.err.println(bottom_left_points[z]);
//        }                                
//    }
//    else
//    {
//        // Iterate over all Markables in this set
//        for (int z=0;z<size;z++)
//        {
//            // Get and store points of line
//            currentPoint = ((Markable)orderedSet.get(z)).getPoint();
//            X_points[z] = (int)currentPoint.getX();
//            Y_points[z] = (int)currentPoint.getY();            
//        }                    
//    }        
//}    
    
    
    /** This method is called ONCE when this set is to be rendered initially. It renders both the individual set member Markables
        and the lines between them (if any.) It is also called after deletion of set members occurred. */
    public final void select(Graphics2D graphics, MMAX2Document doc, Markable currentlySelectedMarkable)
    {        
        Markable temp = null;
        // Make sure all coordinates are correct
        updateLinePoints(false);
        // Notify document of (minimal) change
        doc.startChanges(leftMostPosition, (rightMostPosition-leftMostPosition)+1);
       
        if (ordered)            
        {
            // Right now, only ordered sets can be rendered !!
            if (size > 1)
            {                  
                // Iterate over entire set
                for (int z=0;z<size;z++)
                {
                    // Render Markable itself, unless it is the currently selected one, which is expected to be highlighted already
                    temp = ((Markable)orderedSet.get(z));
                    if (temp.equals(currentlySelectedMarkable)==false)
                    {
                        temp.renderMe(MMAX2Constants.RENDER_IN_SET);
                    }
                }
                // Draw entire line at once; points have been set/updated by call to updateLinePoints() 
                drawSet(graphics);
            }
        } // unordered              
        doc.commitChanges();
    }
    
    /** This method is called whenever this set is to be re-rendered. It renders only the lines between set member Markables
        (if any.) */
    public final void refresh(Graphics2D graphics)
    {            
        if (X_points == null) { updateLinePoints(false); }
        // Currently, only ordered sets can be rendered        
        if (size > 1)
        {   
            // Re-render entire line
            drawSet(graphics);
        }
    }
    

    /** This method is called ONCE to remove all selection-dependent rendering from this set. It renders unselected all member
        set Markables, and refreshes the display so that lines between Markables (if any) are removed. */
    public final void unselect(MMAX2Document doc)
    {               
        doc.startChanges(leftMostPosition, (rightMostPosition-leftMostPosition)+1);
        if (ordered)            
        {
            // Right now, only ordered sets can be rendered !!
            if (size > 1)
            {   
                // Iterate over entire set
                for (int z=0;z<size;z++)
                {
                    // Reset each Markable individually
                    ((Markable)orderedSet.get(z)).renderMe(MMAX2Constants.RENDER_UNSELECTED);
                }
            }
        }
        X_points = null;
        Y_points = null;        
        doc.commitChanges();
        ambient = false;
    }
        
    public final void drawSet(Graphics2D graphics)
    {
    	Color co = markableRelation.getLineColor();
	    graphics.setColor(co);
        if (!ambient) { graphics.setStroke(new BasicStroke(this.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1)); }
        else
        {
            float[] dash = new float[2];
            dash[0] =10;
            dash[1] =10;
            graphics.setStroke(new BasicStroke(this.lineWidth-1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, dash, 0));
        }
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        QuadCurve2D.Double c = null;
        Point ctrlPoint = null;
        int x1,y1,x2,y2 = 0;        
        int tempStyle = markableRelation.getLineStyle();
        if (tempStyle==MMAX2Constants.STRAIGHT)
        {
        	boolean classic=false;
        	if (classic)
        	{
        		graphics.drawPolyline(X_points,Y_points,size);
        	}
        	else
        	{
                for (int z=0;z<size-1;z++)
                {
            		double dist, mindist=100000;
            		int minx1=0;  
            		int miny1=0;
            		int minx2=0;
            		int miny2 =0;
                	Point[]r1=rects[z];
                	Point[]r2=rects[z+1];
                	for (int rx1=0;rx1<4;rx1++)
                	{
                		for (int rx2=0;rx2<4;rx2++)
                		{
                			dist=Point2D.distance(r1[rx1].getX(), r1[rx1].getY(), r2[rx2].getX(), r2[rx2].getY());
                			if (dist<mindist)
                			{
                				mindist=dist;
                				minx1=(int)r1[rx1].getX();
                				miny1=(int)r1[rx1].getY();
                				minx2=(int)r2[rx2].getX();
                				miny2=(int)r2[rx2].getY();                				
                			}
                		}
                	}		
            		// shortest link between x1 and x2 has been found
            		graphics.drawLine(minx1, miny1, minx2, miny2);
                }
        		
        	}
        }
        else if (tempStyle==MMAX2Constants.LCURVE || tempStyle==MMAX2Constants.RCURVE || tempStyle==MMAX2Constants.XCURVE)            
        {
            for (int z=0;z<size-1;z++)
            {
                x1 = X_points[z];
                y1 = Y_points[z];
                x2 = X_points[z+1];
                y2 = Y_points[z+1];
                c = new QuadCurve2D.Double();
                ctrlPoint = MMAX2Utils.calculateControlPoint(x1,y1,x2,y2,markableRelation.getLineStyle());
                c.setCurve((double)x1,(double)y1,(double)ctrlPoint.getX(),(double)ctrlPoint.getY(),(double)x2,(double)y2);
                graphics.draw(c);
            }
        }
    }
    
    public final boolean containsMatchingMarkableFromLevel(String markableProducingQuery, MarkableLevel level)
    {    
        boolean result = false;
        
        MMAX2QueryTree tree = null;
        try
        {
            tree = new MMAX2QueryTree(markableProducingQuery, level);
        }
        catch (org.eml.MMAX2.annotation.query.MMAX2QueryException ex)
        {
            ex.printStackTrace();
        }       
        ArrayList resultList = tree.execute(null);
        
        for (int b=0;b<resultList.size();b++)
        {
            if (containsMarkable((Markable)resultList.get(b)))
            {
                result=true;
                break;
            }
        }
        return result;
    }

    public final boolean startsWithMatchingMarkableFromLevel(String markableProducingQuery, MarkableLevel level)
    {    
        boolean result = false;
        
        MMAX2QueryTree tree = null;
        try
        {
            tree = new MMAX2QueryTree(markableProducingQuery, level);
        }
        catch (org.eml.MMAX2.annotation.query.MMAX2QueryException ex)
        {
            ex.printStackTrace();
        }       
        ArrayList resultList = tree.execute(null);
        
        for (int b=0;b<resultList.size();b++)
        {
            if (startsWithMarkable((Markable)resultList.get(b)))
            {
                result=true;
                break;
            }
        }
        return result;
    }
    
       
    
    //////
    
    
    public final MarkableRelation getMarkableRelation()
    {
        return markableRelation;
    }
        
    public final boolean containsMarkable(Markable markable)
    {
        if (ordered)
        {
           return orderedSet.contains(markable);
        }
        else
        {
            return unorderedSet.contains(markable);
        }
    }

    public final boolean startsWithMarkable(Markable markable)
    {
        if (ordered)
        {
           return ((Markable)orderedSet.get(0)).equals((Markable)markable);
        }
        else
        {
            System.err.println("startsWith undefined for unordered MarkableSet!");
            return false;
        }
    }

    public final Markable getInitialMarkable()
    {
        Markable result = null;
        if (ordered)
        {
            result =  (Markable)orderedSet.get(0);
        }
        return result;
    }
        
    public final Markable[] getOrderedMarkables()
    {
        ArrayList<Markable> temp = new ArrayList<Markable>(orderedSet);
        return (Markable[]) temp.toArray(new Markable[0]);   
    }
    
    public final int getMarkableIndex(Markable _markable)
    {
        int index = -1;
        if (ordered)
        {
            for (int z=0;z<orderedSet.size();z++)
            {
                if (((Markable)orderedSet.get(z))==_markable)
                {
                    index = z;
                    break;
                }
            }
        }
        else
        {
            System.err.println("Error: getMarkableIndex is undefined for unordered MarkableSets!");
        }
        return index;
    }

    
    
    
    
    
    
    
    public final boolean matches(int requiredSize, ArrayList startsWithMarkables)
    {
        // This way, result will be true of both attributes are not set
        boolean result = true;
        boolean sizeMatch = false;
        boolean startsWithMatch = false;
        
        if (requiredSize!=-1)
        {
            if (ordered)
            {
                sizeMatch = (requiredSize == orderedSet.size());
            }
            else
            {
                sizeMatch = (requiredSize == unorderedSet.size());
            }
        }
        
        if (startsWithMarkables != null)
        {
            startsWithMatch = startsWithMarkables.contains((Markable)orderedSet.get(0));
        }
        
        
        if (requiredSize!=-1 && startsWithMarkables!=null)
        {
            // Both criteria were specified and must therefore match
            if (sizeMatch && startsWithMatch)
            {
                result=true;
            }
            else
            {
                result = false;
            }
        }
        else if (requiredSize!=-1)
        {
            // Only the size criterion was specified
            if (sizeMatch) 
            {
                result = true;
            }
            else
            {
                result = false;
            }
        }
        else if (startsWithMarkables!=null)
        {
            // Only the startsWith criterion was specified
            if (startsWithMatch) 
            {
                result = true;
            }
            else
            {
                result = false;
            }
        }
                    
        return result;
    }
    
    public final ArrayList getVilainRepresentation()
    {
        ArrayList result = new ArrayList();
        if (ordered)
        {
           for (int z=0;z<orderedSet.size()-1;z++)
           {
               String entry = MarkableHelper.getSpan((Markable)orderedSet.get(z))+" "+MarkableHelper.getSpan((Markable)orderedSet.get(z+1));
               result.add(entry);
           }
        }
        else
        {
            Markable[] temp = (Markable[])unorderedSet.toArray(new Markable[1]);
            for (int z=0;z<temp.length-1;z++)
            {
                //String entry = MarkableHelper.getSpan((Markable)unorderedSet.get(z))+" "+MarkableHelper.getSpan((Markable)unorderedSet.get(z+1));
                String entry = MarkableHelper.getSpan(temp[z])+" "+MarkableHelper.getSpan(temp[z+1]);
                result.add(entry);
            }
        }
        
        return result;
    }

    public final ArrayList getIDVilainRepresentation()
    {
        ArrayList result = new ArrayList();
        if (ordered)
        {
           for (int z=0;z<orderedSet.size()-1;z++)
           {
               String entry = ((Markable)orderedSet.get(z)).getID()+" "+((Markable)orderedSet.get(z+1)).getID();
               result.add(entry);
           }
        }
        else
        {
            Markable[] temp = (Markable[])unorderedSet.toArray(new Markable[1]);
            for (int z=0;z<temp.length-1;z++)
            {
                String entry = ((Markable)orderedSet.get(z)).getID()+" "+((Markable)orderedSet.get(z+1)).getID();
                result.add(entry);
            }
        }
        
        return result;
    }
    
    public final HashSet getSetOfStrings()
    {
        HashSet result = new HashSet();
        if (ordered)
        {
            // Iterate over markables that are ordered in disc order
           for (int z=0;z<orderedSet.size();z++)
           {
               // Add each markable's span to result set
               result.add(MarkableHelper.getSpan((Markable)orderedSet.get(z)));
           }
        }
        else
        {          
            Markable[] temp = (Markable[])unorderedSet.toArray(new Markable[1]);
            for (int z=0;z<temp.length;z++)
            {
                result.add(MarkableHelper.getSpan(temp[z]));
            }
        }
        return result;
    }
    
    public final int getSize()
    {
        return size;
    }  
    
    public final String toString()
    {
        return getInitialMarkable().toString().toUpperCase()+" ["+size+" elements]";
    }
    
    public final void removeMeFromMarkableRelation()
    {
        markableRelation.removeMarkableSet(this);
        size=0;
    }
        
    
    public void setFlagLevel(int _level) 
    {

    }
    
}
