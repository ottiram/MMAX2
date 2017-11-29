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
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTuple;

/** This interface defines public methods for accessing query results.
 */
public interface QueryResultListAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns the MMAX2QueryResultTuple at index listIndex.
	 * 
	 * @param listIndex The (zero-based) position in this list of the result tuple to return.
	 * @return The {@link QueryResultTupleAPI MMAX2QueryResultTuple} object at index listIndex.
	 */
	public MMAX2QueryResultTuple getTupleAtIndex(int listIndex);
	
	/** <b><font size=+1 color=green>(API)</font></b> Returns the size of the query result as the number of MMAX2QueryResultTuple objects. 
	 * 
	 * @return The number of {@link QueryResultTupleAPI MMAX2QueryResultTuple} objects in this list.
	 */
	public int getResultSize();
}
