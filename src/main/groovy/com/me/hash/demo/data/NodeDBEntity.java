package com.me.hash.demo.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "nodes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NodeDBEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String host;
    private String hash;
    private String special;

    public static NodeDBEntity copy(NodeDBEntity toCopy){
        var result = new NodeDBEntity();
        result.hash = toCopy.getHash();
        result.host = toCopy.getHost();
        result.special = toCopy.getSpecial();
        return result;
    }

    @Override
    public String toString() {
        return "NodeDBEntity{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", hash='" + hash + '\'' +
                ", special='" + special + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDBEntity that = (NodeDBEntity) o;
        return id == that.id && Objects.equals(host, that.host) && Objects.equals(hash, that.hash) && Objects.equals(special, that.special);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, hash, special);
    }
}
