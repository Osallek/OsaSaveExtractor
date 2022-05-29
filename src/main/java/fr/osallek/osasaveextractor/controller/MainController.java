package fr.osallek.osasaveextractor.controller;

import fr.osallek.osasaveextractor.OsaSaveExtractorApplication;
import fr.osallek.osasaveextractor.controller.object.ParseDTO;
import fr.osallek.osasaveextractor.service.Eu4Service;
import fr.osallek.osasaveextractor.service.ServerService;
import java.io.IOException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class MainController {

    private final Eu4Service eu4Service;

    private final ServerService serverService;

    private final MessageSource messageSource;

    private static final String DEFAULT = "default";

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
        model.addAttribute("parse", new ParseDTO(DEFAULT, DEFAULT));

        return "index";
    }

    @PostMapping(value = "/parse")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void parse(@ModelAttribute ParseDTO parse) {
        this.eu4Service.parseSave(parse.save(), parse.previousSave());
    }

    @GetMapping("/progress")
    public String getProgress(Model model) {
        if (this.eu4Service.getState().getProgress() >= 100) {
            model.addAttribute("link", this.eu4Service.getState().getLink());
            model.addAttribute("parse", new ParseDTO(DEFAULT, DEFAULT));

            return "finished :: finished";
        } else {
            model.addAttribute("progress", this.eu4Service.getState().getProgress());
            model.addAttribute("progressError", this.eu4Service.getState().isError());
            model.addAttribute("progressLabel", this.messageSource.getMessage(this.eu4Service.getState().getStep().name(), null,
                                                                              this.eu4Service.getLauncherSettings().getGameLanguage().locale));
            model.addAttribute("progressSubLabel", this.eu4Service.getState().getSubStep() == null ? null :
                                                   this.messageSource.getMessage(this.eu4Service.getState().getSubStep().name(), null,
                                                                                 this.eu4Service.getLauncherSettings().getGameLanguage().locale));

            return "progress :: progress";
        }
    }
}
