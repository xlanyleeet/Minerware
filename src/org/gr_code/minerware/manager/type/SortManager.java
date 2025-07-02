package org.gr_code.minerware.manager.type;

import org.gr_code.minerware.arena.GamePlayer;

import java.util.Comparator;

public class SortManager implements Comparator<GamePlayer> {

    @Override
    public int compare(GamePlayer o1, GamePlayer o2) {
        return compareTo(o1.getPoints(), o2.getPoints());
    }

    private static int compareTo(int a, int b){
        return Integer.compare(a, b);
    }

}


