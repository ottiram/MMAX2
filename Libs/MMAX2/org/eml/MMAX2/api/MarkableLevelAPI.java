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

import java.util.ArrayList;
import java.util.HashMap;

import org.eml.MMAX2.annotation.markables.Markable;

/** This interface defines public methods for accessing MarkableLevel objects.
 */
public interface MarkableLevelAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the name of this MarkableLevel. Each MarkableLevel in a MMAX2 document must have a unique name. This name is assigned in the common_paths.xml file.  
	 * 
	 * @return The name of this MarkableLevel.
	 */
	public String getMarkableLevelName();
	
	/** <b><font size=+1 color=green>(API)</font></b> Saves the Markables on this MarkableLevel to file fileName. If fileName is an empty string, it saves the Markables to the location the MarkableLevel was loaded from.
	 * 
	 * @param fileName The name of the file in which the Markables are to be saved.
	 */
	public void saveMarkables(String fileName);
	
	/** <b><font size=+1 color=green>(API)</font></b> Adds a Markable to this MarkableLevel, and returns the newly added Markable. 
	 * @param discourseElements An ArrayList of DiscourseElement objects which the new Markable is to span.
	 * @param attributes A HashMap of content attibutes that the new Markable is to carry. 
	 * @return The newly added Markable object.
	 */
	public Markable addMarkable(ArrayList discourseElements, HashMap attributes);
	
	/** <b><font size=+1 color=green>(API)</font></b> Deletes the Markable toDelete from this MarkableLevel.
	 * 
	 * @param toDelete The Markable to delete.
	 */
	public void deleteMarkable(Markable toDelete);
	
	/** <b><font size=+1 color=green>(API)</font></b> Deletes all Markables from this MarkableLevel.
	 */
	public void deleteAllMarkables();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns an array of all Markables associated with the DiscourseElement at discourse position discoursePosition, or empty array if none.
	 * 
	 * @param discoursePosition The numeric discourse position for which the Markables on this MarkableLevel are to be returned.
	 * @return An array of Markable objects
	 */
    public Markable[] getAllMarkablesAtDiscoursePosition(int discoursePosition);

    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all Markables whose last DiscourseElement is the one with ID discourseElementID, or empty array if none.
     * 
     * @param discourseElementID The ID of a DiscourseElement (e.g. 'word_4').
     * @return An array of Markable objects.
     */
    public Markable[] getAllMarkablesEndedByDiscourseElement(String discourseElementID);

    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all Markables whose first DiscourseElement is the one with ID discourseElementID, or empty array if none.
     * 
     * @param discourseElementID The ID of a DiscourseElement (e.g. 'word_4').
     * @return An array of Markable objects.
     */
    public Markable[] getAllMarkablesStartedByDiscourseElement(String discourseElementID);
 
    
    /** <b><font size=+1 color=green>(API)</font></b> Creates, adds and returns an new Markable which spans the DiscourseElements (i.e. words) with the IDs (e.g. 'word_4') in discourseElementId. 
     * 
     * @param discourseElementIDs An array of strings of the IDs of the DiscourseElements that the new Markable spans.
     * @param attributes A HashMap of attribute-value pairs that constitute the new Markable's initial attributes, or null if no attributes are.
     * @return A Markable object.
     */
    public Markable addMarkable(String[] discourseElementIDs, HashMap attributes);    
    
}
