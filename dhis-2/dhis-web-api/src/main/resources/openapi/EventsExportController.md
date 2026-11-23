# Get Events

## Specific endpoints

### `getEvents`

Get events matching given query parameters.

The `program` parameter is required. Events of programs with registration (tracker programs) and
programs without registration (event programs) are exported by this endpoint, but some parameters
only apply to one of them. Parameters that only apply to programs with registration are ignored
for programs without registration. Their descriptions say so.

Organisation unit scoping (`orgUnit` and `orgUnitMode`) depends on the type of program:

- For programs with registration, the event is scoped by the organisation unit that owns the
  tracked entity in the `program`.
- For programs without registration, the event is scoped by the organisation unit of the event.

### `getEventsAsCsv`

Get events matching given query parameters as CSV. Accepts the same parameters as `getEvents`.
Use `skipHeader` to omit the header row.

### `getEventsAsCsvGZip`

Get events matching given query parameters as gzip compressed CSV. Accepts the same parameters as
`getEvents`. Use `skipHeader` to omit the header row.

### `getEventsAsCsvZip`

Get events matching given query parameters as zip compressed CSV. Accepts the same parameters as
`getEvents`. Use `skipHeader` to omit the header row.

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

Get an event with the specified UID.

### `getEventByUid.parameter.uid`

Get an event with the specified UID.

### `getEventByUid.parameter.fields`

Get only the specified fields in the JSON response. This query parameter allows you to remove
unnecessary fields from the response and in some cases decrease the response time. Refer to
https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/metadata.html#webapi_metadata_field_filter
for how to use it.

### `getEventDataValueFile`

Get an event data value file or image for the specified event and data element UID. Images are
returned in their original dimension. Use `getEventDataValueImage` to get an image in a different
dimension.

### `getEventDataValueImage`

Get an event data value image for the specified event and data element UID. Images are returned in
their original dimension by default. This endpoint is only supported for data elements of value
type image.

### `getEventDataValueImage.parameter.dimension`

Get the image in the specified dimension. The image is returned in its original dimension when no
`dimension` is given.

### `getEventChangeLogsByUid`

Get the change logs of all data elements and event fields belonging to the specified event UID.
Change logs are ordered by newest (`createdAt` desc) by default.

Change logs can be ordered using `order` by the following case-sensitive properties

* `change`
* `createdAt`
* `username`

Change logs can be filtered using `filter=<propertyName>:eq:<value>`. Only one filter can be
specified, and the only supported operator is `eq`. Change logs can be filtered by the following
case-sensitive properties

* `dataElement` - the UID of a data element
* `field` - the name of an event field like `occurredAt`, `scheduledAt` or `geometry`
* `username` - the username of the user who made the change

## Common for all endpoints

### `*.parameter.event`

The UID of the event.

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

### `*.parameter.EventRequestParams.program`

Get events from the specified program. Required.

### `*.parameter.EventRequestParams.programStage`

Get events from the specified program stage.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.enrollmentStatus`

Get events from enrollments that have the specified enrollment status.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.programStatus`

Get events from enrollments that have the specified enrollment status.

**DEPRECATED as of 2.42:** Use parameter `enrollmentStatus` instead.

See `enrollmentStatus` for details.

### `*.parameter.EventRequestParams.followUp`

Get events with specified follow-up status. When set to true, only events from enrollments marked
for follow-up will be returned. When set to false, only events from enrollments not marked for
follow-up will be returned.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.trackedEntity`

Get events belonging to the tracked entity with the specified UID.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.enrollments`

`<enrollment1-uid>[,<enrollment2-uid>...]`

Get events belonging to the enrollments with the specified UID(s).

Only applies to programs with registration.

### `*.parameter.EventRequestParams.orgUnit`

Get events belonging to the specified `orgUnit` relative to the `orgUnitMode`. For programs with
registration, the event belongs to the organisation unit that owns the tracked entity in the
`program`. For programs without registration, the event belongs to its own organisation unit.

`orgUnit` cannot be used with `orgUnitMode` `ALL`, `ACCESSIBLE` or `CAPTURE`.

### `*.parameter.EventRequestParams.orgUnitMode`

Get events belonging to the organisation units referenced by the specified organisation unit
mode. Defaults to `SELECTED` when an `orgUnit` is given, and to `ACCESSIBLE` otherwise.

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

### `*.parameter.EventRequestParams.assignedUserMode`

Get events assigned to users according to the specified user mode. By default,
all events will be retrieved, regardless of whether a user is assigned.

- When `assignedUserMode=ALL` or no `assignedUserMode`(default) is given, all events are
  returned irrespective of whether they are assigned to a user or not.

- When `assignedUserMode=CURRENT`, events that are assigned to the logged-in user will be returned.

- When `assignedUserMode=ANY`, events that are assigned to any user will be returned.

- When `assignedUserMode=NONE`, events that are not assigned to any user will be returned.

- When `assignedUserMode=PROVIDED`, events that are assigned to the users specified in 
  `assignedUsers` will be returned. `assignedUsers` is required for this mode.

### `*.parameter.EventRequestParams.assignedUsers`

`<user1-uid>[,<user2-uid>...]`

Get events that are assigned to the given user(s). Specifying `assignedUsers` is only valid
if `assignedUserMode` is either `PROVIDED` or not specified.

### `*.parameter.EventRequestParams.occurredAfter`

Get events that occurred after given date and time. This parameter is inclusive, so results with 
the exact date and time specified will be included in the response.

### `*.parameter.EventRequestParams.occurredBefore`

Get events that occurred before given date and time. This parameter is inclusive, so results 
with the exact date and time specified will be included in the response.

### `*.parameter.EventRequestParams.scheduledAfter`

Get events that are scheduled after given date and time. This parameter is inclusive, so 
results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.scheduledBefore`

Get events that are scheduled before given date and time. This parameter is inclusive, so 
results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.updatedAfter`

Get events updated after given date and time. This parameter is inclusive, so results with the 
exact date and time specified will be included in the response. Cannot be combined with
`updatedWithin`.

### `*.parameter.EventRequestParams.updatedBefore`

Get events updated before given date and time. This parameter is inclusive, so results with the
exact date and time specified will be included in the response. Cannot be combined with
`updatedWithin`.

### `*.parameter.EventRequestParams.updatedWithin`

Get events updated since the given duration. The supported durations are `d` (Days), `h` (Hours), 
`m` (Minutes) and `s` Seconds. For example, `updatedWithin=2d` can be used to get events updated in 
the last 2 days. Cannot be combined with `updatedAfter` or `updatedBefore`.

### `*.parameter.EventRequestParams.enrollmentEnrolledAfter`

Get events from enrollments with an enrollment date after given date and time. This parameter is
inclusive, so results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.enrollmentEnrolledBefore`

Get events from enrollments with an enrollment date before given date and time. This parameter is
inclusive, so results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.enrollmentOccurredAfter`

Get events from enrollments with an occurred date after given date and time. This parameter is
inclusive, so results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.enrollmentOccurredBefore`

Get events from enrollments with an occurred date before given date and time. This parameter is
inclusive, so results with the exact date and time specified will be included in the response.

Only applies to programs with registration.

### `*.parameter.EventRequestParams.status`

Get events with the specified event status.

### `*.parameter.EventRequestParams.attributeCategoryCombo`

Get events with the attribute option combination given by this attribute category combination
UID and the `attributeCategoryOptions`. `attributeCategoryCombo` and `attributeCategoryOptions`
must be specified together.

### `*.parameter.EventRequestParams.attributeCategoryOptions`

`<attributeCategoryOption1-uid>[,<attributeCategoryOption2-uid>...]`

Get events with the attribute option combination given by these attribute category option UIDs
and the `attributeCategoryCombo`. `attributeCategoryCombo` and `attributeCategoryOptions` must be
specified together.

### `*.parameter.EventRequestParams.includeDeleted`

Get soft-deleted events by specifying `includeDeleted=true`. Soft-deleted events are excluded by
default.

### `*.parameter.EventRequestParams.events`

`<event1-uid>[,<event2-uid>...]`

Get events with specified UID(s). Cannot be combined with `filter`.

### `*.parameter.EventRequestParams.order`

`<propertyName1:sortDirection>[,<propertyName2:sortDirection>...]`

Get events in the specified order. Events can be ordered by data elements and, for programs with
registration, tracked entity attributes by passing a UID instead of a property name. Events can
also be ordered by the following case-sensitive properties

* `assignedUser`
* `assignedUserDisplayName`
* `attributeOptionCombo`
* `completedAt`
* `completedBy`
* `createdAt`
* `createdAtClient`
* `createdBy`
* `deleted`
* `enrolledAt` (only programs with registration)
* `enrollment` (only programs with registration)
* `enrollmentStatus` (only programs with registration)
* `event`
* `followUp` (only programs with registration)
* `occurredAt`
* `orgUnit`
* `program`
* `programStage` (only programs with registration)
* `scheduledAt` (only programs with registration)
* `status`
* `trackedEntity` (only programs with registration)
* `updatedAt`
* `updatedAtClient`
* `updatedBy`

Valid `sortDirection`s are `asc` and `desc`. `sortDirection` is case-insensitive. `sortDirection`
defaults to `asc` for properties or UIDs without explicit `sortDirection` as in `order=scheduledAt`.

Events are ordered by newest (internal id desc) by default, when no `order` parameter is
provided.

### `*.parameter.EventRequestParams.fields`

Get only the specified fields in the JSON response. This query parameter allows you to remove
unnecessary fields from the JSON response and in some cases decrease the response time. Refer to
https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/metadata.html#webapi_metadata_field_filter
for how to use it.

NOTE: this query parameter has no effect on a CSV response!

### `*.parameter.EventRequestParams.filter`

`<filter1>[,<filter2>...]`

Get events matching the given filters on data values. A filter is a colon separated data element UID
with optional operator and value pairs. Cannot be combined with `events`.

We differentiate between two types of operators: unary and 
binary. Unary operators don't require a value, while binary operators do.
- Unary: `filter=H9IlTX2X6SL:null`
- Binary: `filter=H9IlTX2X6SL:sw:A` 

Special characters like `+` must be percent-encoded (`%2B` instead of `+`). Characters like `:` and
`,` in filter values must be escaped with `/`. Likewise, `/` needs to be escaped.
Multiple operators are allowed for the same data element, e.g.,
`filter=AuPLng5hLbE:gt:438901703:lt:448901704`. Operators and values are
case-insensitive. A user needs metadata read access to the data element and data read access to the
program (if the program is without registration) or the program stage (if the program is with
registration).

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

### `*.parameter.EventRequestParams.filterAttributes`

`<filter1>[,<filter2>...]`

Get events matching the given filters on tracked entity attributes. A filter is a colon separated 
attribute UID with optional operator and value pairs. 

Only applies to programs with registration.

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
