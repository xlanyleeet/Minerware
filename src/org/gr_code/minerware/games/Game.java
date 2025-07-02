package org.gr_code.minerware.games;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.games.bossgames.*;
import org.gr_code.minerware.games.microgames.*;

import java.util.*;

public enum Game {

    INFO {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Info(arena);
        }

        @Override
        public boolean m() {
            return true;
        }
    }, FALLING_PLATFORMS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FallingPlatform(arena);
        }
    }, INVISIBLE_BUTTON {
        @Override
        public MicroGame createGame(Arena arena) {
            return new InvisibleButton(arena);
        }
    }, HIGH_PLATFORM {
        @Override
        public MicroGame createGame(Arena arena) {
            return new HighPlatform(arena);
        }
    }, FALLING_ARROWS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FallingArrows(arena);
        }
    }, DANGEROUS_CACTUS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new DangerousCactus(arena);
        }
    }, FIND_THE_BEACON {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FindTheBeacon(arena);
        }
    }, GRAB_A_SAPLING {
        @Override
        public MicroGame createGame(Arena arena) {
            return new GrabASapling(arena);
        }
    }, LIGHTNING_STRIKES {
        @Override
        public MicroGame createGame(Arena arena) {
            return new LightningStrikes(arena);
        }
    }, GRAB_A_FLOWER {
        @Override
        public MicroGame createGame(Arena arena) {
            return new GrabAFlower(arena);
        }
    }, SWIM_TO_THE_PLATFORM {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SwimToThePlatform(arena);
        }
    }, GIVE_MR_BLOBBO {
        @Override
        public MicroGame createGame(Arena arena) {
            return new GiveMrBlobbo(arena);
        }
    }, LAND_ON_SLIME {
        @Override
        public MicroGame createGame(Arena arena) {
            return new LandOnSlime(arena);
        }
    }, DONT_CRASH {
        @Override
        public MicroGame createGame(Arena arena) {
            return new DontCrash(arena);
        }
    }, KNOCK_EVERYONE {
        @Override
        public MicroGame createGame(Arena arena) {
            return new KnockEveryone(arena);
        }
    }, SNOWBALL_FIGHT {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SnowballFight(arena);
        }
    }, PARKOUR {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Parkour(arena);
        }
    }, FLY_FLY_AND_FLY {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FlyFlyAndFly(arena);
        }
    }, SPIDER_MAN {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SpiderMan(arena);
        }
    }, KING_OF_THE_HILL {
        @Override
        public MicroGame createGame(Arena arena) {
            return new KingOfTheHill(arena);
        }
    }, CHEST_PVP {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ChestPvP(arena);
        }
    }, TNT_TAG {
        @Override
        public MicroGame createGame(Arena arena) {
            return new TNTTag(arena);
        }
    }, MILK_COW {
        @Override
        public MicroGame createGame(Arena arena) {
            return new MilkCow(arena);
        }
    }, IGNITE_TNT {
        @Override
        public MicroGame createGame(Arena arena) {
            return new IgniteTNT(arena);
        }
    }, LAND_ON_THE_FLOOR {
        @Override
        public MicroGame createGame(Arena arena) {
            return new LandOnTheFloor(arena);
        }
    }, MINE_ORE {
        @Override
        public MicroGame createGame(Arena arena) {
            return new MineOre(arena);
        }
    }, FALLING_ANVILS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FallingAnvils(arena);
        }
    }, STAND_ON_DIAMOND {
        @Override
        public MicroGame createGame(Arena arena) {
            return new StandOnDiamond(arena);
        }
    }, FEED_ANIMALS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new FeedAnimals(arena);
        }
    }, SHEAR_SHEEP {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ShearSheeps(arena);
        }
    }, RIDE_COW {
        @Override
        public MicroGame createGame(Arena arena) {
            return new RideCow(arena);
        }
    }, STACK_BLOCKS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new StackBlocks(arena);
        }
    }, JUMP_AND_STAY {
        @Override
        public MicroGame createGame(Arena arena) {
            return new JumpAndStay(arena);
        }
    }, HOT_POTATO {
        @Override
        public MicroGame createGame(Arena arena) {
            return new HotPotato(arena);
        }
    }, BLOCK_PARTY {
        @Override
        public MicroGame createGame(Arena arena) {
            return new BlockParty(arena);
        }
    }, CATCH_EMERALDS {
        @Override
        public MicroGame createGame(Arena arena) {
            return new CatchEmeralds(arena);
        }
    }, MATH {
        @Override
        public MicroGame createGame(Arena arena) {
            return new MathGame(arena);
        }
    }, JUMP_OR_FLY {
        @Override
        public MicroGame createGame(Arena arena) {
            return new JumpOrFly(arena);
        }
    }, MOVE {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Move(arena);
        }
    }, DROP_ITEM {
        @Override
        public MicroGame createGame(Arena arena) {
            return new DropItem(arena);
        }
    }, TNT_EXPLOSION {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ExplosionTNT(arena);
        }
    }, SNEAK {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Sneak(arena);
        }
    }, ICE_FLOOR {
        @Override
        public MicroGame createGame(Arena arena) {
            return new IceFloor(arena);
        }
    }, LAVA_FLOOR {
        @Override
        public MicroGame createGame(Arena arena) {
            return new LavaFloor(arena);
        }
    }, KNOCKBACK {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Knockback(arena);
        }
    }, DONT_MOVE {
        @Override
        public MicroGame createGame(Arena arena) {
            return new DontMove(arena);
        }
    }, PVP {
        @Override
        public MicroGame createGame(Arena arena) {
            return new PvP(arena);
        }
    }, QUESTION {
        @Override
        public MicroGame createGame(Arena arena) {
            return new Question(arena);
        }
    }, ONE_IN_THE_CHAMBER {
        @Override
        public MicroGame createGame(Arena arena) {
            return new OneInTheChamber(arena);
        }
    }, SECOND_QUESTION {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SecondQuestion(arena);
        }
    }, THROW_TNT {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ThrowTNT(arena);
        }
    }, EXPLOSIVE_BOW {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ExplosiveBow(arena);
        }
    }, BOSS_SPLEEF {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SpleefBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    }, BOSS_BOW_SPLEEF {
        @Override
        public MicroGame createGame(Arena arena) {
            return new BowSpleefBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    }, BOSS_TNT_RUN {
        @Override
        public MicroGame createGame(Arena arena) {
            return new TNTRunBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    }, BOSS_BLOCK_PARTY {
        @Override
        public MicroGame createGame(Arena arena) {
            return new BlockPartyBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    }, BOSS_COLOURED_FLOOR {
        @Override
        public MicroGame createGame(Arena arena) {
            return new ColouredFloorBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    }, BOSS_SNAKE {
        @Override
        public MicroGame createGame(Arena arena) {
            return new SnakeBoss(arena);
        }

        @Override
        public boolean isBossGame() {
            return true;
        }
    };

    public static Game e = Game.INFO;

    public abstract MicroGame createGame(Arena arena);

    public boolean isBossGame() {
        return false;
    }

    private boolean b() {
        return isBossGame();
    }

    public boolean m() {
        return false;
    }

    public boolean a() {
        return isBossGame();
    }

    public static boolean a(String s) {
        return s.equals("INFO");
    }


    public static ArrayList<Game> a(Set<String> b) {
        ArrayList<Game> list = new ArrayList<>(Arrays.asList(values().clone()));
        for (String aS : b) {
            if (c(aS) != null)
                list.remove(valueOf(aS));
        }
        return list;
    }

    public static Game c(String b) {
        try {
            Game.valueOf(b);
        } catch (Exception e) {
            return null;
        }
        return Game.valueOf(b);
    }

    public static LinkedList<Game> b(ArrayList<Game> c, int f) {
        LinkedList<Game> d = new LinkedList<>();
        d.add(c.get(0));
        while (d.size() < f) {
            int i = new Random().nextInt(c.size());
            Game a = c.get(i);
            if (d.size() != f - 1) {
                if (a.b())
                    continue;
                if (!d.contains(a))
                    d.add(a);
            }
            if (a.b()) {
                d.add(a);
            }
        }
        return d;
    }

    public static Game c(ItemStack itemStack) {
        return Arrays.stream(values())
                .filter(game -> Objects.requireNonNull(game.createGame(null).getGameItemStack().getItemMeta()).
                        getDisplayName().equals(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName()))
                .findFirst().orElse(null);
    }

    public static int a(List<Inventory> itemStacks) {
        int count = 0;
        for(Inventory i : itemStacks) {
            for (ItemStack itemStack : i) {
                if (itemStack == null || !itemStack.hasItemMeta())
                    continue;
                if (c(itemStack) != null && c(itemStack).b() && !itemStack.getEnchantments().isEmpty())
                    count++;
            }
        }
        return count;
    }

    public static int a_(List<Inventory> itemStacks) {
        int count = 0;
        for(Inventory i : itemStacks){
            for (ItemStack item : i) {
                if (item == null || !item.hasItemMeta())
                    continue;
                if (c(item) != null && !c(item).a() && !item.getEnchantments().isEmpty())
                    count++;
            }
        }
        return count;
    }

}


