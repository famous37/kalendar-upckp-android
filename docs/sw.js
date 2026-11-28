const CACHE = 'upckp-html-v3.0.1';
const FILES = ['./', './index.html', './site.webmanifest', './icon.png'];
self.addEventListener('install', event => { event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(FILES)).then(() => self.skipWaiting())); });
self.addEventListener('activate', event => { event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE).map(key => caches.delete(key)))).then(() => self.clients.claim())); });
self.addEventListener('fetch', event => { if(event.request.method !== 'GET') return; const req = event.request; event.respondWith(caches.match(req).then(hit => hit || fetch(req))); });
