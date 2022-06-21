package cn.jeanbart.useless;

import cn.jeanbart.util.FfmpegVideoUtil;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Deprecated
public class command {
    public static void main(String[] args) {
        String video = "E:\\video\\2.mp4";
        FfmpegVideoUtil ffmpegVideoToImg = new FfmpegVideoUtil();
        ffmpegVideoToImg.setVideoPath(video);
       // ffmpegVideoToImg.createFFmpegFG();
        int max = ffmpegVideoToImg.getMax();
        int WidthAndHeight[] = ffmpegVideoToImg.getWidthAndHeight();
        ffmpegVideoToImg.destroyFFmpegFG();

        int begin = 0;
        int end = 25780;
        final int gap = 1;
        int y1=0,y2=1080;
        final int width=WidthAndHeight[0],x=0;
        int height = y2-y1, y =y1;
        FutureTask futureTask1 = new FutureTask(new VideoToImg(video,begin,end,gap,1920,height,x,y,false,x,y,0));
        Thread thread = new Thread(futureTask1);
        thread.start();
        Process process = null;
        try {
            process = (Process)futureTask1.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        File videoFile = new File(video);
        String videoName = videoFile.getName();
        videoName = videoName.substring(0,videoName.indexOf('.'));
        File finalFile = new File(videoFile.getParent()+"/"+videoName+"_img/"+"img_"+String.format("%06d",end/gap)+".jpg");
        FutureTask<Long> futureTask2 = new FutureTask<Long>(new CheckFile(finalFile));
        new Thread(futureTask2).start();
        try {
            Long useTime = futureTask2.get();
            System.out.println(useTime+"ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        process.destroy();
        System.exit(0);
    }
}
