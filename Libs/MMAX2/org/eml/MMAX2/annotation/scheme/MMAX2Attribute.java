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
    /** Name of the Attribute this MMAX2Attribute controls, set to lower (for matching purposes & writing to XML) */
    private String lowerCasedAttributeName;            
    /** Whether this Attribute is currently frozen. */
    private boolean frozen = false;    
    /** Number of options for this Attribute (for exclusive nominal attributes only). */
    private int size;
    /** Reference to the AnnotationScheme object that this Attribute is defined in / part of */    
    private MMAX2AnnotationScheme annotationscheme;            
    /** Type of this Attribute: AttributeAPI.NOMINAL_BUTTON, AttributeAPI.NOMINAL_LIST, AttributeAPI.FREETEXT, AttributeAPI.MARKABLE_SET, AttributeAPI.MARKABLE_POINTER, ... */
    private int type;
    
    private String toShowInFlag="";
    
    private int lineWidth;
    private Color lineColor;
    private int lineStyle;
    private int maxSize;
    
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
    
    // For type==NOMINAL_BUTTON
    // JButton to be set when all buttons are to be unset
    JRadioButton invisibleButton;
    
    // Contains at position x the string value of the button / list item at position x
    // Used in getDefaultValue, getSelectedValue, setSelectedValue and getDefault
    private ArrayList buttonIndicesToLowerCasedValueStrings;
    // Contains all buttons
    private ArrayList buttons;
    
    /* Contains on index c the 'next' value of the JRadioButton / ListItem at index c */
    private ArrayList nextAttributes;
    
    private Hashtable lowerCasedValueStringsToButtonIndices;
    /* Groups the JRadioButtons for each SchemeLevel */
    ButtonGroup group = null;

    // For type==NOMINAL_LIST
    private JComboBox listSelector = null;
    
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
//    private String noneAvailableForAttribute = "<no hint available for this attribute>";
    
    private ArrayList dependsOn = new ArrayList();
    
    private ArrayList orderedValues = new ArrayList();
    
    /** Creates new SchemeLevel. attributeName is in the original spelling (upper/lower case) as supplied in the annotation scheme. */
    /* All attribute _values_ are stored and displayed in lowercase */
    public MMAX2Attribute(String id, String attributeName, int _type, NodeList allChildren, MMAX2AnnotationScheme currentScheme, int width, String tiptext, String hintText, int _lineWidth, Color _color, int _lineStyle, int _maxSize, String _targetDomain, String _add_instruction, String _remove_instruction, String _adopt_instruction, String _merge_instruction, String _point_to_markable_instruction, String _remove_pointer_to_markable_instruction, float fontSize, boolean _dashed, String _toShowInFlag, UIMATypeMapping _uimaTypeMapping)
    {            
        super();
        
        setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        toShowInFlag = _toShowInFlag;
        
        dashed = _dashed;
        //setBorder(new EmptyBorder(0,0,1,1));
        // TODO: Support ordering for relation-type attributes
        type = _type;        
        // tiptext is the text on the 'text' attrbute on the attribute
        // It should only be used for tooltips, and not for annotationhints
        tooltiptext = tiptext;        
        final String tempName = attributeName;
        final String tip = tooltiptext;
        final String tempHintText=hintText;
        
        ID = id;
        displayAttributeName = attributeName;
        lowerCasedAttributeName = attributeName.toLowerCase();
        size = 0;
        annotationscheme = currentScheme;
        lineWidth = _lineWidth;
        lineColor = _color;
        lineStyle = _lineStyle;
        maxSize = _maxSize;
        targetDomain = _targetDomain;
        
        add_to_markableset_instruction = _add_instruction;
        remove_from_markableset_instruction = _remove_instruction;
        adopt_into_markableset_instruction = _adopt_instruction;
        merge_into_markableset_instruction = _merge_instruction;
        
        point_to_markable_instruction = _point_to_markable_instruction;
        remove_pointer_to_markable_instruction = _remove_pointer_to_markable_instruction;
                
        // Init list of MMAX2Attributes this one points to, if any
        nextAttributes = new ArrayList();      
               
        uimaTypeMapping = _uimaTypeMapping;
        
        String filler = "";
        for (int q=0;q<width+3;q++)
        {
            filler = filler + " ";
        }                
        
        attributeLabel = new JLabel(displayAttributeName);     
        attributeLabel.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
        if (MMAX2.getStandardFont() != null)
        {
            attributeLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));
        }
        attributeLabel.setForeground(Color.darkGray);               
        if (tooltiptext.equals("")==false)
        {
            attributeLabel.setToolTipText(tooltiptext);
        }
        final MMAX2AnnotationScheme schemeCopy = currentScheme;                
        if (tempHintText.equals("")==false)
        {
            attributeLabel.addMouseListener(
            new java.awt.event.MouseAdapter()
            {
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    schemeCopy.showAnnotationHint(tempHintText,false,tempName);
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                    schemeCopy.hideAnnotationHint();
                }

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
                public void mouseEntered(java.awt.event.MouseEvent me)
                {
                    if (schemeCopy.getAttributePanel().getContainer().isHintToFront())
                    {
                        schemeCopy.annotationHintToFront();
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent me)
                {
                }
            });        

        labelPanel.add(attributeLabel);
        
        //labelBox.add(attributeLabel); //
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
        buttonIndicesToLowerCasedValueStrings = new ArrayList();
        buttons = new ArrayList();
        lowerCasedValueStringsToButtonIndices = new Hashtable();                
        
        /* Iterate over allChildren (i.e. <value> elements from <attribute> XML element) */
        // This loop is processed once for every attribute, so no resetting of defaultIsBranching is required
        for (int z=0;z<allChildren.getLength();z++)
        {                        
            /* Get current child node */
            currentNode = allChildren.item(z);
            
            /* Only if current child is of type ELEMENT */
            if (currentNode.getNodeType() == Node.ELEMENT_NODE)
            {                                                 
                /* Try to extract value of 'text' attribute from <value> element */
                try
                {
                    tempText = currentNode.getAttributes().getNamedItem("text").getNodeValue();
                }
                catch (java.lang.NullPointerException ex)
                {
                    tempText="";
                }
                
                String descFileName = "";
                try
                {
                    descFileName = currentNode.getAttributes().getNamedItem("description").getNodeValue();
                }
                catch (java.lang.NullPointerException ex)
                {
                    
                }
                
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
                                try
                                {
                                    tempText = "<html>"+valueChild.getFirstChild().getNodeValue()+"</html>";
                                }
                                catch (java.lang.NumberFormatException ex)
                                {
                                    tempText="";
                                }
                                break;
                            }   
                        }
                    }
                }
                
                if (tempText.equals(""))
                {
                    /* Try to extract value of 'id' attribute from <value> element */
                    try
                    {
                        tempText = currentNode.getAttributes().getNamedItem("id").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                    }
                }
                                
                /* If attribute is there, but empty, use noneAvailableForValue */
                if (tempText.equals("")) 
                {                    
                    // tempText = noneAvailableForValue;
                }
                else
                {
                    // New February 18, 2005: Replace { with < and } with >
                    tempText = tempText.replaceAll("\\{","<");
                    tempText = tempText.replaceAll("\\}",">");
                }
                                
                /* Make final copy of current 'text' for use in ME */
                final String currentText = tempText;                
                /* Get 'name' of this <value> element */
                try
                {
                    currentValue = currentNode.getAttributes().getNamedItem("name").getNodeValue().toLowerCase();
                }
                catch (java.lang.NullPointerException ex)
                {
                    System.out.println("Error: No 'name' attribute for <value> "+currentNode);
                }
                
                // NEW: Add to list of ordered values (for oneclickanno)
                orderedValues.add(currentValue);
                
                /* Make final copy of value for use in ME */
                final String valueName = currentValue;
                
                if (type == AttributeAPI.NOMINAL_BUTTON)
                {
                    /* Create one JRadioButton for each value (lower cased !) */                    
                    currentButton = null;
                    currentButton = new JRadioButton(currentValue);
                    if (MMAX2.getStandardFont() != null)
                    {
                        currentButton.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));  
                    }
                    
                    final String currentAtt=displayAttributeName+":"+currentValue;
                    
                    /* Set tool tip */
                    if (currentText.equals(noneAvailableForValue)==false)
                    {
                        currentButton.addMouseListener(
                        new java.awt.event.MouseAdapter()
                        {
                            public void mouseEntered(java.awt.event.MouseEvent me)
                            {
                                schemeCopy.showAnnotationHint(currentText,false,currentAtt);
                            }
                            public void mouseExited(java.awt.event.MouseEvent me)
                            {
                                schemeCopy.hideAnnotationHint();
                            }
                            
                            public void mouseClicked(java.awt.event.MouseEvent me)
                            {
                                if (me.getButton()==java.awt.event.MouseEvent.BUTTON3)
                                {
//                                    System.err.println("Lock");
                                    schemeCopy.showAnnotationHint(currentText,true,currentAtt);
                                    return;
                                }
                            }
                        }                                        
                        );
                    }
                    else
                    {
                        currentButton.setToolTipText(currentText);    
                    }
                    /* Add action listener */
                    currentButton.addActionListener(this);
                    
                    /* Map lower-cased value string to (0-based) index of JRadioButton in this SchemeLevel */
                    /* This makes it possible to retrieve the name from the index of the button */
                    buttonIndicesToLowerCasedValueStrings.add(currentValue);
                    /* Map index to name of value */
                    /* This makes it possible to retrieve the button index from the lower-cased value name */
                    lowerCasedValueStringsToButtonIndices.put(new String(currentValue),new Integer(size)); 
                    /* Tell button which number it is */
                    currentButton.setActionCommand(new String(size+""));
                    /* Store Button itself */
                    buttons.add(currentButton);
                
                    /* Get value of 'next' attribute */
                    try
                    {
                        nextValue = currentNode.getAttributes().getNamedItem("next").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        nextValue ="";
                    }

                    /* Set this to non-initial if 'next' attribute is present and non-empty for current possible value */
                    if (nextValue.equals("")==false)
                    {
                        // There is a next value associated with the current possible value
                        // So this attribute is branching
                        isBranching = true;
                    }
                                        
                    /* Add button to ButtonGroup, so selection is mutually exclusive */
                    group.add(currentButton);
                    /* Add button to display, so button is visible */
                    
                    buttonBox.add(currentButton);
                    
                    /* Store 'next' value of Button c at position c (this may be empty!) */
                    nextAttributes.add(nextValue);
                    /* Increase size only afterwards */
                    size++;
                }//type = button
  
                else if (type == AttributeAPI.NOMINAL_LIST)
                {
                    /* Init listSelector */
                    if (listSelector == null) 
                    {
                        listSelector = new JComboBox();
                        if (MMAX2.getStandardFont() != null)
                        {
                            listSelector.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));
                        }
                    }
                                        
                    /* Map lower-cased value string to index of list entry in this SchemeLevel */
                    buttonIndicesToLowerCasedValueStrings.add(currentValue);
                    /* Map index of list entry to value name */
                    lowerCasedValueStringsToButtonIndices.put(new String(currentValue),new Integer(size)); 

                    listSelector.addItem(currentValue);
                    // item = null;
                                                        
                    /* Get value of 'next' attribute */
                    try
                    {
                        nextValue = currentNode.getAttributes().getNamedItem("next").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        nextValue ="";
                    }

                    /* Set this to non-initial if 'next' attribute is present for current possible value*/
                    if (nextValue.equals("")==false)
                    {
                        isBranching = true;
                    }
                                        
                    /* Add JComboBox to display only once */
                    if (listSelector.getItemCount()==1)
                    {
                        buttonBox.add(listSelector);
                    }
                    /* Store 'next' value of list entry at c at position c (this may be empty!) */
                    nextAttributes.add(nextValue);
                    size++;
                }//type = button
                                
                else if (this.type == AttributeAPI.FREETEXT)
                {
                    /* type = freetext */
                    freetextArea = new JTextArea(1,10);
                    freetextArea.getDocument().addDocumentListener(this);
                    freetextArea.setLineWrap(false);
                    freetextArea.setWrapStyleWord(true);
                    if (MMAX2.getStandardFont() != null)
                    {
                        freetextArea.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));
                    }
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
                        if (MMAX2.getStandardFont() != null)
                        {
                            idLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));                       
                        }
                        idLabel.setEnabled(false);
                        buttonBox.add(idLabel);
                    }
                    // New: Make pointer relations branching as well
                    /* Get value of 'next' attribute */
                    try
                    {
                        nextValue = currentNode.getAttributes().getNamedItem("next").getNodeValue();
                    }
                    catch (java.lang.NullPointerException ex)
                    {
                        nextValue ="";
                    }

                    /* Set this to non-initial if 'next' attribute is present for current possible value*/
                    if (nextValue.equals("")==false)
                    {
                        isBranching = true;
                    }

                    /* Store 'next' value of list entry at c at position c (this may be empty!) */
                    nextAttributes.add(nextValue);
                    size++;
                }
                else if (type == AttributeAPI.MARKABLE_SET)
                {
                    /* type = markable_SET */
                    idLabel = new JLabel(MMAX2.defaultRelationValue);
                    if (MMAX2.getStandardFont() != null)
                    {
                        idLabel.setFont(MMAX2.getStandardFont().deriveFont((float)fontSize));                    
                    }
                    idLabel.setEnabled(false);
                    buttonBox.add(idLabel);
                }
                
            }//ELEMENT_NODE
        }// for z
        
        if (listSelector != null) listSelector.addActionListener(this);
        if (isBranching)
        {
            attributeLabel.setText("< > "+attributeLabel.getText());
        }
        
        innerBox.add(buttonBox);
        //buttonBox.add(innerBox);
        this.add(innerBox);
    }
        
    public final String getDisplayAttributeName()
    {
        return displayAttributeName;
    }
    
    public final ArrayList getOrderedValues()
    {
        return orderedValues;
    }
    
    public final MMAX2Attribute[] getDirectlyDependentAttributes()
    {
        MMAX2Attribute[] result = null;
        ArrayList temp = new ArrayList();
        if (type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.NOMINAL_LIST || type==AttributeAPI.MARKABLE_POINTER)
        {
            // Iterate over all possible values defined for this attribute
            for (int z=0;z<nextAttributes.size();z++)
            {
                String nextVal = (String) nextAttributes.get(z);
                if (nextVal.equals("")==false)
                {
                    /* Parse String into List of Ids */
                    ArrayList tempresult = MarkableHelper.parseCompleteSpan(nextVal);
                    /* Iterate over all IDs found */
                    for (int p=0;p<tempresult.size();p++)
                    {
                        MMAX2Attribute currentAttrib = (MMAX2Attribute) annotationscheme.getAttributeByID((String) tempresult.get(p));                        
                        if (currentAttrib != null)
                        {                                                    
                            /* Add each Attribute to result only once */                               
                            if (temp.contains(currentAttrib)==false)
                            {
                                temp.add(currentAttrib);
                            }
                        }
                        else
                        {
                            System.err.println("Attribute "+(String) tempresult.get(p)+" not found!");
                        }
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
    
    /** This method returns the (lower cased) default value for this attribute */
    public String getDefaultValue()
    {
        String result = "";
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {
            result = (String) buttonIndicesToLowerCasedValueStrings.get(0);
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
        
        /* Do nothing if the action was initiated automatically */
        if (annotationscheme.ignoreClick) 
        {
            return;
        }
                        
        if (type == AttributeAPI.NOMINAL_BUTTON)        
        {            
            // The attribute is rendered as a list of RadioButtons, so get index of clicked button, communicated through actionCommand
            int position = new Integer(p1.getActionCommand()).intValue();
     
            // Ignore if current value is clicked again
            if (position == currentIndex)
            {
                return;
            }
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
            // Ignore is current value is chosen again
            if (position == currentIndex)
            {
                return;
            }
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
        
        ArrayList tempresult = new ArrayList();      
        int selIndex = -1;
        if (getType() == AttributeAPI.NOMINAL_BUTTON || getType() == AttributeAPI.NOMINAL_LIST)
        {
            /* Get Index of selected JRadioButton or List */
            selIndex = getSelectedIndex();                     
        }
        else if (getType() == AttributeAPI.MARKABLE_POINTER)
        {
            String currentValue = getSelectedValue();
            if (currentValue.equalsIgnoreCase(MMAX2.defaultRelationValue))
            {
                selIndex = 0;
            }
            else
            {
                selIndex = 1;
            }
        }
        else if (getType() == AttributeAPI.MARKABLE_SET || getType()== AttributeAPI.FREETEXT)
        {
            return result;
        }
        else
        {
            System.out.println("Error: Unknown attribute type! "+getLowerCasedAttributeName());
            return result;            
        }
            
        /* Get String of next attributes this points to */
        String nextString = (String) nextAttributes.get(selIndex);
        if (nextString.equals("")==false)
        {
            /* Parse String into List of Ids */
            tempresult = MarkableHelper.parseCompleteSpan(nextString);
            result = new MMAX2Attribute[tempresult.size()];
            /* Iterate over all IDs found */
            for (int p=0;p<tempresult.size();p++)
            {
                /* Add each Attribute to result */                               
                result[p] = (MMAX2Attribute) annotationscheme.getAttributeByID((String) tempresult.get(p));
                /* Reset to default */
                if (result[p] != null)
                {
                    if (toDefault) 
                    {
                        ((MMAX2Attribute)result[p]).toDefault();
                    }
                }
                else
                {
                    System.err.println("No Attribute with ID "+(String)tempresult.get(p)+"!");
                }
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
            for (int p=0;p<this.size;p++)
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
            System.err.println("getSelectedIndex not legal for attribute "+this.attributeLabel.getText());
        }
        return index;
    }
    
    
    /* This method returns the value string associated with the currently selected JRadioButton/list item, or empty string */
    public String getSelectedValue()
    {
        if (frozen) 
        {
            return "";
        }
        
        String value = "";
        JRadioButton currentButton=null;

        if (this.type == AttributeAPI.NOMINAL_BUTTON)
        {
            for (int p=0;p<this.size;p++)
            {
                currentButton = (JRadioButton) buttons.get(p);
                if (currentButton.isSelected())
                {
                    value=((String) buttonIndicesToLowerCasedValueStrings.get(p));
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
            try
            {
                value = freetextArea.getText();
            }
            catch (java.lang.NullPointerException ex)
            {
                value = "";
            }            
            
            value.trim();                        
            
            /* Clean string for XML storage */
            String tempresult = "";
            String currentChar = "";
            for (int z=0;z<value.length();z++)
            {
                currentChar = value.substring(z,z+1);
                if (currentChar.equals("\"")) currentChar = "'";
                else if (currentChar.equals("�")) currentChar = "ae";
                else if (currentChar.equals("�")) currentChar = "ue";
                else if (currentChar.equals("�")) currentChar = "oe" ;               
                else if (currentChar.equals("�")) currentChar = "AE";
                else if (currentChar.equals("�")) currentChar = "UE";
                else if (currentChar.equals("�")) currentChar = "OE" ;               
                else if (currentChar.equals("<")) currentChar = "";
                else if (currentChar.equals(">")) currentChar = "";
                else if (currentChar.equals("�")) currentChar = "ss";
                else if (currentChar.equals("\n")) currentChar = " ";
                
                tempresult = tempresult + currentChar;                
            }
            value = tempresult.trim();                          
            
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
    // desiredValue must be lowercase !!!!! 
    // NEW: if ignore = false, calling this will be equivalent to actually clicking the attribute
    // (Used for oneclickAnnotation and set_values, which is the only case where ignore = false
    public boolean setSelectedValue(String desiredValue, boolean ignore)
    {        
        boolean result = false;
        int buttonPosition =0;
        int itemPosition = -1;
        
        if (type == AttributeAPI.NOMINAL_BUTTON)
        {        
            if (desiredValue == null || desiredValue.equals("") )
            {
                // Value is null or empty, so set to default with ignore = true               
                setSelectedIndex(0);
                result = true;               
            }
            else
            {
                /** DesiredValue is not "" and not _mmax default value_ either */
                try
                {
                    /* Get index of JRadioButton to set, if any */
                    buttonPosition = ((Integer) lowerCasedValueStringsToButtonIndices.get(desiredValue)).intValue();
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
                    if (ignore)
                    {
                        setSelectedIndex(buttonPosition);
                    }
                    else
                    {                    
                        ((JRadioButton) buttons.get(buttonPosition)).doClick();
                    }
                    result = true;
                }
                else
                {
                    System.err.println("Error: Value "+desiredValue+" not found on attribute "+displayAttributeName+"!");
                }
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
                        if (desiredValue.equalsIgnoreCase((String)listSelector.getItemAt(z)))
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
                    if (ignore)
                    {
                        annotationscheme.ignoreClick = true;                        
                    }
                    listSelector.setSelectedIndex(itemPosition);
                    annotationscheme.ignoreClick = false;
                    currentIndex = itemPosition;
                    result = true;
                }
                else
                {
                    System.err.println("Error: Value "+desiredValue+" not found on attribute "+this.displayAttributeName+"!");
                }
            }// else
        }
        
        else if (type == AttributeAPI.FREETEXT)
        {
            if (desiredValue != null)
            {
                freetextArea.setText(desiredValue);
            }
            else
            {
                freetextArea.setText("");
            }
            annotationscheme.ignoreClick = false; // HERE
            result = true;
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            if (desiredValue != null && desiredValue.equals("")==false && desiredValue.equals(MMAX2.defaultRelationValue)==false)
            {
                idLabel.setText(desiredValue);
            }
            else
            {
                idLabel.setText(MMAX2.defaultRelationValue);
            }
            result = true;
        }
        else if (type == AttributeAPI.MARKABLE_POINTER)
        {
            if (desiredValue != null && desiredValue.equals("")==false && desiredValue.equals(MMAX2.defaultRelationValue)==false)
            {
                idLabel.setText(MMAX2Utils.condenseTargetSpan(desiredValue));
            }
            else
            {
                idLabel.setText(MMAX2.defaultRelationValue);
            }
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
            for (int o=0;o<this.size;o++)
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
            return buttonIndicesToLowerCasedValueStrings.contains(value);
        }
        // MODIFIED March 22, 2005: This used to default to true
        else if (type == AttributeAPI.FREETEXT)
        {
            // Any value is defined for a freetext attribute
            return true;
        }
        else if (type == AttributeAPI.MARKABLE_SET)
        {
            if (value.equalsIgnoreCase("empty")==true)
            {
                return true;
            }
            else if (value.equalsIgnoreCase("initial")==true)
            {
                return true;
            }
            else if (value.equalsIgnoreCase("final")==true)
            {
                return true;
            }            
            else
            {
                return false;
            }            
        }
        else if (type == AttributeAPI.MARKABLE_POINTER)
        {
            if (value.equalsIgnoreCase("empty")==true)
            {
                return true;
            }
            else if (value.equalsIgnoreCase("target")==true)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public final String getLowerCasedAttributeName()
    {
        return this.lowerCasedAttributeName;
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
