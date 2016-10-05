# jsonid
Java Framework to serialize and deserialize json

```java
JsonID jsonID = JsonID.baru();
jsonID.keJson(1); \\ hasil 1
jsonID.keJson("Hello dunia")\\ hasil  “Hello Dunia”
jsonID.keJson(new Float(10))\\ hasil 10.0
jsonID.keJson(new int[] { 1, 2, 3, 4, 5 })\\hasil [1,2,3,4,5]
```
