package com.bytegen.common.reload.core;

import com.bytegen.common.reload.ReloadResource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Processes beans on start up collecting property resources configured in a {@link ReloadResource}.
 * </p>
 */
@Component
public class ReloadResourceFactoryProcessor implements BeanFactoryPostProcessor {

    private List<AnnotatedBeanDefinition> candidates;

    public List<AnnotatedBeanDefinition> getCandidates() {
        return candidates;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        candidates = getCandidates(beanFactory);
    }

    private List<AnnotatedBeanDefinition> getCandidates(ConfigurableListableBeanFactory beanFactory) {
        List<AnnotatedBeanDefinition> candidates = new ArrayList<>();

        String[] candidateNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : candidateNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            // String className = bd.getBeanClassName();

            if (bd instanceof AnnotatedBeanDefinition &&
                    /*className.equals(((AnnotatedBeanDefinition) bd).getMetadata().getClassName()) &&*/
                    ((AnnotatedBeanDefinition) bd).getMetadata().isAnnotated(ReloadResource.class.getCanonicalName())) {

                candidates.add((AnnotatedBeanDefinition) bd);
            }
        }
        return candidates;
    }
}
