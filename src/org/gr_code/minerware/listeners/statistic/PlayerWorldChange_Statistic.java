package org.gr_code.minerware.listeners.statistic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;

public class PlayerWorldChange_Statistic implements Listener {
    @EventHandler
    public void onChange(PlayerChangedWorldEvent playerChangedWorldEvent) {
        Cached cached = CachedMySQL.get(playerChangedWorldEvent.getPlayer().getUniqueId());
        if (cached == null)
            return;
        // StatisticManager.spawnHolograms(cached.getUUID()); // Removed - no longer
        // spawn holograms automatically
    }
}
