package cn.jeanbart.util;

import static org.opencv.imgproc.Imgproc.COLOR_BGRA2RGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void mergeImgTest() throws IOException {
        OpenCVUtil openCVUtil = new OpenCVUtil();
        Mat img1 = getMat("E:\\video\\192010802_img\\img_000000.jpg");
        Mat img2 = getMat("E:\\video\\192010802_img\\img_000001.jpg");
        Mat img3 = getMat("E:\\video\\192010802_img\\img_000002.jpg");
        Mat mat = openCVUtil.mergeImg(openCVUtil.mergeImg(img1, img2),img3);
        Imgcodecs.imwrite("E:\\video\\192010802_img\\abc\\aaa.jpg",mat);

    }

    @Test
    public void locationDiffTest() throws IOException {
        OpenCVUtil openCVUtil = new OpenCVUtil();
        Mat img1 = openCVUtil.getMat("E:\\video\\192010802_img\\img_007588.jpg");
        Mat img2 = openCVUtil.getMat("E:\\video\\192010802_img\\img_007595.jpg");
        Mat mat = openCVUtil.locationDiff(img1, img2);
        Imgcodecs.imwrite("E:\\video\\192010802_img\\abc\\aaa.jpg",mat);

    }
    public Mat getMat(String filePath) {
        File file = new File(filePath);
        Mat mat;
        byte[] byt;
        if (isContainChinese(file.getAbsolutePath())) {
            byt = new byte[(int) file.length()];
            mat = Imgcodecs.imdecode(new MatOfByte(byt), Imgcodecs.IMREAD_COLOR);
        } else {
            mat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_ANYCOLOR);
        }
        return mat;
    }
    public static BufferedImage mat2BufImg (Mat matrix) {
        Mat tempMat=new Mat();
        cvtColor(matrix,tempMat,COLOR_BGRA2RGBA);//先要转bgra->rgba
        OpenCVFrameConverter.ToMat openCVConverter = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
        return java2DConverter.convert(openCVConverter.convert(tempMat));
    }
    /**
     * 判断字符串中是否包含中文
     * @param str 待校验字符串
     * @return 是否为中文
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    @Test
    public void testTime(){
        OpenCVUtil openCVUtil = new OpenCVUtil();
        long start = System.currentTimeMillis();
        openCVUtil.ComparePic("E:\\video\\1\\2_img\\img_000000.jpg", "E:\\video\\1\\2_img\\img_000001.jpg");
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        openCVUtil.ComparePic("E:\\video\\1\\2_img\\img_000000.jpg", "E:\\video\\1\\2_img\\img_000001.jpg");
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        openCVUtil.ComparePic("E:\\video\\1\\2_img\\img_000000.jpg", "E:\\video\\1\\2_img\\img_000001.jpg");
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
    }
}
