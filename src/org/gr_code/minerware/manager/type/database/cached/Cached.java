package org.gr_code.minerware.manager.type.database.cached;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.database.type.MySQL;
import org.gr_code.minerware.api.hologram.IHologram;
import org.gr_code.minerware.api.hologram.ModernHologram;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;

import java.util.UUID;

public class Cached {

    public enum Block {

        RANDOM_WOOL(0, "random_wool"),
        DIAMOND_BLOCK(5, "diamond_block"),
        GLASS(3, "random_glass"),
        HAY_BLOCK(1, "hay_block"),
        RANDOM_WOOD(2, "random_wood"),
        ICE(6, "ice_block"),
        OAK_LOG(4, "oak_log");

        Block(int minRank, String id) {
            this.minRank = minRank;
            this.id = id;
        }

        private final int minRank;

        private final String id;

        public int getRank() {
            return minRank;
        }

        public String getID() {
            return id;
        }

        public static Block getByID(String id) {
            for (Block block : Block.values()) {
                if (block.getID().equalsIgnoreCase(id))
                    return block;
            }
            return null;
        }
    }

    public enum Trail {

        SNOWBALL(1000, "snowball"),
        ANGRY_VILLAGER(1250, "angry_villager"),
        LAVA(2000, "lava"),
        NOTE(2500, "note"),
        MAGIC_CRIT(2500, "magic_crit"),
        PORTAL(3000, "portal"),
        REDSTONE(6000, "redstone"),
        HEART(3500, "heart"),
        BUBBLE(4000, "bubble"),
        CLOUD(4000, "cloud"),
        CRIT(4500, "crit"),
        HAPPY_VILLAGER(4500, "happy_villager"),
        SMOKE(5000, "smoke"),
        FLAME(6000, "flame");

        Trail(int price, String id) {
            this.price = price;
            this.id = id;
            this.particle = ModernMinerAPI.MinerParticle.valueOf(name());
        }

        private final int price;

        private final String id;

        private final ModernMinerAPI.MinerParticle particle;

        public String getID() {
            return id;
        }

        public int getPrice() {
            return price;
        }

        public ModernMinerAPI.MinerParticle getParticle() {
            return particle;
        }

        public static Trail getByID(String id) {
            for (Trail trail : Trail.values()) {
                if (trail.getID().equalsIgnoreCase(id))
                    return trail;
            }
            return null;
        }
    }

    private int wins, gamesPlayed, maxPoints, level, exp;

    private final UUID uuid;

    private IHologram iHologram;

    public Cached(Player player) {
        this.uuid = player.getUniqueId();
        this.wins = -1;
        this.gamesPlayed = -1;
        this.maxPoints = -1;
        this.level = -1;
        this.exp = -1;
    }

    public Cached(UUID uuid) {
        this.uuid = uuid;
        this.wins = -1;
        this.gamesPlayed = -1;
        this.maxPoints = -1;
        this.level = -1;
        this.exp = -1;
    }

    public int getWins() {
        return wins;
    }

    public UUID getUUID() {
        return uuid;
    }

    public IHologram getIHologram() {
        return iHologram;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setStatsHologram(IHologram iHologram) {
        this.iHologram = iHologram;
    }

    public void setWins(int wins) {
        if (this.wins != -1)
            MySQL.set(uuid, MySQL.Path.WINS, wins);
        this.wins = wins;
    }

    public void setGamesPlayed(int gamesPlayed) {
        if (this.gamesPlayed != -1)
            MySQL.set(uuid, MySQL.Path.GAMES_PLAYED, gamesPlayed);
        this.gamesPlayed = gamesPlayed;
    }

    public void setMaxPoints(int maxPoints) {
        if (this.maxPoints != -1)
            MySQL.set(uuid, MySQL.Path.MAX_POINTS, maxPoints);
        this.maxPoints = maxPoints;
    }

    public void setExp(int exp) {
        if (this.exp != -1)
            MySQL.set(uuid, MySQL.Path.EXP, exp);
        this.exp = exp;
    }

    public void setLevel(int level) {
        if (this.level != -1)
            MySQL.set(uuid, MySQL.Path.LEVEL, level);
        this.level = level;
    }

    public void addExp(int exp) {
        if (exp < 1)
            return;
        if (this.exp + exp >= getNext(level)) {
            int result = exp - getNext(level) + this.exp;
            setLevel(level + 1);
            this.exp = 0;
            addExp(result);
            return;
        }
        setExp(exp + this.exp);
    }

    public String getPercentage() {
        return String.format("%.0f", (float) exp / getNext(level) * 100);
    }

    private static int getNext(int level) {
        if (level > 10)
            return level * 350;
        return Math.max(100, level * 300);
    }

    public String createLevelBar() {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        String symbol = fileConfiguration.getString("level-bar.symbol");
        assert symbol != null;
        String codeA = fileConfiguration.getString("level-bar.code-a");
        String codeB = fileConfiguration.getString("level-bar.code-b");
        int percentage = Integer.parseInt(getPercentage());
        percentage = percentage / 10;
        assert codeA != null;
        StringBuilder stringBuilder = new StringBuilder(codeA);
        boolean changed = false;
        for (int i = 0; i < 9; i++) {
            String string = symbol;
            if (i == percentage && !changed) {
                string = codeB + symbol;
                changed = true;
            }
            stringBuilder.append(string);
        }
        return Utils.translate(stringBuilder.toString());
    }

}
