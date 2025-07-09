package dev.jaronline.simplyhopper.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RedStoneWireBlock extends net.minecraft.world.level.block.RedStoneWireBlock {
    public RedStoneWireBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return this.canSurviveOn(pLevel, blockpos, blockstate);
    }

    private boolean canSurviveOn(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return pState.isFaceSturdy(pLevel, pPos, Direction.UP) || pState.is(Blocks.HOPPER) || pState.is(BlockRegistry.SIMPLY_HOPPER_BLOCK.getBlock());
    }
}
