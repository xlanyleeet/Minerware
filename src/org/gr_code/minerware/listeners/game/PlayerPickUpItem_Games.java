package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class PlayerPickUpItem_Games implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClick(PlayerPickupItemEvent playerPickupItemEvent) {
        Player player = playerPickupItemEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        playerPickupItemEvent.setCancelled(true);
    }
}