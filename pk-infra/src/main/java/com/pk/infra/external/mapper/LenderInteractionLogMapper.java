package com.pk.infra.external.mapper;
import com.pk.core.external.LenderInteractionLog;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface LenderInteractionLogMapper { int insert(LenderInteractionLog log); }
