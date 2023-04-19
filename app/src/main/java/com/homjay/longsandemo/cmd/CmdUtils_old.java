package com.homjay.longsandemo.cmd;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: taimin
 * @Date: 2021/4/22
 * @Description: 处理指令
 */
public class CmdUtils_old {

    //蓝牙名字
    public static final String BLU_TAG = "HLK";
    // 透传数据的相关服务与特征
    public static final String UUID_Server = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_Read = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String UUID_Write = "0000fff2-0000-1000-8000-00805f9b34fb";

    public static final String Code_wo = "92";//握手包
    public static final String Code_bp = "b0";//血压
    public static final String Code_heartRate = "a2";//心率
    public static final String Code_spo2 = "a9";//血氧
    public static final String Code_spo2_x = "a8";//血氧波形图

    public static final String Type_bp = "bp";//血压
    public static final String Type_sbp = "sbp";//收缩压
    public static final String Type_dbp = "dbp";//舒张压
    public static final String Type_heartRate = "heartRate";//心率
    public static final String Type_spo2 = "spo2";//血氧
    public static final String Type_spo2_x = "spo2_x";//血氧波形图

    public static StringBuilder mCmd = new StringBuilder();//指令临时容器
    public static final String Cmd_Start = "aa55";

    //临时的指令
    //指令
    public static final String CMD_H = "aa55";
    public static final String CMD_WAKE = "aa5501720073";
    public static final String CMD_HEART = "aa5501130014";
    //标定指令
    public static final String CMD_Location_Start = "aa55371004313630363239363231333937396571737967317631363036323936323133393830677565386c3176697166697a626d6370623031000081";
    public static final String CMD_Location_Start_end = "65386c3176697166697a626d6370623031000081";
    public static final String CMD_Location_end = "aa550211000417";
    public static final String CMD_PM_1 = "aa55819121";
    public static final String CMD_PM_2 = "aa55819122";
    //测量指令
    public static final String CMD_Send_User = "aa550c1700000000000000000000000023";
    public static final String CMD_Start_Measure = "aa5537100f313630363239363834353334357a33776c7a6572313630363239363834353334357a33776c7a65723030000000000000000000000000ce";
    public static final String CMD_Location_1 = "aa55fd183141cea91b417e2726414a593c413e2408c1ba815cc14b16134195c4f74167ff5c419ed3fb41a03d1a4000000053bafa0c53bafa0b421400003f800000431b000042380000000000003f80000042b40000427000003f789b4344f2066644ffeccd3ea3bcd33f5b3d083ef22d0e41b9999a3d81062543b74ccd470e519a481be3a6422266663dde69ad4234cccd3dfaacda3c86594b3f31eb853e4d35a83f004189412def9e40e644a2bf9200fd40fdd0d7be90d38ebf3fc2a7beee440640493766c030fbcf40bcefacbd8b6891406d0364c00e203f413bc1c04073e398c0066e15be0b904f411937a9c0839f22beba52b7bfcddc29bf310fc7bb8d00f683";
    public static final String CMD_Location_2 = "aa55fd183240bdd91f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004278000042780000421400003f800000431b000042380000000000003f8000004334000042f000003f76f45645371333452e00003ec2c3ca3f39f55a3f24b5dd41b666663d8c7e2843a200004731b666486de2cd425f33333e29c77a4230cccd3e0aa64c3cb780343f30a3d73e99652c3f1ac711418c4fdf40f5e3babf777f2b4106d163bf07d20bbee4a132be47e28b40480de1c02bb08840b8f790bd99f9c64080b229bfe60c6e413f48bac9";
    public static final String CMD_Location_3 = "aa558d183340726ab3c000123abe1b380041204d19c07321b4bebe03ffbf9a479ebf00d8723ed4a8ea40ba25e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004278000042780000c22a5842c0a850bc42e67e1942b4000021";
    public static final String CMD_Location_1_end = "22beba52b7bfcddc29bf310fc7bb8d00f683";
    public static final String CMD_Location_2_end = "90bd99f9c64080b229bfe60c6e413f48bac9";
    public static final String CMD_Location_3_end = "1942b4000021";
    public static final String CMD_Measure_end = "aa550211000f22";
    //接收
    public static final String REV_Send_User = "aa55028417009d";
    public static final String REV_Location = "aa55028418009e";
    public static final String REV_Measure = "aa550284100096";

    /**
     * 解析数据,把合并以及分段接收的数据整理 (待处理)
     *
     * @return
     */
    public static List<String> getDataCmdList(String data) {
        if (TextUtils.isEmpty(data)) return null;

        mCmd.append(data);
        String mCmdStr = mCmd.toString();
        if (!mCmdStr.startsWith(Cmd_Start)) {
            //数据起始位不包含头部
            if (mCmdStr.contains(Cmd_Start)) {
                //数据包含头部(把头部起始位之前数据去掉)
                int i = mCmdStr.indexOf(Cmd_Start);
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

        cmdStr = mCmdStr.split(Cmd_Start);
        //如果只有一个指令，直接处理
        for (int i = 0; i < cmdStr.length; i++) {
            String cmd = Cmd_Start + cmdStr[i];
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
     * 解析数据的真实值
     * aa5504b00078004f7baa5502a90052fdaa5502a20036daaa5501920093aa5508a88080808080808080b0
     * aa55 04b00078004f7b
     *
     * @param cmd
     * @return
     */
    public static Map<String, List<Integer>> getData(String cmd) {
        try {
            if (TextUtils.isEmpty(cmd) || !cmd.startsWith(Cmd_Start)) return null;

            Map<String, List<Integer>> map = new HashMap<>();
            String[] cmdStr = cmd.split(Cmd_Start);
            for (String s : cmdStr) {
                if (TextUtils.isEmpty(s)) continue;

                int length = s.length();
                //判断校验
                String checkStartHex = s.substring(0, length - 2);
                String checkHex = s.substring(length - 2, length);
                if (!check(checkStartHex, checkHex)) continue;

                //解析数据
                String codeHex = s.substring(2, 4);
                String dataHex = s.substring(4, length - 2);
                getPropertyData(codeHex, dataHex, map);
            }

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取单个属性的数据
     * b0 0078004f
     *
     * @return
     */
    private static void getPropertyData(String codeHex, String data, Map<String, List<Integer>> map) {
        List<Integer> ints = new ArrayList<>();
        if (Code_bp.equals(codeHex)) {
            //血压
            int sbp = Integer.valueOf(data.substring(0, 4), 16);
            int dbp = Integer.valueOf(data.substring(4), 16);
            if (sbp == 0 && dbp == 0) return;
            ints.add(sbp);
            ints.add(dbp);
            map.put(Type_bp, ints);
        } else if (Code_heartRate.equals(codeHex)) {
            //心率
            int i = Integer.valueOf(data, 16);
            if (i == 0) return;
            ints.add(i);
            map.put(Type_heartRate, ints);
        } else if (Code_spo2.equals(codeHex)) {
            //血氧
            int i = Integer.valueOf(data, 16);
            if (i == 0) return;
            ints.add(i);
            map.put(Type_spo2, ints);
        } else if (Code_spo2_x.equals(codeHex)) {
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

            List<Integer> dataNew = getSpo2Data(ints);
            if (dataNew.size() == 0) return;
            map.put(Type_spo2_x, dataNew);
        }
    }

    /**
     * 防止一包数据中有多个同类型数据，把数据增加到List中
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
     * 是否有效长度
     * aa55 04 b0 0078004f 7b
     *
     * @return
     */
    private static boolean isLength(String cmd) {
        if (TextUtils.isEmpty(cmd)) return false;
        if (!cmd.startsWith(Cmd_Start)) return false;
        int length = cmd.length();
        if (length < 10) return false;
        String s = cmd.substring(4, 6); //数据长度
        int d = Integer.valueOf(s, 16); //转十进制
        return cmd.length() == (10 + d * 2);
    }

    /**
     * 筛选血氧中无用数据
     *
     * @param data
     * @return
     */
    public static List<Integer> getSpo2Data(List<Integer> data) {
        List<Integer> dataNew = new ArrayList<>();
        for (int i : data) {
            if (i != 0 && i != 255) {
                dataNew.add(i);
            }
        }
        return dataNew;
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

}
