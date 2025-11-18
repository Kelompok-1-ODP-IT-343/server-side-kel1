// properties-loadtest.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// ============== CONFIG VIA ENV =================
const HOST       = __ENV.HOST || 'http://localhost:18080';       // wajib ada skema
const BASE_PATH  = (__ENV.BASE_PATH || '').replace(/\/+$/,'');   // contoh: /api/v1
const SCENARIO   = (__ENV.K6_SCENARIO || 'constant').toLowerCase();
const DURATION   = __ENV.DURATION || '5m';
const VUS        = Number(__ENV.VUS || 50);
const RPS        = Number(__ENV.RPS || 100);
const THINK_MS   = Number(__ENV.THINK_MS || 250);
const LOG_FAIL   = String(__ENV.LOG_FAIL || 'false').toLowerCase() === 'true';
const SHOW_MSG   = String(__ENV.SHOW_MSG || 'false').toLowerCase() === 'true';

const CITY_LIST  = (__ENV.CITIES || 'Jakarta Selatan,Jakarta,Bandung,Surabaya')
    .split(',').map(s => s.trim()).filter(Boolean);

// default kecil sesuai contoh Postman-mu (lowercase)
const TYPES_LIST = (__ENV.TYPES || 'rumah,apartemen,ruko')
    .split(',').map(s => s.trim()).filter(Boolean);

// Harga (hindari out-of-range aneh)
const MIN_PRICE_MIN = Number(__ENV.MIN_PRICE_MIN || 100_000_000);
const MIN_PRICE_MAX = Number(__ENV.MIN_PRICE_MAX || 500_000_000);
const MAX_PRICE_MIN = Number(__ENV.MAX_PRICE_MIN || 600_000_000);
const MAX_PRICE_MAX = Number(__ENV.MAX_PRICE_MAX || 2_000_000_000);

// Pagination
const DEFAULT_LIMIT = Number(__ENV.LIMIT || 10);
const MAX_OFFSET    = Number(__ENV.MAX_OFFSET || 1000);

const INSECURE   = String(__ENV.INSECURE || 'false').toLowerCase() === 'true';

// ============== METRICS & THRESHOLDS ===========
const durationTrend = new Trend('req_duration', true);
const okRate        = new Rate('ok_rate');
const errCount      = new Counter('errors');

// breakdown status
const status2xx = new Counter('status_2xx');
const status4xx = new Counter('status_4xx');
const status5xx = new Counter('status_5xx');

function scenarioConfig() {
    switch (SCENARIO) {
        case 'smoke':
            return {
                scenarios: {
                    smoke: {
                        executor: 'ramping-vus',
                        stages: [
                            { duration: '10s', target: 1 },
                            { duration: '40s', target: 1 },
                            { duration: '10s', target: 0 },
                        ],
                        exec: 'testFlow',
                        gracefulRampDown: '5s',
                        tags: { scenario: 'smoke' },
                    },
                },
                thresholds: {
                    http_req_failed: ['rate<0.05'],
                    http_req_duration: ['p(95)<1500'],
                    ok_rate: ['rate>0.95'],
                },
            };

        case 'ramp':
            return {
                scenarios: {
                    ramp: {
                        executor: 'ramping-arrival-rate',
                        startRate: Math.max(1, Math.floor(RPS / 10)),
                        timeUnit: '1s',
                        preAllocatedVUs: Math.max(VUS, 50),
                        maxVUs: Math.max(VUS * 2, 200),
                        stages: [
                            { target: Math.max(10, Math.floor(RPS / 2)), duration: '1m' },
                            { target: RPS, duration: '2m' },
                            { target: Math.max(10, Math.floor(RPS / 4)), duration: '1m' },
                        ],
                        exec: 'testFlow',
                        tags: { scenario: 'ramp' },
                    },
                },
                thresholds: {
                    http_req_failed: ['rate<0.02'],
                    http_req_duration: ['p(95)<900', 'p(99)<1500'],
                    ok_rate: ['rate>0.98'],
                },
            };

        case 'spike':
            return {
                scenarios: {
                    spike: {
                        executor: 'ramping-vus',
                        stages: [
                            { duration: '20s', target: 10 },
                            { duration: '10s', target: VUS || 300 },
                            { duration: '40s', target: 10 },
                            { duration: '10s', target: 0 },
                        ],
                        exec: 'testFlow',
                        tags: { scenario: 'spike' },
                    },
                },
                thresholds: {
                    http_req_failed: ['rate<0.03'],
                    http_req_duration: ['p(95)<1200', 'p(99)<2500'],
                    ok_rate: ['rate>0.97'],
                },
            };

        case 'soak':
            return {
                scenarios: {
                    soak: {
                        executor: 'constant-vus',
                        vus: VUS || 30,
                        duration: DURATION || '30m',
                        exec: 'testFlow',
                        tags: { scenario: 'soak' },
                    },
                },
                thresholds: {
                    http_req_failed: ['rate<0.01'],
                    http_req_duration: ['p(95)<1000'],
                    ok_rate: ['rate>0.99'],
                },
            };

        case 'constant':
        default:
            return {
                scenarios: {
                    steady: {
                        executor: 'constant-arrival-rate',
                        rate: RPS || 100,
                        timeUnit: '1s',
                        duration: DURATION || '5m',
                        preAllocatedVUs: Math.max(VUS, 100),
                        maxVUs: Math.max(VUS * 2, 300),
                        exec: 'testFlow',
                        tags: { scenario: 'constant' },
                    },
                },
                thresholds: {
                    http_req_failed: ['rate<0.01'],
                    http_req_duration: ['p(95)<800', 'p(99)<1500'],
                    ok_rate: ['rate>0.99'],
                    'http_reqs{status:500}': ['count==0'],
                    'http_reqs{status:429}': ['count==0'],
                },
            };
    }
}

export const options = {
    discardResponseBodies: false,
    insecureSkipTLSVerify: INSECURE,
    // @ts-ignore
    http: { timeout: '30s' },
    ...scenarioConfig(),
};

// ============== HELPERS =======================
function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + Number(min);
}
function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }

function buildQuery(params) {
    const qs = Object.entries(params)
        .filter(([_, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
    return qs ? `?${qs}` : '';
}

function extractMessage(res) {
    try {
        const body = res.json();
        return body?.message || body?.msg || body?.status || body?.error || '';
    } catch {
        return '';
    }
}

// ============== SETUP =========================
export function setup() {
    console.log(
        `TARGET → ${HOST}${BASE_PATH}/properties | scenario=${SCENARIO} | RPS=${RPS} | VUS=${VUS} | DURATION=${DURATION}`,
    );
    return { authHeader: {} }; // tanpa Authorization
}

// ============== MAIN FLOW =====================
export function testFlow(data) {
    const USE_FORM = String(__ENV.USE_FORM || 'false').toLowerCase() === 'true';

    const headers = {
        ...(data?.authHeader || {}),
        Accept: 'application/json',
    };

    const city         = pick(CITY_LIST);
    const minPrice     = randInt(MIN_PRICE_MIN, MIN_PRICE_MAX);
    const maxPrice     = randInt(Math.max(minPrice + 1, MAX_PRICE_MIN), MAX_PRICE_MAX);
    const propertyType = pick(TYPES_LIST); // contoh: rumah|apartemen|ruko
    const limit        = DEFAULT_LIMIT;
    const offset       = randInt(0, MAX_OFFSET);

    let res, url;

    if (USE_FORM) {
        const bodyForm =
            `city=${encodeURIComponent(city)}` +
            `&minPrice=${encodeURIComponent(minPrice)}` +
            `&maxPrice=${encodeURIComponent(maxPrice)}` +
            `&propertyType=${encodeURIComponent(propertyType)}` +
            `&offset=${encodeURIComponent(offset)}` +
            `&limit=${encodeURIComponent(limit)}`;

        url = `${HOST.replace(/\/+$/,'')}${BASE_PATH}/properties`;
        res = http.request('GET', url, bodyForm, {
            headers: { ...headers, 'Content-Type': 'application/x-www-form-urlencoded' },
            tags: { endpoint: '/properties', city, propertyType, mode: 'form' },
        });
    } else {
        const query = buildQuery({ city, minPrice, maxPrice, propertyType, offset, limit });
        url = `${HOST.replace(/\/+$/,'')}${BASE_PATH}/properties${query}`;
        res = http.get(url, {
            headers,
            tags: { endpoint: '/properties', city, propertyType, mode: 'query' },
        });
    }

    // klasifikasi status
    if (res.status >= 200 && res.status < 300) status2xx.add(1);
    else if (res.status >= 400 && res.status < 500) status4xx.add(1);
    else if (res.status >= 500) status5xx.add(1);

    // optional log status + message
    if (SHOW_MSG) {
        const msg = extractMessage(res);
        console.log(`[${res.status}] ${msg || '(no message)'} — ${url}`);
    }

    const ok = check(res, {
        'status 2xx': r => r.status >= 200 && r.status < 300,
        'json list-ish': r => {
            try {
                const body = r.json();
                return Array.isArray(body) || Array.isArray(body?.data) || Array.isArray(body?.data?.items);
            } catch { return false; }
        },
    });

    if (!ok) {
        errCount.add(1);
        if (LOG_FAIL) {
            const msg = extractMessage(res);
            console.error(
                `FAIL ${res.status} dur=${res.timings.duration}ms msg="${msg}" url=${url} bodyLen=${(res.body || '').length}`,
            );
        }
    }

    okRate.add(ok);
    durationTrend.add(res.timings.duration);

    sleep(THINK_MS / 1000);
}

// ============== SUMMARY =======================
export function handleSummary(data) {
    const d = data.metrics.http_req_duration?.values || {};
    const failedRate = data.metrics.http_req_failed?.values?.rate || 0;

    const s2 = data.metrics.status_2xx?.values?.count || 0;
    const s4 = data.metrics.status_4xx?.values?.count || 0;
    const s5 = data.metrics.status_5xx?.values?.count || 0;

    const errors = data.metrics.errors?.values?.count || 0;

    const text = [
        '=== Properties Load Test Summary ===\n',
        `Host       : ${HOST}${BASE_PATH}\n`,
        `Scenario   : ${SCENARIO}\n`,
        `Duration   : ${DURATION}\n`,
        `RPS/VUs    : ${RPS || '-'} / ${VUS}\n`,
        `Cities     : ${CITY_LIST.join(', ')}\n`,
        `Types      : ${TYPES_LIST.join(', ')}\n`,
        `FailedRate : ${(failedRate * 100).toFixed(2)}%\n`,
        `2xx/4xx/5xx: ${s2}/${s4}/${s5}\n`,
        `p50        : ${d['p(50)']?.toFixed?.(2) || 'n/a'} ms\n`,
        `p95        : ${d['p(95)']?.toFixed?.(2) || 'n/a'} ms\n`,
        `p99        : ${d['p(99)']?.toFixed?.(2) || 'n/a'} ms\n`,
        `Errors     : ${errors}\n`,
        '====================================\n',
    ].join('');

    const html = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Properties Load Test Report</title>
  <style>
    body {
      font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      margin: 24px;
      background: #f5f5f5;
      color: #222;
    }
    h1, h2 {
      margin-bottom: 0.4rem;
    }
    .card {
      background: #fff;
      padding: 16px 20px;
      border-radius: 10px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.06);
      margin-bottom: 16px;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px 24px;
    }
    .label {
      font-size: 0.85rem;
      color: #666;
    }
    .value {
      font-weight: 600;
      font-size: 0.95rem;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 8px;
      font-size: 0.9rem;
    }
    th, td {
      border: 1px solid #ddd;
      padding: 8px 10px;
      text-align: left;
    }
    th {
      background: #f0f0f0;
    }
    .status-ok {
      color: #1a7f37;
      font-weight: 600;
    }
    .status-bad {
      color: #b42318;
      font-weight: 600;
    }
    .mono {
      font-family: "SF Mono", Menlo, Monaco, Consolas, "Liberation Mono", monospace;
      font-size: 0.85rem;
    }
  </style>
</head>
<body>
  <h1>Properties Load Test Report</h1>

  <div class="card">
    <h2>Test Configuration</h2>
    <div class="grid">
      <div>
        <div class="label">Target Host</div>
        <div class="value mono">${HOST}${BASE_PATH}/properties</div>
      </div>
      <div>
        <div class="label">Scenario</div>
        <div class="value">${SCENARIO}</div>
      </div>
      <div>
        <div class="label">Duration</div>
        <div class="value">${DURATION}</div>
      </div>
      <div>
        <div class="label">RPS / VUs</div>
        <div class="value">${RPS || '-'} / ${VUS}</div>
      </div>
      <div>
        <div class="label">Cities</div>
        <div class="value">${CITY_LIST.join(', ')}</div>
      </div>
      <div>
        <div class="label">Types</div>
        <div class="value">${TYPES_LIST.join(', ')}</div>
      </div>
    </div>
  </div>

  <div class="card">
    <h2>High-level Metrics</h2>
    <table>
      <tr>
        <th>Metric</th>
        <th>Value</th>
      </tr>
      <tr>
        <td>Failed Rate (http_req_failed)</td>
        <td class="${failedRate <= 0.01 ? 'status-ok' : 'status-bad'}">
          ${(failedRate * 100).toFixed(2)}%
        </td>
      </tr>
      <tr>
        <td>Status Count (2xx / 4xx / 5xx)</td>
        <td>${s2} / ${s4} / ${s5}</td>
      </tr>
      <tr>
        <td>Latency p50</td>
        <td>${d['p(50)']?.toFixed?.(2) || 'n/a'} ms</td>
      </tr>
      <tr>
        <td>Latency p95</td>
        <td>${d['p(95)']?.toFixed?.(2) || 'n/a'} ms</td>
      </tr>
      <tr>
        <td>Latency p99</td>
        <td>${d['p(99)']?.toFixed?.(2) || 'n/a'} ms</td>
      </tr>
      <tr>
        <td>Total Errors (custom)</td>
        <td>${errors}</td>
      </tr>
    </table>
  </div>

  <div class="card">
    <h2>Raw k6 Metrics Snapshot</h2>
    <p class="label">Potongan ini hanya ringkasan, data lengkap ada di <span class="mono">summary.json</span>.</p>
    <pre class="mono">${JSON.stringify({
        http_req_duration: data.metrics.http_req_duration,
        http_req_failed: data.metrics.http_req_failed,
        checks: data.metrics.checks,
    }, null, 2)}</pre>
  </div>

</body>
</html>
`;

    // k6 akan generate 3 output:
    return {
        stdout: text,                      // tetap tampil di terminal
        'summary.json': JSON.stringify(data, null, 2), // buat analisis lanjut
        'report.html': html,              // ini yang kamu buka / convert ke PDF
    };
}