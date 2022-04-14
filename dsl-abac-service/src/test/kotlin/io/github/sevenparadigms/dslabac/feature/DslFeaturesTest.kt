package io.github.sevenparadigms.dslabac.feature

import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.expression.ExpressionParserCache
import org.springframework.data.r2dbc.support.DslUtils
import reactor.test.StepVerifier

class DslFeaturesTest : AbstractIntegrationTest() {

    @Test
    fun `should values set feature verify`() {
        val feature = Feature(group = ExpressionParserCache.INSTANCE.parseExpression("a==5"),
            equality = 5, customEquality = "fife", readonly = "r/o", customReadonly = 6
        )

        featureRepository.save(feature).block()

        // check initial values
        assertNotNull(feature.id)
        assertEquals(feature.group, ExpressionParserCache.INSTANCE.parseExpression("a==5"))
        assertEquals(feature.version, 1)
        assertNotNull(feature.versionAnn)
        assertEquals(feature.customVersion, 1)
        assertNotNull(feature.createdAt)
        assertNotNull(feature.createdAnn)
        assertNotNull(feature.customCreate)
        assertNotNull(feature.customUpdate)
        assertNotNull(feature.attrNow)
        assertEquals(feature.equality, 5)
        assertEquals(feature.customEquality, "fife")
        assertEquals(feature.readonly, "r/o")
        assertEquals(feature.customReadonly, 6)

        val firstVersion = feature.copy()

        featureRepository.save(feature).block()

        // check versions up and now times updated
        assertNotNull(feature.id)
        assertEquals(feature.group, ExpressionParserCache.INSTANCE.parseExpression("a==5"))
        assertEquals(feature.version, 2)
        assertEquals(feature.customVersion, 2)
        assertTrue(!DslUtils.compareDateTime(feature.customUpdate, firstVersion.customUpdate))
        assertTrue(!DslUtils.compareDateTime(feature.attrNow, firstVersion.attrNow))

        // all other values do not changed
        assertEquals(feature.createdAt, firstVersion.createdAt)
        assertTrue(DslUtils.compareDateTime(feature.createdAnn, firstVersion.createdAnn))
        assertTrue(DslUtils.compareDateTime(feature.customCreate, firstVersion.customCreate))
        assertEquals(feature.equality, 5)
        assertEquals(feature.customEquality, "fife")
        assertEquals(feature.readonly, "r/o")
        assertEquals(feature.customReadonly, 6)

        //try change readonly values and get old values
        feature.readonly = "r/o - changed!!!"
        featureRepository.save(feature).block()
        featureRepository.findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.readonly, "r/o") }
            .verifyComplete()
        feature.readonly = "r/o"

        feature.customReadonly = 6 + 111
        featureRepository.save(feature).block()
        assertEquals(feature.customReadonly, 6)

        //try change equality values and get exception
        feature.equality = 5 + 111
        featureRepository.save(feature) //
            .`as`(StepVerifier::create) //
            .expectError(IllegalStateException::class.java)
        featureRepository.findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.equality, 5) }
            .verifyComplete()

        feature.customEquality = "fife - changed!!!"
        featureRepository.save(feature) //
            .`as`(StepVerifier::create) //
            .expectError(IllegalStateException::class.java)
        featureRepository.findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.customEquality, "fife") }
            .verifyComplete()
    }
}