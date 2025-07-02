package org.gr_code.minerware.manager.type.resources.effects;

import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.resources.effects.win.*;

public abstract class WinEffect {

    public abstract Type getType();

    private final Arena arena;

    private final int time;

    public WinEffect(Arena arena, int time) {
        this.arena = arena;
        this.time = time;
    }

    public Arena getArena() {
        return arena;
    }

    public int getTime() {
        return time;
    }

    public abstract void update();

    public abstract void start();

    public abstract void stop();

    public abstract ItemStack getItemStack();

    public enum Type {

        ICE {
            @Override
            public WinEffect getInstance(Arena arena, int time) {
                return new IceWinEffect(arena, time);
            }
        }, BLOCKS {
            @Override
            public WinEffect getInstance(Arena arena, int time) {
                return new BlocksWinEffect(arena, time);
            }
        }, FIRE {
            @Override
            public WinEffect getInstance(Arena arena, int time) {
                return new FireWinEffect(arena, time);
            }
        }, EXPLOSION {
            @Override
            public WinEffect getInstance(Arena arena, int time) {
                return new ExplosionWinEffect(arena, time);
            }
        }, ROCKET {
            @Override
            public WinEffect getInstance(Arena arena, int time) {
                return new RocketWinEffect(arena, time);
            }
        };

        public abstract WinEffect getInstance(Arena arena, int time);

        public ItemStack getItem(){
            return this.getInstance(null, 0).getItemStack();
        }

    }

    private static Type parseType(String string){
        try {
            Type.valueOf(string);
        }catch (Exception exception){
            return null;
        }
        return Type.valueOf(string);
    }

}


