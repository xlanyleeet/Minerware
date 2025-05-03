package org.gr_code.minerware;

import com.google.common.base.Charsets;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class MinerPlugin extends JavaPlugin implements Listener {
    
    private static final HashSet<Arena> ARENA_REGISTRY = new HashSet<>();

    public MinerPlugin() {
        minerPlugin = this;
    }

    private static MinerPlugin minerPlugin;

    public static MinerPlugin getInstance() {
        return minerPlugin;
    }

    @Override
    public void onEnable() {
        initialize();
        ManageHandler.setupMinerware();
    }

    @Override
    public void onDisable() {
        ManageHandler.stop();
    }

    public static HashSet<Arena> getARENA_REGISTRY() {
        return ARENA_REGISTRY;
    }

    public FileConfiguration getArenas() {
        return configurations[0];
    }

    public FileConfiguration getOptions() {
        return configurations[2];
    }

    public FileConfiguration getBungee() {
        return configurations[5];
    }

    public FileConfiguration getMessages() {
        return configurations[1];
    }

    public FileConfiguration getGames() {
        return configurations[3];
    }

    public FileConfiguration getBossGames() {
        return configurations[4];
    }

    public FileConfiguration getLanguage() {
        return configurations[6];
    }

    public File getArenasFile() {
        return files[0];
    }

    public File getMessagesFile() {
        return files[1];
    }

    public File getOptionsFile() {
        return files[2];
    }

    private static final String[] strings = {"arenas.yml", "configuration.yml", "options.yml", "games.yml", "boss_games.yml", "bungeecord.yml", "language.yml"};

    private final FileConfiguration[] configurations = new FileConfiguration[strings.length * 2 - 1];

    private final File[] files = new File[strings.length];

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initialize() {
        for (int i = 0; i < strings.length; i++) {
            String name = strings[i];
            File file = new File(getDataFolder(), name);
            if (!getDataFolder().exists()) {
                file.getParentFile().mkdirs();
                saveResource(name, false);
                System.out.println("[MinerWare] File " + file.getName() + " has been created.");
            }
            FileConfiguration fileConfiguration = new MinerConfiguration();
            try {
                fileConfiguration.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                System.out.println("[MinerWare] Could not load file " + file.getName() + "!");
            }
            files[i] = file;
            configurations[i] = fileConfiguration;
        }
        internalInitialize();
    }

    private void internalInitialize() {
        for (int i = 1; i < strings.length; i++) {
            InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(getResource(strings[i])), StandardCharsets.UTF_8);
            configurations[strings.length + i - 1] = YamlConfiguration.loadConfiguration(inputStreamReader);
        }
        defaults();
    }

    private void defaults() {
        for (int i = strings.length; i < strings.length * 2 - 1; i++)
            validateEmpty(configurations[i - 6], files[i - 6], configurations[i]);
    }

    private void validateEmpty(FileConfiguration fileConfiguration, File file, FileConfiguration configuration) {
        for (String path : configuration.getKeys(true)) {
            if (!(configuration.get(path) != null && fileConfiguration.get(path) == null))
                continue;
            Object object = configuration.get(path);
            if (!(object instanceof List || object instanceof Boolean || object instanceof Number)) {
                assert object != null;
                String string = object.toString();
                fileConfiguration.set(path, string);
                continue;
            }
            fileConfiguration.set(path, object);
        }
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            System.out.println("[MinerWare] Could not save file " + file.getName() + "!");
        }
    }
    
    public static class MinerConfiguration extends YamlConfiguration {

        @Override
        public @Nullable String getString(@NotNull String path) {
            String string = super.getString(path);
            if (string != null && ManageHandler.getNMS().oldVersion()) {
                string = new String(string.getBytes(), Charsets.UTF_8);
                return string;
            }
            return string;
        }

        @Override
        public void setDefaults(@NotNull Configuration defaults) {
            super.setDefaults(defaults);
        }
    }


}
