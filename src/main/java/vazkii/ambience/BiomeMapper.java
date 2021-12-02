package vazkii.ambience;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public final class BiomeMapper{
    
    public static Biome getBiome(String s){
        return MinecraftClient.getInstance().world.getRegistryManager().get(Registry.BIOME_KEY).get(new Identifier(s));
    }
    
    private static final Map<String, Biome.Category> CATEGORY_MAP = Stream.of(Biome.Category.values())
        .collect(Collectors.toUnmodifiableMap(Biome.Category::asString, Function.identity()));
    
    public static @Nullable Biome.Category getBiomeType(String s){
        return CATEGORY_MAP.get(s.toLowerCase(Locale.ROOT));
    }
    
}
