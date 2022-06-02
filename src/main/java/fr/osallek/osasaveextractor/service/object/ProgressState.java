package fr.osallek.osasaveextractor.service.object;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class ProgressState {

    private final MessageSource messageSource;

    private final Locale locale;

    private final ObjectProperty<ProgressStep> step;

    private final ObjectProperty<ProgressStep> subStep;

    private final IntegerProperty progress;

    private final StringProperty link;

    private final BooleanProperty error;

    private final StringProperty label;

    public ProgressState(ProgressStep step, MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.step = new SimpleObjectProperty<>(step);
        this.progress = new SimpleIntegerProperty(this.step.get().progress);
        this.subStep = new SimpleObjectProperty<>(null);
        this.link = new SimpleStringProperty(null);
        this.error = new SimpleBooleanProperty(false);
        this.label = new SimpleStringProperty();

        this.step.addListener((observable, oldValue, newValue) -> {
            this.progress.set(newValue.progress);
            computeLabel();
        });
        this.subStep.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.progress.set(newValue.progress);
            }
            computeLabel();
        });
    }

    private void computeLabel() {
        String s = this.messageSource.getMessage("ose.progress." + this.step.get(), null, this.locale);

        if (this.subStep.get() != null) {
            s += " (" + this.messageSource.getMessage("ose.progress." + this.subStep.get(), null, this.locale) + ")";
        }

        this.label.set(s);
    }

    public ProgressStep getStep() {
        return step.get();
    }

    public ObjectProperty<ProgressStep> stepProperty() {
        return step;
    }

    public void setStep(ProgressStep step) {
        this.step.set(step);
    }

    public ProgressStep getSubStep() {
        return subStep.get();
    }

    public ObjectProperty<ProgressStep> subStepProperty() {
        return subStep;
    }

    public void setSubStep(ProgressStep subStep) {
        this.subStep.set(subStep);
    }

    public int getProgress() {
        return progress.get();
    }

    public IntegerProperty progressProperty() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress.set(progress);
    }

    public String getLink() {
        return link.get();
    }

    public StringProperty linkProperty() {
        return link;
    }

    public void setLink(String link) {
        this.link.set(link);
    }

    public boolean isError() {
        return error.get();
    }

    public BooleanProperty errorProperty() {
        return error;
    }

    public void setError(boolean error) {
        this.error.set(error);
    }

    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setLabel(String label) {
        this.label.set(label);
    }
}
