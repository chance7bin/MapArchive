package com.opengms.maparchivebackendprj.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/04
 */
public class XmlUtils {

    /**
     * 将XML转为指定的POJO
     * @param clazz 指定我们需要转换的对象
     * @param xmlStr 需要转的xml字符串
     * @return
     */
    public static Object xmlStrToObject(Class<?> clazz, String xmlStr) throws JAXBException, IOException {
        Object xmlObject = null;
        Reader reader = null;
        JAXBContext context = JAXBContext.newInstance(clazz);
        // XML 转为对象的接口
        Unmarshaller unmarshaller = context.createUnmarshaller();
        reader = new StringReader(xmlStr);
        //以文件流的方式传入这个string
        xmlObject = unmarshaller.unmarshal(reader);
        if (null != reader) {
            reader.close();
        }
        return xmlObject;
    }

}
