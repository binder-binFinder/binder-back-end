package net.binder.api.binsetup.service;

import net.binder.api.common.binsetup.service.BinDataSetupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BinDataSetupServiceTest {

    @Autowired
    private BinDataSetupService binDataSetupService;

    @Test
    void test(){

        String path = "C:\\Users\\정연호\\Desktop\\공부방법\\이력서\\스위프\\서울특별시 가로쓰레기통 설치정보_202312.xlsx";
        binDataSetupService.saveInitialDate(path);
    }
}