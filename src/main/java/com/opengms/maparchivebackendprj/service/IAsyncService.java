package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.dto.mapItem.BatchProcessDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.ProcessDTO;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import com.opengms.maparchivebackendprj.entity.po.MapItem;

/**
 * @Description 异步执行脚本
 * <code>@Async</code>注解的实现是基于Spring的AOP，而AOP的实现是基于动态代理模式实现的
 * 被调用方法 和 调用处的代码都处在同一个类，所以只是相当于本类调用，并没有使用代理类 从而@Async并没有产生效果
 * 所以要放在不同的类中
 * @Author bin
 * @Date 2022/03/24
 */
public interface IAsyncService {

    //异步方法不能传入item对象，因为对象里的属性随时可能发生变化
    //所以要在修改该对象的时候用id查到该属性去修改

    /**
     * 地图条目入库
     * @param mapItem 地图条目
     * @param loadPath 加载的文件路径
     * @param savePath 生成新文件保存的路径
     * @param rootPath 在多级目录的情况下，以该路径作为根路径进行切分，按照该路径后面的格式生成对应的文件夹
     * @param processDTO 批量处理所传入的参数
     * @param fileInfo 与地图条目关联的文件信息(上传的功能才关联)
     * @return void
     * @Author bin
     **/
    void initMapItem(
        MapItem mapItem,
        String loadPath, String savePath, String rootPath,
        ProcessDTO processDTO, FileInfo fileInfo);

    //批量处理
    void batchProcess(MapItem mapItem, BatchProcessDTO processDTO);


    void generateThumbnail(String mapId);

    void generateTiles(String mapId);

}
