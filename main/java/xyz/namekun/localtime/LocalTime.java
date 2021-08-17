package xyz.namekun.localtime;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class LocalTime extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("localtime").setExecutor(new LocalTimeCmd());
        getCommand("localtime").setTabCompleter(new LocalTimeCmd());
        getCommand("localtimereload").setExecutor(new LocalTimeCmd());
        createFiles();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File configFile = new File(this.getDataFolder(), "config.yml");
    public static FileConfiguration config = new YamlConfiguration();

    public void createFiles() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            this.saveResource("config.yml", false);
        }
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
