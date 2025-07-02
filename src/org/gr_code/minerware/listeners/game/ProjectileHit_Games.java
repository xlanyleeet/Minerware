package org.gr_code.minerware.listeners.game;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;

public class ProjectileHit_Games implements Listener {
	@EventHandler
	public void onClick(ProjectileHitEvent projectileHitEvent) {
		if (projectileHitEvent.getEntity().getType() == EntityType.FISHING_HOOK) return;
		Arena arena = ServerManager.getArena(projectileHitEvent.getEntity().getWorld().getName());
		if (arena == null) return;
		Block hitBlock = getHitBlockNMS(projectileHitEvent);
		if (hitBlock == null) return;
		projectileHitEvent.getEntity().remove();
		if (!(arena.getStage().equals(Arena.Stage.PLAYING) && arena.getMicroGame() != null)) return;
		if (arena.getProperties().getCuboid().notInside(hitBlock.getLocation())) return;
		arena.getMicroGame().event(projectileHitEvent);
	}

	public static Block getHitBlockNMS(ProjectileHitEvent projectileHitEvent) {
		if (!ManageHandler.getModernAPI().oldVersion()) return projectileHitEvent.getHitBlock();
		World world = projectileHitEvent.getEntity().getWorld();
		Vector vector = projectileHitEvent.getEntity().getLocation().toVector();
		Vector vector2 = projectileHitEvent.getEntity().getVelocity().normalize();
		BlockIterator iterator = new BlockIterator(world, vector, vector2, 0, 4);
		Block hitBlock;
		while (iterator.hasNext()) {
			hitBlock = iterator.next();
			if (hitBlock.getType() != Material.AIR) return hitBlock;
		}
		return null;
	}
}


