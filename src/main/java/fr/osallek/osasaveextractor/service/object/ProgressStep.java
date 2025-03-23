package fr.osallek.osasaveextractor.service.object;

public enum ProgressStep {
    NONE(0),
    PARSING_GAME(1),
    PARSING_SAVE(33),
    PARSING_SAVE_INFO(33),
    PARSING_SAVE_PROVINCES(35),
    PARSING_SAVE_COUNTRIES(50),
    PARSING_SAVE_WARS(63),
    GENERATING_DATA(66),
    GENERATING_DATA_PROVINCES(68),
    GENERATING_DATA_COUNTRIES(75),
    SENDING_DATA(90),
    SENDING_SAVE(95),
    FINISHED(100);

    public final int progress;

    ProgressStep(int progress) {
        this.progress = progress;
    }

    public ProgressStep next() {
        if (ordinal() >= ProgressStep.values().length - 1) {
            return ProgressStep.values()[0];
        } else {
            return ProgressStep.values()[ordinal() + 1];
        }
    }

    public int getProgress() {
        return progress;
    }
}
