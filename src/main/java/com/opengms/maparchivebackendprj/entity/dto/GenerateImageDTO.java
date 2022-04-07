package com.opengms.maparchivebackendprj.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateImageDTO {

    String mapItemId;
    String inputPath;
    String outputDir;

}
