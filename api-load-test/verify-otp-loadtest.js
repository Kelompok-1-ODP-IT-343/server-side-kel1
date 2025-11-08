import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const options = {
    vus: Number(__ENV.VUS || 5),          // bisa diubah via env
    duration: __ENV.DURATION || '30s',    // atau pakai --iterations (lihat contoh di bawah)
};

const HOST         = (__ENV.HOST || 'http://localhost:18080').replace(/\/+$/,'');
const VERIFY_PATH  = __ENV.VERIFY_PATH || '/api/v1/auth/verify-otp';
const PURPOSE      = __ENV.PURPOSE || 'login';
const THINK_MS     = Number(__ENV.THINK_MS || 200);

// daftar user & OTP (statis untuk env test)
const IDENTIFIERS  = (__ENV.IDENTIFIERS || 'testingiano')
    .split(',').map(s => s.trim()).filter(Boolean);
const OTPS         = (__ENV.OTPS || '000000')
    .split(',').map(s => s.trim()).filter(Boolean);

const failed = new Rate('failed');

function pick(arr){ return arr[Math.floor(Math.random() * arr.length)]; }

export default function () {
    const identifier = pick(IDENTIFIERS);
    const otp = pick(OTPS); // OTP statis (atau isi dari data dummy-mu)

    const payload = JSON.stringify({ identifier, otp, purpose: PURPOSE });
    const res = http.post(`${HOST}${VERIFY_PATH}`, payload, {
        headers: { 'Content-Type': 'application/json' },
        tags: { endpoint: VERIFY_PATH }
    });

    const ok = check(res, { 'status 2xx': r => r.status >= 200 && r.status < 300 });
    if (!ok) {
        failed.add(1);
        if (String(__ENV.LOG_FAIL || 'false').toLowerCase() === 'true') {
            console.error(`FAIL ${res.status} id=${identifier} otp=${otp} body=${res.body}`);
        }
    }

    sleep(THINK_MS / 1000);
}
