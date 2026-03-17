/**
 * Resolves image paths relative to the Vite base URL.
 * Images in /public are served under the base path at runtime.
 * Config JSON paths like "resources/assets/images/bank.png" must be prefixed.
 */
const BASE = import.meta.env.BASE_URL.replace(/\/$/, '');

export function resolveImageUrl(path: string): string {
    if (!path) return path;
    // Already an absolute URL or data URI — leave it alone
    if (path.startsWith('http') || path.startsWith('data:')) return path;
    // Already has the base prefix — avoid double-prefixing
    if (path.startsWith(BASE)) return path;
    // Normalize: strip leading slash from path, then prepend base
    return `${BASE}/${path.replace(/^\//, '')}`;
}
