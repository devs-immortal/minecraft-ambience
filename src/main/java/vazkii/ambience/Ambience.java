package vazkii.ambience;

import java.io.IOException;
import java.nio.file.Files;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Ambience implements ClientModInitializer{
    
    public static final String MOD_ID = "ambience";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    private static final int WAIT_DURATION = 120;
    public static final int FADE_DURATION = 120;
    public static final int SILENCE_DURATION = 120;
    
    public static PlayerThread thread;
    
    String currentSong;
    public static volatile String nextSong;
    int waitTick = WAIT_DURATION;
    int fadeOutTicks = FADE_DURATION;
    int fadeInTicks = 0;
    int silenceTicks = 0;
    
    public static volatile boolean fading = false;
    public static volatile boolean starting = false;
    
    @Override
    public void onInitializeClient(){
        preInit();
        
        ClientTickEvents.END_CLIENT_TICK.register((client)->onTick());
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register((context)->onRenderOverlay(context));
    }
    
    public void preInit(){
        var ambienceDir = FabricLoader.getInstance().getConfigDir().resolve("ambience_music");
        try{
            Files.createDirectories(ambienceDir);
        }catch(IOException ignored){
        }
        
        SongLoader.loadFrom(ambienceDir);
        
        if(SongLoader.enabled){
            thread = new PlayerThread();
        }
    }
    
    public void onTick(){
        if(thread == null){
            return;
        }
        
        String songs = SongPicker.getSongsString();
        String song = null;
        
        if(songs != null){
            if(nextSong == null || !songs.contains(nextSong)){
                do{
                    song = SongPicker.getRandomSong();
                }while(song.equals(currentSong) && songs.contains(","));
            }else{
                song = nextSong;
            }
        }
        
        if(songs != null && (!songs.equals(PlayerThread.currentSongChoices) || (song == null && PlayerThread.currentSong != null) || !thread.playing)){
            if(nextSong != null && nextSong.equals(song)){
                waitTick--;
            }
            
            if(!song.equals(currentSong)){
                if(currentSong != null && PlayerThread.currentSong != null && !PlayerThread.currentSong.equals(song) && songs.equals(PlayerThread.currentSongChoices)){
                    currentSong = PlayerThread.currentSong;
                }else{
                    nextSong = song;
                }
            }else if(nextSong != null && !songs.contains(nextSong)){
                nextSong = null;
            }
            
            if(waitTick <= 0){
                if(PlayerThread.currentSong == null){
                    currentSong = nextSong;
                    nextSong = null;
                    PlayerThread.currentSongChoices = songs;
                    changeSongTo(song);
                    fadeOutTicks = 0;
                    waitTick = WAIT_DURATION;
                }else if(fadeOutTicks < FADE_DURATION){
                    thread.setGain(PlayerThread.fadeGains[fadeOutTicks]);
                    fadeOutTicks++;
                    silenceTicks = 0;
                }else{
                    if(silenceTicks < SILENCE_DURATION){
                        silenceTicks++;
                    }else{
                        nextSong = null;
                        PlayerThread.currentSongChoices = songs;
                        changeSongTo(song);
                        fadeOutTicks = 0;
                        waitTick = WAIT_DURATION;
                    }
                }
            }
        }else{
            nextSong = null;
            thread.setGain(PlayerThread.fadeGains[0]);
            silenceTicks = 0;
            fadeOutTicks = 0;
            waitTick = WAIT_DURATION;
        }
        
        if(thread != null){
            thread.setRealGain();
        }
    }
    
    public void onRenderOverlay(WorldRenderContext context){
        if(!MinecraftClient.getInstance().options.debugEnabled){
            return;
        }
        
        /*TODO
        event.getRight().add(null);
        if(PlayerThread.currentSong != null){
            String name = "Now Playing: " + SongPicker.getSongName(PlayerThread.currentSong);
            event.getRight().add(name);
        }
        if(nextSong != null){
            String name = "Next Song: " + SongPicker.getSongName(nextSong);
            event.getRight().add(name);
        }
         */
    }
    
    public void changeSongTo(String song){
        currentSong = song;
        thread.play(song);
    }
}
