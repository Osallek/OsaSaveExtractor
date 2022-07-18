package fr.osallek.osasaveextractor.controller;

import fr.osallek.osasaveextractor.OsaSaveExtractorApplication;
import fr.osallek.osasaveextractor.common.Constants;
import fr.osallek.osasaveextractor.controller.object.AutoCompleteTextField;
import fr.osallek.osasaveextractor.controller.object.BootstrapColumn;
import fr.osallek.osasaveextractor.controller.object.BootstrapPane;
import fr.osallek.osasaveextractor.controller.object.BootstrapRow;
import fr.osallek.osasaveextractor.controller.object.LocalSaveListCell;
import fr.osallek.osasaveextractor.service.Eu4Service;
import fr.osallek.osasaveextractor.service.ServerService;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    private static final String DEFAULT = "default";

    private final Eu4Service eu4Service;

    private final ServerService serverService;

    private final MessageSource messageSource;

    private final Application application;

    private BootstrapPane root;

    private Button finishedButton;

    private ProgressBar progressBar;

    private Text progressText;

    private Text errorText;

    private VBox progressVBox;

    private ReadOnlyObjectProperty<Path> localSave;

    private AutoCompleteTextField<ServerSave> serverSavesField;

    private BooleanProperty serverSavesInvalid = new SimpleBooleanProperty();

    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    public MainController(Eu4Service eu4Service, ServerService serverService, MessageSource messageSource, Application application) throws IOException {
        this.eu4Service = eu4Service;
        this.serverService = serverService;
        this.messageSource = messageSource;
        this.application = application;

        prepareView();
    }

    private void prepareView() throws IOException {
        this.root = new BootstrapPane();
        this.root.setPadding(new Insets(50));
        this.root.setVgap(25);
        this.root.setHgap(25);
        this.root.setBackground(new Background(new BackgroundFill(null, null, null)));

        BootstrapRow titleRow = new BootstrapRow(true);
        Label title = new Label("Osa Save Extractor");
        title.setAlignment(Pos.TOP_CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("h1");
        titleRow.addColumn(new BootstrapColumn(title, new int[] {12, 10, 8, 6, 4}));

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

        Label idLabel = new Label(OsaSaveExtractorApplication.ID);
        idPanel.setBody(idLabel);

        idRow.addColumn(new BootstrapColumn(idPanel, new int[] {12, 10, 8, 6, 4}));

        BootstrapRow localSavesRow = new BootstrapRow(true);
        Panel localSavesPanel = new Panel();
        localSavesPanel.getStyleClass().add("panel-default");

        Label localSavesTitleLabel = new Label(this.messageSource.getMessage("ose.local-saves", null,
                                                                             Locale.getDefault()));
        localSavesTitleLabel.getStyleClass().addAll("h5", "b");
        localSavesPanel.setHeading(localSavesTitleLabel);

        List<Path> localSaves = this.eu4Service.getSaves();

        if (CollectionUtils.isNotEmpty(localSaves)) {
            ComboBox<Path> localSavesBox = new ComboBox<>(FXCollections.observableArrayList(localSaves));
            localSavesBox.setVisibleRowCount(20);
            localSavesBox.setCellFactory(param -> new LocalSaveListCell(this.eu4Service));
            localSavesBox.setButtonCell(new LocalSaveListCell(this.eu4Service));
            localSavesBox.setPromptText(this.messageSource.getMessage("ose.local-saves.choose", null,
                                                                      Locale.getDefault()));
            localSavesBox.disableProperty().bind(this.loading);
            this.localSave = localSavesBox.getSelectionModel().selectedItemProperty();
            localSavesPanel.setBody(localSavesBox);
        } else {
            localSavesPanel.setBody(new Text(this.messageSource.getMessage("ose.saves.none", null,
                                                                           Locale.getDefault())));
        }

        localSavesRow.addColumn(new BootstrapColumn(localSavesPanel, new int[] {12, 10, 8, 6, 4}));

        BootstrapRow serverSavesRow = new BootstrapRow(true);
        Panel serverSavesPanel = new Panel();
        serverSavesPanel.getStyleClass().add("panel-default");

        Label serverSavesTitleLabel = new Label(this.messageSource.getMessage("ose.server-saves", null,
                                                                              Locale.getDefault()));
        serverSavesTitleLabel.getStyleClass().addAll("h5", "b");
        serverSavesPanel.setHeading(serverSavesTitleLabel);

        SortedSet<ServerSave> serverSaves = this.serverService.getSaves();

        if (CollectionUtils.isNotEmpty(serverSaves)) {

            this.serverSavesField = new AutoCompleteTextField<>(serverSaves.stream()
                                                                           .collect(Collectors.toMap(s -> s.toString(this.messageSource), Function.identity())));
            this.serverSavesField.disableProperty().bind(this.loading);

            Label label = new Label(this.messageSource.getMessage("ose.server-saves.format", null, Locale.getDefault()));
            label.visibleProperty().bind(this.serverSavesInvalid);
            label.setTextFill(Color.RED);

            this.serverSavesField.textProperty()
                                 .addListener((observable, oldValue, newValue) ->
                                                      this.serverSavesInvalid.set(StringUtils.isNotBlank(newValue)
                                                                                  && serverSaves.stream()
                                                                                                .noneMatch(serverSave -> newValue.equals(serverSave.toString(this.messageSource)))
                                                                                  && !Constants.UUID_PATTERN.matcher(newValue).matches()));

            VBox vBox = new VBox();
            vBox.setSpacing(8);
            vBox.getChildren().add(this.serverSavesField);
            vBox.getChildren().add(label);

            serverSavesPanel.setBody(vBox);
        } else {
            serverSavesPanel.setBody(new Text(this.messageSource.getMessage("ose.saves.none", null,
                                                                            Locale.getDefault())));
        }

        serverSavesRow.addColumn(new BootstrapColumn(serverSavesPanel, new int[] {12, 10, 8, 6, 4}));

        BootstrapRow actionRow = new BootstrapRow(true);
        Button submitButton = new Button(this.messageSource.getMessage("ose.analyse", null, Locale.getDefault()));
        submitButton.getStyleClass().addAll("btn", "btn-primary");
        submitButton.disableProperty().bind(this.localSave.isNull().or(this.loading).or(this.serverSavesInvalid));
        submitButton.setOnAction(event -> {
            this.errorText.setVisible(false);
            this.finishedButton.setVisible(false);
            this.loading.setValue(true);
            this.progressVBox.setVisible(true);
            this.eu4Service.parseSave(this.localSave.get(),
                                      this.serverSavesField.getSelected() != null ? this.serverSavesField.getSelected().id() : this.serverSavesField.getText())
                           .whenComplete((o, throwable) -> {
                               this.loading.set(false);

                               if (throwable == null) {
                                   this.finishedButton.setVisible(true);
                               } else {
                                   this.errorText.setVisible(true);
                               }
                           });
            this.progressBar.progressProperty().bind(this.eu4Service.getState().progressProperty().divide(100d));
            this.progressText.textProperty().bind(this.eu4Service.getState().labelProperty());
        });

        actionRow.addColumn(new BootstrapColumn(submitButton, new int[] {12, 10, 8, 6, 4}));

        BootstrapRow progressRow = new BootstrapRow(true);

        this.progressBar = new ProgressBar(0);
        this.progressBar.getStyleClass().add("progress-bar-primary");
        this.progressBar.setMaxWidth(Double.MAX_VALUE);

        this.progressText = new Text("Progress");
        this.errorText = new Text(this.messageSource.getMessage("ose.progress.error", null, Locale.getDefault()));
        this.errorText.getStyleClass().addAll("alert", "alert-danger");
        this.errorText.setVisible(false);

        this.finishedButton = new Button(this.messageSource.getMessage("ose.view-save", null, Locale.getDefault()));
        this.finishedButton.getStyleClass().addAll("btn", "btn-primary");
        this.finishedButton.setVisible(false);
        this.finishedButton.setOnAction(event -> this.application.getHostServices().showDocument(this.eu4Service.getState().getLink()));

        this.progressVBox = new VBox(10);
        this.progressVBox.setAlignment(Pos.CENTER);
        this.progressVBox.getChildren().addAll(this.progressBar, this.progressText, this.errorText, this.finishedButton);
        this.progressVBox.setVisible(false);

        progressRow.addColumn(new BootstrapColumn(this.progressVBox, new int[] {12, 10, 8, 6, 4}));

        this.root.addRow(titleRow);
        this.root.addRow(idRow);
        this.root.addRow(localSavesRow);
        this.root.addRow(serverSavesRow);
        this.root.addRow(actionRow);
        this.root.addRow(progressRow);
    }

    public GridPane getScene() {
        return this.root;
    }
}
