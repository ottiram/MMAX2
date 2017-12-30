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

package org.eml.MMAX2.gui.windows;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
public class MMAX2AnnotationHintWindow extends javax.swing.JFrame
{
    JEditorPane pane = null;
    String defaultText = "<html><body></body></html>";
    /** Creates a new instance of MMAX2AnnotationHintWindow */
    public MMAX2AnnotationHintWindow() 
    {
        super();
        pane = new JEditorPane("text/html",defaultText);
        pane.setEditable(false);
        getContentPane().add(new JScrollPane(pane));
    }
    
    public final void setText(String textToShow)
    {
        pane.setText(textToShow);
    }
    
    public final void hideText()
    {
        pane.setText(defaultText);
    }
}
