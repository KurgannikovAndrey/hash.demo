package com.me.hash.demo.utils

class SHAIdComparator implements Comparator<SHAId> {

    @Override
    public int compare(SHAId o1, SHAId o2) {
        def sha1 = new BigInteger(o1.hash.bytes).abs()
        def sha2 = new BigInteger(o2.hash.bytes).abs()
        sha1 == sha2 ? 0 : sha1 > sha2 ? 1 : -1
    }
}
