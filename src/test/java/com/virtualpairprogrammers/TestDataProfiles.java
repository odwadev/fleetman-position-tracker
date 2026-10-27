package com.virtualpairprogrammers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.StandardEnvironment;

import com.virtualpairprogrammers.tracker.data.Data;
import com.virtualpairprogrammers.tracker.data.DataBasicInMemoryImpl;
import com.virtualpairprogrammers.tracker.data.DataMongoDbImpl;

@RunWith(Parameterized.class)
public class TestDataProfiles {

    @Parameters(name = "{0}")
    public static Collection<Object[]> profiles() {
        return Arrays.asList(new Object[][] {
            {"standalone", new String[] {"standalone"}, DataBasicInMemoryImpl.class},
            {"localhost", new String[] {"localhost"}, DataBasicInMemoryImpl.class},
            {"production-microservice", new String[] {"production-microservice"}, DataMongoDbImpl.class},
            {"local-microservice", new String[] {"local-microservice"}, DataMongoDbImpl.class},
            {"aws", new String[] {"aws"}, DataMongoDbImpl.class},
            {"default", new String[] {"default"}, DataMongoDbImpl.class},
            {"no active profile", new String[0], DataMongoDbImpl.class}
        });
    }

    private final String[] activeProfiles;
    private final Class<? extends Data> expectedImplementation;

    public TestDataProfiles(String name, String[] activeProfiles,
            Class<? extends Data> expectedImplementation) {
        this.activeProfiles = activeProfiles;
        this.expectedImplementation = expectedImplementation;
    }

    @Test
    public void exactlyOneDataImplementationIsEligible() {
        StandardEnvironment environment = new StandardEnvironment();
        // Isolate profile selection from the machine's system properties and environment.
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.setActiveProfiles(activeProfiles);

        List<Class<? extends Data>> eligible = new ArrayList<>();
        for (Class<? extends Data> implementation : Arrays.asList(
                DataBasicInMemoryImpl.class, DataMongoDbImpl.class)) {
            Profile profile = implementation.getAnnotation(Profile.class);
            if (profile == null || environment.acceptsProfiles(profile.value())) {
                eligible.add(implementation);
            }
        }

        assertEquals(Collections.singletonList(expectedImplementation), eligible);
    }
}
