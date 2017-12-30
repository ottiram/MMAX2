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

package org.eml.MMAX2.annotation.markables;

public class AlphabeticMarkableComparator implements java.util.Comparator
{
    /** Creates new AlphabeticMarkableComparator. */
    public AlphabeticMarkableComparator() 
    {
        super();
    }
    
    public int compare(Object _markable1, Object _markable2)
    {
        Markable markable1 = (Markable) _markable1;
        Markable markable2 = (Markable) _markable2;

        String string1 = markable1.toString();
        string1 = string1.substring(1,string1.length()-1);
        
        String string2 = markable2.toString();
        string2 = string2.substring(1,string2.length()-1);
        
        return string1.compareToIgnoreCase(string2);
    }    
}

