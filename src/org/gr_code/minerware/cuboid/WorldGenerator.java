package org.gr_code.minerware.cuboid;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Random;


@SuppressWarnings("NullableProblems")
public class WorldGenerator extends ChunkGenerator {

    private final List<BlockPopulator> blockPopulation = Collections.emptyList();

    private final byte[] bytes = new byte[32768];

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 100, 100, 100);
    }

    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        chunkData.setRegion(0, 0, 0, 16, 16, 16, Material.AIR);
        return chunkData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World paramWorld) {
        return this.blockPopulation;
    }

    public byte[] generate(World paramWorld, Random paramRandom, int x, int z) {
        return this.bytes;
    }
}


