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

/** This interface defines public methods for accessing MMAX2DiscourseElement objects.
 */
public interface DiscourseElementAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the ID of this DiscourseElement.
	 * 
	 * @return The ID of this DiscourseElement (e.g. 'word_4').
	 */
	public String getID();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the text string of this DiscourseElement.
	 * 
	 * @return The text string of this DiscourseElement.
	 */
	public String toString();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the discourse position of this DiscourseElement.
	 * 
	 * @return The numerical discourse position of this DiscourseElement. This is normally the base data element's position in the base data file.
	 */
	public int getDiscoursePosition();
	
}
