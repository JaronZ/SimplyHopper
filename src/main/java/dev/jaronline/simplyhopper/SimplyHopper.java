package dev.jaronline.simplyhopper;

import com.mojang.logging.LogUtils;
import dev.jaronline.simplyhopper.block.BlockRegistry;
import dev.jaronline.simplyhopper.block.entity.BlockEntityRegistry;
import dev.jaronline.simplyhopper.gui.CreativeTabRegistry;
import dev.jaronline.simplyhopper.item.ItemRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SimplyHopper.MOD_ID)
public class SimplyHopper {
    public static final String MOD_ID = "simplyhopper";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SimplyHopper() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BlockRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting");
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup");
    }
}
