import { Hono } from 'hono'
import { serveStatic } from 'hono/cloudflare-workers'

const app = new Hono()

// Serve static assets (css/js) from public/static
app.use('/static/*', serveStatic({ root: './public' }))

// Inline SVG favicon (calculator on blue)
app.get('/favicon.ico', (c) => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32"><rect width="32" height="32" rx="7" fill="#2563EB"/><text x="16" y="22" font-size="17" text-anchor="middle" fill="#fff" font-family="Arial">₩</text></svg>`
  return c.body(svg, 200, { 'Content-Type': 'image/svg+xml' })
})

// SPA shell
app.get('/', (c) => {
  return c.html(`<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>사전원가 시뮬레이션 웹 시스템</title>
  <link rel="preconnect" href="https://cdn.jsdelivr.net">
  <link href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.4.0/css/all.min.css" rel="stylesheet">
  <link href="/static/app.css" rel="stylesheet">
</head>
<body>
  <div id="root"></div>
  <script src="/static/calc.js"></script>
  <script src="/static/data.js"></script>
  <script src="/static/power-data.js"></script>
  <script src="/static/power-calc.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
  <script src="/static/app.js"></script>
</body>
</html>`)
})

export default app
