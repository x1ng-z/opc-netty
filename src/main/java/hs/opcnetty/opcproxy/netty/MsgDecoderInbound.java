package hs.opcnetty.opcproxy.netty;



import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opcproxy.command.CommandImp;
import hs.opcnetty.opcproxy.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;

@ChannelHandler.Sharable
@Component
public class MsgDecoderInbound extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(MsgDecoderInbound.class);
    private SessionManager sessionManager;
    private OpcConnectManger opcConnectManger;
    private Map<Byte, CommandImp> cmdipm;

    
    
    @Autowired
    public MsgDecoderInbound(SessionManager sessionManager, OpcConnectManger opcConnectManger) {
        super();
        this.sessionManager = sessionManager;
        this.opcConnectManger = opcConnectManger;
        cmdipm=CommandImp.getCmdMapping();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = ipSocket.getAddress().getHostAddress();
        Integer port = ipSocket.getPort();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = ipSocket.getAddress().getHostAddress();
        Integer port = ipSocket.getPort();
        logger.info("come out " + clientIp + ":" + port);
        sessionManager.removeSessionModule(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            ByteBuf candidatecontext = (ByteBuf) msg;
            if (candidatecontext.isReadable()) {
                byte[] bytes = new byte[candidatecontext.readableBytes()];
                candidatecontext.readBytes(bytes);
                byte[] command = Arrays.copyOfRange(bytes, 2, 3);
                CommandImp candidate=null;
                if(null!=(candidate=cmdipm.get(command[0]))){
                    candidate.operate(bytes,ctx,sessionManager,opcConnectManger);
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error(cause.getMessage(), cause);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = ipSocket.getAddress().getHostAddress();
        Integer port = ipSocket.getPort();
        logger.info(" because exception come out" + clientIp + ":" + port);
        sessionManager.removeSessionModule(ctx);


    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;

            InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIp = ipSocket.getAddress().getHostAddress();
            IdleStateEvent stateEvent = (IdleStateEvent) evt;

            switch (stateEvent.state()) {
                case READER_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                case WRITER_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                case ALL_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                default:
                    break;
            }
        }
    }
}
