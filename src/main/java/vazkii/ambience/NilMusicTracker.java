package vazkii.ambience;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;

import static vazkii.ambience.Ambience.fading;
import static vazkii.ambience.Ambience.nextSong;
import static vazkii.ambience.Ambience.thread;
import static vazkii.ambience.PlayerThread.player;
import static vazkii.ambience.PlayerThread.realGainHolder;

public final class NilMusicTracker extends MusicTracker{
    
    public NilMusicTracker(MinecraftClient client){
        super(client);
    }
    
    @Override
    public void tick(){
        // NO-OP
        if(nextSong != null && player != null){
            thread.fadeStep(-0.2f);
            fading = true;
        }
        if(nextSong == null && player != null && fading){
            fading = false;
        }
        if(!fading && realGainHolder < 0f){
            thread.fadeStep(0.2f);
        }
    }
    
}
