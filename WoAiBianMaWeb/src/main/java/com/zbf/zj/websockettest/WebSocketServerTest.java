package com.zbf.zj.websockettest;

import com.zbf.utils.SpringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@ServerEndpoint ( "/webSocketTest/sendMessage/{userid}" )
public class WebSocketServerTest {

   private static RedisTemplate redisTemplate=SpringUtil.getBean ( "redisTemplate",RedisTemplate.class);

   public static Map<String,Session> mapsession=new HashMap<String,Session> (  );

   private static Log logger=LogFactory.getLog ( WebSocketServerTest.class );


   @OnOpen
   public void onOpen(Session seesion, @PathParam ( "userid" )String userid){

       logger.info ( "===建立连接=====" );

       mapsession.put ( userid, seesion);

       List<String> range = redisTemplate.opsForList ().range ( "userid_" + userid, 0, -1 );
       range.forEach ( (msg)->{
           WebSocketServerTest.sendMessageFromServer ( seesion,msg,userid);

           redisTemplate.opsForList ().remove ( "userid_" + userid,1, msg);
       } );


   }


   @OnMessage
   public void  onMessage(Session session,String message,@PathParam ( "userid" )String userid){
       logger.info ( "===onMessage==服务端接收到的客户端的消息==="+message );

   }


    @OnClose
    public void  onClose(Session session,@PathParam ( "userid" )String userid){
        logger.info ( "===onClose==连接通道关闭" );
        //清除Session
        mapsession.remove ( userid );
    }

    /**
     * 通道发生错误的时候触发
     * @param session
     * @param throwable
     * @param userid
     */
    @OnError
    public void onError(Session session,Throwable throwable,@PathParam ( "userid" )String userid){

    }


    /**
     * 服务端推送消息
     * @param session
     * @param message
     * @param userid
     */
    public static void sendMessageFromServer(Session session,String message,String userid){

        try{
            session.getAsyncRemote ().setSendTimeout (2000);
            session.getAsyncRemote ().sendText ( message );

        }catch (Exception e){
            e.printStackTrace ();
            redisTemplate.expire ( "userid_"+ userid,1000,TimeUnit.SECONDS );
            redisTemplate.opsForList ().leftPush ( "userid_"+ userid,message);
        }


    }


}
