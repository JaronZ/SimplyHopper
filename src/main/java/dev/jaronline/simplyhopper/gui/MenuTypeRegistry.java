package dev.jaronline.simplyhopper.gui;

import dev.jaronline.simplyhopper.SimplyHopper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeRegistry {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES, SimplyHopper.MOD_ID);

//    public static final RegistryObject<MenuType<SimplyHopperMenu>> SIMPLY_HOPPER_MENU = registerMenuType(
//            "simply_hopper_menu", SimplyHopperMenu::new);

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

//    public static void registerScreens() {
//        MenuScreens.register(SIMPLY_HOPPER_MENU.get(), SimplyHopperScreen::new);
//    }

    private static <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> RegistryObject<MenuType<T>> registerMenuType(
            String name, IContainerFactory<T> menuFactory) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(menuFactory));
    }
}
