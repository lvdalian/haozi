package com.youshikoudai;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
public class NewPorn91ImagesProcessor implements PageProcessor {

    private Site site = Site.me().setSleepTime(3000).setRetryTimes(3);

    @Override
    public void process(Page page) {
        // https://f.wonderfulday30.live/forumdisplay.php?fid=19&filter=digest&page=2

        page.addTargetRequest(page.getHtml().css(".next").links().toString());

        page.addTargetRequests(page.getHtml().css(".new span").links().all());

        List<String> list = page.getHtml().xpath("//td[@class='t_msgfont']/img/@file").all();

        list.removeIf(Objects::nonNull);

        list.forEach(NewPorn91ImagesProcessor::download);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new NewPorn91ImagesProcessor())
                .addUrl("https://f.wonderfulday30.live/forumdisplay.php?fid=19&filter=digest")
                .thread(5)
                .run();
    }

    private static void download(String url) {
        try {
            FileUtils.copyURLToFile(new URL(url), new File(LocalDate.now().toString() + "/" + url.substring(url.lastIndexOf("/") + 1)));
        } catch (IOException e) {
            log.error("{}--下载失败!{}", url, e);
        }

    }
}
