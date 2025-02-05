package com.github.linyuzai.connection.loadbalance.core.subscribe;

import com.github.linyuzai.connection.loadbalance.core.concept.Connection;
import com.github.linyuzai.connection.loadbalance.core.concept.ConnectionLoadBalanceConcept;
import com.github.linyuzai.connection.loadbalance.core.server.ConnectionServer;
import lombok.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 服务实例连接订阅者。
 * <p>
 * Subscriber of connection of service instance.
 */
public abstract class ServerInstanceConnectionSubscriber<T extends Connection> implements ConnectionSubscriber {

    @Override
    public void subscribe(Consumer<Connection> onSuccess,
                          Consumer<Throwable> onError,
                          Runnable onComplete,
                          ConnectionLoadBalanceConcept concept) {
        List<ConnectionServer> servers = concept.getConnectionServerManager()
                .getConnectionServers();
        subscribe(0, servers, onSuccess, onError, onComplete, concept);
    }

    protected void subscribe(int index,
                             List<ConnectionServer> servers,
                             Consumer<Connection> onSuccess,
                             Consumer<Throwable> onError,
                             Runnable onComplete,
                             ConnectionLoadBalanceConcept concept) {
        if (index < servers.size()) {
            ConnectionServer server = servers.get(index);
            subscribe(onSuccess, onError, () ->
                            subscribe(index + 1, servers, onSuccess, onError, onComplete, concept),
                    server, concept);
        } else {
            onComplete.run();
        }
    }

    public void subscribe(ConnectionServer server,
                          ConnectionLoadBalanceConcept concept) {
        subscribe(ConnectionSubscriber.onSubscribeSuccess(concept),
                ConnectionSubscriber.onSubscribeError(concept), () -> {
                }, server, concept);
    }

    /**
     * 尝试订阅服务实例
     * <p>
     * 当对应的连接存在时
     * <p>
     * 如果存活 {@link Connection#isAlive()} 就不进行重复订阅
     * <p>
     * 否则关闭之前的连接并重新订阅
     * <p>
     * 加锁防止主动订阅和被动订阅冲突
     * <p>
     * 当接收到服务实例信息时会反向订阅 {@link ConnectionSubscribeHandler}
     *
     * @param server 需要订阅的服务实例
     */
    public void subscribe(Consumer<Connection> onSuccess,
                          Consumer<Throwable> onError,
                          Runnable onComplete,
                          ConnectionServer server,
                          ConnectionLoadBalanceConcept concept) {
        //需要判断是否已经订阅对应的服务
        Connection exist = getSubscriberConnection(server, concept);
        if (exist != null) {
            if (exist.isAlive()) {
                //如果连接还存活则直接返回
                onComplete.run();
                return;
            } else {
                //否则关闭连接
                exist.close(Connection.Close.NOT_ALIVE);
            }
        }
        doSubscribe(server, concept, connection -> {
            onSuccess.accept(connection);
            ConnectionServer local = concept.getConnectionServerManager().getLocal();
            if (local != null) {
                connection.send(concept.createMessage(local));
            }
        }, e -> onError.accept(new ConnectionServerSubscribeException(server, e.getMessage(), e)), onComplete);
    }

    /**
     * 获得订阅了对应服务实例的连接
     * <p>
     * 如不存在则返回 null
     *
     * @param server 服务实例
     * @return 对应的连接或 null
     */
    public Connection getSubscriberConnection(ConnectionServer server, ConnectionLoadBalanceConcept concept) {
        if (server == null) {
            return null;
        }
        Collection<Connection> connections = concept.getConnectionRepository()
                .select(Connection.Type.SUBSCRIBER);
        for (Connection connection : connections) {
            ConnectionServer exist = (ConnectionServer) connection.getMetadata().get(ConnectionServer.class);
            if (concept.getConnectionServerManager().isEqual(server, exist)) {
                return connection;
            }
        }
        return null;
    }

    public abstract void doSubscribe(ConnectionServer server, ConnectionLoadBalanceConcept concept,
                                     Consumer<T> onSuccess,
                                     Consumer<Throwable> onError,
                                     Runnable onComplete);

    /**
     * 获得对应服务的连接 host
     * <p>
     * 如果手动定义了值则使用手动定义的
     * <p>
     * 否则使用服务的 host
     *
     * @param server 服务实例
     * @return 对应服务的连接 host
     */
    public String getHost(ConnectionServer server) {
        String host = server.getMetadata().get(getHostKey());
        if (host == null || host.isEmpty()) {
            return server.getHost();
        }
        return host;
    }

    /**
     * 获得对应服务的连接 port
     * <p>
     * 如果手动定义了值则使用手动定义的
     * <p>
     * 否则使用服务的 port
     *
     * @param server 服务实例
     * @return 对应服务的连接 port
     */
    public int getPort(ConnectionServer server) {
        String port = server.getMetadata().get(getPortKey());
        if (port == null || port.isEmpty()) {
            return server.getPort();
        }
        return Integer.parseInt(port);
    }

    /**
     * 拼接对应服务实例连接的 {@link URI}
     *
     * @param server 服务实例
     * @return 对应的 {@link URI}
     */
    @SneakyThrows
    public URI getUri(ConnectionServer server) {
        return new URI(getProtocol() + "://" + getHost(server) + ":" + getPort(server) +
                getEndpoint());
    }

    public String getHostKey() {
        return "concept-connection-host";
    }

    public String getPortKey() {
        return "concept-connection-port";
    }

    /**
     * 获得端点协议
     *
     * @return 端点协议
     */
    public abstract String getProtocol();

    /**
     * 获得端点路径
     *
     * @return 端点路径
     */
    public abstract String getEndpoint();
}
