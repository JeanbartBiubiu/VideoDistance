package cn.jeanbart.util;

import cn.jeanbart.bean.OutputImgStruct;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class FfmpegVideoUtil {

    //视频文件路径
    private String videoPath = "";
    //视频帧图片存储路径
    private String videoFramesPath = videoPath + "";
    private FFmpegFrameGrabber fFmpegFrameGrabber = null;
    private int index, max = 0;
    private int begin, end, gap;//开始、结束、每帧间隔
    private int width, height, x, y;//裁切的起点坐标和长宽
    private boolean isScale;
    private int newWidth, newHeight;//是否降分辨率

    public String getVideoPath() {
        return videoPath;
    }

    public int getMax() {
        return max;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        File file = new File(videoPath);
        this.videoFramesPath = file.getParent() + "\\" + file.getName().substring(0, file.getName().indexOf(".")) + "_img";
    }

    public void createFFmpegFG() {
        fFmpegFrameGrabber = new FFmpegFrameGrabber(videoPath);
        try {
            fFmpegFrameGrabber.start();
            max = fFmpegFrameGrabber.getLengthInFrames();
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public void createFFmpegFG(int begin, int end, int gap, int width, int height, int x, int y) {
        createFFmpegFG(begin, end, gap, width, height, x, y, false, 0, 0);
    }

    public void createFFmpegFG(int begin, int end, int gap, int width, int height, int x, int y, boolean isScale, int newWidth, int newHeight) {
        this.begin = begin;
        this.end = end;
        this.gap = gap;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.isScale = isScale;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
        fFmpegFrameGrabber = new FFmpegFrameGrabber(videoPath);
        if (this.isScale) {
            fFmpegFrameGrabber.setImageScalingFlags(1);
            fFmpegFrameGrabber.setImageWidth(this.newWidth);
            fFmpegFrameGrabber.setImageHeight(this.newHeight);
        }
        try {
            fFmpegFrameGrabber.start();
            fFmpegFrameGrabber.setVideoFrameNumber(begin);
            max = fFmpegFrameGrabber.getLengthInFrames();
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 无参，根据createFFmpegFG方法的参数输出图片
     */
    public void grabberVideoFramer() {
        //用线程池输出图片,避免重复创建线程;开发规范说不要用这个，会引发OOM
        ExecutorService cachePool = Executors.newCachedThreadPool();
        String fileName;
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        try {
            int tempIndex = this.begin;
            while (tempIndex <= this.end) {
                System.out.println(tempIndex);
                fileName = videoFramesPath + "/img_" + String.format("%06d", tempIndex) + ".jpg";
                frame = fFmpegFrameGrabber.grabImage();
                if (frame != null) {//图片
                    File file = new File(videoFramesPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File outPut = new File(fileName);
                    Frame finalFrame = frame.clone();
                    //这里丢进线程池里；必须使用多线程，不然IO速度跟不上每秒只能截20帧；
                    //TODO 极端测试；CPU远强与硬盘会不会卡死
                    cachePool.execute(new Thread(
                            () -> {
                                try {
                                    ImageIO.write(converter.getBufferedImage(finalFrame).getSubimage(this.x, this.y, this.width, this.height), "jpg", outPut);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    ));
                }
                tempIndex += this.gap;
            }
            cachePool.shutdown();
            int count = 0;
            while (!cachePool.isTerminated()) {
                //等待最后一个线程跑完
                sleep(1000);
                count++;
                //避免死循环
                if (count > 10) {
                    break;
                }
            }
        } catch (IOException E) {
            E.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param list 输出PDF，合成图片要的帧列表
     */
    public void grabberVideoFramer(List<OutputImgStruct> list,String path) {
        File file = new File(path);
        if(!file.exists()){
            file.mkdir();
        }
        //用线程池输出图片,避免重复创建线程
        ExecutorService cachePool = Executors.newCachedThreadPool();
        int[] n = new int[list.size() * 3];
        int p = 0;
        for (int i = 0; i < list.size(); i++) {
            n[i * 3 + 0] = list.get(i).left;
            n[i * 3 + 1] = list.get(i).mid;
            n[i * 3 + 2] = list.get(i).right;
        }
        String fileName;
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        try {
            int tempIndex = 0;
            while (tempIndex <= n[n.length - 1]) {
                fileName = path + "/img_" + String.format("%06d", tempIndex) + ".jpg";
                frame = fFmpegFrameGrabber.grabImage();
                if (frame != null && tempIndex == n[p]) {//图片
                    File outPut = new File(fileName);
                    Frame finalFrame = frame.clone();
                    //这里丢进线程池里；必须使用多线程，不然IO速度跟不上每秒只能截20帧
                    cachePool.execute(new Thread(
                            () -> {
                                try {
                                    ImageIO.write(converter.getBufferedImage(finalFrame), "jpg", outPut);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    ));
                    p++;
                }
                tempIndex++;
            }
            cachePool.shutdown();
            int count = 0;
            while (!cachePool.isTerminated()) {
                //等待最后一个线程跑完
                sleep(1000);
                count++;
                //避免死循环
                if (count > 10) {
                    break;
                }
            }
        } catch (IOException | InterruptedException E) {
            E.printStackTrace();
        }
    }

    public void destroyFFmpegFG() {
        try {
            fFmpegFrameGrabber.stop();
            fFmpegFrameGrabber.release();
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public int[] getWidthAndHeight() {
        int[] a = new int[2];
        a[0] = fFmpegFrameGrabber.getImageWidth();
        a[1] = fFmpegFrameGrabber.getImageHeight();
        return a;
    }

    public double getFrameRate() {
        return fFmpegFrameGrabber.getFrameRate();
    }
}
