# Introduction to artstor-log-service

This service can log any UI events from the front end so they can be tracked via Captains log

    POST /api/v1/log  Logs the provided message in the Captains log

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