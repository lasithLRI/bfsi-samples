package com.wso2.openbanking.demo;

import com.wso2.openbanking.demo.controller.ApiController;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;

public final class OpenBankingDemoApplication extends Application {

    private final Set<Object> singletons;

 OpenBankingDemoApplication() {
        this.singletons = Set.of(new ApiController());
    }

    /**
     * Returns an unmodifiable view of the singleton resource instances
     * registered with this application.
     *
     * The returned set contains all JAX-RS resource objects that are shared
     * across requests. The JAX-RS runtime will use these instances directly
     * rather than creating new ones per request. An unmodifiable copy is
     * returned to prevent external modification of the internal state.
     *
     * @return an unmodifiable set of singleton resource and provider instances
     */
    @Override
    public Set<Object> getSingletons() {
        return Collections.unmodifiableSet(singletons);
    }
}
