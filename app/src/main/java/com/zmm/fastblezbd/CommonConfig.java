package com.zmm.fastblezbd;

/**
 * Description:
 * Author:zhangmengmeng
 * Date:2018/3/29
 * Time:上午10:40
 */

public class CommonConfig {


    public static byte[] startByte = new byte[]{(byte) 0xA3, (byte) 0x20, (byte) 0x21, (byte) 0x80, (byte) 0x01};
    public static byte[] stopByte = new byte[]{(byte) 0xA3, (byte) 0x20, (byte) 0x21, (byte) 0x86, (byte) 0x00};
    public static byte[] pauseByte = new byte[]{(byte) 0xA3, (byte) 0x20, (byte) 0x21, (byte) 0x85, (byte) 0x01};
    public static byte[] setByte = new byte[]{(byte) 0xA3, (byte) 0x20, (byte) 0x21, (byte) 0x81, (byte) 0x01,(byte) 0x04, (byte) 0x08, (byte) 0x09, (byte) 0x41, (byte) 0x51};
}
