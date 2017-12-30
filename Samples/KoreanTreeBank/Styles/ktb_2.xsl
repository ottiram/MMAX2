<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader"
                xmlns:sentences="www.eml.org/NameSpaces/sentences"
                xmlns:words="www.eml.org/NameSpaces/words"
                xmlns:pos="www.eml.org/NameSpaces/pos">
 <xsl:output method="text" indent="no" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="word">
 <!-- Register at any rate, no matter if word is to be suppressed -->
 <xsl:value-of select="mmax:registerDiscourseElement(@id)"/>

 <xsl:choose> <!-- Open outer choose -->
  <xsl:when test="substring(text(),1,1)!='*'">
   <!-- Do not suppress this word -->
   <xsl:choose> <!-- Open inner choose -->
   <xsl:when test="@affix='true' or 
                      text()='.' or
                      text()='?' or
                      text()=','"> <!-- Do not insert space if word is an affix or separator -->
   </xsl:when>
   <xsl:otherwise> <!-- In all other cases, insert spaces in between words -->
    <xsl:text>   </xsl:text>
   </xsl:otherwise>
   </xsl:choose> <!-- Close inner choose -->
   <xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
    <xsl:value-of select="mmax:setDiscourseElementStart()"/>
     <xsl:apply-templates/>
    <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
   <xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
  </xsl:when>

  <xsl:otherwise>
   <!-- Do suppress this word, so do not call apply-templates -->
   <xsl:value-of select="mmax:setDiscourseElementStart()"/>
   <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
  </xsl:otherwise>
 </xsl:choose> <!-- Close outer choose -->
</xsl:template>

<xsl:template match="sentences:markable" mode="closing">
<xsl:text>

</xsl:text>
</xsl:template>

<xsl:template match="pos:markable" mode="opening">
<xsl:value-of select="mmax:addLeftMarkableHandle(@mmax_level, @id,'[')"/>
</xsl:template>

<xsl:template match="pos:markable" mode="closing">
<xsl:value-of select="mmax:addRightMarkableHandle(@mmax_level, @id,']')"/>
</xsl:template>

<xsl:template match="words:markable" mode="opening">
 <xsl:value-of select="mmax:startBold()"/>
  <xsl:value-of select="mmax:addLeftMarkableHandle(@mmax_level, @id,'[')"/>
 <xsl:value-of select="mmax:endBold()"/> 
</xsl:template>

<xsl:template match="words:markable" mode="closing">
 <xsl:value-of select="mmax:startBold()"/> 
  <xsl:value-of select="mmax:addRightMarkableHandle(@mmax_level, @id,']')"/>
 <xsl:value-of select="mmax:endBold()"/> 
</xsl:template>

</xsl:stylesheet>

