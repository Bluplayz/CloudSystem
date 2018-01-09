package de.bluplayz.cloudlib.netty;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.DisconnectPacket;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.packet.*;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

public abstract class PacketHandler {

    public static List<Class<? extends Packet>> PACKETS = new ArrayList<>();
    public static List<Packet> PACKETS_TO_SEND = new ArrayList<>();

    public PacketHandler() {
        // Register default Packets
        PacketHandler.PACKETS.add( DisconnectPacket.class );
        PacketHandler.PACKETS.add( SetNamePacket.class );

        PacketHandler.PACKETS.add( StartServerPacket.class );
        //PacketHandler.PACKETS.add( StopServerPacket.class ); TODO

        PacketHandler.PACKETS.add( ServerStartedPacket.class );
        PacketHandler.PACKETS.add( ServerStoppedPacket.class );

        PacketHandler.PACKETS.add( RegisterServerPacket.class );
        PacketHandler.PACKETS.add( UnregisterServerPacket.class );

        PacketHandler.PACKETS.add( SaveServerPacket.class );
        PacketHandler.PACKETS.add( DispatchCommandPacket.class );
        PacketHandler.PACKETS.add( VerifyPlayerPacket.class );

        this.registerPackets();
    }

    public static void sendPacketDirectly( Packet packet, Channel channel ) {
        if ( channel == null ) {
            return;
        }

        channel.writeAndFlush( packet, channel.voidPromise() );
    }

    public void sendPacket( Packet packet ) {
        if ( NettyHandler.getInstance().getType() == NettyHandler.Type.CLIENT ) {
            if ( NettyHandler.getInstance().getNettyClient().getChannel() == null ) {
                PacketHandler.PACKETS_TO_SEND.add( packet );
                return;
            }

            this.sendPacket( packet, NettyHandler.getInstance().getNettyClient().getChannel() );
        } else {
            if ( NettyHandler.getClients().size() == 0 ) {
                PacketHandler.PACKETS_TO_SEND.add( packet );
                return;
            }

            for ( Channel channel : NettyHandler.getClients().values() ) {
                this.sendPacket( packet, channel );
            }
        }
    }

    public void sendPacket( Packet packet, Channel channel ) {
        if ( channel == null ) {
            return;
        }

        channel.writeAndFlush( packet, channel.voidPromise() );
    }

    public void registerPacket( Class<? extends Packet> packet ) {
        if ( PacketHandler.PACKETS.contains( packet ) ) {
            return;
        }

        PacketHandler.PACKETS.add( packet );
    }

    public abstract void incomingPacket( Packet packet, Channel channel );

    public abstract void registerPackets();
}
