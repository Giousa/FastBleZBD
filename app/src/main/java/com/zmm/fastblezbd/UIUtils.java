package com.zmm.fastblezbd;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;

import com.zmm.fastblezbd.application.SGBApplication;


/**
 * Description:
 * Author:zhangmengmeng
 * Date:2017/3/17
 * Time:下午1:32
 */

public class UIUtils {

    /**
     * @return	全局的上下文环境
     */
    public static Context getContext(){
        return SGBApplication.getContext();
    }

    /**
     * @return	全局的hander对象
     */
    public static Handler getHandler(){
        return SGBApplication.getHandler();
    }

    /**
     * @return	返回主线程方法
     */
    public static Thread getMainThread(){
        return SGBApplication.getMainThread();
    }

    /**
     * @return	返回主线程id方法
     */
    public static int getMainThreadId(){
        return SGBApplication.getMainThreadId();
    }

    /**
     * 布局文件转换成view对象方法
     * @param layoutId	布局文件id
     * @return	布局文件转换成的view对象
     */
    public static View inflate(int layoutId){
        return View.inflate(getContext(), layoutId, null);
    }

    /**
     * @return	返回资源文件夹对象方法
     */
    public static Resources getResources(){
        return getContext().getResources();
    }

    /**
     * @param stringId	字符串在xml中对应R文件中的id
     * @return	string.xml某节点,对应的值
     */
    public static String getString(int stringId){
        return getResources().getString(stringId);
    }

    /**
     * @param runnable	将任务保证在主线程中运行的方法
     */
    public static void runInMainThread(Runnable runnable){
        if(android.os.Process.myTid() == getMainThreadId()){
            runnable.run();
        }else{
            getHandler().post(runnable);
        }
    }

    /**
     * 执行延迟任务的操作
     * @param runnableTask 延时任务
     * @param delayTime	   延时时间
     */
    public static void postDelayed(Runnable runnableTask, int delayTime) {
        getHandler().postDelayed(runnableTask,delayTime);

    }

    /**
     * 根据年月获得 这个月总共有几天
     * @param year
     * @param month
     * @return
     */
    public static int getDay(int year, int month) {
        int day = 30;
        boolean flag = false;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

    /**
     * int到字节数组的转换.
     */
    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8;// 向右移8位
        }
        return b;
    }

    /**
     * 字节数组到int的转换.
     */
    public static int byteToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }
}
