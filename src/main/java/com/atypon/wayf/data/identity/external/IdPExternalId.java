package com.atypon.wayf.data.identity.external;

import com.atypon.wayf.data.identity.IdentityProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({
        "id",
        "provider"
})
public class IdPExternalId {


    private Long id;

    @JsonIgnore
    private IdentityProvider identityProvider;

    @JsonProperty("id")
    private String externalId;
    private ExternalProvider provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @JsonProperty("id")
    public String getExternalId() {
        return externalId;
    }

    @JsonProperty("id")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ExternalProvider getProvider() {
        return provider;
    }

    public void setProvider(ExternalProvider provider) {
        this.provider = provider;
    }


}
