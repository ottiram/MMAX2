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

package org.eml.MMAX2.api;
import org.eml.MMAX2.annotation.markables.MarkableRelation;
import org.eml.MMAX2.annotation.scheme.UIMATypeMapping;


public interface AttributeAPI 
{
	public static int NOMINAL_BUTTON = 1;
    public static int NOMINAL_LIST = 2;
    public static int FREETEXT = 3;
    public static int MARKABLE_SET = 5;
    public static int MARKABLE_POINTER = 6;  

    /**  <b><font size=+1 color=green>(API)</font></b>  Returns the {@link MarkableRelationAPI MarkableRelation} object that this MMAX2Attribute is associated with.
     * 
     * @return The {@link MarkableRelationAPI MarkableRelation} object that this MMAX2Attribute is associated with, or null if none.
     */
    public MarkableRelation getMarkableRelation();
    
    public UIMATypeMapping getUIMATypeMapping();
}
