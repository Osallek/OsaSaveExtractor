package fr.osallek.osasaveextractor.controller.object;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Inspired from <a href="https://gist.github.com/floralvikings/10290131">...</a>
 */
public class AutoCompleteTextField<T> extends TextField {

    private final Map<String, T> entries;

    private final ContextMenu entriesPopup;

    private T selected;

    public AutoCompleteTextField(Map<String, T> entries) {
        super();
        this.entries = entries;
        this.entriesPopup = new ContextMenu();

        List<CustomMenuItem> menuItems = this.entries.keySet().stream().map(s -> {
            Label entryLabel = new Label(s);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);

            item.setOnAction(actionEvent -> {
                setText(s);
                this.entriesPopup.hide();
                this.selected = this.entries.get(s);
            });

            return item;
        }).toList();

        this.entriesPopup.getItems().addAll(menuItems);

        setOnMouseClicked(event -> {
            if (!this.entriesPopup.isShowing()) {
                this.entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
            }
        });

        textProperty().addListener((observable, oldValue, newValue) -> {
            this.selected = this.entries.get(newValue);
        });
    }

    public Set<String> getEntries() {
        return this.entries.keySet();
    }

    public T getSelected() {
        return this.selected;
    };
}
