<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader">

 <xsl:output method="text" indent="no" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="word">
<xsl:value-of select="mmax:registerDiscourseElement(@id)"/>
<xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
 <xsl:value-of select="mmax:setDiscourseElementStart()"/>
  <xsl:apply-templates/>
  <xsl:value-of select="mmax:setDiscourseElementEnd()"/>
 <xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
</xsl:template>

</xsl:stylesheet>

