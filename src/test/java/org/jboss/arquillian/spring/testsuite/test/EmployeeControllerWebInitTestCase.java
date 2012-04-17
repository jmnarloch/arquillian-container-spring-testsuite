/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.spring.testsuite.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.spring.annotations.SpringWebConfiguration;
import org.jboss.arquillian.spring.testsuite.beans.config.WebAppConfig;
import org.jboss.arquillian.spring.testsuite.beans.controller.EmployeeController;
import org.jboss.arquillian.spring.testsuite.beans.model.Employee;
import org.jboss.arquillian.spring.testsuite.beans.repository.EmployeeRepository;
import org.jboss.arquillian.spring.testsuite.beans.repository.impl.DefaultEmployeeRepository;
import org.jboss.arquillian.spring.testsuite.beans.repository.impl.NullEmployeeRepository;
import org.jboss.arquillian.spring.testsuite.beans.service.EmployeeService;
import org.jboss.arquillian.spring.testsuite.beans.service.impl.DefaultEmployeeService;
import org.jboss.arquillian.spring.testsuite.beans.web.EmployeeWebInitializer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * <p>Tests the {@link org.jboss.arquillian.spring.testsuite.beans.controller.EmployeeController} class.</p>
 */
@RunWith(Arquillian.class)
@SpringWebConfiguration(servletName = "employee")
public class EmployeeControllerWebInitTestCase {

    @Deployment
    @OverProtocol("Servlet 3.0")
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "spring-test.war")
                .addClasses(Employee.class,
                        EmployeeService.class, DefaultEmployeeService.class,
                        EmployeeRepository.class, DefaultEmployeeRepository.class, NullEmployeeRepository.class,
                        EmployeeController.class, WebAppConfig.class, EmployeeWebInitializer.class)
                .addAsLibraries(springDependencies())
                .addAsLibraries(mockitoDependencies());
    }

    /**
     * The injected {@link EmployeeController}.
     */
    @Autowired
    private EmployeeController employeeController;

    /**
     * Tests {@link EmployeeController#getEmployees(org.springframework.ui.Model)} method.
     */
    @Test
    public void testGetEmployees() {

        String result;
        Model model;
        ArgumentCaptor<List> argument;

        assertNotNull("The controller hasn't been injected.", employeeController);

        model = mock(Model.class);
        argument = ArgumentCaptor.forClass(List.class);

        result = employeeController.getEmployees(model);

        verify(model).addAttribute(eq("employees"), argument.capture());
        assertEquals("The controller returned invalid view name, 'employeeList' was expected.", "employeeList", result);
        assertEquals("Two employees should be returned from model.", 2, argument.getValue().size());
    }

    /**
     * Retrieves spring mvc dependencies.
     *
     * @return spring mvc dependencies
     */
    private static File[] springDependencies() {
        return resolveArtifact("org.springframework:spring-webmvc");
    }

    /**
     * Retrieves mockito dependencies.
     *
     * @return mockito dependencies
     */
    private static File[] mockitoDependencies() {
        return resolveArtifact("org.mockito:mockito-all");
    }

    /**
     * Resolves the given artifact by it's name with help of maven build system.
     *
     * @param artifact the fully qualified artifact name
     *
     * @return the resolved files
     */
    private static File[] resolveArtifact(String artifact) {
        MavenDependencyResolver mvnResolver = DependencyResolvers.use(MavenDependencyResolver.class);

        mvnResolver.loadMetadataFromPom("pom.xml");

        return mvnResolver.artifacts(artifact).resolveAsFiles();
    }
}
