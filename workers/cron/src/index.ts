type CronEnv = {
  /** Backend base URL (e.g. https://api.example.com). Used to derive the warmup target. */
  API_BASE_URL?: string;
  /**
   * Explicit warmup URL. Takes precedence over API_BASE_URL.
   * Use this to override the default /actuator/health derivation.
   */
  WARMUP_URL?: string;
};

function resolveWarmupUrl(env: CronEnv): string | null {
  if (env.WARMUP_URL) {
    return env.WARMUP_URL;
  }

  if (!env.API_BASE_URL) {
    return null;
  }

  // API_BASE_URL usually ends with /api. Warm up backend health endpoint directly.
  return env.API_BASE_URL.replace(/\/api\/?$/, "") + "/actuator/health";
}

export default {
  async scheduled(_event: ScheduledEvent, env: CronEnv): Promise<void> {
    const warmupUrl = resolveWarmupUrl(env);
    if (!warmupUrl) {
      console.warn(
        "Warmup skipped: neither WARMUP_URL nor API_BASE_URL is configured.",
      );
      return;
    }

    try {
      const res = await fetch(warmupUrl, {
        method: "GET",
        headers: {
          "User-Agent": "otsukailist-warmup/1.0",
          Accept: "application/json, text/plain;q=0.9,*/*;q=0.8",
        },
      });
      console.log(`Warmup OK: ${warmupUrl} -> ${res.status}`);
    } catch (err) {
      // Scheduled ping is best-effort only; log but do not rethrow.
      console.error("Warmup failed:", err);
    }
  },
} satisfies ExportedHandler<CronEnv>;
