package cn.jeanbart.useless;

import cn.jeanbart.util.OpenCVUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class CompareImg {
    public CompareImg(){
        init();
    }
    private void init(){
        System.load(this.getClass().getClassLoader().getResource("opencv_java411.dll").getPath());
    }
    public boolean CmpPic(String src, String des) {
        //相关性阈值，应大于多少，越接近1表示越像，最大为1
        double HISTCMP_CORREL_THRESHOLD = 0.99;
        boolean flag = false;
        try {
            //long startTime = System.currentTimeMillis();
            Mat mat_src = getMat(src);
            Mat mat_des = getMat(des);
            //  转换成HSV
            Mat hsv_src = new Mat();
            Mat hsv_des = new Mat();
            Imgproc.cvtColor(mat_src, hsv_src, Imgproc.COLOR_BGR2HSV);
            Imgproc.cvtColor(mat_des, hsv_des, Imgproc.COLOR_BGR2HSV);
            List<Mat> listImg1 = new ArrayList<>();
            List<Mat> listImg2 = new ArrayList<>();
            listImg1.add(hsv_src);
            listImg2.add(hsv_des);

            MatOfFloat ranges = new MatOfFloat(0, 255);
            MatOfInt histSize = new MatOfInt(255);
            MatOfInt channels = new MatOfInt(0);//HSV  比较H通道

            Mat histImg1 = new Mat();
            Mat histImg2 = new Mat();

            Imgproc.calcHist(listImg1, channels, new Mat(), histImg1, histSize, ranges);
            Imgproc.calcHist(listImg2, channels, new Mat(), histImg2, histSize, ranges);

            // 数据归一化
            Core.normalize(histImg1, histImg1, 0d, 1d, Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(histImg2, histImg2, 0d, 1d, Core.NORM_MINMAX, -1, new Mat());

            /*
                 public static final int HISTCMP_CORREL = 0;相关性
                 public static final int HISTCMP_CHISQR = 1;卡方
                 public static final int HISTCMP_INTERSECT = 2;交叉核
                 public static final int HISTCMP_BHATTACHARYYA,HISTCMP_HELLINGER = 3;//巴氏距离 HELLINGER方法一样，叫法不同
                 public static final int HISTCMP_CHISQR_ALT = 4;替代卡方法
                 public static final int HISTCMP_KL_DIV = 5;相对熵法（Kullback-Leibler散度）
             */
            double result0;
            result0 = Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CORREL);
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CHISQR));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_INTERSECT));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_BHATTACHARYYA));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_CHISQR_ALT));
            System.out.println(Imgproc.compareHist(histImg1, histImg2, Imgproc.HISTCMP_KL_DIV));

            System.out.println(result0);
            int count = 0;
            if (result0 > HISTCMP_CORREL_THRESHOLD) {
                //这是相似的图像
                count = 1;
            }
            //long estimatedTime = System.currentTimeMillis() - startTime;
            //System.out.println("花费时间= " + estimatedTime + "ms");
            return true;
        } catch (Exception e) {
            System.out.println("出错:" + e);
        }
        return true;
    }

    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public Mat getMat(String filePath){
        File file = new File(filePath);
        Mat mat;
        byte[] byt = null;
        if(isContainChinese(file.getAbsolutePath())){
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filePath);
                byt = new byte[(int) file.length()];
                int read = inputStream.read(byt);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mat =  Imgcodecs.imdecode(new MatOfByte(byt), Imgcodecs.IMREAD_COLOR);
        }else{
            mat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_ANYCOLOR);
        }
        return mat;
    }



    public static void main(String[] args) {
        OpenCVUtil openCVCompareImg = new OpenCVUtil();
        String s = "E:\\video\\2_img\\img_";
        System.out.println(openCVCompareImg.ComparePic("E:\\video\\192010802_img\\img_007588.jpg",
                    "E:\\video\\192010802_img\\img_007595.jpg"));
    }
}
