package com.xx_dev.apn.proxy;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.log4j.Logger;

public class IdleHandler extends ChannelDuplexHandler {

    private static Logger logger = Logger.getLogger(IdleHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            if (logger.isDebugEnabled()) {
                logger.debug("idle event fired!");
            }

            ctx.channel().close();
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.flush(promise);
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        ctx.fireInboundBufferUpdated();
    }
}
