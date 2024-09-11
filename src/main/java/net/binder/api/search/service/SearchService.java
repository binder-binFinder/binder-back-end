package net.binder.api.search.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.repository.SearchQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int DEFAULT_RADIUS = 200;
    private final SearchQueryRepository searchQueryRepository;

    public List<SearchResult> search(SearchDto searchDto, Long memberId){
        return searchQueryRepository.findBins(searchDto, DEFAULT_RADIUS, memberId);

    }
}
