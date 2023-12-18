package committee.nova.asyncsleep.mixin;

import committee.nova.asyncsleep.api.IServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @Shadow
    public abstract List<ServerPlayer> players();

    @Inject(method = "wakeUpAllPlayers", at = @At("TAIL"))
    private void inject$wakeUpAllPlayers(CallbackInfo ci) {
        players().forEach(s -> ((IServerPlayer) s).asyncsleep$setEverSlept(false));
    }
}
