export function ifMatchHeaders(version: number) {
  return {
    headers: {
      'If-Match': `"${version}"`
    }
  };
}
