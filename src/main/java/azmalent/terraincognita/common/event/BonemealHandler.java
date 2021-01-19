package azmalent.terraincognita.common.event;

import azmalent.cuneiform.lib.util.BiomeUtil;
import azmalent.terraincognita.TIConfig;
import azmalent.terraincognita.common.block.plants.SmallLilypadBlock;
import azmalent.terraincognita.common.init.ModBlocks;
import azmalent.terraincognita.common.world.ModVegetation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;

import java.util.Random;

import static azmalent.cuneiform.lib.util.BiomeUtil.hasAnyType;
import static net.minecraftforge.common.BiomeDictionary.Type.*;
import static net.minecraftforge.common.BiomeDictionary.hasType;

public class BonemealHandler {
    private static final int MAX_LILYPAD_GROWING_DEPTH = 4;

    public static void registerListeners() {
        if (TIConfig.Tweaks.bonemealLilypadGrowing.get()) {
            MinecraftForge.EVENT_BUS.addListener(BonemealHandler::onBonemealUnderwater);
        }
    }

    private static boolean isInShallowWater(World world, BlockPos pos) {
        if (world.getFluidState(pos.up()).getFluid() != Fluids.WATER) return false;

        for (int i = 2; i < MAX_LILYPAD_GROWING_DEPTH + 1; i++) {
            if (world.getFluidState(pos.up(i)).getFluid() != Fluids.WATER) {
                return world.isAirBlock(pos.up(i));
            }
        }

        return false;
    }

    private static void placeRandomLilypad(World world, BlockPos pos, boolean isJungle) {
        BlockState blockState;
        Random rand = world.getRandom();
        Float f = rand.nextFloat();

        if (f < 0.25 && isJungle && TIConfig.Flora.lotus.get()) {
            blockState = ModBlocks.LOTUSES.get(rand.nextInt(3)).getBlock().getDefaultState();
        } else if (f < 0.5 && !isJungle && TIConfig.Flora.smallLilypad.get()) {
            blockState = ModBlocks.SMALL_LILYPAD.getBlock().getDefaultState().with(SmallLilypadBlock.LILYPADS, 1 + rand.nextInt(4));
        }
        else blockState = Blocks.LILY_PAD.getDefaultState();

        world.setBlockState(pos, blockState);
    }

    public static void onBonemealUnderwater(BonemealEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        //Don't grow lilypads when bonemealing growable plants
        if (world.getBlockState(pos).getBlock() instanceof IGrowable) {
            return;
        }

        RegistryKey<Biome> biome = BiomeUtil.getBiomeKey(world, pos);
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)) {
            return;
        }

        if (isInShallowWater(world, pos)) {
            boolean isJungle = BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE);
            Random rand = world.getRandom();
            for (int i = 0; i < 4; i++) {
                int x = rand.nextInt(4) - rand.nextInt(4);
                int y = rand.nextInt(MAX_LILYPAD_GROWING_DEPTH) + 2;
                int z = rand.nextInt(4) - rand.nextInt(4);

                BlockPos lilypadPos = pos.add(x, y, z);
                if (world.isAirBlock(lilypadPos) && world.getFluidState(lilypadPos.down()).getFluid() == Fluids.WATER) {
                    placeRandomLilypad(world, lilypadPos, isJungle);
                }
            }
        }
    }
}
