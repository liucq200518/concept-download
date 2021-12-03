package com.github.linyuzai.download.core.writer;

import com.github.linyuzai.download.core.context.DownloadContext;
import com.github.linyuzai.download.core.order.OrderProvider;
import com.github.linyuzai.download.core.range.Range;
import com.github.linyuzai.download.core.source.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface SourceWriter extends OrderProvider {

    boolean support(Source source, Range range, DownloadContext context);

    void write(InputStream is, OutputStream os, Range range, Charset charset, long length) throws IOException;
}
