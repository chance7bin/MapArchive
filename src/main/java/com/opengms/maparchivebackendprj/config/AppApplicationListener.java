package com.opengms.maparchivebackendprj.config;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.service.ITilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author bin
 * @Date 2022/06/07
 */
// @Component
public class AppApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    ITilesService tilesService;

    public AppApplicationListener() {
        System.out.println("DoByApplicationListener constructor");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            System.out.println("DoByApplicationListener do something");

            JSONObject json = tilesService.getMapboxTilesMetadataJson();
            System.out.println(json);
        }
    }
}
