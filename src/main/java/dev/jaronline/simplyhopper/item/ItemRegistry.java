package dev.jaronline.simplyhopper.item;

import dev.jaronline.simplyhopper.SimplyHopper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SimplyHopper.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static <T extends BlockItem> RegistryObject<T> registerBlockItem(String name, Supplier<T> blockItem) {
        return ITEMS.register(name, blockItem);
    }
}
