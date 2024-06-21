# Source Connector

Sumber: https://docs.confluent.io/cloud/current/connectors/cc-postgresql-source.html

Berlainan dari sink connector yang memasukan data dari kafka ke database, source connector berfungsi untuk menjadikan data yang ada di database sebagai sumber data untuk kemudian dimasukan sebagai data ke kafka topic yang dituju. berikut adalah langkah-langkahnya:

1. Saya akan melakukan source connector menggunakan jdbc source connector untuk menghubungkan database postgresql ke kafka, buat terlebih dahulu database postgresql dan buat tabel untuk kemudian diisikan data ke tabel tersebut, saya akan membuat database dengan nama testsource dan membuat tabel komik. Berikut adalah querynya:

```
CREATE TABLE komik (
    id_komik INTEGER PRIMARY KEY,
    judul VARCHAR(255) NOT NULL,
    kategori VARCHAR(100) NOT NULL,
    harga INTEGER NOT null,
    timestamp_komik TIMESTAMP DEFAULT current_timestamp;
);
```

Masukan 20 data berikut :

```
INSERT INTO komik (id_komik, judul, kategori, harga) VALUES
(1, 'Naruto', 'Action', 50000),
(2, 'One Piece', 'Adventure', 55000),
(3, 'Attack on Titan', 'Action', 60000),
(4, 'My Hero Academia', 'Action', 52000),
(5, 'Demon Slayer', 'Action', 58000),
(6, 'Dragon Ball', 'Action', 49000),
(7, 'Fullmetal Alchemist', 'Fantasy', 62000),
(8, 'Death Note', 'Thriller', 53000),
(9, 'Bleach', 'Action', 57000),
(10, 'Fairy Tail', 'Adventure', 54000),
(11, 'Sword Art Online', 'Fantasy', 60000),
(12, 'Tokyo Ghoul', 'Horror', 51000),
(13, 'Hunter x Hunter', 'Adventure', 59000),
(14, 'Black Clover', 'Action', 52000),
(15, 'JoJo Bizarre Adventure', 'Action', 63000),
(16, 'One Punch Man', 'Comedy', 55000),
(17, 'Re:Zero', 'Fantasy', 58000),
(18, 'Toriko', 'Adventure', 50000),
(19, 'Blue Exorcist', 'Action', 51000),
(20, 'The Promised Neverland', 'Thriller', 60000);
```

Pastikan data sudah masuk ke database:

![image](https://github.com/ferdyansahalfariz/belajar-linux/assets/96871156/e59842b3-aa02-4e01-aa76-92764a719cf6)

2. Buat file config untuk membuat connector jdbc source via c3 pada menu connect. berikut adalah isi file confignya:

```
{
  "name": "postgresql-komik-source-connector",
  "config": {
    "value.converter.schema.registry.url": "http://master.k8s.alldataint.com:8081",
    "table.type": "TABLE",
    "key.converter.schema.registry.url": "http://master.k8s.alldataint.com:8081",
    "name": "postgresql-komik-source-connector",
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "key.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "transforms": "createKey, setSchema",
    "transforms.createKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields": "id_komik, timestamp_komik",
    "transforms.setSchema.type": "org.apache.kafka.connect.transforms.SetSchemaMetadata$Value",
    "transforms.setSchema.schema.name": "skemaKomik",
    "connection.url": "jdbc:postgresql://10.100.13.24:5432/testsource",
    "connection.user": "ferdy",
    "connection.password": "ferdy",
    "numeric.precision.mapping": "true",
    "numeric.mapping": "best_fit",
    "dialect.name": "PostgreSqlDatabaseDialect",
    "mode": "timestamp+incrementing",
    "incrementing.column.name": "id_komik",
    "timestamp.column.name": "timestamp_komik",
    "query": "SELECT * FROM (SELECT id_komik,judul,kategori,harga,timestamp_komik FROM komik) AS KOMIK",
    "poll.interval.ms": "5000",
    "batch.max.rows": "5",
    "topic.prefix": "source-komik-postgresql",
    "db.timezone": "Asia/Jakarta"
  }
}
```

pada konfigurasi tersebut, dijelaskan bahwa terdapat kolom yang incremental pada ```id_komik``` dan kolom timestamp yang ada pada ```timestamp_komik```. Kemudian data yang ada di tabel komik akan dimasukan ke topik ```source-komik-postgresql```.

3. Buat topic baru dengan nama ```source-komik-postgresql``` di c3 dengan partisi 1.

4. Import configurasi file yang sebelumnya sudah di buat ke c3 pada cluster connect yang diinginkan, lalu launch untuk menjalankan connectornya sampai muncul keterangan running dan tidak ada error.

5. Jika sudah memastikan tak ada error, buka topic ```source-komik-postgresql``` dan periksa message yang masuk, berikut adalah hasilnya:

![image](https://github.com/ferdyansahalfariz/belajar-linux/assets/96871156/03c7f2a3-7be1-4a38-9779-0055554a5e75)

6. Make sure juga untuk skema yang berhasil di buat, cek pada bagian schema di topic ```source-komik-postgresql```, berikut adalah skema hasil yang diinput berdasarkan konfigurasi source connector:

```
{
  "connect.name": "skemaKomik",
  "fields": [
    {
      "name": "id_komik",
      "type": "int"
    },
    {
      "name": "judul",
      "type": "string"
    },
    {
      "name": "kategori",
      "type": "string"
    },
    {
      "name": "harga",
      "type": "int"
    },
    {
      "default": null,
      "name": "timestamp_komik",
      "type": [
        "null",
        {
          "connect.name": "org.apache.kafka.connect.data.Timestamp",
          "connect.version": 1,
          "logicalType": "timestamp-millis",
          "type": "long"
        }
      ]
    }
  ],
  "name": "skemaKomik",
  "type": "record"
}
```

7. selanjutnya saya akan menginput data baru ke db dengan query dan melihat apakah kafka topic ikut terupdate saat muncul data baru di database. Berikut querynya:

```
INSERT INTO komik (id_komik, judul, kategori, harga) VALUES
(21, 'Chainsaw Man', 'Action', 52000),
(22, 'Jujutsu Kaisen', 'Action', 58000),
(23, 'Vinland Saga', 'Adventure', 55000),
(24, 'Dr. Stone', 'Sci-Fi', 54000),
(25, 'Fire Force', 'Action', 50000),
(26, 'Beastars', 'Drama', 53000),
(27, 'Haikyu!!', 'Sports', 57000),
(28, 'Magi', 'Fantasy', 60000),
(29, 'The Seven Deadly Sins', 'Action', 59000),
(30, 'Mob Psycho 100', 'Comedy', 56000);
```

![image](https://github.com/ferdyansahalfariz/belajar-linux/assets/96871156/74fa133d-8edd-4fb8-998f-24103c4b3ae5)

8. periksa kembali topik, pada gambar dibawah terlihat message berhasil terupdate pada ```offset 29``` dengan ```id_komik = 30``` mengikuti data terakhir yang ada di database.

![image](https://github.com/ferdyansahalfariz/belajar-linux/assets/96871156/6fa009a8-c238-4a85-84ae-774cda58005c)
