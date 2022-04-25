package io.github.sevenparadigms.dslabac

import io.github.sevenparadigms.dslabac.kafka.KafkaConsumer
import io.github.sevenparadigms.dslabac.kafka.KafkaProducer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [Application::class])
@DirtiesContext
@Import(KafkaTestContainer.KafkaTestContainersConfiguration::class)
class KafkaTestContainer {

    companion object {
        var kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

        init {
            kafka.start()
        }
    }

    @Autowired
    private val consumer: KafkaConsumer? = null

    @Autowired
    private val producer: KafkaProducer? = null

    @Value("\${test.topic}")
    private val topic: String? = null

    @Test
    fun containerUpAndRun() {
        Assertions.assertTrue(kafka.isRunning)
    }

    @Test
    @Throws(Exception::class)
    fun givenKafkaDockerContainer_whenSendingToSimpleProducer_thenMessageReceived() {
        createTopics(topic!!)
        producer!!.send(topic, "embedded-test-topic")
        consumer!!.getLatch()!!.await(10000, TimeUnit.MILLISECONDS)
        assertThat(consumer.getLatch()!!.count, equalTo(0L))
        val payload = consumer.getPayload()
        assertThat(payload, containsString("embedded-test-topic"))
    }

    private fun createTopics(vararg topics: String) {
        val newTopics: MutableList<NewTopic>? = Arrays.stream(topics)
            .map { topic -> NewTopic(topic, 1, 1.toShort()) }
            .collect(Collectors.toList())
        AdminClient.create(mutableMapOf<String, Any>(BOOTSTRAP_SERVERS_CONFIG to getKafkaBrokers())).use { admin ->
            admin.createTopics(newTopics)
        }
    }

    private fun getKafkaBrokers(): String {
        return String.format("%s:%d", "localhost", kafka.firstMappedPort)
    }

    @TestConfiguration
    class KafkaTestContainersConfiguration {

        @Bean
        fun consumerConfigs(): MutableMap<String, Any> {
            val props: MutableMap<String, Any> = HashMap()
            props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
            props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            props[ConsumerConfig.GROUP_ID_CONFIG] = "baeldung"
            props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.qualifiedName.toString()
            props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.qualifiedName.toString()
            return props
        }

        @Bean
        fun producerFactory(): DefaultKafkaProducerFactory<Any?, Any?> {
            val configProps: MutableMap<String, Any> = HashMap()
            configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
            configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName.toString()
            configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName.toString()
            return DefaultKafkaProducerFactory<Any?, Any?>(configProps)
        }

        @Bean
        fun consumerFactory(): DefaultKafkaConsumerFactory<Any?, Any?> {
            return DefaultKafkaConsumerFactory(consumerConfigs())
        }

    }

}