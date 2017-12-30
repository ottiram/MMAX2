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
<xsl:text> </xsl:text>
<xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
<xsl:value-of select="mmax:setStartWithAttributes()"/>
<xsl:apply-templates/>
<xsl:value-of select="mmax:setEndWithAttributes()"/>
<xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
</xsl:template>

<xsl:template match="utterances:markable" mode="opening">
<xsl:if test="mmax:startsMarkableFromLevel(@id,@mmax_level,'turns')=false">
<xsl:text>
</xsl:text>
</xsl:if>
<xsl:text>	</xsl:text>
<xsl:variable name="fullSpeechAct">
<xsl:choose>
<xsl:when test="@sp-act = 'answelab'">
<xsl:text>answElab</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'directelab'">
<xsl:text>directElab</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'expressopinion'">
<xsl:text>expressOpinion</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'expresspossibility'">
<xsl:text>expressPossibility</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'expressregret'">
<xsl:text>expressRegret</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'expresswish'">
<xsl:text>expressWish</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'identifyself'">
<xsl:text>identifySelf</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'informintent'">
<xsl:text>informIntent</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'informintent-hold'">
<xsl:text>informIntent-hold</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'raiseissue'">
<xsl:text>raiseIssue</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'reqdirect'">
<xsl:text>reqDirect</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'reqinfo'">
<xsl:text>reqInfo</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'reqmodal'">
<xsl:text>reqModal</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'selftalk'">
<xsl:text>selfTalk</xsl:text>
</xsl:when>
<xsl:when test="@sp-act = 'thirdparty'">
<xsl:text>thirdParty</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@sp-act"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:value-of select="mmax:addLeftMarkableHandle(@mmax_level, @id, 1+string-length(@sp-act),1)"/>
<xsl:text>[</xsl:text><xsl:value-of select="substring(concat($fullSpeechAct,'                    '),1,20)"/>
</xsl:template>


<xsl:template match="utterances:markable" mode="closing">
<xsl:value-of select="mmax:addRightMarkableHandle(@mmax_level, @id,1)"/>
<xsl:text>]</xsl:text>
<xsl:value-of select="mmax:startSubscript()"/>
<xsl:text> </xsl:text>
<xsl:value-of select="@type"/>
<xsl:value-of select="mmax:endSubscript()"/>
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
</xsl:template>

</xsl:stylesheet>