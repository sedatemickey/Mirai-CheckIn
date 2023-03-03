package cn.qwwwq.checkin;

import com.sun.source.tree.Scope;
import jdk.internal.misc.ScopedMemoryAccess;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import javax.swing.*;
import javafx.util.Pair;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import net.mamoe.mirai.message.data.MessageChainBuilder;

class MCheckIn{
    final String dataPath = "./data/coins.ini";
    private Pair<Integer,Long> GetInfo(long QQid){
        //-1:找不到该QQid  -2:找不到数据文件  -3:新建数据文件时错误
        //< coins, date >
        try {
            //读取文件
            BufferedReader reader = new BufferedReader(new FileReader(dataPath));//创建读取器
            String readStr; //记录正在读取的行
            String gottenStr = null; //记录QQid所属的行, 找不到则为null
            while((readStr = reader.readLine()) != null){
                if(readStr.equals("[" + QQid + "]")){ //如果找到QQid所属的行, 就存到getStr中, 然后break
                    gottenStr=readStr;
                    break;
                }
                else{ //如果这行不对, 就直接跳过他包含的data和coins行, 直接读下一个QQid
                    reader.readLine();
                    reader.readLine();
                }
            }
            //判断QQid是否存在
            if(gottenStr == null){
                reader.close();
                return new Pair<>(-1,-1L); //找不到QQid, 返回状态码-1
            }
            //QQid存在, 开始读取coins和date
            //读coins
            gottenStr=reader.readLine();
            int coins = Integer.parseInt(gottenStr.substring(6));
            //读date
            gottenStr=reader.readLine();
            Long date = Long.valueOf(gottenStr.substring(5));
            reader.close();
            return new Pair<>(coins,date);
        }catch(IOException e){//找不到数据文件, 新建数据文件, 并返回状态码-2
            //System.out.println("寄");

            File writer = new File(dataPath);
            try{
                if(!writer.createNewFile()){
                    //新建文件失败, 返回状态码-3
                    return new Pair<>(-1,-3L);
                }
            }catch(IOException e2){
                //新建文件失败, 返回状态码-3
                return new Pair<>(-1,-3L);
            }
            return new Pair<>(-1,-2L);
        }
    }
    private long CheckDate(long oldDate){
        Date getDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        long newDate = Long.parseLong(formatter.format(getDate));
        if(newDate > oldDate){
            return newDate;
        }
        else{
            return -1;
        }
    }
    private boolean EditInfo(int coins, long date, long QQid){
        //true:修改数据文件时错误
        try {
            //读取文件
            BufferedReader reader = new BufferedReader(new FileReader(dataPath));//创建读取器
            String readStr; //记录正在读取的行
            String prepareToWrite = ""; //记录准备写入的数据
            boolean found = false; //记录是否找到已存在的QQid
            while((readStr = reader.readLine()) != null){
                if(readStr.equals("[" + QQid + "]")){ //找到QQid所属的行
                    prepareToWrite += (readStr + '\n');
                    prepareToWrite += ("coins="+coins+'\n');
                    prepareToWrite += ("date="+date+'\n');
                    reader.readLine();
                    reader.readLine();
                    found = true;
                }
                else{ //如果这行不对, 就直接跳过他包含的data和coins行, 直接读下一个QQid
                    prepareToWrite += (readStr + '\n');
                    prepareToWrite += (reader.readLine() + '\n');
                    prepareToWrite += (reader.readLine() + '\n');
                }
            }
            reader.close();
            //判断QQid是否存在
            if(!found){
                prepareToWrite += ("[" + QQid + "]" + '\n');
                prepareToWrite += ("coins="+coins+'\n');
                prepareToWrite += ("date="+date+'\n');
            }
            //写入文件
            //System.out.println(prepareToWrite);
            FileWriter writer = new FileWriter(dataPath);
            writer.write(prepareToWrite);
            writer.flush();
            writer.close();
            return false;
        }catch(IOException e){//寄了
            return true;
        }
    }
    public Pair<Integer,Integer> main(long QQid){
        //读取配置文件中该QQ的信息
        Pair<Integer,Long> info = GetInfo(QQid);
        //解读返回值
        int coins = info.getKey();
        long date = info.getValue();
        //boolean isNew = false; //记录该用户是否为未出现过的用户
        if(coins == -1){ //异常状态处理
            if(date == -1 || date == -2){
                //isNew = true; //该用户从来没签过到
                coins = 0;
                date = 0;
            }
            else if(date == -3){
                return new Pair<>(-2,-1);
            }
        }
        //判定是否已签到
        date = CheckDate(date);
        if(date == -1){
            return new Pair<>(0, coins);
        }
        //获取新群币
        Random random = new Random();
        int addCoins = random.nextInt(100)+1;
        //写入新群币
        if(EditInfo(coins + addCoins, date, QQid)){ //文件写入异常
            return new Pair<>(-2,-2);
        }
        //返回准备输出
        return new Pair<>(addCoins,coins + addCoins);
    }
}

public final class CheckIn extends JavaPlugin {
    public static final CheckIn INSTANCE = new CheckIn();

    private CheckIn() {
        super(new JvmPluginDescriptionBuilder("cn.qwwwq.checkin", "3.0")
                .name("QWQ - CheckIn")
                .author("sedatemickey")
                .build());
    }

    @Override
    public void onEnable() {
        MCheckIn checkiner = new MCheckIn();
        //start listener
        Listener listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, event->{
            //event.getSubject().sendMessage("取到信息");
            MessageChain chain=event.getMessage(); // 可获取到消息内容等, 详细查阅 `GroupMessageEvent`
            getLogger().info("QWQ - Got Message: " + chain);
            if(chain.contentToString().equals("签到")){
                Pair<Integer,Integer> info = checkiner.main(event.getSender().getId());
                getLogger().info("签到方法执行完毕");
                if(info.getKey() == 0){
                    MessageChain replyChain = new MessageChainBuilder()
                            .append(new At(event.getSender().getId()))
                            .append(" 今天签到过了哦，明天再来吧\n你的群币：")
                            .append(info.getValue().toString())
                            .build();
                    event.getSubject().sendMessage(replyChain);
//"[mirai:at:"+event.getSender().getId()+"]"+"签到成功，加"+info.getValue()+"智障群币~\n你的群币："+info.getValue()
                }
                else if(info.getKey() == -2){//签到函数执行出错
                    if(info.getValue() == -1){
                        event.getSubject().sendMessage("签到失败, 读取数据文件错误");
                        getLogger().error("QWQ - 签到失败, -2, -1");
                    }
                    else if(info.getValue() == -2){
                        event.getSubject().sendMessage("签到失败, 写入数据文件错误");
                        getLogger().error("QWQ - 签到失败, -2, -2");
                    }
                    else{
                        event.getSubject().sendMessage("签到失败, 未知错误");
                        getLogger().error("QWQ - 签到失败, -2, ??");
                    }
                }
                else{
                    MessageChain replyChain = new MessageChainBuilder()
                            .append(new At(event.getSender().getId()))
                            .append(" 签到成功，加")
                            .append(info.getKey().toString())
                            .append("智障群币~\n你的群币：")
                            .append(info.getValue().toString())
                            .build();
                    event.getSubject().sendMessage(replyChain);
                }
            }
            //event.getSubject().sendMessage("签到失败, 未找到签到方法"); // 回复消息
        });
        getLogger().info("QWQ - CheckIn plugin has been loaded successfully!");
    }
}