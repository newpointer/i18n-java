package ru.nullpointer.i18n.common;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author ankostyuk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/spring/testContext.xml",
})
public class JsonBundleMessageSourceTest {

    private static Logger logger = LoggerFactory.getLogger(JsonBundleMessageSourceTest.class);
    //

    private static Locale RU_LOCALE = new Locale("ru");
    private static Locale US_LOCALE = Locale.US;
    private static Locale DE_LOCALE = Locale.GERMAN;
    private static Locale[] LOCALES = {RU_LOCALE, US_LOCALE, DE_LOCALE};

    @Resource
    private JsonBundleMessageSource messageSource;

    @Test
    public void test_bean() {
        logger.debug("messageSource: {}", messageSource);
        assertNotNull(messageSource);
    }

    @Test
    public void test_parentMessageSource() {
        String ruMessage = messageSource.getMessage("system.xxx.1", null, RU_LOCALE);
        assertEquals("сообщение 1", ruMessage);

        String enMessage = messageSource.getMessage("system.xxx.1", null, US_LOCALE);
        assertEquals("message 1", enMessage);

        String deMessage = messageSource.getMessage("system.xxx.1", null, DE_LOCALE);
        assertEquals("mmm 1", deMessage);
    }

    @Test
    public void test_tr() {
        String k;
        String c;
        String m;

        for (Locale locale : LOCALES) {
            logger.debug("Локаль: {}...", locale);

            k = "нет такого ключа";
            m = messageSource.tr(k, null, locale);
            logger.debug("{} -> {}", k, m);

            k = "";
            m = messageSource.tr(k, null, locale);
            logger.debug("{} -> {}", k, m);

            k = "Ключ 1-1";
            m = messageSource.tr(k, null, locale);
            logger.debug("{} -> {}", k, m);

            k = "Ключ 1-1-1";
            m = messageSource.tr(k, null, locale);
            logger.debug("{} -> {}", k, m);

            k = "Ключ \"XXX\"";
            c = "Нет такого контекста";
            m = messageSource.trc(k, c, null, locale);
            logger.debug("{}, {} -> {}", new Object[]{k, c, m});

            k = "Ключ \"ZZZ\"";
            c = "Контекст \"ZZZ\"";
            m = messageSource.trc(k, c, null, locale);
            logger.debug("{}, {} -> {}", new Object[]{k, c, m});

            for (int i = 0; i <= 22; i++) {
                k = "Ключ 1-1-1/Ключа 1-1-1/Ключей 1-1-1";
                m = messageSource.trn(k, i, null, locale);
                logger.debug("{} {}", i, m);
            }

            k = "xxx/rrr/vvv";
            m = messageSource.trn(k, 0, null, locale);
            logger.debug("{} -> {}", k, m);

            for (int i = 0; i <= 22; i++) {
                k = "Ключ \"ZZZ\"/Ключа\"ZZZ\"/Ключей \"ZZZ\"";
                c = "Контекст \"ZZZ\"";
                m = messageSource.trnc(k, i, c, null, locale);
                logger.debug("{} {} {}", new Object[]{i, m, c});
            }

            k = "xxx/rrr/vvv";
            c = "Контекст \"ZZZ\"";
            m = messageSource.trnc(k, 0, c, null, locale);
            logger.debug("{} -> {} {}", new Object[]{k, m, c});
        }
    }
}
