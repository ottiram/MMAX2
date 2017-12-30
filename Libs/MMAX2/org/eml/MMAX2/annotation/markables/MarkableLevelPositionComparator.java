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

public class MarkableLevelPositionComparator implements java.util.Comparator
{
    /** Creates new MarkableLevelPositionComparator */
    public MarkableLevelPositionComparator() 
    {
        super();
    }
    
    public int compare(Object _markable1, Object _markable2)
    {
        MarkableLevel level1 = (MarkableLevel) ((Markable)_markable1).getMarkableLevel();
        MarkableLevel level2 = (MarkableLevel) ((Markable)_markable2).getMarkableLevel();
        
        int pos1 = level1.getPosition();
        int pos2 = level2.getPosition();
        if (pos1 < pos2)
        {
            // Level1 is higher in the hierarchy, so we want to close it after level2, so say it's more
            return 1;
        }
        else if (pos2 < pos1)
        {
            // Layer2 is higher in the hierarchy, so we want to close it after layer1, so say it's less
            return -1;
        }
        else
        {
            return 0;
        }        
    }            
}

