package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.eu4parser.model.game.Game;
import fr.osallek.eu4parser.model.game.IdeaGroup;
import fr.osallek.eu4parser.model.game.Province;
import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import fr.osallek.osasaveextractor.common.Constants;
import fr.osallek.osasaveextractor.common.exception.ServerException;
import fr.osallek.osasaveextractor.controller.object.ErrorCode;
import fr.osallek.osasaveextractor.service.object.ProgressState;
import fr.osallek.osasaveextractor.service.object.ProgressStep;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.server.AssetsDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class Eu4Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eu4Service.class);

    private static final long MAGIC_STEAM_ID = 76561197960265728L;
    private final LauncherSettings launcherSettings;

    private final MessageSource messageSource;

    private final ThreadPoolTaskExecutor executor;

    private final ServerService serverService;

    private final ObjectMapper objectMapper;

    private final LinkedHashMap<String, String> steamIds = new LinkedHashMap<>();

    private ProgressState state;

    public Eu4Service(MessageSource messageSource, ThreadPoolTaskExecutor executor, ServerService serverService,
                      ObjectMapper objectMapper) throws IOException, InterruptedException {
        this.messageSource = messageSource;
        this.executor = executor;
        this.serverService = serverService;
        this.objectMapper = objectMapper;

        Optional<Path> installationFolder = Eu4Parser.detectInstallationFolder();

        if (installationFolder.isEmpty()) {
            throw new RuntimeException();
        }

        Optional<Path> steamFolder = Eu4Parser.detectSteamFolder().map(path -> path.resolve("userdata"));

        if (steamFolder.isEmpty()) {
            throw new RuntimeException();
        }

        if (Files.exists(steamFolder.get()) && Files.isDirectory(steamFolder.get())) {
            try (Stream<Path> stream = Files.list(steamFolder.get())) {
                stream.filter(path -> Files.exists(path.resolve("config").resolve("localconfig.vdf")))
                      .sorted(Comparator.comparing(path -> path.resolve("config").resolve("localconfig.vdf").toFile().lastModified(),
                                                   Comparator.reverseOrder()))
                      .forEach(path -> {
                          try {
                              Files.readAllLines(path.resolve("config").resolve("localconfig.vdf"))
                                   .stream()
                                   .filter(s -> s.contains("PersonaName"))
                                   .findFirst()
                                   .ifPresent(s -> {
                                       String name = StringUtils.trimToNull(StringUtils.remove(StringUtils.remove(s, "\"PersonaName\""), "\""));

                                       if (StringUtils.isNotBlank(name)) {
                                           long id = Long.parseLong(path.getFileName().toString());
                                           id += id < MAGIC_STEAM_ID ? MAGIC_STEAM_ID : 0;

                                           this.steamIds.put(String.valueOf(id), name);
                                       }
                                   });
                          } catch (IOException ignored) {
                          }
                      });

            }
        }

        this.launcherSettings = Eu4Parser.loadSettings(installationFolder.get());
    }

    public List<Path> getSaves() throws IOException {
        if (Files.exists(this.launcherSettings.getSavesFolder()) && Files.isDirectory(this.launcherSettings.getSavesFolder())) {
            try (Stream<Path> stream = Files.walk(this.launcherSettings.getSavesFolder())) {
                //Todo filter ironman
                return stream.filter(path -> path.getFileName().toString().endsWith(".eu4"))
                             .sorted(Comparator.comparing(t -> t.toFile().lastModified(), Comparator.reverseOrder()))
                             .toList();
            }
        }

        return new ArrayList<>();
    }

    public CompletableFuture<Void> parseSave(Path toAnalyse, String previousSave, String userId, Consumer<String> error) {
        this.state = new ProgressState(ProgressStep.NONE, this.messageSource, Locale.getDefault());
        Path tmpFolder = Path.of(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString());

        return this.executor.submitListenable(() -> {
            try {
                this.state.setStep(ProgressStep.PARSING_GAME);
                AtomicInteger count = new AtomicInteger(0);
                Path savePath = this.launcherSettings.getSavesFolder().resolve(toAnalyse);
                this.state.setStep(ProgressStep.PARSING_GAME);
                Game game = Eu4Parser.parseGame(Eu4Parser.detectInstallationFolder().get(), Eu4Parser.getMods(savePath), this.launcherSettings,
                                                () -> {
                                                    count.incrementAndGet();
                                                    int progress = ProgressStep.PARSING_GAME.progress;
                                                    progress += (ProgressStep.PARSING_GAME.next().progress - ProgressStep.PARSING_GAME.progress) * count.get()
                                                                / (Game.NB_PARTS + 1);

                                                    this.state.setProgress(progress);
                                                });

                this.state.setStep(ProgressStep.PARSING_SAVE);
                this.state.setSubStep(ProgressStep.PARSING_SAVE_INFO);
                Save save = Eu4Parser.loadSave(savePath, game, Map.of(item -> ClausewitzItem.DEFAULT_NAME.equals(item.getParent().getName()), s -> {
                    if ("provinces".equals(s)) {
                        this.state.setSubStep(ProgressStep.PARSING_SAVE_PROVINCES);
                    } else if ("countries".equals(s)) {
                        this.state.setSubStep(ProgressStep.PARSING_SAVE_COUNTRIES);
                    } else if ("active_advisors".equals(s)) {
                        this.state.setSubStep(ProgressStep.PARSING_SAVE_WARS);
                    }
                }));

                this.state.setStep(ProgressStep.GENERATING_DATA);
                this.state.setSubStep(null);
                FileUtils.forceMkdir(tmpFolder.toFile());

                Path provinceFile = Path.of(game.getProvincesImage().getAbsolutePath());
                Optional<String> provinceChecksum = Constants.getFileChecksum(provinceFile.toFile());

                if (provinceChecksum.isEmpty()) {
                    throw new RuntimeException("Could not get hash of provinces image");
                }

                Path colorsFile = tmpFolder.resolve("colors").resolve("colors.png");
                FileUtils.forceMkdirParent(colorsFile.toFile());
                BufferedImage colorsImage = new BufferedImage(game.getProvinces().size(), 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D colorsImageGraphics = colorsImage.createGraphics();
                int i = 0;
                for (Province province : game.getProvinces().values()) {
                    colorsImageGraphics.setColor(new Color(province.getColor()));
                    colorsImageGraphics.drawLine(i, 0, i, 0);
                    i++;
                }
                ImageIO.write(colorsImage, "PNG", colorsFile.toFile());

                Optional<String> colorsChecksum = Constants.getFileChecksum(colorsFile);
                if (colorsChecksum.isPresent()) {
                    File source = colorsFile.toFile();
                    colorsFile = colorsFile.resolveSibling(colorsChecksum.get() + ".png");
                    FileUtils.moveFile(source, colorsFile.toFile());
                } else {
                    throw new RuntimeException("Could not get hash of colors image");
                }

                Path goodsTmpFolder = tmpFolder.resolve("goods");
                FileUtils.forceMkdir(goodsTmpFolder.toFile());
                save.getGame().getTradeGoods().forEach(tradeGood -> {
                    try {
                        tradeGood.writeImageTo(goodsTmpFolder.resolve(tradeGood.getName() + ".png"));

                        Optional<String> goodChecksum = Constants.getFileChecksum(tradeGood.getWritenTo());
                        if (goodChecksum.isPresent()) {
                            Path source = tradeGood.getWritenTo();
                            tradeGood.setWritenTo(source.resolveSibling(goodChecksum.get() + ".png"));
                            FileUtils.moveFile(source.toFile(), tradeGood.getWritenTo().toFile());
                        } else {
                            LOGGER.warn("Could not get hash for trade good {}", tradeGood.getName());
                        }
                    } catch (FileExistsException ignored) {
                    } catch (IOException e) {
                        LOGGER.warn("Could not write trade good file for {}: {}", tradeGood.getName(), e.getMessage(), e);
                    }
                });

                Path religionsTmpFolder = tmpFolder.resolve("religions");
                FileUtils.forceMkdir(religionsTmpFolder.toFile());
                Map<String, Religion> religions = new HashMap<>();
                save.getGame().getReligions().stream().filter(religion -> religion.getIcon() != null).forEach(religion -> {
                    try {
                        religions.put(religion.getName(), religion);
                        religion.writeImageTo(religionsTmpFolder.resolve(religion.getName() + ".png"));

                        Optional<String> religionChecksum = Constants.getFileChecksum(religion.getWritenTo());
                        if (religionChecksum.isPresent()) {
                            Path source = religion.getWritenTo();
                            religion.setWritenTo(source.resolveSibling(religionChecksum.get() + ".png"));
                            FileUtils.moveFile(source.toFile(), religion.getWritenTo().toFile());
                        } else {
                            LOGGER.warn("Could not get hash for trade religion {}", religion.getName());
                        }
                    } catch (FileExistsException ignored) {
                    } catch (IOException e) {
                        LOGGER.warn("Could not write trade religion file for {}: {}", religion.getName(), e.getMessage(), e);
                    }
                });

                Path estatesTmpFolder = tmpFolder.resolve("estates");
                FileUtils.forceMkdir(estatesTmpFolder.toFile());
                save.getGame().getEstates().forEach(estate -> {
                    try {
                        estate.writeImageTo(estatesTmpFolder.resolve(estate.getName() + ".png"));

                        Optional<String> estateChecksum = Constants.getFileChecksum(estate.getWritenTo());
                        if (estateChecksum.isPresent()) {
                            Path source = estate.getWritenTo();
                            estate.setWritenTo(source.resolveSibling(estateChecksum.get() + ".png"));
                            FileUtils.moveFile(source.toFile(), estate.getWritenTo().toFile());
                        } else {
                            LOGGER.warn("Could not get hash for estate {}", estate.getName());
                        }
                    } catch (FileExistsException ignored) {
                    } catch (IOException e) {
                        LOGGER.warn("Could not write estate file for {}: {}", estate.getName(), e.getMessage(), e);
                    }
                });

                Path flagsFolder = tmpFolder.resolve("flags");
                FileUtils.forceMkdir(flagsFolder.toFile());
                save.getCountries()
                    .values()
                    .stream()
                    .filter(Predicate.not(SaveCountry::isObserver))
                    .filter(country -> !"REB".equals(country.getTag()))
                    .filter(country -> country.getHistory() != null)
                    .filter(country -> CollectionUtils.isNotEmpty(country.getHistory().getEvents()))
                    .filter(country -> country.getHistory()
                                              .getEvents()
                                              .stream()
                                              .anyMatch(event -> event.getDate().isAfter(country.getSave().getStartDate())))
                    .forEach(country -> {
                        try {
                            BufferedImage image = country.getCustomFlagImage();
                            if (image == null) {
                                return;
                            }

                            country.writeImageTo(flagsFolder.resolve(country.getTag() + ".png"), image);

                            Optional<String> flagChecksum = Constants.getFileChecksum(country.getWritenTo());
                            if (flagChecksum.isPresent()) {
                                Path source = country.getWritenTo();
                                country.setWritenTo(source.resolveSibling(flagChecksum.get() + ".png"));
                                FileUtils.moveFile(source.toFile(), country.getWritenTo().toFile());
                            } else {
                                LOGGER.warn("Could not get hash for country {}", country.getTag());
                            }
                        } catch (FileExistsException ignored) {
                        } catch (IOException e) {
                            LOGGER.warn("Could not write country file for {}: {}", country.getTag(), e.getMessage(), e);
                        }
                    });

                SaveDTO saveDTO = new SaveDTO(userId, previousSave, save, provinceChecksum.get(), colorsChecksum.get(), religions,
                                              value -> {
                                                  this.state.setSubStep(ProgressStep.GENERATING_DATA_COUNTRIES);
                                                  int progress = ProgressStep.GENERATING_DATA_COUNTRIES.progress;
                                                  progress += (ProgressStep.GENERATING_DATA_COUNTRIES.next().progress
                                                               - ProgressStep.GENERATING_DATA_COUNTRIES.progress) * value;

                                                  this.state.setProgress(progress);
                                              });

                this.objectMapper.writeValue(tmpFolder.resolve("save.json").toFile(), saveDTO);
                this.state.setStep(ProgressStep.SENDING_DATA);
                this.state.setSubStep(null);

                Path finalColorsFile = colorsFile;
                return this.serverService.uploadData(saveDTO)
                                         .whenComplete((s, throwable) -> {
                                             if (throwable != null) {
                                                 this.state.setError(true);

                                                 if (ServerException.class.equals(throwable.getClass())) {
                                                     error.accept(((ServerException) throwable).getErrorCode().name());
                                                 } else {
                                                     error.accept(ErrorCode.DEFAULT_ERROR.name());
                                                 }

                                                 LOGGER.error(throwable.getMessage(), throwable);

                                                 FileUtils.deleteQuietly(tmpFolder.toFile());
                                             }
                                         })
                                         .thenCompose(response -> {
                                             if (response.assetsDTO() == null || response.assetsDTO().isEmpty()) {
                                                 return CompletableFuture.completedFuture(response);
                                             } else {
                                                 try {
                                                     return sendMissingAssets(response.assetsDTO(), tmpFolder, save, finalColorsFile, provinceFile, religions,
                                                                              response.id(), userId)
                                                             .thenCompose(aBoolean -> {
                                                                 if (BooleanUtils.toBoolean(aBoolean)) {
                                                                     return CompletableFuture.completedFuture(response);
                                                                 } else {
                                                                     return CompletableFuture.failedStage(
                                                                             new RuntimeException("An error occurred while sending assets to server"));
                                                                 }
                                                             });
                                                 } catch (IOException e) {
                                                     return CompletableFuture.failedStage(e);
                                                 }
                                             }
                                         })
                                         .whenComplete((s, throwable) -> {
                                             if (throwable != null) {
                                                 this.state.setError(true);

                                                 if (ServerException.class.equals(throwable.getClass())) {
                                                     error.accept(((ServerException) throwable).getErrorCode().name());
                                                 } else {
                                                     error.accept(ErrorCode.DEFAULT_ERROR.name());
                                                 }

                                                 LOGGER.error(throwable.getMessage(), throwable);

                                                 FileUtils.deleteQuietly(tmpFolder.toFile());
                                             }
                                         })
                                         .thenAccept(response -> {
                                             this.state.setStep(ProgressStep.FINISHED);
                                             this.state.setLink(response.link());

                                             FileUtils.deleteQuietly(tmpFolder.toFile());
                                         });
            } catch (Exception e) {
                this.state.setError(true);
                LOGGER.error("{}", e.getMessage(), e);
                throw new RuntimeException(e);
            } finally {
                FileUtils.deleteQuietly(tmpFolder.toFile());
            }
        }).completable().thenCompose(unused -> unused);
    }

    private CompletableFuture<Boolean> sendMissingAssets(AssetsDTO assets, Path tmpFolder, Save save, Path colorsFile, Path provinceFile,
                                                         Map<String, Religion> religions, String id, String userId) throws IOException {
        List<Path> toSend = new ArrayList<>();

        if (assets.provinces()) {
            Path provinceMapFile = tmpFolder.resolve("provinces").resolve("provinces.png");
            FileUtils.forceMkdirParent(provinceMapFile.toFile());
            ImageIO.write(ImageIO.read(provinceFile.toFile()), "PNG", provinceMapFile.toFile());

            Optional<String> provinceChecksum = Constants.getFileChecksum(provinceFile);
            if (provinceChecksum.isPresent()) {
                File source = provinceMapFile.toFile();
                provinceMapFile = provinceMapFile.resolveSibling(provinceChecksum.get() + ".png");
                FileUtils.moveFile(source, provinceMapFile.toFile());
                toSend.add(provinceMapFile);
            } else {
                throw new RuntimeException("Could not get hash of provinces image");
            }
        }

        if (assets.colors()) {
            toSend.add(colorsFile);
        }

        if (CollectionUtils.isNotEmpty(assets.countries())) {
            Path cPath = tmpFolder.resolve("flags");
            save.getCountries().values().stream().filter(country -> assets.countries().contains(country.getTag())).distinct().forEach(country -> {
                if (country.useCustomFlagImage()) {
                    if (country.getWritenTo() != null) {
                        toSend.add(country.getWritenTo());
                    }
                } else {
                    File file = country.getFlagFile();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                        toSend.add(cPath.resolve(image));
                    });
                }
            });
        }

        if (CollectionUtils.isNotEmpty(assets.advisors())) {
            Path cPath = tmpFolder.resolve("advisors");
            save.getGame().getAdvisors().stream().filter(advisor -> assets.advisors().contains(advisor.getName())).distinct().forEach(advisor -> {
                File file = advisor.getDefaultImage();

                Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                    toSend.add(cPath.resolve(image));
                });
            });
        }

        if (CollectionUtils.isNotEmpty(assets.institutions())) {
            Path cPath = tmpFolder.resolve("institutions");
            save.getGame()
                .getInstitutions()
                .stream()
                .filter(institution -> assets.institutions().contains(institution.getName()))
                .distinct()
                .forEach(institution -> {
                    File file = institution.getImage();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                        toSend.add(cPath.resolve(image));
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.buildings())) {
            Path cPath = tmpFolder.resolve("buildings");
            save.getGame().getBuildings().stream().filter(building -> assets.buildings().contains(building.getName())).distinct().forEach(building -> {
                File file = building.getImage();

                Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                    toSend.add(cPath.resolve(image));
                });
            });
        }

        if (CollectionUtils.isNotEmpty(assets.religions())) {
            religions.values()
                     .stream()
                     .filter(religion -> assets.religions().contains(religion.getName()))
                     .distinct()
                     .forEach(religion -> toSend.add(religion.getWritenTo()));
        }

        if (CollectionUtils.isNotEmpty(assets.tradeGoods())) {
            save.getGame()
                .getTradeGoods()
                .stream()
                .filter(good -> assets.tradeGoods().contains(good.getName()))
                .distinct()
                .forEach(good -> toSend.add(good.getWritenTo()));
        }

        if (CollectionUtils.isNotEmpty(assets.estates())) {
            save.getGame()
                .getEstates()
                .stream()
                .filter(estate -> assets.estates().contains(estate.getName()))
                .distinct()
                .forEach(estate -> toSend.add(estate.getWritenTo()));
        }

        if (CollectionUtils.isNotEmpty(assets.privileges())) {
            Path cPath = tmpFolder.resolve("privileges");
            save.getGame()
                .getEstatePrivileges()
                .stream()
                .filter(privilege -> assets.privileges().contains(privilege.getName()))
                .distinct()
                .forEach(privilege -> {
                    File file = privilege.getImage();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                        toSend.add(cPath.resolve(image));
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.ideaGroups())) {
            Path cPath = tmpFolder.resolve("idea_groups");
            save.getGame().getIdeaGroups().stream().filter(group -> assets.ideaGroups().contains(group.getName())).distinct().forEach(group -> {
                File file = group.getImage();

                Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                    toSend.add(cPath.resolve(image));
                });
            });
        }

        if (CollectionUtils.isNotEmpty(assets.personalities())) {
            Path cPath = tmpFolder.resolve("modifiers");
            save.getGame()
                .getRulerPersonalities()
                .parallelStream()
                .filter(personality -> assets.personalities().contains(personality.getName()))
                .distinct()
                .forEach(personality -> {
                    File file = personality.getImage();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                        toSend.add(cPath.resolve(image));
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.ideas())) {
            Path cPath = tmpFolder.resolve("modifiers");
            save.getGame()
                .getIdeaGroups()
                .parallelStream()
                .map(IdeaGroup::getIdeas)
                .filter(MapUtils::isNotEmpty)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(entry -> assets.ideas().contains(entry.getKey()))
                .map(entry -> entry.getValue().getImage(save.getGame()))
                .filter(Objects::nonNull)
                .distinct()
                .forEach(file -> Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                    toSend.add(cPath.resolve(image));
                }));
        }

        if (CollectionUtils.isNotEmpty(assets.leaderPersonalities())) {
            Path cPath = tmpFolder.resolve("modifiers");
            save.getGame()
                .getLeaderPersonalities()
                .parallelStream()
                .filter(personality -> assets.leaderPersonalities().contains(personality.getName()))
                .distinct()
                .forEach(personality -> {
                    File file = personality.getModifiers().getImage(save.getGame());

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                        toSend.add(cPath.resolve(image));
                    });
                });
        }

        return this.serverService.uploadAssets(toSend, tmpFolder, id, userId);
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public ProgressState getState() {
        return state;
    }

    public LinkedHashMap<String, String> getSteamIds() {
        return steamIds;
    }
}
