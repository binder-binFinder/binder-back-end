package net.binder.api.common.binsetup.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import net.binder.api.common.binsetup.repository.BinBatchInsertRepository;
import net.binder.api.common.kakaomap.service.KakaoMapService;
import net.binder.api.common.binsetup.util.ExcelDataExtractor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BinDataSetupService {

    private final BinBatchInsertRepository binBatchInsertRepository;
    private final KakaoMapService kakaoMapService;

    public void saveInitialDate(String path){
        List<PublicBinData> initialData = ExcelDataExtractor.createInitialData(path);
        List<ProcessedBinData> point = kakaoMapService.getPoints(initialData);
        binBatchInsertRepository.batchInsertInitialBins(point);
    }
}
