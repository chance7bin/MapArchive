package com.opengms.maparchivebackendprj.config;

import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServerList;
import com.opengms.maparchivebackendprj.utils.XmlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/05
 */
@Configuration
public class InitConfig {

    @Value("${resourcePath}")
    private String resourcePath;

    @Bean(name = "dataServerList")
    List<DataServer> dataServerList() throws Exception {

        String xmlPath = resourcePath + "/config/dataServer.xml";

        // 读取XML文件
        BufferedReader br = new BufferedReader(new FileReader(xmlPath));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) !=null) {
            buffer.append(line);
        }
        br.close();
        // XML转为Java对象
        DataServerList dataServerList = (DataServerList) XmlUtils.xmlStrToObject(DataServerList.class, buffer.toString());

        return dataServerList.getDataServerList();

    }

    @Bean(name = "defaultDataServer")
    DataServer defaultDataServer() throws Exception {
        return dataServerList().get(0);
    }

}
