package dev.crypts.simpleVoiceChatMinimumVolume;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    private static final DumperOptions DUMPER_OPTIONS = new DumperOptions();
    private static final LoaderOptions LOADER_OPTIONS = new LoaderOptions();
    private static final Yaml YAML;

    static {
        DUMPER_OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        DUMPER_OPTIONS.setPrettyFlow(true);
        YAML = new Yaml(LOADER_OPTIONS, DUMPER_OPTIONS);
    }

    public static class Config {
        public float minimumVolume = 0.05f;
    }

    private static Config config;

    public static void load() {
        Path path = Path.of("config", "voicechat/voicechat-min-volume.yml");

        try {
            Files.createDirectories(path.getParent());

            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    Map<?, ?> map = YAML.load(in);
                    config = new Config();
                    if (map != null && map.containsKey("minimumVolume")) {
                        config.minimumVolume = ((Number) map.get("minimumVolume")).floatValue();
                    }
                }
            } else {
                config = new Config();
                save();
            }
        } catch (IOException e) {
            SimpleVoiceChatMinimumVolume.LOGGER.error("Failed to load config: {}", e.getMessage());
            config = new Config();
        }
    }

    public static void save() {
        Path path = Path.of("config", "voicechat/voicechat-min-volume.yml");
        try (Writer writer = Files.newBufferedWriter(path)) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("minimumVolume", config.minimumVolume);
            YAML.dump(map, writer);
        } catch (IOException e) {
            SimpleVoiceChatMinimumVolume.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public static void reload() {
        load();
    }

    public static float getMinimumVolume() {
        return config.minimumVolume;
    }

    public static void setMinimumVolume(float volume) {
        config.minimumVolume = volume;
        save();
    }
}
