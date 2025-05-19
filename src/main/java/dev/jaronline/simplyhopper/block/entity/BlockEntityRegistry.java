package dev.jaronline.simplyhopper.block.entity;

import dev.jaronline.simplyhopper.SimplyHopper;
import dev.jaronline.simplyhopper.block.BlockRegistry;
import dev.jaronline.simplyhopper.entity.SimplyHopperBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, SimplyHopper.MOD_ID);

    public static final RegistryObject<BlockEntityType<SimplyHopperBlockEntity>> SIMPLY_HOPPER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("simply_hopper_block_entity", () -> BlockEntityType.Builder.of(
                    SimplyHopperBlockEntity::new, BlockRegistry.SIMPLE_HOPPER_BLOCK.getBlock()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
