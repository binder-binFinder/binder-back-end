package net.binder.api.searchlog.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.searchlog.dto.SearchLogItem;
import net.binder.api.searchlog.entity.SearchLog;
import net.binder.api.searchlog.repository.SearchLogRepository;
import net.binder.api.searchlog.repository.SearchLogQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    private final SearchLogQueryRepository searchLogQueryRepository;

    public void createSearchLog(
            Member member,
            String keyword,
            String address,
            List<SearchResult> searchResults
    ) {

        boolean hasBinsNearBy = !searchResults.isEmpty();
        boolean hasBookmarkedBin = searchResults.stream()
                .anyMatch(SearchResult::getIsBookMarked);

        SearchLog searchLog = SearchLog.builder()
                .member(member)
                .keyword(keyword)
                .address(address)
                .hasBinsNearby(hasBinsNearBy)
                .hasBookmarkedBin(hasBookmarkedBin)
                .build();

        searchLogRepository.save(searchLog);
    }

    public List<SearchLogItem> getSearchLog(String email, Long lastSearchLogId) {
        return searchLogQueryRepository
                .findSearchLogByMember(email, lastSearchLogId);
    }

    public void deleteSearchLog(String email, Long searchLogId) {
        SearchLog searchLog = searchLogRepository.findById(searchLogId).orElseThrow(
                () -> new BadRequestException("존재하지 않는 검색 기록입니다.")
        );

        if (!searchLog.isOwnedBy(email)) {
            throw new BadRequestException("해당 회원의 검색 기록이 아닙니다");
        }
        searchLog.softDelete();
    }
}
