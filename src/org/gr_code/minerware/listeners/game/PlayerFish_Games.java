package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

public class PlayerFish_Games implements Listener {
    @EventHandler
    public void av(PlayerFishEvent playerFishEvent) {
        Player p = playerFishEvent.getPlayer();
        if (!Utils.isInGame(p.getUniqueId())) return;
        Arena arena = ServerManager.getArena(p.getUniqueId());
        assert arena != null;
        if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
        arena.getMicroGame().event(playerFishEvent);
    }
}
