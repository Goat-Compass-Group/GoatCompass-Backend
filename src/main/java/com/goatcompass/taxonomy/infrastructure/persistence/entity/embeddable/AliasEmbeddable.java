package com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AliasEmbeddable {
    @Column(nullable = false)
    private String alias;
    @Column(nullable = false)
    private String normalized;
    @Column(nullable = false, length = 5)
    private String lang;
}
