package io.github.lucaargolo.seasons.utils;

import io.github.lucaargolo.seasons.FabricSeasons;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

public interface Meltable {

    TagKey<Block> REPLACEABLE_BY_SNOW = TagKey.of(RegistryKeys.BLOCK, new ModIdentifier("replaceable_by_snow"));

    default void onMeltableReplaced(ServerWorld world, BlockPos pos) {
        FabricSeasons.getPlacedMeltablesState(world).setManuallyPlaced(pos, false);
        FabricSeasons.getReplacedMeltablesState(world).setReplaced(pos, null);
    }

    default void onMeltableManuallyPlaced(ServerWorld world, BlockPos pos) {
        FabricSeasons.getPlacedMeltablesState(world).setManuallyPlaced(pos, true);
    }

    static void replaceBlockOnSnow(ServerWorld world, BlockPos blockPos, Biome biome) {
        BlockState plantState = world.getBlockState(blockPos);
        if (plantState.isIn(REPLACEABLE_BY_SNOW) || plantState.isOf(Blocks.SNOW_BLOCK)) { // Check for replaceable by snow or ice block
            if (!biome.doesNotSnow(blockPos) && blockPos.getY() >= world.getBottomY() && blockPos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, blockPos) < 10) {
                BlockState upperState = world.getBlockState(blockPos.up());
                if (plantState.getProperties().contains(TallPlantBlock.HALF) && upperState.getProperties().contains(TallPlantBlock.HALF)) {
                    if (upperState.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                        FabricSeasons.setMeltable(blockPos);
                        FabricSeasons.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                        world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), Block.FORCE_STATE);
                        world.setBlockState(blockPos.up(), Blocks.AIR.getDefaultState());
                        Blocks.SNOW.getDefaultState().updateNeighbors(world, blockPos, Block.NOTIFY_ALL);
                        world.updateListeners(blockPos, plantState, Blocks.SNOW.getDefaultState(), Block.NOTIFY_ALL);
                    }
                } else if (upperState.isAir()) {
                    FabricSeasons.setMeltable(blockPos);
                    FabricSeasons.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                    world.setBlockState(blockPos, Blocks.SNOW.getDefaultState());
                } else if (plantState.isOf(Blocks.SNOW_BLOCK)) { // Handle melting of ice block
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}
