package com.youshikoudai.processor;

import com.youshikoudai.common.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageProcessor implements PageProcessor {

    private Site site = Site.me().addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    private static final String PREFIX = "https://";

    @Override
    public void process(Page page) {
        List<String> urls = new ArrayList<>(20);

        page.addTargetRequest(page.getHtml().css(".next").links().toString());
        Document document = Jsoup.parse(page.getHtml().css(".datatable").toString());
        document.getElementsByTag("tbody").forEach(e -> {
            Elements a = e.getElementsByTag("a");
            if (a.size() > 0) {
                String url =  PREFIX + site.getDomain() + "/" + e.getElementsByTag("a").get(0).attr("href");
                urls.add(url);
            }
        });
        urls.parallelStream().forEach(CommonUtils::imageDownload);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", CommonUtils.PROXY_PORT)));
        Spider.create(new ImageProcessor())
                .setDownloader(httpClientDownloader)
                .addUrl("https://f.wonderfulday30.live/forumdisplay.php?fid=19&filter=digest")
                .run();
    }
}

