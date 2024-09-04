package net.binder.api.bin.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name= "sameBin",
                        columnNames={"address", "type"}
                )
        }
)
public class Bin extends BaseEntityWithSoftDelete {

    private String title;

    @Enumerated(EnumType.STRING)
    private BinType type;

    private Point point;

    private String address;

    private long matchCount;

    private long mismatchCount;

    private String imageUrl;

    private LocalDateTime deletedAt;

    @Builder
    public Bin(String title, BinType type, Point point, String address, long matchCount, long mismatchCount,
               String imageUrl) {
        this.title = title;
        this.type = type;
        this.point = point;
        this.address = address;
        this.matchCount = matchCount;
        this.mismatchCount = mismatchCount;
        this.imageUrl = imageUrl;
    }

    public boolean softDelete() {
        if (deletedAt != null) {
            return false;
        }
        deletedAt = LocalDateTime.now();

        return true;
    }

    public void update(String title, BinType type, Point point, String address, String imageUrl) {
        this.title = title;
        this.type = type;
        this.point = point;
        this.address = address;
        this.imageUrl = imageUrl;
    }
}
