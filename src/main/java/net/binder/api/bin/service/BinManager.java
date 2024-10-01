package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.util.PointUtil;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class BinManager {

    public void update(Bin bin, BinUpdateRequest request) {
        Point newPoint = PointUtil.getPoint(request.getLongitude(), request.getLatitude());

        bin.update(request.getTitle(), request.getType(), newPoint, request.getAddress(), request.getImageUrl());
    }
}
