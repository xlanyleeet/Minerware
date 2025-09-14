package org.gr_code.minerware.cuboid;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class WorldGenerator extends ChunkGenerator {
    private final List<BlockPopulator> blockPopulation = Collections.emptyList();
    private final byte[] bytes = new byte[32768];

    public WorldGenerator() {
    }

    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 100.0, 100.0, 100.0);
    }

    public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int x, int z,
            ChunkGenerator.BiomeGrid biome) {
        ChunkGenerator.ChunkData chunkData = this.createChunkData(world);
        chunkData.setRegion(0, 0, 0, 16, 16, 16, Material.AIR);
        return chunkData;
    }

    public List<BlockPopulator> getDefaultPopulators(World paramWorld) {
        return this.blockPopulation;
    }

    public byte[] generate(World paramWorld, Random paramRandom, int x, int z) {
        return this.bytes;
    }
}
