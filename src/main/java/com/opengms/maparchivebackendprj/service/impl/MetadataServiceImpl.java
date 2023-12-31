package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.dto.ExcelPathDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IMatchDataService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import com.sun.javafx.scene.transform.TransformUtils;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

import static com.opengms.maparchivebackendprj.entity.enums.MapClassification.BASIC_SCALE_MAP_TEN;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
public class MetadataServiceImpl implements IMetadataService {


    @Autowired
    IMetadataDao metadataDao;

    @Autowired
    IGenericService genericService;

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Autowired
    IMatchDataService matchDataService;

    @Override
    public Map<String, Object> getMetadataByFilenameByType(String filename, String mapCLSId, String mapType, String excelPath) {
        MetadataTable metadataTable = metadataTableDao.findById(mapCLSId);
        String collection = metadataTable.getCollection();
        try {
            if (collection.contains("BASIC_SCALE_MAP")){        // 符合基本比例尺命名作为一个大类匹配
                return getBSMMetadata(filename,collection,mapType,excelPath);
            }else {     // 其余地图按新添的规则进行分类
                return matchDataService.getMetadataByFilenameByTypeForOther(filename, mapCLSId, mapType, excelPath);
            }
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public JsonResult getMetadataByExcel(ExcelPathDTO excelPathDTO) {
        String excelPath = excelPathDTO.getExcelPath();
        String fileType = FileUtils.getFileType(excelPath);
        if (!(fileType.equals("xls") || fileType.equals("xlsx")))
            return ResultUtils.error("请传入xls或者xlsx格式的文件");
        try {
            List<Map<String, Object>> maps = FileUtils.redExcel(excelPath);
            return ResultUtils.success(maps);
        }catch (Exception e){
            return ResultUtils.error("解析excel出错，excel默认第一行为标题行");
        }
    }

    @Override
    public JsonResult getMetadata(SpecificFindDTO findDTO, String collection) {

        Pageable pageable = genericService.getPageable(findDTO);

        // findDTO.setCurQueryField("档号");

        List<JSONObject> metadataList = metadataDao.findMetadataBySearchText(findDTO.getCurQueryField(),findDTO.getSearchText(),collection,pageable);

        // long count = metadataDao.countMetadataBySearchText(findDTO.getCurQueryField(),findDTO.getSearchText(),collection);

        return ResultUtils.success(metadataList);
    }

    @Override
    public JsonResult countMetadata(SpecificFindDTO findDTO, String collection) {

        long count = metadataDao.countMetadataBySearchText(findDTO.getCurQueryField(),findDTO.getSearchText(),collection);

        return ResultUtils.success(count);

    }

    //得到文件名
    @Override
    public String get_english_name(String name){
        name = name.replace('（', '(');
        name = name.replace('）', ')');
        name = name.replace('，', ',');
        name = name.replace('、', ',');
        name = name.replace(" ","");
        return name;
    }

    /**
     * 向左补齐不足长度 3->003
     * @param length 长度
     * @param number 数字
     * @return
     */
    public static String leftPad(int length, int number) {
        String f = "%0" + length + "d";
        return String.format(f, number);
    }

    public static String leftPad(int length, String number) {
        int num = Integer.parseInt(number);
        //String f = "%0" + length + "d";
        //return String.format(f, num);
        return leftPad(length,num);
    }

    public static String get_tufu_100w_name(int number1, int number2) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);

        return   X + "-" + Y;
    }

    public static String get_tufu_100w_name(String number1, String number2) {
        try {
            String first_name = number1.substring(0,1);
            if (first_name.equals("S") && number1.length() == 3){           //  # 100w中有 S06-40
                int num2 = Integer.parseInt(number2);
                number2 = leftPad(2,num2);
                return number1 + "-" + number2;
            }
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            return get_tufu_100w_name(num1,num2);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_50w_name(int number1, int number2, String number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);

        return   X + "-" + Y + "-" + number3;
    }

    public static String get_tufu_50w_name(String number1, String number2, String number3) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);

            return get_tufu_50w_name(num1,num2,number3);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_25w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(2,number3);

        return   X + "-" + Y + "-" + "[" + Z + "]";
    }

    public static String get_tufu_25w_name(String number1, String number2, String number3) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_25w_name(num1,num2,num3);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_20w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(2,number3);

        return   X + "-" + Y + "-" + "(" + Z + ")";
    }

    public static String get_tufu_20w_name(String number1, String number2, String number3) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_20w_name(num1,num2,num3);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_10w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z;
    }

    public static String get_tufu_10w_name(String number1, String number2, String number3) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_10w_name(num1,num2,num3);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_5w_name(int number1, int number2, int number3, String number4) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z + "-" + number4;
    }

    public static String get_tufu_5w_name(String number1, String number2, String number3, String number4) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_5w_name(num1,num2,num3,number4);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_2Dot5w_name(int number1, int number2, int number3, String number4, String number5) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        //String Y = String.format("%0" + 2 + "d", number2);
        //String Z = String.format("%0" + 3 + "d", number3);
        return   X + "-" + Y + "-" + Z + "-" + number4 + "-" + number5;
    }

    public static String get_tufu_2Dot5w_name(String number1, String number2, String number3, String number4, String number5) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_2Dot5w_name(num1,num2,num3,number4,number5);
        } catch (Exception e) {
            return null;
        }

    }

    public static String get_tufu_1w_name(int number1, int number2, int number3, String number4, String number5, String number6) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z + "-" + number4 + "-" + number5 + "-" + number6;
    }

    public static String get_tufu_1w_name(String number1, String number2, String number3, String number4, String number5, String number6) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_1w_name(num1,num2,num3,number4,number5,number6);
        } catch (Exception e) {
            return null;
        }
    }

    public static String get_tufu_2500_name(int number1, int number2, int number3, String number4, String number5, String number6, String number7, String number8) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z + "-" + number4 + "-" + number5 + "-" + number6 + "-" + number7 + "-" + number8;
    }

    public static String get_tufu_2500_name(String number1, String number2, String number3, String number4, String number5, String number6, String number7, String number8) {

        try {
            int num1 = Integer.parseInt(number1);
            int num2 = Integer.parseInt(number2);
            int num3 = Integer.parseInt(number3);

            return get_tufu_2500_name(num1,num2,num3,number4,number5,number6,number7,number8);
        } catch (Exception e) {
            return null;
        }

    }

    // 通用基础比例尺地图下的元数据匹配
//    public Map<String, Object> getBSMMetadata(String filename, String collection, String excelPath) throws Exception {
//        List<String> scaleArr = Arrays.asList("BASIC_SCALE_MAP_TWO_DOT_FIVE","BASIC_SCALE_MAP_FIVE","BASIC_SCALE_MAP_TEN","BASIC_SCALE_MAP_TWENTY","BASIC_SCALE_MAP_TWENTY_FIVE","BASIC_SCALE_MAP_FIFTY","BASIC_SCALE_MAP_HUNDRED");
//        // 基本比例尺 （原图幅编号 or 原图幅编号+年份）
//        if (!scaleArr.contains(collection)){
//            return null;
//        }
//        //checkFilenameFormat
//        String formatFilename = null;
//        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
//            // error_name.add(filename);
//            return null;
//        }
//        // 得到文件名
//        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀
//        filename = get_english_name(filename);
//
//        //将(1)(2)(3)(4)转换为<1><2><3><4>,以免和年份混淆
//        if(collection == "BASIC_SCALE_MAP_TWENTY"){
//            int oldCount = filename.length();
//            int newCount = filename.replace("(","").length();
//            if(oldCount - newCount>1){      //20w分幅最后有-(4)会造成干扰
//                if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
//                    String name1, name2;
//                    name1 = filename.substring(0, filename.lastIndexOf('('));
//                    name2 = filename.substring(filename.lastIndexOf('('));
//                    name2 = name2.replace('(', '<');
//                    name2 = name2.replace(')', '>');
//                    filename = name1 + name2;
//                }
//            }
//        }
//        else {
//            if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
//                String name1, name2;
//                name1 = filename.substring(0, filename.lastIndexOf('('));
//                name2 = filename.substring(filename.lastIndexOf('('));
//                name2 = name2.replace('(', '<');
//                name2 = name2.replace(')', '>');
//                filename = name1 + name2;
//            }
//        }
//
//        String name_without_year;
//        String name_after_year;
//        //分开为原图幅编号和时间编号
//        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
//        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
//        int potential_year_count = second_brace - first_brace + 1;
//        if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1 && potential_year_count !=4){      //除了(1990) (3) (12) 无括号
//            return null;
//        }
//        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
//            name_without_year = filename.substring(0, first_brace);    //图幅编号，不包含(1)(2)
//            name_after_year = filename.substring(first_brace);    //除了图幅之外的，年份名称，有可能有(1)(2)之类的
//        } else {
//            name_without_year = filename;       //没有时间，可能有(1)(2)之类的
//            name_after_year = "";
//        }
//
//        //去除name_without_year中<>情况
//        int i = name_without_year.indexOf('<');
//        if (i != -1) {      //存在<, 即上一步条件中不存在时间
//            String name_without_idx;
//            name_without_idx = name_without_year.substring(0, i);       //完全不包含时间和(1)(2)的图幅编号
//            name_after_year = name_without_year.substring(i) + name_after_year;     //按道理name_after_year一开始应该是空的
//            name_without_year = name_without_idx;
//        }
//        //进行文件名称规范判别
//        String new_name_str = "";   //为之后拼幅做迭代
//        if (name_without_year.contains(".") || name_without_year.contains(",")){  //说明是拼幅
//            String[] blocks ;
//            if(name_without_year.contains(".")){
//                blocks = name_without_year.split("\\.");
//            }
//            else{
//                blocks = name_without_year.split("\\,");
//            }
//            for (String b : blocks) {
//                if (b.contains("-")){ //说明有完整的图幅
//                    String[] parts = b.split("-");
//                    //按相应比例尺进行图幅处理
//                    switch (collection){
//                        case "BASIC_SCALE_MAP_HUNDRED":
//                            if (parts.length == 2) {
//                                String block_name = get_tufu_100w_name(parts[0], parts[1]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;    //拼接分开的图幅
//                                } else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }
//                            break;
//                        case "BASIC_SCALE_MAP_FIFTY":
//                            if (parts.length == 3) {
//                                String block_name = get_tufu_50w_name(parts[0], parts[1], parts[2]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                } else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }
//                            break;
//                        case "BASIC_SCALE_MAP_TWENTY_FIVE":
//                            if (parts.length == 3) {
//                                int first_square_bracket;
//                                int second_square_bracket;
//                                first_square_bracket = parts[2].indexOf('[');
//                                second_square_bracket = parts[2].indexOf(']');
//                                if (second_square_bracket < 0) {
//                                    second_square_bracket = parts[2].length();
//                                }
//                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
//                                String block_name = get_tufu_25w_name(parts[0], parts[1], parts[2]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                } else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }
//                            break;
//                        case "BASIC_SCALE_MAP_TWENTY":
//                            if (parts.length == 3) {
//                                int first_square_bracket;
//                                int second_square_bracket;
//                                first_square_bracket = parts[2].indexOf('(');
//                                second_square_bracket = parts[2].indexOf(')');
//                                if (second_square_bracket < 0) {
//                                    second_square_bracket = parts[2].length();
//                                }
//                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
//                                String block_name = get_tufu_20w_name(parts[0], parts[1], parts[2]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                } else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }
//                            break;
//                        case "BASIC_SCALE_MAP_TEN":
//                            if (parts.length == 3){
//                                String block_name = get_tufu_10w_name(parts[0],parts[1],parts[2]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                }
//                                else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }break;
//                        case "BASIC_SCALE_MAP_FIVE":
//                            if (parts.length == 4){
//                                String block_name = get_tufu_5w_name(parts[0],parts[1],parts[2],parts[3]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                }
//                                else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }break;
//                        case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
//                            if (parts.length == 5){
//                                String block_name = get_tufu_2Dot5w_name(parts[0],parts[1],parts[2],parts[3],parts[4]);
//                                if (!new_name_str.equals("")) {
//                                    new_name_str = new_name_str + '.' + block_name;
//                                }
//                                else {
//                                    new_name_str = block_name;
//                                }
//                            } else {
//                                return null;
//                            }break;
//                    }
//                }
//                //说明没有完整的，只是最后一个数字
//                else {
//                    int first_square_bracket;
//                    int second_square_bracket;
//                    if (collection == "BASIC_SCALE_MAP_TEN") {
//                        String block_name = leftPad(3, b);
//                        new_name_str = new_name_str + '.' + block_name;
//                    } else if (collection == "BASIC_SCALE_MAP_HUNDRED") {
//                        String block_name = leftPad(2, b);
//                        new_name_str = new_name_str + '.' + block_name;
//                    } else if (collection == "BASIC_SCALE_MAP_TWENTY_FIVE") {
//                        first_square_bracket = b.indexOf('[');
//                        second_square_bracket = b.indexOf(']');
//                        if (second_square_bracket < 0) {
//                            second_square_bracket = b.length();
//                        }
//                        b = b.substring(first_square_bracket + 1, second_square_bracket);
//                        String block_name = leftPad(2, b);
//                        new_name_str = new_name_str + ".[" + block_name + ']';
//                    } else if (collection == "BASIC_SCALE_MAP_TWENTY") {
//                        first_square_bracket = b.indexOf('(');
//                        second_square_bracket = b.indexOf(')');
//                        if (second_square_bracket < 0) {
//                            second_square_bracket = b.length();
//                        }
//                        b = b.substring(first_square_bracket + 1, second_square_bracket);
//                        String block_name = leftPad(2, b);
//                        new_name_str = new_name_str + ".(" + block_name + ')';
//                    } else {
//                        new_name_str = new_name_str + '.' + b;
//                    }
//                }
//            }
//        }
//        //没有拼幅情况
//        else {
//            String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
//            int first_square_bracket;
//            int second_square_bracket;
//            //按比例对文件名进行格式化
//            switch (collection){
//                case "BASIC_SCALE_MAP_HUNDRED":
//                    if (part_list.length != 2) { // 100W的数据，应该就是两个部分，不然也是错的
//                        return null;
//                    }
//                    new_name_str = get_tufu_100w_name(part_list[0], part_list[1]);
//                    break;
//                case "BASIC_SCALE_MAP_FIFTY":
//                    if (part_list.length != 3) { // 50W的数据，应该就是三个部分，不然也是错的
//                        return null;
//                    }
//                    new_name_str = get_tufu_50w_name(part_list[0], part_list[1], part_list[2]);
//                    break;
//                case "BASIC_SCALE_MAP_TWENTY_FIVE":
//                    if (part_list.length != 3) { // 25W的数据，应该就是三个部分，不然也是错的
//                        return null;
//                    }
//                    first_square_bracket = part_list[2].indexOf('[');
//                    second_square_bracket = part_list[2].indexOf(']');
//                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
//                    new_name_str = get_tufu_25w_name(part_list[0], part_list[1], part_list[2]);
//                    break;
//                case "BASIC_SCALE_MAP_TWENTY":
//                    if (part_list.length != 3) { // 20W的数据，应该就是三个部分，不然也是错的
//                        return null;
//                    }
//                    first_square_bracket = part_list[2].indexOf('(');
//                    second_square_bracket = part_list[2].indexOf(')');
//                    //不需要考虑first_square_bracket = -1，因为+1后还是从0开始切
//                    if(second_square_bracket < 0){
//                        second_square_bracket = part_list[2].length();
//                    }
//                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
//                    new_name_str = get_tufu_20w_name(part_list[0], part_list[1], part_list[2]);
//                    break;
//                case "BASIC_SCALE_MAP_TEN":
//                    if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
//                        return null;
//                    }
//                    new_name_str = get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);
//                    break;
//                case "BASIC_SCALE_MAP_FIVE":
//                    if (part_list.length != 4) { // 5W的数据，应该就是四个部分，不然也是错的
//                        return null;
//                    }
//                    new_name_str = get_tufu_5w_name(part_list[0], part_list[1], part_list[2], part_list[3]);
//                    break;
//                case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
//                    if (part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
//                        return null;
//                    }
//                    new_name_str = get_tufu_2Dot5w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4]);
//                    break;
//            }
//        }
//        //获取包括时间的标准化图幅名
//        if (!new_name_str.equals("")) {
//            // Image_name.add(new_name_str + name_after_year);
//            formatFilename = new_name_str + name_after_year;
//        }
//        if (formatFilename == null){
//            return null;
//        }
//
//        // 读excel表中的数据
//        if (excelPath != null && !excelPath.equals("")) {
//            List<Map<String, Object>> maps = FileUtils.redExcel(excelPath);
//            List<String> Database_name = new ArrayList<>();  //原图幅编号+年份
//            List<String> Database_name_block_only = new ArrayList<>(); //原图幅编号
//            Map<String,List<String>> Database_name_year_dict = new HashMap<>();//原图幅号和对应时间的集合
//            Map<String,List<String>> Database_name_style_dict = new HashMap<>();//原图幅号和对应版型的集合
//
//
//            int count = 0; //匹配上的次数
//            int index = -1; //列表索引
//
//            for (int j = 0; j < maps.size(); j++) {
//                String code1 = maps.get(j).get("原图幅编号").toString();
//                String code2 = maps.get(j).get("出版时间").toString();
//                String code3 = maps.get(j).get("版型").toString();
//
//                String format1 = code1;
//                String format2 = code1+"("+code2+")";
//                if(Database_name_year_dict.containsKey(code1)){
//                    Database_name_year_dict.get(code1).add(code2);
//                    Database_name_style_dict.get(code1).add(code3);
//                }
//                else{
//                    LinkedList<String> strings1 = new LinkedList<>();
//                    LinkedList<String> strings2 = new LinkedList<>();
//                    strings1.add(code2);
//                    strings2.add(code3);
//                    Database_name_year_dict.put(code1,strings1);
//                    Database_name_style_dict.put(code1,strings2);
//
//                }
//                Database_name.add(format2);
//                Database_name_block_only.add(format1);
//
//                // if (formatFilename.equals(format2) || formatFilename.equals(format1)){
//                //     count++;
//                //     index = j;
//                // }
//            }
//
//            if (!Database_name.contains(formatFilename)){ //跟原图幅编号+年份的不匹配
//                if (Database_name_block_only.contains(formatFilename)){
//                    int frequency = Collections.frequency(Database_name_block_only, formatFilename);
//                    if (frequency == 1){ //只有唯一的才是成功的
//                        return maps.get(Database_name_block_only.indexOf(formatFilename));
//                    }
//                    else {
//                        if(Database_name_year_dict.containsKey(formatFilename)){
//                            List<String> strings = Database_name_year_dict.get(formatFilename);
//                            Set<String> set=new HashSet<>(strings);
//                            if (set.size()==1 && strings.size()>1){ //多个相同的年份
//                                int time1 = Collections.frequency(Database_name_style_dict.get(formatFilename), '纸');
//                                if (time1 == 1){
//                                    int time2 = Database_name_style_dict.get(formatFilename).indexOf('纸');
//                                    int time3 = 0;
//                                    for(int a = 0; a < Database_name_year_dict.size(); a++){
//                                        if(Database_name_block_only.get(a) == formatFilename){
//                                            time3++;
//                                        }
//                                        if(time3 == time2+1){
//                                            return maps.get(a);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        return null;
//                    }
//                }
//                //特殊情况一：由于<>的存在影响匹配，删去处理
//                if (formatFilename.indexOf('<') > 0){
//                    String name_without_idx;
//                    name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
//                    int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));
//                    int frequency = Collections.frequency(Database_name, name_without_idx);
//                    if (frequency == 1) {   //考虑原图幅+时间匹配上的情况
//                        return maps.get(Database_name.indexOf(name_without_idx)) ;
//                    }
//                    else if(frequency > 1){
//                        int index1 = 0;
//                        for(int a = 0; a < Database_name.size(); a++){
//                            if (Database_name.get(a) == name_without_idx){
//                                Map<String, Object> item = maps.get(a);
//                                String type = item.get("版型").toString();
//                                if(type == "纸"){    //如果是纸则赋给<1>的图
//                                    if(num == 1){
//                                        return item;
//                                    }
//                                }else {     //不是纸，则赋给不为<1>的图
//                                    if (num != 1){
//                                        return item;
//                                    }
//                                }
//                                index1 = a;
//                            }
//
//                        }
//                        return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//                    }
//                    else {
//                        return null;
////
//                    }
//                }
//                //特殊情况二：时间出错，删去时间只对原图幅编号进行匹配
////                int frequency = Collections.frequency(Database_name, new_name_str);
////                List<Map<String,Object>> mapList = new ArrayList<>();
////                if(frequency > 0){
////                    if(frequency == 1){
////                        return maps.get(Database_name.indexOf(new_name_str));
////                    }
////                    else {
////                        final String string=new_name_str;
////
////                        for (int a = 0; a < Database_name.size(); a++) {
////                            if(Database_name.get(a) == new_name_str){
////                                mapList.add(maps.get(a));
////                            }
////                        }
////                        return null;
////                    }
////                }
//                return null;
//            }
//
//            else {  //跟原图幅编号+年份的匹配
//                int frequency = Collections.frequency(Database_name, formatFilename);
//                if (frequency == 1){
//                    return maps.get(Database_name.indexOf(formatFilename));
//                }
//                else if(frequency > 1){     //同一图幅多个相同时间，没有<>,默认不选纸
//                    int index1 = 0;
//                    for(int a = 0; a < Database_name.size(); a++){  //和带有时间的做对比
//                        if (Database_name.get(a) == formatFilename){
//                            Map<String, Object> item = maps.get(a);
//                            String type = item.get("版型").toString();
//                            if(type != "纸"){    //不是纸，则赋给不为<1>的图
//                                return item;
//                            }
//                            index1 = a;
//                        }
//
//                    }
//                    return maps.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//                }
//                return null;
//            }
//        }
//        //进行数据库的匹配
//        else {
//            // 先匹配 原图幅编号+年份 这一列
//            List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear(formatFilename,collection);
//            if (list.size() == 1)
//                return list.get(0);
//            if (list.size() > 1){
//                int index1 = 0;
//                for(int a = 0; a < list.size(); a++){
//                    if (list.get(a).get("版型").toString() != "纸"){
//                        return list.get(a);
//                    }
//                    index1 = a;
//                }
//                return list.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//            }
//            // 再匹配 原图幅编号这一列
//            List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(formatFilename,collection);
//            if (list1.size() == 1)
//                return list1.get(0);
//            List<String> strings = new ArrayList<>();
//            for(int a = 0; a < list1.size(); a++){
//                strings.add(list1.get(a).get("出版时间").toString());
//            }
//            Set<String> set=new HashSet<>(strings);
//            int c = list1.size();
//            int d = set.size();
//            if (list1.size() > 1 && set.size() == 1){
//                int index1 = 0;
//                for(int a = 0; a < list1.size(); a++){
//                    if (list1.get(a).get("版型").toString() != "纸"){
//                        return list1.get(a);
//                    }
//                    index1 = a;
//                }
//                return list1.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//            }
//            //匹配<>的情况
//            if (formatFilename.indexOf('<') > 0){
//                String name_without_idx;
//                name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
//                int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));
//                List<JSONObject> list3 = metadataDao.findMetadataByOriginalNumAndYear(name_without_idx,collection);
//                // 先匹配 原图幅编号+年份 这一列
//                if (list3.size() == 1)
//                    return list3.get(0);
//                if (list3.size() > 1){
//                    int index1 = 0;
//                    for(int a = 0; a < list3.size(); a++){
//                        if(list3.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
//                            if(num == 1){
//                                return list3.get(a);
//                            }
//                        }else {     //不是纸，则赋给不为<1>的图
//                            if (num != 1){
//                                return list3.get(a);
//                            }
//                        }
//                        index1 = a;
//                    }
//                    return list3.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//                }
//                // 再匹配 原图幅编号这一列
//                List<JSONObject> list4 = metadataDao.findMetadataByOriginalNum(name_without_idx,collection);
//                if (list4.size() == 1)
//                    return list4.get(0);
//                List<String> strings1 = new ArrayList<>();
//                for(int a = 0; a < list4.size(); a++){
//                    strings1.add(list4.get(a).get("出版时间").toString());
//                }
//                Set<String> set1=new HashSet<>(strings1);
//                if (list4.size() > 1 && set1.size() == 1){
//                    int index1 = 0;
//                    for(int a = 0; a < list4.size(); a++){
//                        if(list4.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
//                            if(num == 1){
//                                return list4.get(a);
//                            }
//                        }else {     //不是纸，则赋给不为<1>的图
//                            if (num != 1){
//                                return list4.get(a);
//                            }
//                        }
//                        index1 = a;
//                    }
//                    return list4.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
//                }
//            }
//            return null;
//        }
//    }
    // 通用基础比例尺地图下的元数据匹配

    public Map<String, Object> getBSMMetadata(String filename, String collection, String mapType, String excelPath) throws Exception{
        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
            // error_name.add(filename);
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀
        filename = get_english_name(filename);

        // 不规范名质检, 暂时在基本比例尺1w中出现（仅对1w情况处理）
        if(collection.equals("BASIC_SCALE_MAP_ONE")){
            String first_str = filename.substring(0,1);
            int num = 0;
            num = filename.length() - filename.replaceAll("-","").length();
            Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");       // 正则匹配数字
            if(num < 5 && !pattern.matcher(first_str).matches()){       // 南通市(5).tif  正常：05-49-027-A-1-(1).tif     判断规则：1.按-划分5部分  2. 第一个不是数字
                return matchData(mapType,excelPath,filename,collection);
            }
        }

        //将(1)(2)(3)(4)转换为<1><2><3><4>,以免和年份混淆
        if(replaceIndex(collection, filename) == null){
            return null;
        }
        filename = replaceIndex(collection, filename);

        // 对图幅进行时间提取，获取包含和不包含时间的图名  如： 02-47-(4)(1976)<1> =》 02-47-(4)和02-47-(4)(1976)
        if(timeService(filename).get("name_without_year") == null || timeService(filename).get("name_after_year") == null){
            return null;
        }
        String name_without_year = timeService(filename).get("name_without_year");
        String name_after_year = timeService(filename).get("name_after_year");

        // 进行图名规范化
        if(basicMapFormat(name_without_year,name_after_year,collection) == null){
            return null;
        }
        String formatFilename = basicMapFormat(name_without_year,name_after_year,collection);

        // 根据文件名匹配元数据
        return matchData(mapType,excelPath,formatFilename,collection);
    }

    public Map<String, Object> matchMetaData(String filename, String collection, String mapType, String excelPath) throws Exception {
        List<String> scaleArr = Arrays.asList("BASIC_SCALE_MAP_TWO_DOT_FIVE","BASIC_SCALE_MAP_FIVE","BASIC_SCALE_MAP_TEN","BASIC_SCALE_MAP_TWENTY","BASIC_SCALE_MAP_TWENTY_FIVE","BASIC_SCALE_MAP_FIFTY","BASIC_SCALE_MAP_HUNDRED");
        // 基本比例尺 （原图幅编号 or 原图幅编号+年份）
        if (!scaleArr.contains(collection)){
            return null;
        }
        //checkFilenameFormat
        String formatFilename = null;
        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
            // error_name.add(filename);
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));//出去文件名后缀
        filename = get_english_name(filename);

        //将(1)(2)(3)(4)转换为<1><2><3><4>,以免和年份混淆
        if(collection == "BASIC_SCALE_MAP_TWENTY"){
            int oldCount = filename.length();
            int newCount = filename.replace("(","").length();
            if(oldCount - newCount>1){      //20w分幅最后有-(4)会造成干扰
                if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
                    String name1, name2;
                    name1 = filename.substring(0, filename.lastIndexOf('('));
                    name2 = filename.substring(filename.lastIndexOf('('));
                    name2 = name2.replace('(', '<');
                    name2 = name2.replace(')', '>');
                    filename = name1 + name2;
                }
            }
        }
        else {
            if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
                String name1, name2;
                name1 = filename.substring(0, filename.lastIndexOf('('));
                name2 = filename.substring(filename.lastIndexOf('('));
                name2 = name2.replace('(', '<');
                name2 = name2.replace(')', '>');
                filename = name1 + name2;
            }
        }

        String name_without_year;
        String name_after_year;
        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;
        if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1 && potential_year_count !=4){      //除了(1990) (3) (12) 无括号
            return null;
        }
        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
            name_without_year = filename.substring(0, first_brace);    //图幅编号，不包含<1><2>
            name_after_year = filename.substring(first_brace);    //除了图幅之外的，年份名称，有可能有<1><2>之类的
        } else {
            name_without_year = filename;       //没有时间，可能有(1)(2)之类的
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
        if (name_without_year.contains(".") || name_without_year.contains(",")){  //说明是拼幅
            String[] blocks ;
            if(name_without_year.contains(".")){
                blocks = name_without_year.split("\\.");
            }
            else{
                blocks = name_without_year.split("\\,");
            }
            for (String b : blocks) {
                if (b.contains("-")){ //说明有完整的图幅
                    String[] parts = b.split("-");
                    //按相应比例尺进行图幅处理
                    switch (collection){
                        case "BASIC_SCALE_MAP_HUNDRED":
                            if (parts.length == 2) {
                                String block_name = get_tufu_100w_name(parts[0], parts[1]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;    //拼接分开的图幅
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_FIFTY":
                            if (parts.length == 3) {
                                String block_name = get_tufu_50w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TWENTY_FIVE":
                            if (parts.length == 3) {
                                int first_square_bracket;
                                int second_square_bracket;
                                first_square_bracket = parts[2].indexOf('[');
                                second_square_bracket = parts[2].indexOf(']');
                                if (second_square_bracket < 0) {
                                    second_square_bracket = parts[2].length();
                                }
                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                String block_name = get_tufu_25w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TWENTY":
                            if (parts.length == 3) {
                                int first_square_bracket;
                                int second_square_bracket;
                                first_square_bracket = parts[2].indexOf('(');
                                second_square_bracket = parts[2].indexOf(')');
                                if (second_square_bracket < 0) {
                                    second_square_bracket = parts[2].length();
                                }
                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                String block_name = get_tufu_20w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TEN":
                            if (parts.length == 3){
                                String block_name = get_tufu_10w_name(parts[0],parts[1],parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                        case "BASIC_SCALE_MAP_FIVE":
                            if (parts.length == 4){
                                String block_name = get_tufu_5w_name(parts[0],parts[1],parts[2],parts[3]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                        case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
                            if (parts.length == 5){
                                String block_name = get_tufu_2Dot5w_name(parts[0],parts[1],parts[2],parts[3],parts[4]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                    }
                }
                //说明没有完整的，只是最后一个数字
                else {
                    int first_square_bracket;
                    int second_square_bracket;
                    if (collection == "BASIC_SCALE_MAP_TEN") {
                        String block_name = leftPad(3, b);
                        new_name_str = new_name_str + '.' + block_name;
                    } else if (collection == "BASIC_SCALE_MAP_HUNDRED") {
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + '.' + block_name;
                    } else if (collection == "BASIC_SCALE_MAP_TWENTY_FIVE") {
                        first_square_bracket = b.indexOf('[');
                        second_square_bracket = b.indexOf(']');
                        if (second_square_bracket < 0) {
                            second_square_bracket = b.length();
                        }
                        b = b.substring(first_square_bracket + 1, second_square_bracket);
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + ".[" + block_name + ']';
                    } else if (collection == "BASIC_SCALE_MAP_TWENTY") {
                        first_square_bracket = b.indexOf('(');
                        second_square_bracket = b.indexOf(')');
                        if (second_square_bracket < 0) {
                            second_square_bracket = b.length();
                        }
                        b = b.substring(first_square_bracket + 1, second_square_bracket);
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + ".(" + block_name + ')';
                    } else {
                        new_name_str = new_name_str + '.' + b;
                    }
                }
            }
        }
        //没有拼幅情况
        else {
            String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
            int first_square_bracket;
            int second_square_bracket;
            //按比例对文件名进行格式化
            switch (collection){
                case "BASIC_SCALE_MAP_HUNDRED":
                    if (part_list.length != 2) { // 100W的数据，应该就是两个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_100w_name(part_list[0], part_list[1]);
                    break;
                case "BASIC_SCALE_MAP_FIFTY":
                    if (part_list.length != 3) { // 50W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_50w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TWENTY_FIVE":
                    if (part_list.length != 3) { // 25W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    first_square_bracket = part_list[2].indexOf('[');
                    second_square_bracket = part_list[2].indexOf(']');
                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                    new_name_str = get_tufu_25w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TWENTY":
                    if (part_list.length != 3) { // 20W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    first_square_bracket = part_list[2].indexOf('(');
                    second_square_bracket = part_list[2].indexOf(')');
                    //不需要考虑first_square_bracket = -1，因为+1后还是从0开始切
                    if(second_square_bracket < 0){
                        second_square_bracket = part_list[2].length();
                    }
                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                    new_name_str = get_tufu_20w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TEN":
                    if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_FIVE":
                    if (part_list.length != 4) { // 5W的数据，应该就是四个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_5w_name(part_list[0], part_list[1], part_list[2], part_list[3]);
                    break;
                case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
                    if (part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_2Dot5w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4]);
                    break;
            }
        }
        //获取包括时间的标准化图幅名
        if (!new_name_str.equals("")) {
            // Image_name.add(new_name_str + name_after_year);
            formatFilename = new_name_str + name_after_year;
        }
        if (formatFilename == null){
            return null;
        }

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
            // 先匹配 原图幅编号+年份 这一列
            List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear("","numAndYear",formatFilename,collection);
            if (list.size() == 1)
                return list.get(0);
            if (list.size() > 1){
                int index1 = 0;
                for(int a = 0; a < list.size(); a++){
                    if (list.get(a).get("版型").toString() != "纸"){
                        return list.get(a);
                    }
                    index1 = a;
                }
                return list.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再匹配 原图幅编号这一列
            List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum("","原图幅编号",formatFilename,collection);
            if (list1.size() == 1)
                return list1.get(0);
            List<String> strings = new ArrayList<>();
            for(int a = 0; a < list1.size(); a++){
                strings.add(list1.get(a).get("出版时间").toString());
            }
            Set<String> set=new HashSet<>(strings);
            int c = list1.size();
            int d = set.size();
            if (list1.size() > 1 && set.size() == 1){
                int index1 = 0;
                for(int a = 0; a < list1.size(); a++){
                    if (list1.get(a).get("版型").toString() != "纸"){
                        return list1.get(a);
                    }
                    index1 = a;
                }
                return list1.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
            }
            //匹配<>的情况
            if (formatFilename.indexOf('<') > 0){
                String name_without_idx;
                name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
                int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));
                List<JSONObject> list3 = metadataDao.findMetadataByOriginalNumAndYear("","numAndYear",name_without_idx,collection);
                // 先匹配 原图幅编号+年份 这一列
                if (list3.size() == 1)
                    return list3.get(0);
                if (list3.size() > 1){
                    int index1 = 0;
                    for(int a = 0; a < list3.size(); a++){
                        if(list3.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
                            if(num == 1){
                                return list3.get(a);
                            }
                        }else {     //不是纸，则赋给不为<1>的图
                            if (num != 1){
                                return list3.get(a);
                            }
                        }
                        index1 = a;
                    }
                    return list3.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
                // 再匹配 原图幅编号这一列
                List<JSONObject> list4 = metadataDao.findMetadataByOriginalNum("","原图幅编号",name_without_idx,collection);
                if (list4.size() == 1)
                    return list4.get(0);
                List<String> strings1 = new ArrayList<>();
                for(int a = 0; a < list4.size(); a++){
                    strings1.add(list4.get(a).get("出版时间").toString());
                }
                Set<String> set1=new HashSet<>(strings1);
                if (list4.size() > 1 && set1.size() == 1){
                    int index1 = 0;
                    for(int a = 0; a < list4.size(); a++){
                        if(list4.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
                            if(num == 1){
                                return list4.get(a);
                            }
                        }else {     //不是纸，则赋给不为<1>的图
                            if (num != 1){
                                return list4.get(a);
                            }
                        }
                        index1 = a;
                    }
                    return list4.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
            }
            return null;
        }
    }

    //将(1)(2)(3)(4)转换为<1><2><3><4>,以免和年份混淆
    public String replaceIndex(String collection, String filename){
        if(collection.equals("BASIC_SCALE_MAP_TWENTY") || collection.equals("BASIC_SCALE_MAP_ONE")){        // 20w: 02-47-(4).tif   1w: 05-49-027-A-1-(1).tif
            int oldCount = filename.length();
            int newCount = filename.replace("(","").length();
            if(oldCount - newCount>1){      // 02-47-(4)(1976), 02-47-(4)(1)或02-47-(4)(1976)(1)
                if (filename.endsWith("(1)") || filename.endsWith("(2)") || filename.endsWith("(3)") || filename.endsWith("(4)") || filename.endsWith("(5)") || filename.endsWith("(6)")) {
                    String name1, name2;
                    name1 = filename.substring(0, filename.lastIndexOf('('));
                    name2 = filename.substring(filename.lastIndexOf('('));
                    name2 = name2.replace('(', '<');
                    name2 = name2.replace(')', '>');
                    filename = name1 + name2;
                }
            }
        }
        else {  // 原图幅没有()
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

    // 对图幅进行时间提取，获取包含和不包含时间的图名  如： 02-47-(4)(1976)<1> =》 02-47-(4)和02-47-(4)(1976)
    public Map<String,String> timeService(String filename){
        String name_without_year;
        String name_after_year;
        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;
        if(potential_year_count != 6 && potential_year_count !=3 && potential_year_count !=1 && potential_year_count !=4){      //处理除了尾缀为(1990) (3) (12) 无括号其他情况的地图
            return null;
        }
        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
            name_without_year = filename.substring(0, first_brace);     //图幅编号，不包含<1><2> 如：12-30-10
            name_after_year = filename.substring(first_brace);      //除了图幅之外的，年份名称，有可能有<1><2>之类的 如(1985)或(1985)<2>
        } else {
            name_without_year = filename;       //没有时间，可能有<1><2>之类的 12-30-10<2>
            name_after_year = "";
        }

        // 去除name_without_year中<>情况
        int i = name_without_year.indexOf('<');
        if (i != -1) {      //存在<, 即上一步条件中不存在时间,name_after_year应为""
            String name_without_idx;
            name_without_idx = name_without_year.substring(0, i);       //完全不包含时间和<1><2>的图幅编号   12-30-10
            name_after_year = name_without_year.substring(i);     //按道理name_after_year一开始应该是空的
//            name_after_year = name_without_year.substring(i) + name_after_year;     //按道理name_after_year一开始应该是空的
            name_without_year = name_without_idx;
        }
        Map<String,String> nameMap = new HashMap<>();
        nameMap.put("name_after_year",name_after_year);
        nameMap.put("name_without_year",name_without_year);
        return nameMap;
    }

    // 基本比例尺文件名规范化
    public String basicMapFormat(String name_without_year,String name_after_year, String collection){
        String formatFilename = null;
        String new_name_str = "";   //为之后拼幅做迭代
        if (name_without_year.contains(".") || name_without_year.contains(",")){  //说明是拼幅
            String[] blocks ;
            if(name_without_year.contains(".")){
                blocks = name_without_year.split("\\.");
            }
            else{
                blocks = name_without_year.split("\\,");
            }
            // 12-30-10.15 => ['12-30-10','15']
            for (String b : blocks) {
                if (b.contains("-")){ //说明有完整的图幅
                    String[] parts = b.split("-");
                    //按相应比例尺进行图幅处理
                    switch (collection){
                        case "BASIC_SCALE_MAP_HUNDRED":         // 14-56
                            if (parts.length == 2) {
                                String block_name = get_tufu_100w_name(parts[0], parts[1]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;    //拼接分开的图幅
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_FIFTY":
                            if (parts.length == 3) {
                                String block_name = get_tufu_50w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TWENTY_FIVE":
                            if (parts.length == 3) {
                                int first_square_bracket;
                                int second_square_bracket;
                                first_square_bracket = parts[2].indexOf('[');
                                second_square_bracket = parts[2].indexOf(']');
                                if (second_square_bracket < 0) {
                                    second_square_bracket = parts[2].length();
                                }
                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                String block_name = get_tufu_25w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TWENTY":
                            if (parts.length == 3) {
                                int first_square_bracket;
                                int second_square_bracket;
                                first_square_bracket = parts[2].indexOf('(');
                                second_square_bracket = parts[2].indexOf(')');
                                if (second_square_bracket < 0) {
                                    second_square_bracket = parts[2].length();
                                }
                                parts[2] = parts[2].substring(first_square_bracket + 1, second_square_bracket);
                                String block_name = get_tufu_20w_name(parts[0], parts[1], parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                } else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case "BASIC_SCALE_MAP_TEN":
                            if (parts.length == 3){
                                String block_name = get_tufu_10w_name(parts[0],parts[1],parts[2]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                        case "BASIC_SCALE_MAP_FIVE":
                            if (parts.length == 4){
                                String block_name = get_tufu_5w_name(parts[0],parts[1],parts[2],parts[3]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                        case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
                            if (parts.length == 5){
                                String block_name = get_tufu_2Dot5w_name(parts[0],parts[1],parts[2],parts[3],parts[4]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                        case "BASIC_SCALE_MAP_ONE":
                            if (parts.length == 6){
                                String block_name = get_tufu_1w_name(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5]);
                                if (!new_name_str.equals("")) {
                                    new_name_str = new_name_str + '.' + block_name;
                                }
                                else {
                                    new_name_str = block_name;
                                }
                            } else {
                                return null;
                            }break;
                    }
                }
                //说明没有完整的，只是最后一个数字
                else {
                    int first_square_bracket;
                    int second_square_bracket;
                    if (collection.equals("BASIC_SCALE_MAP_TEN")) {          // 06-51-064.063
                        String block_name = leftPad(3, b);
                        new_name_str = new_name_str + '.' + block_name;
                    } else if (collection.equals("BASIC_SCALE_MAP_HUNDRED")) {       // 21-33.34
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + '.' + block_name;
                    } else if (collection.equals("BASIC_SCALE_MAP_TWENTY_FIVE")) {          // 12-46-[12].[13]
                        first_square_bracket = b.indexOf('[');
                        second_square_bracket = b.indexOf(']');
                        if (second_square_bracket < 0) {
                            second_square_bracket = b.length();
                        }
                        b = b.substring(first_square_bracket + 1, second_square_bracket);
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + ".[" + block_name + ']';
                    } else if (collection.equals("BASIC_SCALE_MAP_TWENTY")) {               // 11-52-(22).(23)
                        first_square_bracket = b.indexOf('(');
                        second_square_bracket = b.indexOf(')');
                        if (second_square_bracket < 0) {
                            second_square_bracket = b.length();
                        }
                        b = b.substring(first_square_bracket + 1, second_square_bracket);
                        String block_name = leftPad(2, b);
                        new_name_str = new_name_str + ".(" + block_name + ')';
                    } else {
                        new_name_str = new_name_str + '.' + b;
                    }
                }
            }
        }
        //没有拼幅情况
        else {
            String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
            int first_square_bracket;
            int second_square_bracket;
            //按比例对文件名进行格式化
            switch (collection){
                case "BASIC_SCALE_MAP_HUNDRED":
                    if (part_list.length != 2) { // 100W的数据，应该就是两个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_100w_name(part_list[0], part_list[1]);
                    break;
                case "BASIC_SCALE_MAP_FIFTY":
                    if (part_list.length != 3) { // 50W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_50w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TWENTY_FIVE":
                    if (part_list.length != 3) { // 25W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    first_square_bracket = part_list[2].indexOf('[');
                    second_square_bracket = part_list[2].indexOf(']');
                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                    new_name_str = get_tufu_25w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TWENTY":
                    if (part_list.length != 3) { // 20W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    first_square_bracket = part_list[2].indexOf('(');
                    second_square_bracket = part_list[2].indexOf(')');
                    //不需要考虑first_square_bracket = -1，因为+1后还是从0开始切
                    if(second_square_bracket < 0){
                        second_square_bracket = part_list[2].length();
                    }
                    part_list[2] = part_list[2].substring(first_square_bracket + 1, second_square_bracket);
                    new_name_str = get_tufu_20w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_TEN":
                    if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);
                    break;
                case "BASIC_SCALE_MAP_FIVE":
                    if (part_list.length != 4) { // 5W的数据，应该就是四个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_5w_name(part_list[0], part_list[1], part_list[2], part_list[3]);
                    break;
                case "BASIC_SCALE_MAP_TWO_DOT_FIVE":
                    if (part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_2Dot5w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4]);
                    break;
                case "BASIC_SCALE_MAP_ONE":
                    if (part_list.length != 6 && part_list.length != 5) { // 2.5W的数据，应该就是五个部分，不然也是错的
                        return null;
                    }
                    if (part_list.length == 5){             // # 08-51-013-D-17.tif 数据库中不符合规范，不做处理进行匹配
                        return  name_without_year+name_after_year;
                    }
                    new_name_str = get_tufu_1w_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4], part_list[5]);
                    break;
                case "THEMATIC_GANGAO_RECORD":
                    if (part_list.length != 8) { // 2500的数据，应该就是八个部分，不然也是错的
                        return null;
                    }
                    new_name_str = get_tufu_2500_name(part_list[0], part_list[1], part_list[2], part_list[3], part_list[4], part_list[5], part_list[6], part_list[7]);
                    break;
            }
        }
        //获取包括时间的标准化图幅名
        if (!new_name_str.equals("")) {
            // Image_name.add(new_name_str + name_after_year);
            formatFilename = new_name_str + name_after_year;
        }
        if (formatFilename == null){
            return null;
        }
        return formatFilename;
    }

    // 匹配文件名
    public Map<String, Object> matchData(String mapType, String excelPath, String formatFilename, String collection) throws Exception {
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
            String matchField = "原图幅编号";
            String matchFieldAndYear = "numAndYear";
            // 根据不同类型设置匹配字段
            if(collection.equals("THEMATIC_GANGAO_RECORD")){
                matchField = "图幅编号";
            }
            // 先匹配 原图幅编号+年份 这一列，那么就是不带<>
            List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear(mapType,matchFieldAndYear,formatFilename,collection);
            if (list.size() == 1)
                return list.get(0);
            if (list.size() > 1){
                int index1 = 0;
                for(int a = 0; a < list.size(); a++){       // 对结果遍历
                    if (list.get(a).get("版型").toString() == "纸"){
                        return list.get(a);
                    }
                    index1 = a;
                }
                return list.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
            }
            // 再匹配 原图幅编号这一列
            List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(mapType,matchField,formatFilename,collection);
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
            int c = list1.size();
            int d = set.size();
            if (list1.size() > 1 && set.size() == 1){       // 多个相同时间
                int index1 = 0;
                for(int a = 0; a < list1.size(); a++){
                    if (list1.get(a).get("版型").toString() == "纸"){
                        return list1.get(a);
                    }
                    index1 = a;
                }
                return list1.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
            }
            //匹配<>的情况
            if (formatFilename.indexOf('<') > 0){
                String name_without_idx;
                name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
                int num = Integer.parseInt(formatFilename.substring(formatFilename.indexOf('<')+1,formatFilename.length()-1));          // 提取尾缀序号
                List<JSONObject> list3 = metadataDao.findMetadataByOriginalNumAndYear(mapType,matchFieldAndYear,name_without_idx,collection);
                // 先匹配 原图幅编号+年份 这一列
                if (list3.size() == 1)
                    return list3.get(0);
                if (list3.size() > 1){
                    int index1 = 0;
                    for(int a = 0; a < list3.size(); a++){
                        if(list3.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
                            if(num == 1){
                                return list3.get(a);
                            }
                        }else {     //不是纸，则赋给不为<1>的图
                            if (num != 1){
                                return list3.get(a);
                            }
                        }
                        index1 = a;
                    }
                    return list3.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
                // 再匹配 原图幅编号这一列
                List<JSONObject> list4 = metadataDao.findMetadataByOriginalNum(mapType,matchField,name_without_idx,collection);
                if (list4.size() == 1)
                    return list4.get(0);
                List<String> strings1 = new ArrayList<>();
                for(int a = 0; a < list4.size(); a++){
                    String publicTime = list4.get(a).get("出版时间").toString();
                    // code2为出版时间，可能有文字：1932年制版,1934年印刷（按制版年索引）
                    if (publicTime.length() > 4){
                        publicTime = publicTime.substring(0,4);
                    }
                    if (publicTime.contains(".")){
                        publicTime = publicTime.substring(0,publicTime.indexOf("."));
                    }
                    strings1.add(publicTime);
                }
                Set<String> set1=new HashSet<>(strings1);
                if (list4.size() > 1 && set1.size() == 1){
                    int index1 = 0;
                    for(int a = 0; a < list4.size(); a++){
                        if(list4.get(a).get("版型").toString() == "纸"){    //如果是纸则赋给<1>的图
                            if(num == 1){
                                return list4.get(a);
                            }
                        }else {     //不是纸，则赋给不为<1>的图
                            if (num != 1){
                                return list4.get(a);
                            }
                        }
                        index1 = a;
                    }
                    return list4.get(index1);    //  若不满足之前情况，则匹配最新查到的记录
                }
            }
            return null;
        }
    }
}
