package com.musicplayer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.value.ChangeListener;
import java.io.File;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;
import javafx.util.Duration;


public class MusicPlayer extends Application {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private boolean isPlaying = false;
    private boolean isVideoFile = false;
    private Slider timeSlider;
    private Label timeLabel;
    private Label titleLabel;
    private ProgressBar volumeBar;
    private Circle playPauseButton;
    private Label playPauseLabel;
    private VBox videoBox;
    private double xOffset = 0;
    private double yOffset = 0;
    private Stage primaryStage;
    private VBox mainContainer;
    private Label volumeLabel;
    private boolean isMaximized = false;
    private Rectangle2D backupWindowBounds;
    private double backupX, backupY, backupWidth, backupHeight;
    private ImageView logoImageView;
    private ListView<String> playlistView;
    private ObservableList<String> playlist;
    private List<File> playlistFiles;
    private int currentTrackIndex = 0;
    private TranslateTransition titleAnimation;
    private Label scrollingTitleLabel;
    private HBox titleContainer;


    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Fedhawaq Media Player");

        // Create main container
        mainContainer = new VBox(20);
        mainContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1a1a1a, #2d2d2d);" +
                        "-fx-background-radius: 15;");
        mainContainer.setPadding(new Insets(20));

        // Add window resize listener
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateVideoSize();
        });

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateVideoSize();
        });

        // Create title bar
        HBox titleBar = createTitleBar();

        // Create video section
        videoBox = createVideoSection();
        videoBox.setVisible(false);
        videoBox.setManaged(false);

        // Create media info section
        VBox mediaInfo = createMediaInfoSection();

        // Create controls
        VBox controlsSection = createControlsSection();

        // Create progress section
        VBox progressSection = createProgressSection();

        // Add all sections to main container
        mainContainer.getChildren().addAll(titleBar, videoBox, mediaInfo, controlsSection, progressSection);

        // Make window draggable
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            if (!isMaximized) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        Scene scene = new Scene(mainContainer, 400, 600);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add drop shadow to the window
        mainContainer.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void updateVideoSize() {
        if (mediaView != null && isVideoFile) {
            // Calculate new video size while maintaining aspect ratio
            double windowWidth = primaryStage.getWidth() - 40;
            double windowHeight = primaryStage.getHeight() - 250;
    
            // Set the video size to 90% of the available space
            double videoWidth = windowWidth * 0.9;
            double videoHeight = windowHeight * 0.6;
    
            // Unbind before setting new values
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            
            mediaView.setFitWidth(videoWidth);
            mediaView.setFitHeight(videoHeight);
        }
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10));
        titleBar.setSpacing(10);

        // Create app logo
        try {
            // Replace with your actual logo path
            Image logoImage = new Image(getClass().getResourceAsStream("../../image/mediaImage.jpg"));
            logoImageView = new ImageView(logoImage);
            logoImageView.setFitHeight(30);
            logoImageView.setFitWidth(30);
            logoImageView.setPreserveRatio(true);

            Circle clip = new Circle(15, 15, 15); // Center X, Center Y, Radius
            logoImageView.setClip(clip);
        } catch (Exception e) {
            // Fallback to circle if image loading fails
            Circle appLogo = new Circle(10, Color.valueOf("#3498db"));
            logoImageView = new ImageView();
            logoImageView.setClip(appLogo);
        }

        Label titleText = new Label("GROUP 2 MEDIA PLAYER APP");
        titleText.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = new Button("−");
        Button maximizeButton = new Button("□");
        Button closeButton = new Button("×");

        String buttonStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;";
        minimizeButton.setStyle(buttonStyle);
        maximizeButton.setStyle(buttonStyle);
        closeButton.setStyle(buttonStyle);

        minimizeButton.setOnAction(e -> primaryStage.setIconified(true));
        maximizeButton.setOnAction(e -> toggleMaximize());
        closeButton.setOnAction(e -> primaryStage.close());

        titleBar.getChildren().addAll(logoImageView, titleText, spacer, minimizeButton, maximizeButton, closeButton);
        return titleBar;
    }

    private VBox createVideoSection() {
        VBox videoSection = new VBox();
        videoSection.setAlignment(Pos.CENTER);
        videoSection.setStyle("-fx-background-color: black;");

        mediaView = new MediaView();
        mediaView.setFitWidth(360);
        mediaView.setFitHeight(240);
        mediaView.setPreserveRatio(true);

        mediaView.fitWidthProperty().bind(videoSection.widthProperty());
        mediaView.fitHeightProperty().bind(videoSection.heightProperty());

        videoSection.getChildren().add(mediaView);
        return videoSection;
    }

    private VBox createMediaInfoSection() {
        VBox mediaInfo = new VBox(10);
        mediaInfo.setAlignment(Pos.CENTER);

        try {
            Image mediaIconImage = new Image(getClass().getResourceAsStream("../../image/mediaImage.jpg"));
            Circle mediaIcon = new Circle(40);
            mediaIcon.setFill(new ImagePattern(mediaIconImage));
            mediaIcon.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");

            // Create title container with fixed width
            titleContainer = new HBox();
            titleContainer.setAlignment(Pos.CENTER);
            titleContainer.setPrefWidth(300);
            titleContainer.setStyle("-fx-background-color: transparent;");
            titleContainer.setClip(new javafx.scene.shape.Rectangle(300, 30));

            // Create scrolling title label
            scrollingTitleLabel = new Label("No file selected");
            scrollingTitleLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
            );

            titleContainer.getChildren().add(scrollingTitleLabel);

            // Create playlist view
            playlistView = new ListView<>();
            playlistView.setPrefHeight(200);
            playlistView.setStyle(
                "-fx-background-color: #2d2d2d;" +
                "-fx-control-inner-background: #2d2d2d;" +
                "-fx-text-fill: white;"
            );
            playlistView.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 5;"
                        );
                    }
                }
            });

            playlist = FXCollections.observableArrayList();
            playlistFiles = new ArrayList<>();
            playlistView.setItems(playlist);

            playlistView.setOnMouseClicked(e -> {
                int index = playlistView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    currentTrackIndex = index;
                    playFile(playlistFiles.get(index));
                }
            });

            mediaInfo.getChildren().addAll(mediaIcon, titleContainer, playlistView);
        } catch (Exception e) {
            Circle mediaIconFallback = new Circle(40, Color.valueOf("#3498db"));
            mediaIconFallback.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
            mediaInfo.getChildren().addAll(mediaIconFallback, titleContainer, playlistView);
        }

        return mediaInfo;
    }

    private void startTitleAnimation(String title) {
        if (titleAnimation != null) {
            titleAnimation.stop();
        }

        scrollingTitleLabel.setText(title);
        
        // Reset position
        scrollingTitleLabel.setTranslateX(0);
        
        // Calculate if animation is needed
        double textWidth = scrollingTitleLabel.getBoundsInLocal().getWidth();
        double containerWidth = titleContainer.getWidth();
        
        if (textWidth > containerWidth) {
            titleAnimation = new TranslateTransition(Duration.seconds(8), scrollingTitleLabel);
            titleAnimation.setFromX(0);
            titleAnimation.setToX(-(textWidth - containerWidth));
            titleAnimation.setCycleCount(TranslateTransition.INDEFINITE);
            titleAnimation.setAutoReverse(true);
            titleAnimation.play();
        }
    }


    private VBox createControlsSection() {
        VBox controlsSection = new VBox(20);
        controlsSection.setAlignment(Pos.CENTER);

        HBox controls = new HBox(30);
        controls.setAlignment(Pos.CENTER);

        Button prevButton = createCircularButton("⏮");
        playPauseButton = new Circle(25, Color.valueOf("#3498db"));
        Button nextButton = createCircularButton("⏭");

        playPauseLabel = new Label("▶");
        playPauseLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;");

        StackPane playPauseStack = new StackPane(playPauseButton, playPauseLabel);
        playPauseStack.setOnMouseClicked(e -> togglePlayPause());

        controls.getChildren().addAll(prevButton, playPauseStack, nextButton);

        // Volume control
        HBox volumeControl = new HBox(10);
        volumeControl.setAlignment(Pos.CENTER);

        Slider volumeSlider = new Slider(0, 1, 0.7);
        volumeSlider.setPrefWidth(200);
        volumeSlider.setStyle(
                "-fx-control-inner-background: #404040;" +
                        "-fx-accent: #3498db;");

        // Add volume label
        volumeLabel = new Label("70%");
        volumeLabel.setStyle("-fx-text-fill: white;");

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
            // Update volume label
            int volumePercent = (int) (newVal.doubleValue() * 100);
            volumeLabel.setText(volumePercent + "%");
        });


        prevButton.setOnAction(e -> {
            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                playFile(playlistFiles.get(currentTrackIndex));
            }
        });

        nextButton.setOnAction(e -> {
            if (currentTrackIndex < playlistFiles.size() - 1) {
                currentTrackIndex++;
                playFile(playlistFiles.get(currentTrackIndex));
            }
        });

        Label volumeIcon = new Label("🔊");
        volumeIcon.setStyle("-fx-text-fill: white;");

        volumeControl.getChildren().addAll(volumeIcon, volumeSlider, volumeLabel);

        controlsSection.getChildren().addAll(controls, volumeControl);
        return controlsSection;
    }

    private Button createCircularButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #404040;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-min-width: 40px;" +
                        "-fx-min-height: 40px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-cursor: hand;");
        return button;
    }

    private VBox createProgressSection() {
        VBox progressSection = new VBox(5);
        progressSection.setAlignment(Pos.CENTER);

        timeSlider = new Slider();
        timeSlider.setStyle(
                "-fx-control-inner-background: #404040;" +
                        "-fx-accent: #3498db;");

        timeLabel = new Label("0:00 / 0:00");
        timeLabel.setStyle("-fx-text-fill: white;");

        Button selectButton = new Button("Select Media File");
        selectButton.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-cursor: hand;");
        selectButton.setOnAction(e -> selectFile());

        progressSection.getChildren().addAll(timeSlider, timeLabel, selectButton);
        return progressSection;
    }

    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Media Files", "*.mp3", "*.wav", "*.mp4", "*.avi", "*.mkv"),
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"),
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv")
        );
        
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            playlist.clear();
            playlistFiles.clear();
            
            for (File file : selectedFiles) {
                playlist.add(file.getName());
                playlistFiles.add(file);
            }
            
            currentTrackIndex = 0;
            playFile(playlistFiles.get(0));
        }
    }

    // Add new method to play a specific file
    private void playFile(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        String path = file.toURI().toString();
        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        String extension = path.toLowerCase();
        isVideoFile = extension.endsWith(".mp4") || extension.endsWith(".avi") || extension.endsWith(".mkv");

        videoBox.setVisible(isVideoFile);
        videoBox.setManaged(isVideoFile);

        if (isVideoFile) {
            primaryStage.setHeight(800);
            updateVideoSize();
        } else {
            primaryStage.setHeight(600);
        }

        startTitleAnimation(file.getName());
        setupMediaPlayer();
        playMedia();
    }

    private void setupMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            timeSlider.setValue(newValue.toSeconds());
            updateTimeLabel();
        });

        mediaPlayer.setOnReady(() -> {
            Duration total = mediaPlayer.getTotalDuration();
            timeSlider.setMax(total.toSeconds());
            updateTimeLabel();
        });

        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            if (currentTrackIndex < playlistFiles.size() - 1) {
                currentTrackIndex++;
                playFile(playlistFiles.get(currentTrackIndex));
            } else {
                currentTrackIndex = 0;
                pauseMedia();
            }
        });

    }

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();
            timeLabel.setText(formatTime(current) + " / " + formatTime(total));
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) Math.floor(duration.toMinutes());
        int seconds = (int) Math.floor(duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                pauseMedia();
            } else {
                playMedia();
            }
        }
    }

    private void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            playPauseLabel.setText("⏸");
        }
    }

    private void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseLabel.setText("▶");
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }

    private void toggleMaximize() {
        if (!isMaximized) {
            backupWindowBounds = new Rectangle2D(primaryStage.getX(), primaryStage.getY(), 
                                               primaryStage.getWidth(), primaryStage.getHeight());
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            isMaximized = true;
        } else {
            primaryStage.setX(backupWindowBounds.getMinX());
            primaryStage.setY(backupWindowBounds.getMinY());
            primaryStage.setWidth(backupWindowBounds.getWidth());
            primaryStage.setHeight(backupWindowBounds.getHeight());
            isMaximized = false;
        }
        updateVideoSize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}