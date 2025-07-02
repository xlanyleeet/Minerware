package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class EntityCombust_Games implements Listener {
    @EventHandler
    public void burn(EntityCombustEvent entityCombustEvent) {
    	if (!(entityCombustEvent.getEntity() instanceof Player))
    	    return;
        Player player = (Player) entityCombustEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        entityCombustEvent.setCancelled(true);
    }
}

