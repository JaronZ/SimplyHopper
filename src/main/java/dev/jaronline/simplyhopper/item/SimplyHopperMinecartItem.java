package dev.jaronline.simplyhopper.item;

import dev.jaronline.simplyhopper.entity.SimplyHopperMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class SimplyHopperMinecartItem extends MinecartItem {
    public SimplyHopperMinecartItem(Properties properties) {
        super(AbstractMinecart.Type.HOPPER, properties);
        DispenserBlock.registerBehavior(this, new SimplyHopperMinecartItem.DispenseBehavior());
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("item.simplyhopper.simply_hopper_minecart");
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            RailShape shape = state.getBlock() instanceof BaseRailBlock
                    ? ((BaseRailBlock) state.getBlock()).getRailDirection(state, level, pos, null)
                    : RailShape.NORTH_SOUTH;

            double yOffset = shape.isAscending() ? 0.5D : 0.0D;

            SimplyHopperMinecartEntity cart = new SimplyHopperMinecartEntity(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.0625D + yOffset,
                    pos.getZ() + 0.5D
            );

            if (stack.hasCustomHoverName()) {
                cart.setCustomName(stack.getHoverName());
            }

            level.addFreshEntity(cart);
            level.gameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Context.of(context.getPlayer(), state));
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static class DispenseBehavior extends DefaultDispenseItemBehavior {
        @Override
        public @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            Level level = source.level();
            BlockPos pos = source.pos().relative(direction);
            BlockState state = level.getBlockState(pos);

            RailShape shape = RailShape.NORTH_SOUTH;
            if (state.getBlock() instanceof BaseRailBlock) {
                shape = ((BaseRailBlock) state.getBlock()).getRailDirection(state, level, pos, null);
            }

            double x = pos.getX() + 0.5D;
            double y = pos.getY() + (shape.isAscending() ? 0.6D : 0.1D);
            double z = pos.getZ() + 0.5D;

            if (!state.is(BlockTags.RAILS) && !level.getBlockState(pos.below()).is(BlockTags.RAILS)) {
                return super.dispense(source, stack);
            }

            if (!level.isClientSide) {
                SimplyHopperMinecartEntity cart = new SimplyHopperMinecartEntity(level, x, y, z);
                if (stack.hasCustomHoverName()) {
                    cart.setCustomName(stack.getHoverName());
                }
                level.addFreshEntity(cart);
            }

            stack.shrink(1);
            return stack;
        }

    }
}
