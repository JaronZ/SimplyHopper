package dev.jaronline.simplyhopper.gui;

import dev.jaronline.simplyhopper.entity.SimplyHopperBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimplyHopperSlot extends Slot {
    private final int slot;
    private final SimplyHopperBlockEntity blockEntity;


    SimplyHopperSlot(Container pContainer, int pSlot, int pX, int pY){
        super(pContainer, pSlot, pX, pY);
        slot = pSlot;

        // TODO: FIX vvvv
        blockEntity = (SimplyHopperBlockEntity) pContainer;

    }

    @Override
    public @NotNull ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    @Override
    public void set(@NotNull ItemStack pStack) {
        this.blockEntity.setSlottedItem(this.slot, pStack);
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return this.blockEntity.getSlottedItemMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack pStack) {
        return Math.min(this.getMaxStackSize(), pStack.getMaxStackSize());
    }

    @Override
    public @NotNull ItemStack remove(int pAmount) {
        return this.blockEntity.removeSlottedItem(this.slot, pAmount);
    }
}
