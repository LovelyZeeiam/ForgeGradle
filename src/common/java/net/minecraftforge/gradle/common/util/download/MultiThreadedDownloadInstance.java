package net.minecraftforge.gradle.common.util.download;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class MultiThreadedDownloadInstance extends RangeDownloadInstance {

	private final long chunkSize;

	MultiThreadedDownloadInstance(URL url, File output, @Nullable Map<String, String> headers, long chunkSize, ExecutorService executor) {
		super(url, output, headers, executor);
		this.chunkSize = chunkSize;
	}

	@Override
	protected void init(URLConnection initialConnection) {
		long pointer = chunkSize, pointerEnd = chunkSize * 2 - 1;
		this.submit(new RangeConnection(0, pointer - 1, initialConnection));

		while (pointer <= fileSize) {
			pointerEnd = Math.min(pointerEnd, this.fileSize);
			this.submitDownloadTask(new Range(pointer, pointerEnd));

			pointer += chunkSize;
			pointerEnd += chunkSize;
		}

	}

}
