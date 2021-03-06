/*
 * MIT License
 *
 * Copyright (c) 2018 Bartłomiej Stefański
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package pl.bmstefanski.xanax.core.api.bean.impl;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import pl.bmstefanski.xanax.core.api.bean.CommandBean;
import pl.bmstefanski.xanax.core.api.bean.ListenerBean;
import pl.bmstefanski.xanax.core.api.bean.UtilityBean;
import pl.bmstefanski.xanax.core.api.module.Module;

public final class BeanContainerInitializer {

  private static final Map<String, Object> BEAN_INSTANCES = new LinkedHashMap<>();

  public static void initialize(String packageName, Module module) {
    Reflections reflections = new Reflections(packageName, new TypeAnnotationsScanner(),
        new SubTypesScanner());

    Set<Class<?>> commandBeans = reflections.getTypesAnnotatedWith(CommandBean.class);
    Set<Class<?>> listenerBeans = reflections.getTypesAnnotatedWith(ListenerBean.class);
    Set<Class<?>> utilityBeans = reflections.getTypesAnnotatedWith(UtilityBean.class);

    List<Set<Class<?>>> beans = Arrays
        .asList(commandBeans, listenerBeans, utilityBeans);

    beans.forEach(classes -> classes.forEach(aClass -> {
      try {

        if (aClass.getConstructors()[0].getParameterCount() == 1) {
          BEAN_INSTANCES.putIfAbsent(aClass.getName(),
              aClass.getDeclaredConstructor(module.getClass()).newInstance(module));
          return;
        }

        BEAN_INSTANCES.putIfAbsent(aClass.getName(), aClass.newInstance());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        e.printStackTrace();
      }
    }));
  }

  public static ImmutableMap<String, Object> getBeansInstances() {
    return ImmutableMap.copyOf(BEAN_INSTANCES);
  }

  private BeanContainerInitializer() {
  }

}
