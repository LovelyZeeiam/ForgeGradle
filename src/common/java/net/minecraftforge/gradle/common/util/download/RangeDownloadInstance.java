package net.minecraftforge.gradle.common.util.download;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

class RangeDownloadInstance extends DownloadInstance {

	protected CopyOnWriteArrayList<RangeFile> fileList = Lists.newCopyOnWriteArrayList();

	RangeDownloadInstance(URL url, File output, @Nullable Map<String, String> headers, ExecutorService executor) {
		super(url, output, headers, executor);
	}

	@Override
	protected void init(URLConnection initialConnection) {
		this.submit(new RangeConnection(0, fileSize - 1, initialConnection));
	}

	@Override
	protected void processDownloadFail(Range range, File file, long downloadedSize, Exception e) {
		long downloadEndPointer = range.getFrom() + downloadedSize - 1;
		this.submitDownloadTask(new Range(downloadEndPointer + 1, range.getTo()));
		if (downloadedSize > 0) {
			fileList.add(new RangeFile(range.getFrom(), downloadEndPointer, file));
		} else {
			file.delete();
		}
	}

	@Override
	protected void processDownloadSuccess(Range range, File file) {
		fileList.add(new RangeFile(range.getFrom(), range.getTo(), file));
	}

	@Override
	protected boolean runTheWorker() {
		if (!super.runTheWorker())
			return false;

		try {
			return mergeTheFiles();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		return true;
	}

	private boolean mergeTheFiles() throws IOException {
		byte[] ioBuffer = new byte[4096];
		RandomAccessFile randomAccessFile = new RandomAccessFile(output, "rw");
		randomAccessFile.setLength(fileSize);
		for (RangeFile rangeFile : fileList) {
			randomAccessFile.seek(rangeFile.getFrom());
			BufferedInputStream in = new BufferedInputStream(FileUtils.openInputStream(rangeFile.getFile()));
			while (in.available() > 0) {
				int readBytes = in.read(ioBuffer);
				randomAccessFile.write(ioBuffer, 0, readBytes);
//				System.out.println(readBytes);
			}
			in.close();
			rangeFile.getFile().deleteOnExit();
		}
		randomAccessFile.close();
		return true;
	}

}
