package com.opengms.maparchivebackendprj.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ImageMetadata;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Date;
import java.util.Locale;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/09
 */
@Slf4j
public class ImageUtils {

    // private Logger log = (Logger) LoggerFactory.getLogger(getClass());

    private static String DEFAULT_PREVFIX = "thumb_";
    private static Boolean DEFAULT_FORCE = false;//建议该值为false

    /**
     * <p>Title: thumbnailImage</p>
     * <p>Description: 根据图片路径生成缩略图 </p>
     * @param imagePath    原图片路径
     * @param outputDir            输出图片文件夹
     * @param w            缩略图宽
     * @param h            缩略图高
     * @param prevfix    生成缩略图的前缀
     * @param force        是否强制按照宽高生成缩略图(如果为false，则生成最佳比例缩略图)
     */
    public static void thumbnailImage(String imagePath, String outputDir, int w, int h, String prevfix, boolean force){
        File imgFile = new File(imagePath);
        if(imgFile.exists()){
            try {
                log.info("begin generate thumbnail image: {}", imagePath);
                // ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
                String types = Arrays.toString(ImageIO.getReaderFormatNames());
                String suffix = null;
                // 获取图片后缀
                if(imgFile.getName().indexOf(".") > -1) {
                    suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
                }// 类型和图片后缀全部小写，然后判断后缀是否合法
                if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()) < 0){
                    log.error("Sorry, the image suffix is illegal. the standard image suffix is {}." + types);
                    return ;
                }
                log.debug("target image's size, width:{}, height:{}.",w,h);
                Image img = ImageIO.read(imgFile);
                if(!force){
                    // 根据原图与要求的缩略图比例，找到最合适的缩略图比例
                    int width = img.getWidth(null);
                    int height = img.getHeight(null);
                    if((width*1.0)/w < (height*1.0)/h){
                        if(width > w){
                            h = Integer.parseInt(new java.text.DecimalFormat("0").format(height * w/(width*1.0)));
                            log.debug("change image's height, width:{}, height:{}.",w,h);
                        }
                    } else {
                        if(height > h){
                            w = Integer.parseInt(new java.text.DecimalFormat("0").format(width * h/(height*1.0)));
                            log.debug("change image's width, width:{}, height:{}.",w,h);
                        }
                    }
                }
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                g.drawImage(img, 0, 0, w, h, Color.LIGHT_GRAY, null);
                g.dispose();
                String p = imgFile.getPath();
                // 将图片保存在原目录并加上前缀
                // ImageIO.write(bi, suffix, new File(p.substring(0,p.lastIndexOf(File.separator)) + File.separator + prevfix +imgFile.getName()));
                // 图片存在指定的输出文件下
                // File outFile = new File(outputDir + File.separator + prevfix + imgFile.getName());
                File outFile = new File(outputDir + "/" + prevfix + imgFile.getName());
                // File outFile = new File(outputDir + "/" + customName);
                FileUtils.mkFile(outFile);
                ImageIO.write(bi, suffix, outFile);
                log.debug("缩略图在原路径下生成成功");
                log.info("generate thumbnail image success, path: {}",outFile.getPath());
            } catch (IOException e) {
                log.error("generate thumbnail image failed.",e);
            }
        }else{
            log.warn("thumbnailImage : the image is not exist. Path:{}",imagePath);
        }
    }


    /**
     * @Description 将带有Base64的字符串中的图片存储到本地
     * @param content 带有base64的字符串
     * @param id 条目Id
     * @param resourcePath 项目静态资源路径前缀
     * @param htmlLoadPath 网络访问路径前缀
     * @Return java.lang.String
     * @Author kx
     * @Date 2021/7/6
     **/
    public static String saveBase64Image(String content,String id,String resourcePath,String htmlLoadPath){
        if(content==null){
            return null;
        }
        int startIndex = 0, endIndex = 0, index = 0;
        while (content!=null&&content.indexOf("src=\"data:im", startIndex) != -1) {
            int Start = content.indexOf("src=\"data:im", startIndex) + 5;
            int typeStart = content.indexOf("/", Start) + 1;
            int typeEnd = content.indexOf(";", typeStart);
            String type = content.substring(typeStart, typeEnd);
            startIndex = typeEnd + 8;
            endIndex = content.indexOf("\"", startIndex);
            String imgStr = content.substring(startIndex, endIndex);

            String imageName = "/detailImage/" + id + "/" + id + "_" + (index++) + "." + type;
            base64StrToImage(imgStr, resourcePath + imageName);

            content = content.substring(0, Start) + htmlLoadPath + imageName + content.substring(endIndex, content.length());
        }
        return content;
    }

    /**
     * @Description base64字符串转化成图片
     * @param imgStr base64字符串
     * @param path 图片存储本地路径
     * @Return boolean
     * @Author kx
     * @Date 2021/7/6
     **/
    public static boolean base64StrToImage(String imgStr, String path) {
        if (imgStr == null)
            return false;
        // sun.misc.BASE64Decoder是内部专用 API, 可能会在未来发行版中删除
        // BASE64Decoder decoder = new BASE64Decoder();
        // 所以换成 java.util.Base64.Decoder
        Decoder decoder = Base64.getDecoder();
        try {
            // 解密
            byte[] b = decoder.decode(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //文件夹不存在则自动创建
            File tempFile = new File(path);
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(tempFile);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 图片转瓦片
     * @param inputDir 待切片图片路径
     * @param outputDir 瓦片输出路径
     * @param pyPath python程序调用的路径
     * @return long python程序执行的时间
     * @Author bin
     **/
    public static long image2Tiles(String inputDir, String outputDir, String pyPath){
        File imgFile = new File(inputDir);
        if(imgFile.exists()){
            long start = System.currentTimeMillis();
            // log.info("generate {} tiles begin", imgFile.getName());
            String rootPath = System.getProperty("user.dir");
            // System.out.println(rootPath);
            String exe = "python";
            // 在window下用\表示路径，而在linux都是用/表示路径。在有路径需要修改的时候，要注意区分
            // String command = rootPath + "/src/main/resources/pyTools/gdal2tiles.py";
            String command = pyPath + "/pyTools/gdal2tiles.py";
            // String command = pyPath + "/pyTools/gdal2tiles-multiprocess.py";
            String p1 = "-l";
            String p2 = "-p";
            String p3 = "raster";
            String p4 = "-z";
            String p5 = "0-5";
            String p6 = "-w";
            String p7 = "none";
            String p8 = inputDir;
            String p9 = outputDir;
            String[] cmdArr = new String[] {exe, command, p1, p2, p3, p4, p5, p6, p7, p8, p9};
            log.info("exec cmd: {}", Arrays.toString(cmdArr));
            // System.out.println(Arrays.toString(cmdArr));
            try {
                //这个方法是类似隐形开启了命令执行器，输入指令执行python脚本
                Process process = Runtime.getRuntime()
                    .exec(cmdArr); // "python解释器位置（这里一定要用python解释器所在位置不要用python这个指令）+ python脚本所在路径（一定绝对路径）"
                process.waitFor(); // 阻塞程序，跑完了才输出结果
                long end = System.currentTimeMillis();
                log.info("generate " + imgFile.getName() + " tiles success, cost: " + ((end - start) / 1000) + "s");
                return (end - start) / 1000;
            }catch (Exception e){
                log.error("调用生成tile脚本并读取结果时出错：" + e.getMessage());
                return 0;
            }
        }else{
            log.warn("image2Tiles : the image is not exist. Path:{}",inputDir);
            return 0;
        }
    }


    /**
     * tif转png
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     * @return java.lang.String 输出文件的路径
     * @Author bin
     **/
    public static String tif2png(String inputPath, String outputPath, String pyPath){


        // javacv
        // tif2png_javacv(inputPath,outputPath);

        // 调python脚本
        // tif2png_python(inputPath,outputPath,pyPath);

        return outputPath;
    }


    // 使用springboot依赖包进行转换，缺点是这个依赖包很大，打包后项目大小太大
    /**
     * tif转png (临时文件放在resourcePath的temp目录下)
     * @param inputPath 文件的路径
     * @param outputDir 转换后输出的临时文件夹
     * @param fileType 文件类型
     * @return java.lang.String
     * @Author bin
     **/
    // public static String tif2png_javacv(String inputPath, String outputDir, String fileType){
    //
    //     File file = new File(inputPath);
    //
    //
    //     //特殊字符转义
    //     inputPath = escapeQueryChars(inputPath);
    //     outputDir = escapeQueryChars(outputDir);
    //
    //     //该方法不支持中文路径
    //     inputPath = getPinYin(inputPath);
    //     File file1 = new File(inputPath);
    //     inputPath = outputDir + "/" + file1.getName();
    //     FileUtils.copyFile(file, new File(inputPath));
    //
    //     outputDir = getPinYin(outputDir);
    //     String outputName = file1.getName().substring(0, file1.getName().lastIndexOf(fileType)) + "png";
    //     String outputPath = outputDir + "/" + outputName;
    //
    //     //输出路径的父级目录必须存在
    //     File out = new File(outputPath);
    //     if (!out.getParentFile().exists()) {
    //         out.getParentFile().mkdirs();
    //     }
    //
    //
    //     Mat mat = opencv_imgcodecs.imread(inputPath);
    //     opencv_imgcodecs.imwrite(outputPath,mat);
    //
    //     return outputPath;
    // }

    // 调用python脚本进行转换，前提是要先在环境中安装opencv的wheel
    public static String tif2png_python(String inputPath, String outputDir, String fileType, String pyPath){

        File file = new File(inputPath);


        //特殊字符转义
        inputPath = escapeQueryChars(inputPath);
        outputDir = escapeQueryChars(outputDir);

        //该方法不支持中文路径
        inputPath = getPinYin(inputPath);
        File file1 = new File(inputPath);
        inputPath = outputDir + "/" + file1.getName();
        FileUtils.copyFile(file, new File(inputPath));

        outputDir = getPinYin(outputDir);
        String outputName = file1.getName().substring(0, file1.getName().lastIndexOf(fileType)) + "png";
        String outputPath = outputDir + "/" + outputName;

        //输出路径的父级目录必须存在
        File out = new File(outputPath);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }

        File imgFile = new File(inputPath);
        if(imgFile.exists()){
            String exe = "python";
            String command = pyPath + "/pyTools/tif2png.py";
            String p1 = inputPath;
            String p2 = outputPath;
            String[] cmdArr = new String[] {exe, command, p1, p2};
            log.info("exec cmd: {}", Arrays.toString(cmdArr));
            try {
                Process process = Runtime.getRuntime()
                    .exec(cmdArr); // "python解释器位置（这里一定要用python解释器所在位置不要用python这个指令）+ python脚本所在路径（一定绝对路径）"
                process.waitFor(); // 阻塞程序，跑完了才输出结果
                long end = System.currentTimeMillis();
            }catch (Exception e){
                log.error("tif2png 出错：" + e.getMessage());
            }
        }else{
            log.warn(" tif2png : the image is not exist. Path:{}",inputPath);
        }

        return outputPath;
    }


    // 中文转英文
    public static String getPinYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";
        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {  //判断字符是否是中文
                    //toHanyuPinyinStringArray 如果传入的不是汉字，就不能转换成拼音，那么直接返回null
                    //由于中文有很多是多音字，所以这些字会有多个String，在这里我们默认的选择第一个作为pinyin
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    output += temp[0];
                } else {
                    output += Character.toString(input[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
//    		Log.v(TAG, "BadHanyuPinyinOutputFormatCombination");
        }
        return output;
    }

    /**
     * solr检索时，转换特殊字符
     *
     * @param s 需要转义的字符串
     * @return 返回转义后的字符串
     */
    public static String escapeQueryChars(String s) {

        // char[] replaceChar = {'+','-','!','(',')','^','[',']','、','{','}','~','*','?','|','&','$',';'};

        StringBuilder sb = new StringBuilder();
        //查询字符串一般不会太长，挨个遍历也花费不了多少时间
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
                 || c == '^' || c == '[' || c == ']' || c == '、'
                || c == '{' || c == '}' || c == '~' || c == '*' || c == '?'
                || c == '|' || c == '&' || c == ';'
                || c == '$' || Character.isWhitespace(c)) {
                // sb.append("");
            }
            else sb.append(c);
        }
        return sb.toString();
    }


    //得到图像的元数据
    public static ImageMetadata getImageInfo(String imagePath) throws ParseException, ImageProcessingException, IOException {

        // String path = "E:\\YSS\\demo1\\P1\\06-48-115(1954).tif";
        // String path = "E:\\mapArchiveFiles\\BasicScaleMap\\TEN\\thumbnail\\thumb_06-48-110(1958).png";
        File file = new File(imagePath);
        // 大小、尺寸、时间

        if (!file.exists())
            return null;

        com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(file);

        ImageMetadata imageMetadata = new ImageMetadata();
        // imageMetadata.setSize((int) (file.length() / 1024 /1024));
        //输出所有附加属性数据
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                String tagName = tag.getTagName();
                switch (tagName){
                    case "Image Width":{
                        // String[] pixels = tag.getDescription().split("pixels");
                        // imageMetadata.setWidth(Integer.parseInt(pixels[0].trim()));
                        String[] pixels = tag.getDescription().split(" ");
                        imageMetadata.setWidth(Integer.parseInt(pixels[0]));
                        break;
                    }
                    case "Image Height":{
                        // String[] pixels = tag.getDescription().split("pixels");
                        // imageMetadata.setHeight(Integer.parseInt(pixels[0].trim()));
                        String[] pixels = tag.getDescription().split(" ");
                        imageMetadata.setHeight(Integer.parseInt(pixels[0]));
                        break;
                    }
                    case "File Name":{
                        imageMetadata.setName(tag.getDescription());
                        break;
                    }
                    case "File Size":{
                        // String[] bytes = tag.getDescription().split("bytes");
                        // imageMetadata.setSize(Integer.parseInt(bytes[0].trim()) / 1024 /1024);
                        String[] bytes = tag.getDescription().split(" ");
                        // byte -> MB
                        BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(bytes[0]) / 1024 / 1024);
                        double ans = bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
                        imageMetadata.setSize(ans);
                        break;
                    }
                    case "File Modified Date":{
                        SimpleDateFormat sim1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                        SimpleDateFormat sim2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = sim1.parse(tag.getDescription());
                        // String format = sim2.format(date);
                        // Date date1 = new Date();
                        imageMetadata.setModifiedData(date);
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }
        }

        return imageMetadata;

    }

}
