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

package org.eml.MMAX2.annotation.markables;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eml.MMAX2.annotation.query.MMAX2MatchingCriterion;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTuple;
import org.eml.MMAX2.annotation.query.MMAX2QueryResultTupleElement;
import org.eml.MMAX2.api.AttributeAPI;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import org.eml.MMAX2.utils.MMAX2Constants;
import org.w3c.dom.Node;

public class MarkableHelper 
{
    public static final int LONGER_BEFORE_SHORTER = 1;
    public static final int SHORTER_BEFORE_LONGER = 2;
    public static final String legacyDefaultValue = "_MMAX default value_";    
    
    /** Creates a new instance of MarkableHelper */
    public MarkableHelper() 
    {
        
    }
    
    public static void sort(ArrayList markables, Comparator comp)
    {
        if (comp != null)
        {
            Markable[] tempArray =  (Markable[])markables.toArray(new Markable[0]);;
            java.util.Arrays.sort(tempArray,comp);
            ArrayList newTemp =  new ArrayList(java.util.Arrays.asList(tempArray));
            markables.clear();
            markables.addAll(newTemp);            
        }
    }
    
    public static String getFormattedAttributeString(Markable markable)
    {        
        String result="";
        int maxLen=0;
        int currentLen=0;
        Set allattribs = markable.attributes.keySet();
        String[] keys = (String[])allattribs.toArray(new String[0]);
        for (int i=0;i<keys.length;i++)
        {
            currentLen = ((String)keys[i]).length();
            if (currentLen > maxLen)
            {
                maxLen = currentLen;
            }
        }
        
        // Sort keys alphabetically
        java.util.Arrays.sort(keys);
        
        String key = "";
        String value = "";
        for (int i=0;i<keys.length;i++)
        {
            key=(String) keys[i];
            value=markable.getAttributeValue(key);
            key=key+"                                                                ";
            key = key.substring(0,maxLen+1)+"= ";
            result=result+key+value+"\n";
        }    
        return result;
    }
    
    /** This class receives an ArrayList of MMAX2DiscourseElement objects representing one markable, and returns
        an array of arrays of Strings to be used as a markables fragment field. */
    public static String[][] toFragments(ArrayList DEs)
    {
        ArrayList currentFragment = new ArrayList();
        ArrayList allFragments = new ArrayList();
        MMAX2DiscourseElement currentDE = null;
        int lastPos = -1;
        int currentPos = -1;
        for (int z=0;z<DEs.size();z++)
        {
            // Get current DE
            currentDE = (MMAX2DiscourseElement) DEs.get(z);
            // Get disc pos of current element            
            currentPos = currentDE.getDiscoursePosition();
            if (currentFragment.size()==0)
            {
                currentFragment.add(currentDE.getID());
                // Store disc pos
                lastPos = currentPos;
                continue;
            }
            if (currentPos == lastPos +1)
            {
                currentFragment.add(currentDE.getID());
                // Store disc pos
                lastPos = currentPos;
                continue;                
            }
            else
            {
                // The currentDE starts a new fragment
                // Store currentFragment as array already
                allFragments.add(currentFragment.toArray(new String[0]));
                currentFragment = null;
                currentFragment = new ArrayList();
                currentFragment.add(currentDE.getID());
                lastPos = currentPos;
                continue;                
            }
        }
        allFragments.add(currentFragment.toArray(new String[0]));
        return (String[][]) allFragments.toArray(new String[0][0]);
    }

    /** This class receives an ArrayList of MMAX2DiscourseElement IDs representing one markable, and returns
        an array of arrays of Strings to be used as a markables fragment field. */
    public static String[][] toFragments(ArrayList IDs, MMAX2Discourse discourse)
    {
        ArrayList currentFragment = new ArrayList();
        ArrayList allFragments = new ArrayList();
        MMAX2DiscourseElement currentDE = null;
        String currentID = "";
        int lastPos = -1;
        int currentPos = -1;
        for (int z=0;z<IDs.size();z++)
        {
            // Get current DE
            currentID = (String) IDs.get(z);
            currentDE = discourse.getDiscourseElementByID(currentID);
            // Get disc pos of current element            
            currentPos = currentDE.getDiscoursePosition();
            if (currentFragment.size()==0)
            {
                currentFragment.add(currentDE.getID());
                // Store disc pos
                lastPos = currentPos;
                continue;
            }
            if (currentPos == lastPos +1)
            {
                currentFragment.add(currentDE.getID());
                // Store disc pos
                lastPos = currentPos;
                continue;                
            }
            else
            {
                // The currentDE starts a new fragment
                // Store currentFragment as array already
                allFragments.add(currentFragment.toArray(new String[0]));
                currentFragment = null;
                currentFragment = new ArrayList();
                currentFragment.add(currentDE.getID());
                lastPos = currentPos;
                continue;                
            }
        }
        allFragments.add(currentFragment.toArray(new String[0]));
        return (String[][]) allFragments.toArray(new String[0][0]);
    }

    
    public static void removeDuplicateDiscoursePositions(ArrayList DEs)
    {
        int currentDiscPos = -1;
        int lastDiscPos = -1;
        for (int z=0;z<DEs.size();z++)
        {
            currentDiscPos = ((MMAX2DiscourseElement)DEs.get(z)).getDiscoursePosition();
            if (lastDiscPos == -1)
            {
                lastDiscPos=currentDiscPos;
                continue;
            }
            if (currentDiscPos==lastDiscPos)
            {
                DEs.remove(z);
                z--;
                continue;
            }
            lastDiscPos=currentDiscPos;
        }
    }
    
    /** This returns true if t1 appears before t2, false otherwise. 
    t1 is 'before' t2 if t1 ends before t2 begins. For discontinuous markables, 
    the end of the last fragment of t1 and the beginning of the first fragment 
    of t2 is considered. */    
    public static boolean beforeStrict(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {        
        //int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        //int t2End = 0;

        if (list1ColSpec != -1)
        {
            t1End = t1.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t1End = t1.getRightmostDiscoursePosition();
        }
        
        if (list2ColSpec != -1)
        {
            t2Start = t2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
        }
        return beforeStrict(t1End,t2Start);
    }

    public static boolean beforeStrict(Markable m1, Markable m2)
    {       
        return beforeStrict(m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition());
    }
    
    public static boolean beforeStrict(int e1End, int e2Start)
    {        
        if (e1End < e2Start-1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }    
    
    /** This returns true if t1 appears before t2, false otherwise. 
    t1 is 'before' t2 if t1 ends before t2 begins. For discontinuous markables, 
    the end of the last fragment of t1 and the beginning of the first fragment 
    of t2 is considered. */    
    public static boolean before(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {        
        int t1End = 0;
        int t2Start = 0;

        if (list1ColSpec != -1)
        {
            t1End = t1.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t1End = t1.getRightmostDiscoursePosition();
        }
        
        if (list2ColSpec != -1)
        {
            t2Start = t2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
        }
        return before(t1End,t2Start);
    }
    
    public static boolean before(Markable m1, Markable m2)
    {       
        return before(m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition());
    }
    
    public static boolean before(int e1End, int e2Start)
    {
        if (e1End < e2Start)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
        
    /** This returns true if t1 ends directly before t2 begins (no overlap), false otherwise. 
        For discontinuous markables, the end of the last fragment of t1 and the beginning of 
        the first fragment of t2 is considered. */
    public static boolean meets(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1End = 0;
        int t2Start = 0;

        if (list1ColSpec != -1)
        {
            t1End = t1.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t1End = t1.getRightmostDiscoursePosition();
        }
        if (list2ColSpec != -1)
        {
            t2Start = t2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
        }

        return meets(t1End, t2Start);
    }
    
    public static boolean meets(Markable m1, Markable m2)
    {
        return meets(m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition());
    }

    
    public static boolean meets(int e1End, int e2Start)
    {
        if (e1End == e2Start-1)
        {
            return true;
        }               
        else
        {
            return false;
        }
    }
    
    public static boolean starts(Markable m1, Markable m2)
    {
        return starts(m1.getLeftmostDiscoursePosition(),m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());
    }    
        
    public static boolean starts (int e1Start, int e1End, int e2Start, int e2End)
    {
        if (e1Start == e2Start && e1End <= e2End)
        {
            return true;
        }
        else
        {        
            return false;
        }
    }
    
        
    public static boolean startsStrict(Markable m1, Markable m2)
    {
        return startsStrict(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(), m2.getRightmostDiscoursePosition());
    }
    
    public static boolean startsStrict(int e1Start, int e1End, int e2Start, int e2End)
    {
        if (e1Start == e2Start && e1End < e2End)
        {
            return true;        
        }               
        else
        {
            return false;
        }
    }
               
    
    /** This returns true if t1 starts before t2 starts and t1 ends after t2 starts but 
        before t2 ends, false otherwise. For discontinuous markables, the beginning 
        of the first fragment of t1 and t2 and the end of the last fragment of t1 
        and t2 is considered. */ 
    public static boolean overlaps_right(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)    
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;

        if (list1ColSpec != -1)
        {
            t1Start = t1.getValueAt(list1ColSpec).getLeftmostDiscoursePosition();
            t1End = t1.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t1Start = t1.getLeftmostDiscoursePosition();
            t1End = t1.getRightmostDiscoursePosition();            
        }
        
        if (list2ColSpec != -1)
        {
            t2Start = t2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition();
            t2End = t2.getValueAt(list2ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
            t2End = t2.getRightmostDiscoursePosition();            
        }
                
        return overlaps_right(t1Start,t1End,t2Start,t2End);
    }

    public static boolean overlaps_left(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)    
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;

        if (list1ColSpec != -1)
        {
            t1Start = t1.getValueAt(list1ColSpec).getLeftmostDiscoursePosition();
            t1End = t1.getValueAt(list1ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t1Start = t1.getLeftmostDiscoursePosition();
            t1End = t1.getRightmostDiscoursePosition();            
        }
        
        if (list2ColSpec != -1)
        {
            t2Start = t2.getValueAt(list2ColSpec).getLeftmostDiscoursePosition();
            t2End = t2.getValueAt(list2ColSpec).getRightmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
            t2End = t2.getRightmostDiscoursePosition();            
        }
                
        return overlaps_left(t1Start,t1End,t2Start,t2End);
    }
    
    public static boolean overlaps_right(Markable m1, Markable m2)    
    {
        return overlaps_right(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());
    }    

    public static boolean overlaps_left(Markable m1, Markable m2)    
    {
        return overlaps_left(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());
    }    
    
    public static boolean overlaps_right(int e1Start, int e1End, int e2Start, int e2End)
    {
        // This is overlaps_right
        
        if (e1Start < e2Start && e1End < e2End && e1End >= e2Start)
        {
            return true;
        }                
        else
        {
            return false;
        }
    }

        public static boolean overlaps_left(int e1Start, int e1End, int e2Start, int e2End)
    {
        // This is overlaps_left
        if (e1Start > e2Start && e1Start <= e2End && e1End > e2End)
        {
            return true;
        }                
        else
        {
            return false;
        }
    }

       
    public static boolean finishes(Markable m1, Markable m2)
    {
        return finishes(m1.getRightmostDiscoursePosition(), m2.getRightmostDiscoursePosition());
    }
    
    public static boolean finishes (int t1End, int t2End)
    {
        if (t1End == t2End)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    

    
    public static boolean finishesStrict(Markable m1, Markable m2)
    {
        return finishesStrict(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());
    }
    
    public static boolean finishesStrict(int t1Start, int t1End, int t2Start, int t2End)
    {
        if (t1End == t2End && t1Start > t2Start)
        {
            return true;
        }        
        else
        {
            return false;
        }        
    }
    

    
    /** This returns true if t1 starts at the same time as or after t2 starts, and t1 
        ends at the same time as or before t2 ends, false otherwise. For discontinuous markables,
        each fragment of t1 must be completely within (i.e. during) some fragment of t2.*/    
    public static boolean during(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            // Check whether t1 has a col spec
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // No col spec for t1
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }
                        
            // Check whether t2 has a col spec
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // No colspec for t2
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            
            // Now, all starts and ends have been determined
            // Use 'standard' during since we know that no tuple is discontinuous
            return during(t1Start,t1End,t2Start,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                // t1 has a col spec
                // Get required values from designated element
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            else
            {
                // If t1 does not have a col spec, we cannot set its start and end since it may be discont
            }
            if (list2ColSpec != -1)
            {
                // t2 has a col spec
                // Get required values from designated element
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // If t2 does not have a col spec, we cannot set its start and end since it may be discont
            }
            
            // Here, eX is null unless tX had a col spec
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && // though t1 was discont, e1 at col spec is not
                e2 != null && 
                e2.isDiscontinuous()==false) // though t2 was discont, e2 at col spec is not
                {
                    // The two selected columns are non-discont
                    // So use normal matching (vars are set by now)
                    // This is the case if one or both tuples are discont, but one or both has a col spec
                    // to single out an element which itself is *not* discont
                    return during(t1Start,t1End,t2Start,t2End);
                }
            else
            {                                
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // At least one is discont
                    // Both tuples had a col spec, so we just have to compare the two elements' fragments
                    return duringFragments(e1.getFragments(),e2.getFragments());
                }
                else if (e1 != null)
                {
                    // At least one is discont (WRONG !! e1 may well be non-discont, as well as t2)
                    // So this is not tooooo efficient
                    // t1 had a col spec, t2 not
                    return duringFragments(e1.getFragments(),t2.getFragments());
                }
                else if (e2 != null)
                {
                    // At least one is discont (cf. above)
                    // t2 had a com spec, t1 not
                    return duringFragments(t1.getFragments(),e2.getFragments());
                }
                else
                {
                    // At least one is discont
                    // None had a col spec, so compare both tuples in their entirety
                    return duringFragments(t1.getFragments(),t2.getFragments());
                }
            }  
        }
    }
    

    /** This returns true if t1 starts at the same time as t2 starts, and t1 
        ends at the same time as or before t2 ends, false otherwise. For discontinuous markables,
        each fragment of t1 must be completely within (i.e. during) some fragment of t2, and t1 and
        t2 must start at the same position. */    
    public static boolean starts(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            // Check whether t1 has a col spec
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // No col spec for t1
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }
                        
            // Check whether t2 has a col spec
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // No colspec for t2
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            // Now, all starts and ends have been determined
            // Use 'standard' starts since we know that no tuple is discontinuous
            return starts(t1Start,t1End,t2Start,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                // t1 has a col spec
                // Get required values from designated element
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            else
            {
                // If t1 does not have a col spec, we cannot set its start and end since it may be discont
            }
            if (list2ColSpec != -1)
            {
                // t2 has a col spec
                // Get required values from designated element
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // If t2 does not have a col spec, we cannot set its start and end since it may be discont
            }
            
            // Here, eX is null unless tX had a col spec
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && // though t1 was discont, e1 at col spec is not
                e2 != null && 
                e2.isDiscontinuous()==false) // though t2 was discont, e2 at col spec is not
                {
                    // The two selected columns are non-discont
                    // So use normal matching (vars are set by now)
                    // This is the case if one or both tuples are discont, but one or both has a col spec
                    // to single out an element which itself is *not* discont
                    return starts(t1Start,t1End,t2Start,t2End);
                }
            else
            {                                
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // At least one is discont
                    // Both tuples had a col spec, so we just have to compare the two elements' fragments
                    //System.err.println(3);
                    //System.err.println(e1.getString()+" "+e2.getString());
                    if (e1.getLeftmostDiscoursePosition() == e2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e1 != null)
                {
                    // At least one is discont (WRONG !! e1 may well be non-discont, as well as t2)
                    // So this is not tooooo efficient
                    // t1 had a col spec, t2 not
                    if (e1.getLeftmostDiscoursePosition() == t2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e2 != null)
                {
                    // At least one is discont (cf. above)
                    // t2 had a com spec, t1 not
                    //System.err.println(5);
                    //System.err.println(t1.toString(null)+" "+e2.getString());
                    if (e2.getLeftmostDiscoursePosition() == t1.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    // At least one is discont
                    // None had a col spec, so compare both tuples in their entirety
                    if (t1.getLeftmostDiscoursePosition() == t2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
            }  
        }
    }

    /** This returns true if t1 ends at the same time as t2 ends, and starts 
        at the same time as or after t2 starts, false otherwise. For discontinuous markables,
        each fragment of t1 must be completely within (i.e. during) some fragment of t2, and t1 and
        t2 must end at the same position. */    
    public static boolean finishes(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            // Check whether t1 has a col spec
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // No col spec for t1
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }
                        
            // Check whether t2 has a col spec
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // No colspec for t2
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            // Now, all starts and ends have been determined
            // Use 'standard' finishes since we know that no tuple is discontinuous
            return finishes(t1End,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                // t1 has a col spec
                // Get required values from designated element
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            else
            {
                // If t1 does not have a col spec, we cannot set its start and end since it may be discont
            }
            if (list2ColSpec != -1)
            {
                // t2 has a col spec
                // Get required values from designated element
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // If t2 does not have a col spec, we cannot set its start and end since it may be discont
            }
            
            // Here, eX is null unless tX had a col spec
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && // though t1 was discont, e1 at col spec is not
                e2 != null && 
                e2.isDiscontinuous()==false) // though t2 was discont, e2 at col spec is not
                {
                    // The two selected columns are non-discont
                    // So use normal matching (vars are set by now)
                    // This is the case if one or both tuples are discont, but one or both has a col spec
                    // to single out an element which itself is *not* discont
                    return finishes(t1End,t2End);
                }
            else
            {                                
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // At least one is discont
                    // Both tuples had a col spec, so we just have to compare the two elements' fragments
                    if (e1.getRightmostDiscoursePosition() == e2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e1 != null)
                {
                    // At least one is discont (WRONG !! e1 may well be non-discont, as well as t2)
                    // So this is not tooooo efficient
                    // t1 had a col spec, t2 not
                    if (e1.getRightmostDiscoursePosition() == t2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e2 != null)
                {
                    // At least one is discont (cf. above)
                    // t2 had a com spec, t1 not
                    if (e2.getRightmostDiscoursePosition() == t1.getRightmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    // At least one is discont
                    // None had a col spec, so compare both tuples in their entirety
                    if (t1.getRightmostDiscoursePosition() == t2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
            }  
        }
    }
    
    
    /** This returns true if t1 starts at the same time as t2 starts, and t1 
        ends before t2 ends, false otherwise. For discontinuous markables,
        each fragment of t1 must be completely within (i.e. during) some fragment of t2, and t1 and
        t2 must start at the same position, and t1 must end before t2. */    
    public static boolean startsStrict(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            // Check whether t1 has a col spec
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // No col spec for t1
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }
                        
            // Check whether t2 has a col spec
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // No colspec for t2
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            // Now, all starts and ends have been determined
            // Use 'standard' starts since we know that no tuple is discontinuous
            return startsStrict(t1Start,t1End,t2Start,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                // t1 has a col spec
                // Get required values from designated element
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            else
            {
                // If t1 does not have a col spec, we cannot set its start and end since it may be discont
            }
            if (list2ColSpec != -1)
            {
                // t2 has a col spec
                // Get required values from designated element
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // If t2 does not have a col spec, we cannot set its start and end since it may be discont
            }
            
            // Here, eX is null unless tX had a col spec
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && // though t1 was discont, e1 at col spec is not
                e2 != null && 
                e2.isDiscontinuous()==false) // though t2 was discont, e2 at col spec is not
                {
                    // The two selected columns are non-discont
                    // So use normal matching (vars are set by now)
                    // This is the case if one or both tuples are discont, but one or both has a col spec
                    // to single out an element which itself is *not* discont
                    return startsStrict(t1Start,t1End,t2Start,t2End);
                }
            else
            {                                
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // At least one is discont
                    // Both tuples had a col spec, so we just have to compare the two elements' fragments
                    if (e1.getLeftmostDiscoursePosition() == e2.getLeftmostDiscoursePosition() &&
                        e1.getRightmostDiscoursePosition() < e2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e1 != null)
                {
                    // At least one is discont (WRONG !! e1 may well be non-discont, as well as t2)
                    // So this is not tooooo efficient
                    // t1 had a col spec, t2 not
                    if (e1.getLeftmostDiscoursePosition() == t2.getLeftmostDiscoursePosition() &&
                        e1.getRightmostDiscoursePosition() < t2.getRightmostDiscoursePosition())

                    {
                        return duringFragments(e1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e2 != null)
                {
                    // At least one is discont (cf. above)
                    // t2 had a com spec, t1 not
                    if (t1.getLeftmostDiscoursePosition() == e2.getLeftmostDiscoursePosition() &&
                        t1.getRightmostDiscoursePosition() < e2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    // At least one is discont
                    // None had a col spec, so compare both tuples in their entirety
                    if (t1.getLeftmostDiscoursePosition() == t2.getLeftmostDiscoursePosition() &&
                        t1.getRightmostDiscoursePosition() < t2.getRightmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
            }  
        }
    }
    
    /** This returns true if t1 ends at the same time as t2 ends, and t1 
        starts after t2 starts, false otherwise. For discontinuous markables,
        each fragment of t1 must be completely within (i.e. during) some fragment of t2, and t1 and
        t2 must end at the same position, and t1 must start after t2 starts. */    
    public static boolean finishesStrict(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            // Check whether t1 has a col spec
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // No col spec for t1
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }
                        
            // Check whether t2 has a col spec
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // No colspec for t2
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            // Now, all starts and ends have been determined
            // Use 'standard' starts since we know that no tuple is discontinuous
            return finishesStrict(t1Start,t1End,t2Start,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                // t1 has a col spec
                // Get required values from designated element
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            else
            {
                // If t1 does not have a col spec, we cannot set its start and end since it may be discont
            }
            if (list2ColSpec != -1)
            {
                // t2 has a col spec
                // Get required values from designated element
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                // If t2 does not have a col spec, we cannot set its start and end since it may be discont
            }
            
            // Here, eX is null unless tX had a col spec
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && // though t1 was discont, e1 at col spec is not
                e2 != null && 
                e2.isDiscontinuous()==false) // though t2 was discont, e2 at col spec is not
                {
                    // The two selected columns are non-discont
                    // So use normal matching (vars are set by now)
                    // This is the case if one or both tuples are discont, but one or both has a col spec
                    // to single out an element which itself is *not* discont
                    return finishesStrict(t1Start,t1End,t2Start,t2End);
                }
            else
            {                                
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // At least one is discont
                    // Both tuples had a col spec, so we just have to compare the two elements' fragments
                    if (e1.getRightmostDiscoursePosition() == e2.getRightmostDiscoursePosition() &&
                        e1.getLeftmostDiscoursePosition() > e2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e1 != null)
                {
                    // At least one is discont (WRONG !! e1 may well be non-discont, as well as t2)
                    // So this is not tooooo efficient
                    // t1 had a col spec, t2 not
                    if (e1.getRightmostDiscoursePosition() == t2.getRightmostDiscoursePosition() &&
                        e1.getLeftmostDiscoursePosition() > t2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(e1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else if (e2 != null)
                {
                    // At least one is discont (cf. above)
                    // t2 had a com spec, t1 not
                    if (t1.getRightmostDiscoursePosition() == e2.getRightmostDiscoursePosition() &&
                        t1.getLeftmostDiscoursePosition() > e2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),e2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    // At least one is discont
                    // None had a col spec, so compare both tuples in their entirety
                    if (t1.getRightmostDiscoursePosition() == t2.getRightmostDiscoursePosition() &&
                        t1.getLeftmostDiscoursePosition() > t2.getLeftmostDiscoursePosition())
                    {
                        return duringFragments(t1.getFragments(),t2.getFragments());
                    }
                    else
                    {
                        return false;
                    }
                }
            }  
        }
    }
    
    
    public static boolean during(Markable m1, Markable m2)    
    {
        return during(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());        
    }    
    
    public static boolean during(int t1Start, int t1End, int t2Start, int t2End)
    {
        if (t1Start >=t2Start && t1End <=t2End)
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }

    public static boolean duringFragments(String[][] m1Frags, String[][] m2Frags)
    {
        // This returns true if for every fragment in m1 there is one in m2 that contains it.
        // Iterate over all fragments in m1
        int fragsFound =0;
        for (int z=0;z<m1Frags.length;z++)
        {
            // Get current fragment from m1
            String[] currentM1Frag = m1Frags[z];                       
            // By default, the element has not been found
            boolean currentM1FragFound=false;
            
            // Looking for frag in m2 that contains currentM1Frag ... 
            // Iterate over all fragments in m2
            for (int y=0;y<m2Frags.length;y++)
            {
                // Get current fragment from m2
                String[] currentM2Frag = m2Frags[y];                
                                    
                // Now, all elements in currentM1Frag must be in currentM2Frag
                // Since we know each fragment to be continuous, we must simply find m1's first and last
                // element in m2
                // Get first de_id in M1Frag
                String m1Initial = currentM1Frag[0];
                // Get last de_id in M1Frag
                String m1Final = currentM1Frag[currentM1Frag.length-1];
                
                // By default, none of the two has been found
                boolean initialFound = false;
                boolean finalFound = false;               
                
                // Iterate over frag 2, which should contain m1Initial and m1Final
                for (int b=0;b<currentM2Frag.length;b++)
                {
                    // Get currnt element in curren M2Frag
                    String currentM2Element = currentM2Frag[b];
                    // Check whether it is the required initial one
                    if (currentM2Element.equals(m1Initial))
                    {
                        // Yes. Store that initial has been found
                        initialFound=true;
                        // If the element to find is only one element, do not wait for final, 
                        // which will never be found
                        if (currentM1Frag.length ==1)
                        {
                            // Initial and final are identical, so final is found as well
                            finalFound = true;
                        }
                    }
                    // Check whether current element is the required final one
                    else if (currentM2Element.equals(m1Final))
                    {
                        // Yes. Store that final has been found
                        finalFound=true;
                    }
                    if (initialFound && finalFound)
                    {
                        // m1 was already found completely,
                        // so stop searching the current frag of m2
                        currentM1FragFound = true;
                        // Stop iterating over rest of M2Frag
                        break;
                    }
                }
                // Here, we searched through all of m2 (or broke if m1Frag was found earlier)
                if (currentM1FragFound)
                {
                    // The currentM1 frag was found
                    // Increas counter of found frags of M1
                    fragsFound++;
                    // Reset signals to default
                    currentM1FragFound = false;
                    initialFound = false;
                    finalFound = false;
                    // Stop searching in m2 at all, since the current frag has been found
                    // Note: This means that m2 is searched entirely in every iteration,
                    // which is correct, since m1 may be one frag only!! DO NOT ALTER
                    break;
                }
                else
                {
                    // The current m2 does not contain m1
                    // Keep searching in next frag of m2
                }
            }
        }
        // TODO: If one fragment of m1 was found nowhere in entire m2, break to false.
        // Here, we looked for every frag of m1 in m2
                        
        if (fragsFound==m1Frags.length)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    
      
    /** This returns true if t1 starts at the same time as or after t2 starts, and t1 
        ends at the same time as or before t2 ends, and if t1 and t2 are not identical,false 
        otherwise. For discontinuous markables, each fragment of t1 must be completely within 
        (i.e. during) some fragment of t2.*/    
    public static boolean duringStrict(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        boolean during = during(t1, t2, list1ColSpec, list2ColSpec);        
        boolean equal = equals(t1, t2, list1ColSpec, list2ColSpec);
        if (during && ! equal)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    
    public static boolean duringStrict(Markable m1, Markable m2)    
    {
        boolean during = during(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition()); 
        boolean equal = equals(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition()); 
        if (during && ! equal)
        {
            return true;
        }
        else
        {
            return false;
        }
    }    
    
    
    /** This returns true if t1 and t2 span the same sequence of discourse elements, 
        false otherwise. Discontinuous tuples are compared fragment for fragment. */
    public static boolean equals(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
        
        String[][] e1Frags = null;
        String[][] e2Frags = null;        
        
        // Check whether any of the two tuples is marked as discontinuous.
        // If so, comparison must be done fragment-wise, on either the entire tuple 
        // or the specified column(s)
        if (t1.isDiscontinuous()==false && t2.isDiscontinuous()==false)
        {        
            // None of the two tuples is discontinuous
            // This should be the preferred case !!
            if (list1ColSpec != -1)
            {
                // Get element at colspec
                e1 = t1.getValueAt(list1ColSpec);
                // Get its start and end
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();
            }
            else
            {
                // Get tuples start and end
                t1Start = t1.getLeftmostDiscoursePosition();
                t1End = t1.getRightmostDiscoursePosition();
            }

            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            else
            {
                t2Start = t2.getLeftmostDiscoursePosition();
                t2End = t2.getRightmostDiscoursePosition();
            }            
            return equals(t1Start,t1End,t2Start,t2End);
        }
        else
        {
            // At least one of the tuples is marked as discontinuous
            // Maybe one or both of them has a col spec
            if (list1ColSpec != -1)
            {
                e1 = t1.getValueAt(list1ColSpec);
                t1Start = e1.getLeftmostDiscoursePosition();
                t1End = e1.getRightmostDiscoursePosition();                
            }
            if (list2ColSpec != -1)
            {
                e2 = t2.getValueAt(list2ColSpec);
                t2Start = e2.getLeftmostDiscoursePosition();
                t2End = e2.getRightmostDiscoursePosition();
            }
            
            if (e1 != null && 
                e1.isDiscontinuous()==false && 
                e2 != null && 
                e2.isDiscontinuous()==false)
            {
                // The two selected columns are non-discont
                // So use normal matching (vars are set by now)
                return equals(t1Start,t1End,t2Start,t2End);
            }
            else
            {
                // At least one comparable element is still discont, so compare frag-wise                
                if (e1 != null && e2 != null)
                {
                    // Both had a col spec, so we just have to compare the two elements' fragments
                    return equalFragments(e1.getFragments(),e2.getFragments());
                }
                else if (e1 != null)
                {
                    // t1 had a col spec, t2 not
                    //return equalLists(e1.getDiscourseElementIDs(),t2.getDiscourseElementIDs());
                    return equalFragments(e1.getFragments(),t2.getFragments());
                }
                else if (e2 != null)
                {
                    // t2 had a col spec, t1 not
                    //return equalLists(t1.getDiscourseElementIDs(),e2.getDiscourseElementIDs());
                    return equalFragments(t1.getFragments(),e2.getFragments());
                }
                else
                {
                    // None had a col spec, so compare both tuples in their entirety                                
                    //return equalLists(t1.getDiscourseElementIDs(),t2.getDiscourseElementIDs());
                    return equalFragments(t1.getFragments(),t2.getFragments());
                }
            }  
        }
    }

    
    /** This returns true if t1 and t2 start and end at the same positions regardless of discontinuity
        false otherwise. */
    public static boolean spans(MMAX2QueryResultTuple t1, MMAX2QueryResultTuple t2, int list1ColSpec, int list2ColSpec)
    {
        int t1Start = 0;
        int t1End = 0;
        int t2Start = 0;
        int t2End = 0;
        
        MMAX2QueryResultTupleElement e1 = null;
        MMAX2QueryResultTupleElement e2 = null;
                
        if (list1ColSpec != -1)
        {
            // Get element at colspec
            e1 = t1.getValueAt(list1ColSpec);
            // Get its start and end
            t1Start = e1.getLeftmostDiscoursePosition();
            t1End = e1.getRightmostDiscoursePosition();
        }
        else
        {
            // Get tuples start and end
            t1Start = t1.getLeftmostDiscoursePosition();
            t1End = t1.getRightmostDiscoursePosition();
        }

        if (list2ColSpec != -1)
        {
            e2 = t2.getValueAt(list2ColSpec);
            t2Start = e2.getLeftmostDiscoursePosition();
            t2End = e2.getRightmostDiscoursePosition();
        }
        else
        {
            t2Start = t2.getLeftmostDiscoursePosition();
            t2End = t2.getRightmostDiscoursePosition();
        }            
        return equals(t1Start,t1End,t2Start,t2End);        
    }
                
    
    public static boolean equalFragments(String[][] m1Frags, String[][] m2Frags)
    {
        return getSpan(m1Frags).equalsIgnoreCase(getSpan(m2Frags));        
    }

    /*
    public static boolean equalFragmentsBak(String[][] m1Frags, String[][] m2Frags)
    {
        boolean result = true;
        if (m1Frags.length == m2Frags.length)
        {
            // Only if both have the same number of fragments
            // Iterate over all fragments
            for (int z=0;z<m1Frags.length;z++)
            {
                String[] currentM1Frag = m1Frags[z];
                String[] currentM2Frag = m2Frags[z];
                if (currentM1Frag.length == currentM2Frag.length)
                {
                    // The current fragments have the same length
                    if (currentM1Frag[0].equalsIgnoreCase(currentM1Frag[0])==false)
                    {
                        // They do not have the same initial element
                        // So not equal, and break
                        result=false;
                        break;
                    }
                }
                else
                {
                    // The current fragments are not the same length
                    // So not equal and break
                    result = false;
                    break;
                }
            }
        }
        else
        {
            // Different number of fragments, so not equal and break
            result = false;
        }
        return result;
    }
    */
    
    public static boolean equals(Markable m1, Markable m2)    
    {
        return equals(m1.getLeftmostDiscoursePosition(), m1.getRightmostDiscoursePosition(), m2.getLeftmostDiscoursePosition(),m2.getRightmostDiscoursePosition());
    }       
    
    public static boolean equals (int t1Start, int t1End, int t2Start, int t2End)
    {
        if (t1Start == t2Start &&  t1End == t2End)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    
    /** This returns a string representation of the current markable as a 'Key Word In Context'.*/
    public final static String toKWICString(String wordID, MMAX2Discourse discourse)
    {        
        int maxWidth = 100;
        int maxMarkableWidth = 30;
        
        int discPos = discourse.getDiscoursePositionFromDiscourseElementID(wordID);
        
        MMAX2DiscourseElement elem = discourse.getDiscourseElementAtDiscoursePosition(discPos);
        String result = "  "+elem.toString()+" ";
        String leftPart = "";
        // Get reference to markable level once
        int initialDiscoursePosition = discPos; //markable.getLeftmostDiscoursePosition();// getMarkableLevel().getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(fragments[0][0]);
        int finalDiscoursePosition = discPos;//markable.getRightmostDiscoursePosition();// level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(fragments[0][fragments[0].length-1]);
        // Iterate backwards over DEs left to current Markable
        for (int z=initialDiscoursePosition-1;z>=0;z--)
        {
            String currentWordID = discourse.getDiscourseElementIDAtDiscoursePosition(z);
            String currentWord = discourse.getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            leftPart = currentWord+" "+leftPart;
            if (leftPart.length() >=(maxWidth-maxMarkableWidth)/2)
            {
                leftPart = leftPart.substring(leftPart.length()-((maxWidth-maxMarkableWidth)/2));
                break;
            }
        }
        
        if (leftPart.length() < (maxWidth-maxMarkableWidth)/2)
        {
            for (int p=leftPart.length();p<(maxWidth-maxMarkableWidth)/2;p++)
            {
                leftPart = " "+leftPart;
            }
        }
        result = leftPart + result;
        for (int z=finalDiscoursePosition+1;z<=Integer.MAX_VALUE;z++)
        {
            String currentWordID = discourse.getDiscourseElementIDAtDiscoursePosition(z);
            // Check whether an ID has been found at all, or if the Discourse is shorter 
            if (currentWordID.equals("")) break;
            String currentWord = discourse.getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            result = result + " "+currentWord;
            if (result.length() >=maxWidth)
            {
                result = result.substring(0,maxWidth);
                break;
            }
        }        
        return result;
    }    
    
    
    /** This returns a string representation of the current markable as a 'Key Word In Context'.*/
    public final static String toKWICString(Markable markable)
    {        
        int maxWidth = 100;
        int maxMarkableWidth = 30;
        String result = "  "+markable.toTrimmedString(maxMarkableWidth)+" ";
        String leftPart = "";
        // Get reference to markable level once
        MarkableLevel level = markable.getMarkableLevel();
        int initialDiscoursePosition = markable.getLeftmostDiscoursePosition();
        int finalDiscoursePosition = markable.getRightmostDiscoursePosition();
        // Iterate backwards over DEs left to current Markable
        for (int z=initialDiscoursePosition-1;z>=0;z--)
        {
            String currentWordID = level.getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(z);
            String currentWord = level.getCurrentDiscourse().getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            leftPart = currentWord+" "+leftPart;
            if (leftPart.length() >=(maxWidth-maxMarkableWidth)/2)
            {
                leftPart = leftPart.substring(leftPart.length()-((maxWidth-maxMarkableWidth)/2));
                break;
            }
        }
        
        if (leftPart.length() < (maxWidth-maxMarkableWidth)/2)
        {
            for (int p=leftPart.length();p<(maxWidth-maxMarkableWidth)/2;p++)
            {
                leftPart = " "+leftPart;
            }
        }
        result = leftPart + result;
        for (int z=finalDiscoursePosition+1;z<=Integer.MAX_VALUE;z++)
        {
            String currentWordID = level.getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(z);
            // Check whether an ID has been found at all, or if the Discourse is shorter 
            if (currentWordID.equals("")) break;
            String currentWord = level.getCurrentDiscourse().getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            result = result + " "+currentWord;
            if (result.length() >=maxWidth)
            {
                result = result.substring(0,maxWidth);
                break;
            }
        }        
        return result;
    }    

    
    /** This returns a string representation of the current markable as a 'Key Word In Context'.*/
    public final static String toKWICString(Markable markable, int maxWidth)
    {        
        //int maxWidth = 100;
        int maxMarkableWidth = 30;
        String result = "  "+markable.toTrimmedString(maxMarkableWidth)+" ";
        String leftPart = "";
        // Get reference to markable level once
        MarkableLevel level = markable.getMarkableLevel();
        int initialDiscoursePosition = markable.getLeftmostDiscoursePosition();
        int finalDiscoursePosition = markable.getRightmostDiscoursePosition();
        // Iterate backwards over DEs left to current Markable
        for (int z=initialDiscoursePosition-1;z>=0;z--)
        {
            String currentWordID = level.getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(z);
            String currentWord = level.getCurrentDiscourse().getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            leftPart = currentWord+" "+leftPart;
            if (leftPart.length() >=(maxWidth-maxMarkableWidth)/2)
            {
                leftPart = leftPart.substring(leftPart.length()-((maxWidth-maxMarkableWidth)/2));
                break;
            }
        }
        
        if (leftPart.length() < (maxWidth-maxMarkableWidth)/2)
        {
            for (int p=leftPart.length();p<(maxWidth-maxMarkableWidth)/2;p++)
            {
                leftPart = " "+leftPart;
            }
        }
        result = leftPart + result;
        for (int z=finalDiscoursePosition+1;z<=Integer.MAX_VALUE;z++)
        {
            String currentWordID = level.getCurrentDiscourse().getDiscourseElementIDAtDiscoursePosition(z);
            // Check whether an ID has been found at all, or if the Discourse is shorter 
            if (currentWordID.equals("")) break;
            String currentWord = level.getCurrentDiscourse().getDiscourseElementNode(currentWordID).getFirstChild().getNodeValue();
            result = result + " "+currentWord;
            if (result.length() >=maxWidth)
            {
                result = result.substring(0,maxWidth);
                break;
            }
        }        
        return result;
    }    
    
    
    
    /** This method  */
    public static final void register(Markable markable, boolean updateHash)
    {
        // Get reference to markable level once
        MarkableLevel level = markable.getMarkableLevel();
        // Reset string field
        markable.string="";
        // NEW: reset size field
        markable.size = 0;
        String[] currentFragment = null;
        int currentFragmentLength=0;                
        Node currentDENode = null;
        String currentDE = "";
        
        boolean currentIsInvalid = false;
        boolean currentHasBeenAdded = false;
        
        ArrayList currentFragmentsDEs = null;
        
        StringBuffer buffer = new StringBuffer();
        
        String[][] fragments = markable.getFragments();
        // Iterate over all fragments
        for (int z=0;z<markable.singleFragments;z++)
        {            
            // Get current fragment
            currentFragment = fragments[z];
            currentFragmentLength = currentFragment.length;
            
            // Create empty list to accept 
            currentFragmentsDEs = new ArrayList();
            
            // Sum over number of all fragments for size attribute            
            markable.size = markable.size + currentFragmentLength;            
            
            // Iterate over all DEs in current fragment (this contains IDs)
            for (int o=0;o<currentFragmentLength;o++)
            {
                // Get Node representation of current DE
                currentDENode = level.getCurrentDiscourse().getDiscourseElementNode(currentFragment[o]);                                                
                
                
                if (o==0)
                {
                    // The current DE is the first in a fragment                                                                                        
                        
                    {
                        // current is valid, so use currentDENode (i.e. on index o) as valid node
                        level.registerMarkableAtStartOfFragment(currentFragment[o],markable);
  
                        // Add DE to list of valid ones for this fragment
                        currentFragmentsDEs.add(currentDENode.getFirstChild().getNodeValue());
                        currentHasBeenAdded = true;                        
                    }
                }
                                                                
                if (o==currentFragmentLength-1)
                {
                    // The current DE is the last in its fragment
                                        
                    {
                        level.registerMarkableAtEndOfFragment(currentFragment[o],markable);
                        // The current DE is valid
                        if (currentHasBeenAdded == false)
                        {
                            // The current DE is not the first and last in its fragment, so add string 
                            // Add DE to list of valid ones for this fragment
                            try
                            {
                                currentFragmentsDEs.add(currentDENode.getFirstChild().getNodeValue());
                            }
                            catch (java.lang.NullPointerException ex)
                            {
                                currentFragmentsDEs.add("");
                            }
                            currentHasBeenAdded = true;
                        }
                    }
                }
                
                if (currentHasBeenAdded == false)
                {
                    // The DE with the node currentDENode is inside a markable, so add it to string, but only if it is not invalid
                    if (currentIsInvalid==false)
                    {
                        currentFragmentsDEs.add(currentDENode.getFirstChild().getNodeValue());
                    }
                }
                
                // Tell the MarkableLayer this markable belongs to that this DiscourseElement is part of this markable.
                // Empty DEs are NOT filtered out, since they are parts of the markable nonetheless
                level.registerMarkableAtDiscourseElement(currentFragment[o],markable);
                if (updateHash) level.updateDiscoursePositionToMarkableMapping(currentFragment[o]);
                currentHasBeenAdded = false;
            }            
            
            // Build string representation            
            buffer.append("[");
            
            int v = currentFragmentsDEs.size();
            for (int t=0;t<v;t++)
            {
                if (t==v-1)
                {
                    buffer.append((String )currentFragmentsDEs.get(t));
                }
                else
                {
                    buffer.append((String )currentFragmentsDEs.get(t)+" ");
                }
            }
            buffer.append("]");            
        }        
        markable.string = buffer.toString();
        // Init with one element per DiscourseElement
        markable.discourseElementStartPositions = new int[markable.size];                
    }
    
    /** Needed if Markables contain suppressed Discourse elements. */
    public final static int getFollowingValidDiscourseElementPosition(Markable markable,String[] currentFragment, int currentPos)
    {
        MarkableLevel level = markable.getMarkableLevel();
        int result = -1;
        Node currentNode = null;
        // When this is called, the DE at currentPos is already known to be invalid, so start at element one to the right
        int tempPos = currentPos + 1;
        while(tempPos <currentFragment.length)
        {
            /** Get Node representation of element at tempPos */
            currentNode = level.getCurrentDiscourse().getDiscourseElementNode(currentFragment[tempPos]);
            try
            {
                if (currentNode.getAttributes().getNamedItem("empty").getNodeValue().equals("true"))
                {
                    tempPos++;
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // currentNode does not have the 'empty' attribute, so we found a valid element
                result = tempPos;
                break;
            }        
        }        
        return result;
    }    
    
    /** This method returns the discourse position of the next valid (i.e. non-empty) DiscourseElement in currentFragment, moving
        from currentPos -1 to the left. (Needed if Markables contain suppressed Discourse elements.) */
    public final static int getPreceedingValidDiscourseElementPosition(Markable markable, String[] currentFragment, int currentPos)
    {
        MarkableLevel level = markable.getMarkableLevel();
        int result = -1;
        Node currentNode = null;
        // When this is called, the DE at currentPos is already known to be invalid, so start at element one to the left
        int tempPos = currentPos - 1;
        while(tempPos >=0)
        {
            /** Get Node representation of element at tempPos */
            currentNode = level.getCurrentDiscourse().getDiscourseElementNode(currentFragment[tempPos]);
            try
            {
                if (currentNode.getAttributes().getNamedItem("empty").getNodeValue().equals("true"))
                {
                    tempPos--;
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // currentNode does not have the 'empty' attribute, so we found a valid element
                result = tempPos;
                break;
            }        
        }        
        return result;
    }
    
    /** For each fragment this Markable contains, this method sets the associated display positions in displayStart- and 
        displayEndPosition, by setting displayStartPositions, displayEndPositions, discourseElementStartPositions, 
        leftmostDisplayPosition, rightmostDisplayPosition. */
    public final static void setDisplayPositions(Markable markable)
    {
        MarkableLevel level = markable.getMarkableLevel();
        String[] currentFragment = null;
        String currentDE = "";
        int currentDEsDiscoursePosition = 0;
        int fragPos = 0; // TODO: eliminate fragPOS, replace with q (?)
        String[][] fragments = markable.getFragments();
        // Iterate over all fragments
        for (int z=0;z<markable.singleFragments;z++)
        {
            /** Get current fragment. This is a list of continuous DiscourseElement IDs. */
            currentFragment = fragments[z];
            
            // NEW: 23. Feb.ReCreate discourseElementStartPositions, since fragment may be longer/shorter now
            markable.discourseElementStartPositions = new int[markable.size];
            
            // Iterate over currentFragment
            for (int q=0;q<currentFragment.length;q++)
            {
                // For each DE, get its DiscoursePosition
                int discPos = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentFragment[q]);
                if (discPos == -1)
                {
                    // The current DE does not have a Discourse Position, probably because it is empty. 
                    // Set startPos for fragment at running number fragPos to -1 
                    markable.discourseElementStartPositions[fragPos] = -1;
                }
                else
                {
                    // DiscPos is valid, so set startPos for fragment at running number fragPos to displayStart of DiscPos
                    markable.discourseElementStartPositions[fragPos] = level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(discPos);    
                    // If this is the first DE in the first fragment, set leftMostDisplayPosition
                }                
                fragPos++;
            }
            
            /** Get first DE in current fragment */
            currentDE = currentFragment[0];
            
            if (currentFragment.length ==1)
            {
                // The current fragment consists of one DiscourseElement only
                // Try to get its DiscPos
                currentDEsDiscoursePosition = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE);                
                if (currentDEsDiscoursePosition != -1)
                {
                    // The current DE has a valid discourse position
                    // Set for both start and end, and we are done for this fragment
                    markable.displayStartPositions[z]=level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(currentDEsDiscoursePosition);
                    markable.displayEndPositions[z]=level.getCurrentDiscourse().getDisplayEndPositionFromDiscoursePosition(currentDEsDiscoursePosition);
                    markable.discourseElementStartPositions[z] = markable.displayStartPositions[z];
                }
                else
                {
                    // The current fragment is empty, so set eveything to -1
                    markable.displayStartPositions[z]=-1;
                    markable.displayEndPositions[z]=-1;
                    markable.discourseElementStartPositions[z] = markable.displayStartPositions[z];                    
                }
            }
            else
            {
                // The current fragment contains more than one DiscourseElement
                // Try to get the Discourse position of currentDE
                currentDEsDiscoursePosition = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE);
                if (currentDEsDiscoursePosition == -1)
                {                   
                    // The current DE has no valid discourse position, so try to correct to next valid one
                    int nextPosition = getFollowingValidDiscourseElementPosition(markable,currentFragment,0);
                    if (nextPosition != -1)
                    {
                        // Some next valid DE was found at position nextPosition in the current fragment
                        currentDE = currentFragment[nextPosition];
                        // Get its DiscPos
                        currentDEsDiscoursePosition = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE);
                        // Set start of the current Fragment to start of DE with DiscPos
                        markable.displayStartPositions[z]=level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(currentDEsDiscoursePosition);
                    }
                    else
                    {
                        // No valid discourse position was found, so this is an empty fragment
                        // Set start of current fragment to -1
                        markable.displayStartPositions[z] = -1;
                    }
                }
                else
                {
                    // The current DE is valid, so take its DiscPos
                    markable.displayStartPositions[z]=level.getCurrentDiscourse().getDisplayStartPositionFromDiscoursePosition(currentDEsDiscoursePosition);   
                }
                
                // Try to get the Discourse position of the last element 
                currentDE = currentFragment[currentFragment.length-1];
                currentDEsDiscoursePosition = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE);
                if (currentDEsDiscoursePosition == -1)
                {
                    // The current DE has no valid discourse position, so correct to previous valid one
                    int prevPosition = getPreceedingValidDiscourseElementPosition(markable,currentFragment,currentFragment.length-1);
                    if (prevPosition != -1)
                    {
                        currentDE = currentFragment[prevPosition];
                        currentDEsDiscoursePosition = level.getCurrentDiscourse().getDiscoursePositionFromDiscourseElementID(currentDE);            
                        markable.displayEndPositions[z]=level.getCurrentDiscourse().getDisplayEndPositionFromDiscoursePosition(currentDEsDiscoursePosition);
                    }
                    else
                    {
                        // No valid discourse position was found, so this is an empty fragment
                        // Set end of current fragment to -1
                        markable.displayEndPositions[z] = -1;                        
                    }
                }
                else
                {
                    // The current DE is valid, so take its DiscPos
                    markable.displayEndPositions[z]=level.getCurrentDiscourse().getDisplayEndPositionFromDiscoursePosition(currentDEsDiscoursePosition);                       
                }
            }// size of fragment                                                                                
        }// for all fragments
        markable.leftmostDisplayPosition = markable.displayStartPositions[0];
        markable.rightmostDisplayPosition = markable.displayEndPositions[markable.singleFragments-1];
    }
    /*
    public final static String transposeMarkable(Markable toTranspose, String oldLang, ArrayList absoluteWords)
    {
        String string = toXMLElement(toTranspose);
        String leftPart = string.substring(0,string.indexOf("span=\""));
        String rightPart = string.substring(string.indexOf("span=\"")+5);
        rightPart = rightPart.substring(rightPart.indexOf(" "));
        string = leftPart+ "span=\""+getTransposedSpan(getSpan(toTranspose),oldLang,absoluteWords)+"\""+rightPart;
        return string;
    }
    */
/*    
    public final static String getTransposedSpan(String oldSpan, String oldLang, ArrayList absoluteWords)
    {
        // This assumes that markables are NOT discontinuous! 
        
        // Convert old span to full array of arrays of word ids.
        String[][] fullSpan = parseSpan(oldSpan);      
        
        ArrayList temp = new ArrayList();
        String entireTransposedSpan="";
        // Iterate over full span elements
        for (int b=0;b<fullSpan.length;b++)
        {
            // Get current span
            String[] currentSpan = fullSpan[b];
            String currentTransposedSpan="";
            int currentPos=0;
            if (currentSpan.length==1)
            {
                currentPos = absoluteWords.indexOf(oldLang+":"+currentSpan[0])+1;
                currentTransposedSpan="word_"+currentPos;
            }
            else
            {
                currentPos = absoluteWords.indexOf(oldLang+":"+currentSpan[0])+1;
                currentTransposedSpan="word_"+currentPos+"..word_";
                currentPos = absoluteWords.indexOf(oldLang+":"+currentSpan[currentSpan.length-1])+1;
                currentTransposedSpan=currentTransposedSpan+currentPos;
            }
            entireTransposedSpan=entireTransposedSpan+","+currentTransposedSpan;
        }
        return entireTransposedSpan.substring(1);
    }    
   */ 
    /** This method converts this Markable into an XML String representation for file storage. */
    public final static String toXMLElement(Markable markable)
    {
        String xmlstring = "<markable";
        xmlstring=xmlstring+" id=\""+markable.getID()+"\"";
        xmlstring=xmlstring+" span="+"\""+getSpan(markable)+"\"";
        Set allattribs = markable.attributes.keySet();
        Object[] keys = allattribs.toArray();
        String key = "";
        String value = "";
        for (int i=0;i<keys.length;i++)
        {            
            key = (String) keys[i];
            if (key.compareToIgnoreCase("id")==0)
            {
                continue;
            }
            value = (String) markable.attributes.get((String) keys[i]);
            value = encodeXML(value);
            if (value.equalsIgnoreCase(legacyDefaultValue) || value.equals("")) continue;
            xmlstring=xmlstring+" "+key+"=\""+value+"\" ";
        }                
        xmlstring=xmlstring+"/>";
        return xmlstring;        
    }
    
    public final static String encodeXML(String inString)
    {
    	inString = inString.replaceAll("&([^;]*)$", "&amp;$1");
        inString = inString.replaceAll("\"", "&quot;");
        inString = inString.replaceAll("<", "&lt;");
        inString = inString.replaceAll(">", "&gt;");       
        return inString;
    }
    
    /** This is used for matching queries with RegExp values. */
    public final static boolean matchesRegExp(Markable markable, String _attribute, String _value, int type, boolean _negated)
    {
        boolean negated = _negated;
        Perl5Matcher regmatcher = null;

        String attribute=_attribute.trim();
        String value=_value.trim();
        
        Pattern pattern = null;
        
        try
        {
            pattern = new Perl5Compiler().compile(value);
        }
        catch (org.apache.oro.text.regex.MalformedPatternException ex)
        {
            System.err.println("RegExp error: "+value);            
            return false;
        }
        
        regmatcher = new Perl5Matcher();

        if (type == MMAX2Constants.MARKABLE_TEXT)
        {
            // The criterion tries to match the markable text
            try
            {
                // Retrieve markable text
                String text = markable.toString();
                // cut off leading and trailing brackets
                text = text.substring(1,text.length()-1);
                //if (regmatcher.isMatch(text))               
                if (regmatcher.matches(text,pattern))
                {
                    // The current Markable does match the supplied RE (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match 
                // (this cannot happen, since getString() will always return something
                return false;
            }
        }
        else if (type == MMAX2Constants.LEVEL_NAME)
        {
            // The criterion tries to match the markable level name
            try
            {
                // Retrieve markable text
                String text = markable.getMarkableLevelName();
                // cut off leading and trailing brackets
                text = text.substring(1,text.length()-1);               
                if (regmatcher.matches(text,pattern))
                {
                    // The current Markable does match the supplied RE (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match 
                // (this cannot happen, since getString() will always return something
                return false;
            }
        }

        else if (type == AttributeAPI.NOMINAL_LIST || type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.FREETEXT)
        {
            // The regExp tries to match a nominal or freetext attribute
            try
            {            
                if (regmatcher.matches(markable.getAttributeValue(attribute),pattern))
                {
                    // The current Markable does match the supplied RE (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match
                return false;
            }
        }
        else
        {
            // The regExp tries to match a relation attribute (that's unusual, so print warning)
            try
            {
                if (regmatcher.matches(markable.getAttributeValue(attribute),pattern))
                {
                    // The current Markable does match the supplied RE (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match
                return false;
            }
        }
    }    

    /** This is used for matching queries with RegExp values. */
    public final static boolean matchesRegExp(MMAX2DiscourseElement de, String _attribute, String _value, int type, boolean _negated)
    {
        boolean negated = _negated;
        Perl5Matcher regmatcher = null;

        String attribute=_attribute.trim();
        String value=_value.trim();
        
        Pattern pattern = null;
        
        try
        {
            pattern = new Perl5Compiler().compile(value);
        }
        catch (org.apache.oro.text.regex.MalformedPatternException ex)
        {
            System.err.println("RegExp error: "+value);
            return false;
            
        }
        
        regmatcher = new Perl5Matcher();

        if (type == MMAX2Constants.BASEDATA_TEXT)
        {
            // The criterion tries to match the de text
            // Retrieve de text
            String text = de.toString();
                if (regmatcher.matches(text,pattern))
                {
                    // The current de does match the supplied RE (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current de does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
        }
        else if (type == MMAX2Constants.BASEDATA_ATTRIBUTES)
        {
            // The regExp tries to match a basedata attribute
            if (regmatcher.matches(de.getAttributeValue(attribute,""),pattern))
            {
                // The current de does match the supplied RE (true)
                if (negated)
                {
                    // So return negated true
                    return false;
                }
                else
                {
                    // Return actual true
                    return true;
                }
            }
            else
            {
                // The current Markable does not match the supplied RE (false)
                if (negated)
                {
                    // So return negated false
                    return true;
                }
                else
                {
                    // Return actual false
                    return false;
                }
            }
        }
        else
        {
            System.err.println("Only basedata_text and attribute matching supported!");                
        }
        return false;
    }    
    
    
    /** Used by MMAX query. */
    /** This method is used for matching queries that are NOT RegExps .*/
    public static final boolean matches(Markable markable,String _attribute, String _value, int type, boolean _negated)
    {
        MarkableLevel level = markable.getMarkableLevel();
        boolean negated = _negated;
        
        String attribute=_attribute.trim();
        String value=_value.trim();           
        // No regExpmatch
        if (type == MMAX2Constants.MARKABLE_TEXT)
        {
            // The criterion tries to match the markable text
            try
            {
                // Retrieve markable text
                String text = markable.toString();
                // cut off leading and trailing brackets
                text = text.substring(1,text.length()-1);
                if (value.equalsIgnoreCase(text))
                {
                    // The current Markable does match the supplied value (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied value (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match 
                // (cannot happen since getString will always return something)
                return false;
            }
        }
        else if (type == MMAX2Constants.LEVEL_NAME)
        {
            // The criterion tries to match the markable level name
            try
            {
                // Retrieve markable level name
                String text = markable.getMarkableLevelName();
                // cut off leading and trailing brackets
                //text = text.substring(1,text.length()-1);
                if (value.equalsIgnoreCase(text))
                {
                    // The current Markable does match the supplied value (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied value (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match 
                // (cannot happen since getString will always return something)
                return false;
            }
        }
            
        else if (type == AttributeAPI.NOMINAL_LIST || type == AttributeAPI.NOMINAL_BUTTON || type == AttributeAPI.FREETEXT)
        {
            // The match tries to match a nominal or freetext attribute
            try
            {
                if (value.equalsIgnoreCase(markable.getAttributeValue(attribute,"")))
                {
                    // The current Markable does match the supplied value (true)
                    if (negated)
                    {
                        // So return negated true
                        return false;
                    }
                    else
                    {
                        // Return actual true
                        return true;
                    }
                }
                else
                {
                    // The current Markable does not match the supplied RE (false)
                    if (negated)
                    {
                        // So return negated false
                        return true;
                    }
                    else
                    {
                        // Return actual false
                        return false;
                    }
                }
            }
            catch (java.lang.NullPointerException ex)
            {
                // The attribute is not defined, so no match
                return false;
            }
        }
        else
        {
            // The match tries to match a relation attribute
            if (type == AttributeAPI.MARKABLE_SET)
            {
                MarkableSet set = null;
                // Get all Relations for this Markable
                MarkableRelation[] relations = level.getActiveMarkableSetRelationsForMarkable(markable);

                // Iterate over all Relations for this Markable
                for (int z=0;z<relations.length;z++)
                {
                    // Check if the current Relation is the one accessed in this query
                    if (relations[z].getAttributeName().equalsIgnoreCase(attribute))
                    {
                        // Try to get the set that this Markable is a member of
                        set = relations[z].getMarkableSetWithAttributeValue(markable.getAttributeValue(attribute));
                    }
                }

                // Here, set is the set of name attributeName of which the current markable is a member, or null               
                if (value.equalsIgnoreCase("empty"))
                {
                    if (set == null || set.getSize() == 1)
                    {
                        // The set is either empty or contains only a singleton, and it was supposed to be
                        if (negated)
                        {
                            return false;
                        }
                        else
                        {
                            return true;
                        }
                    }
                    else
                    {
                        // The set is not empty, but is was supposed to be
                        if (negated)
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                }
                else if (value.equalsIgnoreCase("initial"))
                {
                    if (set == null)
                    {
                        // The current markable is in no set, so it is not initial
                        if (negated)
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        // The current markable is in some set
                        // Get set pos
                        int pos = set.getMarkableIndex(markable);
                        if (pos != 0)
                        {
                            // This markable is not initial
                            if (negated)
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        }
                        else
                        {
                            // This markable is initial
                            if (negated)
                            {
                                return false;
                            }
                            else
                            {
                                return true;
                            }
                        }
                    }
                }
                else if (value.equalsIgnoreCase("final"))
                {
                    if (set == null)
                    {
                        // The current markable is in no set, so it is not final
                        if (negated)
                        {
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        // The current markable is in some set
                        // Get set pos
                        int pos = set.getMarkableIndex(markable);
                        if (pos != set.getSize()-1)
                        {
                            // This markable is not final
                            if (negated)
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        }
                        else
                        {
                            // This markable is final
                            if (negated)
                            {
                                return false;
                            }
                            else
                            {
                                return true;
                            }
                        }
                    }
                }
                else
                {
                    // The value might be a number, and thus a query for a set size
                    int val = -1;
                    try
                    {
                        val = Integer.parseInt(value);
                    }
                    catch (java.lang.NumberFormatException ex)
                    {
                        
                    }
                    if (val != -1)
                    {
                        // The query was for set size
                        if (set != null)
                        {
                            if (set.getSize() == val)
                            {
                                // Val and set size are identical
                                if (negated)
                                {
                                    return false;
                                }
                                else
                                {
                                    return true;
                                }
                            }
                            else
                            {
                                // Val and set size are not identical
                                if (negated)
                                {
                                    return true;
                                }
                                else
                                {
                                    return false;
                                }                                
                            }
                        }
                    }
                }
            }
            else if (type == AttributeAPI.MARKABLE_POINTER)
            {
                if (value.equalsIgnoreCase("target"))
                {
                    // The query is for markables being pointed at via relations of name 'attribute'
                    MarkablePointer[] sourcePointer = level.getActiveMarkablePointersForTargetMarkable(markable, attribute);
                    if (sourcePointer.length==0)
                    {
                        // The current markable is not pointed at 
                        if (negated)
                        {
                            // And it wasn't to, so return true
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        // The current markable is being pointed at
                        if (negated)
                        {
                            // But it wasn't to, so return false
                            return false;
                        }
                        else
                        {
                            return true;
                        }                        
                    }
                }
                else
                {
                    MarkablePointer pointer = null;
                    // Get all Relations for this Markable
                    MarkableRelation[] relations = level.getActiveMarkablePointerRelationsForSourceMarkable(markable);

                    // Iterate over all Relations for this Markable
                    for (int z=0;z<relations.length;z++)
                    {
                        // Check if the current Relation is the one accessed in this query
                        if (relations[z].getAttributeName().equalsIgnoreCase(attribute))
                        {
                            // Try to get the pointer that this Markable is the source of
                            pointer = relations[z].getMarkablePointerForSourceMarkable(markable);
                        }
                    }
                
                    if (pointer != null)
                    {
                        // The markable is the source of the desired pointer relation
                        int val = -1;
                        try
                        {
                            val = Integer.parseInt(value);
                        }
                        catch (java.lang.NumberFormatException ex)
                        {
                        
                        }
                    
                        if (val == pointer.getSize() || (value.equalsIgnoreCase("empty")==true && pointer.getSize()==0))
                        {
                            // Val and set size are identical, or correctly empty
                            if (negated)
                            {
                                return false;
                            }
                            else
                            {
                                return true;
                            }                        
                        }
                        else
                        {
                            // Val and set size are not identical
                            if (negated)
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }                                                
                        }
                    }
                    else
                    {
                        // The markable is not the source of the desired pointer relation
                        // i.e. the attribute is empty
                        // There are still two situations in which sth. can be returned
                        if (value.equalsIgnoreCase("empty"))
                        {   
                            // Val and set size are identical
                            if (negated)
                            {
                                return false;
                            }
                            else
                            {
                                return true;
                            }                        
                        }
                        int val = -1;
                        try
                        {
                            val = Integer.parseInt(value);
                        }
                        catch (java.lang.NumberFormatException ex)
                        {
                        
                        }
                        if (val ==0)
                        {
                            // The value specifies the pointer should be empty
                            // Val and pointer size are identical
                            if (negated)
                            {
                                return false;
                            }
                            else
                            {
                                return true;
                            }                                                
                        }
                        else
                        {
                            // The desired val is not null, but the pointer size is
                            if (negated)
                            {
                                return true;
                            }
                            else
                            {
                                return false;
                            }
                        }
                    }
                }
            }
            else 
            {
                //System.out.println("Warning! Matching supported for MARKABLE_SET and MARKABLE_POINTER only!");
            }
        }
            return false;
    }

    
    /** Used by MMAX query. */
    /** This method is used for matching queries that are NOT RegExps .*/
    public static final boolean matches(MMAX2DiscourseElement de,String _attribute, String _value, int type, boolean _negated)
    {
        // MarkableLevel level = markable.getMarkableLevel();
        boolean negated = _negated;
        
        String attribute=_attribute.trim();
        String value=_value.trim();           
        // No regExpmatch
        if (type == MMAX2Constants.BASEDATA_TEXT)
        {
            // The criterion tries to match the de text
            // Retrieve de text
            String text =de.toString();
                
            if (value.equalsIgnoreCase(text))
            {
                // The current de does match the supplied value (true)
                if (negated)
                {
                    // So return negated true
                    return false;
                }
                else
                {
                    // Return actual true
                    return true;
                }
            }
            else
            {
                // The current de does not match the supplied value (false)
                if (negated)
                {
                    // So return negated false
                    return true;
                }
                else
                {
                    // Return actual false
                    return false;
                }
            }
        }
        else if (type == MMAX2Constants.BASEDATA_ATTRIBUTES)
        {
            // The match tries to match some basedata attribute
            if (value.equalsIgnoreCase(de.getAttributeValue(attribute,"")))
            {
                // The current de does match the supplied value (true)
                if (negated)
                {
                    // So return negated true
                    return false;
                }
                else
                {
                    // Return actual true
                    return true;
                }
            }
            else
            {
                // The current de does not match the supplied string (false)
                if (negated)
                {
                    // So return negated false
                    return true;
                }
                else
                {
                    // Return actual false
                    return false;
                }
            }
        }
        else
        {
            System.err.println("Only basedata_text and attribute matching supported!");
        }
        return false;
    }
    
    
    public static final String getSpan(String[][] fragments)
    {
        String[] currentFrag = null;
        String currentSpan = "";
        String span ="";
        //String[][] fragments = markable.getFragments();        
        //for (int z=0;z<markable.singleFragments;z++)
        for (int z=0;z<fragments.length;z++)
        {
            currentFrag = fragments[z];
            currentSpan = currentFrag[0];
            if (currentFrag.length >1)
            {
                currentSpan = currentSpan+".."+currentFrag[currentFrag.length-1];
            }
            span = span+currentSpan+",";
        }
        return span.substring(0,span.length()-1);        
    }
    
    public static final String getSpan(Markable markable)
    {
        String[] currentFrag = null;
        String currentSpan = "";
        String span ="";
        String[][] fragments = markable.getFragments();        
        for (int z=0;z<markable.singleFragments;z++)
        {
            currentFrag = fragments[z];
            currentSpan = currentFrag[0];
            if (currentFrag.length >1)
            {
                currentSpan = currentSpan+".."+currentFrag[currentFrag.length-1];
            }
            span = span+currentSpan+",";
        }
        return span.substring(0,span.length()-1);
    }
    
    /** Used by MMAX query. */
    public final static boolean matchesAny(Markable markable, MMAX2MatchingCriterion criterion)
    {
        if (criterion.isMatchAll())
        {
            return true;
        }
        
        int critlen = criterion.getSize();
        boolean tempresult = false;
        for (int z=0;z<critlen;z++)
        {
            if (criterion.getRegExpMatch())
            {
                tempresult = matchesRegExp(markable,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            else
            {
                tempresult = matches(markable,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }

            if (tempresult == true)
            {
                /* Since this is OR-connected, matchesAny is true if only one is true */
                return true;
            }            
        }
        /* Here, we checked all required criteria. If we made it here, either no match was found, or all criteria were empty, which should return false as well */
        return false;
    }

    /** Used by MMAX query. */
    public final static boolean matchesAny(MMAX2DiscourseElement de, MMAX2MatchingCriterion criterion)
    {
        if (criterion.isMatchAll())
        {
            return true;
        }
        
        int critlen = criterion.getSize();
        boolean tempresult = false;
        for (int z=0;z<critlen;z++)
        {
            if (criterion.getRegExpMatch())
            {
                tempresult = matchesRegExp(de,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            else
            {
                tempresult = matches(de,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }

            if (tempresult == true)
            {
                /* Since this is OR-connected, matchesAny is true if only one is true */
                return true;
            }
        }
        /* Here, we checked all required criteria. If we made it here, either no match was found, or all criteria were empty, which should return false as well */
        return false;
    }
    
    
    /** Used by MMAX query. */
    public final static boolean matchesAll (Markable markable, MMAX2MatchingCriterion criterion)
    {
        if (criterion.isMatchAll())
        {
            return true;
        }
        // A criterion can never contain more than one value to match simultaneously
        int critlen = criterion.getSize();
        boolean tempresult = false;
        boolean anyTrue = false;
        // Iterate over all values in criterion
        for (int z=0;z<critlen;z++)
        {
            if (criterion.getRegExpMatch())
            {
                tempresult = MarkableHelper.matchesRegExp(markable,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            else
            {
                tempresult = MarkableHelper.matches(markable,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            if (tempresult == false)
            {
                /* Since this is AND-connected, matchesAll is false if only one is false */
                return false;
            }
            else
            {
                anyTrue = true;
            }
        }
        /* Here, we checked all required criteria */
        /* If we made it here only because all criteria were empty, anyTrue is false */        
        return anyTrue;        
    }

    /** Used by MMAX query. */
    public final static boolean matchesAll (MMAX2DiscourseElement de, MMAX2MatchingCriterion criterion)
    {
        if (criterion.isMatchAll())
        {
            return true;
        }
        // A criterion can never contain more than one value to match simultaneously
        int critlen = criterion.getSize();
        boolean tempresult = false;
        boolean anyTrue = false;
        // Iterate over all values in criterion
        for (int z=0;z<critlen;z++)
        {
            if (criterion.getRegExpMatch())
            {
                tempresult = MarkableHelper.matchesRegExp(de,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            else
            {
                tempresult = MarkableHelper.matches(de,criterion.getAttributeName(),criterion.getValue(z),criterion.getAttributeType(),criterion.getNegated());
            }
            if (tempresult == false)
            {
                /* Since this is AND-connected, matchesAll is false if only one is false */
                return false;
            }
            else
            {
                anyTrue = true;
            }
        }
        /* Here, we checked all required criteria */
        /* If we made it here only because all criteria were empty, anyTrue is false */        
        return anyTrue;        
    }
    
    
    public static final int getDistanceInDiscoursePositions(Markable m1, Markable m2)
    {
        return getDistanceInDiscoursePositions(new MMAX2QueryResultTupleElement(m1), new MMAX2QueryResultTupleElement(m2));
    }

    
    public static final int getDistanceInDiscoursePositions(MMAX2QueryResultTupleElement e1, MMAX2QueryResultTupleElement e2)
    {
        int m1Pos = e1.getLeftmostDiscoursePosition();
        int m2Pos = e2.getLeftmostDiscoursePosition();
        return Math.abs(m1Pos-m2Pos);
    }
    
    public final static HashMap parseAttributesToHashMap(String attribs)
    {
        HashMap result = new HashMap();
        StringTokenizer toki = new StringTokenizer(attribs,",",false);
        while(toki.hasMoreTokens())
        {
            String currentToken = toki.nextToken().trim();
            int index = currentToken.indexOf("=");
            if (index == -1)
            {
                System.err.println("Error: expected '<att>=<val>', found "+currentToken);
                continue;
            }
            String currentAttrib = currentToken.substring(0,index).trim();
            String currentVal = currentToken.substring(index+1).trim();
            result.put(new String(currentAttrib),new String(currentVal));
        }
        return result;
    }

    public final static ArrayList[] parseAttributesToLists(String attribs)
    {
        ArrayList[] result = new ArrayList[2];
        ArrayList attributes = new ArrayList();
        ArrayList values = new ArrayList();
        StringTokenizer toki = new StringTokenizer(attribs,",",false);
        while(toki.hasMoreTokens())
        {
            String currentToken = toki.nextToken().trim();
            int index = currentToken.indexOf("=");
            if (index == -1)
            {
                System.err.println("Error: expected '<att>=<val>', found "+currentToken);
                continue;
            }
            String currentAttrib = currentToken.substring(0,index).trim();
            String currentVal = currentToken.substring(index+1).trim();
            attributes.add(currentAttrib);
            values.add(currentVal);
            //result.put(new String(currentAttrib),new String(currentVal));
        }
        result[0] = attributes;
        result[1] = values;
        return result;
    }
    
    
    /** This method parses the value of a span attribute and returns an Array with one Array per fragment. 
        Spans of the form word_x..word_y will be expanded to include all intermediate ids */
/*    public final static String[][] parseSpan(String span)
    {
        String currentspan="";
        ArrayList spanlist = new ArrayList();
        String[] fragArray= null;

        if (span.indexOf(",")==-1)
        {
            // This span is not discontinuous, so just word_3 or word_3..word_7
            fragArray=parseSpanFragmentToArray(span);
            String[][] result = new String[1][fragArray.length];;
            result[0]=fragArray;
            return result;
        }
        
        StringTokenizer toki = new StringTokenizer(span,",");
        while (toki.hasMoreTokens())
        {
            currentspan = toki.nextToken().trim();            
            fragArray=parseSpanFragmentToArray(currentspan);
            spanlist.add(fragArray);
        }
        
        return (String[][]) spanlist.toArray(new String[1][1]);
    }
   */
    
    
	/** This method parses the value of a span fragment (either word_1..word_4 or word_3) and returns an array of all elements.
            Spans of the form word_x..word_y are expanded to include all intermediate ids. */
/*    public final static String[] parseSpanFragmentToArray(String span)
    {        
    	if (span.indexOf("..") == -1)
    	{	
            String[] result = new String[1];
            result[0] = span;
            return result;            
    	}

    	String NameSpace;
    	String FirstIDString;
    	String LastIDString;

    	int FirstIDInteger;
    	int LastIDInteger;
    	int i;
        
        // A list is needed, so create one only here
        ArrayList NewWordsIDList = new ArrayList();
         //Extract IDs namespace from span 
         //The namespace is the part up to the first _ character (incl.) 

        NameSpace = span.substring(0,span.indexOf("_")+1);

         //Extract leftmost id string from span 
        FirstIDString=span.substring(0,span.indexOf(".."));

        //System.err.println(FirstIDString);
        
         //Extract rightmost id string from span 
        LastIDString=span.substring(span.lastIndexOf("..") + 2);
        
        //System.err.println(LastIDString);
               
        FirstIDInteger = Integer.parseInt(FirstIDString.substring(FirstIDString.indexOf("_")+1));
        LastIDInteger = Integer.parseInt(LastIDString.substring(LastIDString.indexOf("_")+1));
        //FirstIDDouble = Double.parseDouble(FirstIDString.substring(FirstIDString.indexOf("_")+1));
        //LastIDDouble = Double.parseDouble(LastIDString.substring(LastIDString.indexOf("_")+1));

        
        for(i=FirstIDInteger;i<=LastIDInteger;i++)
        {
            NewWordsIDList.add(NameSpace + String.valueOf(i));
        }

        return (String[]) NewWordsIDList.toArray(new String[1]);
    }
    */
    
    
    /** This method parses the value of a span attribute and returns all elements in an ArrayList. Spans of the form word_x..word_y  
    will be expanded to include all intermediate ids */ 
public final static ArrayList parseCompleteSpan(String span) 
{ 
    String currentspan=""; 
    ArrayList spanlist = new ArrayList(); 
    ArrayList fraglist; 

     //Get overall length of span attribute  
    int spanlen = span.length(); 

    for (int i=0;i<spanlen;i++) 
    { 
        if(span.charAt(i) != ',') 
        { 
            currentspan=currentspan+span.charAt(i); 
            continue; 
        } 
        currentspan.trim(); 
        fraglist=parseSpanFragment(currentspan); 
        if (fraglist.size() > 1) 
        { 
            spanlist.addAll(fraglist); 
        } 
        else 
        { 
            spanlist.add(fraglist.get(0)); 
        } 
        currentspan=""; 
        fraglist=null; 
    }// for 
 
    currentspan.trim(); 
    fraglist=parseSpanFragment(currentspan); 
    if (fraglist.size() > 1) 
    { 
        spanlist.addAll(fraglist); 
    } 
    else 
    { 
        spanlist.add(fraglist.get(0)); 
    } 
    currentspan=""; 
    fraglist=null; 
 
    return spanlist; 
} 


/** This method parses the value of a DE's span attribute and returns all elements in an ArrayList. Spans of the form word_x..word_y  
are expanded to include all intermediate ids */ 
public final static ArrayList parseSpanFragment(String span) 
{ 
String NameSpace; 
String FirstIDString; 
String LastIDString; 

int FirstIDInteger; 
int LastIDInteger; 
int i; 

ArrayList NewWordsIDList = new ArrayList(); 

if (span.indexOf("..") == -1) 
{ 
//         No .. found, so span is one element only  

        NewWordsIDList.add(span); 
        return NewWordsIDList; 
} 

 //Extract IDs namespace from span  
 //The namespace is the part up to the first _ character (incl.)  

NameSpace = span.substring(0,span.indexOf("_")+1); 

 //Extract leftmost id string from span  
FirstIDString=span.substring(0,span.indexOf(".")); 

// Extract rightmost id string from span  
LastIDString=span.substring(span.lastIndexOf(".") + 1); 

FirstIDInteger = Integer.parseInt(FirstIDString.substring(FirstIDString.indexOf("_")+1)); 
LastIDInteger = Integer.parseInt(LastIDString.substring(LastIDString.indexOf("_")+1)); 

for(i=FirstIDInteger;i<=LastIDInteger;i++) 
{ 
        NewWordsIDList.add(NameSpace + String.valueOf(i)); 
} 

return NewWordsIDList; 
}          
    
    
}
