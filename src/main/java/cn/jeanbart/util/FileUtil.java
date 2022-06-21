package cn.jeanbart.util;

import java.io.File;

public class FileUtil {
    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath     被删除目录的文件路径
     * @param notDelete 不删除的图片张数
     */
    public static void deleteDirectory(String sPath, int notDelete) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        //删除文件夹下的所有文件(包括子目录)        获取到的文件列表应该不是有序的，TODO 待测试
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length - notDelete; i++) {
            files[i].delete();
        }
        if (notDelete == 0) {
            dirFile.delete();
        }
    }

    public static String getFileImgDir(String sPath) {
        File file = new File(sPath);
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.indexOf('.'));
        return file.getParent() + "/" + fileName + "_img";
    }
}
