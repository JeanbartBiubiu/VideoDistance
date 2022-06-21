package cn.jeanbart.bean;

import cn.jeanbart.util.FfmpegVideoUtil;
import com.sun.javafx.stage.WindowEventDispatcher;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Data
public class Video {
    private String filePath;//文件路径
    private int max;//文件所有帧
    private int[] widthAndHeight = new int[2];//视频的实际分辨率
    private int y1;//上端字幕最大y坐标
    private int y2;//下端字幕最小y坐标
//    private int x1;//左端字幕最大x坐标
//    private int x2;//右端字幕最小x坐标
    private int index;//进度查看序列号
    private boolean isScale = false;//看是否要从如1080P转720P
    private int[] scaleWidthAndHeight = new int[2];//裁剪的分辨率
    private double frameRate;//预留 看60帧能不能直接转回24帧比较
    private String errorMsg;//错误信息

    public Video(String filePath) {
        this.filePath = filePath;
    }

    public boolean init() {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        FfmpegVideoUtil videoUtil = new FfmpegVideoUtil();
        videoUtil.setVideoPath(filePath);
        videoUtil.createFFmpegFG();
        //max = (int)Math.round((double)this.getLengthInTime() * this.getFrameRate() / 1000000.0D)
        //避免算多跳不出循环，所以减2
        max = videoUtil.getMax() - 2;
        widthAndHeight = videoUtil.getWidthAndHeight();
        frameRate = videoUtil.getFrameRate();
        videoUtil.destroyFFmpegFG();
        return true;
    }

    public boolean isScale() {
        return isScale;
    }

    @Deprecated
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    //本方法是看两视频能不能被比较
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Video)) {
            return false;
        }
        Video video2 = (Video) object;
        String md51, md52 = "";
        try {
            //废性能的话这里就不比较了
            md51 = DigestUtils.md5Hex(new FileInputStream(filePath));
            md52 = DigestUtils.md5Hex(new FileInputStream(video2.getFilePath()));
        } catch (IOException e) {
            errorMsg = "IO出错";
            return false;
        }
        if (md51.equals(md52)) {
            errorMsg = "同一视频";
            return false;
        }
        //长宽比不相同的
        if (widthAndHeight[0] * video2.widthAndHeight[1] != widthAndHeight[1] * video2.widthAndHeight[0]) {
            errorMsg = "长宽比不同，无法比较";
            return false;
        }
        //帧率差大于1的
        if (Math.abs(frameRate - video2.getFrameRate()) > 1) {
            errorMsg = "帧率不同";
            return false;
        }
        return true;
    }
}
