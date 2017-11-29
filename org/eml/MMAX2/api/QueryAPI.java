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
import org.eml.MMAX2.annotation.query.MMAX2QueryResultList;

/** This interface defines public methods for executing queries.
 */
public interface QueryAPI 
{
	/** <b><font size=+1 color=green>(API)</font></b> Returns a MMAX2QueryResultList object containing the result of the execution of the query queryString.
	 * 
	 * @param queryString The string representing the query. Query commmands are supposed to be separated by semicolon. Normal and simplified queries can be mixed!
     * @param interactive If true, a message box will be displayed if the query accesses a {@link MarkableLevelAPI MarkableLevel} which could not be found. 
	 * @return The {@link QueryResultListAPI MMAX2QueryResultList} object containing the result of the execution of the query queryString.
	 */
	public MMAX2QueryResultList executeQuery(String queryString, boolean interactive);
	
}
