package com.zuehlke.oauth;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class OAuthApplication extends Application {

    private static final Set<Class<?>> classes = 
            Collections.unmodifiableSet(new HashSet<Class<?>>(Arrays.asList(SecuredResource.class)));
    
    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
