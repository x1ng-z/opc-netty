package hs.opcnetty.opcproxy.command;


import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/2/16 15:25
 */

public enum CommandImp implements Command {

    /**read opc data*/
    READ(0x01) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //length
            result[7] = (byte) ((context.length >> 16) & 0xff);
            result[8] = (byte) ((context.length >> 8) & 0xff);
            result[9] = (byte) ((context.length >> 0) & 0xff);
            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;

        }

    },
    /**write opc data*/
    WRITE(0x02) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**opc connect status*/
    STATUS(0x03) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**send heart msg*/
    HEART(0x04) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] build(int nodeid) {
            JSONObject object=new JSONObject();
            object.put("msg","success");
            try {
                return build(object.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return null;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**ack any revice msg*/
    ACK(0x05) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] build(int nodeid) {
            JSONObject object=new JSONObject();
            object.put("msg","success");
            try {
                return build(object.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return null;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid >> 24) & 0xff);
            result[4] = (byte) ((nodeid >> 16) & 0xff);
            result[5] = (byte) ((nodeid >> 8) & 0xff);
            result[6] = (byte) ((nodeid >> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**stop opc coneect*/
    STOP(0x06) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**opc data result*/
    OPCREADRESULT(0x07) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
                str=str.replace(":nan,",":0,").replace(":inf,",":"+Double.MAX_VALUE+",").replace(":-inf,",":"+Double.MIN_VALUE+",");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error("the error parse conetx is"+str);
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**opc write result*/
    OPCWRITERESULT(0x08) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**add opc item*/
    ADDITEM(0x09) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","add item");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**remove opc item*/
    REMOVEITEM(0x0a) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**add item result*/
    ADDITEMRESULT(0x0b) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**remove item result*/
    REMOVEITEMRESULT(0x0c) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","stop");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    },
    /**patch add item, suggest only for reconnect ,then patch add already right items*/
    BATCHADDITEM(0x0d) {
        @Override
        public JSONObject analye(byte[] context) {
            return defaultanalye(context);
        }

        public byte[] defaultbuild(int nodeid){
            JSONObject jsonObject= new JSONObject();
            jsonObject.put("msg","patch add item");
            byte[] waitsend= new byte[0];
            try {
                waitsend = build(jsonObject.toJSONString().getBytes("utf-8"),nodeid);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(),e);
            }
            return waitsend;
        }

        @Override
        public byte[] build(byte[] context,int nodeid) {
            int length = context.length + 10;
            byte[] result = new byte[length];
            //header
            result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
            result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
            //command
            result[2] = this.getCommand();
            //nodeid
            result[3] = (byte) ((nodeid>> 24) & 0xff);
            result[4] = (byte) ((nodeid>> 16) & 0xff);
            result[5] = (byte) ((nodeid>> 8) & 0xff);
            result[6] = (byte) ((nodeid>> 0) & 0xff);
            //context length
            result[7] = (byte) ((context.length  >> 16) & 0xff);
            result[8] = (byte) ((context.length  >> 8) & 0xff);
            result[9] = (byte) ((context.length  >> 0) & 0xff);

            for (int index = 10; index < length; index++) {
                result[index] = context[index - 10];
            }
            return result;
        }

        @Override
        public boolean valid(byte[] context) {
            //头校验
            if (context.length <= 10) {
                return false;
            }
            //header check
            if ((0x88 != context[0]) && (0x18 != context[1])) {
                return false;
            }
            if (this.getCommand() != context[2]) {
                return false;
            }
            byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
            try {
                String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
                try {
                    JSONObject.parseObject(str);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    };


    public Logger logger = LoggerFactory.getLogger(CommandImp.class);
    private byte command;
    private int nodeid;

    private CommandImp(int main_command) {
        this.command = (byte) (main_command & 0xff);
    }


    public boolean defaultvalid(byte[] context) {
        //头校验
        if (context.length <= 10) {
            return false;
        }
        //header check
        if ((0x88 != context[0]) && (0x18 != context[1])) {
            return false;
        }
        byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
//            logger.info(str);
            try {
                JSONObject.parseObject(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    public JSONObject defaultanalye(byte[] context) {
        byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
            str=str.replace(":nan,",":0,").replace(":inf,",":"+Double.MAX_VALUE+",").replace(":-inf,",":"+Double.MIN_VALUE+",");
//            logger.info(str);
            try {
                return (JSONObject) JSONObject.parseObject(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public String toString() {
        return super.toString() + command;
    }

    public byte getCommand() {
        return command;
    }


    public int getNodeid() {
        return nodeid;
    }

    public void setNodeid(int nodeid) {
        this.nodeid = nodeid;
    }

    @Override
    abstract public JSONObject analye(byte[] context);

    @Override
    abstract public byte[] build(byte[] context,int nodeid);

    @Override
    abstract public boolean valid(byte[] context);
}
