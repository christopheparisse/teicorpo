<!--
Source - https://stackoverflow.com/a/58510553
Posted by Daniel Haley, modified by community. See post 'Timeline' for change history
Retrieved 2026-02-17, License - CC BY-SA 4.0
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
