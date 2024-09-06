package net.binder.api.bin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
                        name = "sameBin",
                        columnNames = {"address", "type"}
                )
        }
)
public class Bin extends BaseEntityWithSoftDelete {

    private String title;

    @Enumerated(EnumType.STRING)
    private BinType type;

    private Point point;

    private String address;

    private Long likeCount;

    private Long dislikeCount;

    private Long bookmarkCount;

    private String imageUrl;

    private LocalDateTime deletedAt;

    @Builder
    public Bin(String title, BinType type, Point point, String address, Long likeCount, Long dislikeCount,
               Long bookmarkCount, String imageUrl) {
        this.title = title;
        this.type = type;
        this.point = point;
        this.address = address;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.bookmarkCount = bookmarkCount;
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

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        this.likeCount--;
    }

    public void increaseDislike() {
        this.dislikeCount++;
    }

    public void decreaseDisLike() {
        this.dislikeCount--;
    }
}
