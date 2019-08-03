package com.youshikoudai;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class Porn91VideosProcessor implements PageProcessor {

    private Site site = Site.me()
            .setDomain("91porn.com")
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

    @Override
    public void process(Page page) {
//        page.putField("page", page.getHtml());
        List<String> titles = page.getHtml().xpath("//span[@class=title]/text()").all();
        List<String> urls = page.getHtml().xpath("//div[@id=tab-featured]/p/a[1]/@href").all();
        int i = 0;
        for (String url : urls) {
            Porn91VideosProcessor.download(url, titles.get(i));
            i++;
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", 2334)));
        Spider.create(new Porn91VideosProcessor())
                .addUrl("http://91porn.com/index.php")
                .setDownloader(httpClientDownloader)
                .thread(1)
                .run();

    }

    private static void download(String videoUrl, String name) {
        try {
            String url = "https://anothervps.com/api/video/?cached&lang=ch&page=91porn&hash=343bb537ea16fb3728da1acd3e8518a2&video=" + URLEncoder
                    .encode(videoUrl, StandardCharsets.UTF_8);
            String result = Request.Get(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
                    .viaProxy(new HttpHost("127.0.0.1", 2334))
                    .execute()
                    .returnContent()
                    .asString();
            JSONObject jsonO = (JSONObject) JSONObject.parse(result);
            String realUrl = jsonO.get("url").toString();
            InputStream inputStream = Request.Get(realUrl)
                    .viaProxy(new HttpHost("127.0.0.1", 2334))
                    .execute()
                    .returnContent()
                    .asStream();
            String fileName = String.format("bestVideos628/%s.mp4", name);
            FileUtils.copyToFile(inputStream, new File(fileName));
            log.info("success---{}", fileName);
        } catch (Exception e) {
            log.info("下载错误---{}", e);
        }

    }
}
