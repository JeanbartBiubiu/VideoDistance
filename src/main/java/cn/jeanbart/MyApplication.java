package cn.jeanbart;

import com.xwintop.xcore.util.javafx.JavaFxSystemUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MyApplication extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(MyApplication.class.getResource("/"));
        URL url = MyApplication.class.getResource("/fxml/ChoiceFileTool.fxml");
        System.out.println(url);
        FXMLLoader fXMLLoader = new FXMLLoader(url);
        Parent root = fXMLLoader.load();
        primaryStage.setResizable(true);
        primaryStage.setTitle("文件选择");
        double[] screenSize = JavaFxSystemUtil.getScreenSizeByScale(0.7D, 0.7D);
        primaryStage.setScene(new Scene(root, screenSize[0], screenSize[1]));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }
}