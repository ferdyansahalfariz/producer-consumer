package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.avro.generic.GenericRecord;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;

public class ConsumerApp {

    public static void main(String[] args) {
        // Set the Kafka broker address
        String bootstrapServers = "master.k8s.alldataint.com:9092";

        // Set the Schema Registry URL
        String schemaRegistryUrl = "http://master.k8s.alldataint.com:8081";

        // Set the topic name
        String topic = "ulangan";

        // Create consumer properties
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        properties.put("schema.registry.url", schemaRegistryUrl);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "ulangan");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(List.of("ulangan"));

        while (true) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofSeconds(1));
            
	    records.forEach(record -> {
                // Retrieve the Avro record
                GenericRecord avroRecord = record.value();

		// Retrieve key, offset, and value
    		String key = record.key();
    		long offset = record.offset();

                // Create a Ulangan object from the Avro record
                Ulangan ulangan = new Ulangan();
                ulangan.setId(avroRecord.get("id").toString());
                ulangan.setNilai((Double) avroRecord.get("nilai"));

                // Process the Payment object
		System.out.println("Key: " + key + ", Offset: " + offset + ", Ulangan: " + ulangan.toString());
            });
        }
    }
}

