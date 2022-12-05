#
## Loki
```shell
curl -v -H "Content-Type: application/json" -XPOST -s "http://localhost:3100/loki/api/v1/push" --data-raw '{"streams": [{ "stream": { "foo": "bar2" }, "values": [ [ "1669828239000000000", "fizzbuzz" ] ] }]}'
```


## Grafana
Query
{app="observabilityserver"}