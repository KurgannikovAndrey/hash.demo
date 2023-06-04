package com.me.hash.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Data
@Getter
@Setter
@AllArgsConstructor
public class HostRestEntity {
    private String hash;
    private String host;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostRestEntity that = (HostRestEntity) o;
        return Objects.equals(hash, that.hash) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, host);
    }
}
