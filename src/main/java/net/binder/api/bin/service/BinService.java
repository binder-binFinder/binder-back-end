package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.auth.dto.CustomOAuth2User;
import net.binder.api.bin.dto.BinSave;
import net.binder.api.bin.dto.BinUpdate;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.membercreatebin.service.MemberCreateBinService;
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
    private final MemberService memberService;
    private final MemberCreateBinService memberCreateBinService;

    public Long save(BinSave binSave, CustomOAuth2User customOAuth2User){
        BinType type = BinType.getType(binSave.getType());
        Point point = getPoint(binSave.getLatitude(), binSave.getLongitude());

        Bin bin = Bin.builder()
                .title(binSave.getTitle())
                .address(binSave.getAddress())
                .type(type)
                .imageUrl(binSave.getImageUrl())
                .point(point)
                .build();

        Bin binEntity = null;
        try {
         binEntity = binRepository.save(bin);
        } catch (DataIntegrityViolationException e){
            throw new BadRequestException("이미 등록된 쓰레기통입니다.");

        }
        Member memberEntity = memberService.findByEmail(customOAuth2User.getName());
        memberCreateBinService.save(binEntity, memberEntity);

        return binEntity.getId();
    }

    @Transactional(readOnly = true)
    public Bin findById(Long id){
        return binRepository.findByIdAndNotDeleted(id).
                orElseThrow(() ->
                        new NotFoundException("존재하지 않는 쓰레기통입니다."));
    }

    public void delete(Long id){
        Bin bin = findById(id);
        boolean deleted = bin.softDelete();

        if (!deleted) {
            throw new BadRequestException("이미 삭제한 쓰레기통입니다.");
        }
    }

    public void update(long id, BinUpdate binUpdate){
        BinType type = BinType.getType(binUpdate.getType());
        Bin bin = binRepository.findByIdAndNotDeleted(id).
                orElseThrow(() ->
                        new NotFoundException("존재하지 않는 쓰레기통입니다."));

        Point point = getPoint(binUpdate.getLatitude(), binUpdate.getLongitude());
        bin.update(binUpdate.getTitle(), type, point, binUpdate.getAddress(), binUpdate.getImageUrl());
    }

    private Point getPoint(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(latitude, longitude);
        Point point = geometryFactory.createPoint(coordinate);
        return point;
    }
}
