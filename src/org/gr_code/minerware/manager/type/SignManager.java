package org.gr_code.minerware.manager.type;

import io.netty.util.internal.ConcurrentSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SignManager {

    private static Set<String> signLocations;

    static FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();

    public static Set<String> getSignLocations() {
        return signLocations;
    }

    public static String title;

    public static void addSign(Block block) {
        if (!(block.getState() instanceof Sign))
            return;
        if(signLocations.contains(SetupManager.toString(block.getLocation())))
            return;
        signLocations.add(SetupManager.toString(block.getLocation()));
        List<String> aLocations = new ArrayList<>(getSignLocations());
        fileConfiguration.set("sign.locations", aLocations);
        saveFile();
    }

    public static void load() {
        title = fileConfiguration.getString("sign.format.title");
        title = title == null ? "&8[&e&lMiner&6&lWare&8]" : title;
        signLocations = new ConcurrentSet<>();
        signLocations.addAll(fileConfiguration.getStringList("sign.locations"));
        update();
    }

    private static void removeSign(String aString) {
        if (!getSignLocations().contains(aString))
            return;
        getSignLocations().remove(aString);
        List<String> aLocations = new ArrayList<>(getSignLocations());
        fileConfiguration.set("sign.locations", aLocations);
        saveFile();
    }

    public static void update() {
        if(getSignLocations() == null || getSignLocations().isEmpty())
            return;
        signLocations.stream().iterator().forEachRemaining(SignManager::updateSignSynchronously);
    }

    public static void updateSignSynchronously(String signLocation) {
        if (!isParsable(signLocation)) {
            removeSign(signLocation);
            return;
        }
        if(Utils.isNullable(signLocation)) {
            removeSign(signLocation);
            return;
        }
        Location location = SetupManager.fromString(signLocation);
        Block block = location.getBlock();
        if (!(block.getState() instanceof Sign)) {
            removeSign(signLocation);
            return;
        }
        Sign sign = (Sign) block.getState();
        String string = ChatColor.stripColor(sign.getLine(1));
        if (ServerManager.getArena(string) == null) {
            clearLines(sign);
            removeSign(signLocation);
            return;
        }
        Arena arena = ServerManager.getArena(string);
        assert arena != null;
        sign.setLine(2, arena.getStage().getSignString());
        sign.setLine(3, Utils.translate("&8" + arena.getCurrentPlayers() + " / " + arena.getProperties().getMaxPlayers()));
        sign.update();
    }

    private static void saveFile() {
        MinerPlugin minerPlugin = MinerPlugin.getInstance();
        File file = minerPlugin.getOptionsFile();
        try {
            minerPlugin.getOptions().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isParsable(String aString) {
        try {
            SetupManager.fromString(aString);
        } catch (Exception e) {
            Bukkit.getLogger().warning(Utils.translate("&cSomething went wrong loading: " + aString));
            return false;
        }
        return true;
    }

    private static void clearLines(Sign sign) {
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, "");
        }
        sign.update();
    }

}
