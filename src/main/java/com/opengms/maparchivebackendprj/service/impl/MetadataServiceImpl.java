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
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import com.sun.javafx.scene.transform.TransformUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

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


    @Override
    public Map<String, Object> getMetadataByFilenameByType(String filename, String mapCLSId, String excelPath) {
        MetadataTable metadataTable = metadataTableDao.findById(mapCLSId);
        String collection = metadataTable.getCollection();
        try {
            return getBSMMetadata(filename,collection,excelPath);
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
    private String get_english_name(String name){
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

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        return get_tufu_100w_name(num1,num2);
    }

    public static String get_tufu_50w_name(int number1, int number2, String number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);

        return   X + "-" + Y + "-" + number3;
    }

    public static String get_tufu_50w_name(String number1, String number2, String number3) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);

        return get_tufu_50w_name(num1,num2,number3);
    }

    public static String get_tufu_25w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(2,number3);

        return   X + "-" + Y + "-" + "[" + Z + "]";
    }

    public static String get_tufu_25w_name(String number1, String number2, String number3) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_25w_name(num1,num2,num3);
    }

    public static String get_tufu_20w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(2,number3);

        return   X + "-" + Y + "-" + "(" + Z + ")";
    }

    public static String get_tufu_20w_name(String number1, String number2, String number3) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_20w_name(num1,num2,num3);
    }

    public static String get_tufu_10w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z;
    }

    public static String get_tufu_10w_name(String number1, String number2, String number3) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_10w_name(num1,num2,num3);
    }

    public static String get_tufu_5w_name(int number1, int number2, int number3, String number4) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        return   X + "-" + Y + "-" + Z + "-" + number4;
    }

    public static String get_tufu_5w_name(String number1, String number2, String number3, String number4) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_5w_name(num1,num2,num3,number4);
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

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_2Dot5w_name(num1,num2,num3,number4,number5);
    }

    // 通用基础比例尺地图下的元数据匹配
    public Map<String, Object> getBSMMetadata(String filename, String collection, String excelPath) throws Exception {
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
        filename = filename.substring(0, filename.lastIndexOf("."));
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
        //分开为原图幅编号和时间编号
        int first_brace = filename.lastIndexOf('(');      //找到从右到左第一个左括号，认为前面是图幅，后面是年份
        int second_brace = filename.lastIndexOf(')');     //找到从右到左第一个右括号，认为前面是年份
        int potential_year_count = second_brace - first_brace + 1;
        if (first_brace != -1 && potential_year_count == 6) {       //存在时间
            name_without_year = filename.substring(0, first_brace);    //图幅编号，不包含(1)(2)
            name_after_year = filename.substring(first_brace);    //除了图幅之外的，年份名称，有可能有(1)(2)之类的
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


            int count = 0; //匹配上的次数
            int index = -1; //列表索引

            for (int j = 0; j < maps.size(); j++) {
                String code1 = maps.get(j).get("原图幅编号").toString();
                String code2 = maps.get(j).get("出版时间").toString();

                String format1 = code1;
                String format2 = code1+"("+code2+")";

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
                    } else {
                        return null;
                    }
                }
                //特殊情况一：由于<>的存在影响匹配，删去处理
                if (formatFilename.indexOf('<') > 0){
                    String name_without_idx;
                    name_without_idx = formatFilename.substring(0, formatFilename.indexOf('<'));
                    int frequency = Collections.frequency(Database_name, name_without_idx);
                    if (frequency == 1) {
                        return maps.get(Database_name.indexOf(name_without_idx)) ;
                    }
                    else {
                        return null;
//                        formatFilename = name_without_idx;      //将其余情况放在下面处理
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
                } else {
                    return null;
                }
            }

            // if (count == 1){
            //     return maps.get(index);
            // } else {
            //     return null;
            // }

        }
        //进行数据库的匹配
       else {

           // 先匹配 原图幅编号+年份 这一列
           List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear(formatFilename, collection);
           if (list.size() == 1)
               return list.get(0);
           // 再匹配 原图幅编号这一列
           List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(formatFilename, collection);
           if (list1.size() == 1)
               return list1.get(0);
       }
        return null;
    }
}
