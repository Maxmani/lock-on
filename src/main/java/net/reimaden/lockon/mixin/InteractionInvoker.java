package net.reimaden.lockon.mixin;

import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Interaction.class)
public interface InteractionInvoker {

    @Invoker("getHeight")
    float lockon$getHeight();
}
