package net.binder.api.bin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.binder.api.common.exception.BadRequestException;

@Getter
@AllArgsConstructor
public enum BinType {

    GENERAL,

    RECYCLE,

    CIGAR,

    BEVERAGE;

    public static BinType getType(String type) {
        String upperCase = type.toUpperCase();

        BinType[] types = BinType.values();
        for (BinType binType : types) {
            if (binType.name().equals(upperCase)) {
                return binType;
            }
        }
        throw new BadRequestException(type + "은 정해진 쓰레기통 타입이 아닙니다.");
    }
}