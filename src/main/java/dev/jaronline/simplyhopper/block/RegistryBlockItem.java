package dev.jaronline.simplyhopper.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class RegistryBlockItem<T extends Block> {
    private final RegistryObject<T> block;
    private final RegistryObject<BlockItem> blockItem;

    RegistryBlockItem(RegistryObject<T> block, RegistryObject<BlockItem> blockItem) {
        this.block = block;
        this.blockItem = blockItem;
    }

    public RegistryObject<T> getRegistryBlock() {
        return block;
    }

    public Block getBlock() {
        return block.get();
    }

    public RegistryObject<BlockItem> getRegistryBlockItem() {
        return blockItem;
    }

    public BlockItem getBlockItem() {
        return blockItem.get();
    }
}
