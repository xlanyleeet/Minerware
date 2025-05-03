package org.gr_code.minerware.api;

import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.database.type.MySQL;
import org.gr_code.minerware.manager.type.database.cached.Cached;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinerAPI {

    private static final List<Cached> optionalCached = new ArrayList<>();

    /**
     *
     * @param name
     * @return arena with name equals to parameter.
     */

    @Nullable
    public static IArena get(String name){
        return (ServerManager.getArena(name));
    }

    /**
     *
     * @param uuid
     * @return arena which contains player with parameter UUID.
     */

    @Nullable
    public static IArena get(UUID uuid){
        return (ServerManager.getArena(uuid));
    }


    /**
     *
     * @param uuid
     * adds cached data to Optional Cached List
     */

    public static void addCachedObject(UUID uuid) {
        Cached cached = new Cached(uuid);
        MySQL.CallBack<Cached, ResultSet> resultSetCallBack = new MySQL.CallBack<Cached, ResultSet>() {
            @Override
            public void onSuccess(Cached object, ResultSet manager) {
                try {
                    cached.setWins(manager.getInt("WINS"));
                    cached.setGamesPlayed(manager.getInt("GAMES_PLAYED"));
                    cached.setMaxPoints(manager.getInt("MAX_POINTS"));
                    optionalCached.add(cached);
                } catch (SQLException ignored) {
                }
            }

            @Override
            public void onError(Cached object) {
                cached.setWins(0);
                cached.setGamesPlayed(0);
                cached.setMaxPoints(0);
                optionalCached.add(cached);
            }
        };
        MySQL.get(uuid, resultSetCallBack, cached);
    }

    /**
     *
     * @param uuid
     * @return Cached from Optional Cached List
     */

    @Nullable
    public static Cached getOptional(UUID uuid){
        return optionalCached.stream().filter(cached -> cached.getUUID().equals(uuid)).findFirst().orElse(null);
    }

}
