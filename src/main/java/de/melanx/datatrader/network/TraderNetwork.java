package de.melanx.datatrader.network;

import de.melanx.datatrader.network.handler.SelectTrade;
import de.melanx.datatrader.network.handler.SyncTraderOffers;
import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.network.NetworkX;

public class TraderNetwork extends NetworkX {

    public TraderNetwork(ModX mod) {
        super(mod);

        // send to server
        this.register(new SelectTrade());

        // send to client
        this.register(new SyncTraderOffers());
    }

    @Override
    protected String getVersion() {
        return "2";
    }

    public void selectTrade(int item) {
        PacketDistributor.sendToServer(new SelectTrade.Message(item));
    }

    public void syncTrades(Player player, int containerId, TraderOffers offers) {
        if (!player.getCommandSenderWorld().isClientSide) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncTraderOffers.Message(containerId, offers));
        }
    }
}
