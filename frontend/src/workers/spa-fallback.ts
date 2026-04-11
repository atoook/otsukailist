const STATIC_PATH_PREFIXES = ['/assets/', '/favicon', '/manifest', '/robots.txt'];

type WorkerEnv = {
  ASSETS: {
    fetch: (request: Request) => Promise<Response>;
  };
  API_BASE_URL?: string;
};

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

    // Serve actual static files (assets/index.html など)
    if (
      url.pathname === '/' ||
      url.pathname === '/index.html' ||
      STATIC_PATH_PREFIXES.some((prefix) => url.pathname.startsWith(prefix))
    ) {
      return env.ASSETS.fetch(request);
    }

    // SPA fallback: URL を変えずに index.html を返す
    const indexResponse = await env.ASSETS.fetch(
      new Request(new URL('/index.html', url.origin), request)
    );
    return new Response(indexResponse.body, {
      status: 200,
      headers: indexResponse.headers
    });
  }
};
