package com.cros.block.util;

import java.math.BigInteger;
import java.util.Random;


public class StringRandom {


    //生成随机数字和字母,
    public String getStringRandom(int length) {
        
        String val = "";
        Random random = new Random();
        
        //参数length，表示生成几位随机数
        for(int i = 0; i < length; i++) {
            
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if( "char".equalsIgnoreCase(charOrNum) ) {
                //输出小写字母
//                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + 97);
            } else if( "num".equalsIgnoreCase(charOrNum) ) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }
    
    public static int toHash(String key) {  
        int arraySize = 11113; // 数组大小一般取质数  
        int hashCode = 0;  
        for (int i = 0; i < key.length(); i++) { // 从字符串的左边开始计算  
            int letterValue = key.charAt(i) - 96;// 将获取到的字符串转换成数字，比如a的码值是97，则97-96=1  
                                                    // 就代表a的值，同理b=2；  
            hashCode = ((hashCode << 5) + letterValue) % arraySize;// 防止编码溢出，对每步结果都进行取模运算  
        }  
        return hashCode;  
    }  

    public static void  main(String[] args) throws Exception {
        StringRandom test = new StringRandom();
//        System.out.println(test.getStringRandom(64));
        //测试
//        int hash = StringRandom.toHash("username");
//        String md5Encrypt = MD5Util.MD5Encrypt("username");
//        System.out.println(md5Encrypt);
        for(int i=0;i<6;i++){
        	System.out.println(test.getStringRandom(30));
        }
        
        
    }
}
