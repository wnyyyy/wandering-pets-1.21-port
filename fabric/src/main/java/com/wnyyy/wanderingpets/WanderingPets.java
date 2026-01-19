package com.wnyyy.wanderingpets;

import com.wnyyy.wanderingpets.config.ModConfig;
import net.fabricmc.api.ModInitializer;

public class WanderingPets implements ModInitializer {
    
    @Override
    public void onInitialize() {
        ModConfig.initConfig(FabricModConfig.loadConfig());
        WanderingPetsCommon.init();
    }
}
