package com.youshikoudai.processor;

import com.youshikoudai.common.CommonUtils;
import com.youshikoudai.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.ArrayList;
import java.util.List;

public class TopicProcessor implements PageProcessor {

    private Video video;

    private List<Video> videos = new ArrayList<>(20);

    private Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(3000)
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    private static final String TARGET_URL = "http://91porn.com/v.php?category=md&viewtype=basic";

    @Override
    public void process(Page page) {
        Document document = Jsoup.parse(page.getHtml().css("#videobox").toString());
        document.getElementsByClass("listchannel").forEach(e -> {
            video = new Video();
            Attributes attributes = e.getElementsByTag("a").get(1).attributes();
            video.setTitle(attributes.get("title"));
            video.setUrl(attributes.get("href"));
            videos.add(video);
        });
        videos.parallelStream().forEach(CommonUtils::videoDownload);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", CommonUtils.PROXY_PORT)));
        Spider.create(new TopicProcessor())
                .setDownloader(httpClientDownloader)
                .addUrl(TARGET_URL)
                .run();
    }
}
