package committee.nova.asyncsleep.mixin;

import committee.nova.asyncsleep.api.IServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(SleepStatus.class)
public abstract class MixinSleepStatus {
    @Unique
    private boolean asyncsleep$atLeast1PlayerSleeping = false;

    @Shadow
    private int activePlayers;

    @Shadow
    private int sleepingPlayers;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void inject$update(List<ServerPlayer> playerList, CallbackInfoReturnable<Boolean> cir) {
        int i = this.activePlayers;
        int j = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;

        asyncsleep$atLeast1PlayerSleeping = false;

        for (ServerPlayer serverplayer : playerList) {
            if (!serverplayer.isSpectator()) {
                ++this.activePlayers;
                if (serverplayer.isSleeping() || ((IServerPlayer) serverplayer).asyncsleep$hasEverSlept()) {
                    ++this.sleepingPlayers;
                    asyncsleep$atLeast1PlayerSleeping |= serverplayer.isSleeping();
                }
            }
        }

        cir.setReturnValue(
                asyncsleep$atLeast1PlayerSleeping &&
                        (j > 0 || this.sleepingPlayers > 0) &&
                        (i != this.activePlayers || j != this.sleepingPlayers)
        );
    }

    @Redirect(
            method = "areEnoughDeepSleeping",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"
            )
    )
    private <T extends ServerPlayer> Stream<T> redirect$areEnoughDeepSleeping(Stream<T> instance, Predicate<? super T> predicate) {
        return instance.filter(s -> ((IServerPlayer) s).asyncsleep$hasEverSlept() || s.isSleepingLongEnough());
    }

    @Inject(method = "areEnoughSleeping", at = @At("HEAD"), cancellable = true)
    private void inject$areEnoughSleeping(int s, CallbackInfoReturnable<Boolean> cir) {
        if (!asyncsleep$atLeast1PlayerSleeping) cir.setReturnValue(false);
    }
}
