/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013-2019 Minecraft Forge
 * Copyright (C) 2020-2023 anatawa12 and other contributors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.minecraftforge.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.JavaExecSpec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExecHelper {
    private static final ExecInvoker EXEC_INVOKER = GradleVersionUtils.choose("9.0",
            OldExecInvoker::new, NewExecInvoker::new);

    interface ExecInvoker {
        ExecResult exec(Project project, Action<? super ExecSpec> action);
        ExecResult javaexec(Project project, Action<? super JavaExecSpec> action);
    }

    private static class OldExecInvoker implements ExecInvoker {
        private static final Method execMethod;
        private static final Method javaexecMethod;

        static {
            try {
                execMethod = Project.class.getMethod("exec", Action.class);
                javaexecMethod = Project.class.getMethod("javaexec", Action.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExecResult exec(Project project, Action<? super ExecSpec> action) {
            try {
                return (ExecResult) execMethod.invoke(project, action);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke exec method", e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public ExecResult javaexec(Project project, Action<? super JavaExecSpec> action) {
            try {
                return (ExecResult) javaexecMethod.invoke(project, action);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke javaexec method", e);
            }
        }
    }

    private static class NewExecInvoker implements ExecInvoker {
        private static final Method getServicesMethod;
        private static final Method getMethod;

        static {
            try {
                Class<?> projectInternalClass = Class.forName("org.gradle.api.internal.project.ProjectInternal");
                getServicesMethod = projectInternalClass.getMethod("getServices");

                Class<?> serviceRegistryClass = Class.forName("org.gradle.internal.service.ServiceRegistry");
                getMethod = serviceRegistryClass.getMethod("get", Class.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize NewExecInvoker reflection", e);
            }
        }

        private ExecOperations getExecOperations(Project project) {
            try {
                Object serviceRegistry = getServicesMethod.invoke(project);
                return (ExecOperations) getMethod.invoke(serviceRegistry, ExecOperations.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get ExecOperations service", e);
            }
        }

        @Override
        public ExecResult exec(Project project, Action<? super ExecSpec> action) {
            return getExecOperations(project).exec(action);
        }

        @Override
        public ExecResult javaexec(Project project, Action<? super JavaExecSpec> action) {
            return getExecOperations(project).javaexec(action);
        }
    }

    public static ExecResult exec(Project project, Action<? super ExecSpec> action) {
        return EXEC_INVOKER.exec(project, action);
    }

    public static ExecResult javaexec(Project project, Action<? super JavaExecSpec> action) {
        return EXEC_INVOKER.javaexec(project, action);
    }
}
