package org.gr_code.minerware.listeners.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

public class PlayerExpChange_Games implements Listener{
	@EventHandler
	public void onChange(PlayerExpChangeEvent playerExpChangeEvent) {
		Player p = playerExpChangeEvent.getPlayer();
		if (!Utils.isInGame(p.getUniqueId()))
			return;
		Arena arena = ServerManager.getArena(p.getUniqueId());
		assert arena != null;
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null))
			return;
		if (arena.getMicroGame().getGame().isBossGame())
			return;
		if (arena.getMicroGame().getGame().equals(Game.KNOCKBACK))
			return;
		playerExpChangeEvent.setAmount(0);
	}
}
