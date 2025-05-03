package org.gr_code.minerware.manager.type.database.cached;

import org.bukkit.entity.Player;
import org.gr_code.minerware.manager.type.LobbyHelper;
import org.gr_code.minerware.manager.type.StatisticManager;
import org.gr_code.minerware.manager.type.database.type.MySQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CachedMySQL {

    private static final List<Cached> cached = new ArrayList<>();

    public static List<Cached> getCached() {
        return cached;
    }

    public static void add(Player player) {
        UUID uuid = player.getUniqueId();
        Cached cached = new Cached(player);
        MySQL.CallBack<Cached, ResultSet> resultSetCallBack = new MySQL.CallBack<Cached, ResultSet>() {
            @Override
            public void onSuccess(Cached object, ResultSet manager) {
                try {
                    cached.setWins(manager.getInt("WINS"));
                    cached.setGamesPlayed(manager.getInt("GAMES_PLAYED"));
                    cached.setMaxPoints(manager.getInt("MAX_POINTS"));
                    cached.setLevel(manager.getInt("LEVEL"));
                    cached.setExp(manager.getInt("EXP"));
                    CachedMySQL.cached.add(cached);
                    StatisticManager.spawnHolograms(uuid);
                } catch (SQLException ignored) { }
            }

            @Override
            public void onError(Cached object) {
                cached.setWins(0);
                cached.setGamesPlayed(0);
                cached.setMaxPoints(0);
                cached.setLevel(0);
                cached.setExp(0);
                CachedMySQL.cached.add(cached);
                StatisticManager.spawnHolograms(uuid);
            }
        };
        MySQL.get(uuid, resultSetCallBack, cached);
        LobbyHelper.addPlayer(player);
    }

    public static Cached get(UUID uuid) {
        return cached.stream().filter(cached -> cached.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public static void clearAll() {
        cached.clear();
    }
}
