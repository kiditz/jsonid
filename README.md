# JsonID Library

* [Overview](#overview) 
* [Semua Kelas](#semua-kelas) 
* [Tipe data primitif](#tipe-data-primitif) 
* [Serialize Obyek Otomatis](#serialize-obyek-otomatis)
* [Deserialize Obyek Otomatis](#deserialize-obyek-otomatis)
* [Serialize Obyek Manual](#serialize-obyek-manual)
* [Deserialize Obyek Manual](#deserialize-obyek-manual)
* [Serialize dengan Dom Element](#serialize-dengan-dom-element)
# Overview
Framework java untuk serialize dan deserilize Plain Old Java Object (POJO) kedalam format json maupun sebaliknya.
untuk dapat menggunakan jsonid anda dapat menemukannya di [Maven central repository](https://mvnrepository.com/artifact/com.github.kiditz/jsonid/1.0.0).

# Semua Kelas
* JsonElement
* JsonID
* JsonKesalahan
* JsonKosong
* JsonLarik
* JsonNilai
* JsonObyek
* JsonParser
* JsonPembaca
* JsonPenulis


# Tipe data primitif
```java
JsonID jsonID = JsonID.baru().aktifkanSpasi();
jsonID.keJson(1);//int
jsonID.keJson(0.177f);//float
jsonID.keJson(Long.MAX_VALUE);//long
jsonID.keJson(Short.MAX_VALUE);//long
jsonID.keJson(new int[]{1, 2, 3, 4, 5});//integer array
jsonID.keJson(new float[]{1.1f, 2.1f, 3.1f, 4.3f, 5.2f});//float array
jsonID.keJson(new boolean[]{true, false, false, true})//
```
# Serialize Obyek Otomatis

__Note:__ *JsonID menggunakan reflection untuk dapat melakukan serialize obyek secara otomatis*

```java
class User{
  private String username;
  private String password;
  //Getter And Setter
}
```
__*Menggunakan kelas User*__
```java
User user = new User();
user.setUsername("kiditz");
user.setPassword("ganteng");
```
__*Serialize Plain Old Java Object ke json*__
```java
JsonID jsonID = JsonID.baru();
System.out.println(jsonID.keJson(user));

//Hasil
{"username":"kiditz","password":"ganteng"}
```
__Hal tersebut dapat digunakan tapi sangat sulit untuk dibaca, untuk dapat memudahkan pembacaan maka anda perlu mengaktifkan spasi yang dapat anda aktifkan dengan__
```java
JsonID jsonID = JsonID.baru().aktifkanSpasi();
System.out.println(jsonID.keJson(user));

//Hasil
{
  "username": "kiditz",
  "password": "ganteng",
}

```
__*Serialize Larik*__

* Pertama tama ubah kelas user menjadi seperti ini

```java
static class User{
  private String username;
  private String password;
  private Larik skills = new Larik();
  //Getter dan Setter
}

```

* Lalu...

Bhang!! jsonid akan menambahkan tag yang berupa nama kelas yang di gunakan untuk membentuk larik saat ia tidak di inisialisasi dengan kelas yang seharusnya. selain menggunakan kelas larik anda juga dapat menggunakan [ArrayList](http://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html), [Set](https://docs.oracle.com/javase/7/docs/api/java/util/Set.html), dan kelas-kelas lain yang memiliki sifat [Collection](https://docs.oracle.com/javase/7/docs/api/java/util/Collection.html). juga dapat melakukan serialize terhadap kelas-kelas yang memiliki sifat [Map](https://docs.oracle.com/javase/7/docs/api/java/util/Map.html) atau dapat juga menggunakan kelas kustom ```PetaObyek``` yang sudah di sediakan oleh JsonID sejak versi 1.0.0

```java
User user = new User();
user.setUsername("kiditz");
user.setPassword("ganteng");
user.setSkills(new Larik(new Skill("Java", "Android"),new Skill("Java", "J2EE"), new Skill("Java", "J2SE")));
JsonID jsonID = JsonID.baru().aktifkanSpasi();
System.out.println(jsonID.keJson(user));
//Hasil
{
  "username": "kiditz",
  "password": "ganteng",
  "skills": [
    {
      "kelas": "org.raden.json.test.JsonIdTest$Skill",
      "programming": "Java",
      "operatingSystem": "Android"
    },
    {
      "kelas": "org.raden.json.test.JsonIdTest$Skill",
      "programming": "Java",
      "operatingSystem": "J2EE"
    },
    {
      "kelas": "org.raden.json.test.JsonIdTest$Skill",
      "programming": "Java",
      "operatingSystem": "J2SE"
    }
  ]
}
```
Untuk dapat memperbaiki hal ini maka kita perlu mengatur tipe element yang dapat membuat ulang obyek larik agar memiliki  argument seperti ```Larik<Skill>``` dimana field yang ditulis di dalam kelas user harus sama dengan parameter nama field yang di tentukan saat menggunakan method aturTipeElement();

```java
JsonID jsonID = JsonID.baru().aktifkanSpasi().aturTipeElement(User.class, "skills", Skill.class);
System.out.println(jsonID.keJson(user));
//Hasil
{
  "username": "kiditz",
  "password": "ganteng",
  "skills": [
    {
      "programming": "Java",
      "operatingSystem": "Android"
    },
    {
      "programming": "Java",
      "operatingSystem": "J2EE"
    },
    {
      "programming": "Java",
      "operatingSystem": "J2SE"
    }
  ]
}
```
# Deserialize Obyek Otomatis

* Obyek Deserialize 

```java
User user = new User();
user.setUsername("kiditz");
user.setPassword("ganteng");
user.setSkills(new Larik(new Skill("Java", "Android"),new Skill("Java", "J2EE"), new Skill("Java", "J2SE")));
JsonID jsonID = JsonID.baru().aktifkanSpasi().aturTipeElement(User.class, "skills", Skill.class);
String out = jsonID.keJson(user);
System.out.println(out);
User user2 = jsonID.dariJson(out, User.class);
System.out.println(user2);

{
  "username": "kiditz",
  "password": "ganteng",
  "skills": [
    {
      "programming": "Java",
      "operatingSystem": "Android"
    },
    {
      "programming": "Java",
      "operatingSystem": "J2EE"
    },
    {
      "programming": "Java",
      "operatingSystem": "J2SE"
    }
  ]
}
//Hasil deserialize
User [username=kiditz, password=ganteng, skills=[Skill [programming=Java, operatingSystem=Android], Skill [programming=Java, operatingSystem=J2EE], Skill [programming=Java, operatingSystem=J2SE]]]
```
__Note :__ *Obyek* ```dariJson``` *merupakan root untuk dapat melakukan pembacaan json yang akan  membaca semua bidang ataupun semua benda yang ditemui dalam file berformat json secara recrusive. tipe diketahui dan tipe element juga dapat digunakan oleh method ```toJson``` . hal ini hanya akan berguna jika jenis json benar-benar diketahui, jika takut kelas tersebut ambigu dengan nilai json, kita dapat menginisialisasikannya dengan tipe Object . 
*
Sebagai contoh :
```java
User user = new User();
user.setUsername("kiditz");
user.setPassword("ganteng");
user.setSkills(new Larik(new Skill("Java", "Android"),new Skill("Java", "J2EE"), new Skill("Java", "J2SE")));

//May be user come from internet and it is unknown json format because you only read that from Http Method
JsonID jsonID = JsonID.baru().aktifkanSpasi().aturTipeElement(User.class, "skills", Skill.class);
String out = jsonID.keJson(user, Object.class);
System.out.println(out);
Object object = jsonID.dariJson(out, Object.class);
System.out.println(object);
```
# Serialize Obyek Manual
Untuk menulis json dengan menggunakan metode dom seperti file xml maka dapat menggunakan ```JsonPenulis``` untuk dapat membuat map, nilai, larik dan lain-lain.
```java
StringWriter writer = new StringWriter();
JsonPenulis penulis = new JsonPenulis(writer);
penulis.mulaiObyek();
penulis.nama("username").nilai("kiditz");
penulis.nama("binatang");
penulis.mulaiLarik();
penulis.nilai("gajah").nilai("buaya").nilai("baygon").nilai("sianida");
penulis.akhirLarik();
penulis.akhirObyek();
System.out.println(writer.toString());
String json = writer.toString();
//Hasil 
{"username":"kiditz","binatang":["gajah","buaya","baygon","sianida"]}
```
# Deserialize Obyek Manual
```java
JsonPembaca pembaca = new JsonPembaca(new StringReader(json));
JsonParser parser = new JsonParser();
JsonElement element = parser.parse(pembaca);
if(element.iniObyek()){
	JsonObyek obyek = element.sebagaiObyek();
	System.out.println(obyek.raih("username"));
  JsonLarik larik = obyek.raih("binatang").sebagaiLarik();
  int i = 1;
  for (JsonElement element2 : larik) {
     System.out.print(element2.sebagaiString());
     if (i++ != larik.ukuran()) {
       System.out.print(", ");
     }
	}
}
// Hasil

"kiditz"
gajah
buaya
baygon
sianida
```
# Serialize dengan Dom Element

* JsonObyek
```java
JsonObyek obyek = JsonObyek.baru();
obyek.tambah(String.class.getName(), new JsonNilai(String.format("%s", "this is from string")));
obyek.tambah(Integer.class.getName(), new JsonNilai(Integer.MAX_VALUE));
obyek.tambah(Short.class.getName(), new JsonNilai(Short.MAX_VALUE));
obyek.tambah(Boolean.class.getName(), new JsonNilai(false));
obyek.tambah(Double.class.getName(), new JsonNilai(Double.MAX_EXPONENT));
obyek.tambah(Long.class.getName(), new JsonNilai(Long.MAX_VALUE));
obyek.tambah(Float.class.getName(), new JsonNilai(Float.MAX_VALUE));
obyek.tambah(Byte.class.getName(), new JsonNilai(Byte.toString("this is byte roger".getBytes()[0])));
System.out.println(obyek.toString());
//Hasil
{
  "java.lang.String": "this is from string",
  "java.lang.Integer": 2147483647,
  "java.lang.Short": 32767,
  "java.lang.Boolean": "false",
  "java.lang.Double": 1023,
  "java.lang.Long": 9223372036854775807,
  "java.lang.Float": 3.4028235E38,
  "java.lang.Byte": "116"
}
```

* JsonLarik
```java
JsonLarik larik = JsonLarik.baru();
larik.tambah(true);
larik.tambah("hei");
larik.tambah(Float.MAX_VALUE);
larik.tambah(Integer.MAX_VALUE);
larik.tambah(Long.MAX_VALUE);
larik.tambah(Short.MAX_VALUE);
larik.tambah(Byte.MAX_VALUE);
larik.tambah(Double.MAX_VALUE);

//Hasil
[
  "true",
  "hei",
  3.4028235E38,
  2147483647,
  9223372036854775807,
  32767,
  127,
  1.7976931348623157E308
]
```
*__Note__ * Untuk dapat membaca dengan menggunakan dom maka anda dapat melihat kembali penjelasan mengenai ```JsonParser dan JsonPembaca``` yang sudah di jelaskan sebelumnya.

