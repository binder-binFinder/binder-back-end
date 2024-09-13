package net.binder.api.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.service.SearchService;
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

    @Operation(summary = "쓰레기통 검색")
    @GetMapping
    public List<SearchResult> search(
            @RequestParam(value = "longitude") Double longitude,
            @RequestParam(value = "latitude") Double latitude,
            @RequestParam(value = "radius") Integer radius,
            @RequestParam(value = "type", required = false) BinType type,
            @CurrentUser String email) {

        return searchService.search(new SearchDto(type, longitude, latitude, radius),email);
    }
}
