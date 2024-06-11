package com.example;

import org.apache.avro.Schema;

public class Ulangan {
    private String id;
    private double nilai;

    // Default constructor required by Avro
    public Ulangan() {
    }

    // Constructor with parameters
    public Ulangan(String id, double nilai) {
        this.id = id;
        this.nilai = nilai;
    }

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getNilai() {
        return nilai;
    }

    public void setNilai(double nilai) {
        this.nilai = nilai;
    }

    // Add a method to retrieve the Avro schema
    public Schema getSchema() {
        // Define the Avro schema for the Ulangan record
        String avroSchema = "{"
                + "\"type\":\"record\","
                + "\"name\":\"Ulangan\","
                + "\"fields\":["
                + "{\"name\":\"id\",\"type\":\"string\"},"
                + "{\"name\":\"nilai\",\"type\":\"double\"}"
                + "]}";
        return new Schema.Parser().parse(avroSchema);
    }
}
