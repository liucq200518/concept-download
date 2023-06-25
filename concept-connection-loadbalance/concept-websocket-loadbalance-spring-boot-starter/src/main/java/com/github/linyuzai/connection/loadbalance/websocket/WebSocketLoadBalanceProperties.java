package com.github.linyuzai.connection.loadbalance.websocket;

import com.github.linyuzai.connection.loadbalance.websocket.concept.WebSocketLoadBalanceConcept;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ws 配置
 */
@Data
@ConfigurationProperties(prefix = "concept.websocket")
public class WebSocketLoadBalanceProperties {

    /**
     * 类型
     */
    private WebSocketType type = WebSocketType.AUTO;

    /**
     * 服务配置
     */
    private ServerProperties server = new ServerProperties();

    /**
     * 订阅配置
     */
    private LoadBalanceProperties loadBalance = new LoadBalanceProperties();

    @Data
    public static class ServerProperties {

        /**
         * 默认服务端点配置
         */
        private DefaultEndpointProperties defaultEndpoint = new DefaultEndpointProperties();

        /**
         * 心跳配置
         */
        private HeartbeatProperties heartbeat = new HeartbeatProperties();

        @Data
        public static class DefaultEndpointProperties {

            /**
             * 是否启用默认服务端点
             */
            private boolean enabled = true;

            /**
             * 默认端点前缀，默认值：/concept-websocket/
             */
            private String prefix = WebSocketLoadBalanceConcept.SERVER_ENDPOINT_PREFIX;

            /**
             * 路径选择器
             */
            private PathSelectorProperties pathSelector = new PathSelectorProperties();

            /**
             * 用户选择器
             */
            private UserSelectorProperties userSelector = new UserSelectorProperties();

            @Data
            public static class PathSelectorProperties {

                /**
                 * 是否启用路径选择
                 */
                private boolean enabled = false;
            }

            @Data
            public static class UserSelectorProperties {

                /**
                 * 是否启用用户选择
                 */
                private boolean enabled = false;
            }
        }
    }

    @Data
    public static class LoadBalanceProperties {

        /**
         * 订阅协议，默认 WEBSOCKET
         */
        private Protocol protocol = Protocol.WEBSOCKET;

        /**
         * 订阅日志
         */
        private boolean logger = true;

        /**
         * 监控配置
         */
        private MonitorProperties monitor = new MonitorProperties();

        /**
         * 心跳配置
         */
        private HeartbeatProperties heartbeat = new HeartbeatProperties();

        public enum Protocol {

            WEBSOCKET,
            WEBSOCKET_SSL,

            REDISSON_TOPIC,
            REDISSON_SHARED_TOPIC,

            REDIS_TOPIC
        }

        @Data
        public static class MonitorProperties {

            /**
             * 是否启用订阅监控
             */
            private boolean enabled = true;

            /**
             * 是否打印订阅监控日志
             */
            private boolean logger = false;

            /**
             * 订阅监控周期，毫秒
             */
            private long period = 30000;
        }
    }

    @Data
    public static class HeartbeatProperties {

        /**
         * 是否启用心跳
         */
        private boolean enabled = true;

        /**
         * 心跳超时时间，毫秒
         */
        private long timeout = 210000;

        /**
         * 心跳周期，毫秒
         */
        private long period = 60000;
    }
}
