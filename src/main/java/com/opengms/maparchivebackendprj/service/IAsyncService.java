package com.opengms.maparchivebackendprj.service;

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
     * 生成缩略图
     * @param mapItemId 地图Id
     * @param inputPath 输入文件路径
     * @param outputDir 输出文件夹路径
     * @return void
     * @Author bin
     **/
    void generateThumbnailImage(String mapItemId, String inputPath, String outputDir);

    /**
     * 切片
     * @param mapItemId 地图Id
     * @param inputPath 输入文件路径
     * @param outputDir 输出文件夹路径
     * @return void
     * @Author bin
     **/
    void generateTiles(String mapItemId, String inputPath, String outputDir);

    //判断该条目是否处理完成
    boolean hasProcessFinish(MapItem mapItem);

}
