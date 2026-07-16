package com.pk.infra.ocr.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OcrVendorCallLogMapper {
    int insert(OcrVendorCallLogInsertParam param);
}
