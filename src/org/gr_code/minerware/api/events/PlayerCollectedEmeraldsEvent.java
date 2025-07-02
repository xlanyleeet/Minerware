package org.gr_code.minerware.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.arena.Arena;

public class PlayerCollectedEmeraldsEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public PlayerCollectedEmeraldsEvent(Player player, Arena arena, Location location) {
        this.arena = arena;
        this.player = player;
        this.location = location;
    }

    private final Player player;

    private final IArena arena;

    private final Location location;
    
    public Player getPlayer() {
        return player;
    }

    public IArena getArena() {
        return arena;
    }

    public Location getLocation() {
        return location;
    }

}


