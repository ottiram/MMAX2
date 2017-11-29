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

package org.eml.MMAX2.annotation.markables;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.eml.MMAX2.api.MarkablePointerAPI;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;

public class MarkablePointer implements Renderable, MarkablePointerAPI
{
    private Markable sourceMarkable;
    private ArrayList targetMarkables;
    private int lineWidth;
    private int size=0;
    private int leftMostPosition;
    private int rightMostPosition;
    private int X_origin=0;
    private int Y_origin=0;
    private int[] X_points=null;
    private int[] Y_points=null;
    private MarkableRelation markableRelation=null;        
    private int maxSize=-1;
    
    private boolean opaque = false;
    private boolean dashed=false;
    
    private boolean permanent = false;
    private int flagDisplayLevel=0;
    private float[] dash1;
    private float[] dash2;
    
    /** Creates new MarkablePointer */
    public MarkablePointer(Markable _sourceMarkable, int _lineWidth, Color _color, int _lineStyle, int _maxSize, MarkableRelation _relation, boolean _dashed) 
    {
        dash1 = new float[2];
        dash1[0] =10;
        dash1[1] =10;

        dash2 = new float[2];
        dash2[0] =4;
        dash2[1] =4;
        
        markableRelation = _relation;
        sourceMarkable = _sourceMarkable;
        lineWidth = _lineWidth;
        dashed = _dashed;
        leftMostPosition = sourceMarkable.getLeftmostDisplayPosition();
        rightMostPosition = sourceMarkable.getRightmostDisplayPosition();        
        targetMarkables = new ArrayList();                
        maxSize = _maxSize;
    }

    public final void setIsPermanent(boolean _permanent)
    {
        permanent = _permanent;
    }
    
    public final boolean getIsPermanent()
    {
        return permanent;
    }
    
    public final boolean hasMaxSize()
    {
        if (maxSize==-1)
        {
            return false;
        }
        else
        {
            return size==maxSize;
        }
    }
    
    public final String getTargetSpan()
    {
        String span = "";        
        Markable currentTarget = null;
        if (targetMarkables.size()==1)
        {
            currentTarget = ((Markable)targetMarkables.get(0));
            // If source and this target are from the same level, use only id
            if (currentTarget.getMarkableLevelName().equals(getSourceMarkable().getMarkableLevelName()))
            {
                span = currentTarget.getID();
            }
            else
            {
                // Prepend level name to target markable id
                span=currentTarget.getMarkableLevelName()+":"+currentTarget.getID();
            }
        }
        else if (targetMarkables.size()>1)
        {
            // Iterate over all target in this pointer set
            for (int z=0;z<targetMarkables.size();z++)
            {
                // Get current target
                currentTarget = ((Markable)targetMarkables.get(z));
                if (z==0)
                {
                    // If source and this target are from the ame level, use only id
                    if (currentTarget.getMarkableLevelName().equals(getSourceMarkable().getMarkableLevelName()))
                    {
                        span = currentTarget.getID();
                    }
                    else
                    {
                        // Prepend level name to target markable id
                        span=currentTarget.getMarkableLevelName()+":"+currentTarget.getID();
                    }
                }
                else
                {
                    // If source and this target are from the ame level, use only id
                    if (currentTarget.getMarkableLevelName().equals(getSourceMarkable().getMarkableLevelName()))
                    {
                        span = span+";"+currentTarget.getID();
                    }
                    else
                    {
                        // Prepend level name to target markable id
                        span=span+";"+currentTarget.getMarkableLevelName()+":"+currentTarget.getID();
                    }
                }
            }
        }        
        return span;
    }
    
    public final void setOpaque(boolean status)
    {
        this.opaque = status;
    }
    
    public final boolean isOpaque()
    {
        return opaque;
    }
       

    public final int getSize()
    {
        return size;
    }  

    
    ///

    public final MarkableRelation getMarkableRelation()
    {
        return markableRelation;
    }
        
    public final Markable[] getTargetMarkables()
    {
    	return (Markable[]) targetMarkables.toArray(new Markable[0]);
    }
    
    public final Markable getSourceMarkable()
    {
        return sourceMarkable;
    }
    
    public final boolean isSourceMarkable(Markable potentialSourceMarkable)
    {
        return sourceMarkable==potentialSourceMarkable;
    }
    
    public final boolean isTargetMarkable(Markable potentialTargetMarkable)
    {
        return targetMarkables.contains(potentialTargetMarkable);
    }

    public boolean containsMarkable(Markable markable) 
    {
        boolean result = false;
        if (markable == this.sourceMarkable)
        {
            result = true;
        }
        else if (this.targetMarkables.contains(markable))
        {
            result=true;
        }
        return result;
    }


    
    
    
    
    
    public final void removeTargetMarkable(Markable removee)
    {
        targetMarkables.remove(removee);
        size--;
    }
    
    
    
    public final void removeMeFromMarkableRelation()
    {
        markableRelation.removeMarkablePointer(this);
        size=0;
    }
     
    
    public final String toString()
    {
        if (size == 1)
        {
            return getSourceMarkable().toString()+" ["+size+" target]";
        }
        else
        {
            return getSourceMarkable().toString()+" ["+size+" targets]";
        }
    }
    
    
    public void addTargetMarkable(Markable _markable)
    {
        if (_markable.getLeftmostDisplayPosition() < this.leftMostPosition)
        {
            this.leftMostPosition = _markable.getLeftmostDisplayPosition();
        }
        if (_markable.getRightmostDisplayPosition() > this.rightMostPosition)
        {
            this.rightMostPosition = _markable.getRightmostDisplayPosition();
        }
        
        if (targetMarkables.contains(_markable)==false)
        {
            targetMarkables.add(_markable);
            size++;
        }        
    }
    
    public void unselect(MMAX2Document doc) 
    {
        doc.startChanges(leftMostPosition, (rightMostPosition-leftMostPosition)+1);
        if (size > 0)
        {   
            if (permanent)
            {
                // If this is rendered because it is permanent, draw source also, unless it is main
                {
                    getSourceMarkable().renderMe(MMAX2Constants.RENDER_UNSELECTED);
                }
            }                
            
            // Iterate over entire set
            for (int z=0;z<size;z++)
            {
                // Reset each Markable individually
                ((Markable)targetMarkables.get(z)).renderMe(MMAX2Constants.RENDER_UNSELECTED);
            }
        }        
        X_points = null;
        Y_points = null;
        doc.commitChanges();
        
    }
    
    public void refresh(Graphics2D graphics) 
    {
        if (size > 0)
        {            
            drawSet(graphics);            
        }        
    }

    
    
    /** This method is called ONCE when this markable_pointer is to be rendered initially. It renders both the 
        individual set member Markables and the lines between them (if any.) */
    public final void select(Graphics2D graphics, MMAX2Document doc, Markable currentlySelectedMarkable)
    {        
        Markable temp = null;
        // Make sure the line points are up-to-date
        updateLinePoints();
        doc.startChanges(leftMostPosition, (rightMostPosition-leftMostPosition)+1);
        if (size > 0)
        {          
            if (permanent)
            {
                // If this is rendered because it is permanent, draw source also, unless it is main
                temp = getSourceMarkable();
                if (temp.equals(currentlySelectedMarkable)==false)
                {
                    temp.renderMe(MMAX2Constants.RENDER_IN_SET);
                }
            }
            // Iterate over entire set
            for (int z=0;z<size;z++)
            {
                // Render Markable itself, unless it is the currently selected one, which is expected to be highlighted already
                // Get current set member
                temp = ((Markable)targetMarkables.get(z));
                if (temp.equals(currentlySelectedMarkable)==false)
                {
                    temp.renderMe(MMAX2Constants.RENDER_IN_SET);
                }
            }
            // Draw entire line at once; points have been set/updated by call to updateLinePoints() 
            drawSet(graphics);
        }
        doc.commitChanges();
    }
    
    public final void updateLinePoints()
    {
        Point currentPoint = sourceMarkable.getPoint();
        X_origin = (int)currentPoint.getX();
        Y_origin = (int)currentPoint.getY();
     
        currentPoint=null;
        
        X_points = new int[size];
        Y_points = new int[size];
        // Iterate over all Markables in this set
        for (int z=0;z<size;z++)
        {
            // Get and store points of line
            currentPoint = ((Markable)targetMarkables.get(z)).getPoint();
            X_points[z] = (int)currentPoint.getX();
            Y_points[z] = (int)currentPoint.getY();
        }        
    }
    
    
    private final void drawSet(Graphics2D graphics)
    {        
        QuadCurve2D.Double c = null;
        Point ctrlPoint = null;
        // Iterate over entire set
        //for (int z=0;z<size;z++)
        for (int z=size-1;z>=0;z--)
        {
            // Set color as defined in scheme
            graphics.setColor(markableRelation.getLineColor());
            // Set stroke
            if (dashed==false)
            {
                graphics.setStroke(new BasicStroke(lineWidth));
            }
            else
            {
                graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER,10,dash1,0));            
            }
            
            if (markableRelation.getLineStyle() == MMAX2Constants.STRAIGHT)
            {
                graphics.drawLine(X_origin,Y_origin,X_points[z],Y_points[z]);
            }
            else
            {
                // Default: non-smart
                boolean smart=false;
                if (markableRelation.getLineStyle()==MMAX2Constants.SMARTCURVE)
                {
                    smart=true;
                    if (X_origin < X_points[z])
                    {
                       //lineStyle=MMAX2.LCURVE;
                        markableRelation.setLineStyle(new Integer(MMAX2Constants.LCURVE));
                    }
                    else
                    {
                        //lineStyle=MMAX2.RCURVE;
                        markableRelation.setLineStyle(new Integer(MMAX2Constants.RCURVE));
                    }
                }
                c = new QuadCurve2D.Double();
                ctrlPoint = MMAX2Utils.calculateControlPoint(X_origin,Y_origin,X_points[z],Y_points[z], markableRelation.getLineStyle());
                c.setCurve((double)X_origin,(double)Y_origin,(double)ctrlPoint.getX(),(double)ctrlPoint.getY(),(double)X_points[z],(double)Y_points[z]);
                if (smart)
                {
                    graphics.fillOval(X_origin-4, Y_origin-4,8,8);                    
                }
                // Draw pointer
                graphics.draw(c);
                
                if (smart)
                {
                    // Default: flag to right
                    boolean flagToRight = true;
                    // Todo: Implement check for flag flip
                    // Get font metrics
                    FontMetrics m = graphics.getFontMetrics();                    
                    
                    String toDisplay = getMarkableRelation().getAttributeName();
                    String attributeToDisplay = getMarkableRelation().getAttributeNameToShowInFlag();
                    if (attributeToDisplay.equals("")==false)
                    {
                       // The value of some attribute is to be displayed in the flag
                        Markable currentTarget = (Markable) targetMarkables.get(z);
                        toDisplay = toDisplay+" "+attributeToDisplay+"="+currentTarget.getAttributeValue(attributeToDisplay,"");
                    }                    
                    
                    // Get rect containing the string to draw
                    Rectangle2D rect = m.getStringBounds(toDisplay,graphics);
                    // Get const for flag X level (set in renderer)
                    int flagXLevel = (flagDisplayLevel+1+z)*25;
                    
                    if (((int)rect.getWidth())+flagXLevel+X_points[z]+6 >= sourceMarkable.getMarkableLevel().getCurrentDiscourse().getMMAX2().getCurrentTextPane().getWidth())
                    {
                        flagToRight = false;
                    }
                    
                    // The attribute name is to be shown as a flag
                    // Set color to black for flag
                    graphics.setColor(Color.black);
                    // Set stroke to massive
                    graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));
                    if (flagToRight)
                    {
                        // Draw flag line from start point to right top
                        graphics.drawLine(X_points[z],Y_points[z],X_points[z]+flagXLevel,Y_points[z]-flagXLevel);
                    }
                    else
                    {
                        // Draw flag line from start point to left top
                        graphics.drawLine(X_points[z],Y_points[z],X_points[z]-flagXLevel,Y_points[z]-flagXLevel);
                    }
                    // Set font for flag
                    graphics.setFont(graphics.getFont().deriveFont(java.awt.Font.BOLD,11));                    
                    graphics.setColor(Color.white);                    
                    
                    int flagXOrigin=0;
                    int flagYOrigin=0;
                    
                    int flagWidth=(int)rect.getWidth()+6;
                    int flagHeight=(int)rect.getHeight()+6;

                    if (flagToRight)
                    {
                        flagXOrigin=(int)X_points[z]+flagXLevel;
                        flagYOrigin=(int)Y_points[z]-flagXLevel-(int)rect.getHeight()-6;
                    }
                    else
                    {
                        flagXOrigin=(int)X_points[z]-flagWidth-flagXLevel;
                        flagYOrigin=(int)Y_points[z]-flagXLevel-(int)rect.getHeight()-6;                        
                    }
                    graphics.fillRect(flagXOrigin, flagYOrigin, flagWidth, flagHeight);                    
                    //graphics.fillRect(X_points[z]+flagXLevel,(int)Y_points[z]-flagXLevel-(int)rect.getHeight()-6,(int)rect.getWidth()+6,(int)rect.getHeight()+6);
                    
                    graphics.setColor(Color.black);
                    //graphics.drawRect(X_points[z]+flagXLevel,(int)Y_points[z]-flagXLevel-(int)rect.getHeight()-6,(int)rect.getWidth()+6,(int)rect.getHeight()+6);
                    graphics.drawRect(flagXOrigin, flagYOrigin, flagWidth, flagHeight);
                    if (flagToRight)
                    {
                        graphics.drawString(toDisplay,X_points[z]+flagXLevel+3,Y_points[z]-(flagXLevel+6));
                    }
                    else
                    {
                        graphics.drawString(toDisplay,X_points[z]-flagXLevel-flagWidth+3,Y_points[z]-(flagXLevel+6));
                    }
                    //lineStyle=MMAX2.SMARTCURVE;
                    markableRelation.setLineStyle(new Integer(MMAX2Constants.SMARTCURVE));
                }
            }
        }
    }
        
    public void setFlagLevel(int _level) 
    {
        flagDisplayLevel = _level;
    }
    
}
