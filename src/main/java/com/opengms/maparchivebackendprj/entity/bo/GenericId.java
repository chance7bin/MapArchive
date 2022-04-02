package com.opengms.maparchivebackendprj.entity.bo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.UUID;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
@Data
public class GenericId {
    @org.springframework.data.annotation.Id
    @Field(targetType = FieldType.STRING)
    String id = UUID.randomUUID().toString();
}
