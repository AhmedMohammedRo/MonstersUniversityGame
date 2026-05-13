package game.gui;

import game.engine.dataloader.DataLoader;
import game.engine.monsters.Monster;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;

public class StartMenu {
    private Scene scene;
    private Main app;
    private ArrayList<Monster> availableMonsters;
    
    private Monster player1Monster;
    private Monster player2Monster;
    private boolean isVsComputer;

    private StackPane rootContainer;

    public StartMenu(Main app) {
        this.app = app;
        try {
            availableMonsters = DataLoader.readMonsters();
        } catch (Exception e) {
            availableMonsters = new ArrayList<>();
        }

        rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 75%, #1a1a2e, #0f0f1a);");

        showModeSelection();

        scene = new Scene(rootContainer, 1400, 850);
        
        Platform.runLater(() -> {
            app.getWindow().setFullScreenExitHint(""); 
            app.getWindow().setFullScreen(true);
        });
    }

    private void showModeSelection() {
        rootContainer.getChildren().clear();

        VBox layout = new VBox(40);
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("MONSTERS UNIVERSITY");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        title.setTextFill(Color.web("#66fcf1"));
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(102, 252, 241, 0.6), 15, 0, 0, 0);");
        
        Label subtitle = new Label("CHOOSE GAME MODE");
        subtitle.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        subtitle.setTextFill(Color.WHITE);

        HBox buttonsBox = new HBox(30);
        buttonsBox.setAlignment(Pos.CENTER);

        Button vsComputerBtn = createMenuButton("🤖 1 PLAYER\n(VS COMPUTER)", "#00b894");
        vsComputerBtn.setOnAction(e -> {
            isVsComputer = true;
            showCharacterSelection();
        });

        Button vsPlayerBtn = createMenuButton("👥 2 PLAYERS\n(LOCAL MATCH)", "#e84393");
        vsPlayerBtn.setOnAction(e -> {
            isVsComputer = false;
            showCharacterSelection();
        });

        buttonsBox.getChildren().addAll(vsComputerBtn, vsPlayerBtn);
        layout.getChildren().addAll(title, subtitle, buttonsBox);
        
        rootContainer.getChildren().add(layout);
    }

    private void showCharacterSelection() {
        rootContainer.getChildren().clear();

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Label title = new Label(isVsComputer ? "SELECT YOUR CHAMPION" : "CHOOSE YOUR FIGHTERS");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#e94560"));
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        HBox selectionArea = new HBox(50);
        selectionArea.setAlignment(Pos.CENTER);

        VBox p1Box = createPlayerSelectionBox("🟢 PLAYER 1", true);
        selectionArea.getChildren().add(p1Box);

        if (!isVsComputer) {
            VBox p2Box = createPlayerSelectionBox("🔴 PLAYER 2", false);
            selectionArea.getChildren().add(p2Box);
        }

        Button startBtn = new Button("⚔️ ENTER ARENA");
        startBtn.setPrefWidth(300);
        startBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 22px; -fx-padding: 15; -fx-background-radius: 10; -fx-cursor: hand;");
        addHoverEffect(startBtn);
        
        startBtn.setOnAction(e -> {
            if (player1Monster == null) {
                showWarningPopup("Player 1 must select a monster!");
                return;
            }
            if (!isVsComputer && player2Monster == null) {
                showWarningPopup("Player 2 must select a monster!");
                return;
            }
            
            if (!isVsComputer) {
                if (player1Monster.getName().equals(player2Monster.getName())) {
                    showWarningPopup("Players cannot choose the exact same monster!");
                    return;
                }
                if (player1Monster.getRole() == player2Monster.getRole()) {
                    showWarningPopup("Teams must be balanced!\n(One Scarer and One Laugher)");
                    return;
                }
            }
            
            app.getWindow().setScene(new GameBoard(app, player1Monster, player2Monster, isVsComputer).getScene());
        });

        Button backBtn = new Button("⬅ BACK");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-font-size: 16px; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> {
            player1Monster = null;
            player2Monster = null;
            showModeSelection();
        });

        layout.getChildren().addAll(title, selectionArea, startBtn, backBtn);
        rootContainer.getChildren().add(layout);
    }

    private Button createMenuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(250, 120);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-border-width: 3px; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 20px; -fx-border-radius: 15; -fx-cursor: hand; -fx-text-alignment: center;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #1a1a2e; -fx-font-weight: bold; -fx-font-size: 20px; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand; -fx-text-alignment: center;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-border-width: 3px; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 20px; -fx-border-radius: 15; -fx-cursor: hand; -fx-text-alignment: center;"));
        
        return btn;
    }

    private void addHoverEffect(Button btn) {
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    }

    private VBox createPlayerSelectionBox(String titleText, boolean isPlayer1) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setPrefSize(350, 450);
        String playerColor = isPlayer1 ? "#00b894" : "#e84393";
        card.setStyle("-fx-background-color: rgba(22, 33, 62, 0.8); -fx-border-color: " + playerColor + "; -fx-border-width: 3px; -fx-background-radius: 15; -fx-border-radius: 15;");

        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(playerColor));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        Label nameLabel = new Label("---");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);

        Label roleLabel = new Label("Role: ???");
        roleLabel.setTextFill(Color.LIGHTGRAY);

        Label energyLabel = new Label("Energy: 0");
        energyLabel.setTextFill(Color.web("#f1c40f"));
        energyLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        ComboBox<String> picker = new ComboBox<>();
        for (Monster m : availableMonsters) {
            picker.getItems().add(m.getName());
        }
        picker.setPromptText("Select Monster");
        picker.setPrefWidth(220);
        
        picker.setStyle("-fx-background-color: #0f3460; -fx-border-color: " + playerColor + "; -fx-border-width: 2px; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        picker.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(picker.getPromptText());
                    setStyle("-fx-text-fill: #aaaaaa; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-color: transparent;");
                }
            }
        });

        picker.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #0f3460;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 10;");
                    
                    setOnMouseEntered(e -> setStyle("-fx-background-color: " + playerColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 10;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 10;"));
                }
            }
        });

        picker.setOnAction(e -> {
            int index = picker.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                Monster selected = availableMonsters.get(index);
                if (isPlayer1) player1Monster = selected;
                else player2Monster = selected;

                nameLabel.setText(selected.getName().toUpperCase());
                roleLabel.setText("Role: " + selected.getRole());
                energyLabel.setText("Energy: " + selected.getEnergy() + " ⚡");

                try {
                    String imagePath = "file:src/assets/" + selected.getName() + ".png";
                    imageView.setImage(new Image(imagePath));
                } catch (Exception ex) {}
            }
        });

        card.getChildren().addAll(title, picker, imageView, nameLabel, roleLabel, energyLabel);
        return card;
    }

    public Scene getScene() { return scene; }

    private void showWarningPopup(String message) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);
        
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #e74c3c; -fx-border-width: 3px; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 0);");
        
        Label lbl = new Label("⚠️ " + message);
        lbl.setTextFill(Color.web("#e74c3c"));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        lbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lbl.setWrapText(true);
        
        Button btn = new Button("GOT IT");
        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 25; -fx-background-radius: 5; -fx-cursor: hand;");
        btn.setOnAction(ev -> popupStage.close());
        
        layout.getChildren().addAll(lbl, btn);
        popupStage.setScene(new Scene(layout, 400, 180));
        popupStage.showAndWait();
    }
}