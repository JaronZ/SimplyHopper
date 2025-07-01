package dev.jaronline.simplyhopper.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class RegistryEntityItem <T extends EntityType<? extends Entity>, R extends Item> {
    private final RegistryObject<T> entity;
    private final RegistryObject<R> item;

    RegistryEntityItem(RegistryObject<T> entity, RegistryObject<R> item) {
        this.entity = entity;
        this.item = item;
    }

    public RegistryObject<T> getRegistryEntity() {
        return entity;
    }

    public T getEntity() {
        return entity.get();
    }

    public RegistryObject<R> getRegistryEntityItem() {
        return item;
    }

    public R getItem() {
        return item.get();
    }
}
