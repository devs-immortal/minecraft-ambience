package vazkii.ambience.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.ambience.SongLoader;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin{
    @Inject(
        method = "play",
        at = @At(
            value ="INVOKE",
            target = "Lnet/minecraft/client/sound/SoundInstance;canPlay()Z"
        ),
        cancellable = true
    )
    private void play(SoundInstance sound, CallbackInfo ci){
        if(SongLoader.enabled && sound.getCategory() == SoundCategory.MUSIC){
            ci.cancel();
        }
    }
}
