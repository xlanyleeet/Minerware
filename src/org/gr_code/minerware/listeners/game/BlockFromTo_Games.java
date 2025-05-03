package org.gr_code.minerware.listeners.game;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;

import java.util.Objects;

public class BlockFromTo_Games implements Listener{
	@EventHandler
	public void onBlock(BlockFromToEvent blockFromToEvent) {
		if (!isWaterOrLava(blockFromToEvent.getBlock()))
			return;
		String world = Objects.requireNonNull(blockFromToEvent.getBlock().getLocation().getWorld()).getName();
		if (ServerManager.getArena(world) == null)
			return;
		Arena arena = ServerManager.getArena(world);
		assert arena != null;
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null))
			return;
		if (arena.getProperties().getCuboid().notInside(blockFromToEvent.getBlock().getLocation()))
			return;
		blockFromToEvent.setCancelled(true);
	}

	private boolean isWaterOrLava(Block block) {
		int idBlock = ManageHandler.getNMS().getTypeId(block);
		Material materialBlock = block.getType();
		if (!ManageHandler.getNMS().isLegacy())
			return (materialBlock == Material.WATER || materialBlock == Material.LAVA);
		return (idBlock == 8 || idBlock == 9 || idBlock == 10 || idBlock == 11);
	}
}
