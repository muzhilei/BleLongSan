package com.homjay.longsandemo.cmd;

import android.text.TextUtils;
import android.util.Log;


import com.homjay.longsandemo.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * @Author: taimin
 * @Date: 2021/4/22
 * @Description: 处理指令
 */
public class CmdUtils {

    public static StringBuilder mCmd = new StringBuilder();//指令临时容器

    /**
     * 解析数据,把合并以及分段接收的数据整理
     * <p>
     * 解析数据的真实值
     * aa5504b00078004f7baa5502a90052fdaa5502a20036daaa5501920093aa5508a88080808080808080b0
     * aa55 04b00078004f7b
     *
     * @return
     */
    public static List<String> getDataCmdList(String data) {
        if (TextUtils.isEmpty(data)) return null;

        //如果数据包含头部,说明新数据来了
        if (data.startsWith(CmdConstants.Cmd_Start)) {
            mCmd = new StringBuilder();
        }
        mCmd.append(data);

        String mCmdStr = mCmd.toString();
        if (!mCmdStr.startsWith(CmdConstants.Cmd_Start)) {
            //数据起始位不包含头部
            if (mCmdStr.contains(CmdConstants.Cmd_Start)) {
                //数据包含头部(把头部起始位之前数据去掉)
                int i = mCmdStr.indexOf(CmdConstants.Cmd_Start);
                mCmdStr = mCmdStr.substring(i);
                mCmd = new StringBuilder();
                mCmd.append(mCmdStr);
            } else {
                //数据不包含头部(清空容器)
                mCmdStr = "";
                mCmd = new StringBuilder();
            }
        }

        if (TextUtils.isEmpty(mCmdStr)) return null;
        List<String> cmdList = new ArrayList<>(); //处理指令容器
        String[] cmdStr = null;

        cmdStr = mCmdStr.split(CmdConstants.Cmd_Start);
        //如果只有一个指令，直接处理
        for (int i = 0; i < cmdStr.length; i++) {
            String cmd = CmdConstants.Cmd_Start + cmdStr[i];
            if (isLength(cmd)) {
                cmdList.add(cmd);
            } else {
                //如果是最后一个数据，并且长度不正确(有可能是指令不完整,等待接收)
                // 并且放入容器
                if (i == cmdStr.length - 1) {
                    mCmd = new StringBuilder();
                    mCmd.append(cmd);
                }
            }
        }
        return cmdList;
    }

    /**
     * 解析数据,把合并以及分段接收的数据整理
     * <p>
     * 整理，合并蓝牙返回数据
     * aa5504b00078004f7baa5502a90052fdaa5502a20036daaa5501920093aa5508a88080808080808080b0
     * aa55 04b00078004f7b
     *
     * @return
     */
    public static synchronized List<String> getDataHandSetCmdList(String data) {
        if (TextUtils.isEmpty(data)) return null;
        List<String> cmdList = new ArrayList<>(); //处理指令容器
        String mCmdStr = MyApplication.getIns().gettCmd() + data;
        String mCmdOder = "";
        //判断有多少个aa55
        String[] includeStart = mCmdStr.split(CmdConstants.Cmd_Start);
        if (includeStart.length != 0){
            for (int i = 0 ; i < includeStart.length-1;i ++){
                String s = mCmdStr.substring(4, 6); //数据长度
                int d = Integer.valueOf(s, 16); //转十进制
                int length =  10 + d * 2;
                //判断当前返回的值是否是完整的值
                if (mCmdStr.length() >= length){
                    mCmdOder = mCmdStr.substring(0,length);
                    String[] check = mCmdOder.split(CmdConstants.Cmd_Start);
                    if (check.length-1 != 1){
                        MyApplication.getIns().settCmd("");
                    }else {
                        cmdList.add(mCmdOder);
                        mCmdStr = mCmdStr.substring(length);
                    }
                }
            }
        }
        MyApplication.getIns().settCmd(mCmdStr);

//        if (TextUtils.isEmpty(mCmdStr)) return null;
//        List<String> cmdList = new ArrayList<>(); //处理指令容器
//        String[] cmdStr = null;
//
//        cmdStr = mCmdStr.split(CmdConstants.Cmd_Start);
//        //如果只有一个指令，直接处理
//        for (int i = 0; i < cmdStr.length; i++) {
//            String cmd = CmdConstants.Cmd_Start + cmdStr[i];
//            if (isLength(cmd)) {
//                cmdList.add(cmd);
//            } else {
//                //如果是最后一个数据，并且长度不正确(有可能是指令不完整,等待接收)
//                // 并且放入容器
//                if (i == cmdStr.length - 1) {
//                    mCmd = new StringBuilder();
//                    mCmd.append(cmd);
//                }
//            }
//        }
        return cmdList;
    }


    /**
     * 解析数据的真实值
     * aa5504b00078004f7baa5502a90052fdaa5502a20036daaa5501920093aa5508a88080808080808080b0
     * aa55 04 b0 0078004f 7b
     *
     * @return
     */
    public static Map<String, List<Integer>> getData(List<String> cmdList) {
        try {
            if (cmdList == null || cmdList.size() == 0) return null;

            Map<String, List<Integer>> map = new HashMap<>();
            for (String s : cmdList) {
                if (TextUtils.isEmpty(s)) continue;

                int length = s.length();
                //判断校验
                String checkStartHex = s.substring(4, length - 2);
                String checkHex = s.substring(length - 2, length);

                boolean isok = check(checkStartHex, checkHex);
                if (!isok) Log.e(CmdUtils.class.getSimpleName(), "getData: s ===  " + s);
                if (!isok) continue;

                //解析数据
                String codeHex = s.substring(6, 8);
                String dataHex = s.substring(8, length - 2);
                getPropertyData(codeHex, dataHex, map);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取单个属性的数据
     * b0 0078004f
     *
     * @return
     */
    private static void getPropertyData(String codeHex, String data, Map<String, List<Integer>> map) {
        List<Integer> ints = new ArrayList<>();
        if (CmdConstants.Code_bp.equals(codeHex)) {
            //血压 0078004f
            int sbp = Integer.valueOf(data.substring(0, 4), 16);
            int dbp = Integer.valueOf(data.substring(4), 16);
            if (sbp == 0 || dbp == 0) return;
            ints.add(sbp);
            ints.add(dbp);
            map.put(CmdConstants.Type_bp, ints);
        } else if (CmdConstants.Code_heartRate.equals(codeHex)) {
            //心率
            int i = Integer.valueOf(data, 16);
            if (i == 0) return;
            ints.add(i);
            map.put(CmdConstants.Type_heartRate, ints);
        } else if (CmdConstants.Code_spo2.equals(codeHex)) {
            //血氧
            int i = Integer.valueOf(data, 16);
            if (i == 0) return;
            ints.add(i);
            map.put(CmdConstants.Type_spo2, ints);
        } else if (CmdConstants.Code_spo2_x.equals(codeHex)) {
            //血氧波形 8080808080808080
            StringBuilder s = new StringBuilder();
            for (char c : data.toCharArray()) {
                s.append(c);
                //截取两个
                if (s.length() == 2) {
                    int d = Integer.valueOf(s.toString(), 16); //转十进制
                    //存map
                    if (d == 0) continue;
                    ints.add(d);
                    s = new StringBuilder();
                }
            }

            //过滤
            List<Integer> dataNew = getSpo2Data(ints);
            if (dataNew.size() == 0) return;
            map.put(CmdConstants.Type_spo2_x, dataNew);
        }else if (CmdConstants.Code_heart.equals(codeHex)){
            //心电波形
            StringBuilder s = new StringBuilder();
            for (char c : data.toCharArray()) {
                s.append(c);
                //截取两个
                if (s.length() == 4) {
                    byte[] j = HexUtils.hexStringToBytes(s.toString());
                    //int d = (((j[0]&0xFF) & 0x0F) << 8 ) + (j[1]&0xFF);
                    int d = (((j[0]&0xFF) & 0xFF) << 8 ) + (j[1]&0xFF);



                    if (d<=0 || d >= 4096){
                        Log.e(CmdUtils.class.getSimpleName(), "getPropertyData:  d === " + d);
//                        Log.d(CmdUtils.class.getSimpleName(), "data == " + data + "    d == " + d + "   s == " +s.toString()
//                                + "   j1 == " + (j[0]&0xff) + "  j2 == " + (j[1]&0xFF) + "  e == " + e + "   f == " + f);
                    }
                    //存map
//                    if (d == 0) continue;
                    ints.add(d);
                    s = new StringBuilder();
                }
            }
            //过滤
            List<Integer> dataNew = getHeart2Data(ints);
            if (dataNew.size() == 0) return;
            map.put(CmdConstants.Code_heart, dataNew);
        }else if (CmdConstants.Code_hrv.equals(codeHex)){
            //HRV数据
            int fatigue = Integer.valueOf(data.substring(0,2), 16);
            int pressure = Integer.valueOf(data.substring(2,4), 16);
            int spirit = Integer.valueOf(data.substring(4,6), 16);
            int compressive = Integer.valueOf(data.substring(6,8), 16);

            if (fatigue == 0 || pressure == 0 || spirit == 0 || compressive == 0) return;
            ints.add(fatigue);
            ints.add(pressure);
            ints.add(spirit);
            ints.add(compressive);
            map.put(CmdConstants.Code_hrv, ints);
        }
    }

    /**
     * 防止一包数据中有多个同类型数据，把数据增加到List中(废弃)
     *
     * @param type
     * @param map
     */
    private static void setData(String type, Map<String, List<Integer>> map, int data) {
        List<Integer> listNew = new ArrayList<>();
        if (map.containsKey(type)) {
            listNew = map.get(type);
            if (listNew == null) {
                listNew = new ArrayList<>();
            }
            listNew.add(data);
        } else {
            listNew.add(data);
        }
        map.put(type, listNew);
    }

    /**
     * 04b00078004f 7b
     * 前面相加，后一个字节，等于校验
     *
     * @return
     */
    private static boolean check(String checkStartHex, String checkHex) {
        if (checkStartHex.length() % 2 != 0) return false;
        int totle = 0;

        StringBuilder s = new StringBuilder();
        for (char c : checkStartHex.toCharArray()) {
            s.append(c);
            //截取两个
            if (s.length() == 2) {
                int d = Integer.valueOf(s.toString(), 16); //转十进制
                totle = totle + d; //相加
                s = new StringBuilder();
            }
        }

        String totleHex = Integer.toHexString(totle);
        if (totleHex.length() > 2) {
            totleHex = totleHex.substring(totleHex.length() - 2);
        } else if (totleHex.length() < 2) {
            totleHex = "0" + totleHex;
        }

        return totleHex.equals(checkHex);
    }

    /**
     * 得到校验码(既可以传带头部数据，也可以不带头部)
     * aa55
     * 04 b0 0078004f
     * 7b
     */
    public static String getCheck(String checkHex) {
        if (checkHex.startsWith(CmdConstants.Cmd_Start)) {
            checkHex = checkHex.substring(4);
        }

        if (checkHex.length() % 2 != 0) return null;
        int totle = 0;

        StringBuilder s = new StringBuilder();
        for (char c : checkHex.toCharArray()) {
            s.append(c);
            //截取两个
            if (s.length() == 2) {
                int d = Integer.valueOf(s.toString(), 16); //转十进制
                totle = totle + d; //相加
                s = new StringBuilder();
            }
        }

        String totleHex = Integer.toHexString(totle);
        if (totleHex.length() > 2) {
            totleHex = totleHex.substring(totleHex.length() - 2);
        } else if (totleHex.length() < 2) {
            totleHex = "0" + totleHex;
        }
        return totleHex;
    }

    /**
     * 是否有效长度
     * aa55 04 b0 0078004f 7b
     *
     * @return
     */
    private static boolean isLength(String cmd) {
        if (TextUtils.isEmpty(cmd)) return false;
        if (!cmd.startsWith(CmdConstants.Cmd_Start)) return false;
        int length = cmd.length();
        if (length < 10) return false;
        String s = cmd.substring(4, 6); //数据长度
        int d = Integer.valueOf(s, 16); //转十进制
        return cmd.length() == (10 + d * 2);
    }

    /**
     * 返回指令中的code
     * aa55 04 b0 0078004f 7b 血压返回
     * aa55 02 84 10 00 96 指令接收成功返回
     * <p>
     * code=84,中的00代表成功
     * 4 参数错误 3 不可用命令 2 长度错误 1 校验错误 0 命令接受成功
     */
    public static String getCode(String cmd) {
        if (TextUtils.isEmpty(cmd)) return "";
        String code = cmd.substring(6, 8);
        if (CmdConstants.Code_back.equals(code)) {
            //如果是指令接收成功返回
            code = cmd.substring(8, 10);
        }
        return code;
    }

    /**
     * 返回指令中的状态码
     * aa55 02 84 10 00 96
     * <p>
     * code=84,中的00代表成功
     * 04 参数错误 03 不可用命令 02 长度错误 01 校验错误 00 命令接受成功
     */
    public static String getBackState(String cmd) {
        if (TextUtils.isEmpty(cmd)) return null;
        String code = cmd.substring(6, 8);
        if (CmdConstants.Code_back.equals(code)) {
            //如果是指令接收成功返回
            String state = cmd.substring(10, 12);
            if (!"00".equals(state)) {
                return state;
            }
        }
        return null;
    }

    /**
     * 返回指令中的data的数据长度DATALEN
     * 04
     * 0078004f
     */
    public static String getLength(String data) {
        int lengthByte = data.length() / 2;
        return HexUtils.intToHex(lengthByte);
    }

    /**
     * 求平均值
     *
     * @return
     */
    public int getAVG(List<Integer> data) {
        if (data.size() == 0) return 0;
        int avg = 0;
        for (int i : data) {
            avg += i;
        }
        return avg / data.size();
    }

    /**
     * 把PM小包合并起来
     * aa558191 21
     * 3f741fac4582b0004534a0003ee24dd33edfbe773f747ae141b000003da3d70a438900004708de0048841ec0425400003e45a1cb423400003e27ef9e3d0f5c293f3645a23ea6e9793f17ced941d69fbe410245e4bf510291410c9f54bf5f4d56bd3c98963edfb9b74045d3a4c021a58a40b39ebfbdaf38fc407e1943bfd2917e
     * 81
     */
    public static String getPmFirst(List<String> list) {
        StringBuilder PM = new StringBuilder();
        for (String s : list) {
            if (TextUtils.isEmpty(s)) continue;
            String sub = s.substring(10, s.length() - 2);
            PM.append(sub);
        }
        return PM.toString();
    }

    /**
     * 把PM大包合并起来,传给后台
     */
    public static String getPmEnd(List<String> list) {
        StringBuilder PM = new StringBuilder();
        for (String s : list) {
            if (TextUtils.isEmpty(s)) continue;
            PM.append(s);
            PM.append(";");
        }
        return PM.toString();
    }

    /**
     * 是否是标定返回数据的第一小包
     * aa558191 21
     * 3f741fac4582b0004534a0003ee24dd33edfbe773f747ae141b000003da3d70a438900004708de0048841ec0425400003e45a1cb423400003e27ef9e3d0f5c293f3645a23ea6e9793f17ced941d69fbe410245e4bf510291410c9f54bf5f4d56bd3c98963edfb9b74045d3a4c021a58a40b39ebfbdaf38fc407e1943bfd2917e
     * 81
     */
    public static boolean isPmFirst(String data) {
        String num = data.substring(9, 10);
        return "1".equals(num);
    }

    /**
     * 标定返回数据总共多少包
     * aa558191 21
     * 3f741fac4582b0004534a0003ee24dd33edfbe773f747ae141b000003da3d70a438900004708de0048841ec0425400003e45a1cb423400003e27ef9e3d0f5c293f3645a23ea6e9793f17ced941d69fbe410245e4bf510291410c9f54bf5f4d56bd3c98963edfb9b74045d3a4c021a58a40b39ebfbdaf38fc407e1943bfd2917e
     * 81
     */
    public static int getPmNum(String data) {
        String num = data.substring(8, 9);
        return Integer.parseInt(num);
    }

    /**
     * 解析序列号数据,转化成ASCII码
     * aa5517d0 00 5231413230323031303030303033 ed2f19481995f6b5 99
     */
    public static String getSn(String data) {
        if (TextUtils.isEmpty(data) || data.length() < 38) return null;
        String sn = data.substring(10, 38);
        return HexUtils.hexToASCII(sn);
    }

    /**
     * 组装开始测量和开始标定指令
     * isMeasure true 测量 false 标定
     * posture 0 坐势 1 站势 2 卧势
     * drug 0 未用药 1 用药
     * <p>
     * aa553710
     * 04
     * 31363036323936323133393739 65717379673176
     * 31363036323936323133393830 677565386c3176697166697a626d6370623031
     * 0000
     * 81
     */
    public static String getStartMeasureOrMarkCmd(boolean isMeasure, int posture, int drug) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(CmdConstants.Cmd_Start);
        cmd.append("01");
        cmd.append(CmdConstants.Code_start_measure_mask);
        if (isMeasure) {
            cmd.append("21");
        } else {
            cmd.append("20");
        }

//        long millis = System.currentTimeMillis();
//        String millisHex = HexUtils.ASCIIToHex(String.valueOf(millis));
//        Random random = new Random();
//
//        //批次号：当前时间戳+7个随机字符
//        StringBuilder str7 = new StringBuilder();
//        for (int i = 1; i <= 7; i++) {
//            int ii = random.nextInt(9);
//            str7.append(ii);
//        }
//        String str7Hex = HexUtils.ASCIIToHex(String.valueOf(str7));
//        cmd.append(millisHex).append(str7Hex);
//
//        //bptag：当前时间戳+19位随机字符
//        StringBuilder str19 = new StringBuilder();
//        for (int i = 1; i <= 19; i++) {
//            int ii = random.nextInt(9);
//            str19.append(ii);
//        }
//        String str19Hex = HexUtils.ASCIIToHex(String.valueOf(str19));
//        cmd.append(millisHex).append(str19Hex);
//
//        cmd.append("0").append(posture);
//        cmd.append("0").append(drug);

        String check = getCheck(cmd.toString());
        cmd.append(check);
        return cmd.toString();
    }

    /**
     * 组装发送用户信息指令
     * 下发测量前参数（用户基本信息）
     * aa55 0c 17 000000000000000000000000 23
     */
    public static String getUserCmd() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(CmdConstants.Cmd_Start);
        cmd.append("15");
        cmd.append(CmdConstants.Code_user);

//        UserMarkBean bean = Hawk.get(AppConstants.Hawk_Mark_Bean);
        //年龄
        cmd.append(HexUtils.intToHex(30));
        //性别
        int sex = 1;
        if (sex == 0) {
            //女，发送相反
            cmd.append("01");
        } else {
            cmd.append("00");
        }
        //身高
        cmd.append("00");
        cmd.append( Integer.toHexString(175));
        //体重
        cmd.append("00");
        cmd.append( Integer.toHexString(60));
        //是否吃药
        cmd.append("00");
        //测试姿势
        cmd.append("00");
        //补零
        cmd.append("0000");
        cmd.append("0000");
        //是否高血压
        cmd.append("00");
        //补零
        cmd.append("00000000");
        cmd.append("00000000");
        //收缩压
//        cmd.append(HexUtils.intToHex(115, 2));
//        舒张压
//        cmd.append(HexUtils.intToHex(75, 2));

        String check = getCheck(cmd.toString());
        cmd.append(check);
        return cmd.toString();
    }

    /**
     * 处理测量前需要发送的标定信息
     * aa55 fd 18 31
     * 41cea91b417e2726414a593c413e2408c1ba815cc14b16134195c4f74167ff5c419ed3fb41a03d1a4000000053bafa0c53bafa0b421400003f800000431b000042380000000000003f80000042b40000427000003f789b4344f2066644ffeccd3ea3bcd33f5b3d083ef22d0e41b9999a3d81062543b74ccd470e519a481be3a6422266663dde69ad4234cccd3dfaacda3c86594b3f31eb853e4d35a83f004189412def9e40e644a2bf9200fd40fdd0d7be90d38ebf3fc2a7beee440640493766c030fbcf40bcefacbd8b6891406d0364c00e203f413bc1c04073e398c0066e15be0b904f411937a9c0839f22beba52b7bfcddc29bf310fc7bb8d00f6
     * 83
     * <p>
     * 31中的3表示有几包，1表示第几包
     * 注意：数据长度包含31
     */
    public static List<String> getMarkCmd(String mark) {
        if (TextUtils.isEmpty(mark)) return null;
        if (mark.length() % 2 != 0) return null;
        List<String> markList = new ArrayList<>();

        int dataLength = 200 * 2;
        int length = mark.length();
        int packageNum = length / dataLength;
        if (length % dataLength != 0) {
            packageNum = packageNum + 1;
        }

        String data = "";
        for (int i = 1; i <= packageNum; i++) {
            StringBuilder cmd = new StringBuilder();
            cmd.append(CmdConstants.Cmd_Start);

            if (i == 1) {
                data = mark.substring(0, dataLength);
            } else if (i == packageNum) {
                data = mark.substring(dataLength * (i - 1));
            } else {
                data = mark.substring(dataLength * (i - 1), dataLength * i);
            }

            String packageHex = HexUtils.intToHexBit(packageNum, 1)
                    + HexUtils.intToHexBit(i, 1);
            cmd.append(CmdUtils.getLength(packageHex + data));
            cmd.append(CmdConstants.Code_send_mask);
            cmd.append(packageHex);
            cmd.append(data);

            String check = getCheck(cmd.toString());
            cmd.append(check);

            markList.add(cmd.toString());
        }
        return markList;
    }

    /**
     * 筛选血氧中无用数据
     * 血氧中会返回一些0 或255 的数据
     *
     * @param data
     * @return
     */
    public static List<Integer> getSpo2Data(List<Integer> data) {
        List<Integer> dataNew = new ArrayList<>();
        for (int i : data) {
            if (i > 30 && i < 200) {
                dataNew.add(i);
            }
        }
        return dataNew;
    }


    /**
     * 筛选血氧中无用数据
     * 血氧中会返回一些0 或255 的数据
     *
     * @param data
     * @return
     */
    public static List<Integer> getHeart2Data(List<Integer> data) {
        List<Integer> dataNew = new ArrayList<>();
        for (int i : data) {
            if (i > 0 && i < 4096) {
                dataNew.add(i);
            }
        }
        return dataNew;
    }

    /**
     * 检查测量的数据是否合格
     * 收缩压  体检数据  mmHg  无小数点  是  41-300
     * 舒张压  体检数据  mmHg  无小数点  是  30-200
     * 心率  体检数据  bpm  无小数点  是  30-140
     * 血氧  体检数据  %  无小数点  否  70-100
     */
    public static boolean checkData(String type, int data) {
        if (CmdConstants.Type_sbp.equals(type)) {
            return data >= 41 && data <= 300;
        }
        if (CmdConstants.Type_dbp.equals(type)) {
            return data >= 30 && data <= 200;
        }
        if (CmdConstants.Type_heartRate.equals(type)) {
            return data >= 30 && data <= 140;
        }
        if (CmdConstants.Type_spo2.equals(type)) {
            return data >= 70 && data <= 100;
        }
        return false;
    }

    /**
     * 更新固件返回指令中的状态码
     * aa5504e175 00 00005a
     * 10 数据包顺序异常，请核对后重新发送 01 接收失败，请重新发送 00 指令接收成功
     */
    public static String getOtaBackState(String cmd) {
        if (TextUtils.isEmpty(cmd)) return null;
        //如果是指令接收成功返回
        String state = cmd.substring(10, 12);
        if (!"00".equals(state)) {
            return state;
        }
        return null;
    }

    /**
     * 更新固件返回指令中的包数
     * aa5504e17500 0000 5a
     */
    public static int getOtaBaoNum(String cmd) {
        if (TextUtils.isEmpty(cmd)) return -1;
        //如果是指令接收成功返回
        String state = cmd.substring(12, 16);
        return HexUtils.hexToInt(state);
    }

    /**
     * 更新固件返回指令中的包数
     * aa551085 ...
     * aa551085 01 00 20 00000000000004010101000000bd
     * 数据第一字节：固件版本号高位
     * 数据第二字节：固件版本号中位
     * 数据第三字节：固件件版本号低位(7:4)最低位(3:0)
     * 例：v1.20.1.1 传输高位 1，中位 20，低 位 17(0x11)
     * <p>
     * 低位0x11 .1.1
     * 低位0x84
     */
    public static String getVersionInfo(String cmd) {
        if (TextUtils.isEmpty(cmd)) return null;
        //如果是指令接收成功返回
        int v1 = HexUtils.hexToInt(cmd.substring(8, 10));
        int v2 = HexUtils.hexToInt(cmd.substring(10, 12));
        int v3_1 = HexUtils.hexToInt(cmd.substring(12, 13));
        int v3_2 = HexUtils.hexToInt(cmd.substring(13, 14));
        return v1 + "." + v2 + "." + v3_1 + "." + v3_2;
    }

}
