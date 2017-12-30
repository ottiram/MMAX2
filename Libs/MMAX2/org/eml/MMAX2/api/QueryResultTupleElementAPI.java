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
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;

/** This interface defines public methods for accessing MMAX2QueryResultTupleElement objects. 
 *  A MMAX2QueryResultTupleElement is a wrapper that contains either a Markable or a MMAX2DiscourseElement. 
 *  */

public interface QueryResultTupleElementAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns true if the content of this MMAX2QueryResultTupleElement is a {@link MarkableAPI Markable} object.
	 * 
	 * @return True if the content of this MMAX2QueryResultTupleElement is a {@link MarkableAPI Markable} object, else false.
	 */
	public boolean isMarkable();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableAPI Markable} object that this MMAX2QueryResultTupleElement contains.
	 * 
	 * @return The {@link MarkableAPI Markable} object that this MMAX2QueryResultTupleElement contains, or null if {@link QueryResultTupleElementAPI#isMarkable() isMarkable()} is false. 
	 */	
	public Markable getMarkable();

	/** <b><font size=+1 color=green>(API)</font></b> Returns true if the content of this MMAX2QueryResultTupleElement is a {@link DiscourseElementAPI MMAX2DiscourseElement} object.
	 * 
	 * @return True if the content of this MMAX2QueryResultTupleElement is a {@link DiscourseElementAPI MMAX2DiscourseElement} object, else false.
	 */
	public boolean isDiscourseElement();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link DiscourseElementAPI MMAX2DiscourseElement} object that this MMAX2ResultTupleElement contains.
	 * 
	 * @return The {@link DiscourseElementAPI MMAX2DiscourseElement} object that this MMAX2ResultTupleElement contains, or null if {@link QueryResultTupleElementAPI#isDiscourseElement() isDiscourseElement()} is false. 
	 */	
	public MMAX2DiscourseElement getDiscourseElement();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the string representation of this MMAX2QueryResultTupleElement object.
	 * 
	 * @return The string representation of this MMAX2QueryResultTupleElement object.
	 */
	public String toString();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the trimmed string representation of this MMAX2QueryResultTupleElement object.
	 * @param maxWidth The maximum string width to return.
	 * @return The string representation of this MMAX2QueryResultTupleElement's text. If {@link QueryResultTupleElementAPI#isMarkable() isMarkable()} returns true, this calls {@link MarkableAPI#toTrimmedString(int maxWidth) Markable.toTrimmedString(int maxWidth)}, else it calls {@link DiscourseElementAPI#toString() MMAX2DiscourseElement.toString()}  
	 */
	public String toTrimmedString(int maxWidth);

	/** <b><font size=+1 color=green>(API)</font></b> Returns the ID of the {@link MarkableAPI Markable} or {@link DiscourseElementAPI MMAX2DiscourseElement} object that this MMAX2QueryResultTupleElement contains.
	 * 
	 * @return The ID of the {@link MarkableAPI Markable} or {@link DiscourseElementAPI MMAX2DiscourseElement} object that this MMAX2QueryResultTupleElement contains. If {@link QueryResultTupleElementAPI#isMarkable() isMarkable()} returns true, this calls {@link MarkableAPI#getID() Markable.getID()}, else it calls {@link DiscourseElementAPI#getID() MMAX2DiscourseElement.getID()} 
	 */
	public String getID();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the discourse position of the leftmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this MMAX2QueryResultTupleElement object contains.
	 * 
	 * @return The discourse position of the leftmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this MMAX2QueryResultTupleElement object contains. The discourse position is normally the base data element's position in the base data file. If {@link QueryResultTupleElementAPI#isMarkable() isMarkable()} returns true, this calls {@link MarkableAPI#getLeftmostDiscoursePosition() Markable.getLeftmostDiscoursePosition}, else it calls {@link DiscourseElementAPI#getDiscoursePosition() MMAX2DiscourseElement.getDiscoursePosition()}
	 */
	public int getLeftmostDiscoursePosition();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the discourse position of the rightmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this MMAX2QueryResultTupleElement object contains.
	 * 
	 * @return The discourse position of the rightmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this MMAX2QueryResultTupleElement object contains. The discourse position is normally the base data element's position in the base data file. If {@link QueryResultTupleElementAPI#isMarkable() isMarkable()} returns true, this calls {@link MarkableAPI#getRightmostDiscoursePosition() Markable.getRightmostDiscoursePosition}, else it calls {@link DiscourseElementAPI#getDiscoursePosition() MMAX2DiscourseElement.getDiscoursePosition()}
	 */
	public int getRightmostDiscoursePosition();

}
