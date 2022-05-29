package fr.osallek.osasaveextractor.service.object;

public class ProgressState {

    private ProgressStep step;

    private ProgressStep subStep;

    private int progress;

    private String link;

    private boolean error;

    public ProgressState() {
    }

    public ProgressState(ProgressStep step) {
        this.step = step;
        this.progress = this.step.progress;
    }

    public ProgressStep getStep() {
        return step;
    }

    public void setStep(ProgressStep step) {
        this.step = step;
        setProgress(this.step.progress);
    }

    public ProgressStep getSubStep() {
        return subStep;
    }

    public void setSubStep(ProgressStep subStep) {
        this.subStep = subStep;

        if (this.subStep != null) {
            setProgress(this.subStep.progress);
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
