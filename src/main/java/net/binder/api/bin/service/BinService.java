package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.dto.BinCreateRequest;
import net.binder.api.bin.dto.BinDetailResponseForLoginUser;
import net.binder.api.bin.dto.BinUpdate;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinDetailProjection;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BinService {

    private final BinRepository binRepository;

    private final BinRegistrationRepository binRegistrationRepository;

    private final MemberService memberService;

    public Long create(BinCreateRequest binCreateRequest, String email) {
        Member member = memberService.findByEmail(email);
        BinType type = BinType.getType(binCreateRequest.getType());
        Point point = getPoint(binCreateRequest.getLatitude(), binCreateRequest.getLongitude());
        Bin bin = getBin(binCreateRequest, type, point);

        try {
            binRepository.save(bin);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("이미 등록된 쓰레기통입니다.");

        }
        BinRegistration binRegistration = getBuildRegistration(member, bin);
        binRegistrationRepository.save(binRegistration);

        return bin.getId();
    }

    @Transactional(readOnly = true)
    public BinDetailResponseForLoginUser findByIdForLoginUser(Member member, Long bindId) {

        BinDetailProjection projection = binRepository.findDetailByIdAndMemberIdNative(bindId, member.getId()).
                orElseThrow(() ->
                        new NotFoundException("존재하지 않는 쓰레기통입니다."));

        return new BinDetailResponseForLoginUser(
                projection.getId(),
                projection.getCreatedAt(),
                projection.getModifiedAt(),
                projection.getTitle(),
                projection.getType(),
                projection.getLatitude(),
                projection.getLongitude(),
                projection.getAddress(),
                projection.getLikeCount(),
                projection.getDislikeCount(),
                projection.getImageUrl(),
                projection.getIsLiked() == 1,
                projection.getIsDisliked() == 1,
                projection.getIsBookmarked() == 1
        );
    }

    @Transactional(readOnly = true)
    public Bin findById(Long bindId) {

        return binRepository.findByIdAndNotDeleted(bindId).
                orElseThrow(() ->
                        new NotFoundException("존재하지 않는 쓰레기통입니다."));
    }

    public void delete(Long binId) {
        Bin bin = binRepository.findById(binId).orElseThrow(() ->
                new NotFoundException("존재하지 않는 쓰레기통입니다."));
        boolean deleted = bin.softDelete();

        if (!deleted) {
            throw new BadRequestException("이미 삭제한 쓰레기통입니다.");
        }
    }

    public void update(long id, BinUpdate binUpdate) {
        BinType type = BinType.getType(binUpdate.getType());
        Bin bin = binRepository.findByIdAndNotDeleted(id).
                orElseThrow(() ->
                        new NotFoundException("존재하지 않는 쓰레기통입니다."));

        Point point = getPoint(binUpdate.getLatitude(), binUpdate.getLongitude());
        bin.update(binUpdate.getTitle(), type, point, binUpdate.getAddress(), binUpdate.getImageUrl());
    }

    private Bin getBin(BinCreateRequest binCreateRequest, BinType type, Point point) {
        return Bin.builder()
                .title(binCreateRequest.getTitle())
                .address(binCreateRequest.getAddress())
                .type(type)
                .imageUrl(binCreateRequest.getImageUrl())
                .likeCount(0L)
                .dislikeCount(0L)
                .bookmarkCount(0L)
                .point(point)
                .build();
    }

    private Point getPoint(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(latitude, longitude);
        return geometryFactory.createPoint(coordinate);
    }

    private BinRegistration getBuildRegistration(Member member, Bin bin) {
        return BinRegistration.builder()
                .member(member)
                .bin(bin)
                .status(BinRegistrationStatus.PENDING)
                .build();
    }
}
