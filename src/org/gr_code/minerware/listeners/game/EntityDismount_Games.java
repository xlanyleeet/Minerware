package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.UUID;

public class EntityDismount_Games implements Listener {
    @EventHandler
    public void kjb(EntityDismountEvent entityDismountEvent) {
        if (!(entityDismountEvent.getEntity() instanceof Player)) return;
        Player player = (Player) entityDismountEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        arena.getMicroGame().event(entityDismountEvent);
    }
}


