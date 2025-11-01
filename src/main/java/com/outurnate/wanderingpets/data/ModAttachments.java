package com.outurnate.wanderingpets.data;

import com.outurnate.wanderingpets.WanderingPets;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WanderingPets.MODID);

    public static final Supplier<AttachmentType<Boolean>> SHOULD_FOLLOW = ATTACHMENT_TYPES.register(
            "should_follow",
            () -> AttachmentType.builder(() -> true)
                    .serialize(Codec.BOOL.fieldOf("should_follow"))
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}