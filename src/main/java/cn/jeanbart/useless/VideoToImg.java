package cn.jeanbart.useless;

import cn.jeanbart.util.FileUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/*
* 废弃；走了点弯路；感觉不如JAVACV....方便；
* */
@Deprecated
public class VideoToImg implements Callable {
    //视频文件路径
    private String videoPath = "";
    private FFmpegFrameGrabber fFmpegFrameGrabber = null;
    private int index, max = 0;
    private ProcessBuilder pb = null;

    public VideoToImg(String video,int begin,int end,int gap,int width,int height,int x,int y,boolean isScale,int newWidth,int newHeight,int notDelete){
        List<String> commands = new ArrayList<String>();
        File file = new File(video);
        String fileName = file.getName().substring(0,file.getName().indexOf('.'));
        file = new File(file.getParent()+"\\"+fileName+"_img");
        if(file.exists()){
            //清空历史帧
            FileUtil.deleteDirectory(file.getPath(),notDelete);
        }
        if(!file.exists()){
            file.mkdirs();
        }
        commands.add("ffmpeg");
        //commands.add("-hwaccel cuvid -c:v h264_cuvid");应该可以用GPU加快解码速度
        commands.add("-i");//视频文件
        commands.add(video);
        commands.add("-vf");
        commands.add("\"select=between(n\\,"+begin+"\\,"+end+")*not(mod(n\\,"+gap+")),");
        if(isScale) {
            commands.add("scale=" + newWidth + ":" + newHeight + ",");
        }
        commands.add("crop="+width+":"+height+":"+x+":"+y+"\"");
        commands.add("-vsync");
        commands.add("0");
        commands.add("-start_number");
        commands.add(""+begin);
        commands.add(file.getPath()+"\\img_%6d.jpg");
        System.out.println(commands);
        pb =new ProcessBuilder(commands);
    }

    @Override
    public Process call() {
        Process process = null;
        try {
            process = pb.start();
            final Process p = process;
            // 处理InputStream的线程
            new Thread() {
                @Override
                public void run() {
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;
                    try {
                        while ((line = in.readLine()) != null) {
                            // logger.info("output: " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            // 处理ErrorStream的线程
            new Thread() {
                @Override
                public void run() {
                    BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    try {
                        while ((line = err.readLine()) != null) {
                            //  logger.info("err: " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            err.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }
}
