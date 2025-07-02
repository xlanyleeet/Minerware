package org.gr_code.minerware.manager.type;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.listeners.statistic.PluginEnable_Statistic;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;

@SuppressWarnings("ALL")
public class PlaceholderManager extends PlaceholderExpansion {

    private static PlaceholderManager instance;

    @Override
    public String getIdentifier() {
        return "minerware";
    }

    @Override
    public String getAuthor() {
        return "Gr_Code";
    }

    @Override
    public String getVersion() {
        return "2.6.9";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equalsIgnoreCase("online"))
            return String.valueOf(ServerManager.getOnline());
        if (identifier.startsWith("arena_stage_")) {
            Arena arena = ServerManager.getArena(identifier.split("_")[2]);
            if (arena == null)
                return identifier;
            return arena.getStage().getSignString();
        }
        if (identifier.startsWith("arena_online_")) {
            Arena arena = ServerManager.getArena(identifier.split("_")[2]);
            if (arena == null)
                return identifier;
            return arena.getCurrentPlayers() + "";
        }
        if (identifier.startsWith("arena_type_")) {
            Arena arena = ServerManager.getArena(identifier.split("_")[2]);
            if (arena == null)
                return identifier;
            return arena.getProperties().getType().toLowerCase();
        }
        if (identifier.startsWith("arena_max_players_")) {
            Arena arena = ServerManager.getArena(identifier.split("_")[3]);
            if (arena == null)
                return identifier;
            return arena.getProperties().getMaxPlayers() + "";
        }
        if (player == null)
            return identifier;
        Cached cached = CachedMySQL.get(player.getUniqueId());
        if (cached == null)
            return identifier;
        if (identifier.equalsIgnoreCase("games_played"))
            return String.valueOf(cached.getGamesPlayed());
        if (identifier.equalsIgnoreCase("wins"))
            return String.valueOf(cached.getWins());
        if (identifier.equalsIgnoreCase("max_points"))
            return String.valueOf(cached.getMaxPoints());
        if (identifier.equalsIgnoreCase("level"))
            return String.valueOf(cached.getLevel());
        if (identifier.equalsIgnoreCase("exp"))
            return String.valueOf(cached.getExp());
        if (identifier.equalsIgnoreCase("percentage"))
            return cached.getPercentage();
        if (identifier.equalsIgnoreCase("level_bar"))
            return cached.createLevelBar();
        return identifier;
    }

    @Override
    public boolean register() {
        MinerPlugin.getInstance().getLogger().info("PlaceholderAPI hooked!");
        return super.register();
    }

    public static boolean setup() {
        instance = new PlaceholderManager();
        instance.register();
        PluginEnable_Statistic.ENABLED = true;
        return true;
    }

    public static PlaceholderManager getInstance() {
        return instance;
    }
}
