package com.fly.algorithm.array;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 判断两个数据是否相等，如果数组A中的每个元素在数组B中都能找到， 
 * 且数组B中的每个元素也能在数组A中找到，则说明这两个数组相同 例如：a={1,2,2,3,4}
 * b={1,2,3,4},则说明a=b
 * 
 * @author fly
 */
public class SameArray
{

    public static void main(String[] args)
    {
        Integer[] a = {2, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 5};
        Integer[] b = {1, 4, 2, 3, 4};
        System.out.print("相同的数组：");
        System.out.println(testForceResolve(a, b));
        System.out.println(testUseList(a, b));
        System.out.println(testUseMap(a, b));
        System.out.println(testTwoPointer(a, b));

        Integer[] a1 = {2, 1, 2, 3, 3, 4, 4, 4, 4, 4, 4, 5};
        Integer[] b1 = {1, 4, 2, 3, 4};
        System.out.print("不同的数组：");
        System.out.println(testForceResolve(a1, b1));
        System.out.println(testUseList(a1, b1));
        System.out.println(testUseMap(a1, b1));
        System.out.println(testTwoPointer(a1, b1));
    }

    public static boolean testForceResolve(Integer[] a, Integer[] b)
    {
        if (commonCheck(a, b))
        {
            return forceResolve(a, b) && forceResolve(b, a);
        }
        else
        {
            return false;
        }
    }

    public static boolean testUseList(Integer[] a, Integer[] b)
    {
        if (commonCheck(a, b))
        {
            return useList(a, b) && useList(b, a);
        }
        return false;
    }

    public static boolean testUseMap(Integer[] a, Integer[] b)
    {
        if (commonCheck(a, b))
        {
            return useMap(a, b);
        }
        return false;
    }

    public static boolean testTwoPointer(Integer[] a, Integer[] b)
    {
        if (commonCheck(a, b))
        {
            return twoPointer(a, b);
        }
        return false;
    }

    public static boolean commonCheck(Integer[] a, Integer[] b)
    {
        /**
         * 如果有一个数组未初始化，则认为不相等
         */
        if (a == null || b == null)
        {
            return false;
        }
        /**
         * 如果两个空数组，则认为相等
         */
        if (a.length == 0 && b.length == 0)
        {
            return true;
        }
        return true;
    }

    /**
     * 第一种解法，暴力破解, 即采用两次for循环来对比， 
     * 该方法可以判断a中的元素是否全在b中，如果要相等，还得再调用一次。
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean forceResolve(Integer[] a, Integer[] b)
    {

        boolean result = true;
        for (int i = 0; i < a.length; i++ )
        {
            boolean isContain = false;// 数组b是否包含数组a
            for (int j = 0; j < b.length; j++ )
            {
                if (a[i] == b[j])
                {
                    isContain = true;
                    break;
                }
            }
            // 只要a数组有一个不存在数组b中，则说明不相等
            if (!isContain)
            {
                result = false;
                break;
            }

        }
        return result;
    }

    /**
     * 借助List.contains()方法，减少一次循环,但是也需要调用两次
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean useList(Integer a[], Integer[] b)
    {
        List array = Arrays.asList(b);
        boolean result = true;
        for (int i = 0; i < a.length; i++ )
        {
            if (!array.contains(a[i]))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 使用HashMap,通过其remove方法的返回值， 
     * 来判断是否包含， 删除之后，如果hashMap为空，则表示相等
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean useMap(Integer a[], Integer[] b)
    {
        boolean result = true;
        Map<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        /**
         * 使用set去除重复数据，防止第二次删除时为空
         */
        Set<Integer> set = new HashSet<Integer>();
        set.addAll(Arrays.asList(b));
        for (Integer i : a)
        {
            hashMap.put(i, 0);
        }

        for (Integer j : set)
        {
            if (hashMap.remove(j) == null)
            {
                result = false;
                break;
            }
        }
        // 如果b的每个元素都能通过a中的元素删除，则表示两个数组相等
        return result && hashMap.size() == 0;
    }

    /**
     * 首先对两个数组进行排序，然后用两个指针进行比较
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean twoPointer(Integer a[], Integer[] b)
    {
        SortUtils.sort(a, 0, a.length - 1);
        SortUtils.sort(b, 0, b.length - 1);
        if (a[0] != b[0])
        {
            return false;
        }
        int i = 0;
        int j = 0;
        boolean result = true;
        int a_last = 0;
        int b_last = 0;
        while (i < a.length && j < b.length & result)
        {

            if (a[i] < b[j])
            {
                i++ ;
            }
            else if (a[i] > b[j])
            {
                j++ ;
            }
            else
            {
                i++ ;
                j++ ;
            }

        }

        // 最后检查两个数组未比较的部分
        if (i <= a.length - 1)
        {
            b_last = b[j - 1];
            for (; i < a.length; i++ )
            {
                if (b_last != a[i])
                {
                    result = false;
                    break;
                }
            }
        }
        if (j <= b.length - 1)
        {
            a_last = a[i - 1];
            for (; j < b.length; j++ )
            {
                if (a_last != b[j])
                {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 快速排序工具类
     * 
     * @author fly
     */
    static class SortUtils
    {
        public static int division(Integer[] list, int left, int right)
        {
            int baseNum = list[left];
            while (left < right)
            {
                while (left < right && list[right] >= baseNum)
                {
                    right-- ;
                }
                list[left] = list[right];
                while (left < right && list[left] <= baseNum)
                {
                    left++ ;
                }
                list[right] = list[left];
            }
            list[left] = baseNum;
            return left;
        }

        public static void sort(Integer[] list, int left, int right)
        {
            if (left < right)
            {
                int i = division(list, left, right);
                sort(list, left, i - 1);
                sort(list, i + 1, right);
            }
        }
    }

}
