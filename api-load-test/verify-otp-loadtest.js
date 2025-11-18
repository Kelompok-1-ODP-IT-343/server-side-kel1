import http from 'k6/http';
import { check, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.4/index.js';

// ============== ENV & DEFAULTS ==============
const HOST_RAW    = __ENV.HOST || 'http://localhost:18080';
const HOST        = HOST_RAW.replace(/\/+$/,'');
// default ganti ke verify-login-otp biar match controller-mu
const VERIFY_PATH = (__ENV.VERIFY_PATH || '/api/v1/auth/verify-login-otp').startsWith('/')
    ? (__ENV.VERIFY_PATH || '/api/v1/auth/verify-login-otp')
    : `/${__ENV.VERIFY_PATH}`;

const PURPOSE   = __ENV.PURPOSE || 'login';
const THINK_MS  = Number(__ENV.THINK_MS || 0);       // default 0ms biar lebih “keras”
const LOG_FAIL  = String(__ENV.LOG_FAIL || 'false').toLowerCase() === 'true';

// Env buat load profile “keras”
const RPS       = Number(__ENV.RPS || 100);          // target request per second
const DURATION  = __ENV.DURATION || '60s';           // lama tes
const VUS       = Number(__ENV.VUS || 20);           // VU awal
const MAX_VUS   = Number(__ENV.MAX_VUS || 100);      // VU maksimum

// daftar user & OTP (comma-separated)
const IDENTIFIERS = (__ENV.IDENTIFIERS || 'testingiano,testingiano3,testkpr')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);

const OTPS = (__ENV.OTPS || '000000')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);

// ============== HELPERS ==============
function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

// ============== OPTIONS: CONSTANT ARRIVAL RATE ==============
export const options = {
    scenarios: {
        hard_verify_otp: {
            executor: 'constant-arrival-rate',
            rate: RPS,            // berapa request per detik
            timeUnit: '1s',
            duration: DURATION,
            preAllocatedVUs: VUS,
            maxVUs: MAX_VUS,
            exec: 'default',
            tags: { scenario: 'hard_verify_otp' },
        },
    },
    thresholds: {
        // pakai metric bawaan saja
        http_req_failed: ['rate<0.01'],          // kurang dari 1% gagal
        http_req_duration: ['p(95)<300'],        // 95% request < 300ms
    },
    discardResponseBodies: false,
    // @ts-ignore
    http: { timeout: '60s' },
};

// ============== SCENARIO ==============
export default function () {
    const identifier = pick(IDENTIFIERS);
    const otp = pick(OTPS);

    const url = `${HOST}${VERIFY_PATH}`;
    const payload = JSON.stringify({ identifier, otp, purpose: PURPOSE });

    const res = http.post(url, payload, {
        headers: {
            'Content-Type': 'application/json',
            'X-Load-Test': 'verify-login-otp',
        },
        timeout: '60s',
        tags: { endpoint: VERIFY_PATH },
    });

    const ok = check(res, {
        'status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    if (!ok && LOG_FAIL) {
        console.error(
            `FAIL ${res.status} id=${identifier} otp=${otp} body=${res.body}`,
        );
    }

    if (THINK_MS > 0) {
        sleep(THINK_MS / 1000);
    }
}

// ============== SUMMARY (HTML + console) ==============
export function handleSummary(data) {
    const dur = data.metrics.http_req_duration?.values || {};
    const failedRate = data.metrics.http_req_failed?.values?.rate || 0;

    // total request pakai metric bawaan http_reqs
    const totalReq = data.metrics.http_reqs?.values?.count || 0;

    const html = `
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Verify OTP Load Test Report</title>
  <style>
    body { font-family: system-ui, -apple-system, sans-serif; padding: 24px; background:#f5f5f5; }
    h1 { margin-bottom: 0.2rem; }
    .card { background:#fff; border-radius:8px; padding:16px 20px; margin-bottom:16px; box-shadow:0 2px 6px rgba(0,0,0,0.06); }
    table { border-collapse: collapse; width: 100%; margin-top: 8px; }
    th, td { border: 1px solid #ddd; padding: 8px 10px; text-align: left; }
    th { background:#fafafa; }
    code { background:#eee; padding:2px 4px; border-radius:4px; }
  </style>
</head>
<body>
  <h1>Verify OTP Load Test Report</h1>
  <p><strong>Target:</strong> <code>${HOST}${VERIFY_PATH}</code></p>

  <div class="card">
    <h2>Summary</h2>
    <table>
      <tr><th>Purpose</th><td>${PURPOSE}</td></tr>
      <tr><th>Total Requests</th><td>${totalReq}</td></tr>
      <tr><th>Failed Rate</th><td>${(failedRate * 100).toFixed(2)} %</td></tr>
      <tr><th>p50</th><td>${dur['p(50)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
      <tr><th>p95</th><td>${dur['p(95)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
      <tr><th>p99</th><td>${dur['p(99)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
    </table>
  </div>

  <div class="card">
    <h2>Config</h2>
    <table>
      <tr><th>HOST</th><td>${HOST}</td></tr>
      <tr><th>VERIFY_PATH</th><td>${VERIFY_PATH}</td></tr>
      <tr><th>IDENTIFIERS</th><td>${IDENTIFIERS.join(', ')}</td></tr>
      <tr><th>OTPS</th><td>${OTPS.join(', ')}</td></tr>
      <tr><th>THINK TIME</th><td>${THINK_MS} ms</td></tr>
      <tr><th>RPS</th><td>${RPS}</td></tr>
      <tr><th>DURATION</th><td>${DURATION}</td></tr>
      <tr><th>VUS / MAX_VUS</th><td>${VUS} / ${MAX_VUS}</td></tr>
      <tr><th>LOG_FAIL</th><td>${LOG_FAIL}</td></tr>
    </table>
  </div>
</body>
</html>
`;

    return {
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
        'verify-otp-summary.html': html,
    };
}
