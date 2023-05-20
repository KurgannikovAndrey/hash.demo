package com.me.hash.demo.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "resources")
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDBEntity {
    @Override
    public String toString() {
        return "ResourceDBEntity{" + "id=" + id + ", path='" + path + "\'" + ", name='" + name + "\'" + ", hash='" + hash + "\'" + "}";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private String path;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String hash;
}
