// Compile source HTML into the single-file Cloudflare Worker in src/index.js.
import { readFileSync, writeFileSync } from "node:fs";
const html=readFileSync("web/index.html","utf8");
const sw=readFileSync("web/sw.js","utf8");
const manifest=readFileSync("web/manifest.webmanifest","utf8");
const icon=readFileSync("web/icon.svg","utf8");
const worker=[
  "const HTML="+JSON.stringify(html)+";",
  "const SW="+JSON.stringify(sw)+";",
  "const MANIFEST="+JSON.stringify(manifest)+";",
  "const ICON="+JSON.stringify(icon)+";",
  'export default {async fetch(request){const p=new URL(request.url).pathname;let body,type;switch(p){case "/":case "/index.html":body=HTML;type="text/html; charset=utf-8";break;case "/sw.js":body=SW;type="application/javascript; charset=utf-8";break;case "/manifest.webmanifest":body=MANIFEST;type="application/manifest+json; charset=utf-8";break;case "/icon.svg":body=ICON;type="image/svg+xml";break;case "/api/health":return Response.json({ok:true,product:"MEJP",type:"unofficial-prototype",jpJIntegration:false,storage:"client-only",version:"0.1.0"},{headers:{"Cache-Control":"no-store","Access-Control-Allow-Origin":"*"}});default:return new Response("Not Found",{status:404,headers:{"X-Content-Type-Options":"nosniff"}})}return new Response(body,{headers:{"Content-Type":type,"X-Content-Type-Options":"nosniff","Referrer-Policy":"no-referrer","X-Frame-Options":"DENY","Permissions-Policy":"camera=(), microphone=(), geolocation=()","Cache-Control":p==="/sw.js"?"no-cache":"public, max-age=300","Content-Security-Policy":"default-src \'self\'; connect-src \'self\'; img-src \'self\' data:; script-src \'self\' \'unsafe-inline\'; style-src \'self\' \'unsafe-inline\'; base-uri \'none\'; form-action \'self\'; frame-ancestors \'none\'"}})}};'
].join("\n");
writeFileSync("src/index.js",worker);
console.log("Worker rebuilt, bytes:",Buffer.byteLength(worker));
