package cn.leo.annotation_lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author : Jarry Leo
 * @date : 2018/9/11 17:49
 */
public class ButterKnife {
    private ButterKnife() {

    }

    private static  void initialization(Object target, String suffix) {
        Class<?> tClass = target.getClass();
        String className = tClass.getName();
        try {
            Class<?> bindingClass = tClass.getClassLoader().loadClass(className + suffix);
            Constructor<?> constructor = bindingClass.getConstructor(tClass);
            constructor.newInstance(target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void bind(Object activity) {
        initialization(activity, "$Binding");
    }
}
