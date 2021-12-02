package vazkii.ambience;

import java.io.InputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import vazkii.ambience.thirdparty.javazoom.jl.player.AudioDevice;
import vazkii.ambience.thirdparty.javazoom.jl.player.JavaSoundAudioDevice;
import vazkii.ambience.thirdparty.javazoom.jl.player.advanced.AdvancedPlayer;

import static vazkii.ambience.Ambience.starting;

public final class PlayerThread extends Thread{
    
    public static float MIN_GAIN = -50F;
    public static final float ORIGINAL_MIN = -50F;
    public static final float MAX_GAIN = 10F;
    
    public static float[] fadeGains;
    
    static{
        fadeGains = new float[Ambience.FADE_DURATION];
        float totaldiff = MIN_GAIN - MAX_GAIN;
        float diff = totaldiff / fadeGains.length;
        for(int i = 0; i < fadeGains.length; i++){
            fadeGains[i] = MAX_GAIN + diff * i;
        }
    }
    
    public volatile static float gain = MAX_GAIN;
    public volatile static float realGain = 0;
    public final static float realGainPin = 0;
    public volatile static float realGainHolder = 0;
    
    public volatile static String currentSong = null;
    public volatile static String currentSongChoices = null;
    
    public static AdvancedPlayer player;
    
    volatile boolean queued = false;
    
    volatile boolean kill = false;
    volatile boolean playing = false;
    
    public PlayerThread(){
        setDaemon(true);
        setName("Ambience Player Thread");
        start();
    }
    
    @Override
    public void run(){
        try{
            while(!kill){
                if(queued && currentSong != null){
                    if(player != null){
                        resetPlayer();
                    }
                    InputStream stream = SongLoader.getStream();
                    if(stream == null){
                        continue;
                    }
                    
                    player = new AdvancedPlayer(stream);
                    setGain(fadeGains[0]);
                    starting = true;
                    queued = false;
                }
                
                boolean played = false;
                if(player != null && player.getAudioDevice() != null && realGain > MIN_GAIN){
                    player.play();
                    playing = true;
                    played = true;
                }
    
                if(played && !queued){
                    next();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void next(){
        if(!currentSongChoices.contains(",")){
            play(currentSong);
        }else{
            if(SongPicker.getSongsString().equals(currentSongChoices)){
                String newSong;
                do{
                    newSong = SongPicker.getRandomSong();
                }while(newSong.equals(currentSong));
                play(newSong);
            }else{
                play(null);
            }
        }
    }
    
    public void resetPlayer(){
        playing = false;
        if(player != null){
            player.close();
        }
        
        currentSong = null;
        player = null;
    }
    
    public void play(String song){
        resetPlayer();
        
        currentSong = song;
        queued = true;
    }
    
    public float getGain(){
        if(player == null){
            return gain;
        }
        
        AudioDevice device = player.getAudioDevice();
        if(device instanceof JavaSoundAudioDevice){
            return ((JavaSoundAudioDevice)device).getGain();
        }
        return gain;
    }
    
    public void addGain(float gain){
        setGain(getGain() + gain);
    }
    
    public void setGain(float gain){
        PlayerThread.gain = gain;
    
        if(player == null){
            return;
        }
        
        setRealGain();
    }
    
    public void fadeStep(float gain){
        float realGain = getGain() + gain;
        
        realGainHolder = realGain;
        PlayerThread.realGain = realGain;
        if(player != null){
            if(player.getAudioDevice() instanceof JavaSoundAudioDevice device){
                try{
                    device.setGain(realGain);
                }catch(IllegalArgumentException e){
                }
                // You have to try harder than this Vazkii
                // If you can't fix the bug just put a catch around it
            }
        }
    }
    
    public void setRealGain(){
        var settings = MinecraftClient.getInstance().options;
        float musicGain = settings.getSoundVolume(SoundCategory.MUSIC) * settings.getSoundVolume(SoundCategory.MASTER);
        float realGain = MIN_GAIN + (MAX_GAIN - MIN_GAIN) * musicGain;
        
        PlayerThread.realGain = realGain;
        if(player != null){
            if(player.getAudioDevice() instanceof JavaSoundAudioDevice device){
                try{
                    device.setGain(realGain);
                }catch(IllegalArgumentException e){
                }
                // You have to try harder than this Vazkii
                // If you can't fix the bug just put a catch around it
            }
        }
    
        if(musicGain == 0){
            play(null);
        }
    }
    
    public float getRelativeVolume(){
        return getRelativeVolume(getGain());
    }
    
    public float getRelativeVolume(float gain){
        float width = MAX_GAIN - MIN_GAIN;
        float rel = Math.abs(gain - MIN_GAIN);
        return rel / Math.abs(width);
    }
    
    public int getFramesPlayed(){
        return player == null ? 0 : player.getFrames();
    }
}
