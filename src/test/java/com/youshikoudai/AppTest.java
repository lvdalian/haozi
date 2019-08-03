package com.youshikoudai;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class AppTest {
    @Test
    public void m1() throws IOException {
        Files.lines(Paths.get("G:/test")).parallel()
                .forEach(e -> {
                    try {
                        InputStream inputStream = Request.Get(e)
                                .viaProxy(new HttpHost("127.0.0.1", 2334))
                                .execute()
                                .returnContent()
                                .asStream();
                        FileUtils.copyToFile(inputStream, new File(e.substring(e.lastIndexOf("=") + 1)));
                        log.info("successfully download video [{}]", e.substring(e.lastIndexOf("=") + 1));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
    }

    @Test
    public void m2() {
        String string = "wohao\\/fjalk";
        System.out.println(string.replaceAll("\\\\", ""));
    }
}
