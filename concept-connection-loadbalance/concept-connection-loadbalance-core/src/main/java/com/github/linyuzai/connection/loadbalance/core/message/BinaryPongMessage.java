package com.github.linyuzai.connection.loadbalance.core.message;

import java.nio.ByteBuffer;

/**
 * 二进制的 pong 消息。
 * <p>
 * Pong message has binary payload.
 */
public class BinaryPongMessage extends BinaryMessage implements PongMessage {

    public BinaryPongMessage() {
        super(new byte[0]);
    }

    public BinaryPongMessage(byte[] payload) {
        super(payload);
    }

    public BinaryPongMessage(ByteBuffer payload) {
        super(payload);
    }
}
