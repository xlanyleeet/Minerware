package org.gr_code.minerware.api.arena;

import java.util.UUID;

public interface IArena {

    boolean canJoin(UUID uuid);

    boolean equals(Object o);

    boolean forceStartArena();

    void addPlayer(UUID uuid);

    void removePlayer(UUID uuid, boolean sendMessage);

    void sendWarningMessages();

    void forceStopArena();

    int getSeconds();

    int getGamesRemaining();

    int getCurrentPlayers();

    String toString();

    IPlayer getPlayer(UUID uuid);

}


