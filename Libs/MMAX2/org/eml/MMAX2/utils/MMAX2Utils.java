package org.eml.MMAX2.utils;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.eml.MMAX2.annotation.scheme.MMAX2AnnotationScheme;
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;
import org.eml.MMAX2.core.MMAX2;
import org.w3c.dom.NamedNodeMap;

public class MMAX2Utils {

	/* This is only executed once upon attribute generation */
	public static final Color getColorByName(String name)
	{        
	    Color result = null;       
	    if (name != null)
	    {
	    	name=name.toLowerCase();
	    	// TODO switch case 
	        if (name.startsWith("black")) result = Color.black;
	        if (name.startsWith("blue")) result = Color.blue;
	        if (name.startsWith("cyan")) result = Color.cyan;
	        if (name.startsWith("darkGray")) result = Color.darkGray;
	        if (name.startsWith("gray")) result = Color.gray;
	        if (name.startsWith("green")) result = Color.green;
	        if (name.startsWith("lightGray")) result = Color.lightGray;
	        if (name.startsWith("magenta")) result = Color.magenta;
	        if (name.startsWith("orange")) result = Color.orange;
	        if (name.startsWith("pink")) result = Color.pink;
	        if (name.startsWith("red")) result = Color.red;        
	        if (name.startsWith("white")) result = Color.white;
	        if (name.startsWith("yellow")) result = Color.yellow;
	        
	    	int alpha = 255;
	    	if (result != null)
	    	{
	    		try
	    		{
	    			String als = name.substring(name.lastIndexOf(':')+1);
	    			System.err.println(als);
	    			alpha = Integer.parseInt(als);
	    		}
	    		catch (java.lang.StringIndexOutOfBoundsException ex)
	    		{
	    			
	    		}
	    		catch (java.lang.NumberFormatException ex)
	    		{
	    			
	    		}

	    		result=new Color(result.getRed(), result.getGreen(), result.getBlue(), alpha);
	    	}
	    		        
	        if (name.startsWith("x:"))
	        {
	            result = getColorByHexValue(name);
	        }
	        if (name.startsWith("d:"))
	        {
	            result = getColorByDecValue(name);
	        }
	    }
	    else
	    {
	        result = Color.black;
	    }
	    if (result == null)
	    {
	        System.err.println("Illegal color name: "+name);
	        result = Color.black;
	    }        	    
	    return result;        
	}

	public final static Color getColorByDecValue(String value)
	{
	    Color result = null;
	    int red=0;
	    int green=0;
	    int blue=0;
	    
	    if (value.startsWith("d:")==false || value.length() != 11)
	    {
	        System.err.println("Format error in dec color code!!");
	        result = Color.red;
	    }
	    try
	    {
	        red = Integer.decode(value.substring(2,5)).intValue();
	        green = Integer.decode(value.substring(5,8)).intValue();
	        blue = Integer.decode(value.substring(8,11)).intValue();            
	    }
	    catch (java.lang.NumberFormatException ex)
	    {
	        System.err.println("Parsing error in dec color code!!");
	        result = Color.red;
	    }
	    if (result==null) result = new Color(red,green,blue);
	    return result;
	}

	public final static Color getColorByHexValue(String value)
	{
	    Color result = null;
	    int red=0;
	    int green=0;
	    int blue=0;       
	    
	    if (value.startsWith("x:")==false || value.length() != 8)
	    {
	        System.err.println("Format error in hex color code!!");
	        result = Color.red;
	    }
	    try
	    {
	        red = Integer.decode("#"+value.substring(2,4)).intValue();
	        green = Integer.decode("#"+value.substring(4,6)).intValue();
	        blue = Integer.decode("#"+value.substring(6,8)).intValue();            
	    }
	    catch (java.lang.NumberFormatException ex)
	    {
	        System.err.println("Parsing error in hex color code!!");
	        result = Color.red;
	    }
	    if (result==null) result = new Color(red,green,blue);
	    return result;
	}

	public final static SimpleAttributeSet createSimpleAttributeSet(String attributestring, boolean blackAndWhiteDefault)
	    {
	    SimpleAttributeSet resultSet = new SimpleAttributeSet();
	    attributestring = attributestring.toLowerCase().trim();
	    String value = "";
	
	    StringTokenizer tokenizer = new StringTokenizer(attributestring);
	    while (tokenizer.hasMoreTokens())
	    {
	        value = tokenizer.nextToken();
	        if (value.startsWith("handles"))
	        {
	            resultSet.addAttribute("handles",getColorByName(value.substring(value.indexOf("=")+1)));
	            continue;
	        }
	        if (value.startsWith("markable_set_line_color") || value.startsWith("markable_pointer_line_color"))
	        {
	            String temp = value.substring(0,value.indexOf("="));
	            resultSet.addAttribute(temp,getColorByName(value.substring(value.indexOf("=")+1)));
	            continue;
	        }
	        if (value.startsWith("markable_set_line_style") || value.startsWith("markable_pointer_line_style"))
	        {
	            String temp = value.substring(0,value.indexOf("="));
	            String val = value.substring(value.indexOf("=")+1);
	            int toSet = MMAX2Constants.STRAIGHT;
	            if (val.equalsIgnoreCase("lcurve"))
	            {
	                toSet = MMAX2Constants.LCURVE;
	            }
	            else if (val.equalsIgnoreCase("rcurve"))
	            {
	                toSet = MMAX2Constants.RCURVE;
	            }
	            else if (val.equalsIgnoreCase("smartcurve"))
	            {
	                toSet = MMAX2Constants.SMARTCURVE;
	            }
	            else if (val.equalsIgnoreCase("xcurve"))
	            {
	                toSet = MMAX2Constants.XCURVE;
	            }
	            else if (val.equalsIgnoreCase("straight"))
	            {
	                toSet = MMAX2Constants.STRAIGHT;
	            }
	            resultSet.addAttribute(temp,new Integer(toSet));
	            continue;
	        }    
	        
	        if (value.startsWith("background"))
	        {
	            StyleConstants.setBackground(resultSet,getColorByName(value.substring(value.indexOf("=")+1)));
	            continue;
	        }
	        
	        if (value.startsWith("foreground"))
	        {
	            StyleConstants.setForeground(resultSet,getColorByName(value.substring(value.indexOf("=")+1)));
	            continue;
	        }
	        
	        if (value.startsWith("bold="))
	        {
	            if (value.startsWith("bold=true"))
	            {
	                StyleConstants.setBold(resultSet,true);
	            }
	        }
	        
	        if (value.startsWith("italic="))
	        {
	            if (value.startsWith("italic=true"))
	            {
	                StyleConstants.setItalic(resultSet,true);
	            }
	            continue;
	        }
	        
	        if (value.startsWith("strikethrough="))
	        {
	            if (value.startsWith("strikethrough=true"))
	            {
	                StyleConstants.setStrikeThrough(resultSet,true);
	            }
	            continue;
	        }
	        
	        if (value.startsWith("subscript="))
	        {
	            if (value.startsWith("subscript=true"))
	            {
	                StyleConstants.setSubscript(resultSet,true);
	            }
	            continue;
	        }
	        
	        if (value.startsWith("superscript="))
	        {
	            if (value.startsWith("superscript=true"))
	            {
	                StyleConstants.setSuperscript(resultSet,true);
	            }
	            continue;
	        }
	        
	        if (value.startsWith("underline="))
	        {
	            if (value.startsWith("underline=true"))
	            {
	                StyleConstants.setUnderline(resultSet,true);
	            }
	            continue;
	        }                               
	        
	        if (value.startsWith("size="))
	        {
	            if (value.startsWith("size=huge"))
	            {
	                StyleConstants.setFontSize(resultSet,MMAX2.currentDisplayFontSize+10);
	            }
	            else if (value.startsWith("size=big"))
	            {
	                StyleConstants.setFontSize(resultSet,MMAX2.currentDisplayFontSize+6);
	            }                
	            else if (value.startsWith("size=normal"))
	            {
	                StyleConstants.setFontSize(resultSet,MMAX2.currentDisplayFontSize);
	            }
	            else if (value.startsWith("size=small"))
	            {
	                StyleConstants.setFontSize(resultSet,MMAX2.currentDisplayFontSize-6);
	            }
	            else if (value.startsWith("size=tiny"))
	            {
	                StyleConstants.setFontSize(resultSet,MMAX2.currentDisplayFontSize-10);
	            }
	            else
	            {
	                String numPart = value.substring(value.indexOf("=")+1);
	                try
	                {
	                    StyleConstants.setFontSize(resultSet,Integer.parseInt(numPart));
	                }
	                catch (java.lang.NumberFormatException ex)
	                {
	                    
	                }
	            }
	        }
	        
	        if (value.startsWith("font-family="))
	        {
	            if (value.startsWith("font-family=courier"))
	            {
	                StyleConstants.setFontFamily(resultSet, "courier");
	            }
	        }                
	    }
	    if (blackAndWhiteDefault)
	    {
	        if (resultSet.isDefined(StyleConstants.Foreground)) StyleConstants.setForeground(resultSet,Color.black);
	        if (resultSet.isDefined(StyleConstants.Background)) StyleConstants.setBackground(resultSet,Color.white);
	    }
	    return resultSet;
	}

	/** This method converts a NodeMap to a HashMap. scheme is used for retrieving the referenced attribute objects, for normalizing their names. */	 
	public static final HashMap<String, String> convertNodeMapToHashMap(NamedNodeMap nodemap, MMAX2AnnotationScheme scheme)
	{
		// This also normalizes attribute and value names
	    HashMap<String, String> resultmap = new HashMap<String, String>();
	    if (nodemap != null)
	    {
	        for (int i=0;i<nodemap.getLength();i++)
	        {
	        	String attName =  (String) nodemap.item(i).getNodeName();
	        	String valName = (String) nodemap.item(i).getNodeValue();
	        	//if (scheme.isDebug()) { System.err.println("'"+attName+"' : '"+valName+"'"); }
	        	if (scheme != null && 
	        			scheme.isDefined(attName) && 
	        			attName.equalsIgnoreCase("id")==false && attName.equalsIgnoreCase("mmax_level")==false && attName.equalsIgnoreCase("span")==false)
	        	{
	        		// This is case-insensitive
	        		MMAX2Attribute att = scheme.getAttributeByName(attName);
	        		// Get canonical att name, for defined attributes only 
	        		attName = att.getDisplayName();
	        		// Get canonical value name
	        		valName =  att.getNormalizedValueName(valName);
	        	}
	            resultmap.put(attName, valName);
	        }
	    }
	    return resultmap;
	}

	public final static String colorToHTML(Color _color)
	{
	    String red = Integer.toHexString(_color.getRed());
	    if (red.equals("0")) red="00";
	    String green = Integer.toHexString(_color.getGreen());
	    if (green.equals("0")) green="00";
	    String blue = Integer.toHexString(_color.getBlue());
	    if (blue.equals("0")) blue="00";
	    return ("#"+red+green+blue);
	}

	public final static int parseID(String id)
	{
	    int result = 0;
	    result = Integer.parseInt(id.substring(id.indexOf("_")+1));
	    return result;
	}

	public final static String toAttributeString(NamedNodeMap attributeMap, boolean removeIDAttribute)
	{
	    String result="";
	                
	    HashMap allAttribsAsHash = convertNodeMapToHashMap(attributeMap, null);
	    Iterator allAttribs = allAttribsAsHash.keySet().iterator();
	    while (allAttribs.hasNext())
	    {
	        String currentAttribute=(String)allAttribs.next();
	        if (currentAttribute.equalsIgnoreCase("id") && removeIDAttribute)
	        {
	            continue;
	        }
	        String currentValue=(String)allAttribsAsHash.get(currentAttribute);
	        if (currentValue.startsWith("'")==false && currentValue.startsWith("\"")==false)
	        {
	            currentValue = "\""+currentValue;
	        }
	        if (currentValue.endsWith("'")==false && currentValue.endsWith("\"")==false)
	        {
	            currentValue = currentValue+"\"";
	        }
	        
	        result=result+currentAttribute+"="+currentValue+" ";
	    }
	    
	    return result;
	}

	public final static String toAttributeString(HashMap allAttribsAsHash, boolean removeIDAttribute)
	{
	    String result="";
	                
	    //HashMap allAttribsAsHash = convertNodeMapToHashMap(attributeMap);
	    Iterator allAttribs = allAttribsAsHash.keySet().iterator();
	    while (allAttribs.hasNext())
	    {
	        String currentAttribute=(String)allAttribs.next();
	        if (currentAttribute.equalsIgnoreCase("id") && removeIDAttribute)
	        {
	            continue;
	        }
	        String currentValue=(String)allAttribsAsHash.get(currentAttribute);
	        if (currentValue.startsWith("'")==false && currentValue.startsWith("\"")==false)
	        {
	            currentValue = "\""+currentValue;
	        }
	        if (currentValue.endsWith("'")==false && currentValue.endsWith("\"")==false)
	        {
	            currentValue = currentValue+"\"";
	        }
	        
	        result=result+currentAttribute+"="+currentValue+" ";
	    }
	    
	    return result;
	}

	/** This method converts a string of the form "attribute1=value1 attribute2=value2 ... in a list of
	    entries of the form attribute1="value1" etc. Attributes with name 'id' are not copied to the list. */
	public final static ArrayList toAttributeList(String attributeString)
	{
	    
	    // Todo: This does not work correctly !!
	    ArrayList result = new ArrayList();
	    StringTokenizer toki = new StringTokenizer(attributeString," ");
	    while (toki.hasMoreElements())
	    {
	        String currentToken = toki.nextToken().trim();
	        int temp = currentToken.indexOf("=");
	        if (temp == -1)
	        {
	            System.err.println("Error with attribute "+currentToken);
	            continue;
	        }
	        String currentAttribute=currentToken.substring(0,temp).trim();
	        if (currentAttribute.equalsIgnoreCase("id"))
	        {
	            continue;
	        }
	        String currentValue=currentToken.substring(temp+1).trim();
	        if (currentValue.startsWith("\"")==false && currentValue.startsWith("'")==false)
	        {
	            currentValue = "\""+currentValue;
	        }
	        if (currentValue.endsWith("\"")==false && currentValue.endsWith("'")==false)
	        {
	            currentValue = currentValue+"\"";
	        }
	        result.add(currentAttribute+"="+currentValue);
	    }
	    return result;
	
	}

	public final static Point calculateControlPoint(int x_source, int y_source, int x_target, int y_target, int lineStyle)
	{
	    Point result = null;
	    int x_diff=0;
	    int y_diff=0;
	    double x_ortho=0;
	    double y_ortho=0;
	    int x_middle=0;
	    
	    double c=0.2;
	    
	    if (lineStyle == MMAX2Constants.XCURVE)
	    {
	        x_diff = x_target - x_source; 
	        y_diff = y_target - y_source; 
	
	        x_ortho =   y_diff * c; // * length_diff; 
	        y_ortho = - x_diff * c; // * length_diff; 
	
	        x_middle = (x_source + x_target)/2;  
	
	        result = new Point((int)(x_middle + x_ortho),(int) (x_middle + y_ortho));
	    }
	    
	    // Links oben nach rechts unten
	    else if (x_source < x_target && y_source < y_target)
	    {
	        if (lineStyle == MMAX2Constants.LCURVE)
	        {
	            result = new Point((int)x_target,(int) y_source);
	        }
	        else if (lineStyle == MMAX2Constants.RCURVE)
	        {
	            result = new Point((int)x_source,(int) y_target);
	        }
	    }
	    // Rechts oben nach links unten
	    else if (x_source > x_target && y_source < y_target)
	    {
	        if (lineStyle == MMAX2Constants.LCURVE)
	        {
	            result = new Point((int)x_source,(int) y_target);
	        }
	        else if (lineStyle == MMAX2Constants.RCURVE)
	        {
	            result = new Point((int)x_target,(int) y_source);
	        }
	    }
	    // Rechts unten nach links oben
	    else if (x_source > x_target && y_source > y_target)
	    {
	        if (lineStyle == MMAX2Constants.LCURVE)
	        {
	            result = new Point((int)x_target,(int) y_source);
	        }
	        else if (lineStyle == MMAX2Constants.RCURVE)
	        {
	            result = new Point((int)x_source,(int) y_target);
	        }
	    }
	    // Links unten nach rechts oben
	    else if (x_source < x_target && y_source > y_target)
	    {
	        if (lineStyle == MMAX2Constants.LCURVE)
	        {
	            result = new Point((int)x_source,(int) y_target);
	        }
	        else if (lineStyle == MMAX2Constants.RCURVE)
	        {
	            result = new Point((int)x_target,(int) y_source);
	        }
	    }
	    // Parallel
	    else if (y_source == y_target)
	    {
	        if (x_target < x_source)
	        {
	            if (lineStyle == MMAX2Constants.LCURVE)
	            {
	                result = new Point((int)((int)x_source-(int)x_target)/2+(int)x_target,(int)y_source+20);
	            }
	            else if (lineStyle == MMAX2Constants.RCURVE)
	            {
	                result = new Point((int)((int)x_source-(int)x_target)/2+(int)x_target,(int)y_source-20);
	            }
	        }
	        else
	        {
	            if (lineStyle == MMAX2Constants.LCURVE)
	            {
	                result = new Point((int)((int)x_target-(int)x_source)/2+(int)x_source,(int)y_source-20);
	            }
	            else if (lineStyle == MMAX2Constants.RCURVE)
	            {
	                result = new Point((int)((int)x_target-(int)x_source)/2+(int)x_source,(int)y_source+20);
	            }
	        }
	    }
	    // Senkrecht
	    else if (x_source == x_target)
	    {
	        if (y_target < y_source)
	        {
	            if (lineStyle == MMAX2Constants.LCURVE)
	            {
	                result = new Point((int)x_source-20,(int)((int)y_source-(int)y_target)/2+(int)y_target);
	            }
	            else if (lineStyle == MMAX2Constants.RCURVE)
	            {
	                result = new Point((int)x_source+20,(int)((int)y_source-(int)y_target)/2+(int)y_target);
	            }
	        }
	        else
	        {
	            if (lineStyle == MMAX2Constants.LCURVE)
	            {
	                result = new Point((int)x_source-20,(int)((int)y_target-(int)y_source)/2+(int)y_source);
	            }
	            else if (lineStyle == MMAX2Constants.RCURVE)
	            {
	                result = new Point((int)x_source+20,(int)((int)y_target-(int)y_source)/2+(int)y_source);
	            }
	        }            
	    }
	    return result;
	}

//	public final static String condenseSatelliteSpan_bak(String span)
//	{
//	    String result = "";
//	    ArrayList list = parseTargetSpan(span,";");        
//	    for (int b=0;b<list.size();b++)
//	    {
//	        String currentEntry = (String)list.get(b);
//	        currentEntry = currentEntry.substring(currentEntry.indexOf("_")+1);
//	        if (b==0)
//	        {
//	            result = currentEntry;
//	        }
//	        else
//	        {
//	            result = result+" "+currentEntry;
//	        }
//	    }
//	    return result.trim();
//	}

	/** This method accepts a target span string and returns a condensed form of it for display in the AttributeWindow. */
	public final static String condenseTargetSpan(String span)
	{
	    String result = "";
	    ArrayList list = parseTargetSpan(span,";");
	    for (int b=0;b<list.size();b++)
	    {
	        String currentEntry = (String)list.get(b);
//	        System.out.println(currentEntry);
	        String currentNameSpace="";
	        // Namespace (=level name) is expected before the id, separated by :
	        // If present, it is the same for all markables in the span
	        if (currentEntry.indexOf(":")!=-1)
	        {
	            // The current entry has a name space
	        	// Identify
	            currentNameSpace = currentEntry.substring(0,currentEntry.indexOf(":")+1);
	            // and cut off
	            currentEntry=currentEntry.substring(currentEntry.indexOf(":")+1);
//		        System.out.println(currentEntry);
	        }
	        currentEntry = currentEntry.substring(currentEntry.indexOf("_")+1);
	        if (b==0)
	        {
	            result = currentNameSpace+currentEntry;
	        }
	        else
	        {
	            result = result+" "+currentNameSpace+currentEntry;
	        }
	    }
	    return result.trim();
	}

	public final static String expandTargetSpan(String span)
	{
	    String result =MMAX2.defaultRelationValue;
	    ArrayList list = parseTargetSpan(span," ");
	    for (int z=0;z<list.size();z++)
	    {            
	        String currentEntry = (String)list.get(z);
	        if (currentEntry.equals(MMAX2.defaultRelationValue))
	        {
	            break;
	        }
	        String currentNameSpace="";
	        if (currentEntry.indexOf(":")!=-1)
	        {
	            // The current entry has a name space
	            currentNameSpace=currentEntry.substring(0,currentEntry.indexOf(":")+1);
	            currentEntry = currentEntry.substring(currentEntry.indexOf(":")+1);
	        }
	        if (z==0)
	        {
	            result = currentNameSpace+"markable_"+currentEntry;
	        }
	        else
	        {
	            result = result+";"+currentNameSpace+"markable_"+currentEntry;
	        }
	    }
	    return result;
	}

	
	public final static ArrayList<String> parseTargetSpan(String span, String delimiter)
	{
	    ArrayList<String> result = new ArrayList<String>();
	    StringTokenizer toki = new StringTokenizer(span,delimiter);
	    while (toki.hasMoreTokens())
	    {
	        result.add(toki.nextToken());
	    }        
	    return result;
	}

}
