package com.me.hash.demo.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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
}
