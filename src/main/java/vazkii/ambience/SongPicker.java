package vazkii.ambience;

import java.util.*;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;
import vazkii.ambience.mixin.BossBarHudAccessor;

public final class SongPicker{
    
    public static final String EVENT_MAIN_MENU = "mainMenu";
    public static final String EVENT_BOSS = "boss";
    public static final String EVENT_BOSS_WITHER = "bossWither";
    public static final String EVENT_BOSS_DRAGON = "bossDragon";
    public static final String EVENT_IN_NETHER = "nether";
    public static final String EVENT_IN_END = "end";
    public static final String EVENT_HORDE = "horde";
    public static final String EVENT_NIGHT = "night";
    public static final String EVENT_RAIN = "rain";
    public static final String EVENT_UNDERWATER = "underwater";
    public static final String EVENT_UNDERGROUND = "underground";
    public static final String EVENT_VOID = "void";
    public static final String EVENT_DEEP_UNDEGROUND = "deepUnderground";
    public static final String EVENT_HIGH_UP = "highUp";
    public static final String EVENT_NETHERFORTRESS = "netherFortress";
    public static final String EVENT_ENTITY = "entity";
    public static final String EVENT_VILLAGE = "village";
    public static final String EVENT_VILLAGE_NIGHT = "villageNight";
    public static final String EVENT_MINECART = "minecart";
    public static final String EVENT_BOAT = "boat";
    public static final String EVENT_HORSE = "horse";
    public static final String EVENT_PIG = "pig";
    public static final String EVENT_FISHING = "fishing";
    public static final String EVENT_DYING = "dying";
    public static final String EVENT_PUMPKIN_HEAD = "pumpkinHead";
    public static final String EVENT_CREDITS = "credits";
    public static final String EVENT_GENERIC = "generic";
    
    public static final Map<String, String[]> eventMap = new HashMap<>();
    public static final Map<Biome, String[]> biomeMap = new HashMap<>();
    public static final Map<Biome.Category, String[]> primaryTagMap = new HashMap<>();
    public static final Map<Biome.Category, String[]> secondaryTagMap = new HashMap<>();
    public static final Map<RegistryKey<World>, String[]> dimensionMap = new HashMap<>();
    public static final Queue<RegistryKey<World>> dimensions = new LinkedList<>();
    
    public static final Random rand = new Random();
    
    public static boolean inVillage = false;
    public static boolean inFortress = false;
    public static boolean underAssault = false;
    
    public static void reset(){
        eventMap.clear();
        biomeMap.clear();
        primaryTagMap.clear();
        secondaryTagMap.clear();
        dimensionMap.clear();
    }
    
    public static String[] getSongs(){
        var mc = MinecraftClient.getInstance();
        var player = mc.player;
        var world = mc.world;
    
        if(player == null || world == null){
            return getSongsForEvent(EVENT_MAIN_MENU);
        }
    
        if(mc.currentScreen instanceof CreditsScreen){
            return getSongsForEvent(EVENT_CREDITS);
        }
        
        BlockPos pos = player.getBlockPos();
        
        var result = AmbienceEventEvent.PRE.invoker().invoke(world, pos)
            .map(SongPicker::getSongsForEvent);
        if(result.isPresent()){
            return result.get();
        }
        
        var bossOverlay = mc.inGameHud.getBossBarHud();
        Map<UUID, ClientBossBar> map = ((BossBarHudAccessor)bossOverlay).getBossBars();
        if(!map.isEmpty()){
            for(var bar : map.values()){
                var name = bar.getName();
                String[] songs = null;
                if(name instanceof TranslatableText text){
                    songs = switch(text.getKey()){
                        case "entity.minecraft.wither" -> getSongsForEvent(EVENT_BOSS_WITHER);
                        case "entity.minecraft.ender_dragon" -> getSongsForEvent(EVENT_BOSS_DRAGON);
                        default -> null;
                    };
                }
                if(songs != null){
                    return songs;
                }
            }
            String[] songs = getSongsForEvent(EVENT_BOSS);
            if(songs != null){
                return songs;
            }
        }
        
        float hp = player.getHealth();
        if(hp < 7){
            String[] songs = getSongsForEvent(EVENT_DYING);
            if(songs != null){
                return songs;
            }
        }
        
        int monsterCount = world.getEntitiesByClass(MobEntity.class, new Box(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16), (ignored)->true).size();
        int witherCount = world.getEntitiesByClass(WitherSkeletonEntity.class, new Box(player.getX() - 64, player.getY() - 16, player.getZ() - 64, player.getX() + 64, player.getY() + 16, player.getZ() + 64), (ignored)->true).size();
        
        if(underAssault && monsterCount < 2){
            underAssault = false;
        }
        
        if((monsterCount > 8 && witherCount < 3) || underAssault){
            String[] songs = getSongsForEvent(EVENT_HORDE);
            if(songs != null){
                underAssault = true;
            }
            return songs;
        }
        
        if(player.fishHook != null){
            String[] songs = getSongsForEvent(EVENT_FISHING);
            if(songs != null){
                return songs;
            }
        }
        
        ItemStack headItem = player.getEquippedStack(EquipmentSlot.HEAD);
        if(headItem != null && headItem.isOf(Items.PUMPKIN)){
            String[] songs = getSongsForEvent(EVENT_PUMPKIN_HEAD);
            if(songs != null){
                return songs;
            }
        }
        var indimension = world.getRegistryKey();
        
        if(World.NETHER.equals(indimension)){
            if(witherCount >= 3){
                String[] songs = getSongsForEvent(EVENT_NETHERFORTRESS);
                if(songs != null){
                    return songs;
                }
            }
            String[] songs = getSongsForEvent(EVENT_IN_NETHER);
            if(songs != null){
                return songs;
            }
        }else if(World.END.equals(indimension)){
            String[] songs = getSongsForEvent(EVENT_IN_END);
            if(songs != null){
                return songs;
            }
        }
        
        while(!dimensions.isEmpty()){
            var dim = dimensions.remove();
            if(dim.equals(indimension)){
                return dimensionMap.get(dim);
            }
        }
        
        var riding = player.getVehicle();
        if(riding != null){
            if(riding instanceof MinecartEntity){
                String[] songs = getSongsForEvent(EVENT_MINECART);
                if(songs != null){
                    return songs;
                }
            }
            if(riding instanceof BoatEntity){
                String[] songs = getSongsForEvent(EVENT_BOAT);
                if(songs != null){
                    return songs;
                }
            }
            if(riding instanceof HorseEntity){
                String[] songs = getSongsForEvent(EVENT_HORSE);
                if(songs != null){
                    return songs;
                }
            }
            if(riding instanceof PigEntity){
                String[] songs = getSongsForEvent(EVENT_PIG);
                if(songs != null){
                    return songs;
                }
            }
        }
        
        if(player.getBlockStateAtPos().getMaterial() == Material.WATER){
            String[] songs = getSongsForEvent(EVENT_UNDERWATER);
            if(songs != null){
                return songs;
            }
        }
        
        // TODO Is there a better way?
        long time = world.getTime() % 24000;
        boolean night = time > 13300 && time < 23200;
        
        if(world.getDimension().isNatural()){
            boolean underground = !world.isSkyVisible(pos);
            
            int villagerCount = world.getEntitiesByClass(VillagerEntity.class, new Box(player.getX() - 30, player.getY() - 8, player.getZ() - 30, player.getX() + 30, player.getY() + 8, player.getZ() + 30), (ignored)->true).size();
            if(villagerCount > 3){
                if(night){
                    String[] songs = getSongsForEvent(EVENT_VILLAGE_NIGHT);
                    if(songs != null){
                        return songs;
                    }
                }
                
                String[] songs = getSongsForEvent(EVENT_VILLAGE);
                if(songs != null){
                    return songs;
                }
            }
            
            if(underground){
                if(pos.getY() < 20){
                    String[] songs = getSongsForEvent(EVENT_DEEP_UNDEGROUND);
                    if(songs != null){
                        return songs;
                    }
                }
                if(pos.getY() < 55){
                    String[] songs = getSongsForEvent(EVENT_UNDERGROUND);
                    if(songs != null){
                        return songs;
                    }
                }
            }
            
            if(pos.getY() < 0){
                String[] songs = getSongsForEvent(EVENT_VOID);
                if(songs != null){
                    return songs;
                }
            }
            
            if(world.isRaining()){
                String[] songs = getSongsForEvent(EVENT_RAIN);
                if(songs != null){
                    return songs;
                }
            }
            
            if(pos.getY() > 196){
                String[] songs = getSongsForEvent(EVENT_HIGH_UP);
                if(songs != null){
                    return songs;
                }
            }
            
            if(night){
                String[] songs = getSongsForEvent(EVENT_NIGHT);
                if(songs != null){
                    return songs;
                }
            }
        }
        
        result = AmbienceEventEvent.POST.invoker().invoke(world, pos)
            .map(SongPicker::getSongsForEvent);
        if(result.isPresent()){
            return result.get();
        }
        
        if(world != null){
            var biome = world.getBiome(pos);
            if(biomeMap.containsKey(biome)){
                return biomeMap.get(biome);
            }
            
            var type = biome.getCategory();
            if(primaryTagMap.containsKey(type)){
                return primaryTagMap.get(type);
            }
            if(secondaryTagMap.containsKey(type)){
                return secondaryTagMap.get(type);
            }
        }
        
        return getSongsForEvent(EVENT_GENERIC);
    }
    
    public static String getSongsString(){
        return StringUtils.join(getSongs(), ",");
    }
    
    public static String getRandomSong(){
        String[] songChoices = getSongs();
        
        return songChoices[rand.nextInt(songChoices.length)];
    }
    
    public static String[] getSongsForEvent(String event){
        if(eventMap.containsKey(event)){
            return eventMap.get(event);
        }
        
        return null;
    }
    
    public static String getSongName(String song){
        return song == null ? "" : song.replaceAll("([^A-Z])([A-Z])", "$1 $2");
    }
}
