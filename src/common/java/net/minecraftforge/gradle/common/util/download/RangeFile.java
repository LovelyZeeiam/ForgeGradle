package net.minecraftforge.gradle.common.util.download;

import java.io.File;

class RangeFile extends Range {

	private final File file;

	public RangeFile(long start, long to, File file) {
		super(start, to);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
