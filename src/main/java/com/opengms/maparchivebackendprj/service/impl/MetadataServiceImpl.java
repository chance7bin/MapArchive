package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.dto.ExcelPathDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import com.sun.javafx.scene.transform.TransformUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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


    @Override
    public Map<String, Object> getMetadataByFilenameByType(String filename, MapClassification mapCLS, String excelPath) {
        switch (mapCLS){
            case BASIC_SCALE_MAP_TEN:{
                return getBSMMetadata10w(filename,mapCLS,excelPath);
            }
            default:{
                // GeoInfo coordinate10w = null;
                return null;
            }
        }

    }

    @Override
    public JsonResult getMetadataByExcel(ExcelPathDTO excelPathDTO) {
        String excelPath = excelPathDTO.getExcelPath();
        try {
            List<Map<String, Object>> maps = FileUtils.redExcel(excelPath);

            return ResultUtils.success(maps);
        }catch (Exception e){
            return ResultUtils.error();
        }
    }

    @Override
    public JsonResult getMetadata(SpecificFindDTO findDTO, MapClassification mapCLS) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<JSONObject> metadataList = metadataDao.findMetadataBySearchText(findDTO.getCurQueryField(),findDTO.getSearchText(),mapCLS,pageable);

        long count = metadataDao.countMetadataBySearchText(findDTO.getCurQueryField(),findDTO.getSearchText(),mapCLS);

        return ResultUtils.success(new PageableResult<>(count,metadataList));
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
    private String leftPad(int length, int number) {
        String f = "%0" + length + "d";
        return String.format(f, number);
    }

    private String leftPad(int length, String number) {
        int num = Integer.parseInt(number);
        //String f = "%0" + length + "d";
        //return String.format(f, num);
        return leftPad(length,num);
    }

    private String get_tufu_10w_name(int number1, int number2, int number3) {

        String X = leftPad(2,number1);
        String Y = leftPad(2,number2);
        String Z = leftPad(3,number3);
        //String Y = String.format("%0" + 2 + "d", number2);
        //String Z = String.format("%0" + 3 + "d", number3);
        return   X + "-" + Y + "-" + Z;
    }

    private String get_tufu_10w_name(String number1, String number2, String number3) {

        int num1 = Integer.parseInt(number1);
        int num2 = Integer.parseInt(number2);
        int num3 = Integer.parseInt(number3);

        return get_tufu_10w_name(num1,num2,num3);
    }

    // 基础比例尺地图下的元数据匹配（1：10w）
    private Map<String, Object> getBSMMetadata10w(String filename, MapClassification mapCLS, String excelPath)  {


        // 基本比例尺 （原图幅编号 or 原图幅编号+年份）

        //checkFilenameFormat
        String formatFilename = null;
        if (!filename.contains(".")){ //如果一个.都没有，说明文件名不对
            // error_name.add(filename);
            return null;
        }
        // 得到文件名
        filename = filename.substring(0, filename.lastIndexOf("."));
        filename = get_english_name(filename);
        String name_without_year;
        String name_after_year;
        int i = filename.indexOf("("); //找到从左到右第一个左括号，认为前面是图幅，后面是年份
        if (i != -1){
            // 有带年份的
            name_without_year = filename.substring(0,i); //图幅名称
            name_after_year = filename.substring(i);  //除了图幅之外的，年份名称，有可能有(1)(2)之类的
        }
        else {
            name_without_year = filename;
            name_after_year = "";
        }
        String new_name_str = "";
        if (name_without_year.contains(".")){  //说明是拼幅
            String[] blocks = name_without_year.split("\\.");
            for (String b : blocks) {
                if (b.contains("-")){ //说明有完整的图幅
                    String[] parts = b.split("-");
                    if (parts.length == 3){
                        String block_name = get_tufu_10w_name(parts[0],parts[1],parts[2]);
                        if (!new_name_str.equals("")) {
                            new_name_str = new_name_str + '.' + block_name;
                        }
                        else {
                            new_name_str = block_name;
                        }
                    } else {
                        // error_part.add(filename);
                        // new_name_str = "";
                        //continue;
                        // break; //这里用break
                        return null;
                    }
                }
                else { //说明没有完整的，只是最后一个数字
                    String block_name = leftPad(3,b);
                    new_name_str = new_name_str + '.' + block_name;
                }
            }
        }
        else {
            String[] part_list = name_without_year.split("-"); //进行分解，从而处理前面补齐0的问题
            if (part_list.length != 3) { // 10W的数据，应该就是三个部分，不然也是错的
                // error_part.add(filename);
                // continue;
                return null;
            }
            new_name_str = get_tufu_10w_name(part_list[0], part_list[1], part_list[2]);

        }
        if (!new_name_str.equals("")) {
            // Image_name.add(new_name_str + name_after_year);
            formatFilename = new_name_str + name_after_year;
        }

        if (formatFilename == null){
            return null;
        }

        // 读excel表中的数据
        if (excelPath != null && !excelPath.equals("")) {
            List<Map<String, Object>> maps = null;
            try {
                maps = FileUtils.redExcel(excelPath);
            } catch (Exception e) {
                return null;
            }
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
                if (!Database_name_block_only.contains(formatFilename)){
                    return null;
                } else {
                    int frequency = Collections.frequency(Database_name_block_only, formatFilename);
                    if (frequency == 1){ //只有唯一的才是成功的
                        return maps.get(Database_name_block_only.indexOf(formatFilename));
                    } else {
                        return null;
                    }
                }
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



        } else {

            // 先匹配 原图幅编号+年份 这一列
            List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear(formatFilename ,mapCLS);
            if (list.size() == 1)
                return list.get(0);
            // 再匹配 原图幅编号这一列
            List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(formatFilename ,mapCLS);
            if (list1.size() == 1)
                return list1.get(0);

            return null;
        }


    }
}
