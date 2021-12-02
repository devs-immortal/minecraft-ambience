package vazkii.ambience;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.Level;

public final class SongLoader{
    
    public static Path mainDir;
    public static boolean enabled = false;
    
    public static void loadFrom(Path f){
        var config = f.resolve("ambience.properties");
        if(!Files.exists(config)){
            initConfig(config);
        }
        
        Properties props = new Properties();
        try(var reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)){
            props.load(reader);
            enabled = props.getProperty("enabled").equals("true");
            
            if(enabled){
                SongPicker.reset();
                Set<Object> keys = props.keySet(); //Gets all the events
                for(Object obj : keys){
                    String s = (String)obj;
                    
                    String[] tokens = s.split("\\."); //Splits them one at a time
                    if(tokens.length < 2){
                        continue;
                    }
                    
                    
                    String keyType = tokens[0];
                    switch(keyType){
                        case "event" -> {
                            String event = tokens[1];
                            
                            SongPicker.eventMap.put(event, props.getProperty(s).split(","));
                        }
                        case "biome" -> {
                            String biomeName = joinTokensExceptFirst(tokens).replaceAll("\\+", " ");
                            Biome biome = BiomeMapper.getBiome(biomeName);
                            
                            if(biome != null){
                                SongPicker.biomeMap.put(biome, props.getProperty(s).split(","));
                            }
                        }
                        case "primarytag" -> {
                            String tagName = tokens[1].toUpperCase();
                            var type = BiomeMapper.getBiomeType(tagName);
                            
                            if(type != null){
                                SongPicker.primaryTagMap.put(type, props.getProperty(s).split(","));
                            }
                        }
                        case "secondarytag" -> {
                            String tagName = tokens[1].toUpperCase();
                            var type = BiomeMapper.getBiomeType(tagName);
                            
                            if(type != null){
                                SongPicker.secondaryTagMap.put(type, props.getProperty(s).split(","));
                            }
                        }
                        case "dimension" -> {
                            var dim = RegistryKey.of(Registry.WORLD_KEY, new Identifier(tokens[1]));
                            
                            SongPicker.dimensions.add(dim);
                            SongPicker.dimensionMap.put(dim, props.getProperty(s).split(","));
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        var musicDir = f.resolve("music");
        try{
            Files.createDirectories(musicDir);
        }catch(IOException ignored){}
    
        mainDir = musicDir;
    }
    
    public static void initConfig(Path f){
        try(var writer = Files.newBufferedWriter(f, StandardCharsets.UTF_8)){
            writer.write("# Ambience Config\n");
            writer.write("enabled=false");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static InputStream getStream(){
        if(PlayerThread.currentSong == null || PlayerThread.currentSong.equals("null")){
            return null;
        }
        
        var f = mainDir.resolve(PlayerThread.currentSong + ".mp3");
        if(f.getFileName().toString().equals("null.mp3")){
            return null;
        }
        
        try{
            return Files.newInputStream(f);
        }catch(IOException e){
            Ambience.LOGGER.log(Level.ERROR, "File " + f + " not found. Fix your Ambience config!");
            e.printStackTrace();
            return null;
        }
    }
    
    private static String joinTokensExceptFirst(String[] tokens){
        StringBuilder s = new StringBuilder();
        int i = 0;
        for(String token : tokens){
            i++;
            if(i == 1){
                continue;
            }
            s.append(token);
        }
        return s.toString();
    }
}
