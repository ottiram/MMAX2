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

/*
 * Re-worked handling of casing for attribute names and values (1.15)
 */

package org.eml.MMAX2.annotation.scheme;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eml.MMAX2.annotation.markables.MarkableHelper;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.core.MMAX2;
import org.eml.MMAX2.utils.MMAX2Utils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MMAX2Attribute extends JPanel implements java.awt.event.ActionListener, javax.swing.event.DocumentListener, org.eml.MMAX2.api.AttributeAPI
{
    private String ID;   
    /** Name of the Attribute this MMAX2Attribute controls, as supplied in the annotation scheme (for display purposes) */
    private String displayAttributeName;    
    
    /** Whether this Attribute is currently frozen. */
    private boolean frozen = false;    
    /** Number of options for this Attribute (for exclusive nominal attributes only). */
//    private int size;
    /** Reference to the AnnotationScheme object that this Attribute is defined in / part of */    
    private MMAX2AnnotationScheme annotationscheme;            
    /** Type of this Attribute: AttributeAPI.NOMINAL_BUTTON, AttributeAPI.NOMINAL_LIST, AttributeAPI.FREETEXT, AttributeAPI.MARKABLE_SET, AttributeAPI.MARKABLE_POINTER, ... */
    private int type;
    
    private String toShowInFlag="";
    
    private int 	lineWidth;
    private Color 	lineColor;
    private int 	lineStyle;
    private int 	maxSize;
    
    private boolean dashed;
    private String add_to_markableset_instruction;
    private String remove_from_markableset_instruction;
    private String adopt_into_markableset_instruction;
    private String merge_into_markableset_instruction;
    private String point_to_markable_instruction;
    private String remove_pointer_to_markable_instruction;    
    private MarkableRelation markableRelation;    
    private String targetDomain;    
    private UIMATypeMapping uimaTypeMapping;
    
    // For type==NOMINAL_BUTTON: JButton to be set when all buttons are to be unset
    JRadioButton invisibleButton;
    
    // Contains at position x the string value of the button / list item at position x. Used in getDefaultValue, getSelectedValue, setSelectedValue and getDefault 
    // TODO Value strings should not be lower-cased here, because we might need the returned values for display, and for this they need to have the correct casing
    // DONE: This now contains the values as they are defined in the anno scheme
    private ArrayList<String> valueIndicesToValueStrings;

    /* Here, values are used as keys, so they need to be normalized */
    /* For retrieving a button index for a value, the value can be lower-cased ad-hoc */
    private Hashtable<String, Integer> lowerCasedValueStringsToValueIndices;
    
    // New 1.15: This maps a lower-cased value string (as key) to the correctly cased value string (as value)
    // It is used for finding the correct value name in cases where values in stored annotations (legacy) do not conform to the spelling in the anno scheme
    private Hashtable<String, String> lowerCasedValueStringsToValueStrings;
    
    // Contains all buttons
    private ArrayList<JRadioButton> buttons;
    
    /* Contains on index c the 'next' value of the JRadioButton / ListItem at index c */
    private ArrayList<String> nextAttributes;
    
    /* Groups the JRadioButtons for each SchemeLevel */
    ButtonGroup group = null;

    // For type==NOMINAL_LIST
    private JComboBox<String> listSelector = null;    
    // For type==FREETEXT
    JTextArea freetextArea;
    JScrollPane scrollPane;
    // For type==MARKABLE_SET, MARKABLE_POINTER
    JLabel idLabel;     	    
    JLabel attributeLabel;    
               
    public boolean isBranching = false;
    public boolean readOnly = false;
    String tooltiptext;    
    public String oldValue = "";
    int currentIndex = -1;
    
    private String noneAvailableForValue = "<no hint available for this value>";
    
    private ArrayList<MMAX2Attribute> dependsOn 	= new ArrayList<MMAX2Attribute>();    
    private ArrayList<String> orderedValues = new ArrayList<String>();
    
    /** Creates new SchemeLevel. attributeName is in the original spelling (upper/lower case) as supplied in the annotation scheme. */
    /* New in 1.15: Maintain casing for attributes and values, but make all comparisons case-insensitive */
    public MMAX2Attribute(String id, String attributeName, int _type, NodeList allChildren, MMAX2AnnotationScheme currentScheme, int width, String tiptext, String hintText, int _lineWidth, Color _color, int _lineStyle, int _maxSize, String _targetDomain, String _add_instruction, String _remove_instruction, String _adopt_instruction, String _merge_instruction, String _point_to_markable_instruction, String _remove_pointer_to_markable_instruction, float fontSize, boolean _dashed, String _toShowInFlag, UIMATypeMapping _uimaTypeMapping)
    {                                    
        boolean compact = false;
    	String compactVar = System.getProperty("compact");
    	if (compactVar != null && compactVar.equalsIgnoreCase("true")) { compact = true; }
        if (compact) { fontSize-=3;}
                                                
        setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        toShowInFlag = _toShowInFlag;        
        dashed = _dashed;
        // TODO: Support ordering for relation-type attributes
        type = _type;        
        // tiptext is the text on the 'text' attribute on the attribute
        // It should only be used for tooltips, and not for annotationhints
        tooltiptext = tiptext;        
        final String tempName = attributeName;
        final String tip = tooltiptext;
        final String tempHintText=hintText;
        
        if (currentScheme.isVerbose()) {System.err.println("\n    Attribute '"+attributeName +"' (type "+type+"):");}
                
        ID 						= id;
        displayAttributeName 	= attributeName;
//        size 					= 0;
        annotationscheme 		= currentScheme;
        lineWidth 				= _lineWidth;
        lineColor 				= _color;
        lineStyle 				= _lineStyle;
        maxSize 				= _maxSize;
        targetDomain 			= _targetDomain;
        
        add_to_markableset_instruction 			= _add_instruction;
        remove_from_markableset_instruction 	= _remove_instruction;
        adopt_into_markableset_instruction 		= _adopt_instruction;
        merge_into_markableset_instruction 		= _merge_instruction;        
        point_to_markable_instruction 			= _point_to_markable_instruction;
        remove_pointer_to_markable_instruction 	= _remove_pointer_to_markable_instruction;
                
        // Init list of MMAX2Attributes this one points to, if any
        nextAttributes = new ArrayList<String>();                     
        uimaTypeMapping = _uimaTypeMapping;        
        String filler = "";
        for (int q=0;q<width+3;q++)
        {
            filler = filler + " ";
        }                
        
        attributeLabel = new JLabel(displayAttributeName);     
        attributeLabel.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
        if (MMAX2.getStandardFont() != null) { attributeLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize)); }

        // TODO Support different colors for different attribute labels
        attributeLabel.setForeground(Color.darkGray);               
        if (tooltiptext.equals("")==false){ attributeLabel.setToolTipText(tooltiptext); }

        final MMAX2AnnotationScheme schemeCopy = currentScheme;

        if (tempHintText.equals("")==false)
        {
            attributeLabel.addMouseListener(
            new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me) { schemeCopy.showAnnotationHint(tempHintText,false,tempName); }
                public void mouseExited(java.awt.event.MouseEvent me) { schemeCopy.hideAnnotationHint(); }
                public void mouseClicked(java.awt.event.MouseEvent me)
                {
                    if (me.getButton()==java.awt.event.MouseEvent.BUTTON3)
                    {
                        schemeCopy.showAnnotationHint(tempHintText,true,tempName);
                        return;
                    }
                }
            }
            );
        }

        setAlignmentX(JComponent.LEFT_ALIGNMENT);
        setLayout(new FlowLayout(FlowLayout.LEADING,0,0));

        /* Create left to right Box to accept label and (JRadioButtons or JComboBox) */                
        Box innerBox = Box.createHorizontalBox();//                
        Box labelBox = Box.createVerticalBox();
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
                
        JPanel buttonBox = new JPanel();        
        
        /* Add Attribute Name as first element */        
        attributeLabel.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
        attributeLabel.addMouseListener(
        new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me) { if (schemeCopy.getAttributePanel().getContainer().isHintToFront()) { schemeCopy.annotationHintToFront(); }}
                public void mouseExited(java.awt.event.MouseEvent me) { }
            });        

        labelPanel.add(attributeLabel);

        // TODO Vary this to optimize layout
        labelBox.add(Box.createHorizontalStrut(120));
                        
        labelBox.add(labelPanel);//
        innerBox.add(labelBox);
        
        Node currentNode = null;
        String nextValue = "";

        String currentValue = "";
        String tempText = "";

        JRadioButton currentButton = null;
        invisibleButton = new JRadioButton();
        group = new ButtonGroup();
        group.add(invisibleButton);                    
        buttons = new ArrayList<JRadioButton>();

        // TODO Can these be merged into one list, using index and direct lookup ?
        valueIndicesToValueStrings = new ArrayList<String>();
        lowerCasedValueStringsToValueIndices = new Hashtable<String, Integer>();                        

        // New in 1.15
        lowerCasedValueStringsToValueStrings = new Hashtable<String, String>();        
        
        /* Iterate over allChildren (i.e. <value> elements from <attribute> XML element) */
        for (int z=0;z<allChildren.getLength();z++)
        {                        
            /* Get current child node */
            currentNode = allChildren.item(z);           
                        
            /* Only if current child is of type ELEMENT */
            if (currentNode.getNodeType() == Node.ELEMENT_NODE)
            {          
            	
// begin tool tip stuff            	
            	// text is used as content of the annotation hint window 
                /* Try to extract value of 'text' attribute from <value> element */
                try { tempText = currentNode.getAttributes().getNamedItem("text").getNodeValue(); }
                catch (java.lang.NullPointerException ex) { tempText=""; }               

                // Get HTML file with anno hint content
                String descFileName = "";
                try { descFileName = currentNode.getAttributes().getNamedItem("description").getNodeValue();}
                catch (java.lang.NullPointerException ex){ }
                
                // This will override any text supplied above!
                // Get HTML content for anno hint
                if (descFileName.equals("")==false)
                {
                    String schemeFileName = annotationscheme.getSchemeFileName();
                    tempText = MMAX2AnnotationScheme.readHTMLFromFile(schemeFileName.substring(0,schemeFileName.lastIndexOf(File.separator)+1)+descFileName);
                }
                                
                if (tempText.equals(""))
                {
                    // No text attribute, so try to extract (longer) text from <longtext> child
                    NodeList valueChildren = currentNode.getChildNodes();                    
                    if (valueChildren != null)
                    {
                        for (int q=0;q<valueChildren.getLength();q++)
                        {
                            Node valueChild = (Node) valueChildren.item(q);
                            if (valueChild.getNodeName().equalsIgnoreCase("longtext"))
                            {
                                try { tempText = "<html>"+valueChild.getFirstChild().getNodeValue()+"</html>";}
                                catch (java.lang.NumberFormatException ex) { tempText=""; }
                                break;
                            }   
                        }
                    }
                }
                
                if (tempText.equals(""))
                {
                    /* Try to extract value of 'id' attribute from <value> element, as a last resort to display in anno hint window */
                    try { tempText = currentNode.getAttributes().getNamedItem("id").getNodeValue(); }
                    catch (java.lang.NullPointerException ex) { }
                }
                                
                if (tempText.equals("") == false)
                {
                    // New February 18, 2005: Replace { with < and } with >
                    tempText = tempText.replaceAll("\\{","<");
                    tempText = tempText.replaceAll("\\}",">");
                }
                                
                /* Make final copy of current 'text' for use in ME */
                final String currentText = tempText;                
                /* Get 'name' of this <value> element */
                try { currentValue = currentNode.getAttributes().getNamedItem("name").getNodeValue();}
                catch (java.lang.NullPointerException ex) { System.out.println("Error: No 'name' attribute for <value> "+currentNode); }                
                
                // TODO Fix tooltip vs anno hint
// end tool tip stuff
                                                               
                // Start handling actual attribute and values
                // These two have a lot in common
                if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST)
                {
                    // NEW 1.15 : Add val name to list of ordered values (for oneclickanno) for this attribute
                    orderedValues.add(currentValue);

                    // Create mapping from lower-cased to correctly cased value string
                    // This is used for retrieving, for a given value string, the correct value spelling
                    lowerCasedValueStringsToValueStrings.put(currentValue.toLowerCase(), currentValue); 

                    // Create mapping from val index to name of value at this index
                    valueIndicesToValueStrings.add(currentValue);
                    // TODO Replace size with e.g. len(ordered values)
                    //lowerCasedValueStringsToValueIndices.put(currentValue.toLowerCase(), size);
                    // This should map the first value to index 0
                    lowerCasedValueStringsToValueIndices.put(currentValue.toLowerCase(), lowerCasedValueStringsToValueIndices.size());
                    
                    /* Get value of 'next' attribute */
                    try { nextValue = currentNode.getAttributes().getNamedItem("next").getNodeValue(); }
                    catch (java.lang.NullPointerException ex) { nextValue =""; }

                    // There is a next value associated with the current possible value. So this attribute is branching.
                    if (nextValue.equals("")==false) 
                    {
                    	isBranching = true;
                    	if (currentScheme.isVerbose()) { System.err.println("     "+currentValue +" --> "+nextValue); }
                    }
                    else
                    {
                    	if (currentScheme.isVerbose()) { System.err.println("     "+currentValue);}
                    }
                    /* Store 'next' value of Button c at position c (this might be empty!) */
                    nextAttributes.add(nextValue);
//                    size++;
                }
                	
                if (type == AttributeAPI.NOMINAL_BUTTON)
                {                    
                    /* Create one JRadioButton for each value, using correct spelling */
                    currentButton = null;
                    currentButton = new JRadioButton(currentValue);
                    if (MMAX2.getStandardFont() != null) { currentButton.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));}
                    currentButton.addActionListener(this);
                    
                    /* Tell button which number it is (-1 because it was added to the list already) */
                    currentButton.setActionCommand(lowerCasedValueStringsToValueIndices.size()-1+"");
                    /* Store Button itself */
                    buttons.add(currentButton);
                    //                    
                    // Handle anno hint stuff
                    final String currentAtt=displayAttributeName+":"+currentValue;                   
                    /* Set anno hint text */
                    if (currentText.equals(noneAvailableForValue)==false)
                    {
                    	// New 1.15
                    	currentButton.setToolTipText(currentText);                    	
                        currentButton.addMouseListener( 
                        new java.awt.event.MouseAdapter()
                        {
                            public void mouseEntered(java.awt.event.MouseEvent me) { schemeCopy.showAnnotationHint(currentText,false,currentAtt); }
                            public void mouseExited(java.awt.event.MouseEvent me) { schemeCopy.hideAnnotationHint(); }                            
                            public void mouseClicked(java.awt.event.MouseEvent me)
                            {
                                if (me.getButton()==java.awt.event.MouseEvent.BUTTON3)
                                {
                                    schemeCopy.showAnnotationHint(currentText,true,currentAtt);
                                    return;
                                }
                            }
                        }
                        );
                    }

                    /* Add button to ButtonGroup, so selection is mutually exclusive */
                    group.add(currentButton);
                    /* Add button to display, so button is visible */                    
                    buttonBox.add(currentButton);
                }//type = button
  
                else if (type == AttributeAPI.NOMINAL_LIST)
                {                                        
                    /* Init listSelector */
                    if (listSelector == null) 
                    {
                        listSelector = new JComboBox<String>();
                        if (MMAX2.getStandardFont() != null) { listSelector.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize)); }
                    }
                                                            
                    listSelector.addItem(currentValue);
                    /* Add JComboBox to display only once */
                    if (listSelector.getItemCount()==1) { buttonBox.add(listSelector); }
                }//type = button
                                
                else if (this.type == AttributeAPI.FREETEXT)
                {
                	/* type = freetext */
                	int ft_cols = 10;    	
                	try { ft_cols = Integer.parseInt(System.getProperty("freetext_field_columns")); }
                	catch (java.lang.NullPointerException | java.lang.NumberFormatException ex) {}
                	int ft_font_inc = 0;
                	try { ft_font_inc = Integer.parseInt(System.getProperty("freetext_font_increase")); }
                	catch (java.lang.NullPointerException | java.lang.NumberFormatException ex) {}
                	                	
                    freetextArea = new JTextArea(1,ft_cols);
                    freetextArea.getDocument().addDocumentListener(this);
                    freetextArea.setLineWrap(false);
                    freetextArea.setWrapStyleWord(true);
                    //if (MMAX2.getStandardFont() != null) { freetextArea.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize)); }
                    if (MMAX2.getStandardFont() != null) { freetextArea.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize+ft_font_inc)); }                    

                    scrollPane = new JScrollPane(freetextArea);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    buttonBox.add(scrollPane);
                    freetextArea.setVisible(true);
                }// type = freetext
                
                else if (type == AttributeAPI.MARKABLE_POINTER)
                {
                    /* type = markable_pointer */
                    if (idLabel == null)
                    {
                        idLabel = new JLabel(MMAX2.defaultRelationValue);
                        if (MMAX2.getStandardFont() != null) { idLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));}
                        idLabel.setEnabled(false);
                        buttonBox.add(idLabel);
                    }
                                        
                    /* Get value of 'next' attribute */
                    try { nextValue = currentNode.getAttributes().getNamedItem("next").getNodeValue(); }
                    catch (java.lang.NullPointerException ex) { nextValue =""; }

                    // There is a next value associated with the current possible value. So this attribute is branching.
                    if (nextValue.equals("")==false) 
                    {
                    	isBranching = true;
                    	if (currentScheme.isVerbose()) { System.err.println("      --> "+nextValue); }                    	
                    }                                                            
                    /* Store 'next' value of Button c at position c (this might be empty!) */
                    nextAttributes.add(nextValue);
                    
                }
                else if (type == AttributeAPI.MARKABLE_SET)
                {
                    /* type = markable_SET */
                	/* TODO Make sets branching as well ... */
                    idLabel = new JLabel(MMAX2.defaultRelationValue);
                    if (MMAX2.getStandardFont() != null) { idLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));}
                    idLabel.setEnabled(false);
                    buttonBox.add(idLabel);
                }                              
            }//ELEMENT_NODE
        }// for z
        
        if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST)
        {
        	if (currentScheme.isDebug()) System.err.println("     Name mappings:            "+ lowerCasedValueStringsToValueStrings);
        	if (currentScheme.isDebug()) System.err.println("     Index to String mappings: "+ valueIndicesToValueStrings);
        	if (currentScheme.isDebug()) System.err.println("     String to Index mappings: "+ lowerCasedValueStringsToValueIndices);
        }
        
        if (listSelector != null) listSelector.addActionListener(this);
        if (isBranching) { attributeLabel.setText("< > "+attributeLabel.getText()); }        
        innerBox.add(buttonBox);
        this.add(innerBox); 
    }
        
    public final String getNormalizedValueName(String name)
    {
    	if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST)
    	{
    		return this.lowerCasedValueStringsToValueStrings.get(name.toLowerCase());
    	}
    	else
    		{ return name; }
    }
    
    public final String getDisplayName()
    {
        return displayAttributeName;
    }
    
    public final ArrayList<String> getOrderedValues()
    {
        return orderedValues;
    }
        
    public final MMAX2Attribute[] getDirectlyDependentAttributes()
    {
        ArrayList<MMAX2Attribute> temp = new ArrayList<MMAX2Attribute>();
        if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST || type==AttributeAPI.MARKABLE_POINTER)
        {
            // Iterate over all possible values defined for this attribute
            for (int z=0;z<nextAttributes.size();z++)
            {
                String nextVal = (String) nextAttributes.get(z);
                // next attribute *IDs* come as they are specified in the next attributes in the scheme file
                if (nextVal.equals("")==false)
                {
                    /* Parse String into List of Ids */
                    ArrayList tempresult = MarkableHelper.parseCompleteSpan(nextVal);
                    /* Iterate over all IDs found */
                    for (int p=0;p<tempresult.size();p++)
                    {
                    	// New 1.15: Lowercase ids found in next attribute before lookup 
                        MMAX2Attribute currentAttrib = (MMAX2Attribute) annotationscheme.getAttributeByID(((String) tempresult.get(p)).toLowerCase() );                        
                        if (currentAttrib != null)
                        {                                                    
                            /* Add each Attribute to result only once */                               
                            if (temp.contains(currentAttrib)==false) { temp.add(currentAttrib); }
                        }
                        else { System.err.println("Dependent attribute "+(String) tempresult.get(p)+" not found!"); }
                    }                    
                }
            }
        }        
        return (MMAX2Attribute[])temp.toArray(new MMAX2Attribute[0]);
    }
    
    public final String getAttributeNameToShowInMarkablePointerFlag()
    {
        return toShowInFlag;
    }
    
    public final void destroy()
    {
        markableRelation = null;
        annotationscheme = null;       
    }
    
    public final boolean inDomain(String domain)
    {
        boolean result = false;
        if (targetDomain.equals("")) 
        {
            result = true;
        }
        else
        {
            if (targetDomain.equals(domain) ||
                targetDomain.startsWith(domain+",") ||
                targetDomain.endsWith(","+domain) ||
                targetDomain.indexOf(","+domain+",")!=-1)
            {
                result=true;
            }
        }
        return result;
    }
    
    public final UIMATypeMapping getUIMATypeMapping()
    {
    	return uimaTypeMapping;
    }
    
    public final String getAddToMarkablesetInstruction()
    {
        return this.add_to_markableset_instruction;
    }
    
    public final String getRemoveFromMarkablesetInstruction()
    {
        return this.remove_from_markableset_instruction;
    }

    public final String getAdoptIntoMarkablesetInstruction()
    {
        return this.adopt_into_markableset_instruction;
    }

    public final String getMergeIntoMarkablesetInstruction()
    {
        return this.merge_into_markableset_instruction;
    }

    public final String getPointToMarkableInstruction()
    {
        return this.point_to_markable_instruction;
    }
    
    public final String getRemovePointerToMarkableInstruction()
    {
        return this.remove_pointer_to_markable_instruction;
    }

    public final void setMarkableRelation(MarkableRelation mrelation)
    {
        markableRelation = mrelation;
    }
    
    public final MarkableRelation getMarkableRelation()
    {
        return markableRelation;
    }
    
    /** This method returns the correctly cased default value for this attribute */
    // New 1.15: Values for NOMINAL_* are not lower-cased any more
    public String getDefaultValue()
    {
        String result = "";
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {
            result = (String) valueIndicesToValueStrings.get(0);
        }
        else if (type == AttributeAPI.NOMINAL_LIST)
        {
            result = (String) listSelector.getItemAt(0);
        }        
        else if (type == AttributeAPI.FREETEXT)
        {
            result = "";
        }
        else if (type == AttributeAPI.MARKABLE_SET || type == AttributeAPI.MARKABLE_POINTER)
        {
            result = MMAX2.defaultRelationValue;
        }
        return result;
    }
    
    public final boolean getIsDashed()
    {
        return dashed;
    }
    
    public final int getLineWidth()
    {
        return lineWidth;
    }
    
    public final Color getLineColor()
    {
        return lineColor;
    }

    public final int getLineStyle()
    {
        return lineStyle;
    }
    
    public final int getMaxSize()
    {
        return maxSize;
    }

        
    /** This handler is called upon the selection of a button or a box menu item on this Attribute. */
    public void actionPerformed(java.awt.event.ActionEvent p1) 
    {
//    	System.err.println(p1);
        /* Do nothing if the action was initiated automatically */
        if (annotationscheme.ignoreClick)  { return; }
                        
        if (type == AttributeAPI.NOMINAL_BUTTON)        
        {            
            // The attribute is rendered as a list of RadioButtons, so get index of clicked button, communicated through actionCommand
        	// index should be 0-based
            int position = Integer.parseInt(p1.getActionCommand());
            System.err.println(position);
            System.err.println(currentIndex);
            // Ignore if current value is clicked again
            if (position == currentIndex) { return; }            
            currentIndex = position;
            // A selection always resets any freezing 
            setIsFrozen(false,"");
            /* Call valueChanged with this MMAX2Attribute object as callingLevel */
            annotationscheme.valueChanged(this,this,null,position,new ArrayList());
        }
        else if (type == AttributeAPI.NOMINAL_LIST)
        {
            // The attribute is rendered as a drop down list, so get index of selected item
            int position = listSelector.getSelectedIndex();
            // Ignore if current value is chosen again
            if (position == currentIndex) { return; }
            currentIndex = position;            
            // A selection always resets any freezing
            setIsFrozen(false,"");
            /* Call valueChanged with this MMAX2Attribute object as callingLevel */
            annotationscheme.valueChanged(this,this,null,position,new ArrayList());
        }
    }
    
    /** This method returns an array of the MMAX2Attributes that *the current value* of this points to as 'next', or empty Array. */
    public MMAX2Attribute[] getNextAttributes(boolean toDefault)
    {
        MMAX2Attribute[] result = new MMAX2Attribute[0];
        // A frozen level does not point to any valid next levels
        if (frozen) return result;
        
        ArrayList<String> tempresult; // = new ArrayList();      
        int selIndex = -1;
        if (getType() == AttributeAPI.NOMINAL_BUTTON || getType() == AttributeAPI.NOMINAL_LIST)
        {
            /* Get Index of selected JRadioButton or List */
            selIndex = getSelectedIndex();                     
        }
        else if (getType() == AttributeAPI.MARKABLE_POINTER)
        {
            String currentValue = getSelectedValue();
            if (currentValue.equalsIgnoreCase(MMAX2.defaultRelationValue))  { selIndex = 0; }
            else 															{ selIndex = 1; }
        }
        else if (getType() == AttributeAPI.MARKABLE_SET || getType()== AttributeAPI.FREETEXT)
        {
            return result;
        }
//        else
//        {
//            System.out.println("Error: Unknown attribute type! "+getDisplayAttributeName());
//            return result;            
//        }
            
        /* Get String of next attributes this points to (might be empty) */
        String nextString = (String) nextAttributes.get(selIndex);
        if (nextString.equals("")==false)
        {
            /* Parse String into List of Ids */
            tempresult = MarkableHelper.parseCompleteSpan(nextString);
            result = new MMAX2Attribute[tempresult.size()];
            /* Iterate over all IDs found */
            for (int p=0;p<tempresult.size();p++)
            {
                /* Add each Attribute to result, using the lower-cased id as key */                               
                result[p] = (MMAX2Attribute) annotationscheme.getAttributeByID(((String) tempresult.get(p)).toLowerCase());
                /* Reset to default */
                if (result[p] != null)
                {
                    if (toDefault) { ((MMAX2Attribute)result[p]).toDefault(); }
                }
                else { System.err.println("No Attribute with ID "+(String)tempresult.get(p)+"!"); }
            }
        }        
        return result;
    }
   
    
    /* This method returns the index of the currently selected JRadioButton / JListItem, or -1. It is used
       for determining the index of the current value, for retrieving the corresp. 'next' levels. */
    public int getSelectedIndex()
    {
        int index=-1;
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {
            JRadioButton currentButton=null;
            
            //for (int p=0;p<this.size;p++)
            for (int p=0;p<this.lowerCasedValueStringsToValueIndices.size();p++)
            {
                currentButton = (JRadioButton) buttons.get(p);
                if (currentButton.isSelected())
                {
                    index=p;
                    break;
                }
            }
        }
        else if (type == AttributeAPI.NOMINAL_LIST)
        {
            index = this.listSelector.getSelectedIndex();
        }
        else
        {
            System.err.println("getSelectedIndex not applicable for attribute "+this.attributeLabel.getText());
        }
        return index;
    }
    
    
    /* This method returns the value string associated with the currently selected JRadioButton/list item, or empty string */
    public String getSelectedValue()
    {
        if (frozen)  { return ""; }
        
        String value = "";
        JRadioButton currentButton=null;

        if (this.type == AttributeAPI.NOMINAL_BUTTON)
        {
        	
            //for (int p=0;p<this.size;p++)
        	for (int p=0;p<this.lowerCasedValueStringsToValueIndices.size();p++)
            {
                currentButton = (JRadioButton) buttons.get(p);
                if (currentButton.isSelected())
                {
                	// Fixed for 1.15: This now returns the value string in the correct case
                    value=((String) valueIndicesToValueStrings.get(p));
                    break;
                }
            }
        }
        else if (this.type == AttributeAPI.NOMINAL_LIST)
        {
            value = (String) listSelector.getSelectedItem();
        }        
        else if (this.type == AttributeAPI.FREETEXT)
        {
            try 										{ value = freetextArea.getText(); }
            catch (java.lang.NullPointerException ex) 	{ value = ""; }            
            
            value.trim();                        
            
//            /* Clean string for XML storage */
//            String tempresult = "";
//            String currentChar = "";
//            for (int z=0;z<value.length();z++)
//            {
//                currentChar = value.substring(z,z+1);
//                if (currentChar.equals("\"")) currentChar = "'";
//                else if (currentChar.equals("ä")) currentChar = "ae";
//                else if (currentChar.equals("ü")) currentChar = "ue";
//                else if (currentChar.equals("ö")) currentChar = "oe" ;               
//                else if (currentChar.equals("Ä")) currentChar = "AE";
//                else if (currentChar.equals("Ü")) currentChar = "UE";
//                else if (currentChar.equals("Ö")) currentChar = "OE" ;               
//                else if (currentChar.equals("<")) currentChar = "";
//                else if (currentChar.equals(">")) currentChar = "";
//                else if (currentChar.equals("ß")) currentChar = "ss";
//                else if (currentChar.equals("\n")) currentChar = " ";
//                
//                tempresult = tempresult + currentChar;                
//            }
//            value = tempresult.trim();      
//            System.err.println(value);
            
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            value = idLabel.getText();
        }
        else if (type == AttributeAPI.MARKABLE_POINTER) 
        {
            value = MMAX2Utils.expandTargetSpan(idLabel.getText());
        }
        return value;
    }
    
    /** This method tries to select the JRadioButton associated with the value desiredValue. 
        For attributes of type freetext or id, it sets the value to desiredValue.
        It returns true if the value was found, false otherwise. For attributes of type id and freetext, 
        result is always true, because no constraints can exist for these attributes.  FIXME
        If desiredValue is a null String, index 0 is set. */ 
    // NEW: if ignore = false, calling this will be equivalent to actually clicking the attribute
    // (Used for oneclickAnnotation and set_values, which is the only case where ignore = false
    public boolean setSelectedValue(String desiredValue, boolean ignore)
    {        
    	System.err.println(desiredValue);
        boolean result = false;
        int buttonPosition = 0;
        int itemPosition = -1;
        
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {        
            if (desiredValue == null || desiredValue.equals("") )
            {
                // Value is null or empty, so set to default with ignore = true
            	// This can only happen programmatically 
                setSelectedIndex(0);
                result = true;               
            }
            else
            {
                /** DesiredValue is not "" and not _mmax default value_ either */
                try
                {
                    /* Get index of JRadioButton to set, if any */
                	// New 1.15
                    //buttonPosition = ((Integer) lowerCasedValueStringsToButtonIndices.get(desiredValue)).intValue();
                	// before lookup of value, normalize it to the spelling that is known by
//                 	String normVal = lowerCasedValueStringsToValueStrings.get(desiredValue.toLowerCase());
                    //buttonPosition = ((Integer) lowerCasedValueStringsToValueIndices.get(lowerCasedValueStringsToValueStrings.get(desiredValue.toLowerCase()))).intValue();
                    buttonPosition = lowerCasedValueStringsToValueIndices.get(desiredValue.toLowerCase());
                }
                catch (java.lang.NullPointerException ex)
                {
                    /* No button available, so nothing to set */
                    // This means that the desired value is not defined for this attribute 
                    buttonPosition = -1;
                    result = false;
                }
                
                /* Only if a button was found */
                if (buttonPosition != -1)
                {
                    if (ignore) 	{ setSelectedIndex(buttonPosition); }
                    else 			{ ((JRadioButton) buttons.get(buttonPosition)).doClick(); }
                    result = true;
                }
                else { System.err.println("Error: Value "+desiredValue+" not found on attribute "+displayAttributeName+"!"); }
            }// else
        }
        else if (type == AttributeAPI.NOMINAL_LIST)
        {        
            if (desiredValue == null || desiredValue.equals(""))
            {
                listSelector.setSelectedIndex(0);
                result = true;
            }
            else
            {
                /** DesiredValue is not "" and not _mmax default value_ either */                                                              
                try
                {
                    /* Get index of JRadioButton to set, if any */
                    for (int z=0;z<listSelector.getItemCount();z++)
                    {   
                        // New 1.15. Now we can even use equals() because the casing MUST be identical
                        if (lowerCasedValueStringsToValueStrings.get(desiredValue.toLowerCase()).equals((String)listSelector.getItemAt(z)))
                        {
                            itemPosition = z;
                            break;
                        }
                    }
                }
                catch (java.lang.NullPointerException ex)
                {
                    /* No button available, so nothing to set */
                    // This means that the desired value is not defined for this attribute 
                    itemPosition = -1;
                    result = false;
                }
                
                /* Only if a button was found */
                if (itemPosition != -1)
                {
                    if (ignore) { annotationscheme.ignoreClick = true;}
                    listSelector.setSelectedIndex(itemPosition);
                    annotationscheme.ignoreClick = false;
                    currentIndex = itemPosition;
                    result = true;
                }
                else { System.err.println("Error: Value "+desiredValue+" not found on attribute "+this.displayAttributeName+"!"); }
            }// else
        }
        
        else if (type == AttributeAPI.FREETEXT)
        {
//        	System.err.println(desiredValue);        	
            if (desiredValue != null) { freetextArea.setText(desiredValue); }
            else { freetextArea.setText(""); }
            annotationscheme.ignoreClick = false;
            result = true;
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            if (desiredValue != null && desiredValue.equals("")==false && desiredValue.equals(MMAX2.defaultRelationValue)==false) { idLabel.setText(desiredValue); }
            else 																												  { idLabel.setText(MMAX2.defaultRelationValue); }
            result = true;
        }
        else if (type == AttributeAPI.MARKABLE_POINTER)
        {
            if (desiredValue != null && desiredValue.equals("")==false && desiredValue.equals(MMAX2.defaultRelationValue)==false) { idLabel.setText(MMAX2Utils.condenseTargetSpan(desiredValue)); }
            else 																											      { idLabel.setText(MMAX2.defaultRelationValue); }
            result = true;
        }
                return result;            
    }
    
    public final void addDependsOn(MMAX2Attribute attrib)
    {
        dependsOn.add(attrib);
    }
    
    public final boolean isIndependent()
    {
        return (dependsOn.size()==0);
    }
    
    public final boolean dependsOn(MMAX2Attribute superiorAttribute)
    {
        boolean result = false;
        if (dependsOn.contains(superiorAttribute))
        {
            // This depends on superiorAttribute if it directly depends on it
            result = true;
        }
        else
        {
            MMAX2Attribute currentAttribute = null;
            // Iterate over all attributes the current on depends on
            for (int z=0;z<dependsOn.size();z++)
            {
                currentAttribute = (MMAX2Attribute)dependsOn.get(z);
                if (currentAttribute.dependsOn(superiorAttribute))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
      
    public void setSelectedIndex(int num)
    {
        // This always uses ignore = true! 
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {
            annotationscheme.ignoreClick = true;
            ((JRadioButton) buttons.get(num)).setSelected(true);
            currentIndex = num;
            annotationscheme.ignoreClick = false;
        }
        else if (type == AttributeAPI.NOMINAL_LIST)
        {
            annotationscheme.ignoreClick = true;
            listSelector.setSelectedIndex(num);
            currentIndex = num;
            annotationscheme.ignoreClick = false;
        }            
    }
    
    public void setEnabled(boolean status)
    {
        /* No support for id type necessary */
        if (this.type == AttributeAPI.NOMINAL_BUTTON)
        {        	
            //for (int o=0;o<this.size;o++)
            for (int o=0;o<lowerCasedValueStringsToValueIndices.size();o++)
            {
                ((JRadioButton) buttons.get(o)).setEnabled(status);
                if (this.readOnly) ((JRadioButton) buttons.get(o)).setEnabled(false);
            }
        }
        else if (this.type == AttributeAPI.NOMINAL_LIST)
        {
            listSelector.setEnabled(status);
        }
        else if (this.type == AttributeAPI.FREETEXT)
        {
            this.freetextArea.setEnabled(status);
            this.freetextArea.setEditable(status);
        }        
    }
    
    /** This method resets this Schemelevel to default */
    public void toDefault()
    {  
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {
            annotationscheme.ignoreClick = true;
            ((JRadioButton) buttons.get(0)).setSelected(true);
            annotationscheme.ignoreClick = false;
            // NEW February 17, 2005: 
            // Make sure currentIndex is updated at setDefault
            currentIndex = 0;            
        } 
        if (type == AttributeAPI.NOMINAL_LIST)
        {
            annotationscheme.ignoreClick = true;
            listSelector.setSelectedIndex(0);
            annotationscheme.ignoreClick = false;
            // NEW February 17, 2005: 
            // Make sure currentIndex is updated at setDefault
            currentIndex = 0;
        }                
        else if (type == AttributeAPI.FREETEXT)
        {
            annotationscheme.ignoreClick = true;                
            freetextArea.setText("");
            annotationscheme.ignoreClick = false;
        }
        else if (type == AttributeAPI.MARKABLE_SET || type == AttributeAPI.MARKABLE_POINTER)
        {
            idLabel.setText(MMAX2.defaultRelationValue);
        }
    }
               
    public void removeUpdate(javax.swing.event.DocumentEvent p1) 
    {
        if  (annotationscheme.ignoreClick) return;        
        if (annotationscheme.getCurrentAttributePanel().hasUncommittedChanges==false) annotationscheme.getCurrentAttributePanel().setHasUncommittedChanges(true);
        annotationscheme.getCurrentAttributePanel().setApplyEnabled(true);
        annotationscheme.getCurrentAttributePanel().setUndoEnabled(true);        
    }
    
    public void changedUpdate(javax.swing.event.DocumentEvent p1) 
    {
        if (annotationscheme.ignoreClick) return;        
        if (annotationscheme.getCurrentAttributePanel().hasUncommittedChanges==false) annotationscheme.getCurrentAttributePanel().setHasUncommittedChanges(true);
        annotationscheme.getCurrentAttributePanel().setApplyEnabled(true);
        annotationscheme.getCurrentAttributePanel().setUndoEnabled(true);        
    }
    
    public void insertUpdate(javax.swing.event.DocumentEvent p1) 
    {
        if (annotationscheme.ignoreClick) return;               
        if (annotationscheme.getCurrentAttributePanel().hasUncommittedChanges==false) annotationscheme.getCurrentAttributePanel().setHasUncommittedChanges(true);
        annotationscheme.getCurrentAttributePanel().setApplyEnabled(true);
        annotationscheme.getCurrentAttributePanel().setUndoEnabled(true);
    }
    public int getType()
    {
        return type;
    }

    public final String decodeAttributeType()
    {
        String result = "unknown type !";
        if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST)
        {
            result = "(NOMINAL";
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            result = "(MARKABLE_SET";
        }
        else if (type == AttributeAPI.MARKABLE_POINTER)
        {
            result = "(MARKABLE_POINTER";
        }
        else if (type == AttributeAPI.FREETEXT)
        {
            result = "(FREETEXT";
        }
        
        if (this.isBranching)
        {
            result = result +", branching)";
        }
        else
        {
            result = result + ")";
        }
        return result;
    }
    
    public boolean isDefined(String value)
    {
        /* Constraints exist for nominal attributes only !! */    	
        if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST)
        {
            //return buttonIndicesToValueStrings.contains(value);
        	// New 1.15
        	return lowerCasedValueStringsToValueStrings.containsKey(value.toLowerCase());
        }
        // MODIFIED March 22, 2005: This used to default to true
        else if (type == AttributeAPI.FREETEXT)
        {
            // Any value is defined for a freetext attribute
            return true;
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            if (value.equalsIgnoreCase("empty")==true) 			{ return true; }
            else if (value.equalsIgnoreCase("initial")==true)	{ return true; }
            else if (value.equalsIgnoreCase("final")==true) 	{ return true; }            
            else 												{ return false; }            
        }
        else if (type == AttributeAPI.MARKABLE_POINTER)
        {
            if (value.equalsIgnoreCase("empty")==true) 			{ return true; }
            else if (value.equalsIgnoreCase("target")==true) 	{ return true; }
            else 												{ return false; }
        }
        else { return false; }
    }
    
    public final String getID()
    {
        return ID;
    }
    
    public final boolean getIsBranching()
    {
        return this.isBranching;
    }

    public final boolean getIsFrozen()
    {
        return this.frozen;
    }

    public final boolean getIsReadOnly()
    {
        return this.readOnly;
    }
        
    public void setIsFrozen(boolean status, String illegalValue)
    {
        if (status == true)
        {
            if (type == AttributeAPI.NOMINAL_BUTTON)
            {
                String warning = "Illegal attribute value: '"+illegalValue+"'";
                invisibleButton.setSelected(true);
                attributeLabel.setForeground(Color.red);
                attributeLabel.setToolTipText(warning);
                frozen = true;
            }
            else if (type == AttributeAPI.NOMINAL_LIST)
            {
                String warning = "Illegal attribute value: '"+illegalValue+"'";
                attributeLabel.setForeground(Color.red);
                attributeLabel.setToolTipText(warning);
                frozen = true;
            }

        }
        else if (frozen == true)
        {
            attributeLabel.setForeground(Color.darkGray);
            attributeLabel.setToolTipText(tooltiptext);
            toDefault(); // Set to default
            frozen = false;
        }                
    }        
}
