package com.msk.imageencryption.util;

import java.util.HashMap;

/**
 * author   : 陈龙江
 * time     : 2019/4/3 21:38
 * desc     :
 * version  : 1.0
 */
public class ScramblingUtil {

    private static final String TAG = "ScramblingUtil";

    /**
     * author : 陈龙江
     * time   : 2019/4/3 21:41
     * desc   : 生成以initValue为初值logistic混沌序列
     *          Logistic映射定义为：f(x) = u * f(x-1) * (1 - f(x-1))
     *          记 u_0 = 3.569945672…，有 u_0 < u <= 4
     */
    private static void generateLogisticArray(double initValue, double logisticArray[], int N) {
        double u = 3.88888888;
        logisticArray[0] = initValue;
        for (int i = 1; i < N; ++i) {
            logisticArray[i] = u * logisticArray[i - 1] * (1 - logisticArray[i - 1]);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 21:47
     * desc   : 根据混沌序列，生成 值-->下标 的反向映射
     */
    private static void generateMapKeyIndex(HashMap mapKeyIndex, double logisticArray[], int N) {
        mapKeyIndex.clear();
        for (int i = 0; i < N; ++i) {
            mapKeyIndex.put(logisticArray[i], i);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:06
     * desc   : 根据值-下标反向映射和混沌序列，生成地址映射表
     *          若 x 为混沌序列中的某元素值，则地址映射为：
     *              x在混沌序列中的下标-->x在排序后的混沌序列中的下表
     */
    private static void generateAddressArray(int addressArray[], HashMap mapKeyIndex, double logisticArray[], int N) {
        // 排序混沌序列
        double sortedLogisticArray[] = new double[N];
        ArrayUtil.copy(logisticArray, sortedLogisticArray, N);
        ArrayUtil.sort(sortedLogisticArray, N);
        // 生成地址映射
        for (int i = 0; i < N; ++i) {
            int address = Integer.parseInt(String.valueOf(mapKeyIndex.get(sortedLogisticArray[i])));
            addressArray[address] = i;
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:23
     * desc   : 加密，将行置乱算法作用于curRow行
     * return : 返回混沌序列的最后一个元素作为生成下一个混沌序列的初值
     */
    private static double rowScramblingEncrypt(double initValue, int curRow, int pixel[][], int M, int N) {
        // 生成混沌序列
        double logisticArray[] = new double[N];
        generateLogisticArray(initValue,logisticArray, N);
        // 生成混沌序列的 值-下标 的映射
        HashMap mapKeyIndex = new HashMap<Double, Integer>();
        generateMapKeyIndex(mapKeyIndex, logisticArray, N);
        // 生成地址映射表
        int addressArray[] = new int[N];
        generateAddressArray(addressArray, mapKeyIndex, logisticArray, N);
        // 用临时数组保存被置乱后的像素
        int temp[] = new int[N];
        for (int j = 0; j < N; ++j) {
            temp[addressArray[j]] = pixel[curRow][j];
        }
        // 置乱原图
        for (int j = 0; j < N; ++j) {
            pixel[curRow][j] = temp[j];
        }
        return logisticArray[N - 1];
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:17
     * desc   : 加密，将行置乱算法作用于curCol列
     * return : 返回混沌序列的最后一个元素作为生成下一个混沌序列的初值
     */
    private static double colScramblingEncrypt(double initValue, int curCol, int pixel[][], int M, int N) {
        // 生成混沌序列
        double logisticArray[] = new double[M];
        generateLogisticArray(initValue,logisticArray, M);
        // 生成混沌序列的 值-下标 的映射
        HashMap mapKeyIndex = new HashMap<Double, Integer>();
        generateMapKeyIndex(mapKeyIndex, logisticArray, M);
        // 生成地址映射表
        int addressArray[] = new int[M];
        generateAddressArray(addressArray, mapKeyIndex, logisticArray, M);
        // 用临时数组保存被置乱后的像素
        int temp[] = new int[M];
        for (int i = 0; i < M; ++i) {
            temp[addressArray[i]] = pixel[i][curCol];
        }
        // 置乱原图
        for (int i = 0; i < M; ++i) {
            pixel[i][curCol] = temp[i];
        }
        return logisticArray[M - 1];
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:34
     * desc   : 加密，将行置乱算法作用于所有行
     */
    private static void rowScramblingEncryptForAll(double initValue, int pixel[][], int M, int N) {
        double value = initValue;
        for (int i = 0; i < M; ++i) {
            value = rowScramblingEncrypt(value, i, pixel, M, N);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:26
     * desc   : 加密，将列置乱算法作用于所有列
     */
    private static void colScramblingEncryptForAll(double initValue, int pixel[][], int M, int N) {
        double value = initValue;
        for (int j = 0; j < N; ++j) {
            value = colScramblingEncrypt(value, j, pixel, M, N);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/4 2:28
     * desc   : 解密，将行置乱算法作用于curRow行
     * return : 返回混沌序列的最后一个元素作为生成下一个混沌序列的初值
     */
    private static double rowScramblingDecrypt(double initValue, int curRow, int pixel[][], int M, int N) {
        // 生成混沌序列
        double logisticArray[] = new double[N];
        generateLogisticArray(initValue,logisticArray, N);
        // 生成混沌序列的 值-下标 的映射
        HashMap mapKeyIndex = new HashMap<Double, Integer>();
        generateMapKeyIndex(mapKeyIndex, logisticArray, N);
        // 生成地址映射表
        int addressArray[] = new int[N];
        generateAddressArray(addressArray, mapKeyIndex, logisticArray, N);
        // 用临时数组保存被置乱后的像素
        int temp[] = new int[N];
        for (int j = 0; j < N; ++j) {
            temp[j] = pixel[curRow][addressArray[j]];
        }
        // 置乱原图
        for (int j = 0; j < N; ++j) {
            pixel[curRow][j] = temp[j];
        }
        return logisticArray[N - 1];
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:49
     * desc   : 解密，将列置乱算法作用于curCol列
     * return : 返回混沌序列的最后一个元素作为生成下一个混沌序列的初值
     */
    private static double colScramblingDecrypt(double initValue, int curCol, int pixel[][], int M, int N) {
        // 生成混沌序列
        double logisticArray[] = new double[M];
        generateLogisticArray(initValue,logisticArray, M);
        // 生成混沌序列的 值-下标 的映射
        HashMap mapKeyIndex = new HashMap<Double, Integer>();
        generateMapKeyIndex(mapKeyIndex, logisticArray, M);
        // 生成地址映射表
        int addressArray[] = new int[M];
        generateAddressArray(addressArray, mapKeyIndex, logisticArray, M);
        // 用临时数组保存被置乱后的像素
        int temp[] = new int[M];
        for (int i = 0; i < M; ++i) {
            temp[i] = pixel[addressArray[i]][curCol];
        }
        // 置乱原图
        for (int i = 0; i < M; ++i) {
            pixel[i][curCol] = temp[i];
        }
        return logisticArray[M - 1];
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/4 2:30
     * desc   : 解密，将行置乱算法作用于所有行
     */
    private static void rowScramblingDecryptForAll(double initValue, int pixel[][], int M, int N) {
        double value = initValue;
        for (int i = 0; i < M; ++i) {
            value = rowScramblingDecrypt(value, i, pixel, M, N);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:36
     * desc   : 解密，将列置乱算法作用于所有列
     */
    private static void colScramblingDecryptForAll(double initValue, int pixel[][], int M, int N) {
        double value = initValue;
        for (int j = 0; j < N; ++j) {
            value = colScramblingDecrypt(value, j, pixel, M, N);
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:38
     * desc   : 用置乱算法进行加密
     */
    public static void encrypt(double initValue, int pixel[][], int M, int N) {
        LogUtil.d(TAG, "encrypt: begin");
        rowScramblingEncryptForAll(initValue, pixel, M, N);
        colScramblingEncryptForAll(initValue, pixel, M, N);
        LogUtil.d(TAG, "encrypt: end");
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/7 20:41
     * desc   : 用置乱算法进行解密
     */
    public static void decrypt(double initValue, int pixel[][], int M, int N) {
        LogUtil.d(TAG, "decrypt: begin");
        colScramblingDecryptForAll(initValue, pixel, M, N);
        rowScramblingDecryptForAll(initValue, pixel, M, N);
        LogUtil.d(TAG, "decrypt: end");
    }

}
