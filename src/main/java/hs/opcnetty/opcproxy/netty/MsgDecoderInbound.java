package hs.opcnetty.opcproxy.netty;


import com.alibaba.fastjson.JSONObject;

import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opcproxy.command.CommandImp;
import hs.opcnetty.opcproxy.session.Session;
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
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
@Component
public class MsgDecoderInbound extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(MsgDecoderInbound.class);

    private SessionManager sessionManager;
    private OpcConnectManger opcConnectManger;


    @Autowired
    public MsgDecoderInbound(SessionManager sessionManager, OpcConnectManger opcConnectManger) {
        super();
        this.sessionManager = sessionManager;
        this.opcConnectManger = opcConnectManger;

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();

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
            ByteBuf wait_for_read = (ByteBuf) msg;
            if (wait_for_read.isReadable()) {
                byte[] bytes = new byte[wait_for_read.readableBytes()];
                wait_for_read.readBytes(bytes);
                byte[] opcserveidarray = Arrays.copyOfRange(bytes, 3, 7);
                int opcserveid = byteToInt(opcserveidarray);
                byte[] command = Arrays.copyOfRange(bytes, 2, 3);
                switch (command[0]) {
                    case 0x03:
                        if (CommandImp.STATUS.valid(bytes)) {
                            logger.debug("STATUS");
                            logger.debug("opcserveid=" + opcserveid + ":" + CommandImp.STATUS.analye(bytes).toJSONString());
                        }
                        break;
                    case 0x04:
                        if (CommandImp.HEART.valid(bytes)) {
                            JSONObject heartmsg = CommandImp.HEART.analye(bytes);
                            logger.debug(heartmsg.toJSONString());
                            sessionManager.addSessionModule(opcserveid, heartmsg.getString("function"), ctx);
                            //写进程重连判定在心跳包中进行处理，主要是在连接完成后，再断开重连一下，已应对某些opc服务器单次重连读取数据异常的问题
                            if (heartmsg.getString("function").equals(OpcExecute.FUNCTION_WRITE) && (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().getReconnectcount() > 0)) {

                                synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute()) {
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().sendStopItemsCmd();
                                    TimeUnit.MICROSECONDS.sleep(100);
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().minsReconnectcount();
                                    Session session = sessionManager.removeSessionModule(ctx);
                                    if (session != null) {
                                        session.getCtx().close();
                                    }
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().reconnect();
                                }

                                break;
                            }
                        }
                        break;
                    case 0x05:
                        if (CommandImp.ACK.valid(bytes)) {
                            logger.debug(CommandImp.ACK.analye(bytes).toJSONString());
                        }
                        break;
                    case 0x07:
                        if (CommandImp.OPCREADRESULT.valid(bytes)) {
                            JSONObject readjson = CommandImp.OPCREADRESULT.analye(bytes);
                            logger.debug(readjson.toJSONString());
                            //判断读取数据之前那判断下重连次数值是否为大于0，如果大于0，那么需要进行重连操作，同时把write processor重启
                            if (readjson.getString("function").equals(OpcExecute.FUNCTION_READ) && (opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().getReconnectcount() > 0)) {

                                synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute()) {
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().sendStopItemsCmd();
                                    TimeUnit.MICROSECONDS.sleep(100);
//                                opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().getExecutePythonBridge().stop();
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().minsReconnectcount();
                                    Session session = sessionManager.removeSessionModule(ctx);
                                    if (session != null) {
                                        session.getCtx().close();
                                    }
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().reconnect();
                                }

                                synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute()) {
                                    //知道已经断线了，那么也重连下write processor
                                    Session writesession = sessionManager.getSpecialSession(opcserveid, OpcExecute.FUNCTION_WRITE);
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().sendStopItemsCmd();
                                    TimeUnit.MICROSECONDS.sleep(100);
                                    if (writesession != null) {
                                        sessionManager.removeSessionModule(writesession.getCtx());
                                        writesession.getCtx().close();
                                    }
                                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().reconnect();
                                }

                                break;
                            }
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealReadAllItemsResult(readjson);
                        }
                        break;
                    case 0x08:
                        if (CommandImp.OPCWRITERESULT.valid(bytes)) {
                            logger.debug("OPCWRITERESULT");
                            logger.debug(CommandImp.OPCWRITERESULT.analye(bytes).toJSONString());
                        }
                        break;
                    case 0x0b:
                        if (CommandImp.ADDITEMRESULT.valid(bytes)) {
                            logger.debug("ADDITEMRESULT");
                            JSONObject addjson = CommandImp.ADDITEMRESULT.analye(bytes);
                            if (addjson.getString("function").equals(OpcExecute.FUNCTION_READ)) {
                                opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealAddItemResult(CommandImp.ADDITEMRESULT.analye(bytes));
                            } else if (addjson.getString("function").equals(OpcExecute.FUNCTION_WRITE)) {
                                opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().dealAddItemResult(CommandImp.ADDITEMRESULT.analye(bytes));
                            }
                            logger.info(addjson.toJSONString());
                        }
                        break;
                    case 0x0c:
                        if (CommandImp.REMOVEITEMRESULT.valid(bytes)) {
                            logger.debug("REMOVEITEMRESULT");
                            JSONObject removejson = CommandImp.REMOVEITEMRESULT.analye(bytes);
                            if (removejson.getString("function").equals(OpcExecute.FUNCTION_READ)) {
                                opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealRemoveResult(CommandImp.REMOVEITEMRESULT.analye(bytes));
                            } else if (removejson.getString("function").equals(OpcExecute.FUNCTION_WRITE)) {
                                opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().dealRemoveResult(CommandImp.REMOVEITEMRESULT.analye(bytes));
                            }
                            logger.debug(CommandImp.REMOVEITEMRESULT.analye(bytes).toJSONString());
                        }
                        break;
                    default:
                        logger.warn("no match any command");
                        break;
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


    private int byteToInt(byte[] data) {

        int reult = ((data[0] << 24) & 0xff000000) |
                ((data[1] << 16) & 0xff0000) |
                ((data[2] << 8) & 0xff00) |
                ((data[3] << 0) & 0xff);

        return reult;
    }

}
