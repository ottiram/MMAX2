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

package org.eml.MMAX2;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseLoader;
public class Process 
{       
    public Process() 
    {
        
    }
    
    /** Process a .mmax file with an (enhanced) XSL style sheet and direct output to STDOUT or to a file.
     * @param args Valid arguments are <br>
     * -in The .mmax file to process (obligatory) <br>
     * -common_paths The common_paths file to use. If missing, the default common_paths.xml <b>in the same directory as the .mmax file</b> will be used. <br>
     * -xsl The xsl style sheet to use (obligatory) <br>
     * -out The name of the output file to write to. If missing, output will be written to the standard output.
     */
    public static void main(String[] args) 
    {
        String mmaxFile="";
        String xslFile="";
        String outFile="";
        String cpFile="";
        BufferedWriter fw = null;
        
        for (int u=0;u<args.length;u++)
        { 
            if (args[u].equalsIgnoreCase("-in"))
            {
                mmaxFile=args[u+1];
            }
            if (args[u].equalsIgnoreCase("-out"))
            {
                outFile=args[u+1];
            }            
            if (args[u].equalsIgnoreCase("-xsl"))
            {
                xslFile=args[u+1];
            }
            if (args[u].equalsIgnoreCase("-common_paths"))
            {
                cpFile=args[u+1];
            }            
            
        }
        
        if (outFile.equals("")==false)
        {
            try
            {
                fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            }
            catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
        }
        
        MMAX2DiscourseLoader loader = null;
        MMAX2Discourse discourse = null;
        if (mmaxFile.equals("")==false && xslFile.equals("")==false)
        {
            loader = new MMAX2DiscourseLoader(mmaxFile,false,cpFile);
            discourse = loader.getCurrentDiscourse();
            discourse.applyStyleSheet(xslFile);
            if (fw != null)
            {
                try
                {
                    fw.write(discourse.getStyleSheetOutput());
                    fw.flush();
                    fw.close();
                }
                catch (java.io.IOException ex)
                {
                    ex.printStackTrace();
                }                
            }
            else
            {
            	// Write to STDOUT
                System.out.println(discourse.getStyleSheetOutput());                
            }
        }
        else
        {
            System.err.println("Usage: org.eml.MMAX2.Process -in .mmax-file [-common_paths common_paths-file] -xsl style-file [-out out-file]");
        }
    }
    
}
