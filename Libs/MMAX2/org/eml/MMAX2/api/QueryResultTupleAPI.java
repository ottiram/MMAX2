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

import org.eml.MMAX2.annotation.query.MMAX2QueryResultTupleElement;

/** This interface defines public methods for accessing MMAX2QueryResultTuples.
 */

public interface QueryResultTupleAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the width of the result tuple as the number of {@link QueryResultTupleElementAPI MMAX2QueryResultTupleElement} objects it contains.
	 * 
	 * @return The number of {@link QueryResultTupleElementAPI MMAX2QueryResultTupleElement} objects this tuple contains. 
	 */
	public int getWidth();
	
	/** <b><font size=+1 color=green>(API)</font></b>  Returns the {@link QueryResultTupleElementAPI MMAX2QueryResultTupleElement} object at index elementIndex.
	 * 
	 * @param elementIndex
	 * @return The {@link QueryResultTupleElementAPI MMAX2QueryResultTupleElement} object at index elementIndex.
	 */
	public MMAX2QueryResultTupleElement getElementAt(int elementIndex);
	
}
