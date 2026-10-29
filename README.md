# MEJP — Rebuilt Web/PWA Prototype

**Live URL:** https://mejp.fscapitalmanagement.workers.dev/

This is an **independent, unofficial prototype**, made by studying the module structure of a user-supplied MyJPJ 5.1.2 XAPK. It is **not** the exact original Flutter project, a JPJ-certified application, or a replacement for the official MyJPJ Android app.

## Features available

- Installable mobile-first progressive web app (PWA), responsive desktop view and offline cache.
- Dashboard, licence preview, vehicles/e-LKM workspace, summons and KEJARA integration status, e-VOC status, directory link, e-Aduan notes and profile/settings.
- Add/remove personal vehicle entries; write/remove complaint drafts; customise display name and theme; export/reset browser-local data.
- Dedicated clearly labelled nonofficial demo UI throughout. All data stays in your browser; no NRIC, official credentials or payments are requested.
- Cloudflare Worker edge deployment with strict path routing, security headers, and health endpoint.

## Important limitation

The original XAPK includes compiled Flutter AOT code and resources, **not source files or JPJ backend credentials/servers**. The official MyJPJ login, real licence data, summons verification, road-tax renewals, payments, QR verification and government back-office transactions cannot be reimplemented without authorized official integration. This prototype does not impersonate these functions or show fake verification results.

## Development

Edit `web/index.html`, `web/sw.js`, `web/manifest.webmanifest`, or `web/icon.svg`.

```bash
npm run build
npx wrangler dev
npx wrangler deploy
```

`npm run build` generates `src/index.js` using `scripts/build.mjs`, which embeds the static files into a single Cloudflare Worker. Deployment config in `wrangler.jsonc`. `GET /api/health` returns app mode and status.

## Original binary research

See [verified original MyJPJ APK inspection](docs/VERIFIED_FINDINGS.md) and the [original extractor](scripts/extract_original.py). The original APK, proprietary assets and credentials are not committed to this public repository.

MEJP is unaffiliated with Jabatan Pengangkutan Jalan Malaysia. Official site: https://www.jpj.gov.my/.
