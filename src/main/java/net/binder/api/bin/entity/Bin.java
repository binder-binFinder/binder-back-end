package net.binder.api.bin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntity;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bin extends BaseEntity {

    private String title;

    @Enumerated(EnumType.STRING)
    private BinType type;

    private Point point;

    private String address;

    private long match_count;

    private long mismatch_count;

    private String image_url;

    @Builder
    public Bin(String title, BinType type, Point point, String address, long match_count, long mismatch_count,
               String image_url) {
        this.title = title;
        this.type = type;
        this.point = point;
        this.address = address;
        this.match_count = match_count;
        this.mismatch_count = mismatch_count;
        this.image_url = image_url;
    }
}
