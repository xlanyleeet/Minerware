package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.UUID;

public class FoodLevelChange_Games implements Listener {
    @EventHandler
    public void onDamage(FoodLevelChangeEvent foodLevelChangeEvent) {
        if (!(foodLevelChangeEvent.getEntity() instanceof Player)) return;
        Player player = (Player) foodLevelChangeEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        Arena arena = ServerManager.getArena(uuid);
        assert arena != null;
        if (arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null && arena.getMicroGame().getGame().equals(Game.BOSS_SPLEEF)) return;
        foodLevelChangeEvent.setCancelled(true);
        player.setFoodLevel(20);
    }

}
