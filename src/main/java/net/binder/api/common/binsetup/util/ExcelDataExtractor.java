package net.binder.api.common.binsetup.util;

import net.binder.api.bin.entity.BinType;
import net.binder.api.common.binsetup.dto.PublicBinData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.nio.file.Path;

import java.util.List;
import java.util.stream.StreamSupport;

public class ExcelDataExtractor {

    private static final int HEADER_ROWS = 4;
    private static final int DISTRICT_COLUMN = 1;
    private static final int ROAD_ADDRESS_COLUMN = 2;
    private static final int DETAILED_LOCATION_COLUMN = 3;
    private static final int BIN_TYPE_COLUMN = 5;

    public static List<PublicBinData> createInitialData(String path) {

        try (Workbook workbook = WorkbookFactory.create(Path.of(path).toFile())) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(HEADER_ROWS)
                    .map(ExcelDataExtractor::rowToPublicBinData)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }
    }

    private static PublicBinData rowToPublicBinData(Row row) {
        String district = row.getCell(DISTRICT_COLUMN).getStringCellValue();
        String roadNameAddress = row.getCell(ROAD_ADDRESS_COLUMN).getStringCellValue();
        String detailedAddress = row.getCell(DETAILED_LOCATION_COLUMN).getStringCellValue();
        String type = row.getCell(BIN_TYPE_COLUMN).getStringCellValue();

        return PublicBinData.builder()
                .type(getBinTypeFromString(type))
                .address(district + " " + roadNameAddress)
                .title(detailedAddress)
                .imageUrl(null)
                .build();
    }

    private static BinType getBinTypeFromString(String type) {
        return "일반쓰레기".equals(type) ? BinType.GENERAL : BinType.RECYCLE;
    }

}
