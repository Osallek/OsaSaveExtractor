package fr.osallek.osasaveextractor.controller;

import fr.osallek.osasaveextractor.OsaSaveExtractorApplication;
import fr.osallek.osasaveextractor.service.Eu4Service;
import fr.osallek.osasaveextractor.service.ServerService;
import fr.osallek.osasaveextractor.service.object.ProgressStep;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private static final Random RANDOM = new Random();

    private final Eu4Service eu4Service;

    private final ServerService serverService;

    private final MessageSource messageSource;

    private ProgressStep progress = ProgressStep.PARSING_GAME;

    public MainController(Eu4Service eu4Service, ServerService serverService, MessageSource messageSource) {
        this.eu4Service = eu4Service;
        this.serverService = serverService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String getPage(Model model) throws IOException {
        this.eu4Service.prepare();

        model.addAttribute("id", OsaSaveExtractorApplication.ID);
        model.addAttribute("localSaves", this.eu4Service.getSaves());
        model.addAttribute("serverSaves", this.serverService.getSaves());
        model.addAttribute("buttonDisabled", true);

        return "index";
    }

    @GetMapping("/progress")
    public String getProgress(Model model) {
        if (this.progress.progress >= 100) {
            model.addAttribute("link", "http://localhost:8080/saves/" + UUID.randomUUID());
            this.progress = ProgressStep.PARSING_GAME;

            return "finished :: finished";
        } else {
            model.addAttribute("progress", this.progress.progress);
            model.addAttribute("progressLabel", this.messageSource.getMessage(this.progress.name(), null,
                                                                              this.eu4Service.getLauncherSettings().getGameLanguage().locale));

            this.progress = this.progress.next();

            return "progress :: progress";
        }
    }
}
