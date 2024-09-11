package net.binder.api.search.service;

import net.binder.api.bin.entity.BinType;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Test
    void test1() {
        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027752353367, 37.495544565616);
        List<SearchResult> search = searchService.search(searchDto, 1L);

        assertThat(search.size()).isEqualTo(2);

        assertThat(search.get(0).getTitle()).isEqualTo("서초동 1327-5");
        assertThat(search.get(0).getAddress()).isEqualTo("서초구 서초대로78길 24 ");
        assertThat(search.get(0).getType()).isEqualTo(BinType.CIGAR);
//        assertThat(search.get(0).getLongitude()).isCloseTo(127.027752353367, within(0.000000000001));
//        assertThat(search.get(0).getLatitude()).isCloseTo(37.495544565616, within(0.000000000001));

        assertThat(search.get(1).getTitle()).isEqualTo("서초동 1330-18");
        assertThat(search.get(1).getAddress()).isEqualTo("서초구 서초대로78길 42 ");
        assertThat(search.get(1).getType()).isEqualTo(BinType.CIGAR);
//        assertThat(search.get(1).getLongitude()).isCloseTo(127.028224355185, within(0.000000000001));
//        assertThat(search.get(1).getLatitude()).isCloseTo(37.49402562647, within(0.000000000001));
    }

    @Test
    void test2() {
        SearchDto searchDto = new SearchDto(null, 127.027752353367, 37.495544565616);
        List<SearchResult> search = searchService.search(searchDto, 1L);

        assertThat(search.size()).isEqualTo(11);

        assertThat(search.get(0).getTitle()).isEqualTo("서초동 1327-5");
        assertThat(search.get(0).getAddress()).isEqualTo("서초구 서초대로78길 24 ");
        assertThat(search.get(0).getType()).isEqualTo(BinType.CIGAR);
        assertThat(search.get(0).getPoint().getX()).isCloseTo(127.027752353367, within(0.000000000001));
        assertThat(search.get(0).getPoint().getY()).isCloseTo(37.495544565616, within(0.000000000001));

        assertThat(search.get(1).getTitle()).isEqualTo("던킨도너츠 앞");
        assertThat(search.get(1).getAddress()).isEqualTo("서초구 강남대로 373 ");
        assertThat(search.get(1).getType()).isEqualTo(BinType.RECYCLE);
//        assertThat(search.get(1).getLongitude()).isCloseTo(127.028010119934, within(0.000000000001));
//        assertThat(search.get(1).getLatitude()).isCloseTo(37.495982934664, within(0.000000000001));

        assertThat(search.get(2).getTitle()).isEqualTo("도씨에빛 1 앞");
        assertThat(search.get(2).getAddress()).isEqualTo("서초구 강남대로 365 ");
        assertThat(search.get(2).getType()).isEqualTo(BinType.RECYCLE);
//        assertThat(search.get(2).getLongitude()).isCloseTo(127.028348895147, within(0.000000000001));
//        assertThat(search.get(2).getLatitude()).isCloseTo(37.495323407006, within(0.000000000001));

        assertThat(search.get(3).getTitle()).isEqualTo("우성아파트O3");
        assertThat(search.get(3).getAddress()).isEqualTo("서초구 강남대로 373 ");
        assertThat(search.get(3).getType()).isEqualTo(BinType.GENERAL);
//        assertThat(search.get(3).getLongitude()).isCloseTo(127.027969792487, within(0.000000000001));
//        assertThat(search.get(3).getLatitude()).isCloseTo(37.496099264184, within(0.000000000001));
    }
}