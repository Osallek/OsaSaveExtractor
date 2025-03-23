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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProgressState {

    private final MessageSource messageSource;

    private final Locale locale;

    private final ObjectProperty<ProgressStep> step;

    private final ObjectProperty<ProgressStep> subStep;

    private final IntegerProperty progress;

    private final StringProperty link;

    private final BooleanProperty error;

    private final StringProperty label;

    private final IntegerProperty[] args;

    private final ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);

    public ProgressState(ProgressStep step, MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.step = new SimpleObjectProperty<>(step);
        this.progress = new SimpleIntegerProperty(this.step.get().progress);
        this.subStep = new SimpleObjectProperty<>(null);
        this.link = new SimpleStringProperty(null);
        this.error = new SimpleBooleanProperty(false);
        this.label = new SimpleStringProperty();
        this.args = new IntegerProperty[] {new SimpleIntegerProperty(0), new SimpleIntegerProperty(1)};
        this.scheduledExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        this.step.addListener((observable, oldValue, newValue) -> {
            this.progress.set(newValue.progress);
            computeLabel();

            if (ProgressStep.FINISHED == newValue) {
                this.scheduledExecutor.close();
            }
        });
        this.subStep.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.progress.set(newValue.progress);
            }
            computeLabel();
        });

        this.scheduledExecutor.scheduleAtFixedRate(this::computeLabel, 75, 75, TimeUnit.MILLISECONDS);
    }

    private synchronized void computeLabel() {
        String s = this.messageSource.getMessage("ose.progress." + this.step.get(), new Object[] {this.args[0].get(), this.args[1].get()}, this.locale);

        if (this.subStep.get() != null) {
            s += " (" + this.messageSource.getMessage("ose.progress." + this.subStep.get(), new Object[] {this.args[0].get(), this.args[1].get()}, this.locale) + ")";
        }

        this.label.set(s);
    }

    public void setStep(ProgressStep step) {
        if (!Objects.equals(this.step.get(), step)) {
            this.step.set(step);
            this.args[0].set(0);
            this.args[1].set(0);
        }
    }

    public void setSubStep(ProgressStep subStep) {
        if (!Objects.equals(this.subStep.get(), subStep)) {
            this.subStep.set(subStep);
            this.args[0].set(0);
            this.args[1].set(0);
        }
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

    public void setLink(String link) {
        this.link.set(link);
    }

    public boolean isError() {
        return error.get();
    }

    public void setError(boolean error) {
        this.error.set(error);
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setArg0(Integer args) {
        this.args[0].set(args);
    }

    public void setArg1(Integer args) {
        this.args[1].set(args);
    }
}
