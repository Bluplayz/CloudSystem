package de.bluplayz.server;

import de.bluplayz.logging.Logger;
import de.bluplayz.server.template.Template;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudWrapper {

    public static final AtomicInteger STATIC_ID = new AtomicInteger( 0 );

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
        this.id = CloudWrapper.STATIC_ID.incrementAndGet();
        this.name = this.getClass().getSimpleName() + "-" + this.id;
    }

    public void onDisconnect() {
        for ( BungeeCordProxy bungeeCordProxy : this.getBungeeCordProxies() ) {
            bungeeCordProxy.shutdown();
        }
        for ( SpigotServer spigotServer : this.getSpigotServers() ) {
            spigotServer.shutdown();
        }

        this.channel = null;
    }

    public void startServers( Template... templates ) {
        List<SpigotServer> spigotServers = new ArrayList<>();
        for ( Template template : templates ) {
            spigotServers.add( new SpigotServer( this, template ) );
        }

        for ( SpigotServer spigotServer : spigotServers ) {
            spigotServer.startServer();
        }
    }

    public void startProxies( Template... templates ) {
        List<BungeeCordProxy> bungeeCordProxies = new ArrayList<>();
        for ( Template template : templates ) {
            bungeeCordProxies.add( new BungeeCordProxy( this, template ) );
        }

        for ( BungeeCordProxy bungeeCordProxy : bungeeCordProxies ) {
            bungeeCordProxy.startProxy();
        }
    }
}
