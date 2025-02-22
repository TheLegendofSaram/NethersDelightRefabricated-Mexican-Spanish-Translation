package umpaz.nethersdelight.common.block;

import io.github.fabricators_of_create.porting_lib.common.util.IPlantable;
import io.github.fabricators_of_create.porting_lib.common.util.PlantType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.nethersdelight.common.registry.NDBlocks;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.utility.MathUtils;

public class RichSoulSoilBlock extends Block {
    public RichSoulSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            BlockPos abovePos = pos.above();
            BlockPos belowPos = pos.below();
            BlockState aboveState = level.getBlockState(abovePos);
            BlockState belowState = level.getBlockState(belowPos);
            Block aboveBlock = aboveState.getBlock();
            Block belowBlock = belowState.getBlock();

            // Convert mushrooms to colonies if it's dark enough
            if (aboveBlock == Blocks.CRIMSON_FUNGUS) {
                level.setBlockAndUpdate(pos.above(), NDBlocks.CRIMSON_FUNGUS_COLONY.get().defaultBlockState());
                return;
            }
            if (aboveBlock == Blocks.WARPED_FUNGUS) {
                level.setBlockAndUpdate(pos.above(), NDBlocks.WARPED_FUNGUS_COLONY.get().defaultBlockState());
                return;
            }
            if (level.isEmptyBlock(abovePos) && level.getMaxLocalRawBrightness(abovePos) < 1 && MathUtils.RAND.nextInt(50) == 0) {
                level.setBlockAndUpdate(pos.above(), NDBlocks.MIMICARNATION.get().defaultBlockState());
                return;
            }

            if (Configuration.RICH_SOIL_BOOST_CHANCE.get() == 0.0) {
                return;
            }

            if ((aboveBlock instanceof TwistingVinesBlock || aboveBlock instanceof TwistingVinesPlantBlock) && MathUtils.RAND.nextFloat() <= (Configuration.RICH_SOIL_BOOST_CHANCE.get() * 0.3F)) {
                BonemealableBlock growable = (BonemealableBlock) aboveBlock;
                if (growable.isValidBonemealTarget(level, pos.above(), aboveState, false)) {
                    growable.performBonemeal(level, level.random, pos.above(), aboveState);
                    level.levelEvent(2005, pos.above(), 0);
                }
                return;
            }
            if ((belowBlock instanceof WeepingVinesBlock || belowBlock instanceof WeepingVinesPlantBlock) && MathUtils.RAND.nextFloat() <= (Configuration.RICH_SOIL_BOOST_CHANCE.get() * 0.3F)) {
                BonemealableBlock growable = (BonemealableBlock) belowBlock;
                    if (growable.isValidBonemealTarget(level, pos.below(), belowState, false)) {
                        growable.performBonemeal(level, level.random, pos.below(), belowState);
                        level.levelEvent(2005, pos.below(), 0);
                    }
                    return;
            }
            if (aboveBlock == Blocks.NETHER_WART && MathUtils.RAND.nextFloat() <= (Configuration.RICH_SOIL_BOOST_CHANCE.get() * 0.8F)) {
                int age = aboveState.getValue(NetherWartBlock.AGE);
                if (age < NetherWartBlock.MAX_AGE) {
                    level.setBlockAndUpdate(pos.above(), aboveState.setValue(NetherWartBlock.AGE, age + 1));
                    level.levelEvent(2005, pos.above(), 0);
                }
            }
            if (aboveBlock == NDBlocks.PROPELPLANT_CANE.get() && MathUtils.RAND.nextFloat() <= (Configuration.RICH_SOIL_BOOST_CHANCE.get())) {
                BlockPos topPos = pos;
                while (level.getBlockState(topPos.above()).getBlock() instanceof PropelplantCaneBlock) {
                    topPos = topPos.above();
                }
                BlockState topState = level.getBlockState(topPos);

                if (!topState.getValue(PropelplantCaneBlock.PEARL)) {
                    level.setBlockAndUpdate(topPos, topState.setValue(PropelplantCaneBlock.PEARL, true));
                    level.levelEvent(2005, topPos, 0);
                }
            }
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        PlantType plantType = plantable.getPlantType(level, pos.relative(facing));
        if (plantType == PlantType.NETHER) return true;

        BlockState plant = plantable.getPlant(level, pos.relative(facing));
        Block plantBlock = plant.getBlock();
        if (plantBlock instanceof RootsBlock) return true;
        if (plantBlock instanceof FungusBlock) return true;
        if (plantBlock instanceof NetherSproutsBlock) return true;

        return false;
    }
}