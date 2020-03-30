package com.nusinfineon;

import static com.nusinfineon.util.Directories.ICON_APPLICATION;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.nusinfineon.core.Core;
import com.nusinfineon.storage.JsonParser;
import com.nusinfineon.ui.Ui;
import com.nusinfineon.ui.UiManager;
import com.nusinfineon.util.Messages;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private Core core;
    private JsonParser jsonParser;
    private Ui ui;

    public Main() throws IOException {
        jsonParser = new JsonParser();

        try {
            core = jsonParser.loadData();
        } catch (UnrecognizedPropertyException | JsonParseException e) {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(Messages.SAVE_FILE_WRONG_FORMAT_MESSAGE
                    + "\nOtherwise, delete the file and try opening the program again.");
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(480, 240);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

            alert.showAndWait();

            Platform.exit();
            System.exit(1);
        }

        ui = new UiManager(core, jsonParser);
    }

    @Override
    public void start(Stage stage) {
        ui.start(stage);
    }

    @Override
    public void stop() throws IOException {
        jsonParser.storeData(core);
    }
}
