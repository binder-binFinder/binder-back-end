package net.binder.api.bin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.binder.api.common.exception.BadRequestException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BinType {

    GENERAL("general"),

    RECYCLE("recycle"),

    CIGAR("cigar"),

    BEVERAGE( "beverage");

    private final String name;

    public static BinType getType(String type){
        BinType[] types = BinType.values();
        for(BinType binType : types){
            if(binType.getName().equals(type)){
                return binType;
            }
        }
        throw new BadRequestException(type+"은 정해진 쓰레기통 타입이 아닙니다.");
    }
}