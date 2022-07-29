package net.minecraftforge.gradle.common.util.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadUtils2 {

	public static void main(String[] args) throws IOException {
		String url = "http://launchermeta.mojang.com/mc/game/version_manifest.json";
//		String url = "https://bmclapi2.bangbang93.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";
		File file = new File("./temp/temp.json");

		ExecutorService executor = Executors.newWorkStealingPool();
		DownloadInstance.create(new URL(url), file, null, 65536, executor).run();
		executor.shutdown();

	}

}
