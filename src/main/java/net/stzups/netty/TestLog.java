package net.stzups.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.stzups.util.LogFactory;

import java.util.logging.Logger;

public class TestLog {
    private static final Logger LOGGER = LogFactory.getLogger("scribbleshare");
    public static Logger getLogger() {
        return LOGGER;
    }

    private static final AttributeKey<Logger> LOGGER_KEY = AttributeKey.valueOf(TestLog.class, "LOGGER");
    public static void setLogger(Channel channel) {
        channel.attr(LOGGER_KEY).set(LogFactory.getLogger(channel.remoteAddress().toString()));
    }
    public static Logger getLogger(ChannelHandlerContext ctx) {
        return ctx.channel().attr(LOGGER_KEY).get();
    }
}
