package net.minecraftforge.gradle.common.util.download;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class MultiThreadedDownloadInstance extends RangeDownloadInstance {

	private final long chunKSize;

	MultiThreadedDownloadInstance(URL url, File output, @Nullable Map<String, String> headers, long chunkSize, ExecutorService executor) {
		super(url, output, headers, executor);
		this.chunKSize = chunkSize;
	}

	@Override
	protected void init(URLConnection initialConnection) {
		super.init(initialConnection);

		long pointer = chunKSize, pointerEnd = chunKSize * 2 - 1;
		while (pointer <= fileSize) {
			pointerEnd = Math.min(pointerEnd, this.fileSize);
			this.submitDownloadTask(new Range(pointer, pointerEnd));

			pointer += chunKSize;
			pointerEnd += chunKSize;
		}

	}

}
