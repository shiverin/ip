package nimbus;

import java.nio.file.Path;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Provides the JavaFX user interface for Nimbus. */
public class Main extends Application {
    private final Nimbus nimbus = new Nimbus(Path.of("data", "nimbus.txt"));
    private final VBox dialogContainer = new VBox(10);
    private final TextField userInput = new TextField();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Nimbus");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ScrollPane dialogScrollPane = new ScrollPane(dialogContainer);
        dialogScrollPane.setFitToWidth(true);
        dialogScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogScrollPane.setStyle("-fx-background-color: transparent;");
        dialogContainer.setPadding(new Insets(12));
        dialogContainer.heightProperty().addListener((observable, oldValue, newValue) ->
                dialogScrollPane.setVvalue(1.0));

        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> handleUserInput());
        userInput.setPromptText("Enter a command...");
        userInput.setOnAction(event -> handleUserInput());
        HBox inputBar = new HBox(8, userInput, sendButton);
        HBox.setHgrow(userInput, Priority.ALWAYS);

        BorderPane root = new BorderPane(dialogScrollPane, title, null, inputBar, null);
        root.setPadding(new Insets(16));
        BorderPane.setMargin(title, new Insets(0, 0, 12, 0));
        BorderPane.setMargin(inputBar, new Insets(12, 0, 0, 0));

        stage.setScene(new Scene(root, 640, 520));
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.setTitle("Nimbus");
        stage.show();

        addMessage("Hello! I'm Nimbus.\nWhat can I do for you?", false);
        userInput.requestFocus();
    }

    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        addMessage(input, true);
        userInput.clear();
        addMessage(nimbus.getResponse(input), false);
        if (input.equalsIgnoreCase("bye")) {
            userInput.setDisable(true);
            Platform.runLater(() -> ((Stage) userInput.getScene().getWindow()).close());
        }
    }

    private void addMessage(String message, boolean isUser) {
        TextArea bubble = new TextArea(message);
        bubble.setEditable(false);
        bubble.setWrapText(true);
        bubble.setFocusTraversable(false);
        bubble.setMaxWidth(460);
        bubble.setPrefRowCount(Math.max(1, message.lines().toList().size()));
        bubble.setStyle(isUser
                ? "-fx-control-inner-background: #dbeafe; -fx-font-size: 14px;"
                : "-fx-control-inner-background: #f3f4f6; -fx-font-size: 14px;");
        HBox row = new HBox(bubble);
        row.setStyle(isUser ? "-fx-alignment: center-right;" : "-fx-alignment: center-left;");
        dialogContainer.getChildren().add(row);
    }

    /** Starts the JavaFX application. */
    public static void main(String[] args) {
        launch(args);
    }
}
