import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiniApplicationContext {
    private Map<String, Object> beans = new HashMap<>();

    public MiniApplicationContext(String xmlPath) {
        loadBeans(xmlPath);
        instantiateBeans();
        populateBeans();
    }

    private void loadBeans(String xmlPath) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(this.getClass().getClassLoader().getResourceAsStream(xmlPath));
            Element root = document.getRootElement();
            List<Element> elements = root.elements();

            for (Element element : elements) {
                String id = element.attributeValue("id");
                String className = element.attributeValue("class");

                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setClassName(className);

                List<Element> propertyElements = element.elements("property");
                for (Element propertyElement : propertyElements) {
                    String name = propertyElement.attributeValue("name");
                    String value = propertyElement.attributeValue("value");

                    beanDefinition.getProperties().put(name, value);
                }

                beans.put(id, beanDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void instantiateBeans() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            try {
                String beanId = entry.getKey();
                BeanDefinition beanDefinition = (BeanDefinition) entry.getValue();

                Class<?> beanClass = Class.forName(beanDefinition.getClassName());
                Object beanInstance = beanClass.getDeclaredConstructor().newInstance();
                beans.put(beanId, beanInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateBeans() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String beanId = entry.getKey();
            BeanDefinition beanDefinition = (BeanDefinition) entry.getValue();
            Object beanInstance = beans.get(beanId);

            try {
                for (Map.Entry<String, Object> property : beanDefinition.getProperties().entrySet()) {
                    Field field = beanInstance.getClass().getDeclaredField(property.getKey());
                    field.setAccessible(true);

                    Object value = property.getValue();
                    if (value instanceof String) {
                        field.set(beanInstance, value);
                    } // Handle other types as needed

                    field.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
