package com.me.hash.demo.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "resources_mapping")
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMappingDBEntity {
    @Override
    public String toString() {
        return "ResourceMappingDBEntity{" + "id=" + id + ", host='" + host + "\'" + ", hash='" + hash + "\'" + "}";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private String hash;
}
