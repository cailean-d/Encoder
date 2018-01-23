package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader= new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Encrypter");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 450, 150));
        primaryStage.setMinHeight(150);
        primaryStage.setMinWidth(450);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("ico.png")));
        primaryStage.show();

        List<String> parameters = getParameters().getUnnamed();

        if(parameters.size() > 0){

            decodeFromStart(primaryStage, parameters);

        }
    }

    public static void main(String[] args) { launch(args); }

    private void decodeFromStart(Stage stage, List<String> params){
        Controller con = new Controller();

        con.chooseFile = new File(params.get(0));

        if(!con.chooseFile.getName().endsWith(".encrypted")) {
            con.showAlert(null, "Некорректное расширение файла");
            return;
        }

        Optional<String> result = con.showDialogInput("Введите секретное слово :");

        if (result.isPresent() && result.get().length() > 0){

            byte[] fileBytes = con.readFromFile(con.chooseFile);
            byte[] decodedBytes = con.decodeString(con.secretTo32byte(result.get()), fileBytes);

            if(decodedBytes == null){
                con.chooseFile = null;
                return;
            }

            ButtonType btn1 = new ButtonType("Показать в программе");
            ButtonType btn2 = new ButtonType("Записать в файл");
            Optional<ButtonType> result2 =
                    con.showDialogChoose("Показать результат в программе или записать в файл?", btn1, btn2);


            if (result2.isPresent() && result2.get() == btn1){

                con.showInfo("Сообщение...", new String(decodedBytes));

                con.chooseFile = null;

            } else if (result2.isPresent() && result2.get() == btn2) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Выберите файл для сохранения");

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {

                    con.writeToFile(file, decodedBytes);

                    con.showInfo(null, "Файл успешно создан");

                    con.chooseFile = null;

                }
            }

        } else {
            con.chooseFile = null;
        }

    }
}
