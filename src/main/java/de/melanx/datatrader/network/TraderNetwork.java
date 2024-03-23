package de.melanx.datatrader.network;

import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class TraderNetwork extends NetworkX {

    public TraderNetwork(ModX mod) {
        super(mod);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("1");
    }

    @Override
    protected void registerPackets() {
        this.registerGame(NetworkDirection.PLAY_TO_SERVER, new SelectTrade.Serializer(), () -> SelectTrade.Handler::new);

        this.registerGame(NetworkDirection.PLAY_TO_CLIENT, new SyncTraderOffers.Serializer(), () -> SyncTraderOffers.Handler::new);
    }

    public void selectTrade(int item) {
        this.channel.sendToServer(new SelectTrade(item));
    }

    public void syncTrades(Player player, int containerId, TraderOffers offers) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SyncTraderOffers(containerId, offers));
        }
    }
}
