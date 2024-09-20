package net.binder.api.bookmark.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.dto.CreateBookmarkRequest;
import net.binder.api.bookmark.service.BookmarkService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookmarks")
@Tag(name = "쓰레기통 북마크")
public class BookmarkController {

    private final BookmarkService bookMarkService;

    @Operation(summary = "쓰레기통 북마크 지정")
    @PostMapping
    public void createBookmark(@CurrentUser String email, @Valid @RequestBody CreateBookmarkRequest createBookmarkRequest) {
         bookMarkService.createBookMark(email, createBookmarkRequest.getBinId());
    }

    @Operation(summary = "쓰레기통 북마크 해제")
    @DeleteMapping("/{id}")
    public void deleteBookmark(@CurrentUser String email, @PathVariable long id) {
        bookMarkService.deleteBookMark(email, id);
    }

    @Operation(summary = "쓰레기통 북마크 목록 조회")
    @GetMapping
    public List<BookmarkResponse> getBookmarks(@CurrentUser String email,
                                               @RequestParam Double longitude,
                                               @RequestParam Double latitude){

       return bookMarkService.getBookmarks(email, longitude, latitude);
    }
}
