package org.gr_code.minerware.api.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.gr_code.minerware.api.ModernAPI;

/**
 * Modern effects system using Bukkit API
 * Replaces NMS-based particle and sound effects
 */
public class ModernEffects {

    /**
     * Create explosion effect
     */
    public static void createExplosionEffect(Location location) {
        // Explosion particles
        ModernAPI.spawnParticle(Particle.EXPLOSION_LARGE, location, 1);
        ModernAPI.spawnParticle(Particle.EXPLOSION_NORMAL, location, 10, 2, 2, 2, 0.1);

        // Sound effect
        ModernAPI.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }

    /**
     * Create win effect for player
     */
    public static void createWinEffect(Player player) {
        Location location = player.getLocation();

        // Golden particles around player
        ModernAPI.spawnParticle(Particle.FIREWORKS_SPARK, location.add(0, 1, 0), 20, 1, 1, 1, 0.1);
        ModernAPI.spawnParticle(Particle.VILLAGER_HAPPY, location, 15, 1, 1, 1, 0.1);

        // Victory sound
        ModernAPI.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        ModernAPI.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Create teleport effect
     */
    public static void createTeleportEffect(Location location) {
        ModernAPI.spawnParticle(Particle.PORTAL, location, 50, 1, 2, 1, 0.5);
        ModernAPI.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * Create death effect
     */
    public static void createDeathEffect(Location location) {
        ModernAPI.spawnParticle(Particle.SMOKE_LARGE, location, 10, 0.5, 0.5, 0.5, 0.1);
        ModernAPI.spawnParticle(Particle.LAVA, location, 5, 0.3, 0.3, 0.3, 0.1);
        ModernAPI.playSound(location, Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
    }

    /**
     * Create pickup effect
     */
    public static void createPickupEffect(Location location) {
        ModernAPI.spawnParticle(Particle.VILLAGER_HAPPY, location, 5, 0.3, 0.3, 0.3, 0.1);
        ModernAPI.playSound(location, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
    }

    /**
     * Create magic effect
     */
    public static void createMagicEffect(Location location) {
        ModernAPI.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 15, 1, 1, 1, 0.5);
        ModernAPI.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
    }

    /**
     * Create fire effect
     */
    public static void createFireEffect(Location location) {
        ModernAPI.spawnParticle(Particle.FLAME, location, 10, 0.5, 0.5, 0.5, 0.1);
        ModernAPI.spawnParticle(Particle.SMOKE_NORMAL, location, 5, 0.3, 0.3, 0.3, 0.1);
        ModernAPI.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * Create water effect
     */
    public static void createWaterEffect(Location location) {
        ModernAPI.spawnParticle(Particle.WATER_SPLASH, location, 15, 1, 0.5, 1, 0.1);
        ModernAPI.spawnParticle(Particle.WATER_BUBBLE, location, 10, 0.5, 0.5, 0.5, 0.1);
        ModernAPI.playSound(location, Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * Create countdown effect
     */
    public static void createCountdownEffect(Player player, int number) {
        Location location = player.getLocation().add(0, 2, 0);

        // Number-based particle color
        Particle particle = number <= 3 ? Particle.REDSTONE : Particle.VILLAGER_HAPPY;
        ModernAPI.spawnParticle(particle, location, 10, 0.5, 0.5, 0.5, 0.1);

        // Countdown sound
        Sound sound = number <= 3 ? Sound.BLOCK_NOTE_BLOCK_BASS : Sound.BLOCK_NOTE_BLOCK_PLING;
        float pitch = 0.5f + (number * 0.2f);
        ModernAPI.playSound(player, sound, 1.0f, pitch);
    }
}
