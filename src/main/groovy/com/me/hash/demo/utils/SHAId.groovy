package com.me.hash.demo.utils

import java.security.MessageDigest
import java.util.function.Predicate

class SHAId {
    private String hash
    private static BigInteger MAX_VALUE = BigInteger.TWO.pow(160)

    public static SHAId from(String hash) {
        def result = new SHAId()
        result.hash = hash
        result
    }

    public static SHAId of(String str) {
        def md = MessageDigest.getInstance("SHA-1")
        def preHash = new String(md.digest(str.bytes))
        def modHash = new BigInteger(preHash.bytes).abs() % MAX_VALUE
        from(new String((modHash).toByteArray()))
    }

    private SHAId() {

    }

    String getHash() {
        return hash
    }

    public static SHAId sum(String hash, int twoPow) {
        //println "debug sum"
        def a = new BigInteger(hash.bytes).abs()
        def b = BigInteger.TWO.pow(twoPow)
        def sum = (a + b) % MAX_VALUE
        //println "a : ${a} b : ${b} sum : ${sum}"
        from(new String((sum).toByteArray()))
    }

    public static SHAId fromTransportBytesRepresentation(String representation){
        def bytes = representation.replaceAll("[\\[\\s\\]]", "").split(",").collect{Byte.valueOf(it)}.toArray() as byte[]
        from(new String(bytes))
    }

    public static BigInteger toBInt(String hash) {
        new BigInteger(hash.bytes).abs()
    }

    @Override
    public String toString() {
        return "SHAId{int = ${new BigInteger(hash.bytes).abs()}";
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SHAId shaId = (SHAId) o

        if (hash != shaId.hash) return false

        return true
    }

    int hashCode() {
        return (hash != null ? hash.hashCode() : 0)
    }
}
