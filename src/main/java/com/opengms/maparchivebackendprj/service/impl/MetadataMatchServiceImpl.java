package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IMatchDataService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class MetadataMatchServiceImpl implements IMatchDataService {

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Autowired
    IMetadataService metadataService;

    @Autowired
    IMetadataDao metadataDao;

    @Autowired
    MetadataServiceImpl metadataServiceImpl;

    @Override
    public Map<String, Object> getMetadataByFilenameByTypeForOther(String filename, String mapCLSId, String mapType, String excelPath){
        MetadataTable metadataTable = metadataTableDao.findById(mapCLSId);
        String collection = metadataTable.getCollection();
        try {
            if (collection.contains("STATE_FOUNDING_AROUND")){           // 建国前后
                return getFondingMetadata(filename,collection,excelPath);
            }
            else if(collection.contains("THEMATIC_GANGAO_RECORD")) {     // 其余地图按新添的规则进行分类
                if(mapType.equals("地形图")){                  // 其中有2500比例尺地形图：06-50-061-A-2-(4)-3-1.tif
                    return metadataServiceImpl.getBSMMetadata(filename, collection, mapType, excelPath);
                }
                else {
                    return getGARecordMetadata(filename,collection,excelPath);
                }
            }
            else if(collection.contains("THEMATIC_ISLAND")) {     // 其余地图按新添的规则进行分类
                return getIslandMetadata(filename,collection,excelPath);
            }
            else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 按 顺序号/顺序号+图名 两种方式进行匹配        如：  G49-101-d  ; 014西盐务村
    public Map<String, Object> getFondingMetadata(String filename, String collection, String excelPath) throws Exception{
        //checkFilenameFormat
        String formatFilename = null;
        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀
        filename = metadataService.get_english_name(filename);

        //将(1)(2)(3)(4)删去, 建国前后多个地图信息重复
        if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
            filename = filename.substring(0,filename.lastIndexOf("("));
        }

        // 时间规范
        String name_without_year;
        String name_after_year;
        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;
        if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1 && potential_year_count !=5){      //处理除了尾缀为(1990) (3) 176安陆(锺祥縣) 无括号其他情况的地图
            return null;
        }
        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
            name_without_year = filename.substring(0, first_brace);     //图幅编号
            name_after_year = filename.substring(first_brace);      //除了图幅之外的，年份名称
        } else {
            name_without_year = filename;       //没有时间
            name_after_year = "";
        }

        formatFilename = name_without_year+name_after_year;

        // 读excel表中的数据
        if (excelPath != null && !excelPath.equals("")) {
            List<Map<String, Object>> maps = FileUtils.redExcel(excelPath);
            List<String> Database_name = new ArrayList<>();  //原图幅编号+年份
            List<String> Database_name_block_only = new ArrayList<>(); //原图幅编号
            Map<String,List<String>> Database_name_year_dict = new HashMap<>();//原图幅号和对应时间的集合
            Map<String,List<String>> Database_name_style_dict = new HashMap<>();//原图幅号和对应版型的集合


            int count = 0; //匹配上的次数
            int index = -1; //列表索引

            for (int j = 0; j < maps.size(); j++) {
                String code1 = maps.get(j).get("原图幅编号").toString();
                String code2 = maps.get(j).get("出版时间").toString();
                String code3 = maps.get(j).get("版型").toString();

                String format1 = code1;
                String format2 = code1+"("+code2+")";
                if(Database_name_year_dict.containsKey(code1)){
                    Database_name_year_dict.get(code1).add(code2);
                    Database_name_style_dict.get(code1).add(code3);
                }
                else{
                    LinkedList<String> strings1 = new LinkedList<>();
                    LinkedList<String> strings2 = new LinkedList<>();
                    strings1.add(code2);
                    strings2.add(code3);
                    Database_name_year_dict.put(code1,strings1);
                    Database_name_style_dict.put(code1,strings2);

                }
                Database_name.add(format2);
                Database_name_block_only.add(format1);

                // if (formatFilename.equals(format2) || formatFilename.equals(format1)){
                //     count++;
                //     index = j;
                // }
            }

            if (!Database_name.contains(formatFilename)){ //跟原图幅编号+年份的不匹配
                if (Database_name_block_only.contains(formatFilename)){
                    int frequency = Collections.frequency(Database_name_block_only, formatFilename);
                    if (frequency == 1){ //只有唯一的才是成功的
                        return maps.get(Database_name_block_only.indexOf(formatFilename));
                    }
                    else {
                        if(Database_name_year_dict.containsKey(formatFilename)){
                            List<String> strings = Database_name_year_dict.get(formatFilename);
                            Set<String> set=new HashSet<>(strings);
                            if (set.size()==1 && strings.size()>1){ //多个相同的年份
                                int time1 = Collections.frequency(Database_name_style_dict.get(formatFilename), '纸');
                                if (time1 == 1){
                                    int time2 = Database_name_style_dict.get(formatFilename).indexOf('纸');
                                    int time3 = 0;
                                    for(int a = 0; a < Database_name_year_dict.size(); a++){
                                        if(Database_name_block_only.get(a) == formatFilename){
                                            time3++;
                                        }
                                        if(time3 == time2+1){
                                            return maps.get(a);
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }
                //特殊情况一：由于<>的存在影响匹配，删去处理
                if (formatFilename.indexOf('<') > 0){
                    String name_without_idx;
                    name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
                    int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));
                    int frequency = Collections.frequency(Database_name, name_without_idx);
                    if (frequency == 1) {   //考虑原图幅+时间匹配上的情况
                        return maps.get(Database_name.indexOf(name_without_idx)) ;
                    }
                    else if(frequency > 1){
                        int index1 = 0;
                        for(int a = 0; a < Database_name.size(); a++){
                            if (Database_name.get(a) == name_without_idx){
                                Map<String, Object> item = maps.get(a);
                                String type = item.get("版型").toString();
                                if(type == "纸"){    //如果是纸则赋给<1>的图
                                    if(num == 1){
                                        return item;
                                    }
                                }else {     //不是纸，则赋给不为<1>的图
                                    if (num != 1){
                                        return item;
                                    }
                                }
                                index1 = a;
                            }

                        }
                        return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                    }
                    else {
                        return null;
//
                    }
                }
                //特殊情况二：时间出错，删去时间只对原图幅编号进行匹配
//                int frequency = Collections.frequency(Database_name, new_name_str);
//                List<Map<String,Object>> mapList = new ArrayList<>();
//                if(frequency > 0){
//                    if(frequency == 1){
//                        return maps.get(Database_name.indexOf(new_name_str));
//                    }
//                    else {
//                        final String string=new_name_str;
//
//                        for (int a = 0; a < Database_name.size(); a++) {
//                            if(Database_name.get(a) == new_name_str){
//                                mapList.add(maps.get(a));
//                            }
//                        }
//                        return null;
//                    }
//                }
                return null;
            }

            else {  //跟原图幅编号+年份的匹配
                int frequency = Collections.frequency(Database_name, formatFilename);
                if (frequency == 1){
                    return maps.get(Database_name.indexOf(formatFilename));
                }
                else if(frequency > 1){     //同一图幅多个相同时间，没有<>,默认不选纸
                    int index1 = 0;
                    for(int a = 0; a < Database_name.size(); a++){  //和带有时间的做对比
                        if (Database_name.get(a) == formatFilename){
                            Map<String, Object> item = maps.get(a);
                            String type = item.get("版型").toString();
                            if(type != "纸"){    //不是纸，则赋给不为<1>的图
                                return item;
                            }
                            index1 = a;
                        }

                    }
                    return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
                return null;
            }
        }
        //进行数据库的匹配
        else {
            // 顺序号
            // 先进行仅按顺序号+时间匹配 J-51-020(1985)
            List<JSONObject> list_index_year = metadataDao.findMetadataByOriginalNumAndYear("","indexAndYear",formatFilename,collection);
            if (list_index_year.size() == 1)
                return list_index_year.get(0);
            if (list_index_year.size() > 1){
                return list_index_year.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再进行仅按顺序号匹配 如：J-51-020
            List<JSONObject> list_index = metadataDao.findMetadataByOriginalNum("","顺序号",formatFilename,collection);
            if (list_index.size() == 1)
                return list_index.get(0);
            List<String> strings_index = new ArrayList<>();
            for(int a = 0; a < list_index.size(); a++){
                String publicTime = list_index.get(a).get("出版时间").toString();
                // code2为出版时间，可能有文字：1932年制版,1934年印刷（按制版年索引）
                if (publicTime.length() > 4){
                    publicTime = publicTime.substring(0,4);
                }
                if (publicTime.contains(".")){
                    publicTime = publicTime.substring(0,publicTime.indexOf("."));
                }
                strings_index.add(publicTime);
            }
            Set<String> set_index=new HashSet<>(strings_index);
            if (list_index.size() > 1 && set_index.size() == 1){       //
                return list_index.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 顺序号+图名
            // 先匹配 顺序号+图名+年份 这一列  196平桥镇(1971)
            List<JSONObject> list_index_name_year = metadataDao.findMetadataByOriginalNumAndYear("","indexAndNameAndYear",formatFilename,collection);
            if (list_index_name_year.size() == 1)
                return list_index_name_year.get(0);
            if (list_index_name_year.size() > 1){
                return list_index_name_year.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再匹配 顺序号+图名 这一列       196平桥镇
            List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum("","indexAndName",formatFilename,collection);
            if (list1.size() == 1)
                return list1.get(0);
            List<String> strings = new ArrayList<>();
            for(int a = 0; a < list1.size(); a++){
                String publicTime = list1.get(a).get("出版时间").toString();
                // code2为出版时间，可能有文字：1932年制版,1934年印刷（按制版年索引）
                if (publicTime.length() > 4){
                    publicTime = publicTime.substring(0,4);
                }
                if (publicTime.contains(".")){
                    publicTime = publicTime.substring(0,publicTime.indexOf("."));
                }
                strings.add(publicTime);
            }
            Set<String> set=new HashSet<>(strings);
            if (list1.size() > 1 && set.size() == 1){       //
                return list1.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            return null;
        }



    }

    // 按 图名 进行匹配 大岭(2-1)(2013)
    public Map<String, Object> getIslandMetadata(String filename, String collection, String excelPath) throws Exception{
        //checkFilenameFormat
        String formatFilename = null;
        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀
        filename = metadataService.get_english_name(filename);
        filename = filename.replaceAll(" ","");     // 去除多余空格, 注：数据库匹配字段的值中没有使用到空格

        // 岛屿图：白犬沃(5)； 东霜岛  ，有的有()有的无，故不好区分()是否属于匹配字段中的值还是尾缀，带入匹配
//        //将(1)(2)(3)(4)删去, 建国前后多个地图信息重复
//        if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
//            filename = filename.substring(0,filename.lastIndexOf("("));
//        }

        // 时间规范
        String name_without_year;
        String name_after_year;
        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;

        // 岛屿图有：大岭(2-1)(2013)  (无图名)(2013)  这类特殊，故不先删除这些，之后无法匹配再返回null
//        if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1 && potential_year_count !=4){      //处理除了尾缀为(1990) (3) (12) 无括号其他情况的地图
//            return null;
//        }

        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
            name_without_year = filename.substring(0, first_brace);     //图幅编号
            name_after_year = filename.substring(first_brace);      //除了图幅之外的，年份名称
        } else {
            name_without_year = filename;       //没有时间
            name_after_year = "";
        }

        formatFilename = name_without_year+name_after_year;

        // 读excel表中的数据
        if (excelPath != null && !excelPath.equals("")) {
            List<Map<String, Object>> maps = FileUtils.redExcel(excelPath);
            List<String> Database_name = new ArrayList<>();  //原图幅编号+年份
            List<String> Database_name_block_only = new ArrayList<>(); //原图幅编号
            Map<String,List<String>> Database_name_year_dict = new HashMap<>();//原图幅号和对应时间的集合
            Map<String,List<String>> Database_name_style_dict = new HashMap<>();//原图幅号和对应版型的集合


            int count = 0; //匹配上的次数
            int index = -1; //列表索引

            for (int j = 0; j < maps.size(); j++) {
                String code1 = maps.get(j).get("原图幅编号").toString();
                String code2 = maps.get(j).get("出版时间").toString();
                String code3 = maps.get(j).get("版型").toString();

                String format1 = code1;
                String format2 = code1+"("+code2+")";
                if(Database_name_year_dict.containsKey(code1)){
                    Database_name_year_dict.get(code1).add(code2);
                    Database_name_style_dict.get(code1).add(code3);
                }
                else{
                    LinkedList<String> strings1 = new LinkedList<>();
                    LinkedList<String> strings2 = new LinkedList<>();
                    strings1.add(code2);
                    strings2.add(code3);
                    Database_name_year_dict.put(code1,strings1);
                    Database_name_style_dict.put(code1,strings2);

                }
                Database_name.add(format2);
                Database_name_block_only.add(format1);

                // if (formatFilename.equals(format2) || formatFilename.equals(format1)){
                //     count++;
                //     index = j;
                // }
            }

            if (!Database_name.contains(formatFilename)){ //跟原图幅编号+年份的不匹配
                if (Database_name_block_only.contains(formatFilename)){
                    int frequency = Collections.frequency(Database_name_block_only, formatFilename);
                    if (frequency == 1){ //只有唯一的才是成功的
                        return maps.get(Database_name_block_only.indexOf(formatFilename));
                    }
                    else {
                        if(Database_name_year_dict.containsKey(formatFilename)){
                            List<String> strings = Database_name_year_dict.get(formatFilename);
                            Set<String> set=new HashSet<>(strings);
                            if (set.size()==1 && strings.size()>1){ //多个相同的年份
                                int time1 = Collections.frequency(Database_name_style_dict.get(formatFilename), '纸');
                                if (time1 == 1){
                                    int time2 = Database_name_style_dict.get(formatFilename).indexOf('纸');
                                    int time3 = 0;
                                    for(int a = 0; a < Database_name_year_dict.size(); a++){
                                        if(Database_name_block_only.get(a) == formatFilename){
                                            time3++;
                                        }
                                        if(time3 == time2+1){
                                            return maps.get(a);
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }
                //特殊情况一：由于<>的存在影响匹配，删去处理
                if (formatFilename.indexOf('<') > 0){
                    String name_without_idx;
                    name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
                    int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));
                    int frequency = Collections.frequency(Database_name, name_without_idx);
                    if (frequency == 1) {   //考虑原图幅+时间匹配上的情况
                        return maps.get(Database_name.indexOf(name_without_idx)) ;
                    }
                    else if(frequency > 1){
                        int index1 = 0;
                        for(int a = 0; a < Database_name.size(); a++){
                            if (Database_name.get(a) == name_without_idx){
                                Map<String, Object> item = maps.get(a);
                                String type = item.get("版型").toString();
                                if(type == "纸"){    //如果是纸则赋给<1>的图
                                    if(num == 1){
                                        return item;
                                    }
                                }else {     //不是纸，则赋给不为<1>的图
                                    if (num != 1){
                                        return item;
                                    }
                                }
                                index1 = a;
                            }

                        }
                        return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                    }
                    else {
                        return null;
//
                    }
                }
                //特殊情况二：时间出错，删去时间只对原图幅编号进行匹配
//                int frequency = Collections.frequency(Database_name, new_name_str);
//                List<Map<String,Object>> mapList = new ArrayList<>();
//                if(frequency > 0){
//                    if(frequency == 1){
//                        return maps.get(Database_name.indexOf(new_name_str));
//                    }
//                    else {
//                        final String string=new_name_str;
//
//                        for (int a = 0; a < Database_name.size(); a++) {
//                            if(Database_name.get(a) == new_name_str){
//                                mapList.add(maps.get(a));
//                            }
//                        }
//                        return null;
//                    }
//                }
                return null;
            }

            else {  //跟原图幅编号+年份的匹配
                int frequency = Collections.frequency(Database_name, formatFilename);
                if (frequency == 1){
                    return maps.get(Database_name.indexOf(formatFilename));
                }
                else if(frequency > 1){     //同一图幅多个相同时间，没有<>,默认不选纸
                    int index1 = 0;
                    for(int a = 0; a < Database_name.size(); a++){  //和带有时间的做对比
                        if (Database_name.get(a) == formatFilename){
                            Map<String, Object> item = maps.get(a);
                            String type = item.get("版型").toString();
                            if(type != "纸"){    //不是纸，则赋给不为<1>的图
                                return item;
                            }
                            index1 = a;
                        }

                    }
                    return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
                return null;
            }
        }
        //进行数据库的匹配
        else {
            // 图名
            // 先进行仅图名+时间匹配 仙人画(3)(1962)
            List<JSONObject> list_index_year = metadataDao.findMetadataByOriginalNumAndYear("","numAndYear",formatFilename,collection);
            if (list_index_year.size() == 1)
                return list_index_year.get(0);
            if (list_index_year.size() > 1){
                return list_index_year.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再进行仅按图名匹配 如：西台山岛
            List<JSONObject> list_index = metadataDao.findMetadataByOriginalNum("","图名",formatFilename,collection);
            if (list_index.size() == 1)
                return list_index.get(0);
            List<String> strings_index = new ArrayList<>();
            for(int a = 0; a < list_index.size(); a++){
                // 出版时间字段特殊值：'1927年测图'  在数据库匹配中，提前在数据库中做修改了
                strings_index.add(list_index.get(a).get("出版时间").toString());
            }
            Set<String> set_index=new HashSet<>(strings_index);
            if (list_index.size() > 1 && set_index.size() == 1){       //
                return list_index.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            return null;
        }
    }

    // 暂时留位，处理不规则的港澳档案文件名
    public Map<String, Object> getGARecordMetadata(String filename, String collection, String excelPath) throws Exception{
        return null;
    }

}
