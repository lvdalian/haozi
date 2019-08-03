package com.youshikoudai;

import com.youshikoudai.common.CommonUtils;
import com.youshikoudai.entity.Video;
import lombok.extern.slf4j.Slf4j;
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


    @Override
    public void process(Page page) {
        Document document = Jsoup.parse(page.getHtml().css("#tab-featured").toString());
        document.getElementsByTag("p").forEach(e -> {
            video = new Video();
            video.setTitle(e.getElementsByClass("title").text());
            video.setUrl(e.getElementsByTag("a").get(0).attr("href"));
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
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", PROXY_PORT)));
        Spider.create(new Porn91VideosProcessor())
                .addUrl("http://91porn.com/index.php")
                .setDownloader(httpClientDownloader)
                .thread(1)
                .run();
    }

}
