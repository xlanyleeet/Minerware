package org.gr_code.minerware.listeners.statistic;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;

import java.util.UUID;

public class PlayerRespawn_Statistic implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Cached cached = CachedMySQL.get(uuid);
        if (cached == null)
            return;
        // StatisticManager.spawnHolograms(cached.getUUID()); // Removed - no longer
        // spawn holograms automatically
    }
}
