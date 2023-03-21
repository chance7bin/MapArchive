package com.opengms.maparchivebackendprj.entity.po;

import com.opengms.maparchivebackendprj.entity.bo.GenericItem;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ImageMetadata;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ImageUrl;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ProcessParam;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import lombok.Data;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
@Document
@Data
public class MapItem extends GenericItem {
    // MapClassification mapCLS;
    String mapCLSId; //对应MetadataTable中的id

    ImageUrl imageUrl = new ImageUrl();  //图片存放的相对路径
    String relativeFileId; //该地图关联的文件id,用于下载以及删除关联文件
    String mapType;     // 同一地种类下地图细分匹配，如基本比例尺中有（地形图、协同图、联合作战图）

    // StatusEnum copyStatus = StatusEnum.Inited;  //复制文件的进程
    StatusEnum thumbnailStatus = StatusEnum.Inited;  //生成缩略图的进程
    StatusEnum tileStatus = StatusEnum.Inited;  //生成切片的进程
    StatusEnum processStatus = StatusEnum.Inited; //该地图处理的过程
    boolean hasMatchMetaData = false; // 是否匹配上元数据
    boolean hasCalcCoordinate = false; // 有没有计算出坐标
    boolean hasNeedManual = true; //是否需要人工处理
    // thumbnailStatus tileStatus hasMatchMetaData hasCalcCoordinate只要其中之一没有完成就要人工处理

    // String rootPath; //地图条目上传时图片存放的根路径
    String resourceDir; //资源存放目录

    Map<String, Object> metadata; //元数据信息(新)

    // @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    GeoJsonPoint center;  // 图像中心点

    // Box box; // 图像box范围

    // @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    GeoJsonPolygon polygon; // 图像polygon范围

    ProcessParam processParam; // 生成缩略图以及进行切片所需的参数

    ImageMetadata imageMetadata; //图片的元数据信息

    String server; //服务器别名，用于标识资源存储的服务器位置，详细信息在/config/dataServer.xml中

}
