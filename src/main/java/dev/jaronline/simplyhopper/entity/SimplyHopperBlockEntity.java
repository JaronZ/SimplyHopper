package dev.jaronline.simplyhopper.entity;

import com.mojang.logging.LogUtils;
import dev.jaronline.simplyhopper.block.entity.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

import static net.minecraft.world.level.block.entity.HopperBlockEntity.*;


// slottedItems can have a stacksize of 1.
// items have their normal stacksize and a partitioned slot of (slottedItems.capacity * stacksize / slottedItems.count)
// the simplyhopper can get an input as a normal hopper, but checks if the input is in the slottedItems.
// the simplyhopper will only work if it has an output container.
// if the simplyhopper has items in its container and no output container, it will drop its contents.
// if the output container of simplyhopper is another simplyhopper, it will not work.
// if the simplyhopper receives a redstone signal, it will lock the item input but not the item output.
// the slotted items are separate containers, as in you will only fill 1 slot when shift clicking a stack of items, and not all 5 slots. (kinda like a furnace)
// if the simplyhopper has 1 slotted item and others are null, it will only allow the slotted item
// if the simplyhopper has no slotted items, it will allow all items

public class SimplyHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_COUNT = 5;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int cooldownTime = -1;
    private long tickedGameTime;

    public SimplyHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.SIMPLY_HOPPER_BLOCK_ENTITY.get(),pos, state);
    }

    /**
     * Our custom tick logic — run this every tick instead of vanilla logic
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, SimplyHopperBlockEntity hopper) {
        if (level.isClientSide) return;

        // TODO: Fix this hopper can do push and pull at the same time.
        // TODO: Fix this hopper will pull first and push last if simply hopper is above.
        // TODO: Fix this hopper will pull last and push first if hopper (vanilla/custom) above has output direction towards this hopper.

        // This is so it will act the same as a vanilla hopper.
        // This is so the simplyhopper can be used to filter from other simplyhoppers.
        // This is so the inventory updates correctly and remains the correct amount of items, needed for the filter.

        --hopper.cooldownTime;
        hopper.tickedGameTime = level.getGameTime();
        if (!hopper.isOnCooldown()) {
            hopper.setCooldown(0);
            tryMoveItems(level, pos, state, hopper, () -> tryPullItems(level,hopper));
        }
    }

    private static void tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState, SimplyHopperBlockEntity pBlockEntity, BooleanSupplier pValidator) {
        if (pLevel.isClientSide) {return;}

        // TODO: Fix this hopper can do push and pull at the same time.
        // TODO: Fix this hopper will pull first and push last if simply hopper is above.
        // TODO: Fix this hopper will pull last and push first if hopper (vanilla/custom) above has output direction towards this hopper.

        if (!pBlockEntity.isOnCooldown() && pState.getValue(HopperBlock.ENABLED)) {
            boolean flag = false;

            if (!pBlockEntity.isEmpty()) {
                flag = tryPushItems(pLevel, pBlockEntity);
            }
            if (!pBlockEntity.inventoryFull()) {
                flag |= pValidator.getAsBoolean();
            }

//            if(getSourceContainer(pLevel, pBlockEntity) instanceof HopperBlockEntity sourceHopper) {
//                if(sourceHopper.getBlockState().getValue(HopperBlock.FACING) == Direction.DOWN) {
//                    if (!pBlockEntity.isEmpty()) {
//                        flag = tryPushItems(pLevel, pBlockEntity);
//                    }
//                    if (!pBlockEntity.inventoryFull()) {
//                        flag |= pValidator.getAsBoolean();
//                    }
//                }
//                else{
//                    if (!pBlockEntity.inventoryFull()) {
//                        flag |= pValidator.getAsBoolean();
//                    }
//
//                    if (!pBlockEntity.isEmpty()) {
//                        flag = tryPushItems(pLevel, pBlockEntity);
//                    }
//                }
//            }
//            else{
//                if (!pBlockEntity.isEmpty()) {
//                    flag = tryPushItems(pLevel, pBlockEntity);
//                }
//
//                if (!pBlockEntity.inventoryFull()) {
//                    flag |= pValidator.getAsBoolean();
//                }
//            }

            if (flag) {
                pBlockEntity.setCooldown(8);
                setChanged(pLevel, pPos, pState);
            }
        }
    }

    private int getMinimalStackSize() {
        // This can be adjusted based on your requirements
        return 2; // Minimum stack size to consider for pushing
    }

    /**
     * PUSH logic: only push out items if there's more than one
     */
    private static boolean tryPushItems(Level level, SimplyHopperBlockEntity hopper) {
        Direction outDir = hopper.getBlockState().getValue(HopperBlock.FACING);
        BlockPos outPos = hopper.getBlockPos().relative(outDir);
        BlockEntity targetBE = level.getBlockEntity(outPos);

        LOGGER.info("Trying to push items from {} to {}", hopper.getBlockPos(), outPos);
        System.out.println("Trying to push items from "+  hopper.getBlockPos()+" to "+outPos);
        if (targetBE == null) {
            LOGGER.warn("No target block entity found at {}", outPos);
            return false;
        }
        if (targetBE instanceof Container targetInv) {
            for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
                ItemStack stackInSlot = hopper.getItem(slot);

                LOGGER.info("Checking slot {}: {}, and if it is > {}", slot, stackInSlot, hopper.getMinimalStackSize());
                System.out.println("Checking slot "+slot+": "+stackInSlot+", and if it is > "+hopper.getMinimalStackSize());

                LOGGER.info(targetBE instanceof SimplyHopperBlockEntity ? "Target is a Simply Hopper" : "Target is a regular container");
                System.out.println(targetBE instanceof SimplyHopperBlockEntity ? "Target is a Simply Hopper" : "Target is a regular container");

//                if (targetBE instanceof SimplyHopperBlockEntity otherSimplyHopper) {
//                    // If the target is another Simply Hopper, we can only push items that match existing contents
//                    if (!otherSimplyHopper.canPlaceItem(stackInSlot)) {
//                        continue; // Skip if the item doesn't match
//                    }
//                }

                if (!stackInSlot.isEmpty() && targetInv.canTakeItem(hopper,slot,stackInSlot) && stackInSlot.getCount() >= hopper.getMinimalStackSize()) {
                    ItemStack toTransfer = stackInSlot.copy();
                    toTransfer.setCount(1);

                    LOGGER.info("Attempting to transfer {} to {}", toTransfer, targetInv);
                    System.out.println("Attempting to transfer "+toTransfer+" to "+targetInv);

                    ItemStack remaining = addItem(hopper, targetInv, toTransfer, outDir.getOpposite());
                    if (remaining.isEmpty()) {
                        LOGGER.info("Successfully transferred {} to {}", toTransfer, targetInv);
                        System.out.println("Successfully transferred "+toTransfer+" to "+targetInv);
                        stackInSlot.shrink(1);
                        hopper.setChanged();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * PULL logic: only pull items that match existing contents
     */
    private static boolean tryPullItems(Level level, SimplyHopperBlockEntity hopper) {
        if (hopper.isOnCooldown() || hopper.inventoryFull()) {
            return false; // Don't pull if on cooldown or inventory is full
        }

        LOGGER.debug("Trying to pull items from source container at {}", hopper.getBlockPos());
        System.out.println("Trying to pull items from source container at "+hopper.getBlockPos());

        Container source = SimplyHopperBlockEntity.getSourceContainer(level, hopper);
        if (source == null) {
            // Try picking up item entities on top of the hopper
            return hopper.tryPullItemEntities(level);
        }

        Direction pullDir = Direction.DOWN;

        if(!source.hasAnyMatching(stack -> !stack.isEmpty())) {
            return false; // No items to pull
        }

        for (int slot = 0; slot < source.getContainerSize(); slot++) {
            ItemStack stackInSource = source.getItem(slot);

            if (!stackInSource.isEmpty() && hopper.canPlaceItem(stackInSource) && source.canTakeItem(hopper, slot, stackInSource)) {
                ItemStack toTransfer = stackInSource.copy();
                toTransfer.setCount(1);

                LOGGER.debug("Trying to pull {} as {}", stackInSource, toTransfer);
                System.out.println("Trying to pull "+stackInSource+" as "+toTransfer);

                ItemStack remaining = addItem(source, hopper, toTransfer, pullDir);
                if (remaining.isEmpty()) {
                    stackInSource.shrink(1);
                    source.setChanged();
                    return true;
                }
            }
        }

        return false;
    }

    public static void entityInside(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, SimplyHopperBlockEntity pBlockEntity) {
        if (pEntity instanceof ItemEntity itementity) {
            if (!itementity.getItem().isEmpty() && Shapes.joinIsNotEmpty(Shapes.create(pEntity.getBoundingBox().move((double)(-pPos.getX()), (double)(-pPos.getY()), (double)(-pPos.getZ()))), pBlockEntity.getSuckShape(), BooleanOp.AND)) {
                tryMoveItems(pLevel, pPos, pState, pBlockEntity, () -> addItem(pBlockEntity, itementity));
            }
        }
    }

    @Override
    public boolean canTakeItem(@NotNull Container pTarget, int pIndex, @NotNull ItemStack pStack) {
        return acceptTakeAttempt(pTarget,pIndex,pStack);
    }


    public boolean canTakeItem(Container pTarget, ItemStack pStack) {
        if (pTarget == null || pStack.isEmpty()) {
            return false; // No target or empty stack
        }
        for (int i = 0; i < this.getContainerSize(); i++) {
            if(acceptTakeAttempt(pTarget, i, pStack)){
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param pIndex
     * @param pStack
     * @return
     */
    public boolean acceptTakeAttempt(Container pTarget, int pIndex, @NotNull ItemStack pStack) {
        if (pTarget == null || pStack.isEmpty()) {
            return false; // No target or empty stack
        }
        if(pTarget instanceof SimplyHopperBlockEntity targetBE) {
            if(pStack.getCount() >= targetBE.getMinimalStackSize() && ItemStack.isSameItemSameTags(pStack, pTarget.getItem(pIndex))) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int pIndex, @NotNull ItemStack pStack) {
        return canAccept(pIndex,pStack);
    }

    public boolean canPlaceItem(@NotNull ItemStack pStack) {
        return canAccept(pStack);
    }

    /**
     * Check if hopper inventory already contains this item type
     */
    private boolean canAccept(ItemStack stack) {
        if (stack.isEmpty()) {
        return false; // Don't accept empty stacks
    }

        for (int i = 0; i < this.getContainerSize(); i++) {
            if(canAccept(i, stack)){
                return true;
            }
        }
        return false; // If no slots match, return false
    }
    private boolean canAccept(int pIndex,ItemStack stack) {
        if (stack.isEmpty()) {
            return false; // Don't accept empty stacks
        }

//        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack slotStack = this.getItem(pIndex);

            LOGGER.info("Checking slot {}: {} for acceptance of {}",slotStack,ItemStack.isSameItemSameTags(slotStack, stack),stack);
            System.out.println("Checking slot "+pIndex+": "+slotStack+" for acceptance of "+stack);

            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, stack) && slotStack.getCount() < stack.getMaxStackSize()) {
                LOGGER.info("Accepted {} in slot {}", stack, pIndex);
                System.out.println("Accepted "+stack+" in slot "+pIndex);
                return true;
            }
//        }
        return false;
    }

    /**
     * Fallback: Try pulling nearby loose items
     */
    protected boolean tryPullItemEntities(Level level) {
        List<ItemEntity> itemsAndAbove = getItemsAtAndAbove(level, this);
        for (ItemEntity itemEntity : itemsAndAbove) {
            ItemStack entityStack = itemEntity.getItem();

            if (canPlaceItem(itemsAndAbove.indexOf(itemEntity),entityStack)) {
                ItemStack remaining = addItem(new SimpleContainer(entityStack), this, entityStack.copy(), Direction.DOWN);

                if (remaining.isEmpty()) {
                    itemEntity.discard();
                    return true;
                } else {
                    entityStack.setCount(remaining.getCount());
                }
            }
        }

        return false;
    }

    @Nullable
    private static Container getSourceContainer(Level pLevel, SimplyHopperBlockEntity pHopper) {
        BlockPos hopperPos = pHopper.getBlockPos().relative(Direction.Axis.Y, (int) 1.0D);
        return getContainerAt(pLevel, hopperPos);
    }

    private boolean inventoryFull() {
        for(ItemStack itemstack : this.getItems()) {
            if (itemstack.isEmpty() || itemstack.getCount() < itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private void setCooldown(int ticks) {
        cooldownTime = ticks;
    }

    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }

@Override
public boolean isEmpty() {
    for (ItemStack stack : items) {
        if (!stack.isEmpty()) return false;
    }
    return true;
}

@Override
protected @NotNull NonNullList<ItemStack> getItems() {
    return items;
}

@Override
protected void setItems(@NotNull NonNullList<ItemStack> items) {
    this.items = items;
}

@Override
protected @NotNull Component getDefaultName() {
    return Component.translatable("block.simplyhopper.simply_hopper");
}

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory) {
        return new HopperMenu(id, playerInventory, this);
    }

@Override
public int getContainerSize() {
    return SLOT_COUNT;
}

@Override
public void load(@NotNull CompoundTag tag) {
    super.load(tag);
    this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    ContainerHelper.loadAllItems(tag, this.items);
    this.cooldownTime = tag.getInt("Cooldown");
}

@Override
protected void saveAdditional(@NotNull CompoundTag tag) {
    super.saveAdditional(tag);
    ContainerHelper.saveAllItems(tag, this.items);
    tag.putInt("Cooldown", this.cooldownTime);
}

    public @NotNull ItemStack removeItem(int pIndex, int pCount) {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
    }

    public void setItem(int pIndex, ItemStack pStack) {
        this.unpackLootTable((Player)null);
        this.getItems().set(pIndex, pStack);
        if (pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }

    }

    /**
     * Gets the world X position for this hopper entity.
     */
    @Override
    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5D;
    }

    /**
     * Gets the world Y position for this hopper entity.
     */
    @Override
    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5D;
    }

    /**
     * Gets the world Z position for this hopper entity.
     */
    @Override
    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5D;
    }
}
