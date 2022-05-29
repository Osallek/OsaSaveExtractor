package fr.osallek.osasaveextractor.config;

import fr.osallek.eu4parser.model.LauncherSettings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.nativex.hint.MethodHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;

@Configuration
@TypeHint(types = {LauncherSettings.class, Map.class, LinkedHashMap.class, Map.Entry.class, ArrayList.class, List.class},
          typeNames = {"java.util.LinkedHashMap$Entry", "java.util.HashMap$Node"},
          access = {TypeAccess.DECLARED_FIELDS, TypeAccess.DECLARED_CLASSES, TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS})
@TypeHint(typeNames = {"java.util.LinkedHashMap$Entry"}, methods = {@MethodHint(name = "getValue"), @MethodHint(name = "getKey")})
@TypeHint(types = Map.Entry.class, methods = {@MethodHint(name = "getValue"), @MethodHint(name = "getKey")})
public class CustomHintConfig {
}
