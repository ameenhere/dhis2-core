/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.tracker.acl;

import static org.hisp.dhis.tracker.test.TrackerTestBase.createEnrollment;
import static org.hisp.dhis.tracker.test.TrackerTestBase.createEvent;
import static org.hisp.dhis.tracker.test.TrackerTestBase.createTrackedEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;
import org.hisp.dhis.category.Category;
import org.hisp.dhis.category.CategoryCombo;
import org.hisp.dhis.category.CategoryOption;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.common.DataDimensionType;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.feedback.NotFoundException;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramType;
import org.hisp.dhis.security.acl.AccessStringHelper;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.tracker.Page;
import org.hisp.dhis.tracker.PageParams;
import org.hisp.dhis.tracker.export.enrollment.EnrollmentOperationParams;
import org.hisp.dhis.tracker.export.enrollment.EnrollmentService;
import org.hisp.dhis.tracker.export.trackerevent.TrackerEventOperationParams;
import org.hisp.dhis.tracker.export.trackerevent.TrackerEventService;
import org.hisp.dhis.tracker.model.Enrollment;
import org.hisp.dhis.tracker.model.TrackedEntity;
import org.hisp.dhis.tracker.model.TrackerEvent;
import org.hisp.dhis.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * A user who cannot read the attribute option combo (AOC) of an enrollment cannot see the
 * enrollment, and therefore must not see any event in it, even if the event AOC is readable.
 *
 * <p>Fixture: program with event combo "Funding partner" (P1 readable) and enrollment combo
 * "Enrolling site" (S1 readable, S2 not readable). The hidden enrollment has AOC S2, the visible
 * one S1; each contains one event with AOC P1.
 */
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrollmentAocEventAccessTest extends PostgresIntegrationTestBase {
  @Autowired private IdentifiableObjectManager manager;

  @Autowired private CategoryService categoryService;

  @Autowired private TrackedEntityProgramOwnerService ownerService;

  @Autowired private EnrollmentService enrollmentService;

  @Autowired private TrackerEventService trackerEventService;

  private User user;

  private Program program;

  private Enrollment visibleEnrollment;

  private TrackerEvent eventInHiddenEnrollment;

  private TrackerEvent eventInVisibleEnrollment;

  @BeforeAll
  void setUp() {
    OrganisationUnit orgUnit = createOrganisationUnit('A');
    manager.save(orgUnit);

    CategoryOption coP1 = option("Partner 1", "trkPartner1", AccessStringHelper.FULL);
    Category caPartner = category("Funding partner", "trkCatPartn", coP1);
    CategoryCombo ccPartner = combo("Partner combo", "trkCcPartnr", caPartner);
    CategoryOptionCombo cocP1 = coc(ccPartner, "trkCocPart1", coP1);

    CategoryOption coS1 = option("Site 1", "trkSiteOpt1", AccessStringHelper.FULL);
    CategoryOption coS2 = option("Site 2", "trkSiteOpt2", AccessStringHelper.DEFAULT);
    Category caSite = category("Enrolling site", "trkCatSiteX", coS1, coS2);
    CategoryCombo ccSite = combo("Site combo", "trkCcSiteXX", caSite);
    CategoryOptionCombo cocS1 = coc(ccSite, "trkCocSite1", coS1);
    CategoryOptionCombo cocS2 = coc(ccSite, "trkCocSite2", coS2);

    TrackedEntityType teType = createTrackedEntityType('A');
    manager.save(teType);

    ProgramStage stage = createProgramStage('A', 0);
    stage.setRepeatable(true);
    manager.save(stage);

    program = createProgram('A');
    program.setProgramType(ProgramType.WITH_REGISTRATION);
    program.setTrackedEntityType(teType);
    program.getOrganisationUnits().add(orgUnit);
    program.getProgramStages().add(stage);
    program.setCategoryCombo(ccPartner);
    program.setEnrollmentCategoryCombo(ccSite);
    manager.save(program);
    stage.setProgram(program);
    manager.update(stage);

    TrackedEntity te1 = createTrackedEntity(orgUnit, teType);
    TrackedEntity te2 = createTrackedEntity(orgUnit, teType);
    manager.save(te1);
    manager.save(te2);

    Enrollment hiddenEnrollment = enroll(te1, orgUnit, cocS2);
    visibleEnrollment = enroll(te2, orgUnit, cocS1);
    eventInHiddenEnrollment = event(stage, hiddenEnrollment, orgUnit, cocP1);
    eventInVisibleEnrollment = event(stage, visibleEnrollment, orgUnit, cocP1);

    // manager.save() may reset sharing; apply it explicitly afterwards
    share(teType, AccessStringHelper.FULL);
    share(stage, AccessStringHelper.FULL);
    share(program, AccessStringHelper.FULL);
    share(coP1, AccessStringHelper.FULL);
    share(coS1, AccessStringHelper.FULL);
    share(coS2, AccessStringHelper.DEFAULT);

    user = createUserWithAuth("trackeraoc");
    user.addOrganisationUnit(orgUnit);
    userService.addUser(user);

    manager.flush();
  }

  @BeforeEach
  void injectUser() {
    injectSecurityContextUser(user);
  }

  @Test
  void shouldOnlyReturnEnrollmentWithReadableAttributeOptionCombo() throws Exception {
    List<Enrollment> enrollments =
        enrollmentService.findEnrollments(
            EnrollmentOperationParams.builder().program(UID.of(program)).build());

    assertEquals(
        List.of(visibleEnrollment.getUid()), enrollments.stream().map(Enrollment::getUid).toList());
  }

  @Test
  void shouldNotReturnEventsInEnrollmentWithNotReadableAttributeOptionCombo() throws Exception {
    List<TrackerEvent> events =
        trackerEventService.findEvents(
            TrackerEventOperationParams.builderForProgram(UID.of(program)).build());

    assertEquals(
        List.of(eventInVisibleEnrollment.getUid()),
        events.stream().map(TrackerEvent::getUid).toList());
  }

  @Test
  void shouldNotCountEventsInEnrollmentWithNotReadableAttributeOptionCombo() throws Exception {
    Page<TrackerEvent> page =
        trackerEventService.findEvents(
            TrackerEventOperationParams.builderForProgram(UID.of(program)).build(),
            PageParams.of(1, 10, true));

    assertEquals(
        List.of(eventInVisibleEnrollment.getUid()),
        page.getItems().stream().map(TrackerEvent::getUid).toList());
    assertEquals(1L, page.getTotal());
  }

  @Test
  void shouldNotFindEventInEnrollmentWithNotReadableAttributeOptionCombo() throws Exception {
    assertEquals(
        eventInVisibleEnrollment.getUid(),
        trackerEventService.getEvent(UID.of(eventInVisibleEnrollment)).getUid());
    assertThrows(
        NotFoundException.class,
        () -> trackerEventService.getEvent(UID.of(eventInHiddenEnrollment)));
  }

  @Test
  void shouldReturnEventsInAllEnrollmentsForSuperuser() throws Exception {
    injectAdminIntoSecurityContext();

    List<TrackerEvent> events =
        trackerEventService.findEvents(
            TrackerEventOperationParams.builderForProgram(UID.of(program))
                .orgUnitMode(OrganisationUnitSelectionMode.ALL)
                .build());

    assertEquals(2, events.size());
  }

  private void share(IdentifiableObject object, String publicAccess) {
    object.getSharing().setPublicAccess(publicAccess);
    object.getSharing().setOwner("nobodyOwner");
    object.getSharing().resetUserAccesses();
    manager.update(object);
  }

  private CategoryOption option(String name, String uid, String publicAccess) {
    CategoryOption co = new CategoryOption(name);
    co.setAutoFields();
    co.setUid(uid);
    co.setShortName(name);
    co.getSharing().setPublicAccess(publicAccess);
    manager.save(co);
    return co;
  }

  private Category category(String name, String uid, CategoryOption... options) {
    Category ca = createCategory(name, uid, options);
    ca.setDataDimensionType(DataDimensionType.ATTRIBUTE);
    ca.getSharing().setPublicAccess(AccessStringHelper.READ);
    manager.save(ca);
    return ca;
  }

  private CategoryCombo combo(String name, String uid, Category... categories) {
    CategoryCombo cc = createCategoryCombo(name, uid, categories);
    cc.setDataDimensionType(DataDimensionType.ATTRIBUTE);
    manager.save(cc);
    return cc;
  }

  private CategoryOptionCombo coc(CategoryCombo cc, String uid, CategoryOption option) {
    CategoryOptionCombo coc = createCategoryOptionCombo(option.getName(), uid, cc, option);
    categoryService.addCategoryOptionCombo(coc);
    cc.getOptionCombos().add(coc);
    manager.update(cc);
    option.getCategoryOptionCombos().add(coc);
    manager.update(option);
    return coc;
  }

  private Enrollment enroll(TrackedEntity te, OrganisationUnit orgUnit, CategoryOptionCombo aoc) {
    Enrollment enrollment = createEnrollment(program, te, orgUnit);
    enrollment.setEnrollmentDate(new Date());
    enrollment.setOccurredDate(new Date());
    enrollment.setAttributeOptionCombo(aoc);
    manager.save(enrollment);
    te.getEnrollments().add(enrollment);
    manager.update(te);
    ownerService.createTrackedEntityProgramOwner(te, program, orgUnit);
    return enrollment;
  }

  private TrackerEvent event(
      ProgramStage stage,
      Enrollment enrollment,
      OrganisationUnit orgUnit,
      CategoryOptionCombo aoc) {
    TrackerEvent event = createEvent(stage, enrollment, orgUnit);
    event.setOccurredDate(new Date());
    event.setAttributeOptionCombo(aoc);
    manager.save(event);
    return event;
  }
}
