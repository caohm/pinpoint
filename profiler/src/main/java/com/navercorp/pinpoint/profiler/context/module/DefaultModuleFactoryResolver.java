/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.module;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.exception.PinpointException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Taejin Koo
 */
public class DefaultModuleFactoryResolver implements ModuleFactoryResolver {

    private final Logger logger = LoggerFactory.getLogger(DefaultModuleFactoryResolver.class);

    private final String moduleFactoryClazzName;
    private final boolean defaultModuleFactory;

    public DefaultModuleFactoryResolver() {
        this(ApplicationContextModuleFactory.class.getName());
    }

    public DefaultModuleFactoryResolver(String moduleFactoryClazzName) {
        this.moduleFactoryClazzName = Assert.requireNonNull(moduleFactoryClazzName, "moduleFactoryClazzName must not be null");
        this.defaultModuleFactory = isDefaultModuleFactory(moduleFactoryClazzName);
    }

    private boolean isDefaultModuleFactory(String moduleFactoryClazzName) {
        if (StringUtils.isEmpty(moduleFactoryClazzName)) {
            return true;
        }
        if (ApplicationContextModuleFactory.class.getName().equals(moduleFactoryClazzName)) {
            return true;
        }
        return false;
    }

    @Override
    public ModuleFactory resolve() {
        if (defaultModuleFactory) {
            return new ApplicationContextModuleFactory();
        }
        logger.info("{} ModuleFactory lookup", moduleFactoryClazzName);
        ClassLoader classLoader = getClassLoader(DefaultModuleFactoryResolver.class.getClassLoader());
        try {
            final Class<? extends ModuleFactory> moduleFactoryClass =
                    (Class<? extends ModuleFactory>) Class.forName(moduleFactoryClazzName, true, classLoader);
            Constructor<? extends ModuleFactory> constructor = moduleFactoryClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception ex) {
            logger.warn("{} ModuleFactory initialize fail", moduleFactoryClazzName, ex);
            throw new PinpointException(moduleFactoryClazzName + " ModuleFactory initialize fail", ex);
        }
    }

    private ClassLoader getClassLoader(ClassLoader classLoader) {
        return Assert.requireNonNull(classLoader, "can't find classLoader");
    }

}