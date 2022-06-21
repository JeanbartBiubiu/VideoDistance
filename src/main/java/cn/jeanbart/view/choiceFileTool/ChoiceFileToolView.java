package cn.jeanbart.view.choiceFileTool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class ChoiceFileToolView implements Initializable {
    @FXML
    protected Button addFileButton;//添加单个文件
    @FXML
    protected Button addFolderButton;//添加文件夹
    @FXML
    protected Button startButton;
    @FXML
    protected TextField y1;
    @FXML
    protected TextField y2;
    @FXML
    protected TextField gap;
    @FXML
    protected TextField algorithm;
    @FXML
    protected TextField critical;
    @FXML
    protected TextField watermark;
    @FXML
    protected TableView<Map<String, String>> fileInfoTableView;
    @FXML
    protected TableColumn<Map<String, String>, String> statusTableColumn;
    @FXML
    protected TableColumn<Map<String, String>, String> fileNameTableColumn;
    @FXML
    protected TableColumn<Map<String, String>, String> filesPathTableColumn;
    @FXML
    protected TableColumn<Map<String, String>, String> rateTableColumn;
    @FXML
    protected TableColumn<Map<String, String>, String> errorInfoTableColumn;
}