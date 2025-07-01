package dev.jaronline.simplyhopper.gui;

import dev.jaronline.simplyhopper.block.BlockRegistry;
import dev.jaronline.simplyhopper.entity.EntityRegistry;
import dev.jaronline.simplyhopper.item.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import static dev.jaronline.simplyhopper.SimplyHopper.MOD_ID;

public class CreativeTabRegistry {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    static {
        CREATIVE_MODE_TABS.register(
                "simply_hopper_tab", () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.simply_hopper"))
                        .icon(() -> BlockRegistry.SIMPLY_HOPPER_BLOCK.getBlockItem().getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            output.accept(BlockRegistry.SIMPLY_HOPPER_BLOCK.getBlockItem());
                            output.accept(EntityRegistry.SIMPLY_HOPPER_MINECART.getItem());
                        })
                        .build());
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        eventBus.addListener(CreativeTabRegistry::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(BlockRegistry.SIMPLY_HOPPER_BLOCK.getBlockItem());
            event.accept(EntityRegistry.SIMPLY_HOPPER_MINECART.getItem());
        }
    }
}
