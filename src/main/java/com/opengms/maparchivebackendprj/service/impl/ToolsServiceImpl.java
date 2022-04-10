package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.impl.MetadataDaoImpl;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.CheckDTO;
import com.opengms.maparchivebackendprj.service.ToolsService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @Description
 * @Author bin
 * @Date 2022/03/21
 */
@Slf4j
@Service
public class ToolsServiceImpl implements ToolsService {

    @Autowired
    MetadataDaoImpl metadataDao;
    
    @Autowired
    GenericServiceImpl genericService;

    @Autowired
    MetadataServiceImpl metadataService;

    @Value("${resourcePath}")
    private String resourcePath;
    
    
    public JsonResult statisticsMatchCount(CheckDTO checkDTO) {
        int totalFileNum = 0;
        int formatFileNum = 0; //文件名规范的数量
        int matchFileNum = 0; //匹配上的文件数量
        int notMatchFileNum = 0; //未匹配上的文件数量
        int notMatchFileNumMulti = 0; //未匹配上的多个相同时间的文件数量
        int notMatchFileNumOption = 0; //未匹配上的多个不同时间的文件数量
        int error_name1 = 0; //未匹配上的多个不同时间的文件数量
        int error_part1 = 0; //未匹配上的多个不同时间的文件数量

        //获取数据库中相应比例尺下的全部数据
        List<JSONObject> bsmMetadata = new ArrayList<>();
        String mapCLSId = checkDTO.getMapCLSId();
        String checkScale = null;
        switch (mapCLSId){
            case "ea07a0e4-642d-46ee-b375-d58fa881f552":
                checkScale = "1:2.5万";
                break;
            case "1262f876-d5f4-4406-8bc7-177f9f89cd38":
                checkScale = "1:5万";
                break;
            case "87086982-5ab1-473e-a65c-c010958f3ef3":
                checkScale = "1:10万";
                break;
            case "e8b7b805-cf9e-439a-8fd6-37123e88736c":
                checkScale = "1:20万";
                break;
            case "751d4e74-97fe-4dbc-b4cf-a3f87deb4758":
                checkScale = "1:25万";
                break;
            case "cc52e3e6-1fa1-442c-8a01-1629588b29bd":
                checkScale = "1:50万";
                break;
            case "60d36541-e067-425a-a158-159cf0242306":
                checkScale = "1:100万";
                break;
        }
        bsmMetadata = metadataDao.findBSMMetadata(checkScale);
        List<String> Database_name = new ArrayList<>();
        List<String> Database_name_block_only = new ArrayList<>();
        Map<String,List<String>> Database_name_year_dict = new HashMap<>();

        for (JSONObject metadata : bsmMetadata) {
            String code1 = metadata.getString("原图幅编号");
            String code2 = metadata.getString("出版时间");
            Database_name.add(code1 + "(" + code2 + ")");
            Database_name_block_only.add(code1);
            //
            if(Database_name_year_dict.containsKey(code1)){
                Database_name_year_dict.get(code1).add(code2);
            }
            else{
                LinkedList<String> strings = new LinkedList<>();
                strings.add(code2);
                Database_name_year_dict.put(code1,strings);

            }
        }
//        System.out.println(Database_name_block_only);


        //List<String> allFile = getAllFile("D:\\地图档案文件名\\已查", false);
        List<List<String>> allFile = getAllFile(checkDTO.getCheckDir(), false);
        int a = 0;
        for (List<String> filenameList : allFile) {
            //循环读取文件，把该文件中的图片名存到临时数组中
            //String filePath = allFile.get(0);
            totalFileNum += filenameList.size();
            Map<String, List<String>> map = new HashMap<>();
            //检查文件是否规范
            map = checkFilenameFormat(filenameList, checkScale);

            List<String> Image_name = map.get("Image_name");
            List<String> error_name = map.get("error_name");
            List<String> error_part = map.get("error_part");

            formatFileNum += Image_name.size();

            Map<String, List<String>> map1 = matchMetadata(Image_name, Database_name, Database_name_block_only, Database_name_year_dict);
            List<String> Match_Error = map1.get("Match_Error");
            List<String> Match_Error_Multi = map1.get("Match_Error_Multi");
            List<String> Match_OK = map1.get("Match_OK");
            List<String> Match_Error_Options = map1.get("Match_Error_Options");
            matchFileNum += Match_OK.size();
            notMatchFileNum += Match_Error.size();
            notMatchFileNumMulti += Match_Error_Multi.size();
            notMatchFileNumOption += Match_Error_Options.size();
            error_name1 += error_name.size();
            error_part1 += error_part.size();
            a++;
        }
        JSONObject result = new JSONObject();
        result.put("totalFileNum", totalFileNum);
        result.put("formatFileNum", formatFileNum);
        result.put("matchFileNum", matchFileNum);
        result.put("notMatchFileNum", notMatchFileNum);
        result.put("notMatchFileNumMulti", notMatchFileNumMulti);
        result.put("notMatchFileNumOption", notMatchFileNumOption);
        result.put("error_name", error_name1);
        result.put("error_part", error_part1);


        return ResultUtils.success(result);

    }

    //将文件名与Excel的字段进行匹配
    Map<String, List<String>> matchMetadata(List<String> Image_name, List<String> Database_name, List<String> Database_name_block_only, Map<String,List<String>> Database_name_year_dict) {
        List<String> Match_Error = new ArrayList<>();           //图幅信息错误
        List<String> Match_Error_Multi = new ArrayList<>();     //多个不同匹配日期
        List<String> Match_OK = new ArrayList<>();              //匹配成功
        List<String> Match_Error_Options = new ArrayList<>();    //多个相同的匹配时间

        for (String item : Image_name) {

            if (!Database_name.contains(item)) { //跟原图幅编号+年份的不匹配
                if (Database_name_block_only.contains(item)) {
                    int frequency = Collections.frequency(Database_name_block_only, item);
                    if (frequency == 1) { //只有唯一的才是成功的
                        Match_OK.add(item);
                    }
                    else {
                        if(Database_name_year_dict.containsKey(item)){
                            List<String> strings = Database_name_year_dict.get(item);
                            Set<String> set=new HashSet<>(strings);
                            if (set.size()==1){
                                Match_Error_Multi.add(item + ":" + strings);
                            }else {
                                Match_Error_Options.add(item + ":" + strings);
                            }
                        }
                    }
                    continue;
                }

                if (item.indexOf('<') > 0) {
                    String name_without_idx;
                    name_without_idx = item.substring(0, item.indexOf('<'));
                    int frequency = Collections.frequency(Database_name, name_without_idx);
                    if (frequency == 1) {
                        Match_OK.add(item);
                        continue;
                    }
                    else item = name_without_idx;
                }

                String name_without_year;
                //分开原图幅编号和时间
                int first_brace = item.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
                int second_brace = item.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
                int potential_year_count = second_brace - first_brace + 1;
                if (first_brace != -1 && potential_year_count == 6) {
                    name_without_year = item.substring(0, first_brace);    //图幅编号
                }
                else {
                    name_without_year = item;
                }
                if(Database_name_year_dict.containsKey(name_without_year)){
                    List<String> strings = Database_name_year_dict.get(name_without_year);
                    if(strings.size()>1){
                        Set<String> set = new HashSet<>(strings);
                        if (set.size() == 1) {
                            Match_Error_Multi.add(item + ":" + strings);
                        }
                        else {
                            Match_Error_Options.add(item + ":" + strings);
                        }
                    }
                    else {
                        Match_Error.add(item);
                    }
                }
                else{
                    Match_Error.add(item);
                }
            }
            else {  //跟原图幅编号+年份的匹配
                int frequency = Collections.frequency(Database_name, item);

                if (frequency == 1) {
                    Match_OK.add(item);
                }
                else {
                    //分开原图幅编号和时间
                    int first_brace = item.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
                    int second_brace = item.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
                    item = item.substring(0, first_brace);    //图幅编号
                    if(Database_name_year_dict.containsKey(item)) {
                        List<String> strings = Database_name_year_dict.get(item);
                        Set<String> set = new HashSet<>(strings);
                        if (set.size() == 1) {
                            Match_Error_Multi.add(item + ":" + strings);
                        } else {
                            Match_Error_Options.add(item + ":" + strings);
                        }
                    }
                    else Match_Error_Multi.add(item);
                }
            }

        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("Match_Error", Match_Error);
        map.put("Match_Error_Multi", Match_Error_Multi);
        map.put("Match_OK", Match_OK);
        map.put("Match_Error_Options", Match_Error_Options);

        return map;


    }

    //检查文件是否规范
    //@Test
//    Map<String, List<String>> checkFilenameFormat10w(List<String> filenameList){
//
//        //09-48-143
//        //07-49-1
//        //09-51-36,24
//        //07-49-7(1978)
//        //09-51-136(1958)
//        //07-49-51
//        //07-49-50
//        //05-49-50.62(1970 )
//        //05-49-63(1970 )
//        //05-49-133.121(1952)
//        //03-50-120.3-51-109
//
//        //String filename = "07-49-7(1978)(1)";
//
//        List<String> Image_name = new ArrayList<>();  //正确的列表
//        List<String> error_name = new ArrayList<>();  //错误的文件名 没有文件类型后缀
//        List<String> error_part = new ArrayList<>();  //不符合规范的文件名
//
//        for (String filename : filenameList) {
//            if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
//                error_name.add(filename);
//            }
//
//            // 得到文件名
//            filename = filename.substring(0, filename.lastIndexOf("."));
//
//            filename = get_english_name(filename);
//
//            String name_without_year;
//            String name_after_year;
//            int i = filename.indexOf("("); //找到从左到右第一个左括号，认为前面是图幅，后面是年份
//            if (i != -1){
//                // 有带年份的
//                name_without_year = filename.substring(0,i); //图幅名称
//                name_after_year = filename.substring(i);  //除了图幅之外的，年份名称，有可能有(1)(2)之类的
//            }
//            else {
//                name_without_year = filename;
//                name_after_year = "";
//            }
//
//            String new_name_str = "";
//
//            if (name_without_year.contains(".")||name_without_year.contains(",")){  //说明是拼幅
//                String[] blocks ;
//                if(name_without_year.contains(".")){
//                    blocks = name_without_year.split("\\.");
//                }
//                else{
//                    blocks = name_without_year.split("\\,");
//                }
//                for (String b : blocks) {
//                    if (b.contains("-")){ //说明有完整的图幅
//                        String[] parts = b.split("-");
//                        if (parts.length == 3){
//                            String block_name = TransformUtils.get_tufu_10w_name(parts[0],parts[1],parts[2]);
//                            if (!new_name_str.equals("")) {
//                                new_name_str = new_name_str + '.' + block_name;
//                            }
//                            else {
//                                new_name_str = block_name;
//                            }
//                        } else {
//                            error_part.add(filename);
//                            new_name_str = "";
//                            //continue;
//                            break; //这里用break
//                        }
//                    }
//                    else { //说明没有完整的，只是最后一个数字
//                        String block_name = TransformUtils.leftPad(3,b);
//                        new_name_str = new_name_str + '.' + block_name;
//                    }
//
//                }
//
//            }
//
//            else {
//                String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
//                if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
//                    error_part.add(filename);
//                    continue;
//                }
//                new_name_str = TransformUtils.get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);
//
//            }
//            if (!new_name_str.equals("")) {
//                Image_name.add(new_name_str + name_after_year);
//            }
//        }
//
//
//        System.out.println();
//
//        Map<String,List<String>> map = new HashMap<>();
//        map.put("Image_name",Image_name);
//        map.put("error_name",error_name);
//        map.put("error_part",error_part);
//
//        return map;
//
//
//    }

//    Map<String, List<String>> checkFilenameFormat5w(List<String> filenameList){
//
//        List<String> Image_name = new ArrayList<>();  //正确的列表
//        List<String> error_name = new ArrayList<>();  //错误的文件名 没有文件类型后缀
//        List<String> error_part = new ArrayList<>();  //不符合规范的文件名
//
//        for (String filename : filenameList) {
//            if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
//                error_name.add(filename);
//            }
//            // 得到文件名
//            filename = filename.substring(0, filename.lastIndexOf("."));
//            filename = get_english_name(filename);
//
//            String name_without_year;
//            String name_after_year;
//            int i = filename.indexOf("("); //找到从左到右第一个左括号，认为前面是图幅，后面是年份
//            if (i != -1){
//                // 有带年份的
//                name_without_year = filename.substring(0,i); //图幅名称
//                name_after_year = filename.substring(i);  //除了图幅之外的，年份名称，有可能有(1)(2)之类的
//            }
//            else {
//                name_without_year = filename;
//                name_after_year = "";
//            }
//            String new_name_str = "";
//            if (name_without_year.contains(".")||name_without_year.contains(",")){  //说明是拼幅
//                String[] blocks ;
//                if(name_without_year.contains(".")){
//                    blocks = name_without_year.split("\\.");
//                }
//                else{
//                    blocks = name_without_year.split("\\,");
//                }
//                for (String b : blocks) {
//                    if (b.contains("-")){ //说明有完整的图幅
//                        String[] parts = b.split("-");
//                        if (parts.length == 4){
//                            String block_name = TransformUtils.get_tufu_5w_name(parts[0],parts[1],parts[2],parts[3]);
//                            if (!new_name_str.equals("")) {
//                                new_name_str = new_name_str + '.' + block_name;
//                            }
//                            else {
//                                new_name_str = block_name;
//                            }
//                        } else {
//                            error_part.add(filename);
//                            new_name_str = "";
//                            //continue;
//                            break; //这里用break
//                        }
//                    }
//                    else { //说明没有完整的，只是最后一个字母
//                        new_name_str = new_name_str + '.' + b;
//                    }
//
//                }
//
//            }
//
//            else {
//                String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
//                if (part_list.length != 4) { // 5W的数据，应该就是四个部分，不然也是错的
//                    error_part.add(filename);
//                    continue;
//                }
//                new_name_str = TransformUtils.get_tufu_5w_name(part_list[0], part_list[1], part_list[2], part_list[3]);
//
//            }
//            if (!new_name_str.equals("")) {
//                Image_name.add(new_name_str + name_after_year);
//            }
//        }
//
//
//        System.out.println();
//
//        Map<String,List<String>> map = new HashMap<>();
//        map.put("Image_name",Image_name);
//        map.put("error_name",error_name);
//        map.put("error_part",error_part);
//
//        return map;
//
//
//    }

    //    Map<String, List<String>> checkFilenameFormat2Dot5w(List<String> filenameList){
//
//        List<String> Image_name = new ArrayList<>();  //正确的列表
//        List<String> error_name = new ArrayList<>();  //错误的文件名 没有文件类型后缀
//        List<String> error_part = new ArrayList<>();  //不符合规范的文件名
//
//        for (String filename : filenameList) {
//            if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
//                error_name.add(filename);
//            }
//
//            // 得到文件名
//            filename = filename.substring(0, filename.lastIndexOf("."));
//
//            filename = get_english_name(filename);
//
//            String name_without_year;
//            String name_after_year;
//            int i = filename.indexOf("("); //找到从左到右第一个左括号，认为前面是图幅，后面是年份
//            if (i != -1){
//                // 有带年份的
//                name_without_year = filename.substring(0,i); //图幅名称
//                name_after_year = filename.substring(i);  //除了图幅之外的，年份名称，有可能有(1)(2)之类的
//            }
//            else {
//                name_without_year = filename;
//                name_after_year = "";
//            }
//
//            String new_name_str = "";
//            if (name_without_year.contains(".")||name_without_year.contains(",")){  //说明是拼幅
//                String[] blocks ;
//                if(name_without_year.contains(".")){
//                    blocks = name_without_year.split("\\.");
//                }
//                else{
//                    blocks = name_without_year.split("\\,");
//                }
//                for (String b : blocks) {
//                    if (b.contains("-")){ //说明有完整的图幅
//                        String[] parts = b.split("-");
//                        if (parts.length == 5){
//                            String block_name = TransformUtils.get_tufu_2Dot5w_name(parts[0],parts[1],parts[2],parts[3],parts[4]);
//                            if (!new_name_str.equals("")) {
//                                new_name_str = new_name_str + '.' + block_name;
//                            }
//                            else {
//                                new_name_str = block_name;
//                            }
//                        } else {
//                            error_part.add(filename);
//                            new_name_str = "";
//                            //continue;
//                            break; //这里用break
//                        }
//                    }
//                    else { //说明没有完整的，只是最后一个数字
//                        String block_name = TransformUtils.leftPad(3,b);
//                        new_name_str = new_name_str + '.' + block_name;
//                    }
//
//                }
//
//            }
//
//            else {
//                String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
//                if (part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
//                    error_part.add(filename);
//                    continue;
//                }
//                new_name_str = TransformUtils.get_tufu_2Dot5w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4]);
//
//            }
//            if (!new_name_str.equals("")) {
//                Image_name.add(new_name_str + name_after_year);
//            }
//        }
//
//
//        System.out.println();
//
//        Map<String,List<String>> map = new HashMap<>();
//        map.put("Image_name",Image_name);
//        map.put("error_name",error_name);
//        map.put("error_part",error_part);
//
//        return map;
//
//
//    }
    //文件名格式化
    Map<String, List<String>> checkFilenameFormat(List<String> filenameList, String checkScale) {
        //09-48-143
        //07-49-1
        //09-51-36,24
        //07-49-7(1978)
        //09-51-136(1958)
        //07-49-51
        //07-49-50
        //05-49-50.62(1970 )
        //05-49-63(1970 )
        //05-49-133.121(1952)
        //03-50-120.3-51-109
        //String filename = "07-49-7(1978)(1)";

        List<String> Image_name = new ArrayList<>();    //正确的列表
        List<String> error_name = new ArrayList<>();    //错误的文件名 没有文件类型后缀
        List<String> error_part = new ArrayList<>();    //不符合规范的文件名
        for (String filename : filenameList) {
            if (!filename.contains(".")) {    //如果一个.都没有，说明文件名不对
                error_name.add(filename);
                continue;
            }
            //得到文件名
            filename = filename.substring(0, filename.lastIndexOf('.')); //出去文件名后缀
            filename = get_english_name(filename);

            //将(1)(2)(3)(4)转换为<1><2><3><4>,以免和年份混淆
            if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)")) {
                String name1, name2;
                name1 = filename.substring(0, filename.lastIndexOf('('));
                name2 = filename.substring(filename.lastIndexOf('('));
                name2 = name2.replace('(', '<');
                name2 = name2.replace(')', '>');
                filename = name1 + name2;
            }

            String name_without_year;
            String name_after_year;
            //分开原图幅编号和时间
            int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
            int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
            int potential_year_count = second_brace - first_brace + 1;
            if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1){
                error_name.add(filename);
                continue;
            }
            if (first_brace != -1 && potential_year_count == 6) {
                name_without_year = filename.substring(0, first_brace);    //图幅编号
                name_after_year = filename.substring(first_brace);    //除了图幅之外的，年份名称，有可能有(1)(2)之类的
            } else {
                name_without_year = filename;
                name_after_year = "";
            }

            //去除name_without_year中<>情况
            int i = name_without_year.indexOf('<');
            if (i != -1) {      //存在<, 即上一步条件中不存在时间
                String name_without_idx;
                name_without_idx = name_without_year.substring(0, i);       //完全不包含时间和(1)(2)的图幅编号
                name_after_year = name_without_year.substring(i) + name_after_year;     //按道理name_after_year一开始应该是空的
                name_without_year = name_without_idx;
            }

            //进行文件名称规范判别
            String new_name_str = "";   //为之后拼幅做迭代
            if (name_without_year.contains(".") || name_without_year.contains(",")) { //说明是拼幅
                String[] blocks;
                if (name_without_year.contains(".")) {
                    blocks = name_without_year.split("\\.");    //将拼幅内容分开
                } else {
                    blocks = name_without_year.split(",");
                }
                for (String b : blocks) {
                    if (b.contains("-")) {
                        String[] parts = b.split("-");
                        switch (checkScale) {
                            case "1:100万":
                                if (parts.length == 2) {
                                    String block_name = metadataService.get_tufu_100w_name(parts[0], parts[1]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:50万":
                                if (parts.length == 3) {
                                    String block_name = metadataService.get_tufu_50w_name(parts[0], parts[1], parts[2]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:25万":
                                if (parts.length == 3) {
                                    int first_square_bracket;
                                    int second_square_bracket;
                                    first_square_bracket = parts[2].indexOf('[');
                                    second_square_bracket = parts[2].indexOf(']');
                                    if (second_square_bracket < 0) {
                                        second_square_bracket = parts[2].length();
                                    }
                                    parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                    String block_name = metadataService.get_tufu_25w_name(parts[0], parts[1], parts[2]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:20万":
                                if (parts.length == 3) {
                                    int first_square_bracket;
                                    int second_square_bracket;
                                    first_square_bracket = parts[2].indexOf('(');
                                    second_square_bracket = parts[2].indexOf(')');
                                    if (second_square_bracket < 0) {
                                        second_square_bracket = parts[2].length();
                                    }
                                    parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                    String block_name = metadataService.get_tufu_20w_name(parts[0], parts[1], parts[2]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:10万":
                                if (parts.length == 3) {
                                    String block_name = metadataService.get_tufu_10w_name(parts[0], parts[1], parts[2]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:5万":
                                if (parts.length == 4) {
                                    String block_name = metadataService.get_tufu_5w_name(parts[0], parts[1], parts[2], parts[3]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                            case "1:2.5万":
                                if (parts.length == 5) {
                                    String block_name = metadataService.get_tufu_2Dot5w_name(parts[0], parts[1], parts[2], parts[3], parts[4]);
                                    if (!new_name_str.equals("")) {
                                        new_name_str = new_name_str + '.' + block_name;
                                    } else {
                                        new_name_str = block_name;
                                    }
                                } else {
                                    error_part.add(filename);
                                    new_name_str = "";
                                }
                                break;
                        }
                    } else { //说明没有完整的，只是最后一个数字
                        int first_square_bracket;
                        int second_square_bracket;
                        if (checkScale == "1:10万") {
                            String block_name = metadataService.leftPad(3, b);
                            new_name_str = new_name_str + '.' + block_name;
                        } else if (checkScale == "1:100万") {
                            String block_name = metadataService.leftPad(2, b);
                            new_name_str = new_name_str + '.' + block_name;
                        } else if (checkScale == "1:25万") {
                            first_square_bracket = b.indexOf('[');
                            second_square_bracket = b.indexOf(']');
                            if (second_square_bracket < 0) {
                                second_square_bracket = b.length();
                            }
                            b = b.substring(first_square_bracket + 1, second_square_bracket);
                            String block_name = metadataService.leftPad(2, b);
                            new_name_str = new_name_str + ".[" + block_name + ']';
                        } else if (checkScale == "1:20万") {
                            first_square_bracket = b.indexOf('(');
                            second_square_bracket = b.indexOf(')');
                            if (second_square_bracket < 0) {
                                second_square_bracket = b.length();
                            }
                            b = b.substring(first_square_bracket + 1, second_square_bracket);
                            String block_name = metadataService.leftPad(2, b);
                            new_name_str = new_name_str + ".(" + block_name + ')';
                        } else {
                            new_name_str = new_name_str + '.' + b;
                        }
                    }
                }

            }
            else {
                String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
                int first_square_bracket;
                int second_square_bracket;
                switch (checkScale) {
                    case "1:100万":
                        if (part_list.length != 2) { // 100W的数据，应该就是两个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        new_name_str = metadataService.get_tufu_100w_name(part_list[0], part_list[1]);
                        break;
                    case "1:50万":
                        if (part_list.length != 3) { // 50W的数据，应该就是三个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        new_name_str = metadataService.get_tufu_50w_name(part_list[0], part_list[1], part_list[2]);
                        break;
                    case "1:25万":
                        if (part_list.length != 3) { // 25W的数据，应该就是三个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        first_square_bracket = part_list[2].indexOf('[');
                        second_square_bracket = part_list[2].indexOf(']');
                        part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                        new_name_str = metadataService.get_tufu_25w_name(part_list[0], part_list[1], part_list[2]);
                        break;
                    case "1:20万":
                        if (part_list.length != 3) { // 20W的数据，应该就是三个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        first_square_bracket = part_list[2].indexOf('(');
                        second_square_bracket = part_list[2].indexOf(')');
                        part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                        new_name_str = metadataService.get_tufu_20w_name(part_list[0], part_list[1], part_list[2]);
                        break;
                    case "1:10万":
                        if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        new_name_str = metadataService.get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);
                        break;
                    case "1:5万":
                        if (part_list.length != 4) { // 5W的数据，应该就是四个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        new_name_str = metadataService.get_tufu_5w_name(part_list[0], part_list[1], part_list[2], part_list[3]);
                        break;
                    case "1:2.5万":
                        if (part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
                            error_part.add(filename);
                            continue;
                        }
                        new_name_str = metadataService.get_tufu_2Dot5w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4]);
                        break;
                }
            }
            if (!new_name_str.equals("")) {
                Image_name.add(new_name_str + name_after_year);
            }
        }
        System.out.println();

        Map<String, List<String>> map = new HashMap<>();
        map.put("Image_name", Image_name);
        map.put("error_name", error_name);
        map.put("error_part", error_part);

        return map;

    }

    //得到文件名
    String get_english_name(String name) {
        name = name.replace('（', '(');
        name = name.replace('）', ')');
        name = name.replace('，', ',');
        name = name.replace('、', ',');
        name = name.replace(" ", "");
        return name;
    }


    /**
     * @Description: 使用 Apache Commons IO 流逐行读取文件
     * Maven 依赖:
     * <!-- https://mvnrepository.com/artifact/commons-io/commons-io -->
     * <dependency>
     * <groupId>commons-io</groupId>
     * <artifactId>commons-io</artifactId>
     * <version>2.6</version>
     * </dependency>
     * @Param: [filePath] 文件路径
     * @Author: Seven-Steven
     * @Date: 19-1-24
     **/
    public List<String> apacheCommonsIoReadFile(String filePath) {

        List<String> filenameList = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            LineIterator iterator = org.apache.commons.io.FileUtils.lineIterator(file, "UTf-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                filenameList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filenameList;
    }

    /**
     * 获取路径下的所有文件/文件夹
     *
     * @param directoryPath  需要遍历的文件夹路径
     * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
     * @return
     */
//    public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {
//        List<String> list = new ArrayList<>();
//        File baseFile = new File(directoryPath);
//        if (baseFile.isFile() || !baseFile.exists()) {
//            return list;
//        }
//        File[] files = baseFile.listFiles();
//        for (File file : files) {
//            if (file.isDirectory()) {
//                if (isAddDirectory) {
//                    list.add(file.getAbsolutePath());
//                }
//                list.addAll(getAllFile(file.getAbsolutePath(), isAddDirectory));
//            } else {
//                list.add(file.getAbsolutePath());
//            }
//        }
//        return list;
//    }
    public static List<List<String>> getAllFile(String directoryPath, boolean isAddDirectory) {
        List<String> list = new ArrayList<>();
        List<List<String>> nameList = new ArrayList<>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return nameList;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            File[] items = file.listFiles();
            list.clear();
            for (File item : items){

                list.add(item.getName());
            }
            List<String> l = new ArrayList<>(list);
            nameList.add(l);
//            nameList.add(list);
        }
        return nameList;
    }

}


