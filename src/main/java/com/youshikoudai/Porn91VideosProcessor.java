package com.youshikoudai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youshikoudai.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.assertj.core.util.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Slf4j
public class Porn91VideosProcessor implements PageProcessor {

    private Site site = Site.me()
            .setDomain("91porn.com")
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    private Video video;

    private List<Video> videos = Lists.newArrayList();

    private static final int PROXY_PORT = 2334;

    private static final String PARSE_VIDEO_URL = "https://pv.vlogdownloader.com/api.php?callback=jQuery112407641688669088684_1564803563821";

    @Override
    public void process(Page page) {
        Document document = Jsoup.parse(page.getHtml().css("#tab-featured").toString());
        document.getElementsByTag("p").forEach(e -> {
            video = new Video();
            video.setTitle(e.getElementsByClass("title").text());
            video.setUrl(e.getElementsByTag("a").get(0).attr("href"));
            videos.add(video);
        });
        videos.parallelStream().forEach(Porn91VideosProcessor::download);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", PROXY_PORT)));
        Spider.create(new Porn91VideosProcessor())
                .addUrl("http://91porn.com/index.php")
                .setDownloader(httpClientDownloader)
                .thread(1)
                .run();
    }

    private static void download(Video video) {
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
}
