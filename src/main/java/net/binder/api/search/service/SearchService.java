package net.binder.api.search.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.search.dto.SearchRequest;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.repository.SearchQueryRepository;
import net.binder.api.searchlog.service.SearchLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchService {

    private final SearchLogService searchLogService;

    private final SearchQueryRepository searchQueryRepository;

    private final MemberService memberService;

    public List<SearchResult> searchByCoordinate(
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

        SearchRequest searchRequest = new SearchRequest(bintype, longitude, latitude, radiusToUse);

        if (email == null) {
            return searchQueryRepository.findBins(searchRequest, null);
        }

        Member member = memberService.findByEmail(email);

        return searchQueryRepository.findBins(searchRequest, member.getId());
    }

    public List<SearchResult> searchByKeyword(
            Double longitude,
            Double latitude,
            Double targetLongitude,
            Double targetLatitude,
            String keyword,
            String address,
            String email) {

        //한국의 경도는 124도에서 132도, 위도는 33~ 43도
        if (longitude < 124 || longitude > 133 || latitude < 33 || latitude > 44) {
            throw new BadRequestException("서비스 불가능 지역에서 온 요청입니다");
        }

        if (targetLongitude < 124 || targetLongitude > 133 || targetLatitude < 33 || targetLatitude > 44) {
            throw new BadRequestException("잘못된 좌표입니다.");
        }

        if(email == null){
            return searchQueryRepository.findBinsByKeyword(
                    longitude,
                    latitude,
                    targetLongitude,
                    targetLatitude,
                    null);
        }

        Member member = memberService.findByEmail(email);
        List<SearchResult> results = searchQueryRepository.findBinsByKeyword(
                longitude,
                latitude,
                targetLongitude,
                targetLatitude,
                member.getId());
        searchLogService.createSearchLog(member, keyword, address, results);

        return results;

    }
}
