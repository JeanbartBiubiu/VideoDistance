package cn.jeanbart.bean;

import lombok.Data;

import java.util.List;

/**
 * 构建PDF文件所需的第一帧，中间帧，最后一帧位置
 */
@Data
public class OutputImgStruct {
    // TODO mid好像没卵用，看能不能删掉
    public int left,mid,right;
    public OutputImgStruct(int left, int mid, int right) {
        this.left = left;
        this.mid = mid;
        this.right = right;
    }
    //将相邻的两个间隔小于1秒的片断合并为一个
    public static void mergeList(List<OutputImgStruct> list,double rate) {
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).right - list.get(i + 1).left <= rate / 2) {
                list.get(i).setRight(list.get(i + 1).right);
                list.get(i).setMid((list.get(i).getLeft() + list.get(i).getRight()) / 2);
                list.remove(i + 1);
            } else {
                i++;
            }
        }
    }
}
