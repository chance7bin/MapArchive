package com.opengms.maparchivebackendprj.entity.bo.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

/**
 * @Description 数据服务器配置文件
 * @Author bin
 * @Date 2022/04/04
 */
//根元素
@XmlRootElement(name = "server")
//访问类型，通过字段
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataServer {
    @XmlElement(name = "name")
    String name;
    @XmlElement(name = "ip")
    String ip;
    @XmlElement(name = "realPath")
    String realPath;
    @XmlElement(name = "loadPath")
    String loadPath;

}
