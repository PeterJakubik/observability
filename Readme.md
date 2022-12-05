# Springboot 3 and grafana observability
Aim is to check the Springboot 3 observability with grafana & loki.
Later activate the OpenTelemetry with grafana tempo and correlate the logs with telemetry 

## Loki
```shell
curl -v -H "Content-Type: application/json" -XPOST -s "http://localhost:3100/loki/api/v1/push" --data-raw '{"streams": [{ "stream": { "foo": "bar2" }, "values": [ [ "1669828239000000000", "fizzbuzz" ] ] }]}'
```


## Grafana
Query
{app="observabilityserver"}