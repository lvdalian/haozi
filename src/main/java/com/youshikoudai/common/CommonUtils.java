package com.youshikoudai.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.youshikoudai.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CommonUtils {

    /**
     * 代理端口
     */
    public static final int PROXY_PORT = 2334;

    public static final String LOCAL_ADDRESS = "127.0.0.1";

    /**
     * 解析地址
     */
    private static final String PARSE_VIDEO_URL = "https://pv.vlogdownloader.com/api.php?callback=jQuery112407641688669088684_1564803563821";

    /**
     * 下载
     *
     * @param video
     */
    public static void videoDownload(Video video) {
        try {
            String result = Request.Post(PARSE_VIDEO_URL)
                    .viaProxy(new HttpHost("127.0.0.1", PROXY_PORT))
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36")
//                    .addHeader("cookie", "__cfduid=d5253031a0964e8d7621afcd28d85b6161564803203; _ga=GA1.2.179766073.1564803212; _gid=GA1.2.1428371002.1564803212; PHPSESSID=ec3999ff1763d5266d7bd59e88e7e7a9; _gat=1; __atuvc=2%7C31; __atuvs=5d45008bb4476c80001")
                    .bodyForm(Form.form()
                            .add("url", video.getUrl())
                            .add("hash", "37bef9052efa3b7a19531379ed2f1741")
                            .build())
                    .execute()
                    .returnContent()
                    .asString();
            JSONObject object = (JSONObject) JSON.parse(result.substring(result.indexOf("(") + 1, result.lastIndexOf(")")));
            if ("ok".equals(object.get("status"))) {
                JSONArray jsonArray = (JSONArray) object.get("video");
                JSONObject videoJ = (JSONObject) jsonArray.get(0);
                String url = videoJ.get("url").toString().replaceAll("\\\\", "");
                System.out.println(url);
                InputStream inputStream = Request.Get(url)
                        .viaProxy(new HttpHost("127.0.0.1", PROXY_PORT))
                        .execute()
                        .returnContent().asStream();
                try (inputStream) {
                    FileUtils.copyToFile(inputStream, new File("G:/porn/" + video.getTitle() + ".mp4"));
                    log.info("successfully download the video [{}]", video.getTitle() + "mp4");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void imageDownload(String url) {
        try {
            String html = Request.Get(url)
                    .viaProxy(new HttpHost(CommonUtils.LOCAL_ADDRESS, CommonUtils.PROXY_PORT))
                    .execute()
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);
            Document document = Jsoup.parse(html);
            Elements element = document.getElementsByClass("t_msgfont");
            element.forEach(e -> {
                String src = e.getElementsByTag("img").attr("file");
                if (!Strings.isNullOrEmpty(src)) {
                    try {
                        File file = new File("G:/porn/image/" + document.title() + src.substring(src
                                .lastIndexOf("//") + 20));
                        FileUtils.copyURLToFile(new URL(src), file);
                        log.info(file.getName());
                    } catch (IOException e1) {
                        log.error("url解析错误[{}]", e1);
                    }
                }
            });
        } catch (IOException e) {
            log.error("", e);
        }
    }

}
