package org.gr_code.minerware.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.gr_code.minerware.api.ModernAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modern hologram system using ArmorStands
 * Replaces the old NMS-based hologram system
 */
public class ModernHologram implements IHologram {
    private final List<ArmorStand> lines;
    private final Location location;
    private UUID owner;
    private boolean spawned = false;

    public ModernHologram(Location location) {
        this.location = location.clone();
        this.lines = new ArrayList<>();
    }

    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public void spawnAll() {
        if (!spawned) {
            spawned = true;
            // Holograms are spawned when lines are added
        }
    }

    @Override
    public void destroyAll() {
        clear();
        spawned = false;
    }

    @Override
    public void update() {
        // Update all existing lines (they're already updated when setLine is called)
        // This method exists for compatibility
    }

    /**
     * Add a line to the hologram
     */
    public void addLine(String text) {
        Location lineLocation = location.clone().add(0, -0.25 * lines.size(), 0);
        ArmorStand line = ModernAPI.createHologram(lineLocation, text);
        lines.add(line);
    }

    /**
     * Clear all lines from the hologram
     */
    public void clearLines() {
        for (ArmorStand line : lines) {
            ModernAPI.removeHologram(line);
        }
        lines.clear();
    }

    /**
     * Set text for a specific line
     */
    public void setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            ModernAPI.updateHologram(lines.get(index), text);
        }
    }

    /**
     * Remove a specific line
     */
    public void removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            ArmorStand line = lines.remove(index);
            ModernAPI.removeHologram(line);

            // Reposition remaining lines
            for (int i = index; i < lines.size(); i++) {
                Location newLocation = location.clone().add(0, -0.25 * i, 0);
                lines.get(i).teleport(newLocation);
            }
        }
    }

    /**
     * Clear all lines
     */
    public void clear() {
        for (ArmorStand line : lines) {
            ModernAPI.removeHologram(line);
        }
        lines.clear();
    }

    /**
     * Get number of lines
     */
    public int size() {
        return lines.size();
    }

    /**
     * Check if hologram is empty
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Get all lines
     */
    public List<ArmorStand> getLines() {
        return new ArrayList<>(lines);
    }

    /**
     * Move the entire hologram to a new location
     */
    public void teleport(Location newLocation) {
        this.location.setX(newLocation.getX());
        this.location.setY(newLocation.getY());
        this.location.setZ(newLocation.getZ());
        this.location.setWorld(newLocation.getWorld());

        for (int i = 0; i < lines.size(); i++) {
            Location lineLocation = location.clone().add(0, -0.25 * i, 0);
            lines.get(i).teleport(lineLocation);
        }
    }

    /**
     * Remove the entire hologram
     */
    public void remove() {
        clear();
    }
}
