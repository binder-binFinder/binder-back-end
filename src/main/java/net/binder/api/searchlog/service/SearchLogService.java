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
            List<SearchResult> searchResults) {
        SearchLog searchLog = buildSearchLog(member, keyword, address, searchResults);
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

        validateSearchLogOwnership(email, searchLog);
        searchLog.softDelete();
    }

    private SearchLog buildSearchLog(Member member, String keyword, String address, List<SearchResult> searchResults) {
        return SearchLog.builder()
                .member(member)
                .keyword(keyword)
                .address(address)
                .hasBinsNearby(!searchResults.isEmpty())
                .hasBookmarkedBin(searchResults.stream().anyMatch(SearchResult::getIsBookMarked))
                .build();
    }

    private void validateSearchLogOwnership(String email, SearchLog searchLog) {
        if (!searchLog.isOwnedBy(email)) {
            throw new BadRequestException("해당 회원의 검색 기록이 아닙니다.");
        }
    }
}
