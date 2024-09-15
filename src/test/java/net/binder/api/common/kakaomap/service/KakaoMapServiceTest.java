package net.binder.api.common.kakaomap.service;

import net.binder.api.bin.entity.BinType;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KakaoMapServiceTest {

    @Autowired
    private KakaoMapService kakaoMapService;

    @DisplayName("쓰레기통 주소를 입력하면 카카오맵에서 longitue, latitude를 받아올 수 있다.")
    @Test
    void test1(){
        PublicBinData publicBinData = new PublicBinData("경복궁 4번출구", "종로구 사직로 125", BinType.GENERAL, null );
        ProcessedBinData processedBinData = kakaoMapService.getPoint(publicBinData);

        assertThat(processedBinData.getTitle()).isEqualTo("경복궁 4번출구");
        assertThat(processedBinData.getAddress()).isEqualTo("서울 종로구 사직로 125");
        assertThat(processedBinData.getLongitude()).isCloseTo(126.972894955703, within(0.000000000001));
        assertThat(processedBinData.getLatitude()).isCloseTo(37.5761344458967, within(0.0000000000001));
        assertThat(processedBinData.getImageUrl()).isNull();
    }
}