package com.fly.algorithm.leetcode;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Given an array of integers, find two numbers such that they add up to a
 * specific target number. The function twoSum should return indices of the two
 * numbers such that they add up to the target, where index1 must be less than
 * index2. Please note that your returned answers (both index1 and index2) are
 * not zero-based. You may assume that each input would have exactly one
 * solution. Input: numbers={2, 7, 11, 15}, target=9 Output: index1=1, index2=2
 * 
 * @author fly
 *
 */
public class TwoSum {

	public static void main(String[] args) {
		int numbers[] = { 2, 7, 4, 5, 6, 15, 10, 12, 24 };
		int target = 9;
		useHashMapResolve(numbers, target);
	}

	/**
	 * 暴力破解，根据每个值与目标数的差，然后在找对应的差值 比如9-5=4，则只需子数组中找到4即可
	 * 
	 * @param numbers
	 * @param target
	 */
	public static void forceResolve(int[] numbers, int target) {
		boolean notFound = true;
		for (int i = 0; i < numbers.length; i++) {
			int needValue = target - numbers[i];
			for (int j = 1; j < numbers.length; j++) {
				if (numbers[j] == needValue) {
					notFound = false;
					System.out.println("Output: index1=" + (i + 1)
							+ ", index2=" + (j + 1));
				}
			}
		}
		if (notFound) {
			System.out.println("not found");
		}
	}

	public static void sortAfterResolve(int[] numbers, int target) {
		Arrays.sort(numbers);
		System.out.println(numbers);
	}

	public static void useHashMapResolve(int[] numbers, int target) {
		if (numbers == null || numbers.length < 2)
			return;
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < numbers.length; i++) {
			if (map.containsKey(target - numbers[i])) {
				System.out.println("Output: index1=" + (map.get(target - numbers[i]) + 1)
						+ ", index2=" + (i + 1));
			}
			map.put(numbers[i], i);
		}
	}
}
