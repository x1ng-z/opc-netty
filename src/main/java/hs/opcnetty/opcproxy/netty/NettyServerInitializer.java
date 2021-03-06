package hs.opcnetty.opcproxy.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    public MsgDecoderInbound msgDecoder_inbound;

    @Autowired
    public MsgencoderOutbound msgencoder_outbound;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline=socketChannel.pipeline();
        pipeline.addLast("msgendcode1", msgencoder_outbound);
        pipeline.addLast("inlengthcontrl",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,7,3,0,0));
        pipeline.addLast("msgdecode1", msgDecoder_inbound);
    }
}
