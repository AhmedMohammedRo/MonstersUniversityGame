package game.gui;

import game.engine.Game;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Monster;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip; 
import javafx.util.Duration;

public class GameBoard {

    private Scene scene;
    private Game game;
    private GridPane grid;
    private Label turnLabel;
    private Label actionLogLabel; 
    private Main app;
    
    private Button pRollBtn, pPowerBtn, oRollBtn, oPowerBtn, muteBtn, exitBtn;
    private ImageView pDiceView, oDiceView;
    
    private ImageView pImageView, oImageView;
    private Label pName, pEnergy, pPos;
    private Label oName, oEnergy, oPos; 
    private HBox pStatusBox, oStatusBox; 

    private java.util.HashMap<String, Image> imageCache = new java.util.HashMap<>();
    private javafx.scene.shape.Rectangle[] highlights = new javafx.scene.shape.Rectangle[100];
    private StackPane[] cellPanes = new StackPane[100]; 
    
    private Integer overrideCurrentMonsterPos = null;
    private Monster animatingMonster = null;
    
    private boolean isMuted = false;
    private final int POWERUP_COST = 10; 
    
    private boolean isVsComputer;

    public GameBoard(Main app, Monster p1, Monster p2, boolean isVsComputer) {
        this.app = app;
        this.isVsComputer = isVsComputer;
        
        try {
            if (isVsComputer) {
                game = new Game(p1); 
            } else {
                game = new Game(p1, p2); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 75%, #1f2833, #050608);");

        BorderPane root = new BorderPane();

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setHgap(2);
        grid.setVgap(2);
        root.setCenter(grid);

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(15, 0, 0, 0));

        muteBtn = new Button("🔊");
        muteBtn.setStyle("-fx-background-color: rgba(31, 40, 51, 0.8); -fx-text-fill: #66fcf1; -fx-font-size: 20px; -fx-border-color: #45a29e; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: hand;");
        muteBtn.setOnAction(e -> {
            isMuted = !isMuted;
            muteBtn.setText(isMuted ? "🔇" : "🔊");
        });
        addHoverEffect(muteBtn);

        exitBtn = new Button("🚪 EXIT TO MENU");
        exitBtn.setStyle("-fx-background-color: rgba(231, 76, 60, 0.8); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #c0392b; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 5 20;");
        exitBtn.setOnAction(e -> {
            app.getWindow().setFullScreen(false);
            app.getWindow().setScene(new StartMenu(app).getScene());
        });
        addHoverEffect(exitBtn);

        topBar.getChildren().addAll(muteBtn, exitBtn);
        root.setTop(topBar);

        VBox playerSide = createPlayerCard("🟢 PLAYER 1", "#00b894", true);
        pDiceView = new ImageView(getDiceImage(1));
        pDiceView.setFitWidth(80); pDiceView.setFitHeight(80);
        pDiceView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0, 184, 148, 0.8), 15, 0, 0, 0);");
        
        pRollBtn = new Button("🎲 ROLL DICE");
        pRollBtn.setPrefWidth(200);
        addHoverEffect(pRollBtn); 
        
        pPowerBtn = new Button("⚡ USE POWERUP");
        pPowerBtn.setPrefWidth(200);
        addHoverEffect(pPowerBtn);

        pRollBtn.setOnAction(e -> handleRoll());
        pPowerBtn.setOnAction(e -> handlePowerup());

        playerSide.getChildren().addAll(pDiceView, pRollBtn, pPowerBtn);
        root.setLeft(playerSide);
        BorderPane.setMargin(playerSide, new Insets(10, 10, 20, 20));

        String p2Title = isVsComputer ? "🤖 COMPUTER" : "🔴 PLAYER 2";
        VBox opponentSide = createPlayerCard(p2Title, "#e84393", false);
        oDiceView = new ImageView(getDiceImage(1));
        oDiceView.setFitWidth(80); oDiceView.setFitHeight(80);
        oDiceView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(232, 67, 147, 0.8), 15, 0, 0, 0);");
        
        oRollBtn = new Button("🎲 ROLL DICE");
        oRollBtn.setPrefWidth(200);
        addHoverEffect(oRollBtn); 
        
        oPowerBtn = new Button("⚡ USE POWERUP");
        oPowerBtn.setPrefWidth(200);
        addHoverEffect(oPowerBtn);

        oRollBtn.setOnAction(e -> handleRoll());
        oPowerBtn.setOnAction(e -> handlePowerup());

        opponentSide.getChildren().add(oDiceView);
        
        if (!isVsComputer) {
            opponentSide.getChildren().addAll(oRollBtn, oPowerBtn);
        }
        
        root.setRight(opponentSide);
        BorderPane.setMargin(opponentSide, new Insets(10, 20, 20, 10));

        VBox bottomControlPanel = new VBox(15);
        bottomControlPanel.setAlignment(Pos.CENTER);
        bottomControlPanel.setPadding(new Insets(15));
        bottomControlPanel.setStyle("-fx-background-color: rgba(31, 40, 51, 0.9); -fx-border-color: #45a29e; -fx-border-width: 4px 0 0 0;");

        turnLabel = new Label();
        turnLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 26px; -fx-text-fill: #66fcf1; -fx-effect: dropshadow(three-pass-box, rgba(102, 252, 241, 0.6), 10, 0, 0, 0);");

        actionLogLabel = new Label("Game Started! " + game.getCurrent().getName() + " turns first.");
        actionLogLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f1c40f; -fx-font-style: italic; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5 20; -fx-background-radius: 10;");

        bottomControlPanel.getChildren().addAll(turnLabel, actionLogLabel);
        root.setBottom(bottomControlPanel);

        mainContainer.getChildren().add(root);
        scene = new Scene(mainContainer, 1400, 850);
        
        Platform.runLater(() -> {
            app.getWindow().setFullScreenExitHint(""); 
            app.getWindow().setFullScreen(true);
        });
        
        update();
    }

    private void addHoverEffect(Button btn) {
        btn.setOnMouseEntered(e -> { if (!btn.isDisabled()) btn.setOpacity(0.7); });
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    }

    private Image getDiceImage(int number) {
        String dName = "dice" + number + ".png";
        if (!imageCache.containsKey(dName)) {
            imageCache.put(dName, new Image(getClass().getResourceAsStream("/assets/" + dName), 120, 120, true, true));
        }
        return imageCache.get(dName);
    }

    private void playSound(String fileName) {
        if (isMuted) return; 
        try {
            String path = getClass().getResource("/assets/" + fileName).toExternalForm();
            AudioClip clip = new AudioClip(path);
            clip.play();
        } catch (Exception e) {}
    }

    private void logAction(String message) {
        actionLogLabel.setText(">> " + message + " <<");
    }

    private VBox createPlayerCard(String title, String colorHex, boolean isPlayer) {
        VBox card = new VBox(10); 
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(31, 40, 51, 0.8); -fx-border-color: " + colorHex + "; -fx-border-width: 3px; -fx-background-radius: 15; -fx-border-radius: 15;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: " + colorHex + ";");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 5);");
        
        if (isPlayer) pImageView = imageView; else oImageView = imageView;

        Label nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white; -fx-text-alignment: center;");
        if (isPlayer) pName = nameLabel; else oName = nameLabel;

        Label energyLabel = new Label();
        energyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #66fcf1;");
        if (isPlayer) pEnergy = energyLabel; else oEnergy = energyLabel;

        Label posLabel = new Label();
        posLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c5c6c7;");
        if (isPlayer) pPos = posLabel; else oPos = posLabel;

        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setMinHeight(40);
        if (isPlayer) pStatusBox = statusBox; else oStatusBox = statusBox;

        card.getChildren().addAll(titleLabel, imageView, nameLabel, energyLabel, posLabel, statusBox);
        return card;
    }

    private Label createBadge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
        return badge;
    }

    private void handleRoll() {
        Monster actingMonster = game.getCurrent();
        animatingMonster = actingMonster; 
        int startPos = actingMonster.getPosition();
        boolean wasFrozen = actingMonster.isFrozen();

        // قفل كل الزراير أثناء اللعب
        pRollBtn.setDisable(true); pPowerBtn.setDisable(true);
        oRollBtn.setDisable(true); oPowerBtn.setDisable(true);

        if (wasFrozen) {
            try { game.playTurn(); } catch (Exception e) {}
            playSound("freeze.wav");
            logAction(actingMonster.getName() + " is FROZEN and skips their turn!");
            showCustomPopup("Turn Skipped!", actingMonster.getName() + " was frozen and skipped this turn.", this::finishTurn);
            return;
        }

        try {
            game.playTurn();
            int actualRoll = game.getLastRoll();
            int finalPos = actingMonster.getPosition();
            
            int tempIntermediate = finalPos; 
            if (startPos != finalPos) {
                for (int i = startPos + 1; i <= 99; i++) {
                    Cell c = game.getBoard().getBoardCells()[i / 10][i % 10];
                    if (c instanceof TransportCell) {
                        if (i + ((TransportCell) c).getEffect() == finalPos) {
                            tempIntermediate = i; 
                            break;
                        }
                    }
                }
            }

            final int intermediatePos = tempIntermediate;
            ImageView activeDiceView = (actingMonster.getName().equals(game.getPlayer().getName())) ? pDiceView : oDiceView;

            showInlineDiceAnimation(activeDiceView, actualRoll, () -> {
                logAction(actingMonster.getName() + " rolled a " + actualRoll + "!");

                animateStepping(startPos, intermediatePos, () -> {
                    
                    if (intermediatePos != finalPos) {
                        if (finalPos < intermediatePos) {
                            highlights[finalPos].setStroke(javafx.scene.paint.Color.web("#e74c3c"));
                            playSound("error.wav"); 
                            logAction("Oh no! " + actingMonster.getName() + " stepped on a Contamination Sock!");
                        } else {
                            highlights[finalPos].setStroke(javafx.scene.paint.Color.web("#f1c40f"));
                            playSound("roll.wav"); 
                            logAction("Awesome! " + actingMonster.getName() + " took a Conveyor Belt!");
                        }
                        
                        highlights[finalPos].setVisible(true);

                        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
                        pause.setOnFinished(e -> {
                            highlights[finalPos].setVisible(false);
                            overrideCurrentMonsterPos = finalPos;
                            playCellSoundAndLog(finalPos, actingMonster); 
                            drawBoard();
                            finishTurn();
                        });
                        pause.play();
                    } else {
                        playCellSoundAndLog(finalPos, actingMonster);
                        finishTurn();
                    }
                });
            });

        } catch (InvalidMoveException ex) {
            showCustomPopup("Invalid Move", ex.getMessage(), this::finishTurn);
        } catch (Throwable ex) {
            ex.printStackTrace();
            showCustomPopup("Logic Error", "Error: " + ex.getMessage(), this::finishTurn);
        }
    }

    private void showInlineDiceAnimation(ImageView diceView, int actualRoll, Runnable onFinished) {
        playSound("roll.wav"); 
        Timeline timeline = new Timeline();
        for (int i = 0; i < 15; i++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(80 * i), e -> {
                diceView.setImage(getDiceImage((int) (Math.random() * 6) + 1));
            }));
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(80 * 15), e -> {
            diceView.setImage(getDiceImage(actualRoll));
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> onFinished.run());
            delay.play();
        }));
        timeline.play();
    }

    private void animateStepping(int from, int to, Runnable onFinished) {
        Timeline timeline = new Timeline();
        int steps = to - from;
        if (steps <= 0) {
            onFinished.run();
            return;
        }

        for (int i = 1; i <= steps; i++) {
            int currentStep = from + i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(250 * i), e -> {
                overrideCurrentMonsterPos = currentStep;
                drawBoard();
            }));
        }
        timeline.setOnFinished(e -> onFinished.run());
        timeline.play();
    }

    private void playCellSoundAndLog(int pos, Monster m) {
        if (pos >= 100) return;
        Cell cell = game.getBoard().getBoardCells()[pos / 10][pos % 10];
        
        if (cell instanceof DoorCell) {
            playSound("door.wav");
            logAction(m.getName() + " found a Door! Energy Updated.");
        } else if (cell instanceof CardCell) {
            playSound("card.wav");
            logAction(m.getName() + " drew a Special Card!");
        } else if (cell instanceof ContaminationSock) {
            playSound("error.wav");
        } else {
            playSound("bonus.wav");
        }
    }

    private void finishTurn() {
        overrideCurrentMonsterPos = null; 
        animatingMonster = null; 
        update();
        checkWinnerStatus();

        if (game.getWinner() == null) {
            boolean isPlayer1Turn = game.getCurrent().getName().equals(game.getPlayer().getName());
            
            if (!isPlayer1Turn && isVsComputer) {
                turnLabel.setText("▶ COMPUTER IS THINKING... ◀");
                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> handleRoll()); 
                pause.play();
            } else {
                updateControlsVisibility();
            }
        }
    }

    private void updateControlsVisibility() {
        boolean p1Turn = game.getCurrent().getName().equals(game.getPlayer().getName());
        boolean p2Turn = !p1Turn;

        pRollBtn.setDisable(!p1Turn);
        styleButton(pRollBtn, p1Turn, "#00b894");

        boolean p1CanPower = p1Turn && game.getPlayer().getEnergy() >= POWERUP_COST;
        pPowerBtn.setDisable(!p1CanPower);
        styleButton(pPowerBtn, p1CanPower, "#f1c40f");

        if (!isVsComputer) {
            oRollBtn.setDisable(!p2Turn);
            styleButton(oRollBtn, p2Turn, "#e84393"); // لون الخصم وردي

            boolean p2CanPower = p2Turn && game.getOpponent().getEnergy() >= POWERUP_COST;
            oPowerBtn.setDisable(!p2CanPower);
            styleButton(oPowerBtn, p2CanPower, "#f1c40f");
        }
    }

    private void styleButton(Button btn, boolean isEnabled, String activeColor) {
        if (isEnabled) {
            btn.setStyle("-fx-background-color: " + activeColor + "; -fx-text-fill: " + (activeColor.equals("#f1c40f") ? "#2c3e50" : "white") + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #555555; -fx-text-fill: #888888; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10; -fx-background-radius: 8;");
        }
    }

    private void handlePowerup() {
        try {
            game.usePowerup();
            playSound("powerup.wav"); 
            logAction(game.getCurrent().getName() + " activated their POWERUP!");
            showCustomPopup("Powerup Activated!", game.getCurrent().getName() + " used their special ability.", this::update);
        } catch (OutOfEnergyException ex) {
            showCustomPopup("Not Enough Energy", ex.getMessage(), null);
        } catch (Throwable ex) {
            showCustomPopup("Logic Error", "Error: " + ex.getMessage(), null);
        }
    }

    private void update() {
        drawBoard();
        String currentTurnName = game.getCurrent().getName().toUpperCase();
        
        if (isVsComputer && !game.getCurrent().getName().equals(game.getPlayer().getName())) {
            turnLabel.setText("▶ COMPUTER IS THINKING... ◀");
        } else {
            turnLabel.setText("▶ CURRENT TURN: " + currentTurnName + " ◀");
        }
        
        updateCardData(game.getPlayer(), pImageView, pName, pEnergy, pPos, pStatusBox);
        updateCardData(game.getOpponent(), oImageView, oName, oEnergy, oPos, oStatusBox);
        
        if (animatingMonster == null) {
            updateControlsVisibility();
        }
    }

    private void updateCardData(Monster m, ImageView iv, Label name, Label energy, Label pos, HBox statusBox) {
        try {
            String imgName = m.getName() + ".png";
            Image img = imageCache.get("HD_" + imgName); 
            if (img == null) {
                img = new Image(getClass().getResourceAsStream("/assets/" + imgName), 200, 200, true, true);
                imageCache.put("HD_" + imgName, img);
            }
            iv.setImage(img);
        } catch (Exception e) {}

        name.setText(m.getName() + "\n(" + m.getRole() + ")");
        energy.setText("Energy: " + m.getEnergy() + " ⚡");
        pos.setText("Position: " + m.getPosition());

        statusBox.getChildren().clear();
        if (m.isConfused()) statusBox.getChildren().add(createBadge("Confused (" + m.getConfusionTurns() + ")", "#9b59b6"));
        if (m.isShielded()) statusBox.getChildren().add(createBadge("Shielded", "#3498db"));
        if (m.isFrozen()) statusBox.getChildren().add(createBadge("Frozen", "#00cec9"));
    }

    private void drawBoard() {
        grid.getChildren().clear();
        Cell[][] cells = game.getBoard().getBoardCells();
        
        int pDrawPos = game.getPlayer().getPosition();
        int oDrawPos = game.getOpponent().getPosition();

        if (animatingMonster != null && overrideCurrentMonsterPos != null) {
            if (animatingMonster.getName().equals(game.getPlayer().getName())) {
                pDrawPos = overrideCurrentMonsterPos;
            } else if (animatingMonster.getName().equals(game.getOpponent().getName())) {
                oDrawPos = overrideCurrentMonsterPos;
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                
                int index = (r % 2 == 0) ? (r * 10 + c) : (r * 10 + (9 - c));
                int displayCol = c;
                int displayRow = 9 - r; 

                Cell cell = cells[r][c];
                StackPane cellStack = new StackPane(); 
                cellPanes[index] = cellStack; 
                
                Button b = new Button();
                b.setPrefSize(75, 75); 

                boolean isDark = (r + displayCol) % 2 == 0;
                String bgColor = isDark ? "rgba(31, 40, 51, 0.6)" : "rgba(11, 12, 16, 0.6)"; 
                b.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 0;");

                String cellImageName = null;
                if (cell instanceof DoorCell) cellImageName = "door.png";
                else if (cell instanceof CardCell) cellImageName = "card.png";
                else if (cell instanceof ConveyorBelt) cellImageName = "conveyor.png";
                else if (cell instanceof ContaminationSock) cellImageName = "sock.png";
                else if (cell instanceof MonsterCell) cellImageName = "monster_cell.png";

                if (cellImageName != null) {
                    try {
                        Image cellImg = imageCache.get(cellImageName);
                        if (cellImg == null) {
                            cellImg = new Image(getClass().getResourceAsStream("/assets/" + cellImageName), 100, 100, true, true);
                            imageCache.put(cellImageName, cellImg);
                        }
                        
                        ImageView cellIcon = new ImageView(cellImg);
                        cellIcon.setFitWidth(65); 
                        cellIcon.setFitHeight(65);
                        cellIcon.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.7), 8, 0, 0, 3);");
                        b.setGraphic(cellIcon);
                        cellStack.getChildren().add(b);

                        if (cell instanceof DoorCell) {
                            DoorCell dc = (DoorCell) cell;
                            if (dc.isActivated()) {
                                cellIcon.setOpacity(0.3);
                            } else {
                                String roleColor = dc.getRole() == Role.SCARER ? "#e84393" : "#00b894";
                                String roleLetter = dc.getRole() == Role.SCARER ? "S" : "L";
                                Label doorBadge = new Label(roleLetter + " | " + dc.getEnergy());
                                doorBadge.setStyle("-fx-background-color: " + roleColor + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 4; -fx-background-radius: 4;");
                                StackPane.setAlignment(doorBadge, Pos.BOTTOM_CENTER);
                                StackPane.setMargin(doorBadge, new Insets(0, 0, 5, 0));
                                cellStack.getChildren().add(doorBadge);
                            }
                        }
                        
                        if (cell instanceof TransportCell) {
                            int effect = ((TransportCell) cell).getEffect();
                            int dest = index + effect; 
                            
                            if (dest >= 0 && dest < 100 && effect != 0) {
                                String dir = effect > 0 ? "⬆" : "⬇";
                                String badgeColor = effect > 0 ? "#00b894" : "#e74c3c"; 
                                
                                Label beltBadge = new Label(dir + " To " + dest);
                                beltBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 4; -fx-background-radius: 4;");
                                StackPane.setAlignment(beltBadge, Pos.BOTTOM_CENTER);
                                StackPane.setMargin(beltBadge, new Insets(0, 0, 5, 0));
                                cellStack.getChildren().add(beltBadge);
                            }
                        }

                    } catch (Exception e) {}
                } else {
                    cellStack.getChildren().add(b);
                }

                Label numLabel = new Label(String.valueOf(index));
                numLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.4); -fx-font-size: 11px; -fx-font-weight: bold;");
                StackPane.setAlignment(numLabel, Pos.TOP_RIGHT);
                StackPane.setMargin(numLabel, new Insets(2, 4, 0, 0));
                cellStack.getChildren().add(numLabel);

                javafx.scene.shape.Rectangle highlight = new javafx.scene.shape.Rectangle(75, 75);
                highlight.setFill(javafx.scene.paint.Color.TRANSPARENT);
                highlight.setStroke(javafx.scene.paint.Color.web("#f1c40f")); 
                highlight.setStrokeWidth(4);
                highlight.setVisible(false); 
                highlights[index] = highlight;
                cellStack.getChildren().add(highlight);

                if (pDrawPos == index) {
                    addMonsterToCell(cellStack, game.getPlayer(), Pos.TOP_LEFT);
                }
                if (oDrawPos == index) {
                    addMonsterToCell(cellStack, game.getOpponent(), Pos.BOTTOM_RIGHT);
                }

                grid.add(cellStack, displayCol, displayRow);
            }
        }
    }

    private void addMonsterToCell(StackPane stack, Monster monster, Pos position) {
        try {
            String imageName = monster.getName() + ".png";
            Image img = imageCache.get(imageName);
            if (img == null) {
                img = new Image(getClass().getResourceAsStream("/assets/" + imageName), 80, 80, true, true);
                imageCache.put(imageName, img);
            }
            
            ImageView iv = new ImageView(img);
            iv.setFitWidth(45); 
            iv.setFitHeight(45);
            
            boolean isCurrentTurn = monster.getName().equals(game.getCurrent().getName());
            String shadowEffect = isCurrentTurn 
                ? "-fx-effect: dropshadow(three-pass-box, rgba(102, 252, 241, 0.9), 15, 0.6, 0, 0);" 
                : "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);"; 
            
            iv.setStyle(shadowEffect);
            
            stack.getChildren().add(iv);
            StackPane.setAlignment(iv, position);
        } catch (Exception e) {}
    }

    private void checkWinnerStatus() {
        Monster w = game.getWinner();
        if (w != null) {
            playSound("win.wav"); 
            showCustomPopup("🏆 MATCH FINISHED 🏆", w.getName() + " HAS WON THE GAME!\nFinal Energy: " + w.getEnergy(), () -> {
                app.getWindow().setFullScreen(false);
                app.getWindow().setScene(new StartMenu(app).getScene());
            });
        }
    }

    private void showCustomPopup(String title, String message, Runnable onConfirm) {
        StackPane rootContainer = (StackPane) scene.getRoot();
        
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setOnMouseClicked(e -> e.consume()); 
        
        VBox dialog = new VBox(20);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPadding(new Insets(30));
        dialog.setMaxWidth(450);
        dialog.setMaxHeight(250);
        dialog.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #45a29e; -fx-border-width: 3px; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 0);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: " + (title.contains("Error") || title.contains("Invalid") ? "#e74c3c" : "#66fcf1") + ";");
        
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-text-alignment: center;");
        msgLabel.setWrapText(true);
        
        Button okBtn = new Button("CONTINUE");
        okBtn.setStyle("-fx-background-color: #45a29e; -fx-text-fill: #0b0c10; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        okBtn.setOnAction(e -> {
            rootContainer.getChildren().remove(overlay); 
            if (onConfirm != null) onConfirm.run(); 
        });
        
        dialog.getChildren().addAll(titleLabel, msgLabel, okBtn);
        overlay.getChildren().add(dialog);
        
        rootContainer.getChildren().add(overlay);
    }

    public Scene getScene() { return scene; }
}