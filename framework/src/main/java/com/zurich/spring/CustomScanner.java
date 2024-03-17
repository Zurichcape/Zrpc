package com.zurich.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 18:58
 * @description 自定义包扫描器
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annotationType){
        super(registry);
        super.addExcludeFilter(new AnnotationTypeFilter(annotationType));
    }
    @Override
    public int scan(String... basePackages){return super.scan(basePackages);}
}
