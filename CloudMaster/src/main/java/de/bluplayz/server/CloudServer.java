package de.bluplayz.server;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudServer {

    public static final AtomicInteger staticId = new AtomicInteger( 0 );

    @Getter
    public Channel channel;

    @Getter
    public List<BungeeCordProxy> bungeeCordProxies = new LinkedList<>();

    @Getter
    public List<SpigotServer> spigotServers = new LinkedList<>();

    @Getter
    private int id = 0;

    @Getter
    private String name = "";

    public void onConnect( Channel channel ) {
        this.channel = channel;
        this.id = staticId.incrementAndGet();
        this.name = "CloudServer-" + this.id;
    }

    public void onDisconnect() {
        this.channel = null;
    }

    public void startServers( SpigotServer... spigotServers ) {
        // TODO

        for ( SpigotServer spigotServer : spigotServers ) {
            spigotServer.startServer();
        }
    }

    public void startProxies( BungeeCordProxy... bungeeCordProxies ) {
        // TODO

        for ( BungeeCordProxy bungeeCordProxy : bungeeCordProxies ) {
            bungeeCordProxy.startProxy();
        }
    }
}
