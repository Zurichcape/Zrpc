package com.zurich.remote.transport.netty.codec;

import com.zurich.compress.Compress;
import com.zurich.enums.CompressTypeEnum;
import com.zurich.enums.SerializationTypeEnum;
import com.zurich.extension.ExtensionLoader;
import com.zurich.serialize.Serializer;
import com.zurich.remote.constants.RpcConstants;
import com.zurich.remote.dto.RpcMessage;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 17:31
 * @description
 * * * custom protocol decoder
 *  *  * <pre>
 *  *  *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *  *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *  *  *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *  *  *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  *  *   |                                                                                                       |
 *  *  *   |                                         body                                                          |
 *  *  *   |                                                                                                       |
 *  *  *   |                                        ... ...                                                        |
 *  *  *   +-------------------------------------------------------------------------------------------------------+
 *  *  * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  *  * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *  *  * body（object类型数据）
 *  *  * </pre>
 *  *  * <p>
 *  *  * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 *  *  * </p>
 *  *  *
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder(){
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }

    /**
     * @param maxFrameLength      最大帧长度，决定了数据的最大长度，一旦超过了就会被丢弃
     * @param lengthFieldOffset   长度偏移量，会跳过指定长度字节的位置
     * @param lengthFieldLength   长度字段的字节长度
     * @param lengthAdjustment    长度字段的添加补充值
     * @param initialBytesToStrip 可以跳跃的字节数，如果你需要接收head+body，那么这个值为0
     *                            如果你需要跳过head数据，那么将该值设置为header的长度即可
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded =  super.decode(ctx, in);
        if(decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if(frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try{
                    return decodeFrame(frame);
                }catch (Exception e){
                    log.error("Decode frame error",e);
                    throw e;
                }finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }
    private Object decodeFrame(ByteBuf in){
        //注意：必须顺序读取ByteBuf
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        //build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId= in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType)
                .build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            //解压缩内容
            String decompressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(decompressName);
            bs = compress.decompress(bs);
            //反序列化对象
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if(messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest tmpVal = serializer.deserialize(bs,RpcRequest.class);
                rpcMessage.setData(tmpVal);
            }else{
                RpcResponse tmpVal = serializer.deserialize(bs,RpcResponse.class);
                rpcMessage.setData(tmpVal);
            }
        }
        return rpcMessage;
    }
    private void checkVersion(ByteBuf in){
        byte version = in.readByte();
        if(version != RpcConstants.VERSION){
            throw new RuntimeException("version is not compatible"+version);
        }
    }

    private void checkMagicNumber(ByteBuf in){
        //读取前4字节的数据，然后进行比较
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        //逐一进行比较
        for(int i=0;i<len;i++){
            if(tmp[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("Unknown magic code: "+ Arrays.toString(tmp));
            }
        }
    }
}
