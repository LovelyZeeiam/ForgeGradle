package net.minecraftforge.gradle.common.util.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadUtils2 {

	public static void main(String[] args) throws IOException {
//		String url = "http://launchermeta.mojang.com/mc/game/version_manifest.json";
//		String url = "https://bmclapi2.bangbang93.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";
//		String url = "https://launcher.mojang.com/v1/objects/749f128f21b7c5efcc2cc14649541c0f207cfe4e/windows_server.exe";
		String url = "https://launcher.mojang.com/v1/objects/e5838277b3bb193e58408713f1fc6e005c5f3c0c/client.jar";
		File file = new File("./temp/temp.jar");

		ExecutorService executor = Executors.newWorkStealingPool();
		DownloadInstance.create(new URL(url), file, null, 65536, executor).run();
		executor.shutdown();

	}

}
