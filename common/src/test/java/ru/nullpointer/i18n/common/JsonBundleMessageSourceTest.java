package ru.nullpointer.i18n.common;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.annotation.Resource;
import org.junit.Before;

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
    "classpath:/spring/testContext.xml",})
public class JsonBundleMessageSourceTest {

    private static Logger logger = LoggerFactory.getLogger(JsonBundleMessageSourceTest.class);
    //

    private static Locale RU_LOCALE = new Locale("ru");
    private static Locale EN_LOCALE = Locale.ENGLISH;
    private static Locale DE_LOCALE = Locale.GERMAN;

    @Resource
    private JsonBundleMessageSource messageSource;

    @Before
    public void setUp() {
        messageSource.setDefaultBundle("en");
    }

    @Test
    public void test_bean() {
        logger.debug("messageSource: {}", messageSource);
        assertNotNull(messageSource);
    }

    @Test
    public void test_parentMessageSource() {
        String ruMessage = messageSource.getMessage("system.xxx.1", null, RU_LOCALE);
        assertEquals("сообщение 1", ruMessage);

        String enMessage = messageSource.getMessage("system.xxx.1", null, EN_LOCALE);
        assertEquals("message 1", enMessage);

        String deMessage = messageSource.getMessage("system.xxx.1", null, DE_LOCALE);
        assertEquals("mmm 1", deMessage);
    }

    @Test
    public void test_baseLang() {
        // Базовый язык [ru] - как такового перевода нет :)...
        Locale locale = RU_LOCALE;

        // tr - простой перевод
        assertEquals(messageSource.tr("ключ", null, locale), "ключ");

        // trc - перевод с контекстом
        assertEquals(messageSource.trc("ключ", "к разгадке чего-либо", null, locale), "ключ");
        assertEquals(messageSource.trc("ключ", "ключ воды, приток реки", null, locale), "ключ");

        // trn - простой перевод с формами множественного числа
        assertEquals(messageSource.trn("ключ", 0, null, locale), "ключей");
        assertEquals(messageSource.trn("ключ", 1, null, locale), "ключ");
        assertEquals(messageSource.trn("ключ", 2, null, locale), "ключа");
        assertEquals(messageSource.trn("ключ", 5, null, locale), "ключей");

        // trnc - перевод с контекстом и с формами множественного числа
        assertEquals(messageSource.trnc("ключ", 0, "к разгадке чего-либо", null, locale), "ключей");
        assertEquals(messageSource.trnc("ключ", 1, "к разгадке чего-либо", null, locale), "ключ");
        assertEquals(messageSource.trnc("ключ", 2, "к разгадке чего-либо", null, locale), "ключа");
        assertEquals(messageSource.trnc("ключ", 5, "к разгадке чего-либо", null, locale), "ключей");

        // В бандле только ключ, нет контекста, нет форм множественного числа...
        // tr - простой перевод
        assertEquals(messageSource.tr("корова", null, locale), "корова");
        // trc - нет контекста в бандле
        assertEquals(messageSource.trc("корова", "корова", null, locale), "корова");
        // trn - нет форм множественного числа в бандле
        assertEquals(messageSource.trn("корова", 0, null, locale), "корова");
        assertEquals(messageSource.trn("корова", 1, null, locale), "корова");
        assertEquals(messageSource.trn("корова", 2, null, locale), "корова");
        assertEquals(messageSource.trn("корова", 5, null, locale), "корова");
        // trnc - нет форм множественного числа в бандле, нет контекста в бандле
        assertEquals(messageSource.trnc("корова", 0, "корова", null, locale), "корова");
        assertEquals(messageSource.trnc("корова", 1, "корова", null, locale), "корова");
        assertEquals(messageSource.trnc("корова", 2, "корова", null, locale), "корова");
        assertEquals(messageSource.trnc("корова", 5, "корова", null, locale), "корова");

        // Перевод предложений...
        // tr - простой перевод
        assertEquals(messageSource.tr("Красивый лиловый шар, наполненный водородом.", null, locale), "Красивый лиловый шар, наполненный водородом.");
        // trn - простой перевод с формами множественного числа
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 0, null, locale), "Красивых лиловых шаров, наполненных водородом.");
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 1, null, locale), "Красивый лиловый шар, наполненный водородом.");
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 2, null, locale), "Красивых лиловых шара, наполненных водородом.");

        // Трансляция "системных ключей"
        assertEquals(messageSource.tr("SYSTEM_ERROR", null, locale), "Системная ошибка");
    }

    @Test
    public void test_translatedLang() {
        // Язык перевода [en]...
        Locale locale = EN_LOCALE;

        // tr - простой перевод
        assertEquals(messageSource.tr("ключ", null, locale), "key");

        // trc - перевод с контекстом
        assertEquals(messageSource.trc("ключ", "к разгадке чего-либо", null, locale), "clue");
        assertEquals(messageSource.trc("ключ", "ключ воды, приток реки", null, locale), "feeder");

        // trn - простой перевод с формами множественного числа
        assertEquals(messageSource.trn("ключ", 0, null, locale), "keys");
        assertEquals(messageSource.trn("ключ", 1, null, locale), "key");
        assertEquals(messageSource.trn("ключ", 2, null, locale), "keys");
        assertEquals(messageSource.trn("ключ", 5, null, locale), "keys");

        // trnc - перевод с контекстом и с формами множественного числа
        assertEquals(messageSource.trnc("ключ", 0, "к разгадке чего-либо", null, locale), "clues");
        assertEquals(messageSource.trnc("ключ", 1, "к разгадке чего-либо", null, locale), "clue");
        assertEquals(messageSource.trnc("ключ", 2, "к разгадке чего-либо", null, locale), "clues");
        assertEquals(messageSource.trnc("ключ", 5, "к разгадке чего-либо", null, locale), "clues");

        assertEquals(messageSource.trnc("ключ", 0, "ключ воды, приток реки", null, locale), "feeders");
        assertEquals(messageSource.trnc("ключ", 1, "ключ воды, приток реки", null, locale), "feeder");
        assertEquals(messageSource.trnc("ключ", 2, "ключ воды, приток реки", null, locale), "feeders");
        assertEquals(messageSource.trnc("ключ", 5, "ключ воды, приток реки", null, locale), "feeders");

        // В бандле только ключ, нет контекста, нет форм множественного числа...
        // tr - простой перевод
        assertEquals(messageSource.tr("корова", null, locale), "cow");
        // trc - нет контекста в бандле
        assertEquals(messageSource.trc("корова", "корова", null, locale), "корова");
        // trn - нет форм множественного числа в бандле
        assertEquals(messageSource.trn("корова", 0, null, locale), "корова");
        assertEquals(messageSource.trn("корова", 1, null, locale), "cow");
        assertEquals(messageSource.trn("корова", 2, null, locale), "корова");
        // trnc - нет форм множественного числа в бандле, нет контекста в бандле
        assertEquals(messageSource.trnc("корова", 0, "корова", null, locale), "корова");
        assertEquals(messageSource.trnc("корова", 1, "корова", null, locale), "корова");
        assertEquals(messageSource.trnc("корова", 2, "корова", null, locale), "корова");

        // Перевод предложений...
        // tr - простой перевод
        assertEquals(messageSource.tr("Красивый лиловый шар, наполненный водородом.", null, locale), "Beautiful purple balloon filled with hydrogen.");
        // trn - простой перевод с формами множественного числа
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 0, null, locale), "Beautiful purple balloons filled with hydrogen.");
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 1, null, locale), "Beautiful purple balloon filled with hydrogen.");
        assertEquals(messageSource.trn("Красивый лиловый шар, наполненный водородом.", 2, null, locale), "Beautiful purple balloons filled with hydrogen.");

        // Трансляция "системных ключей"
        assertEquals(messageSource.tr("SYSTEM_ERROR", null, locale), "System error");
    }

    @Test
    public void testDefaultBundle() {
        assertEquals(messageSource.tr("ключ", null, Locale.FRENCH), "key");
    }
}
