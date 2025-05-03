package org.gr_code.minerware.listeners.waiting;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;
import java.util.UUID;

public class EntityDamage_Waiting implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent entityDamageEvent){
        if(!(entityDamageEvent.getEntity() instanceof Player))
            return;
        Player player = (Player) entityDamageEvent.getEntity();
        UUID uuid = player.getUniqueId();
        if(!Utils.isInGame(uuid))
            return;
        if(Objects.requireNonNull(ServerManager.getArena(uuid)).getStage().equals(Arena.Stage.PLAYING))
            return;
        if(entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
            entityDamageEvent.setDamage(0);
            player.setFallDistance(0);
            player.teleport(Cuboid.getRandomLocation(Objects.requireNonNull(ServerManager.getArena(uuid))));
            return;
        }
        entityDamageEvent.setCancelled(true);
    }
}
