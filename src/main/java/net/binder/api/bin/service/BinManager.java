package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.util.PointUtil;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class BinManager {

    public void update(Bin bin, String title, BinType type, String address, Double longitude, Double latitude,
                       String imageUrl) {
        Point newPoint = PointUtil.getPoint(longitude, latitude);

        bin.update(title, type, newPoint, address, imageUrl);
    }
}
