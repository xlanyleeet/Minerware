package org.gr_code.minerware.listeners.statistic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.gr_code.minerware.api.events.PlayerWinMatchEvent;
import org.gr_code.minerware.manager.type.StatisticManager;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;

import java.util.UUID;

public class PlayerWin_Statistic implements Listener {
    @EventHandler
    public void onWin(PlayerWinMatchEvent playerWinMatchEvent){
        UUID uuid = playerWinMatchEvent.getPlayer().getUniqueId();
        Cached cached = CachedMySQL.get(uuid);
        if(cached == null)
            return;
        cached.setWins(cached.getWins()+1);
        StatisticManager.update(uuid);
    }
}


