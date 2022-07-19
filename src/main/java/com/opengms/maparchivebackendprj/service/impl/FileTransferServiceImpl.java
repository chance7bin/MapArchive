package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IFileInfoDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.bo.DBConnectMap;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.dto.Chunk;
import com.opengms.maparchivebackendprj.entity.dto.MapChunk;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IFileTransferService;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
@Slf4j
public class FileTransferServiceImpl implements IFileTransferService {

    @Value("${mapItemDir}")
    private String mapItemDir;

    @Value("${resourcePath}")
    private String resourcePath;

    @Resource(name="defaultDataServer")
    DataServer defaultDataServer;

    @Autowired
    IFileInfoDao fileInfoDao;

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Autowired
    IGenericService genericService;

    @Override
    public JsonResult uploadMapFile(MapChunk chunk, HttpServletResponse response) {

        /**
         * 每一个上传块都会包含如下分块信息：
         * chunkNumber: 当前块的次序，第一个块是 1，注意不是从 0 开始的。
         * totalChunks: 文件被分成块的总数。
         * chunkSize: 分块大小，根据 totalSize 和这个值你就可以计算出总共的块数。注意最后一块的大小可能会比这个要大。
         * currentChunkSize: 当前块的大小，实际大小。
         * totalSize: 文件总大小。
         * identifier: 这个就是每个文件的唯一标示。
         * filename: 文件名。
         * relativePath: 文件夹上传的时候文件的相对路径属性。
         * 一个分块可以被上传多次，当然这肯定不是标准行为，但是在实际上传过程中是可能发生这种事情的，这种重传也是本库的特性之一。
         *
         * 根据响应码认为成功或失败的：
         * 200 文件上传完成
         * 201 文加快上传成功
         * 500 第一块上传失败，取消整个文件上传
         * 507 服务器出错自动重试该文件块上传
         */


        if (chunk.getServername() == null || chunk.getServername().equals("")){
            response.setStatus(500);
            return ResultUtils.error("未找到指定数据服务器");
        }
        String loadPath = genericService.getLoadPath(chunk.getServername());


        // 判断文件类型
        String fileName = chunk.getFilename();
        String[] split = fileName.split("\\.");
        String fileType = split[split.length - 1];

        //判断是否是压缩包
        boolean isZip = fileType.equals("zip");

        MetadataTable metadataTable = metadataTableDao.findById(chunk.getMapCLSId());

        //文件上传路径
        // String fileDir = mapItemDir + "/file";
        String fileDir = "/" + metadataTable.getCollection() + "/file";
        //压缩包上传路径
        // String zipDir = mapItemDir + "/uploadZip";
        String zipDir = "/uploadZip";
        // String uploadPath = isZip ? (resourcePath + zipDir) : (resourcePath + fileDir);
        String uploadPath = isZip ? (loadPath + mapItemDir + zipDir) : (loadPath + mapItemDir + fileDir);

        // String localFileName = chunk.getIdentifier() + "_" + chunk.getFilename();
        String localFileName = fileName;
        File file= new File(uploadPath + "/" + localFileName);
        //第一个块,则新建文件
        if(chunk.getChunkNumber()==1 && !file.exists()){

            boolean b = FileUtils.mkFile(file);
            if (!b){
                response.setStatus(500);
                return ResultUtils.error("exception:createFileException");
            }
        }

        //进行写文件操作
        InputStream is = null;
        RandomAccessFile raf = null;
        try{
            //将块文件写入文件中
            is = chunk.getFile().getInputStream();
            raf =new RandomAccessFile(file,"rw");
            int len = -1;
            byte[] buffer=new byte[1024];
            raf.seek((chunk.getChunkNumber()-1) * chunk.getChunkSize());
            while((len = is.read(buffer)) != -1){
                raf.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(chunk.getChunkNumber()==1) {
                file.delete();
            }
            response.setStatus(507);
            return ResultUtils.error("exception:writeFileException");
        } finally {
            // 注意要把流给关了，不然后面的文件删不掉
            try {
                if (raf != null) {
                    raf.close();
                }
                if (is != null) {

                    is.close();
                }
            }catch (IOException e) {
                log.error("InputStream close error");
            }
        }
        if(chunk.getChunkNumber().equals(chunk.getTotalChunks())){
            response.setStatus(200);

            // 如果上传的是压缩包，要先解压再入库
            List<JSONObject> fileList;
            // 传给前端的数据
            List<FileInfo> dto = new ArrayList<>();
            if (isZip){
                // 压缩包解压的路径
                String uncompressDir = loadPath + mapItemDir + fileDir + "/" + file.getName().substring(0,file.getName().lastIndexOf("."));
                try {
                    fileList = FileUtils.zipUncompress(uploadPath + "/" + localFileName, uncompressDir);
                }catch (Exception e){
                    return ResultUtils.error("exception:uncompressFileException");
                }

                //解压成功后把压缩包给删了
                FileUtils.deleteFile(uploadPath + "/" + localFileName);

                for (JSONObject f : fileList) {
                    String name = f.getString("fileName");
                    String path = f.getString("path");
                    FileInfo fileInfo = initFileInfo(name, path);
                    dto.add(fileInfo);
                }
                return ResultUtils.success(dto);
            }

            // 向数据库中保存上传信息
            FileInfo fileInfo = initFileInfo(localFileName, file.getAbsolutePath());
            dto.add(fileInfo);

            return ResultUtils.success(dto);
        }else {
            response.setStatus(201);
            return ResultUtils.success("upload part success");
        }
    }

    @Override
    public JsonResult uploadFile(Chunk chunk, HttpServletResponse response) {

        // 判断文件类型
        String fileName = chunk.getFilename();
        String[] split = fileName.split("\\.");
        String fileType = split[split.length - 1];

        if(fileType.equals("zip") || fileType.equals("rar")){
            response.setStatus(500);
            return ResultUtils.error("不能上传压缩包");
        }


        String uploadPath = resourcePath + mapItemDir + "/uploadFile";
        String localFileName = System.currentTimeMillis() + "_" + fileName;
        File file= new File(uploadPath + "/" + localFileName);
        //第一个块,则新建文件
        if(chunk.getChunkNumber()==1 && !file.exists()){

            boolean b = FileUtils.mkFile(file);
            if (!b){
                response.setStatus(500);
                return ResultUtils.error("exception:createFileException");
            }
        }

        //进行写文件操作
        InputStream is = null;
        RandomAccessFile raf = null;
        try{
            //将块文件写入文件中
            is = chunk.getFile().getInputStream();
            raf =new RandomAccessFile(file,"rw");
            int len = -1;
            byte[] buffer=new byte[1024];
            raf.seek((chunk.getChunkNumber()-1) * chunk.getChunkSize());
            while((len = is.read(buffer)) != -1){
                raf.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(chunk.getChunkNumber()==1) {
                file.delete();
            }
            response.setStatus(507);
            return ResultUtils.error("exception:writeFileException");
        } finally {
            // 注意要把流给关了，不然后面的文件删不掉
            try {
                if (raf != null) {
                    raf.close();
                }
                if (is != null) {

                    is.close();
                }
            }catch (IOException e) {
                log.error("InputStream close error");
            }
        }
        if(chunk.getChunkNumber().equals(chunk.getTotalChunks())){
            response.setStatus(200);

            // 向数据库中保存上传信息
            FileInfo fileInfo = initFileInfo(localFileName, file.getAbsolutePath());

            return ResultUtils.success(fileInfo);
        }else {
            response.setStatus(201);
            return ResultUtils.success("upload part success");
        }
    }


    /**
     * 文件信息入库
     * @param fileName 文件名
     * @param filePath 文件路径(相对于resourcePath的路径)
     * @return com.example.maparchivebackend.entity.po.FileInfo
     * @Author bin
     **/
    private FileInfo initFileInfo(String fileName, String filePath){
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        String[] split = fileName.split("\\.");
        fileInfo.setType(split[split.length - 1]);
        fileInfo.setPath(filePath.replace("\\", "/"));
        return fileInfoDao.insert(fileInfo);
    }
}
