package cn.jeanbart.controller.choiceFileTool;

import cn.jeanbart.service.choiceFileTool.ChoiceFileToolService;
import cn.jeanbart.view.choiceFileTool.ChoiceFileToolView;
import com.xwintop.xcore.util.javafx.FileChooserUtil;
import com.xwintop.xcore.util.javafx.JavaFxViewUtil;
import com.xwintop.xcore.util.javafx.TooltipUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Getter
@Setter
public class ChoiceFileToolController extends ChoiceFileToolView {
    private ChoiceFileToolService service = new ChoiceFileToolService(this);
    private ObservableList<Map<String, String>> fileInfoTableData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initView();
        initEvent();
    }

    /**
     * 界面初始化
     */
    private void initView() {
        JavaFxViewUtil.setTableColumnMapAsCheckBoxValueFactory(statusTableColumn, "status");
        JavaFxViewUtil.setTableColumnMapValueFactory(fileNameTableColumn, "fileName");
        JavaFxViewUtil.setTableColumnMapValueFactory(filesPathTableColumn, "filesPath");
        JavaFxViewUtil.setTableColumnMapValueFactory(rateTableColumn, "fileRate");
        JavaFxViewUtil.setTableColumnMapValueFactory(errorInfoTableColumn, "errorInfo");
        fileInfoTableView.setItems(fileInfoTableData);
    }

    /**
     * 添加右键
     */
    @Deprecated
    private void initEvent() {
        JavaFxViewUtil.addTableViewOnMouseRightClickMenu(fileInfoTableView);
    }

    /**
     * 开始按钮
     *
     * @param event
     */
    @FXML
    private void startAction(ActionEvent event) {
        if (!service.isEnd()) {
            TooltipUtil.showToast("请等待上个任务完成再继续！");
            return;
        }
        service.setEnd(false);
        new Thread(()->service.startAction()).start();
    }

    /**
     * 添加文件按钮
     *
     * @param event
     */
    @FXML
    private void addFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("请选择文件");
        List<File> fileList = fileChooser.showOpenMultipleDialog(null);
        for (File file : fileList) {
            service.addFileAction(file);
        }
    }

    /**
     * 添加文件夹按钮
     *
     * @param event
     */
    @FXML
    private void addFolderAction(ActionEvent event) {
        File folderFile = FileChooserUtil.chooseDirectory();
        if (folderFile != null) {
            for (File file : FileUtils.listFiles(folderFile, null, false)) {
                service.addFileAction(file);
            }
        }
    }
}