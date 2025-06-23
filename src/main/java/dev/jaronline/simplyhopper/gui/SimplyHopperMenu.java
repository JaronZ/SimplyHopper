package dev.jaronline.simplyhopper.gui;

import dev.jaronline.simplyhopper.entity.SimplyHopperBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimplyHopperMenu extends HopperMenu {
    public SimplyHopperMenu(int pContainerId, Inventory pPlayerInventory) {
        super(pContainerId, pPlayerInventory);
    }

    public SimplyHopperMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
        super(pContainerId, pPlayerInventory, pContainer);
    }
//    public static final int CONTAINER_SIZE = 5;
//    private final Container hopper;

//    public SimplyHopperMenu(int pContainerId, Inventory pPlayerInventory) {
//        super(pContainerId, pPlayerInventory, new SimpleContainer(CONTAINER_SIZE));
//    }
//
//    public SimplyHopperMenu(int containerId, Inventory playerInventory, FriendlyByteBuf additionalData) {
//        this(containerId, playerInventory);
//    }

//    public SimplyHopperMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
//        super(MenuTypeRegistry.SIMPLY_HOPPER_MENU.get(), pContainerId);
//
//        this.hopper = pContainer;
//        checkContainerSize(pContainer, CONTAINER_SIZE);
//        pContainer.startOpen(pPlayerInventory.player);
//        int i = 51;
//
//        for(int j = 0; j < CONTAINER_SIZE; ++j) {
//            this.addSlot(new SimplyHopperSlot(pContainer, j, 44 + j * 18, 20));
//        }
//
//        for(int l = 0; l < 3; ++l) {
//            for(int k = 0; k < 9; ++k) {
//                this.addSlot(new Slot(pPlayerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
//            }
//        }
//
//        for(int i1 = 0; i1 < 9; ++i1) {
//            this.addSlot(new Slot(pPlayerInventory, i1, 8 + i1 * 18, 109));
//        }
//    }
//
//    /**
//     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
//     * inventory and the other inventory(s).
//     */
//    @Override
//    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
//        ItemStack itemstack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(pIndex);
//        if (slot != null && slot.hasItem()) {
//            ItemStack itemstack1 = slot.getItem();
//            itemstack = itemstack1.copy();
//            if (pIndex < this.hopper.getContainerSize()) {
//                if (!this.moveItemStackTo(itemstack1, this.hopper.getContainerSize(), this.slots.size(), true)) {
//                    return ItemStack.EMPTY;
//                }
//            } else if (!this.moveItemStackTo(itemstack1, 0, this.hopper.getContainerSize(), false)) {
//                return ItemStack.EMPTY;
//            }
//
//            if (itemstack1.isEmpty()) {
//                slot.setByPlayer(ItemStack.EMPTY);
//            } else {
//                slot.setChanged();
//            }
//        }
//
//        return itemstack;
//    }
//
//    /**
//     * Merges provided ItemStack with the first available one in the container/player inventor between minIndex
//     * (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, negativDirection. [!] the Container
//     * implementation do not check if the item is valid for the slot
//     */
//    @Override
//    protected boolean moveItemStackTo(@NotNull ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
//        boolean flag = false;
//        int i = pStartIndex;
//        if (pReverseDirection) {
//            i = pEndIndex - 1;
//        }
//
//        if (pStack.isStackable()) {
//            while(!pStack.isEmpty()) {
//                if (pReverseDirection) {
//                    if (i < pStartIndex) {
//                        break;
//                    }
//                } else if (i >= pEndIndex) {
//                    break;
//                }
//
//                Slot slot = this.slots.get(i);
//                ItemStack itemstack = slot.getItem();
//                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(pStack, itemstack)) {
//                    int j = itemstack.getCount() + pStack.getCount();
//                    int maxSize = Math.min(((SimplyHopperBlockEntity)this.hopper).getSlottedItemMaxStackSize(), pStack.getMaxStackSize());
//                    if (j <= maxSize) {
//                        pStack.setCount(0);
//                        itemstack.setCount(j);
//                        slot.setChanged();
//                        flag = true;
//                    } else if (itemstack.getCount() < maxSize) {
//                        pStack.shrink(maxSize - itemstack.getCount());
//                        itemstack.setCount(maxSize);
//                        slot.setChanged();
//                        flag = true;
//                    }
//                }
//
//                if (pReverseDirection) {
//                    --i;
//                } else {
//                    ++i;
//                }
//            }
//        }
//
//        if (!pStack.isEmpty()) {
//            if (pReverseDirection) {
//                i = pEndIndex - 1;
//            } else {
//                i = pStartIndex;
//            }
//
//            while(true) {
//                if (pReverseDirection) {
//                    if (i < pStartIndex) {
//                        break;
//                    }
//                } else if (i >= pEndIndex) {
//                    break;
//                }
//
//                Slot slot1 = this.slots.get(i);
//                ItemStack itemstack1 = slot1.getItem();
//                if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
//                    if (pStack.getCount() > ((SimplyHopperBlockEntity)this.hopper).getSlottedItemMaxStackSize()) {
//                        slot1.setByPlayer(pStack.split(((SimplyHopperBlockEntity)this.hopper).getSlottedItemMaxStackSize()));
//                    } else {
//                        slot1.setByPlayer(pStack.split(pStack.getCount()));
//                    }
//
//                    slot1.setChanged();
//                    flag = true;
//                    break;
//                }
//
//                if (pReverseDirection) {
//                    --i;
//                } else {
//                    ++i;
//                }
//            }
//        }
//
//        return flag;
//    }
//
//    /**
//     * Called when the container is closed.
//     */
//    @Override
//    public void removed(@NotNull Player pPlayer) {
//        super.removed(pPlayer);
//        this.hopper.stopOpen(pPlayer);
//    }
//
//    /**
//     * Determines whether supplied player can use this container
//     */
//    @Override
//    public boolean stillValid(@NotNull Player pPlayer) {
//        return this.hopper.stillValid(pPlayer);
//    }
}
