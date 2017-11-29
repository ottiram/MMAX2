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

package org.eml.MMAX2.annotation.query.simplified;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.eml.MMAX2.annotation.markables.MarkableChart;
import org.eml.MMAX2.gui.windows.MMAX2QueryWindow;

public class SimplifiedMMAXQLConverter 
{
    MarkableChart chart = null;
    boolean interactive = false;
    HashSet userLevelVariables = null;
    public SimplifiedMMAXQLConverter(MarkableChart _chart, boolean _interactive)
    {
        chart = _chart;
        interactive = _interactive;
        userLevelVariables = new HashSet();
    }
    
    /** This method receives a simplified MMAXQL sequence query and converts it to a sequence of MMAXQL
        queries ready to be executed. */
    public final String simplifiedSequenceQueryToMMAXQL(String simplifiedCommand, String toplevelCommand, String varNameSpace)
    {        
        String convertedMMAXQL = "";
                
        // List to temporarily accept each single sequence query token (i.e. string between spaces)
        // Note: A single sequence query token can be complex if it has to match more than one attribute
        // In this case, several conditions are separated by comma (no space in between, cf. below!)
        ArrayList allSequenceQueryTokens = new ArrayList();
        // Break entire simplified string at spaces
        // This means that no spaces are allowed *within* a sequence query token!
        // Spaces in RegExps must be specified as \s
        // New April 4th: This will treat non-default connectors as query tokens, since 
        // they must be included in spaces
        
        String tempCommand= simplifiedCommand;
        String currentCommand="";
        while(true)
        {
            int end = MMAX2QueryWindow.getNextCommandSeparator(tempCommand, " ");
            currentCommand = tempCommand.substring(0,end).trim();
//            System.err.println("just extracted: '"+currentCommand+"'");
            if (currentCommand.equals("")==false)
            {
                // Add each query token to list, trimmed
                allSequenceQueryTokens.add(currentCommand);
            }
            if (tempCommand.length() <=end)
            {
                break;
            }            
            tempCommand = tempCommand.substring(end+1).trim();
            if (tempCommand.equals(""))
            {
                break;
            }        
        }
        
        String currentSequenceQueryToken="";
        boolean inSequence=false;
        
        // Iterate over list of all tokens        
        for (int z=0;z<allSequenceQueryTokens.size();z++)
        {
            // Try to find implicit literal sequences of more than one element, and make explicit
            // 'the man said' --> 'the' 'man' 'said'
            // Get current token
            currentSequenceQueryToken = (String) allSequenceQueryTokens.get(z);
            if (currentSequenceQueryToken.startsWith("'") && currentSequenceQueryToken.endsWith("'")==false)
            {
                // The current sequence query token starts an implicit sequence: 'the ...
                // Add closing '
                currentSequenceQueryToken=currentSequenceQueryToken+"'";                                
                // And write it back
                allSequenceQueryTokens.set(z,currentSequenceQueryToken);
                inSequence=true;
                continue;
            }
            else if (currentSequenceQueryToken.startsWith("'")==false && currentSequenceQueryToken.endsWith("'"))
            {
                // The current sequence query token ends an implicit sequence: ... man'
                // Add opening '
                currentSequenceQueryToken="'"+currentSequenceQueryToken;
                // And write it back
                allSequenceQueryTokens.set(z,currentSequenceQueryToken);
                inSequence=false;
                continue;                
            }
            else if (currentSequenceQueryToken.equals("'") && inSequence)
            {
                // The current sequence query token is a single ', which ends the current sequence                
                inSequence=false;                
                // The token itself can then be ignored                
                allSequenceQueryTokens.remove(z);
                z--;
            }
            else if (currentSequenceQueryToken.equals("'") && !inSequence)
            {
                // The current sequence query token is a single ', which starts the current sequence                
                inSequence=true;
                // The token itself can then be ignored                
                allSequenceQueryTokens.remove(z);
                z--;
            }            
            else if (inSequence)
            {
                currentSequenceQueryToken = "'"+currentSequenceQueryToken+"'";
                allSequenceQueryTokens.set(z,currentSequenceQueryToken);
            }
        }

        // Here, any implicit sequence query has been made explicit
        
        ArrayList relations = new ArrayList();
        ArrayList negations = new ArrayList();
        ArrayList useAs = new ArrayList();
        // Iterate over list of tokens (this will also contain relation operators!)
        for (int z=0;z<allSequenceQueryTokens.size();z++)
        {
            boolean useDetermined = false;
            String currentToken = (String) allSequenceQueryTokens.get(z);
            System.err.println("CT: '"+currentToken+"'");
            // New April 20th: Handle filter signals
            // Can be handled here for all cases, because no ambiguity possible
            if (currentToken.startsWith("#"))
            {
                // The current token is an operator to be used as a filter
                useAs.add("filter");
                // Cut off leading #
                currentToken = currentToken.substring(1);
                useDetermined=true;
            }
             
            // New: allow variables as input in sMMAXQL
            if (currentToken.startsWith("$"))
            {
                // The current token is a user-level variable,
                // since it is referenced in the simplified string.
                // Store its name for later cleansing procedure
                userLevelVariables.add(currentToken);
                // mask with a var: name spece to make recognizable
                currentToken = "var:"+currentToken;
                allSequenceQueryTokens.set(z, currentToken);                                
            }            
            
            // New April 15th: Use case-sensitive matching to support strict matching
            if (currentToken.equals("or"))
            {
                // The merge relation 'or' was specified
                relations.add("merge");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;               
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("equals"))
            {
                // The non-default relation 'equals' was specified
                relations.add("equals");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }           
            else if (currentToken.equals("spans"))
            {
                // The non-default relation 'spans' was specified
                relations.add("spans");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }                       
            else if (currentToken.equals("="))
            {
                // The non-default relation 'equals' was specified
                relations.add("equals");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }                       
            else if (currentToken.equals("!equals"))
            {
                // The non-default relation 'equals' was specified
                relations.add("equals");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }           
            else if (currentToken.equals("!="))
            {
                // The non-default relation 'equals' was specified
                relations.add("equals");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }                       
            
            else if (currentToken.equals("in"))
            {
                // The non-default relation 'in' was specified
                relations.add("during");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!in"))
            {
                // The non-default relation in was specified
                relations.add("during");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("overlaps_right"))
            {                
                relations.add("overlaps_right");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!overlaps_right"))
            {
                // The non-default relation in was specified
                relations.add("overlaps_right");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("overlaps_left"))
            {                
                relations.add("overlaps_left");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!overlaps_left"))
            {
                // The non-default relation in was specified
                relations.add("overlaps_left");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            
            else if (currentToken.equals("In"))
            {
                // The non-default relation in was specified
                relations.add("during_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;                
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!In"))
            {
                // The non-default relation in was specified
                relations.add("during_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }                        
            else if (currentToken.equals("meets"))
            {
                relations.add("meets");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!meets"))
            {
                relations.add("meets");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }                        
            else if (currentToken.equals("dom"))
            {
                // The non-default relation in was specified
                relations.add("contains");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!dom"))
            {
                // The non-default relation in was specified
                relations.add("contains");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Dom"))
            {
                // The non-default relation in was specified
                relations.add("contains_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Dom"))
            {
                // The non-default relation in was specified
                relations.add("contains_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }
            else if (currentToken.equals("starts"))
            {
                // The non-default relation in was specified
                relations.add("starts");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!starts"))
            {
                // The non-default relation in was specified
                relations.add("starts");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Starts"))
            {
                // The non-default relation in was specified
                relations.add("starts_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Starts"))
            {
                // The non-default relation in was specified
                relations.add("starts_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }
            else if (currentToken.equals("starts_with"))
            {
                // The non-default relation in was specified
                relations.add("starts_with");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!starts_with"))
            {
                // The non-default relation in was specified
                relations.add("starts_with");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Starts_with"))
            {
                // The non-default relation in was specified
                relations.add("starts_with_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Starts_with"))
            {
                // The non-default relation in was specified
                relations.add("starts_with_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("ends"))
            {
                // The non-default relation in was specified
                relations.add("ends");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!ends"))
            {
                // The non-default relation in was specified
                relations.add("ends");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Ends"))
            {
                // The non-default relation in was specified
                relations.add("ends_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Ends"))
            {
                // The non-default relation in was specified
                relations.add("ends_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }
            else if (currentToken.equals("ends_with"))
            {
                // The non-default relation in was specified
                relations.add("ends_with");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!ends_with"))
            {
                // The non-default relation in was specified
                relations.add("ends_with");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Ends_with"))
            {
                // The non-default relation in was specified
                relations.add("ends_with_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Ends_with"))
            {
                // The non-default relation in was specified
                relations.add("ends_with_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("startswith"))
            {
                // The non-default relation in was specified
                relations.add("starts_with");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!startswith"))
            {
                // The non-default relation in was specified
                relations.add("starts_with");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Startswith"))
            {
                // The non-default relation in was specified
                relations.add("starts_with_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Startswith"))
            {
                // The non-default relation in was specified
                relations.add("starts_with_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }
            else if (currentToken.equals("endswith"))
            {
                // The non-default relation in was specified
                relations.add("ends_with");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!endswith"))
            {
                // The non-default relation in was specified
                relations.add("ends_with");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Endswith"))
            {
                // The non-default relation in was specified
                relations.add("ends_with_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Endswith"))
            {
                // The non-default relation in was specified
                relations.add("ends_with_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                // Negations are always filters!
                if (!useDetermined) useAs.add("filter");
                continue;
            }
            else if (currentToken.equals("before"))
            {
                // The non-default relation in was specified
                relations.add("before");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.startsWith("before:"))
            {
                // The non-default relation before was specified
                // OK, before with dist and reflist id recognized here
                relations.add(currentToken);
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }            
            else if (currentToken.equals("!before"))
            {
                // The non-default relation in was specified
                relations.add("before");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.equals("Before"))
            {
                // The non-default relation in was specified
                relations.add("before_strict");
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.equals("!Before"))
            {
                // The non-default relation in was specified
                relations.add("before_strict");
                negations.add("!");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("filter");
                continue;
            }            
            else if (currentToken.startsWith("anypeer:"))
            {
                relations.add(currentToken);
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            else if (currentToken.startsWith("nextpeer:"))
            {
                relations.add(currentToken);
                negations.add("");
                allSequenceQueryTokens.remove(z);
                z--;
                if (!useDetermined) useAs.add("joiner");
                continue;
            }
            
            // New April 12th: Handle pre-assigned variables as well
            // Note: Slash is now also possible in operators, i.e. when they bring their own
            // ref list command!! In that case, it should be included in square brackets
            else if (isQueryToken(currentToken))
            {
                // The current token is a markable or basedata token or a variable
                // Check if there is another token
                if (z<allSequenceQueryTokens.size()-1)
                {
                    // There is a next token
                    // Get next token
                    String nextToken = (String) allSequenceQueryTokens.get(z+1);
                    // If next token is also a markable
                    if (isQueryToken(nextToken))
                    {
                        // Add default relation
                        relations.add("meets");
                        negations.add("");
                        if (!useDetermined) useAs.add("joiner");                        
                    }
                    
                    else if (nextToken.startsWith("$"))
                    {
                        // next token is a user level variable!
                        // The var: will only be appended in the next iteration
                        // Add default relation
                        relations.add("meets");
                        negations.add("");
                        if (!useDetermined) useAs.add("joiner");
                    }                                        
                }
            }
        }
                
        // For each sequence query token, a variable is created which contains all elements (basedata or 
        // markable) that match the token's required attributes. This variable is then used in the actual
        // relation query.
        // For query tokens accessing basedata (e.g. '[Ii]t'), a variable assignment looks as follows:
        // let $0 = basedata (*basedata_text={[Ii]t});
        
        // For query token accessing markables, a variable assignment looks as follows:
        // let $0 = pos (pos={jj}); (if pos is a nominal attribute)
        // let $0 = pos (*pos={jj}); (if pos is a freetext attribute)
        
        // Now, create one variable assignment for each sequence query token
        String currentToken="";
        // Iterate over all sequence query tokens
        for (int z=0;z<allSequenceQueryTokens.size();z++)
        {
            // Get current sequence query token
            currentToken = (String) allSequenceQueryTokens.get(z);
            String temp="";
            if (currentToken.startsWith("'") && currentToken.endsWith("'"))
            {
                // The current sequence query token accesses the basedata level
                temp = createVariableAssignmentForBasedataAccess(currentToken,varNameSpace+""+z+"");
                // If the above failed, temp is an empty string
                if (temp.equals(""))
                {
                    String message = "Error: Could not parse basedata access token\n\n";
                    message = message +currentToken+"\n";                    
                    System.err.println(message);
                    if (interactive) displayUserMessage(message);
                    return "";
                }
                convertedMMAXQL = convertedMMAXQL + temp +";";                
            }
            else
            {            
                // The current sequence query token accesses a markable level
                temp = createVariableAssignmentForMarkableAccess(currentToken,varNameSpace+""+z+"");
                // If the above failed, temp is an empty string
                if (temp.equals(""))
                {                    
                    System.err.println("Error: Could not parse markable access token!");
                    return "";
                }
                
                // This is the point to intercept user-level variable to variable assignments!
                // How to recognize user-level variables?
                convertedMMAXQL = convertedMMAXQL + temp +" ; ";
            }
        }
        
        // Now, one variable assignment has been created for each sequence query token such that the
        // variable names ($0-$n) reflect the position in the query sequence.
        // The actual relation query is now built using these variable names.        
        if (allSequenceQueryTokens.size() == 1)
        {
            // April 19th: No modification necessary for peer
            convertedMMAXQL = convertedMMAXQL + toplevelCommand+" $"+varNameSpace+"0";
        }
        else if (allSequenceQueryTokens.size() == 2)
        {
            // Get relation to be used for this query
            String relation = (String) relations.get(0);
            String use = (String) useAs.get(0);
            if (use.equals("filter")) 
            {
                use="#";
            }
            else
            {
               use="";
            }
            if (relation.startsWith("anypeer:")== false && relation.startsWith("nextpeer:")==false)
            {
                convertedMMAXQL = convertedMMAXQL + toplevelCommand+" "+use+((String)negations.get(0))+relation+"( $"+varNameSpace+"0 , $"+varNameSpace+"1 )";
            }
            else
            {
                String setName = relation.substring(relation.indexOf(":")+1);
                relation = relation.substring(0,relation.indexOf(":"));
                // Set relation name to actual name to be used in MMAXQL
                if (relation.equals("anypeer")) relation="peer";
                if (relation.equals("nextpeer")) relation="next_peer";                
                
                convertedMMAXQL = convertedMMAXQL + toplevelCommand+" "+use+((String)negations.get(0))+relation+"('"+setName+"', $"+varNameSpace+"0 , $"+varNameSpace+"1 )";
            }
        }
        else
        {
            // The pattern contains three or more elements
            // New April 8th: Leave out column specifiers when building queries from sMMAXQL
            String temp = "";
            
            String relation = "";
            try
            {
                relation = (String)relations.get(0);
            }
            catch (java.lang.IndexOutOfBoundsException  ex)
            {
                JOptionPane.showMessageDialog(null,"Error with query","Simplified MMAXQL Error",JOptionPane.ERROR_MESSAGE);
                return "";
            }
            String use = (String) useAs.get(0);
            if (use.equals("filter")) 
            {
                use="#";
            }
            else
            {
               use="";
            }
            
            if (relation.startsWith("anypeer:")== false && relation.startsWith("nextpeer:")==false)
            {
                temp = (use+((String)negations.get(0))+relation)+"( $"+varNameSpace+"0 , $"+varNameSpace+"1 )";
            }
            else
            {
                String setName = relation.substring(relation.indexOf(":")+1);
                relation = relation.substring(0,relation.indexOf(":"));
                // Set relation name to actual name to be used in MMAXQL
                if (relation.equals("anypeer")) relation="peer";
                if (relation.equals("nextpeer")) relation="next_peer";                
                temp = use+((String)negations.get(0))+relation+"('"+setName+"', $"+varNameSpace+"0 , $"+varNameSpace+"1 )";
            }
            

            for (int b=2;b<allSequenceQueryTokens.size();b++)
            {
                // Build up MMAXQL string incrementally                
                // New April 8th: Leave out column specifiers when building queries from sMMAXQL
                relation = (String) relations.get(b-1);
                use = (String) useAs.get(b-1);
                if (use.equals("filter")) 
                {
                    use="#";
                }
                else
                {
                    use="";
                }
                                
                if (relation.startsWith("anypeer:")== false && relation.startsWith("nextpeer:")==false)
                {
                    temp = use+((String)negations.get(b-1))+relation+"("+temp+", $"+varNameSpace+""+b+" )";
                }
                else
                {
                    String setName = relation.substring(relation.indexOf(":")+1);
                    relation = relation.substring(0,relation.indexOf(":"));
                    // Set relation name to actual name to be used in MMAXQL
                    if (relation.equals("anypeer")) relation="peer";
                    if (relation.equals("nextpeer")) relation="next_peer";
                    temp = use+((String)negations.get(b-1))+relation+"('"+setName+"',"+temp+", $"+varNameSpace+""+b+" )";
                }
            }            
            convertedMMAXQL =convertedMMAXQL + toplevelCommand+" "+temp;
        }
        return convertedMMAXQL;
    }

    
    public final boolean isQueryToken(String currentToken)
    {
        boolean result = false;
        if(currentToken.startsWith("'") && currentToken.endsWith("'"))
        {
            result = true;
        }
        else if (currentToken.startsWith("var:$"))
        {
            result = true;
        }
        else if (currentToken.indexOf("/")!=-1)
        {
            // The current token contains a slash, which might be a token indicator
            result = true;
            // But only if the slash is not included in square brackets, in which case it
            // means it is a query embedded as a ref list command.           
            int openIndex = currentToken.indexOf("[");
            int closeIndex = currentToken.indexOf("]");
            System.err.println(openIndex+" "+currentToken.indexOf("/")+" "+closeIndex);
            if (openIndex != -1 && 
                closeIndex != -1 && 
                currentToken.indexOf("/")>openIndex && 
                currentToken.indexOf("/")<closeIndex)
            {                
                // The slash is in betweeen brackets
                result = false;
            }
        }
        return result;
    }
    
    /** This method receives a sequence query token for basedata access (e.g. '[Ii]t') and returns a
        MMAXQL statement for assigning the result to the variable variableName. */
    public final String createVariableAssignmentForBasedataAccess(String token, String variableName)
    {
        String result = "";
        // Trim leading and trailing ' (which are expected to be there)
        String trimmedToken =token.substring(1);
        trimmedToken = trimmedToken.substring(0,trimmedToken.length()-1);
        
        if (trimmedToken.startsWith("!"))
        {
            // The current expression is negated
            // Cut off negation sign
            trimmedToken =trimmedToken.substring(1);
            result = "let $"+variableName+" = basedata (!*basedata_text={"+trimmedToken+"})";
        }
        else
        {
            result = "let $"+variableName+" = basedata (*basedata_text={"+trimmedToken+"})";            
        }
        
        // Create assignment
        // Basedata access is always as RegExp

        return result;
    }
    
    /** This method receives a sequence query token for markable access, and returns a MMAXQL statement
        for assigning the result to the variable variableName. 
        Sequence query tokens have the form text/attributes, where text is the (optional) specification
        of the markable text, and attributes the required attribute-value pair(s).
        If a query token does only contain a text specification, the attribute part is interpreted
        as the name of the level, if it does not contain ,.=. The attribute part can be empty if
        only one markable level is active at the time of execution.
        
        The attribute part can be complex, if a query token is required to match several attributes. In this
        case, single conditions must be separated by comma.
        Individual conditions for one query token must come from the same level!!
     
        Each condition can exhibit different forms of underspecification:
        It can consist of attribute values only, e.g. jj or {jj,jjr,jjs}.
         In this case, all supplied attribute values must be uniquely associated with *one* attribute 
         on *one* level. */
    public final String createVariableAssignmentForMarkableAccess(String token, String variableName)
    {   
        // The current token accesses some markable level
        boolean error = false;
        String result = "";
        String textToMatch="";
        // Make copy of original input (for error message)
        String tokenAsInput = token;
        
        
        if (token.startsWith("var:"))
        {            
            // The current token is a placeholder for a pre-assigned variable
            token = token.substring(4);
            result = "let $"+variableName+" = "+ token;
            return result;
        }
        
        if (token.indexOf("/")==-1)
        {
            // The current token does not have the correct form
            String message = "Error: The query token\n";
            message = message + token+"\n"; 
            message = message + "is not well-formed! ";
            message = message + "Required format:\n\n";
            message = message + "markable_text/attributes\n\n";
            message = message + "where markable_text is a regular expression (optional), and\n";
            message = message + "attributes is a comma-separated list of (unambiguous) attribute values.";
            System.err.println(message);
            if (interactive) displayUserMessage(message);
            return "";            
        }
               
        
        if (token.startsWith("/")==false)
        {
            // The current token specifies a markable_text to be matched
            // Save specified string ...
            
            if (token.startsWith("!"))
            {
                // The regExp match is negated
                // Store negation info
                result = "!";
                // and cut it off
                token=token.substring(1);      
            }
            
            int sep = token.indexOf("/");
            textToMatch=token.substring(0,sep);
            // ... and cut it off
            token=token.substring(sep+1);      
        
            // Store this as first condition in result
            result = result + "*markable_text={"+textToMatch+"} and ";
        }
        else
        {
            // The current token starts with /, so it does not specify a markable_text to be matched
            // Cut off leading /
            token=token.substring(1);
        }
        
        // Check if token is now empty
        // This can happen if input was
        // / or text/, i.e. if no attributes were entered
        if (token.equals(""))
        {            
            if (chart.getActiveLevels().length>1)
            {
                // If more than one level is currently available, the level name is required
                // or at least one condition
                String message = "Error: More than one active level, and no level specified!\n";
                message = message + "Specify at least one unambiguous condition,\n"; 
                message = message + "or the name of the level you want to access,\n";
                message = message + "or deactivate all but one level!";
                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";
            }
            else if (chart.getActiveLevels().length==1)
            {
                // If only one level is available, take its name as default
                token = chart.getActiveLevels()[0].getMarkableLevelName();
            }
            else
            {
                // If no level is active, a level name or at least one condition is required
                String message = "Error: No active level, and no level specified!\n";
                message = message + "Specify at least one unambiguous condition,\n";
                message = message + "or the name of the level you want to access,\n";
                message = message + "or activate one level!";
                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";
            }
        }
        
        // Check if the token can be interpreted as a level name
        if (chart.getMarkableLevelByName(token, false)!=null)
        {
            // The token consists of a level name only
            if (result.equals("")==false)
            {
                // There was also a text match, which is already stored in result
                // Cut off trailing 'and'
                result = result.substring(0,result.length()-5);
                result = "let $"+variableName+" = "+ token+" ("+result+" )";                               
            }
            else
            {
                // There was no text match, so result is empty
                result = "let $"+variableName+" = "+ token;
            }
            return result;                        
        }
                
        // Split into list of individual conditions and list of negations, and list of one logical connector
        ArrayList[] temp2 = parseSequenceQueryTokenConditions(token);
        
        if (temp2[0].size()==0)
        {
            // If no conditions were retrieved, some error must have happened
            return "";
        }
        
        ArrayList singleQueryTokenConditions = temp2[0];
        ArrayList singleQueryTokenConditionNegations = temp2[1];
        ArrayList connectorList = temp2[2];
        
        String connector = "and";
        if (connectorList.size()> 0) 
        {
            connector = (String) connectorList.get(0);
        }
        
        //ArrayList singleQueryTokenConditions = parseSequenceQueryTokenConditions(token);
        // singleQueryTokenConditions now is a list of conditions in various forms
        // All conditions must relate to the same markable level!!
        String accessedLevel="";
        // Thus, complex conditions can be built using booleans in MMAXQL (which is great!!)

        // Iterate over all conditions for the current sequence query token (may often be only one)
        String currentCondition="";
        for (int v=0;v<singleQueryTokenConditions.size();v++)
        {
            // Get current condition
            currentCondition = (String) singleQueryTokenConditions.get(v);            
            if (containsLevelName(currentCondition))
            {
                // The current condition contains a level name + .
                // Extract level name: Assume level name is part from beginning up to dot (excl.)
                String levelName=currentCondition.substring(0,currentCondition.indexOf("."));
                
                // Remove level name from current condition
                singleQueryTokenConditions.set(v, currentCondition.substring(currentCondition.indexOf(".")+1));
                
                if (accessedLevel.equals(""))
                {
                    // No level has been identified so far, so store the current one
                    accessedLevel=levelName;
                }
                else
                {
                    // There has been a level name already
                    // This is only a problem if it is different from the currently found one
                    if (accessedLevel.equalsIgnoreCase(levelName)==false)
                    {
                        String message = "Error: You try to access at least two different levels\n";
                        message = message + "in one query token:\n\n"+accessedLevel+"\n"+levelName+"!\n\n";
                        message = message + "Each token must only access a single level!";
                        System.err.println(message);
                        if (interactive) displayUserMessage(message);
                        // This is a critical error which cannot be resolved
                        error=true;
                        break;
                    }
                    else
                    {
                        System.err.println("Note: Redundant level specifier in query!");
                    }
                }
            }
        }
        
        if (error)
        {
            // If some critical error occured earlier, break and return nothing
            return "";
        }
        
        // Now we know that the query does not *explicitly* try to access several levels
        // Any explicit level specifier has been removed by now!                                       
        
        // We may still not know the level that the query is accessing
        if (accessedLevel.equals("")==true)
        {                        
            ArrayList accumulatedDistinctLevelNames = new ArrayList();
            // Accessed level has not yet been determined, as is was not mentioned explicitly
            // Iterate over single conditions again
            for (int v=0;v<singleQueryTokenConditions.size();v++)
            {
                // Get current condition
                currentCondition = (String)singleQueryTokenConditions.get(v);            
                // Get list of pairs of level and attribute names compatible with the current condition
                // This call is for disambiguation purposes, so use only active levels
                ArrayList levelAttributePairs = getCompatibleLevelAndAttributeNames(currentCondition,true);
                // From these, get list of *distinct* level names
                ArrayList distinctLevelNames = getDistinctLevelNames(levelAttributePairs);
                System.err.println("Distinct levels for "+currentCondition+": "+distinctLevelNames.toString());
                
                if (distinctLevelNames.size()==0)
                {
                    // If any part of the token does not return at least one level, the entire token
                    // cannot be resolved
                    String message = "Error: Query token condition\n\n";
                    message = message + currentCondition+"\n\n";
                    message = message + "cannot be identified: No matching levels, attributes, and/or values!";
        
                    message = message +"\n\nIf one of the attributes you access is of type FREETEXT,\n";
                    message = message + "note that these cannot be disambiguated by their values!";
                    
                    System.err.println(message);
                    if (interactive) displayUserMessage(message);
                    return "";                                        
                }
                
                if (accumulatedDistinctLevelNames.size()==0)
                {
                    // This is the first set of distinct level names, so nothing to disambiguate 
                    // against yet. Store all distinct names.
                    accumulatedDistinctLevelNames.addAll(distinctLevelNames);
                }
                else
                {
                    // There already is a list of level names to disambiguate against
                    // accumulatedDistinctLevelNames contains at least 2 elements (why?)
                    // and so does distinctLevelNames (why?)
                    ArrayList intersection = intersectLists(distinctLevelNames, accumulatedDistinctLevelNames);
                    // So keep intersection as disambiguation list for next condition
                    accumulatedDistinctLevelNames = null;
                    accumulatedDistinctLevelNames = new ArrayList(intersection);
                }
            }
            
            // Now, accumulatedDistinctLevelNames must contain exactly one entry
            if (accumulatedDistinctLevelNames.size()==1)
            {
                accessedLevel = (String) accumulatedDistinctLevelNames.get(0);
            }
            else if (accumulatedDistinctLevelNames.size()==0)
            {
                // No common level could be found
                // That is a serious error that cannot be resolved
                String message = "Error: Query token\n\n";
                message = message + tokenAsInput+"\n\n";
                message = message + "accesses more than a single level!";
                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";                
            }
            else
            {
                // No unique level could be found
                // No unique level name could be determined
                // That is a serious error that cannot be resolved
                String message = "Error: Query token\n\n";
                message = message + tokenAsInput+"\n\n";
                message = message + "is ambiguous! Possible levels:\n\n";
                for (int r=0;r<accumulatedDistinctLevelNames.size();r++)
                {
                    message = message + (String) accumulatedDistinctLevelNames.get(r)+"\n";
                }
                message = message +"\n\nIf one of the attributes you access is of type FREETEXT,\n";
                message = message + "note that these cannot be disambiguated by their values!";
                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";                
            }
        }
                        
        // Now, accessed level is certain to be non-empty        
        // Iterate over single conditions again        
        for (int v=0;v<singleQueryTokenConditions.size();v++)
        {
            // Get current condition
            currentCondition = (String)singleQueryTokenConditions.get(v);
            
            // Get list of pairs of level and attribute names compatible with the current condition
            ArrayList levelAttributePairs = getCompatibleLevelAndAttributeNames(currentCondition,false);
            // If currentCondition contained an explicit attribute, all mismatching entries have been 
            // removed by now        
        
            // We already know which level the attribute has to come from
            // Iterate over all level:attribute pairs
            for (int o=levelAttributePairs.size()-1;o>=0;o--)
            {
                if (((String)levelAttributePairs.get(o)).startsWith(accessedLevel+":")==false)
                {
                    // If the current level:attribute pair does not come from accessed level, remove it
                    levelAttributePairs.remove(o);
                }
            }                
            
            // Now, levelAttributePairs should contain exactly one entry
            if (levelAttributePairs.size()==1)
            {
                // Get single entry of the form level:attribute
                String uniqueEntry = (String) levelAttributePairs.get(0);
                // Extract level part
                String level = uniqueEntry.substring(0,uniqueEntry.indexOf(":"));
                // Extract attribute part
                String attribute = uniqueEntry.substring(uniqueEntry.indexOf(":")+1);
                
                if (currentCondition.indexOf("=")!=-1)
                {
                    // If current condition did contain an attribute name, cut that off
                    currentCondition=currentCondition.substring(currentCondition.indexOf("=")+1);
                }
                
                // Now, current condition is a *value* only.
                
                if (currentCondition.startsWith("{")==false && currentCondition.endsWith("}")==false)
                {
                    // If it is not in parentheses, put it in now
                    currentCondition="{"+currentCondition+"}";
                }
                                
                result = result + (String)singleQueryTokenConditionNegations.get(v)+ attribute+"="+currentCondition+" "+connector+" ";
            }
            else if (levelAttributePairs.size()==0)
            {
                String message = "Error: Query token\n\n";
                message = message + tokenAsInput+"\n\n";
                message = message + "cannot be identified: No matching levels, attributes, and/or values!";
                message = message +"\n\nIf one of the attributes you access is of type FREETEXT,\n";
                message = message + "note that these cannot be disambiguated by their values!";

                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";
            }
            else
            {
                // More than one level-attribute pair was found
                String message = "Error: Query token\n\n";
                message = message + tokenAsInput+"\n\n";
                message = message + "is ambiguous! Matching candidates:\n\n";
                for (int b=0;b<levelAttributePairs.size();b++)
                {
                    String temp = (String) levelAttributePairs.get(b);
                    String level = temp.substring(0,temp.indexOf(":"));
                    String attribute = temp.substring(temp.indexOf(":")+1);
                    if (attribute.startsWith("*"))
                    {
                        attribute = attribute.substring(1);
                    }
                   message = message + "attribute "+attribute +" on level "+level+"\n";
                }
                message = message + "\nPlease make your query more specific!";
                System.err.println(message);
                if (interactive) displayUserMessage(message);
                return "";
            }
        }
        result = result.substring(0,result.length()-4);
        result = "let $"+variableName+" = "+ accessedLevel+" ("+result+" )";
        return result;
    }
    
    private final ArrayList intersectLists (ArrayList _list1, ArrayList _list2)
    {        
        ArrayList list1 = new ArrayList(_list1);
        ArrayList list2 = new ArrayList(_list2);
        if (list1.size() > list2.size())
        {
            list1.retainAll(list2);
            return list1;
        }
        else if (list2.size() > list1.size())
        {
            list2.retainAll(list1);
            return list2;
        }
        else
        {
            list1.retainAll(list2);
            return list1;            
        }            
    }
    
    private final ArrayList getDistinctLevelNames(ArrayList list)
    {
        ArrayList result = new ArrayList();
        // Iterate over input list
        for (int g=0;g<list.size();g++)
        {
            // Get current level-attribute pair
            String currentEntry = (String) list.get(g);
            // Extract level name
            String currentLevelName = currentEntry.substring(0,currentEntry.indexOf(":"));
            if (result.contains(currentLevelName)==false)
            {
                // Add level name to list only once
                result.add(currentLevelName);
            }
        }        
        return result;
    }
    
    private final boolean containsLevelName(String condition)
    {
        boolean result = false;
        // Get numerical pos of first . in string
        int dotPos = condition.indexOf(".");
        if (dotPos != -1)
        {
            // Some dot was found
            // Now make sure it is not in the condition part of a freetext query
            // I.E: exclude test=.*, for test = freetext attribute
            // This can be excluded if we find the = at a later position,
            // because for freetext queries, there always has to be the attribute name explicit
            // Get position of = in condition
            int equalsPos = condition.indexOf("=");
            if (equalsPos == -1 || equalsPos > dotPos)
            {
                // Only if an = was found after the dotPos do we know that the dot is a level name separator
                result=true;
            }
            else
            {
                // Either no = was found, or it occured before the .
            }
        }
        return result;
    }
    
    
    public final ArrayList getCompatibleLevelAndAttributeNames(String singleCondition, boolean activeLevelsOnly)
    {
        // Possible forms of input:
        // 1. jj
        // 2. pos={jj}, pos=jj
        // 3. {jj,jjr,jjs}
        // 4. pos={jj,jjr,jjs}
    
        // Freetext attributes can be accessed, but they *must* specify the attribute name
        
        ArrayList result=new ArrayList();
        
        String attributeName="";
        int equalsAt = singleCondition.indexOf("=");
        if (equalsAt!=-1)
        {
            // The current condition explicitly contains the attribute name
            // Extract it
            attributeName = singleCondition.substring(0,equalsAt);
            // And remove it
            singleCondition=singleCondition.substring(equalsAt+1);
        }
        // Make sure attribute values are always in {...}
        if (singleCondition.startsWith("{")==false && singleCondition.endsWith("}")==false)
        {
            singleCondition="{"+singleCondition+"}";
        }        
        
        // Get list of level:attribute pairs for which all values are defined
        // New: Also supply extracted attribute name, if any was found
        result = chart.getLevelAndAttributeNamesForValues(singleCondition,attributeName,activeLevelsOnly);
        // If the current condition did specify an attribute name, throw out all others
        if (attributeName.equals("")==false)
        {
            // Iterate over level:attribute pairs backwards
            for (int n=result.size()-1;n>=0;n--)
            {
                // If the current entry does not belong to the specified attribute, delete it
                if (((String)result.get(n)).endsWith(":"+attributeName)==false && ((String)result.get(n)).endsWith(":*"+attributeName)==false)
                {
                    result.remove(n);
                }
            }
        }
        return result;
    }
        
    /** This method receives a string representing a single simplified query token, and returns 
        a list of all its AND- or OR-connected parts (separated by , or ;, respectively).
        This method is required because splitting the string at all , positions is not safe because a string
        may contain ,-tokens at certain positions. */
    public final ArrayList[] parseSequenceQueryTokenConditions(String tokenString)
    {
        ArrayList[] result = new ArrayList[3];
        ArrayList conditions = new ArrayList();        
        ArrayList negations = new ArrayList();
        ArrayList connector = new ArrayList();
        
        result[0] = conditions;
        result[1] = negations;
        result[2] = connector;
        
        // Iterate over entire string
        boolean inCurlyBraces = false;
        int currentStart = 0;
        String currentToken = "";
        // Iterate over string containing alll conditions
        for (int b=0;b<tokenString.length();b++)
        {
            // Get current character
            String currentChar = tokenString.substring(b,b+1);
            if (currentChar.equals("{"))
            {
                inCurlyBraces = true;
                continue;
            }
            if (currentChar.equals("}"))
            {
                inCurlyBraces = false;
                continue;
            }
            if (currentChar.equals(",") && !inCurlyBraces)
            {
                // We found a comma as connector
                if (connector.contains("or"))
                {
                    // But we found a semicolon earlier
                    // Thats an error
                    displayUserMessage("Inconsistent use of ,(AND) and ;(OR) in query token!");
                    result[0] = new ArrayList();
                    result[1] = new ArrayList();
                    result[2] = new ArrayList();
                    return result;                    
                }
                else
                {
                    connector.add("and");
                    currentToken = tokenString.substring(currentStart,b).trim();
                    conditions.add(currentToken);
                    currentStart = b+1;
                }
            }
            else if (currentChar.equals(";") && !inCurlyBraces)
            {
                // We found a semicolon as connector
                if (connector.contains("and"))
                {
                    // But we found a comma earlier
                    // Thats an error
                    displayUserMessage("Inconsistent use of ,(AND) and ;(OR) in query token!");
                    result[0] = new ArrayList();
                    result[1] = new ArrayList();
                    result[2] = new ArrayList();                    
                    return result;                    
                }
                else
                {
                    connector.add("or");
                    currentToken = tokenString.substring(currentStart,b).trim();
                    conditions.add(currentToken);
                    currentStart = b+1;
                }                
            }
        }
        currentToken = tokenString.substring(currentStart).trim();
        conditions.add(currentToken);     
        
        String currentCondition = "";
        // Iterate over all conditions
        for (int z=0;z<conditions.size();z++)
        {
            currentCondition=(String) conditions.get(z);
            if (currentCondition.startsWith("!")==false)
            {
                // Add empty string in negation list of not negated
                negations.add("");
            }
            else
            {
                // Add negation sign
                negations.add("!");
                // and remove from condition proper
                conditions.set(z, currentCondition.substring(1));
            }
        }
        
        result[0] = conditions;
        result[1] = negations;
        result[2] = connector;
        return result;
    }
    
    
    /** This method is the main processing method for simplified MMAXQL queries. It receives a string in 
        simplified MMAXQL and returns a sequence of queries in converted MMAXQL, ready to be executed. */
    public final String convertFromSimplifiedMMAXQL(String simplifiedCommand)
    {
        return processBracketedSimplifiedMMAXQLQuery(simplifiedCommand);
    }
    
    public final void displayUserMessage(String message)
    {       
        JOptionPane.showMessageDialog(null,message,"Simplified MMAXQL Converter",JOptionPane.ERROR_MESSAGE);
    }
    

    public final boolean isTopLevelOpeningBracket(String inString, int pos)
    {
        boolean result=false;
        if (inString.substring(pos,pos+1).equals("("))
        {
            // The char at current pos is indeed an opening bracket
            // It is top level only if 
            // - it has a space left to it, or is at the beginning of the line
            if (pos==0 || inString.substring(pos-1,pos).equals(" "))
            {                
                // Now get pos. of matching bracket
                int match=MMAX2QueryWindow.getFollowingMatchingBracket(inString,")",pos);
                if (isInFirstPartOfQueryToken(inString,match))
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }     
        return result;
    }
    
    public final boolean isInFirstPartOfQueryToken(String entireString, int pos)
    {
        // This method returns true if the current pos is in the left part of the token it is part
        // of. This is determined by finding the next /, if any. That must appear before the next space,
        // if any
        boolean result = false;
        int nextSpacePos = entireString.indexOf(" ",pos);
        if (nextSpacePos==-1)
        {
            nextSpacePos=Integer.MAX_VALUE;
        }
        int nextSlashPos = entireString.indexOf("/",pos);
        
        if (nextSlashPos!=-1 && nextSlashPos < nextSpacePos)
        {
            // The next slash after pos occurs before the next space after pos
            // so pos is in the left part of the token
            // This does also work if no space was found at all
            result = true;
        }
        return result;
    }
    
    public final int[] getNextTerminalPositions(String entireInputString, int from, int to)
    {
        
        // At the first call, from is 0 and to is string.length!
        int[] result = new int[2];
        String currentSubstring ="";
        try
        {
            currentSubstring = entireInputString.substring(from,to);
        }
        catch (java.lang.StringIndexOutOfBoundsException ex)
        {
            JOptionPane.showMessageDialog(null,"Query Syntax error in\n\n"+entireInputString+"\n\nBracketing mismatch?\nQuery will terminate.","MMAXQL Query Error",JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        // Check whether there are any brackets in the not-yet-processed part from from to to
        // This is not safe!! Brackets can appear in regExps anywhere in the query!!
        // New May 26, 2005:
        // However, if NO brackets can be found, we can assume safely 
        // that we have a terminal
        if (currentSubstring.indexOf("(")==-1 && currentSubstring.indexOf(")")==-1)
        {
            // If not, the current substring is our next terminal
            // This is the 'abbruchbedingung' of the recursion!
            // Return span of substring as was passed in
            result[0]=from;
            result[1]=to;
            return result;
        }
        else
        {   
            // There are still some brackets somewhere in substring
            // String is always the entire string, modified by replacements
            int matchingStart = 0;
            int matchingEnd = 0;
            boolean found = false;
            String currentChar="";
            
            // Iterate from from to to
            for (matchingStart=from;matchingStart<to-1;matchingStart++)
            {       
                if (isTopLevelOpeningBracket(entireInputString,matchingStart))
                {
                    if (MMAX2QueryWindow.isInReferenceListBrackets(entireInputString,matchingStart))
                    {
                        continue;
                    }                    
                    found = true;
                    matchingEnd = MMAX2QueryWindow.getFollowingMatchingBracket(entireInputString,")", matchingStart);
                    break;
                }                                    
            }
            
            if (!found)
            {
                // The entire input string was parsed in its entirety, and
                // no matching pair of TLDs was found
                // Return span of substring as was passed in
                result[0]=from;
                result[1]=to;
                return result;
            }
            else
            {
                // A pair of tl brackets was found, at positions
                // matchingStart and -end                
                char[] temp = entireInputString.toCharArray();
                temp[matchingStart]=' ';
                // Overwrite final )
                temp[matchingEnd]=' ';
                matchingEnd++;
                entireInputString = new String(temp);                
            }
            
            return getNextTerminalPositions(entireInputString, matchingStart, matchingEnd);        
        }        
    }

               
    /** This method receives an entire (potentially complex) string of simplified MMAXQL and returns 
        the equivalent complex MMAXQL statement. */
    public final String processBracketedSimplifiedMMAXQLQuery(String simplifiedCommand)
    {
        String result = "";
        userLevelVariables.clear();
        // Leading s! has been chopped off already
        int[] terminal = new int[2];
        // Init counter for terminals, used as disambiguator for var names
        int termsFound=0;
        while (simplifiedCommand.length()>0)
        {
            // Search string depth first for first terminal
            terminal = getNextTerminalPositions(simplifiedCommand,0,simplifiedCommand.length());
        
            if (terminal == null)
            {
                return "";
            }
            // Get text of first terminal
            if (simplifiedCommand.substring(terminal[0],terminal[0]+1).equals("(") &&
                simplifiedCommand.substring(terminal[1]-1,terminal[1]).equals(")"))
            {
                // Only remove if brackets are indeed grouping brackets
                terminal[0]=terminal[0]+1;
                terminal[1]=terminal[1]-1;
            }            
            
            // Get current substring that was just extracted
            String extract = simplifiedCommand.substring(terminal[0],terminal[1]);                        
            
            // New April 18th, 2005: Handle col specs 
            String colSpec = "";
            if (terminal[1]+2<=simplifiedCommand.length() &&
                simplifiedCommand.substring(terminal[1]+1,terminal[1]+2).equals("."))
            {
                // There is a dot directly after the closing bracket, which means there is a col spec
                // Extract it
                for (int m=terminal[1]+1;m<simplifiedCommand.length();m++)
                {
                    String g = simplifiedCommand.substring(m,m+1);
                    if (g.equals(";")==false && g.equals(" ")==false)
                    {
                        colSpec = colSpec+g;
                    }
                    else
                    {
                        break;
                    }
                }
                if (colSpec.equals(""))
                {
                    System.err.println("Error with column specifier!");
                    return "";
                }
                terminal[1]=terminal[1]+colSpec.length();
                colSpec = colSpec.trim();                
            }            
            
            // Increase counter for terms found
            termsFound++;
            
            // From current terminal, create MMAXQL sequence, using termsFound as name for 
            // this level and as namespace for embedded levels. 
            // Store result
            result = result + simplifiedSequenceQueryToMMAXQL(extract,"let $"+termsFound+" = ", termsFound+"");
            result=result+colSpec+";";
            
            
            if (terminal[0] == 0 && terminal[1] == simplifiedCommand.length())
            {
                System.err.println("Done");
                break;
            }            
            String leftPart = "";
            if (terminal[0] > 0)
            {
                leftPart = simplifiedCommand.substring(0,terminal[0]-1);
            }
            
            String rightPart = "";
            if (terminal[1]+1<simplifiedCommand.length())
            {
                rightPart = simplifiedCommand.substring(terminal[1]+1);
            }
            simplifiedCommand = leftPart + " var:$"+termsFound+" "+ rightPart;
        }
        
        return termsFound+":"+result;
    }
        
    public ArrayList removeRedundantVariableAssigments(ArrayList list)
    {
        // This removes unneccessary assignments of user vars to system vars.
        // It does NOT (YET) remove similar assignments from system to system vars!!
        
        String[] userVars = (String[])userLevelVariables.toArray(new String[0]);
        // Iterate over all user-level variables used in current query
        for (int z=0;z<userVars.length;z++)
        {
            // Get current user-level variable            
            String currentUserVar = userVars[z];
            // Iterate over all result parts
            for (int t=0;t<list.size();t++)
            {
                // Get current Command
                String currentCommand = ((String)list.get(t)).trim();
                // Now check whether currentCommand assigns the current user var to some variable
                if (((currentCommand+" ").indexOf("= "+currentUserVar+" "))!=-1)
                {
                    // Yes
                    // Extract name of variable to which user var is assigned
                    String currentReceiver = currentCommand.substring(currentCommand.indexOf(" "),currentCommand.indexOf("=")).trim();
                    // Destroy current command
                    list.set(t,"  ");
                    // Iterate over entire list again
                    for (int b=0;b<list.size();b++)
                    {
                        String currentModifiableCommand = (String)list.get(b);
                        currentModifiableCommand = currentModifiableCommand.replaceAll("\\"+currentReceiver, "\\"+currentUserVar);
                        list.set(b, currentModifiableCommand);
                    }
                }
            }
        }                
        
        // Iterate over all commands, which are cleansed for sv = uv already
        for (int z=0;z<list.size();z++)
        {
            String currentCommand=((String) list.get(z)).trim();            
            if (currentCommand.startsWith("let"))
            {
                // The current command is an assignment
                // Get var that is assigned to (can be user or system)
                String currentReceiver = currentCommand.substring(currentCommand.indexOf(" "),currentCommand.indexOf("=")).trim();
                if (MMAX2QueryWindow.isSystemVariable(currentReceiver))
                {
                    // Current var is system!
                    // Now determine whether currentReceiver receives val of other var
                    String currentVal = currentCommand.substring(currentCommand.lastIndexOf(" ")).trim();
                    if (MMAX2QueryWindow.isSystemVariable(currentVal))
                    {
                        // Current val is indeed a system variable
                        // Destroy current assignment
                        list.set(z,"  ");                    
                        // Iterate over entire list again
                        for (int b=0;b<list.size();b++)
                        {
                            String currentModifiableCommand = ((String)list.get(b))+" ";
                            currentModifiableCommand = currentModifiableCommand.replaceAll("\\"+currentVal+" ", "\\"+currentReceiver+" ");
                            list.set(b, currentModifiableCommand);
                        }                        
                    }
                }
            }
        }
                

        // Iterate over all commands, which are cleansed for sv = uv already
        for (int z=0;z<list.size();z++)
        {
            String currentCommand=((String) list.get(z)).trim();            
            if (currentCommand.startsWith("let"))
            {
                // The current command is an assignment
                // Get var that is assigned to (can be user or system)
                String currentReceiver = currentCommand.substring(currentCommand.indexOf(" "),currentCommand.indexOf("=")).trim();
                if (currentReceiver.startsWith("$") && MMAX2QueryWindow.isSystemVariable(currentReceiver)==false)
                {
                    // Current var is system!
                    // Now determine whether currentReceiver receives val of other var
                    String currentVal = currentCommand.substring(currentCommand.lastIndexOf(" ")).trim();
                    if (MMAX2QueryWindow.isSystemVariable(currentVal))
                    {
                        // Current val is indeed a system variable
                        // Destroy current assignment
                        list.set(z,"  ");                    
                        // Iterate over entire list again
                        for (int b=0;b<list.size();b++)
                        {
                            String currentModifiableCommand = ((String)list.get(b))+" ";
                            currentModifiableCommand = currentModifiableCommand.replaceAll("\\"+currentVal+" ", "\\"+currentReceiver+" ");
                            list.set(b, currentModifiableCommand);
                        }                        
                    }
                }
            }
        }

        // Iterate over all commands, which are cleansed for sv = uv already
        for (int z=0;z<list.size();z++)
        {
            String currentCommand=((String) list.get(z)).trim();            
            if (currentCommand.startsWith("let"))
            {
                // The current command is an assignment
                // Get var that is assigned to (can be user or system)
                String currentReceiver = currentCommand.substring(currentCommand.indexOf(" "),currentCommand.indexOf("=")).trim();
                if (MMAX2QueryWindow.isSystemVariable(currentReceiver))
                {
                    // Current var is system!
                    // Now determine whether currentReceiver receives val of other var
                    String currentVal = currentCommand.substring(currentCommand.lastIndexOf(" ")).trim();
                    if (currentVal.startsWith("$") && MMAX2QueryWindow.isSystemVariable(currentVal)==false)
                    {
                        // Current val is indeed a system variable
                        // Destroy current assignment
                        list.set(z,"  ");                    
                        // Iterate over entire list again
                        for (int b=0;b<list.size();b++)
                        {
                            String currentModifiableCommand = ((String)list.get(b))+" ";
                            currentModifiableCommand = currentModifiableCommand.replaceAll("\\"+currentReceiver+" ", "\\"+currentVal+" ");
                            list.set(b, currentModifiableCommand);
                        }                        
                    }
                }
            }
        }

        // New May 15th: Remove sequences of let $1 = (...); display $1 and modify into display (...)
        // Iterate over all commands, which are cleansed for sv = uv already
        for (int z=0;z<list.size();z++)
        {
            boolean found = false;
            String currentCommand=((String) list.get(z)).trim();
            if (currentCommand.startsWith("display "))
            {
                // The current command is a top-level command
                // Now look if its argument is a system-var
                String currentArgument = currentCommand.substring(currentCommand.indexOf(" ")).trim();
                if (MMAX2QueryWindow.isSystemVariable(currentArgument))
                {
                    // The argument to display is a system-var
                    // Find command before the current one in which currentArgument is assigned a value
                    for (int n=0;n<z;n++)
                    {
                        String tempCommand=((String) list.get(n)).trim();
                        if (tempCommand.startsWith("let "+currentArgument+" "))
                        {
                            // The current tempCommand is the one in which currentArg is assigned a value
                            String tempAssigment = tempCommand.substring(currentArgument.length()+6).trim();
                            list.set(n, ("display "+tempAssigment));
                            found=true;
                            break;
                        }
                    }
                    if (found) 
                    {
                        list.remove(z);
                        break;
                    }
                }
            }
        }
        
        return list;
    }
    
    public int getMatchingBracketPosition(int startposition, String queryString, String openingBracket, String closingBracket)
    {
        int openedBrackets = 0;
        int closedBrackets = 0;
        String currentChar="";
        /* Iterate over rest of tree from startposition on */
        for (int z=startposition+1;z<queryString.length();z++)
        {
            currentChar = queryString.substring(z,z+1);
            if (currentChar.equals(closingBracket))
            {
                /* The character at the current position is ) */
                if (openedBrackets == closedBrackets)
                {
                    /* We are on par */
                    return z+1;
                }
                else
                {
                    closedBrackets++;
                }
            }
            else if (currentChar.equals(openingBracket))
            {
                openedBrackets++;
            }
        }// for z        
        return -1;
    }    
    
    
}
