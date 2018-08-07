# Introduction to artstor-log-service

This service can log any UI events from the front end so they can be tracked via Ithaka's Captains log

Requirements:
Requires Ithaka's sequoia libraries for logging and platform infrastructure.
However,it can be forked to use similar back-end.

Log message needs to be a json of syntax
{
  "description": "string",
  "additional_fields": {"key1": "value1", "key2" : "value2"},
  "eventType": "string",
  "item_id": "string",
  "referring_requestid": "string",
  "reason": "string",
  "origin": "string",
  "status_code": "string",
  "uri": "string"
}