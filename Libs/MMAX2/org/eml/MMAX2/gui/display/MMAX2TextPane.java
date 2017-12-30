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

package org.eml.MMAX2.gui.display;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.Timer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class MMAX2TextPane extends JTextPane implements AdjustmentListener, KeyListener
{                                
    private boolean isDraggingGoingOn=false;
    
    private boolean CONTROL_DOWN=false;
    private boolean mouseInPane = true;
    
    private Timer refreshControlTimer = null;
    // Milliseconds before refreshTimer is stopped after the last scrolling ocurred
    private int TIME_TO_REFRESH_AFTER_LAST_SCROLLING = 1000;
    private Timer refreshTimer = null;
    // Milliseconds between separate refreshTimer firings
    private int TIME_BETWEEN_REFRESHES = 10;        
    
    private Timer activateHoveringLatencyTimer = null;
    private int HOVERING_LATENCY_TIME = 100;
    
    private MMAX2CaretListener currentCaretListener = null;
    private MMAX2MouseListener currentMouseListener = null;
    private MMAX2MouseMotionListener currentMouseMotionListener = null;
    
    private MMAX2 mmax2 = null;

    private MouseEvent currentMouseMoveEvent = null;
    
    private Markable currentHoveree = null;
    
    private boolean showHandlesOfCurrentFragmentOnly = false;
    private boolean showFloatingAttributeWindow = false;
    
    private int currentDot = 0;
    
    private JPopupMenu floatingAttributeWindow = null;
    private JPopupMenu markableSetPeerWindow = null;

    public MMAX2TextPane()
    {
        super();
        
        currentMouseListener = new MMAX2MouseListener();
        addMouseListener(currentMouseListener);     

        currentMouseMotionListener = new MMAX2MouseMotionListener();
        this.addMouseMotionListener(currentMouseMotionListener);     

        currentCaretListener = new MMAX2CaretListener();
        addCaretListener(currentCaretListener);
        
        setCaret(new MMAX2Caret());
        // Prevent text modifications
        setEditable(false);        
        
        // Make sure graphics are optimized
        setDoubleBuffered(true);
        // Init refreshControlTimer (0 is a placeholder, overridden by TIME_TO_REFRESH_AFTER_LAST_SCROLLING later)
        refreshControlTimer = new Timer(0 , new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                stopAutoRefresh();
            }                
        });
            
        refreshControlTimer.setCoalesce(true);
        // Make refreshControlTimer fire only once
        refreshControlTimer.setRepeats(false);
        // Set time before first (and only) firing
        refreshControlTimer.setInitialDelay(TIME_TO_REFRESH_AFTER_LAST_SCROLLING);
            
        refreshTimer = new Timer(TIME_BETWEEN_REFRESHES, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    mmax2.redraw(null);
                }
                catch (java.lang.NullPointerException ex)
                {
                    //
                }                
            }                
        });
        refreshTimer.setCoalesce(true);
        refreshTimer.setInitialDelay(0);
        // Make refreshTimer fire several times
        refreshTimer.setRepeats(true);       
        this.activateHoveringLatencyTimer = new Timer(0, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                startHovering();
            }                                        
        });                
        activateHoveringLatencyTimer.setInitialDelay(HOVERING_LATENCY_TIME);
    
        addKeyListener(this);  
               
    }// end constructor
    
    
    public final void setIsDraggingGoingOn(boolean status)
    {
        isDraggingGoingOn = status;
    }
    public final boolean getIsDraggingGoingOn()
    {
        return isDraggingGoingOn;
    }
    
    public final boolean getIsControlDown()
    {
        return CONTROL_DOWN;
    }
    
    public boolean print()
    {                
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();       
        
        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(null, "svg", null);

        SVGGeneratorContext context = SVGGeneratorContext.createDefault(document);
        context.setEmbeddedFontsOn(true);
        // Create an instance of the SVGGenerator using the document
        SVGGraphics2D svgGenerator = new SVGGraphics2D(context,true);       
        // Cause this component to render itself in svgGenerator        
        paint(svgGenerator);
        // Cause mmax2 to redraw lines in svgGenerator
        mmax2.redraw(svgGenerator);               
              
        //BufferedWriter writer = null;
        Writer out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("image.svg"),"UTF-8"));
        }
        catch (java.io.IOException ex)
        {
            ex.printStackTrace();
        }
        
        try
        {
            svgGenerator.stream(out, false);        
        }
        catch (org.apache.batik.svggen.SVGGraphics2DIOException ex)        
        {
            
        }         
        
        try
        {
            out.flush();
            out.close();
        }
        catch (java.io.IOException ex)
        {
            
        }       
        return true;
    }
    
    public final void setMouseInPane(boolean in)
    {
        mouseInPane = in;
    }
    
    protected void finalize()
    {
        System.err.println("TextPane is being finalized!");        
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable ex)
        {
            ex.printStackTrace();
        }        
    }
            
    protected final MMAX2CaretListener getCurrentCaretListener()
    {
        return currentCaretListener;
    }
    
    protected final void setCurrentMouseMoveEvent(MouseEvent _me)
    {
        currentMouseMoveEvent = _me;
    }
    
    public final MouseEvent getCurrentMouseMoveEvent()
    {
        return currentMouseMoveEvent;
    }
    
    public final void setMMAX2(MMAX2 _mmax2)
    {
        mmax2 = _mmax2;
        currentCaretListener.setMMAX2(_mmax2);
        currentMouseListener.setMMAX2(_mmax2);
        currentMouseMotionListener.setMMAX2(_mmax2);
    }

    public final void deactivateFloatingAttributeWindow()
    {
        if (floatingAttributeWindow != null)
        {
            floatingAttributeWindow.setVisible(false);
            mmax2.redraw(null);
        }
        floatingAttributeWindow = null;
    }
    
    protected final void activateFloatingAttributeWindow()
    {
        floatingAttributeWindow = new JPopupMenu();
        floatingAttributeWindow.setEnabled(false);
        JMenuItem item = null;
           
        if (currentHoveree != null)
        {            
            if (showFloatingAttributeWindow)
            {
                Markable markable = getCurrentHoveree();
                item = new JMenuItem("ID"+" : "+markable.getID());
                item.setFont(MMAX2.getStandardFont());
                floatingAttributeWindow.add(item);
                item = null;
                
                floatingAttributeWindow.addSeparator();
                HashMap atts = markable.getAttributes();
                Iterator enum2 = atts.keySet().iterator();
                while (enum2.hasNext())
                {
                    String att = (String) enum2.next();
                    String val = (String) atts.get(att);
                    item = new JMenuItem(att+" : "+val);
                    item.setFont(MMAX2.getStandardFont());
                    floatingAttributeWindow.add(item);
                    item = null;                    
                }                
                floatingAttributeWindow.show(this,this.currentMouseMoveEvent.getX(), this.currentMouseMoveEvent.getY());
            }
        }
    }

    public final Markable getCurrentHoveree()
    {
        return currentHoveree;       
    }
    
    
    public final int getCurrentDot()
    {
        return currentDot;
    }
    
    /** This method is called when the hovering Markable changes. */
    public final void setCurrentHoveree(Markable _hoveree, int _currentDot)
    {
        // Save dot position (needed for discontinuous markables) 
        currentDot = _currentDot;        
                
        if (_hoveree == null)
        {
            // The method was called with a null Markable, so any hoovering-related stuff has to be removed.
            if (currentHoveree == null)
            {
                // There was not any hovering going on, so nothing to do                
                return;
            }
            else
            {
                // There was some hovering going on, so reset first, but do not force it
                deactivateMarkableHandleHighlight(false);
                deactivateMarkableSetPeerWindow();
                // Then set currentHoveree to null. This indicates that no hovering is currently going on
                currentHoveree = null;
            }
            deactivateMarkableSetPeerWindow();                           
        }
        else
        {
            // The method was called with a valid hoveree markable
            currentHoveree = _hoveree;
            if (currentHoveree.isDiscontinuous())
            {
                mmax2.setStatusBar("Press 'i' to inspect attributes, 'f' to highlight current fragment only"); 
            }
            else
            {
                mmax2.setStatusBar("Press 'i' to inspect attributes");
            }
            activateMarkableHandleHighlight();
            if (mmax2.getRenderingListSize() != 0)
            {
                activateMarkableSetPeerWindow();
            }
            activateFloatingAttributeWindow();            
        }        
    }

    private final void deactivateMarkableSetPeerWindow()
    {
        if (markableSetPeerWindow != null)
        {
            markableSetPeerWindow.setVisible(false);
            markableSetPeerWindow = null;
        }
    }

    private final void activateMarkableSetPeerWindow()
    {
        if (mmax2.getShowMarkableSetPeerWindow()==false) return;
        JMenuItem item = null;
        MarkableSet current = (MarkableSet)mmax2.getCurrentlyRenderedMarkableRelationContaining(currentHoveree);
        if (current != null)
        {
            ArrayList content =  (ArrayList)java.util.Arrays.asList(current.getOrderedMarkables());
            markableSetPeerWindow = new JPopupMenu();
            for (int z=0;z<content.size();z++)
            {
                if (((Markable)content.get(z))!= currentHoveree)
                {
                    item = new JMenuItem(((Markable)content.get(z)).toString());
                }
                else
                {
                    item = new JMenuItem("--> "+((Markable)content.get(z)).toString());
                }
                item.setFont(MMAX2.getStandardFont().deriveFont(8));
                markableSetPeerWindow.add(item);
                item = null;
            }
            markableSetPeerWindow.show(this,currentMouseMoveEvent.getX(),currentMouseMoveEvent.getY());
        }
    }
    private final void activateMarkableHandleHighlight()
    {
        if (mmax2.getHighlightMatchingHandles() && mouseInPane && (mmax2.getIsRendering()==false || mmax2.getSuppressHandlesWhenRendering()==false))
        {
            if (showHandlesOfCurrentFragmentOnly)
            {
                currentHoveree.renderMe(MMAX2Constants.RENDER_CURRENT_HANDLE);
            }
            else
            {
                currentHoveree.renderMe(MMAX2Constants.RENDER_ALL_HANDLES);
            }
        }
    }
    
    public final void deactivateMarkableHandleHighlight(boolean force)
    {
        if (mmax2.getHighlightMatchingHandles() && (mmax2.getIsRendering()==false || force || mmax2.getSuppressHandlesWhenRendering()==false))
        {
            if (currentHoveree!=null)
            {
                currentHoveree.renderMe(MMAX2Constants.RENDER_NO_HANDLES);
                mmax2.redraw(null);
            }
        }
    }
    

    /** This method is triggered automatically by activateHoveringLatencyTimer. */
    private final void startHovering()
    {
        /** Set CaretListener in hoovering mode */
        currentCaretListener.setUpdateMode(MMAX2Constants.MOUSE_HOVERED);
        /** Create positionCaretEvent using the last stored Mouse Event. */
        ((MMAX2Caret)this.getCaret()).positionCaret(currentMouseMoveEvent);
        activateHoveringLatencyTimer.stop();
    }
    
    /** Called from MMAX2MouseMotionListener upon MouseMotion detection. */
    protected final Timer getHoveringLatencyTimer()
    {
        return activateHoveringLatencyTimer;
    }
    
    public final void setShowFloatingAttributeWindow(boolean status)
    {
        showFloatingAttributeWindow = status;
    }
    
    public final void setShowHandlesOfCurrentFragmentOnly(boolean status)
    {
        showHandlesOfCurrentFragmentOnly = status;
    }
    
    /** This method starts/restarts the refreshTimer and the refreshControlTimer. It is called automatically upon each 
        adjustmentValueChanged event on */
    public final void startAutoRefresh()
    {
        if (refreshTimer.isRunning()) 
        {
            refreshTimer.restart();
        }
        else
        {
            refreshTimer.start();            
        }
            
        if (refreshControlTimer.isRunning()) 
        {
            refreshControlTimer.restart();
        }
        else
        {
            refreshControlTimer.start();            
        }                        
    }
        
    public final void stopAutoRefresh()
    {
        refreshTimer.stop();
        refreshControlTimer.stop();
    }
        
    // Overridden to implement custom painting
    public final void paintComponent(Graphics gr)
    {
        try
        {
            mmax2.redraw(null);            
        }
        catch (java.lang.NullPointerException ex)
        {
            //
        }
        
        
        try
        {
            super.paintComponent(gr);
        }
        catch (java.lang.ArrayIndexOutOfBoundsException ex)
        {

        }
        catch (java.lang.NullPointerException ex)
        {

        }
        
        try
        {
            mmax2.redraw(null);            
        }
        catch (java.lang.NullPointerException ex)
        {
            //
        }
    }                        
                       
    public final MMAX2MouseMotionListener getCurrentMouseMotionListener()
    {
        return this.currentMouseMotionListener;
    }
    
    // Called when display is scrolled or resized
    public void adjustmentValueChanged(java.awt.event.AdjustmentEvent p1) 
    {            
        // Get origin of this adjustment event
        JScrollBar adjustable = (JScrollBar) p1.getAdjustable();
        if (adjustable.getOrientation()==JScrollBar.HORIZONTAL)
        {
            // If this event was caused by a horizontal resize, the linePoints of MarkableSets currently rendered 
            // must be recalculated
            try
            {
                mmax2.updateRenderingListObjects();
            }
            catch (java.lang.NullPointerException ex)
            {
                //
            }
        }
        
        // Start automatic display refresh
        startAutoRefresh();
        try
        {
            // Put currently visble Viewport in current RepaintManager's dirty list, thus scheduling it for (real) repaint
            mmax2.getCurrentViewport().repaint();
        }
        catch (java.lang.NullPointerException ex)
        {
            //
        }        
        
    }                        
    
    public void setControlIsPressed(boolean state)
    {    
        CONTROL_DOWN=state;
        if (state == true)
        {
            if (mmax2.getIsBasedataEditingEnabled())
            {
                mmax2.setStatusBar("CONTROL");
                setCursor(new Cursor(Cursor.TEXT_CURSOR));
            }
            else
            {
                mmax2.setStatusBar("Base data editing is disabled!");
            }
        }
        else
        {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mmax2.clearStatusBar();
        }
    }
    
    
    public void keyPressed(KeyEvent e)
    {
    	if (e.getModifiers() == KeyEvent.CTRL_MASK)
    	{
        	System.err.println("Control down");
            setControlIsPressed(true);
            if (e.getKeyCode() == KeyEvent.VK_X)
            {
            	mmax2.requestExit();
            }
            
    	}
    }
    
    
    public void keyPressed_bak(KeyEvent e) 
    {    	    	
        int code = e.getKeyCode();       
        if (code == KeyEvent.VK_CONTROL)
        {        	        	
        	System.err.println("Control");
            setControlIsPressed(true);                        
        }     
    }    
    
    public void keyReleased_bak(KeyEvent e) 
    {
        int code = e.getKeyCode();       
        if (code == KeyEvent.VK_CONTROL)
        {
            setControlIsPressed(false);
        }                        
    }

    public void keyReleased(KeyEvent e) 
    {
        //if (e.getModifiers() == KeyEvent.CTRL_MASK)
    	if (CONTROL_DOWN)
        {
        	System.err.println("Control up");
            setControlIsPressed(false);
        }                        
    }
    
    
    public void keyTyped(KeyEvent e) 
    {
    	
    }
        
    class ComponentPrintable implements Printable 
    {
    	private MMAX2TextPane mComponent;

    	public ComponentPrintable(MMAX2TextPane p) 
    	{
    		mComponent = p;
    	}
    
    	public int print(Graphics g, PageFormat pageFormat, int pageIndex) 
    	{
    		if (pageIndex > 0) return NO_SUCH_PAGE;        
    		g.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
    		mComponent.paintComponent(g);
    		return PAGE_EXISTS;
    	}
    }        
}