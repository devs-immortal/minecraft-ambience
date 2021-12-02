package vazkii.ambience;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// top lel name
// works as an api, feel free to include in your mods to add custom events
public interface AmbienceEventEvent{
    Event<Pre> PRE = EventFactory.createArrayBacked(Pre.class, (callbacks)->(world, pos)->{
        for(var callback : callbacks){
            var result = callback.invoke(world, pos);
            if(result.isPresent()){
                return result;
            }
        }
        return Optional.empty();
    });
    
    Event<Post> POST = EventFactory.createArrayBacked(Post.class, (callbacks)->(world, pos)->{
        for(var callback : callbacks){
            var result = callback.invoke(world, pos);
            if(result.isPresent()){
                return result;
            }
        }
        return Optional.empty();
    });
    
    Optional<String> invoke(World world, BlockPos pos);
    
    interface Pre extends AmbienceEventEvent{}
    
    interface Post extends AmbienceEventEvent{}
}
