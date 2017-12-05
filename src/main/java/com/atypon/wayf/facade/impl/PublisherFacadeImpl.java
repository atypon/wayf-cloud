/*
 * Copyright 2017 Atypon Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atypon.wayf.facade.impl;

import com.atypon.wayf.cache.Cache;
import com.atypon.wayf.dao.PublisherDao;
import com.atypon.wayf.data.ServiceException;
import com.atypon.wayf.data.authentication.AuthenticatedEntity;
import com.atypon.wayf.data.authentication.AuthorizationToken;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.publisher.PublisherQuery;
import com.atypon.wayf.data.publisher.PublisherStatus;
import com.atypon.wayf.data.publisher.registration.PublisherRegistration;
import com.atypon.wayf.data.publisher.registration.PublisherRegistrationStatus;
import com.atypon.wayf.data.user.User;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.reactivex.FacadePolicies;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.http.HttpStatus;

import static com.atypon.wayf.reactivex.FacadePolicies.singleOrException;

@Singleton
public class PublisherFacadeImpl implements PublisherFacade {

    @Inject
    private PublisherDao publisherDao;

    @Inject
    private ClientJsFacade clientJsFacade;

    @Inject
    private PublisherRegistrationFacade registrationFacade;

    @Inject
    private UserFacade userFacade;

    @Inject
    private CryptFacade cryptFacade;

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private AuthorizationTokenFactory authorizationTokenFactory;

    @Inject
    @Named("publisherSaltCache")
    private Cache<Long, String> saltCache;

    public PublisherFacadeImpl() {
    }

    @Override
    public Completable delete(Long publisherId) {
        Publisher publisher = singleOrException(publisherDao.read(publisherId),
                HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not find Publisher for id [{}]", publisherId).blockingGet();
        AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());
        return publisherDao.delete(publisherId)
                .compose((completable) -> FacadePolicies.applyCompletable(completable))
                .andThen(registrationFacade.delete(publisher.getContact() != null ? publisher.getContact().getId() : null))
                .compose((completable) -> FacadePolicies.applyCompletable(completable))
                .andThen(userFacade.delete(publisher.getContact() != null ? publisher.getContact().getId() : null));
    }

    @Override
    public Single<Publisher> create(Publisher publisher) {
        publisher.setStatus(PublisherStatus.ACTIVE);
        publisher.setSalt(cryptFacade.generateSalt());


        User admin = AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());

        return userFacade.create(publisher.getContact()) // Create the contact user
                .flatMap((contact) -> {
                    // Set the newly created contact on the publisher
                    publisher.setContact(contact);

                    // Generate the publisher specific Javascript widget
                    clientJsFacade.generateWidgetForPublisher(publisher).subscribe(publisher::setWidgetLocation);


                    // Create the publisher
                    return publisherDao.create(publisher);
                })
                .flatMap((createdPublisher) ->

                        Single.zip(
                                // Create an authorization token for the newly created publisher
                                authenticationFacade.createCredentials(authorizationTokenFactory.generateToken(createdPublisher))
                                        .compose(FacadePolicies::applySingle),

                                // Approve the publisher registration if one existed
                                handleRegistrationApproval(publisher).compose(FacadePolicies::applySingle),

                                // Combine the results with the previously created publisher
                                (token, approvedRegistration) -> {
                                    createdPublisher.setToken(token);
                                    createdPublisher.setContact(publisher.getContact());
                                    createdPublisher.setWidgetLocation(publisher.getWidgetLocation());
                                    return createdPublisher;
                                }
                        )
                );
    }

    @Override
    public Single<Publisher> read(Long id) {
        return singleOrException(publisherDao.read(id), HttpStatus.SC_NOT_FOUND, "Invalid Publisher ID");
    }

    @Override
    public Observable<Publisher> filter(PublisherQuery filter) {

        boolean isAdminView = PublisherQuery.ADMIN_VIEW.equals(filter.getView());

        if (isAdminView) {
            AuthenticatedEntity.authenticatedAsAdmin(RequestContextAccessor.get().getAuthenticated());
        }

        return publisherDao.filter(filter).flatMap(publisher -> {
            if (!isAdminView) {
                publisher.setWidgetLocation(null);
            } else {
                authenticationFacade.getCredentialsForAuthenticatable(publisher).filter(token -> token instanceof AuthorizationToken).
                        singleOrError().onErrorReturnItem(new AuthorizationToken()).subscribe(credentials -> publisher.setToken((AuthorizationToken)credentials));

            }
            return Observable.just(publisher);
        });
    }

    @Override
    public Single<Publisher> lookupCode(String publisherCode) {
        PublisherQuery query = new PublisherQuery();
        query.setCodes(publisherCode);

        return singleOrException(filter(query), HttpStatus.SC_BAD_REQUEST, "Could not find publisher for code [{}]", publisherCode);
    }

    private Single<PublisherRegistration> handleRegistrationApproval(Publisher publisher) {
        if (publisher.getRegistration() != null && publisher.getRegistration().getId() != null) {
            publisher.getRegistration().setStatus(PublisherRegistrationStatus.APPROVED);

            return registrationFacade.updateStatus(publisher.getRegistration());
        } else {
            return Single.just(new PublisherRegistration());
        }
    }

    @Override
    public String getPublishersSalt(Long publisherId) {
        return singleOrException(saltCache.get(publisherId), HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not find Publisher encryption salt for id [{}]", publisherId).blockingGet();
    }
}
