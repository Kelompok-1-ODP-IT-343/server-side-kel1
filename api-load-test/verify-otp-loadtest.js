import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// ============== ENV & DEFAULTS ==============
const HOST_RAW    = __ENV.HOST || 'http://localhost:18080';
const HOST        = HOST_RAW.replace(/\/+$/,'');
const VERIFY_PATH = (__ENV.VERIFY_PATH || '/api/v1/auth/verify-otp').startsWith('/')
    ? (__ENV.VERIFY_PATH || '/api/v1/auth/verify-otp')
    : `/${__ENV.VERIFY_PATH}`;
const PURPOSE     = __ENV.PURPOSE || 'login';
const THINK_MS    = Number(__ENV.THINK_MS || 200);
const LOG_FAIL    = String(__ENV.LOG_FAIL || 'false').toLowerCase() === 'true';

// daftar user & OTP (comma-separated)
const IDENTIFIERS = (__ENV.IDENTIFIERS || 'testingiano', 'dev_nusantara', '')
    .split(',').map(s => s.trim()).filter(Boolean);
const OTPS = (__ENV.OTPS || '000000')
    .split(',').map(s => s.trim()).filter(Boolean);

// ============== METRICS ==============
const failed = new Rate('failed');
const statusCount = new Counter('status_count');

// ============== HELPERS ==============
function pick(arr){ return arr[Math.floor(Math.random() * arr.length)]; }

// ============== TEST OPTIONS (opsional) ==============
// export const options = {
//   stages: [
//     { duration: '20s', target: 5 },
//     { duration: '40s', target: 20 },
//     { duration: '20s', target: 0 },
//   ],
//   thresholds: {
//     failed: ['rate<0.05'],
//     http_req_failed: ['rate<0.05'],
//     http_req_duration: ['p(95)<200'],
//   },
// };

// ============== SCENARIO ==============
export default function () {
    const identifier = pick(IDENTIFIERS);
    const otp = pick(OTPS);

    const url = `${HOST}${VERIFY_PATH}`;
    const payload = JSON.stringify({ identifier, otp, purpose: PURPOSE });

    const res = http.post(url, payload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '60s',
        tags: { endpoint: VERIFY_PATH }
    });

    statusCount.add(1, { status: String(res.status) });

    const ok = check(res, { 'status 2xx': r => r.status >= 200 && r.status < 300 });
    if (!ok) {
        failed.add(1);
        if (LOG_FAIL) console.error(`FAIL ${res.status} id=${identifier} otp=${otp} body=${res.body}`);
    }

    sleep(THINK_MS / 1000);
}