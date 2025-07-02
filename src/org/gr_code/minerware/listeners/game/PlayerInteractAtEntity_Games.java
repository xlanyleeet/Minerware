package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class PlayerInteractAtEntity_Games implements Listener {
    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        Player player = playerInteractAtEntityEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        if (!(playerInteractAtEntityEvent.getRightClicked() instanceof ArmorStand)) return;
        playerInteractAtEntityEvent.setCancelled(true);
    }
}

