package io.github.sevenparadigms.dslabac.feature

import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.expression.ExpressionParserCache

class DslFeaturesTest : AbstractIntegrationTest() {

    @Test
    fun `should auto values set feature verify`() {
        val feature = Feature(group = ExpressionParserCache.INSTANCE.parseExpression("a==5"),
            equality = 5, customEquality = "fife", readonly = "r/o", customReadonly = 6
        )

        featureRepository.save(feature).block()

        assertNotNull(feature.id)
        assertEquals(feature.group, ExpressionParserCache.INSTANCE.parseExpression("a==5"))
        assertEquals(feature.version, 1)
        assertNotNull(feature.versionAnn)
        val oldVersionAnn = feature.versionAnn
        assertEquals(feature.customVersion, 1)
        assertNotNull(feature.createdAt)
        assertNotNull(feature.createdAnn)
        assertNotNull(feature.customCreate)
        assertNotNull(feature.customUpdate)
        val oldCustomUpdate = feature.customUpdate
        assertNotNull(feature.attrNow)
        assertEquals(feature.equality, 5)
        assertEquals(feature.customEquality, "fife")
        assertEquals(feature.readonly, "r/o")
        assertEquals(feature.customReadonly, 6)

        featureRepository.save(feature).block()

        assertNotNull(feature.id)
        assertEquals(feature.group, ExpressionParserCache.INSTANCE.parseExpression("a==5"))
        assertEquals(feature.version, 2)
        assertNotNull(feature.versionAnn)
        assertNotEquals(feature.versionAnn, oldVersionAnn)
        assertEquals(feature.customVersion, 2)
        assertNotNull(feature.createdAt)
        assertNotNull(feature.createdAnn)
        assertNotNull(feature.customCreate)
        assertNotNull(feature.customUpdate)
        assertNotEquals(feature.customUpdate, oldCustomUpdate)
        assertNotNull(feature.attrNow)
        assertEquals(feature.equality, 5)
        assertEquals(feature.customEquality, "fife")
        assertEquals(feature.readonly, "r/o")
        assertEquals(feature.customReadonly, 6)
    }
}