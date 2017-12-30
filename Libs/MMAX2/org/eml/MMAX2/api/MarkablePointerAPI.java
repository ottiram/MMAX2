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

public interface MarkablePointerAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableRelationAPI MarkableRelation} object that this MarkablePointer is associated with.
	 * 
	 * @return The {@link MarkableRelationAPI MarkableRelation} object that this MarkablePointer is associated with.
	 */
	public MarkableRelation getMarkableRelation();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns an array of {@link MarkableAPI Markable} objects that are the target markables in this MarkablePointer.
	 * 
	 * @return An array of {@link MarkableAPI Markable} objects that are the target markables in this MarkablePointer.
	 */
	public Markable[] getTargetMarkables();

	/** <b><font size=+1 color=green>(API)</font></b> Returns the {@link MarkableAPI Markable} objects that is the source markable in this MarkablePointer.
	 * 
	 * @return The {@link MarkableAPI Markable} object that is the source markable in this MarkablePointer.
	 */
	public Markable getSourceMarkable();
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns whether {@link MarkableAPI Markable} potentialSourceMarkable is the source markable in this MarkablePointer.
	 * 
	 * @param potentialSourceMarkable The {@link MarkableAPI Markable} to be tested as source markable.
	 * @return True if potentialSourceMarkable is the source markable in this MarkablePointer, else false.
	 */
	public boolean isSourceMarkable(Markable potentialSourceMarkable);

	/** <b><font size=+1 color=green>(API)</font></b> Returns whether {@link MarkableAPI Markable} potentialTargetMarkable is on of the target markables in this MarkablePointer.
	 * 
	 * @param potentialTargetMarkable The {@link MarkableAPI Markable} to be tested as target markable.
	 * @return True if potentialTargetMarkable is the source markable in this MarkablePointer, else false.
	 */	
	public boolean isTargetMarkable(Markable potentialTargetMarkable);
	
	/**  <b><font size=+1 color=green>(API)</font></b> Returns whether {@link MarkableAPI Markable} markable is the source or target markable in this MarkablePointer.
	 * 
	 * @param markable The {@link MarkableAPI Markable} to be tested as source or target markable.
	 * @return True if markable is the source or target markable in this MarkablePointer, else false.
	 */
	public boolean containsMarkable(Markable markable);
}
