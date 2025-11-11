# K6 Load Test for `/properties` Endpoint

This project contains a K6 load test script to evaluate the performance of the `/properties` API endpoint.  
The test supports two request modes:

1. **Query String** (default)
2. **GET with Form Body** (`USE_FORM=true`) — used when the backend expects `x-www-form-urlencoded` parameters, similar to Postman.

## Requirements
- Node.js (optional, only if modifying scripts with tooling)
- **k6** installed locally  
  **MacOS installation:**
  ```bash
  brew install k6
  ```

Alternatively, run via Docker:
```bash
docker run --rm -i grafana/k6:latest version
```

## Script Overview
Main test script:
```
properties-loadtest.js
```

The test sends randomized requests varying:
- `city`
- `propertyType`
- `minPrice`
- `maxPrice`
- `offset`
- `limit`

## Environment Variables

| Variable        | Default                                  | Description |
|----------------|------------------------------------------|-------------|
| `HOST`         | `http://localhost:18080`                 | Base API host |
| `BASE_PATH`    | *(empty)*                                | Optional API prefix (example: `/api/v1`) |
| `K6_SCENARIO`  | `constant`                                | Test mode: `smoke`, `ramp`, `constant`, `spike`, `soak` |
| `RPS`          | `100`                                     | Requests per second (for `constant-arrival-rate`) |
| `DURATION`     | `5m`                                      | Test duration |
| `CITIES`       | `Jakarta Selatan,Jakarta,Bandung,Surabaya`| Comma-separated city list |
| `TYPES`        | `rumah,apartemen,ruko`                    | Property types |
| `MAX_OFFSET`   | `1000`                                    | Maximum offset |
| `LIMIT`        | `10`                                      | Request page size |
| `USE_FORM`     | `false`                                   | `true` → send parameters in body form (`x-www-form-urlencoded`) |
| `LOG_FAIL`     | `false`                                   | Print failed requests for debugging |

## Example: Constant Load (RPS-based)

```bash
k6 run \
  -e HOST=http://localhost:18080 \
  -e BASE_PATH=/api/v1 \
  -e K6_SCENARIO=constant \
  -e RPS=5 \
  -e DURATION=30s \
  properties-loadtest.js
```

## Example: Run a Fixed Number of Requests

```bash
k6 run --vus 1 --iterations 50 properties-loadtest.js
```

This will execute exactly **50 requests** total.

## Example: Use Form Body (If Backend Requires Form URL Encoded)

```bash
k6 run \
  -e HOST=http://localhost:18080 \
  -e BASE_PATH=/api/v1 \
  -e USE_FORM=true \
  -e RPS=5 \
  -e DURATION=30s \
  properties-loadtest.js
```

## Running via Docker

```bash
docker run --rm -i \
  -v "$PWD":/scripts -w /scripts grafana/k6:latest run \
  -e HOST=http://host.docker.internal:18080 \
  -e BASE_PATH=/api/v1 \
  -e RPS=5 \
  -e DURATION=30s \
  properties-loadtest.js
```

## Output Summary
The script produces:
- Console summary (latency percentiles, throughput, error rate)
- `summary.json` (full metrics for dashboard / analysis)

## Result Interpretation (Key Metrics)

| Metric | Meaning |
|--------|---------|
| `FailedRate` | Percentage of requests with non-2xx status codes |
| `p95` | 95th percentile latency (most important indicator under load) |
| `Errors` | Number of validation failures in test logic |

> If thresholds are crossed, the test will exit with a non-zero result, indicating performance targets were not met.

## Notes
- If the API becomes slow, increase DB indexing, optimize queries, or tune connection pooling.
- Threshold failures mean performance targets are unmet — not that the API is broken.

##  Run verify-otp-loadtest.js
- k6 run --vus 10 --duration 10s verify-otp-loadtest.js
OR 
- k6 run \
  -e HOST=http://localhost:18080 \
  -e VERIFY_PATH=/api/v1/auth/verify-otp \
  -e IDENTIFIERS="testingiano" \
  -e OTPS="000000" \
  -e LOG_FAIL=true \
  verify-otp-loadtest.js
