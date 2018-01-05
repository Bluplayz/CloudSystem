package de.bluplayz.cloudmaster.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.server.template.Template;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ServerManager {

    @Getter
    private List<CloudWrapper> cloudWrappers = new LinkedList<>();

    public CloudWrapper addCloudWrapper( Channel channel ) {
        // Check for existing CloudWrapper with this channel
        if ( this.getCloudWrapperByChannel( channel ) != null ) {
            return null;
        }

        CloudWrapper cloudWrapper = new CloudWrapper();
        cloudWrapper.onConnect( channel );
        this.getCloudWrappers().add( cloudWrapper );
        return cloudWrapper;
    }

    public CloudWrapper removeCloudWrapper( Channel channel ) {
        CloudWrapper cloudWrapper = this.getCloudWrapperByChannel( channel );
        if ( cloudWrapper == null ) {
            return null;
        }

        cloudWrapper.onDisconnect();
        this.getCloudWrappers().remove( cloudWrapper );
        return cloudWrapper;
    }

    public CloudWrapper getCloudWrapperByChannel( Channel channel ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            if ( cloudWrapper.getChannel() == channel ) {
                return cloudWrapper;
            }
        }

        return null;
    }

    public CloudWrapper getCloudWrapperByName( String name ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            if ( cloudWrapper.getName().equalsIgnoreCase( name ) ) {
                return cloudWrapper;
            }
        }

        return null;
    }

    public CloudWrapper getCloudWrapperByServer( SpigotServer spigotServer ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            if ( cloudWrapper.getSpigotServers().contains( spigotServer ) ) {
                return cloudWrapper;
            }
        }

        return null;
    }

    public CloudWrapper getCloudWrapperByProxy( BungeeCordProxy bungeeCordProxy ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            if ( cloudWrapper.getBungeeCordProxies().contains( bungeeCordProxy ) ) {
                return cloudWrapper;
            }
        }

        return null;
    }

    public SpigotServer getServerByName( String name ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            for ( SpigotServer spigotServer : cloudWrapper.getSpigotServers() ) {
                if ( spigotServer.getName().equals( name ) ) {
                    return spigotServer;
                }
            }
        }

        return null;
    }

    public BungeeCordProxy getProxyByName( String name ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            for ( BungeeCordProxy bungeeCordProxy : cloudWrapper.getBungeeCordProxies() ) {
                if ( bungeeCordProxy.getName().equals( name ) ) {
                    return bungeeCordProxy;
                }
            }
        }

        return null;
    }

    public List<SpigotServer> getServersByTemplate( Template template ) {
        List<SpigotServer> servers = new ArrayList<>();

        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            for ( SpigotServer spigotServer : cloudWrapper.getSpigotServers() ) {
                if ( spigotServer.getTemplate() == template ) {
                    servers.add( spigotServer );
                }
            }
        }

        return servers;
    }

    public List<BungeeCordProxy> getProxiesByTemplate( Template template ) {
        List<BungeeCordProxy> proxies = new ArrayList<>();

        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            for ( BungeeCordProxy bungeeCordProxy : cloudWrapper.getBungeeCordProxies() ) {
                if ( bungeeCordProxy.getTemplate() == template ) {
                    proxies.add( bungeeCordProxy );
                }
            }
        }

        return proxies;
    }

    public List<SpigotServer> getServers() {
        List<SpigotServer> servers = new ArrayList<>();

        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            servers.addAll( cloudWrapper.getSpigotServers() );
        }

        return servers;
    }

    public List<BungeeCordProxy> getProxies() {
        List<BungeeCordProxy> proxies = new ArrayList<>();

        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            proxies.addAll( cloudWrapper.getBungeeCordProxies() );
        }

        return proxies;
    }

    public void checkForServers() {
        CloudMaster.getInstance().getSynchronizedPool().execute( () -> {
            if ( this.getCloudWrappers().size() == 0 ) {
                return;
            }

            for ( Template template : Template.getAllTemplates() ) {
                if ( template.getType() == Template.Type.PROXY ) {
                    List<BungeeCordProxy> proxies = this.getProxiesByTemplate( template );
                    if ( proxies.size() < template.getMinOnlineServers() ) {
                        int neededServer = template.getMinOnlineServers() - proxies.size();
                        for ( int i = 0; i < neededServer; i++ ) {
                            CloudWrapper bestCloudWrapper = this.getCloudWrappers().get( 0 );
                            for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
                                if ( ( cloudWrapper.getBungeeCordProxies().size() + cloudWrapper.getSpigotServers().size() ) <= ( bestCloudWrapper.getBungeeCordProxies().size() + bestCloudWrapper.getSpigotServers().size() ) ) {
                                    bestCloudWrapper = cloudWrapper;
                                }
                            }

                            bestCloudWrapper.startProxies( template );
                        }
                    }
                } else if ( template.getType() == Template.Type.SPIGOT ) {
                    List<SpigotServer> servers = this.getServersByTemplate( template );
                    if ( servers.size() < template.getMinOnlineServers() ) {
                        int neededServer = template.getMinOnlineServers() - servers.size();
                        for ( int i = 0; i < neededServer; i++ ) {
                            CloudWrapper bestCloudWrapper = this.getCloudWrappers().get( 0 );
                            for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
                                if ( ( cloudWrapper.getBungeeCordProxies().size() + cloudWrapper.getSpigotServers().size() ) <= ( bestCloudWrapper.getBungeeCordProxies().size() + bestCloudWrapper.getSpigotServers().size() ) ) {
                                    bestCloudWrapper = cloudWrapper;
                                }
                            }

                            bestCloudWrapper.startServers( template );
                        }
                    }
                }
            }
        } );
    }
}
