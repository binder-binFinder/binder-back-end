package net.binder.api.bin.entity;

import static net.binder.api.bin.entity.BinRegistrationStatus.PENDING;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import net.binder.api.member.entity.Member;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "sameBin",
                        columnNames = {"address", "type", "title"}
                )
        },
        indexes = {
                @Index(name = "idx_bin_point", columnList = "point")
        }
)
@ToString
public class Bin extends BaseEntityWithSoftDelete {

    private String title;

    @Enumerated(EnumType.STRING)
    private BinType type;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point point;

    private String address;

    private Long likeCount;

    private Long dislikeCount;

    private Long bookmarkCount;

    private String imageUrl;

    @OneToOne(mappedBy = "bin", cascade = CascadeType.ALL)
    private BinRegistration binRegistration;

    @Builder
    public Bin(String title, BinType type, Point point, String address, Long likeCount,
               Long dislikeCount,
               Long bookmarkCount, String imageUrl, BinRegistration binRegistration) {
        this.title = title;
        this.type = type;
        this.point = point;
        this.address = address;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.bookmarkCount = bookmarkCount;
        this.imageUrl = imageUrl;
        this.binRegistration = binRegistration;
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

    public void increaseBookmark() {
        this.bookmarkCount++;
    }

    public void decreaseBookmark() {
        if (this.bookmarkCount <= 0) {
            return;
        }
        this.bookmarkCount--;
    }

    public boolean isOwner(Member member) {
        return this.binRegistration.getMember().equals(member);
    }

    public void setBinRegistration(BinRegistration binRegistration) {
        this.binRegistration = binRegistration;
        binRegistration.setBin(this);
    }

    public boolean isPending() {
        return this.binRegistration.getStatus() == PENDING;
    }
}
