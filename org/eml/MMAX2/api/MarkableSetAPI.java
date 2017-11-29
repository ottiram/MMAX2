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

package org.eml.MMAX2.api;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableRelation;

/** This interface defines public methods to access MarkableSet objects. MarkableSets are maintained 
 *  by {@link MarkableRelationAPI MarkableRelation} objects of type {@link AttributeAPI#MARKABLE_SET} MARKABLE_SET: 
 *  MarkableRelations of this type have sets of MarkableSet objects, and there is one MarkableSet for 
 *  each different value for the attribute they are associated with. */

public interface MarkableSetAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableRelationAPI MarkableRelation} object that this MarkableSet is associated with.
	 * 
	 * @return The {@link MarkableRelationAPI MarkableRelation} object that this MarkableSet is associated with.
	 */
	public MarkableRelation getMarkableRelation();
	
	/** <b><font size=+1 color=green>(API)</font></b> Whether this MarkableSet contains the {@link MarkableAPI Markable} markable.
	 * 
	 * @param markable The {@link MarkableAPI Markable} object to be tested for membership in this MarkableSet.
	 * @return True if markable is contained in this MarkableSet, else false.
	 */
	public boolean containsMarkable(Markable markable);

	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableAPI Markable} object that this MarkableSet starts with.
	 * 
	 * @return The {@link MarkableAPI Markable} object that this MarkableSet begins with. <b>This method is available for ordered sets only</b>!
	 */	
	public Markable getInitialMarkable();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link MarkableAPI Markable} objects in this MarkableSet.
	 * 
	 * @return An array of all {@link MarkableAPI Markable} objects in this MarkableSet, in discourse order. <b>This method is available for ordered sets only</b>!
	 */		
	public Markable[] getOrderedMarkables();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the position of {@link MarkableAPI Markable} markable in this MarkableSet.
	 * 
	 * @param markable The {@link MarkableAPI Markable} to be localized in this MarkableSet.
	 * @return The index (zero-based) of {@link MarkableAPI Markable} markable, or -1. <b>This method is available for ordered sets only</b>! 
	 */
	public int getMarkableIndex(Markable markable);
}
