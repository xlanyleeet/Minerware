package org.gr_code.minerware.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.gr_code.minerware.api.events.EventA;
import org.gr_code.minerware.arena.Arena;

public class EventManager {

    public static void craftArenaEvent(Player player, EventA eventA, Arena arena){
        Bukkit.getPluginManager().callEvent(eventA.getEvent(player, arena));
    }

}

