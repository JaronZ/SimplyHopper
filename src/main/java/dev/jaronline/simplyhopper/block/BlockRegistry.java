package dev.jaronline.simplyhopper.block;

import com.mojang.datafixers.types.Func;
import dev.jaronline.simplyhopper.SimplyHopper;
import dev.jaronline.simplyhopper.item.ItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SimplyHopper.MOD_ID);

    public static final RegistryBlockItem<Block, BlockItem> SIMPLY_HOPPER_BLOCK = registerBlock("simply_hopper",
            () -> new SimplyHopperBlock(BlockBehaviour.Properties.copy(Blocks.HOPPER)),
            (name, registryBlock) -> ItemRegistry.registerBlockItem(name, () -> new BlockItem(registryBlock.get(), new Item.Properties())));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block, R extends BlockItem> RegistryBlockItem<T, R> registerBlock(String name, Supplier<T> block, BiFunction<String, RegistryObject<T>, RegistryObject<R>> blockItemSupplier) {
        RegistryObject<T> registryBlock = BLOCKS.register(name, block);
        RegistryObject<R> blockItem = blockItemSupplier.apply(name, registryBlock);
        return new RegistryBlockItem<>(registryBlock, blockItem);
    }
}
