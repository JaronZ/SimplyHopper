package dev.jaronline.simplyhopper.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import dev.jaronline.simplyhopper.gui.SimplyHopperMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.Collections.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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

public class SimplyHopperBlockEntity extends HopperBlockEntity {
    private static final int SLOTTED_ITEMS_SIZE = 5;
    private static final Logger LOGGER = LogUtils.getLogger();
    private NonNullList<ItemStack> slottedItems = NonNullList.withSize(SLOTTED_ITEMS_SIZE, ItemStack.EMPTY);
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY); // Internal storage
    private int cooldownTime = -1;
    private long tickedGameTime;
    private boolean isLocked = false;
    private static final int BASE_STACK_SIZE = 64;
    private boolean isPowered = false;

    public SimplyHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public static void pushItemsTick(Level pLevel, BlockPos pPos, BlockState pState, SimplyHopperBlockEntity pBlockEntity) {
        --pBlockEntity.cooldownTime;
        pBlockEntity.tickedGameTime = pLevel.getGameTime();
        Container container = getAttachedContainer(pLevel, pPos, pState);

        LOGGER.debug("Container: {} {}", container, container instanceof SimplyHopperBlockEntity);

        if(container == null || container instanceof SimplyHopperBlockEntity) {

            LOGGER.debug("Container arrived to drop contents");
            Containers.dropContents(pLevel, pPos, pBlockEntity);
            pLevel.setBlock(pPos,pState.setValue(HopperBlock.ENABLED, Boolean.FALSE),1);
        }

        if (!pBlockEntity.isOnCooldown()) {
            pBlockEntity.setCooldown(0);
            tryMoveItems(pLevel, pPos, pState, pBlockEntity, () -> suckInItems(pLevel, pBlockEntity));
        }
    }

    private static boolean ejectItems(Level pLevel, BlockPos pPos, BlockState pState, SimplyHopperBlockEntity pSourceContainer) {
        if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(pSourceContainer)) return true;
        Container container = getAttachedContainer(pLevel, pPos, pState);
        LOGGER.debug("Container arrived to ejectItems");
        if (container == null) {
            return false;
        } else {
            Direction direction = pState.getValue(HopperBlock.FACING).getOpposite();
            if (isFullContainer(container, direction)) {
                return false;
            } else {
                for(int i = 0; i < pSourceContainer.getContainerSize(); ++i) {
                    if (!pSourceContainer.getItem(i).isEmpty()) {
                        ItemStack itemstack = pSourceContainer.getItem(i).copy();
                        ItemStack itemstack1 = addItem(pSourceContainer, container, pSourceContainer.removeItem(i, 1), direction);
                        if (itemstack1.isEmpty()) {
                            container.setChanged();
                            return true;
                        }

                        pSourceContainer.setItem(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    @Override
    /**
     * Returns the number of slots in the inventory.
     */
    public int getContainerSize() {
        return this.items.size();
    }

    private static boolean tryMoveItems(Level pLevel, BlockPos pPos, BlockState pState, SimplyHopperBlockEntity pBlockEntity, BooleanSupplier pValidator) {
        if (pLevel.isClientSide) {
            return false;
        } else {
            LOGGER.debug("Container arrived to tryMoveItems {}", pState.getValue(HopperBlock.ENABLED));

            if (!pBlockEntity.isOnCooldown() && pState.getValue(HopperBlock.ENABLED)) {
                boolean flag = false;
                if (!pBlockEntity.isEmpty()) {
                    flag = SimplyHopperBlockEntity.ejectItems(pLevel, pPos, pState, pBlockEntity);
                }
                LOGGER.debug("flag: {}", flag);

                if (!pBlockEntity.inventoryFull()) {
                    flag |= pValidator.getAsBoolean();
                }

                LOGGER.debug("flag = {}", flag);
                if (flag) {
                    pBlockEntity.setCooldown(8);
                    setChanged(pLevel, pPos, pState);
                    return true;
                }
            }

            return false;
        }
    }

    private static ItemStack tryMoveInItem(@Nullable Container pSource, Container pDestination, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
        ItemStack itemstack = pDestination.getItem(pSlot);
        if (canPlaceItemInContainer(pDestination, pStack, pSlot, pDirection)) {
            boolean flag = false;
            boolean flag1 = pDestination.isEmpty();
            if (itemstack.isEmpty()) {
                pDestination.setItem(pSlot, pStack);
                pStack = ItemStack.EMPTY;
                flag = true;
            } else if (canMergeItems(itemstack, pStack)) {
                int i = pStack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(pStack.getCount(), i);
                pStack.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag) {
                if (flag1 && pDestination instanceof SimplyHopperBlockEntity) {
                    SimplyHopperBlockEntity hopperblockentity1 = (SimplyHopperBlockEntity)pDestination;
                    if (!hopperblockentity1.isOnCustomCooldown()) {
                        int k = 0;
                        if (pSource instanceof SimplyHopperBlockEntity) {
                            SimplyHopperBlockEntity hopperblockentity = (SimplyHopperBlockEntity)pSource;
                            if (hopperblockentity1.tickedGameTime >= hopperblockentity.tickedGameTime) {
                                k = 1;
                            }
                        }

                        hopperblockentity1.setCooldown(8 - k);
                    }
                }

                pDestination.setChanged();
            }
        }

        return pStack;
    }

    public static boolean addItem(Container pContainer, ItemEntity pItem) {
        boolean flag = false;
        ItemStack itemstack = pItem.getItem().copy();
        ItemStack itemstack1 = addItem((Container)null, pContainer, itemstack, (Direction)null);
        if (itemstack1.isEmpty()) {
            flag = true;
            pItem.setItem(ItemStack.EMPTY);
            pItem.discard();
        } else {
            LOGGER.debug("Arrived at addItem");
            pItem.setItem(itemstack1);
        }

        return flag;
    }

    /**
     * Attempts to place the passed stack in the container, using as many slots as required.
     * @return any leftover stack
     */
    public static @NotNull ItemStack addItem(@Nullable Container pSource, Container pDestination, @NotNull ItemStack pStack, @Nullable Direction pDirection) {
        if (pDestination instanceof WorldlyContainer worldlycontainer) {
            if (pDirection != null) {
                int[] aint = worldlycontainer.getSlotsForFace(pDirection);

                for(int k = 0; k < aint.length && !pStack.isEmpty(); ++k) {
                    pStack = tryMoveInItem(pSource, pDestination, pStack, aint[k], pDirection);
                }

                return pStack;
            }
        }

        int i = pDestination.getContainerSize();

        for(int j = 0; j < i && !pStack.isEmpty(); ++j) {
            pStack = tryMoveInItem(pSource, pDestination, pStack, j, pDirection);
        }

        return pStack;
    }

    private static boolean isFullContainer(Container pContainer, Direction pDirection) {
        return getSlots(pContainer, pDirection).allMatch((p_59379_) -> {
            ItemStack itemstack = pContainer.getItem(p_59379_);
            return itemstack.getCount() >= itemstack.getMaxStackSize();
        });
    }

    private static IntStream getSlots(Container pContainer, Direction pDirection) {
        return pContainer instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)pContainer).getSlotsForFace(pDirection)) : IntStream.range(0, pContainer.getContainerSize());
    }

    private static boolean isEmptyContainer(Container pContainer, Direction pDirection) {
        return getSlots(pContainer, pDirection).allMatch((p_59319_) -> pContainer.getItem(p_59319_).isEmpty());
    }

    @Nullable
    private static Container getAttachedContainer(Level pLevel, BlockPos pPos, BlockState pState) {
        Direction direction = pState.getValue(HopperBlock.FACING);
        return getContainerAt(pLevel, pPos.relative(direction));
    }

    @Nullable
    private static Container getSourceContainer(Level pLevel, Hopper pHopper) {
        return getContainerAt(pLevel, pHopper.getLevelX(), pHopper.getLevelY() + 1.0D, pHopper.getLevelZ());
    }

    /**
     * @return the container for the given position or {@code null} if none was found
     */
    @Nullable
    private static Container getContainerAt(Level pLevel, double pX, double pY, double pZ) {
        Container container = null;
        BlockPos blockpos = BlockPos.containing(pX, pY, pZ);
        BlockState blockstate = pLevel.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder)block).getContainer(blockstate, pLevel, blockpos);
        } else if (blockstate.hasBlockEntity()) {
            BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
            if (blockentity instanceof Container) {
                container = (Container)blockentity;
                if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    container = ChestBlock.getContainer((ChestBlock)block, blockstate, pLevel, blockpos, true);
                }
            }
        }

        if (container == null) {
            List<Entity> list = pLevel.getEntities((Entity)null, new AABB(pX - 0.5D, pY - 0.5D, pZ - 0.5D, pX + 0.5D, pY + 0.5D, pZ + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list.isEmpty()) {
                container = (Container)list.get(pLevel.random.nextInt(list.size()));
            }
        }

        return container;
    }

    private static boolean canMergeItems(ItemStack pStack1, ItemStack pStack2) {
        return pStack1.getCount() <= pStack1.getMaxStackSize() && ItemStack.isSameItemSameTags(pStack1, pStack2);
    }

    public static boolean suckInItems(Level pLevel, Hopper pHopper) {
        Boolean ret = net.minecraftforge.items.VanillaInventoryCodeHooks.extractHook(pLevel, pHopper);
        if (ret != null) return ret;
        Container container = getSourceContainer(pLevel, pHopper);
        LOGGER.debug("Arrived at suckInItems: {}", container);
        if (container != null) {
            Direction direction = Direction.DOWN;
            LOGGER.debug("Arrived at suckInItems: {} -> {}", container, pHopper);
            return isEmptyContainer(container, direction) ? false : getSlots(container, direction).anyMatch((p_59363_) -> tryTakeInItemFromSlot(pHopper, container, p_59363_, direction));
        } else {
            for(ItemEntity itementity : getItemsAtAndAbove(pLevel, pHopper)) {
                if (addItem(pHopper, itementity)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Pulls from the specified slot in the container and places in any available slot in the hopper.
     * @return {@code true} if the entire stack was moved.
     */
    private static boolean tryTakeInItemFromSlot(Hopper pHopper, Container pContainer, int pSlot, Direction pDirection) {
        ItemStack itemstack = pContainer.getItem(pSlot);
        LOGGER.debug("Arrived at tryTakeInItemFromSlot");
        if (!itemstack.isEmpty() && canTakeItemFromContainer(pHopper, pContainer, itemstack, pSlot, pDirection)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = addItem(pContainer, pHopper, pContainer.removeItem(pSlot, 1), (Direction)null);
            LOGGER.debug("Arrived at tryTakeInItemFromSlot: {} -> {}", pContainer, pHopper);
            if (itemstack2.isEmpty()) {
                pContainer.setChanged();
                return true;
            }

            pContainer.setItem(pSlot, itemstack1);
        }

        return false;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        if (index < SLOTTED_ITEMS_SIZE) {
            return stack.getCount() < 1;
        }
        return false;
    }

    private boolean inventoryFull() {
        for(ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    @Override
    public void setCooldown(int ticks) {
        this.cooldownTime = ticks;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public NonNullList<ItemStack> getSlottedItems() {
        return this.slottedItems;
    }

    public void setSlottedItems(NonNullList<ItemStack> slottedItems) {
        List<ItemStack> trimmed = slottedItems.stream()
                .limit(SLOTTED_ITEMS_SIZE)
                .toList();
        this.slottedItems = NonNullList.withSize(SLOTTED_ITEMS_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < trimmed.size(); i++) {
            this.slottedItems.set(i, trimmed.get(i));
        }
    }

    private int getPartitionedSlotStackSize(ItemStack stack) {
        int slotCount = Math.max(1, this.getSlottedItems().size());  // Avoid divide-by-zero
        return (SLOTTED_ITEMS_SIZE * stack.getMaxStackSize()) / slotCount;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        // Save internal inventory
        ContainerHelper.saveAllItems(tag, this.items);

        // Save slottedItems
        ListTag slottedTagList = new ListTag();
        for (ItemStack stack : slottedItems) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            slottedTagList.add(itemTag);
        }
        tag.put("SlottedItems", slottedTagList);

        tag.putInt("CooldownTime", this.cooldownTime);
        tag.putLong("TickedGameTime", this.tickedGameTime);
        tag.putBoolean("IsLocked", this.isLocked);
        tag.putBoolean("IsPowered", this.isPowered);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        // Load internal inventory
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);

        // Load slottedItems
        ListTag slottedTagList = tag.getList("SlottedItems", Tag.TAG_COMPOUND);
        this.slottedItems = NonNullList.withSize(SLOTTED_ITEMS_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < slottedTagList.size(); i++) {
            CompoundTag itemTag = slottedTagList.getCompound(i);
            this.slottedItems.set(i, ItemStack.of(itemTag));
        }

        this.cooldownTime = tag.getInt("CooldownTime");
        this.tickedGameTime = tag.getLong("TickedGameTime");
        this.isLocked = tag.getBoolean("IsLocked");
        this.isPowered = tag.getBoolean("IsPowered");
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (index < 0 || index >= SLOTTED_ITEMS_SIZE) return;

        Item allowedItem = slottedItems.size() > index ? slottedItems.get(index).getItem() : null;

        // If no allowed item for this slot, reject
        if (allowedItem == null || !stack.getItem().equals(allowedItem)) {
            return;
        }

        int partitionedLimit = getPartitionedSlotStackSize(stack);
        if (stack.getCount() > partitionedLimit) {
            stack.setCount(partitionedLimit);
        }

        this.unpackLootTable(null); // Required for hopper behavior
        items.set(index, stack);
        setChanged();
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> pItems) {
        items = pItems;
    }

    public void setSlottedItem(int index, @NotNull ItemStack stack) {
        if (index < 0 || index >= SLOTTED_ITEMS_SIZE) return;

        this.unpackLootTable((Player)null); // Required for hopper behavior
        slottedItems.set(index, stack);
        setChanged();
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public @NotNull ItemStack removeItem(int pIndex, int pCount) {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public @NotNull ItemStack removeSlottedItem(int pIndex, int pCount) {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.getSlottedItems(), pIndex, pCount);
    }
    
    private static boolean canPlaceItemInContainer(net.minecraft.world.Container pContainer, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
        if (!pContainer.canPlaceItem(pSlot, pStack)) {
            return false;
        } else {
            LOGGER.debug("Arrived at canPlaceItemInContainer: {}", pContainer);
            if (pContainer instanceof WorldlyContainer) {
                WorldlyContainer worldlycontainer = (WorldlyContainer)pContainer;
                if (!worldlycontainer.canPlaceItemThroughFace(pSlot, pStack, pDirection)) {
                    return false;
                }
//                if(canSlottedItems(pStack))
//                    return false;

            }

            return true;
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    public int getSlottedItemMaxStackSize() {
        return 1;
    }

    private boolean canSlottedItems(ItemStack pStack) {
        return !slottedItems.stream().anyMatch((ItemStack o) -> o.getItem().equals(pStack.getItem())) || !slottedItems.isEmpty();
    }

    private static boolean canTakeItemFromContainer(net.minecraft.world.Container pSource, Container pDestination, ItemStack pStack, int pSlot, Direction pDirection) {
        if (!pDestination.canTakeItem(pSource, pSlot, pStack)) {
            return false;
        } else {
            LOGGER.debug("Arrived at canTakeItemFromContainer: {} -> {}", pSource,pDestination);
            if (pDestination instanceof WorldlyContainer) {
                WorldlyContainer worldlycontainer = (WorldlyContainer)pDestination;
                if (!worldlycontainer.canTakeItemThroughFace(pSlot, pStack, pDirection)) {
                    return false;
                }

                if (worldlycontainer instanceof SimplyHopperBlockEntity)
                    return false;
//                if(items.stream().noneMatch((ItemStack o) -> pSource.countItem(o.getItem()) < getPartitionedSlotStackSize(o))){
//                    return false;
//                }
            }
//            if(!this.getSlottedItems().stream().anyMatch((ItemStack o)-> o.getItem().equals(pStack.getItem()))){
//                return false;
//            }

//            if(!canPlaceItemInContainer(pSource, pStack, pSlot, pDirection) || pSource instanceof SimplyHopperBlockEntity){
//                return false;
//            }

            return true;
        }
    }

    @Override
    protected @NotNull SimplyHopperMenu createMenu(int pId, @NotNull Inventory pPlayer) {
        return new SimplyHopperMenu(pId, pPlayer, this);
    }

    public static void entityInside(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity, SimplyHopperBlockEntity pBlockEntity) {
        if (pEntity instanceof ItemEntity itementity) {
            if (!itementity.getItem().isEmpty() && Shapes.joinIsNotEmpty(Shapes.create(pEntity.getBoundingBox().move((double)(-pPos.getX()), (double)(-pPos.getY()), (double)(-pPos.getZ()))), pBlockEntity.getSuckShape(), BooleanOp.AND)) {
                LOGGER.debug("Arrived at entityInside: {}   {}", pBlockEntity,itementity.getItem().getDisplayName());
                tryMoveItems(pLevel, pPos, pState, pBlockEntity, () -> addItem(pBlockEntity, itementity));
            }
        }

    }

    @Override
    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }
}
