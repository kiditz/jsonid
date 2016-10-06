# JsonID Library

* [<b>Overview</b>](#overview) 
* [<b>Tipe data primitif</b>](#tipe-data-primitif) 

##<h1>Overview </h1>
Framework java untuk serialize dan deserilize json.
untuk menggunakan jsonid anda dapat menemukannya di maven central repository(https://mvnrepository.com/artifact/com.github.kiditz/jsonid/1.0.0).

* JsonElement
* JsonID
* JsonKesalahan
* JsonKosong
* JsonLarik
* JsonNilai
* JsonObyek
* JsonParser


##<h1>Tipe data primitif</h1>
```java
JsonID jsonID = JsonID.baru().aktifkanSpasi();
StringBuilder builder = new StringBuilder();
builder.append(jsonID.keJson(1)).append("\n");
builder.append(jsonID.keJson(1.11f)).append("\n");
builder.append(jsonID.keJson(new int[]{1, 2, 3, 4, 5}));
System.out.println(builder.toString());
```




