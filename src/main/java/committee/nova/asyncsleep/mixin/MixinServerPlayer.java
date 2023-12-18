package committee.nova.asyncsleep.mixin;

import committee.nova.asyncsleep.api.IServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements IServerPlayer {
    @Unique
    private boolean asyncsleep$everSlept;

    @Inject(method = "startSleeping", at = @At("HEAD"))
    private void inject$startSleeping(BlockPos pos, CallbackInfo ci) {
        this.asyncsleep$everSlept = true;
    }

    @Override
    public boolean asyncsleep$hasEverSlept() {
        return asyncsleep$everSlept;
    }

    @Override
    public void asyncsleep$setEverSlept(boolean slept) {
        this.asyncsleep$everSlept = slept;
    }
}
