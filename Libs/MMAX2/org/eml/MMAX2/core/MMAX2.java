/*
 * Copyright 2007 Mark-Christoph Mï¿½ller
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


package org.eml.MMAX2.core;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableHelper;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkableSet;
import org.eml.MMAX2.annotation.markables.Renderable;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseLoader;
import org.eml.MMAX2.gui.display.MMAX2TextPane;
import org.eml.MMAX2.gui.document.MMAX2Document;
import org.eml.MMAX2.gui.sound.Player;
import org.eml.MMAX2.gui.windows.MMAX2AnnotationHintWindow;
import org.eml.MMAX2.gui.windows.MMAX2BatchPluginWindow;
import org.eml.MMAX2.gui.windows.MMAX2MarkableBrowser;
import org.eml.MMAX2.gui.windows.MMAX2MarkablePointerBrowser;
import org.eml.MMAX2.gui.windows.MMAX2MarkableSetBrowser;
import org.eml.MMAX2.gui.windows.MMAX2ProjectWizard;
import org.eml.MMAX2.gui.windows.MMAX2QueryWindow;
import org.eml.MMAX2.gui.windows.MMAXFileFilter;
import org.eml.MMAX2.gui.windows.MarkableLevelControlWindow;
import org.eml.MMAX2.plugin.MMAX2Plugin;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MMAX2 extends javax.swing.JFrame implements KeyListener ,java.awt.event.ComponentListener , java.awt.event.ActionListener
{              		
    private boolean isBatchPluginMode=false;    
        
    private int oneClickAnnotationGroupValue = 0;
    
    private boolean commonPathWasSetViaConsole=false;
    
    private Timer waitCursorTimer = null;
    private Timer autoSaveTimer = null;
    
    private HashMap markableSelectorAttributes = new HashMap();
    private String currentCommonBasedataPath ="";
    private HashSet switchHash = new HashSet();
    private String commonPathsFileName="";
    
    private Markable currentPrimaryMarkable;
    private Markable currentSecondaryMarkable;
    private JCheckBoxMenuItem suppressHandlesWhenRenderingMenuItem = null;
    
    private JMenuItem saveAll = null;
    private JMenuItem saveBasedata = null;
    private JMenuItem exit = null;
    private JCheckBoxMenuItem enableBasedataEditing=null;
    private JCheckBoxMenuItem autoReapplyAfterBasedataEditing=null;
    private JMenu saveLevelMenu = null;
    
    private ButtonGroup autoSaveEveryMenuButtongroup = new ButtonGroup();
    private JMenu autoSaveMenu = null;
    private JMenu autoSaveEveryMenu = null;
    
    private JMenuItem wizard = null;
    private JMenu settingsMenu = null;
    private JMenu displayMenu = null;
    private JMenu toolsMenu = null;
    private JMenu browserMenu = null;
    private JMenu showInPopupMenu = null;
    private JMenu switchesMenu = null;    
        
    private JMenu pluginMenu = null;
    private JMenuItem batchPluginMenuItem = null;
    
    private JMenuItem searchItem = null;
    private JMenuItem markableBrowserItem = null;
    private JMenuItem setBrowserItem = null;
    private JMenuItem pointerBrowserItem = null;
    
    // Controls MarkableLevelControlPanel
    private JCheckBox showMLCPBox = null;
    
    private Box mainBox = null;
    private Box statusBox = null;

    private JPanel statusPanel = null;    
    private JLabel statusBar = null;
        
    private static String versionString = "1.13.003";
    
    // private, because accessed by get/set methods only
    private JScrollPane currentScrollPane = null;
    private MMAX2TextPane currentTextPane = null;    
    private MMAX2Document currentDocument = null;
    private MMAX2Discourse currentDiscourse = null;
    
    private ArrayList markableBrowsers = null;
    private int markableBrowserCount = 0;

    private ArrayList markableSetBrowsers = null;
    private int markableSetBrowserCount = 0;
    
    private ArrayList markablePointerBrowsers = null;
    private int markablePointerBrowserCount = 0;
    
    public static Font standardFont = null;
    public static Font markableSelectorFont = null;
    
    private static boolean useDisplayFontInPopups = false;
    
    public static ButtonGroup fontNameButtonGroup = null;
    public static ButtonGroup fontSizeButtonGroup = null;
    public static ButtonGroup lineSpacingButtonGroup = null;
    public static String currentDisplayFontName = "default";
    public static int currentDisplayFontSize = MMAX2Constants.DEFAULT_FONT_SIZE;
    
    public static String defaultRelationValue="empty";
    
    private int currentKey = 0;    
    private boolean selectFromActiveLevelsOnly=true;
    private boolean autoRefreshUponPanelAction=false; // NEW
    private boolean useFancyLabels=true;
    private boolean highlightMatchingHandles=true;
    // New March 31st, 2006: Group by default
    private boolean groupMarkablesByLevel=false;
    private boolean showMarkableSetPeerWindow = false;
    private boolean createSilently = false;
    private boolean suppressHandlesWhenRendering = true;
    private boolean selectAfterCreation = false;    
    
    // New March 31st, 2006: Use as default
    private boolean useFancyMultilineRendering = true;
    
    private ArrayList markableRelationsToRender;
    public boolean initializing = true;
        
    private Dimension screenSize = null;
    private int screenWidth = 0;
    private int screenHeight = 0;
    
    private boolean isRendering = false;    
    private boolean redrawAllOnNextRefresh = false;
    private boolean ignoreCaretUpdate=false;
    
    private SimpleAttributeSet selectedMarkableGlobalStyle=null;
    private String selectedMarkableGlobalStyleString="background=yellow";
    
    private SimpleAttributeSet selectionSpanGlobalStyle=null;
    private String selectionSpanGlobalStyleString="background=d:204204255";
    
    private MMAX2QueryWindow queryWindow;

    private boolean isAnnotationModified = false;
    private boolean isBasedataModified = false;
    
    public String currentWorkingDirectory="";
    
    private String commonQueryPath="";
    
    private boolean blockAllInput=false;
    
    public JFrame editBasedataWindow=null;
    
    MMAX2AnnotationHintWindow hintWindow = null;
    
    public MMAX2()
    {                       
        setIconImage(Toolkit.getDefaultToolkit().getImage("main.gif"));
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.width;
        screenHeight = (int) screenSize.height;        
        
        addComponentListener(this);
            
        currentScrollPane = new JScrollPane();
        currentScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainBox = Box.createVerticalBox();
        mainBox.add(currentScrollPane);
        statusPanel = new JPanel();
        statusPanel.setOpaque(true);
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusBar = new JLabel("Welcome to "+versionString);
        
        standardFont = statusBar.getFont().deriveFont((float)11.0);
        markableSelectorFont = statusBar.getFont().deriveFont((float)11.0);
        
        statusBar.setForeground(Color.black);      
        statusBox = Box.createHorizontalBox();
        statusBox.add(statusBar);
        
        statusPanel.add(statusBox);
        setContentPane(mainBox);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);             
        addWindowListener(new MMAX2MainWindowListener());
                
        markableRelationsToRender = new ArrayList();       
    }

    
    public final void setUseFancyMultilineRendering(boolean status)
    {
        useFancyMultilineRendering = status;
    }
    
    public final boolean getUseFancyMultilineRendering()
    {
        return useFancyMultilineRendering;
    }
    
    public final void setCommonQueryPath(String path)
    {
        commonQueryPath = path;
    }
    
    public final String getCommonQueryPath()
    {
        return commonQueryPath;
    }
    
    public final void addSaveMarkableLevelItem(JMenuItem item)
    {
        saveLevelMenu.add(item);
    }
    
    public final void unregisterQueryWindow()
    {
        queryWindow = null;
    }
    
    public final void setBlockAllInput(boolean state)
    {
        blockAllInput=state;
    }
    
    public final boolean getBlockAllInput()
    {
        return blockAllInput;
    }
    
    public final void requestReapplyDisplay()
    {
        startWaitCursor();
        getCurrentDiscourse().reapplyStyleSheet();
        stopWaitCursor();
        requestRefreshDisplay();
    }                        
    
    public final void copyDocumentSpanToClipboard(int start, int end)
    {
        String text = "";
        try
        {
            text = getCurrentDocument().getText(start,(end-start)+1);
        }
        catch (javax.swing.text.BadLocationException ex)
        {
            
        }
        StringSelection sel = new StringSelection(text);
        Clipboard clippi = Toolkit.getDefaultToolkit().getSystemClipboard();
        clippi.setContents(sel, null);
    }

    public final void copyMarkableToClipboard(Markable _markable, boolean includeAttributes)
    {
        String text = _markable.toString();
        if (includeAttributes)
        {
            text=text+"\n"+MarkableHelper.getFormattedAttributeString(_markable);
        }
        StringSelection sel = new StringSelection(text);
        Clipboard clippi = Toolkit.getDefaultToolkit().getSystemClipboard();
        clippi.setContents(sel, null);
    }
    
    
    public final void requestRefreshDisplay()
    {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        currentScrollPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        currentTextPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        getCurrentDiscourse().getCurrentMarkableChart().rerender();                
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        currentScrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        currentTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));                
    }                        
    
    
    public final int getRenderingListSize()
    {
        return markableRelationsToRender.size();
    }

    public final boolean getUseDisplayFontInPopups()
    {
        return useDisplayFontInPopups;
    }
    
    public final int getScreenWidth()
    {
        return screenWidth;
    }

    public final int getScreenHeight()
    {
        return screenHeight;
    }
    
    public final void setIgnoreCaretUpdate(boolean status)
    {
        ignoreCaretUpdate = status;
    }
    
    public final boolean getIgnoreCaretUpdate()
    {
        return ignoreCaretUpdate;
    }
    
    public final void setCommonPathsFileName(String name)
    {
        commonPathsFileName = name;
    }
    
    public final String getCommonPathsFileName()
    {
        return commonPathsFileName;
    }
    
    public final boolean getIsBasedataEditingEnabled()
    {
        return enableBasedataEditing.isSelected();
    }

    public final boolean getIsAutoReapplyEnabled()
    {
        return autoReapplyAfterBasedataEditing.isSelected();
    }    
    
    public final boolean getIsAnnotationModified()
    {
        return isAnnotationModified;
    }

    public final boolean getIsBasedataModified()
    {
        return isBasedataModified;
    }
    
    public final void updateIsAnnotationModified()
    {
        setIsAnnotationModified(getCurrentDiscourse().getCurrentMarkableChart().getIsAnyMarkableLevelModified());
    }
    
    /** This method is called by updateIsAnnotaionModified, after changes to the dirty status of each MarkableLevel. */
    public final void setIsAnnotationModified(boolean status)
    {
        if (status)
        {
            // We are to set dirty to true
            if (isAnnotationModified==false && isBasedataModified == false)
            {
                // Modify title only if we are not dirty already
                setTitle(getTitle()+" [modified]");
            }
            // Enable saveAll
            saveAll.setEnabled(true);
            // Enable access to menu under which the individual levels are represented
            saveLevelMenu.setEnabled(true);
            isAnnotationModified = status;
        }
        else
        {
            // We are to set dirty to false
            saveAll.setEnabled(false);
            saveLevelMenu.setEnabled(false);
            String title = getTitle();
            if (title.endsWith("[modified]"))
            {
                // Remove only if something is there to remove
                setTitle(title.substring(0,title.length()-11));
            }
            isAnnotationModified = status;
        }
    }

    /** This method is called afterbase data modification or saving. */
    public final void setIsBasedataModified(boolean status, boolean refresh)
    {
        if (status)
        {
            // We are to set dirty to true
            if (isBasedataModified==false && isAnnotationModified==false)
            {
                // Modify title only if we are not dirty already
                setTitle(getTitle()+" [modified]");
            }
            if (isAnnotationModified)
            {
                // Enable saveAll only if annotation is also dirty
                saveAll.setEnabled(true);
            }
            saveBasedata.setEnabled(true);
            isBasedataModified = status;
        }
        else
        {
            // We are to set dirty to false
            saveAll.setEnabled(false);
            saveBasedata.setEnabled(false);
            String title = getTitle();
            if (title.endsWith("[modified]"))
            {
                // Remove only if something is there to remove
                setTitle(title.substring(0,title.length()-11));
            }
            isBasedataModified = status;
        }
        
        if (refresh)
        {
            // New: check for and update currently active markable browsers
            ArrayList activeBrowsers = getAllMarkableBrowsers();
            
            for (int z=0;z<activeBrowsers.size();z++)
            {
                ((MMAX2MarkableBrowser)activeBrowsers.get(z)).refresh();
            }
        }        
    }
    
    
    
    public final void setOneClickAnnotationGroupValue(int val)
    {
        oneClickAnnotationGroupValue = val;
    }

    public final int getOneClickAnnotationGroupValue()
    {
        return oneClickAnnotationGroupValue;
    }
        
    
    public final void setStatusBar(String text)
    {
        statusBar.setForeground(Color.black);            
        statusBar.setText(text);
    }
    
    public final void clearStatusBar()
    {
        statusBar.setForeground(statusBar.getBackground());
    }

    
    public final MMAX2QueryWindow getMMAX2QueryWindow()
    {
        return queryWindow;
    }
    
    public final Renderable getCurrentlyRenderedMarkableRelationContaining(Markable _markable)
    {
        Renderable temp = null;
        Renderable result = null;
        for (int z=0;z<markableRelationsToRender.size();z++)
        {
            try
            {
                temp = (Renderable) markableRelationsToRender.get(z);
            }
            catch (java.lang.ClassCastException ex)
            {
                continue;
            }
            if (temp.containsMarkable(_markable))
            {
                result = temp;
                break;
            }
        }
        return result;
    }

    public final MarkableSet getFirstCurrentlyRenderedMarkableSetContaining(Markable markable)
    {        
        return (MarkableSet) getCurrentlyRenderedMarkableRelationContaining(markable);
    }
    
    public final Markable getMarkableFromCurrentlyRenderedMarkableSetContaining(String deID)
    {
        // Get all Markables at pos
        int pos = getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(deID);
        boolean found = false;
        Markable[][] temp = getCurrentDiscourse().getCurrentMarkableChart().getMarkablesAtDiscoursePosition(pos, true);
        Markable result = null;
        Markable tempRes = null;
        for (int z=0;z<temp.length;z++)
        {
            Markable[] currentLevel = temp[z];
            for (int b=0;b<currentLevel.length;b++)
            {
                tempRes = currentLevel[b];                
                if (getCurrentlyRenderedMarkableRelationContaining(tempRes)!=null)
                {
                    result = tempRes;
                    found=true;
                    break;
                }
            } 
            if (found) break;
        }
        return result;
    }
    
    
    public final void putOnRenderingList(Renderable set)
    {
        if (set==null)
        {
            return;
        }
        if (markableRelationsToRender.contains(set)==false)
        {
            markableRelationsToRender.add(set);
            set.select((Graphics2D)getCurrentTextPane().getGraphics(), this.getCurrentDocument(), this.getCurrentPrimaryMarkable());
            getCurrentTextPane().startAutoRefresh();
            isRendering = true;
        }
    }

    public final void removeFromRenderingList(Renderable set)
    {
        if (set.getIsPermanent())
        {
            set.unselect(getCurrentDocument());
            set.setIsPermanent(false);
        }
        if (markableRelationsToRender != null)
        {
            set.unselect(getCurrentDocument());            
            markableRelationsToRender.remove(set);
            repaint();
        }
        if (markableRelationsToRender.size() ==0)
        {
            markableRelationsToRender = new ArrayList();
            isRendering = false;
        }
    }
    
    public final void removeOpaqueRenderablesFromRenderingList()
    {
        for (int z=markableRelationsToRender.size()-1;z>=0;z--)
        {
            if (((Renderable)markableRelationsToRender.get(z)).isOpaque())
            {
                ((Renderable)markableRelationsToRender.get(z)).unselect(getCurrentDocument());
                markableRelationsToRender.remove(z);                
            }
        }
    }
    
    public final void emptyRenderingList()
    {
    	emptyRenderingList(false);
    }
    
    
    public final void emptyRenderingList(boolean force)
    {
        // Make sure that a SearchResult is not removed 
        boolean mod = false;
        Renderable currentRenderable = null;
        try
        {
            for (int r=markableRelationsToRender.size()-1;r>=0;r--)
            {
                currentRenderable = ((Renderable)markableRelationsToRender.get(r));
                if (currentRenderable.getIsPermanent()==false || force)
                {
                	((Renderable)markableRelationsToRender.get(r)).unselect(getCurrentDocument());
                	markableRelationsToRender.remove(currentRenderable);
                	currentRenderable.setIsPermanent(false);
                	mod = true;
                }                
            }
        }
        catch (java.lang.IndexOutOfBoundsException ex)
        {
            
        }
        if (mod)
        {
            repaint();
        }        
        if (markableRelationsToRender.size()==0)
        {
            isRendering = false;
        }
    }


    public final boolean isCurrentlyBeingRendered(Renderable renderable)
    {
        if (markableRelationsToRender == null)
        {
            return false;
        }
        if (markableRelationsToRender.contains(renderable))
        {
            return true;
        }
        else
        {
            return false;            
        }
    }

    public final void updateRenderingListObjects()
    {
        isRendering = false;
        for (int z=0;z<markableRelationsToRender.size();z++)
        {
            ((Renderable)markableRelationsToRender.get(z)).updateLinePoints();
            isRendering = true;
        }                
    }
    
    public final boolean getIsRendering()
    {
        return isRendering;
    }
    
    public final void setSelectFromActiveLevelsOnly(boolean status)
    {
        selectFromActiveLevelsOnly = status;
    }

    public final boolean getSelectFromActiveLevelsOnly()
    {
        return selectFromActiveLevelsOnly;
    }

    
    public final void setHighlightMatchingHandles(boolean status)
    {
        highlightMatchingHandles = status;
        if (status == false)
        {
            this.currentTextPane.deactivateMarkableHandleHighlight(false);
        }
    }

    public final boolean getHighlightMatchingHandles()
    {
        return highlightMatchingHandles;
    }
    
    public final void setAutoRefreshUponPanelAction(boolean status)
    {
        autoRefreshUponPanelAction = status;
    }

    public final boolean getAutoRefreshUponPanelAction()
    {
        return autoRefreshUponPanelAction;
    }

    public final void setGroupMarkablesByLevel(boolean status)
    {
        groupMarkablesByLevel = status;
    }

    public final boolean getGroupMarkablesByLevel()
    {
        return groupMarkablesByLevel;
    }
    
    
    public final void setUseFancyLabels(boolean status)
    {
        useFancyLabels = status;
        this.getCurrentDiscourse().getCurrentMarkableChart().updateLabels();
    }
    
    public final boolean getUseFancyLabels()
    {
        return useFancyLabels;
    }
    
    public static final Font getMarkableSelectorFont()
    {
        return markableSelectorFont;
    }
    
    public static final Font getStandardFont()
    {
        return standardFont;
    }
    public final MMAX2Discourse getCurrentDiscourse()
    {
        return currentDiscourse;
    }
    
    public final void setCurrentDocument(MMAX2Document doc)
    {
        this.currentDocument = doc;
    }
    
    public final MMAX2Document getCurrentDocument()
    {
        return this.currentDocument;
    }

    public final MMAX2TextPane getCurrentTextPane()
    {
        return currentTextPane;
    }
     
    
    
    
    
    
    public void executeHotSpot(String hotSpotString)
    {
        String currentToken ="";
        if (hotSpotString.startsWith("playwavsound"))
        {
            String file="";
            long offset = 0;
            long length = 0;
            long temp = 0;
            double start = 0.0;
            double end= 0.0;
            StringTokenizer toki = new StringTokenizer(hotSpotString," ");
            toki.nextToken();
            file = toki.nextToken();
            // Get start time as string
            currentToken = toki.nextToken();
            // Convert start from String to double
            start = Double.parseDouble(currentToken);
            offset = (long)((double)start*(long)16000*(long)2);
            currentToken = toki.nextToken();
            end = Double.parseDouble(currentToken);
            temp = (long) ((double)end*(long)16000*(long)2);
            length = (long)(long)temp-(long)offset;
            getCurrentTextPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));            
            Player.playWAVSound(getCurrentDiscourse().getCommonBasedataPath()+file,offset,length);            
            getCurrentTextPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        else if (hotSpotString.startsWith("playmp3sound"))
        {
            StringTokenizer toki = new StringTokenizer(hotSpotString," ");
            String file = toki.nextToken();
            file = toki.nextToken();

            getCurrentTextPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));            
            Player.playMP3Sound(file, getCurrentDiscourse().getCommonBasedataPath());
            getCurrentTextPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));            
        }
    }
        
    /**  
     * The main method for starting the MMAX2 application from the console.
     * If the name of a .mmax file is supplied, this file is automatically loaded. 
     * The parameter -common_paths 'filename' can be used to use the file 'filename' instead of the default file 'common_paths.xml' as common paths file. 
     * 
     */
    public static void main(String[] args)
    {        
        /** Create new MMAX2 instance */
        MMAX2 mmax2 = null;
        
        try
        {
        	mmax2 = new MMAX2();
        }
        catch (HeadlessException ex)
        {
        	System.err.println("MMAX2 main application cannot be run without a graphical display!");
        	System.exit(0);
        }
        String toLoad = "";
        for (int z=0;z<args.length;z++)
        {
            if (args[z].equalsIgnoreCase("-common_paths"))
            {
                mmax2.setCommonPathsFileName(args[z+1]);
                // Skip file name for next iteration
                z++;
                // If true, cp will not be overwritten when loading a new file
                mmax2.commonPathWasSetViaConsole = true;
            }
            else if (args[z].startsWith("-")==false)
            {
                toLoad = args[z];
            }
        }
        
        mmax2.setTitle("MMAX2 "+versionString);
        
        mmax2.createMenu();
        mmax2.initGlobalStyles();        

        int initialWidth = 800;
        int initialHeight = 500;
        
        mmax2.setSize(initialWidth,initialHeight);   
        mmax2.setLocation((mmax2.getScreenWidth()/2)-initialWidth/2,(mmax2.getScreenHeight()/2)-initialHeight/2);
                   
        if (toLoad.equals("")==false)
        {            
            mmax2.loadMMAXFile(toLoad);
        }

        mmax2.setVisible(true);        
        mmax2.toFront();        

    }
    
    private final void arrangeWindows()
    {
        MarkableLevelControlWindow mlcw = this.getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow;
        mlcw.setVisible(true);
        currentDiscourse.getCurrentMarkableChart().setShowMarkableLevelControlWindow(true);        
        mlcw.setLocation(getScreenWidth()-mlcw.getWidth(),0);        
    }
    
    
    private final void initGlobalStyles()
    {
        selectedMarkableGlobalStyle = MMAX2Utils.createSimpleAttributeSet(selectedMarkableGlobalStyleString,false);
        selectionSpanGlobalStyle = MMAX2Utils.createSimpleAttributeSet(selectionSpanGlobalStyleString,false);
    }
    
    
    public final SimpleAttributeSet getSelectedStyle()
    {
        return selectedMarkableGlobalStyle;
    }
    
    public final SimpleAttributeSet getSelectionSpanStyle()
    {
        return selectionSpanGlobalStyle;
    }
    
    
    public final void startWaitCursor()
    {
        waitCursorTimer = new Timer(1000, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                setToWaitCursor(true);
            }                
        });
        waitCursorTimer.setCoalesce(false);
        waitCursorTimer.setInitialDelay(1);
        waitCursorTimer.setRepeats(true);               
        waitCursorTimer.start();
    }
    
    
    public final void stopWaitCursor()
    {
        setToWaitCursor(false);
        waitCursorTimer.stop();
    }
    
    public final void setReapplyBarToolTip(String text)
    {
    }    
    
    
    public final void setToWaitCursor(boolean status)
    {
        if (status)
        {
            if (getGlassPane().getMouseListeners().length==1)
            {
                getGlassPane().addMouseListener( new MouseAdapter() {});
            }
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            getGlassPane().setVisible(true);            
        }
        else
        {
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getGlassPane().setVisible(false);
        }
    }
    
    private final void loadMMAXFile(String fileName)
    {    
    	
    	boolean verbose = true;
    	
    	String verboseVar = System.getProperty("verbose");
    	if (verboseVar != null && verboseVar.equalsIgnoreCase("false"))
    	{
    		verbose = false;
    	}
    	
    	
        if (hintWindow != null)
        {
            hintWindow.setVisible(false);
            hintWindow = null;
        }
        
        if (queryWindow != null)
        {
            queryWindow.setVisible(false);
            queryWindow.dismiss();
            queryWindow = null;
        }                
               
        initializing = true;        
        getGlassPane().addMouseListener( new MouseAdapter() {});
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        
        if (currentTextPane != null)
        {
            // This is the case if sth. has been loaded before
            currentScrollPane.getVerticalScrollBar().removeAdjustmentListener(currentTextPane);
            currentScrollPane.getHorizontalScrollBar().removeAdjustmentListener(currentTextPane);
            currentScrollPane.getViewport().remove(currentTextPane);
            currentTextPane = null;            
        }
        
        currentPrimaryMarkable = null;
        currentSecondaryMarkable = null;

        emptyRenderingList();
        
        if (currentDiscourse != null)
        {
            currentDiscourse.getCurrentMarkableChart().destroyDependentComponents();
            currentDiscourse.destroyDependentComponents();
        }
        currentDiscourse = null;
        currentDocument = null;
        markableSelectorAttributes = new HashMap();
        showInPopupMenu.removeAll();
        System.gc();
        
        currentTextPane = new MMAX2TextPane();
        currentTextPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        currentTextPane.setPreferredSize(this.getSize());
        currentTextPane.setSize(this.getSize());
        currentScrollPane.setSize(this.getSize());

        currentScrollPane.getVerticalScrollBar().addAdjustmentListener(currentTextPane);
        currentScrollPane.getHorizontalScrollBar().addAdjustmentListener(currentTextPane);
        
        currentScrollPane.getViewport().add(currentTextPane); // new 

        /** Create DiscourseLoader from supplied .mmax file */
        if (verbose) System.err.print("Loading Discourse ... ");
        long temp = System.currentTimeMillis();
        
        MMAX2DiscourseLoader loader = new MMAX2DiscourseLoader(fileName, true,getCommonPathsFileName());
        
        // NEW: Feb. 25, 2005: Tell MMAX about query directory
        setCommonQueryPath(loader.getCommonQueryPath());
        
        currentWorkingDirectory = loader.getWorkingDirectory();
        if (verbose) System.err.println("Current working directory set to "+currentWorkingDirectory);
        
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-temp)+" milliseconds");
        /** Assign Discourse loaded by DiscourseLoader to MMAX2 instance. */
        currentDiscourse = loader.getCurrentDiscourse();
        /** Supply Discourse with reference to current MMAX2 instance. */
        currentDiscourse.setMMAX2(this);
        /** Destroy reference to DiscourseLoader. */
        /** Create new (empty) Document. */
        currentDocument = new MMAX2Document("default",MMAX2Constants.DEFAULT_FONT_SIZE);
        /** Supply Document with reference to current MMAX2 instance. */
        currentDocument.setMMAX2(this);
        
        // Do this before style sheet application
        initializeUserSwitches(loader.getUserSwitches());        
        
        if (verbose) System.err.print("Applying stylesheet "+currentDiscourse.getCurrentStyleSheet()+" ... ");
        temp = System.currentTimeMillis();
        currentDiscourse.reapplyStyleSheet();
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-temp)+" milliseconds");

        currentDiscourse.getCurrentMarkableChart().getCurrentLevelControlPanel().setStyleSheetFileNames(currentDiscourse.getStyleSheetFileNames());
        if (verbose) System.err.print("Creating Markable mappings ... ");
        temp = System.currentTimeMillis();        
        // Call to create e.g. DiscoursePositionToMarkableMappings
        currentDiscourse.getCurrentMarkableChart().createDiscoursePositionToMarkableMappings();
        currentDiscourse.getCurrentMarkableChart().setMarkableLevelDisplayPositions();
        currentDiscourse.getCurrentMarkableChart().initMarkableRelations();        
        currentDiscourse.getCurrentMarkableChart().initAttributePanelContainer();
        currentDiscourse.getCurrentMarkableChart().initShowInMarkableSelectorPopupMenu();
        currentDiscourse.getCurrentMarkableChart().updateLabels();
        currentDiscourse.getCurrentMarkableChart().initAnnotationHints();
        if (verbose) System.err.println("done in "+(System.currentTimeMillis()-temp)+" milliseconds");
    
        currentTextPane.setMMAX2(this); // sets mmax2 on currentTextPane.currentCaretListener and MouseListener as well !!
        currentTextPane.setStyledDocument((DefaultStyledDocument)currentDocument); 
        initializing = false;   
        
        getGlassPane().setVisible(false);
        
        try
        {
            pack();
        }
        catch (java.lang.NullPointerException ex)
        {
        }
        setTitle("MMAX2 "+versionString+" "+fileName);
        updateIsAnnotationModified();
        
        autoSaveMenu.setEnabled(true);
        
        enableBasedataEditing.setEnabled(true);
        searchItem.setEnabled(true);
        markableBrowserItem.setEnabled(true);

        browserMenu.setEnabled(true);
        setBrowserItem.setEnabled(true);
        pointerBrowserItem.setEnabled(true);
        settingsMenu.setEnabled(true);
        displayMenu.setEnabled(true);
        showMLCPBox.setEnabled(true);
        currentDiscourse.getCurrentMarkableChart().initializeSaveMenu(saveLevelMenu);        
        
        arrangeWindows();
        requestRefreshDisplay();
        
        showValidationRequestDialogWindow();
        
        hintWindow = new MMAX2AnnotationHintWindow();
                        
        initPluginMenu();
        
        setVisible(true);        
        loader = null;
        System.gc();    
    }
    
    private final void initializeUserSwitches(String[] switches)
    {                             
        
        if (currentCommonBasedataPath.equals(this.getCurrentDiscourse().getCommonBasedataPath())==false)
        {
            switchesMenu.setEnabled(false);
            switchesMenu.removeAll();
            for (int z=0;z<switches.length;z++)
            {
                if (switches[z]!=null)
                {
                    String name=switches[z].substring(0,switches[z].indexOf(":::"));                
                    String comment = switches[z].substring(switches[z].indexOf(":::")+3,switches[z].lastIndexOf(":::")).trim();
                    String def = switches[z].substring(switches[z].lastIndexOf(":::")+3);
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
                    if (def.equalsIgnoreCase("on"))
                    {
                        switchHash.add(name);
                        item.setSelected(true);
                    }                
                    if (comment.equals("")==false)
                    {
                        item.setToolTipText(comment);
                    }
                    item.setFont(getStandardFont());
                    final String temp=name;
                    item.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(java.awt.event.ActionEvent ae)
                        {
                            modifyUserSwitches(temp, ae);
                        }            
                    });

                    switchesMenu.add(item);      
                    item=null;                
                }
            }
            if (switchesMenu.getItemCount()!=0)
            {
                switchesMenu.setEnabled(true);
            }
            currentCommonBasedataPath=getCurrentDiscourse().getCommonBasedataPath();
        }
    }
    
    public final boolean isOn(String name)
    {
        return switchHash.contains(name);
    }
   
    
    private final void modifyUserSwitches(String name, ActionEvent ae)
    {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)ae.getSource();
        if (item.isSelected())
        {
            switchHash.add(name);
        }
        else
        {
            switchHash.remove(name);
        }
    }
    
    private final void showValidationRequestDialogWindow()
    {
        String message = "Do you want to validate the annotations now?\n";
        message = message + "It is recommended to validate annotations once after initial creation,\n";
        message = message + "and after each MMAX-external modification.\n";
        message = message + "Validation will check initial data consistency. It will also set default\n";
        message = message + "values for all attributes, so the annotations should be saved afterwards!\n";
        message = message + "\nWhat do you want to do?\n";// If in doubt, use 'Do not validate'!\n";
        Object[] options = { "Do not validate", "Validate now"};
        int result = JOptionPane.showOptionDialog(null, message, "MMAX2: Annotation validation prompt", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,null, options, options[0]);

        if (result == 1)
        {
            getCurrentDiscourse().getCurrentMarkableChart().validateAll();
        }        
    }
        
    private final void createMenu()
    {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadFile = new JMenuItem("Load");
        loadFile.setFont(MMAX2.getStandardFont());
        loadFile.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestLoadFile();
           }
            
        });
        fileMenu.add(loadFile);
    
        JMenu saveMenu = new JMenu("Save");
        saveMenu.setFont(MMAX2.getStandardFont());
        saveAll = new JMenuItem("All");        
        saveAll.setFont(MMAX2.getStandardFont());
        saveAll.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestSaveAll();
           }
            
        });
        saveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveAll.setEnabled(false);
        
        saveMenu.add(saveAll);
        saveMenu.addSeparator();
        saveLevelMenu = new JMenu("Level");
        saveLevelMenu.setFont(MMAX2.getStandardFont());
        saveLevelMenu.setEnabled(false);
        saveMenu.add(saveLevelMenu);
        saveMenu.addSeparator();
        saveBasedata = new JMenuItem("Base data");
        saveBasedata.setFont(MMAX2.getStandardFont());
        saveBasedata.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestSaveBasedata();
           }            
        });
        
        saveBasedata.setEnabled(false);
        saveMenu.add(saveBasedata);        
        fileMenu.add(saveMenu);
        
    
        autoSaveMenu = new JMenu("Auto-Save");
        autoSaveMenu.setFont(MMAX2.getStandardFont());
        autoSaveMenu.setEnabled(false);
        autoSaveEveryMenu = new JMenu("Every");
        autoSaveEveryMenu.setFont(MMAX2.getStandardFont());
        JRadioButtonMenuItem tempAutoSaveItem0 = new JRadioButtonMenuItem("0 mins (off)");
        autoSaveEveryMenuButtongroup.add(tempAutoSaveItem0);
        tempAutoSaveItem0.setFont(MMAX2.getStandardFont());
        tempAutoSaveItem0.setSelected(true);
        tempAutoSaveItem0.addActionListener(this);
        tempAutoSaveItem0.setActionCommand("auto-save:0");       
        autoSaveEveryMenu.add(tempAutoSaveItem0);
        
        JRadioButtonMenuItem tempAutoSaveItem1 = null;
        tempAutoSaveItem1 = new JRadioButtonMenuItem("1 min");
        autoSaveEveryMenuButtongroup.add(tempAutoSaveItem1);
        tempAutoSaveItem1.setFont(MMAX2.getStandardFont());
        tempAutoSaveItem1.addActionListener(this);
        tempAutoSaveItem1.setActionCommand("auto-save:1");
        autoSaveEveryMenu.add(tempAutoSaveItem1);
        
        JRadioButtonMenuItem tempAutoSaveItem5 = null;
        tempAutoSaveItem5 = new JRadioButtonMenuItem("5 mins");
        autoSaveEveryMenuButtongroup.add(tempAutoSaveItem5);
        tempAutoSaveItem5.setFont(MMAX2.getStandardFont());
        tempAutoSaveItem5.addActionListener(this);
        tempAutoSaveItem5.setActionCommand("auto-save:5");
        autoSaveEveryMenu.add(tempAutoSaveItem5);
        
        JRadioButtonMenuItem tempAutoSaveItem10 = null;
        tempAutoSaveItem10 = new JRadioButtonMenuItem("10 mins");
        autoSaveEveryMenuButtongroup.add(tempAutoSaveItem10);
        tempAutoSaveItem10.setFont(MMAX2.getStandardFont());
        tempAutoSaveItem10.addActionListener(this);
        tempAutoSaveItem10.setActionCommand("auto-save:10");
        autoSaveEveryMenu.add(tempAutoSaveItem10);
        
        JRadioButtonMenuItem tempAutoSaveItem15 = null;
        tempAutoSaveItem15 = new JRadioButtonMenuItem("15 mins");
        autoSaveEveryMenuButtongroup.add(tempAutoSaveItem15);
        tempAutoSaveItem15.setFont(MMAX2.getStandardFont());
        tempAutoSaveItem15.addActionListener(this);
        tempAutoSaveItem15.setActionCommand("auto-save:15");
        autoSaveEveryMenu.add(tempAutoSaveItem15);
        autoSaveMenu.add(autoSaveEveryMenu);
        fileMenu.add(autoSaveMenu);
        
        exit = new JMenuItem("Exit");
        exit.setFont(MMAX2.getStandardFont());
        exit.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestExit();
           }
            
        });        
        
        exit.setEnabled(true);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        fileMenu.add(exit);        
        menu.add(fileMenu);                            
        
        settingsMenu = new JMenu("Settings");

        settingsMenu.setEnabled(false);
        
        enableBasedataEditing = new JCheckBoxMenuItem("Enable base data editing");
        enableBasedataEditing.setFont(MMAX2.getStandardFont());       
        enableBasedataEditing.setEnabled(true);
        settingsMenu.add(enableBasedataEditing);

        
        autoReapplyAfterBasedataEditing = new JCheckBoxMenuItem("Reapply style sheet after base data editing");
        autoReapplyAfterBasedataEditing.setFont(MMAX2.getStandardFont());       
        autoReapplyAfterBasedataEditing.setEnabled(true);
        autoReapplyAfterBasedataEditing.setSelected(true);        
        settingsMenu.add(autoReapplyAfterBasedataEditing);        
        
        JCheckBoxMenuItem autorefreshUponPanelActionMenuItem = new JCheckBoxMenuItem("Auto-refresh");
        autorefreshUponPanelActionMenuItem.setFont(MMAX2.getStandardFont());
        autorefreshUponPanelActionMenuItem.setSelected(false);
        autorefreshUponPanelActionMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setAutoRefreshUponPanelAction(source.isSelected());
           }
            
        });
        settingsMenu.add(autorefreshUponPanelActionMenuItem);
        
        JCheckBoxMenuItem highlightHandlesMenuItem = new JCheckBoxMenuItem("Highlight matching handles");
        highlightHandlesMenuItem.setFont(MMAX2.getStandardFont());
        highlightHandlesMenuItem.setSelected(true);
        highlightHandlesMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setHighlightMatchingHandles(source.isSelected());
                if (source.isSelected()) 
                {
                    suppressHandlesWhenRenderingMenuItem.setEnabled(true);
                }
                else
                {
                    suppressHandlesWhenRenderingMenuItem.setEnabled(false);
                }
           }            
        });
        settingsMenu.add(highlightHandlesMenuItem);

        suppressHandlesWhenRenderingMenuItem = new JCheckBoxMenuItem("Suppress handle highlights when rendering sets");
        suppressHandlesWhenRenderingMenuItem.setFont(MMAX2.getStandardFont());
        suppressHandlesWhenRenderingMenuItem.setSelected(true);
        suppressHandlesWhenRenderingMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setSuppressHandlesWhenRendering(source.isSelected());
           }
            
        });
        settingsMenu.add(suppressHandlesWhenRenderingMenuItem);
        
        
        JCheckBoxMenuItem showMarkablePeerMenuItem = new JCheckBoxMenuItem("Show Markable set popup");
        showMarkablePeerMenuItem.setFont(MMAX2.getStandardFont());
        showMarkablePeerMenuItem.setSelected(false);
        showMarkablePeerMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setShowMarkableSetPeerWindow(source.isSelected());
           }
            
        });
        settingsMenu.add(showMarkablePeerMenuItem);

        JCheckBoxMenuItem createSilentlyMenuItem = new JCheckBoxMenuItem("Create new Markable silently");
        createSilentlyMenuItem.setFont(MMAX2.getStandardFont());
        createSilentlyMenuItem.setSelected(false);
        createSilentlyMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setCreateSilently(source.isSelected());
           }
            
        });
        settingsMenu.add(createSilentlyMenuItem);        
        
        JCheckBoxMenuItem selectAfterCreationMenuItem = new JCheckBoxMenuItem("Select new Markable after creation");
        selectAfterCreationMenuItem.setFont(MMAX2.getStandardFont());
        selectAfterCreationMenuItem.setSelected(false);
        selectAfterCreationMenuItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setSelectAfterCreation(source.isSelected());
           }
            
        });
        settingsMenu.add(selectAfterCreationMenuItem);        
        
        menu.add(settingsMenu);   
                
        displayMenu = new JMenu("Display");
        displayMenu.setEnabled(false);
        JMenuItem refreshDisplay = new JMenuItem("Refresh");
        refreshDisplay.setFont(MMAX2.getStandardFont());
        refreshDisplay.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
               requestRefreshDisplay();
           }
            
        });
        displayMenu.add(refreshDisplay);
    
        JMenuItem reapplyDisplay = new JMenuItem("Reapply current style sheet");
        reapplyDisplay.setFont(MMAX2.getStandardFont());
        reapplyDisplay.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
               requestReapplyDisplay();
           }
            
        });
        reapplyDisplay.setAccelerator(KeyStroke.getKeyStroke("F5"));
        displayMenu.add(reapplyDisplay);
        
        JMenu fontMenu = new JMenu("Font");
        fontMenu.setFont(MMAX2.getStandardFont());
        JMenu setFontName = new JMenu("Name");
        setFontName.setFont(MMAX2.getStandardFont());
        fontNameButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem temp = null;
    
        GraphicsEnvironment myEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = myEnv.getAvailableFontFamilyNames();
    
        int fontNum = fontNames.length;
    
        for (int z=0;z<fontNum;z++)
        {
            final String currentFontName = fontNames[z];
            temp = new JRadioButtonMenuItem(currentFontName);
            temp.setFont(MMAX2.getStandardFont());
            if (currentFontName.equalsIgnoreCase("default"))
            {
                temp.setSelected(true);
            }
            temp.addActionListener(new ActionListener()
            {
            	public void actionPerformed (ActionEvent ae)
            	{
            		requestSetFontName(currentFontName);
            	}
            });
            
            fontNameButtonGroup.add(temp);
            setFontName.add(temp);
            temp=null;
        }
        
        fontMenu.add(setFontName);
        
        fontSizeButtonGroup = new ButtonGroup();
        JMenu setFontSize = new JMenu("Size");
        setFontSize.setFont(MMAX2.getStandardFont());
        temp = null;
    
        for (int z=8;z<30;z=z+2)
        {
            final String currentFontSize = z+"";;
            temp = new JRadioButtonMenuItem(z+"");
            temp.setFont(MMAX2.getStandardFont());
            if (z==MMAX2Constants.DEFAULT_FONT_SIZE)
            {
                temp.setSelected(true);
            }
            temp.addActionListener(new ActionListener()
            {
            	public void actionPerformed (ActionEvent ae)
            	{
            		requestSetFontSize(currentFontSize);
            	}
            });
            fontSizeButtonGroup.add(temp);
            setFontSize.add(temp);
            temp=null;
        }
        
        fontMenu.add(setFontSize);
        
        lineSpacingButtonGroup = new ButtonGroup();
        JMenu setLineSpacing = new JMenu("Line spacing");
        setLineSpacing.setFont(MMAX2.getStandardFont());
        temp = null;
    
        for (int z=0;z<15;z++)
        {
            final String currentLineSpacing = ((float)z/10)+"";
            temp = new JRadioButtonMenuItem(((float)z/10)+"");
            temp.setFont(MMAX2.getStandardFont());
            if (z==0)
            {
                temp.setSelected(true);
            }
            temp.addActionListener(new ActionListener()
        {
            public void actionPerformed (ActionEvent ae)
            {
                requestSetLineSpacing(currentLineSpacing);
            }
        });
            lineSpacingButtonGroup.add(temp);
            setLineSpacing.add(temp);
            temp=null;
        }
        
        fontMenu.add(setLineSpacing);
        
        
        JCheckBoxMenuItem useInPopups = new JCheckBoxMenuItem("Use display font in popups");
        useInPopups.setFont(MMAX2.getStandardFont());
        useInPopups.setSelected(false);
        useInPopups.addActionListener(new ActionListener()
        {
            public void actionPerformed (ActionEvent ae)
            {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)ae.getSource();
                useDisplayFontInPopups = item.getState();
            }
        });

        fontMenu.add(useInPopups);
        displayMenu.add(fontMenu);
        
        JMenuItem printItem = new JMenuItem("Print (experimental)");
        printItem.setFont(MMAX2.getStandardFont());
        printItem.addActionListener(this);
        printItem.setActionCommand("print");
        displayMenu.add(printItem);
        
        showInPopupMenu = new JMenu("Show in Markable Selector");
        showInPopupMenu.setFont(MMAX2.getStandardFont());
        displayMenu.add(showInPopupMenu);
                        
        JCheckBoxMenuItem useFancyMultilineRendering = new JCheckBoxMenuItem("Use smart multi-line Markable rendering");
        useFancyMultilineRendering.setFont(MMAX2.getStandardFont());
        useFancyMultilineRendering.setSelected(true);
        useFancyMultilineRendering.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) ae.getSource();
                setUseFancyMultilineRendering(source.isSelected());
           }
            
        });
        
        displayMenu.add(useFancyMultilineRendering);        
        displayMenu.addSeparator();
        
        switchesMenu = new JMenu("User switches");
        switchesMenu.setFont(MMAX2.getStandardFont());
        switchesMenu.setEnabled(false);
        displayMenu.add(switchesMenu);
        
        menu.add(displayMenu);
                
        
        toolsMenu = new JMenu("Tools");
        wizard = new JMenuItem("Project Wizard");
        wizard.setFont(MMAX2.getStandardFont());
        wizard.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestProjectWizard();
           }
            
        });
        wizard.setEnabled(true);
        toolsMenu.add(wizard);        
        
        searchItem = new JMenuItem("Query Console");
        searchItem.setEnabled(false);
        searchItem.setFont(MMAX2.getStandardFont());
        searchItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestQueryWindow();
           }
            
        });
        toolsMenu.add(searchItem);
        
        browserMenu = new JMenu("Browsers");
        browserMenu.setFont(MMAX2.getStandardFont());
        browserMenu.setEnabled(false);
        
        markableBrowserItem = new JMenuItem("Markable Browser");
        markableBrowserItem.setFont(MMAX2.getStandardFont());
        markableBrowserItem.setEnabled(false);
        markableBrowserItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
               requestMarkableBrowser();
           }
            
        });
        browserMenu.add(markableBrowserItem);

        setBrowserItem = new JMenuItem("Markable Set Browser");
        setBrowserItem.setEnabled(false);
        setBrowserItem.setFont(MMAX2.getStandardFont());
        setBrowserItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestMarkableSetBrowser();
           }
            
        });
        browserMenu.add(setBrowserItem);

        pointerBrowserItem = new JMenuItem("Markable Pointer Browser");
        pointerBrowserItem.setEnabled(false);
        pointerBrowserItem.setFont(MMAX2.getStandardFont());
        pointerBrowserItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
                requestMarkablePointerBrowser();
           }
            
        });
        browserMenu.add(pointerBrowserItem);
                        
        toolsMenu.add(browserMenu);
                
        menu.add(toolsMenu);
                             
        
        pluginMenu = new JMenu("Plugins");
        pluginMenu.setEnabled(false);
        menu.add(pluginMenu);
        
        JMenu infoMenu = new JMenu("Info");
        JMenuItem aboutItem = new JMenuItem("About MMAX2");
        aboutItem.setFont(MMAX2.getStandardFont());
        aboutItem.addActionListener(new ActionListener()
        {
           public void actionPerformed(java.awt.event.ActionEvent ae)
           {
               requestAboutWindow();
           }
            
        });
        infoMenu.add(aboutItem);
        menu.add(infoMenu);

        showMLCPBox = new JCheckBox("Show ML Panel");
        showMLCPBox.setFont(MMAX2.getStandardFont().deriveFont(8));
        showMLCPBox.setSelected(true);
        showMLCPBox.setActionCommand("mlcp");
        showMLCPBox.addActionListener(this);
        showMLCPBox.setEnabled(false);
        menu.add(showMLCPBox);
        setJMenuBar(menu);     
        
        
    }
    
    public final void requestSetLineSpacing(String spacing)
    {       
        float val = (float)0.0;
        try
        {
            val = Float.parseFloat(spacing);
        }
        catch (java.lang.NumberFormatException ex)
        {
            
        }
        
        SimpleAttributeSet result = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(result, (float)val);
        getCurrentDocument().setParagraphAttributes(0, getCurrentDocument().getLength(),result, false);         
    }
    
    public final void addShowInMarkableSelectorEntry(String levelName, ArrayList allValues)
    {
        // Create new menu item for this level
        JMenu item = new JMenu(levelName);
        item.setFont(MMAX2.getStandardFont());
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem temp = new JRadioButtonMenuItem("<none>");
        temp.setSelected(true);
        group.add(temp);
        temp.setFont(MMAX2.getStandardFont());
        temp.setActionCommand("set_showinselector_attribute "+levelName+":::<none>");
        temp.addActionListener(this);
        item.add(temp);
        temp = null;
        showInPopupMenu.add(item);
                
        // Start at 1 to skip initial <none> value
        for (int z=1;z<allValues.size();z++)
        {
            temp = new JRadioButtonMenuItem((String)allValues.get(z));
            group.add(temp);
            temp.setFont(MMAX2.getStandardFont());
            temp.setActionCommand("set_showinselector_attribute "+levelName+":::"+allValues.get(z));
            temp.addActionListener(this);
            item.add(temp);
            temp = null;
        }
        
        
    }
    
    public final JMenu getPluginMenu()
    {
        return pluginMenu;
    }
    
    private final void requestExecutePlugin(String className, HashMap attributes)
    {
        MMAX2Plugin plugin  = null;
        try
        {
            Class pluginClass = Class.forName(className);
            System.err.println(pluginClass);
            plugin = (MMAX2Plugin) pluginClass.newInstance();
        }
        catch (java.lang.ClassNotFoundException ex)
        {
            JOptionPane.showMessageDialog(null,"Could not find plugin class "+className+"!","MMAX2 Plugin Handler",JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (java.lang.ClassCastException ex)
        {
            ex.printStackTrace();
        }
        catch (java.lang.InstantiationException ex)
        {
            ex.printStackTrace();
        }
        catch (java.lang.IllegalAccessException ex)
        {
            ex.printStackTrace();
        }                                                    
        
        if (plugin != null)
        {
            plugin.callPlugin(getCurrentDiscourse(),attributes, getIsBatchPluginMode());
        }
    }
    
    private final void initPluginMenu()
    {
        if (pluginMenu != null)
        {            
            pluginMenu.removeAll();
        }
        
        DOMParser parser = new DOMParser();
        DocumentImpl PLUGINDOM = null;
        
        String fileName="plugins.xml";
        
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
        catch (java.io.FileNotFoundException ex)
        {
            System.err.println("No plugin.xml found");
            return;
        }        
        catch (org.xml.sax.SAXParseException exception)
        {
            String error = "Line: "+exception.getLineNumber()+" Column: "+exception.getColumnNumber()+"\n"+exception.toString();            
            JOptionPane.showMessageDialog(null,error,"PluginLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }                
        catch (org.xml.sax.SAXException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"PluginLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }
        catch (java.io.IOException exception)
        {
            String error = exception.toString();
            JOptionPane.showMessageDialog(null,error,"PluginLoader: "+fileName,JOptionPane.ERROR_MESSAGE);
        }                      
        PLUGINDOM = (DocumentImpl) parser.getDocument();
        
        NodeList pluginList = PLUGINDOM.getElementsByTagName("plugin");
        // Iterate over all plugin entries
        for (int n=0;n<pluginList.getLength();n++)
        {
            String name="";
            String forClass="";
            try
            {
                name=pluginList.item(n).getAttributes().getNamedItem("name").getNodeValue();
            }
            catch (java.lang.NullPointerException ex)
            {
                System.err.println("Plugin with empty 'name' attribute, ignored!");
                name="";
                continue;
            }
            
            try
            {
                forClass=pluginList.item(n).getAttributes().getNamedItem("class").getNodeValue();
            }
            catch (java.lang.NullPointerException ex)
            {
                System.err.println("Plugin with empty 'class' attribute, ignored!");
                forClass="";
                continue;
            }
            
            if (name.equals("")==false && forClass.equals("")==false)
            {
                final HashMap parameterMap = new HashMap();
                // Only if all information is available
                NodeList parameters = pluginList.item(n).getChildNodes();
                for (int v=0;v<parameters.getLength();v++)
                {
                    if (parameters.item(v).getNodeName().equals("parameter"))
                    {
                        String currentAttribute = "";
                        try
                        {
                            currentAttribute = parameters.item(v).getAttributes().getNamedItem("attribute").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            System.err.println("Parameter with empty 'attribute' attribute, ignored!");
                            currentAttribute="";
                        }
                        
                        String currentAttributeValue = "";
                        try
                        {
                            currentAttributeValue = parameters.item(v).getAttributes().getNamedItem("value").getNodeValue();
                        }
                        catch (java.lang.NullPointerException ex)
                        {
                            System.err.println("Parameter with empty 'value' attribute");
                            currentAttributeValue="";
                        }
                        
                        if (currentAttribute.equals("")==false)
                        {
                            parameterMap.put(currentAttribute, currentAttributeValue);
                        }
                    }
                }
                pluginMenu.setEnabled(true);
                // Here, we have everything we need
                JMenuItem nextPluginMenuItem = new JMenuItem(name);
                nextPluginMenuItem.setFont(getStandardFont());
                final String finalClassName = forClass;
                nextPluginMenuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(java.awt.event.ActionEvent ae)
                {
                    requestExecutePlugin(finalClassName,parameterMap);
                }            
               });                                         
               pluginMenu.add(nextPluginMenuItem);
            }
        }
        
        if (pluginMenu.getItemCount() != 0)
        {
           batchPluginMenuItem = new JMenuItem("Batch Plugins ...");
           batchPluginMenuItem.setFont(getStandardFont());
           batchPluginMenuItem.setEnabled(true);
           batchPluginMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(java.awt.event.ActionEvent ae)
            {
                requestBatchPluginWindow();
            }            
           });     

           pluginMenu.addSeparator();
           pluginMenu.add(batchPluginMenuItem);
        }          
    }
        
    private final void requestBatchPluginWindow()
    {
        new MMAX2BatchPluginWindow(this);
    }
    
    public final void setIsBatchPluginMode(boolean mode)
    {
        isBatchPluginMode = mode;
    }
    
    public final boolean getIsBatchPluginMode()
    {
        return isBatchPluginMode;
    }
    
    private final void requestAboutWindow()
    {
        String message =  "This is version "+versionString+" of MMAX2. ";
        message = message + "Copyright 2003-2010 Dr. Mark-Christoph Müller.\n";
        message = message + "MMAX2 has originally been developed at the European Media Lab / EML Research, Heidelberg, Germany.";
        message = message + "\n\nProject home page: http://mmax2.net\n\n";
        message = message + "Contact:             contact@mmax2.net\n\n";
        message = message + "This product includes software developed by the Apache Software Foundation (http://www.apache.org/).";

        JOptionPane.showMessageDialog(this, message, "About MMAX2", JOptionPane.INFORMATION_MESSAGE);        
    }
    
    public final void executeBatchPlugins(ArrayList namesToBeExecuted)
    {
        setIsBatchPluginMode(true);
        for (int b=0;b<namesToBeExecuted.size();b++)
        {
            String currentItemName = (String) namesToBeExecuted.get(b);
            for (int n=0;n<pluginMenu.getItemCount()-2;n++)
            {
                JMenuItem item = (JMenuItem)pluginMenu.getItem(n);
                if (item.getText() != null && item.getText().equals(currentItemName))
                {
                    System.err.println("Batch mode: executing plugin '"+currentItemName+"'");
                    item.doClick();
                }
            }
        }
        setIsBatchPluginMode(false);
    }
        
    public final ArrayList getAllMarkableBrowsers()
    {
        ArrayList result = new ArrayList();
        if (markableBrowsers != null)
        {
            result = new ArrayList(markableBrowsers);
        }        
        return result;
    }
    
    public final ArrayList getMarkableBrowsersForMarkableLevel(String name)
    {
        ArrayList result = new ArrayList();
        if (markableBrowsers != null)
        {
            for (int z=0;z<markableBrowsers.size();z++)
            {
                MMAX2MarkableBrowser browser = (MMAX2MarkableBrowser)markableBrowsers.get(z);
                if (browser.getCurrentlyDisplayedMarkableLevelName().equals(name))
                {
                    result.add(browser);
                }
            }
        }
        return result;
    }

    public final ArrayList getMarkableSetBrowsersForMarkableLevel(String name)
    {
        ArrayList result = new ArrayList();
        if (markableSetBrowsers != null)
        {
            for (int z=0;z<markableSetBrowsers.size();z++)
            {
                MMAX2MarkableSetBrowser browser = (MMAX2MarkableSetBrowser)markableSetBrowsers.get(z);
                if (browser.getCurrentlyDisplayedMarkableLevelName().equalsIgnoreCase(name))
                {
                    result.add(browser);
                }
            }
        }
        return result;
    }

    public final ArrayList getMarkablePointerBrowsersForMarkableLevel(String name)
    {
        ArrayList result = new ArrayList();
        if (markablePointerBrowsers != null)
        {
            for (int z=0;z<markablePointerBrowsers.size();z++)
            {
                MMAX2MarkablePointerBrowser browser = (MMAX2MarkablePointerBrowser)markablePointerBrowsers.get(z);
                if (browser.getCurrentlyDisplayedMarkableLevelName().equalsIgnoreCase(name))
                {
                    result.add(browser);
                }
            }
        }
        return result;
    }

    
    public final int registerMarkableSetBrowser(MMAX2MarkableSetBrowser browser)
    {
        if (markableSetBrowsers == null)
        {
            markableSetBrowsers = new ArrayList();
        }
        markableSetBrowsers.add(browser);
        markableSetBrowserCount++;
        return markableSetBrowserCount;
    }

    public final int registerMarkablePointerBrowser(MMAX2MarkablePointerBrowser browser)
    {
        if (markablePointerBrowsers == null)
        {
            markablePointerBrowsers = new ArrayList();
        }
        markablePointerBrowsers.add(browser);
        markablePointerBrowserCount++;
        return markablePointerBrowserCount;
    }
    
    
    public final int registerMarkableBrowser(MMAX2MarkableBrowser browser)
    {
        if (markableBrowsers == null)
        {
            markableBrowsers = new ArrayList();
        }
        markableBrowsers.add(browser);
        markableBrowserCount++;
        return markableBrowserCount;
    }
    
    public final void unregisterMarkableBrowser(MMAX2MarkableBrowser browser)
    {
        markableBrowsers.remove(browser);
        if (markableBrowsers.size()==0) 
        {
            markableBrowsers = null;
            markableBrowserCount=0;
        }
    }

    public final void unregisterMarkableSetBrowser(MMAX2MarkableSetBrowser browser)
    {
        markableSetBrowsers.remove(browser);
        if (markableSetBrowsers.size()==0) 
        {
            markableSetBrowsers = null;
            markableSetBrowserCount=0;
        }
    }
    
    public final void unregisterMarkablePointerBrowser(MMAX2MarkablePointerBrowser browser)
    {
        markablePointerBrowsers.remove(browser);
        if (markablePointerBrowsers.size()==0) 
        {
            markablePointerBrowsers = null;
            markablePointerBrowserCount=0;
        }
    }

    
    private final void dismissAllMarkableBrowsers()
    {
        markableBrowserCount = 0;
        if (markableBrowsers != null)
        {
            while(markableBrowsers!=null)
            {
                ((MMAX2MarkableBrowser)markableBrowsers.get(0)).dismiss();
            }
        }
    }

    private final void dismissAllMarkableSetBrowsers()
    {
        markableSetBrowserCount = 0;
        if (markableSetBrowsers != null)
        {
            while(markableSetBrowsers!=null)
            {
                ((MMAX2MarkableSetBrowser)markableSetBrowsers.get(0)).dismiss();
            }
        }
    }

    private final void dismissAllMarkablePointerBrowsers()
    {
        markablePointerBrowserCount = 0;
        if (markablePointerBrowsers != null)
        {
            while(markablePointerBrowsers!=null)
            {
                ((MMAX2MarkablePointerBrowser)markablePointerBrowsers.get(0)).dismiss();
            }
        }
    }
       
    
    private final void requestSetFontName(String newFontName)
    {
        currentDisplayFontName = newFontName;
        getCurrentDiscourse().getMMAX2().getCurrentDocument().setDisplayFontName(newFontName);
        requestRefreshDisplay();
    }

    private final void requestSetFontSize(String newFontSize)
    {
        currentDisplayFontSize = new Integer(newFontSize).intValue();        
        getCurrentDiscourse().getMMAX2().getCurrentDocument().setDisplayFontSize(currentDisplayFontSize);
        requestRefreshDisplay();
    }

    private final void requestMarkableSetBrowser()
    {
        new MMAX2MarkableSetBrowser(this);
    }
    
    private final void requestMarkablePointerBrowser()
    {
        new MMAX2MarkablePointerBrowser(this);
    }

    private final void requestMarkableBrowser()
    {
        new MMAX2MarkableBrowser(this,"","",null);
    }
    
    private final void requestQueryWindow()
    {
        if (queryWindow == null)
        {
            queryWindow = new MMAX2QueryWindow(currentDiscourse.getCurrentMarkableChart());
            queryWindow.pack();
        }
        if (queryWindow.isVisible()==false)
        {
            queryWindow.setVisible(true);
        }
        
        queryWindow.toFront();
    }
    
    
    private final void requestProjectWizard()
    {
        wizard.setEnabled(false);
        new MMAX2ProjectWizard(this);
    }
    
    public final void wizardClosed()
    {
        wizard.setEnabled(true);
    }
    
    public final void requestExit()
    {
    	System.err.println("Requesting exit...");
    	
        if (getIsAnnotationModified())
        {
            String message = "The annotation has been modified.\nWould you like to save before exiting?";
            Object[] options = { "Save and exit", "Exit without saving", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Exit application: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                dismissAllMarkableBrowsers();
                dismissAllMarkableSetBrowsers();
                currentDiscourse.getCurrentMarkableChart().saveAllMarkableLevels();
            }
            else if (result == 1)
            {
                dismissAllMarkableBrowsers();
                dismissAllMarkableSetBrowsers();
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
            	System.out.println("Cancelling exit!");
            	this.getCurrentTextPane().setControlIsPressed(false);
                return;
            }                            
        }
        
        // This point is only reached if either the data was saved, or there was nothing to save
        
        if (getIsBasedataModified())
        {
            String message = "The base data has been modified.\nWould you like to save before exiting?";
            Object[] options = { "Save and exit", "Exit without saving", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Exit application: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                dismissAllMarkableBrowsers();
                dismissAllMarkableSetBrowsers();
                currentDiscourse.saveBasedata("");
                System.exit(0);
            }
            else if (result == 1)
            {
                dismissAllMarkableBrowsers();
                dismissAllMarkableSetBrowsers();
                System.exit(0);
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
                System.out.println("Cancelling exit!");
                return;
            }                            
        }                
        
        // Neither anno nor base data was dirty
        dismissAllMarkableSetBrowsers();
        dismissAllMarkableBrowsers();
        System.exit(0);
    }
    
    private final void requestSaveAll()
    {
        currentDiscourse.getCurrentMarkableChart().saveAllMarkableLevels();
        currentDiscourse.saveBasedata("");
    }
    
    private final void requestSaveBasedata()
    {
        currentDiscourse.saveBasedata("");
    }
        
    public final void requestLoadFile(String fileToLoad)
    {
        // Called from Project Wizard
        if (getIsAnnotationModified())
        {
            String message = "The annotation has been modified.\nWould you like to save it before loading a new project?";
            Object[] options = { "Save changes", "Discard changes", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Load annotation: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                currentDiscourse.getCurrentMarkableChart().saveAllMarkableLevels();
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
                System.err.println("Cancelling exit!");
                return;
            }                            
        }        
        
        if (getIsBasedataModified())
        {
            String message = "The base data has been modified.\nWould you like to save it before loading a new project?";
            Object[] options = { "Save changes", "Discard changes", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Load annotation: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                currentDiscourse.saveBasedata("");
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
                System.err.println("Cancelling exit!");
                return;
            }                            
        }        
        
        dismissAllMarkableBrowsers();
        dismissAllMarkableSetBrowsers();
        dismissAllMarkablePointerBrowsers();
        
        if (getCurrentDiscourse() != null && getCurrentDiscourse().getCurrentMarkableChart() != null)
        {
            getCurrentDiscourse().getCurrentMarkableChart().nothingClicked(-1);
        }
        setCommonPathsFileName(""); // added July 29th
        String requestedFileName = fileToLoad;
        loadMMAXFile(requestedFileName);
    }

    
    private final void requestLoadFile()
    {
        if (getIsAnnotationModified())
        {
            String message = "The annotation has been modified.\nWould you like to save it before loading a new project?";
            Object[] options = { "Save changes", "Discard changes", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Load annotation: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                currentDiscourse.getCurrentMarkableChart().saveAllMarkableLevels();
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
                System.err.println("Cancelling exit!");
                return;
            }                            
        }

        if (getIsBasedataModified())
        {
            String message = "The base data has been modified.\nWould you like to save it before loading a new project?";
            Object[] options = { "Save changes", "Discard changes", "Cancel" };
            int result = JOptionPane.showOptionDialog(null, message, "Load annotation: Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[2]);
            if (result == 0)
            {
                currentDiscourse.saveBasedata("");
            }
            else if (result==2 || result==JOptionPane.CLOSED_OPTION || result==JOptionPane.CANCEL_OPTION)
            {
                System.err.println("Cancelling exit!");
                return;
            }                            
        }
                
        dismissAllMarkableBrowsers();
        dismissAllMarkableSetBrowsers();
        dismissAllMarkablePointerBrowsers();
        
        String requestedFileName = "";
        JFileChooser chooser = new JFileChooser(currentWorkingDirectory);
        chooser.setFileFilter(new MMAXFileFilter("mmax","MMAX2 annotation files"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            if (commonPathWasSetViaConsole==false)
            {
                setCommonPathsFileName(""); // added July 29th    
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Note: MMAX2 will use the common paths file "+getCommonPathsFileName()+" supplied from the console!\nRestart the tool without the -common_paths argument to use the default common_paths.xml!","MMAX2 loader",JOptionPane.INFORMATION_MESSAGE);
            }
            
            requestedFileName = chooser.getSelectedFile().getAbsolutePath();
            System.out.println(requestedFileName);
            
            loadMMAXFile(requestedFileName);
        }                
    }
    
    /** This method is called automatically to redraw lines after display changes. */
    final public void redraw(SVGGraphics2D svgGraph)
    {
        Graphics2D graphics = (Graphics2D) getCurrentTextPane().getGraphics();
        if (svgGraph != null)
        {
            graphics = svgGraph;
        }
        MMAX2Document doc = getCurrentDocument();
        if (getRedrawAllOnNextRefresh())
        {            
            for (int z=0;z<markableRelationsToRender.size();z++)
            {
                ((Renderable)markableRelationsToRender.get(z)).select(graphics, doc, getCurrentPrimaryMarkable());
            }            
        }
        else
        {
            for (int z=0;z<markableRelationsToRender.size();z++)
            {
                ((Renderable)markableRelationsToRender.get(z)).refresh(graphics);
            }            
        }     
        setRedrawAllOnNextRefresh(false);
    }
                    
    
    public final void setRedrawAllOnNextRefresh(boolean status)
    {
        redrawAllOnNextRefresh = status;
    }

    public final boolean getRedrawAllOnNextRefresh()
    {
        return redrawAllOnNextRefresh;
    }
    
    public final void setCurrentPrimaryMarkable(Markable _markable)
    {
        currentPrimaryMarkable = _markable;
    }

    public final Markable getCurrentPrimaryMarkable()
    {
        return currentPrimaryMarkable;
    }
    
    public final void setCurrentSecondaryMarkable(Markable _markable)
    {
        currentSecondaryMarkable = _markable;
    }

    public final Markable getCurrentSecondaryMarkable()
    {
        return currentSecondaryMarkable;
    }
        
    /** Returns the JViewPort object of the JScrollPane object currently assigned to MMAX2.scrollPane. */
    public final JViewport getCurrentViewport()
    {
        return currentScrollPane.getViewport();
    }

    public final void setShowMarkableSetPeerWindow(boolean status)
    {
        this.showMarkableSetPeerWindow = status;
    }
    
    public final boolean getShowMarkableSetPeerWindow()
    {
        return showMarkableSetPeerWindow;
    }

    public final void setCreateSilently(boolean status)
    {
        this.createSilently = status;
    }
    
    public final boolean getCreateSilently()
    {
        return createSilently;
    }

    public final void setSelectAfterCreation(boolean status)
    {
        selectAfterCreation = status;
    }
    
    public final boolean getSelectAfterCreation()
    {
        return selectAfterCreation;
    }
    
    
    public final void setSuppressHandlesWhenRendering(boolean status)
    {
        suppressHandlesWhenRendering = status;
    }
    public final boolean getSuppressHandlesWhenRendering()
    {
        return suppressHandlesWhenRendering;
    }
    
    
    public void keyPressed(java.awt.event.KeyEvent keyEvent) 
    {
        
        int code = keyEvent.getKeyCode();
        if (code == MMAX2Constants.SHOW_FLOATING_ATTRIBUTEWINDOW_KEYCODE)
        {
            /** Update only if change has ocurred */ 
            {
                currentKey = MMAX2Constants.SHOW_FLOATING_ATTRIBUTEWINDOW_KEYCODE;
                getCurrentTextPane().setShowFloatingAttributeWindow(true);
                getCurrentTextPane().setCurrentHoveree(getCurrentTextPane().getCurrentHoveree(),getCurrentTextPane().getCurrentDot());
            }
        }
        else if (code == MMAX2Constants.HIGHLIGHT_CURRENT_FRAGMENT_ONLY_KEYCODE)
        {
            if (currentKey != MMAX2Constants.HIGHLIGHT_CURRENT_FRAGMENT_ONLY_KEYCODE) 
            {
                currentKey = MMAX2Constants.HIGHLIGHT_CURRENT_FRAGMENT_ONLY_KEYCODE;
                getCurrentTextPane().setShowHandlesOfCurrentFragmentOnly(true);
                getCurrentTextPane().setCurrentHoveree(getCurrentTextPane().getCurrentHoveree(),getCurrentTextPane().getCurrentDot());                
            }
        }
        /*
        else if ((code & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK)
        {
            System.err.println("alt");
        }        
        */
        
    }    
    
    public void keyReleased(java.awt.event.KeyEvent keyEvent) 
    {
        int code = keyEvent.getKeyCode();
        if (code == MMAX2Constants.SHOW_FLOATING_ATTRIBUTEWINDOW_KEYCODE)
        {
            currentKey = -1;
            getCurrentTextPane().setShowFloatingAttributeWindow(false);            
            getCurrentTextPane().deactivateFloatingAttributeWindow();            
            getCurrentTextPane().setCurrentHoveree(getCurrentTextPane().getCurrentHoveree(),getCurrentTextPane().getCurrentDot());            
            
        }
        else if (code == MMAX2Constants.HIGHLIGHT_CURRENT_FRAGMENT_ONLY_KEYCODE)
        {
            currentKey = -1;
            getCurrentTextPane().setShowHandlesOfCurrentFragmentOnly(false);
            getCurrentTextPane().setCurrentHoveree(getCurrentTextPane().getCurrentHoveree(),getCurrentTextPane().getCurrentDot());
        }
        
    }
    
    public void keyTyped(java.awt.event.KeyEvent keyEvent) 
    {
        
    }
    
    
   public final void showAnnotationHint(String text, String att)
    {
        if (hintWindow.isVisible()==false)
        {
            hintWindow.setVisible(true);
        }
        hintWindow.setText(text);        
        hintWindow.setTitle(att);
        if (getCurrentDiscourse().getCurrentMarkableChart().attributePanelContainer.isHintToFront())
        {
            hintWindow.toFront();
        }
    }
    
    public final void hideAnnotationHint()
    {
        hintWindow.setText("<html><body></body></html>");
    }
    
    public final void annotationHintToFront()
    {
    	
    }

    public final void annotationHintToBack()
    {
    	
    }    
    
    
    class MMAX2MainWindowListener extends WindowAdapter
    {
        public void windowActivated(WindowEvent we)
        {
            try
            {
                getCurrentTextPane().startAutoRefresh();               
            }
            catch (java.lang.NullPointerException ex)
            {

            }
        }
        
        public void windowDeiconified(WindowEvent we)
        {
            try
            {
                getCurrentTextPane().startAutoRefresh();
            }
            catch (java.lang.NullPointerException ex)
            {

            }
        }
        
        public void windowClosing(WindowEvent we)
        {
            requestExit();
        }
    }           
    
    public void finalize()
    {
        System.err.println("Finalizing MMAX2");
        try
        {
            super.finalize();
        }
        catch (java.lang.Throwable th)
        {
            th.printStackTrace();
        }
    }
    
    public void componentHidden(java.awt.event.ComponentEvent e) {
    }
    
    public void componentMoved(java.awt.event.ComponentEvent e) {
    }
    
    public void componentResized(java.awt.event.ComponentEvent e) 
    {
    	
    }
    
    public void componentShown(java.awt.event.ComponentEvent e) 
    {
        
    }
    
    public void setShowInMarkableSelectorAttribute(String level, String attribute)
    {
        markableSelectorAttributes.put(level, attribute);
        System.err.println(level+" "+attribute);
    }
    
    public String getShowInMarkableSelectorAttribute(String level)
    {
        String result =  (String) markableSelectorAttributes.get(level);
        if (result ==null)
        {
            result="";
        }
        return result;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String command = (String)e.getActionCommand();
        if (command.startsWith("set_showinselector_attribute"))
        {
            String level = command.substring(command.indexOf(" "),command.indexOf(":::")).trim();
            String attribute = command.substring(command.indexOf(":::")+3).trim();
            if (attribute.equals("<none>"))
            {
                attribute="";
            }
            setShowInMarkableSelectorAttribute(level,attribute);
        }
        else if (command.equalsIgnoreCase("print"))
        {            
            getCurrentTextPane().print();        
        }
        else if (command.equals("mlcp"))
        {
            if (getCurrentDiscourse()!=null)
            {
                getCurrentDiscourse().getCurrentMarkableChart().currentLevelControlWindow.setVisible(showMLCPBox.isSelected());
            }
        }
        else if (command.startsWith("auto-save"))
        {
        	String newTime = command.substring(command.indexOf(":")+1);
        	int num = Integer.parseInt(newTime);
        	requestSetAutoSaveInterval(num);        	
        }
    }
    
    public void requestSetAutoSaveInterval(int interval)
    {
    	System.err.println("Setting auto-save interval to "+interval+" minutes.");
    	if (autoSaveTimer == null) // If the timer has nevver been initialized
    	{
    		if (interval > 0)
    		{
    			autoSaveTimer = new Timer(60000*interval, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                        	executeAutoSave();
                        }                
                    });
    		}
    	}
    	else // The timer migh be running already
    	{
    		if (autoSaveTimer.isRunning())
    		{
    			autoSaveTimer.stop();
    		}
    		if (interval > 0) // only if the timer was not switched off
    		{
    			autoSaveTimer = new Timer(60000*interval, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                        	executeAutoSave();
                        }                
                    });
    		}
    	}
    	
    	if (interval > 0)
    	{    	
    		autoSaveTimer.setCoalesce(true);
    		autoSaveTimer.setRepeats(true);
    		autoSaveTimer.start();
    	}
    }
    
    public void executeAutoSave()
    {
    	MarkableLevel[] levels = getCurrentDiscourse().getCurrentMarkableChart().getActiveLevels();
    	System.err.println("Found "+levels.length+" active levels.");
    	for (int b=0;b<levels.length;b++)
    	{
    		levels[b].saveMarkables("",true);
    	}
    }
         
}