package com.opengms.maparchivebackendprj.entity.bo.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/04
 */
@XmlRootElement(name = "dataServer")
@XmlAccessorType
    (XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataServerList {
    @XmlElement(name = "server")
    List<DataServer> dataServerList;
}
