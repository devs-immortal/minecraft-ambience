package vazkii.ambience.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.ambience.NilMusicTracker;
import vazkii.ambience.SongLoader;
import vazkii.ambience.SongPicker;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin{
    @Redirect(
        method = "<init>",
        at = @At(
            value = "NEW",
            target = "Lnet/minecraft/client/sound/MusicTracker;"
        )
    )
    private MusicTracker createTracker(MinecraftClient client){
        return SongLoader.enabled ? new NilMusicTracker(client) : new MusicTracker(client);
    }
}
