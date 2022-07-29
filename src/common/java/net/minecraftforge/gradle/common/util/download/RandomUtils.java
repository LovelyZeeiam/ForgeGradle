package net.minecraftforge.gradle.common.util.download;

import java.util.Random;

public class RandomUtils {

	private static final String CHARS = "qwertyuiopasdfghjklzxcvbnm1234567890";
	private static final Random RANDOM = new Random();

	public static String genRandomStr(int length) {
		StringBuffer buf = new StringBuffer(length);
		RANDOM.ints(length).forEach(i -> buf.append(CHARS.charAt(Math.floorMod(i, CHARS.length()))));
		return buf.toString();
	}

}
