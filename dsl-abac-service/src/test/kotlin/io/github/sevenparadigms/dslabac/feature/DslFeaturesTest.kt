package io.github.sevenparadigms.dslabac.feature

import io.github.sevenparadigms.dslabac.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.expression.ExpressionParserCache
import org.springframework.data.r2dbc.support.DslUtils
import reactor.test.StepVerifier

class DslFeaturesTest : AbstractIntegrationTest() {
    @Test
    fun `should values set features verify`() {
        val feature = Feature(
            group = ExpressionParserCache.INSTANCE.parseExpression("a==5"),     // using reserved word
            equality = 5,
            customEquality = "fife",
            readonly = "r/o",
            customReadonly = 6
        )

        // check initial values by name, annotation and properties
        featureRepository.save(feature)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual ->
                run {
                    assertNotNull(actual.id)
                    assertEquals(actual.version, 1)
                    assertNotNull(actual.versionAnn)
                    assertEquals(actual.customVersion, 1)
                    assertNotNull(actual.createdAt)
                    assertNotNull(actual.createdAnn)
                    assertNotNull(actual.customCreate)
                    assertNotNull(actual.customUpdate)
                    assertNotNull(actual.attrNow)
                }
            }
            .verifyComplete()

        // check versions up and now times updated
        val previous = feature.copy()
        featureRepository.save(feature)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual ->
                run {
                    assertEquals(actual.version, 2)
                    assertEquals(actual.customVersion, 2)
                    assertTrue(!DslUtils.compareDateTime(actual.customUpdate, previous.customUpdate))
                    assertTrue(!DslUtils.compareDateTime(actual.attrNow, previous.attrNow))
                }
            }
            .verifyComplete()

        // other values do not changed from start
        assertEquals(feature.createdAt, previous.createdAt)
        assertTrue(DslUtils.compareDateTime(feature.createdAnn, previous.createdAnn))
        assertTrue(DslUtils.compareDateTime(feature.customCreate, previous.customCreate))
        assertEquals(feature.group, ExpressionParserCache.INSTANCE.parseExpression("a==5"))
        assertEquals(feature.equality, 5)
        assertEquals(feature.customEquality, "fife")
        assertEquals(feature.readonly, "r/o")
        assertEquals(feature.customReadonly, 6)

        //try change readonly values and always get old value
        feature.readonly = "r/o - changed!!!"
        featureRepository.save(feature).block()
        featureRepository.findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.readonly, "r/o") }
            .verifyComplete()
        feature.readonly = "r/o"     // in model value restored also

        feature.customReadonly = 6 + 111
        featureRepository.save(feature).`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.customReadonly, 6) }
            .verifyComplete()
        assertEquals(feature.customReadonly, 6)     // in model value restored also

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

    @Test
    fun `should cache features verify with HazelcastCacheManager`() {
        val feature = Feature(
            group = ExpressionParserCache.INSTANCE.parseExpression("a==5"),
            equality = 5,
            customEquality = "fife",
            readonly = "r/o",
            customReadonly = 6
        )
        // save feature
        featureRepository.save(feature).block()
        assertNotNull(feature.id)

        // change feature in cache and get it
        featureRepository
            .cache().put(feature.copy(group = ExpressionParserCache.INSTANCE.parseExpression("a==6")))
            .findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.group, ExpressionParserCache.INSTANCE.parseExpression("a==6")) }
            .verifyComplete()

        // evict cache, get real feature from database and from cache
        featureRepository
            .cache().evict(feature.id)
            .findById(feature.id!!)
            .`as`(StepVerifier::create)
            .consumeNextWith { actual -> assertEquals(actual.group, ExpressionParserCache.INSTANCE.parseExpression("a==5")) }
            .verifyComplete()
        assertEquals(featureRepository.cache().get(feature.id)?.group?.expressionString,
            ExpressionParserCache.INSTANCE.parseExpression("a==5").expressionString)
    }
}