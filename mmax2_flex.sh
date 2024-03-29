#!/bin/bash
# libs="Libs/annotationdiffplugin.jar:jar_archive/1.14.005/MMAX2.jar:Libs/batik-1.5.1/batik-awt-util.jar:Libs/batik-1.5.1/batik-dom.jar:Libs/batik-1.5.1/batik-svggen.jar:Libs/batik-1.5.1/batik-util.jar:Libs/batik-1.5.1/batik-xml.jar:Libs/jakarta-oro-2.0.8/jakarta-oro-2.0.8.jar:Libs/xalan-j_2_7_2-bin/xalan.jar:Libs/xalan-j_2_7_2-bin/xml-apis.jar:Libs/Xerces-J-bin.2.12.1/xercesImpl.jar:Libs/Xerces-J-bin.2.12.1/xml-apis.jar:Libs/Xerces-J-bin.2.12.1/serializer.jar:."
libs="Libs/annotationdiffplugin.jar:Libs/MMAX2.jar:Libs/batik-1.5.1/batik-awt-util.jar:Libs/batik-1.5.1/batik-dom.jar:Libs/batik-1.5.1/batik-svggen.jar:Libs/batik-1.5.1/batik-util.jar:Libs/batik-1.5.1/batik-xml.jar:Libs/jakarta-oro-2.0.8/jakarta-oro-2.0.8.jar:Libs/xalan-j_2_7_2-bin/xalan.jar:Libs/xalan-j_2_7_2-bin/xml-apis.jar:Libs/Xerces-J-bin.2.12.1/xercesImpl.jar:Libs/Xerces-J-bin.2.12.1/xml-apis.jar:Libs/Xerces-J-bin.2.12.1/serializer.jar:."

if [ $# -eq 0 ]; then
	java -Dfreetext_field_columns=20 -Dfreetext_font_increase=2  -classpath  $libs org.eml.MMAX2.core.MMAX2
fi

if [ $# -eq 1 ]; then
	echo "Using default common_paths.xml"
	java -Dfreetext_field_columns=20 -Dfreetext_font_increase=2 -classpath  $libs org.eml.MMAX2.core.MMAX2 $1
fi

if [ $# -eq 2 ]; then
	echo "Using provided common_paths:" $2
	java -Dfreetext_field_columns=20 -Dfreetext_font_increase=2 -classpath $libs org.eml.MMAX2.core.MMAX2 $1 -common_paths $2
fi
