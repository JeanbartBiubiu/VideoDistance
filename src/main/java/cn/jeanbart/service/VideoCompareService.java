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
        int[][] firstFragment = finalFrame(0, 0);

        while (index1 < video1.getMax() && index2 < video2.getMax()) {
            String imgpath1 = imgName1 + String.format("%06d", index1) + ".jpg";
            String imgpath2 = imgName2 + String.format("%06d", index2) + ".jpg";
            if (!new File(imgpath1).exists()) {
                needImg(video1, true);//正常比较
            }
            if (!new File(imgpath2).exists()) {
                needImg(video2, true);
            }
            if (compareImg.ComparePic(imgpath1, imgpath2)) {
                lastIndex1 = index1;
                lastIndex2 = index2;
                index1 += gap;
                index2 += gap;
            } else {
                //不同，开始找不同的第一帧
                int[][] diffImgs = BinarySearch();
                //先加进来，后面一起输出
                OutputImgStruct outputImgStruct1 = new OutputImgStruct(diffImgs[0][0], diffImgs[0][1], diffImgs[0][2]);
                OutputImgStruct outputImgStruct2 = new OutputImgStruct(diffImgs[1][0], diffImgs[1][1], diffImgs[1][2]);
                imgList1.add(outputImgStruct1);
                imgList2.add(outputImgStruct2);
            }
        }
        List<List<OutputImgStruct>> list = new ArrayList<List<OutputImgStruct>>();
        if (imgList1.size() != 0) {
            list.add(imgList1);
            list.add(imgList2);
        }
        endFlag = true;
        return list;
    }

    /**
     * @param video
     * @param isDelete TODO 去掉删除参数，新增视频2与视频1帧的偏移量，可减少video1、video2删除后重新获取帧的次数
     *                 根据isDelete参数，为false的时候区接下来1分钟内的所有帧，为true的时候区现在开始，接下来的每隔gap帧取一帧
     * @return
     */
    private void needImg(Video video, boolean isDelete, int specifyStart) {
        String videoPath = video.getFilePath();
        int start = video.getIndex();
        if(specifyStart>0){
            start = specifyStart;
        }
        int end;
        int tempGap = 1;
        if (isDelete) {
            end = video.getMax();
            tempGap = this.gap;
        } else {
            end = video.getIndex() + (int) (video.getFrameRate() * 60) <= video.getMax() ? (int) (video.getFrameRate() * 60) : video.getMax();
        }
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
        ffmpegVideoUtil.createFFmpegFG(start, end, tempGap, width, height, 0, y1, isScale, newWidth, newHeight);
        ffmpegVideoUtil.grabberVideoFramer();
    }

    private void needImg(Video video, boolean isDelete){
        needImg(video,isDelete,-1);
    }

    /**
     * 二分查找不同的第一帧，然后返回调用的finalFrame方法的返回值
     */
    private int[][] BinarySearch() {
        int mid1, mid2;
        while (lastIndex1 <= index1) {
            mid1 = lastIndex1 + ((index1 - lastIndex1) >> 1);
            mid2 = lastIndex2 + ((index2 - lastIndex2) >> 1);
            //这里的帧肯定存在，不需要再检查一遍
            String imgpath1 = imgName1 + String.format("%06d", mid1) + ".jpg";
            String imgpath2 = imgName2 + String.format("%06d", mid2) + ".jpg";
            if (compareImg.ComparePic(imgpath1, imgpath2)) {
                index1 = mid1 - 1;
                index2 = mid2 - 1;
            } else {
                imgpath1 = imgName1 + String.format("%06d", mid1 - 1) + ".jpg";
                imgpath2 = imgName2 + String.format("%06d", mid2 - 1) + ".jpg";
                if (compareImg.ComparePic(imgpath1, imgpath2)) {
                    break;
                } else {
                    lastIndex1 = mid1 + 1;
                    lastIndex2 = mid2 + 1;
                }
            }
        }
        return finalFrame(index1, index2);
    }

    /**
     * 找不同的段落的最后一帧
     *
     * @param firstDiff1
     * @param firstDiff2
     * @return 返回video1和video2的片段的开始帧、中间帧和结束帧   a[2][3]
     */
    private int[][] finalFrame(int firstDiff1, int firstDiff2) {
        List<Integer> finalDiffIndex1 = new ArrayList<>();
        List<Integer> finalDiffIndex2 = new ArrayList<>();
        finalDiffIndex1.add(firstDiff1);
        finalDiffIndex2.add(firstDiff2);//避免删减情况

        while (index1 < video1.getMax() && index2 < video2.getMax()) {
            getFragmentFirst(finalDiffIndex1, video1);
            getFragmentFirst(finalDiffIndex2, video2);

            for (int i = 0; i < finalDiffIndex1.size(); i++) {
                if (i != finalDiffIndex1.size() - 1) {//降低时间复杂度
                    for (int j = 0; j < finalDiffIndex2.size(); j++) {
                        String firstSimpImg1 = imgName1 + String.format("%06d", finalDiffIndex1.get(i) + 1) + ".jpg";
                        String firstSimpImg2 = imgName2 + String.format("%06d", finalDiffIndex2.get(j) + 1) + ".jpg";
                        if (compareImg.ComparePic(firstSimpImg1, firstSimpImg2)) {
                            index1 = finalDiffIndex1.get(i) + 1;
                            index2 = finalDiffIndex2.get(j) + 1;
                            break;
                        }
                    }
                } else {
                    String firstSimpImg1 = imgName1 + String.format("%06d", finalDiffIndex1.get(i) + 1) + ".jpg";
                    String firstSimpImg2 = imgName2 + String.format("%06d", finalDiffIndex2.get(finalDiffIndex2.size() - 1) + 1) + ".jpg";
                    if (compareImg.ComparePic(firstSimpImg1, firstSimpImg2)) {
                        index1 = finalDiffIndex1.get(i) + 1;
                        index2 = finalDiffIndex2.get(finalDiffIndex2.size() - 1) + 1;
                        break;
                    }
                }
            }
        }
        int[][] diffImg = new int[2][3];
        diffImg[0][0] = firstDiff1;
        diffImg[0][1] = (firstDiff1 + index1) / 2;
        diffImg[0][2] = index1;
        if (firstDiff2 == index2) {//删减情况
            diffImg[1][0] = -1;
            diffImg[1][1] = -1;
            diffImg[1][2] = -1;
        } else {//和谐情况
            diffImg[1][0] = firstDiff2;
            diffImg[1][1] = (firstDiff2 + index2) / 2;
            diffImg[1][2] = index2;
        }
        return diffImg;
    }

    /**
     * 此方法是将视频下一段落第一一帧存进列表中
     *
     * @param imgList 存放不同段落的第一帧的列表
     * @param video   该视频对象
     */
    private void getFragmentFirst(List<Integer> imgList, Video video) {
        compareImg.setHISTCMP_CORREL_THRESHOLD(0.8f);
        int index = imgList.get(imgList.size() - 1);

        index = findFirstDiff(index,video);
        imgList.add(index);

        compareImg.setHISTCMP_CORREL_THRESHOLD(0.99f);
    }

    /**
     * 二倍放大法找到不同的第一帧
     */
    private int findFirstDiff(int compareImgIndex, Video video) {
        String imgName = FileUtil.getFileImgDir(video.getFilePath()) + "/img_";
        String firstImgPass = imgName + String.format("%06d", compareImgIndex) + ".jpg";

        int i = 1;
        while (true) {
            if (compareImgIndex + i > video.getMax()) {
                i = video.getMax() - compareImgIndex;
                return i;
            }
            String compareImgPass = imgName + String.format("%06d", compareImgIndex + i) + ".jpg";

            if (!new File(compareImgPass).exists()) {
                needImg(video1, false);
            }

            if (!compareImg.ComparePic(firstImgPass, compareImgPass)) {
                if (i == 1) {
                    return i;
                } else {
                    return findFirstDiff(compareImgIndex + (i >> 2), video);
                }
            } else {
                i = i << 2;
            }
        }
    }
}
