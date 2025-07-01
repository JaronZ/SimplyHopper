package dev.jaronline.simplyhopper.entity;

import dev.jaronline.simplyhopper.SimplyHopper;
import dev.jaronline.simplyhopper.item.ItemRegistry;
import dev.jaronline.simplyhopper.item.SimplyHopperMinecartItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MinecartItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SimplyHopper.MOD_ID);

    public static final RegistryEntityItem<EntityType<SimplyHopperMinecartEntity>,SimplyHopperMinecartItem> SIMPLY_HOPPER_MINECART = registerMinecartEntity(
            "simply_hopper_minecart",
            () -> EntityType.Builder.<SimplyHopperMinecartEntity>of(
                            SimplyHopperMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(8)
                    .build(SimplyHopper.MOD_ID + ":simply_hopper_minecart"),
            () -> new SimplyHopperMinecartItem(new Item.Properties()));


    public static <T extends Entity,R extends MinecartItem> RegistryEntityItem<EntityType<T>,R> registerMinecartEntity(String name, Supplier<EntityType<T>> entityTypeSupplier, Supplier<R> itemSupplier) {
        RegistryObject<EntityType<T>> registryEntity = ENTITIES.register(name, entityTypeSupplier);
        RegistryObject<R> item = ItemRegistry.registerMinecartItem(name, itemSupplier);
        return new RegistryEntityItem<>(registryEntity, item);
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
