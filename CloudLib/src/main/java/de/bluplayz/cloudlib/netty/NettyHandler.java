package de.bluplayz.cloudlib.netty;

import de.bluplayz.cloudlib.netty.client.NettyClient;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.server.NettyServer;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;

public class NettyHandler {

    public static boolean DEBUGMODE = false;

    @Getter
    private static List<PacketHandler> packetHandlers = new ArrayList<>();

    @Getter
    private static List<ConnectionListener> connectionListeners = new ArrayList<>();

    @Getter
    private static Map<String, Channel> clients = new LinkedHashMap<>();

    @Getter
    private static NettyHandler instance;

    @Getter
    private Map<UUID, ArrayList<Consumer<Packet>>> packetCallbacks = new HashMap<>();

    @Getter
    @Setter
    private PacketHandler handler;

    @Getter
    @Setter
    private Type type = Type.CLIENT;

    @Getter
    private NettyClient nettyClient;

    @Getter
    private NettyServer nettyServer;

    public NettyHandler() {
        NettyHandler.instance = this;

        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
            @Override
            public void run() {
                if ( NettyHandler.this.getNettyClient() != null ) {
                    NettyHandler.this.getNettyClient().disconnect();
                }
                if ( NettyHandler.this.getNettyServer() != null ) {
                    NettyHandler.this.getNettyServer().stopServer();
                }
            }
        } ) );
    }

    public void reconnectToServer( Consumer<Boolean> consumer ) {
        if ( this.getNettyClient() == null ) {
            return;
        }

        this.getNettyClient().reconnect();
    }

    public void reconnectToServer( int delaySeconds, Consumer<Boolean> consumer ) {
        if ( this.getNettyClient() == null ) {
            return;
        }

        this.getNettyClient().scheduleConnect( delaySeconds * 1000 );
    }

    public void connectToServer( String host, int port, Consumer<Boolean> consumer ) {
        this.type = Type.CLIENT;

        this.unregisterAllPacketHandler();
        this.unregisterAllConnectionListener();

        // Close Existing Server Connection
        if ( this.getNettyServer() != null ) {
            this.getNettyServer().stopServer();
        }

        // Close Existing Client Connection
        if ( this.getNettyClient() != null ) {
            this.getNettyClient().disconnect();
        }


        this.nettyClient = new NettyClient();
        this.nettyClient.connect( host, port, consumer );
    }

    public void startServer( int port, Consumer<Boolean> consumer ) {
        this.type = Type.SERVER;

        this.unregisterAllPacketHandler();
        this.unregisterAllConnectionListener();

        // Close Existing Server Connection
        if ( this.getNettyServer() != null ) {
            this.getNettyServer().stopServer();
        }

        // Close Existing Client Connection
        if ( this.getNettyClient() != null ) {
            this.getNettyClient().disconnect();
        }

        this.nettyServer = new NettyServer();
        this.nettyServer.startServer( port, consumer );
    }

    public void registerPacketHandler( PacketHandler handler ) {
        if ( NettyHandler.getPacketHandlers().contains( handler ) ) {
            return;
        }

        NettyHandler.getPacketHandlers().add( handler );
    }

    public void unregisterPacketHandler( PacketHandler handler ) {
        if ( !NettyHandler.getPacketHandlers().contains( handler ) ) {
            return;
        }

        NettyHandler.getPacketHandlers().remove( handler );
    }

    public void unregisterAllPacketHandler() {
        NettyHandler.getPacketHandlers().clear();
    }

    public void registerConnectionListener( ConnectionListener handler ) {
        if ( NettyHandler.getConnectionListeners().contains( handler ) ) {
            return;
        }

        NettyHandler.getConnectionListeners().add( handler );
    }

    public void unregisterConnectionListener( ConnectionListener handler ) {
        if ( !NettyHandler.getConnectionListeners().contains( handler ) ) {
            return;
        }

        NettyHandler.getConnectionListeners().remove( handler );
    }

    public void unregisterAllConnectionListener() {
        NettyHandler.getConnectionListeners().clear();
    }

    public String getClientnameByChannel( Channel channel ) {
        for ( Map.Entry entry : NettyHandler.getClients().entrySet() ) {
            if ( entry.getValue() == channel ) {
                return (String) entry.getKey();
            }
        }

        return "";
    }

    public void addPacketCallback( Packet packet, Consumer<Packet> consumer ) {
        if ( !this.getPacketCallbacks().containsKey( packet.getUniqueId() ) ) {
            this.getPacketCallbacks().put( packet.getUniqueId(), new ArrayList<>() );
        }

        this.getPacketCallbacks().get( packet.getUniqueId() ).add( consumer );
    }

    public void removePacketCallbacks( Packet packet ) {
        if ( !this.getPacketCallbacks().containsKey( packet.getUniqueId() ) ) {
            return;
        }

        this.getPacketCallbacks().remove( packet.getUniqueId() );
    }

    public void runPacketCallbacks( Packet packet ) {
        if ( !this.getPacketCallbacks().containsKey( packet.getUniqueId() ) ) {
            return;
        }

        for ( Consumer<Packet> consumer : this.getPacketCallbacks().get( packet.getUniqueId() ) ) {
            consumer.accept( packet );
        }
    }

    public enum Type {
        SERVER,
        CLIENT
    }
}
