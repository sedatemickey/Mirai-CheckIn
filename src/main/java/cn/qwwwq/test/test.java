package cn.qwwwq.test;
import javafx.util.Pair;
import java.io.*;

class CheckIn{
    private Pair<Integer,Long> GetInfo(long QQid){
        //-1:找不到该QQid  -2:找不到数据文件
        //< coins, date >
        try {
            //读取文件
            BufferedReader reader = new BufferedReader(new FileReader("C:\\coins.ini"));//创建读取器
            String readStr; //记录正在读取的行
            String gottenStr = null; //记录QQid所属的行, 找不到则为null
            while((readStr = reader.readLine()) != null){
                if(readStr.equals("["+QQid+"]")){ //如果找到QQid所属的行, 就存到getStr中, 然后break
                    gottenStr=readStr;
                    break;
                }
                else{ //如果这行不对, 就直接跳过他包含的data和coins行, 直接读下一个QQid
                    reader.readLine();
                    reader.readLine();
                }
            }
            //判断QQid是否存在
            if(gottenStr == null)return new Pair<>(-1,-1L); //找不到QQid, 返回状态码-1
            //QQid存在, 开始读取coins和date
            //读coins
            gottenStr=reader.readLine();
            int coins = Integer.parseInt(gottenStr.substring(6));
            //读date
            gottenStr=reader.readLine();
            Long date = Long.valueOf(gottenStr.substring(5));
            return new Pair<>(coins,date);
        }catch(IOException e){//找不到数据文件, 返回状态码-2
            System.out.println("-Catch-");
            return new Pair<>(-1,-2L);
        }
    }
    public void main(long QQid){
        Pair<Integer,Long> info = GetInfo(QQid);
        System.out.println(info.getKey());
        System.out.println(info.getValue());
    }
}

public class test {
    public static void main(String[] args) {
        CheckIn checkIn = new CheckIn();
        checkIn.main(2941904954L);
    }
}
