package net.binder.api.bin.repository;

import net.binder.api.bin.entity.Bin;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import net.binder.api.common.kakaomap.service.KakaoMapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class BinRepositoryTest {

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private KakaoMapService kakaoMapService;

    @Test
    @DisplayName("DB에 들어간 좌표 데이터가 카카오맵의 좌표와 동일하다")
    void test() {
        List<Bin> all = binRepository.findAll();
        for(int i =0; i<all.size(); i++){
            Bin bin = all.get(i);

            PublicBinData publicBinData = new PublicBinData(bin.getTitle(), bin.getAddress(), bin.getType(), null);
            ProcessedBinData point = kakaoMapService.getPoint(publicBinData);

            assertThat(bin.getPoint().getX()).isCloseTo(point.getLongitude(),  within(0.000000000001));
            assertThat(bin.getPoint().getY()).isCloseTo(point.getLatitude(), within(0.0000000000001));
        }
    }
}