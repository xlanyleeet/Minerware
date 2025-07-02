package org.gr_code.minerware.api;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Modern API replacement for NMS functionality
 * Uses only Bukkit/Spigot API for better compatibility
 */
public class ModernAPI {

    /**
     * Set entity AI disabled
     */
    public static void setNoAI(Entity entity) {
        if (entity instanceof org.bukkit.entity.Mob) {
            ((org.bukkit.entity.Mob) entity).setAI(false);
        }
    }

    /**
     * Get block type ID (deprecated in modern versions)
     */
    @Deprecated
    public static int getTypeId(Block block) {
        return block.getType().ordinal();
    }

    /**
     * Set block type from ItemStack
     */
    public static void setBlock(ItemStack baseItem, Block block) {
        block.setType(baseItem.getType());
    }

    /**
     * Set block type by ID and data (legacy)
     */
    @Deprecated
    public static void setBlock(int id, byte data, Block block) {
        // Modern approach - use Material enum
        Material[] materials = Material.values();
        if (id >= 0 && id < materials.length) {
            block.setType(materials[id]);
        }
    }

    /**
     * Spawn particles at location
     */
    public static void spawnParticle(Particle particle, Location location, int count) {
        location.getWorld().spawnParticle(particle, location, count);
    }

    /**
     * Spawn particles with extra data
     */
    public static void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
    }

    /**
     * Spawn particles with speed
     */
    public static void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double speed) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    /**
     * Play sound for player
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Play sound at location
     */
    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    /**
     * Send title to player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Send action bar to player
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    /**
     * Create hologram (armor stand based)
     */
    public static ArmorStand createHologram(Location location, String text) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
        armorStand.setCustomNameVisible(true);
        armorStand.setAI(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        return armorStand;
    }

    /**
     * Update hologram text
     */
    public static void updateHologram(ArmorStand hologram, String text) {
        hologram.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
    }

    /**
     * Remove hologram
     */
    public static void removeHologram(ArmorStand hologram) {
        hologram.remove();
    }

    /**
     * Check if item stacks are similar
     */
    public static boolean equalsItemStack(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.isSimilar(itemStack2);
    }

    /**
     * Get server version
     */
    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Check if version is legacy (pre-1.13)
     */
    public static boolean isLegacy() {
        String version = getServerVersion();
        return version.contains("1_8") || version.contains("1_9") ||
                version.contains("1_10") || version.contains("1_11") || version.contains("1_12");
    }

    /**
     * Set up button direction (modern approach)
     */
    public static void setUpDirectionButton(Block block) {
        if (block.getType() == Material.STONE_BUTTON || block.getType() == Material.OAK_BUTTON) {
            // Modern buttons handle direction automatically
            // No need for manual direction setting
        }
    }
}
