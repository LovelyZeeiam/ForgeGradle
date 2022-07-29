package net.minecraftforge.gradle.common.util.download;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

abstract class DownloadInstance extends DeterminedListWorker<RangeConnection> {

	protected final URL url;
	protected final File output;
	protected final Map<String, String> headers;

	protected long fileSize = 0;

	DownloadInstance(URL url, File output, @Nullable Map<String, String> headers, ExecutorService executor) {
		super(executor);
		this.url = url;
		this.output = output;
		this.headers = headers;
	}

	/**
	 * submit the initial connection to avoid spending time on connection
	 */
	protected void init(URLConnection initialConnection) {
	}

	protected void submitDownloadTask(Range range) {
		try {
			this.submit(RangeConnection.makeConnection(range.getFrom(), range.getTo(), url, headers));
		} catch (IOException e) {
			this.processDownloadFail(range, null, 0, e);
		}
	}

	protected String genRandomString() {
		return "." + RandomUtils.genRandomStr(20);
	}

	@Override
	protected void runTask(RangeConnection rangeCon) throws Exception {
//		long downloadSize = rangeCon.getTo() - rangeCon.getFrom() + 1;
		long byteCount = 0;

		InputStream in = null;
		OutputStream out = null;

		String randomStr = genRandomString();
		File file = new File(output.getAbsolutePath() + (randomStr == null ? "" : randomStr));

		byte[] ioBuffer = new byte[4096];

		try {
			in = rangeCon.getConnection().getInputStream();
			out = new BufferedOutputStream(FileUtils.openOutputStream(file));

			int read = 0;
			while (read != -1) {
				read = in.read(ioBuffer);
				if (read > 0) {
					out.write(ioBuffer, 0, read);
					byteCount += read;
				}
//				System.out.println(read + ", " + byteCount + ", " + downloadSize);
			}

			this.processDownloadSuccess(rangeCon, file);

		} catch (IOException e) {
			this.processDownloadFail(rangeCon, file, byteCount, e);
		} finally {
			if (in != null)
				in.close();
			if (out != null) {
				out.flush();
				out.close();
			}
		}

	}

	protected abstract void processDownloadFail(Range range, File file, long downloadedSize, Exception e);

	protected abstract void processDownloadSuccess(Range range, File file);

	public static DownloadInstance create(URL url, File output, Map<String, String> headers, ExecutorService executor) throws IOException {
		return create(url, output, headers, 16384, executor);
	}

	public static DownloadInstance create(URL url, File output, Map<String, String> headers, long chunkSize, ExecutorService executor) throws IOException {
		String proto = url.getProtocol().toLowerCase();
		URLConnection con = createConnection(url, headers);

		boolean supportRange = false;
		if ("http".equals(proto) || "https".equals(proto)) {
			// Open the connection to check whether Range is supported
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestProperty("Range", "bytes=1-");
			int responseCode = httpCon.getResponseCode();

			switch (responseCode) {
				case HttpURLConnection.HTTP_OK:
					break;
				case HttpURLConnection.HTTP_PARTIAL:
					supportRange = true;
					break;
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_SEE_OTHER:
					String newLocation = httpCon.getHeaderField("Location");
					URL newUrl = new URL(newLocation);
					return create(newUrl, output, headers, chunkSize, executor);
				default:
					throw new IOException(String.format("Get %d when fetch url: %s%n", responseCode, url));
			}
		}
		long fileSize = con.getContentLengthLong();

		DownloadInstance instance;
		if (fileSize > 0) {
			instance = supportRange ?
					(chunkSize > 0 ? new MultiThreadedDownloadInstance(url, output, headers, chunkSize, executor) : new RangeDownloadInstance(url, output, headers, executor))
					: new SingleThreadDownloadInstance(url, output, headers, executor);
		} else {
			instance = new SingleThreadDownloadInstance(url, output, headers, executor);
		}

		instance.fileSize = fileSize;
		instance.init(con);

		return instance;
	}

	public static URLConnection createConnection(URL url, Map<String, String> headers) throws IOException {
		URLConnection con = url.openConnection();
		con.setConnectTimeout(10000);
		con.setReadTimeout(5000);

		if (con instanceof HttpURLConnection && headers != null) {
			headers.forEach(con::setRequestProperty);
		}

		return con;
	}

}
