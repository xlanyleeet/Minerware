package org.gr_code.minerware.games.resources;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.*;

public class Snake {

    private final Inventory inventory;
    private final GamePlayer gamePlayer;
    private final List<Integer> slots = new ArrayList<>();
    private SnakeDirection direction = SnakeDirection.UP;
    private boolean enabled = true;
    private int countApple = 0;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final static List<Integer> allSlots = new ArrayList<>(Arrays.asList(0, 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 ,
            21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53));

    public Snake(GamePlayer gamePlayer, String inventoryName) {
        this.gamePlayer = gamePlayer;
        inventory = Bukkit.createInventory(null, 54, inventoryName);
        gamePlayer.getPlayer().openInventory(inventory);
        slots.add(22);
    }

    public void update() {
        if (!enabled) return;
        if (slots.size() - 1 != 0) inventory.setItem(slots.get(0), XMaterial.LIME_WOOL.parseItem());
        inventory.setItem(slots.get(slots.size() - 1), null);
        for (int i = slots.size() - 1; i > 0; i --) slots.set(i, slots.get(i - 1));
        slots.set(0, getFirstSlot(direction));
        if (inventory.getItem(slots.get(0)) != null) {
            ItemStack had = Objects.requireNonNull(inventory.getItem(slots.get(0)));
            if (had.isSimilar(XMaterial.APPLE.parseItem())) {
                countApple --;
                enlarge();
            } else if (had.isSimilar(XMaterial.LIME_WOOL.parseItem())) {
                Objects.requireNonNull(gamePlayer.getArena().getMicroGame()).onLose(gamePlayer.getPlayer(), true);
                enabled = false;
            }
        }
        inventory.setItem(slots.get(0), XMaterial.GREEN_WOOL.parseItem());
    }

    private int getFirstSlot(SnakeDirection direction) {
        int newSlot = slots.get(0) + direction.getAddedSlots();
        switch (direction) {
            case UP:
                if (slots.get(0) > 8) return newSlot;
                return 45 + slots.get(0);
            case DOWN:
                if (slots.get(0) < 45) return newSlot;
                return slots.get(0) - 45;
            case LEFT:
                if (slots.get(0) % 9 != 0) return newSlot;
                return newSlot + SnakeDirection.DOWN.getAddedSlots();
            default:
                if ((slots.get(0) + 1) % 9 != 0) return newSlot;
                return newSlot + SnakeDirection.UP.getAddedSlots();
        }
    }

    public int getCountApple() {
        return countApple;
    }

    public void generateApple() {
        List<Integer> randomSlots = new ArrayList<>(allSlots);
        randomSlots.removeAll(slots);
        if (randomSlots.size() == 0) return;
        inventory.setItem(randomSlots.get(new Random().nextInt(randomSlots.size())), XMaterial.APPLE.parseItem());
        countApple ++;
    }

    public void enlarge() {
        slots.add(slots.get(slots.size() - 1));
    }

    public void setDirection(SnakeDirection direction) {
        if (slots.size() > 1 && slots.get(1) == getFirstSlot(direction)) return;
        this.direction = direction;
    }

    public int getSize() {
        return slots.size();
    }

    public enum SnakeDirection {
        RIGHT {
            @Override
            public int getAddedSlots() {
                return 1;
            }
        }, LEFT {
            @Override
            public int getAddedSlots() {
                return -1;
            }
        }, UP {
            @Override
            public int getAddedSlots() {
                return -9;
            }
        }, DOWN {
            @Override
            public int getAddedSlots() {
                return +9;
            }
        };

        public abstract int getAddedSlots();
    }

}
