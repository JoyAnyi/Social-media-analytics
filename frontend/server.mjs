import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { createServer } from 'node:http';
import { extname, join, normalize, resolve } from 'node:path';

const port = Number.parseInt(process.env.PORT ?? '8080', 10);
const root = resolve('dist');

const contentTypes = new Map([
  ['.css', 'text/css; charset=utf-8'],
  ['.html', 'text/html; charset=utf-8'],
  ['.ico', 'image/x-icon'],
  ['.js', 'text/javascript; charset=utf-8'],
  ['.json', 'application/json; charset=utf-8'],
  ['.map', 'application/json; charset=utf-8'],
  ['.png', 'image/png'],
  ['.svg', 'image/svg+xml'],
  ['.txt', 'text/plain; charset=utf-8'],
  ['.webp', 'image/webp'],
]);

const securityHeaders = {
  'Permissions-Policy': 'geolocation=(), microphone=(), camera=()',
  'Referrer-Policy': 'no-referrer',
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
};

createServer(async (request, response) => {
  try {
    if (!request.url || request.method !== 'GET') {
      send(response, 405, 'Method Not Allowed', 'text/plain; charset=utf-8');
      return;
    }

    const url = new URL(request.url, `http://${request.headers.host ?? 'localhost'}`);
    const pathname = decodeURIComponent(url.pathname);
    const candidate = safePath(pathname);
    const file = await existingFile(candidate);
    streamFile(response, file);
  } catch {
    streamFile(response, join(root, 'index.html'));
  }
}).listen(port, '0.0.0.0');

function safePath(pathname) {
  const clean = normalize(pathname).replace(/^(\.\.[/\\])+/, '');
  const target = resolve(root, clean === '/' ? 'index.html' : clean.slice(1));
  if (!target.startsWith(root)) {
    return join(root, 'index.html');
  }
  return target;
}

async function existingFile(candidate) {
  const metadata = await stat(candidate);
  if (metadata.isFile()) {
    return candidate;
  }
  return join(root, 'index.html');
}

function streamFile(response, file) {
  response.writeHead(200, {
    ...securityHeaders,
    'Cache-Control': file.includes('/assets/') ? 'public, max-age=31536000, immutable' : 'no-store',
    'Content-Type': contentTypes.get(extname(file)) ?? 'application/octet-stream',
  });
  createReadStream(file).pipe(response);
}

function send(response, status, body, contentType) {
  response.writeHead(status, {
    ...securityHeaders,
    'Content-Type': contentType,
  });
  response.end(body);
}
