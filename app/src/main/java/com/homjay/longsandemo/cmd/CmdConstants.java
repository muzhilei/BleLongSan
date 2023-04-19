package com.homjay.longsandemo.cmd;

/**
 * @Author: taimin
 * @Date: 2021/6/3
 * @Description: 指令
 */
public class CmdConstants {
    //蓝牙名字
    public static final String BLU_TAG = "Blood_Pressure";
    public static final String BLU_TAG_HLK = "HLK";
    // 透传数据的相关服务与特征
    public static final String UUID_Server = "6e400001-b5a3-f393-e0a9-e50e24dcca9f";
    public static final String UUID_Notify = "6e400003-b5a3-f393-e0a9-e50e24dcca9f";
    public static final String UUID_Write = "6e400002-b5a3-f393-e0a9-e50e24dcca9f";
    public static final String UUID_Chanage_CPU = "6e4000a1-b5a3-f393-e0a9-e50e24dcca9f";//控制进入硬件升级模式，或者重启

    public static final String UUID_Electricity_Server = "0000180f-0000-1000-8000-00805f9b34fb";//遥控器电池服务
    public static final String UUID_Electricity_Notify = "00002a19-0000-1000-8000-00805f9b34fb";//遥控器电池通知

    public static final String UUID_Handsetmac_Server = "0000180a-0000-1000-8000-00805f9b34fb";//遥控器固定MAC服务
    public static final String UUID_Handsetmac_Notify = "0000ff60-0000-1000-8000-00805f9b34fb";//遥控器固定MAC通知

    public static final String UUID_Read_Mac = "0000fe01-0000-1000-8000-00805f9b34fb";
    public static final String UUID_Read_Elec = "0000fe02-0000-1000-8000-00805f9b34fb";

    //血压夹子
//    public static final String UUID_Server = "0000fff0-0000-1000-8000-00805f9b34fb";
//    public static final String UUID_Notify = "0000fff1-0000-1000-8000-00805f9b34fb";
//    public static final String UUID_Write = "0000fff2-0000-1000-8000-00805f9b34fb";

//    public static final String UUID_Read_Macw = "0000fe01-0000-1000-8000-00805f9b34fb";
//    public static final String UUID_Read_Elecw = "0000fe02-0000-1000-8000-00805f9b34fb";

    public static final String Type_bp = "bp";//血压
    public static final String Type_sbp = "sbp";//收缩压
    public static final String Type_dbp = "dbp";//舒张压
    public static final String Type_heartRate = "heartRate";//心率
    public static final String Type_spo2 = "spo2";//血氧
    public static final String Type_spo2_x = "spo2_x";//血氧波形图

    //指令code
    public static final String Code_hrv= "98";//hrv
    public static final String Code_heart = "a0";//心电波形
    public static final String Code_wo = "92";//握手包
    public static final String Code_back = "84";//指令接收成功
    public static final String Code_bp = "b0";//血压
    public static final String Code_heartRate = "a7";//心率
    public static final String Code_spo2 = "a9";//血氧
    public static final String Code_spo2_x = "a8";//血氧波形图
    public static final String Code_pm = "91"; //返回pm
    public static final String Code_sn = "d0"; //返回sn
    public static final String Code_start_measure_mask = "10"; //开始测量,开始标定
    public static final String Code_user = "17"; //下发测量前参数（用户基本信息）
    public static final String Code_send_mask = "18"; //测量前发送标定
    public static final String Code_ota = "e1"; //升级固件回复
    public static final String Code_ota_check = "e3"; //升级固件回复，校验是否成功
    public static final String Code_version = "85"; //软硬件信息

    //固定指令
    public static final String Cmd_Start = "aa55";
    public static final String Cmd_Wake = "aa5501720073";
    public static final String Cmd_Heart = "aa5501130014";

    //获取序列号
    public static final String Cmd_Sn = "aa55005353";
    public static final String Cmd_Sn_Start = "aa5517d0";

    //标定指令
    public static final String Cmd_Location_Start = "aa55371004313630363239363231333937396571737967317631363036323936323133393830677565386c3176697166697a626d6370623031000081";
    public static final String Cmd_Location_Start_end = "65386c3176697166697a626d6370623031000081";
    public static final String Cmd_Location_end = "aa550211000417";

    //测量指令
    public static final String Cmd_Send_User = "aa550c1700000000000000000000000023";
    public static final String Cmd_Start_Measure = "aa5537100f313630363239363834353334357a33776c7a6572313630363239363834353334357a33776c7a65723030000000000000000000000000ce";
    public static final String Cmd_Location_1 = "aa55fd183141cea91b417e2726414a593c413e2408c1ba815cc14b16134195c4f74167ff5c419ed3fb41a03d1a4000000053bafa0c53bafa0b421400003f800000431b000042380000000000003f80000042b40000427000003f789b4344f2066644ffeccd3ea3bcd33f5b3d083ef22d0e41b9999a3d81062543b74ccd470e519a481be3a6422266663dde69ad4234cccd3dfaacda3c86594b3f31eb853e4d35a83f004189412def9e40e644a2bf9200fd40fdd0d7be90d38ebf3fc2a7beee440640493766c030fbcf40bcefacbd8b6891406d0364c00e203f413bc1c04073e398c0066e15be0b904f411937a9c0839f22beba52b7bfcddc29bf310fc7bb8d00f683";
    public static final String Cmd_Location_2 = "aa55fd183240bdd91f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004278000042780000421400003f800000431b000042380000000000003f8000004334000042f000003f76f45645371333452e00003ec2c3ca3f39f55a3f24b5dd41b666663d8c7e2843a200004731b666486de2cd425f33333e29c77a4230cccd3e0aa64c3cb780343f30a3d73e99652c3f1ac711418c4fdf40f5e3babf777f2b4106d163bf07d20bbee4a132be47e28b40480de1c02bb08840b8f790bd99f9c64080b229bfe60c6e413f48bac9";
    public static final String Cmd_Location_3 = "aa558d183340726ab3c000123abe1b380041204d19c07321b4bebe03ffbf9a479ebf00d8723ed4a8ea40ba25e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004278000042780000c22a5842c0a850bc42e67e1942b4000021";
    public static final String Cmd_Location_1_end = "22beba52b7bfcddc29bf310fc7bb8d00f683";
    public static final String Cmd_Location_2_end = "90bd99f9c64080b229bfe60c6e413f48bac9";
    public static final String Cmd_Location_3_end = "1942b4000021";
    public static final String Cmd_Measure_end = "aa550211000f22";

    //接收
    public static final String Rev_Send_User = "aa55028417009d";
    public static final String Rev_Location = "aa55028418009e";
    public static final String Rev_Measure = "aa550284100096";

    //升级固件
    public static final String Rev_Update_Start = "43"; //开始升级返回
    public static final String Rev_Update_Error_ReSend = "15"; //接收失败，请求重发同一个数据包
    public static final String Rev_Update_Error_Stop_Can = "18"; //送方发送的信息包序号不正确时，会发送CAN终止传输
    public static final String Rev_Update_Error_Host_Cancel = "44"; //主机取消
    public static final String Rev_Update_Error_Over_Time = "46"; //超时没有收到数据
    public static final String Rev_Update_Error_Wrong_Sequence = "47"; //包序号错
    public static final String Rev_Update_Error_Wrong_Head = "4c"; //头校验错误
    public static final String Rev_Update_Error_Wrong_Context = "4d"; //头校验错误
    public static final String Rev_Update_Error_Wrong_Characteristic = "50"; //签名错误
    public static final String Rev_Update_Bao_Start = "aa5504e100"; //升级包返回，aa5504e10 00 00000 e5 第0个包
    public static final String Rev_Update_Success = "06";//升级校验成功
    public static final String Rev_Update_Fail = "aa5501e300e4";//升级校验失败
    public static final String Cmd_Boot = "aa55000808"; //进入bootloader
    public static final String Rev_Boot = "aa5500e0e0";//进入bootloader,返回

    //获取版本信息
    public static final String Cmd_Version = "aa55000303";
}
