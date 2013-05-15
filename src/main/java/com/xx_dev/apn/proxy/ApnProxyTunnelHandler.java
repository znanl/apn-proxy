package com.xx_dev.apn.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author mingxing.xumx
 * @version $Id: ApnProxyTunnelHandler.java, v 0.1 2013-5-15 上午11:18:36 mingxing.xumx Exp $
 */
public class ApnProxyTunnelHandler extends ChannelInboundMessageHandlerAdapter<HttpObject> {

    /** 
     * @see io.netty.channel.ChannelHandlerUtil.SingleInboundMessageHandler#messageReceived(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void messageReceived(final ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            String hostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);
            String remoteHost = getHostName(hostHeader);
            int remotePort = getPort(hostHeader);
            final String remoteAddr = remoteHost + ":" + remotePort;

            Channel uaChannel = ctx.channel();

            // connect remote
            Bootstrap b = new Bootstrap();
            b.group(uaChannel.eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ApnProxyTunnelChannelInitializer(remoteAddr + " --> UA", uaChannel));
            b.connect(remoteHost, remotePort).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future1) throws Exception {
                    if (future1.isSuccess()) {
                        // successfully connect to the original server
                        // send connect success msg to UA

                        HttpResponse proxyConnectSuccessResponse = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
                                "Connection established"));
                        ctx.write(proxyConnectSuccessResponse);

                        ctx.flush().addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future2) throws Exception {
                                // remove handlers
                                ctx.pipeline().remove("codec");
                                ctx.pipeline().remove("handler2");

                                // add relay handler
                                ctx.pipeline().addLast(
                                    new ApnProxyRelayHandler("UA --> " + remoteAddr, future1
                                        .channel()));
                            }

                        });

                    } else {
                        if (ctx.channel().isActive()) {
                            ctx.channel().flush().addListener(ChannelFutureListener.CLOSE);
                        }
                    }
                }
            });
        }

    }

    private static String getHostName(String addr) {
        return StringUtils.split(addr, ": ")[0];
    }

    private static int getPort(String addr) {
        String[] ss = StringUtils.split(addr, ": ");
        if (ss.length == 2) {
            return Integer.parseInt(ss[1]);
        }
        return 443;
    }
}