package org.gr_code.minerware.api.hologram;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Interface for hologram management
 * Replacement for the old NMS-based IHologram
 */
public interface IHologram {

    /**
     * Add a line to the hologram
     * 
     * @param text The text to add
     */
    void addLine(String text);

    /**
     * Clear all lines from the hologram
     */
    void clearLines();

    /**
     * Set text for a specific line
     * 
     * @param index The line index
     * @param text  The new text
     */
    void setLine(int index, String text);

    /**
     * Remove a specific line
     * 
     * @param index The line index to remove
     */
    void removeLine(int index);

    /**
     * Clear all lines
     */
    void clear();

    /**
     * Get number of lines
     * 
     * @return Number of lines
     */
    int size();

    /**
     * Check if hologram is empty
     * 
     * @return True if empty
     */
    boolean isEmpty();

    /**
     * Move the hologram to a new location
     * 
     * @param location New location
     */
    void teleport(Location location);

    /**
     * Remove the entire hologram
     */
    void remove();

    /**
     * Set owner of the hologram
     * 
     * @param uuid Owner UUID
     */
    void setOwner(UUID uuid);

    /**
     * Spawn all hologram parts
     */
    void spawnAll();

    /**
     * Destroy all hologram parts
     */
    void destroyAll();

    /**
     * Update hologram display
     */
    void update();
}
