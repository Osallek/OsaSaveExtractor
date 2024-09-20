package fr.osallek.osasaveextractor;

import javafx.application.Application;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class OsaSaveExtractorListener implements ApplicationContextAware {

    private final ApplicationArguments args;

    private final ThreadPoolTaskExecutor executor;

    static ConfigurableApplicationContext applicationContext;

    public OsaSaveExtractorListener(ApplicationArguments args, ThreadPoolTaskExecutor executor) {
        this.args = args;
        this.executor = executor;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Application.launch(OsaSaveExtractorUiApplication.class, this.args.getSourceArgs());
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        this.executor.shutdown();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        OsaSaveExtractorListener.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
