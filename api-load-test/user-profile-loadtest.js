import http from 'k6/http';
import { sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.4/index.js';

// ================= ENV VARS =================
const HOST = __ENV.HOST || "http://localhost:18080";
const PATH = __ENV.TARGET_PATH || "/api/v1/user/profile";
const TOKEN = __ENV.TOKEN || "";
const THINK_MS = Number(__ENV.THINK_MS || 200);

// ================= OPTIONS ==================
export const options = {
    vus: Number(__ENV.VUS || 5),           // default 5 user
    duration: __ENV.DURATION || "10s",     // default 10 detik
};

// ================= TEST =====================
export default function () {
    const url = `${HOST}${PATH}`;

    http.get(url, {
        headers: {
            Authorization: `Bearer ${TOKEN}`,
        },
        timeout: '30s',
    });

    sleep(THINK_MS / 1000);
}

// ================= HTML SUMMARY =================
export function handleSummary(data) {

    const dur = data.metrics.http_req_duration?.values || {};
    const failRate = data.metrics.http_req_failed?.values?.rate || 0;

    const html = `
<!doctype html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>User Profile Load Test Report</title>
  <style>
    body { font-family: Arial, sans-serif; padding: 20px; background: #f9f9f9; }
    h1 { margin-bottom: 0; }
    .card { background: #fff; padding: 18px 22px; border-radius: 8px; margin-top: 18px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
    table { width: 100%; border-collapse: collapse; margin-top: 10px; }
    th, td { border: 1px solid #ddd; padding: 8px; }
    th { background: #eee; }
  </style>
</head>
<body>
  <h1>User Profile Load Test Report</h1>
  <p><strong>Endpoint:</strong> ${HOST}${PATH}</p>

  <div class="card">
    <h2>Summary</h2>
    <table>
      <tr><th>Total Requests</th><td>${data.metrics.http_reqs.values.count}</td></tr>
      <tr><th>Failed Rate</th><td>${(failRate * 100).toFixed(2)} %</td></tr>
      <tr><th>p50</th><td>${dur['p(50)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
      <tr><th>p95</th><td>${dur['p(95)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
      <tr><th>p99</th><td>${dur['p(99)']?.toFixed?.(2) || 'n/a'} ms</td></tr>
      <tr><th>Avg Duration</th><td>${dur['avg']?.toFixed?.(2) || 'n/a'} ms</td></tr>
    </table>
  </div>

  <div class="card">
    <h2>Config</h2>
    <table>
      <tr><th>VUs</th><td>${options.vus}</td></tr>
      <tr><th>Duration</th><td>${options.duration}</td></tr>
      <tr><th>Think Time</th><td>${THINK_MS} ms</td></tr>
    </table>
  </div>
</body>
</html>
`;

    return {
        stdout: textSummary(data, { indent: " ", enableColors: true }),
        "user-profile-summary.html": html,
    };
}
