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
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;

/** This interface defines public methods for accessing MMAX2Discourse objects.
 */
public interface DiscourseAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the MarkableLevel object of name levelName, or null if no MarkableLevel of this name exists.
	 * 
	 * @param levelName The name of the {@link MarkableLevelAPI MarkableLevel} to return.
	 * @param interactive If true, a message box will be displayed if no {@link MarkableLevelAPI MarkableLevel} of name levelName could be found.
	 * @return The {@link MarkableLevelAPI MarkableLevel} object of name levelName, or null. 
	 */
	public MarkableLevel getMarkableLevelByName(String levelName, boolean interactive);
	
	/**  <b><font size=+1 color=green>(API)</font></b>  Loads the .mmax file of name inFileName, using the common_paths.xml file specified in commonPathsFileName.
	 * <br> Note: This is a dummy implementation only. The actual method to use is the <b>*static*</b> method
	 * <br><br>
	 * public static MMAX2Discourse buildDiscourse(String inFileName, String commonPathsFileName);
	 * 
	 * @param inFileName The name and path of the .mmax file to load.
	 * @param commonPathsFileName The name (and possibly path) of the common_paths.xml file to use. If this parameter is an empty string, the file common_paths.xml in the current directory will be used. 
	 * @return The MMAX2Discourse object containing the loaded .mmax file.
	 */
	public MMAX2Discourse _buildDiscourse(String inFileName, String commonPathsFileName);
	
	/**  <b><font size=+1 color=green>(API)</font></b>  Returns the {@link DiscourseElementAPI MMAX2DiscourseElement} object (i.e. base data element) associated with discourse position discoursePosition, or null if none exists at this position.
	 * @param discoursePosition The numeric discourse position of the {@link DiscourseElementAPI MMAX2DiscourseElement} to return. This is normally the base data element's position in the base data file.
	 * @return The {@link DiscourseElementAPI MMAX2DiscourseElement} at discourse position discoursePosition.
	 */
	public MMAX2DiscourseElement getDiscourseElementAtDiscoursePosition(int discoursePosition);
	
	/**  <b><font size=+1 color=green>(API)</font></b>  Returns the {@link DiscourseElementAPI MMAX2DiscourseElement} object (i.e. base data element) associated with the ID discourseElementID (e.g. 'word_4'), or null if none exists.
	 * @param discourseElementID The ID of the {@link DiscourseElementAPI MMAX2DiscourseElement} to return.
	 * @return The {@link DiscourseElementAPI MMAX2DiscourseElement} with ID discourseElementID.
	 */
	public MMAX2DiscourseElement getDiscourseElementByID(String discourseElementID);

	/**  <b><font size=+1 color=green>(API)</font></b>  Returns an array containing all {@link DiscourseElementAPI MMAX2DiscourseElement} objects in the current discourse, ordered in discourse order.
	 * 
	 * @return An array of all {@link DiscourseElementAPI MMAX2DiscourseElement} objects in the current discourse.
	 */
	public MMAX2DiscourseElement[] getDiscourseElements();
	
	/**  <b><font size=+1 color=green>(API)</font></b>  Returns an array containing all {@link DiscourseElementAPI MMAX2DiscourseElement} objects in {@link MarkableAPI Markable} markable, ordered in discourse order.
	 * @param markable The Markable object whose {@link DiscourseElementAPI MMAX2DiscourseElement} objects are to be returned.  
	 * @return An array of all {@link DiscourseElementAPI MMAX2DiscourseElement} objects in {@link MarkableAPI Markable} markable.
	 */	
	public MMAX2DiscourseElement[] getDiscourseElements(Markable markable);

	/** <b><font size=+1 color=green>(API)</font></b>  Returns the {@link DiscourseElementAPI MMAX2DiscourseElement} object at discourse position currentElement.{@link DiscourseElementAPI#getDiscoursePosition()}+1, or null if none exists at this position.
	 * 
	 * @param currentElement The {@link DiscourseElementAPI MMAX2DiscourseElement} object at the current position, or null.
	 * @return The {@link DiscourseElementAPI MMAX2DiscourseElement} object at discourse position currentElement.{@link DiscourseElementAPI#getDiscoursePosition()}+1, or null if none exists at this position. If currentElement is null, the first {@link DiscourseElementAPI MMAX2DiscourseElement} object in the current discourse is returned.  
	 */
	public MMAX2DiscourseElement getNextDiscourseElement(MMAX2DiscourseElement currentElement);
	
	/** <b><font size=+1 color=green>(API)</font></b>  Returns the {@link DiscourseElementAPI MMAX2DiscourseElement} object at discourse position currentElement.{@link DiscourseElementAPI#getDiscoursePosition()}-1, or null if none exists at this position.
	 * 
	 * @param currentElement The {@link DiscourseElementAPI MMAX2DiscourseElement} object at the current position, or null.
	 * @return The {@link DiscourseElementAPI MMAX2DiscourseElement} object at discourse position currentElement.{@link DiscourseElementAPI#getDiscoursePosition()}-1, or null if none exists at this position. If currentElement is null, the last {@link DiscourseElementAPI MMAX2DiscourseElement} object in the current discourse is returned.  
	 */
	public MMAX2DiscourseElement getPreviousDiscourseElement(MMAX2DiscourseElement currentElement);

	/** <b><font size=+1 color=green>(API)</font></b>  Returns the number of {@link DiscourseElementAPI MMAX2DiscourseElement} objects in this discourse.
	 * 
	 * @return The number of {@link DiscourseElementAPI MMAX2DiscourseElement} objects in this discourse.
	 */
	public int getDiscourseElementCount();
	
    /** <b><font size=+1 color=green>(API)</font></b> Returns the DiscoursePosition of the {@link DiscourseElementAPI MMAX2DiscourseElement} currently displayed at display position 
    * displayPosition, or -1 if no DiscourseElement is displayed at this position. 
    * This method is called by the CaretListener to determine which DiscourseElement has been clicked or is hovered over. 
    * Markable handles, literal text and empty space do not have discourse positions! 
    * This method uses the arrays DisplayStartPosition and DisplayEndPosition. 
    * @return discourse position of the element at display position displayPosition
    * */
    public int getDiscoursePositionAtDisplayPosition(int displayPosition);

    /** <b><font size=+1 color=green>(API)</font></b> Returns the display start position (i.e. initial string position) for the 
     * {@link DiscourseElementAPI MMAX2DiscourseElement} at discourse position discoursePosition. It is called during display initialization from
     * Markable.setDisplayPositions(), and during display selection / rendering. 
     * @return display position of the beginning of the discourse element at at discourse position discoursePosition.
     * */
    public int getDisplayStartPositionFromDiscoursePosition(int discoursePosition);

    /** <b><font size=+1 color=green>(API)</font></b> Returns the display end position (i.e. final string position) for the 
     * {@link DiscourseElementAPI MMAX2DiscourseElement} at discourse position discoursePosition. It is called during display initialization from
     * Markable.setDisplayPositions(), and during display selection / rendering. 
     * @return display position of the end of the discourse element at at discourse position discoursePosition.
     * */
    public int getDisplayEndPositionFromDiscoursePosition(int discoursePosition);

}

