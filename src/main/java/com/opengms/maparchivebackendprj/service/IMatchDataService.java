package com.opengms.maparchivebackendprj.service;

import java.util.Map;

public interface IMatchDataService {
    Map<String, Object> getMetadataByFilenameByTypeForOther(String filename, String mapCLSId, String mapType, String excelPath);
}
