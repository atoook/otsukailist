export default {
  async fetch(request, env, ctx) {
    // Try to serve the requested asset first
    let response = await env.ASSETS.fetch(request);

    if (response && response.status !== 404) {
      return response;
    }

    // Fallback to index.html for SPA routes
    const url = new URL(request.url);
    const indexRequest = new Request(new URL('/index.html', url.origin), request);
    response = await env.ASSETS.fetch(indexRequest);

    return response;
  }
};
