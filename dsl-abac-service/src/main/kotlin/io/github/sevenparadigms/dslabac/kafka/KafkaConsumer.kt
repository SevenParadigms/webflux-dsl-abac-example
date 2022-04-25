package io.github.sevenparadigms.dslabac.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class KafkaConsumer {

    private val latch: CountDownLatch = CountDownLatch(1)
    private var payload: String? = null

    @KafkaListener(topics = ["embedded-test-topic"])
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        setPayload(consumerRecord.toString())
        latch.countDown()
    }

    fun getLatch(): CountDownLatch? {
        return latch
    }

    fun getPayload(): String? {
        return payload
    }

    fun setPayload(payload: String) {
        this.payload = payload
    }

}