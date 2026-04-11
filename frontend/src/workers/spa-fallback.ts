type WorkerEnv = {
  ASSETS: {
    fetch: (request: Request) => Promise<Response>;
  };
};

export default {
  async fetch(request: Request, env: WorkerEnv) {
    const url = new URL(request.url);

    // Try asset first
    const assetResponse = await env.ASSETS.fetch(request);
    if (assetResponse && assetResponse.status !== 404) {
      return assetResponse;
    }

    // SPA fallback
    const indexRequest = new Request(new URL('/index.html', url.origin), request);
    return env.ASSETS.fetch(indexRequest);
  }
};
