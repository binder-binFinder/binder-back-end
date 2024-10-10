package net.binder.api.filtering.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "word_idx", columnList = "word", unique = true))
public class Curse extends BaseEntity {

    private String word;

    public Curse(String word) {
        this.word = word;
    }
}
