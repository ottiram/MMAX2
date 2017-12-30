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
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.annotation.markables.MarkableSet;

/** This interface defines public methods for accessing MarkableRelation objects.<br/>
 *  A MarkableRelation object establishes contact between relation-type attributes (as defined in the annotation scheme file) and
 *  concrete {@link MarkableAPI Markable} objects on a {@link MarkableLevelAPI MarkableLevel} object. There is one MarkableRelation object per {@link AttributeAPI Attribute}. Attributes must be of type
 *  {@link AttributeAPI#MARKABLE_SET MARKABLE_SET} or {@link AttributeAPI#MARKABLE_POINTER MARKABLE_POINTER}.
 *  <br/><br/>
 *  <b>MARKABLE_SET</b>: For each value occurring for an {@link AttributeAPI Attribute}, the MarkableRelation contains a {@link MarkableSetAPI MarkableSet} object containing
 *  those {@link MarkableAPI Markable} objects having this value. 
 *  <br/>
 *  Example: Attribute 'coref_class' has values set_5, set_6. There are two {@link MarkableSetAPI MarkableSet} objects in
 *  this MarkableRelation, mapped to the Strings 'set_5', 'set_6' as keys respectively.
 *  <br/><br/>
 *  <b>MARKABLE_POINTER</b>: For each {@link MarkableAPI Markable} object having a non-empty value for {@link AttributeAPI Attribute} _attributeName, this MarkableRelation contains a
 *  {@link MarkablePointerAPI MarkablePointer} object containing both the source ('pointing') Markables and all Markables pointed to. Example: Markable XY has value
 *  'markable_5,markable_18' in its 'antecedent' {@link AttributeAPI Attribute}. There is one {@link MarkablePointerAPI MarkablePointer} object in this MarkableRelation, mapped to the ID
 *   String of the source Markable as key. */

public interface MarkableRelationAPI 
{
	/**  <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableSetAPI MarkableSet} object containing {@link MarkableAPI Markable} objects with the value setID in the {@link AttributeAPI Attribute} pertaining to this MarkableRelation.
	 * 
	 * @param setID The ID of the {@link MarkableSetAPI MarkableSet} object to be returned (e.g. 'set_4').<br\> In order to retrieve the {@link MarkableSetAPI MarkableSet} containing a particular {@link MarkableAPI Markable} M and all of M's peer Markables in the set called 'coref', use {@link MarkableAPI#getAttributeValue(String, String) M.getAttributeValue('coref', 'empty')} to retrieve the ID of the set that M is a member of, and use this method to retrieve the corresponding set.  
	 * @return The {@link MarkableSetAPI MarkableSet} object with ID setID. 
	 */
	public MarkableSet getMarkableSetWithAttributeValue(String setID);

	/**  <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkablePointerAPI MarkablePointer} object for which the {@link MarkableAPI Markable} sourceMarkable is the source Markable (i.e. the origin of the pointing relation.) 
	 * 
	 * @param  sourceMarkable The source {@link MarkableAPI Markable} of the {@link MarkablePointerAPI MarkablePointer} object to be returned.  
	 * @return The {@link MarkablePointerAPI MarkablePointer} containing the {@link MarkableAPI Markable} sourceMarkable as source Markable.  
	 */
	public MarkablePointer getMarkablePointerForSourceMarkable(Markable sourceMarkable);
	
	/**  <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableSetAPI MarkableSet} object of which {@link MarkableAPI Markable} markable is a member. 
	 * 
	 * @param markable The {@link MarkableAPI Markable} object to be contained in the {@link MarkableSetAPI MarkableSet} to be returned. 
	 * @return The {@link MarkableSetAPI MarkableSet} object containing the {@link MarkableAPI Markable} markable (among others).
	 */
	public MarkableSet getMarkableSetContainingMarkable(Markable markable);
	
	/**  <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link MarkableSetAPI MarkableSet} objects associated with this MarkableRelation.
	 * 
	 * @param sort If true, the array of {@link MarkableSetAPI MarkableSet} objects is sorted according to the discourse position of the initial elements. If false, the order is undefined.
	 * @return An array of all {@link MarkableSetAPI MarkableSet} objects associated with this MarkableRelation.
	 */
	public MarkableSet[] getMarkableSets(boolean sort);

	/** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link MarkablePointerAPI MarkablePointer} objects associated with this MarkableRelation.
	 * 
	 * @param sort If true, the array of {@link MarkablePointerAPI MarkablePointer} objects is sorted according to the discourse position of the respective source Markables elements. If false, the order is undefined.
	 * @return An array of all {@link MarkablePointerAPI MarkablePointer} objects associated with this MarkableRelation.
	 */
	public MarkablePointer[] getMarkablePointers(boolean sort);
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link MarkablePointerAPI MarkablePointer} objects that contain {@link MarkableAPI Markable} targetMarkable as target markable.  
	 * 
	 * @param targetMarkable The {@link MarkableAPI Markable} object to which the returned {@link MarkablePointerAPI MarkablePointer} objects are supposed to point.
	 * @return An array of all {@link MarkablePointerAPI MarkablePointer} objects pointing to {@link MarkableAPI Markable} targetMarkable as target markable.
	 */
	public MarkablePointer[] getMarkablePointersWithTargetMarkable(Markable targetMarkable);
}

