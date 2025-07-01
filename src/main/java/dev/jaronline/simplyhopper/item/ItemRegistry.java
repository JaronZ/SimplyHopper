package dev.jaronline.simplyhopper.item;

import dev.jaronline.simplyhopper.SimplyHopper;
import dev.jaronline.simplyhopper.entity.SimplyHopperBlockEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    public static RegistryObject<BlockItem> registerBlockItem(String name, Supplier<BlockItem> blockItem) {
        return ITEMS.register(name, blockItem);
    }

    public static <T extends MinecartItem> RegistryObject<T> registerMinecartItem(String name, Supplier<T> minecartItem) {
        return ITEMS.register(name, minecartItem);
    }
}
