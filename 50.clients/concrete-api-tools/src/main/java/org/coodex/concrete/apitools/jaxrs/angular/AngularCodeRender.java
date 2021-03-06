/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.apitools.jaxrs.angular;

import org.coodex.concrete.apitools.AbstractAngularRender;
import org.coodex.concrete.apitools.jaxrs.JaxrsRenderHelper;
import org.coodex.concrete.apitools.jaxrs.angular.meta.TSClass;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-10.
 */
public class AngularCodeRender extends AbstractAngularRender<Unit> {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.angular.ts.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/angular/code/v1/";


    @Override
    public void writeTo(String... packages) throws IOException {
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? null : moduleName.substring(1);
        List<Module> jaxrsModules = ConcreteHelper.loadModules(RENDER_NAME, packages);
        String contextPath = Common.isBlank(moduleName) ? "@concrete/" : (getModuleName(moduleName) + "/");

        // 按包归类
        CLASSES.set(new HashMap<String, Map<Class, TSClass>>());
        try {
            for (Module module : jaxrsModules) {
                process(moduleName, module);
            }

            // AbstractConcreteService.ts
            if (!exists(contextPath + "AbstractConcreteService.ts")) {
                Map<String, Object> versionAndStyle = new HashMap<String, Object>();
                versionAndStyle.put("version", ConcreteHelper.VERSION);
                versionAndStyle.put("style", JaxRSHelper.used024Behavior());

                writeTo(contextPath + "AbstractConcreteService.ts",
                        "abstractConcreteService.ftl",
                        versionAndStyle);
//                copyTo("abstractConcreteService.ftl",
//                        contextPath + "AbstractConcreteService.ts");
            }

            // packages
            packages(contextPath);

        } finally {
            CLASSES.remove();
        }
    }


    @Override
    protected String getModuleType() {
        return "JaxRS";
    }

    @Override
    protected String getMethodPath(AbstractModule<Unit> module, Unit unit) {
        return JaxrsRenderHelper.getMethodPath(module, unit);
    }

    @Override
    protected String getBody(Unit unit) {
        return JaxrsRenderHelper.getBody(unit);
//        Param[] pojoParams = unit.getPojo();
//        switch (unit.getPojoCount()) {
//            case 1:
//                return pojoParams[0].getName();
//            case 0:
//                return null;
//            default:
//                StringBuilder builder = new StringBuilder("{ ");
//                for (int i = 0; i < pojoParams.length; i++) {
//                    if (i > 0) builder.append(", ");
//                    builder.append(pojoParams[i].getName()).append(": ").append(pojoParams[i].getName());
//                }
//                builder.append(" }");
//                return builder.toString();
//        }
    }


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }
}
