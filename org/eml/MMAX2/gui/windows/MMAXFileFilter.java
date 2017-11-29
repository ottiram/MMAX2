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

package org.eml.MMAX2.gui.windows;


import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

public class MMAXFileFilter extends FileFilter 
{
    private Hashtable filters = null;
    private String description = null;
    private String fullDescription = null;
    
    private String nameSpaceToMatch = null;
    
    private boolean useExtensionsInDescription = true;

    
    public MMAXFileFilter() 
    {
    	filters = new Hashtable();
    }

    public MMAXFileFilter(String extension) 
    {
    	this(extension,null);
    }

    public MMAXFileFilter(String extension,String description) 
    {
    	this();
    	if(extension!=null) addExtension(extension);
    	if(description!=null) setDescription(description);
    }

    public MMAXFileFilter(String extension,String description, String nameSpace) 
    {
    	this();
    	if(extension!=null) addExtension(extension);
    	if(description!=null) setDescription(description);
        if (nameSpace!=null) nameSpaceToMatch = nameSpace;
    }
    
    
    public MMAXFileFilter(String[] filters) 
    {
    	this(filters, null);
    }

    public MMAXFileFilter(String[] filters,String description) 
    {
    	this();
    	for (int i = 0; i < filters.length; i++) 
    	{
    		// add filters one by one
    		addExtension(filters[i]);
    	}
    	if(description!=null)
    	{
    		setDescription(description);
    	}
    }

    public boolean accept(File f) 
    {
	if(f != null) 
	{
	    if (f.isDirectory()) 
	    {
	    	return true;
	    }
	    String extension = getExtension(f);
	    if(extension != null && filters.get(getExtension(f)) != null) 
	    {
	    	// The extension matches
	    	if (nameSpaceToMatch==null || nameSpaceToMatch.equals(""))
	    	{
	    		// There is no name spavce to matchr
	    		// so accept
	    		return true;
	    	}
	    	else
	    	{
	    		if (f.getName().indexOf(nameSpaceToMatch)!=-1)
	    		{
	    			return true;
	    		}
	    		else
	    		{
	    			return false;
	    		}                    
	    	}
	    }                       
	}
	return false;
    }

    public String getExtension(File f) 
    {
    	if(f != null) 
    	{
    		String filename = f.getName();
    		int i = filename.lastIndexOf('.');
    		if(i>0 && i<filename.length()-1) 
    		{
    			return filename.substring(i+1).toLowerCase();
    		};
    	}
    	return null;
    }

    public void addExtension(String extension) 
    {
    	if(filters == null) 
    	{
        	filters = new Hashtable(5);
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }


    public String getDescription() 
    {
    	if(fullDescription == null) 
    	{
    		if(description == null || isExtensionListInDescription()) 
    		{
    			fullDescription = description==null ? "(" : description + " (";
    			Enumeration extensions = filters.keys();
    			if(extensions != null) 
    			{
    				fullDescription += "." + (String) extensions.nextElement();
    				while (extensions.hasMoreElements()) 
    				{
    					fullDescription += ", " + (String) extensions.nextElement();
    				}
    			}
    			fullDescription += ")";
    		} 
    		else 
    		{
    			fullDescription = description;
    		}
    	}
    	return fullDescription;
    }

    public void setDescription(String description) 
    {
    	description = description;
    	fullDescription = null;
    }

    public void setExtensionListInDescription(boolean b) 
    {
    	useExtensionsInDescription = b;
    	fullDescription = null;
    }

    public boolean isExtensionListInDescription() 
    {
    	return useExtensionsInDescription;
    }
}
