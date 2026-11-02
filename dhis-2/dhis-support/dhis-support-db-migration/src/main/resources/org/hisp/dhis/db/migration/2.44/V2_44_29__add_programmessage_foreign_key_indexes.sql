-- https://dhis2.atlassian.net/browse/DHIS2-16351
-- The programmessage foreign-key columns were never indexed. Postgres only
-- auto-indexes the referenced (parent) side of a foreign key, not the
-- referencing (child) side. When a parent row is deleted, Postgres fires an
-- ON DELETE NO ACTION referential-integrity check that scans programmessage for
-- referencing rows, once per deleted parent row. Without these indexes that
-- check degrades to a full sequential scan of programmessage per deleted row,
-- making deletion of soft-deleted tracked entities / enrollments (and the
-- Maintenance "remove soft-deleted" job) catastrophically slow on instances
-- where programmessage has grown large.
CREATE INDEX IF NOT EXISTS "in_programmessage_trackedentityid"
    ON "programmessage" ("trackedentityid");
CREATE INDEX IF NOT EXISTS "in_programmessage_enrollmentid"
    ON "programmessage" ("enrollmentid");
CREATE INDEX IF NOT EXISTS "in_programmessage_trackereventid"
    ON "programmessage" ("trackereventid");
CREATE INDEX IF NOT EXISTS "in_programmessage_singleeventid"
    ON "programmessage" ("singleeventid");
CREATE INDEX IF NOT EXISTS "in_programmessage_organisationunitid"
    ON "programmessage" ("organisationunitid");

ANALYZE "programmessage";
