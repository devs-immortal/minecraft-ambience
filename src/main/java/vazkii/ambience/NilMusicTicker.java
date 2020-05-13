package vazkii.ambience;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import vazkii.ambience.PlayerThread.*;

import static vazkii.ambience.Ambience.*;
import static vazkii.ambience.PlayerThread.*;

public class NilMusicTicker extends MusicTicker {

	public NilMusicTicker(Minecraft p_i45112_1_) {
		super(p_i45112_1_);
	}
	
	public void update() {
		// NO-OP
		if(nextSong != null && player != null){
			thread.fadeStep(-0.2f);
			fading = true;
		}
		if(nextSong == null && player != null && fading == true){
			fading = false;
		}
		if(!fading && realGainHolder < 0f){
			thread.fadeStep(0.2f);
		}
	}

}
