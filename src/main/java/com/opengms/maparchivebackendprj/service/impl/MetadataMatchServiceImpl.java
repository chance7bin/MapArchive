package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IMatchDataService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
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
            if (collection.equals("M03_03_02")){           // 建国前后
                return getFondingMetadata(filename,collection,excelPath);
            }
            else if(collection.equals("M03_02_02_01")) {     // 地图制图-专题制图-港澳地区-档案
                if(mapType.equals("地形图")){                  // 其中有2500比例尺地形图：06-50-061-A-2-(4)-3-1.tif
                    Map<String,String> mapInfo = new HashMap<>();
                    mapInfo.put("type","2500");
                    mapInfo.put("matchField","图幅编号");
                    mapInfo.put("matchFieldAndYear","numAndYear");
                    return metadataServiceImpl.getBSMMetadata(filename,mapInfo, collection, mapType, excelPath);
                }
                else {
                    return getGARecordMetadata(filename,collection,excelPath);
                }
            }
            else if(collection.equals("M03_02_06")) {     // 地图制图-专题制图-岛屿图_地区图
                return getIslandMetadata(filename,collection,excelPath);
            }
            else if(collection.equals("M03_02_07")){       // 地图制图-专题制图-国外地区图
                return getForDistMetadata(filename, collection, mapType, excelPath);
            }
            else if(collection.equals("M07_01_07_02_01")){          // 国外测绘-普通地图-地形图-北美洲-美国
                return getAmericaMetadata(filename, collection, mapType, excelPath);
            }
            else if(collection.equals("M03_02_01_01")){          // 地图制图-专题制图-台湾地区-档案
                return getTaiwanMetadata(filename, collection, excelPath);
            }
            else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 通用方法
    public String get_tufu_name(String[] parts, String Type) {
        try {
            String new_name = "";
            if (Type.equals("USA")){
                if (parts.length == 5){
                    String num1 = parts[0];
                    String num2 = metadataServiceImpl.leftPad(2,parts[1]);
                    String num3 = metadataServiceImpl.leftPad(2,parts[2]);
                    String num4 = metadataServiceImpl.leftPad(2,parts[3]);
                    String num5 = parts[4];
                    new_name = MessageFormat.format("{0}-{1}-{2}-{3}-{4}",num1,num2,num3,num4,num5);
                }
                if (parts.length == 4){
                    String num1 = parts[0];
                    String num2 = metadataServiceImpl.leftPad(2,parts[1]);
                    String num3 = metadataServiceImpl.leftPad(2,parts[2]);
                    String num4 = parts[3];
                    new_name = MessageFormat.format("{0}-{1}-{2}-{3}",num1,num2,num3,num4);
                    System.out.print(new_name);
                }
            }
            return new_name;
        } catch (Exception e) {
            return null;
        }

    }
    // 文件规范(按类型)
    public String checkName(String filename, String type){
        //checkFilenameFormat
        if (!filename.contains(".tif")){ //如果一个.都没有，说明文件名不对
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀

        // 中转英，空格是否删除看具体情况
        if(type.equals("tw")){      // 不删除空格
            filename = metadataService.get_english_name(filename,false);
        }else{
            filename = metadataService.get_english_name(filename,true);
        }

        // 对判别尾缀(1)(2)等的处理
        if (!type.equals("dy")){        // 对于图幅原本命名存在尾缀(1)(2)的，按其余方式处理
            if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
                String name1, name2;
                name1 = filename.substring(0, filename.lastIndexOf('('));
                name2 = filename.substring(filename.lastIndexOf('('));
                name2 = name2.replace('(', '<');
                name2 = name2.replace(')', '>');
                filename = name1 + name2;
            }
        }

        return filename;
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
        filename = metadataService.get_english_name(filename,true);

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
        filename = metadataService.get_english_name(filename,true);
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

    // 外国地形图   图幅编号：NO 35 N/14；NO 49-07；10-51-012-D等
    // 13-31-74.tif => 13-31-074  同10w的规范
    // NP 39,40-1.tif 将“，”修改为.即可匹配
    // NO 30 P/03|NO 39 P/16.44 D/04|NO 43 A/11.A/15,按照/分开补0
    // NP 35.36-02 | NL 48-2(1)
    // NO 82 H => NO 82.H
    public Map<String, Object> getForDistMetadata(String filename, String collection, String maptype, String excelPath) throws Exception{
        //checkFilenameFormat
        String formatFilename = filename;
        if (!filename.contains(".tif")){ //如果一个.都没有，说明文件名不对
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf(".tif"));//出去文件名后缀
        filename = metadataService.get_english_name(filename,false);   // 不需要删除空格，文件名带空格

        // 10w类型格式
        String  name_10w = filename.replaceAll("-","");
        if (filename.length() - name_10w.length() == 2){
            filename = filename.replaceAll("\\.","");       // 替换分幅号 '.' 防止匹配数字出错
            String[] parts = filename.split("-");
            String numString = parts[0] +parts[1] +parts[2];      // 拼接所有部分，共同判断是否都为数字
            Pattern pattern = Pattern.compile("^[0-9]*$");   // 判断字符串否都为数字
            if (pattern.matcher(numString).matches()) {         // 都为数字，采用10w基本比例尺匹配
                Map<String,String> mapInfo = new HashMap<>();
                mapInfo.put("type","TEN");          // 和1w同种规范的命名
                mapInfo.put("matchField","图幅编号");   // 采用自己元数据字段
                mapInfo.put("matchFieldAndYear","numAndYear");
                return metadataServiceImpl.getBSMMetadata(formatFilename,mapInfo, collection, "", excelPath);       // 传递未处理过的文件名
            }
        }

        // 替换为标准符号
        filename = filename.replaceAll("／", "/");
        filename = filename.replaceAll(",", ".");

        // 删除（1）、（2）等
        if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
            filename = filename.substring(0, filename.lastIndexOf('('));
        }

        // 文件名规范
        // NO和/系列，按照/分开补0
        if(filename.indexOf("NO") > -1 && filename.indexOf("/") > -1){
            String filename1 = filename.replaceAll("/","");
            String filename2 = filename.replaceAll(" ","");
            int signNum1 = filename.length() - filename1.length();      // "/"次数
            int signNum2 = filename.length() - filename2.length();      // " "空格次数
            if(filename.indexOf(".") < 0){      // NO 30 P/03
                String[] parts = filename.split("/");
                // 对'/'前进行处理
                String[] front_parts = parts[0].split(" ");
                front_parts[1] = metadataService.leftPad(2,front_parts[1]);
                // 拼接
                filename = StringUtils.join(Arrays.asList(front_parts),' ') + "/" + metadataService.leftPad(2,parts[1]);
            }
            else if(signNum1 == 1){
                String[] parts = filename.split("/");
                //# part1
                String[] items1 = parts[0].split(" ");
                items1[1] = metadataService.leftPad(2,items1[1]);
                parts[0] = StringUtils.join(Arrays.asList(items1),'/');
                //# part2
                String[] items2 = parts[1].split(".");
                items2[0] = metadataService.leftPad(2,items2[0]);
                items2[1] = metadataService.leftPad(2,items2[1]);
                parts[1] = StringUtils.join(Arrays.asList(items2),'.');
                //# 拼接
                filename = StringUtils.join(Arrays.asList(parts),'/');
            }
            else if(signNum2 == 2){
                String[] parts = filename.split("/");
                //# part1
                String[] items1 = parts[0].split(" ");
                items1[1] = metadataService.leftPad(2,items1[1]);
                parts[0] = StringUtils.join(Arrays.asList(items1),'/');
                //# part2
                String[] items2 = parts[1].split(".");
                items2[0] = metadataService.leftPad(2,items2[0]);
                parts[1] = StringUtils.join(Arrays.asList(items2),'.');
                //# part3
                parts[2] = metadataService.leftPad(2,parts[2]);
                //# 拼接
                filename = StringUtils.join(Arrays.asList(parts),'/');
            }
            else if(signNum2 > 2){
                String[] parts = filename.split(".");
                // # 循环处理
                for(String part : parts){
                    int index1 = 0;
                    if(filename.indexOf("NO") > -1){
                        index1 = 3;
                    }
                    int index2 = part.lastIndexOf(" ");
                    String nameDemo1 = filename.substring(index1,index2);
                    String nameDemo2 = filename.substring(index2);
                    filename = filename.replaceAll(nameDemo1,metadataService.leftPad(2,nameDemo1));
                    filename = filename.replaceAll(nameDemo2,metadataService.leftPad(2,nameDemo2));
                }
                filename = StringUtils.join(Arrays.asList(parts),'.');
            }
        }
        // NP 35,36-10 | NP 35,36-8 | NK 10-01.04等
        else if(filename.indexOf(".") > -1 || filename.indexOf("-") > -1){      // NP 35.36-10
            String numName = filename.substring(filename.indexOf(" ")+1);
            int char_index = -1;
            String new_name = "";
            // 统计字符中"."和"-"的位置
            for(int i = 0; i < numName.length(); i++) {
                if (i == numName.length() - 1) {
                    if (i - char_index < 2) {
                        new_name = new_name + numName.charAt(char_index) + metadataService.leftPad(2, numName.substring(char_index + 1));
                    } else {
                        new_name = new_name + numName.substring(char_index);
                    }
                    char_index = i;
                    continue;
                }
                // 当遇见字符或到末尾，则处理中间的数字
                String char_sign = numName.substring(i,i+1);
                Pattern pattern = Pattern.compile("^[0-9]*$");   // 判断字符串否都为数字
                if (!pattern.matcher(char_sign).matches()) {           // 匹配到字符
                    if (i - char_index < 3){
                        if (new_name.equals("")){
                            new_name = metadataService.leftPad(2, numName.substring(0,i));
                        }else {
                            new_name = new_name + numName.charAt(char_index) + metadataService.leftPad(2, numName.substring(char_index + 1,i));
                        }
                    }else {
                        if (new_name.equals("")){
                            new_name = numName.substring(0,i);
                        }else {
                            new_name = new_name + numName.substring(char_index,i);
                        }
                    }
                    char_index = i;
                }
            }
            filename = filename.substring(0,filename.indexOf(" ")+1) + new_name;
        }

        // 时间规范
        String name_without_year;
        String name_after_year;

        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;


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
            // 图幅编号
            // 先进行仅图幅编号+时间匹配:NO 43.N(1963)
            List<JSONObject> list_index_year = metadataDao.findMetadataByOriginalNumAndYear("","numAndYear",formatFilename,collection);
            if (list_index_year.size() == 1)
                return list_index_year.get(0);
            if (list_index_year.size() > 1){
                int index = 0;
                for(int a = 0; a < list_index_year.size(); a++){       // 对结果遍历
                    if (list_index_year.get(a).get("版型").toString() == "纸"){        // 选取版型为纸的记录匹配
                        return list_index_year.get(a);
                    }
                    index = a;
                }
                return list_index_year.get(index);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再进行仅按图幅编号匹配 如：NO 43.N
            List<JSONObject> list_index = metadataDao.findMetadataByOriginalNum("","图幅编号",formatFilename,collection);
            if (list_index.size() == 1)
                return list_index.get(0);
            List<String> strings_index = new ArrayList<>();
            for(int a = 0; a < list_index.size(); a++){
                strings_index.add(list_index.get(a).get("出版时间").toString());
            }
            Set<String> set_index=new HashSet<>(strings_index);
            if (list_index.size() > 1 && set_index.size() == 1){       //
                return list_index.get(0);    //  若不满足之前情况，则匹配最新查到的记录
            }
            return null;
        }
    }

    // 美(E | I-10-09-08-A.07-B,06-31-D.32-C | 11(1) | B-5-14)
    // 五位：E/F/G/H/I/J/K/L/M-04-03-08-D（1:2.4万）
    // 四位：N-3-3-B3（1:63360） | L-12-11-06（1:62500）| H-13-9-VI&VII（1:12.5万）
    // 三位：A-3-02 | 09-12-24（1:10万） | NJ-18-8&11（1:25万）
    // 特殊：01 | 11(1) | ""(空) | E2871I（1:5万） |
    public Map<String, Object> getAmericaMetadata(String filename, String collection, String maptype, String excelPath) throws Exception{
        String formatFilename = null;
        if (!filename.contains(".tif")){ //如果一个.都没有，说明文件名不对
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf(".tif"));//出去文件名后缀
        filename = metadataService.get_english_name(filename,true);   // 不需要删除空格，文件名带空格

        // 替换（1） （2）等为<>，特殊情况：11(1)  12(1)  15(1)  31(1)  52(1)不替换
        if (!filename.contains("11(") && !filename.contains("12(") && !filename.contains("15(") && !filename.contains("31(") && !filename.contains("52(")){
            if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
                String name1, name2;
                name1 = filename.substring(0, filename.lastIndexOf('('));
                name2 = filename.substring(filename.lastIndexOf('('));
                name2 = name2.replace('(', '<');
                name2 = name2.replace(')', '>');
                filename = name1 + name2;
            }
        }

        // 提取时间
        String name_without_year = metadataServiceImpl.timeService(filename).get("name_without_year");
        String name_after_year = metadataServiceImpl.timeService(filename).get("name_after_year");

        // 类型复杂，直接匹配  特殊：01 | 11(1) | ""(空) | E2871I（1:5万); 其余需要处理
        String new_name_str = "";   //为之后拼幅做迭代
        if (filename.contains("-")){
            if (name_without_year.contains(".") || name_without_year.contains(",")){  //说明是拼幅
                String[] blocks ;
                if(name_without_year.contains(".")){
                    blocks = name_without_year.split("\\.");
                }
                else{
                    blocks = name_without_year.split("\\,");
                }
                for (String b : blocks) {
                    if (b.contains("-")) { //说明有完整的图幅
                        String[] parts = b.split("-");
                        String block_name;
                        if (parts.length == 5) {          // M-04-03-08-D
                            block_name = get_tufu_name(parts, "USA");
                            if (!new_name_str.equals("")) {
                                new_name_str = new_name_str + '.' + block_name;    //拼接分开的图幅
                            } else {
                                new_name_str = block_name;
                            }
                        }
                        if (parts.length == 4) {
                            String last_Char = parts[3];
                            Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                            if (pattern.matcher(last_Char).matches()) {           // 最后一位都是数字 L-12-11-06
                                block_name = get_tufu_name(parts, "USA");
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;    //拼接分开的图幅
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {    // 最后一位不全为数字，不做处理直接匹配 N-3-3-B3 H-13-9-VI
                                new_name_str = name_without_year;
                                break;
                            }
                        }
                        else {  // 复杂类型直接匹配
                            new_name_str = name_without_year;
                            break;
                        }
                    }
                    //说明没有完整的，只是最后一个数字
                    else {
                        new_name_str = new_name_str + '.' + b;
                    }
                }
            }
            //没有拼幅情况
            else {
                String[] parts = name_without_year.split("-");
                if (parts.length == 5) {          // M-04-03-08-D
                    new_name_str = get_tufu_name(parts, "USA");
                }
                if (parts.length == 4) {
                    String last_Char = parts[3];
                    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                    if (pattern.matcher(last_Char).matches()) {           // 最后一位都是数字 L-12-11-06
                        new_name_str = get_tufu_name(parts, "USA");
                    } else {    // 最后一位不全为数字，不做处理直接匹配 N-3-3-B3 H-13-9-VI
                        new_name_str = name_without_year;
                    }
                }
                else {  // 复杂类型直接匹配
                    new_name_str = name_without_year;
                }

            }
        }
        else {
            new_name_str = name_without_year;  // 类型复杂，直接匹配  特殊：01 | 11(1) | ""(空) | E2871I（1:5万);
        }

        //获取包括时间的标准化图幅名
        if (!new_name_str.equals("")) {
            formatFilename = new_name_str + name_after_year;
        }
        if (formatFilename == null){
            return null;
        }

        // 数据匹配
        return metadataServiceImpl.matchData("图号","numAndYear","",excelPath,formatFilename,collection);
    }

    // 台 （06-50-012-C-1-(4) | 0124-IV SW）
    public Map<String, Object> getTaiwanMetadata(String filename, String collection, String excelPath) throws Exception{
        String newName = filename.replaceAll("-","");
        int Num = filename.length() - newName.length();
        if (Num == 5){
            Map<String,String> mapInfo = new HashMap<>();
            mapInfo.put("type","ONE");          // 和1w同种规范的命名
            mapInfo.put("matchField","图幅编号");
            mapInfo.put("matchFieldAndYear","numAndYear");
            return metadataServiceImpl.getBSMMetadata(filename,mapInfo, collection, "", excelPath);
        }else {
            filename = checkName(filename, "tw");
        }
        return metadataServiceImpl.matchData("图幅编号","numAndYear","",excelPath,filename,collection);
    }
}
