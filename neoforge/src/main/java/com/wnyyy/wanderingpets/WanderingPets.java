package com.wnyyy.wanderingpets;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod(Constants.MOD_ID)
public class WanderingPets {

    public WanderingPets(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeModConfig.SPEC);
        modEventBus.addListener(this::onConfigLoad);

        WanderingPetsCommon.init();
    }

    private void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == NeoForgeModConfig.SPEC) {
            com.wnyyy.wanderingpets.config.ModConfig.initConfig(NeoForgeModConfig.loadConfig());
        }
    }
}