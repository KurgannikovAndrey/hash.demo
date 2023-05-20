package com.me.hash.demo.entity;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRestEntity {
    private String hash;
    private String host;
    private String name;
}
