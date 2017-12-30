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
import org.eml.MMAX2.annotation.markables.MarkableLevel;

/** This interface defines public methods for accessing Markable objects.
 */
public interface MarkableAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the ID string of this Markable, e.g. 'markable_12'. 
	 * @return The ID of this Markable. 
	 * */
	public String getID();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the string value of this Markable's attribute, or returnIfUndefined if attribute is undefined for this Markable. 
	 * @param attributeName The name of the attribute whose value to return.
	 * @param returnIfUndefined The string to return if attribute is undefined.
	 * @return The value of the attribute, or returnIfUndefined if attribute is undefined.
	 * */
	public String getAttributeValue(String attributeName, String returnIfUndefined);
	
	/** <b><font size=+1 color=green>(API)</font></b> Sets this markable's attribute to value.
	 * 
	 * @param attributeName The attribute whose value to set.
	 * @param value The value to assign to the attribute.
	 */
	public void setAttributeValue(String attributeName, String value);
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the MarkableLevel object that this Markable belongs to.
	 * 
	 * @return The {@link MarkableLevelAPI MarkableLevel} that this Markable belongs to.
	 */
	public MarkableLevel getMarkableLevel();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the string representation of this Markable object.
	 * 
	 * @return The string representation of this Markable's text, including square brackets at fragment boundaries.
	 */
	public String toString();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the trimmed string representation of this Markable object.
	 * 
	 * @param maxWidth The maximum string width to return. 
	 * @return The string representation of this Markable's text, including square brackets at fragment boundaries. The text is trimmed to contain maximally maxWidth characters. Trimming is performed by removing a part of the string in the middle and replacing it with [...].
	 */
	public String toTrimmedString(int maxWidth);
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the discourse position of the leftmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this Markable object contains.
	 * 
	 * @return The discourse position of the leftmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this Markable object contains. The discourse position is normally the base data element's position in the base data file.
	 */
	public int getLeftmostDiscoursePosition();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the discourse position of the rightmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this Markable object contains.
	 * 
	 * @return The discourse position of the rightmost {@link DiscourseElementAPI MMAX2DiscourseElement} that this Markable object contains. The discourse position is normally the base data element's position in the base data file.
	 */
	public int getRightmostDiscoursePosition();

}
