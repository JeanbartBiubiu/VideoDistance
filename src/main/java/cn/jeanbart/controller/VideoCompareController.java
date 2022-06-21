package cn.jeanbart.controller;


import cn.jeanbart.bean.OutputImgStruct;
import cn.jeanbart.bean.Video;
import cn.jeanbart.service.PdfService;
import cn.jeanbart.service.VideoCompareService;

import java.util.List;

public class VideoCompareController {
    private Video video1;
    private Video video2;
    private int gap;

    public boolean init(String path1, String path2, int gap, int y1, int y2) {
        video1 = new Video(path1);
        video2 = new Video(path2);
        this.gap = gap;
        video1.init();
        video2.init();
        if (!video1.equals(video2)) {
            return false;
        }
        //大的分辨率向小的降
        if (video1.getWidthAndHeight()[0] != video2.getWidthAndHeight()[0]) {
            if (video1.getWidthAndHeight()[0] > video2.getWidthAndHeight()[0]) {
                video1.setScale(true);
                video1.getScaleWidthAndHeight()[0] = video2.getWidthAndHeight()[0];
                video1.getScaleWidthAndHeight()[1] = video2.getWidthAndHeight()[1];
            } else {
                video2.setScale(true);
                video2.getScaleWidthAndHeight()[0] = video1.getWidthAndHeight()[0];
                video2.getScaleWidthAndHeight()[1] = video1.getWidthAndHeight()[1];
            }
        }
        video1.setWidthAndHeight(video1.getScaleWidthAndHeight());
        video1.setY1(y1);
        video1.setY1(y2);
        video2.setWidthAndHeight(video2.getScaleWidthAndHeight());
        video2.setY1(y1);
        video2.setY1(y2);
        return true;
    }

    public void start() {
        VideoCompareService videoCompareService = new VideoCompareService(video1, video2, gap);
        List<List<OutputImgStruct>> list = videoCompareService.startCompare();
        if (list.size() == 0) {
            new PdfService(video1,video2).outputPdF();
        } else {
            new PdfService(video1,video2).outputPdF(list.get(0),list.get(1));
        }
    }

    public String getProgressBar() {
        int index = video1.getIndex();
        int max = video1.getMax();
        return "" + index + "/" + max + "(" + index / max * 100 + "%)";
    }

    public String getVideoErrorMsg() {
        return video1.getErrorMsg();
    }
}
