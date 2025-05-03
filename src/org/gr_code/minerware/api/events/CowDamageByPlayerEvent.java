package org.gr_code.minerware.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.arena.Arena;

public class CowDamageByPlayerEvent extends Event{
	
	private static final HandlerList handlerList = new HandlerList();
	
	public static HandlerList getHandlerList(){
        return handlerList;
    }
	
	@SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
	
	public CowDamageByPlayerEvent(Player player, Arena arena, Vector vector) {
		this.vector = vector;
		this.arena = arena;
		this.player = player;
	}
	
    private final Player player;

    private final IArena arena;
    
    private Vector vector;

    public Player getPlayer() {
        return player;
    }

    public IArena getArena() {
        return arena;
    }

    public Vector getVector() {
    	return vector;
    }
    
    public void setVector(Vector vector) {
    	this.vector = vector;
    }

}
