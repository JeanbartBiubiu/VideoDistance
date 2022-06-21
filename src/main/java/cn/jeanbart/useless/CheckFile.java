package cn.jeanbart.useless;

import java.io.File;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

@Deprecated
public class CheckFile implements Callable {
    private File file;
    public CheckFile(File file){
        this.file = file;
    }
    @Override
    public Long call() throws Exception {
        long start = System.currentTimeMillis();
        while(!file.exists()) {
            Thread.yield();
            sleep(1000);
        }
        long end = System.currentTimeMillis();
        return end-start;
    }
}
