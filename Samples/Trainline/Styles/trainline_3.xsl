<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader"
                xmlns:utterances="www.eml.org/NameSpaces/utterances"
                xmlns:turns="www.eml.org/NameSpaces/turns">
  <xsl:output method="text" indent="no" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="words">
 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="word">
 <xsl:value-of select="mmax:registerDiscourseElement(@id)"/>
  <xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
   <xsl:value-of select="mmax:setDiscourseElementStart()"/>
    <xsl:apply-templates/>
   <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
  <xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
 <xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="turns:markable" mode="opening">
<xsl:text>
</xsl:text>
<xsl:value-of select="mmax:startBold()"/>
 <xsl:value-of select="@speaker"/>
 <xsl:text>.</xsl:text>
 <xsl:value-of select="@number"/>
 <xsl:text>: </xsl:text>
<xsl:value-of select="mmax:endBold()"/>
<xsl:text>	</xsl:text> <!-- This is a tab character -->
<xsl:value-of select="mmax:startBold()"/>
  <xsl:value-of select="mmax:addLeftMarkableHandle(@mmax_level, @id, '[')"/>
<xsl:value-of select="mmax:endBold()"/>
</xsl:template>

<xsl:template match="turns:markable" mode="closing">
 <xsl:value-of select="mmax:startBold()"/>
  <xsl:value-of select="mmax:addRightMarkableHandle(@mmax_level, @id, ']')"/>
 <xsl:value-of select="mmax:endBold()"/>
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="utterances:markable" mode="opening">
 <xsl:if test="mmax:startsMarkableFromLevel(@id, @mmax_level, 'turns')=false">
 <xsl:text>
	</xsl:text> <!-- This is a tab character -->
 </xsl:if>
 <xsl:value-of select="mmax:addLeftMarkableHandle(@mmax_level, @id, '[')"/>
</xsl:template>

<xsl:template match="utterances:markable" mode="closing">
 <xsl:value-of select="mmax:addRightMarkableHandle(@mmax_level, @id, concat(']  ',@type),1)"/>
</xsl:template>
</xsl:stylesheet>