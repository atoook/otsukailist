type WorkerEnv = {
  ASSETS: {
    fetch: (request: Request) => Promise<Response>;
  };
  API_BASE_URL?: string;
  WARMUP_URL?: string;
};

function resolveWarmupUrl(env: WorkerEnv): string | null {
  if (env.WARMUP_URL) {
    return env.WARMUP_URL;
  }

  if (!env.API_BASE_URL) {
    return null;
  }

  // API_BASE_URL usually ends with /api. Warm up backend health endpoint directly.
  return env.API_BASE_URL.replace(/\/api\/?$/, '') + '/actuator/health';
}

function fetchSpaShell(request: Request, env: WorkerEnv): Promise<Response> {
  const shellUrl = new URL(request.url);
  shellUrl.pathname = '/';
  shellUrl.search = '';
  return env.ASSETS.fetch(new Request(shellUrl.toString(), request));
}

function isSpaNavigationRequest(request: Request, url: URL): boolean {
  if (request.method !== 'GET') {
    return false;
  }

  if (url.pathname.startsWith('/api')) {
    return false;
  }

  // Requests with file extensions are treated as static asset requests.
  const hasFileExtension = /\.[a-zA-Z0-9]+$/.test(url.pathname);
  if (hasFileExtension) {
    return false;
  }

  const accept = request.headers.get('Accept') ?? '';
  return accept.includes('text/html');
}

function canFallbackToSpaShell(request: Request, url: URL): boolean {
  if (request.method !== 'GET') {
    return false;
  }

  if (url.pathname.startsWith('/api')) {
    return false;
  }

  return !/\.[a-zA-Z0-9]+$/.test(url.pathname);
}

export default {
  async fetch(request: Request, env: WorkerEnv) {
    const url = new URL(request.url);

    if (url.pathname.startsWith('/api')) {
      if (!env.API_BASE_URL) {
        return new Response('API_BASE_URL is not configured', { status: 500 });
      }
      const apiUrl = new URL(url.pathname + url.search, env.API_BASE_URL);
      const apiRequest = new Request(apiUrl.toString(), request);
      return fetch(apiRequest);
    }

    if (isSpaNavigationRequest(request, url)) {
      return fetchSpaShell(request, env);
    }

    const assetResponse = await env.ASSETS.fetch(request);
    if (assetResponse.status !== 404) {
      return assetResponse;
    }

    if (canFallbackToSpaShell(request, url)) {
      return fetchSpaShell(request, env);
    }

    return assetResponse;
  },

  async scheduled(_event: unknown, env: WorkerEnv): Promise<void> {
    const warmupUrl = resolveWarmupUrl(env);
    if (!warmupUrl) {
      return;
    }

    try {
      await fetch(warmupUrl, {
        method: 'GET',
        headers: {
          'User-Agent': 'otsukailist-warmup/1.0',
          Accept: 'application/json, text/plain;q=0.9,*/*;q=0.8'
        }
      });
    } catch {
      // Ignore warm-up errors: scheduled ping is best-effort only.
    }
  }
};
