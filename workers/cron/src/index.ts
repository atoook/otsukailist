type CronEnv = {
  /** Backend base URL (e.g. https://api.example.com). Used to derive the warmup target. */
  API_BASE_URL?: string;
  /**
   * Explicit warmup URL. Takes precedence over API_BASE_URL.
   * Use this to override the default /actuator/health derivation.
   */
  WARMUP_URL?: string;
};

const WARMUP_TIMEOUT_MS = 10_000;

function resolveWarmupUrl(env: CronEnv): string | null {
  if (env.WARMUP_URL) {
    return env.WARMUP_URL;
  }

  if (!env.API_BASE_URL) {
    return null;
  }

  try {
    const url = new URL(env.API_BASE_URL);
    const pathSegments = url.pathname.split("/").filter(Boolean);

    // API_BASE_URL usually ends with /api. Warm up backend health endpoint directly.
    if (pathSegments[pathSegments.length - 1] === "api") {
      pathSegments.pop();
    }

    url.pathname = `/${[...pathSegments, "actuator", "health"].join("/")}`;
    url.search = "";
    url.hash = "";
    return url.toString();
  } catch {
    return null;
  }
}

export default {
  async scheduled(
    _controller: ScheduledController,
    env: CronEnv,
  ): Promise<void> {
    const warmupUrl = resolveWarmupUrl(env);
    if (!warmupUrl) {
      console.warn(
        "Warmup skipped: neither WARMUP_URL nor API_BASE_URL is configured.",
      );
      return;
    }

    const abort = new AbortController();
    const timeoutId = setTimeout(() => abort.abort(), WARMUP_TIMEOUT_MS);

    try {
      const res = await fetch(warmupUrl, {
        method: "GET",
        headers: {
          "User-Agent": "otsukailist-warmup/1.0",
          Accept: "application/json, text/plain;q=0.9,*/*;q=0.8",
        },
        signal: abort.signal,
      });

      if (!res.ok) {
        const body = await res.text();
        console.error(
          `Warmup failed: ${warmupUrl} -> ${res.status}${body ? ` body=${body}` : ""}`,
        );
        return;
      }

      console.log(`Warmup OK: ${warmupUrl} -> ${res.status}`);
    } catch (err) {
      if (err instanceof Error && err.name === "AbortError") {
        console.error(
          `Warmup timed out after ${WARMUP_TIMEOUT_MS}ms: ${warmupUrl}`,
        );
      } else {
        // Scheduled ping is best-effort only; log but do not rethrow.
        console.error("Warmup failed:", err);
      }
    } finally {
      clearTimeout(timeoutId);
    }
  },
} satisfies ExportedHandler<CronEnv>;
