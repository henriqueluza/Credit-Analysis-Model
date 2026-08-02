import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@creditanalysis.local';
const ADMIN_SENHA = __ENV.ADMIN_SENHA;

if (!ADMIN_SENHA) {
  throw new Error('Defina a variável de ambiente ADMIN_SENHA antes de rodar o teste (ex: k6 run -e ADMIN_SENHA=... analise-credito.js)');
}

const analiseDuration = new Trend('analise_duration', true);
const historicoDuration = new Trend('historico_duration', true);
const statsDuration = new Trend('stats_duration', true);

export const options = {
  scenarios: {
    analise_credito: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '15s', target: 0 },
      ],
      exec: 'fluxoAnaliseCredito',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    // POST /api/analises inclui a chamada síncrona Spring -> FastAPI (inferência do modelo).
    analise_duration: ['p(95)<2000', 'p(99)<3000'],
    historico_duration: ['p(95)<500', 'p(99)<1000'],
    stats_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: ADMIN_EMAIL, senha: ADMIN_SENHA }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  check(loginRes, { 'login retorna 200': (r) => r.status === 200 });

  return { token: loginRes.json('token') };
}

export function fluxoAnaliseCredito(data) {
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.token}`,
  };

  const payload = JSON.stringify({
    idade: 20 + Math.floor(Math.random() * 40),
    salarioAnual: 30000 + Math.random() * 100000,
    situacaoMoradia: ['OWN', 'RENT', 'FREE'][Math.floor(Math.random() * 3)],
    saldoContaCorrente: Math.random() * 10000,
    saldoContaPoupanca: Math.random() * 20000,
    valorEmprestimo: 1000 + Math.random() * 50000,
    prazoMeses: 6 + Math.floor(Math.random() * 60),
  });

  const analiseRes = http.post(`${BASE_URL}/api/analises`, payload, {
    headers,
    tags: { name: 'analise' },
  });
  analiseDuration.add(analiseRes.timings.duration);
  check(analiseRes, {
    'analise: status 201': (r) => r.status === 201,
    'analise: tem resultado': (r) => ['APROVADO', 'REPROVADO'].includes(r.json('resultado')),
  });

  const historicoRes = http.get(`${BASE_URL}/api/analises?page=0&size=10`, {
    headers,
    tags: { name: 'historico' },
  });
  historicoDuration.add(historicoRes.timings.duration);
  check(historicoRes, { 'historico: status 200': (r) => r.status === 200 });

  const statsRes = http.get(`${BASE_URL}/api/analises/stats`, {
    headers,
    tags: { name: 'stats' },
  });
  statsDuration.add(statsRes.timings.duration);
  check(statsRes, { 'stats: status 200': (r) => r.status === 200 });

  sleep(1);
}
