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
const LOG_BODY_MAX_LENGTH = 200;

type WarmupUrlResult =
  | { ok: true; url: string }
  | { ok: false; reason: "not_configured" | "invalid_url" };

function resolveWarmupUrl(env: CronEnv): WarmupUrlResult {
  if (env.WARMUP_URL) {
    return { ok: true, url: env.WARMUP_URL };
  }

  if (!env.API_BASE_URL) {
    return { ok: false, reason: "not_configured" };
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
    return { ok: true, url: url.toString() };
  } catch {
    return { ok: false, reason: "invalid_url" };
  }
}

export default {
  async scheduled(
    _controller: ScheduledController,
    env: CronEnv,
  ): Promise<void> {
    const result = resolveWarmupUrl(env);
    if (!result.ok) {
      if (result.reason === "invalid_url") {
        console.error(
          `Warmup skipped: API_BASE_URL is set but could not be parsed as a valid URL.`,
        );
      } else {
        console.warn(
          "Warmup skipped: neither WARMUP_URL nor API_BASE_URL is configured.",
        );
      }
      return;
    }
    const warmupUrl = result.url;

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
        const rawBody = await res.text();
        const body =
          rawBody.length > LOG_BODY_MAX_LENGTH
            ? rawBody.slice(0, LOG_BODY_MAX_LENGTH) + "..."
            : rawBody;
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
