package com.opengms.maparchivebackendprj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description sqlite配置
 * @Author bin
 * @Date 2021/12/09
 */
@Slf4j
@Configuration
public class SqliteConfig {

    @Value("${resourcePath}")
    private String resourcePath;


    @Bean(name = "imgConnection")
    Connection imgStatement() throws SQLException {
        return getConnection("jdbc:sqlite:" + resourcePath + "/tianditu/img_c.mbtiles");
    }

    @Bean(name = "cvaConnection")
    Connection cvaStatement() throws SQLException {
        return getConnection("jdbc:sqlite:" + resourcePath + "/tianditu/cva_c.mbtiles");
    }

    @Bean(name = "ciaConnection")
    Connection ciaStatement() throws SQLException {
        return getConnection("jdbc:sqlite:" + resourcePath + "/tianditu/vec-cia/cia_c.mbtiles");
    }

    @Bean(name = "vecConnection")
    Connection vecStatement() throws SQLException {
        return getConnection("jdbc:sqlite:" + resourcePath + "/tianditu/vec-cia/vec_c.mbtiles");
    }

    @Bean(name = "mapboxConnection")
    Connection mapboxStatement() throws SQLException {
        // return getConnection("jdbc:sqlite:" + resourcePath + "/mapbox/2017-07-03_planet_z0_z14.mbtiles");
        // return getConnection("jdbc:sqlite:Z:/2017-07-03_planet_z0_z14.mbtiles");
        return null;
        // return getConnection("jdbc:sqlite:Z:/trails.mbtiles");
    }



    /**
     * 连接数据库 返回连接数据库的Connection 不能返回执行SQL语句的statement，
     * 因为每个Statement对象只能同时打开一个ResultSet对象，
     * 高并发情况下会出现 <code>rs.isOpen() on exec</code> 的错误
     * @param conurl 数据库地址
     * @return java.sql.Connection
     * @Author bin
     **/
    Connection getConnection(String conurl) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            // e.printStackTrace();
            log.warn("Database driver not found!");
        }
        // 得到连接 会在你所填写的文件夹建一个你命名的文件数据库
        Connection conn;
        // String conurl = "jdbc:sqlite:E:/mapArchiveFiles/tianditu/img_c.mbtiles";
        conn = DriverManager.getConnection(conurl,null,null);
        // 设置自己主动提交为false
        conn.setAutoCommit(false);

        //推断表是否存在
        ResultSet rsTables = conn.getMetaData().getTables(null, null, "tiles", null);
        if(!rsTables.next()){
            log.warn("Table does not exist!");
        }
        log.info("{} successfully connected!", conurl);

        return conn;

        // return conn.createStatement();
    }
}
