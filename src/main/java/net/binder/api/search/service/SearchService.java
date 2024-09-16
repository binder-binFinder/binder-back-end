package net.binder.api.search.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.exception.BadRequestException;
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

    public List<SearchResult> search(
            BinType bintype,
            Double longitude,
            Double latitude,
            int radius,
            String email) {

        if(radius<=0){
            throw new BadRequestException("잘못된 반경 설정입니다.");
        }

        //한국의 경도는 124도에서 132도, 위도는 33~ 43도

        if (longitude < 124 || longitude > 133 || latitude < 33 || latitude > 44) {
            throw new BadRequestException("잘못된 좌표입니다.");
        }

        int radiusToUse = radius;

        if(radius<100){
            radiusToUse = 100;

        } else if(radiusToUse > 500){
            radiusToUse = 500;
        }

        SearchDto searchDto = new SearchDto(bintype, longitude, latitude, radiusToUse);

        if (email == null) {
            return searchQueryRepository.findBins(searchDto, null);
        }

        Member member = memberService.findByEmail(email);
        return searchQueryRepository.findBins(searchDto, member.getId());
    }
}
