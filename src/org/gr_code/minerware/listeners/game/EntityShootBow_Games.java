package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class EntityShootBow_Games implements Listener {
    @EventHandler
    public void qw(EntityShootBowEvent entityShootBowEvent) {
        if (!(entityShootBowEvent.getEntity() instanceof Player)) return;
        Player player = (Player) entityShootBowEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        arena.getMicroGame().event(entityShootBowEvent);
    }
}

