package fr.osallek.osasaveextractor.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.osasaveextractor.controller.object.DataAssetDTO;
import fr.osallek.osasaveextractor.controller.object.ErrorObject;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import fr.osallek.osasaveextractor.service.object.server.UploadResponseDTO;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
@RegisterReflectionForBinding({ServerSave.class, UploadResponseDTO.class, ErrorObject.class, DataAssetDTO.class, LauncherSettings.class})
public class OsaSaveExtractorConfig {

    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.addBasenames("messages/ose");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultLocale(Locale.US);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());

        return messageSource;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                         .findAndAddModules()
                         .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                         .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                         .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                         .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                         .build()
                         .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }
}
