package com.opengms.maparchivebackendprj.entity.bo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2022/06/06
 */
@Data
// @Component
public class DBConnectMap {

    Map<String, Connection> connectList;

    public Connection getValue(String key){
        return connectList.get(key);
    }



    public void setValue(String key, Connection conn){
        connectList.put(key, conn);
    }

}
