package net.binder.api.search.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.repository.SearchQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchService {

    private final SearchQueryRepository searchQueryRepository;
    private final MemberService memberService;

    public List<SearchResult> search(SearchDto searchDto, String email) {
        if (email == null) {
            return searchQueryRepository.findBins(searchDto, null);
        }
        Member member = memberService.findByEmail(email);
        return searchQueryRepository.findBins(searchDto, member.getId());

    }
}
