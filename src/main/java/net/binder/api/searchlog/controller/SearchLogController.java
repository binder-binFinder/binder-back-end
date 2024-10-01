package net.binder.api.searchlog.controller;

import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.searchlog.dto.SearchLogItem;
import net.binder.api.searchlog.service.SearchLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/searchlog")
@RestController
public class SearchLogController {

    private final SearchLogService searchLogService;

    @GetMapping
    public List<SearchLogItem> getSearchLog(
            @RequestParam(value = "lastSearchLogId", required = false) Long lastSearchLogId,
            @CurrentUser String email){
        return searchLogService.getSearchLog(email, lastSearchLogId);
    }

    @DeleteMapping("/{id}")
    public void deleteSearchLog(
            @PathVariable("id") Long searchLogId,
            @CurrentUser String email){
        searchLogService.deleteSearchLog(email, searchLogId);
    }
}
