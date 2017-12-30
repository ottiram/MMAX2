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

package org.eml.MMAX2.annotation.query;

public class MMAX2TermConnector 
{
    int type = -1;
    int endsAt = 0;
    static int AND = 1;
    static int OR = 2;
    
    /** Creates new MMAX2TermConnector */
    public MMAX2TermConnector(int _type, int _endsAt) 
    {
        type = _type;
        endsAt = _endsAt;
    }

    public final int getEndsAt()
    {
        return endsAt;
    }
    
    public final int getType()
    {
        return type;
    }
}
