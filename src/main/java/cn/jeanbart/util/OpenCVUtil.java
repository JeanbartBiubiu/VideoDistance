package cn.jeanbart.util;

import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.cache.impl.LRUCache;
import cn.jeanbart.useless.CompareImg;
import lombok.Setter;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenCVUtil {
    @Setter
    private double HISTCMP_CORREL_THRESHOLD;

    /**
     * 取大点，最多多用1、2G内存，可以接受
     */
    private static LRUCache<String, Mat> matCache = new LRUCache<>(512);
    private static int count = 0;

    public OpenCVUtil() {
        HISTCMP_CORREL_THRESHOLD = 0.99;
        init();
    }

    public OpenCVUtil(double HISTCMP_CORREL_THRESHOLD) {
        //相关性阈值，应大于多少，越接近1表示越像，最大为1
        this.HISTCMP_CORREL_THRESHOLD = HISTCMP_CORREL_THRESHOLD;
        init();
    }

    private void init() {
        System.load(this.getClass().getClassLoader().getResource("opencv_java411.dll").getPath());
    }

    public boolean ComparePic(String src, String des) {
        try {
            Mat histImg1 = getMat(src);
            Mat histImg2 = getMat(des);

            double result0;
            //算法位置 https://github.com/opencv/opencv/blob/17234f82d025e3bbfbf611089637e5aa2038e7b8/modules/imgproc/src/histogram.cpp
            result0 = Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CORREL);
            /*System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CHISQR));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_INTERSECT));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_BHATTACHARYYA));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CHISQR_ALT));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_KL_DIV));
            System.out.println(result0);*/
            //int count = 0;
            //释放资源，防止内存泄漏
            histImg1.release();
            histImg2.release();
            System.out.println(result0);
            //这是相似的图像
            return result0 > HISTCMP_CORREL_THRESHOLD;
            //long estimatedTime = System.currentTimeMillis() - startTime;
            //System.out.println("花费时间= " + estimatedTime + "ms");
        } catch (Exception e) {
            System.out.println("出错:" + e);
            return false;
        }
    }

    /**
     * 判断字符串中是否包含中文,opencv不支持中文路径
     *
     * @param str 待校验字符串
     * @return 是否为中文
     */
    private boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    public Mat getMat(String filePath) {
        Mat cache = matCache.get(filePath);
        if (cache != null) {
            // 返回的Mat上层会释放掉，所以要拷贝一份
            Mat returnMat = new Mat();
            cache.copyTo(returnMat);
            return returnMat;
        }
        File file = new File(filePath);
        Mat mat;
        byte[] byt;
        if (isContainChinese(file.getAbsolutePath())) {
            byt = new byte[(int) file.length()];
            mat = Imgcodecs.imdecode(new MatOfByte(byt), Imgcodecs.IMREAD_COLOR);
        } else {
            mat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        }
        Mat hsv_src = new Mat();
        Imgproc.cvtColor(mat, hsv_src, Imgproc.COLOR_BGR2HSV);
        List<Mat> listImg = new ArrayList<>();
        listImg.add(hsv_src);

        MatOfFloat ranges = new MatOfFloat(0, 255);
        MatOfInt histSize = new MatOfInt(255);
        MatOfInt channels = new MatOfInt(0);//HSV  比较H通道

        Mat histImg = new Mat();

        Imgproc.calcHist(listImg, channels, new Mat(), histImg, histSize, ranges);

        // 数据归一化
        Core.normalize(histImg, histImg, 0d, 1d, Core.NORM_MINMAX, -1, new Mat());
        /*
             public static final int HISTCMP_CORREL = 0;相关性
             public static final int HISTCMP_CHISQR = 1;卡方
             public static final int HISTCMP_INTERSECT = 2;交叉核
             public static final int HISTCMP_BHATTACHARYYA,HISTCMP_HELLINGER = 3;//巴氏距离 HELLINGER方法一样，叫法不同
             public static final int HISTCMP_CHISQR_ALT = 4;替代卡方法
             public static final int HISTCMP_KL_DIV = 5;相对熵法（Kullback-Leibler散度）
         */
        Mat temp = new Mat();
        histImg.copyTo(temp);
        matCache.put(filePath, temp);
        mat.release();
        hsv_src.release();
        return histImg;
    }

    //左右拼接图片
    public Mat mergeImg(Mat img1, Mat img2) {
        Mat result = new Mat(img1.rows(), img1.cols() + img2.cols() + 1, img1.type());
        img1.colRange(0, img1.cols()).copyTo(result.colRange(0, img1.cols()));
        img2.colRange(0, img2.cols()).copyTo(result.colRange(img1.cols() + 1, img1.cols() + img2.cols() + 1));
        return result;
    }

    //图片减操作
    public Mat locationDiff(Mat img1, Mat img2) {
        if (img1 == null) {
            return null;
        }
        if (img2 == null) {
            return img1;
        }
        Mat result = new Mat();
        Core.absdiff(img1, img2, result);
        return result;
    }

}
