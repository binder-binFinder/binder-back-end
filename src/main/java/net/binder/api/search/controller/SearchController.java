package net.binder.api.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "쓰레기통 검색")
@RequestMapping("/search/bins")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "좌표 기반 쓰레기통 검색")
    @GetMapping
    public List<SearchResult> searchByCoordinate(
            @RequestParam(value = "longitude") Double longitude,
            @RequestParam(value = "latitude") Double latitude,
            @RequestParam(value = "radius") Integer radius,
            @RequestParam(value = "type", required = false) BinType type,
            @CurrentUser String email) {
        return searchService.searchByCoordinate(type, longitude, latitude, radius, email);
    }

    @Operation(summary = "키워드 기반 쓰레기통 검색")
    @GetMapping("/keyword")
    public List<SearchResult> searchByKeyword(
            @RequestParam(value = "longitude") Double longitude,
            @RequestParam(value = "latitude") Double latitude,
            @RequestParam(value = "targetLongitude") Double targetLongitude,
            @RequestParam(value = "targetLatitude") Double targetLatitude,
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "address") String address,
            @CurrentUser String email) {

        return searchService.searchByKeyword(
                longitude,
                latitude,
                targetLongitude,
                targetLatitude,
                keyword,
                address,
                email
        );
    }
}
