/*
 * Copyright (c) 2004-2025, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.tracker.audit;

import static org.hisp.dhis.audit.AuditOperationType.READ;
import static org.hisp.dhis.test.TestBase.clearSecurityContext;
import static org.hisp.dhis.test.TestBase.injectSecurityContextNoSettings;
import static org.hisp.dhis.test.TestBase.makeUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.trackedentity.TrackedEntityAuditQueryParams;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.tracker.acl.TrackerAccessManager;
import org.hisp.dhis.tracker.model.TrackedEntity;
import org.hisp.dhis.user.UserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Morten Svanæs
 */
@ExtendWith(MockitoExtension.class)
class DefaultTrackedEntityAuditServiceTest {
  @Mock private TrackedEntityAuditStore store;
  @Mock private IdentifiableObjectManager manager;
  @Mock private TrackerAccessManager accessManager;
  @Mock private AsyncTrackedEntityAuditWriter writer;
  @InjectMocks private DefaultTrackedEntityAuditService service;

  @Test
  void shouldNotDispatchEmptyBatch() {
    service.addTrackedEntityAudit(READ, "reader", List.of());

    verifyNoInteractions(writer, store);
  }

  @Test
  void shouldNotDispatchBatchWithoutEligibleEntities() {
    TrackedEntityType type = new TrackedEntityType();
    type.setAllowAuditLog(false);
    TrackedEntity disabled = new TrackedEntity();
    disabled.setTrackedEntityType(type);

    service.addTrackedEntityAudit(
        READ, "reader", Arrays.asList(null, new TrackedEntity(), disabled));

    verifyNoInteractions(writer, store);
  }

  @Test
  void shouldNotDispatchSingleEntityWithoutAuditType() {
    service.addTrackedEntityAudit(READ, "reader", (TrackedEntity) null);
    service.addTrackedEntityAudit(READ, "reader", new TrackedEntity());

    verifyNoInteractions(writer, store);
  }

  @Test
  void shouldNotDispatchWithoutUsername() {
    TrackedEntityType type = new TrackedEntityType();
    type.setAllowAuditLog(true);
    TrackedEntity enabled = new TrackedEntity();
    enabled.setTrackedEntityType(type);

    service.addTrackedEntityAudit(READ, null, enabled);
    service.addTrackedEntityAudit(READ, null, List.of(enabled));

    verifyNoInteractions(writer, store);
  }

  @AfterEach
  void tearDown() {
    clearSecurityContext();
  }

  @Test
  void shouldNotRestrictAuditQueryForSuperuser() {
    injectSecurityContextNoSettings(UserDetails.fromUser(makeUser("S", List.of("ALL"))));
    TrackedEntityAuditQueryParams params = new TrackedEntityAuditQueryParams();
    List<TrackedEntityAudit> audits = List.of(new TrackedEntityAudit("teUid000001", "u", READ));
    when(store.getTrackedEntityAudit(params)).thenReturn(audits);
    when(store.getTrackedEntityAuditCount(params)).thenReturn(1);

    assertSame(audits, service.getTrackedEntityAudits(params));
    assertEquals(1, service.getTrackedEntityAuditsCount(params));
    verifyNoInteractions(manager, accessManager);
  }

  @Test
  void shouldNotQueryAuditsForNonSuperuserWithoutTrackedEntities() {
    injectSecurityContextNoSettings(UserDetails.fromUser(makeUser("A")));
    TrackedEntityAuditQueryParams params = new TrackedEntityAuditQueryParams();

    assertTrue(service.getTrackedEntityAudits(params).isEmpty());
    assertEquals(0, service.getTrackedEntityAuditsCount(params));
    verifyNoInteractions(store, manager, accessManager);
  }

  @Test
  void shouldRestrictAuditQueryToReadableExistingTrackedEntitiesForNonSuperuser() {
    UserDetails user = UserDetails.fromUser(makeUser("A"));
    injectSecurityContextNoSettings(user);
    TrackedEntity readable = trackedEntity("teReadable1");
    TrackedEntity notReadable = trackedEntity("teNotReadb1");
    List<String> requested = List.of("teReadable1", "teNotReadb1", "teNotExist1");
    // teNotExist1 was hard deleted: its audits remain but the tracked entity cannot be loaded
    when(manager.getByUid(TrackedEntity.class, requested))
        .thenReturn(List.of(readable, notReadable));
    when(accessManager.canRead(any(), eq(readable))).thenReturn(List.of());
    when(accessManager.canRead(any(), eq(notReadable))).thenReturn(List.of("no access"));
    Pager pager = new Pager(2, 10, 3);
    TrackedEntityAuditQueryParams params =
        new TrackedEntityAuditQueryParams()
            .setTrackedEntities(requested)
            .setUsers(List.of("admin"))
            .setAuditTypes(List.of(READ))
            .setPager(pager);

    service.getTrackedEntityAudits(params);
    service.getTrackedEntityAuditsCount(params);

    ArgumentCaptor<TrackedEntityAuditQueryParams> query =
        ArgumentCaptor.forClass(TrackedEntityAuditQueryParams.class);
    verify(store).getTrackedEntityAudit(query.capture());
    assertEquals(List.of("teReadable1"), query.getValue().getTrackedEntities());
    assertEquals(List.of("admin"), query.getValue().getUsers());
    assertEquals(List.of(READ), query.getValue().getAuditTypes());
    assertSame(pager, query.getValue().getPager());
    verify(store).getTrackedEntityAuditCount(query.capture());
    assertEquals(List.of("teReadable1"), query.getValue().getTrackedEntities());
    assertEquals(requested, params.getTrackedEntities(), "caller params must not be mutated");
  }

  @Test
  void shouldNotQueryAuditsForNonSuperuserWithoutReadableTrackedEntities() {
    injectSecurityContextNoSettings(UserDetails.fromUser(makeUser("A")));
    TrackedEntity notReadable = trackedEntity("teNotReadb1");
    List<String> requested = List.of("teNotReadb1", "teNotExist1");
    when(manager.getByUid(TrackedEntity.class, requested)).thenReturn(List.of(notReadable));
    when(accessManager.canRead(any(), eq(notReadable))).thenReturn(List.of("no access"));
    TrackedEntityAuditQueryParams params =
        new TrackedEntityAuditQueryParams().setTrackedEntities(requested);

    assertTrue(service.getTrackedEntityAudits(params).isEmpty());
    assertEquals(0, service.getTrackedEntityAuditsCount(params));
    verifyNoInteractions(store);
  }

  private static TrackedEntity trackedEntity(String uid) {
    TrackedEntity trackedEntity = new TrackedEntity();
    trackedEntity.setUid(uid);
    return trackedEntity;
  }
}
