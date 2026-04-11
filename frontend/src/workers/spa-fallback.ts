type WorkerEnv = {
  ASSETS: {
    fetch: (request: Request) => Promise<Response>;
  };
  API_BASE_URL?: string;
};

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
      const fallbackUrl = new URL(request.url);
      fallbackUrl.pathname = '/index.html';
      fallbackUrl.search = '';
      return env.ASSETS.fetch(new Request(fallbackUrl.toString(), request));
    }

    const assetResponse = await env.ASSETS.fetch(request);
    if (assetResponse.status !== 404) {
      return assetResponse;
    }

    const fallbackUrl = new URL(request.url);
    fallbackUrl.pathname = '/index.html';
    fallbackUrl.search = '';
    return env.ASSETS.fetch(new Request(fallbackUrl.toString(), request));
  }
};
