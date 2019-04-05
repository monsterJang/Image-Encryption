package com.msk.imageencryption.util;

import java.util.Arrays;

/**
 * author   : 陈龙江
 * time     : 2019/4/3 15:40
 * desc     : 一系列数组方面的操作
 * version  : 1.0
 */
public class ArrayUtil {

    /**
     * author : 陈龙江
     * time   : 2019/4/3 15:55
     * desc   : 二维数组降一维
     */
    public static void change2Dto1D(int arr2D[][], int arr1D[], int M, int N) {
        int k = 0;
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                arr1D[k++] = arr2D[i][j];
            }
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 15:58
     * desc   : 一维数组升二维
     */
    public static void change1Dto2D(int arr1D[], int arr2D[][], int M, int N) {
        int k = 0;
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                arr2D[i][j] = arr1D[k++];
            }
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 16:03
     * desc   : 将二维数组行列倒换
     */
    public static void switchRowCol(int source[][], int result[][], int M, int N) {
        for (int i = 0; i < M; ++i) {
            for (int j = 0; j < N; ++j) {
                result[j][i] = source[i][j];
            }
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 16:07
     * desc   : 一维double数组的复制
     */
    public static void copy(double source[], double result[], int N) {
        for (int i = 0; i < N; ++i) {
            result[i] = source[i];
        }
    }

    /**
     * author : 陈龙江
     * time   : 2019/4/3 22:02
     * desc   : 一维double数组的排序
     */
    public static void sort(double source[], int N) {
        Arrays.sort(source);
    }

}
