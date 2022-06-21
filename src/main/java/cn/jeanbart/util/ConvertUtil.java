package cn.jeanbart.util;

public class ConvertUtil {
    /**
     * 将帧数转换为时间戳
     * @param index 帧的下标
     * @param rate  帧率
     * @return    时间戳
     */
    public static String frameIndexToTimeTemp(int index, double rate) {
        int second = 0, minute = 0, hour = 0;
        second = (int) (index / rate + 0.5);//不精确到毫秒
        if (second >= 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        return String.format("%02d",hour)+":"+String.format("%02d",minute)+":"+String.format("%02d",second);
    }
}
