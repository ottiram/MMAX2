<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader"
                xmlns:sentences="www.eml.org/NameSpaces/sentences">
 <xsl:output method="text" indent="no" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="word">
<xsl:value-of select="mmax:registerDiscourseElement(@id)"/>
<xsl:if test="substring(text(),1,1)!='*'"> <!-- Consider for space insertion only if word is not empty -->
 <xsl:choose>
  <xsl:when test="@affix='true' or 
                   text()='.' or
                   text()='?' or
                   text()=','"> <!-- Do not insert space if word is an affix or separator -->
  </xsl:when>
  <xsl:otherwise> <!-- In all other cases, insert spaces in between words -->
   <xsl:text>   </xsl:text>
  </xsl:otherwise>
 </xsl:choose>
</xsl:if>

<xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
 <xsl:value-of select="mmax:setDiscourseElementStart()"/>
  <!-- Display word text only if it is not empty -->
   <xsl:if test="substring(text(),1,1)!='*'">
    <xsl:apply-templates/>
   </xsl:if>
  <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
 <xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
</xsl:template>

<xsl:template match="sentences:markable" mode="opening">
</xsl:template>

<xsl:template match="sentences:markable" mode="closing">
<xsl:text>

</xsl:text>
</xsl:template>

</xsl:stylesheet>

