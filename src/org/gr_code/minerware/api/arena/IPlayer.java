package org.gr_code.minerware.api.arena;

import org.gr_code.minerware.arena.GamePlayer;

public interface IPlayer {

    int getPoints();

    GamePlayer.State getState();

    void setPoints(int points);

    void setPlace(int place);

    int getPlace();

}


