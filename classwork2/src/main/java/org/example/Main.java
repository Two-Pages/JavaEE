import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        // 加载属性文件
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 从属性文件中获取类名
        String className = properties.getProperty("className");

        try {
            // 根据类名创建对象
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.getDeclaredConstructor().newInstance();

            // 获取类的所有方法
            Method[] methods = clazz.getDeclaredMethods();

            // 查找带有@InitMethod注解的方法并调用
            for (Method method : methods) {
                if (method.isAnnotationPresent(InitMethod.class)) {
                    method.setAccessible(true); // 设置私有方法可访问
                    method.invoke(obj); // 调用带有@InitMethod注解的方法
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
