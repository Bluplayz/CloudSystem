package de.bluplayz.netty;

import de.bluplayz.netty.client.NettyClient;
import de.bluplayz.netty.server.NettyServer;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NettyHandler {

    public static boolean DEBUGMODE = false;

    @Getter
    private static ArrayList<PacketHandler> packetHandlers = new ArrayList<>();

    @Getter
    private static ArrayList<ConnectionListener> connectionListeners = new ArrayList<>();

    @Getter
    private static HashMap<String, Channel> clients = new HashMap<>();

    @Getter
    private static NettyHandler instance;

    @Getter
    @Setter
    private PacketHandler handler;

    @Getter
    @Setter
    private types type = types.CLIENT;

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
        this.type = types.CLIENT;

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
        this.type = types.SERVER;

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

    public enum types {
        SERVER,
        CLIENT
    }
}
