package c.y.mig.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static Object getBean(HttpServletRequest request, Class<?> beanClass) throws Exception {
        Map<String, Object> properties = getMap(request);
        Object bean = beanClass.newInstance();
        BeanUtils.populate(bean, properties);
        return bean;
    }

    public static Object getBean(HttpServletRequest request, Class<?> beanClass, String prefix) throws Exception {
        Object bean = beanClass.newInstance();
        BeanUtils.populate(bean, getMap(prefix, request));
        return bean;
    }

    public static Map<String, Object> getMap(HttpServletRequest request) throws Exception {

        Map<String, Object> param = new HashMap<String, Object>();
        Enumeration<?> enums = request.getParameterNames();

        while (enums.hasMoreElements()) {
            String name = (String) enums.nextElement();
            String[] values = request.getParameterValues(name);

            if (values != null) {
                if (values.length == 1)
                    param.put(name, values[0]);
                else if (values.length > 1)
                    param.put(name, values);
                else if (values.length < 1)
                    param.put(name, "");
            } else
                param.put(name, "");
        }

        return param;
    }

    public static Map<String, Object> getMap(String prefix, HttpServletRequest request) throws Exception {

        Map<String, Object> param = new HashMap<String, Object>();
        Enumeration<?> enums = request.getParameterNames();

        while (enums.hasMoreElements()) {
            String name = (String) enums.nextElement();
            if (!name.startsWith(prefix + ".")) {
                continue;
            }

            String[] values = request.getParameterValues(name);
            name = name.substring(prefix.length() + 1);

            if (values == null) {
                param.put(name, null);
            } else if (values.length == 0) {
                param.put(name, "");
            } else if (values.length == 1) {
                param.put(name, values[0]);
            } else {
                param.put(name, values);
            }
        }

        return param;
    }

    // Legacy String getters
    public static String getStringParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value == null) ? "" : value.trim();
    }

    public static int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String valueStr = getStringParameter(request, name);
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
}
