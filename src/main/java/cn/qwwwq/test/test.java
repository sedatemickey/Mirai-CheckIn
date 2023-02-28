package cn.qwwwq.test;
import javafx.util.Pair;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.io.*;

class CheckIn{
    final String dataPath = "H:\\coins.ini";
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
            System.out.println(prepareToWrite);
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
            if(date == -1){
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

public class test {
    public static void main(String[] args) {
        CheckIn checkIn = new CheckIn();
        Pair<Integer,Integer> info = checkIn.main(9947226871L);
        System.out.println(info.getKey());
        System.out.println(info.getValue());
    }
}
