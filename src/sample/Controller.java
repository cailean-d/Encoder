package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


public class Controller {

    @FXML private TextField inputMessage;

    public File chooseFile;
    public File saveFile;

    public void encode_message(ActionEvent actionEvent) {

        String message = inputMessage.getText();

        if(inputMessage.getText().length() > 0){

            if (saveFile == null) {

                saveFile = showFileSaver(actionEvent, true);

            }

            if (saveFile != null) {

                Optional <String> result = showDialogInput("Введите секретное слово :");

                if (result.isPresent() && result.get().length() > 0){

                    byte[] encodedBytes = encodeString(secretTo32byte(result.get()), message);

                    writeToFile(saveFile, encodedBytes);

                    showInfo(null, "Файл успешно создан");

                    saveFile = null;

                } else {
                    showAlert(null, "Вы должны ввести секретное слово");
                }

            }

        } else {
            this.showAlert(null, "Поле с сообщением не может быть пустым");
        }

    }

    public void decode_message(ActionEvent actionEvent) {


        if (chooseFile == null) {

            chooseFile = this.showFileChooser(actionEvent);

        }

        if (chooseFile != null) {

            Optional <String> result = showDialogInput("Введите секретное слово :");

            if (result.isPresent() && result.get().length() > 0){

                byte[] fileBytes = readFromFile(chooseFile);
                byte[] decodedBytes = decodeString(secretTo32byte(result.get()), fileBytes);

                if(decodedBytes == null){
                    chooseFile = null;
                    return;
                }

                ButtonType btn1 = new ButtonType("Показать в программе");
                ButtonType btn2 = new ButtonType("Записать в файл");
                Optional<ButtonType> result2 =
                showDialogChoose("Показать результат в программе или записать в файл?", btn1, btn2);


                if (result2.isPresent() && result2.get() == btn1){

                    this.showInfo("Сообщение...", new String(decodedBytes));

                    chooseFile = null;

                } else if (result2.isPresent() && result2.get() == btn2) {

                    File file2 = this.showFileSaver(actionEvent, false);

                    if (file2 != null) {

                        writeToFile(file2, decodedBytes);

                        showInfo(null, "Файл успешно создан");

                        chooseFile = null;

                    }
                }

            } else {
                chooseFile = null;
            }
        }
    }

    public void showAlert(String title, String text){
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Controller.class.getResourceAsStream("ico.png")));
        dialog.setContentText(text);
        dialog.showAndWait();
    }

    public void showInfo(String title, String text){
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(title);
        info.setHeaderText(null);
        info.setContentText(text);
        info.showAndWait();
    }

    private File showFileChooser(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("encrypted files(*.encrypted)", "*.encrypted"));
        fileChooser.setTitle("Выберите файл");
        return fileChooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());
    }

    private File showFileSaver(ActionEvent event, boolean filter){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для сохранения");
        if(filter){
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("encrypted files(*.encrypted)", "*.encrypted"));
            fileChooser.setInitialFileName("*.encrypted");
        } else {
            fileChooser.setInitialFileName("*.txt");
        }
        File file = fileChooser.showSaveDialog(((Node)event.getTarget()).getScene().getWindow());
        if(filter){
            if (file != null) {
                if(!file.getName().endsWith(".encrypted")) {
                    file = new File(file.getAbsolutePath() + ".encrypted");
                }
            }
        }
        return file;
    }

    public Optional <String> showDialogInput(String text){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(null);
        dialog.setHeaderText(null);
        dialog.setContentText(text);
        return dialog.showAndWait();
    }

    public Optional <ButtonType>  showDialogChoose(String text, ButtonType btn1, ButtonType btn2){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.getButtonTypes().setAll(btn1, btn2);
        return alert.showAndWait();
    }

    private byte[] encodeString(String secret, String message){

        byte[] encodedBytes = null;

        try{

            Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encodedBytes = cipher.doFinal(message.getBytes());

        } catch(NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException e){
            e.printStackTrace();
        } catch (BadPaddingException e){
            this.showAlert(null, "Вы ввели некорректное секретное слово");
        }

        return encodedBytes;
    }

    public byte[] decodeString(String secret, byte[] encodedBytes){

        byte[] decodedBytes = null;

        try{

            Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decodedBytes = cipher.doFinal(encodedBytes);

        } catch(NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException e){
            e.printStackTrace();
        } catch (BadPaddingException e){
            this.showAlert(null, "Вы ввели некорректное секретное слово");
        }

        return decodedBytes;
    }

    public void writeToFile(File filename, byte[] textBytes){
        try{

            FileOutputStream outputStream = new FileOutputStream(filename);
            outputStream.write(textBytes);
            outputStream.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public byte[] readFromFile(File filename){

        byte[] fileBytes = null;

        try{

            FileInputStream inputStream = new FileInputStream(filename);
            byte[] inputBytes = new byte[(int) chooseFile.length()];
            inputStream.read(inputBytes);
            fileBytes = inputBytes;

        } catch (IOException e){
            e.printStackTrace();
        }

        return  fileBytes;
    }

    public String secretTo32byte(String secret){

        char[] secretArray = secret.toCharArray();
        StringBuilder finalString = new StringBuilder();

        if(secretArray.length == 16){

            return secret;

        } else if (secretArray.length > 16){

            for(char symbol : secretArray){
                finalString.append(symbol);
                if(finalString.length() == 16){
                    break;
                }
            }

            return  new String(finalString);
        } else {

            while (finalString.length() < 16){

                for(char symbol : secretArray){

                    finalString.append(symbol);

                    if(finalString.length() == 16){
                        break;
                    }
                }

            }

            return  new String(finalString);
        }
    }
}
