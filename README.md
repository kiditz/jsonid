# JsonID Library

* [<b>Overview</b>](#overview) 
* [<b>Tipe data primitif</b>](#tipe-data-primitif) 

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
* JsonParser
* JsonParser


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
# Serialize dan Deserialize Obyek Otomatis
__Note:__ *JsonID menggunakan reflection untuk dapat melakukan serialize obyek secara otomatis*
```java
class User{
  private String username;
  private String password;
}
```
__*Menggunakan kelas User*__
```java
User user = new User();
user.setUsername("kiditz");
user.setPassword("ganteng");
```
__*Serialize User ke json*__
```java
JsonID jsonID = JsonID.baru();
System.out.println(jsonID.keJson(user));
{"username":"kiditz","password":"ganteng"}
```
__Hal tersebut dapat digunakan tapi sangat sulit untuk dibaca, untuk dapat memudahkan pembacaan maka anda perlu mengaktifkan spasi yang dapat anda aktifkan dengan__
```java
JsonID jsonID = JsonID.baru().aktifkanSpasi();
System.out.println(jsonID.keJson(user));

```
__*Serialize Larik*__


# Menulis dengan Json Obyek
```java
JsonObyek obyek = JsonObyek.baru();
obyek.tambah("username", new JsonNilai("kiditzs"));
obyek.tambah("phone", new JsonNilai("0877-8874-4374"));
obyek.tambah("skills", new JsonNilai("Java Programmers"));
System.out.println(obyek.toString());
```

