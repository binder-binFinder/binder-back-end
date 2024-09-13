package net.binder.api.search.dto;

import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class SearchResult {

    private final Long id;

    private final String address;

    private final String title;

    private final BinType type;

    private final Point point;

    private final Boolean isBookMarked;

    private final Double distance;

}
