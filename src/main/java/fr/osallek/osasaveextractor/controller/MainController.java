package fr.osallek.osasaveextractor.controller;

import fr.osallek.osasaveextractor.common.Constants;
import fr.osallek.osasaveextractor.config.ApplicationProperties;
import fr.osallek.osasaveextractor.controller.object.AutoCompleteTextField;
import fr.osallek.osasaveextractor.controller.object.BootstrapColumn;
import fr.osallek.osasaveextractor.controller.object.BootstrapPane;
import fr.osallek.osasaveextractor.controller.object.BootstrapRow;
import fr.osallek.osasaveextractor.controller.object.LocalSaveListCell;
import fr.osallek.osasaveextractor.controller.object.SteamIdConverter;
import fr.osallek.osasaveextractor.service.Eu4Service;
import fr.osallek.osasaveextractor.service.ServerService;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MainController {

    private final Eu4Service eu4Service;

    private final ServerService serverService;

    private final MessageSource messageSource;

    private final Application application;

    private final ApplicationProperties properties;

    private BootstrapPane root;

    private Button finishedButton;

    private ProgressBar progressBar;

    private Text progressText;

    private VBox progressVBox;

    private ComboBox<Map.Entry<String, String>> steamIdBox;

    private ComboBox<Path> localSavesBox;

    private AutoCompleteTextField<ServerSave> serverSavesField;

    private final BooleanProperty serverSavesInvalid = new SimpleBooleanProperty();

    private final BooleanProperty serverInvalid = new SimpleBooleanProperty(false);

    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    private Text errorText;

    private Stage stage;


    public MainController(Eu4Service eu4Service, ServerService serverService, MessageSource messageSource, Application application,
                          ApplicationProperties properties) {
        this.eu4Service = eu4Service;
        this.serverService = serverService;
        this.messageSource = messageSource;
        this.application = application;
        this.properties = properties;
    }

    public void prepareView() throws IOException {
        this.root = new BootstrapPane();
        this.root.setPadding(new Insets(50));
        this.root.setVgap(25);
        this.root.setHgap(25);
        this.root.setBackground(new Background(new BackgroundFill(null, null, null)));

        BootstrapRow errorRow = new BootstrapRow(true);

        this.errorText = new Text(this.messageSource.getMessage("ose.progress.error", null, Locale.getDefault()));
        this.errorText.getStyleClass().addAll("alert", "alert-danger");
        this.errorText.setTextAlignment(TextAlignment.CENTER);
        this.errorText.setVisible(false);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(this.errorText);

        errorRow.addColumn(new BootstrapColumn(hBox, new int[] {12, 12, 10, 8, 6}));

        BootstrapRow titleRow = new BootstrapRow(true);
        Label title = new Label("Osa Save Extractor");
        title.setAlignment(Pos.TOP_CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("h1");
        titleRow.addColumn(new BootstrapColumn(title, new int[] {12, 12, 10, 8, 6}));

        this.serverSavesField = new AutoCompleteTextField<>(new LinkedHashMap<>());
        this.serverSavesField.disableProperty().bind(this.loading);
        this.serverSavesField.textProperty()
                             .addListener((observable, oldValue, newValue) ->
                                                  this.serverSavesInvalid.set(StringUtils.isNotBlank(newValue)
                                                                              && this.serverSavesField.getEntries().stream().noneMatch(newValue::equals)
                                                                              && !Constants.UUID_PATTERN.matcher(newValue).matches()));

        BootstrapRow idRow = new BootstrapRow(true);
        Panel idPanel = new Panel();
        idPanel.getStyleClass().add("panel-default");
        idPanel.setMaxWidth(Double.MAX_VALUE);
        idPanel.setBorder(new Border(new BorderStroke(Color.VIOLET, BorderStrokeStyle.SOLID, null, null)));

        Label idTitleLabel = new Label(this.messageSource.getMessage("ose.id", null, Locale.getDefault()));
        idTitleLabel.getStyleClass().addAll("h5", "b");

        Label idSubTitleLabel = new Label(this.messageSource.getMessage("ose.id.desc", null, Locale.getDefault()));
        idSubTitleLabel.setPadding(new Insets(5, 0, 0, 0));
        idSubTitleLabel.getStyleClass().addAll("h6", "text-mute");

        VBox idTitleVbox = new VBox();
        idTitleVbox.getChildren().add(idTitleLabel);
        idTitleVbox.getChildren().add(idSubTitleLabel);

        idPanel.setHeading(idTitleVbox);

        if (MapUtils.isEmpty(this.eu4Service.getSteamIds())) {
            this.serverInvalid.set(true);
            this.errorText.setText(this.messageSource.getMessage("ose.steam.error", null, Locale.getDefault()));
            this.errorText.setVisible(true);
        } else {
            this.steamIdBox = new ComboBox<>(FXCollections.observableArrayList(this.eu4Service.getSteamIds().entrySet()));
            this.steamIdBox.getSelectionModel().select(0);
            this.steamIdBox.setVisibleRowCount(20);
            this.steamIdBox.setConverter(new SteamIdConverter());
            this.steamIdBox.setPromptText(this.messageSource.getMessage("ose.id", null, Locale.getDefault()));
            this.steamIdBox.disableProperty().bind(this.loading.or(this.serverInvalid));
            this.steamIdBox.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    fetchServerSaves();
                }
            });
        }

        Button savesButton = new Button(this.messageSource.getMessage("ose.view-saves", null, Locale.getDefault()));
        savesButton.getStyleClass().addAll("btn", "btn-default");
        savesButton.setOnAction(event -> this.application.getHostServices()
                                                         .showDocument(this.properties.getFrontUrl() + "/user/" + this.steamIdBox.getSelectionModel()
                                                                                                                                 .selectedItemProperty()
                                                                                                                                 .get().getKey()));

        HBox idHBox = new HBox();
        idHBox.setSpacing(20);
        idHBox.setAlignment(Pos.CENTER_LEFT);
        idHBox.getChildren().add(this.steamIdBox);
        idHBox.getChildren().add(savesButton);

        idPanel.setBody(idHBox);

        idRow.addColumn(new BootstrapColumn(idPanel, new int[] {12, 12, 10, 8, 6}));

        BootstrapRow localSavesRow = new BootstrapRow(true);
        Panel localSavesPanel = new Panel();
        localSavesPanel.getStyleClass().add("panel-default");

        Label localSavesTitleLabel = new Label(this.messageSource.getMessage("ose.local-saves", null,
                                                                             Locale.getDefault()));
        localSavesTitleLabel.getStyleClass().addAll("h5", "b");
        localSavesPanel.setHeading(localSavesTitleLabel);

        List<Path> localSaves = this.eu4Service.getSaves();

        if (CollectionUtils.isNotEmpty(localSaves)) {
            this.localSavesBox = new ComboBox<>(FXCollections.observableArrayList(localSaves));
            this.localSavesBox.setVisibleRowCount(20);
            this.localSavesBox.setCellFactory(param -> new LocalSaveListCell(this.eu4Service));
            this.localSavesBox.setButtonCell(new LocalSaveListCell(this.eu4Service));
            this.localSavesBox.setPromptText(this.messageSource.getMessage("ose.local-saves.choose", null,
                                                                           Locale.getDefault()));
            this.localSavesBox.disableProperty().bind(this.loading.or(this.serverInvalid));
            localSavesPanel.setBody(this.localSavesBox);
        } else {
            localSavesPanel.setBody(new Text(this.messageSource.getMessage("ose.saves.none", null,
                                                                           Locale.getDefault())));
        }

        localSavesRow.addColumn(new BootstrapColumn(localSavesPanel, new int[] {12, 12, 10, 8, 6}));

        BootstrapRow serverSavesRow = new BootstrapRow(true);
        Panel serverSavesPanel = new Panel();
        serverSavesPanel.getStyleClass().add("panel-default");

        Label serverSavesTitleLabel = new Label(this.messageSource.getMessage("ose.server-saves", null,
                                                                              Locale.getDefault()));
        serverSavesTitleLabel.getStyleClass().addAll("h5", "b");
        serverSavesPanel.setHeading(serverSavesTitleLabel);

        Label label = new Label(this.messageSource.getMessage("ose.server-saves.format", null, Locale.getDefault()));
        label.visibleProperty().bind(this.serverSavesInvalid);
        label.setTextFill(Color.RED);

        VBox vBox = new VBox();
        vBox.setSpacing(8);
        vBox.getChildren().add(this.serverSavesField);
        vBox.getChildren().add(label);

        serverSavesPanel.setBody(vBox);
        serverSavesRow.addColumn(new BootstrapColumn(serverSavesPanel, new int[] {12, 12, 10, 8, 6}));

        fetchServerSaves();

        BootstrapRow actionRow = new BootstrapRow(true);
        Button submitButton = new Button(this.messageSource.getMessage("ose.analyse", null, Locale.getDefault()));
        submitButton.getStyleClass().addAll("btn", "btn-primary");
        submitButton.disableProperty().bind(this.localSavesBox.getSelectionModel()
                                                              .selectedItemProperty()
                                                              .isNull()
                                                              .or(this.loading)
                                                              .or(this.serverSavesInvalid)
                                                              .or(this.serverInvalid));
        submitButton.setOnAction(event -> {
            this.errorText.setVisible(false);
            this.finishedButton.setVisible(false);
            this.loading.setValue(true);
            this.progressVBox.setVisible(true);
            this.eu4Service.parseSave(this.localSavesBox.getSelectionModel().selectedItemProperty().get(),
                                      this.serverSavesField.getSelected() != null ? this.serverSavesField.getSelected().id() : this.serverSavesField.getText(),
                                      this.steamIdBox.getSelectionModel().selectedItemProperty().get().getKey(),
                                      s -> this.errorText.setText(this.messageSource.getMessage("ose.server.error." + s, null, Locale.getDefault())))
                           .whenComplete((o, throwable) -> {
                               this.loading.set(false);

                               if (throwable == null) {
                                   this.finishedButton.setVisible(true);
                               } else {
                                   this.errorText.setVisible(true);
                               }
                           })
                           .thenAccept(unused -> {
                               this.serverSavesField.setEntries(this.serverService.getSaves(this.steamIdBox.getSelectionModel().getSelectedItem().getKey())
                                                                                  .stream()
                                                                                  .limit(20)
                                                                                  .collect(Collectors.toMap(s -> s.toString(this.messageSource),
                                                                                                            Function.identity(),
                                                                                                            (a, b) -> a, LinkedHashMap::new)));
                               Platform.runLater(() -> {
                                   this.localSavesBox.getSelectionModel().clearSelection();
                                   this.serverSavesField.clear();
                               });
                           });
            this.progressBar.progressProperty().bind(this.eu4Service.getState().progressProperty().divide(100d));
            this.progressText.textProperty().bind(this.eu4Service.getState().labelProperty());
        });

        actionRow.addColumn(new BootstrapColumn(submitButton, new int[] {12, 12, 10, 8, 6}));

        BootstrapRow progressRow = new BootstrapRow(true);

        this.progressBar = new ProgressBar(0);
        this.progressBar.getStyleClass().add("progress-bar-primary");
        this.progressBar.setMaxWidth(Double.MAX_VALUE);

        this.progressText = new Text("Progress");

        this.finishedButton = new Button(this.messageSource.getMessage("ose.view-save", null, Locale.getDefault()));
        this.finishedButton.getStyleClass().addAll("btn", "btn-primary");
        this.finishedButton.setVisible(false);
        this.finishedButton.setOnAction(event -> this.application.getHostServices().showDocument(this.eu4Service.getState().getLink()));

        this.progressVBox = new VBox(10);
        this.progressVBox.setAlignment(Pos.CENTER);
        this.progressVBox.getChildren().addAll(this.progressBar, this.progressText, this.finishedButton);
        this.progressVBox.setVisible(false);

        progressRow.addColumn(new BootstrapColumn(this.progressVBox, new int[] {12, 12, 10, 8, 6}));

        this.root.addRow(titleRow);
        this.root.addRow(idRow);
        this.root.addRow(localSavesRow);
        this.root.addRow(serverSavesRow);
        this.root.addRow(actionRow);
        this.root.addRow(progressRow);
        this.root.addRow(errorRow);
    }

    private void fetchServerSaves() {
        try {
            SortedSet<ServerSave> serverSaves = this.serverService.getSaves(this.steamIdBox.getSelectionModel().getSelectedItem().getKey());
            this.serverInvalid.set(false);

            this.serverSavesField.setEntries(serverSaves.stream()
                                                        .limit(20)
                                                        .collect(Collectors.toMap(s -> s.toString(this.messageSource), Function.identity(), (a, b) -> a,
                                                                                  LinkedHashMap::new)));
        } catch (Exception e) {
            this.serverInvalid.set(true);
            this.errorText.setText(this.messageSource.getMessage("ose.server.error", null, Locale.getDefault()));
            this.errorText.setVisible(true);
        }
    }

    public GridPane getScene() throws IOException {
        if (this.root == null) {
            prepareView();
        }

        return this.root;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
