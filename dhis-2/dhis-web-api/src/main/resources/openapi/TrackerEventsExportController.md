# Get Tracker Events

## Specific endpoints

### `getEvents`

Get tracker events matching given query parameters. Tracker events are events of programs with
registration (tracker programs). Use `/tracker/singleEvents` for events of programs without
registration.

The `program` parameter is required and must be a program with registration.

Events are scoped by the organisation unit that owns the tracked entity in the `program`, not the
organisation unit of the event. See `orgUnit` and `orgUnitMode`.

### `getEventsAsCsv`

Get tracker events matching given query parameters as CSV. Accepts the same parameters as
`getEvents`. Use `skipHeader` to omit the header row.

### `getEventsAsCsvGZip`

Get tracker events matching given query parameters as gzip compressed CSV. Accepts the same
parameters as `getEvents`. Use `skipHeader` to omit the header row.

### `getEventsAsCsvZip`

Get tracker events matching given query parameters as zip compressed CSV. Accepts the same
parameters as `getEvents`. Use `skipHeader` to omit the header row.

### `getEventsAsCsv.parameter.skipHeader`

Omit the header row from the CSV response by specifying `skipHeader=true`. The header row is
included by default.

### `getEventsAsCsvGZip.parameter.skipHeader`

Omit the header row from the CSV response by specifying `skipHeader=true`. The header row is
included by default.

### `getEventsAsCsvZip.parameter.skipHeader`

Omit the header row from the CSV response by specifying `skipHeader=true`. The header row is
included by default.

### `getEventByUid`

Get a tracker event with the specified UID.

### `getEventByUid.parameter.uid`

Get a tracker event with the specified UID.

### `getEventByUid.parameter.fields`

Get only the specified fields in the JSON response. This query parameter allows you to remove
unnecessary fields from the response and in some cases decrease the response time. Refer to
https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/metadata.html#webapi_metadata_field_filter
for how to use it.

### `getEventDataValueFile`

Get a tracker event data value file or image for the specified event and data element UID. Images
are returned in their original dimension. Use `getEventDataValueImage` to get an image in a
different dimension.

### `getEventDataValueImage`

Get a tracker event data value image for the specified event and data element UID. Images are
returned in their original dimension by default. This endpoint is only supported for data elements
of value type image.

### `getEventDataValueImage.parameter.dimension`

Get the image in the specified dimension. The image is returned in its original dimension when no
`dimension` is given.

### `getEventChangeLogsByUid`

Get the change logs of all data elements and event fields belonging to the specified tracker event
UID. Change logs are ordered by newest (`createdAt` desc) by default.

Change logs can be ordered using `order` by the following case-sensitive properties

* `change`
* `createdAt`
* `username`

Change logs can be filtered using `filter=<propertyName>:eq:<value>`. Only one filter can be
specified, and the only supported operator is `eq`. Change logs can be filtered by the following
case-sensitive properties

* `dataElement` - the UID of a data element
* `field` - the name of an event field: `occurredAt`, `scheduledAt` or `geometry`
* `username` - the username of the user who made the change

## Common for all endpoints

### `*.parameter.event`

The UID of the tracker event.

### `*.parameter.dataElement`

The UID of the data element.

### `*.parameter.idScheme`

Get metadata identifiers in the response using the specified identifier scheme. This applies to
all metadata unless overridden by a metadata specific `*IdScheme` parameter. Valid values are
`UID` (default), `CODE`, `NAME` and `ATTRIBUTE:<attribute-uid>`.

The request fails with status 422 if any metadata in the response does not have an identifier for
the requested scheme. Request parameters like `program` or `orgUnit` must always be given as UIDs.

### `*.parameter.programIdScheme`

Get program identifiers in the response using the specified identifier scheme. Defaults to the
value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.programStageIdScheme`

Get program stage identifiers in the response using the specified identifier scheme. Defaults to
the value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.orgUnitIdScheme`

Get organisation unit identifiers in the response using the specified identifier scheme. Defaults
to the value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.dataElementIdScheme`

Get data element identifiers in the response using the specified identifier scheme. Defaults to the
value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.categoryOptionComboIdScheme`

Get attribute option combination identifiers in the response using the specified identifier
scheme. Defaults to the value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.categoryOptionIdScheme`

Get attribute category option identifiers in the response using the specified identifier scheme.
Defaults to the value of `idScheme`. See `idScheme` for valid values.

### `*.parameter.TrackerEventRequestParams.program`

Get tracker events from the specified program. Required. The program must be a program with
registration.

### `*.parameter.TrackerEventRequestParams.programStage`

Get tracker events from the specified program stage.

### `*.parameter.TrackerEventRequestParams.enrollmentStatus`

Get tracker events from enrollments that have the specified enrollment status.

### `*.parameter.TrackerEventRequestParams.programStatus`

Get tracker events from enrollments that have the specified enrollment status.

**DEPRECATED as of 2.42:** Use parameter `enrollmentStatus` instead.

See `enrollmentStatus` for details.

### `*.parameter.TrackerEventRequestParams.followUp`

Get tracker events with specified follow-up status. When set to true, only events from enrollments
marked for follow-up will be returned. When set to false, only events from enrollments not marked
for follow-up will be returned.

### `*.parameter.TrackerEventRequestParams.trackedEntity`

Get tracker events belonging to the tracked entity with the specified UID.

### `*.parameter.TrackerEventRequestParams.enrollments`

`<enrollment1-uid>[,<enrollment2-uid>...]`

Get tracker events belonging to the enrollments with the specified UID(s).

### `*.parameter.TrackerEventRequestParams.orgUnit`

Get tracker events belonging to the specified `orgUnit` relative to the `orgUnitMode`. A tracker
event belongs to the organisation unit that owns the tracked entity in the `program`.

`orgUnit` cannot be used with `orgUnitMode` `ALL`, `ACCESSIBLE` or `CAPTURE`.

### `*.parameter.TrackerEventRequestParams.orgUnitMode`

Get tracker events belonging to the organisation units referenced by the specified organisation
unit mode. Defaults to `SELECTED` when an `orgUnit` is given, and to `ACCESSIBLE` otherwise.

- When `orgUnitMode=SELECTED`, events belonging to the `orgUnit` are returned.
- When `orgUnitMode=CHILDREN`, events belonging to the `orgUnit` or its direct children are
  returned.
- When `orgUnitMode=DESCENDANTS`, events belonging to the `orgUnit` or any of its descendants are
  returned.
- When `orgUnitMode=ALL`, events are returned irrespective of the organisation unit they belong
  to. Only superusers can use this mode.
- When `orgUnitMode=ACCESSIBLE`, events belonging to any organisation unit in the user's
  effective search scope are returned. Effective search scope is the union of the user's search
  scope and capture scope. For protected and closed programs, only the capture scope is used.
- When `orgUnitMode=CAPTURE`, events belonging to any organisation unit in the user's capture
  scope are returned.

The `orgUnit` parameter is required for `SELECTED`, `CHILDREN` and `DESCENDANTS`, and not allowed
for `ALL`, `ACCESSIBLE` and `CAPTURE`. See `orgUnit` for which organisation unit an event belongs
to.

### `*.parameter.TrackerEventRequestParams.assignedUserMode`

Get tracker events assigned to users according to the specified user mode. By default, all events
will be retrieved, regardless of whether a user is assigned.

- When `assignedUserMode=ALL` or no `assignedUserMode`(default) is given, all events are
  returned irrespective of whether they are assigned to a user or not.

- When `assignedUserMode=CURRENT`, events that are assigned to the logged-in user will be returned.

- When `assignedUserMode=ANY`, events that are assigned to any user will be returned.

- When `assignedUserMode=NONE`, events that are not assigned to any user will be returned.

- When `assignedUserMode=PROVIDED`, events that are assigned to the users specified in
  `assignedUsers` will be returned. `assignedUsers` is required for this mode.

### `*.parameter.TrackerEventRequestParams.assignedUsers`

`<user1-uid>[,<user2-uid>...]`

Get tracker events that are assigned to the given user(s). Specifying `assignedUsers` is only valid
if `assignedUserMode` is either `PROVIDED` or not specified.

### `*.parameter.TrackerEventRequestParams.occurredAfter`

Get tracker events that occurred after given date and time. This parameter is inclusive, so results
with the exact date and time specified will be included in the response.

### `*.parameter.TrackerEventRequestParams.occurredBefore`

Get tracker events that occurred before given date and time. This parameter is inclusive, so
results with the exact date and time specified will be included in the response.

### `*.parameter.TrackerEventRequestParams.scheduledAfter`

Get tracker events that are scheduled after given date and time. This parameter is inclusive, so
results with the exact date and time specified will be included in the response.

### `*.parameter.TrackerEventRequestParams.scheduledBefore`

Get tracker events that are scheduled before given date and time. This parameter is inclusive, so
results with the exact date and time specified will be included in the response.

### `*.parameter.TrackerEventRequestParams.updatedAfter`

Get tracker events updated after given date and time. This parameter is inclusive, so results with
the exact date and time specified will be included in the response. Cannot be combined with
`updatedWithin`.

### `*.parameter.TrackerEventRequestParams.updatedBefore`

Get tracker events updated before given date and time. This parameter is inclusive, so results with
the exact date and time specified will be included in the response. Cannot be combined with
`updatedWithin`.

### `*.parameter.TrackerEventRequestParams.updatedWithin`

Get tracker events updated since the given duration. The supported durations are `d` (Days), `h`
(Hours), `m` (Minutes) and `s` Seconds. For example, `updatedWithin=2d` can be used to get events
updated in the last 2 days. Cannot be combined with `updatedAfter` or `updatedBefore`.

### `*.parameter.TrackerEventRequestParams.enrollmentEnrolledAfter`

Get tracker events from enrollments with an enrollment date after given date and time. This
parameter is inclusive, so results with the exact date and time specified will be included in the
response.

### `*.parameter.TrackerEventRequestParams.enrollmentEnrolledBefore`

Get tracker events from enrollments with an enrollment date before given date and time. This
parameter is inclusive, so results with the exact date and time specified will be included in the
response.

### `*.parameter.TrackerEventRequestParams.enrollmentOccurredAfter`

Get tracker events from enrollments with an occurred date after given date and time. This parameter
is inclusive, so results with the exact date and time specified will be included in the response.

### `*.parameter.TrackerEventRequestParams.enrollmentOccurredBefore`

Get tracker events from enrollments with an occurred date before given date and time. This
parameter is inclusive, so results with the exact date and time specified will be included in the
response.

### `*.parameter.TrackerEventRequestParams.status`

Get tracker events with the specified event status.

### `*.parameter.TrackerEventRequestParams.attributeOptionCombo`

Get tracker events with the specified attribute option combination UID. The request fails with status
400 if the attribute option combination does not exist, and with status 403 if the user has no data
read access to it.

### `*.parameter.TrackerEventRequestParams.includeDeleted`

Get soft-deleted tracker events by specifying `includeDeleted=true`. Soft-deleted events are
excluded by default.

### `*.parameter.TrackerEventRequestParams.events`

`<event1-uid>[,<event2-uid>...]`

Get tracker events with specified UID(s). Cannot be combined with `filter`.

### `*.parameter.TrackerEventRequestParams.order`

`<propertyName1:sortDirection>[,<propertyName2:sortDirection>...]`

Get tracker events in the specified order. Events can be ordered by data elements and tracked
entity attributes by passing a UID instead of a property name. Events can also be ordered by the
following case-sensitive properties

* `completedAt`
* `createdAt`
* `occurredAt`
* `scheduledAt`
* `updatedAt`

Valid `sortDirection`s are `asc` and `desc`. `sortDirection` is case-insensitive. `sortDirection`
defaults to `asc` for properties or UIDs without explicit `sortDirection` as in `order=scheduledAt`.

Events are ordered by newest (internal id desc) by default, when no `order` parameter is
provided.

### `*.parameter.TrackerEventRequestParams.fields`

Get only the specified fields in the JSON response. This query parameter allows you to remove
unnecessary fields from the JSON response and in some cases decrease the response time. Refer to
https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/metadata.html#webapi_metadata_field_filter
for how to use it.

NOTE: this query parameter has no effect on a CSV response!

### `*.parameter.TrackerEventRequestParams.filter`

`<filter1>[,<filter2>...]`

Get tracker events matching the given filters on data values. A filter is a colon separated data
element UID with optional operator and value pairs. Cannot be combined with `events`.

We differentiate between two types of operators: unary and 
binary. Unary operators don't require a value, while binary operators do.
- Unary: `filter=H9IlTX2X6SL:null`
- Binary: `filter=H9IlTX2X6SL:sw:A` 

Special characters like `+` must be percent-encoded (`%2B` instead of `+`). Characters like `:` and
`,` in filter values must be escaped with `/`. Likewise, `/` needs to be escaped.
Multiple operators are allowed for the same data element, e.g.,
`filter=AuPLng5hLbE:gt:438901703:lt:448901704`. Operators and values are
case-insensitive. A user needs metadata read access to the data element and data read access to the
program stage.

All of the following operators are supported regardless of the value type. Values are compared using
text comparison unless stated otherwise. Integer and decimal value types are treated as Postgres
integer and numeric data types for the specified operators.

Valid binary operators are:
- `eq` - equal to (uses integer/numeric semantics for integer/decimal value types)
- `ieq` - equal to
- `ge` - greater than or equal to (uses integer/number semantics for integer/decimal value types)
- `gt` - greater than (uses integer/number semantics for integer/decimal value types)
- `le` - less than or equal to (uses integer/number semantics for integer/decimal value types)
- `lt` - less than (uses integer/number semantics for integer/decimal value types)
- `ne` - not equal to (uses integer/number semantics for integer/decimal value types)
- `neq` - not equal to (uses integer/number semantics for integer/decimal value types)
- `nieq` - not equal to
- `in` - equal to one of the multiple values separated by semicolon ";" (uses integer/number semantics for integer/decimal value types)
- `ilike` - is like
- `like` - like (free text match)
- `nilike` - not like
- `nlike` - not like
- `sw` - starts with
- `ew` - ends with

Right now all matches are case-insensitive so for example `eq` and `ieq` (`i` for `insensitive`)
behave in the same way.

Valid unary operators are:
- `null` - has no value
- `!null` - has a value

### `*.parameter.TrackerEventRequestParams.filterAttributes`

`<filter1>[,<filter2>...]`

Get tracker events matching the given filters on tracked entity attributes. A filter is a colon
separated attribute UID with optional operator and value pairs. 

We differentiate between two types of
operators: unary and binary. Unary operators don't require a value, while binary operators do.
- Unary: `filterAttributes=H9IlTX2X6SL:null`
- Binary: `filterAttributes=H9IlTX2X6SL:sw:A` 

Special characters like `+` must be percent-encoded (`%2B` instead of `+`). Characters like `:` and 
`,` in filter values must be escaped with `/`. Likewise, `/` needs to be escaped.
Multiple operators are allowed for the same attribute, e.g.,
`filterAttributes=AuPLng5hLbE:gt:438901703:lt:448901704`. Operators and values are
case-insensitive. The attribute must belong to the `program` or its tracked entity type. Operators
blocked on the attribute cannot be used, and a filter value must be at least as long as the
attribute's minimum number of characters to search.

All of the following operators are supported regardless of the value type. Values are compared using
text comparison unless stated otherwise. Integer and decimal value types are treated as Postgres
integer and numeric data types for the specified operators.

Valid binary operators are:
- `eq` - equal to (uses integer/numeric semantics for integer/decimal value types)
- `ieq` - equal to
- `ge` - greater than or equal to (uses integer/number semantics for integer/decimal value types)
- `gt` - greater than (uses integer/number semantics for integer/decimal value types)
- `le` - less than or equal to (uses integer/number semantics for integer/decimal value types)
- `lt` - less than (uses integer/number semantics for integer/decimal value types)
- `ne` - not equal to (uses integer/number semantics for integer/decimal value types)
- `neq` - not equal to (uses integer/number semantics for integer/decimal value types)
- `nieq` - not equal to
- `in` - equal to one of the multiple values separated by semicolon ";" (uses integer/number semantics for integer/decimal value types)
- `ilike` - is like
- `like` - like (free text match)
- `nilike` - not like
- `nlike` - not like
- `sw` - starts with
- `ew` - ends with

Right now all matches are case-insensitive so for example `eq` and `ieq` (`i` for `insensitive`)
behave in the same way.

Valid unary operators are:
- `null` - has no value
- `!null` - has a value
