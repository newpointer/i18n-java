package ru.nullpointer.i18n.common;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.util.ULocale;
import org.springframework.util.StringUtils;

/**
 * @author ankostyuk
 * @author Alexander Yastrebov
 *
 */
// TODO Object[] args
// TODO Оптимизация:
//          new ULocale()
//          buildCldrPluralPattern()
//          getLangBundle()
public class JsonBundleMessageSource extends AbstractMessageSource implements InitializingBean {

    private static final String CONTEXT_GLUE = "\u0004";

    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    private ObjectMapper objectMapper;

    private Map<String, Map<String, Object>> bundle;

    private List<String> jsonBundlePaths;

    private String defaultBundle;

    public void setJsonBundlePaths(List<String> jsonBundlePaths) {
        this.jsonBundlePaths = jsonBundlePaths;
    }

    public void setDefaultBundle(String defaultBundle) {
        this.defaultBundle = defaultBundle;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(jsonBundlePaths, "'jsonBundlePaths' must be set");
        init();
    }

    public String tr(String key, Object[] args, Locale locale) {
        return message(key, null, 1, locale, key);
    }

    public String trc(String key, String context, Object[] args, Locale locale) {
        return message(key, context, 1, locale, key);
    }

    public String trn(String key, double number, Object[] args, Locale locale) {
        return plural(key, number, null, args, locale);
    }

    public String trnc(String key, double number, String context, Object[] args, Locale locale) {
        return plural(key, number, context, args, locale);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return null;
    }

    protected String message(String key, String context, int messageIndex, Locale locale, String defaultMessage) {
        List<String> messageList = getMessageList(key, context, locale);

        String message = (messageIndex >= 0 && messageIndex < messageList.size() ? messageList.get(messageIndex) : null);

        if (StringUtils.hasText(message)) {
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

    protected String plural(String key, double number, String context, Object[] args, Locale locale) {
        List<String> pluralForms = messages(key, context, 1, locale);

        if (pluralForms.isEmpty() || pluralForms.contains("") || pluralForms.contains(null)) {
            return key;
        }

        Integer pluralFormCount = getPluralFormCount(locale);
        if (pluralFormCount > pluralForms.size()) {
            for (Integer i = 0; i < pluralFormCount - pluralForms.size(); i++) {
                pluralForms.add(key);
            }
        }

        String pluralPattern = buildCldrPluralPattern(pluralForms, locale);
        PluralFormat pluralFormat = new PluralFormat(new ULocale(locale.getLanguage()), pluralPattern);

        return pluralFormat.format(number);
    }

    @SuppressWarnings("unchecked")
    protected Integer getPluralFormCount(Locale locale) {
        Map<String, Object> langBundle = getLangBundle(locale);

        return (Integer) ((Map<String, Map<String, Object>>) langBundle.get("")).get("pluralForms").get("count");
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

        if (langBundle == null && defaultBundle != null) {
            langBundle = bundle.get(defaultBundle);
        }

        Assert.notNull(langBundle, "Locale is not supported: " + locale);

        return langBundle;
    }

    private String[] getLangKeyVariants(Locale locale) {
        return new String[]{locale.getLanguage(), locale.getCountry()};
    }

    private void init() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        bundle = makeBundle();

        /*
         Assert.notNull(bundle);
         pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

         org.springframework.core.io.Resource[] resourceList = pathMatchingResourcePatternResolver.getResources(jsonBundlePaths.get(0));
         Assert.notEmpty(resourceList, "'jsonBundle' must be exist");

         bundle = objectMapper.readValue(resourceList[0].getInputStream(), Map.class);
         Assert.notNull(bundle);
         */
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> makeBundle() throws Exception {
        Map<String, Map<String, Object>> bundle = new HashMap<String, Map<String, Object>>();

        pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

        for (String jsonBundlePath : jsonBundlePaths) {
            org.springframework.core.io.Resource[] resourceList = pathMatchingResourcePatternResolver.getResources(jsonBundlePath);
            Assert.notEmpty(resourceList, "'jsonBundle' must be exist");
            Map<String, Map<String, Object>> b = objectMapper.readValue(resourceList[0].getInputStream(), Map.class);

            for (String lang : b.keySet()) {
                if (bundle.containsKey(lang)) {
                    bundle.get(lang).putAll(b.get(lang));
                } else {
                    bundle.put(lang, b.get(lang));
                }
            }
        }

        return bundle;
    }
}
