package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ProducerApp {

    public static void main(String[] args) {
        String bootstrapServers = "master.k8s.alldataint.com:9092";

        // Set the Schema Registry URL
        String schemaRegistryUrl = "http://master.k8s.alldataint.com:8081";

        // Set the topic name
        String topic = "ulangan";

        // Create producer properties
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put("schema.registry.url", schemaRegistryUrl);

        try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(properties)) {
            double nilaiAwal = 80.00;
            for (int i = 1; i <= 5; i++) {
                    // Create a Ulangan object
                    Ulangan ulangan = new Ulangan(String.valueOf(i), nilaiAwal);

                    // Create a GenericRecord based on the Ulangan object
                    GenericRecord genericRecord = new GenericData.Record(ulangan.getSchema());
                    genericRecord.put("id", ulangan.getId());
                    genericRecord.put("nilai", ulangan.getNilai());

                    // Produce the Avro record to Kafka
                    ProducerRecord<String, GenericRecord> record = new ProducerRecord<>("ulangan", Integer.toString(i), genericRecord);
                    producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception == null) {
                        System.out.printf("Avro record terkirim ke partisi %d dengan offset %d%n : ", metadata.partition(), metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            });
            nilaiAwal += 5;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

