package cn.jeanbart.service.choiceFileTool;

import cn.jeanbart.bean.OutputImgStruct;
import cn.jeanbart.bean.Video;
import cn.jeanbart.controller.choiceFileTool.ChoiceFileToolController;
import cn.jeanbart.service.PdfService;
import cn.jeanbart.service.VideoCompareService;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

@Getter
@Setter
public class ChoiceFileToolService {
    private ChoiceFileToolController controller;
    private boolean end = true;

    public ChoiceFileToolService(ChoiceFileToolController controller) {
        this.controller = controller;
    }

    public void startAction() {
        for (Map<String, String> fileInfoTableDatum : controller.getFileInfoTableData()) {
            if ("true".equals(fileInfoTableDatum.get("status"))) {
                String filePath1 = fileInfoTableDatum.get("filesPath");
                File file1 = new File(filePath1);
                File file2 = new File(file1.getParent() + "/1/" + file1.getName());
                System.out.println(file1.getPath());
                System.out.println(file2.getPath());
                Video video1 = new Video(file1.getPath());
                if (video1.init()) {
                    video1.setErrorMsg("视频不存在");
                }
                Video video2 = new Video(file2.getPath());
                video2.init();
                if (video1.equals(video2)) {
                    VideoCompareService videoCompareService = new VideoCompareService(video1, video2, (int) video1.getFrameRate());
                    List<List<OutputImgStruct>> lists;
                    new Thread(
                            () -> {
                                while (!videoCompareService.isEndFlag()) {
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    fileInfoTableDatum.put("rateTableColumn",
                                            "" + (1.0 * videoCompareService.getIndex1() / videoCompareService.getLastIndex1() * 100) + "%");
                                    controller.getFileInfoTableView().refresh();
                                }
                                fileInfoTableDatum.put("rateTableColumn", "已完成");
                                controller.getFileInfoTableView().refresh();
                            }
                    );
                    lists = videoCompareService.startCompare();
                    if(lists == null){
                        return;
                    }
                    if (lists.size() == 0) {
                        new PdfService(video1, video2).outputPdF();
                    } else {
                        new PdfService(video1, video2).outputPdF(lists.get(0), lists.get(1));
                    }

                } else {
                        fileInfoTableDatum.put("errorInfo", video1.getErrorMsg());
                        controller.getFileInfoTableView().refresh();
                }
            }
        }
        Platform.runLater(() -> TooltipUtil.showToast("任务完成！"));
        end = true;
    }

    public void addFileAction(File file) {
        if (file != null) {
            Map<String, String> dataRow = new HashMap<String, String>();
            dataRow.put("status", "true");
            dataRow.put("fileName", file.getName());
            dataRow.put("filesPath", file.getPath());
            dataRow.put("fileRate", "未开始");
            dataRow.put("errorInfo", "");
            controller.getFileInfoTableData().add(dataRow);
        }
    }
}