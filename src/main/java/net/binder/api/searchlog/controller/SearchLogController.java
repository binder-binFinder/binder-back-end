package net.binder.api.searchlog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.searchlog.dto.SearchLogItem;
import net.binder.api.searchlog.service.SearchLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/searchlog")
@Tag(name = "검색 로그")
@RestController
public class SearchLogController {

    private final SearchLogService searchLogService;

    @Operation(summary = "검색 로그 조회")
    @GetMapping
    public List<SearchLogItem> getSearchLog(
            @RequestParam(value = "lastSearchLogId", required = false) Long lastSearchLogId,
            @CurrentUser String email){
        return searchLogService.getSearchLog(email, lastSearchLogId);
    }

    @Operation(summary = "검색 로그 삭제")
    @DeleteMapping("/{id}")
    public void deleteSearchLog(
            @PathVariable("id") Long searchLogId,
            @CurrentUser String email){
        searchLogService.deleteSearchLog(email, searchLogId);
    }
}
