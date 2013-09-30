package watermarking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import watermarking.controllers.FormController;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    public static Stage stage;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        MainApp.stage = stage;

        ApplicationContext context = new ClassPathXmlApplicationContext("/config.xml");
        FormController controller = context.getBean(FormController.class);

        String fxmlFile = "/fxml/form.fxml";
        log.debug("Loading FXML for main view from: {}", fxmlFile);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        loader.setController(controller);
        Parent rootNode = (Parent) loader.load();

        log.debug("Showing JFX scene");
        Scene scene = new Scene(rootNode, 500, 265);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Watermarking");
        stage.setScene(scene);

        stage.show();
        controller.init();

    }
}
