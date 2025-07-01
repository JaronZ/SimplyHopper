package dev.jaronline.simplyhopper.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class RegistryBlockItem<T extends Block, R extends BlockItem> {
    private final RegistryObject<T> block;
    private final RegistryObject<R> blockItem;

    RegistryBlockItem(RegistryObject<T> block, RegistryObject<R> blockItem) {
        this.block = block;
        this.blockItem = blockItem;
    }

    public RegistryObject<T> getRegistryBlock() {
        return block;
    }

    public T getBlock() {
        return block.get();
    }

    public RegistryObject<R> getRegistryBlockItem() {
        return blockItem;
    }

    public R getBlockItem() {
        return blockItem.get();
    }
}
