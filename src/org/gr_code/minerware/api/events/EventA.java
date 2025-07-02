package org.gr_code.minerware.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.gr_code.minerware.arena.Arena;

public enum EventA {
    JOIN {
        @Override
        public Event getEvent(Player player, Arena arena) {
            PlayerJoinArenaEvent playerJoinArenaEvent = new PlayerJoinArenaEvent();
            playerJoinArenaEvent.setPlayer(player);
            playerJoinArenaEvent.setArena(arena);
            return playerJoinArenaEvent;
        }
    }, PREPARE_LEAVE {
        @Override
        public Event getEvent(Player player, Arena arena) {
            PlayerPrepareLeaveArenaEvent playerLeaveArenaEvent = new PlayerPrepareLeaveArenaEvent();
            playerLeaveArenaEvent.setArena(arena);
            playerLeaveArenaEvent.setPlayer(player);
            return playerLeaveArenaEvent;
        }
    },
    LEAVE {
        @Override
        public Event getEvent(Player player, Arena arena) {
            PlayerLeaveArenaEvent playerLeaveArenaEvent = new PlayerLeaveArenaEvent();
            playerLeaveArenaEvent.setPlayer(player);
            playerLeaveArenaEvent.setArena(arena);
            return playerLeaveArenaEvent;
        }
    }, WIN_MATCH {
        @Override
        public Event getEvent(Player player, Arena arena) {
            PlayerWinMatchEvent playerWinMatchEvent = new PlayerWinMatchEvent();
            playerWinMatchEvent.setPlayer(player);
            playerWinMatchEvent.setArena(arena);
            return playerWinMatchEvent;
        }
    }, FINISH_MATCH {
        @Override
        public Event getEvent(Player player, Arena arena) {
            ArenaFinishMatchEvent arenaFinishMatchEvent = new ArenaFinishMatchEvent();
            arenaFinishMatchEvent.setArena(arena);
            return arenaFinishMatchEvent;
        }
    };

    public abstract Event getEvent(Player player, Arena arena);
}


