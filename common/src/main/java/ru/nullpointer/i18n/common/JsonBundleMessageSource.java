/**
 *
 */
package ru.nullpointer.i18n.common;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.util.ULocale;

/**
 * @author ankostyuk
 *
 */
// TODO Object[] args
// TODO cash
public class JsonBundleMessageSource extends AbstractMessageSource implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(JsonBundleMessageSource.class);
    //

    private static final String CONTEXT_GLUE = "\u0004";
    private static final String PLURAL_FORM_SEPARATOR = "/";

    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    private ObjectMapper objectMapper;

    private Map<String, Map<String, Object>> bundle;

    private String jsonBundlePath;

    public void setJsonBundlePath(String jsonBundlePath) {
        this.jsonBundlePath = jsonBundlePath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(jsonBundlePath, "'jsonBundlePath' must be set");
        init();
    }

    public String tr(String key, Object[] args, Locale locale) {
        return message(key, null, 1, locale, key);
    }

    public String trc(String key, String context, Object[] args, Locale locale) {
        return message(key, context, 1, locale, key);
    }

    public String trn(String pluralKey, double number, Object[] args, Locale locale) {
        return plural(pluralKey, number, null, args, locale);
    }

    public String trnc(String pluralKey, double number, String context, Object[] args, Locale locale) {
        return plural(pluralKey, number, context, args, locale);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return null;
    }

    protected String message(String key, String context, int messageIndex, Locale locale, String defaultMessage) {
        List<String> messageList = getMessageList(key, context, locale);

        String message = (messageIndex >= 0 && messageIndex < messageList.size() ? messageList.get(messageIndex) : null);

        if (!StringUtils.isBlank(message)) {
            return message;
        }

        return defaultMessage;
    }

    protected List<String> messages(String key, String context, int beginIndex, Locale locale) {
        List<String> messageList = getMessageList(key, context, locale);

        if (beginIndex < messageList.size()) {
            return messageList.subList(beginIndex, messageList.size());
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    protected List<String> getMessageList(String key, String context, Locale locale) {
        Map<String, Object> langBundle = getLangBundle(locale);

        Object messages = langBundle.get(context != null ? (context + CONTEXT_GLUE + key) : key);

        if (messages != null && messages instanceof List<?>) {
            return (List<String>) messages;
        }

        return Collections.emptyList();
    }

    protected String plural(String pluralKey, double number, String context, Object[] args, Locale locale) {
        List<String> pluralForms = messages(getMessageKeyByPluralKey(pluralKey), context, 1, locale);

        if (pluralForms.isEmpty()) {
            return pluralKey;
        }

        String pluralPattern = buildCldrPluralPattern(pluralForms, locale);
        PluralFormat pluralFormat = new PluralFormat(new ULocale(locale.getLanguage()), pluralPattern);

        return pluralFormat.format(number);
    }

    protected String getMessageKeyByPluralKey(String pluralKey) {
        return pluralKey.split(PLURAL_FORM_SEPARATOR)[0];
    }

    protected String buildCldrPluralPattern(List<String> pluralForms, Locale locale) {
        Map<String, Object> langBundle = getLangBundle(locale);

        @SuppressWarnings("unchecked")
        String cldrFormat = (String) ((Map<String, Map<String, Object>>) langBundle.get("")).get("pluralForms").get("cldrFormat");

        String[] args = new String[pluralForms.size()];
        for (int i = 0; i < pluralForms.size(); i++) {
            args[i] = "{" + pluralForms.get(i) + "}";
        }

        String pluralPattern = new MessageFormat(cldrFormat).format(args);

        return pluralPattern;
    }

    private Map<String, Object> getLangBundle(Locale locale) {
        Map<String, Object> langBundle = null;

        for (String langKey : getLangKeyVariants(locale)) {
            if (bundle.containsKey(langKey)) {
                langBundle = bundle.get(langKey);
                break;
            }
        }

        Assert.notNull(langBundle, "Locale is not supported: " + locale);

        return langBundle;
    }

    private String[] getLangKeyVariants(Locale locale) {
        return new String[]{locale.getLanguage(), locale.getCountry()};
    }

    @SuppressWarnings("unchecked")
    private void init() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

        org.springframework.core.io.Resource[] resourceList = pathMatchingResourcePatternResolver.getResources(jsonBundlePath);
        Assert.notEmpty(resourceList, "'jsonBundle' must be exist");

        bundle = objectMapper.readValue(resourceList[0].getInputStream(), Map.class);
        Assert.notNull(bundle);
    }
}