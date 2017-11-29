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
import org.eml.MMAX2.annotation.scheme.MMAX2Attribute;

/** This interface defines public methods for accessing MMAX2AnnotationScheme objects. */
public interface AnnotationSchemeAPI 
{
    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link AttributeAPI MMAX2Attribute} objects of type attributeType.
     * 
     * @param attributeType The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects of type attributeType, or empty array. 
     */
	public MMAX2Attribute[] getAttributesByType(int attributeType);

    /** <b><font size=+1 color=green>(API)</font></b> Returns the {@link AttributeAPI MMAX2Attribute} object of type attributeType.
     * 
     * @param attributeType The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return A {@link AttributeAPI MMAX2Attribute} objects of type attributeType, or null. 
     */
	public MMAX2Attribute getUniqueAttributeByType(int attributeType);
	
	
    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link AttributeAPI MMAX2Attribute} objects of either type attributeType1 or attributeType2.
     * 
     * @param attributeType1 The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @param attributeType2 The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects of type attributeType1 and attributeType2, or empty array. 
     */
	public MMAX2Attribute[] getAttributesByType(int attributeType1, int attributeType2);
	

    /** <b><font size=+1 color=green>(API)</font></b> Returns the {@link AttributeAPI MMAX2Attribute} object of either type attributeType1 or attributeType2.
     * 
     * @param attributeType1 The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @param attributeType2 The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return A {@link AttributeAPI MMAX2Attribute} object of type attributeType1 or attributeType2, or empty array. 
     */
	public MMAX2Attribute getUniqueAttributeByType(int attributeType1, int attributeType2);

	
    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link AttributeAPI MMAX2Attribute} objects of whose name matches the RegExp attributeNameRegExp.
     * 
     * @param attributeNameRegExp A regular expression to match the name(s) of the {@link AttributeAPI MMAX2Attribute} object(s) to return, as specified in the annotation scheme file.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects whose names match the RegExp attributeName, or empty array. 
     */
	public MMAX2Attribute[] getAttributesByName(String attributeNameRegExp);

    /** <b><font size=+1 color=green>(API)</font></b> Returns the {@link AttributeAPI MMAX2Attribute} object whose name matches the RegExp attributeNameRegExp.
     * 
     * @param attributeNameRegExp A regular expression to match the name(s) of the {@link AttributeAPI MMAX2Attribute} object(s) to return, as specified in the annotation scheme file.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects whose names match the RegExp attributeName, or empty array. 
     */
	public MMAX2Attribute getUniqueAttributeByName(String attributeNameRegExp);
	
	
    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link AttributeAPI MMAX2Attribute} objects of type attributeType whose name matches the RegExp attributeNameRegExp.
     * @param attributeNameRegExp A regular expression to match the name(s) of the {@link AttributeAPI MMAX2Attribute} object(s) to return, as specified in the annotation scheme file.
     * @param attributeType The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects of type attributeType whose name match attributeNameRegExp, or empty array. 
     */
	public MMAX2Attribute[] getAttributesByNameAndType(String attributeNameRegExp, int attributeType);

    /** <b><font size=+1 color=green>(API)</font></b> Returns the {@link AttributeAPI MMAX2Attribute} object of type attributeType whose name matches the RegExp attributeNameRegExp.
     * @param attributeNameRegExp A regular expression to match the name of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file.
     * @param attributeType The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return A {@link AttributeAPI MMAX2Attribute} object of type attributeType whose name match attributeNameRegExp, or empty array. 
     */
	public MMAX2Attribute getUniqueAttributeByNameAndType(String attributeNameRegExp, int attributeType);
		
    /** <b><font size=+1 color=green>(API)</font></b> Returns an array of all {@link AttributeAPI MMAX2Attribute} objects of type attributeType1 or attributeType2 whose name matches the RegExp attributeNameRegExp.
     * @param attributeNameRegExp A regular expression to match the name(s) of the {@link AttributeAPI MMAX2Attribute} object(s) to return, as specified in the annotation scheme file.
     * @param attributeType1 The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @param attributeType2 The type of the {@link AttributeAPI MMAX2Attribute} objects to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return An array of {@link AttributeAPI MMAX2Attribute} objects of type attributeType1 or attributeType2 whose name match attributeNameRegExp, or empty array. 
     */
	public MMAX2Attribute[] getAttributesByNameAndType(String attributeNameRegExp, int attributeType1, int attributeType2);

    /** <b><font size=+1 color=green>(API)</font></b> Returns a {@link AttributeAPI MMAX2Attribute} object of type attributeType1 or attributeType2 whose name matches the RegExp attributeNameRegExp.
     * @param attributeNameRegExp A regular expression to match the name of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file.
     * @param attributeType1 The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @param attributeType2 The type of the {@link AttributeAPI MMAX2Attribute} object to return, as specified in the annotation scheme file, one of the values defined in {@link AttributeAPI AttributeAPI}.
     * @return A {@link AttributeAPI MMAX2Attribute} objects of type attributeType1 or attributeType2 whose name match attributeNameRegExp, or empty array. 
     */
	public MMAX2Attribute getUniqueAttributeByNameAndType(String attributeNameRegExp, int attributeType1, int attributeType2);
	
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link AttributeAPI MMAX2Attribute} object with ID attributeID, as defined in annotation scheme file.
	 * 
	 * @param attributeID The ID of the {@link AttributeAPI MMAX2Attribute} object to return.
	 * @return The {@link AttributeAPI MMAX2Attribute} object with ID attributeID, or null.
	 */
	public MMAX2Attribute getAttributeByID(String attributeID);
	
}
