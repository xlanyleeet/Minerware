package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class EntityPickUpItem_Games implements Listener {
    @EventHandler
    public void onClick(EntityPickupItemEvent entityPickupItemEvent) {
    	if (!(entityPickupItemEvent.getEntity() instanceof Player))
    	    return;
        Player player = (Player) entityPickupItemEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        entityPickupItemEvent.setCancelled(true);
    }
}