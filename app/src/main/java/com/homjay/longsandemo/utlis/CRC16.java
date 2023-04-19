package com.homjay.longsandemo.utlis;

/**
 * @author Homjay
 * @date 2023/3/17 14:23
 * @describe
 */
public class CRC16 {

    //CheckSum校验:
    //    占1 个字节,对传输的数据进行累加和,然后转成16 进制,用两个字节的来表示,取第 1 位.
    public static byte[] intToByteArray(int i){
        byte[] result = new  byte[2];
        result[0] = (byte) (i >> 8 & 0xFF);
        result[1] = (byte) (i & 0xFF);
        return result;
    }

    public static byte getCheckSum(byte[] bytes){
        int checkSum = 0;
        for (byte aByte : bytes){
            checkSum += aByte;
        }
        byte[] bytes1 = intToByteArray(checkSum);
        if (bytes1 != null){
            return bytes1[1];
        }else {
            throw new NullPointerException();
        }
    }

//    public static byte[] calc16(byte[] bytes){
//        char crc = 0x0000;
//
//        for (byte b : bytes){
//            crc = ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
//            crc = ( (crc & 0xFF00) >> 8) | ( (crc & 0x00FF ) << 8);
//        }
//
//    }

}
