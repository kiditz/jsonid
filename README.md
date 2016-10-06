# JsonID Library

* [Overview](#overview) 
* [Semua Kelas](#semua-kelas) 
* [Tipe data primitif](#tipe-data-primitif) 
* [Serialize Obyek Otomatis](#serialize-obyek-otomatis)

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

Bhang!! jsonid akan menambahkan tag yang berupa nama kelas yang di gunakan untuk membentuk larik saat ia tidak di inisialisasi dengan kelas yang seharusnya. selain menggunakan kelas larik anda juga dapat menggunakan [ArrayList](http://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html), [Set](https://docs.oracle.com/javase/7/docs/api/java/util/Set.html), dan kelas-kelas lain yang memiliki sifat [Collection](https://docs.oracle.com/javase/7/docs/api/java/util/Collection.html)
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
Hal ini dapat ditangani secara mudah dengan json id jika obyek tersebut hanya berupa satu obyek maka dapat ditangani dengan 
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
# Menulis dengan Json Obyek
```java
JsonObyek obyek = JsonObyek.baru();
obyek.tambah("username", new JsonNilai("kiditzs"));
obyek.tambah("phone", new JsonNilai("0877-8874-4374"));
obyek.tambah("skills", new JsonNilai("Java Programmers"));
System.out.println(obyek.toString());
```

