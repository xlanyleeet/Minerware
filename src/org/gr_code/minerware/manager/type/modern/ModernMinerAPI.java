package org.gr_code.minerware.manager.type.modern;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Door;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Modern implementation without NMS dependencies
 * Compatible with Minecraft 1.19.4+
 */
public class ModernMinerAPI {

    private final Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
    private final char COLOR_CHAR = ChatColor.COLOR_CHAR;

    // ===== UTILITY METHODS =====

    public boolean isLegacy() {
        return false; // Always false for modern versions
    }

    public boolean oldVersion() {
        return false; // Always false for modern versions
    }

    public String translate(String message) {
        // Support for hex colors in 1.16+
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    // ===== INVENTORY MANAGEMENT =====

    public ItemStack[] getInventoryContents(Player player) {
        ItemStack[] itemStacks = new ItemStack[41];
        PlayerInventory inventory = player.getInventory();

        // Main inventory (36 slots)
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                itemStacks[i] = item.clone();
            }
        }

        // Armor contents (4 slots)
        ItemStack[] armorContents = inventory.getArmorContents();
        for (int i = 0; i < 4; i++) {
            if (armorContents[i] != null) {
                itemStacks[36 + i] = armorContents[i].clone();
            }
        }

        // Off-hand
        ItemStack offHand = inventory.getItemInOffHand();
        itemStacks[40] = offHand != null ? offHand.clone() : null;

        // Clear inventory
        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(null);

        return itemStacks;
    }

    public void restoreInventory(Player player, ItemStack[] contents) {
        PlayerInventory inventory = player.getInventory();

        // Restore main inventory
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, contents[i]);
        }

        // Restore armor
        inventory.setArmorContents(new ItemStack[] {
                contents[36], contents[37], contents[38], contents[39]
        });

        // Restore off-hand
        inventory.setItemInOffHand(contents[40]);
    }

    // ===== ITEM UTILITIES =====

    public ItemStack setGlowing(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public ItemStack setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public boolean equalsItemStack(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isSimilar(itemStack2);
    }

    // ===== BLOCK MANAGEMENT =====

    public int getTypeId(Block block) {
        // Return 0 for modern versions - type IDs are deprecated
        return 0;
    }

    public void setBlock(ItemStack baseItem, Block block) {
        block.setType(baseItem.getType());
    }

    public void setBlock(int id, byte data, Block block) {
        // Legacy method - do nothing in modern versions
    }

    public Consumer<Location> getGeneratorDoors() {
        ItemStack planks = requireNonNull(XMaterial.OAK_PLANKS.parseItem());
        return location -> {
            setBlock(planks, location.getBlock());

            Block bottom = location.clone().add(0, 1, 0).getBlock();
            Block top = location.clone().add(0, 2, 0).getBlock();

            BlockState bottomState = bottom.getState();
            BlockState topState = top.getState();

            Door doorBottom = (Door) Bukkit.createBlockData(Material.OAK_DOOR);
            Door doorTop = (Door) Bukkit.createBlockData(Material.OAK_DOOR);

            doorBottom.setHalf(Bisected.Half.BOTTOM);
            doorTop.setHalf(Bisected.Half.TOP);

            boolean open = Math.random() <= 0.5;
            doorBottom.setOpen(open);
            doorTop.setOpen(open);

            bottom.setBlockData(doorBottom);
            top.setBlockData(doorTop);

            bottomState.update();
            topState.update();
        };
    }

    public void setUpDirectionButton(Block block) {
        if (block.getBlockData() instanceof FaceAttachable button) {
            button.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
            block.setBlockData(button);
        }
    }

    public void setNoAI(org.bukkit.entity.Entity entity) {
        if (entity instanceof org.bukkit.entity.Mob mob) {
            mob.setAI(false);
        }
    }

    public void setHurtEntities(FallingBlock block) {
        block.setHurtEntities(true);
    }

    // ===== PARTICLE EFFECTS =====

    public void playOutParticle(Location location, float offset, MinerParticle particle, int amount) {
        playOutParticle(location, offset, offset, offset, particle, 0, amount);
    }

    public void playOutParticle(Location location, float offset, MinerParticle particle, float speed, int amount) {
        playOutParticle(location, offset, offset, offset, particle, speed, amount);
    }

    public void playOutParticle(Location location, float offsetX, float offsetY, float offsetZ, MinerParticle particle,
            int amount) {
        playOutParticle(location, offsetX, offsetY, offsetZ, particle, 0, amount);
    }

    public void playOutParticle(Location location, float offsetX, float offsetY, float offsetZ, MinerParticle particle,
            float speed, int amount) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(particle.getBukkitParticle(), location, amount, offsetX, offsetY, offsetZ, speed);
        }
    }

    public void playOutParticle(Location location, Player player, float offset, MinerParticle particle, int amount) {
        playOutParticle(location, player, offset, offset, offset, particle, 0, amount);
    }

    public void playOutParticle(Location location, Player player, float offset, MinerParticle particle, float speed,
            int amount) {
        playOutParticle(location, player, offset, offset, offset, particle, speed, amount);
    }

    public void playOutParticle(Location location, Player player, float offsetX, float offsetY, float offsetZ,
            MinerParticle particle, int amount) {
        playOutParticle(location, player, offsetX, offsetY, offsetZ, particle, 0, amount);
    }

    public void playOutParticle(Location location, Player player, float offsetX, float offsetY, float offsetZ,
            MinerParticle particle, float speed, int amount) {
        player.spawnParticle(particle.getBukkitParticle(), location, amount, offsetX, offsetY, offsetZ, speed);
    }

    public void spawnRedstoneParticle(Location location, float red, float green, float blue, float size) {
        World world = location.getWorld();
        if (world != null) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(
                    Color.fromRGB((int) (red * 255), (int) (green * 255), (int) (blue * 255)), size);
            world.spawnParticle(Particle.REDSTONE, location, 50, dustOptions);
        }
    }

    public void spawnRedstoneParticle(Location location, Player player, float red, float green, float blue,
            float size) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(
                Color.fromRGB((int) (red * 255), (int) (green * 255), (int) (blue * 255)), size);
        player.spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
    }

    // ===== TITLES AND MESSAGES =====

    public void sendTitle(Player player, String title) {
        player.sendTitle(title, "", 5, 65, 15);
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int showTime, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, showTime, fadeOut);
    }

    public void sendActionBar(Player player, String text) {
        // Use Spigot API for action bar
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translate(text)));
    }

    // ===== WIN EFFECTS (Modern implementations) =====

    public void spawnIceWinEffect(Player player, Location location) {
        // Create fake ice block effect using particles
        player.spawnParticle(Particle.SNOWFLAKE, location.add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
        player.spawnParticle(Particle.CLOUD, location, 10, 0.3, 0.3, 0.3, 0.05);
    }

    public void updateIceWinEffect(Player player, Location location) {
        if (new Random().nextInt(5) == 2) {
            player.spawnParticle(Particle.SNOWFLAKE,
                    location.getX() + 0.5, location.getY() + 1.0, location.getZ() + 0.5,
                    1, 0.5, 0.5, 0.5, 0);
        }
    }

    public void updateBlocksWinEffect(Player player) {
        Location location = player.getLocation().add(0, -1, 0);
        if (new Random().nextInt(3) == 2) {
            player.spawnParticle(Particle.CLOUD, location.add(0, 1.5, 0), 3, 2, 0.5, 0.5, 0.5);
        }
    }

    public void updateFireWinEffect(Player player) {
        Location location = player.getLocation().clone();
        location = location.add(location.getDirection().setY(0).normalize().multiply(-1.5));

        if (new Random().nextInt(4) == 2) {
            player.spawnParticle(Particle.FLAME, location.add(0, 1.5, 0), 8, 0.5, 0.3, 0.5, 2.5);
        }
    }

    /**
     * Send restore packets to player
     */
    public void sendRestorePackets(Player player, Arena arena) {
        if (player == null || arena == null)
            return;
    }

    /**
     * Update explosion win effect
     */
    public void updateExplosionWinEffect(Player player, int time) {
        if (player == null)
            return;

        // Create explosion effect
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 10, 2, 2, 2, 0.1);
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Send countdown title
        player.sendTitle(translate("&c&lEXPLOSION!"), translate("&e" + time), 0, 20, 0);
    }

    /**
     * Update fire win effect
     */
    public void updateFireWinEffect(Player player, int time) {
        if (player == null)
            return;

        // Create fire effect
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 5, 0.3, 0.3, 0.3, 0.1);
        player.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);

        // Send fire title
        player.sendTitle(translate("&c&lFIRE!"), translate("&6" + time), 0, 20, 0);
    }

    /**
     * Update ice win effect
     */
    public void updateIceWinEffect(Player player, int time) {
        if (player == null)
            return;

        // Create water/ice effect
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 15, 1, 0.5, 1, 0.1);
        player.getWorld().spawnParticle(Particle.WATER_BUBBLE, loc, 10, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(loc, Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);

        // Send ice title
        player.sendTitle(translate("&b&lICE!"), translate("&9" + time), 0, 20, 0);
    }

    /**
     * Update rocket win effect
     */
    public void updateRocketWinEffect(Player player, int time) {
        if (player == null)
            return;

        // Create magic/rocket effect
        Location loc = player.getLocation().add(0, 2, 0);
        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 15, 1, 1, 1, 0.5);
        player.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);

        // Send rocket title
        player.sendTitle(translate("&a&lROCKET!"), translate("&2" + time), 0, 20, 0);
    }

    /**
     * Update blocks win effect
     */
    public void updateBlocksWinEffect(Player player, int time) {
        if (player == null)
            return;

        // Create pickup effect
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 5, 0.3, 0.3, 0.3, 0.1);
        player.getWorld().playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);

        // Send blocks title
        player.sendTitle(translate("&e&lBLOCKS!"), translate("&6" + time), 0, 20, 0);
    }

    // ===== PARTICLE ENUM =====

    public enum MinerParticle {
        ANGRY_VILLAGER(Particle.VILLAGER_ANGRY),
        BARRIER(Particle.BLOCK_MARKER),
        BUBBLE(Particle.BUBBLE_COLUMN_UP),
        CLOUD(Particle.CLOUD),
        CRIT(Particle.CRIT),
        EXPLODE(Particle.EXPLOSION_NORMAL),
        FLAME(Particle.FLAME),
        HAPPY_VILLAGER(Particle.VILLAGER_HAPPY),
        HEART(Particle.HEART),
        HUGE_EXPLOSION(Particle.EXPLOSION_HUGE),
        LARGE_EXPLOSION(Particle.EXPLOSION_LARGE),
        LARGE_SMOKE(Particle.SMOKE_LARGE),
        LAVA(Particle.LAVA),
        MAGIC_CRIT(Particle.CRIT_MAGIC),
        NOTE(Particle.NOTE),
        PORTAL(Particle.PORTAL),
        REDSTONE(Particle.REDSTONE),
        SMOKE(Particle.SMOKE_NORMAL),
        SNOWBALL(Particle.SNOWBALL);

        private final Particle bukkitParticle;

        MinerParticle(Particle bukkitParticle) {
            this.bukkitParticle = bukkitParticle;
        }

        public Particle getBukkitParticle() {
            return bukkitParticle;
        }

        public int getId() {
            return ordinal() + 1;
        }
    }
}
