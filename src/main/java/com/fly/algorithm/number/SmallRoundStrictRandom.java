package com.fly.algorithm.number;

import java.util.Random;

/**
 * 功能：严谨的小范围随机数生成类。在一个随机过程中，可以严谨的生成指定范围内不重复的随机数。
 * 例如：你取0—9的随机数，在0-9都获被取过一次之前，不会有某个数出现2次。
 * 
 * 注意：是取小范围随机数，如果取的范围比较大，请使用系统的随机数获取函数，
 * 因为本算法要记录随机数使用情况信息，所以有一定的内存开销，算法的主要内存开销为int[范围]。
 * 
 * @author cjc 2011-0512
 */
public class SmallRoundStrictRandom {
	/**
	 * 随机数种子，内部算法使用
	 */
	private static Random random = new Random();

	/**
	 * 随机数分布数组
	 */
	private int[] data = null;

	/**
	 * 一次随机过程中，已经获取过数据，和未获取过的数据的分界index
	 */
	private int indexBoundary;

	/**
	 * 创建本类对象的方法
	 * 
	 * @param maxSize
	 *            要取随机数的范围，例如10就是取0~9的随机数
	 * @return
	 */
	public static SmallRoundStrictRandom create(int maxSize) {
		if (maxSize <= 0) {
			System.out
					.println("Err! SmallRoundRandom maxSize must greater than 0..");
			return null;
		}
		return new SmallRoundStrictRandom(maxSize);
	}

	/**
	 * 内部构造方法
	 * 
	 * @param maxSize
	 */
	private SmallRoundStrictRandom(int maxSize) {
		data = new int[maxSize];
		initRecord();
	}

	/**
	 * 在每个随机过程开始前，初始化随机数分布的功能函数，仅内部算法使用
	 */
	private void initRecord() {
		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}
		indexBoundary = data.length;
	}

	/**
	 * 获取随机数的函数
	 * 
	 * @return 获取出来的随机数
	 */
	public int getRandomData() {
		// 一个随机过程结束以后，需要重新初始化随机数分布。
		// 比如要取0-9的随机数，当0-9都获取过了一次以后就需要重新初始化一下。
		if (indexBoundary == 0) {
			initRecord();
		}
		// 要获取的随机数的位置，只在未获取过的数据中找一个。
		int index = random.nextInt(indexBoundary);

		// 获取过的随机数，就放到分界点的后面，以便实现每个数在一个随机过程中只被获取一次
		indexBoundary--;
		int temp = data[indexBoundary];
		data[indexBoundary] = data[index];
		data[index] = temp;

		return data[indexBoundary];
	}

	public static void main(String[] args) {
		SmallRoundStrictRandom r = SmallRoundStrictRandom.create(5);// 需要得到0~4的随机数
		for (int i = 0; i < 23; i++) {
			if (i % 5 == 0)
				System.out.println("------------");
			int a = r.getRandomData();
			System.out.println("[" + i + "]:" + a);
		}
	}
}
