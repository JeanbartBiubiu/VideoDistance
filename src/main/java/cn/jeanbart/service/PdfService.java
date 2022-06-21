package cn.jeanbart.service;

import cn.jeanbart.bean.OutputImgStruct;
import cn.jeanbart.bean.Video;
import cn.jeanbart.util.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.io.File;
import java.util.List;

public class PdfService {
    Video video1,video2;
    OpenCVUtil openCV = null;

    public PdfService(Video video1,Video video2){
        this.video1 = video1;
        this.video2 = video2;
        openCV = new OpenCVUtil();
    }

    public void outputPdF(List<OutputImgStruct> list1, List<OutputImgStruct> list2) {
        File path = new File(video1.getFilePath() + "/pdf").getParentFile();
        if (!path.exists()) {
            path.mkdir();
        }
        OutputImgStruct.mergeList(list1,video1.getFrameRate());
        OutputImgStruct.mergeList(list2,video1.getFrameRate());
        PDFReport pdfReport = new PDFReport(path.getPath() + new File(video1.getFilePath()).getName());
        OpenCVUtil openCVUtil = new OpenCVUtil();
        File picPath = new File(path.getPath() + "/picTemp");
        if (picPath.exists()) {
            FileUtil.deleteDirectory(picPath.getPath(), 0);
        } else {
            picPath.mkdir();
        }
        //输出完整的帧，然后三合一
        FfmpegVideoUtil ffmpegVideoUtil = new FfmpegVideoUtil();
        ffmpegVideoUtil.setVideoPath(video1.getFilePath());
        ffmpegVideoUtil.createFFmpegFG();
        ffmpegVideoUtil.grabberVideoFramer(list1,picPath+"/1");
        ffmpegVideoUtil.destroyFFmpegFG();
        ffmpegVideoUtil.setVideoPath(video2.getFilePath());
        ffmpegVideoUtil.createFFmpegFG();
        ffmpegVideoUtil.grabberVideoFramer(list1,picPath+"/2");
        ffmpegVideoUtil.destroyFFmpegFG();
        for (int i = 0; i < list1.size(); i++) {
            OutputImgStruct outputImgStruct1 = list1.get(i);
            Mat mat1 = openCV.getMat(picPath + "/1/" + String.format("%06d", outputImgStruct1.left)+".jpg");
            openCV.mergeImg(mat1,openCV.getMat(picPath + "/1/" + String.format("%06d", outputImgStruct1.mid)+".jpg"));
            openCV.mergeImg(mat1,openCV.getMat(picPath + "/1/" + String.format("%06d", outputImgStruct1.right)+".jpg"));

            OutputImgStruct outputImgStruct2 = list2.get(i);
            Mat mat2;
            if(outputImgStruct2.left == -1){
                mat2 = null;
            }else {
                mat2 = openCV.getMat(picPath + "/2/" + String.format("%06d", outputImgStruct2.left) + ".jpg");
                openCV.mergeImg(mat2, openCV.getMat(picPath + "/2/" + String.format("%06d", outputImgStruct2.mid) + ".jpg"));
                openCV.mergeImg(mat2, openCV.getMat(picPath + "/2/" + String.format("%06d", outputImgStruct2.right) + ".jpg"));
            }
            Mat mat3 = openCV.locationDiff(mat1,mat2);

            MatOfByte matOfByte1 = new MatOfByte(mat1);
            byte[] bytes1 = matOfByte1.toArray();
            MatOfByte matOfByte2 = new MatOfByte(mat2);
            byte[] bytes2 = matOfByte2.toArray();
            MatOfByte matOfByte3 = new MatOfByte(mat3);
            byte[] bytes3 = matOfByte3.toArray();
            pdfReport.addPage(bytes1, ConvertUtil.frameIndexToTimeTemp(outputImgStruct1.left,video1.getFrameRate())+"&&"+ConvertUtil.frameIndexToTimeTemp(outputImgStruct1.mid,video1.getFrameRate())+"&&"+ConvertUtil.frameIndexToTimeTemp(outputImgStruct1.right,video1.getFrameRate()),
                    bytes2, ConvertUtil.frameIndexToTimeTemp(outputImgStruct2.left,video2.getFrameRate())+"&&"+ConvertUtil.frameIndexToTimeTemp(outputImgStruct2.mid,video2.getFrameRate())+"&&"+ConvertUtil.frameIndexToTimeTemp(outputImgStruct2.right,video1.getFrameRate()),
                    bytes3);
        }
        pdfReport.close();
    }

    public void outputPdF(){
        File path = new File(video1.getFilePath() + "/pdf").getParentFile();
        if (!path.exists()) {
            path.mkdir();
        }
        PDFReport pdfReport = new PDFReport(path.getPath() + new File(video1.getFilePath()).getName());
        pdfReport.addParagraph("未检测到两视频不一致内容");
    }
    //TODO 添加水印
}
