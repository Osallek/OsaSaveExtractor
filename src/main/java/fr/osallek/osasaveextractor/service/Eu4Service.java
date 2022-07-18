package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.eu4parser.model.game.Game;
import fr.osallek.eu4parser.model.game.Province;
import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.save.Save;
import fr.osallek.eu4parser.model.save.country.SaveCountry;
import fr.osallek.osasaveextractor.common.Constants;
import fr.osallek.osasaveextractor.service.object.ProgressState;
import fr.osallek.osasaveextractor.service.object.ProgressStep;
import fr.osallek.osasaveextractor.service.object.save.SaveDTO;
import fr.osallek.osasaveextractor.service.object.server.AssetsDTO;
import fr.osallek.osasaveextractor.service.object.server.ServerSave;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class Eu4Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eu4Service.class);
    private final LauncherSettings launcherSettings;

    private final MessageSource messageSource;

    private final ThreadPoolTaskExecutor executor;

    private final ServerService serverService;

    private final ObjectMapper objectMapper;

    private ProgressState state;

    public Eu4Service(MessageSource messageSource, ThreadPoolTaskExecutor executor, ServerService serverService, ObjectMapper objectMapper) throws IOException {
        this.messageSource = messageSource;
        this.executor = executor;
        this.serverService = serverService;
        this.objectMapper = objectMapper;

        Optional<Path> installationFolder = Eu4Parser.detectInstallationFolder();

        if (installationFolder.isEmpty()) {
            throw new RuntimeException(); //Todo modal to ask ?
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

    public CompletableFuture<Void> parseSave(Path toAnalyse, String previousSave) {
        this.state = new ProgressState(ProgressStep.NONE, this.messageSource, Locale.getDefault());

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

                Path tmpFolder = Path.of(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString());
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

                Path flagsFolder = tmpFolder.resolve("flags");
                FileUtils.forceMkdir(flagsFolder.toFile());
                save.getCountries().values().stream().filter(SaveCountry::isAlive).forEach(country -> {
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

                SaveDTO saveDTO = new SaveDTO(previousSave, save, provinceChecksum.get(), colorsChecksum.get(), religions,
                                              value -> {
                                                  this.state.setSubStep(ProgressStep.GENERATING_DATA_COUNTRIES);
                                                  int progress = ProgressStep.GENERATING_DATA_COUNTRIES.progress;
                                                  progress += (ProgressStep.GENERATING_DATA_COUNTRIES.next().progress
                                                               - ProgressStep.GENERATING_DATA_COUNTRIES.progress) * value;

                                                  this.state.setProgress(progress);
                                              });
                Path dataFile = tmpFolder.resolve("save.json"); //Todo remove when uploading
                this.objectMapper.writeValue(dataFile.toFile(), saveDTO);

                this.state.setStep(ProgressStep.SENDING_DATA);
                this.state.setSubStep(null);

                Path finalColorsFile = colorsFile;
                return this.serverService.uploadData(saveDTO)
                                         .whenComplete((s, throwable) -> {
                                             if (throwable != null) {
                                                 this.state.setError(true);
                                                 LOGGER.error(throwable.getMessage(), throwable);

                                                 FileUtils.deleteQuietly(tmpFolder.toFile());
                                             }
                                         })
                                         .thenCompose(response -> {
                                             if (response.assetsDTO() == null) {
                                                 return CompletableFuture.completedFuture(response);
                                             } else {
                                                 try {
                                                     return sendMissingAssets(response.assetsDTO(), tmpFolder, save, finalColorsFile, provinceFile, religions)
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
                                                 LOGGER.error(throwable.getMessage(), throwable);

                                                 FileUtils.deleteQuietly(tmpFolder.toFile());
                                             }
                                         })
                                         .thenAccept(response -> {
                                             this.state.setStep(ProgressStep.FINISHED);
                                             this.state.setLink(response.link());

                                             //FileUtils.deleteQuietly(tmpFolder.toFile()); //Todo
                                         });
            } catch (Exception e) {
                this.state.setError(true);
                LOGGER.error("{}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).completable().thenCompose(unused -> unused);
    }

    private CompletableFuture<Boolean> sendMissingAssets(AssetsDTO assets, Path tmpFolder, Save save, Path colorsFile, Path provinceFile,
                                                         Map<String, Religion> religions) throws IOException {
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
            save.getCountries().values().stream().filter(country -> assets.countries().contains(country.getTag())).forEach(country -> {
                if (country.useCustomFlagImage()) {
                    if (country.getWritenTo() != null) {
                        toSend.add(country.getWritenTo());
                    }
                } else {
                    File file = country.getFlagFile();

                    Constants.getFileChecksum(file)
                             .ifPresentOrElse(checksum -> {
                                                  Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                                                  toSend.add(cPath.resolve(image));
                                              },
                                              () -> LOGGER.error("Could not get hash of country {}", country.getTag()));
                }
            });
        }

        if (CollectionUtils.isNotEmpty(assets.advisors())) {
            Path cPath = tmpFolder.resolve("advisors");
            save.getGame().getAdvisors().stream().filter(advisor -> assets.advisors().contains(advisor.getName())).forEach(advisor -> {
                File file = advisor.getDefaultImage();

                Constants.getFileChecksum(file)
                         .ifPresentOrElse(checksum -> {
                                              Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                                              toSend.add(cPath.resolve(image));
                                          },
                                          () -> LOGGER.error("Could not get hash of advisor {}", advisor.getName()));
            });
        }

        if (CollectionUtils.isNotEmpty(assets.institutions())) {
            Path cPath = tmpFolder.resolve("institutions");
            save.getGame().getInstitutions().stream().filter(institution -> assets.institutions().contains(institution.getName())).forEach(institution -> {
                File file = institution.getImage();

                Constants.getFileChecksum(file)
                         .ifPresentOrElse(checksum -> {
                                              Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                                              toSend.add(cPath.resolve(image));
                                          },
                                          () -> LOGGER.error("Could not get hash of institution {}", institution.getName()));
            });
        }

        if (CollectionUtils.isNotEmpty(assets.buildings())) {
            Path cPath = tmpFolder.resolve("buildings");
            save.getGame().getBuildings().stream().filter(building -> assets.buildings().contains(building.getName())).forEach(building -> {
                File file = building.getImage();

                Constants.getFileChecksum(file)
                         .ifPresentOrElse(checksum -> {
                                              Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());
                                              toSend.add(cPath.resolve(image));
                                          },
                                          () -> LOGGER.error("Could not get hash of building {}", building.getName()));
            });
        }

        if (CollectionUtils.isNotEmpty(assets.religions())) {
            religions.values()
                     .stream()
                     .filter(religion -> assets.religions().contains(religion.getName()))
                     .forEach(religion -> toSend.add(religion.getWritenTo()));
        }

        if (CollectionUtils.isNotEmpty(assets.tradeGoods())) {
            save.getGame()
                .getTradeGoods()
                .stream()
                .filter(good -> assets.tradeGoods().contains(good.getName()))
                .forEach(good -> toSend.add(good.getWritenTo()));
        }

        return this.serverService.uploadAssets(toSend, tmpFolder);
    }

    public LauncherSettings getLauncherSettings() {
        return launcherSettings;
    }

    public ProgressState getState() {
        return state;
    }
}
