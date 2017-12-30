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

package org.eml.MMAX2.plugin;
                
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eml.MMAX2.discourse.MMAX2Discourse;

public class MMAX2Plugin extends JFrame 
{		
	public void callPlugin(MMAX2Discourse discourse, HashMap parameters, boolean isBatchMode)
	{
		// The class to call is always the implementing class.
		Class classToCall = getClass();
		
		String methodNameToCall = getMethodName(parameters);
		
		Class[] parameterTypes = getParameterTypes(parameters,discourse);
		
		Object[] parameterValues = getParameterValues(parameters,discourse);
				
        Method method = null;
        try
        {
            method = classToCall.getMethod(methodNameToCall,parameterTypes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }      
		
        if (method != null)
        {
            try
            {
                method.invoke(this,parameterValues);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }        	
        }        		
	}
	
	public final Object[] getParameterValues(HashMap parameters, MMAX2Discourse discourse)
	{
        // Store MMAX2Discourse object as first obligatory parameter
        ArrayList allSuppliedParameterValues = new ArrayList();
        allSuppliedParameterValues.add(discourse);
        // Iterate from 1 to end of list
        for (int b=1;b<10;b++)
        {
            // Create parameter name
            String testKey = "arg"+b;
            // See if something is mapped to it
            String testValue = (String)parameters.get(testKey);
            // If yes, collect it
            if (testValue != null)
            {
                allSuppliedParameterValues.add(testValue);
            }
            else
            {
                // Stop looking for parameters
                break;
            }
        }

        // Check if any of the supplied parameters is one with options
        // Start at pos 1, as 0 holds the discourse object
        for (int p=1;p<allSuppliedParameterValues.size();p++)
        {
            String currentVal = (String) allSuppliedParameterValues.get(p);
            currentVal = performParameterValueSelection(currentVal);
            allSuppliedParameterValues.set(p,currentVal);
        }                                               
                
        return allSuppliedParameterValues.toArray();
     		
	}
	
	public final Class[] getParameterTypes(HashMap parameters, MMAX2Discourse discourse)
	{
        // Store MMAX2Discourse object as first obligatory parameter
        ArrayList allSuppliedParameterValues = new ArrayList();
        allSuppliedParameterValues.add(discourse);
       
        // Iterate from 1 to end of list
        for (int b=1;b<10;b++)
        {
            // Create parameter name
            String testKey = "arg"+b;
            // See if something is mapped to it
            String testValue = (String)parameters.get(testKey);
            // If yes, collect it
            if (testValue != null)
            {
                allSuppliedParameterValues.add(testValue);
            }
            else
            {
                // Stop looking for parameters
                break;
            }
        }
              
        // Create array to accept parameter classes
        // Size: Size of para list (which already contains discourse object)
        Class[] formParas = new Class[allSuppliedParameterValues.size()];
        // First para is always a discourse object
        formParas[0] = MMAX2Discourse.class;
        for (int v=1;v<formParas.length;v++)        	
        {
        	// All other params are of type String
            formParas[v] = String.class;
        }        
        return formParas;		
	}
	
	public final String getMethodName(HashMap parameters)
	{
        String result = (String)parameters.get("task");
        if (result == null) result = "show";
        return result;		
	}
	
    /** Method to be called from MMAX2 Plugin Menu (not to be changed). */
    /* public final void initPlugin_ (MMAX2Discourse discourse, HashMap parameters, boolean isBatchMode)
    {
        int MAXARGS = 20;
        String refreshAction = "ask"; // always ask

        // Task is the name of the method that is to be called
        String task = (String)parameters.get("task");
        if (task==null)
        {
            System.err.println("No 'task' attribute, using $create$ as default.");
            task = "$create$";
        }

        String rfAction = (String)parameters.get("refresh");
        if (rfAction != null) refreshAction = rfAction;
        
        // Create list to accept all supplied parameters in correct order
        ArrayList allSuppliedParameterValues = new ArrayList();
       
        // Store MMAX2Discourse object as first obligatory parameter
        allSuppliedParameterValues.add(discourse);
       
        // Iterate from 1 to end of list
        for (int b=1;b<MAXARGS;b++)
        {
            // Create parameter name
            String testKey = "arg"+b;
            // See if something is mapped to it
            String testValue = (String)parameters.get(testKey);
            // If yes, collect it
            if (testValue != null)
            {
                allSuppliedParameterValues.add(testValue);
            }
            else
            {
                // Stop looking for parameters
                break;
            }
        }
              
        // Create array to accept parameter classes
        // Size: Size of para list (which already contains discourse object)
        Class[] formParas = new Class[allSuppliedParameterValues.size()];
        // First para is always sa discourse object
        formParas[0] = MMAX2Discourse.class;
        for (int v=1;v<formParas.length;v++)
        {
            formParas[v] = String.class;
        }
       
        Class c = getClass();
        Method method = null;
        try
        {
            method = c.getMethod(task,formParas);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }      
                 
        if (method != null)
        {
            // Check if any of the supplied parameters is one with options
            // Start at pos 1, as 0 holds the discourse object
            for (int p=1;p<allSuppliedParameterValues.size();p++)
            {
                String currentVal = (String) allSuppliedParameterValues.get(p);
                currentVal = performParameterValueSelection(currentVal);
                allSuppliedParameterValues.set(p,currentVal);
            }                                               
            try
            {
                method.invoke(this,allSuppliedParameterValues.toArray());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
                              
        // Set to dirty, including refresh of any markable browsers    
        if (!isBatchMode)
        {    
            // The plugin was called in single mode
            // We have the full range of possible ref actions
            if (refreshAction.equalsIgnoreCase("always"))
            {
                discourse.getMMAX2().requestReapplyDisplay();
            }
            else if (refreshAction.equalsIgnoreCase("ask"))
            {            
                int result = JOptionPane.showConfirmDialog(null,"The plugin has finished.\nDo you want to reapply the stylesheet now?","MMAX2Plugin: "+getClass().toString(),JOptionPane.YES_NO_OPTION);               
                if (result == JOptionPane.YES_OPTION)
                {
                    discourse.getMMAX2().requestReapplyDisplay();
                }
            }
        }  
        else
        {
            // The plugin was called in batchmode
            // So never ask in batch mode, but do it always if always = true
            if (refreshAction.equalsIgnoreCase("always"))
            {
                discourse.getMMAX2().requestReapplyDisplay();
            }            
        }
    }
    */
	
	
    protected final static String performParameterValueSelection(String parameter)
    {
        String result="";
        if (parameter.toLowerCase().startsWith("choose:"))
        {
            // Cut off leading 'choose:'
            parameter=parameter.substring(7);
            String askText=parameter.substring(0,parameter.indexOf(":"));
            String toChooseFrom = parameter.substring(parameter.indexOf(":")+1);               
            ArrayList temp = new ArrayList();
            StringTokenizer toki = new StringTokenizer(toChooseFrom,",");
            while (toki.hasMoreTokens())
            {
                String levelName = (String)toki.nextToken();
                temp.add(levelName);
            }               
            if (temp.size()>0)
            {
                String[] levels = (String[])temp.toArray(new String[0]);
                result = (String) JOptionPane.showInputDialog(null,askText,"Plugin Attribute Value Chooser",JOptionPane.QUESTION_MESSAGE,null,levels,levels[0]);
            }              
        }
        else
        {
            // If parameter is a normal one, just pass it back out
            result = parameter;
        }                               
        return result; 
    }        
}
