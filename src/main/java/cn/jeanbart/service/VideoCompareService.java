package cn.jeanbart.service;

import cn.jeanbart.bean.OutputImgStruct;
import cn.jeanbart.bean.Video;
import cn.jeanbart.util.FfmpegVideoUtil;
import cn.jeanbart.util.FileUtil;
import cn.jeanbart.util.OpenCVUtil;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Data
public class VideoCompareService {
    private Video video1, video2;
    private int index1, index2, lastIndex1, lastIndex2;
    String imgName1, imgName2;
    boolean endFlag = false;
    int gap = 1;
    OpenCVUtil compareImg = null;
    private List<OutputImgStruct> imgList1 = new LinkedList<OutputImgStruct>();
    private List<OutputImgStruct> imgList2 = new LinkedList<OutputImgStruct>();

    public VideoCompareService(Video video1, Video video2, int gap) {
        System.out.println(video1);
        System.out.println(video2);
        this.video1 = video1;
        this.video2 = video2;
        this.gap = gap;
        this.imgName1 = FileUtil.getFileImgDir(video1.getFilePath()) + "/img_";
        this.imgName2 = FileUtil.getFileImgDir(video2.getFilePath()) + "/img_";
        index1 = ((int) (video1.getFrameRate()));
        index2 = ((int) (video2.getFrameRate()));
        compareImg = new OpenCVUtil();
    }

    public List<List<OutputImgStruct>> startCompare() {
        // 直接将两视频的第一帧视为不同的开始，得到下一片段相同第一帧的位置；这可能会产生一些误差

            FutureTask<List<Integer>> task1 = new FutureTask<>(() -> needImg(video1));
            FutureTask<List<Integer>> task2 = new FutureTask<>(() -> needImg(video2));
            new Thread(task1).start();
            new Thread(task2).start();
            List<Integer> imgs1 = new ArrayList<>();
            List<Integer> imgs2 = new ArrayList<>();
            try {
                imgs1 = task1.get();
                imgs2 = task1.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            List<OutputImgStruct> list = new ArrayList<>();
            int left1=0,right1=0,left2=0,right2=1;
            int lastIndex1 = 0,lastIndex2 = 0;
            while(true){
                boolean find = false;
                for(int i=left1;i<=right1;i++){
                    String imgpath1 = imgName1 + String.format("%06d", imgs1.get(i)) + ".jpg";
                    for(int j= left2;j<=right2;j++){
                        String imgpath2 = imgName2 + String.format("%06d", imgs2.get(j)) + ".jpg";
                        if(compareImg.ComparePic(imgpath1,imgpath2)){
                            left1 = i+1;
                            left2 = j+1;
                            right1 = left1;
                            right2 = left2;
                            find =true;
                            break;
                        }
                    }
                }
                if(find){
                    break;
                }else{
                    right1++;
                    right2++;
                }
            }
          /*  if (compareImg.ComparePic(imgpath1, imgpath2)) {
                lastIndex1 = index1;
                lastIndex2 = index2;
                index1 += gap;
                index2 += gap;
            } else {
                //不同，开始找不同的第一帧
                //先加进来，后面一起输出
            }
        List<List<OutputImgStruct>> list = new ArrayList<List<OutputImgStruct>>();
        if (imgList1.size() != 0) {
            list.add(imgList1);
            list.add(imgList2);
        }*/
        endFlag = true;
        return null;
    }

    /**
     * @param video
     * @return
     */
    private List<Integer> needImg(Video video) {
        String videoPath = video.getFilePath();
        int width = video.getWidthAndHeight()[0],
                height = video.getWidthAndHeight()[1],
                y1 = video.getY1(),
                y2 = video.getY2();
        boolean isScale = video.isScale();
        int newWidth = video.getScaleWidthAndHeight()[0], newHeight = video.getScaleWidthAndHeight()[1];
        if (video.isScale()) {
            isScale = true;
            newWidth = video.getScaleWidthAndHeight()[0];
            newHeight = video.getScaleWidthAndHeight()[1];
        }
        FfmpegVideoUtil ffmpegVideoUtil = new FfmpegVideoUtil();
        ffmpegVideoUtil.setVideoPath(videoPath);
        ffmpegVideoUtil.createFFmpegFG(width, y2 - y1, 0, y1, isScale, newWidth, newHeight);
        return ffmpegVideoUtil.grabberVideoFramer();
    }

    private List<OutputImgStruct> aaa(List<Integer> imgs1,List<Integer> imgs2){

return null;
    }
}
