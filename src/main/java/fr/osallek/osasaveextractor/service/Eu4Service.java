package fr.osallek.osasaveextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.osallek.clausewitzparser.model.ClausewitzItem;
import fr.osallek.eu4parser.Eu4Parser;
import fr.osallek.eu4parser.common.Eu4Utils;
import fr.osallek.eu4parser.common.ImageReader;
import fr.osallek.eu4parser.model.LauncherSettings;
import fr.osallek.eu4parser.model.game.Building;
import fr.osallek.eu4parser.model.game.Estate;
import fr.osallek.eu4parser.model.game.Game;
import fr.osallek.eu4parser.model.game.IdeaGroup;
import fr.osallek.eu4parser.model.game.Province;
import fr.osallek.eu4parser.model.game.Religion;
import fr.osallek.eu4parser.model.game.TradeGood;
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
import org.apache.commons.lang3.math.NumberUtils;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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
                      .filter(path -> NumberUtils.isParsable(path.getFileName().toString()))
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
                return stream.filter(path -> path.getFileName().toString().endsWith(".eu4"))
                             .filter(Eu4Parser::isValid)
                             .sorted(Comparator.comparing(t -> t.toFile().lastModified(), Comparator.reverseOrder()))
                             .toList();
            }
        }

        return new ArrayList<>();
    }

    public CompletableFuture<Void> parseSave(Path toAnalyse, String name, String previousSave, String userId, Consumer<String> error) {
        this.state = new ProgressState(ProgressStep.NONE, this.messageSource, Constants.LOCALE);
        Path tmpFolder = Path.of(FileUtils.getTempDirectoryPath(), UUID.randomUUID().toString());

        return this.executor.submitListenable(() -> {
            try {
                this.state.setStep(ProgressStep.PARSING_GAME);
                AtomicInteger count = new AtomicInteger(0);
                Path savePath = this.launcherSettings.getSavesFolder().resolve(toAnalyse);
                this.state.setStep(ProgressStep.PARSING_GAME);

                Map<Integer, String> tokens = null;
                if (Eu4Parser.isIronman(toAnalyse)) {
                    tokens = this.serverService.getTokens();
                }

                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).position(0).getFloat();

                Game game = Eu4Parser.parseGame(Eu4Parser.detectInstallationFolder().get(), Eu4Parser.getMods(savePath, tokens), this.launcherSettings,
                                                () -> {
                                                    count.incrementAndGet();
                                                    int progress = ProgressStep.PARSING_GAME.progress;
                                                    progress += (ProgressStep.PARSING_GAME.next().progress - ProgressStep.PARSING_GAME.progress) * count.get()
                                                                / (Game.NB_PARTS + 1);

                                                    this.state.setProgress(progress);
                                                });

                this.state.setStep(ProgressStep.PARSING_SAVE);
                this.state.setSubStep(ProgressStep.PARSING_SAVE_INFO);
                Save save = Eu4Parser.loadSave(savePath, game, tokens, Map.of(item -> ClausewitzItem.DEFAULT_NAME.equals(item.getParent().getName()), s -> {
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

                if (!provinceFile.toFile().exists()) {
                    throw new RuntimeException("Could not get hash of provinces image");
                }

                BufferedImage provinceImage = ImageIO.read(provinceFile.toFile());
                BufferedImage provinceMapImage = new BufferedImage(provinceImage.getWidth(), provinceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D provinceMapGraphics = provinceMapImage.createGraphics();

                for (int y = 0; y < provinceImage.getHeight(); y++) {
                    for (int x = 0; x < provinceImage.getWidth(); x++) {
                        Province province = game.getProvincesByColor().get(provinceImage.getRGB(x, y));

                        if (province.isImpassable()) {
                            provinceMapGraphics.setColor(new Color(200, 255, 255));
                        } else if (province.isOcean() || province.isLake()) {
                            provinceMapGraphics.setColor(new Color(227, 255, 255));
                        } else {
                            provinceMapGraphics.setColor(new Color(province.getId()));
                        }

                        if (!province.isOcean() && !province.isLake()) {
                            if (x < provinceImage.getWidth() - 1) {
                                Province p2 = game.getProvincesByColor().get(provinceImage.getRGB(x + 1, y));

                                if ((!province.equals(p2) && province.getId() < p2.getId()) || p2.isOcean() || p2.isLake()) {
                                    provinceMapGraphics.setColor(Color.BLACK);
                                }
                            }

                            if (x > 0) {
                                Province p2 = game.getProvincesByColor().get(provinceImage.getRGB(x - 1, y));

                                if ((!province.equals(p2) && province.getId() < p2.getId()) || p2.isOcean() || p2.isLake()) {
                                    provinceMapGraphics.setColor(Color.BLACK);
                                }
                            }

                            if (y < provinceImage.getHeight() - 1) {
                                Province p2 = game.getProvincesByColor().get(provinceImage.getRGB(x, y + 1));

                                if ((!province.equals(p2) && province.getId() < p2.getId()) || p2.isOcean() || p2.isLake()) {
                                    provinceMapGraphics.setColor(Color.BLACK);
                                }
                            }

                            if (y > 0) {
                                Province p2 = game.getProvincesByColor().get(provinceImage.getRGB(x, y - 1));

                                if ((!province.equals(p2) && province.getId() < p2.getId()) || p2.isOcean() || p2.isLake()) {
                                    provinceMapGraphics.setColor(Color.BLACK);
                                }
                            }
                        }

                        provinceMapGraphics.drawRect(x, y, 1, 1);
                    }
                }

                Path provinceMapFile = tmpFolder.resolve("provinces").resolve("provinces.png");
                FileUtils.forceMkdirParent(provinceMapFile.toFile());
                ImageIO.write(provinceMapImage, "PNG", provinceMapFile.toFile());

                Optional<String> provinceChecksum = Constants.getFileChecksum(provinceMapFile);
                if (provinceChecksum.isPresent()) {
                    File source = provinceMapFile.toFile();
                    provinceMapFile = provinceMapFile.resolveSibling(provinceChecksum.get() + ".png");
                    FileUtils.moveFile(source, provinceMapFile.toFile());
                } else {
                    throw new RuntimeException("Could not get hash of provinces image");
                }

                Path goodsTmpFolder = tmpFolder.resolve("goods");
                FileUtils.forceMkdir(goodsTmpFolder.toFile());

                BufferedImage tradeGoodsImage = ImageReader.convertFileToImage(save.getGame().getResourcesImage());
                IntStream.rangeClosed(0, tradeGoodsImage.getWidth() / 64).parallel().forEach(j -> {
                    try {
                        List<TradeGood> tradeGoods = save.getGame().getTradeGoods().stream().filter(good -> good.getIndex() == j).toList();

                        if (CollectionUtils.isNotEmpty(tradeGoods)) {
                            BufferedImage tradeGoodImageImage = tradeGoodsImage.getSubimage(j * 64, 0, 64, 64);
                            Path dest = goodsTmpFolder.resolve(tradeGoods.get(0).getName() + ".png");
                            ImageIO.write(tradeGoodImageImage, "png", dest.toFile());
                            Eu4Utils.optimizePng(dest, dest);
                            tradeGoods.get(0).setWritenTo(dest);

                            Optional<String> goodChecksum = Constants.getFileChecksum(tradeGoods.get(0).getWritenTo());
                            if (goodChecksum.isPresent()) {
                                tradeGoods.forEach(tradeGood -> tradeGood.setWritenTo(dest.resolveSibling(goodChecksum.get() + ".png")));
                                FileUtils.moveFile(dest.toFile(), tradeGoods.get(0).getWritenTo().toFile());
                            } else {
                                LOGGER.warn("Could not get hash for trade good {}", tradeGoods.get(0).getName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                });

                Path religionsTmpFolder = tmpFolder.resolve("religions");
                FileUtils.forceMkdir(religionsTmpFolder.toFile());
                Map<String, Religion> religionsMap = new HashMap<>();

                BufferedImage religionsImage = ImageReader.convertFileToImage(save.getGame().getReligionsImage());
                IntStream.rangeClosed(0, religionsImage.getWidth() / 64).parallel().forEach(j -> {
                    try {
                        List<Religion> religions = save.getGame()
                                                       .getReligions()
                                                       .stream()
                                                       .filter(r -> r.getIcon() != null)
                                                       .filter(r -> j == (r.getIcon() - 1))
                                                       .toList();

                        if (CollectionUtils.isNotEmpty(religions)) {
                            religions.forEach(religion -> religionsMap.put(religion.getName(), religion));
                            BufferedImage religionImage = religionsImage.getSubimage(j * 64, 0, 64, 64);
                            Path dest = religionsTmpFolder.resolve(religions.get(0).getName() + ".png");
                            ImageIO.write(religionImage, "png", dest.toFile());
                            Eu4Utils.optimizePng(dest, dest);
                            religions.get(0).setWritenTo(dest);

                            Optional<String> religionChecksum = Constants.getFileChecksum(religions.get(0).getWritenTo());
                            if (religionChecksum.isPresent()) {
                                religions.forEach(religion -> religion.setWritenTo(dest.resolveSibling(religionChecksum.get() + ".png")));
                                FileUtils.moveFile(dest.toFile(), religions.get(0).getWritenTo().toFile());
                            } else {
                                LOGGER.warn("Could not get hash for religion {}", religions.get(0).getName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                });

                Path estatesTmpFolder = tmpFolder.resolve("estates");
                FileUtils.forceMkdir(estatesTmpFolder.toFile());

                BufferedImage estatesImage = ImageReader.convertFileToImage(save.getGame().getEstatesImage());
                IntStream.rangeClosed(0, estatesImage.getWidth() / 47).parallel().forEach(j -> {
                    try {
                        List<Estate> estates = save.getGame()
                                                   .getEstates()
                                                   .stream()
                                                   .filter(e -> e.getIcon() != null)
                                                   .filter(e -> j == (e.getIcon() - 1))
                                                   .toList();

                        if (CollectionUtils.isNotEmpty(estates)) {
                            BufferedImage estateImage = estatesImage.getSubimage(j * 47, 0, 47, 44);
                            Path dest = estatesTmpFolder.resolve(estates.get(0).getName() + ".png");
                            ImageIO.write(estateImage, "png", dest.toFile());
                            Eu4Utils.optimizePng(dest, dest);
                            estates.get(0).setWritenTo(dest);

                            Optional<String> estateChecksum = Constants.getFileChecksum(estates.get(0).getWritenTo());
                            if (estateChecksum.isPresent()) {
                                estates.forEach(estate -> estate.setWritenTo(dest.resolveSibling(estateChecksum.get() + ".png")));
                                FileUtils.moveFile(dest.toFile(), estates.get(0).getWritenTo().toFile());
                            } else {
                                LOGGER.warn("Could not get hash for estate {}", estates.get(0).getName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
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
                    .filter(country -> country.getHistory().hasEvents())
                    .parallel()
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

                SaveDTO saveDTO = new SaveDTO(userId, name, previousSave, save, provinceChecksum.get(), religionsMap,
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

                Path finalProvinceMapFile = provinceMapFile;
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
                                                     return sendMissingAssets(response.assetsDTO(), tmpFolder, save, finalProvinceMapFile, religionsMap,
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
                                         .thenCompose(response -> {
                                             try {
                                                 this.state.setStep(ProgressStep.SENDING_SAVE);
                                                 this.serverService.uploadSave(toAnalyse, tmpFolder, response.id(), userId);

                                                 return CompletableFuture.completedFuture(response);
                                             } catch (IOException e) {
                                                 return CompletableFuture.failedStage(e);
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

    private CompletableFuture<Boolean> sendMissingAssets(AssetsDTO assets, Path tmpFolder, Save save, Path provinceMapFile,
                                                         Map<String, Religion> religions, String id, String userId) throws IOException {
        Queue<Path> toSend = new ConcurrentLinkedQueue<>();

        if (assets.provinces()) {
            toSend.add(provinceMapFile);
        }

        if (CollectionUtils.isNotEmpty(assets.countries())) {
            Path cPath = tmpFolder.resolve("flags");
            save.getCountries().values().parallelStream().filter(country -> assets.countries().contains(country.getTag())).distinct().forEach(country -> {
                if (country.useCustomFlagImage()) {
                    if (country.getWritenTo() != null) {
                        toSend.add(country.getWritenTo());
                    }
                } else {
                    File file = country.getFlagFile();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
                    });
                }
            });
        }

        if (CollectionUtils.isNotEmpty(assets.advisors())) {
            Path cPath = tmpFolder.resolve("advisors");
            save.getGame().getAdvisors().parallelStream().filter(advisor -> assets.advisors().contains(advisor.getName())).distinct().forEach(advisor -> {
                File file = advisor.getDefaultImage();

                Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                    if (image != null) {
                        toSend.add(cPath.resolve(image));
                    }
                });
            });
        }

        if (CollectionUtils.isNotEmpty(assets.institutions())) {
            Path cPath = tmpFolder.resolve("institutions");
            save.getGame()
                .getInstitutions()
                .parallelStream()
                .filter(institution -> assets.institutions().contains(institution.getName()))
                .distinct()
                .forEach(institution -> {
                    File file = institution.getImage();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.buildings())) {
            Path cPath = tmpFolder.resolve("buildings");
            List<Building> buildings = save.getGame()
                                           .getBuildings()
                                           .stream()
                                           .filter(building -> assets.buildings().contains(building.getName()))
                                           .distinct()
                                           .toList();

            CountDownLatch countDownLatch = new CountDownLatch(buildings.size());
            ExecutorService poolExecutor = Executors.newFixedThreadPool(8);
            for (Building building : buildings) {
                poolExecutor.submit(() -> {
                    try {
                        File file = building.getImage();

                        Constants.getFileChecksum(file).ifPresent(checksum -> {
                            Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                            if (image != null) {
                                toSend.add(cPath.resolve(image));
                            }
                        });
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.error("An error occurred while waiting for building images: {}", e.getMessage(), e);
            } finally {
                poolExecutor.shutdownNow();
            }
        }

        if (CollectionUtils.isNotEmpty(assets.religions())) {
            religions.values()
                     .parallelStream()
                     .filter(religion -> assets.religions().contains(religion.getName()))
                     .distinct()
                     .map(Religion::getWritenTo)
                     .filter(Objects::nonNull)
                     .forEach(toSend::add);
        }

        if (CollectionUtils.isNotEmpty(assets.tradeGoods())) {
            save.getGame()
                .getTradeGoods()
                .parallelStream()
                .filter(good -> assets.tradeGoods().contains(good.getName()))
                .distinct()
                .map(TradeGood::getWritenTo)
                .filter(Objects::nonNull)
                .forEach(toSend::add);
        }

        if (CollectionUtils.isNotEmpty(assets.estates())) {
            save.getGame()
                .getEstates()
                .parallelStream()
                .filter(estate -> assets.estates().contains(estate.getName()))
                .distinct()
                .map(Estate::getWritenTo)
                .filter(Objects::nonNull)
                .forEach(toSend::add);
        }

        if (CollectionUtils.isNotEmpty(assets.privileges())) {
            Path cPath = tmpFolder.resolve("privileges");
            save.getGame()
                .getEstatePrivileges()
                .parallelStream()
                .filter(privilege -> assets.privileges().contains(privilege.getName()))
                .distinct()
                .forEach(privilege -> {
                    File file = privilege.getImage();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.ideaGroups())) {
            Path cPath = tmpFolder.resolve("idea_groups");
            save.getGame().getIdeaGroups().parallelStream().filter(group -> assets.ideaGroups().contains(group.getName())).distinct().forEach(group -> {
                File file = group.getImage();

                Constants.getFileChecksum(file).ifPresent(checksum -> {
                    Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                    if (image != null) {
                        toSend.add(cPath.resolve(image));
                    }
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

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
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

                    if (image != null) {
                        toSend.add(cPath.resolve(image));
                    }
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

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
                    });
                });
        }

        if (CollectionUtils.isNotEmpty(assets.missions())) {
            Path cPath = tmpFolder.resolve("missions");
            save.getGame()
                .getMissions()
                .parallelStream()
                .filter(mission -> assets.missions().contains(mission.getName()))
                .distinct()
                .forEach(mission -> {
                    File file = mission.getIconFile();

                    Constants.getFileChecksum(file).ifPresent(checksum -> {
                        Path image = Game.convertImage(cPath, Path.of(""), checksum, file.toPath());

                        if (image != null) {
                            toSend.add(cPath.resolve(image));
                        }
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
