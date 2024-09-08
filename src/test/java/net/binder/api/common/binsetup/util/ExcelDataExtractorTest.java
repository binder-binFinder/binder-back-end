package net.binder.api.common.binsetup.util;

import net.binder.api.bin.entity.BinType;
import net.binder.api.common.binsetup.dto.PublicBinData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelDataExtractorTest {

    @Test
    @DisplayName("엑셀 파일에 있는 row들을 읽어온다.")
    void test1(){

        String path = "C:\\Users\\정연호\\Desktop\\공부방법\\이력서\\스위프\\서울특별시 가로쓰레기통 설치정보_202312.xlsx";
        List<PublicBinData> initialData = ExcelDataExtractor.createInitialData(path);

        assertThat(initialData.size()).isEqualTo(5380);
        assertThat(initialData.get(0).getTitle()).isEqualTo("경복궁역 4번출구");
        assertThat(initialData.get(0).getAddress()).isEqualTo("종로구 사직로 125");
        assertThat(initialData.get(0).getType()).isEqualTo(BinType.GENERAL);

        assertThat(initialData.get(1).getTitle()).isEqualTo("경복궁역 4번출구");
        assertThat(initialData.get(1).getAddress()).isEqualTo("종로구 사직로 125");
        assertThat(initialData.get(1).getType()).isEqualTo(BinType.RECYCLE);


    }

}