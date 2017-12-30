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

package org.eml.MMAX2.gui.document;

import java.awt.Color;
import java.awt.Font;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.core.MMAX2;

public class MMAX2Document extends DefaultStyledDocument
{        
    private DefaultDocumentEvent uncommittedChanges = null;
    
    private SimpleAttributeSet currentAttributes = null;        
    
    private MMAX2 mmax2 = null;
    
    /** Creates new MMAX2Document */
    public MMAX2Document(String currentDisplayFontName, int currentDisplayFontSize) 
    {       
        super();               
        currentAttributes = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(currentAttributes, (float)1.5);
        setDisplayFontName(currentDisplayFontName);
        setDisplayFontSize(currentDisplayFontSize);        
    }  

    protected void finalize()
    {
//        System.err.println("MMAXDocument is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }

    
    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;     
    }
                
    public final String getDisplayFontName()
    {
        return StyleConstants.getFontFamily(currentAttributes);
    }
    
    public final void setDisplayFontName(String name)
    {
        StyleConstants.setFontFamily(currentAttributes,name);   
        mmax2.markableSelectorFont = new Font(name, Font.PLAIN,11);
    }
    
    public final void setDisplayFontSize(int size)
    {
        StyleConstants.setFontSize(currentAttributes,size);   
    }
        
    
    public final void setSize(int size)
    {
        flush();
        StyleConstants.setFontSize(this.currentAttributes,size);
    }
        
    public final void setBold(boolean status)// int documentPosition)
    {
        flush();
        StyleConstants.setBold(currentAttributes,status);
    }
    
    public final void setUnderline(boolean status)
    {
        flush();
        StyleConstants.setUnderline(currentAttributes,status);        
    }

    public final void setItalic(boolean status)
    {
        flush();
        StyleConstants.setItalic(currentAttributes,status);        
    }

    public final void setSubscript(boolean status)
    {
        flush();
        StyleConstants.setSubscript(currentAttributes,status);        
    }

    public final void setSuperscript(boolean status)
    {
        flush();
        StyleConstants.setSuperscript(currentAttributes,status);
    }
           
    public final void setStrikeThrough(boolean status)
    {
        flush();
        StyleConstants.setStrikeThrough(currentAttributes,status);                
    }
      
    public final void setColor(Color color, boolean status)
    {
        flush();
        if (status)
        {          
            /** color is to be activated. So set it from current position on. */
            StyleConstants.setForeground(currentAttributes, color);
            /** And put it on colorStack. */
        }
    }

    public final void setDefaultColor()
    {
        
        flush();
        // color is to be activated. So set it from current position on. 
        StyleConstants.setForeground(currentAttributes, Color.black);        
    }
    
    
    public final void dump()
    {
        try
        {
            System.out.println(getText(0,getLength()));
        }
        catch (javax.swing.text.BadLocationException ex)
        {
            
        }
    }
    
    public void flush()
    {                                                
        int currentLen = getLength();
        String toAppend = mmax2.getCurrentDiscourse().getNextDocumentChunk();
        
        if (toAppend.equals("")==false)
        {                       
            // Make all attribute sets distinct, so distinct elements are forced            
            currentAttributes.addAttribute("t",new String (currentLen+""));            
            try
            {
                insertString(getLength(),toAppend, currentAttributes);
            }
            catch (javax.swing.text.BadLocationException ex)
            {
                ex.printStackTrace();
            }            
        }
    }
            
    /** OK */
    public final void bulkApplyStyleToDisplaySpanBackground(int firstDisplayPositionToChange, int sizeOfChange, AttributeSet s)//, boolean replaceBackground) 
    {
        // Used for: setting / removing dragging selection and
        //           RENDER_IN_SET, RENDER_IN_SEARCHRESULT, RENDER_SELECTED, RENDER_UNSELECTED
        
        if (s.isDefined(StyleConstants.Background)==false)
        {
            System.err.println("Warning: No Background color defined in attributes for 'bulkApplyStyleToDisplaySpanBackground'!");            
        }
        
        int offset =0;            
        int start = 0;
        int len =0 ;
        Element run = null;
        MutableAttributeSet attr = null;
                        
        for (int r=0;r<sizeOfChange;r++)
        {                   
            /* Get Start position of current fragment */
            offset = firstDisplayPositionToChange+r;
            run = getCharacterElement(offset);
            if (mmax2.getUseFancyMultilineRendering())
            {
                if (mmax2.getCurrentDiscourse().getDiscoursePositionAtDisplayPosition(offset)==-1)
                {                
                    if (mmax2.getCurrentDiscourse().getMarkableAtDisplayAssociation(offset)==null)
                    {
                        continue;
                    }
                }
            }

            start  = run.getStartOffset();
            len = run.getEndOffset()-start;
            r = r + (len-1);            
            try
            {
                attr = (MutableAttributeSet) run.getAttributes();
                
                StyleConstants.setBackground((MutableAttributeSet)attr,StyleConstants.getBackground(s));
            }
            catch (java.lang.Error ex)
            {
                ex.printStackTrace();
            }
            offset = 0;
            run = null;
            attr = null;
        }// next r                        
    }// method           

    /** OK */        
    public final void bulkApplyStyleToMarkableHandle(int characterPosition, AttributeSet s, boolean replace)
    {
        Element run = null;
        MutableAttributeSet attr = null;                               
        run = getCharacterElement(characterPosition); 
        attr = (MutableAttributeSet) run.getAttributes();            
        if (replace) attr.removeAttributes(attr);
        attr.addAttributes(s);                
    }
    
    /** OK */
    public final void bulkApplyStyleToDiscourseElement(int discourseElementStartPosition, AttributeSet s, boolean replace) 
    {        
    		try 
        	{
            	Element run = null;
            	MutableAttributeSet attr = null;
                               
            	run = getCharacterElement(discourseElementStartPosition);           
            	try
            	{
            		attr = (MutableAttributeSet) run.getAttributes();
            		if (replace) attr.removeAttributes(attr);	                               
            		attr.addAttributes(s);                
            	}
            	catch (java.lang.Error ex)
            	{
            		ex.printStackTrace();
            	}                                
        	}// try  
        	catch (java.lang.Exception ex)
        	{
        		ex.printStackTrace();
        	}
    }           
    
    
    public final void startChanges(Markable concerned)
    {
        if (concerned==null) return;
        int start = 0;
        int end = 0;
        start = concerned.getLeftmostDisplayPosition();
        end = concerned.getRightmostDisplayPosition();        
        startChanges(start,(end-start)+1);
        
    }
    
    public final void startChanges(Markable[] concerned)
    {
        int start = this.getLength();
        int end = 0;
        int currentLeft=0;
        int currentRight=0;
        for (int i=0;i<concerned.length;i++)
        {
            if (concerned[i]==null) continue;
            currentLeft = ((Markable)concerned[i]).getLeftmostDisplayPosition();
            if (currentLeft < start) start = currentLeft;
            currentRight = ((Markable)concerned[i]).getRightmostDisplayPosition();
            if (currentRight > end) end = currentRight;            
        }        
        startChanges(start,(end-start)+1);
    }
    
    public final void startChanges(int start, int length)
    {      	
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

    	if (verbose) System.err.print("Starting changes ... ");
        if (uncommittedChanges != null)
        {
            commitChanges();
        }
        writeLock();
        uncommittedChanges = new DefaultDocumentEvent(start, length, DocumentEvent.EventType.CHANGE);
    	if (verbose) System.err.println("done!");
    }

    public final void commitChanges()
    {
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}

    	if (verbose) System.err.println("Committing changes ... ");
    	
        if (uncommittedChanges == null)
        {
            return;
        }
        uncommittedChanges.end();            
        try
        {
            fireChangedUpdate(uncommittedChanges);
        }
        catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {

        }        
        uncommittedChanges = null;
        writeUnlock();
        if (verbose) System.err.println("done!");
    }   
}
