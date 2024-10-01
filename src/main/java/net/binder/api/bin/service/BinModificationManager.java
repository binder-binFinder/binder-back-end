package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.common.exception.BadRequestException;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class BinModificationManager {

    public void approve(BinModification binModification) {

        validateModificationStatus(binModification);

        Bin target = binModification.getBin();

        Point newPoint = PointUtil.getPoint(binModification.getLongitude(), binModification.getLatitude());

        target.update(binModification.getTitle(), binModification.getType(), newPoint, binModification.getAddress(),
                binModification.getImageUrl());

        binModification.approve();
    }

    public void reject(BinModification binModification) {

        validateModificationStatus(binModification);

        binModification.reject();
    }

    private void validateModificationStatus(BinModification binModification) {
        if (!binModification.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
